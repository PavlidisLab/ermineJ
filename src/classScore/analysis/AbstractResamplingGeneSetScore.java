package classScore.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import classScore.data.Histogram;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class AbstractResamplingGeneSetScore implements
      NullDistributionGenerator {

   protected static final Log log = LogFactory
         .getLog( AbstractResamplingGeneSetScore.class );

   /**
    * The squared deviation change in fit to a normal before we stop iterating.
    */
   protected static final double TOLERANCE = 1e-5;

   /**
    * Check for fit to a normal distribution every n trials. This sets n.
    */
   protected static final int NORMAL_APPROX_SAMPLE_FREQUENCY = 200;
  

   /**
    * The size we use to start skipping sizes for resampling.
    */
   protected static final int SPEEDUPSIZECUT = 20;
   protected static final double SPEDUPSIZEEXTRASTEP = 0.1;
   protected boolean useSpeedUp = true;
   protected boolean useNormalApprox = true;
   
   protected int classMaxSize = 100;
   protected int numRuns = 10000;
   protected int numClasses = 0;
   protected double histogramMax = 0;
   protected double histogramMin = 0;
   protected int classMinSize = 2;
   protected Histogram hist = null;

   

   /**
    * @param value int
    */
   public void setClassMaxSize( int value ) {
      classMaxSize = value;
   }
   
   
   /**
    * If you set this to true, when large class sizes are analyzed, not every size is measured directly.
    * 
    * @param useSpeedUp
    */
   public void setUseSpeedUp( boolean useSpeedUp ) {
      this.useSpeedUp = useSpeedUp;
   }
   
   
   public void setUseNormalApprox( boolean useNormalApprox ) {
      this.useNormalApprox = useNormalApprox;
   }
   

   /**
    * @return int
    */
   public int get_number_of_runs() {
      return numRuns;
   }

   /**
    * @return int
    */
   public int get_class_max_size() {
      return classMaxSize;
   }

   /**
    * @return double
    */
   public double get_range() {
      return histogramMax;
   }

   /**
    * @return int
    */
   public int get_class_min_size() {
      return classMinSize;
   }

   /**
    * @return histogram
    */
   public Histogram get_hist() {
      return hist;
   }

   /**
    * Determine how far off of a normal distribution we are.
    * 
    * @param mean of the normal distribution
    * @param variance of the normal distribution
    * @param class size to be checked from the table of empirical distributions.
    * @return squared deviation from the fitted normal distribution
    */
 //  protected double normalDeviation( double mean, double variance, int classSize ) {
//      double[] ha = hist.getHistogram( classSize );
//
//      DoubleArrayList hal = new DoubleArrayList( ha );
//      double sum = Descriptive.sum( hal );
//      double histMin = hist.getHistMin();
//      double binSize = hist.getBinSize();
//
//      double deviation = 0.0;
//      for ( int i = 0; i < ha.length; i++ ) {
//
//         double actual = ha[i] / sum; // fraction of area in this bin.
//
//         // the value we are evaluating the normal distribution at. mean and variance are empirical.
//         double x = histMin + binSize * i;
//
//         // expected area under this part of the histogram assuming normality
//         double nval = Probability.normal( mean, variance, x )
//               - Probability.normal( mean, variance, x - binSize );
//         
//         deviation += ( nval - actual ) * ( nval - actual );
//      }
//      return deviation;
//   }

  

}