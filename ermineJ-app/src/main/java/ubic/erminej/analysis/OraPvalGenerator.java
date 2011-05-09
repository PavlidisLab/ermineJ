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
import java.util.HashSet;
import java.util.Map;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.math.SpecFunc;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;
import cern.jet.math.Arithmetic;

/**
 * Compute gene set scores based on over-representation analysis (ORA).
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class OraPvalGenerator extends AbstractGeneSetPvalGenerator {

    protected double geneScoreThreshold;
    protected int inputSize;
    protected int numOverThreshold = 0;
    protected int numUnderThreshold = 0;

    /**
     * @param settings
     * @param a
     * @param csc
     * @param not
     * @param nut
     * @param gon
     * @param inputSize
     */
    public OraPvalGenerator( Settings settings, GeneAnnotations a, GeneSetSizeComputer csc, int not, int nut,
            GONames gon, int inputSize ) {

        super( settings, a, csc, gon );
        this.numOverThreshold = not;
        this.numUnderThreshold = nut;
        this.inputSize = inputSize;

        if ( settings.getUseLog() ) {
            this.geneScoreThreshold = -Arithmetic.log10( settings.getPValThreshold() );
        } else {
            this.geneScoreThreshold = settings.getPValThreshold();
        }
    }

    /**
     * Get results for one class, based on class id. The other arguments are things that are not constant under
     * permutations of the data.
     */
    public GeneSetResult classPval( String className, Map<String, Double> geneToScoreMap,
            Map<String, Double> probesToScores ) {

        if ( !super.checkAspect( className ) ) {
            if ( log.isDebugEnabled() ) log.debug( className + " is not in a selected aspect" );
            return null;
        }
        // inputs for hypergeometric distribution
        int successes = 0;
        int failures = 0;

        // variables for outputs
        double oraPval = Double.NaN;

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

        Collection<String> probes = geneAnnots.getGeneSetProbes( className );

        /*
         * Only count genes once!
         */
        Collection<String> seenGenes = new HashSet<String>();

        for ( String probe : probes ) {
            ifInterruptedStop();

            if ( !probesToScores.containsKey( probe ) ) {
                continue;
            }

            if ( settings.getUseWeights() ) {

                String geneName = geneAnnots.getProbeToGeneMap().get( probe );

                if ( !geneToScoreMap.containsKey( geneName ) ) {
                    continue;
                }

                if ( seenGenes.contains( geneName ) ) {
                    continue;
                }

                Double geneScore = geneToScoreMap.get( geneName );

                if ( geneScore == null ) {
                    log.warn( "Null gene score for " + probe );
                    continue;
                }

                if ( scorePassesThreshold( geneScore, geneScoreThreshold ) ) {
                    /*
                     * if ( log.isDebugEnabled() ) { log.debug( className + " " + probe + " " + geneScore + " beats " +
                     * geneScoreThreshold ); }
                     */
                    successes++;
                } else {
                    failures++;
                }

                seenGenes.add( geneName );
            } else {

                /*
                 * pvalue for this probe. This will not be null if things have been done correctly so far. This is the
                 * only place we need the raw pvalue for a probe.
                 */
                Double score = probesToScores.get( probe );

                if ( scorePassesThreshold( score, geneScoreThreshold ) ) {
                    // if ( log.isDebugEnabled() ) log.debug( probe + " " + score + " beats " + geneScoreThreshold );
                    successes++;
                } else {
                    failures++;
                }

            }
        } // end of while over items in the class.

        // Hypergeometric p value calculation (or binomial approximation)
        // successes=number of genes in class which meet criteria
        // (successes); numOverThreshold= number of genes which
        // meet criteria (trials); pos_prob: fractional size of
        // class wrt data size.

        oraPval = 0.0;
        for ( int i = successes; i <= Math.min( numOverThreshold, effectiveGeneSetSize ); i++ ) {
            oraPval += SpecFunc.dhyper( i, effectiveGeneSetSize, inputSize - effectiveGeneSetSize, numOverThreshold );
        }

        log.debug( className + " ingroupoverthresh=" + successes + " setsize=" + effectiveGeneSetSize
                + " totalinputsize=" + inputSize + " totaloverthresh=" + numOverThreshold + " oraP="
                + String.format( "%.2g", oraPval ) );

        if ( Double.isNaN( oraPval ) ) {
            double pos_prob = ( double ) effectiveGeneSetSize / ( double ) inputSize;

            oraPval = 0.0;
            for ( int i = successes; i <= Math.min( numOverThreshold, effectiveGeneSetSize ); i++ ) {
                oraPval += SpecFunc.dbinom( i, numOverThreshold, pos_prob );
            }
        }

        // set up the return object.

        String nameForId = className;
        if ( goName != null ) {
            nameForId = goName.getNameForId( className );
        }
        GeneSetResult res = new GeneSetResult( className, nameForId, actualSizes.get( className ).intValue(),
                effectiveGeneSetSize );
        res.setScore( successes );
        res.setPValue( oraPval );
        return res;

    }
}