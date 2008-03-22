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
package ubic.erminej.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.CancellationException;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GeneAnnotations;
import cern.jet.math.Arithmetic;
import ubic.erminej.Settings;

/**
 * Parse and store probe->pvalue associations.
 * <p>
 * The values are stored in a Map probeToPvalMap. This is used to see what probes are int the data set, as well as the
 * score for each probe.
 * </p>
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneScores {
    private static final double SMALL = 10e-16;
    protected static final Log log = LogFactory.getLog( GeneScores.class );
    private Map<String, Double> geneToPvalMap;
    private int numScores;
    private List<String> probeIDs = null;
    private String[] probeIDsArray;
    private List<Double> probePvalues = null;
    private double[] probePvaluesArray;
    private Map<String, Double> probeToScoreMap;
    private Settings settings;
    double[] geneScores;
    private GeneAnnotations geneAnnots;

    /**
     * @param is - input stream
     * @param settings
     * @param messenger
     * @param geneAnnotations
     * @throws IOException
     */
    public GeneScores( InputStream is, Settings settings, StatusViewer messenger, GeneAnnotations geneAnnotations )
            throws IOException {
        this.geneAnnots = geneAnnotations;
        this.init();
        this.settings = settings;
        read( is, messenger );
    }

    /**
     * Constructor designed for use when input is not a file.
     * 
     * @param probes List of Strings.
     * @param scores List of java.lang.Doubles containing the scores for each probe.
     * @param geneToProbeMap, usually obtained from a GeneAnnotation object (a Map of String keys with Collection
     *        values)
     * @param probeToGeneMap, usually obtained from a GeneAnnotation object (a Map of String keys with Collection
     *        values)
     * @param settings
     */
    public GeneScores( List probes, List scores, Map geneToProbeMap, Map probeToGeneMap, Settings settings ) {
        if ( probes.size() != scores.size() ) {
            throw new IllegalArgumentException( "Probe and scores must be equal in number" );
        }
        if ( probes.size() == 0 ) {
            throw new IllegalArgumentException( "No probes" );
        }

        if ( !( probes.get( 0 ) instanceof String ) ) {
            throw new IllegalArgumentException( "Probes must be a list of Strings" );
        }
        if ( !( scores.get( 0 ) instanceof Double ) ) {
            throw new IllegalArgumentException( "Scores must be a list of Doubles" );
        }
        if ( geneToProbeMap == null || geneToProbeMap.size() == 0 ) {
            throw new IllegalStateException( "groupToProbeMap was not set." );
        }

        if ( probeToGeneMap == null ) {
            throw new IllegalStateException( "probeToPvalMap was not set." );
        }

        if ( geneToProbeMap.size() == 0 ) {
            throw new IllegalStateException( "Group to probe map was empty" );
        }

        this.settings = settings;
        this.init();
        boolean invalidLog = false;
        boolean invalidNumber = false;
        String badNumberString = "";
        int numProbesKept = 0;
        int numRepeatedProbes = 0;
        Collection<String> unknownProbes = new HashSet<String>();
        for ( int i = 0; i < probes.size(); i++ ) {
            String probe = ( String ) probes.get( i );
            Double value = ( Double ) scores.get( i );

            // only keep probes that are in our array platform.
            if ( !probeToGeneMap.containsKey( probe ) ) {
                unknownProbes.add( probe );
                continue;
            }

            if ( probe.matches( "AFFX.*" ) ) { // FIXME: put this rule somewhere else // todo use a filter.
                continue;
            }

            double pValue = value.doubleValue();

            // Fudge when pvalues are zero.
            if ( settings.getDoLog() && pValue <= 0.0 ) {
                invalidLog = true;
                pValue = SMALL;
            }

            if ( settings.getDoLog() ) {
                pValue = -Arithmetic.log10( pValue );
            }

            /* we're done... */
            numProbesKept++;
            probeIDs.add( probe );
            probePvalues.add( new Double( pValue ) );
            if ( probeToScoreMap.containsKey( probe ) ) {
                log.warn( "Repeated identifier: " + probe + ", keeping original value." );
                numRepeatedProbes++;
            } else {
                probeToScoreMap.put( probe, new Double( pValue ) );
            }
            numScores++;
        }
        setUpRawArrays();
        reportProblems( null, invalidLog, unknownProbes, invalidNumber, badNumberString, numProbesKept,
                numRepeatedProbes );
        setUpGeneToScoreMap( settings, geneToProbeMap, null );
    }

    /**
     * @throws IOException
     * @param filename
     * @param settings
     * @param StatusViewer messenger
     * @param geneToProbeMap
     * @param probeToGeneMap
     */
    public GeneScores( String filename, Settings settings, StatusViewer messenger, GeneAnnotations geneAnnots )
            throws IOException, IllegalStateException {
        if ( StringUtils.isBlank( filename ) ) {
            throw new IllegalArgumentException( "Filename for gene scores can't be blank" );
        }
        this.geneAnnots = geneAnnots;
        this.settings = settings;
        this.init();
        FileTools.checkPathIsReadableFile( filename );
        InputStream is = new FileInputStream( filename );
        read( is, messenger );
        is.close();
    }

    public double[] getGeneScores() {
        return this.geneScores;
    }

    /**
     * @return
     */
    public Map<String, Double> getGeneToPvalMap() {
        return geneToPvalMap;
    }

    /**
     * @param shuffle Whether the map should be scrambled first. If so, then groups are randomly associated with scores,
     *        but the actual values are the same. This is used for resampling multiple test correction.
     * @return Map of groups of genes to pvalues.
     */
    public Map<String, Double> getGeneToPvalMap( boolean shuffle ) {
        if ( shuffle ) {
            Map<String, Double> scrambled_map = new LinkedHashMap<String, Double>();
            Set<String> keys = geneToPvalMap.keySet();
            Iterator<String> it = keys.iterator();

            Collection<Double> values = geneToPvalMap.values();
            List<Double> valvec = new Vector<Double>( values );
            Collections.shuffle( valvec );

            // randomly associate keys and values
            int i = 0;
            while ( it.hasNext() ) {
                scrambled_map.put( it.next(), valvec.get( i ) );
                i++;
            }
            return scrambled_map;

        }
        return geneToPvalMap;

    }

    /**
     */
    public int getNumGeneScores() {
        return numScores;
    }

    /**
     */
    public String[] getProbeIds() {
        return probeIDsArray;
    }

    /**
     */
    public Map<String, Double> getProbeToScoreMap() {
        return probeToScoreMap;
    }

    /**
     */
    public double[] getPvalues() {
        return probePvaluesArray;
    }

    /**
     * @return Returns the settings.
     */
    public Settings getSettings() {
        return this.settings;
    }

    /**
     * @param probe_id
     * @return
     */
    public double getValueMap( String probe_id ) {
        double value = 0.0;

        if ( probeToScoreMap.get( probe_id ) != null ) {
            value = Double.parseDouble( ( probeToScoreMap.get( probe_id ) ).toString() );
        }

        return value;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for ( Iterator<String> iter = probeIDs.iterator(); iter.hasNext(); ) {
            String probe = iter.next();
            double score = probeToScoreMap.get( probe ).doubleValue();
            buf.append( probe + "\t" + score + "\n" );
        }
        return buf.toString();
    }

    private void init() {
        this.geneToPvalMap = new HashMap<String, Double>( 1000 );
        this.probeToScoreMap = new HashMap<String, Double>( 1000 );
        this.probePvalues = new ArrayList<Double>( 1000 );
        this.probeIDs = new ArrayList<String>( 1000 );
    }

    /**
     * 
     *
     */
    private void letUserReadMessage() {
        try {
            Thread.sleep( 2000 );
        } catch ( InterruptedException e ) {
            throw new CancellationException();
        }
    }

    private void read( InputStream is, StatusViewer messenger ) throws IOException, IllegalStateException {
        assert geneAnnots != null;
        int scoreCol = settings.getScoreCol();
        if ( scoreCol < 2 ) {
            throw new IllegalArgumentException( "Illegal column number " + scoreCol + ", must be greater than 1" );
        }

        if ( messenger != null ) {
            messenger.showStatus( "Reading gene scores from column " + scoreCol );
        }

        BufferedReader dis = new BufferedReader( new InputStreamReader( new BufferedInputStream( is ) ) );
        String row;
        boolean invalidLog = false;
        boolean invalidNumber = false;
        String badNumberString = "";
        int scoreColumnIndex = scoreCol - 1;
        int numProbesKept = 0;
        int numUnknownProbes = 0;
        int numRepeatedProbes = 0;
        Collection<String> unknownProbes = new HashSet<String>();
        dis.readLine(); // skip header.
        while ( ( row = dis.readLine() ) != null ) {
            String[] fields = row.split( "\t" );

            // ignore rows that have insufficient columns.
            if ( fields.length < scoreCol ) {
                continue;
            }

            String probeId = StringUtils.strip( fields[0] );

            // only keep probes that are in our array platform.
            if ( !geneAnnots.hasProbe( probeId ) ) {
                log.debug( "\"" + probeId + "\" not in the annotations, ignoring" );
                unknownProbes.add( probeId );
                numUnknownProbes++;
                continue;
            }

            if ( probeId.matches( "AFFX.*" ) ) { // FIXME: put this rule somewhere else // todo use a filter.
                if ( messenger != null ) {
                    messenger.showStatus( "Skipping probe in pval file: " + probeId );
                }
                continue;
            }

            double pValue = 0.0;
            try {
                pValue = Double.parseDouble( fields[scoreColumnIndex] );
            } catch ( NumberFormatException e ) {
                /* the first line can be a header; we ignore it if it looks bad */
                if ( probeIDs.size() > 0 ) {
                    invalidNumber = true;
                    badNumberString = fields[scoreColumnIndex];
                }
            }

            // Fudge when pvalues are zero.
            if ( settings.getDoLog() && pValue <= 0.0 ) {
                invalidLog = true;
                pValue = SMALL;

            }

            if ( settings.getDoLog() ) {
                pValue = -Arithmetic.log10( pValue );
            }

            /* we're done... */
            numProbesKept++;
            probeIDs.add( probeId );
            probePvalues.add( new Double( pValue ) );

            if ( probeToScoreMap.containsKey( probeId ) ) {
                log.warn( "Repeated identifier: " + probeId + ", keeping original value." );
                numRepeatedProbes++;
            } else {
                probeToScoreMap.put( probeId, new Double( pValue ) );
            }

            if ( Thread.currentThread().isInterrupted() ) {
                dis.close();
                throw new CancellationException();
            }

        }
        dis.close();
        numScores = probePvalues.size();
        assert numScores == probeIDs.size();

        setUpRawArrays();
        reportProblems( messenger, invalidLog, unknownProbes, invalidNumber, badNumberString, numProbesKept,
                numRepeatedProbes );

        // check if active probes and probes in the platform are the same.
        for ( Iterator iter = geneAnnots.getProbeToGeneMap().keySet().iterator(); iter.hasNext(); ) {
            String probe = ( String ) iter.next();
            if ( !probeToScoreMap.keySet().contains( probe ) ) {
                log.debug( "Activeprobes must be set - data doesn't contain " + probe );
                assert geneAnnots != null;
                geneAnnots.setActiveProbes( probeToScoreMap.keySet() );
                break;
            }
        }

        setUpGeneToScoreMap( settings, null, messenger );

    }

    /**
     * @param messenger
     * @param invalidLog
     * @param unknownProbe
     * @param invalidNumber
     * @param badNumberString
     * @param numProbesKept
     */
    private void reportProblems( StatusViewer messenger, boolean invalidLog, Collection<String> unknownProbes,
            boolean invalidNumber, String badNumberString, int numProbesKept, int numRepeatedProbes ) {
        if ( invalidNumber && messenger != null ) {

            messenger.showError( "Non-numeric gene scores(s) " + " ('" + badNumberString + "') "
                    + " found for input file. These are set to an initial value of zero." );
            letUserReadMessage();
        }
        if ( invalidLog && messenger != null ) {
            messenger
                    .showError( "Warning: There were attempts to take the log of non-positive values. These are set to "
                            + SMALL );
            letUserReadMessage();
        }
        if ( messenger != null && unknownProbes.size() > 0 ) {
            messenger.showError( "Warning: " + unknownProbes.size()
                    + " probes in your gene score file don't match the ones in the annotation file." );
            letUserReadMessage();

            int count = 0;
            StringBuffer buf = new StringBuffer();
            for ( Iterator<String> iter = unknownProbes.iterator(); iter.hasNext(); ) {
                if ( count >= 10 ) break;
                String probe = iter.next();
                buf.append( probe + "," );
                count++;
            }
            messenger.showError( "Unmatched probes are (up to 10 shown): " + buf );
            letUserReadMessage();

        }
        if ( messenger != null && numRepeatedProbes > 0 ) {
            messenger
                    .showError( "Warning: "
                            + numRepeatedProbes
                            + " identifiers in your gene score file were repeats. Only the first occurrence encountered was kept in each case." );
            letUserReadMessage();
        }

        if ( numProbesKept == 0 && messenger != null ) {
            messenger.showError( "None of the probes in the gene score file correspond to probes in the "
                    + "annotation file you selected. None of your data will be displayed." );
            letUserReadMessage();
        }

        if ( numScores == 0 && messenger != null ) {
            messenger.showError( "No probe scores found! Please check the file has"
                    + " the correct plain text format and"
                    + " corresponds to the microarray annotation (\".an\") file you selected." );
            letUserReadMessage();
        }
        if ( messenger != null ) {
            messenger.showStatus( "Found " + numScores + " scores in the file" );
        }
    }

    /**
     * Each pvalue is adjusted to the mean (or best) of all the values in the 'replicate group' to yield a "group to
     * pvalue map".
     * 
     * @param settings
     * @param geneToProbeMap - this should be generated from the annotation file.
     * @param messenger
     */
    private void setUpGeneToScoreMap( Settings settings, Map geneToProbeMap, StatusViewer messenger ) {

        int gp_method = settings.getGeneRepTreatment();

        Collection genes = null;
        if ( geneToProbeMap != null ) {
            genes = geneToProbeMap.keySet();
        } else {
            genes = geneAnnots.getGenes();
        }

        double[] geneScoreTemp = new double[genes.size()];
        int counter = 0;

        for ( Iterator groupMapItr = genes.iterator(); groupMapItr.hasNext(); ) {

            if ( Thread.currentThread().isInterrupted() ) {
                return;
            }

            String geneSymbol = ( String ) groupMapItr.next();
            /*
             * probes in this group according to the array platform.
             */
            Collection probes;

            if ( geneToProbeMap != null ) {
                probes = ( Collection ) geneToProbeMap.get( geneSymbol );
            } else {
                probes = geneAnnots.getGeneProbes( geneSymbol );
            }
            int in_size = 0;

            if ( probes == null ) continue;

            // Analyze all probes in this 'group' (pointing to the same gene)
            for ( Iterator pbItr = probes.iterator(); pbItr.hasNext(); ) {
                String probe = ( String ) pbItr.next();

                if ( !probeToScoreMap.containsKey( probe ) ) {
                    continue;
                }

                // these values are already log transformed if the user selected that option.
                double score = probeToScoreMap.get( probe ).doubleValue();

                switch ( gp_method ) {
                    case Settings.MEAN_PVAL: {
                        geneScoreTemp[counter] += score;
                        break;
                    }
                    case Settings.BEST_PVAL: {
                        if ( in_size == 0 ) {
                            // fix suggested by Hubert Rehrauer, to initialize values to first score, not zero.
                            geneScoreTemp[counter] = score;
                        } else {
                            if ( settings.upperTail() ) {
                                geneScoreTemp[counter] = Math.max( score, geneScoreTemp[counter] );
                            } else {
                                geneScoreTemp[counter] = Math.min( score, geneScoreTemp[counter] );
                            }
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException( "Illegal selection for groups score method." );
                    }
                }
                in_size++;
            }

            if ( in_size > 0 ) {
                if ( gp_method == Settings.MEAN_PVAL ) {
                    geneScoreTemp[counter] /= in_size; // take the mean
                }
                Double dbb = new Double( geneScoreTemp[counter] );
                geneToPvalMap.put( geneSymbol, dbb );
                counter++;
            }
        } // end of while

        if ( counter == 0 ) {
            // this is okay, if we're trying to show the class despite there being no results.
            log.warn( "No gene to score mappings were found." );
            return;
        }

        if ( messenger != null ) messenger.showStatus( counter + " distinct genes found in the annotations." );

        geneScores = new double[counter];
        for ( int i = 0; i < counter; i++ ) {
            geneScores[i] = geneScoreTemp[i];
        }

    }

    private void setUpRawArrays() {
        probePvaluesArray = new double[numScores];
        probeIDsArray = new String[numScores];
        for ( int i = 0; i < numScores; i++ ) {
            probePvaluesArray[i] = probePvalues.get( i ).doubleValue();
            probeIDsArray[i] = probeIDs.get( i );
        }
    }

} // end of class
