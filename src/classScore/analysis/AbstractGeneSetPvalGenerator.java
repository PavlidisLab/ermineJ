package classScore.analysis;

import java.util.Map;

import classScore.data.GONames;
import classScore.data.expClassScore;
import classScore.data.histogram;

/**
* Base implementation of pvalue generator
 * <p>Copyright (c) 2004 Columbia University</p>
 * @author Paul Pavlidis
 * @version $Id$
 */

public abstract class AbstractGeneSetPvalGenerator {
   
   protected Map effectiveSizes = null;
   protected Map actualSizes = null;
   protected expClassScore probePvalMapper;
   protected Map classToProbe;
   protected histogram hist;
   protected GONames goName;
   protected Map probeGroups;
   protected boolean weight_on;
   
   public AbstractGeneSetPvalGenerator( Map ctp, Map pg, boolean w,
         histogram hi, expClassScore pvm, GeneSetSizeComputer csc, GONames gon ) {
      this.weight_on = w;
      this.classToProbe = ctp;
      this.probeGroups = pg;
      this.hist = hi;
      this.probePvalMapper = pvm;
      this.effectiveSizes = csc.getEffectiveSizes();
      this.actualSizes = csc.getActualSizes();
      if ( gon != null ) {
         this.goName = gon;
      }
   }
   
}
