package classScore;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.reader.DoubleMatrixReader;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneScoreReader;
import classScore.gui.GeneSetScoreFrame;
import classScore.gui.GeneSetScoreStatus;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version $Id$
 * @todo don't use stop. use interrupt instead, and stop the readers
 */

public class AnalysisThread {
   private volatile Thread athread;
   GeneSetScoreFrame csframe;
   Settings settings;
   GeneSetScoreStatus messenger;
   GONames goData;
   GeneAnnotations geneData = null;
   Map rawDataSets;
   Map geneScoreSets;
   Map geneDataSets;
   String loadFile;

   public AnalysisThread() {
   }

   public void startAnalysisThread( GeneSetScoreFrame csframe,
         Settings settings, GeneSetScoreStatus messenger, GONames goData,
         Map geneDataSets, Map rawDataSets, Map geneScoreSets )
         throws IllegalStateException {
      this.csframe = csframe;
      this.settings = settings;
      this.messenger = messenger;
      this.goData = goData;
      this.rawDataSets = rawDataSets;
      this.geneScoreSets = geneScoreSets;
      this.geneDataSets = geneDataSets;
      // @todo check this
      this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer(
            "original".hashCode() ) ); //this is the default geneData

      if ( athread != null ) throw new IllegalStateException();
      athread = new Thread( new Runnable() {
         public void run() {
            doAnalysis();
         }
      } );
      athread.start();
   }

   private void doAnalysis( Map results ) {
      try {
         messenger.setStatus( "Starting analysis..." );

         /* read in the rawData, if we haven't already */
         // todo maybe only read if we need it.
         DenseDoubleMatrix2DNamed rawData=null;
         /*
         if ( rawDataSets.containsKey( settings.getRawFile() ) ) {
            messenger.setStatus( "Raw data are in memory");
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
         */
         GeneScoreReader geneScores;
         if ( geneScoreSets.containsKey( settings.getScoreFile() ) ) {
            messenger.setStatus( "Gene Scores are in memory");
            geneScores = ( GeneScoreReader ) geneScoreSets.get( settings
                  .getScoreFile() );
         } else {
            messenger.setStatus( "Reading gene scores from file "
                  + settings.getScoreFile() );
            geneScores = new GeneScoreReader( settings.getScoreFile(),
                  settings, messenger, geneData.getGeneToProbeList() );
            geneScoreSets.put( settings.getScoreFile(), geneScores );
         }

         if (!settings.getScoreFile().equals("") && geneScores == null) {
            messenger.setStatus("Didn't get geneScores");
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

         for ( Iterator it = geneDataSets.keySet().iterator(); it.hasNext(); ) {
            GeneAnnotations test = ( GeneAnnotations ) geneDataSets.get( it
                  .next() );

            if ( test.getProbeToGeneMap().keySet().equals( activeProbes ) ) {
               geneData = test;
               break;
            }

         }

         if ( geneData == null ) {
            geneData = new GeneAnnotations( geneData, activeProbes );
            geneDataSets.put( new Integer( geneData.hashCode() ), geneData );
         }

         //         GeneGroupReader groupName = new GeneGroupReader( geneData
         //               .getGeneToProbeList(), geneData.getProbeToGeneMap() );

         /* do work */
         classPvalRun runResult;
         if ( results != null ) {
            runResult = new classPvalRun( activeProbes, settings, geneData,
                  rawData, goData, geneScores, messenger, results );

         } else {
            runResult = new classPvalRun( activeProbes, settings, geneData,
                  rawData, goData, geneScores, messenger );
         }

         csframe.addResult( runResult );
         csframe.setSettings( settings );
         csframe.enableMenusForAnalysis();
         athread = null;
      } catch ( IOException ioe ) {
         //do something
      }
   }

   private void doAnalysis() {
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
         Settings settings, GeneSetScoreStatus messenger, GONames goData,
         Map geneDataSets, Map rawDataSets, Map geneScoreSets, String loadFile )
         throws IllegalStateException {
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

      if ( athread != null ) throw new IllegalStateException();
      athread = new Thread( new Runnable() {
         public void run() {
            loadAnalysis();
         }
      } );
      athread.start();
   }

   private void loadAnalysis() {
      try {
         messenger.setStatus( "Loading analysis..." );
         ResultsFileReader rfr = new ResultsFileReader( loadFile );
         Map results = rfr.getResults();
         doAnalysis(results);
      } catch ( IOException ioe ) {
         ioe.printStackTrace();
      }
   }

}
