package classScore.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import classScore.GONameReader;
import classScore.classresult;
import classScore.expClassScore;
import cern.jet.stat.Probability;
/**
* <p>Copyright (c) 2004</p>
 * <p>Institution: Columbia University</p>
 * @author Paul Pavlidis
 * @version 1.0
 *  @todo add tests
 * @todo returns a new result object, probably not what we want (?)
 */

public class OraPvalGenerator
    extends ExperimentScorePvalGenerator {
   protected Map effectiveSizes = null;
   protected expClassScore probePvalMapper;
   protected Map classToProbe;

   protected boolean weight_on = true;
   protected Map probeGroups;
   protected double user_pvalue;
   protected int inputSize;
   protected int numOverThreshold = 0; // number of genes over the threshold
   protected int numUnderThreshold = 0; // number of genes below the threshold
   protected GONameReader goName;
   protected Map actualSizes = null;

   /**
         Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution. This
         is a constant under permutations, but depends on weights.
       @param inp_entries The pvalues for the probes (no weights) or groups (weights)
    @todo make this private and called by OraPvalGenerator.
       */
      public void hgSizes(Collection inp_entries) {

         Iterator itr = inp_entries.iterator();
         while (itr.hasNext()) {
            Map.Entry m = (Map.Entry) itr.next();
            double groupval = Double.parseDouble((m.getValue()).toString());

            if (groupval >= user_pvalue) {
               numOverThreshold++;
            } else {
               numUnderThreshold++;
            }
         }
         System.err.println(numOverThreshold + " genes are above the threshold " +
                            user_pvalue);
      }




   public OraPvalGenerator(Map ctp, Map pg, boolean w,
                                            expClassScore pvm, ClassSizeComputer csc, GONameReader gon, int nuot, int nuut) {
      super(ctp, pg, w, null, pvm, csc, gon);
      this.numOverThreshold = nuot;
      this.numUnderThreshold = nuut;
   }


   /**
    * Get results for one class, based on class id. The other arguments are
    * things that are not constant under permutations of the data.
    *
    * @param class_name a <code>String</code> value
    * @param groupToPvalMap a <code>Map</code> value
    * @param probesToPvals a <code>Map</code> value
    * @param input_rank_map a <code>Map</code> value
    * @return a <code>classresult</code> value
    */
   public classresult classPval(String class_name, Map groupToPvalMap,
                                Map probesToPvals, Map input_rank_map) {
      //inputs for hypergeometric distribution
      int successes = 0; // number of genes in this class which are above the threshold
      int failures = 0; // number of genes in this calss which are below the threshold

      //variables for outputs
      double hyper_pval = -1.0;

      int effSize = (int) ( (Integer) effectiveSizes.get(class_name)).intValue(); // effective size of this class.
      if (effSize < probePvalMapper.get_class_min_size() ||
          effSize > probePvalMapper.get_class_max_size()) {
         return null;
      }

      ArrayList values = (ArrayList) classToProbe.get(class_name);
      Iterator classit = values.iterator();
      double[] groupPvalArr = new double[effSize]; // store pvalues for items in the class.
      Map record = new HashMap();
      Object ranking = null;

      int v_size = 0;

      // foreach item in the class.
      while (classit.hasNext()) {

         String probe = (String) classit.next(); // probe id

         if (probesToPvals.containsKey(probe)) { // if it is in the data set. This is invariant under permutations.

            if (weight_on == true) {
               Double grouppval = (Double) groupToPvalMap.get(probeGroups.get(probe)); // probe -> group
               if (!record.containsKey(probeGroups.get(probe))) { // if we haven't done this probe already.
                  record.put(probeGroups.get(probe), null); // mark it as done.
                  groupPvalArr[v_size] = grouppval.doubleValue();

                  //  (hypergeometric) if the user_pval is met by this probe, we count it
                  if (groupPvalArr[v_size] >= user_pvalue) {
                     successes++; // successs.
                  } else {
                     failures++; // failure.
                  }
                  v_size++;
               }

            } else { // no weights
               Double pbpval = (Double) probesToPvals.get(probe); // pvalue for this probe. This will not be null if things have been done correctly so far. This is the only place we need the raw pvalue for a probe.

               // hypergeometric pval info.
               if (pbpval.doubleValue() >= user_pvalue) {
                  successes++; // successs.
               } else {
                  failures++; // failure.
               }

            }
         } // if in data set
      } // end of while over items in the class.

      // Hypergeometric p value calculation.
      // successes=number of genes in class which meet criteria
 // (successes); numOverThreshold= number of genes which
 // meet criteria (trials); pos_prob: fractional size of
 // class wrt data size.
      double pos_prob = (double) effSize / (double) inputSize;
      double expected = (double) numOverThreshold * pos_prob;
      // lower tail.
      if (successes < expected || pos_prob == 0.0) { // fewer than expected, or we didn't/cant get anything.
         hyper_pval =
             (double) (Probability.binomial(numOverThreshold, successes,
                                                pos_prob));
      } else {
         // Upper tail.
         hyper_pval = Probability.binomialComplemented(numOverThreshold, successes,
                                               pos_prob);
      }

      // set up the return object.
      classresult res = new classresult(class_name,
                                        goName.get_GoName_value_map(class_name),
                                        (int) ( (Integer) actualSizes.get(
          class_name)).intValue(), effSize);
      res.sethypercut(successes);
      res.sethyperp(hyper_pval);
      return res;

   }

   /* scoreClass */

}
