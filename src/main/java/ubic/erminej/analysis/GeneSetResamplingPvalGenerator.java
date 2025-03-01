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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ubic.basecode.util.StatusViewer;
import ubic.erminej.SettingsHolder;
import ubic.erminej.SettingsHolder.GeneScoreMethod;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;

/**
 * Generates gene set p values using the resampling-based 'experiment score' method of Pavlidis et al. 2002, or by using
 * precision-recall curves (which are also calibrated by using resampling).
 *
 * @author Paul Pavlidis
 */
public class GeneSetResamplingPvalGenerator extends AbstractGeneSetPvalGenerator {

    protected Histogram hist;

    protected GeneSetResamplingBkgDistGenerator generator;

    /**
     * <p>
     * Constructor for GeneSetResamplingPvalGenerator.
     * </p>
     *
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param a a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param geneToScoreMap already log-transformed, if requested.
     * @param messenger a {@link ubic.basecode.util.StatusViewer} object.
     */
    public GeneSetResamplingPvalGenerator( SettingsHolder settings, GeneAnnotations a,
            Map<Gene, Double> geneToScoreMap, StatusViewer messenger ) {
        super( settings, a, geneToScoreMap, messenger );
        this.generator = new GeneSetResamplingBkgDistGenerator( settings, geneToScoreMap );
        this.hist = generator.generateNullDistribution( messenger );
    }

    /**
     * Get results for one class, based on class id. The other arguments are things that are not constant under
     * permutations of the data.
     *
     * @param geneSetName a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a {@link ubic.erminej.data.GeneSetResult} object.
     */
    public GeneSetResult classPval( GeneSetTerm geneSetName ) {
        if ( !super.checkAspectAndRedundancy( geneSetName ) ) return null;

        int numGenesInSet = numGenesInSet( geneSetName );

        if ( numGenesInSet < settings.getMinClassSize() || numGenesInSet > settings.getMaxClassSize() ) {
            return null;
        }

        Collection<Gene> genesInSet = geneAnnots.getGeneSetGenes( geneSetName );

        // store scores for items in the class.
        double[] groupGeneScores = new double[numGenesInSet];

        int v_size = 0;

        // foreach item in the class.
        for ( Gene gene : genesInSet ) {
            // if it is in the data set. This is invariant under permutations.
            if ( geneToScoreMap.containsKey( gene ) ) {
                groupGeneScores[v_size] = geneToScoreMap.get( gene );
                v_size++;
            } // if in data set
        }

        // get raw score and pvalue.
        double rawscore = generator.computeRawScore( groupGeneScores, genesInSet );

        double pval = scoreToPval( numGenesInSet, rawscore );

        if ( pval < 0.0 ) {
            throw new IllegalStateException( "A raw score (" + rawscore + ") yielded an invalid pvalue: Classname: "
                    + geneSetName );
        }

        // set up the return object.
        GeneSetResult res = new GeneSetResult( geneSetName, numElementsInSet( geneSetName ),
                numGenesInSet( geneSetName ), settings );
        res.setScore( rawscore );
        res.setPValue( pval );
        return res;

    }

    /**
     * Same thing as classPval, but returns a more raw map of genesets to scores (pvalues) (see below) instead of adding
     * them to the results object. This is used to get class pvalues for permutation analysis (W-Y correction)
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<GeneSetTerm, Double> classPvalGeneratorRaw() {
        Map<GeneSetTerm, Double> results = new HashMap<>();

        ExperimentScoreQuickPvalGenerator cpv = new ExperimentScoreQuickPvalGenerator( settings, geneAnnots,
                geneToScoreMap, getMessenger() );

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getGeneSetTerms().iterator(); iter.hasNext(); ) {
            GeneSetTerm className = iter.next();
            double pval = cpv.classPvalue( className );

            if ( pval >= 0.0 ) {
                results.put( className, pval );
            }
        }
        return results;
    }

    /*
     * (non-Javadoc)
     *
     * @see ubic.erminej.analysis.AbstractGeneSetPvalGenerator#classPvalGenerator()
     */
    /** {@inheritDoc} */
    @Override
    public Map<GeneSetTerm, GeneSetResult> generateGeneSetResults() {

        Map<GeneSetTerm, GeneSetResult> results = generateGeneSetResults( true );

        return results;
    }

    /**
     * <p>
     * generateGeneSetResults.
     * </p>
     *
     * @param useMultifunctionalityCorrection a boolean.
     * @return a {@link java.util.Map} object.
     */
    protected Map<GeneSetTerm, GeneSetResult> generateGeneSetResults( boolean useMultifunctionalityCorrection ) {
        Map<GeneSetTerm, GeneSetResult> results;
        results = new HashMap<>();

        int i = 0;
        for ( GeneSetTerm className : geneAnnots.getGeneSetTerms() ) {
            ifInterruptedStop();
            GeneSetResult res = this.classPval( className );
            if ( res != null ) {
                results.put( className, res );
                if ( ++i % ALERT_UPDATE_FREQUENCY == 0 ) {
                    getMessenger().showProgress( i + " gene sets analyzed" );
                }
            }
        }
        if ( results.isEmpty() ) return results;
        GeneSetPvalRun.populateRanks( results );

        if ( useMultifunctionalityCorrection ) {

            boolean useRanks = false;//settings.getGeneSetResamplingScoreMethod().equals( SettingsHolder.GeneScoreMethod.PRECISIONRECALL );

            Map<Gene, Double> adjustScores = this.geneAnnots.getMultifunctionality().adjustScores( geneToScoreMap,
                    useRanks, true /* weighted regression */ );

            /* compute new results */
            GeneSetResamplingPvalGenerator pvg = new GeneSetResamplingPvalGenerator( this.settings, this.geneAnnots,
                    adjustScores, this.messenger );

            Map<GeneSetTerm, GeneSetResult> mfCorrectedResults = pvg.generateGeneSetResults( false );
            List<GeneSetTerm> sortedClasses = GeneSetPvalRun.getSortedClasses( mfCorrectedResults );
            multipleTestCorrect( sortedClasses, mfCorrectedResults );
            GeneSetPvalRun.populateRanks( mfCorrectedResults );

            for ( GeneSetTerm t : results.keySet() ) {
                GeneSetResult geneSetResult = results.get( t );
                if ( mfCorrectedResults.get( t ) != null ) {
                    geneSetResult.setMultifunctionalityCorrectedRankDelta( mfCorrectedResults.get( t ).getRank()
                            - geneSetResult.getRank() );

                    geneSetResult.setMfCorrectedPvalue( mfCorrectedResults.get( t ).getPvalue() );
                    geneSetResult.setMfCorrectedFdr( mfCorrectedResults.get( t ).getCorrectedPvalue() );
                }
            }

        }
        return results;
    }

    /**
     * Convert a raw score into a pvalue, based on background distribution
     *
     * @param geneSetSize the size of the gene set that this score is for (used to identify which distribution to use)
     * @param rawscore the raw score of the gene set
     * @return double the pvalue for the raw score.
     */
    protected double scoreToPval( int geneSetSize, double rawscore ) {

        if ( hist == null ) throw new IllegalStateException( "Histogram is null" );

        double pval = hist.getValue( geneSetSize, rawscore, settings.upperTail() );

        if ( pval < 0.0 ) {
            throw new IllegalStateException( "P value less than zero. Upper tail?" + settings.upperTail()
                    + " Raw score: " + rawscore );
        }

        if ( Double.isNaN( pval ) ) {
            throw new IllegalStateException( "A pvalue was not a number: raw score = " + rawscore );
        }

        return pval;
    }

}
