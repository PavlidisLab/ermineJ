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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.FastRowAccessDoubleMatrix;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.basecode.util.CancellationException;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Probe;

/**
 * @author pavlidis
 * @version $Id$
 */
public class AnalysisThread extends Thread {
    protected static final Log log = LogFactory.getLog( AnalysisThread.class );
    private Map<String, GeneScores> geneScoreSets;
    private volatile GeneSetPvalRun latestResults;
    private String loadFile;
    private StatusViewer messenger;
    private GeneAnnotations geneAnnots;
    private SettingsHolder oldSettings = null;
    private Map<String, DoubleMatrix<Probe, String>> rawDataSets;
    private volatile Method runningMethod;
    private SettingsHolder settings;
    private volatile boolean stop = false;
    private boolean wasError = false;
    private boolean finishedNormally = false;

    /**
     * @param csframe
     * @param settings
     * @param messenger
     * @param geneAnnots
     * @param rawDataSets
     * @param geneScoreSets
     */
    public AnalysisThread( SettingsHolder settings, final StatusViewer messenger, GeneAnnotations geneAnnots,
            Map<String, DoubleMatrix<Probe, String>> rawDataSets, Map<String, GeneScores> geneScoreSets ) {
        this.settings = settings;
        this.messenger = messenger;
        this.rawDataSets = rawDataSets;
        this.geneScoreSets = geneScoreSets;
        this.geneAnnots = geneAnnots;

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
     * @param geneAnnots
     * @param rawDataSets
     * @param geneScoreSets
     * @param loadFile
     */
    public AnalysisThread( SettingsHolder settings, final StatusViewer messenger, GeneAnnotations geneAnnots,
            Map<String, DoubleMatrix<Probe, String>> rawDataSets, Map<String, GeneScores> geneScoreSets, String loadFile ) {
        this.settings = settings;
        this.messenger = messenger;
        this.loadFile = loadFile;
        this.rawDataSets = rawDataSets;
        this.geneScoreSets = geneScoreSets;
        this.geneAnnots = geneAnnots;

        if ( StringUtils.isBlank( loadFile ) ) {
            throw new IllegalArgumentException( "The result file must be provided." );
        }

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
     */
    public synchronized GeneSetPvalRun doAnalysis() {
        try {
            return doAnalysis( null );
        } catch ( Exception e ) {
            this.messenger.showError( e );
            return null;
        }
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
     * @return Returns the finishedNormally.
     */
    public boolean isFinishedNormally() {
        return this.finishedNormally;
    }

    /**
     * @return
     */
    public boolean isStop() {
        if ( stop == false ) this.finishedNormally = false;
        return stop;

    }

    /**
     * @return Returns the wasError.
     */
    public boolean isWasError() {
        return this.wasError;
    }

    /**
     * Load from a file.
     * 
     * @return
     * @throws IOException
     */
    public synchronized GeneSetPvalRun loadAnalysis() throws IOException {
        ResultsFileReader rfr = new ResultsFileReader( geneAnnots, loadFile, messenger );
        Map<GeneSetTerm, GeneSetResult> results = rfr.getResults();
        return doAnalysis( results );
    }

    @Override
    public void run() {
        try {
            this.wasError = false;
            assert runningMethod != null : "No running method assigned";
            log.debug( "Invoking runner in " + Thread.currentThread().getName() );
            log.debug( "Running method is " + runningMethod.getName() );
            GeneSetPvalRun results = ( GeneSetPvalRun ) runningMethod.invoke( this, ( Object[] ) null );
            log.debug( "Runner returned" );

            if ( results == null ) {
                stop = true;
                this.finishedNormally = false;
            } else {
                this.setLatestResults( results );
                this.finishedNormally = true;
            }
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
     * @param stop
     */
    public void stopRunning( boolean s ) {
        this.stop = s;
        log.debug( "Stop set to : " + s );
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
            geneScores = geneScoreSets.get( settings.getScoreFile() );
        } else {
            if ( messenger != null )
                messenger.showStatus( "Reading gene scores from file " + settings.getScoreFile() );
            geneScores = new GeneScores( settings.getScoreFile(), settings, messenger, geneAnnots );
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
    private synchronized DoubleMatrix<Probe, String> addRawData() throws IOException {
        DoubleMatrix<Probe, String> rawData;
        if ( rawDataSets.containsKey( settings.getRawDataFileName() ) ) {
            if ( messenger != null ) messenger.showStatus( "Raw data are in memory" );
            rawData = rawDataSets.get( settings.getRawDataFileName() );
        } else {
            if ( messenger != null )
                messenger.showStatus( "Reading raw data from file " + settings.getRawDataFileName() );
            DoubleMatrixReader r = new DoubleMatrixReader();

            DoubleMatrix<String, String> omatrix = r.read( settings.getRawDataFileName() );

            rawData = new FastRowAccessDoubleMatrix<Probe, String>( omatrix.asArray() );
            rawData.setColumnNames( omatrix.getColNames() );
            for ( int i = 0; i < omatrix.rows(); i++ ) {
                String n = omatrix.getRowName( i );
                Probe p = geneAnnots.findProbe( n );
                if ( p == null ) {
                    throw new IllegalArgumentException( "Some probes in the data don't match those in the annotations" );
                }
                rawData.setRowName( p, i );
            }

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
    private synchronized GeneSetPvalRun doAnalysis( Map<GeneSetTerm, GeneSetResult> results ) throws IOException {
        log.debug( "Entering doAnalysis in " + Thread.currentThread().getName() );

        StopWatch timer = new StopWatch();
        timer.start();

        DoubleMatrix<Probe, String> rawData = null;
        if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) ) {
            rawData = addRawData();
        }
        GeneScores geneScores = addGeneScores();
        if ( this.stop ) return null;

        if ( this.stop ) return null;

        /* do work */
        if ( messenger != null ) messenger.showStatus( "Starting analysis..." );
        GeneSetPvalRun newResults = null;
        if ( results != null ) { // read from a file.
            newResults = new GeneSetPvalRun( settings, geneAnnots, rawData, geneScores, messenger, results, "LoadedRun" );
        } else {
            newResults = new GeneSetPvalRun( settings, geneAnnots, rawData, geneScores, messenger, "NewRun" );
        }

        timer.stop();
        if ( messenger != null )
            messenger.showStatus( String.format( "%d seconds elapsed", Math.round( timer.getTime() / 1000 ) ) );

        if ( this.stop ) return null;
        // settings.writePrefs();
        oldSettings = settings;
        return newResults;
    }

    // see if we have to read the gene scores or if we can just use the old ones
    private synchronized boolean geneScoreSettingsDirty() {
        if ( oldSettings == null ) return true;
        return settings.getDoLog() != oldSettings.getDoLog() || settings.getScoreCol() != oldSettings.getScoreCol();
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
    private synchronized void showError( Throwable e ) {
        log.error( e, e );
        wasError = true;
        if ( messenger != null ) messenger.showError( e );
    }

}