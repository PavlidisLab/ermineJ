package classScore.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import baseCode.math.DescriptiveWithMissing;
import baseCode.math.RandomChooser;
import baseCode.math.Rank;
import baseCode.math.Stats;
import baseCode.util.StatusViewer;
import classScore.Settings;
import classScore.data.GeneScoreReader;
import classScore.data.Histogram;

/**
 * Calculates a background distribution for class sscores derived from randomly
 * selected individual gene scores...and does other things. Created 09/02/02.
 *
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class ResamplingExperimentGeneSetScore extends
      AbstractResamplingGeneSetScore {
   private double[] groupPvals = null; // pvalues for groups.
   private double[] pvals = null; // pvalues for probes.
   private Map groupPvalMap; // groups -> pvalues
   private Map probePvalMap; // probes -> pval
   private boolean useWeights;
   private static int quantile = 50;
   private static double quantfract = 0.5;
   private int method;


   /**
    * Used for methods which require randomly sampling classes to generate a
    * null distribution of scores based on gene-by-gene scores.
    *
    * @return A histogram object containing a cdf that can be used to generate
    *         pvalues.
    * @param m GeneSetScoreStatus
    */
   public Histogram generateNullDistribution( StatusViewer m ) {

      int num_genes;
      double[] in_pval;

      if ( hist == null ) {
         throw new NullPointerException( "Histogram object was null." );
      }

      // do the right thing if we are using weights.
      if ( useWeights ) {
         num_genes = groupPvals.length;
         in_pval = groupPvals;
      } else {
         num_genes = pvals.length;
         in_pval = pvals;
      }

      if ( num_genes == 0 ) {
         System.err.println( "No pvalues!" );
         System.exit( 1 );
      }

      // we use this throughout.
      int[] deck = new int[num_genes];
      for ( int i = 0; i < num_genes; i++ ) {
         deck[i] = i;
      }

      for ( int i = classMinSize; i <= classMaxSize; i++ ) {
         double[] random_class = new double[i]; // holds data for random class.
         double rawscore = 0.0;
         for ( int k = 0; k < numRuns; k++ ) {
            RandomChooser.chooserandom( random_class, in_pval, deck, num_genes,
                  i );
            rawscore = calc_rawscore( random_class, i, method );
            hist.update( i - classMinSize, rawscore );
         }

         if ( m != null ) {
            m.setStatus( "Currently running class size " + i );
         }

//         try {
//            Thread.sleep( 1 );
//         } catch ( InterruptedException ex ) {
//            Thread.currentThread().interrupt();
//         }

      }

      hist.tocdf();

      return hist;
   }


   /**
    * @param settings
    * @param geneScores
    */
   public ResamplingExperimentGeneSetScore( Settings settings, GeneScoreReader geneScores ) {
      this.classMaxSize = settings.getMaxClassSize();
      this.classMinSize = settings.getMinClassSize();
      this.numRuns = settings.getIterations();
      this.setQuantile( settings.getQuantile() );
      this.useWeights = ( Boolean.valueOf( settings.getUseWeights() ) )
            .booleanValue();
      this.setMethod( settings.getClassScoreMethod() );

      if ( classMaxSize < classMinSize ) {
         throw new IllegalArgumentException(
               "Error:The maximum class size is smaller than the minimum." );
      }

      this.numClasses = classMaxSize - classMinSize + 1;
      pvals = geneScores.getPvalues(); // array of pvalues.
      groupPvals = geneScores.getGroupPvalues();
      probePvalMap = geneScores.getProbeToPvalMap(); // reference to the probe -> pval map.
      groupPvalMap = geneScores.getGroupToPvalMap(); // this gets initialized by set_input_pvals

      this.setHistogramRange(); // figure out the max pvalue possible.
      this.hist = new Histogram( numClasses, classMinSize, numRuns,
            histogramMax, 0.0 );
   }

   /**
    *
    * @return double[]
    */
   public double[] get_in_pvals() {
      return useWeights ? groupPvals : pvals;
   }


   /**
    *
    * @param value int
    */
   public void setQuantile( int value ) {
      quantile = value;
      quantfract = quantile / 100.0;
   }

   /**
    *
    * @return int
    */
   public int get_quantile() {
      return quantile;
   }


   /**
    *
    * @return Map
    */
   public Map get_map() {
      return probePvalMap;
   }

   /**
    *
    * @param shuffle boolean
    * @return Map
    */
   public Map get_map( boolean shuffle ) {

      if ( shuffle ) {
         Map scrambled_probe_pval_map = new LinkedHashMap();

         Set keys = probePvalMap.keySet();
         Iterator it = keys.iterator();

         Collection values = probePvalMap.values();
         Vector valvec = new Vector( values );
         Collections.shuffle( valvec );

         // randomly associate keys and values
         int i = 0;
         while ( it.hasNext() ) {
            scrambled_probe_pval_map.put( it.next(), valvec.get( i ) );
            //		 System.err.println(it.next() + " " + valvec.get(i));
            i++;
         }
         return scrambled_probe_pval_map;

      }
      return probePvalMap;

   }

   /**
    *
    * @param probe_id String
    * @return double
    */
   public double get_value_map( String probe_id ) {
      double value = 0.0;
      if ( probePvalMap.get( probe_id ) != null ) {
         value = Double.parseDouble( ( probePvalMap.get( probe_id ) )
               .toString() );
      }
      return value;
   }

   /**
    * Basic method to calculate the raw score, given an array of the gene scores
    * for items in the class. Note that performance here is important.
    *
    * @param genevalues double[]
    * @param effsize int
    * @throws IllegalArgumentException
    * @return double
    */
   public static double calc_rawscore( double[] genevalues, int effsize, int method  )
         throws IllegalArgumentException {

      if ( method == Settings.MEAN_METHOD ) {
         return DescriptiveWithMissing.mean( genevalues, effsize );
      }
      int index = ( int ) Math.floor( quantfract * effsize );
      if ( method == Settings.QUANTILE_METHOD ) {
         return Stats.quantile( index, genevalues, effsize );
      } else if ( method == Settings.MEAN_ABOVE_QUANTILE_METHOD ) {
         return Stats.meanAboveQuantile( index, genevalues, effsize );
      } else {
         throw new IllegalStateException(
               "Unknown raw score calculation method selected" );
      }

   }

   /**
    *
    * @param meth String
    * @throws IllegalArgumentException
    */
   private void setMethod( int meth )  {
      method = meth;
   }

   /**  */
   public void setHistogramRange() {
      if (groupPvals == null || pvals == null) {
         throw new IllegalStateException("Null pvalue arrays for histogram range setting");
      }

      histogramMax = Rank.meanOfTop2( useWeights ? groupPvals : pvals );
   }

}
