package classScore.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import classScore.data.GeneScoreReader;

import baseCode.bio.geneset.GeneAnnotations;

/**
 * Class for computing the actual and effective sizes of gene sets.
 * <p>
 * Copyright: Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class GeneSetSizeComputer {
   protected Map effectiveSizes = null;
   protected Map actualSizes = null;
   protected Map classToProbe;
   protected boolean weight_on = true;
   protected Map probeGroups;
   protected GeneScoreReader geneScores;
   protected Set activeProbes;

   public GeneSetSizeComputer( Set activeProbes, GeneAnnotations geneData,
         GeneScoreReader geneScores, boolean w ) {
      this.weight_on = w;
      this.activeProbes = activeProbes;
      this.classToProbe = geneData.getGeneSetToProbeMap();
      this.probeGroups = geneData.getProbeToGeneMap();
      this.geneScores = geneScores;
      effectiveSizes = new HashMap();
      actualSizes = new HashMap();
      getClassSizes();
   }

   /**
    * Calculate class sizes for all classes - both effective and actual size
    */
   private void getClassSizes() {
      Collection entries = classToProbe.entrySet(); // go -> probe map. Entries
      // are the class names.
      Iterator it = entries.iterator();

      Map record = new HashMap();
      int size;
      int v_size;

     // assert !( activeProbes == null || activeProbes.size() == 0 ) : "ActiveProbes was not initialized or was empty";
    //  assert !( geneScores == null ) : "GeneScores was not initialized";
    //  assert !( geneScores.getGeneToPvalMap() == null ) : "getGroupToPvalMap was not initialized";

      boolean gotAtLeastOneNonZero = false;

      while ( it.hasNext() ) {
         Map.Entry e = ( Map.Entry ) it.next(); // next class.
         String className = ( String ) e.getKey(); // id of the class
         // (GO:XXXXXX)
         ArrayList values = ( ArrayList ) e.getValue(); // items in the class.
         Iterator I = values.iterator();

         record.clear();
         size = 0;
         v_size = 0;

         while ( I.hasNext() ) { // foreach item in the class.
            String probe = ( String ) I.next();

            if ( probe != null ) {
               if ( activeProbes.contains( probe ) ) { // if it is in the data
                  // set
                  size++;

                  if ( weight_on ) { //routine for weights
                     // compute pval for every replicate group
                     if ( geneScores.getGeneToPvalMap().containsKey(
                           probeGroups.get( probe ) )

                           /*
                            * if we haven't done this probe already.
                            */
                           && !record.containsKey( probeGroups.get( probe ) ) ) {

                        /*
                         * mark it as done for this class.
                         */
                        record.put( probeGroups.get( probe ), null );
                        v_size++; // this is used in any case.
                     }
                  }
               }
            } // end of null check
         } // end of while over items in the class.

         if ( !weight_on ) {
            v_size = size;
         }

         gotAtLeastOneNonZero = gotAtLeastOneNonZero || v_size > 0;

         effectiveSizes.put( className, new Integer( v_size ) );
         actualSizes.put( className, new Integer( size ) );
      }

   //   assert gotAtLeastOneNonZero;

   }

   /**
    * @return Map
    */
   public Map getEffectiveSizes() {
      return effectiveSizes;
   }

   /**
    * @return Map
    */
   public Map getActualSizes() {
      return actualSizes;
   }

}