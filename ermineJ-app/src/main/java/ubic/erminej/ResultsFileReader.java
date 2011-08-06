/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import ubic.basecode.util.StatusViewer;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;

/**
 * Load results from a file
 * 
 * @author Paul Pavlidis
 * @author Homin Lee
 * @version $Id$
 */
public class ResultsFileReader {

    /**
     * Possibility that this is a project, vs. a single analysis.
     * 
     * @param geneAnnots
     * @param filename
     * @param messenger
     * @throws IOException
     */
    public static Collection<GeneSetPvalRun> load( GeneAnnotations geneAnnots, String filename, StatusViewer messenger )
            throws IOException, ConfigurationException {

        checkFile( filename );

        messenger.showStatus( "Loading analysis..." );

        Collection<GeneSetPvalRun> finalResult = new LinkedHashSet<GeneSetPvalRun>();

        BufferedReader dis = new BufferedReader( new FileReader( filename ) );

        /*
         * Until we get to the end of the file ....
         */
        int runNum = 1;
        while ( dis.ready() ) {
            GeneSetPvalRun loadedResults = readOne( dis, geneAnnots, runNum, messenger );
            if ( loadedResults != null ) finalResult.add( loadedResults );
        }
        dis.close();

        return finalResult;
    }

    /**
     * @param geneAnnots
     * @param filename
     * @param messenger
     * @return
     * @throws IOException
     * @throws ConfigurationException
     */
    public static GeneSetPvalRun loadOne( GeneAnnotations geneAnnots, String filename, StatusViewer messenger )
            throws IOException, ConfigurationException {
        BufferedReader dis = new BufferedReader( new FileReader( filename ) );
        GeneSetPvalRun loadedResults = readOne( dis, geneAnnots, 1, messenger );
        return loadedResults;
    }

    /**
     * @param filename
     * @throws IOException
     */
    private static void checkFile( String filename ) throws IOException {
        if ( StringUtils.isBlank( filename ) ) {
            throw new IllegalArgumentException( "File name was blank" );
        }

        File infile = new File( filename );
        if ( !infile.exists() || !infile.canRead() ) {
            throw new IOException( "Could not read " + filename );
        }

        if ( infile.length() == 0 ) {
            throw new IOException( "File has zero length" );
        }
    }

    /**
     * @param dis
     * @param geneAnnots
     * @param runNum
     * @param messenger
     * @return
     * @throws IOException
     * @throws ConfigurationException
     */
    private static GeneSetPvalRun readOne( BufferedReader dis, GeneAnnotations geneAnnots, int runNum,
            StatusViewer messenger ) throws IOException, ConfigurationException {
        /*
         * Load settings for the analysis.
         */
        SettingsHolder runSettings = readOneSetOfSettings( dis );

        Map<GeneSetTerm, GeneSetResult> results = new LinkedHashMap<GeneSetTerm, GeneSetResult>();

        boolean warned = false;
        String line = null;
        String runName = "";
        while ( ( line = dis.readLine() ) != null ) {
            StringTokenizer st = new StringTokenizer( line, "\t" );
            String firstword = st.nextToken();

            /*
             * Lines that start with the commons configuration comment character "!" indicate data.
             */
            if ( firstword.compareTo( "!" ) == 0 ) {
                st.nextToken(); // / class name, ignored.
                String classId = st.nextToken();
                GeneSetTerm term = geneAnnots.findTerm( classId );
                if ( term == null && !warned ) {
                    messenger.showError( "Term " + classId + " not recognized, skipping (further warnings skipped)" );
                    warned = true;
                    continue;
                }

                // we could recompute this, but better not.
                int numProbes = Integer.parseInt( st.nextToken() );
                int numGenes = Integer.parseInt( st.nextToken() );
                double score = Double.parseDouble( st.nextToken() );
                double pval = Double.parseDouble( st.nextToken() );

                // we could recompute this, but better not.
                double correctedPval = Double.parseDouble( st.nextToken() );

                GeneSetResult c = new GeneSetResult( term, numProbes, numGenes, score, pval, correctedPval );
                results.put( term, c );
            } else if ( firstword.startsWith( ResultsPrinter.RUN_NAME_FIELD_PATTERN ) ) {
                /*
                 * Special field to get run name.
                 */
                String[] kv = StringUtils.splitByWholeSeparator( firstword, "=", 2 );
                if ( kv.length > 1 ) {
                    runName = kv[1];
                } else {
                    runName = "Run " + runNum;
                    ++runNum;
                }
            } else if ( firstword.startsWith( ResultsPrinter.RUN_INDICATOR ) ) {
                // reached end of the run.
                break;
            }
        }

        if ( results.isEmpty() ) {
            messenger.showError( "Results section was empty" );
            return null;
        }

        /*
         * At this point, it is possible that we got a corrupted file, and the results are only partial, etc. Hard to
         * know.
         */

        GeneSetPvalRun newResults = new GeneSetPvalRun( runSettings, geneAnnots, messenger, results, runName );

        messenger.showStatus( "Read run: " + runName );

        return newResults;
    }

    /**
     * Genreate Settings from the file, reading from the current point to the next 'end of settings' marker.
     * 
     * @param r
     * @return
     * @throws IOException
     * @throws ConfigurationException
     */
    private static SettingsHolder readOneSetOfSettings( BufferedReader r ) throws IOException, ConfigurationException {

        File tmp = File.createTempFile( "ermineJ.", ".tmp.properties" );
        Writer w = new FileWriter( tmp );
        while ( r.ready() ) {
            String line = r.readLine();
            if ( line.startsWith( ResultsPrinter.END_OF_SETTINGS_SEPARATOR ) ) {
                break;
            }

            w.write( line + "\n" );
        }
        w.close();

        SettingsHolder s = new Settings( tmp.getAbsolutePath() ).getSettingsHolder();

        tmp.delete();

        return s;
    }

}