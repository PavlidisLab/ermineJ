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

import classScore.gui.geneSet.JDetailsFrame;
import classScore.analysis.ClassPvalSetGenerator;
import classScore.analysis.ClassSizeComputer;
import classScore.analysis.MultipleTestCorrector;
import baseCode.math.Rank;

/**
  Main class to make 'experiment score' pvalues. Includes multiple
  test correction.   Created :09/02/02
  @author Shahmil Merchant; Paul Pavlidis (major changes)
  @version $Id$
 * @todo set up way to do different types of analysis
 * @todo pass all the maps around in a container instead of as lots of parameters.
 */
public class classPvalRun {

   private expClassScore probePvalMapper;
   private GeneAnnotations geneData;
   private Map probeGroups;
   private Map classToProbe;
   private histogram hist;
   private boolean weight_on = true;
   private Map results = null;
   private Vector sortedclasses = null; // this holds the results.
   private int inputSize;
   private int numOverThreshold = 0; // number of genes over the threshold
   private int numUnderThreshold = 0; // number of genes below the threshold
   private NumberFormat nf = NumberFormat.getInstance();
   private boolean useUniform = false; // assume input values come from uniform distribution under null hypothesis.

   Settings settings;
   classScoreStatus messenger;

   public classPvalRun( Settings settings,
                        GeneAnnotations geneData,
                        GONames goData,
                        expClassScore probePvalMapper,
                        String resultsFile,
                        String mtc_method,
                        classScoreStatus messenger,
                        boolean loadResults ) throws
       IllegalArgumentException, IOException {
      this.settings = settings;
      this.messenger = messenger;
      this.probePvalMapper = probePvalMapper;
      this.geneData = geneData;
      this.probeGroups = geneData.getProbeToGeneMap();
      this.classToProbe = geneData.getClassToProbeMap();

      nf.setMaximumFractionDigits( 8 );

      // user flags and constants:
      //    user_pvalue = -(Math.log(pval) / Math.log(10)); // user defined pval (cutoff) for hypergeometric todo: this should NOT be here. What if the cutoff isn't a pvalue. See pvalue parse.
      weight_on = ( Boolean.valueOf( settings.getUseWeights() ) ).booleanValue();
      //dest_file = resultsFile;

      if ( loadResults ) {
         readResultsFromFile( resultsFile );
         sortResults();
      }
      else {
         // Calculate random classes. todo: what a mess. This histogram should be held by the class that originated it.
         if ( !useUniform ) {
            messenger.setStatus( "Starting resampling" );
            System.out.println( "Starting resampling" );
            hist = probePvalMapper.generateNullDistribution( messenger );
            messenger.setStatus( "Finished resampling" );
         }
         System.out.println( "Hist to string: " + hist.toString() );
         // Initialize the results data structure.
         results = new LinkedHashMap();
         // get the class sizes. /* todo use initmap */
         ClassSizeComputer csc = new ClassSizeComputer( probePvalMapper,
             classToProbe, probeGroups,
             weight_on );
         csc.getClassSizes();

         //    Collection inp_entries; // this is only used for printing.
         Map input_rank_map;
         if ( weight_on ) {
            //      inp_entries = probePvalMapper.get_group_pval_map().entrySet();
            input_rank_map = Rank.rankTransform( probePvalMapper.
                                                 get_group_pval_map() );
         }
         else {
            //        inp_entries = probePvalMapper.get_map().entrySet();
            input_rank_map = Rank.rankTransform( probePvalMapper.get_map() );
         }
         inputSize = input_rank_map.size(); // how many pvalues. This is constant under permutations of the data

         // hgSizes(inp_entries); // get numOverThreshold and numUnderThreshold. Constant under permutations of the data.

         System.out.println( "Input size=" + inputSize + " numOverThreshold=" +
                             numOverThreshold + " numUnderThreshold=" +
                             numUnderThreshold + " " );

         /* todo use initmap */
         ClassPvalSetGenerator pvg = new ClassPvalSetGenerator( classToProbe,
             probeGroups, weight_on,
             hist, probePvalMapper, csc, goData );

         // calculate the actual class scores and correct sorting. /** todo make this use initmap */
         pvg.classPvalGenerator( probePvalMapper.get_group_pval_map(),
                                 probePvalMapper.get_map(),
                                 input_rank_map );
         results = pvg.getResults();
         sortResults();

         messenger.setStatus( "Multiple test correction" );

         /** todo use initmap */
         MultipleTestCorrector mt = new MultipleTestCorrector( sortedclasses,
             results,
             probePvalMapper, weight_on, hist, probeGroups, classToProbe,
             csc );
         if ( mtc_method.equals( "bon" ) ) {
            mt.bonferroni(); // no arg: bonferroni. integer arg: w-y, int trials. Double arg: FDR
         } else if ( mtc_method.equals( "bh" ) ) {
            mt.benjaminihochberg( 0.05 );
         } else if ( mtc_method.equals( "wy" ) ) {
            mt.westfallyoung( 10000 );
         }

         messenger.setStatus( "Beginning output" );
         // all done:
         // print the results
         //if ( dest_file.compareTo( "" ) != 0 ) {
         //  ResultsPrinter rpr = new ResultsPrinter( dest_file, sortedclasses,
         //      results, goData, probeToClassMap );
         //}
         //printResults(true);
      }

      //for table output
      for ( int i = 0; i < sortedclasses.size(); i++ ) {
         ( ( classresult ) results.get( ( String ) sortedclasses.get( i ) ) ).setRank( i +
             1 );
      }
      messenger.setStatus( "Done!" );
   }

   /* Done! */

   /**
    Sorted order of the class results - all this has to hold is the class names.
    */
   private void sortResults() {
      sortedclasses = new Vector( results.entrySet().size() );
      Collection k = results.values();
      Vector l = new Vector();
      l.addAll( k );
      Collections.sort( l );
      for ( Iterator it = l.iterator(); it.hasNext(); ) {
         sortedclasses.add( ( ( classresult ) it.next() ).getClassId() );
      }
   }

   /**
    * @param id String
    */
   public void showDetails( String id ) {
      final classresult res = (classresult) results.get(id);
      String name = res.getClassName();
      System.out.println( name );
      final ArrayList values = ( ArrayList ) classToProbe.get( id );

      final Map pvals = new HashMap();
      for ( int i = 0, n = values.size(); i < n; i++ ) {
         //System.err.println((String ) ( ( ArrayList ) classToProbe.get( id ) ).get( i ) );
         Double pvalue = new Double( Math.pow( 10.0,
                                               -probePvalMapper.getPval( (
             String ) ( ( ArrayList ) classToProbe.get( id ) ).get( i ) ) ) );
         pvals.put( ( String ) ( ( ArrayList ) classToProbe.get( id ) ).get( i ), pvalue );
      }

      if ( values == null ) {
         throw new RuntimeException( "Class data retrieval error for " + name );
      }

      // create the details frame
      JDetailsFrame f = new JDetailsFrame(
          values, pvals, classToProbe, id, geneData, settings
          );
      f.setTitle( name + " (" + values.size() + " items)" );
      f.show();
   }

   /* */
   private void readResultsFromFile( String destination_file ) {
      ResultsFileReader f = new ResultsFileReader( destination_file );
      this.results = f.getResults();
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
}
