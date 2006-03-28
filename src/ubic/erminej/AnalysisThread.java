/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.CancellationException;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.dataStructure.matrix.DoubleMatrixNamed;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.erminej.data.GeneScores;

/**
 * @author pavlidis
 * @version $Id$
 */
public class AnalysisThread extends Thread {
    protected static final Log log = LogFactory.getLog( AnalysisThread.class );
    private GeneAnnotations geneData = null;
    private Map geneScoreSets;
    private GONames goData;
    private volatile GeneSetPvalRun latestResults;
    private String loadFile;
    private StatusViewer messenger;
    // private int numRuns = 0;
    private Settings oldSettings = null;
    private Map rawDataSets;
    private volatile Method runningMethod;
    private Settings settings;
    private volatile boolean stop = false;
    private boolean wasError = false;
    private boolean finishedNormally;

    /**
     * @return Returns the wasError.
     */
    public boolean isWasError() {
        return this.wasError;
    }

    /**
     * @return
     */
    public boolean isStop() {
        if ( stop == false ) this.finishedNormally = false;
        return stop;

    }

    /**
     * @param stop
     */
    public void stopRunning( boolean s ) {
        this.stop = s;
        log.debug( "Stop set to : " + s );
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
        if ( geneDataSets == null ) throw new IllegalArgumentException();
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

    public synchronized GeneSetPvalRun getLatestResults() throws IllegalStateException {
        log.debug( "Status: " + latestResults );
        while ( !stop && this.latestResults == null ) {
            try {
                wait( 100 );
            } catch ( InterruptedException e ) {
            }
        }
        notifyAll();
        GeneSetPvalRun lastResults = latestResults;
        this.latestResults = null;
        if ( lastResults != null ) log.debug( "Got results!" );
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
            this.wasError = false;
            assert runningMethod != null : "No running method assigned";
            log.debug( "Invoking runner in " + Thread.currentThread().getName() );
            log.debug( "Running method is " + runningMethod.getName() );
            GeneSetPvalRun results = ( GeneSetPvalRun ) runningMethod.invoke( this, null );
            log.debug( "Runner returned" );
            this.setLatestResults( results );
            this.finishedNormally = true;
        } catch ( InvocationTargetException e ) {
            if ( !( e.getCause() instanceof CancellationException ) ) {
                showError( e.getCause() );
            } else {
                log.debug( "Cancelled" );
            }
            if ( messenger != null ) messenger.showStatus( "Ready" );
        } catch ( Exception e ) {
            stop = true;
            showError( e );
            throw new RuntimeException( e.getCause() );
        }
    }

    /**
     * @return
     * @throws IOException
     */
    private synchronized GeneScores addGeneScores() throws IOException {

        if ( StringUtils.isBlank( settings.getScoreFile() ) ) {
            return null;
        }

        GeneScores geneScores = null;
        if ( !geneScoreSettingsDirty() && geneScoreSets.containsKey( settings.getScoreFile() ) ) {
            if ( messenger != null ) messenger.showStatus( "Gene Scores are in memory" );
            geneScores = ( GeneScores ) geneScoreSets.get( settings.getScoreFile() );
        } else {
            if ( messenger != null )
                messenger.showStatus( "Reading gene scores from file " + settings.getScoreFile() );
            geneScores = new GeneScores( settings.getScoreFile(), settings, messenger, geneData );
            geneScoreSets.put( settings.getScoreFile(), geneScores );
        }
        if ( !settings.getScoreFile().equals( "" ) && geneScores == null ) {
            if ( messenger != null ) messenger.showStatus( "Didn't get geneScores" );
        }
        return geneScores;
    }

    /**
     * @return
     * @throws IOException
     */
    private synchronized DoubleMatrixNamed addRawData() throws IOException {
        DoubleMatrixNamed rawData;
        if ( rawDataSets.containsKey( settings.getRawDataFileName() ) ) {
            if ( messenger != null ) messenger.showStatus( "Raw data are in memory" );
            rawData = ( DoubleMatrixNamed ) rawDataSets.get( settings.getRawDataFileName() );
        } else {
            if ( messenger != null )
                messenger.showStatus( "Reading raw data from file " + settings.getRawDataFileName() );
            DoubleMatrixReader r = new DoubleMatrixReader();
            rawData = ( DoubleMatrixNamed ) r.read( settings.getRawDataFileName() );
            rawDataSets.put( settings.getRawDataFileName(), rawData );
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
        log.debug( "Entering doAnalysis in " + Thread.currentThread().getName() );

        StopWatch timer = new StopWatch();
        timer.start();

        DoubleMatrixNamed rawData = null;
        if ( settings.getClassScoreMethod() == Settings.CORR ) {
            rawData = addRawData();
        }
        GeneScores geneScores = addGeneScores();
        if ( this.stop ) return null;

        Set activeProbes = getActiveProbes( rawData, geneScores );
        if ( activeProbes == null || Thread.currentThread().isInterrupted() ) return latestResults;

        // boolean needToMakeNewGeneData = needNewGeneData( activeProbes );
        GeneAnnotations useTheseAnnots = geneData;
        // if ( needToMakeNewGeneData ) {
        // log.debug( "Making new gene data from existing" );
        // useTheseAnnots = new GeneAnnotations( geneData, activeProbes ); // / don't redo the parent adding.
        // /*
        // * todo I don't like this way of keeping track of the different geneData sets....though it works.
        // */
        // geneDataSets.put( new Integer( useTheseAnnots.hashCode() ), useTheseAnnots );
        // } else {
        // log.debug( "No need to make new gene data, reusing existing." );
        // }

        if ( this.stop ) return null;

        /* do work */
        if ( messenger != null ) messenger.showStatus( "Starting analysis..." );
        GeneSetPvalRun newResults = null;
        if ( results != null ) { // read from a file.
            newResults = new GeneSetPvalRun( activeProbes, settings, useTheseAnnots, rawData, goData, geneScores,
                    messenger, results, "LoadedRun" );
        } else {
            newResults = new GeneSetPvalRun( activeProbes, settings, useTheseAnnots, rawData, goData, geneScores,
                    messenger, "NewRun" );
        }

        timer.stop();
        if ( messenger != null ) messenger.showStatus( timer.getTime() / 1000 + " seconds elapsed" );

        if ( this.stop ) return null;
        // settings.writePrefs();
        oldSettings = settings;
        return newResults;
    }

    // see if we have to read the gene scores or if we can just use the old ones
    private synchronized boolean geneScoreSettingsDirty() {
        if ( oldSettings == null ) return true;
        return ( settings.getDoLog() != oldSettings.getDoLog() )
                || ( settings.getScoreCol() != oldSettings.getScoreCol() );
    }

    /**
     * @param rawData
     * @param geneScores
     * @param activeProbes
     * @return
     */
    private synchronized Set getActiveProbes( DoubleMatrixNamed rawData, GeneScores geneScores ) {
        Set activeProbes = null;
        if ( rawData != null && geneScores != null ) { // favor the geneScores list.
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        } else if ( rawData == null && geneScores != null ) {
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        } else if ( rawData != null && geneScores == null ) {
            activeProbes = new HashSet( rawData.getRowNames() );
        }
        log.debug( activeProbes.size() + " active probes" );
        return activeProbes;
    }

    // /**
    // * @param activeProbes
    // * @param needToMakeNewGeneData
    // * @return
    // */
    // private synchronized boolean needNewGeneData( Set activeProbes ) {
    // log.debug( "Entering needNewGeneData" );
    // for ( Iterator it = geneDataSets.keySet().iterator(); it.hasNext(); ) {
    // GeneAnnotations test = ( GeneAnnotations ) geneDataSets.get( it.next() );
    // if ( test.getProbeToGeneMap().keySet().equals( activeProbes ) ) {
    // geneData = test;
    // return false;
    // }
    // }
    // return true;
    // }

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
    private synchronized void showError( Throwable e ) {
        log.error( e, e );
        wasError = true;
        if ( messenger != null ) messenger.showError( e );
    }

    /**
     * @return Returns the finishedNormally.
     */
    public boolean isFinishedNormally() {
        return this.finishedNormally;
    }

}