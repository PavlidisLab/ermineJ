package classScore.analysis;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

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

public class ClassPvalSetGenerator {

   private Vector sortedclasses;
     private Map results;
     private expClassScore probePvalMapper;
     private boolean weight_on = true;
     private histogram hist;
     private Map probeGroups;
     private Map classToProbe;
     private ClassSizeComputer csc;
     private NumberFormat nf = NumberFormat.getInstance();
     private GONames goName;

     public ClassPvalSetGenerator(GeneAnnotations geneData, boolean w, histogram hi,
                                         expClassScore pvm, ClassSizeComputer csc, GONames gon) {
        this.weight_on = w;
        this.classToProbe = geneData.getClassToProbeMap();
        this.probeGroups = geneData.getProbeToGeneMap();
        this.hist = hi;
        this.probePvalMapper = pvm;
        this.csc = csc;
        if (gon != null) {
           this.goName = gon;
        }
        results = new HashMap();
     }

     public Map getResults() {
        return results;
     }

   /**
     * Generate a complete set of class results. The arguments are not
     constant under pemutations. The second is only needed for the
     aroc method. This is to be used only for the 'real' data since
     it modifies 'results',
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     * @param input_rank_map a <code>Map</code> value
     */
    public void classPvalGenerator(Map group_pval_map, Map probesToPvals,
                                   Map input_rank_map) {
       Collection entries = classToProbe.entrySet(); // go -> probe map. Entries are the class names.
       Iterator it = entries.iterator(); // the classes.

       ExperimentScorePvalGenerator cpv = new ExperimentScorePvalGenerator(classToProbe, probeGroups, weight_on, hist, probePvalMapper, csc, goName);

       // For each class.
       while (it.hasNext()) {
          Map.Entry e = (Map.Entry) it.next();
          String class_name = (String) e.getKey();
          classresult res = cpv.classPval(class_name, group_pval_map, probesToPvals, input_rank_map);
          if (res != null) {
             results.put(class_name, res);
          }
       }
    }

    /* class_pval_generator */

    /**
         Same thing as class_pval_generator, but returns a collection of
         scores (pvalues) (see below) instead of adding them to the results
         object. This is used to get class pvalues for permutation
         analysis.
     */
    public HashMap class_v_pval_generator(Map group_pval_map, Map probesToPvals) {
       Collection entries = classToProbe.entrySet(); // go -> probe map. Entries are the class names.
       Iterator it = entries.iterator(); // the classes.
       //	Vector results = new Vector();
       HashMap results = new HashMap();

       ExperimentScoreQuickPvalGenerator cpv = new ExperimentScoreQuickPvalGenerator(classToProbe, probeGroups, weight_on, hist, probePvalMapper, csc);

       // For each class.
       while (it.hasNext()) {
          Map.Entry e = (Map.Entry) it.next();
          String class_name = (String) e.getKey();
          double pval = cpv.classPvalue(class_name, group_pval_map, probesToPvals);

          if (pval >= 0.0) {
             results.put(class_name, new Double(pval));

          }
       }
       return results;
    }


}
