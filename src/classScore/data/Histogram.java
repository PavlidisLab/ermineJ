package classScore.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.math.Stats;
import cern.colt.list.DoubleArrayList;

/**
 * Stores distributions for geneSets ( a series of histograms). For generic
 * histograms, use hep.aida.
 * 
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 * @todo default bin size
 */
public class Histogram {
   protected static final Log log = LogFactory.getLog( Histogram.class );
   private int minimumGeneSetSize = 0;
   private double binSize = 0.002; // todo: set this automatically?, so there
   // are always a reasonable # of bins.
   private double minimum = 0.0;  
   private double maximum = 5.0; // this gets adjusted if need be.
   private int numBins = 0;
   private int numItemsPerHistogram = 0;
   private DenseDoubleMatrix2DNamed M = null; // holds the actual histograms.
   // Each
   // row is a histogram.
   private double minPval; // the smallest possible pvalue: used when a

   // requested score is out of the top of the range.

   public Histogram() {
   }

   /**
    * 
    * @param number_of_class
    * @param min_class_size
    * @param number_of_runs
    * @param max
    */
   public Histogram( int number_of_class, int min_class_size,
         int number_of_runs, double max, double min ) {

      if ( number_of_class < 1 ) {
         throw new IllegalArgumentException( "No classes." );
      }

      this.minimum = min;
      this.maximum = max;
      this.minimumGeneSetSize = min_class_size;
      set_number_of_runs( number_of_runs );
      set_number_of_bins();

      M = new DenseDoubleMatrix2DNamed( number_of_class, numBins + 1 );
   }

   /**
    */
   public void set_number_of_bins() {
      numBins = ( int ) ( ( maximum - minimum ) / binSize );
      if ( numBins < 1 ) {
         throw new IllegalStateException(
               "Histogram had no bins or too few bins. (" + numBins + ")" );
      }
   }

   /**
    * 
    * @param runs int
    */
   public void set_number_of_runs( int runs ) {
      numItemsPerHistogram = runs;
      minPval = 0.5 / numItemsPerHistogram; // the best possible
      // pvalue for
      // a class.
      log.debug( "Minimum pvalue will be " + minPval + ", "
            + numItemsPerHistogram + " runs." );
   }

   /**
    * Update the count for one bin.
    * 
    * @param row int
    * @param value double
    */
   public void update( int row, double value ) {

      int thebin = ( int ) Math.floor( ( value - minimum ) / binSize );

      // make sure we're in the range
      if ( thebin < 0 ) {
         thebin = 0;
      }

      if ( thebin > numBins - 1 ) { // this shouldn't happen since we
         // make sure there are enough bins.
         log.debug( "Last bin exceeded!" );
         thebin = numBins - 1;
      }

      M.setQuick( row, thebin, M.getQuick( row, thebin ) + 1 );
   }

   /**
    * Convert a raw histogram to a cdf.
    * 
    * @todo this is probably not very efficient - calls new a lot.
    */
   public void tocdf() {

      for ( int i = 0; i < M.rows(); i++ ) { // for each histogram (class size)

         DoubleArrayList cdf = Stats.cdf( new DoubleArrayList( M.viewRow( i )
               .toArray() ) );
         for ( int j = 0; j < M.columns(); j++ ) {
            M.setQuick( i, j, cdf.getQuick( j ) );
         }
      }
      log.debug( "Made cdf" );
   }

   /**
    * 
    * @return double
    */
   public double get_bin_size() {
      return binSize;
   }

   /**
    * 
    * @return double
    */
   public double get_hist_min() {
      return minimum;
   }

   /**
    * 
    * @return double
    */
   public double get_hist_max() {
      return maximum;
   }

   /**
    * 
    * @return int
    */
   public int get_number_of_bins() {
      return numBins;
   }

   public int get_number_of_histograms() {
      return M.rows();
   }

   /**
    * 
    * @return int
    */
   public int get_number_of_runs() {
      return numItemsPerHistogram;
   }


   public int get_min_class_size() {
      return minimumGeneSetSize;
   }

   public String toString() {
      return "There are " + numBins
            + " bins in the histogram. The maximum possible value is "
            + maximum + ", the minimum is " + minimum + "." + " Min class is "
            + minimumGeneSetSize + ".";
   }

   /**
    * 
    * @param class_size int
    * @param min_class_size int
    * @return int
    */
   public int getClassIndex( int class_size, int min_class_size ) {
      //get corresponding index for each class size
      return class_size - min_class_size;
   }

   /**
    * 
    * @param class_size int - NOT the row, that is determined here.
    * @param rawscore double
    * @return double
    */
   public double getValue( int class_size, double rawscore ) {
      if ( rawscore > maximum || rawscore < minimum ) { // sanity check.
         throw new IllegalStateException(
               "Warning, a rawscore yielded a bin number which was out of the range: "
                     + rawscore );
      }
      int row = this.getClassIndex( class_size, minimumGeneSetSize );
      int binnum = ( int ) Math.floor( ( rawscore - minimum ) / binSize );

      if ( binnum < 0 ) {
         binnum = 0;

      }
      if ( binnum > numBins - 1 ) {
         binnum = numBins - 1;

      }
      return this.getProbability( row, binnum );

   }

   /**
    * Prints the histogram to stdout.
    */
   public void print() {
      // print a heading
      int stepsize = 20;
      System.out.print( "heading:" );
      for ( int j = 0; j < M.columns(); j += stepsize ) { // for each bin in
         // this histogram.
         System.out.print( "\t" + ( minimum + binSize * j ) );
      }
      System.out.print( "\n" );

      for ( int i = 0; i < M.rows(); i++ ) { // for each histogram (class size)
         System.out.print( "row:" );
         for ( int j = 0; j < M.columns(); j += stepsize ) { // for each bin in
            // this histogram.
            System.out.print( "\t" + M.getQuick( i, j ) );
         }
         System.out.print( "\n" );
      }
   }

   /**
    * 
    * @param row int
    * @param binnum int
    * @return double
    */
   public double getProbability( int row, int binnum ) {
      double pval = M.getQuick( row, binnum );
      if ( pval == 0.0 ) {
         return minPval;
      }
      return pval;
   }

}