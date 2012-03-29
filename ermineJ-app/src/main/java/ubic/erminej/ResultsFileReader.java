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

import java.awt.HeadlessException;
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

import javax.swing.JFileChooser;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.util.GuiUtil;

/**
 * Load results from a file. Files can contain more than one result set.
 * 
 * @author Paul Pavlidis
 * @author Homin Lee
 * @version $Id$
 */
public class ResultsFileReader {

    private static Log log = LogFactory.getLog( ResultsFileReader.class );

    /**
     * @param geneAnnots
     * @param filename .
     * @param messenger
     * @return
     */
    public static Collection<GeneSetPvalRun> load( GeneAnnotations geneAnnots, String filename, StatusViewer messenger )
            throws IOException, ConfigurationException {
        FileTools.testFile( filename );

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
        SettingsHolder runSettings = readOneSetOfSettings( geneAnnots, dis, messenger );

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

                // we cannot easily recompute this.
                int mfRankChange = Integer.parseInt( st.nextToken() );
                c.setMultifunctionalityCorrectedRankDelta( mfRankChange );

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
     * Generate Settings from the file, reading from the current point to the next 'end of settings' marker.
     * 
     * @param annots
     * @param r
     * @param statusViewer
     * @return
     * @throws IOException
     * @throws ConfigurationException
     */
    private static SettingsHolder readOneSetOfSettings( GeneAnnotations annots, BufferedReader r,
            StatusViewer statusViewer ) throws IOException, ConfigurationException {

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

        if ( !checkValid( s ) ) {
            GuiUtil.error( "There was a problem loading the analysis.\nFiles referred to in the analysis may have been moved or deleted." );
        }

        tmp.delete();

        return s;
    }

    /**
     * @param loadSettings
     */
    private static boolean checkValid( SettingsHolder loadSettings ) {

        String file;

        String rawDataFileName = loadSettings.getRawDataFileName();
        if ( StringUtils.isNotBlank( rawDataFileName ) ) {
            file = checkFile( loadSettings, rawDataFileName ); // looks for the file.
            if ( file == null ) {
                return false;
            }
            loadSettings.setRawFile( file );
        }

        String scoreFile = loadSettings.getScoreFile();
        if ( StringUtils.isNotBlank( scoreFile ) ) {
            file = checkFile( loadSettings, scoreFile );
            if ( file == null ) {
                return false;
            }
            loadSettings.setScoreFile( file );
        }

        return true;
    }

    /**
     * Check whether a file exists, and if not and the GUI is available, prompt the user to enter one. The path is
     * returned.
     * 
     * @param file
     * @return If the user doesn't locate the file, return null, otherwise the path to the file.
     */
    private static String checkFile( SettingsHolder settings, String file ) {
        if ( StringUtils.isBlank( file ) ) return null;

        if ( !FileTools.testFile( file ) ) {

            try {

                // try to start them somewhere useful.
                JFileChooser fc = new JFileChooser( settings.getGeneScoreFileDirectory() );

                fc.setDialogTitle( "Please locate " + new File( file ).getName() );
                fc.setDialogType( JFileChooser.OPEN_DIALOG );
                fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
                int result = fc.showOpenDialog( null );
                if ( result == JFileChooser.APPROVE_OPTION ) {
                    File f = fc.getSelectedFile();
                    return f.getAbsolutePath();
                }
            } catch ( HeadlessException e ) {
                // we must be using the CLI.
                log.error( file + " referred to in the file could not be found; please fix the file" );
                return null;
            }
            return null;
        }
        return file;
    }

}