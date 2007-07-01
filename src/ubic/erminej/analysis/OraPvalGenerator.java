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
import java.util.Map;

import ubic.basecode.math.SpecFunc;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import cern.jet.math.Arithmetic;
import cern.jet.stat.Probability;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;

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
    public GeneSetResult classPval( String className, Map geneToScoreMap, Map probesToScores ) {

        if ( !super.checkAspect( className ) ) {
            if ( log.isDebugEnabled() ) log.debug( className + " is not in a selected aspect" );
            return null;
        }
        // inputs for hypergeometric distribution
        int successes = 0;
        int failures = 0;

        // variables for outputs
        double oraPval = -1.0;

        if ( !effectiveSizes.containsKey( className ) ) {
            log.warn( "No size information available for " + className + ", skipping" );
            return null;
        }

        int effectiveGeneSetSize = ( ( Integer ) effectiveSizes.get( className ) ).intValue();
        if ( effectiveGeneSetSize < settings.getMinClassSize() || effectiveGeneSetSize > settings.getMaxClassSize() ) {
            if ( log.isDebugEnabled() ) log.debug( "Class " + className + " is outside of selected size range" );
            return null;
        }

        Collection probes = geneAnnots.getGeneSetProbes( className );
        Iterator classit = probes.iterator();

        // store pvalues for items in the class.
        double[] groupPvalArr = new double[effectiveGeneSetSize];
        Map record = new HashMap();
        int v_size = 0;

        while ( classit.hasNext() ) {
            ifInterruptedStop();
            String probe = ( String ) classit.next(); // probe id

            if ( probesToScores.containsKey( probe ) ) {
                if ( settings.getUseWeights() ) {

                    if ( !record.containsKey( geneAnnots.getProbeToGeneMap().get( probe ) ) ) {
                        record.put( geneAnnots.getProbeToGeneMap().get( probe ), null );

                        if ( !geneToScoreMap.containsKey( geneAnnots.getProbeToGeneMap().get( probe ) ) ) {
                            throw new NullPointerException( "No gene score for " + probe );
                        }

                        Double geneScore = ( Double ) geneToScoreMap.get( geneAnnots.getProbeToGeneMap().get( probe ) );

                        if ( geneScore == null ) {
                            throw new NullPointerException( "Null gene score for " + probe );
                        }

                        groupPvalArr[v_size] = geneScore.doubleValue();
                        double rawGeneScore = groupPvalArr[v_size];
                        if ( scorePassesThreshold( rawGeneScore, geneScoreThreshold ) ) {
                            successes++;
                        } else {
                            failures++;
                        }
                        v_size++;
                    }

                } else { // no weights

                    /*
                     * pvalue for this probe. This will not be null if things have been done correctly so far. This is
                     * the only place we need the raw pvalue for a probe.
                     */
                    Double pbpval = ( Double ) probesToScores.get( probe );

                    double score = pbpval.doubleValue();

                    // hypergeometric pval info.
                    if ( scorePassesThreshold( score, geneScoreThreshold ) ) {
                        successes++;
                    } else {
                        failures++;
                    }

                }
            } // if in data set
        } // end of while over items in the class.

        // Hypergeometric p value calculation (or binomial approximation)
        // successes=number of genes in class which meet criteria
        // (successes); numOverThreshold= number of genes which
        // meet criteria (trials); pos_prob: fractional size of
        // class wrt data size.
        double pos_prob = ( double ) effectiveGeneSetSize / ( double ) inputSize;
        double expected = numOverThreshold * pos_prob;

        // System.err.println( successes + ", " + effectiveGeneSetSize + ", "
        // + ( inputSize - effectiveGeneSetSize ) + ", "
        // + numOverThreshold );

        if ( successes < expected || pos_prob == 0.0 ) { // fewer than expected,
            // still do upper tail - to be consistent with other methods.

            // successes, positives, negatives, trials
            oraPval = SpecFunc.phyper( successes, effectiveGeneSetSize, inputSize - effectiveGeneSetSize,
                    numOverThreshold, false );

            if ( Double.isNaN( oraPval ) ) {
                oraPval = Probability.binomialComplemented( successes, numOverThreshold, pos_prob );
            }

        } else {

            oraPval = SpecFunc.phyper( successes, effectiveGeneSetSize, inputSize - effectiveGeneSetSize,
                    numOverThreshold, false );

            if ( Double.isNaN( oraPval ) ) {
                oraPval = Probability.binomialComplemented( successes, numOverThreshold, pos_prob );
            }
        }

        // set up the return object.

        String nameForId = className;
        if ( goName != null ) {
            nameForId = goName.getNameForId( className );
        }
        GeneSetResult res = new GeneSetResult( className, nameForId, ( ( Integer ) actualSizes.get( className ) )
                .intValue(), effectiveGeneSetSize );
        res.setScore( successes );
        res.setPValue( oraPval );
        return res;

    }
}