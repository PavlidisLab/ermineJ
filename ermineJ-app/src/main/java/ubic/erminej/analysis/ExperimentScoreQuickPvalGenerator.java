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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;

/**
 * Does the same thing as {@link ExperimentScorePvalGenerator}but is stripped-down for using during resampling.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class ExperimentScoreQuickPvalGenerator extends ExperimentScorePvalGenerator {

    public ExperimentScoreQuickPvalGenerator( SettingsHolder settings, GeneAnnotations a, Histogram hi ) {
        super( settings, a, hi );
    }

    /**
     * This is stripped-down version of classPvalue. We use this when doing permutations, it is much faster.
     * 
     * @param class_name String
     * @param group_pval_map Map
     * @param probesToPvals Map
     * @throws IllegalStateException
     * @return double
     */
    public double classPvalue( GeneSetTerm geneSetName, Map<Gene, Double> genePvalueMap ) {

        double pval = 0.0;
        double rawscore = 0.0;

        if ( !super.checkAspectAndRedundancy( geneSetName ) ) return -1.0;

        int numGenesInSet = numGenesInSet( geneSetName );
        if ( numGenesInSet < settings.getMinClassSize() || numGenesInSet > settings.getMaxClassSize() ) {
            return -1.0;
        }

        double[] scoresForGenesInSet = new double[numGenesInSet]; // store pvalues for items in
        // the class.
        Set<Gene> record = new HashSet<Gene>();

        int v_size = 0;

        for ( Gene gene : this.geneAnnots.getGeneSetGenes( geneSetName ) ) {

            Double grouppval = genePvalueMap.get( gene );
            if ( !record.contains( gene ) ) {
                record.add( gene );
                scoresForGenesInSet[v_size] = grouppval.doubleValue();
                v_size++;
            }
        }

        rawscore = GeneSetResamplingBackgroundDistributionGenerator.computeRawScore( scoresForGenesInSet, settings
                .getGeneSetResamplingScoreMethod() );
        pval = scoreToPval( numGenesInSet, rawscore );

        if ( pval < 0 ) {
            throw new IllegalStateException( "Warning, a rawscore yielded an invalid pvalue: Classname: " + geneSetName );
        }
        return pval;
    }

}