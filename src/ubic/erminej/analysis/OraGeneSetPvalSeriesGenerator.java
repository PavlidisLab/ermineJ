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

import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import cern.jet.math.Arithmetic;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;

/**
 * Generate Overrepresentation p values for gene sets.
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