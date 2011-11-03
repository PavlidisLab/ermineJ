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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.DataIOUtils;
import ubic.erminej.data.EmptyGeneSetResult;
import ubic.erminej.data.Gene;
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

    private GeneAnnotations geneData; // ones used in the analysis -- this may be immutable, should only be used for
    // analysis

    private Histogram hist;
    private Map<GeneSetTerm, GeneSetResult> results = new HashMap<GeneSetTerm, GeneSetResult>();
    private SettingsHolder settings;

    private StatusViewer messenger = new StatusStderr();

    private double multifunctionalityCorrelation = -1;

    private String name; // name of this run.

    private int numAboveThreshold = 0;

    private String geneScoreColumnName = "";

    private double multifunctionalityEnrichment = -1;

    /**
     * Do a new analysis, starting from the bare essentials (correlation method not available) (simple API)
     */
    public GeneSetPvalRun( SettingsHolder settings, GeneScores geneScores ) {
        this.settings = settings;
        this.geneData = geneScores.getPrunedGeneAnnotations();
        runAnalysis( null, geneScores );
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
    public GeneSetPvalRun( SettingsHolder settings, GeneAnnotations originalAnnots, StatusViewer messenger ) {
        this.settings = settings;

        if ( messenger != null ) this.messenger = messenger;
        this.settings = settings;
        try {
            DoubleMatrix<Probe, String> rawData = null;
            GeneScores geneScores = null;
            if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) ) {
                rawData = DataIOUtils.readDataMatrixForAnalysis( originalAnnots, settings );
                this.geneData = originalAnnots.subClone( rawData.getRowNames() );
                setName( "New corr. run" );
            } else {
                geneScores = new GeneScores( settings.getScoreFile(), settings, messenger, originalAnnots );
                if ( StringUtils.isNotBlank( geneScores.getScoreColumnName() ) ) {
                    setName( "Run on: " + geneScores.getScoreColumnName() );
                }
                this.geneData = geneScores.getPrunedGeneAnnotations();
            }

            runAnalysis( rawData, geneScores );
        } catch ( IOException e ) {
            this.messenger.showError( e );
        }
    }

    /**
     * Use this when we are loading in existing results from a file.
     * 
     * @param settings
     * @param originalAnnots - this does not need to be pruned by the Reader.
     * @param messenger
     * @param results
     * @param name Name of the run
     */
    public GeneSetPvalRun( SettingsHolder settings, GeneAnnotations originalAnnots, StatusViewer messenger,
            Map<GeneSetTerm, GeneSetResult> results, String name ) throws IOException {

        this.results = results;
        this.settings = settings;
        if ( messenger != null ) this.messenger = messenger;
        this.name = name;

        DoubleMatrix<Probe, String> rawData = null;
        GeneScores geneScores = null;

        if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) ) {
            // this is quite wasteful -- we really just need to know the probe names
            rawData = DataIOUtils.readDataMatrixForAnalysis( originalAnnots, settings );
        } else {
            // this is wasteful, but not as big a deal.
            geneScores = new GeneScores( settings.getScoreFile(), settings, messenger, originalAnnots );
            this.geneScoreColumnName = geneScores.getScoreColumnName();
        }

        Set<Probe> activeProbes = getActiveProbes( rawData, geneScores );
        this.geneData = getPrunedAnnotations( activeProbes, originalAnnots );
    }

    public GeneAnnotations getGeneData() {
        return geneData;
    }

    public String getGeneScoreColumnName() {
        return geneScoreColumnName;
    }

    public Histogram getHist() {
        return hist;
    }

    public double getMultifunctionalityCorrelation() {
        return multifunctionalityCorrelation;
    }

    /**
     * For ORA only.
     * 
     * @return
     */
    public double getMultifunctionalityEnrichment() {
        return multifunctionalityEnrichment;
    }

    public String getName() {
        return name;
    }

    /**
     * ORA-only.
     * 
     * @return
     */
    public int getNumAboveThreshold() {
        return numAboveThreshold;
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

    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GeneSetPvalRun [name=" + name + "]";
    }

    /* private methods */

    private Set<Probe> getActiveProbes( DoubleMatrix<Probe, String> rawData, GeneScores geneScores ) {
        Set<Probe> activeProbes = null;
        if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) && rawData != null ) {
            activeProbes = new HashSet<Probe>( rawData.getRowNames() );
        } else {
            assert geneScores != null;
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        }
        return activeProbes;
    }

    /**
     * Restrict to genes that have annotations and which are included in the data.
     * 
     * @param activeProbes
     * @param geneDataSets
     * @return
     */
    private synchronized GeneAnnotations getPrunedAnnotations( Collection<Probe> activeProbes, GeneAnnotations original ) {
        return original.subClone( activeProbes );
    }

    /**
     * @return Ranked list. Removes any sets which are not scored.
     */
    private List<GeneSetTerm> getSortedClasses() {
        Comparator<GeneSetTerm> c = new Comparator<GeneSetTerm>() {
            @Override
            public int compare( GeneSetTerm o1, GeneSetTerm o2 ) {
                return results.get( o1 ).compareTo( results.get( o2 ) );
            }
        };

        TreeMap<GeneSetTerm, GeneSetResult> sorted = new TreeMap<GeneSetTerm, GeneSetResult>( c );
        sorted.putAll( results );

        assert sorted.size() == results.size();

        List<GeneSetTerm> sortedSets = new ArrayList<GeneSetTerm>();
        for ( GeneSetTerm r : sorted.keySet() ) {
            if ( results.get( r ) instanceof EmptyGeneSetResult /* just checking... */) {
                continue;
            }
            sortedSets.add( r );
        }

        return sortedSets;

    }

    /**
     * @param csc
     */
    private void rankAndMultipleTestCorrect( GeneScores geneScores ) {
        List<GeneSetTerm> sortedClasses = this.getSortedClasses();

        assert sortedClasses.size() > 0;

        messenger.showStatus( "Multiple test correction for " + sortedClasses.size() + " scored sets." );
        for ( int i = 0; i < sortedClasses.size(); i++ ) {
            results.get( sortedClasses.get( i ) ).setRank( i + 1 );
        }

        MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedClasses, hist, geneData, geneScores,
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
     * Perform the requested analysis, using the rawData or geneScores as appropriate.
     * 
     * @param rawData may be null if not needed
     * @param geneScores may be null if not needed.
     */
    private void runAnalysis( DoubleMatrix<Probe, String> rawData, GeneScores geneScores ) {

        if ( geneScores != null ) geneScoreColumnName = geneScores.getScoreColumnName();

        // only used for ORA
        Collection<Gene> genesAboveThreshold = new HashSet<Gene>();

        switch ( settings.getClassScoreMethod() ) {
            case GSR: {

                assert geneScores != null;
                messenger.showStatus( "Starting GSR analysis" );
                Map<Gene, Double> geneToScoreMap;

                if ( settings.useMultifunctionalityCorrection() ) {
                    geneToScoreMap = this.geneData.getMultifunctionality()
                            .adjustScores( geneScores, false /* not ranks */);
                } else {
                    geneToScoreMap = geneScores.getGeneToScoreMap();
                }

                GeneSetResamplingPvalGenerator pvg = new GeneSetResamplingPvalGenerator( settings, geneToScoreMap,
                        geneData, messenger );
                if ( Thread.currentThread().isInterrupted() ) return;

                results = pvg.classPvalGenerator( geneToScoreMap );
                break;
            }
            case ORA: {

                messenger.showStatus( "Starting ORA analysis" );
                assert geneScores != null;
                OraPvalGenerator pvg = new OraPvalGenerator( settings, geneScores, geneData );

                numAboveThreshold = pvg.getNumGenesOverThreshold();

                if ( numAboveThreshold == 0 ) {
                    if ( messenger != null ) messenger.showError( "No genes selected at that threshold!" );
                    break;
                }
                Map<Gene, Double> geneToScoreMap = geneScores.getGeneToScoreMap();

                results = pvg.classPvalGenerator( geneToScoreMap, messenger );

                genesAboveThreshold = pvg.getGenesAboveThreshold();

                messenger.showStatus( "Finished with ORA computations: " + numAboveThreshold
                        + " probes passed your threshold." );

                break;
            }
            case CORR: {

                messenger.showStatus( "Starting correlation resampling in " + Thread.currentThread().getName() );

                NullDistributionGenerator probePvalMapper = new ResamplingCorrelationGeneSetScore( settings, rawData );

                hist = probePvalMapper.generateNullDistribution( messenger );

                if ( Thread.currentThread().isInterrupted() ) return;

                CorrelationPvalGenerator pvg = new CorrelationPvalGenerator( settings, geneData, rawData, hist );

                messenger.showStatus( "Finished resampling, computing for gene sets" );

                results = pvg.classPvalGenerator( messenger );

                if ( Thread.currentThread().isInterrupted() ) return;

                messenger.showStatus( "Finished computing scores" );

                break;
            }
            case ROC: {
                RocPvalGenerator rpg = new RocPvalGenerator( settings, geneData, messenger );

                assert geneScores != null;

                Map<Gene, Double> geneToScoreMap;
                if ( settings.useMultifunctionalityCorrection() ) {
                    geneToScoreMap = this.geneData.getMultifunctionality().adjustScores( geneScores, true );
                } else {
                    geneToScoreMap = geneScores.getGeneToScoreMap();
                }

                messenger.showStatus( "Computing gene set scores" );

                results = rpg.classPvalGenerator( geneToScoreMap );

                break;
            }

            default: {
                throw new UnsupportedOperationException( "Unsupported analysis method" );
            }
        }

        if ( results.size() == 0 ) {
            return;
        }

        rankAndMultipleTestCorrect( geneScores );

        setMultifunctionalities( geneScores, genesAboveThreshold );

        if ( messenger != null ) messenger.showStatus( "Done!" );
    }

    /**
     * @param genesAboveThreshold
     */
    private void setMultifunctionalities( GeneScores geneScores, Collection<Gene> genesAboveThreshold ) {

        Multifunctionality mf = geneData.getMultifunctionality();

        if ( geneScores != null ) {
            multifunctionalityCorrelation = mf.correlationWithGeneMultifunctionality( geneScores.getRankedGenes() );
            messenger.showStatus( String.format( "Multifunctionality correlation is %.2f for %d values",
                    multifunctionalityCorrelation, geneScores.getRankedGenes().size() ) );

            if ( genesAboveThreshold != null && !genesAboveThreshold.isEmpty() ) {
                this.multifunctionalityEnrichment = mf.enrichmentForMultifunctionality( genesAboveThreshold );
            }
        }

        for ( Object o : this.results.values() ) {
            GeneSetResult gsr = ( GeneSetResult ) o;
            double auc = mf.getGOTermMultifunctionality( gsr.getGeneSetId() );
            gsr.setMultifunctionality( auc );
        }

    }

}