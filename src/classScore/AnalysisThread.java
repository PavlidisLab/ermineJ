package classScore;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.gui.GuiUtil;
import baseCode.io.reader.DoubleMatrixReader;
import baseCode.util.StatusViewer;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneScoreReader;
import classScore.gui.GeneSetScoreFrame;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 *
 * @author not attributable
 * @version $Id$
 * @todo don't use stop. use interrupt instead, and stop the readers
 */

public class AnalysisThread {
   private volatile Thread athread;
   GeneSetScoreFrame csframe;
   Settings settings;
   StatusViewer messenger;
   GONames goData;
   GeneAnnotations geneData = null;
   Map rawDataSets;
   Map geneScoreSets;
   Map geneDataSets;
   String loadFile;

   public AnalysisThread() {
   }

   public void startAnalysisThread( GeneSetScoreFrame csframe,
                                    Settings settings, StatusViewer messenger, GONames goData,
                                    Map geneDataSets, Map rawDataSets, Map geneScoreSets ) throws
       IllegalStateException {
      this.csframe = csframe;
      this.settings = settings;
      this.messenger = messenger;
      this.goData = goData;
      this.rawDataSets = rawDataSets;
      this.geneScoreSets = geneScoreSets;
      this.geneDataSets = geneDataSets;

      this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer(
          "original".hashCode() ) ); //this is the default geneData

      if ( athread != null )throw new IllegalStateException();
      athread = new Thread( new Runnable() {
         public void run() {
            try {
               doAnalysis();
            }
            catch ( IOException e ) {
               GuiUtil.error( "During analysis", e );
            }
         }
      } );
      athread.start();
   }

   private void doAnalysis( Map results ) throws IOException {

      /* read in the rawData, if we need it, and if we haven't already */
      DenseDoubleMatrix2DNamed rawData = null;
      if ( settings.getAnalysisMethod() == Settings.CORR ) {
         if ( rawDataSets.containsKey( settings.getRawFile() ) ) {
            messenger.setStatus( "Raw data are in memory" );
            rawData = ( DenseDoubleMatrix2DNamed ) rawDataSets.get( settings
                .getRawFile() );
         } else {
            messenger.setStatus( "Reading raw data from file "
                                 + settings.getRawFile() );
            DoubleMatrixReader r = new DoubleMatrixReader();
            rawData = ( DenseDoubleMatrix2DNamed ) r.read( settings
                .getRawFile() );
            rawDataSets.put( settings.getRawFile(), rawData );
         }
      }

      GeneScoreReader geneScores;
      if ( geneScoreSets.containsKey( settings.getScoreFile() ) ) {
         messenger.setStatus( "Gene Scores are in memory" );
         geneScores = ( GeneScoreReader ) geneScoreSets.get( settings
             .getScoreFile() );
      } else {
         messenger.setStatus( "Reading gene scores from file "
                              + settings.getScoreFile() );
         geneScores = new GeneScoreReader( settings.getScoreFile(),
                                           settings, messenger, geneData.getGeneToProbeList() );
         geneScoreSets.put( settings.getScoreFile(), geneScores );
      }

      if ( !settings.getScoreFile().equals( "" ) && geneScores == null ) {
         messenger.setStatus( "Didn't get geneScores" );
      }

      // todo need logic to choose which source of probes to use.
      Set activeProbes = null;
      if ( rawData != null && geneScores != null ) { // favor the geneScores list.
         activeProbes = geneScores.getProbeToPvalMap().keySet();
      } else if ( rawData == null && geneScores != null ) {
         activeProbes = geneScores.getProbeToPvalMap().keySet();
      } else if ( rawData != null && geneScores == null ) {
         activeProbes = new HashSet( rawData.getRowNames() );
      }

      boolean needToMakeNewGeneData = true;
      for ( Iterator it = geneDataSets.keySet().iterator(); it.hasNext(); ) {
         GeneAnnotations test = ( GeneAnnotations ) geneDataSets.get( it
             .next() );

         if ( test.getProbeToGeneMap().keySet().equals( activeProbes ) ) {
            geneData = test;
            needToMakeNewGeneData = false;
            break;
         }

      }

      if ( needToMakeNewGeneData ) {
         geneData = new GeneAnnotations( geneData, activeProbes );
         geneDataSets.put( new Integer( geneData.hashCode() ), geneData );
      }

      /* do work */
      messenger.setStatus( "Starting analysis..." );
      GeneSetPvalRun runResult;
      if ( results != null ) {
         runResult = new GeneSetPvalRun( activeProbes, settings, geneData,
                                       rawData, goData, geneScores, messenger, results );

      } else {
         runResult = new GeneSetPvalRun( activeProbes, settings, geneData,
                                       rawData, goData, geneScores, messenger );
      }

      csframe.addResult( runResult );
      csframe.setSettings( settings );
      csframe.enableMenusForAnalysis();
      athread = null;
   }

   private void doAnalysis() throws IOException {
      doAnalysis( null );
   }

   public void cancelAnalysisThread() {
      if ( athread != null ) {
         athread.stop();
         //athread.interrupt();
         athread = null;
         csframe.enableMenusForAnalysis();
      }
   }

   public void loadAnalysisThread( GeneSetScoreFrame csframe,
                                   Settings settings, StatusViewer messenger, GONames goData,
                                   Map geneDataSets, Map rawDataSets, Map geneScoreSets,
                                   String loadFile ) throws IllegalStateException {
      this.csframe = csframe;
      this.settings = settings;
      this.messenger = messenger;
      this.goData = goData;
      this.loadFile = loadFile;
      this.rawDataSets = rawDataSets;
      this.geneScoreSets = geneScoreSets;
      this.geneDataSets = geneDataSets;

      this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer(
          "original".hashCode() ) ); //this is the default geneData

      if ( athread != null )throw new IllegalStateException();
      athread = new Thread( new Runnable() {
         public void run() {
            loadAnalysis();
         }
      } );
      athread.start();
   }

   private void loadAnalysis() {
      try {
         ResultsFileReader rfr = new ResultsFileReader( loadFile, messenger );
         Map results = rfr.getResults();
         doAnalysis( results );
      }
      catch ( Exception e ) {
         GuiUtil.error( "File format is not valid." );
      }
   }

}
