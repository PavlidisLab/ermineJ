package classScore.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import classScore.Settings;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

import baseCode.dataFilter.RowNameFilter;
import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.math.MatrixStats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Calculates the raw average class correlations using a background distribution.
 * 
 * @author Shahmil Merchant, Paul Pavlidis (MAJOR fixes and refactoring!)
 * @version $Id$
 */
public class CorrelationsGeneSetPvalSeriesGenerator extends
      AbstractGeneSetPvalGenerator {
   
   protected static final Log log = LogFactory
         .getLog( CorrelationsGeneSetPvalSeriesGenerator.class );
   
   private Histogram hist;
   private CorrelationPvalGenerator probeCorrelData;
   private Map probeToGeneSetMap; // stores probe->go Hashtable
   private Map geneSetToProbeMap; // stores go->probe Hashtable
   private Map results;
   private DenseDoubleMatrix2DNamed rawData;

   /**
    * 
    * @return
    */
   public Map getResults() {
      return results;
   }

   /**
    * 
    * @param probe_datafile
    * @param probe_annotfile
    * @param class_max_size
    * @param class_min_size
    * @param number_of_runs
    * @param range
    * @throws IOException
    * @todo do we need to pass the data file name here.
    */
   public CorrelationsGeneSetPvalSeriesGenerator( Settings settings,
         GeneAnnotations geneAnnots, GeneSetSizeComputer csc, GONames gon,
         DenseDoubleMatrix2DNamed rawData, Histogram hist ) {
      super( settings, geneAnnots, csc, gon );
      this.probeCorrelData = new CorrelationPvalGenerator( settings,
            geneAnnots, csc, gon, rawData ); // main data file
      this.geneAnnots = geneAnnots;
      this.probeToGeneSetMap = geneAnnots.getProbeToClassMap();
      this.geneSetToProbeMap = geneAnnots.getClassToProbeMap();
      this.hist = hist;
      this.rawData = rawData;
      probeCorrelData.set_class_max_size( settings.getMaxClassSize() );
      probeCorrelData.set_class_min_size( settings.getMinClassSize() );
      results = new HashMap();
   }

   /**
    * 
    *  
    */
   public void geneSetCorrelationGenerator() {
      RowNameFilter f = new RowNameFilter();
      
      //iterate over each class
      for ( Iterator it = geneSetToProbeMap.entrySet().iterator(); it.hasNext(); ) {
         Map.Entry e = ( Map.Entry ) it.next();
         ArrayList probesInSet = ( ArrayList ) e.getValue();
         String class_name = ( String ) e.getKey();
         int effSize = ( ( Integer ) effectiveSizes.get( class_name ) )
               .intValue();

         if ( effSize < probeCorrelData.getMinGeneSetSize() ) {
            continue; // then there is no hope.
         }

         //this calculation is done just in case the hashtable has no value
         // for an element...hence we keep track of the number of elements
         // with values and then create a Matrix to be used for correlation
         // based on that
         int classSize = 0;
         for ( Iterator mit = probesInSet.iterator(); mit.hasNext(); ) {
            String element = ( String ) mit.next();
            if ( probeCorrelData.containsRow( element ) ) {
               classSize++;
            }
            if (classSize > probeCorrelData.getMaxClassSize()) {
               break;
            }
         }

         // to check if class size
         if ( classSize < probeCorrelData.getMinGeneSetSize()
               || classSize > probeCorrelData.getMaxClassSize() ) {
            continue;
         }

      
         DenseDoubleMatrix2DNamed V = null;
         int index = 0;

         System.err.println( class_name + " " + classSize );

         /** @todo this is too slooow
          * @todo need to do weighting */
         for ( Iterator vit = probesInSet.iterator(); vit.hasNext(); ) {
            String probe = ( String ) vit.next();
            //check if element exists in map
            if ( probeCorrelData.containsRow( probe ) ) {

               double[] rawCorrelations = rawData.getRowByName( probe );

               if ( rawCorrelations.length > 0 ) {
                  if ( V == null ) {
                     //create a new Matrix so as to add each row of data
                     // for a particular probe in matrix
                     V = new DenseDoubleMatrix2DNamed( classSize,
                           rawCorrelations.length );
                  }

                  //store value in intermediate Matrix which is used to
                  // correlation calculation
                  int j = 0;
                  for ( int i = 0; i < rawCorrelations.length; i++ ) {
                     V.setQuick( index, j, rawCorrelations[i] );
                     j++;
                  }
                  index++;
               }
            }
         }

         if ( V == null ) {
            continue;
         }

         DenseDoubleMatrix2DNamed C = MatrixStats.correlationMatrix( V );
         double geneSetMeanCorrel = probeCorrelData.geneSetMeanCorrel( C );
         GeneSetResult result = new GeneSetResult( class_name, goName
               .getNameForId( class_name ), ( ( Integer ) actualSizes
               .get( class_name ) ).intValue(), effSize );
         result.setScore( geneSetMeanCorrel );
         result.setPValue( hist.getValue( classSize, geneSetMeanCorrel ) );
         results.put(class_name, result);
      }
   }
}