package classScore.analysis;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * Generate gene set p values for a bunch of gene sets.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class GeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

 //  private Vector sortedclasses;
   private Map results;
   //  private ResamplingExperimentGeneSetScore probePvalMapper;
   private Histogram hist;
   private GeneSetSizeComputer csc;
   private NumberFormat nf = NumberFormat.getInstance();

   public GeneSetPvalSeriesGenerator( Settings settings,
         GeneAnnotations geneData, Histogram hi, GeneSetSizeComputer csc,
         GONames gon ) {
      super( settings, geneData, csc, gon );
      this.hist = hi;
      //      this.probePvalMapper = pvm;
      this.csc = csc;
      results = new HashMap();
   }

   public Map getResults() {
      return results;
   }

   /**
    * Generate a complete set of class results. The arguments are not constant under pemutations. The second is only
    * needed for the aroc method. This is to be used only for the 'real' data since it modifies 'results',
    * 
    * @param group_pval_map a <code>Map</code> value
    * @param probesToPvals a <code>Map</code> value
    */
   public void classPvalGenerator( Map group_pval_map, Map probesToPvals ) {
      Collection entries = geneAnnots.getClassToProbeMap().entrySet(); // go ->
      // probe
      // map.
      // Entries
      // are the class names.
      Iterator it = entries.iterator(); // the classes.

      ExperimentScorePvalGenerator cpv = new ExperimentScorePvalGenerator(
            settings, geneAnnots, csc, goName, hist );

      // For each class.
      while ( it.hasNext() ) {
         Map.Entry e = ( Map.Entry ) it.next();
         String class_name = ( String ) e.getKey();
         GeneSetResult res = cpv.classPval( class_name, group_pval_map,
               probesToPvals );
         if ( res != null ) {
            results.put( class_name, res );
         }
      }
   }

   /* class_pval_generator */

   /**
    * Same thing as class_pval_generator, but returns a collection of scores (pvalues) (see below) instead of adding
    * them to the results object. This is used to get class pvalues for permutation analysis.
    */
   public Map class_v_pval_generator( Map group_pval_map, Map probesToPvals ) {
      Collection entries = geneAnnots.getClassToProbeMap().entrySet(); // go ->
      // probe
      // map.
      // Entries
      // are the class names.
      Iterator it = entries.iterator(); // the classes.
      //	Vector results = new Vector();

      ExperimentScoreQuickPvalGenerator cpv = new ExperimentScoreQuickPvalGenerator(
            settings, geneAnnots, csc, goName, hist );

      // For each class.
      while ( it.hasNext() ) {
         Map.Entry e = ( Map.Entry ) it.next();
         String class_name = ( String ) e.getKey();
         double pval = cpv.classPvalue( class_name, group_pval_map,
               probesToPvals );

         if ( pval >= 0.0 ) {
            results.put( class_name, new Double( pval ) );
         }
      }
      return results;
   }

}