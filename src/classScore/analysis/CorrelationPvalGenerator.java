package classScore.analysis;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import classScore.Settings;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;

/**
 * 
 * 
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class CorrelationPvalGenerator extends AbstractGeneSetPvalGenerator{

  
   private double histRange = 0;
   private DenseDoubleMatrix2DNamed data = null;
   
   public CorrelationPvalGenerator(Settings settings, GeneAnnotations a,
         GeneSetSizeComputer csc, GONames gon, DenseDoubleMatrix2DNamed data) {
      super( settings, a, csc, gon );
      this.data = data;
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
    * @return
    */
   public double geneSetMeanCorrel( DenseDoubleMatrix2DNamed correls ) {
      int classSize = correls.rows();

      double avecorrel = 0.0;
      int nummeas = 0;
      for ( int i = 0; i < classSize; i++ ) {
         for ( int j = i + 1; j < classSize; j++ ) {
            avecorrel += Math.abs( correls.getQuick( i, j ) );
            nummeas++;
         }
      }
      return avecorrel / nummeas;
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

}