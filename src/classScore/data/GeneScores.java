/*
 * The ermineJ project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScore.data;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GeneAnnotations;
import baseCode.util.CancellationException;
import baseCode.util.FileTools;
import baseCode.util.StatusViewer;
import cern.jet.math.Arithmetic;
import classScore.Settings;

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
    private Map geneToPvalMap;
    private int numScores;
    private List probeIDs = null;
    private String[] probeIDsArray;
    private List probePvalues = null;
    private double[] probePvaluesArray;
    private Map probeToScoreMap;
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
        Collection unknownProbes = new HashSet();
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
            probeToScoreMap.put( probe, new Double( pValue ) );
            numScores++;
        }
        setUpRawArrays();
        reportProblems( null, invalidLog, unknownProbes, invalidNumber, badNumberString, numProbesKept );
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
    public Map getGeneToPvalMap() {
        return geneToPvalMap;
    }

    /**
     * @param shuffle Whether the map should be scrambled first. If so, then groups are randomly associated with scores,
     *        but the actual values are the same. This is used for resampling multiple test correction.
     * @return Map of groups of genes to pvalues.
     */
    public Map getGeneToPvalMap( boolean shuffle ) {
        if ( shuffle ) {
            Map scrambled_map = new LinkedHashMap();
            Set keys = geneToPvalMap.keySet();
            Iterator it = keys.iterator();

            Collection values = geneToPvalMap.values();
            List valvec = new Vector( values );
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
    public Map getProbeToScoreMap() {
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
        for ( Iterator iter = probeIDs.iterator(); iter.hasNext(); ) {
            String probe = ( String ) iter.next();
            double score = ( ( Double ) probeToScoreMap.get( probe ) ).doubleValue();
            buf.append( probe + "\t" + score + "\n" );
        }
        return buf.toString();
    }

    private void init() {
        this.geneToPvalMap = new HashMap( 1000 );
        this.probeToScoreMap = new HashMap( 1000 );
        this.probePvalues = new ArrayList( 1000 );
        this.probeIDs = new ArrayList( 1000 );
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
        Collection unknownProbes = new HashSet();
        dis.readLine(); // skip header.
        while ( ( row = dis.readLine() ) != null ) {
            String[] fields = row.split( "\t" );

            // ignore rows that have insufficient columns.
            if ( fields.length < scoreCol ) {
                continue;
            }

            String probeId = fields[0];

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
            probeToScoreMap.put( probeId, new Double( pValue ) );

            if ( Thread.currentThread().isInterrupted() ) {
                dis.close();
                throw new CancellationException();
            }

        }
        dis.close();
        numScores = probePvalues.size();
        assert numScores == probeIDs.size();

        setUpRawArrays();
        reportProblems( messenger, invalidLog, unknownProbes, invalidNumber, badNumberString, numProbesKept );
        geneAnnots.setActiveProbes( probeToScoreMap.keySet() );
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
    private void reportProblems( StatusViewer messenger, boolean invalidLog, Collection unknownProbes,
            boolean invalidNumber, String badNumberString, int numProbesKept ) {
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

            if ( unknownProbes.size() <= 5 ) {
                StringBuffer buf = new StringBuffer();
                for ( Iterator iter = unknownProbes.iterator(); iter.hasNext(); ) {
                    String probe = ( String ) iter.next();
                    buf.append( probe + "," );
                }
                messenger.showError( "Unknown probes are: " + buf );
                letUserReadMessage();
            }

        }
        if ( numProbesKept == 0 ) {
            throw new IllegalStateException( "None of the probes in the gene score file correspond to probes in the "
                    + "annotation (\".an\") file you selected." );
        }
        if ( numScores == 0 ) {
            throw new IllegalStateException( "No probe scores found! Please check the file has"
                    + " the correct plain text format and"
                    + " corresponds to the microarray annotation (\".an\") file you selected." );
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
                double pbPval = ( ( Double ) probeToScoreMap.get( probe ) ).doubleValue();

                switch ( gp_method ) {
                    case Settings.MEAN_PVAL: {
                        geneScoreTemp[counter] += pbPval;
                        break;
                    }
                    case Settings.BEST_PVAL: {
                        if ( settings.upperTail() ) {
                            geneScoreTemp[counter] = Math.max( pbPval, geneScoreTemp[counter] );
                        } else {
                            geneScoreTemp[counter] = Math.min( pbPval, geneScoreTemp[counter] );
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
            throw new IllegalStateException( "No gene to score mappings were found." );
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
            probePvaluesArray[i] = ( ( Double ) probePvalues.get( i ) ).doubleValue();
            probeIDsArray[i] = ( String ) probeIDs.get( i );
        }
    }

} // end of class
