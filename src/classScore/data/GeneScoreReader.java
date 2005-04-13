package classScore.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import baseCode.util.FileTools;
import baseCode.util.StatusViewer;
import cern.jet.math.Arithmetic;
import classScore.Settings;

/**
 * Description:Parses the file of the form
 * 
 * <pre>
 * 
 *  
 *   
 *    
 *     
 *      
 *       
 *        
 *        
 *                           probe_id[tab]pval
 *        
 *         
 *        
 *       
 *      
 *     
 *    
 *   
 *  
 * </pre>
 * 
 * <p>
 * The values are stored in a Map probeToPvalMap. This is used to see what probes are int the data set, as well as the
 * score for each probe.
 * </p>
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneScoreReader {

    private static final double SMALL = 10e-16;
    double[] groupPvalues;
    private String[] probeIDs = null;
    private double[] probePvalues = null;
    private int num_pvals;
    private Map probeToPvalMap;
    private Map geneToPvalMap;

    /**
     * @throws IOException
     * @param filename
     * @param settings
     * @param StatusViewer messenger
     * @param geneToProbeMap
     * @param probeToGeneMap
     */
    public GeneScoreReader( String filename, Settings settings, StatusViewer messenger, Map geneToProbeMap,
            Map probeToGeneMap ) throws IOException {
        this();
        FileTools.checkPathIsReadableFile( filename );
        InputStream is = new FileInputStream( filename );
        read( is, settings, messenger, geneToProbeMap, probeToGeneMap );
    }

    /**
     * @param is - input stream
     * @param settings
     * @param messenger
     * @param groupToProbeMap
     * @param probeToGeneMap
     * @throws IOException
     */
    public GeneScoreReader( InputStream is, Settings settings, StatusViewer messenger, Map geneToProbeMap,
            Map probeToGeneMap ) throws IOException {
        this();
        read( is, settings, messenger, geneToProbeMap, probeToGeneMap );
    }

    private GeneScoreReader() {
        geneToPvalMap = new HashMap();
        probeToPvalMap = new LinkedHashMap();
    }

    private void read( InputStream is, Settings settings, StatusViewer messenger, Map geneToProbeMap, Map probeToGeneMap )
            throws IOException {

        if ( settings.getScorecol() < 2 ) {
            throw new IllegalArgumentException( "Illegal column number " + settings.getScorecol()
                    + ", must be greater than 1" );
        }

        if ( messenger != null ) {
            messenger.setStatus( "Reading gene scores from column " + settings.getScorecol() );
        }

        BufferedReader dis = new BufferedReader( new InputStreamReader( new BufferedInputStream( is ) ) );
        String row;
        boolean invalidLog = false;
        boolean unknownProbe = false;
        boolean invalidNumber = false;
        String badNumberString = "";
        Vector rows = new Vector();
        while ( ( row = dis.readLine() ) != null ) {
            StringTokenizer st = new StringTokenizer( row, "\t" );
            Vector cols = new Vector();
            while ( st.hasMoreTokens() ) {
                cols.add( st.nextToken() );
            }
            if ( cols.size() > 0 ) rows.add( cols );
            if ( Thread.currentThread().isInterrupted() ) {
                dis.close();
                throw new RuntimeException( "Interrupted" );
            }
        }
        dis.close();

        probeIDs = new String[rows.size() - 1];
        probePvalues = new double[rows.size() - 1];
        int numProbesKept = 0;
        for ( int i = 1; i < rows.size(); i++ ) {

            if ( Thread.currentThread().isInterrupted() ) {
                throw new RuntimeException( "Interrupted" );
            }

            if ( ( ( Vector ) ( rows.elementAt( i ) ) ).size() < settings.getScorecol() ) {
                throw new IOException( "Insufficient gene score columns in row " + i
                        + ", expecting file to have at least " + settings.getScorecol()
                        + " columns. Please check that the file has the " + "correct plain text format." );
            }

            String name = ( String ) ( ( ( Vector ) ( rows.elementAt( i ) ) ).elementAt( 0 ) );

            if ( name.matches( "AFFX.*" ) ) { // todo: put this rule somewhere else // todo use a filter.
                if ( messenger != null ) {
                    messenger.setStatus( "Skipping probe in pval file: " + name );
                }
                continue;
            }
            probeIDs[i - 1] = name;

            try {
                probePvalues[i - 1] = Double.parseDouble( ( String ) ( ( ( Vector ) ( rows.elementAt( i ) ) )
                        .elementAt( settings.getScorecol() - 1 ) ) );
            } catch ( NumberFormatException e ) {
                invalidNumber = true;
                badNumberString = ( String ) ( ( ( Vector ) ( rows.elementAt( i ) ) )
                        .elementAt( settings.getScorecol() - 1 ) );
                probePvalues[i - 1] = 0.0;
            }

            // Fudge when pvalues are zero.
            if ( settings.getDoLog() && probePvalues[i - 1] <= 0.0 ) {
                invalidLog = true;
                probePvalues[i - 1] = SMALL;

            }

            if ( settings.getDoLog() ) {
                probePvalues[i - 1] = -Arithmetic.log10( probePvalues[i - 1] );
            }

            // only keep probes that are in our array platform.
            if ( !probeToGeneMap.containsKey( probeIDs[i - 1] ) ) {
                unknownProbe = true;
                continue;
            }

            numProbesKept++;

            probeToPvalMap.put( probeIDs[i - 1], new Double( probePvalues[i - 1] ) );
        }

        num_pvals = Array.getLength( probePvalues );

        if ( invalidNumber && messenger != null ) {

            messenger.setError( "Non-numeric gene scores(s) " + " ('" + badNumberString + "') "
                    + " found for input file. These are set to an initial value of zero." );

            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                throw new RuntimeException( "Interrupted" );
            }

        }

        if ( invalidLog && messenger != null ) {
            messenger
                    .setError( "Warning: There were attempts to take the log of non-positive values. These are set to "
                            + SMALL );
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                throw new RuntimeException( "Interrupted" );
            }
        }

        if ( unknownProbe ) {
            messenger
                    .setError( "Warning: Some probes in your gene score file don't match the ones in the annotation file." );
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                throw new RuntimeException( "Interrupted" );
            }
        }

        if ( numProbesKept == 0 ) {
            throw new IllegalStateException( "None of the probes in the gene score file correspond to probes in the "
                    + "annotation (\".an\") file you selected." );
        }

        if ( num_pvals == 0 ) {
            throw new IllegalStateException( "No pvalues found in the gene score file! Please check the file has"
                    + " the correct plain text format and"
                    + " corresponds to the microarray annotation (\".an\") file you selected." );
        }

        if ( messenger != null ) {
            messenger.setStatus( "Found " + num_pvals + " pvals in the file" );
        }

        setUpGeneToPvalMap( settings, geneToProbeMap, messenger );

    } //

    /**
     * Each pvalue is adjusted to the mean (or best) of all the values in the 'replicate group' to yield a "group to
     * pvalue map".
     * 
     * @param gp_method gp_method Which method we use to calculate scores for genes that occur more than once in the
     *        data set.
     */
    private void setUpGeneToPvalMap( Settings settings, Map geneToProbeMap, StatusViewer messenger ) {

        int gp_method = settings.getGroupMethod();

        if ( geneToProbeMap == null || geneToProbeMap.size() == 0 ) {
            throw new IllegalStateException( "groupToProbeMap was not set." );
        }

        if ( probeToPvalMap == null ) {
            throw new IllegalStateException( "probeToPvalMap was not set." );
        }

        if ( geneToProbeMap.size() == 0 ) {
            throw new IllegalStateException( "Group to probe map was empty" );
        }

        double[] group_pval_temp = new double[geneToProbeMap.size()];
        int counter = 0;

        for ( Iterator groupMapItr = geneToProbeMap.keySet().iterator(); groupMapItr.hasNext(); ) {

            if ( Thread.currentThread().isInterrupted() ) {
                return;
            }

            String group = ( String ) groupMapItr.next();

            ArrayList probes = ( ArrayList ) geneToProbeMap.get( group ); /*
                                                                             * probes in this group according to the
                                                                             * array platform.
                                                                             */
            int in_size = 0;

            // Analyze all probes in this 'group' (pointing to the same gene)
            for ( Iterator pbItr = probes.iterator(); pbItr.hasNext(); ) {
                String probe = ( String ) pbItr.next();

                if ( !probeToPvalMap.containsKey( probe ) ) {
                    continue;
                }

                // these values are already log transformed if the user selected that option.
                double pbPval = ( ( Double ) probeToPvalMap.get( probe ) ).doubleValue();

                switch ( gp_method ) {
                    case Settings.MEAN_PVAL: {
                        group_pval_temp[counter] += pbPval;
                        break;
                    }
                    case Settings.BEST_PVAL: {
                        if ( settings.upperTail() ) {
                            group_pval_temp[counter] = Math.max( pbPval, group_pval_temp[counter] );
                        } else {
                            group_pval_temp[counter] = Math.min( pbPval, group_pval_temp[counter] );
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
                    group_pval_temp[counter] /= in_size; // take the mean
                }
                Double dbb = new Double( group_pval_temp[counter] );
                geneToPvalMap.put( group, dbb );
                counter++;
            }
        } // end of while

        if ( counter == 0 ) {
            throw new IllegalStateException( "No gene to pvalue mappings were found." );
        }

        if ( messenger != null ) messenger.setStatus( counter + " distinct genes found in the annotations." );

        groupPvalues = new double[counter];
        for ( int i = 0; i < counter; i++ ) {
            groupPvalues[i] = group_pval_temp[i];
        }

    }

    /**
     */
    public String[] getProbeIds() {
        return probeIDs;
    }

    /**
     */
    public double[] getPvalues() {
        return probePvalues;
    }

    public double[] getGroupPvalues() {
        return this.groupPvalues;
    }

    /**
     */
    public int getNumGeneScores() {
        return num_pvals;
    }

    /**
     */
    public Map getProbeToPvalMap() {
        return probeToPvalMap;
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
            Vector valvec = new Vector( values );
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
     * @return
     */
    public Map getGeneToPvalMap() {
        return geneToPvalMap;
    }

    /**
     * @param probe_id
     * @return
     */
    public double getValueMap( String probe_id ) {
        double value = 0.0;

        if ( probeToPvalMap.get( probe_id ) != null ) {
            value = Double.parseDouble( ( probeToPvalMap.get( probe_id ) ).toString() );
        }

        return value;
    }

} // end of class
