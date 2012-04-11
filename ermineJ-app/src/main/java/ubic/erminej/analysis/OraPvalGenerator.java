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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ubic.basecode.math.SpecFunc;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import cern.colt.list.DoubleArrayList;
import cern.jet.math.Arithmetic;
import cern.jet.stat.Descriptive;

/**
 * Compute gene set scores based on over-representation analysis (ORA).
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class OraPvalGenerator extends AbstractGeneSetPvalGenerator {

    /**
     * We only deal with multifunctionality if the hit list is nominally enriched for multifunctional genes in the first
     * place. This is a p-value
     */
    private static final double MF_BIAS_TO_TRIGGER_CORRECTION = 0.05;

    /**
     * Nominal threshold for significance of groups (after multiple test correction) used for checking
     * multifunctionality effects.
     */
    private final double GROUP_SELECTION_THRESHOLD_FOR_MF_CHECK = 0.05;

    /**
     * Maximum number of gene sets which will be monitored for sensitivity to removing multifunctional genes.
     */
    private final int NUMBER_OF_RANKS_TO_INSPECT_FOR_MF_SENSITIVITY = 10;

    protected double geneScoreThreshold;

    private GeneScores geneScores;

    private Collection<Gene> genesAboveThreshold = new HashSet<Gene>();

    /**
     * @param settings
     * @param a
     * @param messenger
     * @param csc
     * @param not
     * @param nut
     * @param inputSize
     */
    public OraPvalGenerator( SettingsHolder settings, GeneScores geneScores, GeneAnnotations a, StatusViewer messenger ) {

        super( settings, a, geneScores.getGeneToScoreMap(), messenger );

        this.geneScores = geneScores;

        if ( settings.getUseLog() ) {
            this.geneScoreThreshold = -Arithmetic.log10( settings.getGeneScoreThreshold() );
        } else {
            this.geneScoreThreshold = settings.getGeneScoreThreshold();
        }

        computeCounts();

    }

    /**
     * Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution.
     * 
     * @param geneScores The pvalues for the probes (no weights) or groups (weights)
     * @return number of entries that meet the user-set threshold.
     * @todo make this private and called by OraPvalGenerator.
     */
    public void computeCounts() {

        Map<Gene, Double> probeScores = geneScores.getGeneToScoreMap();

        for ( Gene p : probeScores.keySet() ) {
            double geneScore = probeScores.get( p );

            if ( scorePassesThreshold( geneScore ) ) {
                genesAboveThreshold.add( p );
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.erminej.analysis.AbstractGeneSetPvalGenerator#generateGeneSetResults()
     */
    public Map<GeneSetTerm, GeneSetResult> generateGeneSetResults() {

        this.numGenesUsed = geneToScoreMap.size();

        int numMultifunctionalRemoved = 0;

        double hitListMultifunctionalityBiasPvalue = this.geneAnnots.getMultifunctionality()
                .enrichmentForMultifunctionalityPvalue( genesAboveThreshold );

        this.messenger.showStatus( String.format( "Hit list (%d genes) enrichment for multifunctionality: P = %.3g",
                genesAboveThreshold.size(), hitListMultifunctionalityBiasPvalue ) );

        boolean useMultifunctionalityCorrection = this.settings.useMultifunctionalityCorrection()
                && hitListMultifunctionalityBiasPvalue < MF_BIAS_TO_TRIGGER_CORRECTION;

        Map<GeneSetTerm, GeneSetResult> referenceResults = computeResultsForHitList( genesAboveThreshold, false );

        if ( referenceResults.isEmpty() || !useMultifunctionalityCorrection ) {
            this.messenger
                    .showStatus( "'Hits' are not significantly multifunctionality-biased, no multifunctionality correction needed" );
            return referenceResults;
        }

        List<GeneSetTerm> sortedClasses = getSortedClasses( referenceResults );
        multipleTestCorrect( sortedClasses, referenceResults );

        Map<GeneSetTerm, Double> monitoredRanks = getMFMonitoredSets( referenceResults, sortedClasses );

        if ( monitoredRanks.isEmpty() ) {
            this.messenger.showStatus( "Insufficient enrichment found, skipping multifunctionality correction" );
            return referenceResults;
        }

        multifunctionalityCorrect( referenceResults, monitoredRanks, numMultifunctionalRemoved );

        return referenceResults;
    }

    /**
     * Algorithm as described by JG -- the thresholds etc. described are just examples.
     * 
     * <pre>
     * A=set of genes (unordered) [hit list]
     * erminer=function that returns list of p-values for enrichment of A in a given ontology
     * 
     * p_vals=erminer(A,ontology);
     * 
     * rank_of_p_vals is the p_vals with its values replaced by ranks (e.g., lowest is 1)
     * 
     * counter=0;
     * 
     * while at least one p_val is less than 0.05 and A is significantly enriched for multifunctional genes
     *   counter=counter+1; %number of genes to be removed
     * 
     *   A_2=A;
     * 
     *   A_2 has its most multifunctional gene removed (as defined by ontology)
     * 
     *   p_vals2=erminer(A2,ontology)
     * 
     *   score(counter)= (average rank in p_vals2 of (rank_of_p_vals <=10 & p_vals<0.05)) minus average rank in p_vals of (rank_of_p_vals<=10 & p_vals<0.05)
     *   % basically, calculate the new ranks of the previously top 10 functions (or the subset which are significant); the part subtracted is just in case of ties or not a full 10
     * 
     *   A=A_2;
     *   p_vals=p_vals2;
     * end
     * 
     * Find counter associated with maximum score and remove that many genes as correction.  
     * If the original list was not enriched for multifunctional genes (using ROC method), no correction should be made.
     * </pre>
     * 
     * @param referenceResults
     * @param monitoredRanks
     * @param numMultifunctionalRemoved
     * @author PP, JG (algorithm)
     * @since 3.0
     */
    private void multifunctionalityCorrect( Map<GeneSetTerm, GeneSetResult> referenceResults,
            Map<GeneSetTerm, Double> monitoredRanks, int numMultifunctionalRemoved ) {
        Map<GeneSetTerm, Double> previousRanks = new HashMap<GeneSetTerm, Double>();

        Collection<Gene> filteredGenes = new HashSet<Gene>();
        filteredGenes.addAll( genesAboveThreshold );

        double smax = -1.0;
        int numMfToRemove = 0;
        previousRanks.putAll( monitoredRanks );
        double hitListMultifunctionalityBiasPvalue = this.geneAnnots.getMultifunctionality()
                .enrichmentForMultifunctionalityPvalue( filteredGenes );

        List<GeneSetTerm> correctedRanking = new ArrayList<GeneSetTerm>();

        if ( hitListMultifunctionalityBiasPvalue > MF_BIAS_TO_TRIGGER_CORRECTION ) {
            // this is a redundant check
            this.messenger
                    .showStatus( "'Hits' are not significantly multifunctionality-biased, no multifunctionality correction needed" );
            numMultifunctionalRemoved = 0;
            return;
        }

        this.messenger.showStatus( String.format(
                "Before correction enrichment of hit list (%d genes) for multifunctionality is P=%.3g",
                filteredGenes.size(), hitListMultifunctionalityBiasPvalue ) );

        while ( true ) {

            /*
             * Remove a multifunctional gene and recompute
             */
            numMultifunctionalRemoved++;
            Gene mostMultifunctional = geneAnnots.getMultifunctionality().getMostMultifunctional( filteredGenes );
            this.messenger.showStatus( "MF correct: Testing removal of " + mostMultifunctional.getSymbol()
                    + " (most multifunc of hits)" );
            filteredGenes.remove( mostMultifunctional );
            if ( filteredGenes.isEmpty() ) {
                this.messenger.showWarning( "No genes left after remove MF genes" );
                break;
            }

            // compute new results. FIXME only do this for groups the removed gene was in!
            Map<GeneSetTerm, GeneSetResult> results = computeResultsForHitList( filteredGenes, true );
            if ( results.isEmpty() ) {
                break;
            }
            List<GeneSetTerm> sortedRevisedClasses = getSortedClasses( results );
            multipleTestCorrect( sortedRevisedClasses, results );

            // get the new ranks for the monitored set of gene sets.
            Map<GeneSetTerm, Double> newRanks = new HashMap<GeneSetTerm, Double>();
            int index = 0;
            for ( GeneSetTerm t : sortedRevisedClasses ) {
                if ( monitoredRanks.containsKey( t ) ) {
                    double rank = ( index + 1 ) / sortedRevisedClasses.size();
                    newRanks.put( t, rank );
                }
                index++;
            }

            /*
             * see how much the groups change.
             */
            double s = score( previousRanks, newRanks );
            if ( s > smax ) {
                smax = s;
                numMfToRemove = numMultifunctionalRemoved;
                // We could keep these results.
                correctedRanking = sortedRevisedClasses;
            }
            previousRanks.clear();
            previousRanks.putAll( newRanks );

            /*
             * Stop if our list is no longer multifunctionality-biased.
             */
            hitListMultifunctionalityBiasPvalue = this.geneAnnots.getMultifunctionality()
                    .enrichmentForMultifunctionalityPvalue( filteredGenes );
            this.messenger.showStatus( String.format( "After removing " + numMultifunctionalRemoved
                    + " genes, enrichment of hit list (%d genes) for multifunctionality is P=%.3g",
                    filteredGenes.size(), hitListMultifunctionalityBiasPvalue ) );
            if ( hitListMultifunctionalityBiasPvalue >= MF_BIAS_TO_TRIGGER_CORRECTION ) {
                break;
            }
        }
        // Done figuring out the threshold.

        if ( numMfToRemove > 0 ) {
            this.messenger.showStatus( "Computing multifunctionality effect" );
            /*
             * Now we do the final correction.
             */
            assert correctedRanking != null && !correctedRanking.isEmpty();

            for ( int i = 0; i < correctedRanking.size(); i++ ) {
                GeneSetTerm geneSetTerm = correctedRanking.get( i );
                int correctedRank = i + 1;
                int originalRank = referenceResults.get( geneSetTerm ).getRank();
                referenceResults.get( geneSetTerm ).setMultifunctionalityCorrectedRankDelta(
                        correctedRank - originalRank );
            }

        }
    }

    /**
     * Get the classes we are using as a reference.
     * 
     * @param referenceResults
     * @param sortedClasses
     * @return
     */
    private Map<GeneSetTerm, Double> getMFMonitoredSets( Map<GeneSetTerm, GeneSetResult> referenceResults,
            List<GeneSetTerm> sortedClasses ) {

        Map<GeneSetTerm, Double> monitoredRanks = new HashMap<GeneSetTerm, Double>();
        int numSelected = 0;
        for ( GeneSetTerm t : sortedClasses ) {
            GeneSetResult r = referenceResults.get( t );

            double relativeRank = numSelected / sortedClasses.size();
            if ( r.getCorrectedPvalue() < GROUP_SELECTION_THRESHOLD_FOR_MF_CHECK
                    && monitoredRanks.size() < NUMBER_OF_RANKS_TO_INSPECT_FOR_MF_SENSITIVITY ) {
                monitoredRanks.put( t, relativeRank );
            }
            referenceResults.get( t ).setRank( numSelected + 1 );
            referenceResults.get( t ).setRelativeRank( relativeRank );
            numSelected++;
        }
        return monitoredRanks;
    }

    /**
     * @param histList
     * @return
     */
    private Map<GeneSetTerm, GeneSetResult> computeResultsForHitList( Collection<Gene> histList, boolean quiet ) {
        int count = 0;
        Map<GeneSetTerm, GeneSetResult> results = new HashMap<GeneSetTerm, GeneSetResult>();
        for ( GeneSetTerm geneSetName : geneAnnots.getGeneSetTerms() ) {
            GeneSetResult res = classPval( histList, geneSetName );
            if ( res != null ) {
                results.put( geneSetName, res );
                if ( ++count % 100 == 0 ) ifInterruptedStop();
                if ( !quiet ) {
                    if ( getMessenger() != null && count % ALERT_UPDATE_FREQUENCY == 0 ) {
                        getMessenger().showStatus( count + " gene sets analyzed" );
                    }
                }
            }
        }

        if ( results.isEmpty() ) return results;

        populateRanks( results );
        return results;
    }

    /**
     * Compute the change in the ranks for the selected terms.
     * 
     * <pre>
     * s = mean(new ranks) - mean(old ranks)
     * </pre>
     * 
     * @param previousRanks
     * @param monitoredRanks
     * @return s
     */
    private double score( Map<GeneSetTerm, Double> oldRanks, Map<GeneSetTerm, Double> newRanks ) {
        assert newRanks.size() <= oldRanks.size();

        DoubleArrayList oldR = new DoubleArrayList();
        DoubleArrayList newR = new DoubleArrayList();

        for ( GeneSetTerm k : oldRanks.keySet() ) {
            oldR.add( oldRanks.get( k ) );
            Double double1 = newRanks.get( k );
            assert double1 != null;
            newR.add( double1 );
        }

        return Descriptive.mean( newR ) - Descriptive.mean( oldR );
    }

    /**
     * Perform multiple test correction during the multifunctionality correction.
     * 
     * @param sortedClasses
     * @param results
     */
    private void multipleTestCorrect( List<GeneSetTerm> sortedClasses, Map<GeneSetTerm, GeneSetResult> results ) {
        MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedClasses, geneAnnots, geneScores, results,
                getMessenger() );
        Settings.MultiTestCorrMethod multipleTestCorrMethod = settings.getMtc();
        if ( multipleTestCorrMethod.equals( SettingsHolder.MultiTestCorrMethod.BONFERONNI ) ) {
            mt.bonferroni();
        } else if ( multipleTestCorrMethod.equals( SettingsHolder.MultiTestCorrMethod.BENJAMINIHOCHBERG ) ) {
            mt.benjaminihochberg();
        } else {
            throw new UnsupportedOperationException( multipleTestCorrMethod
                    + " is not supported for this analysis method" );
        }
    }

    public Collection<Gene> getGenesAboveThreshold() {
        return genesAboveThreshold;
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

    /**
     * Always for genes.
     * 
     * @return
     */
    public int getNumGenesUnderThreshold() {
        return geneScores.getGeneToScoreMap().size() - getNumGenesOverThreshold();
    }

    /**
     * Get results for one class, based on class id. The other arguments are things that are not constant under
     * permutations of the data.
     */
    protected GeneSetResult classPval( Collection<Gene> genesAboveThresh, GeneSetTerm className ) {

        if ( !super.checkAspectAndRedundancy( className ) ) {
            return null;
        }

        int numGenesInSet = numGenesInSet( className );
        if ( numGenesInSet == 0 || numGenesInSet < settings.getMinClassSize()
                || numGenesInSet > settings.getMaxClassSize() ) {
            // if ( log.isDebugEnabled() ) log.debug( "Class " + className + " is outside of selected size range" );
            return null;
        }

        Collection<Gene> geneSetGenes = geneAnnots.getGeneSetGenes( className );

        Collection<Gene> seenGenes = new HashSet<Gene>();
        int geneSuccesses = 0;
        for ( Gene g : geneSetGenes ) {

            if ( !seenGenes.contains( g ) && genesAboveThresh.contains( g ) ) {
                geneSuccesses++;
            }
            seenGenes.add( g );
        }

        assert seenGenes.size() == geneSetGenes.size();
        assert geneSuccesses >= 0;

        int successes = geneSuccesses;

        int numGenes = geneScores.getGeneToScoreMap().size();

        int numOverThreshold = this.getNumGenesOverThreshold();

        return computeResult( className, numGenes, numGenesInSet, successes, numOverThreshold );

    }

    /**
     * Hypergeometric p value calculation (or binomial approximation) successes=number of genes in class which meet
     * criteria
     * 
     * @param clasName
     * @param total number of genes (or probes)
     * @param number of genes in the set (or the number of probes)
     * @param how many passed the threshold
     */
    private GeneSetResult computeResult( GeneSetTerm className, int numGenes, int numGenesInSet, int successes,
            int numOverThreshold ) {

        double oraPval = Double.NaN;

        if ( successes > 0 ) {
            oraPval = 0.0;

            // sum probs of N or more successes up to max possible.
            for ( int i = successes; i <= Math.min( numOverThreshold, numGenesInSet ); i++ ) {
                oraPval += SpecFunc.dhyper( i, numGenesInSet, numGenes - numGenesInSet, numOverThreshold );
            }

            if ( Double.isNaN( oraPval ) ) {
                // binomial approximation
                double pos_prob = ( double ) numGenesInSet / ( double ) numGenes;

                oraPval = 0.0;
                for ( int i = successes; i <= Math.min( numOverThreshold, numGenesInSet ); i++ ) {
                    oraPval += SpecFunc.dbinom( i, numOverThreshold, pos_prob );
                }
            }

            // if ( log.isDebugEnabled() )
            // log.debug( className + " ingroupoverthresh=" + successes + " setsize=" + numGenesInSet
            // + " totalinputsize=" + numGenes + " totaloverthresh=" + numOverThreshold + " oraP="
            // + String.format( "%.2g", oraPval ) );
        } else {
            oraPval = 1.0;
        }

        GeneSetResult res = new GeneSetResult( className, numProbesInSet( className ), numGenesInSet, settings );
        res.setScore( successes );
        res.setPValue( oraPval );
        return res;
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
}