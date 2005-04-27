package classScore.analysis;

import baseCode.util.StatusViewer;
import classScore.data.Histogram;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public interface NullDistributionGenerator {

    public Histogram generateNullDistribution( StatusViewer messenger );

    /**
     * @param randomSeed
     */
    public void setRandomSeed( long randomSeed );

    public boolean isInterrupted();

}