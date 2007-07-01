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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ubic.basecode.math.ROC;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;

/**
 * Compute gene set p values based on the receiver-operator characterisic (ROC).
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class RocPvalGenerator extends AbstractGeneSetPvalGenerator {

    private Map results;
    private int totalSize = 0;

    public RocPvalGenerator( Settings set, GeneAnnotations an, GeneSetSizeComputer csc, GONames gon ) {
        super( set, an, csc, gon );
        totalSize = 0;
        if ( settings.getUseWeights() ) {
            totalSize = geneAnnots.numGenes();
        } else {
            totalSize = geneAnnots.numProbes();
        }
        log.debug( totalSize + " elements in total" );
        results = new HashMap();
    }

    /**
     * Generate a complete set of class results. The arguments are not constant under pemutations.
     * 
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     */
    public void classPvalGenerator( Map genePvalueMap, Map rankMap, StatusViewer messenger ) {

        int count = 0;

        for ( Iterator iter = geneAnnots.getGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();
            String className = ( String ) iter.next();
            GeneSetResult res = this.classPval( className, genePvalueMap, rankMap );
            if ( res != null ) {
                results.put( className, res );
            }
            count++;
            if ( messenger != null && count % 200 == 0 ) {
                messenger.showStatus( count + " gene sets analyzed" );
            }
        }
    }

    /**
     * Get results for one class, based on class id.
     * 
     * @param geneSet name of the gene set to be tested.
     * @param probesToPvals (or genesToPvals, if using weights)
     * @param rankMap Ranks of all genes (if using weights) or probes.
     * @return a GeneSetResult
     */
    public GeneSetResult classPval( String geneSet, Map probesToPvals, Map rankMap ) {
        if ( !super.checkAspect( geneSet ) ) return null;
        // variables for outputs
        List targetRanks = new ArrayList();

        int effSize = ( ( Integer ) effectiveSizes.get( geneSet ) ).intValue(); // effective size of this class.
        if ( effSize < settings.getMinClassSize() || effSize > settings.getMaxClassSize() ) {
            return null;
        }

        Collection values = null;

        if ( settings.getUseWeights() ) {
            values = geneAnnots.getActiveGeneSetGenes( geneSet );
        } else {
            values = geneAnnots.getGeneSetProbes( geneSet );
        }

        Iterator classit = values.iterator();

        boolean invert = settings.getDoLog() && !settings.getBigIsBetter();

        while ( classit.hasNext() ) {
            ifInterruptedStop();
            String probe = ( String ) classit.next(); // probe id OR gene.

            if ( probesToPvals.containsKey( probe ) ) {
                Integer ranking = ( Integer ) rankMap.get( probe );
                if ( ranking == null ) continue;

                int rank = ranking.intValue();

                /* if the values are log-transformed, and bigger is not better, we need to invert the rank */
                if ( invert ) {
                    rank = totalSize - rank;
                    assert rank >= 0;
                }

                targetRanks.add( new Integer( rank + 1 ) ); // make ranks 1-based.
            }

        }

        double areaUnderROC = ROC.aroc( totalSize, targetRanks );
        double roc_pval = ROC.rocpval( totalSize, targetRanks );

        String nameForId = geneSet;
        if ( goName != null ) {
            nameForId = goName.getNameForId( geneSet );
        }
        GeneSetResult res = new GeneSetResult( geneSet, nameForId, ( ( Integer ) actualSizes.get( geneSet ) )
                .intValue(), effSize );
        res.setScore( areaUnderROC );
        res.setPValue( roc_pval );
        return res;

    }

    /**
     * @return Map the results
     */
    public Map getResults() {
        return results;
    }

}