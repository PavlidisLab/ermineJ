package classScore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import baseCode.math.DescriptiveWithMissing;
import baseCode.math.Stats;

/**
    Calculates a background distribution for class sscores derived
    from randomly selected individual gene scores...and does other
    things.   Created 09/02/02.
    @author Shahmil Merchant, Paul Pavlidis
     @version $Id$
 */
public class expClassScore {
   private double[] groupPvals = null; // pvalues for groups.
   private double[] pvals = null; // pvalues for probes.
   private Map groupPvalMap; // groups -> pvalues
   private Map probePvalMap; // probes -> pval
   private boolean useWeights;
   private int classMaxSize = 100;
   private int numRuns = 10000;
   private int quantile = 50;
   private int numClasses = 0;
   private double quantfract = 0.5;
   private double histogramMax = 0; // todo: this should not be set here, should it? it is in the histogram object
   private int classMinSize = 2;
   private histogram hist = null;

   private int method;
   private static final int MEAN_METHOD = 0;
   private static final int QUANTILE_METHOD = 1;
   private static final int MEAN_ABOVE_QUANTILE_METHOD = 2;

   /**
    * Can use when pvalue column is 1, and taking logs, and defaults are ok.
    *
    * @param pvalFilename String
    * @param wt_check String
    * @param in_method String
    * @throws IOException
    */
   public expClassScore(String pvalFilename, String wt_check,
                        String in_method) throws IOException {
      this(pvalFilename, wt_check, in_method, 1, true);
   }

   /**
    * Use defaults for most things.
    *
    * @param pvalFilename String
    * @param wt_check String
    * @param in_method String
    * @param pvalcolumn int
    * @param dolog boolean
    * @throws IOException
    */
   public expClassScore(String pvalFilename, String wt_check,
                        String in_method, int pvalcolumn, boolean dolog) throws
           IOException {
      this(pvalFilename, wt_check, in_method, pvalcolumn, dolog, 100, 2, 10000,
           50);
   }

   /**
    * Set everything according to parameters.
    *
    * @param filename_pval File that contains the scores for each probe
    * @param wt_check Whether weights should be used or not
    * @param in_method The class scoring method: Mean, Quantile, etc.
    * @param pvalcolumn Which column in the data file contains the scores we
    *   will use. The first column contains probe labels and is not counted.
    * @param dolog Whether the log of the scores should be used. Use true when
    *   working with p-values
    * @param classMaxSize The largest class that will be considered. This
    *   refers to the apparent size.
    * @param classMinSize The smallest class that will be considered. This
    *   refers to the apparent size.
    * @param number_of_runs How many random trials are done when generating
    *   background distributions.
    * @param quantile A number from 1-100. This is ignored unless a quantile
    *   method is selected.
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public expClassScore(String filename_pval, String wt_check,
                        String in_method, int pvalcolumn, boolean dolog,
                        int classMaxSize, int classMinSize,
                        int number_of_runs, int quantile) throws
           IllegalArgumentException,
           IOException {
      this.setClassMaxSize(classMaxSize);
      this.classMinSize = classMinSize;
      this.numRuns = number_of_runs;
      this.setQuantile(quantile);
      this.useWeights = (Boolean.valueOf(wt_check)).booleanValue();
      this.setMethod(in_method);

      if (classMaxSize < classMinSize) {
         throw new IllegalArgumentException(
                 "Error:The maximum class size is smaller than the minimum.");
      }

      this.numClasses = classMaxSize - classMinSize + 1;

      GeneScoreReader parser = new GeneScoreReader(filename_pval, pvalcolumn,
              dolog); // makes the probe -> pval map.
      pvals = parser.get_pval(); // array of pvalues.
      probePvalMap = parser.get_map(); // reference to the probe -> pval map.
      groupPvalMap = new HashMap(); // this gets initialized by set_input_pvals
   }

   /**
    * Set everything according to parameters.
    *
    * @param filename_pval File that contains the scores for each probe
    * @param wt_check Whether weights should be used or not
    * @param in_method The class scoring method: Mean, Quantile, etc.
    * @param pvalcolumn Which column in the data file contains the scores we
    *   will use. The first column contains probe labels and is not counted.
    * @param dolog Whether the log of the scores should be used. Use true when
    *   working with p-values
    * @param classMaxSize The largest class that will be considered. This
    *   refers to the apparent size.
    * @param classMinSize The smallest class that will be considered. This
    *   refers to the apparent size.
    * @param number_of_runs How many random trials are done when generating
    *   background distributions.
    * @param quantile A number from 1-100. This is ignored unless a quantile
    *   method is selected.
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public expClassScore(Settings settings)
       throws IllegalArgumentException, IOException {
      this.classMaxSize =  settings.getMaxClassSize();
      this.classMinSize = settings.getMinClassSize();
      this.numRuns = settings.getIterations();
      this.setQuantile(settings.getQuantile());
      this.useWeights = (Boolean.valueOf(settings.getUseWeights())).booleanValue();
      this.setMethod(settings.getClassScoreMethod());

      if (classMaxSize < classMinSize) {
         throw new IllegalArgumentException(
                 "Error:The maximum class size is smaller than the minimum.");
      }

      this.numClasses = classMaxSize - classMinSize + 1;

      GeneScoreReader parser = new GeneScoreReader(settings.getScoreFile(), settings.getScorecol(), settings.getDoLog()); // makes the probe -> pval map.
      pvals = parser.get_pval(); // array of pvalues.
      probePvalMap = parser.get_map(); // reference to the probe -> pval map.
      groupPvalMap = new HashMap(); // this gets initialized by set_input_pvals
   }

   /**
    *
    * @return classScore.histogram
    */
   public histogram generateNullDistribution() {
      return this.generateNullDistribution(null);
   }

   /**
    * Used for methods which require randomly sampling classes to generate a
    * null distribution of scores.
    *
    * @return A histogram object containing a cdf that can be used to generate
    *   pvalues.
    * @param m classScoreStatus
    */
   public histogram generateNullDistribution(classScoreStatus m) {

      int i, j, k;

      int num_genes;
      double[] in_pval;

      if (hist == null) {
         throw new NullPointerException("Histogram object was null.");
      }

      // do the right thing if we are using weights.
      if (useWeights) {
         num_genes = groupPvals.length;
         in_pval = groupPvals;
      } else {
         num_genes = pvals.length;
         in_pval = pvals;
      }

      if (num_genes == 0) {
         System.err.println("No pvalues!");
         System.exit(1);
      }

      // we use this throughout.
      int[] deck = new int[num_genes];
      for (i = 0; i < num_genes; i++) {
         deck[i] = i;

         // Check for method and accordingly generate values at random.
      }
      for (i = classMinSize; i <= classMaxSize; i++) {
         double[] random_class = new double[i]; // holds data for random class.
         double rawscore = 0.0;
         for (k = 0; k < numRuns; k++) {
            baseCode.math.RandomChooser.chooserandom(random_class, in_pval, deck, num_genes, i);
            rawscore = calc_rawscore(random_class, i);
            hist.update(i - classMinSize, rawscore);
         }

         if (m != null) {
            m.setStatus("Currently running class size " + i);
         }

         try {
            Thread.currentThread().sleep(1);
         } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
         }

      }

      try {
         hist.tocdf();
      } catch (NullPointerException s) {
         System.err.println("Null pointer Exception");
         s.printStackTrace();
      } catch (ArrayIndexOutOfBoundsException s) {
         System.err.println("ArrayIndexOutOfBoundsException");
         s.printStackTrace();
      }
      return hist;
   }

   /**
    *
    * @param value int
    */
   public void setClassMaxSize(int value) {
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

   /**  */
   public void set_range() {
      histogramMax = histogram.meanOfTop2(useWeights ? groupPvals : pvals);
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
    * @return double[]
    */
   public double[] get_pvals() {
      return pvals;
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
    * @param probe String
    * @return double
    */
   public double getPval(String probe) {
      return ((Double)this.probePvalMap.get(probe)).doubleValue();
   }

   public boolean containsProbe(String probe) {
      return this.probePvalMap.containsKey(probe);
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
    * @param value int
    */
   public void setQuantile(int value) {
      quantile = value;
      quantfract = (double) quantile / 100.0;
   }

   /**
    *
    * @return int
    */
   public int get_quantile() {
      return quantile;
   }

   /**
    * Each pvalue is adjusted to the mean (or best) of all the values in the
    * 'replicate group'
    *
    * @param groupProbeMap groupProbeMap Map of groups to probes.
    * @param gp_method gp_method Which method we use to calculate scores for
    *   genes that occur more than once in the data set.
    * @throws IllegalArgumentException
    */
   public void setInputPvals(Map groupProbeMap, String gp_method) throws
           IllegalArgumentException {
      Collection groupEntries = groupProbeMap.entrySet(); // map of groups -> probes in group
      Iterator groupMapItr = groupEntries.iterator();
      double[] group_pval_temp = new double[groupProbeMap.size()];
      int counter = 0;

      while (groupMapItr.hasNext()) {
         Map.Entry groupTuple = (Map.Entry) groupMapItr.next();
         ArrayList probes = (ArrayList) groupTuple.getValue(); // list of probes in this group
         Iterator pbItr = probes.iterator();
         int in_size = 0;
         while (pbItr.hasNext()) {
            Object key = probePvalMap.get(pbItr.next()); // pvalue for the next probe in this group.
            if (key != null) {
               String pbPval = key.toString();
               if (gp_method.equals("MEAN_PVAL")) {
                  group_pval_temp[counter] += Double.parseDouble(pbPval);
               } else if (gp_method.equals("BEST_PVAL")) {
                  group_pval_temp[counter] = Math.max(Double.parseDouble(pbPval),
                          group_pval_temp[counter]);
               } else {
                  throw new IllegalArgumentException(
                          "Illegal selection for groups score method. Valid choices are MEAN_PVAL and BEST_PVAL");
               }
               in_size++;
            }
         }

         if (in_size != 0) {

            if (gp_method.equals("MEAN_PVAL")) {
               group_pval_temp[counter] /= in_size; // take the mean

            }
            Object obb = groupTuple.getKey();
            Double dbb = new Double(group_pval_temp[counter]);
            if (groupTuple.getKey() != null) {
               groupPvalMap.put(obb, dbb);
            }
            counter++;
         }
      } //end of while

      double[] group_pval = new double[counter]; // counter = the number of group_id that actually appears in pval file
      for (int i = 0; i < counter; i++) {
         group_pval[i] = group_pval_temp[i];
      }
      groupPvals = group_pval;

      this.set_range(); // figure out the max pvalue possible.
      this.hist = new histogram(numClasses, classMinSize, numRuns,
                                histogramMax);
   }

   /**
     @return Map of groups to pvalues.
    */

   public Map get_group_pval_map() {
      return groupPvalMap;
   }

   /**
     @param shuffle Whether the map should be scrambled first. If
     so, then groups are randomly associated with scores, but the
     actual values are the same.
     @return Map of groups of genes to pvalues.
    */
   public Map get_group_pval_map(boolean shuffle) {
      if (shuffle) {
         Map scrambled_map = new LinkedHashMap();
         Set keys = groupPvalMap.keySet();
         Iterator it = keys.iterator();

         Collection values = groupPvalMap.values();
         Vector valvec = new Vector(values);
         Collections.shuffle(valvec);

         // randomly associate keys and values
         int i = 0;
         while (it.hasNext()) {
            scrambled_map.put(it.next(), valvec.get(i));
            i++;
         }
         return scrambled_map;

      } else {
         return groupPvalMap;
      }
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
   public Map get_map(boolean shuffle) {

      if (shuffle) {
         Map scrambled_probe_pval_map = new LinkedHashMap();

         Set keys = probePvalMap.keySet();
         Iterator it = keys.iterator();

         Collection values = probePvalMap.values();
         Vector valvec = new Vector(values);
         Collections.shuffle(valvec);

         // randomly associate keys and values
         int i = 0;
         while (it.hasNext()) {
            scrambled_probe_pval_map.put(it.next(), valvec.get(i));
            //		 System.err.println(it.next() + " " + valvec.get(i));
            i++;
         }
         return scrambled_probe_pval_map;

      } else {
         return probePvalMap;
      }
   }

   /**
    *
    * @param probe_id String
    * @return double
    */
   public double get_value_map(String probe_id) {
      double value = 0.0;
      if (probePvalMap.get(probe_id) != null) {
         value = Double.parseDouble((probePvalMap.get(probe_id)).toString());
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
   public double calc_rawscore(double[] genevalues, int effsize) throws
           IllegalArgumentException {

      if (method == MEAN_METHOD) {
         return DescriptiveWithMissing.mean(genevalues, effsize);
      } else {
         int index = (int) Math.floor(quantfract * effsize);
         if (method == QUANTILE_METHOD) {
            return Stats.quantile(index, genevalues, effsize);
         } else if (method == MEAN_ABOVE_QUANTILE_METHOD) {
            return Stats.meanAboveQuantile(index, genevalues,
                    effsize);
         } else {
            throw new IllegalStateException(
                    "Unknown raw score calculation method selected");
         }
      }
   }

   /**
    *
    * @return histogram
    */
   public histogram get_hist() {
      return hist;
   }

   /**
    *
    * @param meth String
    * @throws IllegalArgumentException
    */
   private void setMethod(String meth) throws IllegalArgumentException {
      if (meth.equals("MEAN_METHOD")) {
         method = MEAN_METHOD;
      } else if (meth.equals("QUANTILE_METHOD")) {
         method = QUANTILE_METHOD;
      } else if (meth.equals("MEAN_ABOVE_QUANTILE_METHOD")) {
         method = MEAN_ABOVE_QUANTILE_METHOD;
      } else {
         throw new IllegalArgumentException("Invalid method entered: " + meth);
      }
   }

}
