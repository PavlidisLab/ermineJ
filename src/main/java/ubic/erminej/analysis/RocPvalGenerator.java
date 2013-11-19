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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ubic.basecode.math.ROC;
import ubic.basecode.util.StatusViewer;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;

/**
 * Compute gene set p values based on the receiver operating characteristic (ROC).
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class RocPvalGenerator extends AbstractGeneSetPvalGenerator {

    public RocPvalGenerator( SettingsHolder set, GeneAnnotations an, Map<Gene, Double> geneToScoreMap,
            StatusViewer messenger ) {
        super( set, an, geneToScoreMap, messenger );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.erminej.analysis.AbstractGeneSetPvalGenerator#generateGeneSetResults()
     */
    @Override
    public Map<GeneSetTerm, GeneSetResult> generateGeneSetResults() {
        boolean useMultifunctionalityCorrection = settings.useMultifunctionalityCorrection();

        Map<GeneSetTerm, GeneSetResult> results = generateGeneSetResults( useMultifunctionalityCorrection );

        return results;
    }

    /**
     * @param useMultifunctionalityCorrection
     * @return
     */
    protected Map<GeneSetTerm, GeneSetResult> generateGeneSetResults( boolean useMultifunctionalityCorrection ) {
        Map<GeneSetTerm, GeneSetResult> results = new HashMap<GeneSetTerm, GeneSetResult>();
        int count = 0;

        this.numGenesUsed = geneToScoreMap.size();

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getGeneSetTerms().iterator(); iter.hasNext(); ) {
            GeneSetTerm className = iter.next();
            GeneSetResult res = this.classPval( className );
            if ( res != null ) {
                results.put( className, res );
                if ( ++count % ALERT_UPDATE_FREQUENCY == 0 ) {
                    getMessenger().showProgress( count + " gene sets analyzed" );
                    ifInterruptedStop();

                }
            }
        }
        if ( results.isEmpty() ) return results;
        GeneSetPvalRun.populateRanks( results );

        if ( useMultifunctionalityCorrection ) {
            Map<Gene, Double> adjustScores = this.geneAnnots.getMultifunctionality().adjustScores( geneToScoreMap,
                    false /* not ranks */, false /* unweighted regression */);
            RocPvalGenerator rpg = new RocPvalGenerator( settings, geneAnnots, adjustScores, messenger );
            Map<GeneSetTerm, GeneSetResult> mfCorrectedResults = rpg.generateGeneSetResults( false );

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

        int numGenesInSet = numGenesInSet( geneSet );
        if ( numGenesInSet < settings.getMinClassSize() || numGenesInSet > settings.getMaxClassSize() ) {
            return null;
        }

        Collection<Gene> genesInSet = geneAnnots.getGeneSetGenes( geneSet );

        List<Double> targetRanks = ranksOfGenesInSet( genesInSet );

        int totalSize = geneToScoreMap.size();
        double areaUnderROC = ROC.aroc( totalSize, targetRanks );
        double roc_pval = ROC.rocpval( totalSize, targetRanks );

        GeneSetResult res = new GeneSetResult( geneSet, numProbesInSet( geneSet ), numGenesInSet, settings );
        res.setScore( areaUnderROC );
        res.setPValue( roc_pval );
        return res;

    }

}