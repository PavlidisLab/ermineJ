package classScore.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.math.ROC;
import baseCode.math.Rank;
import classScore.Settings;
import classScore.data.GeneSetResult;

/**
 * Compute gene set p values based on the receiver-operator characterisic (ROC).
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class RocPvalGenerator extends AbstractGeneSetPvalGenerator {

    private Map results;

    public RocPvalGenerator( Settings set, GeneAnnotations an, GeneSetSizeComputer csc, GONames gon ) {
        super( set, an, csc, gon );
        results = new HashMap();
    }

    /**
     * Generate a complete set of class results. The arguments are not constant under pemutations.
     * 
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     */
    public void classPvalGenerator( Map genePvalueMap, Map rankMap ) {

        for ( Iterator iter = geneAnnots.getGeneSetToProbeMap().keySet().iterator(); iter.hasNext(); ) {
            if ( isInterrupted() ) {
                break;
            }
            String className = ( String ) iter.next();
            GeneSetResult res = this.classPval( className, genePvalueMap, rankMap );
            if ( res != null ) {
                results.put( className, res );
            }
        }
    }

    /**
     * Get results for one class, based on class id. The other arguments are things that are not constant under
     * permutations of the data.
     * 
     * @param class_name a <code>String</code> value
     * @param probesToPvals a <code>Map</code> value
     * @param input_rank_map a <code>Map</code> value
     * @return a <code>classresult</code> value
     */
    public GeneSetResult classPval( String geneSet, Map probesToPvals, Map rankMap ) {

        int totalSize = 0;
        if ( settings.getUseWeights() ) {
            totalSize = geneAnnots.getGeneToGeneSetMap().size();
        } else {
            totalSize = geneAnnots.getProbeToGeneMap().size();
        }

        // variables for outputs
        Collection targetRanks = new HashSet();

        int effSize = ( ( Integer ) effectiveSizes.get( geneSet ) ).intValue(); // effective size of this class.
        if ( effSize < settings.getMinClassSize() || effSize > settings.getMaxClassSize() ) {
            return null;
        }

        Collection values = null;

        if ( settings.getUseWeights() ) {
            values = ( Collection ) geneAnnots.getGeneSetToGeneMap().get( geneSet );
        } else {
            values = ( Collection ) geneAnnots.getGeneSetToProbeMap().get( geneSet );
        }

        Iterator classit = values.iterator();
        Object ranking = null;

        // foreach item in the class.
        while ( classit.hasNext() && !isInterrupted() ) {

            String probe = ( String ) classit.next(); // probe id

            if ( probesToPvals.containsKey( probe ) ) {

                // if ( settings.getUseWeights() ) {
                // ranking = rankMap.get( geneAnnots.getProbeToGeneMap().get( probe ) );
                // if ( ranking != null ) {
                // targetRanks.add( ranking );
                // }
                //
                // } else { // no weights
                ranking = rankMap.get( probe );
                if ( ranking != null ) {
                    targetRanks.add( ranking );
                }
                // }
            }
        }

        double areaUnderROC = ROC.aroc( totalSize, targetRanks );
        double roc_pval = ROC.rocpval( totalSize, targetRanks );
        // set up the return object.
        GeneSetResult res = new GeneSetResult( geneSet, goName.getNameForId( geneSet ), ( ( Integer ) actualSizes
                .get( geneSet ) ).intValue(), effSize );
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