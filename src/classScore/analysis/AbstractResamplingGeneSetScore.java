package classScore.analysis;

import classScore.data.Histogram;


/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public abstract class AbstractResamplingGeneSetScore implements NullDistributionGenerator {

   protected int classMaxSize = 100;
   protected int numRuns = 10000;
   protected int numClasses = 0;
   protected double histogramMax = 0;
   protected int classMinSize = 2;
   protected Histogram hist = null;

  
   /**
    * 
    * @param value int
    */
   public void setClassMaxSize( int value ) {
      classMaxSize = value;
   }

   /**
    * 
    * @return int
    */
   public int get_number_of_runs() {
      return numRuns;
   }

   /**
    * 
    * @return int
    */
   public int get_class_max_size() {
      return classMaxSize;
   }

   /**
    * 
    * @return double
    */
   public double get_range() {
      return histogramMax;
   }

  

   /**
    * 
    * @return int
    */
   public int get_class_min_size() {
      return classMinSize;
   }


   /**
    * 
    * @return histogram
    */
   public Histogram get_hist() {
      return hist;
   }

   
   
}
