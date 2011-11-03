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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ubic.basecode.math.SpecFunc;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.EmptyGeneSetResult;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import cern.jet.math.Arithmetic;

/**
 * Compute gene set scores based on over-representation analysis (ORA).
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class OraPvalGenerator extends AbstractGeneSetPvalGenerator {

    protected double geneScoreThreshold;

    private GeneScores geneScores;

    private Collection<Gene> genesAboveThreshold = new HashSet<Gene>();

    /**
     * @param settings
     * @param a
     * @param csc
     * @param not
     * @param nut
     * @param inputSize
     */
    public OraPvalGenerator( SettingsHolder settings, GeneScores geneScores, GeneAnnotations a ) {

        super( settings, a );

        this.geneScores = geneScores;

        if ( settings.getUseLog() ) {
            this.geneScoreThreshold = -Arithmetic.log10( settings.getGeneScoreThreshold() );
        } else {
            this.geneScoreThreshold = settings.getGeneScoreThreshold();
        }

        computeCounts();

    }

    /**
     * @return Ranked list. Removes any sets which are not scored.
     */ 
    private List<GeneSetTerm> getSortedClasses( final Map<GeneSetTerm, GeneSetResult> results ) {
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
     * Generate a complete set of class results.
     * 
     * @param geneToGeneScoreMap
     * @param probesToPvals
     */
    public Map<GeneSetTerm, GeneSetResult> classPvalGenerator( Map<Gene, Double> geneToGeneScoreMap,
            StatusViewer messenger ) {
        Map<GeneSetTerm, GeneSetResult> results = new HashMap<GeneSetTerm, GeneSetResult>();

        this.numGenesUsed = geneToGeneScoreMap.size();

        int count = 0;

        boolean useMultifunctionalityCorrection = this.settings.useMultifunctionalityCorrection();
        double sigThresh = 0.01;
        double maxGroupsFraction = 0.01;
        int numMultifunctionalRemoved = 0;
        Settings.MultiTestCorrMethod multipleTestCorrMethod = settings.getMtc();
        Collection<Gene> filteredGenes = new HashSet<Gene>();
        filteredGenes.addAll( genesAboveThreshold );

        while ( true ) {

            /*
             * See bug 2290
             */

            for ( GeneSetTerm geneSetName : geneAnnots.getGeneSetTerms() ) {

                GeneSetResult res = classPval( filteredGenes, geneSetName );
                if ( res != null ) {
                    results.put( geneSetName, res );

                    if ( ++count % 100 == 0 ) ifInterruptedStop();
                    if ( messenger != null && count % ALERT_UPDATE_FREQUENCY == 0 ) {
                        messenger.showStatus( count + " gene sets analyzed" );
                    }
                }
            }

            if ( results.isEmpty() ) {
                break;
            }

            if ( !useMultifunctionalityCorrection ) {
                break;
            }

            List<GeneSetTerm> sortedClasses = getSortedClasses( results );
            MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedClasses, null, geneAnnots,
                    geneScores, results, messenger );

            if ( multipleTestCorrMethod == SettingsHolder.MultiTestCorrMethod.BONFERONNI ) {
                mt.bonferroni();
            } else if ( multipleTestCorrMethod.equals( SettingsHolder.MultiTestCorrMethod.BENJAMINIHOCHBERG ) ) {
                mt.benjaminihochberg();
            } else {
                throw new UnsupportedOperationException( multipleTestCorrMethod
                        + " is not supported for this analysis method" );
            }

            /*
             * Determine how many groups meet the threshold.
             */
            int maxGroupsToSelect = ( int ) Math.floor( results.size() * maxGroupsFraction );

            if ( maxGroupsToSelect < 1 ) {
                throw new IllegalArgumentException( "No results" );
            }

            int numSelected = 0;
            for ( GeneSetTerm t : sortedClasses ) {
                GeneSetResult r = results.get( t );
                if ( r.getCorrectedPvalue() > sigThresh ) {
                    break;
                }
                numSelected++;
            }

            if ( numSelected > maxGroupsToSelect ) {
                /*
                 * Remove a multifunctional gene and try again
                 */
                numMultifunctionalRemoved++;
                Gene mostMultifunctional = geneAnnots.getMultifunctionality().getMostMultifunctional( filteredGenes );
                log.info( "Removing " + mostMultifunctional + " (most multifunc of hits)" );
                filteredGenes.remove( mostMultifunctional );
                if ( filteredGenes.isEmpty() ) {
                    log.warn( "No genes left after remove MF genes" );
                    break;
                }
            } else {
                break;
            }
        }

        if ( numMultifunctionalRemoved > 0 ) {
            // TODO make sure the user knows about this.
            log.info( numMultifunctionalRemoved + " most multifunctional genes were removed from the selected genes" );
        }

        return results;
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

            if ( log.isDebugEnabled() )
                log.debug( className + " ingroupoverthresh=" + successes + " setsize=" + numGenesInSet
                        + " totalinputsize=" + numGenes + " totaloverthresh=" + numOverThreshold + " oraP="
                        + String.format( "%.2g", oraPval ) );
        } else {
            oraPval = 1.0;
        }

        GeneSetResult res = new GeneSetResult( className, numProbesInSet( className ), numGenesInSet );
        res.setScore( successes );
        res.setPValue( oraPval );
        return res;
    }

    /**
     * @param geneSuccesses
     * @return adjusted value
     * @deprecated, this was just a prototype
     */
    @Deprecated
    private int multiFunctionalityCorrect( int geneSuccesses ) {

        int amountOfCorrection = 2; // TEMPORARY!

        // not quite sure what to do here ...
        if ( geneSuccesses <= amountOfCorrection ) return geneSuccesses;

        int adjustedSuccesses = geneSuccesses;
        Collection<Gene> filteredGenes = new HashSet<Gene>();
        filteredGenes.addAll( genesAboveThreshold );

        for ( int i = 0; i < amountOfCorrection; i++ ) {
            // shortcut/kludge to get multiple mf hits -- should just work with the ranked one - this is a bit slow.
            Gene mostMultifunctional = geneAnnots.getMultifunctionality().getMostMultifunctional( filteredGenes );
            filteredGenes.remove( mostMultifunctional );

            // Remove just one.
            if ( genesAboveThreshold.contains( mostMultifunctional ) ) {
                adjustedSuccesses--;
            }

            if ( adjustedSuccesses == 0 ) {
                break;
            }
        }
        return adjustedSuccesses;
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
            if ( log.isDebugEnabled() ) log.debug( "Class " + className + " is outside of selected size range" );
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

        // original prototype method for Multifuncationality correction: Determine which of those gene above threshold
        // is the most multifunctional
        // boolean useMultifunctionalityCorrection = this.settings.useMultifunctionalityCorrection();
        // if ( useMultifunctionalityCorrection ) {
        // geneSuccesses = multiFunctionalityCorrect( geneSuccesses );
        // }
        // assert geneSuccesses >= 0;

        int successes = geneSuccesses;

        int numGenes = geneScores.getGeneToScoreMap().size();

        int numOverThreshold = this.getNumGenesOverThreshold();

        return computeResult( className, numGenes, numGenesInSet, successes, numOverThreshold );

    }
}