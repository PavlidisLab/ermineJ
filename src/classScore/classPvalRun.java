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
import java.util.Set;
import java.util.Vector;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.math.Rank;
import classScore.analysis.CorrelationsGeneSetPvalSeriesGenerator;
import classScore.analysis.GeneSetPvalSeriesGenerator;
import classScore.analysis.GeneSetSizeComputer;
import classScore.analysis.MultipleTestCorrector;
import classScore.analysis.NullDistributionGenerator;
import classScore.analysis.OraGeneSetPvalSeriesGenerator;
import classScore.analysis.ResamplingCorrelationGeneSetScore;
import classScore.analysis.ResamplingExperimentGeneSetScore;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneScoreReader;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;
import classScore.gui.GeneSetScoreStatus;
import classScore.gui.geneSet.JGeneSetFrame;

/**
 * Class that does all the work in doing gene set scoring. This class holds the results as well.
 * 
 * @author Shahmil Merchant; Paul Pavlidis (major changes)
 * @version $Id$
 * @todo make multiple test correction a 'setting'.
 */
public class classPvalRun {

   private GeneAnnotations geneData;
   private GeneScoreReader geneScores;
   private GONames goData; // shared by all
   private Histogram hist;
   private Map results = null;
   private Vector sortedclasses = null; // this holds the results.
   private NumberFormat nf = NumberFormat.getInstance();
   private boolean useUniform = false; // assume input values come from uniform
   private Set activeProbes;
   private Settings settings;

   /**
    * Use this when we are loading in existing results.
    * 
    * @param settings
    * @param geneData
    * @param goData
    * @param mtc_method
    * @param messenger
    * @param results
    * @todo could combine with other constructor.
    */
   public classPvalRun(  Set activeProbes, Settings settings,
         GeneAnnotations geneData, DenseDoubleMatrix2DNamed rawData,
         GONames goData, GeneScoreReader geneScores,
         GeneSetScoreStatus messenger,
         Map results ) {
      this.settings = settings;
      this.geneData = geneData;
      this.goData = goData;
      this.geneScores = geneScores;
      this.activeProbes = activeProbes;
      sortResults();

      // get the class sizes.
      GeneSetSizeComputer csc = new GeneSetSizeComputer( activeProbes,
            geneData,  geneScores, settings.getUseWeights() );
      csc.getClassSizes();

      messenger.setStatus( "Multiple test correction..." );
      MultipleTestCorrector mt = new MultipleTestCorrector( settings,
            sortedclasses, hist, geneData, csc, geneScores, results );
      String mtc_method = "bh";
      if ( mtc_method.equals( "bon" ) ) {
         mt.bonferroni();
      } else if ( mtc_method.equals( "bh" ) ) {
         mt.benjaminihochberg( 0.05 );
      } else if ( mtc_method.equals( "wy" ) ) {
         mt.westfallyoung( 10000 );
      }

      // For table output
      for ( int i = 0; i < sortedclasses.size(); i++ ) {
         ( ( GeneSetResult ) results.get( sortedclasses.get( i ) ) )
               .setRank( i + 1 );
      }
      messenger.setStatus( "Done!" );
   }

   /**
    * Do a new analysis.
    * @param activeProbes
    * @param settings
    * @param geneData
    * @param rawData
    * @param goData
    * @param geneScores
    * @param messenger
    * @throws IllegalArgumentException
    */
   public classPvalRun( Set activeProbes, Settings settings,
         GeneAnnotations geneData, DenseDoubleMatrix2DNamed rawData,
         GONames goData, GeneScoreReader geneScores,
         GeneSetScoreStatus messenger ) throws IllegalArgumentException {
      this.settings = settings;
      this.geneData = geneData;
      this.goData = goData;
      this.geneScores = geneScores;
      this.activeProbes = activeProbes;

      nf.setMaximumFractionDigits( 8 );
      results = new LinkedHashMap();

      // get the class sizes.
      GeneSetSizeComputer csc = new GeneSetSizeComputer( activeProbes,
            geneData, geneScores, settings.getUseWeights() );
      csc.getClassSizes();

      switch ( settings.getAnalysisMethod() ) {
         case Settings.RESAMP: {
            NullDistributionGenerator probePvalMapper = new ResamplingExperimentGeneSetScore(
                  settings, geneScores );
            messenger.setStatus( "Starting resampling" );
            hist = probePvalMapper.generateNullDistribution( messenger );
            messenger.setStatus( "Finished resampling" );
            //    }
            GeneSetPvalSeriesGenerator pvg = new GeneSetPvalSeriesGenerator(
                  settings, geneData, hist, csc, goData );

            // calculate the actual class scores and correct sorting.
            pvg.classPvalGenerator( geneScores.getGroupToPvalMap(), geneScores
                  .getProbeToPvalMap() );
            results = pvg.getResults();
            break;
         }
         case Settings.ORA: {

            int inputSize = activeProbes.size();

            Collection inp_entries = geneScores.getProbeToPvalMap().entrySet();

            messenger.setStatus( "Starting ORA analysis" );

            OraGeneSetPvalSeriesGenerator pvg = new OraGeneSetPvalSeriesGenerator(
                  settings, geneData, csc, goData, inputSize );
            pvg.hgSizes( inp_entries );
            pvg.classPvalGenerator( geneScores.getGroupToPvalMap(), geneScores
                  .getProbeToPvalMap() );
            results = pvg.getResults();
            messenger.setStatus( "Finished with ORA computations" );
            break;
         }
         case Settings.CORR: {
            messenger.setStatus( "Starting correlation resampling" );
            NullDistributionGenerator probePvalMapper = new ResamplingCorrelationGeneSetScore(
                  rawData );
            hist = probePvalMapper.generateNullDistribution( messenger );

            CorrelationsGeneSetPvalSeriesGenerator pvg = new CorrelationsGeneSetPvalSeriesGenerator(
                  settings, geneData, csc, goData, rawData, hist );
            messenger
                  .setStatus( "Finished resampling, computing for gene sets" );
            pvg.geneSetCorrelationGenerator();
            messenger.setStatus( "Finished computing scores" );
            results = pvg.getResults();
            break;
         }
         case Settings.ROC: { // todo implement this.
            Map input_rank_map;
            if ( settings.getUseWeights() ) {
               input_rank_map = Rank.rankTransform( geneScores
                     .getGroupToPvalMap() );
            } else {
               input_rank_map = Rank.rankTransform( geneScores
                     .getProbeToPvalMap() );
            }
            // fall through. - unsupported.
         }
         default: {
            throw new UnsupportedOperationException(
                  "Unsupported analysis method" );
         }
      }

      sortResults();

      messenger.setStatus( "Multiple test correction..." );
      MultipleTestCorrector mt = new MultipleTestCorrector( settings,
            sortedclasses, hist, geneData, csc, geneScores, results );
      String mtc_method = "bh";
      if ( mtc_method.equals( "bon" ) ) {
         mt.bonferroni();
      } else if ( mtc_method.equals( "bh" ) ) {
         mt.benjaminihochberg( 0.05 );
      } else if ( mtc_method.equals( "wy" ) ) {
         mt.westfallyoung( 10000 );
      }

      // For table output
      for ( int i = 0; i < sortedclasses.size(); i++ ) {
         ( ( GeneSetResult ) results.get( sortedclasses.get( i ) ) )
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
            pvalue = new Double( Math
                  .pow( 10.0, -( ( Double ) geneScores.getProbeToPvalMap().get(
                        probeIDs.get( i ) ) ).doubleValue() ) );
         } else {
            pvalue = ( Double ) geneScores.getProbeToPvalMap().get(
                  probeIDs.get( i ) );
         }
         pvals.put( ( ( ArrayList ) classToProbe.get( classID ) ).get( i ),
               pvalue );
      }

      if ( probeIDs == null ) {
         throw new IllegalStateException( "Class data retrieval error for "
               + name );
      }

      // create the details frame
      JGeneSetFrame f = new JGeneSetFrame( probeIDs, pvals, geneData, settings );
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
    * @return Map the results
    */
   public Vector getSortedClasses() {
      return sortedclasses;
   }

   public GeneAnnotations getGeneData() {
      return geneData;
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
    * Sorted order of the class results - all this has to hold is the class names.
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