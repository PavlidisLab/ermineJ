package classScore.analysis;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import cern.jet.stat.Probability;
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

   /**
    * The squared deviation change in fit to a normal before we stop iterating.
    */
   protected static final double TOLERANCE = 1e-5;
   
   protected int classMaxSize = 100;
   protected int numRuns = 10000;
   protected int numClasses = 0;
   protected double histogramMax = 0;
   protected int classMinSize = 2;
   protected Histogram hist = null;

   /**
    * @param value int
    */
   public void setClassMaxSize( int value ) {
      classMaxSize = value;
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
    * @param values
    * @param mean
    * @param variance
    * @return
    */
   protected double normalDeviation(  double mean, double variance, int classSize ) {
     double[] ha = hist.getHistogram(classSize);
     
     DoubleArrayList hal = new DoubleArrayList(ha);
     double sum = Descriptive.sum(hal);
     double histMin = hist.getHistMin();
     double binSize = hist.getBinSize();
     
     double deviation = 0.0;
     for ( int i = 0; i < ha.length; i++ ) {
        
        double actual = ha[i]/sum; // fraction of area in this bin.
        
        // the value we are evaluating the normal distribution at. mean and variance are empirical.
        double x = histMin + binSize * i;
        
        // expected area under this part of the histogram assuming normality
        double nval = Probability.normal(mean, variance, x) - Probability.normal(mean, variance, x - binSize);
        
      //  System.err.println(nval + "\t"+ actual);
        
        deviation += (nval - actual)*(nval - actual);
     }
    // System.err.println("Deviation:\t" + deviation );
     return deviation;
   }
   

}