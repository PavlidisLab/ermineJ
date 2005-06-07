/*
 * The ermineJ project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScore.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.math.ROC;
import baseCode.util.StatusViewer;
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
    public void classPvalGenerator( Map genePvalueMap, Map rankMap, StatusViewer messenger ) {
        int count = 0;
        for ( Iterator iter = geneAnnots.getGeneSetToProbeMap().keySet().iterator(); iter.hasNext(); ) {
            if ( isInterrupted() ) {
                break;
            }
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

        int totalSize = 0;
        if ( settings.getUseWeights() ) {
            totalSize = geneAnnots.getGeneToGeneSetMap().size();
        } else {
            totalSize = geneAnnots.getProbeToGeneMap().size();
        }

        // variables for outputs
        List targetRanks = new ArrayList();

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

        boolean invert = settings.getDoLog() && !settings.getBigIsBetter();

        while ( classit.hasNext() && !isInterrupted() ) {

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