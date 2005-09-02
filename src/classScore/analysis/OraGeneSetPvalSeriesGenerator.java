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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.util.StatusViewer;
import cern.jet.math.Arithmetic;
import classScore.Settings;
import classScore.data.GeneSetResult;

/**
 * Generate Overrepresentation p values for gene sets.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author pavlidis
 * @version $Id$
 */
public class OraGeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    /**
     * 
     */
    private static final int ALERT_UPDATE_FREQUENCY = 300;
    private Map results;
    private int numOverThreshold;
    private int numUnderThreshold;
    private int inputSize;

    public OraGeneSetPvalSeriesGenerator( Settings settings, GeneAnnotations geneData, GeneSetSizeComputer csc,
            GONames gon, int inputSize ) {
        super( settings, geneData, csc, gon );
        this.inputSize = inputSize;
        results = new HashMap();
    }

    public Map getResults() {
        return results;
    }

    /**
     * Generate a complete set of class results. The arguments are not constant under permutations. The second is only
     * needed for the aroc method. This is to be used only for the 'real' data since it modifies 'results',
     * 
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     */
    public void classPvalGenerator( Map geneToGeneScoreMap, Map probesToPvals, StatusViewer messenger ) {

        OraPvalGenerator cpv = new OraPvalGenerator( settings, geneAnnots, csc, numOverThreshold, numUnderThreshold,
                goName, inputSize );

        int count = 0;
        for ( Iterator iter = geneAnnots.getGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();

            String geneSetName = ( String ) iter.next();
            // log.debug( "Analyzing " + geneSetName );
            GeneSetResult res = cpv.classPval( geneSetName, geneToGeneScoreMap, probesToPvals );
            if ( res != null ) {
                results.put( geneSetName, res );
            }
            count++;
            if ( messenger != null && count % ALERT_UPDATE_FREQUENCY == 0 ) {
                messenger.showStatus( count + " gene sets analyzed" );
            }
        }
    }

    /**
     * Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution. This is a constant under
     * permutations, but depends on weights.
     * 
     * @param inp_entries The pvalues for the probes (no weights) or groups (weights)
     * @return number of entries that meet the user-set threshold.
     * @todo make this private and called by OraPvalGenerator.
     */
    public int hgSizes( Collection inp_entries ) {

        double geneScoreThreshold = settings.getPValThreshold();

        if ( settings.getDoLog() ) {
            geneScoreThreshold = -Arithmetic.log10( geneScoreThreshold );
        }

        Iterator itr = inp_entries.iterator();
        while ( itr.hasNext() ) {
            ifInterruptedStop();
            Map.Entry m = ( Map.Entry ) itr.next();
            double geneScore = ( ( Double ) m.getValue() ).doubleValue();

            if ( scorePassesThreshold( geneScore, geneScoreThreshold ) ) {
                numOverThreshold++;
            } else {
                numUnderThreshold++;
            }

        }
        return numOverThreshold;
    }

}