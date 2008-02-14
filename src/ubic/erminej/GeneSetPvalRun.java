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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ubic.basecode.math.Rank;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.dataStructure.matrix.DoubleMatrixNamed;
import ubic.erminej.analysis.CorrelationsGeneSetPvalSeriesGenerator;
import ubic.erminej.analysis.GeneSetPvalSeriesGenerator;
import ubic.erminej.analysis.GeneSetSizeComputer;
import ubic.erminej.analysis.MultipleTestCorrector;
import ubic.erminej.analysis.NullDistributionGenerator;
import ubic.erminej.analysis.OraGeneSetPvalSeriesGenerator;
import ubic.erminej.analysis.ResamplingCorrelationGeneSetScore;
import ubic.erminej.analysis.ResamplingExperimentGeneSetScore;
import ubic.erminej.analysis.RocPvalGenerator;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.Histogram;

/**
 * Class that does all the work in doing gene set scoring. Holds the results as well.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetPvalRun {

    /**
     * 
     */
    private static final int NUM_WY_SAMPLES = 10000;
    private GeneAnnotations geneData;
    private GeneScores geneScores;

    private Histogram hist;
    private Map results = null;
    private Vector sortedclasses = null; // this holds the results.
    private NumberFormat nf = NumberFormat.getInstance();
    private Settings settings;

    private long randomSeed = -1;

    private String name; // name of this run.

    /**
     * Use this when we are loading in existing results.
     * 
     * @param activeProbes
     * @param settings
     * @param geneData
     * @param rawData
     * @param goData
     * @param geneScores
     * @param messenger
     * @param results
     * @param name Name of the run
     */
    public GeneSetPvalRun( Set activeProbes, Settings settings, GeneAnnotations geneData, DoubleMatrixNamed rawData,
            GONames goData, GeneScores geneScores, StatusViewer messenger, Map results, String name ) {
        this.settings = settings;
        this.geneData = geneData;

        this.geneScores = geneScores;
        this.results = results;
        this.name = name;

        sortResults();
        // get the class sizes.

        GeneSetSizeComputer csc = new GeneSetSizeComputer( activeProbes, geneData, geneScores, settings.getUseWeights() );

        messenger.showStatus( "Multiple test correction..." );
        MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedclasses, hist, geneData, csc, geneScores,
                results, null );
        String mtc_method = "bh";
        if ( mtc_method.equals( "bon" ) ) {
            mt.bonferroni();
        } else if ( mtc_method.equals( "bh" ) ) {
            mt.benjaminihochberg( 0.05 );
        } else if ( mtc_method.equals( "wy" ) ) {
            mt.westfallyoung( 10000 );
        }

        // For table output
        for ( int i = 0; i < sortedclasses.size(); i++ ) {
            ( ( GeneSetResult ) results.get( sortedclasses.get( i ) ) ).setRank( i + 1 );
        }
        messenger.showStatus( "Done!" );
    }

    // /**
    // * Do a new analysis, starting from the bare essentials.
    // */

    /**
     * Do a new analysis.
     * 
     * @param activeProbes
     * @param settings
     * @param geneData
     * @param rawData
     * @param goData
     * @param geneScores
     * @param messenger
     * @param name Name of the run
     */
    public GeneSetPvalRun( Set<String> activeProbes, Settings settings, GeneAnnotations geneData,
            DoubleMatrixNamed<String, String> rawData, GONames goData, GeneScores geneScores, StatusViewer messenger,
            String name ) {
        this.settings = settings;
        this.geneData = geneData;
        this.geneScores = geneScores;
        this.name = name;

        nf.setMaximumFractionDigits( 8 );
        results = new LinkedHashMap();

        runAnalysis( activeProbes, settings, geneData, rawData, goData, geneScores, messenger );
    }

    public GeneSetPvalRun( Settings settings, GeneAnnotations geneData, GONames goData, GeneScores geneScores ) {
        this.settings = settings;
        this.geneData = geneData;
        this.geneScores = geneScores;
        nf.setMaximumFractionDigits( 8 );
        results = new LinkedHashMap();
        runAnalysis( geneData.getActiveProbes(), settings, geneData, null, goData, geneScores, null );
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

    public String getName() {
        return name;
    }

    /**
     * @return Map the results
     */
    public Map getResults() {
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
    public Vector getSortedClasses() {
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
        int multipleTestCorrMethod = settings.getMtc();
        if ( multipleTestCorrMethod == Settings.BONFERONNI ) {
            mt.bonferroni();
        } else if ( multipleTestCorrMethod == Settings.BENJAMINIHOCHBERG ) {
            mt.benjaminihochberg( 0.05 );
        } else if ( multipleTestCorrMethod == Settings.WESTFALLYOUNG ) {
            if ( settings.getClassScoreMethod() != Settings.RESAMP )
                throw new UnsupportedOperationException(
                        "Westfall-Young correction is not supported for this analysis method" );
            mt.westfallyoung( NUM_WY_SAMPLES );
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
    private void runAnalysis( Collection<String> activeProbes, Settings settings1, GeneAnnotations geneData1,
            DoubleMatrixNamed<String, String> rawData, GONames goData, GeneScores geneScores1, StatusViewer messenger ) {
        // get the class sizes.
        GeneSetSizeComputer csc = new GeneSetSizeComputer( activeProbes, geneData1, geneScores1, settings1
                .getUseWeights() );

        switch ( settings1.getClassScoreMethod() ) {
            case Settings.RESAMP: {
                NullDistributionGenerator probePvalMapper = new ResamplingExperimentGeneSetScore( settings1,
                        geneScores1 );

                if ( messenger != null ) messenger.showStatus( "Starting resampling" );

                if ( randomSeed >= 0 ) {
                    probePvalMapper.setRandomSeed( randomSeed );
                }
                hist = probePvalMapper.generateNullDistribution( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                if ( messenger != null ) messenger.showStatus( "Finished resampling" );

                GeneSetPvalSeriesGenerator pvg = new GeneSetPvalSeriesGenerator( settings1, geneData1, hist, csc,
                        goData );
                if ( Thread.currentThread().isInterrupted() ) return;
                // calculate the actual class scores and correct sorting.
                results = pvg.classPvalGenerator( geneScores1.getGeneToPvalMap(), geneScores1.getProbeToScoreMap() );

                break;
            }
            case Settings.ORA: {

                int inputSize = activeProbes.size();

                Collection inp_entries = geneScores1.getProbeToScoreMap().entrySet();

                if ( messenger != null ) messenger.showStatus( "Starting ORA analysis" );

                OraGeneSetPvalSeriesGenerator pvg = new OraGeneSetPvalSeriesGenerator( settings1, geneData1, csc,
                        goData, inputSize );
                int numOver = pvg.hgSizes( inp_entries );

                if ( numOver == 0 ) {
                    if ( messenger != null ) messenger.showError( "No genes selected at that threshold!" );
                    break;
                }

                results = pvg.classPvalGenerator( geneScores1.getGeneToPvalMap(), geneScores1.getProbeToScoreMap(),
                        messenger );

                if ( messenger != null )
                    messenger.showStatus( "Finished with ORA computations: " + numOver
                            + " probes passed your threshold." );
                break;
            }
            case Settings.CORR: {
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
                        geneData1, csc, goData, rawData, hist );
                if ( messenger != null ) messenger.showStatus( "Finished resampling, computing for gene sets" );
                results = pvg.classPvalGenerator( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                if ( messenger != null ) messenger.showStatus( "Finished computing scores" );

                break;
            }
            case Settings.ROC: {
                RocPvalGenerator rpg = new RocPvalGenerator( settings1, geneData1, csc, goData );
                Map geneRanksMap;
                if ( messenger != null ) messenger.showStatus( "Rank transforming" );
                if ( settings1.getUseWeights() ) {
                    geneRanksMap = Rank.rankTransform( geneScores1.getGeneToPvalMap() );
                    if ( messenger != null ) messenger.showStatus( "Computing gene set scores" );
                    results = rpg.classPvalGenerator( geneScores1.getGeneToPvalMap(), geneRanksMap, messenger );
                } else {
                    geneRanksMap = Rank.rankTransform( geneScores1.getProbeToScoreMap() );
                    if ( messenger != null ) messenger.showStatus( "Computing gene set scores" );
                    results = rpg.classPvalGenerator( geneScores1.getProbeToScoreMap(), geneRanksMap, messenger );
                }

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
        if ( messenger != null ) messenger.showStatus( "Done!" );
    }

    /**
     * 
     */
    private void setGeneSetRanks() {
        // For table output
        for ( int i = 0; i < sortedclasses.size(); i++ ) {
            ( ( GeneSetResult ) results.get( sortedclasses.get( i ) ) ).setRank( i + 1 );
        }
    }

    /**
     * Sorted order of the class results - all this has to hold is the class names.
     */
    private void sortResults() {
        sortedclasses = new Vector( results.entrySet().size() );
        Collection k = results.values();
        Vector l = new Vector();
        l.addAll( k );
        Collections.sort( l );
        for ( Iterator it = l.iterator(); it.hasNext(); ) {
            sortedclasses.add( ( ( GeneSetResult ) it.next() ).getGeneSetId() );
        }
    }
}