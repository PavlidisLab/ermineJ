/*
 * The ermineJ project
 * 
 * Copyright (c) 2006-2011 University of British Columbia
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ubic.basecode.math.ROC;
import ubic.basecode.math.Rank;
import ubic.basecode.util.StatusViewer;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Probe;

/**
 * Compute gene set p values based on the receiver-operator characterisic (ROC).
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class RocPvalGenerator extends AbstractGeneSetPvalGenerator {

    private int totalSize = 0;

    StatusViewer messenger;

    Map<Gene, Integer> geneRanks;

    public RocPvalGenerator( SettingsHolder set, GeneAnnotations an, StatusViewer messenger ) {
        super( set, an );
        this.messenger = messenger;
    }

    /**
     * Generate a complete set of class results.
     */
    public Map<GeneSetTerm, GeneSetResult> classPvalGenerator( GeneScores geneScores1 ) {
        Map<GeneSetTerm, GeneSetResult> results = new HashMap<GeneSetTerm, GeneSetResult>();
        int count = 0;

        Map<Gene, Double> geneToScoreMap = geneScores1.getGeneToScoreMap();

        if ( settings.useMultifunctionalityCorrection() ) {
            geneToScoreMap = this.geneAnnots.getMultifunctionality().adjustScores( geneScores1 );

        }

        geneRanks = Rank.rankTransform( geneToScoreMap );

        totalSize = geneRanks.size();

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getNonEmptyGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();
            GeneSetTerm className = iter.next();
            GeneSetResult res = this.classPval( className );
            if ( res != null ) {
                results.put( className, res );
                count++;
            }

            if ( messenger != null && count % ALERT_UPDATE_FREQUENCY == 0 ) {
                messenger.showStatus( count + " gene sets analyzed" );
            }
        }
        return results;
    }

    /**
     * Get results for one class, based on class id.
     * 
     * @param geneSet name of the gene set to be tested.
     * @param probesToPvals (or genesToPvals, if using weights)
     * @param rankMap Ranks of all genes (if using weights) or probes.
     * @return a GeneSetResult
     */
    private GeneSetResult classPval( GeneSetTerm geneSet ) {
        if ( !super.checkAspectAndRedundancy( geneSet ) ) return null;
        // variables for outputs
        List<Integer> targetRanks = new ArrayList<Integer>();

        int numGenesInSet = numGenesInSet( geneSet );
        if ( numGenesInSet < settings.getMinClassSize() || numGenesInSet > settings.getMaxClassSize() ) {
            return null;
        }

        Collection<Probe> values = geneAnnots.getGeneSetProbes( geneSet );

        boolean invert = ( settings.getDoLog() && !settings.getBigIsBetter() )
                || ( !settings.getDoLog() && settings.getBigIsBetter() );

        Collection<Gene> seenGenes = new HashSet<Gene>();
        for ( Probe p : values ) {

            Integer rank;
            Gene g = p.getGene();
            if ( seenGenes.contains( g ) ) continue;
            rank = geneRanks.get( g );

            if ( rank == null ) continue;

            /* if the values are log-transformed, and bigger is not better, we need to invert the rank */
            if ( invert ) {
                rank = totalSize - rank;
                assert rank >= 0;
            }

            targetRanks.add( new Integer( rank + 1 ) ); // make ranks 1-based.

        }

        double areaUnderROC = ROC.aroc( totalSize, targetRanks );
        double roc_pval = ROC.rocpval( totalSize, targetRanks );

        GeneSetResult res = new GeneSetResult( geneSet, numProbesInSet( geneSet ), numGenesInSet );
        res.setScore( areaUnderROC );
        res.setPValue( roc_pval );
        return res;

    }

}