package classScore.analysis;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import classScore.Settings;

/**
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class CorrelationPvalGenerator extends AbstractGeneSetPvalGenerator {

   private double histRange = 0;
   private DenseDoubleMatrix2DNamed data = null;

   public CorrelationPvalGenerator( Settings settings, GeneAnnotations a,
         GeneSetSizeComputer csc, GONames gon, DenseDoubleMatrix2DNamed data ) {
      super( settings, a, csc, gon );
      this.data = data;
   }

   /**
    * @param name
    * @return
    */
   public boolean containsRow( String name ) {
      return data.containsRowName( name );
   }

   /**
    * @return
    */
   public DenseDoubleMatrix2DNamed getData() {
      return data;
   }

   /**
    * Note that we don't worry about replicates here - it would slow things down too much.
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