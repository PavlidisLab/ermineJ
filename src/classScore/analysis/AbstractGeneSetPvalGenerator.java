package classScore.analysis;

import java.util.Map;

import classScore.Settings;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;

/**
* Base implementation of pvalue generator
 * <p>Copyright (c) 2004 Columbia University</p>
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
   
}
