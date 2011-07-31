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
package ubic.erminej.analysis;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;
import ubic.erminej.data.Multifunctionality;
import ubic.erminej.data.Probe;

/**
 * Class that does all the work in doing gene set scoring. Holds the results as well.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetPvalRun {

    private GeneAnnotations geneData; // ones used in the analysis -- this will be immutable, should only be used for
    // this.

    // also pruned down.
    private GeneScores geneScores;

    private Histogram hist;
    private Map<GeneSetTerm, GeneSetResult> results = null;
    private List<GeneSetTerm> sortedclasses = null; // this holds the results.
    private NumberFormat nf = NumberFormat.getInstance();
    private SettingsHolder settings;

    private StatusViewer messenger = new StatusStderr();

    private double multifunctionalityCorrelation = -1;

    private long randomSeed = -1;

    private String name; // name of this run.

    // pruned down
    private DoubleMatrix<Probe, String> rawData;

    /**
     * Restrict to genes that have annotations and which are included in the data.
     * 
     * @param activeProbes
     * @param geneDataSets
     * @return
     */
    public synchronized GeneAnnotations getPrunedAnnotations( Collection<Probe> activeProbes, GeneAnnotations original ) {
        return new GeneAnnotations( original, activeProbes, true );
    }

    /**
     * Get probes that are present in the data.
     * 
     * @param rawData
     * @param scores
     * @return
     */
    private synchronized Set<Probe> getActiveProbes() {

        Set<Probe> activeProbes = null;
        if ( geneScores != null ) { // favor the geneScores list.
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        } else if ( rawData != null ) {
            activeProbes = new HashSet<Probe>( rawData.getRowNames() );
        } else {
            throw new IllegalStateException( "No active probes" );
        }
        return activeProbes;
    }

    /**
     * Do a new analysis.
     * 
     * @param settings
     * @param originalAnnots - original!!! Will be pruned as necessary.
     * @param rawData
     * @param geneScores
     * @param messenger
     * @param name Name of the run
     */
    public GeneSetPvalRun( SettingsHolder settings, GeneAnnotations originalAnnots,
            DoubleMatrix<Probe, String> rawData, GeneScores geneScores, StatusViewer messenger, String name ) {
        this.settings = settings;

        this.geneScores = geneScores;
        this.rawData = rawData;
        this.name = name;
        if ( messenger != null ) this.messenger = messenger;
        this.settings = settings;

        nf.setMaximumFractionDigits( 8 );
        results = new LinkedHashMap<GeneSetTerm, GeneSetResult>();

        Set<Probe> activeProbes = getActiveProbes();
        this.geneData = getPrunedAnnotations( activeProbes, originalAnnots );

        pruneData();

        runAnalysis();

    }

    /**
     * Use this when we are loading in existing results from a file.
     * 
     * @param settings
     * @param geneData
     * @param rawData
     * @param geneScores
     * @param messenger
     * @param results
     * @param name Name of the run
     */
    public GeneSetPvalRun( SettingsHolder settings, GeneAnnotations geneData, DoubleMatrix<Probe, String> rawData,
            GeneScores geneScores, StatusViewer messenger, Map<GeneSetTerm, GeneSetResult> results, String name ) {

        this.geneScores = geneScores;
        this.results = results;
        this.rawData = rawData;
        this.settings = settings;
        if ( messenger != null ) this.messenger = messenger;
        this.name = name;

        Set<Probe> activeProbes = getActiveProbes();
        this.geneData = getPrunedAnnotations( activeProbes, geneData );

        assert this.geneData.isReadOnly();

        setMultifunctionalities();
        sortResults();
        for ( int i = 0; i < sortedclasses.size(); i++ ) {
            results.get( sortedclasses.get( i ) ).setRank( i + 1 );
        }
    }

    /**
     * Do a new analysis, starting from the bare essentials (correlation method not available)
     */
    public GeneSetPvalRun( SettingsHolder settings, GeneAnnotations geneData, GeneScores geneScores ) {
        this.settings = settings;

        this.geneScores = geneScores;
        this.geneData = geneData;
        this.rawData = null;
        nf.setMaximumFractionDigits( 8 );
        results = new HashMap<GeneSetTerm, GeneSetResult>();

        Set<Probe> activeProbes = getActiveProbes();
        this.geneData = getPrunedAnnotations( activeProbes, geneData );

        pruneData();

        runAnalysis();
    }

    /**
     * 
     */
    private void pruneData() {
        if ( this.rawData != null ) {
            this.rawData = this.rawData.subsetRows( this.geneData.getProbes() );
        }
        if ( this.geneScores != null ) {
            this.geneScores = new GeneScores( this.geneScores, this.geneData.getProbes() );
        }

    }

    public GeneAnnotations getGeneData() {
        return geneData;
    }

    /**
     * @return Returns the geneScores.
     */
    public GeneScores getGeneScores() {
        return this.geneScores;
    }

    public Histogram getHist() {
        return hist;
    }

    public double getMultifunctionalityCorrelation() {
        return multifunctionalityCorrelation;
    }

    public String getName() {
        return name;
    }

    /**
     * @return Map the results
     */
    public Map<GeneSetTerm, GeneSetResult> getResults() {
        return results;
    }

    /**
     * @return the settings that were used during the analysis, which may be different than the current application-wide
     *         settings.
     */
    public SettingsHolder getSettings() {
        return settings;
    }

    /**
     * @return Map the results
     */
    public List<GeneSetTerm> getSortedClasses() {
        return sortedclasses;
    }

    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Set a specific random seed for use in all resampling-based runs. If not set, the seed is chosen by the software.
     * <p>
     * 
     * @param randomSeed A positive value. Negative values are ignored.
     */
    public void setRandomSeed( long randomSeed ) {
        if ( randomSeed < 0 ) return;
        this.randomSeed = randomSeed;
    }

    @Override
    public String toString() {
        return "GeneSetPvalRun [name=" + name + "]";
    }

    /* private methods */

    /**
     * @param csc
     */
    private void multipleTestCorrect( GeneSetSizesForAnalysis csc ) {
        if ( messenger != null ) messenger.showStatus( "Multiple test correction..." );
        MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedclasses, hist, geneData, csc, geneScores,
                results, messenger );
        Settings.MultiTestCorrMethod multipleTestCorrMethod = settings.getMtc();
        if ( multipleTestCorrMethod == SettingsHolder.MultiTestCorrMethod.BONFERONNI ) {
            mt.bonferroni();
        } else if ( multipleTestCorrMethod.equals( SettingsHolder.MultiTestCorrMethod.BENJAMINIHOCHBERG ) ) {
            mt.benjaminihochberg();
        } else if ( multipleTestCorrMethod.equals( SettingsHolder.MultiTestCorrMethod.WESTFALLYOUNG ) ) {
            if ( !( settings.getClassScoreMethod().equals( SettingsHolder.Method.GSR ) ) )
                throw new UnsupportedOperationException(
                        "Westfall-Young correction is not supported for this analysis method" );
            mt.westfallyoung();
        } else {
            throw new IllegalArgumentException( "Unknown multiple test correction method: " + multipleTestCorrMethod );
        }
    }

    /**
     * @param activeProbes
     * @param settings
     * @param geneData
     * @param rawData
     * @param goData
     * @param geneScores
     * @param messenger
     */
    private void runAnalysis() {
        // get the class sizes.
        GeneSetSizesForAnalysis csc = new GeneSetSizesForAnalysis( geneData, geneScores, settings );

        switch ( settings.getClassScoreMethod() ) {
            case GSR: {
                NullDistributionGenerator probePvalMapper = new ResamplingExperimentGeneSetScore( settings, geneScores );

                if ( messenger != null ) messenger.showStatus( "Starting resampling" );

                if ( randomSeed >= 0 ) {
                    probePvalMapper.setRandomSeed( randomSeed );
                }
                hist = probePvalMapper.generateNullDistribution( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                if ( messenger != null ) messenger.showStatus( "Finished resampling" );

                GeneSetPvalSeriesGenerator pvg = new GeneSetPvalSeriesGenerator( settings, geneData, hist, csc );
                if ( Thread.currentThread().isInterrupted() ) return;
                // calculate the actual class scores and correct sorting.
                results = pvg.classPvalGenerator( geneScores.getGeneToScoreMap(), geneScores.getProbeToScoreMap() );

                break;
            }
            case ORA: {

                if ( messenger != null ) messenger.showStatus( "Starting ORA analysis" );

                OraPvalGenerator pvg = new OraPvalGenerator( settings, geneScores, geneData, csc );

                int numOver = pvg.getNumGenesOverThreshold();

                if ( numOver == 0 ) {
                    if ( messenger != null ) messenger.showError( "No genes selected at that threshold!" );
                    break;
                }

                results = pvg.classPvalGenerator( geneScores.getGeneToScoreMap(), geneScores.getProbeToScoreMap(),
                        messenger );

                if ( messenger != null )
                    messenger.showStatus( "Finished with ORA computations: " + numOver
                            + " probes passed your threshold." );
                break;
            }
            case CORR: {
                if ( rawData == null )
                    throw new IllegalArgumentException( "Raw data cannot be null for Correlation analysis" );
                if ( messenger != null )
                    messenger.showStatus( "Starting correlation resampling in " + Thread.currentThread().getName() );
                NullDistributionGenerator probePvalMapper = new ResamplingCorrelationGeneSetScore( settings, rawData );

                if ( randomSeed >= 0 ) {
                    probePvalMapper.setRandomSeed( randomSeed );
                }

                hist = probePvalMapper.generateNullDistribution( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                CorrelationPvalGenerator pvg = new CorrelationPvalGenerator( settings, geneData, csc, rawData, hist );
                if ( messenger != null ) messenger.showStatus( "Finished resampling, computing for gene sets" );
                results = pvg.classPvalGenerator( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                if ( messenger != null ) messenger.showStatus( "Finished computing scores" );

                break;
            }
            case ROC: {
                RocPvalGenerator rpg = new RocPvalGenerator( settings, geneData, csc, messenger );
                if ( messenger != null ) messenger.showStatus( "Computing gene set scores" );
                results = rpg.classPvalGenerator( geneScores );

                break;
            }

            default: {
                throw new UnsupportedOperationException( "Unsupported analysis method" );
            }
        }

        if ( results.size() == 0 ) {
            return;
        }

        sortResults();
        if ( Thread.currentThread().isInterrupted() ) return;
        multipleTestCorrect( csc );

        setGeneSetRanks();

        setMultifunctionalities();

        if ( messenger != null ) messenger.showStatus( "Done!" );
    }

    /**
     * 
     */
    private void setGeneSetRanks() {
        // For table output
        for ( int i = 0; i < sortedclasses.size(); i++ ) {
            results.get( sortedclasses.get( i ) ).setRank( i + 1 );
        }
    }

    /**
     * 
     */
    private void setMultifunctionalities() {

        Multifunctionality mf = geneData.getMultifunctionality();
        multifunctionalityCorrelation = mf.correlationWithGeneMultifunctionality( geneScores.getRankedGenes() );

        messenger.showStatus( String.format( "Multifunctionality correlation is %.2f for %d values",
                multifunctionalityCorrelation, geneScores.getRankedGenes().size() ) );

        for ( GeneSetResult gsr : this.results.values() ) {
            double auc = mf.getGOTermMultifunctionality( gsr.getGeneSetId() );
            gsr.setMultifunctionality( auc );
        }

    }

    /**
     * Sorted order of the class results - all this has to hold is the class names.
     */
    private void sortResults() {
        sortedclasses = new Vector<GeneSetTerm>( results.entrySet().size() );
        Collection<GeneSetResult> k = results.values();
        List<GeneSetResult> l = new Vector<GeneSetResult>( k );
        Collections.sort( l );
        for ( Iterator<GeneSetResult> it = l.iterator(); it.hasNext(); ) {
            sortedclasses.add( it.next().getGeneSetId() );
        }
    }
}