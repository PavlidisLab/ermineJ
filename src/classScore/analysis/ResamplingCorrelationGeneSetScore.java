package classScore.analysis;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.SparseDoubleMatrix2DNamed;
import baseCode.math.DescriptiveWithMissing;
import baseCode.math.RandomChooser;
import cern.colt.list.DoubleArrayList;
import classScore.data.Histogram;
import classScore.gui.GeneSetScoreStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
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

   /**
    * 
    * @param dataMatrix
    */
   public ResamplingCorrelationGeneSetScore( DenseDoubleMatrix2DNamed dataMatrix ) {
      data = dataMatrix;
   }

   /**
    * Build background distributions of within-gene set mean correlations. This
    * requires computing a lot of correlations.
    * 
    * @return histogram containing the random distributions of correlations.
    * @throws OutOfMemoryError
    */
   public Histogram generateNullDistribution( GeneSetScoreStatus messenger )
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
            Thread.sleep( 1 );
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
    * @param correls the correlation matrix for the data. This can be passed in
    *        without having filled it in yet. This means that only values that
    *        are visited during resampling are actually computed - this is a big
    *        memory saver.
    * @param classsize How big this class is.
    * @return
    */
   public double geneSetMeanCorrel( int[] indicesToSelect,
         SparseDoubleMatrix2DNamed correls ) {
      int size = indicesToSelect.length;
      double avecorrel;
      int i, j, nummeas;
      avecorrel = 0;
      nummeas = 0;
      for ( i = 0; i < size; i++ ) {
         for ( j = i + 1; j < size; j++ ) {
            double corr = Math.abs( correls.getQuick( i, j ) );

            if ( corr == 0.0 ) { // we haven't done it yet.
               DoubleArrayList irow = new DoubleArrayList( data.getRow( i ) );
               DoubleArrayList jrow = new DoubleArrayList( data.getRow( j ) );
               corr = DescriptiveWithMissing.correlation( irow, jrow );
               correls.setQuick( i, j, corr );
               correls.setQuick( j, i, corr );
            }

            avecorrel += corr;
            nummeas++;
         }
      }
      return avecorrel / nummeas;
   }

}