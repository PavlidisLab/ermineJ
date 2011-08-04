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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.FastRowAccessDoubleMatrix;
import ubic.basecode.io.reader.DoubleMatrixReader;
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
    private volatile GeneSetPvalRun latestResults;
    private String loadFile;
    private StatusViewer messenger;
    private GeneAnnotations geneAnnots;
    private volatile Method runningMethod;
    private SettingsHolder settings;

    private volatile AtomicBoolean stop = new AtomicBoolean( false );
    private AtomicBoolean wasError = new AtomicBoolean( false );
    private AtomicBoolean finishedNormally = new AtomicBoolean( false );

    /**
     * @param csframe
     * @param settings
     * @param messenger
     * @param geneAnnots
     * @param rawDataSets
     * @param geneScoreSets
     */
    public AnalysisThread( SettingsHolder settings, final StatusViewer messenger, GeneAnnotations geneAnnots ) {
        this.settings = settings;
        this.messenger = messenger;
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
     * For loading an analysis from a file.
     * 
     * @param csframe
     * @param settings
     * @param messenger
     * @param geneAnnots
     * @param rawDataSets
     * @param geneScoreSets
     * @param loadFile
     */
    public AnalysisThread( SettingsHolder settings, final StatusViewer messenger, GeneAnnotations geneAnnots,
            String loadFile ) {
        this.settings = settings;
        this.messenger = messenger;
        this.loadFile = loadFile;
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
        while ( !stop.get() && this.latestResults == null ) {
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
        return this.finishedNormally.get();
    }

    /**
     * @return
     */
    public boolean isStop() {
        if ( !stop.get() ) this.finishedNormally.set( false );
        return stop.get();

    }

    /**
     * @return Returns the wasError.
     */
    public boolean isWasError() {
        return this.wasError.get();
    }

    /**
     * Load from a file.
     * 
     * @return
     * @throws IOException
     */
    public synchronized GeneSetPvalRun loadAnalysis() throws Exception {
        ResultsFileReader rfr = new ResultsFileReader( geneAnnots, loadFile, messenger );
        Map<GeneSetTerm, GeneSetResult> results = rfr.getResults();
        return doAnalysis( results );
    }

    @Override
    public void run() {
        try {
            this.wasError.set( false );
            assert runningMethod != null : "No running method assigned";
            log.debug( "Invoking runner in " + Thread.currentThread().getName() );
            log.debug( "Running method is " + runningMethod.getName() );
            GeneSetPvalRun results = ( GeneSetPvalRun ) runningMethod.invoke( this, ( Object[] ) null );
            log.debug( "Runner returned" );

            if ( results == null ) {
                this.finishedNormally.set( false );
            } else {
                this.setLatestResults( results );
                this.finishedNormally.set( true );
            }
        } catch ( InvocationTargetException e ) {
            if ( !( e.getCause() instanceof CancellationException ) ) {
                showError( e.getCause() );
            } else {
                log.debug( "Cancelled" );
            }
            if ( messenger != null ) messenger.showStatus( "Ready" );
        } catch ( Exception e ) {
            showError( e );
            throw new RuntimeException( e.getCause() );
        } finally {
            stop.set( true );
        }
    }

    /**
     * @param stop
     */
    public void stopRunning( boolean s ) {
        this.stop.set( s );
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
        messenger.showStatus( "Reading gene scores from file " + StringUtils.abbreviate( settings.getScoreFile(), 50 ) );
        GeneScores geneScores = new GeneScores( settings.getScoreFile(), settings, messenger, geneAnnots );
        messenger.clear();
        return geneScores;
    }

    /**
     * @return
     * @throws IOException
     */
    private synchronized DoubleMatrix<Probe, String> addRawData() throws IOException {
        messenger.showStatus( "Reading raw data from file "
                + StringUtils.abbreviate( settings.getRawDataFileName(), 50 ) );
        DoubleMatrix<Probe, String> rawData = readDataMatrixForAnalysis();
        messenger.clear();
        return rawData;
    }

    /**
     * @return
     * @return
     * @param results
     * @throws IOException
     */
    private synchronized GeneSetPvalRun doAnalysis( Map<GeneSetTerm, GeneSetResult> results ) throws Exception {
        try {
            log.debug( "Entering doAnalysis in " + Thread.currentThread().getName() );

            StopWatch timer = new StopWatch();
            timer.start();

            DoubleMatrix<Probe, String> rawData = null;
            if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) ) {
                rawData = addRawData();
            }
            GeneScores geneScores = addGeneScores();
            if ( this.stop.get() ) return null;

            /* do work */
            if ( messenger != null ) messenger.showStatus( "Starting analysis..." );
            GeneSetPvalRun newResults = null;
            if ( results != null ) { // read from a file.
                newResults = new GeneSetPvalRun( settings, geneAnnots, rawData, geneScores, messenger, results,
                        "LoadedRun" );
            } else {
                newResults = new GeneSetPvalRun( settings, geneAnnots, rawData, geneScores, messenger, "NewRun" );
            }

            timer.stop();
            if ( messenger != null )
                messenger.showStatus( String.format( "%d seconds elapsed", Math.round( timer.getTime() / 1000 ) ) );

            if ( this.stop.get() ) return null;

            return newResults;
        } catch ( Exception e ) {
            throw e;
        }
    }

    /**
     * @return
     * @throws IOException
     */
    private DoubleMatrix<Probe, String> readDataMatrixForAnalysis() throws IOException {
        DoubleMatrix<Probe, String> rawData;
        DoubleMatrixReader r = new DoubleMatrixReader();

        Collection<String> usableRowNames = new HashSet<String>();
        for ( Probe p : geneAnnots.getProbes() ) {
            usableRowNames.add( p.getName() );
        }

        DoubleMatrix<String, String> omatrix = r.read( settings.getRawDataFileName(), usableRowNames, settings
                .getDataCol() );

        if ( omatrix.rows() == 0 ) {
            throw new IllegalArgumentException( "No rows were read from the file for the probes in the annotations." );
        }

        rawData = new FastRowAccessDoubleMatrix<Probe, String>( omatrix.asArray() );
        rawData.setColumnNames( omatrix.getColNames() );
        for ( int i = 0; i < omatrix.rows(); i++ ) {
            String n = omatrix.getRowName( i );
            Probe p = geneAnnots.findProbe( n );
            assert p != null;
            rawData.setRowName( p, i );
        }
        return rawData;
    }

    /**
     * @param newResults
     */
    private synchronized void setLatestResults( GeneSetPvalRun newResults ) {
        log.debug( "Status: " + latestResults );
        while ( !stop.get() && this.latestResults != null ) {
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
        wasError.set( true );
        if ( messenger != null ) messenger.showError( e );
    }

}