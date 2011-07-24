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

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import ubic.basecode.util.StatusViewer;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.erminej.analysis.CorrelationsGeneSetPvalSeriesGenerator;
import ubic.erminej.analysis.GeneSetPvalSeriesGenerator;
import ubic.erminej.analysis.GeneSetSizeComputer;
import ubic.erminej.analysis.MultipleTestCorrector;
import ubic.erminej.analysis.NullDistributionGenerator;
import ubic.erminej.analysis.OraGeneSetPvalSeriesGenerator;
import ubic.erminej.analysis.ResamplingCorrelationGeneSetScore;
import ubic.erminej.analysis.ResamplingExperimentGeneSetScore;
import ubic.erminej.analysis.RocPvalGenerator;
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

    private GeneAnnotations geneData; // ones used in the analysis
    private GeneScores geneScores;

    private Histogram hist;
    private Map<GeneSetTerm, GeneSetResult> results = null;
    private List<GeneSetTerm> sortedclasses = null; // this holds the results.
    private NumberFormat nf = NumberFormat.getInstance();
    private Settings settings;

    private double multifunctionalityCorrelation = -1;

    private long randomSeed = -1;

    private String name; // name of this run.

    /**
     * @param activeProbes
     * @param geneDataSets
     * @return
     */
    public synchronized GeneAnnotations getPrunedAnnotations( Collection<Probe> activeProbes, GeneAnnotations original ) {
        GeneAnnotations result = null;

        if ( original.getProbes().size() == activeProbes.size() && original.getProbes().containsAll( activeProbes ) ) {
            result = original;
        } else {
            result = new GeneAnnotations( original, activeProbes );
        }

        return result;
    }

    /**
     * @param rawData
     * @param scores
     * @return
     */
    private synchronized Set<Probe> getActiveProbes( DoubleMatrix<Probe, String> rawData, GeneScores scores ) {

        Set<Probe> activeProbes = null;
        if ( scores != null ) { // favor the geneScores list.
            activeProbes = scores.getProbeToScoreMap().keySet();
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
    public GeneSetPvalRun( Settings settings, GeneAnnotations originalAnnots, DoubleMatrix<Probe, String> rawData,
            GeneScores geneScores, StatusViewer messenger, double multifunctionalityCorrelation, String name ) {
        this.settings = settings;

        this.geneScores = geneScores;
        this.name = name;
        this.multifunctionalityCorrelation = multifunctionalityCorrelation;

        nf.setMaximumFractionDigits( 8 );
        results = new LinkedHashMap<GeneSetTerm, GeneSetResult>();

        Set<Probe> activeProbes = getActiveProbes( rawData, geneScores );
        this.geneData = getPrunedAnnotations( activeProbes, originalAnnots );

        runAnalysis( settings, this.geneData, rawData, geneScores, messenger );
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
     * @param multifunctionalityCorrelation
     * @param name Name of the run
     */
    public GeneSetPvalRun( Settings settings, GeneAnnotations geneData, DoubleMatrix<Probe, String> rawData,
            GeneScores geneScores, StatusViewer messenger, Map<GeneSetTerm, GeneSetResult> results,
            double multifunctionalityCorrelation, String name ) {
        this.settings = settings;

        this.geneScores = geneScores;
        this.results = results;
        this.name = name;
        this.multifunctionalityCorrelation = multifunctionalityCorrelation;

        Set<Probe> activeProbes = getActiveProbes( rawData, geneScores );
        this.geneData = getPrunedAnnotations( activeProbes, geneData );

        sortResults();
        // For table output
        for ( int i = 0; i < sortedclasses.size(); i++ ) {
            results.get( sortedclasses.get( i ) ).setRank( i + 1 );
        }
        messenger.showStatus( "Done!" );
    }

    /**
     * Do a new analysis, starting from the bare essentials.
     */
    public GeneSetPvalRun( Settings settings, GeneAnnotations geneData, GeneScores geneScores ) {
        this.settings = settings;

        this.geneScores = geneScores;
        nf.setMaximumFractionDigits( 8 );
        results = new LinkedHashMap<GeneSetTerm, GeneSetResult>();

        Set<Probe> activeProbes = getActiveProbes( null, geneScores );
        this.geneData = getPrunedAnnotations( activeProbes, geneData );

        runAnalysis( settings, geneData, null, geneScores, null );
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
     * @return Settings
     */
    public Settings getSettings() {
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
     * @param settings
     * @param geneData
     * @param geneScores
     * @param messenger
     * @param csc
     */
    private void multipleTestCorrect( StatusViewer messenger, GeneSetSizeComputer csc ) {
        if ( messenger != null ) messenger.showStatus( "Multiple test correction..." );
        MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedclasses, hist, geneData, csc, geneScores,
                results, messenger );
        Settings.MultiTestCorrMethod multipleTestCorrMethod = settings.getMtc();
        if ( multipleTestCorrMethod == Settings.MultiTestCorrMethod.BONFERONNI ) {
            mt.bonferroni();
        } else if ( multipleTestCorrMethod.equals( Settings.MultiTestCorrMethod.BENJAMINIHOCHBERG ) ) {
            mt.benjaminihochberg();
        } else if ( multipleTestCorrMethod.equals( Settings.MultiTestCorrMethod.WESTFALLYOUNG ) ) {
            if ( !( settings.getClassScoreMethod().equals( Settings.Method.GSR ) ) )
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
    private void runAnalysis( Settings settings1, GeneAnnotations geneData1, DoubleMatrix<Probe, String> rawData,
            GeneScores geneScores1, StatusViewer messenger ) {
        // get the class sizes.
        GeneSetSizeComputer csc = new GeneSetSizeComputer( geneData1, geneScores1, settings1.getUseWeights() );

        switch ( settings1.getClassScoreMethod() ) {
            case GSR: {
                NullDistributionGenerator probePvalMapper = new ResamplingExperimentGeneSetScore( settings1,
                        geneScores1 );

                if ( messenger != null ) messenger.showStatus( "Starting resampling" );

                if ( randomSeed >= 0 ) {
                    probePvalMapper.setRandomSeed( randomSeed );
                }
                hist = probePvalMapper.generateNullDistribution( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                if ( messenger != null ) messenger.showStatus( "Finished resampling" );

                GeneSetPvalSeriesGenerator pvg = new GeneSetPvalSeriesGenerator( settings1, geneData1, hist, csc );
                if ( Thread.currentThread().isInterrupted() ) return;
                // calculate the actual class scores and correct sorting.
                results = pvg.classPvalGenerator( geneScores1.getGeneToScoreMap(), geneScores1.getProbeToScoreMap() );

                break;
            }
            case ORA: {

                int inputSize = geneData1.getProbes().size();

                Collection<Entry<Probe, Double>> inp_entries = geneScores1.getProbeToScoreMap().entrySet();

                if ( messenger != null ) messenger.showStatus( "Starting ORA analysis" );

                OraGeneSetPvalSeriesGenerator pvg = new OraGeneSetPvalSeriesGenerator( settings1, geneData1, csc,
                        inputSize );
                int numOver = pvg.hgSizes( inp_entries );

                if ( numOver == 0 ) {
                    if ( messenger != null ) messenger.showError( "No genes selected at that threshold!" );
                    break;
                }

                results = pvg.classPvalGenerator( geneScores1.getGeneToScoreMap(), geneScores1.getProbeToScoreMap(),
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
                NullDistributionGenerator probePvalMapper = new ResamplingCorrelationGeneSetScore( settings1, rawData );

                if ( randomSeed >= 0 ) {
                    probePvalMapper.setRandomSeed( randomSeed );
                }

                hist = probePvalMapper.generateNullDistribution( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                CorrelationsGeneSetPvalSeriesGenerator pvg = new CorrelationsGeneSetPvalSeriesGenerator( settings1,
                        geneData1, csc, rawData, hist );
                if ( messenger != null ) messenger.showStatus( "Finished resampling, computing for gene sets" );
                results = pvg.classPvalGenerator( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                if ( messenger != null ) messenger.showStatus( "Finished computing scores" );

                break;
            }
            case ROC: {
                RocPvalGenerator rpg = new RocPvalGenerator( settings1, geneData1, csc, messenger );
                if ( messenger != null ) messenger.showStatus( "Computing gene set scores" );
                results = rpg.classPvalGenerator( geneScores1 );

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
        multipleTestCorrect( messenger, csc );

        setGeneSetRanks();

        setMultifuncationalities();

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
    private void setMultifuncationalities() {
        Multifunctionality mf = new Multifunctionality( geneData );
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