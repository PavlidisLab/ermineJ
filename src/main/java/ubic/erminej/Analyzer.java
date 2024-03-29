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
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusViewer;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;

/**
 * <p>
 * Analyzer class.
 * </p>
 *
 * @author pavlidis
 * @version $Id$
 */
public class Analyzer extends Thread {

    /** Constant <code>log</code> */
    protected static final Log log = LogFactory.getLog( Analyzer.class );
    private volatile Collection<GeneSetPvalRun> latestResults = new HashSet<>();
    private String loadFile;
    private StatusViewer messenger;
    private GeneAnnotations geneAnnots;
    private volatile Method runningMethod;
    private SettingsHolder settings;

    private volatile AtomicBoolean finished = new AtomicBoolean( false );
    private AtomicBoolean wasError = new AtomicBoolean( false );
    private AtomicBoolean finishedNormally = new AtomicBoolean( false );

    /**
     * <p>
     * Constructor for Analyzer.
     * </p>
     *
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param messenger a {@link ubic.basecode.util.StatusViewer} object.
     * @param geneAnnots a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public Analyzer( SettingsHolder settings, final StatusViewer messenger, GeneAnnotations geneAnnots ) {
        this.settings = settings;
        this.messenger = messenger;
        this.geneAnnots = geneAnnots;

        try {
            this.runningMethod = Analyzer.class.getMethod( "doAnalysis", new Class[] {} );
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
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param messenger a {@link ubic.basecode.util.StatusViewer} object.
     * @param geneAnnots a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param loadFile a {@link java.lang.String} object.
     */
    public Analyzer( SettingsHolder settings, final StatusViewer messenger, GeneAnnotations geneAnnots,
            String loadFile ) {
        this.settings = settings;
        this.messenger = messenger;
        this.loadFile = loadFile;
        this.geneAnnots = geneAnnots;

        if ( StringUtils.isBlank( loadFile ) ) {
            throw new IllegalArgumentException( "The result file must be provided." );
        }

        try {
            this.runningMethod = Analyzer.class.getMethod( "loadAnalysis", new Class[] {} );
            this.setName( "Loading analysis thread" );
        } catch ( SecurityException e ) {
            e.printStackTrace();
        } catch ( NoSuchMethodException e ) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * doAnalysis.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public synchronized Collection<GeneSetPvalRun> doAnalysis() {

        Collection<GeneSetPvalRun> answer = new HashSet<>();

        log.debug( "Entering doAnalysis in " + Thread.currentThread().getName() );

        StopWatch timer = new StopWatch();
        timer.start();

        if ( this.finished.get() ) {
            log.warn( "Bailed early." );
            return null;
        }

        if ( messenger != null ) messenger.showProgress( "Starting analysis..." );

        answer.add( new GeneSetPvalRun( settings, geneAnnots, messenger ) );

        log.info( "Analysis: " + timer.getTime() + "ms" );

        return answer;
    }

    /**
     * Blocks until results are returned.
     *
     * @throws java.lang.IllegalStateException if any.
     * @return a {@link java.util.Collection} object.
     */
    public synchronized Collection<GeneSetPvalRun> getLatestResults() throws IllegalStateException {
        log.debug( "Status: " + latestResults );
        while ( !finished.get() && this.latestResults.isEmpty() ) {
            try {
                wait( 100 );
            } catch ( InterruptedException e ) {
            }
        }
        notifyAll();
        Collection<GeneSetPvalRun> lastResults = new HashSet<>( latestResults );
        this.latestResults.clear();
        return lastResults;
    }

    /**
     * <p>
     * isFinishedNormally.
     * </p>
     *
     * @return Returns the finishedNormally.
     */
    public boolean isFinishedNormally() {
        return this.finishedNormally.get();
    }

    /**
     * <p>
     * isStop.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isStop() {
        if ( !finished.get() ) this.finishedNormally.set( false );
        return finished.get();

    }

    /**
     * <p>
     * isWasError.
     * </p>
     *
     * @return Returns the wasError.
     */
    public boolean isWasError() {
        return this.wasError.get();
    }

    /**
     * Load from a file, which can contain more than one set.
     *
     * @throws IOException if any.
     * @return a {@link java.util.Collection} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized Collection<GeneSetPvalRun> loadAnalysis() throws Exception {
        return ResultsFileReader.load( geneAnnots, loadFile, messenger );
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            this.wasError.set( false );
            assert runningMethod != null : "No running method assigned";
            log.debug( "Invoking runner in " + Thread.currentThread().getName() );
            log.debug( "Running method is " + runningMethod.getName() );
            Collection<GeneSetPvalRun> results = ( Collection<GeneSetPvalRun> ) runningMethod.invoke( this,
                    ( Object[] ) null );
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
            finished.set( true );
        }
    }

    /**
     * <p>
     * stopRunning.
     * </p>
     *
     * @param s a boolean.
     */
    public void stopRunning( boolean s ) {
        this.finished.set( s );
        log.debug( "Stop set to : " + s );
    }

    /**
     * @param newResults
     */
    private synchronized void setLatestResults( Collection<GeneSetPvalRun> newResults ) {
        while ( !finished.get() && !this.latestResults.isEmpty() ) {
            log.debug( "Still waiting for last set of results to be cleared" );
            try {
                wait( 1000 );
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
