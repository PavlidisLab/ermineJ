package classScore.analysis;

import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.math.RandomChooser;
import baseCode.util.CancellationException;
import baseCode.util.StatusViewer;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import classScore.Settings;
import classScore.data.Histogram;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class ResamplingCorrelationGeneSetScore extends AbstractResamplingGeneSetScore {

    private DenseDoubleMatrix2DNamed data = null;

    private double[][] dataAsRawMatrix;
    private double[][] selfSquaredMatrix;
    private static final int MIN_SET_SIZE_FOR_ESTIMATION = 10;
    private static final int MIN_ITERATIONS_FOR_ESTIMATION = 1000;

    /**
     * @param dataMatrix
     */
    public ResamplingCorrelationGeneSetScore( Settings settings, DenseDoubleMatrix2DNamed dataMatrix ) {
        this.classMaxSize = settings.getMaxClassSize();
        this.classMinSize = settings.getMinClassSize();
        this.numRuns = settings.getIterations();
        this.setUseNormalApprox( !settings.getAlwaysUseEmpirical() );
        this.setUseSpeedUp( !settings.getAlwaysUseEmpirical() );
        data = dataMatrix;
        int numGeneSetSizes = classMaxSize - classMinSize + 1;
        this.hist = new Histogram( numGeneSetSizes, classMinSize, numRuns, 1.0, 0.0 );
    }

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
     * @throws InterruptedException Build background distributions of within-gene set mean correlations. This requires
     *         computing a lot of correlations.
     * @return histogram containing the random distributions of correlations.
     */
    public Histogram generateNullDistribution( StatusViewer messenger ) {

        int[] deck = new int[data.rows()];

        dataAsRawMatrix = new double[data.rows()][]; // we use this so we don't call getQuick() too much. Probably
        // doesn't
        // matter.

        for ( int j = 0; j < data.rows(); j++ ) {
            double[] rowValues = data.getRow( j );
            dataAsRawMatrix[j] = rowValues;
            deck[j] = j;
        }
        selfSquaredMatrix = selfSquaredMatrix( dataAsRawMatrix );

        for ( int geneSetSize = classMinSize; geneSetSize <= classMaxSize; geneSetSize++ ) {

            int[] randomnums = new int[geneSetSize];

            ifInterruptedStop();

            if ( messenger != null ) {
                messenger.showStatus( "Currently running class size " + geneSetSize );
            }

            double oldmean = Double.MAX_VALUE;
            double oldvar = Double.MAX_VALUE;

            DoubleArrayList values = new DoubleArrayList();
            for ( int j = 0; j < numRuns; j++ ) {

                RandomChooser.chooserandom( randomnums, deck, data.rows(), geneSetSize );
                double avecorrel = geneSetMeanCorrel( randomnums );
                values.add( avecorrel );
                hist.update( geneSetSize, avecorrel );

                if ( useNormalApprox && j > MIN_ITERATIONS_FOR_ESTIMATION && geneSetSize > MIN_SET_SIZE_FOR_ESTIMATION
                        && j > 0 && j % NORMAL_APPROX_SAMPLE_FREQUENCY == 0 ) {
                    double mean = Descriptive.mean( values );
                    double variance = Descriptive.variance( values.size(), Descriptive.sum( values ), Descriptive
                            .sumOfSquares( values ) );

                    if ( Math.abs( oldvar - variance ) <= TOLERANCE && Math.abs( oldmean - mean ) <= TOLERANCE ) {
                        hist.addExactNormalProbabilityComputer( geneSetSize, mean, variance );
                        log.debug( "Class size: " + geneSetSize + " - Reached convergence to normal after " + j
                                + " iterations." );
                        break; // stop simulation of this class size.
                    }
                    oldmean = mean;
                    oldvar = variance;
                }

                if ( j % 500 == 0 ) {
                    takeABreak();
                }

            }

            /*
             * To improve performance, after a certain gene set size has been surpassed, don't do every size. The
             * distributions are very similar.
             */
            if ( useSpeedUp && geneSetSize >= SPEEDUPSIZECUT ) {
                geneSetSize += Math.floor( SPEDUPSIZEEXTRASTEP * geneSetSize );
            }
            takeABreak();
        }
        hist.tocdf();
        return hist;
    }

    /**
     * 
     */
    private void takeABreak() {
        ifInterruptedStop();

        try {
            Thread.sleep( 10 );
        } catch ( InterruptedException e ) {
            throw new CancellationException();
        }
    }

    /**
     * Compute the average correlation for a set of vectors.
     * 
     * @param indicesToSelect
     * @param correls the correlation matrix for the data. This can be passed in without having filled it in yet. This
     *        means that only values that are visited during resampling are actually computed - this is a big memory
     *        saver. NOT used because it still uses too much memory.
     * @return mean correlation within the matrix.
     */
    public double geneSetMeanCorrel( int[] indicesToSelect ) {

        int size = indicesToSelect.length;
        double avecorrel = 0.0;
        int nummeas = 0;

        for ( int i = 0; i < size; i++ ) {
            double[] irow = dataAsRawMatrix[indicesToSelect[i]];
            for ( int j = i + 1; j < size; j++ ) {
                double[] jrow = dataAsRawMatrix[indicesToSelect[j]];
                double corr = Math.abs( correlation( irow, jrow, selfSquaredMatrix, indicesToSelect[i],
                        indicesToSelect[j] ) );
                avecorrel += corr;
                nummeas++;
            }
        }
        return avecorrel / nummeas;
    }

    // special optimized version of correlation computation for this.

    private static double correlation( double[] x, double[] y, double[][] selfSquaredMatrix, int a, int b ) {
        double syy, sxy, sxx, sx, sy, xj, yj, ay, ax;
        int numused = 0;
        syy = 0.0;
        sxy = 0.0;
        sxx = 0.0;
        sx = 0.0;
        sy = 0.0;

        int length = x.length;
        for ( int j = 0; j < length; j++ ) {
            xj = x[j];
            yj = y[j];

            if ( Double.isNaN( xj ) || Double.isNaN( yj ) ) {
                continue;
            }
            sx += xj;
            sy += yj;
            sxy += xj * yj;
            // sxx += xj * xj;
            // syy += yj * yj;
            sxx += selfSquaredMatrix[a][j];
            syy += selfSquaredMatrix[b][j];
            numused++;
        }

        if ( numused > 0 ) {
            ay = sy / numused;
            ax = sx / numused;
            return ( sxy - sx * ay ) / Math.sqrt( ( sxx - sx * ax ) * ( syy - sy * ay ) );
        }
        return Double.NaN; // signifies that it could not be calculated.
    }

    private static double[][] selfSquaredMatrix( double[][] input ) {
        double[][] returnValue = new double[input.length][];
        for ( int i = 0; i < returnValue.length; i++ ) {
            returnValue[i] = new double[input[i].length];

            for ( int j = 0; j < returnValue[i].length; j++ ) {
                returnValue[i][j] = input[i][j] * input[i][j];
            }

        }
        return returnValue;
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