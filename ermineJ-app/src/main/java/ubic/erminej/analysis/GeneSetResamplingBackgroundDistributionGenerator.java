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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import ubic.basecode.math.PrecisionRecall;
import ubic.basecode.math.RandomChooser;
import ubic.basecode.math.Rank;
import ubic.basecode.math.Stats;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.Histogram;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Calculates a background distribution for class scores derived from randomly selected individual gene scores.
 * 
 * @author Shahmil Merchant, Paul Pavlidis
 * @since Created 09/02/02.
 * @version $Id$
 */
public class GeneSetResamplingBackgroundDistributionGenerator extends AbstractResamplingGeneSetScore {

    /**
     * Scores for ALL the genes.
     */
    private Double[] geneScores = null;

    /**
     * Ranks for all the genes.
     */
    private Map<Gene, Double> geneRanks;

    private static int quantile = 50;
    private static double quantfract = 0.5;
    private Settings.GeneScoreMethod method;

    private static final int MIN_SET_SIZE_FOR_ESTIMATION = 30; // after this size, switch to doing it by normal
    // approximation.

    /**
     * Minimum number of iterations to perform when using approximation methods.
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

        int numGenes = geneScores.length;
        List<Gene> genes = new Vector<Gene>( geneRanks.keySet() );
        assert hist != null;
        assert numGenes >= classMaxSize;

        // we use this throughout.
        int[] deck = new int[numGenes];
        for ( int i = 0; i < numGenes; i++ ) {
            deck[i] = i;
        }

        boolean usingPrecisionRecall = method.equals( SettingsHolder.GeneScoreMethod.PRECISIONRECALL );

        for ( int geneSetSize = classMinSize; geneSetSize <= classMaxSize && geneSetSize <= numGenes; geneSetSize++ ) {

            DoubleArrayList values = new DoubleArrayList();
            double oldmean = Double.MAX_VALUE;
            double oldvar = Double.MAX_VALUE;

            for ( int k = 0; k < numRuns; k++ ) {

                double rawScore = 0;

                /*
                 * Depending on the method, we need either random gene scores or a list of genes.
                 */

                if ( usingPrecisionRecall ) {
                    List<Gene> randomClass = ( List<Gene> ) RandomChooser.chooserandom( genes, deck, geneSetSize );
                    rawScore = computeRawScore( null, randomClass );
                } else {
                    double[] randomClassScores = RandomChooser.chooserandom( geneScores, deck, geneSetSize );
                    rawScore = computeRawScore( randomClassScores, null );
                }

                values.add( rawScore );
                hist.update( geneSetSize, rawScore );

                // check convergence. This doesn't apply to precision recall.
                if ( !usingPrecisionRecall && useNormalApprox && k > MIN_ITERATIONS_FOR_ESTIMATION
                        && geneSetSize > MIN_SET_SIZE_FOR_ESTIMATION && k > 0
                        && k % ( 4 * NORMAL_APPROX_SAMPLE_FREQUENCY ) == 0 ) { // less frequent checking.

                    double mean = Descriptive.mean( values );
                    double variance = Descriptive.variance( values.size(), Descriptive.sum( values ),
                            Descriptive.sumOfSquares( values ) );

                    if ( Math.abs( oldvar - variance ) <= TOLERANCE && Math.abs( oldmean - mean ) <= TOLERANCE ) {
                        hist.addExactNormalProbabilityComputer( geneSetSize, mean, variance );
                        if ( log.isDebugEnabled() )
                            log.debug( "Class size: " + geneSetSize + " - Reached convergence to normal after " + k
                                    + " iterations." );
                        break;
                    }

                    oldmean = mean;
                    oldvar = variance;
                }

                if ( k % 1000 == 0 ) {
                    ifInterruptedStop();
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
     * @param geneScores Should already be multifunctionality corrected if desired.
     */
    public GeneSetResamplingBackgroundDistributionGenerator( SettingsHolder settings, Map<Gene, Double> geneToScoreMap ) {
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

        this.geneScores = geneToScoreMap.values().toArray( new Double[] {} );

        geneRanks = Rank.rankTransform( geneToScoreMap );

        this.setHistogramRange();
        this.hist = new Histogram( numClasses, classMinSize, numRuns, histogramMax, histogramMin );
    }

    /**
     * @param value int
     */
    public void setQuantile( int value ) {
        quantile = value;
        quantfract = quantile / 100.0;
    }

    /**
     * Basic method to calculate the raw score for a gene set, given an array of the gene scores for items in the class.
     * Note that speed here is important. In the prototypical GSR method, the score is the mean of the values for the
     * gene.
     * 
     * @param genevalues double[] raw scores for the items in the class.
     * @param genesInSet
     * @return double
     * @see Settings.GeneScoreMethod for choices of methods.
     */
    public double computeRawScore( double[] genevalues, Collection<Gene> genesInSet ) {

        if ( method.equals( Settings.GeneScoreMethod.MEAN ) ) {
            return Descriptive.mean( new DoubleArrayList( genevalues ) );
        } else if ( method.equals( Settings.GeneScoreMethod.PRECISIONRECALL ) ) {
            assert genesInSet != null;
            return averagePrecision( genesInSet );
        }

        int index = ( int ) Math.floor( quantfract * genevalues.length );

        if ( method.equals( Settings.GeneScoreMethod.QUANTILE ) ) {
            return Stats.quantile( index, genevalues, genevalues.length );
        } else if ( method.equals( Settings.GeneScoreMethod.MEAN_ABOVE_QUANTILE ) ) {
            return Stats.meanAboveQuantile( index, genevalues, genevalues.length );
        } else {
            throw new IllegalStateException( "Unknown raw score calculation method selected" );
        }

    }

    /**
     * @param genesInSet
     * @return
     */
    protected double averagePrecision( Collection<Gene> genesInSet ) {
        assert geneRanks.size() >= genesInSet.size();

        Set<Double> ranksOfPositives = new HashSet<Double>();
        for ( Gene gene : genesInSet ) {
            if ( geneRanks.containsKey( gene ) ) {
                Double rank = geneRanks.get( gene );
                ranksOfPositives.add( rank );
            }
        }

        return PrecisionRecall.averagePrecision( geneRanks.size(), ranksOfPositives );

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
        if ( geneScores == null ) {
            throw new IllegalStateException( "Null gene score arrays for histogram range setting" );
        }

        double[] pgpvals = ArrayUtils.toPrimitive( geneScores );

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