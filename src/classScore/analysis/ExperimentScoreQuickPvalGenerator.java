package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

import classScore.Settings;
import classScore.data.Histogram;

/**
 * Does the same thing as {@link ExperimentScorePvalGenerator}but is stripped-down for using during resampling.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class ExperimentScoreQuickPvalGenerator extends
      ExperimentScorePvalGenerator {

   public ExperimentScoreQuickPvalGenerator( Settings settings,
         GeneAnnotations a, GeneSetSizeComputer csc, GONames gon, Histogram hi ) {
      super( settings, a, csc, gon, hi );
   }

   /**
    * This is stripped-down version of classPvalue. We use this when doing permutations, it is much faster.
    * 
    * @param class_name String
    * @param group_pval_map Map
    * @param probesToPvals Map
    * @throws IllegalStateException
    * @return double
    */
   public double classPvalue( String class_name, Map group_pval_map,
         Map probesToPvals ) throws IllegalStateException {

      double pval = 0.0;
      double rawscore = 0.0;
      ArrayList values = ( ArrayList ) geneAnnots.getGeneSetToProbeMap().get(
            class_name );
      Iterator classit = values.iterator();

      int in_size = ( ( Integer ) effectiveSizes.get( class_name ) ).intValue(); // effective size of this class.
      if ( in_size < settings.getMinClassSize()
            || in_size > settings.getMaxClassSize() ) {
         return -1.0;
      }

      double[] groupPvalArr = new double[in_size]; // store pvalues for items in
      // the class.
      Map record = new HashMap();

      int v_size = 0;

      // foreach item in the class.
      while ( classit.hasNext() ) {
         String probe = ( String ) classit.next(); // probe id

         if ( probesToPvals.containsKey( probe ) ) { // if it is in the data
            // set. This is invariant
            // under permutations.

            if ( settings.getUseWeights() ) {
               Double grouppval = ( Double ) group_pval_map.get( geneAnnots
                     .getProbeToGeneMap().get( probe ) ); // probe -> group
               if ( !record.containsKey( geneAnnots.getProbeToGeneMap().get(
                     probe ) ) ) { // if we
                  // haven't
                  // done
                  // this
                  // probe
                  // already.
                  record
                        .put( geneAnnots.getProbeToGeneMap().get( probe ), null ); // mark it as
                  // done.
                  groupPvalArr[v_size] = grouppval.doubleValue();
                  v_size++;
               }

            } else {
               throw new IllegalStateException(
                     "Sorry, you can't use this without weights" );

            }
         } // if in data set
      } // end of while over items in the class.

      // get raw score and pvalue.
      rawscore = ResamplingExperimentGeneSetScore.calc_rawscore( groupPvalArr,
            in_size, settings.getAnalysisMethod() );
      pval = scoreToPval( in_size, rawscore );

      if ( pval < 0 ) {
         throw new IllegalStateException(
               "Warning, a rawscore yielded an invalid pvalue: Classname: "
                     + class_name );
      }
      return pval;
   }

}