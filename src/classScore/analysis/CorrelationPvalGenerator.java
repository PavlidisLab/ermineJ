package classScore.analysis;

import java.io.IOException;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.reader.DoubleMatrixReader;

/**
 * 
 * 
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class CorrelationPvalGenerator {

  
   private int maxGeneSetSize = 100;
   private int numRuns = 10000;
   private double histRange = 0;
   private int minGeneSetSize = 2;
   private DenseDoubleMatrix2DNamed data = null;
   
   public CorrelationPvalGenerator(DenseDoubleMatrix2DNamed data) {
      this.data = data;
   }
   
   /**
    * 
    * @param dataFileName
    * @throws IOException
    */
   public CorrelationPvalGenerator( String dataFileName )
         throws IOException, OutOfMemoryError {
      DoubleMatrixReader r = new DoubleMatrixReader();
      System.err.println( "Reading data..." );
      data = ( DenseDoubleMatrix2DNamed ) r.read( dataFileName );
      System.err.println( "Read data from " + dataFileName );
   }
   

   /**
    * 
    * @param name
    * @return
    */
   public boolean containsRow( String name ) {
      return data.containsRowName( name );
   }

   /**
    * 
    * @return
    */
   public DenseDoubleMatrix2DNamed getData() {
      return data;
   }


   /**
    * 
    * @param correls
    * @param classsize
    * @return
    */
   public double geneSetMeanCorrel( DenseDoubleMatrix2DNamed correls ) {
      int classSize = correls.rows();

      double avecorrel;
      int nummeas;
      avecorrel = 0;
      nummeas = 0;
      for ( int i = 0; i < classSize; i++ ) {
         for ( int j = i + 1; j < classSize; j++ ) {
            avecorrel += Math.abs( correls.getQuick( i, j ) );
            nummeas++;
         }
      }
      return avecorrel / nummeas;
   }

   
   /**
    * 
    * @param value
    */
   public void set_class_max_size( int value ) {
      maxGeneSetSize = value;
   }

   /**
    * 
    * @param value
    */
   public void set_class_min_size( int value )

   {
      minGeneSetSize = value;
   }

   /**
    */
   public void set_number_of_runs( int value ) {
      numRuns = value;
   }

   /**
    */
   public int get_class_max_size() {
      return maxGeneSetSize;
   }

   /**
    */
   public void set_range( double range ) {
      histRange = range;
   }

   /**
    */
   public double get_range() {
      return histRange;
   }

   /**
    * 
    * @return
    */
   public int getMinGeneSetSize() {
      return minGeneSetSize;
   }

}