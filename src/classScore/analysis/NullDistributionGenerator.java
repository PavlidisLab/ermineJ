package classScore.analysis;

import classScore.data.Histogram;
import classScore.gui.GeneSetScoreStatus;

/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public interface NullDistributionGenerator {

   public Histogram generateNullDistribution(GeneSetScoreStatus messenger);
   
}
