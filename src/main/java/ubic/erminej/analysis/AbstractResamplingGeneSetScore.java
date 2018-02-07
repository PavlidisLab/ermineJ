/*
 * The ermineJ project
 *
 * Copyright (c) 2006 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.data.Histogram;

/**
 * <p>
 * Abstract AbstractResamplingGeneSetScore class.
 * </p>
 *
 * @author pavlidis
 */
public abstract class AbstractResamplingGeneSetScore extends AbstractLongTask implements NullDistributionGenerator {

    /** Constant <code>log</code> */
    protected static final Log log = LogFactory.getLog( AbstractResamplingGeneSetScore.class );

    /**
     * Used to control interations.
     */
    protected static final double TOLERANCE = 1e-4;

    /**
     * Check for fit to a normal distribution every n trials. This sets n.
     */
    protected static final int NORMAL_APPROX_SAMPLE_FREQUENCY = 200;

    /**
     * The size we use to start skipping sizes for resampling.
     */
    protected static final int SPEEDUPSIZECUT = 20;

    /** Constant <code>SPEDUPSIZEEXTRASTEP=0.1</code> */
    protected static final double SPEDUPSIZEEXTRASTEP = 0.1;
    protected boolean useSpeedUp = true;
    protected boolean useNormalApprox = true;

    protected int classMaxSize = 100;
    protected int numRuns = 10000;
    protected int numClasses = 0;
    protected double histogramMax = 0;
    protected double histogramMin = 0;
    protected int classMinSize = 2;
    protected Histogram hist = null;

    /**
     * <p>
     * Getter for the field <code>hist</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.data.Histogram} object.
     */
    public Histogram getHist() {
        return hist;
    }

    /**
     * <p>
     * Setter for the field <code>classMaxSize</code>.
     * </p>
     *
     * @param value int
     */
    public void setClassMaxSize( int value ) {
        classMaxSize = value;
    }

    /**
     * <p>
     * Setter for the field <code>useNormalApprox</code>.
     * </p>
     *
     * @param useNormalApprox a boolean.
     */
    public void setUseNormalApprox( boolean useNormalApprox ) {
        this.useNormalApprox = useNormalApprox;
    }

    /**
     * If you set this to true, when large class sizes are analyzed, not every size is measured directly.
     *
     * @param useSpeedUp a boolean.
     */
    public void setUseSpeedUp( boolean useSpeedUp ) {
        this.useSpeedUp = useSpeedUp;
    }

}
