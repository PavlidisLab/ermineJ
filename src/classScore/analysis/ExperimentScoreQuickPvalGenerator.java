package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import classScore.data.expClassScore;
import classScore.data.Histogram;

/**
 * Does the same thing as {@link ExperimentScorePvalGenerator}but is
 * stripped-down for using during resampling.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class ExperimentScoreQuickPvalGenerator extends
      ExperimentScorePvalGenerator {

   public ExperimentScoreQuickPvalGenerator( Map ctp, Map pg, boolean w,
         Histogram hi, expClassScore pvm, GeneSetSizeComputer csc ) {
      super( ctp, pg, w, hi, pvm, csc, null );
   }

   /**
    * This is stripped-down version of classPvalue. We use this when doing
    * permutations, it is much faster.
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
      ArrayList values = ( ArrayList ) classToProbe.get( class_name );
      Iterator classit = values.iterator();

      int in_size = ( int ) ( ( Integer ) effectiveSizes.get( class_name ) )
            .intValue(); // effective size of this class.
      if ( in_size < probePvalMapper.get_class_min_size()
            || in_size > probePvalMapper.get_class_max_size() ) {
         return -1.0;
      }

      double[] groupPvalArr = new double[in_size]; // store pvalues for items in
                                                   // the class.
      Map record = new HashMap();

      int v_size = 0;

      // foreach item in the class.
      // todo: see if this loop can be optimized. Probably. It's important when
      // we are doing random trials that this go fast.
      while ( classit.hasNext() ) {
         String probe = ( String ) classit.next(); // probe id

         if ( probesToPvals.containsKey( probe ) ) { // if it is in the data
                                                     // set. This is invariant
                                                     // under permutations.

            if ( weight_on == true ) {
               Double grouppval = ( Double ) group_pval_map.get( probeGroups
                     .get( probe ) ); // probe -> group
               if ( !record.containsKey( probeGroups.get( probe ) ) ) { // if we
                                                                        // haven't
                                                                        // done
                                                                        // this
                                                                        // probe
                                                                        // already.
                  record.put( probeGroups.get( probe ), null ); // mark it as
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
      rawscore = probePvalMapper.calc_rawscore( groupPvalArr, in_size );
      pval = scoreToPval( in_size, rawscore );

      if ( pval < 0 ) {
         throw new IllegalStateException(
               "Warning, a rawscore yielded an invalid pvalue: Classname: "
                     + class_name );
      }
      return pval;
   }

}