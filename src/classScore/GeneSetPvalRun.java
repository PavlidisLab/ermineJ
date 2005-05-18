package classScore;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.util.StatusViewer;
import classScore.analysis.CorrelationsGeneSetPvalSeriesGenerator;
import classScore.analysis.GeneSetPvalSeriesGenerator;
import classScore.analysis.GeneSetSizeComputer;
import classScore.analysis.MultipleTestCorrector;
import classScore.analysis.NullDistributionGenerator;
import classScore.analysis.OraGeneSetPvalSeriesGenerator;
import classScore.analysis.ResamplingCorrelationGeneSetScore;
import classScore.analysis.ResamplingExperimentGeneSetScore;
import classScore.data.GeneScoreReader;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * Class that does all the work in doing gene set scoring. Holds the results as well.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
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
    private GeneScoreReader geneScores;
    private GONames goData; // shared by all
    private Histogram hist;
    private Map results = null;
    private Vector sortedclasses = null; // this holds the results.
    private NumberFormat nf = NumberFormat.getInstance();
    private boolean useUniform = false; // assume input values come from uniform
    private Set activeProbes;
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
    public GeneSetPvalRun( Set activeProbes, Settings settings, GeneAnnotations geneData,
            DenseDoubleMatrix2DNamed rawData, GONames goData, GeneScoreReader geneScores, StatusViewer messenger,
            Map results, String name ) {
        this.settings = settings;
        this.geneData = geneData;
        this.goData = goData;
        this.geneScores = geneScores;
        this.activeProbes = activeProbes;
        this.results = results;
        this.name = name;

        sortResults();
        // get the class sizes.

        GeneSetSizeComputer csc = new GeneSetSizeComputer( activeProbes, geneData, geneScores, settings.getUseWeights() );

        messenger.setStatus( "Multiple test correction..." );
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
        messenger.setStatus( "Done!" );
    }

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
    public GeneSetPvalRun( Set activeProbes, Settings settings, GeneAnnotations geneData,
            DenseDoubleMatrix2DNamed rawData, GONames goData, GeneScoreReader geneScores, StatusViewer messenger,
            String name ) {
        this.settings = settings;
        this.geneData = geneData;
        this.goData = goData;
        this.geneScores = geneScores;
        this.activeProbes = activeProbes;
        this.name = name;

        nf.setMaximumFractionDigits( 8 );
        results = new LinkedHashMap();

        // get the class sizes.
        GeneSetSizeComputer csc = new GeneSetSizeComputer( activeProbes, geneData, geneScores, settings.getUseWeights() );

        switch ( settings.getAnalysisMethod() ) {
            case Settings.RESAMP: {
                NullDistributionGenerator probePvalMapper = new ResamplingExperimentGeneSetScore( settings, geneScores );

                messenger.setStatus( "Starting resampling" );

                if ( randomSeed >= 0 ) {
                    probePvalMapper.setRandomSeed( randomSeed );
                }
                hist = probePvalMapper.generateNullDistribution( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                messenger.setStatus( "Finished resampling" );

                GeneSetPvalSeriesGenerator pvg = new GeneSetPvalSeriesGenerator( settings, geneData, hist, csc, goData );
                if ( Thread.currentThread().isInterrupted() ) return;
                // calculate the actual class scores and correct sorting.
                pvg.classPvalGenerator( geneScores.getGeneToPvalMap(), geneScores.getProbeToPvalMap() );
                results = pvg.getResults();
                break;
            }
            case Settings.ORA: {

                int inputSize = activeProbes.size();

                Collection inp_entries = geneScores.getProbeToPvalMap().entrySet();

                messenger.setStatus( "Starting ORA analysis" );

                OraGeneSetPvalSeriesGenerator pvg = new OraGeneSetPvalSeriesGenerator( settings, geneData, csc, goData,
                        inputSize );
                int numOver = pvg.hgSizes( inp_entries );

                if ( numOver == 0 ) {
                    messenger.setError( "No genes selected at that threshold!" );
                    break;
                }

                pvg.classPvalGenerator( geneScores.getGeneToPvalMap(), geneScores.getProbeToPvalMap() );
                if ( Thread.currentThread().isInterrupted() ) return;
                results = pvg.getResults();
                messenger.setStatus( "Finished with ORA computations: " + numOver + " probes passed your threshold." );
                break;
            }
            case Settings.CORR: {
                messenger.setStatus( "Starting correlation resampling" );
                NullDistributionGenerator probePvalMapper = new ResamplingCorrelationGeneSetScore( settings, rawData );

                if ( randomSeed >= 0 ) {
                    probePvalMapper.setRandomSeed( randomSeed );
                }

                hist = probePvalMapper.generateNullDistribution( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                CorrelationsGeneSetPvalSeriesGenerator pvg = new CorrelationsGeneSetPvalSeriesGenerator( settings,
                        geneData, csc, goData, rawData, hist );
                messenger.setStatus( "Finished resampling, computing for gene sets" );
                pvg.geneSetCorrelationGenerator( messenger );
                if ( Thread.currentThread().isInterrupted() ) return;
                messenger.setStatus( "Finished computing scores" );
                results = pvg.getResults();

                break;
            }
            case Settings.ROC: { // todo implement this.
                // Map input_rank_map;
                // if ( settings.getUseWeights() ) {
                // input_rank_map = Rank.rankTransform( geneScores
                // .getGroupToPvalMap() );
                // } else {
                // input_rank_map = Rank.rankTransform( geneScores
                // .getProbeToPvalMap() );
                // }
                // fall through. - unsupported.
            }

            case Settings.TTEST: { // todo implement this
                // fall through. - unsupported.
            }

            case Settings.KS: { // todo implement this
                // fall through. - unsupported.
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
        messenger.setStatus( "Done!" );
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
     * @param settings
     * @param geneData
     * @param geneScores
     * @param messenger
     * @param csc
     */
    private void multipleTestCorrect( StatusViewer messenger, GeneSetSizeComputer csc ) {
        messenger.setStatus( "Multiple test correction..." );
        MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedclasses, hist, geneData, csc, geneScores,
                results, messenger );
        int multipleTestCorrMethod = settings.getMtc();
        if ( multipleTestCorrMethod == Settings.BONFERONNI ) {
            mt.bonferroni();
        } else if ( multipleTestCorrMethod == Settings.BENJAMINIHOCHBERG ) {
            mt.benjaminihochberg( 0.05 );
        } else if ( multipleTestCorrMethod == Settings.WESTFALLYOUNG ) {
            if ( settings.getAnalysisMethod() != Settings.RESAMP )
                throw new UnsupportedOperationException(
                        "Westfall-Young correction is not supported for this analysis method" );
            mt.westfallyoung( NUM_WY_SAMPLES );
        } else {
            throw new IllegalArgumentException( "Unknown multiple test correction method: " + multipleTestCorrMethod );
        }
    }

    /**
     * @return Map the results
     */
    public Map getResults() {
        return results;
    }

    /**
     * @return Map the results
     */
    public Vector getSortedClasses() {
        return sortedclasses;
    }

    public GeneAnnotations getGeneData() {
        return geneData;
    }

    /**
     * @return Settings
     */
    public Settings getSettings() {
        return settings;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    /* private methods */

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
            sortedclasses.add( ( ( GeneSetResult ) it.next() ).getClassId() );
        }
    }

    public Histogram getHist() {
        return hist;
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

    /**
     * @return Returns the geneScores.
     */
    public GeneScoreReader getGeneScores() {
        return this.geneScores;
    }
}