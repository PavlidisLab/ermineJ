package classScore.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.SparseDoubleMatrix2DNamed;
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
   }

   /**
    * Build background distributions of within-gene set mean correlations. This requires computing a lot of
    * correlations.
    * 
    * @return histogram containing the random distributions of correlations.
    * @throws OutOfMemoryError
    */
   public Histogram generateNullDistribution( StatusViewer messenger )
         throws OutOfMemoryError {
      int numGeneSets = classMaxSize - classMinSize + 1;
      Histogram hist = new Histogram( numGeneSets, classMinSize, numRuns, 1.0,
            0.0 );
      SparseDoubleMatrix2DNamed correls = new SparseDoubleMatrix2DNamed( data
            .rows(), data.rows() );

      int[] deck = new int[data.rows()];
      for ( int j = 0; j < data.rows(); j++ ) {
         deck[j] = j;
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
         }
         
         try {
            Thread.sleep( 10 );
         } catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
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
    *        saver.
    * @return mean correlation within the matrix.
    */
   public double geneSetMeanCorrel( int[] indicesToSelect,
         SparseDoubleMatrix2DNamed correls ) {
      int size = indicesToSelect.length;
      double avecorrel;
      int i, j, nummeas;
      avecorrel = 0.0;
      nummeas = 0;
      for ( i = 0; i < size; i++ ) {
         int row1 = indicesToSelect[i];
         DoubleArrayList irow = new DoubleArrayList( data.getRow( row1 ) );

         for ( j = i + 1; j < size; j++ ) {
            int row2 = indicesToSelect[j];
            double corr = Math.abs( correls.getQuick( row1, row2 ) );

            if ( corr == 0.0 ) { // we haven't done this one yet it yet.
               
               DoubleArrayList jrow = new DoubleArrayList( data.getRow( row2 ) );

               corr = Math
                     .abs( DescriptiveWithMissing.correlation( irow, jrow ) );
               //         correls.setQuick( row1, row2, corr ); // too much memory.
               //       correls.setQuick( row2, row1, corr );
            }

            avecorrel += corr;
            nummeas++;
         }
      }
      return avecorrel / nummeas;
   }

}