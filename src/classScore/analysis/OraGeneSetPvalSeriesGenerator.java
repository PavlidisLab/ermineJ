package classScore.analysis;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

import classScore.Settings;
import classScore.data.GeneScoreReader;
import classScore.data.GeneSetResult;

/**
 * Generate Overrepresentation p values for gene sets.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author pavlidis
 * @version $Id$
 */
public class OraGeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

   private Vector sortedclasses;
   private Map results;
   private GeneScoreReader geneScores;
   private NumberFormat nf = NumberFormat.getInstance();
   private int numOverThreshold;
   private int numUnderThreshold;
   private int inputSize;

   public OraGeneSetPvalSeriesGenerator( Settings settings,
         GeneAnnotations geneData, GeneSetSizeComputer csc, GONames gon,
         int inputSize ) {
      super( settings, geneData, csc, gon );
      this.inputSize = inputSize;
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
      Collection entries = geneAnnots.getGeneSetToProbeMap().entrySet(); // go ->

      Iterator it = entries.iterator(); // the classes.

      OraPvalGenerator cpv = new OraPvalGenerator( settings, geneAnnots, csc,
            numOverThreshold, numUnderThreshold, goName, inputSize );

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

   /**
    * Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution. This is a constant under
    * permutations, but depends on weights.
    * 
    * @param inp_entries The pvalues for the probes (no weights) or groups (weights)
    * @todo make this private and called by OraPvalGenerator.
    */
   public void hgSizes( Collection inp_entries ) {

      double userPval = settings.getPValThreshold();

      if ( settings.getDoLog() ) {
         userPval = -Math.log( userPval ) / Math.log( 10.0 );
      }

      Iterator itr = inp_entries.iterator();
      while ( itr.hasNext() ) {
         Map.Entry m = ( Map.Entry ) itr.next();
         double genePvalue = ( ( Double ) m.getValue() ).doubleValue();
         // why
         // parsing?

         if ( genePvalue >= userPval ) {
            numOverThreshold++;
         } else {
            numUnderThreshold++;
         }

      }
      System.err.println( numOverThreshold + " genes are above the threshold "
            + userPval );
   }

   /**
    * Same thing as class_pval_generator, but returns a collection of scores (pvalues) (see below) instead of adding
    * them to the results object. This is used to get class pvalues for permutation analysis.
    */
   //      public HashMap class_v_pval_generator( Map group_pval_map, Map
   // probesToPvals ) {
   //         Collection entries = geneAnnots.getClassToProbeMap().entrySet(); // go ->
   // probe map. Entries
   //         // are the class names.
   //         Iterator it = entries.iterator(); // the classes.
   //         // Vector results = new Vector();
   //         HashMap results = new HashMap();
   //
   //         OraPvalGenerator cpv = new OraPvalGenerator(
   //               settings, geneAnnots, csc, goName, numOverThreshold, numUnderThreshold,
   // probePvalMapper );
   //
   //         // For each class.
   //         while ( it.hasNext() ) {
   //            Map.Entry e = ( Map.Entry ) it.next();
   //            String class_name = ( String ) e.getKey();
   //            double pval = cpv.classPvalue( class_name, group_pval_map,
   //                  probesToPvals );
   //
   //            if ( pval >= 0.0 ) {
   //               results.put( class_name, new Double( pval ) );
   //
   //            }
   //         }
   //         return results;
   //      }
   //
   //   }
}