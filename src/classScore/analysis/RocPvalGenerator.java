package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import classScore.GONameReader;
import classScore.classresult;
import classScore.expClassScore;
import classScore.histogram;
import util.Stats;

/**
 * <p>Copyright (c) 2004</p>
 * <p>Institution: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class RocPvalGenerator
    extends ExperimentScorePvalGenerator {

   protected Map effectiveSizes = null;
   protected expClassScore probePvalMapper;
   protected Map classToProbe;
   protected histogram hist;
   protected boolean weight_on = true;
   protected Map probeGroups;
   protected int inputSize;
   protected GONameReader goName;
   protected Map actualSizes = null;

   public RocPvalGenerator(Map ctp, Map pg, boolean w,
                           histogram hi, expClassScore pvm, ClassSizeComputer csc, GONameReader gon) {
      super(ctp, pg, w, hi, pvm, csc, gon);
   }

   /**
    * Get results for one class, based on class id. The other arguments are
    * things that are not constant under permutations of the data.
    *
    * @param class_name a <code>String</code> value
    * @param probesToPvals a <code>Map</code> value
    * @param input_rank_map a <code>Map</code> value
    * @return a <code>classresult</code> value
    */
   public classresult classPval(String class_name,
                                Map probesToPvals, Map input_rank_map) {

      //variables for outputs
      Map target_ranks = new HashMap();

      int effSize = (int) ( (Integer) effectiveSizes.get(class_name)).intValue(); // effective size of this class.
      if (effSize < probePvalMapper.get_class_min_size() ||
          effSize > probePvalMapper.get_class_max_size()) {
         return null;
      }

      ArrayList values = (ArrayList) classToProbe.get(class_name);
      Iterator classit = values.iterator();

      // record.clear();
      //  target_ranks.clear();
      Object ranking = null;

      // foreach item in the class.
      while (classit.hasNext()) {

         String probe = (String) classit.next(); // probe id

         if (probesToPvals.containsKey(probe)) { // if it is in the data set. This is invariant under permutations.

            if (weight_on == true) {
               ranking = input_rank_map.get(probeGroups.get(probe)); // rank of this probe group.
               if (ranking != null) {
                  target_ranks.put(ranking, null); // ranks of items in this class.
               }

            } else { // no weights
               ranking = input_rank_map.get(probe);
               if (ranking != null) {
                  target_ranks.put(ranking, null);
               }

            }
         } // if in data set
      } // end of while over items in the class.

      double area_under_roc = Stats.arocRate(inputSize, target_ranks);
      double roc_pval = Stats.rocpval(target_ranks.size(), area_under_roc);

      // set up the return object.
      classresult res = new classresult(class_name,
                                        goName.get_GoName_value_map(class_name),
                                        (int) ( (Integer) actualSizes.get(
          class_name)).intValue(), effSize);
      res.setaroc(area_under_roc);
      res.setarocp(roc_pval);
      return res;

   }

   /* scoreClass */

}
