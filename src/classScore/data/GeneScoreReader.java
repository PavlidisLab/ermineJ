package classScore.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import baseCode.util.StatusViewer;
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
 *       probe_id[tab]pval
 *
 *
 *
 *
 *
 *
 * </pre>
 *
 * <p>
 * The values are stored in a HashTable probe_pval_map. This is used to see what probes are int the data set, as well as
 * the score for each probe. Created :09/02/02
 * </p>
 *
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class GeneScoreReader {

   double[] groupPvalues;
   private String[] probeIDs = null;
   private double[] probePvalues = null;
   private int num_pvals;
   private Map probeToPvalMap;
   private Map groupToPvalMap;
   

   /**
    * @param filename
    * @param settings
    * @param messenger
    * @param groupToProbeMap
    * @throws IOException
    */
   public GeneScoreReader( String filename, Settings settings,
         StatusViewer messenger, Map groupToProbeMap ) throws IOException {
      //read in file

      groupToPvalMap = new HashMap();
      double log10 = Math.log( 10 );
      File infile = new File( filename );
      if ( !infile.exists() || !infile.canRead() ) {
         throw new IOException( "Could not read " + filename );
      }

      if ( settings.getScorecol() < 1 ) {
         throw new IllegalArgumentException( "Illegal column number "
               + settings.getScorecol() + ", must be greater or equal to 1" );
      }

      if ( messenger != null ) {
         messenger.setStatus( "Reading gene scores from column "
               + settings.getScorecol() );
      }

      BufferedReader dis = new BufferedReader( new InputStreamReader(
            new BufferedInputStream( new FileInputStream( filename ) ) ) );

      String row;
      probeToPvalMap = new LinkedHashMap();
      Vector rows = new Vector();
      while ( ( row = dis.readLine() ) != null ) {
         StringTokenizer st = new StringTokenizer( row, "\t" );
         Vector cols = new Vector();
         while ( st.hasMoreTokens() ) {
            cols.add( st.nextToken() );
         }
         rows.add( cols );
      }
      dis.close();

      probeIDs = new String[rows.size() - 1];
      probePvalues = new double[rows.size() - 1];

      double small = 10e-16;

      for ( int i = 1; i < rows.size(); i++ ) {

         if ( ( ( Vector ) ( rows.elementAt( i ) ) ).size() < settings
               .getScorecol() ) {
            throw new IOException( "Insufficient gene score columns in row "
                  + i + ", expecting file to have at least "
                  + settings.getScorecol() + " columns." );
         }

         String name = ( String ) ( ( ( Vector ) ( rows.elementAt( i ) ) )
               .elementAt( 0 ) );

         if ( name.matches( "AFFX.*" ) ) { // todo: put this rule somewhere else
            if ( messenger != null ) {
               messenger.setStatus( "Skipping probe in pval file: " + name );
            }
            continue;
         }
         probeIDs[i - 1] = name;
         probePvalues[i - 1] = Double
               .parseDouble( ( String ) ( ( ( Vector ) ( rows.elementAt( i ) ) )
                     .elementAt( settings.getScorecol() - 1 ) ) );

         // Fudge when pvalues are zero.
         if ( settings.getDoLog() && probePvalues[i - 1] <= 0 ) {
            if ( messenger != null ) {
               messenger
                     .setStatus( "Warning: Cannot take log of non-positive value for "
                           + name
                           + " ("
                           + probePvalues[i - 1]
                           + ") from gene score file: Setting to " + small );
               probePvalues[i - 1] = small;
            }
         }

         if ( settings.getDoLog() ) {
            probePvalues[i - 1] = -( Math.log( probePvalues[i - 1] ) / log10 ); // Make
            // -log
            // base 10.
         }
         probeToPvalMap
               .put( probeIDs[i - 1], new Double( probePvalues[i - 1] ) );
      }

      num_pvals = Array.getLength( probePvalues );
      if ( num_pvals == 0 ) {
         throw new IllegalStateException( "No pvalues found in the file!" );
      }

      if ( messenger != null ) {
         messenger.setStatus( "Found " + num_pvals + " pvals in the file" );
      }
      setUpGroupToPvalMap( settings.getGeneRepTreatment(), groupToProbeMap, messenger );

   } //

   /**
    * Each pvalue is adjusted to the mean (or best) of all the values in the 'replicate group' to yield a "group to
    * pvalue map".
    *
    * @param gp_method gp_method Which method we use to calculate scores for genes that occur more than once in the data
    *        set.
    */
   private void setUpGroupToPvalMap( int gp_method, Map groupToProbeMap, StatusViewer messenger ) {

      if ( groupToProbeMap == null || groupToProbeMap.size() == 0 ) {
         throw new IllegalStateException( "groupToProbeMap was not set." );
      }

      if ( probeToPvalMap == null ) {
         throw new IllegalStateException( "probeToPvalMap was not set." );
      }

      if (groupToProbeMap.size() == 0) {
         throw new IllegalStateException("Group to probe map was empty");
      }


      double[] group_pval_temp = new double[groupToProbeMap.size()];
      int counter = 0;

      for ( Iterator groupMapItr = groupToProbeMap.keySet().iterator(); groupMapItr
            .hasNext(); ) {
         String group = ( String ) groupMapItr.next();
         /* probes in this group */
         ArrayList probes = ( ArrayList ) groupToProbeMap.get( group );
         int in_size = 0;
         for ( Iterator pbItr = probes.iterator(); pbItr.hasNext(); ) {
            String probe = ( String ) pbItr.next();

            if ( !probeToPvalMap.containsKey( probe ) ) {
          //     messenger.setStatus(
        //             "Annotations contains probe not in the probeToPvalMap: "
         //                  + probe );
               continue;
            }

            double pbPval = ( ( Double ) probeToPvalMap.get( probe ) )
                  .doubleValue();

            switch ( gp_method ) {
               case Settings.MEAN_PVAL: {
                  group_pval_temp[counter] += pbPval;
                  break;
               }
               case Settings.BEST_PVAL: {
                  group_pval_temp[counter] = Math.max( pbPval,
                        group_pval_temp[counter] );
                  break;
               }
               default: {
                  throw new IllegalArgumentException(
                        "Illegal selection for groups score method. Valid choices are MEAN_PVAL and BEST_PVAL" );
               }
            }
            in_size++;
         }

         if ( in_size != 0 ) {
            if ( gp_method == Settings.MEAN_PVAL ) {
               group_pval_temp[counter] /= in_size; // take the mean
            }
            // messenger.setStatus("Processing " + group + " had " + in_size + " probes.");
            Double dbb = new Double( group_pval_temp[counter] );
            groupToPvalMap.put( group, dbb );
            counter++;
         }
      } //end of while

      if (counter == 0) {
         throw new IllegalStateException("No gene to pvalue mappings were found.");
      }

      messenger.setStatus(counter + " distinct genes found in the annotations.");

      groupPvalues = new double[counter];
      for ( int i = 0; i < counter; i++ ) {
         groupPvalues[i] = group_pval_temp[i];
      }

   }

   /**
    */
   public String[] get_probe_ids() {
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
   public int get_numpvals() {
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
   public Map getGroupToPvalMap( boolean shuffle ) {
      if ( shuffle ) {
         Map scrambled_map = new LinkedHashMap();
         Set keys = groupToPvalMap.keySet();
         Iterator it = keys.iterator();

         Collection values = groupToPvalMap.values();
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
      return groupToPvalMap;

   }

   /**
    * @return
    */
   public Map getGroupToPvalMap() {
      return groupToPvalMap;
   }

   /**
    */
   public double get_value_map( String probe_id ) {
      double value = 0.0;

      if ( probeToPvalMap.get( probe_id ) != null ) {
         value = Double.parseDouble( ( probeToPvalMap.get( probe_id ) )
               .toString() );
      }

      return value;
   }

} // end of class
