package classScore.analysis;

import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

import classScore.Settings;

/**
 * Base implementation of pvalue generator
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public abstract class AbstractGeneSetPvalGenerator {

   protected Map effectiveSizes = null;
   protected Map actualSizes = null;
   protected GONames goName;
   protected Settings settings;
   protected GeneAnnotations geneAnnots;
   protected GeneSetSizeComputer csc;
   private int maxGeneSetSize;
   private int minGeneSetSize;

   public AbstractGeneSetPvalGenerator( Settings set, GeneAnnotations annots,
         GeneSetSizeComputer csc, GONames gon ) {

      this.settings = set;
      this.geneAnnots = annots;
      this.effectiveSizes = csc.getEffectiveSizes();
      this.actualSizes = csc.getActualSizes();
      this.csc = csc;
      if ( gon != null ) {
         this.goName = gon;
      }
   }

   /**
    * @param value
    */
   public void set_class_max_size( int value ) {
      maxGeneSetSize = value;
   }

   /**
    * @param value
    */
   public void set_class_min_size( int value ) {
      minGeneSetSize = value;
   }

   /**
    */
   public int getMaxClassSize() {
      return maxGeneSetSize;
   }

   /**
    * @return
    */
   public int getMinGeneSetSize() {
      return minGeneSetSize;
   }

}