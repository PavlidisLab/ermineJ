package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cern.jet.stat.Probability;
import classScore.Settings;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneSetResult;

/**
 * Compute gene set scores based on over-representation analysis (ORA).
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 * @todo add tests
 */

public class OraPvalGenerator extends AbstractGeneSetPvalGenerator {

   protected double user_pvalue;
   protected int inputSize;
   protected int numOverThreshold = 0; // number of genes over the threshold
   protected int numUnderThreshold = 0; // number of genes below the threshold

   public OraPvalGenerator( Settings settings, GeneAnnotations a,
         GeneSetSizeComputer csc, int not, int nut, GONames gon, int inputSize ) {

      super( settings, a, csc, gon );
      this.numOverThreshold = not;
      this.numUnderThreshold = nut;
      this.inputSize = inputSize;
      
      if ( settings.getUseLog() ) {
         this.user_pvalue = -Math.log( settings.getPValThreshold() );
      } else {
         this.user_pvalue = settings.getPValThreshold();
      }
   }

   /**
    * Get results for one class, based on class id. The other arguments are things that are not constant under
    * permutations of the data.
    *  
    */
   public GeneSetResult classPval( String class_name, Map groupToPvalMap,
         Map probesToPvals ) {

      //inputs for hypergeometric distribution
      int successes = 0;
      int failures = 0;

      //variables for outputs
      double hyper_pval = -1.0;

      int effSize = ( ( Integer ) effectiveSizes.get( class_name ) ).intValue(); // effective
      // size
      // of
      // this
      // class.
      if ( effSize < settings.getMinClassSize()
            || effSize > settings.getMaxClassSize() ) {
         return null;
      }

      ArrayList values = ( ArrayList ) geneAnnots.getClassToProbeMap().get(
            class_name );
      Iterator classit = values.iterator();
      double[] groupPvalArr = new double[effSize]; // store pvalues for items in
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
               Double grouppval = ( Double ) groupToPvalMap.get( geneAnnots
                     .getProbeToGeneMap().get( probe ) ); // probe -> group
               if ( !record.containsKey( geneAnnots.getProbeToGeneMap().get(
                     probe ) ) ) {

                  record
                        .put( geneAnnots.getProbeToGeneMap().get( probe ), null );
                  groupPvalArr[v_size] = grouppval.doubleValue();

                  if ( groupPvalArr[v_size] >= user_pvalue ) {
                     successes++; // successs.
                  } else {
                     failures++; // failure.
                  }
                  v_size++;
               }

            } else { // no weights

               /*
                * pvalue for this probe. This will not be null if things have been done correctly so far. This is the
                * only place we need the raw pvalue for a probe.
                */
               Double pbpval = ( Double ) probesToPvals.get( probe );

               // hypergeometric pval info.
               if ( pbpval.doubleValue() >= user_pvalue ) {
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
      double pos_prob = ( double ) effSize / ( double ) inputSize;
      double expected = numOverThreshold * pos_prob;
      // lower tail.
      if ( successes < expected || pos_prob == 0.0 ) { // fewer than expected,
         // or we didn't/cant get
         // anything.
         hyper_pval = Probability.binomial( successes, numOverThreshold,
               pos_prob );
      } else {
         // Upper tail.
         hyper_pval = Probability.binomialComplemented( successes,
               numOverThreshold, pos_prob );
      }

      // set up the return object.
      GeneSetResult res = new GeneSetResult( class_name, goName
            .getNameForId( class_name ), ( ( Integer ) actualSizes
            .get( class_name ) ).intValue(), effSize );
      res.setScore( successes );
      res.setPValue( hyper_pval );
      return res;

   }

   /* scoreClass */

}