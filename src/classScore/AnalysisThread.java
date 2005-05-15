package classScore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import baseCode.io.reader.DoubleMatrixReader;
import baseCode.util.CancellationException;
import baseCode.util.StatusViewer;
import classScore.data.GeneScoreReader;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author not attributable
 * @version $Id$
 */

public class AnalysisThread extends Thread {
    protected static final Log log = LogFactory.getLog( AnalysisThread.class );
    private GeneAnnotations geneData = null;
    private Map geneDataSets;
    private Map geneScoreSets;
    private GONames goData;
    private volatile GeneSetPvalRun latestResults;
    private String loadFile;
    private StatusViewer messenger;
    private int numRuns = 0;
    private Settings oldSettings = null;
    private Map rawDataSets;
    private volatile Method runningMethod;
    private Settings settings;
    private volatile boolean stop = false;

    /**
     * @return
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * @param stop
     */
    public void setStop( boolean stop ) {
        this.stop = stop;
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
    public AnalysisThread( Settings settings, final StatusViewer messenger, GONames goData, Map geneDataSets,
            Map rawDataSets, Map geneScoreSets ) throws IllegalStateException {
        this.settings = settings;
        this.messenger = messenger;
        this.goData = goData;
        this.rawDataSets = rawDataSets;
        this.geneScoreSets = geneScoreSets;
        this.geneDataSets = geneDataSets;

        this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer( "original".hashCode() ) ); // this is the
        // default
        // geneData

        try {
            this.runningMethod = AnalysisThread.class.getMethod( "doAnalysis", new Class[] {} );
            this.setName( "Analysis Thread" );
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
     * @param loadFile
     */
    public AnalysisThread( Settings settings, final StatusViewer messenger, GONames goData, Map geneDataSets,
            Map rawDataSets, Map geneScoreSets, String loadFile ) {
        this.settings = settings;
        this.messenger = messenger;
        this.goData = goData;
        this.loadFile = loadFile;
        this.rawDataSets = rawDataSets;
        this.geneScoreSets = geneScoreSets;
        this.geneDataSets = geneDataSets;

        // this is the
        // default
        // geneData
        this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer( "original".hashCode() ) );

        try {
            this.runningMethod = AnalysisThread.class.getMethod( "loadAnalysis", new Class[] {} );
            this.setName( "Loading analysis thread" );
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
    public synchronized GeneSetPvalRun doAnalysis() throws IOException {
        return doAnalysis( null );
    }

    public synchronized GeneSetPvalRun getLatestResults() {
        log.debug( "Status: " + latestResults );
        while ( !stop && this.latestResults == null ) {
            log.debug( "Still waiting for results..." );
            try {
                wait( 100 );
            } catch ( InterruptedException e ) {
            }
        }
        notifyAll();
        GeneSetPvalRun lastResults = latestResults;
        this.latestResults = null;
        return lastResults;
    }

    /**
     * @return
     * @throws IOException
     */
    public synchronized GeneSetPvalRun loadAnalysis() throws IOException {
        ResultsFileReader rfr = new ResultsFileReader( loadFile, messenger );
        Map results = rfr.getResults();
        return doAnalysis( results );
    }

    public void run() {
        try {
            log.debug( "Invoking runner" );
            assert runningMethod != null : "No running method assigned";
            log.debug( "Running method is " + runningMethod.getName() );
            GeneSetPvalRun results = ( GeneSetPvalRun ) runningMethod.invoke( this, null );
            log.debug( "Runner returned" );
            this.setLatestResults( results );
        } catch ( InvocationTargetException e ) {
            if ( !( e.getCause() instanceof CancellationException ) ) {
                showError( e.getCause() );
            } else {
                log.debug( "Cancelled" );
            }
            messenger.setStatus( "Ready" );
            return;
        } catch ( Exception e ) {
            stop = true;
            showError( e );
        }
    }

    /**
     * @return
     * @throws IOException
     */
    private synchronized GeneScoreReader addGeneScores() throws IOException {
        GeneScoreReader geneScores = null;
        if ( !geneScoreSettingsDirty() && geneScoreSets.containsKey( settings.getScoreFile() ) ) {
            messenger.setStatus( "Gene Scores are in memory" );
            geneScores = ( GeneScoreReader ) geneScoreSets.get( settings.getScoreFile() );
        } else {
            messenger.setStatus( "Reading gene scores from file " + settings.getScoreFile() );
            geneScores = new GeneScoreReader( settings.getScoreFile(), settings, messenger, geneData
                    .getGeneToProbeList(), geneData.getProbeToGeneMap() );
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
    private synchronized DenseDoubleMatrix2DNamed addRawData() throws IOException {
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
     * @return
     * @return
     * @param results
     * @throws IOException
     */
    private synchronized GeneSetPvalRun doAnalysis( Map results ) throws IOException {
        log.debug( "Entering doAnalysis" );
        DenseDoubleMatrix2DNamed rawData = null;
        if ( settings.getAnalysisMethod() == Settings.CORR ) {
            rawData = addRawData();
        }
        GeneScoreReader geneScores = addGeneScores();
        if ( this.stop ) return null;

        Set activeProbes = getActiveProbes( rawData, geneScores );
        if ( activeProbes == null || Thread.currentThread().isInterrupted() ) return latestResults;

        boolean needToMakeNewGeneData = needNewGeneData( activeProbes );
        GeneAnnotations useTheseAnnots = geneData;
        if ( needToMakeNewGeneData ) {
            useTheseAnnots = new GeneAnnotations( geneData, activeProbes ); // / don't redo the parent adding.
            // todo I don't like this
            // way of keeping track of
            // the different geneData
            // sets.
            geneDataSets.put( new Integer( useTheseAnnots.hashCode() ), useTheseAnnots );
        }

        if ( this.stop ) return null;

        /* do work */
        messenger.setStatus( "Starting analysis..." );
        numRuns++;
        GeneSetPvalRun newResults = null;
        if ( results != null ) { // read from a file.
            newResults = new GeneSetPvalRun( activeProbes, settings, useTheseAnnots, rawData, goData, geneScores,
                    messenger, results, new Integer( numRuns ).toString() );
        } else {
            newResults = new GeneSetPvalRun( activeProbes, settings, useTheseAnnots, rawData, goData, geneScores,
                    messenger, new Integer( numRuns ).toString() );
        }

        if ( this.stop ) return null;
        settings.writePrefs();
        oldSettings = settings;
        return newResults;
    }

    // see if we have to read the gene scores or if we can just use the old ones
    private synchronized boolean geneScoreSettingsDirty() {
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
    private synchronized Set getActiveProbes( DenseDoubleMatrix2DNamed rawData, GeneScoreReader geneScores ) {
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
    private synchronized boolean needNewGeneData( Set activeProbes ) {
        log.debug( "Entering needNewGeneData" );
        for ( Iterator it = geneDataSets.keySet().iterator(); it.hasNext(); ) {
            GeneAnnotations test = ( GeneAnnotations ) geneDataSets.get( it.next() );
            if ( test.getProbeToGeneMap().keySet().equals( activeProbes ) ) {
                geneData = test;
                return false;
            }
        }
        return true;
    }

    /**
     * @param newResults
     */
    private synchronized void setLatestResults( GeneSetPvalRun newResults ) {
        log.debug( "Status: " + latestResults );
        while ( !stop && this.latestResults != null ) {
            log.debug( "Still waiting for last set of results to be cleared" );
            try {
                wait( 100 );
            } catch ( InterruptedException e ) {
            }
        }
        this.latestResults = newResults;
        notifyAll();
    }

    /**
     * @param e
     */
    private void showError( Throwable e ) {
        log.error( e, e );
        messenger.setError( e );
    }

}