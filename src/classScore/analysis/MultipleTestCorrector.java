package classScore.analysis;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import classScore.data.GeneAnnotations;
import classScore.data.classresult;
import classScore.data.expClassScore;
import classScore.data.histogram;

/**
 * Perform multiple test correction on class scores.
 * Copyright (c) 2004 Columbia University</p>
 * @author Paul Pavlidis
 * @version $Id$
 */

public class MultipleTestCorrector {

   private Vector sortedclasses;
   private Map results;
   private expClassScore probePvalMapper;
   private boolean weight_on = true;
   private histogram hist;
   private GeneAnnotations geneData;
   private GeneSetSizeComputer csc;
   private NumberFormat nf = NumberFormat.getInstance();
   /**
    *
    * @param sc Vector
    * @param r Map
    * @param ppm expClassScore
    * @param w boolean
    * @param h histogram
    * @param csc ClassSizeComputer
    */
   public MultipleTestCorrector(Vector sc, Map r, expClassScore ppm, boolean w,
                                histogram h, GeneAnnotations geneData, GeneSetSizeComputer csc) {
      this.sortedclasses = sc;
      this.results = r;
      this.probePvalMapper = ppm;
      this.weight_on = w;
      this.hist = h;
      this.geneData = geneData;
      this.csc = csc;
   }

   /**
          Bonferroni correction of class pvalues.
    */
   public void bonferroni() {
      int numclasses = sortedclasses.size();
      double corrected_p;
      for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) {
         String nextclass = (String) it.next();
         classresult res = (classresult) results.get(nextclass);
         double actual_p = res.getPvalue();
         corrected_p = actual_p * numclasses;
         if (corrected_p > 1.0) {
            corrected_p = 1.0;
         }

         res.setpvalue_corr(corrected_p);
      }
   }

   /**
    * Benjamini-Hochberg correction of pvalues. This puts values of 1 or 0 int
    * the corrected p, indicating whether the FDR has been met by a particular
    * pvalue.
    *
    * @param fdr double
    */
   public void benjaminihochberg(double fdr) {
      int numclasses = sortedclasses.size();
      int n = numclasses;
      boolean threshpassed = false;

      Collections.reverse(sortedclasses); // start from the worst class.
      double corrected_p;
      for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) {
         String nextclass = (String) it.next();
         classresult res = (classresult) results.get(nextclass);
         double actual_p = res.getPvalue();

         double thresh = fdr * n / numclasses;

         if (actual_p < thresh || threshpassed) {
            res.setpvalue_corr(1.0);
            threshpassed = true;
         } else {
            res.setpvalue_corr(0.0);
         }

         n--;
      }
      Collections.reverse(sortedclasses); // put it back.
   }

   /**
      Westfall-Young pvalue correction. Based on algorithm 2.8, pg 66
      of 'resampling-based multiple testing'.
      0. Sort the pvalues for the real data (assume worst pvalue is first)
        1. Make an array of count variables, one for each class, intialize to zero.
      loop: (n=10,000).
      2. Generate class pvalues for randomized values (see above);
      3. Iterate over this in the same order as the actual order.
      4. Define successive minima: (q is the trial; p is real, already ranked)
      a. qk = pk (class with worst pvalue)
      b. qk-1 = min (qk, pk-1)
      ...
      5. at each step a.... if qi <= pi, count_i++
      end loop.
      6. p_i* = count_i/n
      7. enforce monotonicity by using successive maximization.
      @param trials  How many random trials to do. According to W-Y, it should be >=10,000.
    */
   public void westfallyoung(int trials) {

      int[] counts = new int[sortedclasses.size()];
      for (int i = 0; i < sortedclasses.size(); i++) {
         counts[i] = 0;
      }

      Collections.reverse(sortedclasses); // start from the worst class.
      HashMap permscores;

      GeneSetPvalSeriesGenerator cver = new GeneSetPvalSeriesGenerator(geneData, weight_on,
          hist, probePvalMapper, csc, null);

      boolean verbose = false;

      for (int i = 0; i < trials; i++) {
         //	    System.err.println("Trial: " + i );

         Map scgroup_pval_map = probePvalMapper.get_group_pval_map(true); // shuffle the association of pvalues to genes.

         // shuffle. Stupidity: this is a different permutation
         // than the group one. If we are using weights, it DOES
         // NOT MATTER - it doesn't even have to be shuffled (it is
         // used only to check for presence of a probe in the data
         // set). If we are not using weights, it only affects the
         // hypergeometric pvalues. (todo: add correction for those
         // values) So we don't even bother shuffling it.
         Map scprobepvalmap = probePvalMapper.get_map();

         // Just for AROC:
         /* doesn't seem to get used??? (homin 7/25)
         Map scinput_rank_map;
         if (weight_on) {
            scinput_rank_map = Rank.rankTransform(scgroup_pval_map);
         } else {
            scinput_rank_map = Rank.rankTransform(scprobepvalmap);
         }
         */

         /// permscores contains a list of the p values for the shuffled data.
         permscores = cver.class_v_pval_generator(scgroup_pval_map, scprobepvalmap); // end of step 1.

         int j = 0;
         double permp = 0.0;
         Double m = new Double(1.0);
         //	    Double m = (Double)permscores.get(j); // first sim value (for worst class in real data)
         double q = m.doubleValue(); // pvalue for the previous permutation, initialized here.
         double qprev = q;
         double actual_p = 0.0;
         String nextclass = "";

         // successive minima of step 2, pg 66. Also does step 3.
         for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) { // going in the correct order for the 'real' data, starting from the worst class.

            nextclass = (String) it.next();

            classresult res = (classresult) results.get(nextclass);
            actual_p = res.getPvalue(); // pvalue for this class on real data.

            m = (Double) permscores.get(nextclass);
            permp = m.doubleValue(); // randomized pvalue for this class.

            q = Math.min(qprev, permp); // The best values for
            // permp for this trial
            // bubbles up. The way
            // this works is that if
            // two classes are highly
            // correlated, their
            // permuted pvalues will
            // tend to be the
            // same. Then, whatever
            // decision is made here
            // will tend to be the
            // same decision made for
            // the next (correlated
            // class). That is how the
            // resulting corrected p
            // values for correlated
            // classes are correlated.

            /* step 3 */
            if (q <= actual_p) { // for bad classes, this will often be true. Otherwise we see it less.
               counts[j]++;
            }

            /* the following tests two classes which are very similar. Their permutation p values should be correlated */
            /*		if (nextclass.equals("GO:0006956")) {
                System.err.print("\tGO:0006956\t" +   nf.format(permp) + "\n");
               }
               if (nextclass.equals("GO:0006958")) {
                System.err.print("\tGO:0006958\t" + nf.format(permp));
               }
             */

            if (verbose && j == sortedclasses.size() - 1) { // monitor what happens to the best class.
               System.err.println("Sim " + i + " class# " + j + " " + nextclass +
                                  " size=" + res.getEffectiveSize() + " q=" +
                                  nf.format(q) +
                                  " qprev=" + nf.format(qprev) + " pperm=" +
                                  nf.format(permp) + " actp=" + nf.format(actual_p) +
                                  " countj=" + counts[j] + " currentp=" +
                                  (double) counts[j] / (i + 1));

            }
            j++;
            qprev = q;
         }

         if (0 == i % 100) {
            System.err.println(i + " Westfall-Young trials, " + (trials - i) +
                               " to go.");
         }

      }

      Collections.reverse(sortedclasses); // now the best class is first.

      int j = sortedclasses.size() - 1; // index of the best class (last one tested above).
      double corrected_p = counts[sortedclasses.size() - 1] / trials; // pvalue for the best class.
      double previous_p = corrected_p;

      // Step 4 and enforce monotonicity, pg 67 (step 5)
      for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) { // starting from the best class.
         classresult res = (classresult) results.get( (String) it.next());
         corrected_p = Math.max( (double) counts[j] / (double) trials, previous_p); // first iteration, these are the same.

         if (verbose) { // print the counts for each class.
            System.err.println(j + " " + counts[j] + " " + trials + " " +
                               corrected_p);

         }
         res.setpvalue_corr(corrected_p);
         previous_p = corrected_p;
         j--;
      }
   }

}
