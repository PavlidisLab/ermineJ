package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.math.SpecFunc;

import cern.jet.math.Arithmetic;
import cern.jet.stat.Probability;
import classScore.Settings;
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

   protected double geneScoreThreshold;
   protected int inputSize;
   protected int numOverThreshold = 0;
   protected int numUnderThreshold = 0;

   public OraPvalGenerator( Settings settings, GeneAnnotations a,
         GeneSetSizeComputer csc, int not, int nut, GONames gon, int inputSize ) {

      super( settings, a, csc, gon );
      this.numOverThreshold = not;
      this.numUnderThreshold = nut;
      this.inputSize = inputSize;

      if ( settings.getUseLog() ) {
         this.geneScoreThreshold = -Arithmetic.log10( settings
               .getPValThreshold() );
      } else {
         this.geneScoreThreshold = settings.getPValThreshold();
      }
   }

   /**
    * Get results for one class, based on class id. The other arguments are things that are not constant under
    * permutations of the data.
    */
   public GeneSetResult classPval( String class_name, Map groupToPvalMap,
         Map probesToPvals ) {

      //inputs for hypergeometric distribution
      int successes = 0;
      int failures = 0;

      //variables for outputs
      double oraPval = -1.0;

      if ( !effectiveSizes.containsKey( class_name ) ) {
         return null;
      }

      int effectiveGeneSetSize = ( ( Integer ) effectiveSizes.get( class_name ) )
            .intValue();
      if ( effectiveGeneSetSize < settings.getMinClassSize()
            || effectiveGeneSetSize > settings.getMaxClassSize() ) {
         return null;
      }

      ArrayList values = ( ArrayList ) geneAnnots.getGeneSetToProbeMap().get(
            class_name );
      Iterator classit = values.iterator();
      double[] groupPvalArr = new double[effectiveGeneSetSize]; // store pvalues for items in
      // the class.
      Map record = new HashMap();
      int v_size = 0;

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

                  double geneScore = groupPvalArr[v_size];

                  if ( scorePassesThreshold( geneScore, geneScoreThreshold ) ) {
                     successes++;
                  } else {
                     failures++;
                  }
                  v_size++;
               }

            } else { // no weights

               /*
                * pvalue for this probe. This will not be null if things have been done correctly so far. This is the
                * only place we need the raw pvalue for a probe.
                */
               Double pbpval = ( Double ) probesToPvals.get( probe );

               double score = pbpval.doubleValue();

               // hypergeometric pval info.
               if ( scorePassesThreshold( score, geneScoreThreshold ) ) {
                  successes++;
               } else {
                  failures++;
               }

            }
         } // if in data set
      } // end of while over items in the class.

      // Hypergeometric p value calculation (or binomial approximation)
      // successes=number of genes in class which meet criteria
      // (successes); numOverThreshold= number of genes which
      // meet criteria (trials); pos_prob: fractional size of
      // class wrt data size.
      double pos_prob = ( double ) effectiveGeneSetSize / ( double ) inputSize;
      double expected = numOverThreshold * pos_prob;

//      System.err.println( successes + ", " + effectiveGeneSetSize + ", "
//            + ( inputSize - effectiveGeneSetSize ) + ", "
//            + numOverThreshold );

      if ( successes < expected || pos_prob == 0.0 ) { // fewer than expected,
         // still do upper tail - to be consistent with other methods.

         // successes, positives, negatives, trials
         oraPval = SpecFunc.phyper( successes, effectiveGeneSetSize, inputSize
               - effectiveGeneSetSize, numOverThreshold, false );

         if ( Double.isNaN( oraPval ) ) {
            oraPval = Probability.binomialComplemented( successes,
                  numOverThreshold, pos_prob );
         }

      } else {

         oraPval = SpecFunc.phyper( successes, effectiveGeneSetSize, inputSize
               - effectiveGeneSetSize, numOverThreshold, false );

         if ( Double.isNaN( oraPval ) ) {
            oraPval = Probability.binomialComplemented( successes,
                  numOverThreshold, pos_prob );
         }
      }

      // set up the return object.
      GeneSetResult res = new GeneSetResult( class_name, goName
            .getNameForId( class_name ), ( ( Integer ) actualSizes
            .get( class_name ) ).intValue(), effectiveGeneSetSize );
      res.setScore( successes );
      res.setPValue( oraPval );
      return res;

   }

}