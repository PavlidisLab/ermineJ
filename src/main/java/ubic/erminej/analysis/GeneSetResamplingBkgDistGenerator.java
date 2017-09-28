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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import ubic.basecode.math.PrecisionRecall;
import ubic.basecode.math.RandomChooser;
import ubic.basecode.math.Rank;
import ubic.basecode.math.Stats;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.SettingsHolder.GeneScoreMethod;
import ubic.erminej.data.Gene;
import ubic.erminej.data.Histogram;

/**
 * Calculates a background distribution for class scores derived from randomly selected individual gene scores. This is
 * used for either the "GSR" method or the precision-recall method.
 *
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @since Created 09/02/02.
 * @version $Id$
 */
public class GeneSetResamplingBkgDistGenerator extends AbstractResamplingGeneSetScore {

    private static int quantile = 50;

    private static double quantfract = 0.5;

    // after this size, switch to doing it by normal approximation.
    private static final int MIN_SET_SIZE_FOR_ESTIMATION = 30;
    /**
     * Minimum number of iterations to perform when using approximation methods.
     */
    private static final int MIN_ITERATIONS_FOR_ESTIMATION = 5000;
    /**
     * Scores for ALL the genes.
     */
    private Double[] geneScores = null;

    /**
     * Ranks for all the genes.
     */
    private Map<Gene, Double> geneRanks;

    private Settings.GeneScoreMethod method;

    /**
     * <p>
     * Constructor for GeneSetResamplingBkgDistGenerator.
     * </p>
     *
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param geneToScoreMap a {@link java.util.Map} object.
     */
    public GeneSetResamplingBkgDistGenerator( SettingsHolder settings, Map<Gene, Double> geneToScoreMap ) {
        this.classMaxSize = settings.getMaxClassSize();
        this.classMinSize = settings.getMinClassSize();
        this.numRuns = settings.getIterations();

        if ( numRuns <= 0 ) throw new IllegalArgumentException( "Number of iterations must be greater than zero" );

        this.setQuantile( settings.getQuantile() );
        this.setMethod( settings.getGeneSetResamplingScoreMethod() );
        this.setUseNormalApprox( !settings.getAlwaysUseEmpirical() );

        // this.setUseSpeedUp( !settings.getAlwaysUseEmpirical() );
        this.setUseSpeedUp( true ); // there is really no major disadvantage, but fix this to be configurable.

        if ( classMaxSize < classMinSize ) {
            throw new IllegalArgumentException( "The maximum class size is smaller than the minimum." );
        }

        this.numClasses = classMaxSize - classMinSize + 1;

        this.geneScores = geneToScoreMap.values().toArray( new Double[] {} );

        if ( geneScores.length < 2 ) {
            throw new IllegalArgumentException( "Gene scores are not valid, too few values" );
        }

        /*
         * Taking into account the "bigger is better" / "log-transform" setting.
         */
        if ( settings.getBigIsBetter() && !settings.getDoLog() ) {
            geneRanks = Rank.rankTransform( geneToScoreMap, true );
        } else if ( settings.getDoLog() && !settings.getBigIsBetter() ) {
            // no need to actually do the transform.
            geneRanks = Rank.rankTransform( geneToScoreMap, true );
        } else {
            geneRanks = Rank.rankTransform( geneToScoreMap, false );
        }

        this.setHistogramRange();
        this.hist = new Histogram( numClasses, classMinSize, numRuns, histogramMax, histogramMin );
    }

    /**
     * Basic method to calculate the raw score for a gene set, given an array of the gene scores for items in the class.
     * Note that speed here is important. In the prototypical GSR method, the score is the mean of the values for the
     * gene. For precision-recall, it is the average precision.
     *
     * @param genevalues double[] raw scores for the items in the class.
     * @param genesInSet a {@link java.util.Collection} object.
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
     * Generate a null distribution, using a selected random seed.
     *
     * @param m a {@link ubic.basecode.util.StatusViewer} object.
     * @param randomSeed a long.
     * @return a {@link ubic.erminej.data.Histogram} object.
     */
    public Histogram generateNulldistribution( StatusViewer m, long randomSeed ) {
        RandomChooser.init( randomSeed );
        return this.generateNullDistribution( m );
    }

    /**
     * {@inheritDoc}
     *
     * Used for methods which require randomly sampling classes to generate a null distribution of scores based on
     * gene-by-gene scores.
     */
    @Override
    public Histogram generateNullDistribution( StatusViewer m ) {

        int numGenes = geneScores.length;
        List<Gene> genes = new Vector<>( geneRanks.keySet() );
        assert hist != null;
        assert numGenes >= classMaxSize;

        // // we use this throughout.
        // int[] deck = new int[numGenes];
        // for ( int i = 0; i < numGenes; i++ ) {
        // deck[i] = i;
        // }

        boolean usingPrecisionRecall = method.equals( SettingsHolder.GeneScoreMethod.PRECISIONRECALL );

        for ( int geneSetSize = classMinSize; geneSetSize <= classMaxSize && geneSetSize <= numGenes; geneSetSize++ ) {

            DoubleArrayList values = new DoubleArrayList();
            double oldmean = Double.MAX_VALUE;
            double oldvar = Double.MAX_VALUE;

            double[] primGeneScores = ArrayUtils.toPrimitive( geneScores );

            for ( int k = 0; k < numRuns; k++ ) {

                double rawScore = 0;

                /*
                 * Depending on the method, we need either random gene scores or a list of genes.
                 */
                if ( usingPrecisionRecall ) {
                    List<Gene> randomClass = ( List<Gene> ) RandomChooser.chooserandom( genes, geneSetSize );
                    rawScore = computeRawScore( null, randomClass );
                } else {
                    double[] randomClassScores = RandomChooser.chooserandom( primGeneScores, geneSetSize );
                    rawScore = computeRawScore( randomClassScores, null );
                }

                values.add( rawScore );
                hist.update( geneSetSize, rawScore );

                // check convergence. Not using this for precision-recall at the moment
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
                m.showProgress( "Generating background distribution for class size " + geneSetSize + " [maxiters = "
                        + numRuns + "]" );
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
     * Figure out the limits that should be used for the histogram.
     */
    public void setHistogramRange() {
        if ( geneScores == null ) {
            throw new IllegalStateException( "Null gene score arrays for histogram range setting" );
        }

        /*
         * If we're using precision recall, the max should be 1.0.
         */
        if ( method.equals( GeneScoreMethod.PRECISIONRECALL ) ) {
            histogramMax = 1.0;
            histogramMin = 0.0;
        } else {

            double[] pgpvals = ArrayUtils.toPrimitive( geneScores );

            histogramMax = Descriptive.max( new DoubleArrayList( pgpvals ) );
            histogramMin = Descriptive.min( new DoubleArrayList( pgpvals ) );

            if ( histogramMax <= histogramMin ) {
                throw new IllegalStateException( "Histogram has no range (max " + histogramMax + " <= min "
                        + histogramMin + ")\nMake sure 'larger scores are better' is set correctly." );
            }
        }
    }

    /**
     * <p>
     * Setter for the field <code>quantile</code>.
     * </p>
     *
     * @param value int
     */
    public void setQuantile( int value ) {
        quantile = value;
        quantfract = quantile / 100.0;
    }

    /*
     * (non-Javadoc)
     *
     * @see classScore.analysis.NullDistributionGenerator#setRandomSeed(long)
     */
    /** {@inheritDoc} */
    @Override
    public void setRandomSeed( long randomSeed ) {
        RandomChooser.init( randomSeed );
    }

    /**
     * <p>
     * averagePrecision.
     * </p>
     *
     * @param genesInSet a {@link java.util.Collection} object.
     * @return a double.
     */
    protected double averagePrecision( Collection<Gene> genesInSet ) {
        assert geneRanks.size() >= genesInSet.size();

        List<Double> ranksOfPositives = new Vector<>();
        for ( Gene gene : genesInSet ) {
            if ( geneRanks.containsKey( gene ) ) {
                Double rank = geneRanks.get( gene );
                ranksOfPositives.add( rank );
            } else {
                log.warn( "Missing rank for " + gene );
            }
        }

        return PrecisionRecall.averagePrecision( ranksOfPositives );

    }

    /**
     * @param meth String
     * @throws IllegalArgumentException
     */
    private void setMethod( Settings.GeneScoreMethod meth ) {
        method = meth;
    }

}
