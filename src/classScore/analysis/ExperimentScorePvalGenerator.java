package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import classScore.Settings;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneSetResult;
import classScore.data.expClassScore;
import classScore.data.Histogram;

/**
 * Generates gene set p values using the resamplin-based 'experiment score'
 * method of Pavlidis et al.
 * 
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class ExperimentScorePvalGenerator extends AbstractGeneSetPvalGenerator {

   Histogram hist;
   expClassScore probePvalMapper;
   
   /**
    * 
    * @param ctp Map
    * @param pg Map
    * @param w boolean
    * @param hi histogram
    * @param pvm expClassScore
    * @param csc ClassSizeComputer
    */
   public ExperimentScorePvalGenerator( Settings settings, GeneAnnotations a, 
           GeneSetSizeComputer csc, GONames gon, Histogram hi, expClassScore pvm ) {
      super( settings, a, csc, gon );
      this.hist = hi;
      this.probePvalMapper = pvm;
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
   public GeneSetResult classPval( String class_name, Map groupToPvalMap,
         Map probesToPvals, Map input_rank_map ) {
      //variables for outputs
      double pval = 0.0;
      double rawscore = 0.0;

      int effSize = ( int ) ( ( Integer ) effectiveSizes.get( class_name ) )
            .intValue(); // effective size of this class.
      if ( effSize < probePvalMapper.get_class_min_size()
            || effSize > probePvalMapper.get_class_max_size() ) {
         return null;
      }

      ArrayList values = ( ArrayList ) geneAnnots.getClassToProbeMap().get( class_name );
      Iterator classit = values.iterator();
      double[] groupPvalArr = new double[effSize]; // store pvalues for items in
      // the class.

      Map target_ranks = new HashMap();
      Map record = new HashMap();

      record.clear();
      target_ranks.clear();
      Object ranking = null;

      int v_size = 0;

      // foreach item in the class.
      while ( classit.hasNext() ) {

         String probe = ( String ) classit.next(); // probe id

         if ( probesToPvals.containsKey( probe ) ) { // if it is in the data
            // set. This is invariant
            // under permutations.

            if ( settings.getUseWeights()  ) {
               Double grouppval = ( Double ) groupToPvalMap.get( geneAnnots.getProbeToGeneMap()
                     .get( probe ) ); // probe -> group

               /*
                * if we haven't done this probe already.
                */
               if ( !record.containsKey( geneAnnots.getProbeToGeneMap().get( probe ) ) ) {

                  record.put( geneAnnots.getProbeToGeneMap().get( probe ), null ); // mark it as
                  // done.
                  groupPvalArr[v_size] = grouppval.doubleValue();
                  v_size++;

               }

            } else { // no weights

               /*
                * pvalue for this probe. This will not be null if things have
                * been done correctly so far. This is the only place we need the
                * raw pvalue for a probe.
                */
               Double pbpval = ( Double ) probesToPvals.get( probe );

               groupPvalArr[v_size] = pbpval.doubleValue();
               v_size++;
            }
         } // if in data set
      } // end of while over items in the class.

      // get raw score and pvalue.
      rawscore = probePvalMapper.calc_rawscore( groupPvalArr, effSize );
      pval = scoreToPval( effSize, rawscore );

      if ( pval < 0 ) {
         throw new IllegalStateException(
               "Warning, a rawscore yielded an invalid pvalue: Classname: "
                     + class_name );
      }

      // set up the return object.
      GeneSetResult res = new GeneSetResult( class_name, goName
            .getNameForId( class_name ), ( int ) ( ( Integer ) actualSizes
            .get( class_name ) ).intValue(), effSize );
      res.setScore( rawscore );
      res.setPValue( pval );
      return res;

   }

   /* scoreClass */

   /**
    * convert a raw score into a pvalue, based on random background distribution
    * 
    * @param in_size int
    * @param rawscore double
    * @throws IllegalStateException
    * @return double
    */
   protected double scoreToPval( int in_size, double rawscore )
         throws IllegalStateException {
      double pval = hist.getValue( in_size, rawscore );

      if ( Double.isNaN( pval ) ) {
         throw new IllegalStateException(
               "Warning, a pvalue was not a number: raw score = " + rawscore );
      }
      return pval;
   }

}