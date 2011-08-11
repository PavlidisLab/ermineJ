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
import java.util.Map;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;

/**
 * Generates gene set p values using the resampling-based 'experiment score' method of Pavlidis et al. 2002
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class ExperimentScorePvalGenerator extends AbstractGeneSetPvalGenerator {

    Histogram hist;

    /**
     * @param settings
     * @param a
     * @param hi null distributions
     */
    public ExperimentScorePvalGenerator( SettingsHolder settings, GeneAnnotations a, Histogram hi ) {
        super( settings, a );
        this.hist = hi;
    }

    /**
     * Get results for one class, based on class id. The other arguments are things that are not constant under
     * permutations of the data.
     * 
     * @param geneSetName
     * @param groupToPvalMap
     * @param probesToPvals
     * @return
     */
    public GeneSetResult classPval( GeneSetTerm geneSetName, Map<Gene, Double> geneToScoreMap ) {
        if ( !super.checkAspectAndRedundancy( geneSetName ) ) return null;

        int numGenesInSet = numGenesInSet( geneSetName );

        if ( numGenesInSet < settings.getMinClassSize() || numGenesInSet > settings.getMaxClassSize() ) {
            return null;
        }

        Collection<Gene> values = geneAnnots.getGeneSetGenes( geneSetName );

        // store pvalues for items in the class.
        double[] groupPvalArr = new double[numGenesInSet];

        int v_size = 0;

        // foreach item in the class.
        for ( Gene gene : values ) {
            // if it is in the data set. This is invariant under permutations.
            if ( geneToScoreMap.containsKey( gene ) ) {
                groupPvalArr[v_size] = geneToScoreMap.get( gene );
                v_size++;
            } // if in data set
        }

        // get raw score and pvalue.
        double rawscore = GeneSetResamplingBackgroundDistributionGenerator.computeRawScore( groupPvalArr, settings
                .getGeneSetResamplingScoreMethod() );

        double pval = scoreToPval( numGenesInSet, rawscore );

        if ( pval < 0.0 ) {
            throw new IllegalStateException( "A raw score (" + rawscore + ") yielded an invalid pvalue: Classname: "
                    + geneSetName );
        }

        // set up the return object.
        GeneSetResult res = new GeneSetResult( geneSetName, numProbesInSet( geneSetName ), numGenesInSet( geneSetName ) );
        res.setScore( rawscore );
        res.setPValue( pval );
        return res;

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