package classScore.analysis;

import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.matrix.DoubleMatrixNamed;
import baseCode.math.DescriptiveWithMissing;
import baseCode.math.MatrixStats;
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

    /**
     * 
     */
    private static final int MIN_STABLE_CHECKS = 3;

    private DoubleMatrixNamed data = null;

    private double[][] dataAsRawMatrix;
    private double[][] selfSquaredMatrix;
    private boolean[][] nanStatusMatrix;

    /**
     * Never start estimating distributions for gene sets sizes smaller than this.
     */
    private static final int MIN_SET_SIZE_FOR_ESTIMATION = 10;

    /**
     * Never estimate distribution with fewer than this many iterations.
     */
    private static final int MIN_ITERATIONS_FOR_ESTIMATION = 2000;

    /**
     * @param dataMatrix
     */
    public ResamplingCorrelationGeneSetScore( Settings settings, DoubleMatrixNamed dataMatrix ) {
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
     * Build background distributions of within-gene set mean correlations. This requires computing a lot of
     * correlations.
     * 
     * @return histogram containing the random distributions of correlations.
     */
    public Histogram generateNullDistribution( StatusViewer messenger ) {

        int[] deck = new int[data.rows()];

        dataAsRawMatrix = new double[data.rows()][];
        for ( int j = 0; j < data.rows(); j++ ) {
            double[] rowValues = data.getRow( j );
            dataAsRawMatrix[j] = rowValues;

            deck[j] = j;
        }
        selfSquaredMatrix = MatrixStats.selfSquaredMatrix( dataAsRawMatrix );
        nanStatusMatrix = MatrixStats.nanStatusMatrix( dataAsRawMatrix );

        int stableChecks = 0;

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

                    if ( isConverged( oldmean, oldvar, mean, variance ) ) {
                        stableChecks++; // this is necessary because we are not guaranteed to decrease the error.
                    } else {
                        stableChecks = 0;
                    }

                    if ( stableChecks >= MIN_STABLE_CHECKS ) {
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
        nanStatusMatrix = null;
        selfSquaredMatrix = null;
        dataAsRawMatrix = null;
        return hist;
    }

    /**
     * @param oldmean
     * @param oldvar
     * @param mean
     * @param variance
     * @return
     */
    private boolean isConverged( double oldmean, double oldvar, double mean, double variance ) {
        return Math.abs( oldvar - variance ) <= TOLERANCE && Math.abs( oldmean - mean ) <= TOLERANCE;
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
     * @param indicesToSelect - rows of the matrix that will get compared.
     * @return mean correlation within the matrix.
     */
    public double geneSetMeanCorrel( int[] indicesToSelect ) {

        int size = indicesToSelect.length;
        double sumCorrelation = 0.0;
        int nummeas = 0;

        for ( int i = 0; i < size; i++ ) {
            int iRowIndex = indicesToSelect[i];
            double[] irow = dataAsRawMatrix[iRowIndex];
            for ( int j = i + 1; j < size; j++ ) {
                int jRowIndex = indicesToSelect[j];
                double[] jrow = dataAsRawMatrix[jRowIndex];
                double corr = Math.abs( DescriptiveWithMissing.correlation( irow, jrow, selfSquaredMatrix[iRowIndex],
                        selfSquaredMatrix[jRowIndex], nanStatusMatrix[iRowIndex], nanStatusMatrix[jRowIndex] ) );
                sumCorrelation += corr;
                nummeas++;
            }
        }
        return sumCorrelation / nummeas;
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