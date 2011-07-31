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

import org.apache.commons.lang.ArrayUtils;

import ubic.basecode.math.DescriptiveWithMissing;
import ubic.basecode.math.RandomChooser;
import ubic.basecode.math.Stats;
import ubic.basecode.util.CancellationException;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.Histogram;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Calculates a background distribution for class sscores derived from randomly selected individual gene scores...and
 * does other things. Created 09/02/02.
 * 
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class ResamplingExperimentGeneSetScore extends AbstractResamplingGeneSetScore {
    private Double[] groupPvals = null; // pvalues for groups.

    private static int quantile = 50;
    private static double quantfract = 0.5;
    private Settings.GeneScoreMethod method;
    private static final int MIN_SET_SIZE_FOR_ESTIMATION = 30; // after this size, switch to doing it by normal
    // approximation.

    /**
     * Minimum mumber of interations to perform when using approximation methods.
     */
    private static final int MIN_ITERATIONS_FOR_ESTIMATION = 5000;

    /**
     * Generate a null distribution, using a selected random seed.
     * 
     * @param m
     * @param randomSeed
     * @return
     */
    public Histogram generateNulldistribution( StatusViewer m, long randomSeed ) {
        RandomChooser.init( randomSeed );
        return this.generateNullDistribution( m );
    }

    /**
     * Used for methods which require randomly sampling classes to generate a null distribution of scores based on
     * gene-by-gene scores.
     * 
     * @return A histogram object containing a cdf that can be used to generate pvalues.
     * @param m
     */
    public Histogram generateNullDistribution( StatusViewer m ) {

        int numGenes;
        Double[] in_pval;

        if ( hist == null ) {
            throw new IllegalStateException( "Histogram object was null." );
        }

        // do the right thing if we are using weights.
        numGenes = groupPvals.length;
        in_pval = groupPvals;

        if ( numGenes == 0 ) {
            throw new IllegalStateException( "No pvalues!" );
        }

        if ( numGenes < classMinSize ) {
            throw new IllegalStateException( "Not enough genes to analyze classes of size " + classMinSize );
        }

        // we use this throughout.
        int[] deck = new int[numGenes];
        for ( int i = 0; i < numGenes; i++ ) {
            deck[i] = i;
        }

        double oldmean = Double.MAX_VALUE;
        double oldvar = Double.MAX_VALUE;

        for ( int geneSetSize = classMinSize; geneSetSize <= classMaxSize && geneSetSize <= numGenes; geneSetSize++ ) {

            double[] randomClass = new double[geneSetSize]; // holds data for random class.
            double rawscore = 0.0;
            DoubleArrayList values = new DoubleArrayList();
            for ( int k = 0; k < numRuns; k++ ) {

                RandomChooser.chooserandom( randomClass, in_pval, deck, numGenes, geneSetSize );
                rawscore = computeRawScore( randomClass, geneSetSize, method );
                values.add( rawscore );
                hist.update( geneSetSize, rawscore );
                if ( useNormalApprox && k > MIN_ITERATIONS_FOR_ESTIMATION && geneSetSize > MIN_SET_SIZE_FOR_ESTIMATION
                        && k > 0 && k % ( 4 * NORMAL_APPROX_SAMPLE_FREQUENCY ) == 0 ) { // less frequent checking.
                    double mean = Descriptive.mean( values );
                    double variance = Descriptive.variance( values.size(), Descriptive.sum( values ), Descriptive
                            .sumOfSquares( values ) );
                    if ( Math.abs( oldvar - variance ) <= TOLERANCE && Math.abs( oldmean - mean ) <= TOLERANCE ) {
                        hist.addExactNormalProbabilityComputer( geneSetSize, mean, variance );
                        log.debug( "Class size: " + geneSetSize + " - Reached convergence to normal after " + k
                                + " iterations." );
                        break;
                    }
                    oldmean = mean;
                    oldvar = variance;
                }
                if ( k % 1000 == 0 ) {
                    try {
                        ifInterruptedStop();
                        Thread.sleep( 10 );
                    } catch ( InterruptedException e ) {
                        throw new CancellationException();
                    }
                }
            }

            if ( m != null ) {
                m.showStatus( "Currently running class size " + geneSetSize );
            }

            /*
             * To improve performance, after a certain gene set size has been surpassed, don't do every size. The
             * distributions are very similar.
             */
            if ( useSpeedUp && geneSetSize >= SPEEDUPSIZECUT ) {
                geneSetSize += Math.floor( SPEDUPSIZEEXTRASTEP * geneSetSize );
            }
        }

        hist.tocdf();
        return hist;
    }

    /**
     * @param settings
     * @param geneScores
     */
    public ResamplingExperimentGeneSetScore( SettingsHolder settings, GeneScores geneScores ) {
        this.classMaxSize = settings.getMaxClassSize();
        this.classMinSize = settings.getMinClassSize();
        this.numRuns = settings.getIterations();
        this.setQuantile( settings.getQuantile() );
        this.setMethod( settings.getGeneSetResamplingScoreMethod() );
        this.setUseNormalApprox( !settings.getAlwaysUseEmpirical() );
        this.setUseSpeedUp( !settings.getAlwaysUseEmpirical() );
        if ( classMaxSize < classMinSize ) {
            throw new IllegalArgumentException( "The maximum class size is smaller than the minimum." );
        }

        this.numClasses = classMaxSize - classMinSize + 1;

        groupPvals = geneScores.getGeneScores();

        this.setHistogramRange();
        this.hist = new Histogram( numClasses, classMinSize, numRuns, histogramMax, histogramMin );
    }

    /**
     * @return double[]
     */
    public Double[] get_in_pvals() {
        return groupPvals;
    }

    /**
     * @param value int
     */
    public void setQuantile( int value ) {
        quantile = value;
        quantfract = quantile / 100.0;
    }

    /**
     * @return int
     */
    public int get_quantile() {
        return quantile;
    }

    /**
     * Basic method to calculate the raw score, given an array of the gene scores for items in the class. Note that
     * performance here is important.
     * 
     * @param genevalues double[]
     * @param effsize int
     * @throws IllegalArgumentException
     * @return double
     */
    public static double computeRawScore( double[] genevalues, int effsize, Settings.GeneScoreMethod method )
            throws IllegalArgumentException {

        if ( method.equals( Settings.GeneScoreMethod.MEAN ) ) {
            return DescriptiveWithMissing.mean( genevalues, effsize );
        }
        int index = ( int ) Math.floor( quantfract * effsize );
        if ( method.equals( Settings.GeneScoreMethod.QUANTILE ) ) {
            return Stats.quantile( index, genevalues, effsize );
        } else if ( method.equals( Settings.GeneScoreMethod.MEAN_ABOVE_QUANTILE ) ) {
            return Stats.meanAboveQuantile( index, genevalues, effsize );
        } else {
            throw new IllegalStateException( "Unknown raw score calculation method selected" );
        }

    }

    /**
     * @param meth String
     * @throws IllegalArgumentException
     */
    private void setMethod( Settings.GeneScoreMethod meth ) {
        method = meth;
    }

    /**  */
    public void setHistogramRange() {
        if ( groupPvals == null ) {
            throw new IllegalStateException( "Null gene score arrays for histogram range setting" );
        }

        double[] pgpvals = ArrayUtils.toPrimitive( groupPvals );

        histogramMax = Descriptive.max( new DoubleArrayList( pgpvals ) );
        histogramMin = Descriptive.min( new DoubleArrayList( pgpvals ) );

        if ( histogramMax <= histogramMin ) {
            throw new IllegalStateException( "Histogram has no range (max " + histogramMax + " <= min " + histogramMin
                    + ")\nMake sure 'larger scores are better' is set correctly." );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.analysis.NullDistributionGenerator#setRandomSeed(long)
     */
    public void setRandomSeed( long randomSeed ) {
        RandomChooser.init( randomSeed );
    }

}