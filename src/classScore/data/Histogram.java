package classScore.data;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.math.NormalProbabilityComputer;
import baseCode.math.ProbabilityComputer;
import baseCode.math.Stats;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.stat.Descriptive;

/**
 * Stores distributions for geneSets ( a series of histograms). For generic histograms, use hep.aida.
 * 
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class Histogram {
   protected static final Log log = LogFactory.getLog( Histogram.class );
   private int minimumGeneSetSize = 0;
   private double binSize = 0.002; // todo: set this automatically?, so there are always a reasonable # of bins.
   private double minimum = 0.0;
   private double maximum = 5.0; // this gets adjusted if need be.
   private int numBins = 0;
   private int numItemsPerHistogram = 0;
   private double minPval; // the smallest possible pvalue: used when a requested score is out of the top of the range.
   private Map analyticDistributions;

   private OpenIntObjectHashMap empiricalDistributions;
   private boolean isCDF = false;

   /**
    * @param numGeneSets
    * @param minGeneSetSize
    * @param numRuns
    * @param max
    * @param min
    */
   public Histogram( int numGeneSetSizes, int minGeneSetSize, int numRuns,
         double max, double min ) {

      if ( numGeneSetSizes < 1 ) {
         throw new IllegalArgumentException( "No classes." );
      }

      this.minimum = min;
      this.maximum = max;
      this.minimumGeneSetSize = minGeneSetSize;
      setNumRuns( numRuns );
      calcNumOfBins();
      analyticDistributions = new HashMap();
      empiricalDistributions = new OpenIntObjectHashMap();

      for ( int i = 0; i < numGeneSetSizes; i++ ) {
         int size = minGeneSetSize + i;
         empiricalDistributions.put( size, new DenseDoubleMatrix1D( numBins ) );
      }
   }

   /**
    * 
    *
    */
   public void calcNumOfBins() {
      numBins = ( int ) ( ( maximum - minimum ) / binSize );
      if ( numBins < 1 ) {
         throw new IllegalStateException(
               "Histogram had no bins or too few bins. (" + numBins + ")" );
      }
   }

   /**
    * @param runs int
    */
   public void setNumRuns( int runs ) {
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
   public void update( int classSize, double value ) {

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
      DenseDoubleMatrix1D histRow = ( ( DenseDoubleMatrix1D ) empiricalDistributions
            .get( classSize ) );
      histRow.setQuick( thebin, histRow.getQuick( thebin ) + 1 );
   }

   /**
    * Convert raw histograms to CDFs.
    */
   public void tocdf() {

      IntArrayList sizes = empiricalDistributions.keys();

      for ( int i = 0; i < sizes.size(); i++ ) {
         DenseDoubleMatrix1D pdf = ( DenseDoubleMatrix1D ) empiricalDistributions
               .get( sizes.get( i ) );
         DoubleArrayList dal = new DoubleArrayList( pdf.toArray() );

         if ( Descriptive.sum( dal ) == 0 ) {
            empiricalDistributions.removeKey( sizes.get( i ) );
         }

         DoubleArrayList cdf = Stats.cdf( dal );
         for ( int j = 0; j < cdf.size(); j++ ) {
            pdf.setQuick( j, cdf.getQuick( j ) );
         }
      }
      log.debug( "Made cdf" );
      this.isCDF = true;
   }

   /**
    * @return double
    */
   public double getBinSize() {
      return binSize;
   }

   /**
    * @return double
    */
   public double getHistMin() {
      return minimum;
   }

   /**
    * @return double
    */
   public double getHistMax() {
      return maximum;
   }

   /**
    * @return int
    */
   public int getNumBins() {
      return numBins;
   }

   public int getNumHistograms() {
      // return M.rows();
      return empiricalDistributions.size();
   }

   /**
    * @return int
    */
   public int getNumRuns() {
      return numItemsPerHistogram;
   }

   public int getMinGeneSetSize() {
      return minimumGeneSetSize;
   }

   public String toString() {
      return "There are " + numBins
            + " bins in the histogram. The maximum possible value is "
            + maximum + ", the minimum is " + minimum + "." + " Min class is "
            + minimumGeneSetSize + ".";
   }

   /**
    * @param class_size int
    * @param min_class_size int
    * @return int
    */
   public int getClassIndex( int geneSetSize, int minGeneSetSize ) {
      //get corresponding index for each class size
      return geneSetSize - minGeneSetSize;
   }

   private int findNearestUsableHistogram( int geneSetSize ) {
      int usedGeneSetSize = geneSetSize;
      while ( !empiricalDistributions.containsKey( usedGeneSetSize ) ) {
         usedGeneSetSize--;
         if ( usedGeneSetSize < minimumGeneSetSize ) {
            throw new IllegalArgumentException(
                  "No distribution or near distribution found for gene set size "
                        + geneSetSize );
         }
      }
      return usedGeneSetSize;
   }

   /**
    * @param geneSetSize int - NOT the row, that is determined here.
    * @param rawscore double
    * @return double
    */
   public double getValue( int geneSetSize, double rawscore ) {
      if ( rawscore > maximum || rawscore < minimum ) { // sanity check.
         throw new IllegalStateException(
               "Warning, a rawscore yielded a bin number which was out of the range: "
                     + rawscore );
      }

      if ( !isCDF ) {
         throw new IllegalStateException(
               "Distributions must be converted to CDFs first before getting probabilities." );
      }

      int usedGeneSetSize = findNearestUsableHistogram( geneSetSize );

      /* use a analytical distribution if we have one for this set size */
      if ( useExactPvalue( usedGeneSetSize ) ) {

         return this.getExactProbability( usedGeneSetSize, rawscore );
      }

      /* use the empirical distribution */
      int binnum = ( int ) Math.floor( ( rawscore - minimum ) / binSize );
      if ( binnum < 0 ) {
         binnum = 0;
      }
      if ( binnum > numBins - 1 ) {
         binnum = numBins - 1;
      }
      return this.getProbability( usedGeneSetSize, binnum );
   }

   /**
    * @param geneSetSize
    * @param rawscore
    * @return
    */
   private double getExactProbability( int geneSetSize, double rawScore ) {
      ProbabilityComputer p = ( ProbabilityComputer ) analyticDistributions
            .get( new Integer( geneSetSize ) );
      if ( p == null ) {
         throw new IllegalStateException( "Gene set size " + geneSetSize
               + " is not associated with an exact probability density." );
      }
      return p.probability( rawScore );
   }

   /**
    * @param classSize
    * @return
    */
   public double[] getHistogram( int geneSetSize ) {
      int usedGeneSetSize = findNearestUsableHistogram( geneSetSize );
      //return M.viewRow( row ).toArray();
      return ( ( DoubleMatrix1D ) empiricalDistributions.get( usedGeneSetSize ) )
            .toArray();
   }

   /**
    * @return
    */
   public double[] getBins() {
      double[] returnVal = new double[numBins];
      for ( int i = 0; i < returnVal.length; i++ ) {
         returnVal[i] = i * this.binSize + minimum;
      }
      return returnVal;
   }

   /**
    * Prints the histogram to stdout.
    */
   public void print( Writer s ) throws IOException {
      // print a heading
      int stepsize = 20;
      s.write( "heading:" );
      for ( int j = 0; j < numBins; j += stepsize ) { // for each bin in
         // this histogram.
         s.write( "\t" + ( minimum + binSize * j ) );
      }
      s.write( "\n" );

      for ( int i = 0; i < empiricalDistributions.size(); i++ ) { // for each histogram (class size)
         s.write( "row:" );
         for ( int j = 0; j < numBins; j += stepsize ) { // for each bin in
            // this histogram.

            System.out.print( "\t"
                  + ( ( DoubleMatrix1D ) empiricalDistributions.get( i ) )
                        .getQuick( j ) );
         }
         s.write( "\n" );
      }
   }

   /**
    * @param row int
    * @param binnum int
    * @return double
    */
   public double getProbability( int classSize, int binnum ) {

      if ( !empiricalDistributions.containsKey( classSize ) ) {
         throw new IllegalArgumentException(
               "There is no empirical distribution for class size " + classSize );
      }

      double pval = ( ( DoubleMatrix1D ) empiricalDistributions.get( classSize ) )
            .getQuick( binnum );
      if ( pval == 0.0 ) {
         return minPval;
      }
      return pval;
   }

   private boolean useExactPvalue( int geneSetSize ) {

      if ( analyticDistributions.containsKey( new Integer( geneSetSize ) ) ) {
         return true;
      }
      return false;
   }

   /**
    * @param i
    * @param mean
    * @param variance
    */
   public void addExactNormalProbabilityComputer( int i, double mean,
         double variance ) {
      analyticDistributions.put( new Integer( i ),
            new NormalProbabilityComputer( variance, mean ) );
   }

}