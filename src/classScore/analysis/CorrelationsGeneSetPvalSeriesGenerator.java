package classScore.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.math.DescriptiveWithMissing;
import baseCode.util.StatusViewer;
import cern.colt.list.DoubleArrayList;
import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * Calculates the raw average class correlations using a background distribution.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
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
   private Map probeToGeneMap;

   /**
    * @return
    */
   public Map getResults() {
      return results;
   }

   /**
    * @param settings
    * @param geneAnnots
    * @param csc
    * @param gon
    * @param rawData
    * @param hist
    */
   public CorrelationsGeneSetPvalSeriesGenerator( Settings settings,
         GeneAnnotations geneAnnots, GeneSetSizeComputer csc, GONames gon,
         DenseDoubleMatrix2DNamed rawData, Histogram hist ) {
      super( settings, geneAnnots, csc, gon );

      this.probeToGeneMap = geneAnnots.getProbeToGeneMap();
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
    * @param messenger
    * @todo make this faster. cache values?
    */

   // here is the original perl implementation.
   //   sub classcorrel {
   //      my ($classdata, $classweights, $replicategroups) = @_;
   //      $numfeat = scalar @{$$classdata[0]};
   //      my $numclass = scalar @$classdata;
   //      my $nummeas = 0;
   //      my $totalcorrel = 0;
   //      # calculate the correlation. This is optimized to avoid
   //      # recalculation of info about vector x.
   //      my $numskipped = 0;
   //      for ($i = 0; $i < $numclass; $i++) {
   //        $meanx = mean($$classdata[$i]);
   //        $sumx = 0;
   //        $sumxs = 0;
   //        $weightx = $$classweights[$i];
   //        $repgroupx = $$replicategroups[$i];
   //        for ($m=0; $m<$numfeat; $m++) {
   //          $sumx+=$$classdata[$i][$m];
   //          $sumxs+=$$classdata[$i][$m]*$$classdata[$i][$m];
   //        }
   //        # Weighting scheme skips comparisons between replicates, and
   //        # comprisions of replicates to other things are given lower
   //        # weight. For example, if a replicate group has 2 members, when
   //        # we compare those to another gene, we get a total of 1 effective
   //        # measurement.
   //        for ($j = $i+1; $j < $numclass; $j++) { # start from $i+1 so we skip self comparisons.
   //          $repgroupy = $$replicategroups[$j];
   //          if ($doweighting && $repgroupy == $repgroupx) { # skip comparisons between "replicates"
   //            $numskipped++;
   //            next;
   //          }
   //          $weighty = $$classweights[$j];
   //          $meany = mean($$classdata[$j]);
   //          $correlation = correlation_op($sumx, $sumxs, $meanx, $$classdata[$i], $meany, $$classdata[$j]);
   //          $totalweight = $weighty * $weightx;
   //          $totalcorrel += $correlation * $totalweight;
   //          $nummeas+=$totalweight;
   //        }
   //      }
   //      print STDERR "$nummeas effective measurements. $numskipped comparisons skipped. Total weight $totalweight. Total
   // correl $totalcorrel.\n";
   //      return 0 if $nummeas == 0; # this will happen if the class is made up of one replicate group.
   //      return $totalcorrel/$nummeas;
   //    }
   public void geneSetCorrelationGenerator( StatusViewer messenger ) {
      //iterate over each class
      int count = 0;
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
            if ( classSize > probeCorrelData.getMaxClassSize() ) {
               break;
            }
         }

         // to check if class size is ok.
         if ( classSize < probeCorrelData.getMinGeneSetSize()
               || classSize > probeCorrelData.getMaxClassSize() ) {
            continue;
         }

         if ( count > 0 && count % 50 == 0 ) {
            messenger.setStatus( "Classes analyzed: " + count );
         }

         double avecorrel = 0.0;
         double nummeas = 0;

         for ( int i = probesInSet.size() - 1; i >= 0; i-- ) {

            String probei = ( String ) probesInSet.get( i );
            String genei = ( String ) probeToGeneMap.get( probei );
            DoubleArrayList irow = new DoubleArrayList( rawData
                  .getRowByName( probei ) );

            // number of probes for this gene.
            int repsi = geneAnnots.getGeneProbeList( genei ).size();

            for ( int j = i - 1; j >= 0; j-- ) {
               String probej = ( String ) probesInSet.get( j );
               String genej = ( String ) probeToGeneMap.get( probej );

               if ( genei.equals( genej ) ) {
                  continue; // always ignore self-comparisons.
               }

               DoubleArrayList jrow = new DoubleArrayList( rawData
                     .getRowByName( probej ) );

               // todo - implement the 'best' method.
               double corr = Math.abs( DescriptiveWithMissing.correlation(
                     irow, jrow ) );

               // The weight is inversely proportional to the number of times this specific pair is going to get tested.
               double weight = 1.0 / ( geneAnnots.getGeneProbeList( genej )
                     .size() * repsi );

               corr *= weight;

               avecorrel += corr;
               nummeas += weight;
            }
         }
         double geneSetMeanCorrel = avecorrel / nummeas;

         //         for ( Iterator vit = probesInSet.iterator(); vit.hasNext(); ) {
         //            String probe = ( String ) vit.next();
         //            //check if element exists in map
         //            if ( probeCorrelData.containsRow( probe ) ) {
         //
         //               double[] rawCorrelations = rawData.getRowByName( probe );
         //
         //               if ( rawCorrelations.length > 0 ) {
         //                  if ( V == null ) {
         //                     //create a new Matrix so as to add each row of data
         //                     // for a particular probe in matrix
         //                     V = new DenseDoubleMatrix2DNamed( classSize,
         //                           rawCorrelations.length );
         //                  }
         //
         //                  // store value in intermediate Matrix which is used to
         //                  // do correlation calculation
         //                  int j = 0;
         //                  for ( int i = 0; i < rawCorrelations.length; i++ ) {
         //                     V.setQuick( index, j, rawCorrelations[i] );
         //                     j++;
         //                  }
         //                  index++;
         //               }
         //            }
         //         }
         //
         //         if ( V == null ) {
         //            continue;
         //         }
         //
         //         DenseDoubleMatrix2DNamed C = MatrixStats.correlationMatrix( V );
         //         double geneSetMeanCorrel = probeCorrelData.geneSetMeanCorrel( C );
         GeneSetResult result = new GeneSetResult( class_name, goName
               .getNameForId( class_name ), ( ( Integer ) actualSizes
               .get( class_name ) ).intValue(), effSize );
         result.setScore( geneSetMeanCorrel );
         result.setPValue( hist.getValue( classSize, geneSetMeanCorrel ) );
         results.put( class_name, result );
      }
   }
}