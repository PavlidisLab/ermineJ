package classScore.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import classScore.*;
import classScore.data.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Institution:: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class ClassSizeComputer {
   protected Map effectiveSizes = null;
   protected Map actualSizes = null;


   protected expClassScore probePvalMapper;
   protected Map classToProbe;
   protected boolean weight_on = true;
   protected Map probeGroups;

   public ClassSizeComputer(expClassScore ppm, GeneAnnotations geneData, boolean w) {
      this.probePvalMapper = ppm;
      this.weight_on = w;
      this.classToProbe = geneData.getClassToProbeMap();
      this.probeGroups = geneData.getProbeToGeneMap();
      effectiveSizes = new HashMap();
      actualSizes = new HashMap();
   }

   /**
     * Calculate class sizes for all classes - both effective and actual size
    */
   public void getClassSizes() {
      Collection entries = classToProbe.entrySet(); // go -> probe map. Entries are the class names.
      Iterator it = entries.iterator();
      Map probetopval = probePvalMapper.get_map(); // probe->pval map. We do not use the pvalues here, just a list of probes.
      Map record = new HashMap();
      int size;
      int v_size;

      while (it.hasNext()) { // for each class.
         Map.Entry e = (Map.Entry) it.next(); // next class.
         String className = (String) e.getKey(); // id of the class (GO:XXXXXX)
         ArrayList values = (ArrayList) e.getValue(); // items in the class.
         Iterator I = values.iterator();

         record.clear();
         size = 0;
         v_size = 0;

         while (I.hasNext()) { // foreach item in the class.
            String probe = (String) I.next();

            if (probe != null) {
               if (probetopval.containsKey(probe)) { // if it is in the data set
                  size++;

                  if (weight_on) { //routine for weights
                     // compute pval for every replicate group
                     if (probePvalMapper.get_group_pval_map().containsKey(probeGroups.
                         get(
                         probe)) && !record.containsKey(probeGroups.get(probe))) { // if we haven't done this probe already.
                        record.put(probeGroups.get(probe), null); // mark it as done for this class.
                        v_size++; // this is used in any case.
                     }
                  }
               }
            } // end of null check
         } // end of while over items in the class.

         if (!weight_on) {
            v_size = size;

         }
         effectiveSizes.put(className, new Integer(v_size));
         actualSizes.put(className, new Integer(size));
      }

  //    log.info("Got class sizes");
   }

   /**
    *
    * @return Map
    */
   public Map getEffectiveSizes() {
      return effectiveSizes;
   }

   /**
    *
    * @return Map
    */
   public Map getActualSizes() {
      return actualSizes;
   }

   /* class sizes */

}
