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

import ubic.basecode.math.Rank;
import ubic.basecode.util.CancellationException;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;

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
    private Map<Gene, Double> geneToScoreMap;
    private Map<Probe, Double> probeToScoreMap;
    private Settings settings;
    final private GeneAnnotations geneAnnots;
    private StatusViewer messenger;

    /**
     * @param is - input stream
     * @param settings
     * @param messenger
     * @param geneAnnotations
     * @throws IOException
     */
    public GeneScores( InputStream is, Settings settings, StatusViewer messenger, GeneAnnotations geneAnnotations )
            throws IOException {

        if ( geneAnnotations == null ) {
            throw new IllegalArgumentException( "Annotations cannot be null" );
        }
        this.geneAnnots = geneAnnotations;
        this.init();
        this.settings = settings;
        this.messenger = messenger;
        read( is );
    }

    /**
     * Constructor designed for use when input is not a file.
     * 
     * @param probes List of Strings.
     * @param scores List of java.lang.Doubles containing the scores for each probe.
     * @param geneAnnots
     * @param settings
     */
    public GeneScores( List<String> probes, List<Double> scores, GeneAnnotations geneAnnots, Settings settings ) {

        this.geneAnnots = geneAnnots;

        if ( probes.size() != scores.size() ) {
            throw new IllegalArgumentException( "Probe and scores must be equal in number" );
        }
        if ( probes.size() == 0 ) {
            throw new IllegalArgumentException( "No probes" );
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
            String ps = probes.get( i );
            Double value = scores.get( i );

            // only keep probes that are in our array platform.
            Probe probe = geneAnnots.findProbe( ps );
            if ( probe == null ) {
                unknownProbes.add( ps );
                continue;
            }

            if ( probe.getName().matches( "AFFX.*" ) ) { // FIXME: put this rule somewhere else // todo use a filter.
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
            if ( probeToScoreMap.containsKey( probe ) ) {
                log.warn( "Repeated identifier: " + probe + ", keeping original value." );
                numRepeatedProbes++;
            } else {
                probeToScoreMap.put( probe, new Double( pValue ) );
            }
        }
        reportProblems( invalidLog, unknownProbes, invalidNumber, badNumberString, numProbesKept, numRepeatedProbes );
        setUpGeneToScoreMap();
    }

    /**
     * @param filename
     * @param settings
     * @param messenger
     * @param geneAnnots
     * @throws IOException
     */
    public GeneScores( String filename, Settings settings, StatusViewer messenger, GeneAnnotations geneAnnots )
            throws IOException {
        if ( StringUtils.isBlank( filename ) ) {
            throw new IllegalArgumentException( "Filename for gene scores can't be blank" );
        }
        this.geneAnnots = geneAnnots;
        this.messenger = messenger;
        this.settings = settings;
        this.init();
        FileTools.checkPathIsReadableFile( filename );
        InputStream is = new FileInputStream( filename );
        read( is );
        is.close();
    }

    public Double[] getGeneScores() {
        return this.geneToScoreMap.values().toArray( new Double[] {} );
    }

    /**
     * @return
     */
    public Map<Gene, Double> getGeneToScoreMap() {
        return geneToScoreMap;
    }

    /**
     * @param shuffle Whether the map should be scrambled first. If so, then groups are randomly associated with scores,
     *        but the actual values are the same. This is used for resampling multiple test correction.
     * @return Map of groups of genes to pvalues.
     */
    public Map<Gene, Double> getGeneToPvalMap( boolean shuffle ) {
        if ( shuffle ) {
            Map<Gene, Double> scrambled_map = new LinkedHashMap<Gene, Double>();
            Set<Gene> keys = geneToScoreMap.keySet();
            Iterator<Gene> it = keys.iterator();

            Collection<Double> values = geneToScoreMap.values();
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
        return geneToScoreMap;

    }

    /**
     */
    public int getNumScores() {
        return probeToScoreMap.size();
    }

    /**
     */
    public Map<Probe, Double> getProbeToScoreMap() {
        return probeToScoreMap;
    }

    /**
     * @return list of genes in order of their scores, where the first probe is the 'best'. If 'big is better', large
     *         scores will be given first.
     */
    public List<Gene> getRankedGenes() {
        Map<Gene, Integer> ranked = Rank.rankTransform( getGeneToScoreMap(), settings.getBigIsBetter() );

        List<Gene> rankedGenes = new ArrayList<Gene>( ranked.keySet() );

        for ( Gene g : ranked.keySet() ) {
            Integer r = ranked.get( g );
            rankedGenes.set( r, g );
        }

        return rankedGenes;
    }

    /**
     */
    public Double[] getScores() {
        return this.probeToScoreMap.values().toArray( new Double[] {} );
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

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for ( Probe probe : probeToScoreMap.keySet() ) {
            double score = probeToScoreMap.get( probe );
            buf.append( probe.getName() + "\t" + score + "\n" );
        }
        return buf.toString();
    }

    private void init() {
        this.geneToScoreMap = new LinkedHashMap<Gene, Double>( 1000 );
        this.probeToScoreMap = new LinkedHashMap<Probe, Double>( 1000 );
    }

    /**
     * @param is
     * @throws IOException
     * @throws IllegalStateException
     */
    private void read( InputStream is ) throws IOException, IllegalStateException {
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

            if ( probeId.matches( "AFFX.*" ) ) { // FIXME: put this rule somewhere else
                if ( messenger != null ) {
                    messenger.showStatus( "Skipping probe in pval file: " + probeId );
                }
                continue;
            }

            // only keep probes that are in our array platform.

            Probe p = geneAnnots.findProbe( probeId );

            if ( p == null ) {
                log.debug( "\"" + probeId + "\" not in the annotations, ignoring" );
                unknownProbes.add( probeId );
                numUnknownProbes++;
                continue;
            }

            double score = 0.0;
            try {
                score = Double.parseDouble( fields[scoreColumnIndex] );
            } catch ( NumberFormatException e ) {
                /* the first line can be a header; we ignore it if it looks bad */
                if ( probeToScoreMap.size() > 0 ) {
                    invalidNumber = true;
                    badNumberString = fields[scoreColumnIndex];
                }
            }

            // Fudge when pvalues are zero.
            if ( settings.getDoLog() && score <= 0.0 ) {
                invalidLog = true;
                score = SMALL;

            }

            if ( settings.getDoLog() ) {
                score = -Arithmetic.log10( score );
            }

            /* we're done... */
            numProbesKept++;

            // log.info( p + " " + score );
            if ( probeToScoreMap.containsKey( p ) ) {
                log.warn( "Repeated identifier: " + probeId + ", keeping original value." );
                numRepeatedProbes++;
            } else {
                probeToScoreMap.put( p, score );
            }

            if ( Thread.currentThread().isInterrupted() ) {
                dis.close();
                throw new CancellationException();
            }

        }
        dis.close();

        reportProblems( invalidLog, unknownProbes, invalidNumber, badNumberString, numProbesKept, numRepeatedProbes );

        setUpGeneToScoreMap();

    }

    /**
     * @param invalidLog
     * @param unknownProbe
     * @param invalidNumber
     * @param badNumberString
     * @param numProbesKept
     */
    private void reportProblems( boolean invalidLog, Collection<String> unknownProbes, boolean invalidNumber,
            String badNumberString, int numProbesKept, int numRepeatedProbes ) {
        if ( invalidNumber && messenger != null ) {

            messenger.showError( "Non-numeric gene scores(s) " + " ('" + badNumberString + "') "
                    + " found for input file. These are set to an initial value of zero." );
        }
        if ( invalidLog && messenger != null ) {
            messenger
                    .showError( "Warning: There were attempts to take the log of non-positive values. These are set to "
                            + SMALL );
        }
        if ( messenger != null && unknownProbes.size() > 0 ) {
            messenger.showError( "Warning: " + unknownProbes.size()
                    + " probes in your gene score file don't match the ones in the annotation file." );

            int count = 0;
            StringBuffer buf = new StringBuffer();
            for ( Iterator<String> iter = unknownProbes.iterator(); iter.hasNext(); ) {
                if ( count >= 10 ) break;
                String probe = iter.next();
                buf.append( probe + "," );
                count++;
            }
            messenger.showError( "Unmatched probes are (up to 10 shown): " + buf );
        }
        if ( messenger != null && numRepeatedProbes > 0 ) {
            messenger
                    .showError( "Warning: "
                            + numRepeatedProbes
                            + " identifiers in your gene score file were repeats. Only the first occurrence encountered was kept in each case." );
        }

        if ( numProbesKept == 0 && messenger != null ) {
            messenger.showError( "None of the probes in the gene score file correspond to probes in the "
                    + "annotation file you selected. None of your data will be displayed." );
        }

        if ( probeToScoreMap.isEmpty() && messenger != null ) {
            messenger.showError( "No probe scores found! Please check the file has"
                    + " the correct plain text format and"
                    + " corresponds to the gene annotation (\".an\") file you selected." );
        } else if ( messenger != null ) {
            messenger.showStatus( "Found " + probeToScoreMap.size() + " scores in the file" );
        }
    }

    /**
     * Each pvalue is adjusted to the mean (or best) of all the values in the 'replicate group' to yield a "group to
     * pvalue map".
     * 
     * @param settings
     * @param collection - this should be generated from the annotation file.
     * @param messenger
     */
    private void setUpGeneToScoreMap() {

        Settings.MultiProbeHandling gp_method = settings.getGeneRepTreatment();

        Collection<Gene> genes = geneAnnots.getGenes();

        assert genes.size() > 0;
        double[] geneScoreTemp = new double[genes.size()];
        int counter = 0;

        for ( Gene geneSymbol : genes ) {

            if ( Thread.currentThread().isInterrupted() ) {
                return;
            }

            /*
             * probes in this group according to the array platform.
             */
            Collection<Probe> probes = geneSymbol.getProbes();

            // Analyze all probes in this 'group' (pointing to the same gene)
            int in_size = 0;
            for ( Probe probe : probes ) {

                if ( !probeToScoreMap.containsKey( probe ) ) {
                    continue;
                }

                // these values are already log transformed if the user selected that option.
                double score = probeToScoreMap.get( probe );

                switch ( gp_method ) {
                    case MEAN: {
                        geneScoreTemp[counter] += score;
                        break;
                    }
                    case BEST: {
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
                if ( gp_method.equals( Settings.MultiProbeHandling.MEAN ) ) {
                    geneScoreTemp[counter] /= in_size; // take the mean
                }
                Double dbb = new Double( geneScoreTemp[counter] );
                geneToScoreMap.put( geneSymbol, dbb );
                counter++;
            }
        } // end of while

        if ( counter == 0 ) {
            // this is okay, if we're trying to show the class despite there being no results.
            log.warn( "No valid gene to score mappings were found." );
            return;
        }

        if ( messenger != null ) messenger.showStatus( counter + " distinct genes found in the gene scores." );

    }

    public Double[] getProbeScores() {
        return this.probeToScoreMap.values().toArray( new Double[] {} );
    }

} // end of class
