package classScore.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.matrix.SparseDoubleMatrix2DNamed;
import baseCode.math.DescriptiveWithMissing;
import baseCode.math.RandomChooser;
import baseCode.util.StatusViewer;
import cern.colt.list.DoubleArrayList;
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
public class ResamplingCorrelationGeneSetScore extends
      AbstractResamplingGeneSetScore {

   protected static final Log log = LogFactory
         .getLog( CorrelationsGeneSetPvalSeriesGenerator.class );

   private DenseDoubleMatrix2DNamed data = null;
   private Settings settings;
   private boolean weights;
   private double[][] dataAsRawMatrix;

   /**
    * @param dataMatrix
    */
   public ResamplingCorrelationGeneSetScore( Settings settings,
         DenseDoubleMatrix2DNamed dataMatrix ) {
      this.settings = settings;
      this.weights = settings.getUseWeights();
      this.classMaxSize = settings.getMaxClassSize();
      this.classMinSize = settings.getMinClassSize();
      this.numRuns = settings.getIterations();
      data = dataMatrix;
      int numGeneSets = classMaxSize - classMinSize + 1;
      this.hist = new Histogram( numGeneSets, classMinSize, numRuns, 1.0, 0.0 );
   }

   /**
    * Build background distributions of within-gene set mean correlations. This requires computing a lot of
    * correlations.
    * 
    * @return histogram containing the random distributions of correlations.
    * @throws OutOfMemoryError
    */
   public Histogram generateNullDistribution( StatusViewer messenger ) {

      SparseDoubleMatrix2DNamed correls = new SparseDoubleMatrix2DNamed( data
            .rows(), data.rows() );

      int[] deck = new int[data.rows()];
      dataAsRawMatrix = new double[data.rows()][]; // we use this so we don't call getQuick() too much.
      for ( int j = 0; j < data.rows(); j++ ) {
         deck[j] = j;
         dataAsRawMatrix[j] = data.getRow( j );
      }

      for ( int i = classMinSize; i <= classMaxSize; i++ ) {
         int[] randomnums = new int[i];

         if ( messenger != null ) {
            messenger.setStatus( "Currently running class size " + i );
         }

         for ( int j = 0; j < numRuns; j++ ) {
            RandomChooser.chooserandom( randomnums, deck, data.rows(), i );
            double avecorrel = geneSetMeanCorrel( randomnums, correls );
            hist.update( i - classMinSize, avecorrel );
            Thread.yield();
         }

         try {
            Thread.sleep( 10 );
         } catch ( InterruptedException e ) {

         }

      }
      hist.tocdf();
      return hist;
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
   public double geneSetMeanCorrel( int[] indicesToSelect,
         SparseDoubleMatrix2DNamed correls ) {

      int size = indicesToSelect.length;
      double avecorrel = 0.0;
      int nummeas = 0;

      for ( int i = 0; i < size; i++ ) {
         int row1 = indicesToSelect[i];
         double[] irow = dataAsRawMatrix[i];

         for ( int j = i + 1; j < size; j++ ) {
            int row2 = indicesToSelect[j];
            //   double corr = Math.abs( correls.getQuick( row1, row2 ) );

            //   if ( corr == 0.0 ) { // we haven't done this one yet it yet.

            double[] jrow = dataAsRawMatrix[j];

            double corr = Math.abs( correlation( irow, jrow ) );
            //         correls.setQuick( row1, row2, corr ); // too much memory.
            //       correls.setQuick( row2, row1, corr );
            //      }

            avecorrel += corr;
            nummeas++;
         }
      }
      return avecorrel / nummeas;
   }

   // special optimized version of correlation computation.
   private static double correlation( double[] x, double[] y ) {
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

         if ( !Double.isNaN( xj ) && !Double.isNaN( yj ) ) {
            sx += xj;
            sy += yj;
            sxy += xj * yj;
            sxx += xj * xj;
            syy += yj * yj;
            numused++;
         }
      }

      if ( numused > 0 ) {
         ay = sy / numused;
         ax = sx / numused;
         return ( sxy - sx * ay )
               / Math.sqrt( ( sxx - sx * ax ) * ( syy - sy * ay ) );
      }
      return Double.NaN; // signifies that it could not be calculated.

   }

}