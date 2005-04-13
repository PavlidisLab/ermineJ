package classScore;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.gui.GuiUtil;
import baseCode.io.reader.DoubleMatrixReader;
import baseCode.util.StatusViewer;
import classScore.data.GeneScoreReader;
import classScore.gui.GeneSetScoreFrame;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author not attributable
 * @version $Id$
 */

public class AnalysisThread {
    /**
     * Thread to run the analysis.
     */
    private final class Athread extends Thread {

        private boolean stop = false;
        Object owner;

        Method runningMethod;

        public Athread( Method runner, Object owner ) {
            this.runningMethod = runner;
            this.owner = owner;
        }

        /**
         * @return Returns the stop.
         */
        public synchronized boolean isStop() {
            return this.stop;
        }

        public void run() {
            Thread myThread = Thread.currentThread();
            try {
                runningMethod.invoke( owner, null );
            }
            // catch ( RuntimeException e ) {
            // // interruption
            // csframe.enableMenusForAnalysis();
            // messenger.setStatus( "Ready" );
            // return;
            // }
            catch ( Exception e ) {
                GuiUtil.error( "Error During analysis: ", e );
                e.printStackTrace();
                csframe.enableMenusForAnalysis();
                messenger.setStatus( "Ready" );
            }
        }

        /**
         * @param stop The stop to set.
         */
        public synchronized void setStop( boolean stop ) {
            this.stop = stop;
        }
    }

    protected static final Log log = LogFactory.getLog( AnalysisThread.class );
    private volatile Athread athread;
    GeneSetScoreFrame csframe;
    GeneAnnotations geneData = null;
    Map geneDataSets;
    Map geneScoreSets;
    GONames goData;
    String loadFile;
    StatusViewer messenger;
    int numRuns = 0;
    Settings oldSettings = null;
    Map rawDataSets;

    Settings settings;

    /**
     * Cancel the current analysis.
     */
    public void cancelAnalysisThread() {
        if ( athread != null ) {
            log.debug( "Canceling" );
            athread.setStop( true );
            athread.interrupt();
            // athread.stop();
            athread = null;
            csframe.enableMenusForAnalysis();
            messenger.setStatus( "Ready" );
        }
    }

    /**
     * @throws IOException
     */
    public void doAnalysis() throws IOException {
        doAnalysis( null );
    }

    /**
     * @throws IOException
     */
    public void loadAnalysis() throws IOException {
        ResultsFileReader rfr = new ResultsFileReader( loadFile, messenger );
        Map results = rfr.getResults();
        doAnalysis( results );
    }

    /**
     * @param csframe
     * @param settings
     * @param messenger
     * @param goData
     * @param geneDataSets
     * @param rawDataSets
     * @param geneScoreSets
     * @param loadFile
     * @throws IllegalStateException
     */
    public void loadAnalysisThread( final GeneSetScoreFrame csframe, Settings settings, final StatusViewer messenger,
            GONames goData, Map geneDataSets, Map rawDataSets, Map geneScoreSets, String loadFile )
            throws IllegalStateException {
        this.csframe = csframe;
        this.settings = settings;
        this.messenger = messenger;
        this.goData = goData;
        this.loadFile = loadFile;
        this.rawDataSets = rawDataSets;
        this.geneScoreSets = geneScoreSets;
        this.geneDataSets = geneDataSets;

        this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer( "original".hashCode() ) ); // this is the
        // default
        // geneData

        if ( athread != null ) throw new IllegalStateException(); // two analyses at once.
        try {
            athread = new Athread( AnalysisThread.class.getMethod( "loadAnalysis", new Class[] {} ), this );
            athread.setName( "Loaded analysis thread" );
            athread.start();
        } catch ( SecurityException e ) {
            e.printStackTrace();
        } catch ( NoSuchMethodException e ) {
            e.printStackTrace();
        }

    }

    /**
     * @param csframe
     * @param settings
     * @param messenger
     * @param goData
     * @param geneDataSets
     * @param rawDataSets
     * @param geneScoreSets
     * @throws IllegalStateException
     */
    public void startAnalysisThread( final GeneSetScoreFrame csframe, Settings settings, final StatusViewer messenger,
            GONames goData, Map geneDataSets, Map rawDataSets, Map geneScoreSets ) throws IllegalStateException {
        this.csframe = csframe;
        this.settings = settings;
        this.messenger = messenger;
        this.goData = goData;
        this.rawDataSets = rawDataSets;
        this.geneScoreSets = geneScoreSets;
        this.geneDataSets = geneDataSets;

        this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer( "original".hashCode() ) ); // this is the
        // default
        // geneData

        if ( athread != null ) throw new IllegalStateException( "Somehow two analyses are running at once." );
        try {
            athread = new Athread( AnalysisThread.class.getMethod( "doAnalysis", new Class[] {} ), this );
            athread.setName( "Analysis Thread" );
            athread.start();
        } catch ( SecurityException e ) {
            e.printStackTrace();
        } catch ( NoSuchMethodException e ) {
            e.printStackTrace();
        }

    }

    /**
     * @return
     * @throws IOException
     */
    private GeneScoreReader addGeneScores() throws IOException {
        GeneScoreReader geneScores = null;
        if ( !geneScoreSettingsDirty() && geneScoreSets.containsKey( settings.getScoreFile() ) ) {
            messenger.setStatus( "Gene Scores are in memory" );
            geneScores = ( GeneScoreReader ) geneScoreSets.get( settings.getScoreFile() );
        } else {
            try {
            messenger.setStatus( "Reading gene scores from file " + settings.getScoreFile() );
            geneScores = new GeneScoreReader( settings.getScoreFile(), settings, messenger, geneData
                    .getGeneToProbeList(), geneData.getProbeToGeneMap() );
            } catch (RuntimeException e) {
                return null; // interrupted.
            }
            geneScoreSets.put( settings.getScoreFile(), geneScores );
        }
        if ( !settings.getScoreFile().equals( "" ) && geneScores == null ) {
            messenger.setStatus( "Didn't get geneScores" );
        }
        return geneScores;
    }

    /**
     * @return
     * @throws IOException
     */
    private DenseDoubleMatrix2DNamed addRawData() throws IOException {
        DenseDoubleMatrix2DNamed rawData;
        if ( rawDataSets.containsKey( settings.getRawFile() ) ) {
            messenger.setStatus( "Raw data are in memory" );
            rawData = ( DenseDoubleMatrix2DNamed ) rawDataSets.get( settings.getRawFile() );
        } else {
            messenger.setStatus( "Reading raw data from file " + settings.getRawFile() );
            DoubleMatrixReader r = new DoubleMatrixReader();
            rawData = ( DenseDoubleMatrix2DNamed ) r.read( settings.getRawFile() );
            rawDataSets.put( settings.getRawFile(), rawData );
        }
        return rawData;
    }

    /**
     * @param results
     * @throws IOException
     */
    private void doAnalysis( Map results ) throws IOException {

        DenseDoubleMatrix2DNamed rawData = null;
        if ( settings.getAnalysisMethod() == Settings.CORR ) {
            rawData = addRawData();
        }
        GeneScoreReader geneScores = addGeneScores();
        if ( Thread.currentThread().isInterrupted() ) return;
        Set activeProbes = getActiveProbes( rawData, geneScores );
        if ( activeProbes == null || Thread.currentThread().isInterrupted() ) return;

        boolean needToMakeNewGeneData = needNewGeneData( activeProbes );
        GeneAnnotations useTheseAnnots = geneData;
        if ( needToMakeNewGeneData ) {
            useTheseAnnots = new GeneAnnotations( geneData, activeProbes );
            // todo I don't like this
            // way of keeping track of
            // the different geneData
            // sets.
            geneDataSets.put( new Integer( useTheseAnnots.hashCode() ), useTheseAnnots );
        }

        if ( Thread.currentThread().isInterrupted() ) return;

        /* do work */
        messenger.setStatus( "Starting analysis..." );
        numRuns++;
        GeneSetPvalRun runResult = null;
        if ( results != null ) { // read from a file.
            runResult = new GeneSetPvalRun( activeProbes, settings, useTheseAnnots, rawData, goData, geneScores,
                    messenger, results, new Integer( numRuns ).toString() );
        } else {
            try {
                runResult = new GeneSetPvalRun( activeProbes, settings, useTheseAnnots, rawData, goData, geneScores,
                        messenger, new Integer( numRuns ).toString() );
            } catch ( RuntimeException e ) {
                return;
            }
        }

        if ( Thread.currentThread().isInterrupted() ) return;

        if ( runResult != null ) csframe.addResult( runResult );

        csframe.setSettings( settings );
        oldSettings = settings;
        csframe.enableMenusForAnalysis();
        athread = null;
    }

    // see if we have to read the gene scores or if we can just use the old ones
    private boolean geneScoreSettingsDirty() {
        if ( oldSettings == null ) return true;
        return ( settings.getDoLog() != oldSettings.getDoLog() )
                || ( settings.getScorecol() != oldSettings.getScorecol() );
    }

    /**
     * @param rawData
     * @param geneScores
     * @param activeProbes
     * @return
     */
    private Set getActiveProbes( DenseDoubleMatrix2DNamed rawData, GeneScoreReader geneScores ) {
        Set activeProbes = null;
        if ( rawData != null && geneScores != null ) { // favor the geneScores list.
            activeProbes = geneScores.getProbeToPvalMap().keySet();
        } else if ( rawData == null && geneScores != null ) {
            activeProbes = geneScores.getProbeToPvalMap().keySet();
        } else if ( rawData != null && geneScores == null ) {
            activeProbes = new HashSet( rawData.getRowNames() );
        }
        return activeProbes;
    }

    /**
     * @param activeProbes
     * @param needToMakeNewGeneData
     * @return
     */
    private boolean needNewGeneData( Set activeProbes ) {
        for ( Iterator it = geneDataSets.keySet().iterator(); it.hasNext(); ) {
            GeneAnnotations test = ( GeneAnnotations ) geneDataSets.get( it.next() );
            if ( test.getProbeToGeneMap().keySet().equals( activeProbes ) ) {
                geneData = test;
                return false;
            }
        }
        return true;
    }
}