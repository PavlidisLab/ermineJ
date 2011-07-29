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
import java.util.HashSet;
import java.util.Map;

import ubic.basecode.math.SpecFunc;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Probe;
import cern.jet.math.Arithmetic;

/**
 * Compute gene set scores based on over-representation analysis (ORA).
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class OraPvalGenerator extends AbstractGeneSetPvalGenerator {

    protected double geneScoreThreshold;

    private static final int ALERT_UPDATE_FREQUENCY = 300;

    private GeneScores geneScores;

    private Collection<Probe> probesAboveThreshold = new HashSet<Probe>();
    private Collection<Gene> genesAboveThreshold = new HashSet<Gene>();

    /**
     * @param settings
     * @param a
     * @param csc
     * @param not
     * @param nut
     * @param inputSize
     */
    public OraPvalGenerator( SettingsHolder settings, GeneScores geneScores, GeneAnnotations a,
            GeneSetSizesForAnalysis csc ) {

        super( settings, a, csc );

        this.geneScores = geneScores;

        if ( settings.getUseLog() ) {
            this.geneScoreThreshold = -Arithmetic.log10( settings.getGeneScoreThreshold() );
        } else {
            this.geneScoreThreshold = settings.getGeneScoreThreshold();
        }

        computeCounts();

    }

    public double getGeneScoreThreshold() {
        return geneScoreThreshold;
    }

    /**
     * Always for the genes.
     * 
     * @return
     */
    public int getNumGenesOverThreshold() {
        return genesAboveThreshold.size();
    }

    public int getNumProbesOverThreshold() {
        return probesAboveThreshold.size();
    }

    /**
     * Always for genes.
     * 
     * @return
     */
    public int getNumGenesUnderThreshold() {
        return geneScores.getGeneToScoreMap().size() - getNumGenesOverThreshold();
    }

    /**
     * Generate a complete set of class results.
     * 
     * @param geneToGeneScoreMap
     * @param probesToPvals
     */
    public Map<GeneSetTerm, GeneSetResult> classPvalGenerator( Map<Gene, Double> geneToGeneScoreMap,
            Map<Probe, Double> probesToPvals, StatusViewer messenger ) {
        Map<GeneSetTerm, GeneSetResult> results = new HashMap<GeneSetTerm, GeneSetResult>();

        int count = 0;

        for ( GeneSetTerm geneSetName : geneAnnots.getNonEmptyGeneSets() ) {

            GeneSetResult res = classPval( geneSetName );
            if ( res != null ) {
                results.put( geneSetName, res );

                if ( ++count % 100 == 0 ) ifInterruptedStop();
                if ( messenger != null && count % ALERT_UPDATE_FREQUENCY == 0 ) {
                    messenger.showStatus( count + " gene sets analyzed" );
                }
            }

        }
        return results;
    }

    /**
     * Test whether a score meets a threshold.
     * 
     * @param geneScore
     * @param geneScoreThreshold
     * @return
     */
    private boolean scorePassesThreshold( double geneScore ) {
        return ( settings.upperTail() && geneScore >= geneScoreThreshold )
                || ( !settings.upperTail() && geneScore <= geneScoreThreshold );
    }

    /**
     * Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution.
     * 
     * @param geneScores The pvalues for the probes (no weights) or groups (weights)
     * @return number of entries that meet the user-set threshold.
     * @todo make this private and called by OraPvalGenerator.
     */
    public void computeCounts() {

        Map<Probe, Double> probeScores = geneScores.getProbeToScoreMap();

        for ( Probe p : probeScores.keySet() ) {
            double geneScore = probeScores.get( p );

            if ( scorePassesThreshold( geneScore ) ) {
                probesAboveThreshold.add( p );
                genesAboveThreshold.add( p.getGene() );
            }

        }

    }

    /**
     * Get results for one class, based on class id. The other arguments are things that are not constant under
     * permutations of the data.
     */
    public GeneSetResult classPval( GeneSetTerm className ) {

        if ( !super.checkAspectAndRedundancy( className ) ) {
            return null;
        }

        if ( !effectiveSizes.containsKey( className ) ) {
            log.warn( "No size information available for " + className + ", skipping" );
            return null;
        }

        int effectiveGeneSetSize = effectiveSizes.get( className );
        if ( effectiveGeneSetSize == 0 || effectiveGeneSetSize < settings.getMinClassSize()
                || effectiveGeneSetSize > settings.getMaxClassSize() ) {
            if ( log.isDebugEnabled() ) log.debug( "Class " + className + " is outside of selected size range" );
            return null;
        }

        Collection<Gene> geneSetGenes = geneAnnots.getGeneSetGenes( className );

        Collection<Gene> seenGenes = new HashSet<Gene>();
        int geneSuccesses = 0;
        int probeSuccesses = 0;
        for ( Gene g : geneSetGenes ) {

            if ( !seenGenes.contains( g ) && genesAboveThreshold.contains( g ) ) {
                geneSuccesses++;
                for ( Probe p : g.getProbes() ) {
                    if ( probesAboveThreshold.contains( p ) ) {
                        probeSuccesses++;
                    }
                }
            }
            seenGenes.add( g );
        }

        assert seenGenes.size() == geneSetGenes.size();

        /*
         * 
         * Now I determine which of those gene above threshold is the most multifunctional
         */
        boolean useMultifunctionalityCorrection = this.settings.useMultifunctionalityCorrection();
        if ( useMultifunctionalityCorrection ) {
            Collection<Gene> filteredGenes = new HashSet<Gene>();
            filteredGenes.addAll( genesAboveThreshold );

            int amountOfCorrection = 2; // TEMPORARY!
            for ( int i = 0; i < amountOfCorrection; i++ ) {
                // shortcut/kludge to get multiple mf hits -- should just work with the ranked one.
                Gene mostMultifunctional = geneAnnots.getMultifunctionality().getMostMultifunctional( filteredGenes );
                filteredGenes.remove( mostMultifunctional );

                // Remove just one.
                if ( genesAboveThreshold.contains( mostMultifunctional ) ) {
                    geneSuccesses--;
                    probeSuccesses -= mostMultifunctional.getProbes().size();
                }

                if ( geneSuccesses == 0 ) {
                    break;
                }
            }
        }

        int successes = settings.getUseWeights() ? geneSuccesses : probeSuccesses;

        int numGenes = settings.getUseWeights() ? geneScores.getGeneToScoreMap().size() : geneScores
                .getProbeToScoreMap().size();

        int numOverThreshold = settings.getUseWeights() ? this.getNumGenesOverThreshold() : this
                .getNumProbesOverThreshold();

        return computeResult( className, numGenes, effectiveGeneSetSize, successes, numOverThreshold );

    }

    /**
     * Hypergeometric p value calculation (or binomial approximation) successes=number of genes in class which meet
     * criteria (successes)
     * 
     * @param (or probes)
     * @param
     * @param
     */
    private GeneSetResult computeResult( GeneSetTerm className, int numGenes, int effectiveGeneSetSize, int successes,
            int numOverThreshold ) {

        double oraPval = Double.NaN;

        if ( successes > 0 ) {
            oraPval = 0.0;

            for ( int i = successes; i <= Math.min( numOverThreshold, effectiveGeneSetSize ); i++ ) {
                oraPval += SpecFunc.dhyper( i, effectiveGeneSetSize, numGenes - effectiveGeneSetSize, numOverThreshold );
            }

            if ( Double.isNaN( oraPval ) ) {
                // binomial approximation
                double pos_prob = ( double ) effectiveGeneSetSize / ( double ) numGenes;

                oraPval = 0.0;
                for ( int i = successes; i <= Math.min( numOverThreshold, effectiveGeneSetSize ); i++ ) {
                    oraPval += SpecFunc.dbinom( i, numOverThreshold, pos_prob );
                }
            }

            if ( log.isDebugEnabled() )
                log.debug( className + " ingroupoverthresh=" + successes + " setsize=" + effectiveGeneSetSize
                        + " totalinputsize=" + numGenes + " totaloverthresh=" + numOverThreshold + " oraP="
                        + String.format( "%.2g", oraPval ) );
        } else {
            oraPval = 1.0;
        }

        GeneSetResult res = new GeneSetResult( className, actualSizes.get( className ).intValue(), effectiveGeneSetSize );
        res.setScore( successes );
        res.setPValue( oraPval );
        return res;
    }
}