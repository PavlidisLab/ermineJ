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
import java.util.Map.Entry;

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

    private static final int ALERT_UPDATE_FREQUENCY = 300;

    private int numOverThreshold;
    private int numUnderThreshold;
    private int inputSize;

    public OraGeneSetPvalSeriesGenerator( Settings settings, GeneAnnotations geneData, GeneSetSizeComputer csc,
            GONames gon, int inputSize ) {
        super( settings, geneData, csc, gon );
        this.inputSize = inputSize;

    }

    /**
     * Generate a complete set of class results.
     * 
     * @param geneToGeneScoreMap
     * @param probesToPvals
     */
    public Map<String, GeneSetResult> classPvalGenerator( Map<String, Double> geneToGeneScoreMap,
            Map<String, Double> probesToPvals, StatusViewer messenger ) {
        Map<String, GeneSetResult> results = new HashMap<String, GeneSetResult>();
        OraPvalGenerator cpv = new OraPvalGenerator( settings, geneAnnots, csc, numOverThreshold, numUnderThreshold,
                goName, inputSize );

        int count = 0;
        for ( Iterator<String> iter = geneAnnots.getGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();

            String geneSetName = iter.next();
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
        return results;
    }

    /**
     * Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution.
     * 
     * @param geneScores The pvalues for the probes (no weights) or groups (weights)
     * @return number of entries that meet the user-set threshold.
     * @todo make this private and called by OraPvalGenerator.
     */
    public int hgSizes( Collection<Entry<String, Double>> geneScores ) {

        double geneScoreThreshold = settings.getGeneScoreThreshold();

        if ( settings.getDoLog() ) {
            geneScoreThreshold = -Arithmetic.log10( geneScoreThreshold );
        }

        for ( Entry<String, Double> m : geneScores ) {
            double geneScore = m.getValue();

            if ( scorePassesThreshold( geneScore, geneScoreThreshold ) ) {
                numOverThreshold++;
            } else {
                numUnderThreshold++;
            }

        }
        return numOverThreshold;
    }

    /**
     * Test whether a score meets a threshold.
     * 
     * @param geneScore
     * @param geneScoreThreshold
     * @return
     */
    private boolean scorePassesThreshold( double geneScore, double geneScoreThreshold ) {
        return ( settings.upperTail() && geneScore >= geneScoreThreshold )
                || ( !settings.upperTail() && geneScore <= geneScoreThreshold );
    }

}