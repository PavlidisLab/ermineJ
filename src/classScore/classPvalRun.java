package classScore;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import baseCode.math.Rank;
import classScore.analysis.GeneSetPvalSeriesGenerator;
import classScore.analysis.GeneSetSizeComputer;
import classScore.analysis.MultipleTestCorrector;
import classScore.analysis.OraGeneSetPvalSeriesGenerator;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneSetResult;
import classScore.data.expClassScore;
import classScore.data.Histogram;
import classScore.gui.GeneSetScoreStatus;
import classScore.gui.geneSet.JDetailsFrame;

/**
 * Main class to make 'experiment score' pvalues. Includes multiple test
 * correction. Created :09/02/02
 * 
 * @author Shahmil Merchant; Paul Pavlidis (major changes)
 * @version $Id$
 * @todo set up way to do different types of analysis
 * @todo pass all the maps around in a container instead of as lots of
 *       parameters.
 */
public class classPvalRun {

   private expClassScore probePvalMapper;
   private GeneAnnotations geneData;
   GONames goData; // shared by all
   private Histogram hist;
   private Map results = null;
   private Vector sortedclasses = null; // this holds the results.
   private NumberFormat nf = NumberFormat.getInstance();
   private boolean useUniform = false; // assume input values come from uniform
   // distribution under null hypothesis.

   Settings settings;
   GeneSetScoreStatus messenger;

   public classPvalRun( Settings settings, GeneAnnotations geneData,
         GONames goData, expClassScore probePvalMapper, String mtc_method,
         GeneSetScoreStatus messenger ) throws IllegalArgumentException,
         IOException {
      this.settings = settings;
      this.messenger = messenger;
      this.probePvalMapper = probePvalMapper;
      this.geneData = geneData;
      this.goData = goData;

      nf.setMaximumFractionDigits( 8 );
      results = new LinkedHashMap();
      
      // get the class sizes.
      GeneSetSizeComputer csc = new GeneSetSizeComputer( probePvalMapper,
            geneData, settings.getUseWeights());
      csc.getClassSizes();
      
      Map input_rank_map;
      if ( settings.getUseWeights()  ) {
         input_rank_map = Rank.rankTransform( probePvalMapper
               .get_group_pval_map() );
      } else {     
         input_rank_map = Rank.rankTransform( probePvalMapper.get_map() );
      }
     
      if ( settings.getAnalysisMethod() == Settings.RESAMP ) {
      //   if ( !useUniform ) {
            messenger.setStatus( "Starting resampling" );
            hist = probePvalMapper.generateNullDistribution( messenger );
            messenger.setStatus( "Finished resampling" );
     //    }
         GeneSetPvalSeriesGenerator pvg = new GeneSetPvalSeriesGenerator(
               settings, geneData, hist, probePvalMapper, csc, goData );

         // calculate the actual class scores and correct sorting.
         pvg.classPvalGenerator( probePvalMapper.get_group_pval_map(),
               probePvalMapper.get_map(), input_rank_map );
         results = pvg.getResults();
      } else if ( settings.getAnalysisMethod() == Settings.ORA ) {
         Collection inp_entries = probePvalMapper.get_map().entrySet();
         
         messenger.setStatus("Starting ORA analysis");
         int inputSize = input_rank_map.size();
         OraGeneSetPvalSeriesGenerator pvg = new OraGeneSetPvalSeriesGenerator(
               settings, geneData, probePvalMapper, csc, goData, inputSize);
         pvg.hgSizes(inp_entries);
         pvg.classPvalGenerator(probePvalMapper.get_group_pval_map(),
               probePvalMapper.get_map(), input_rank_map);
         results = pvg.getResults();
         messenger.setStatus("Finished with ORA computations");
      } else {
         throw new UnsupportedOperationException( "Unsupported analysis method" );
      }
      sortResults();

      messenger.setStatus( "Multiple test correction..." );
      MultipleTestCorrector mt = new MultipleTestCorrector( settings,
            sortedclasses, probePvalMapper, hist, geneData, csc, results );
      if ( mtc_method.equals( "bon" ) ) {
         mt.bonferroni();
      } else if ( mtc_method.equals( "bh" ) ) {
         mt.benjaminihochberg( 0.05 );
      } else if ( mtc_method.equals( "wy" ) ) {
         mt.westfallyoung( 10000 );
      }

      // For table output
      for ( int i = 0; i < sortedclasses.size(); i++ ) {
         ( ( GeneSetResult ) results.get( ( String ) sortedclasses.get( i ) ) )
               .setRank( i + 1 );
      }
      messenger.setStatus( "Done!" );
   }

  

   /**
    * @param classID "GO:0000149" for example
    */
   public void showDetails( String classID ) {
      final GeneSetResult res = ( GeneSetResult ) results.get( classID );
      String name = res.getClassName();
      System.out.println( name );
      Map classToProbe = geneData.getClassToProbeMap();

      final ArrayList probeIDs = ( ArrayList ) classToProbe.get( classID );

      final Map pvals = new HashMap();
      for ( int i = 0, n = probeIDs.size(); i < n; i++ ) {
         Double pvalue;
         if ( settings.getDoLog() == true ) {
            pvalue = new Double( Math.pow( 10.0, -probePvalMapper
                  .getPval( ( String ) ( ( ArrayList ) classToProbe
                        .get( classID ) ).get( i ) ) ) );
         } else {
            pvalue = new Double( probePvalMapper
                  .getPval( ( String ) ( ( ArrayList ) classToProbe
                        .get( classID ) ).get( i ) ) );
         }
         pvals.put( ( String ) ( ( ArrayList ) classToProbe.get( classID ) )
               .get( i ), pvalue );
      }

      if ( probeIDs == null ) {
         throw new IllegalStateException( "Class data retrieval error for "
               + name );
      }

      // create the details frame
      JDetailsFrame f = new JDetailsFrame( probeIDs, pvals, geneData, settings );
      f.setTitle( name + " (" + probeIDs.size() + " items) p="
            + nf.format( res.getPvalue() ) );
      f.show();
   }

   /**
    * 
    * @return Map the results
    */
   public Map getResults() {
      return results;
   }

   /**
    * 
    * @return Settings
    */
   public Settings getSettings() {
      return settings;
   }
   
   /* private methods */
   
   /**
    * Sorted order of the class results - all this has to hold is the class
    * names.
    */
   private void sortResults() {
      sortedclasses = new Vector( results.entrySet().size() );
      Collection k = results.values();
      Vector l = new Vector();
      l.addAll( k );
      Collections.sort( l );
      for ( Iterator it = l.iterator(); it.hasNext(); ) {
         sortedclasses.add( ( ( GeneSetResult ) it.next() ).getClassId() );
      }
   }
   
}