package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.math.ROC;
import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * Compute gene set p values based on the receiver-operator characterisic (ROC). This uses a computation developed by PP
 * for estimating the significance of an area under the ROC curve.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class RocPvalGenerator extends AbstractGeneSetPvalGenerator {

   protected int inputSize;
   /** @todo where is this set? */
   protected ResamplingExperimentGeneSetScore probePvalMapper;
   protected Histogram histogram;

   public RocPvalGenerator( Settings set, GeneAnnotations an,
         GeneSetSizeComputer csc, GONames gon, Histogram hi,
         ResamplingExperimentGeneSetScore pvm ) {
      super( set, an, csc, gon );
      this.histogram = hi;
      this.probePvalMapper = pvm;
   }

   /**
    * Get results for one class, based on class id. The other arguments are things that are not constant under
    * permutations of the data.
    * 
    * @param class_name a <code>String</code> value
    * @param probesToPvals a <code>Map</code> value
    * @param input_rank_map a <code>Map</code> value
    * @return a <code>classresult</code> value
    */
   public GeneSetResult classPval( String class_name, Map probesToPvals,
         Map input_rank_map ) {

      //variables for outputs
      Map target_ranks = new HashMap();

      int effSize = ( ( Integer ) effectiveSizes.get( class_name ) ).intValue(); // effective size of this class.
      if ( effSize < probePvalMapper.get_class_min_size()
            || effSize > probePvalMapper.get_class_max_size() ) {
         return null;
      }

      ArrayList values = ( ArrayList ) geneAnnots.getClassToProbeMap().get(
            class_name );
      Iterator classit = values.iterator();
      Object ranking = null;

      // foreach item in the class.
      while ( classit.hasNext() ) {

         String probe = ( String ) classit.next(); // probe id

         if ( probesToPvals.containsKey( probe ) ) { // if it is in the data
            // set. This is invariant
            // under permutations.

            if ( settings.getUseWeights() ) {
               ranking = input_rank_map.get( geneAnnots.getProbeToGeneMap()
                     .get( probe ) ); // rank
               // of
               // this
               // probe
               // group.
               if ( ranking != null ) {
                  target_ranks.put( ranking, null ); // ranks of items in this
                  // class.
               }

            } else { // no weights
               ranking = input_rank_map.get( probe );
               if ( ranking != null ) {
                  target_ranks.put( ranking, null );
               }

            }
         } // if in data set
      } // end of while over items in the class.

      double areaUnderROC = ROC.aroc( inputSize, target_ranks );
      double roc_pval = ROC.rocpval( target_ranks.size(), areaUnderROC );

      // set up the return object.
      GeneSetResult res = new GeneSetResult( class_name, goName
            .getNameForId( class_name ), ( ( Integer ) actualSizes
            .get( class_name ) ).intValue(), effSize );
      res.setScore( areaUnderROC );
      res.setPValue( roc_pval );
      return res;

   }

   /* scoreClass */

}