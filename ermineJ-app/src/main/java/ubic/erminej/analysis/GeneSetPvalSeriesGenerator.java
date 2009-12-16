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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.Histogram;

/**
 * Generate gene set p values for a bunch of gene sets.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    private Histogram hist;

    public GeneSetPvalSeriesGenerator( Settings settings, GeneAnnotations geneData, Histogram hi,
            GeneSetSizeComputer csc, GONames gon ) {
        super( settings, geneData, csc, gon );
        this.hist = hi;

    }

    /**
     * Generate a complete set of class results. The arguments are not constant under pemutations.
     * 
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     * @return
     */
    public Map<String, GeneSetResult> classPvalGenerator( Map<String, Double> geneToScoreMap,
            Map<String, Double> probeToScoreMap ) {
        Map<String, GeneSetResult> results;
        results = new HashMap<String, GeneSetResult>();
        ExperimentScorePvalGenerator cpv = new ExperimentScorePvalGenerator( settings, geneAnnots, csc, goName, hist );

        for ( Iterator<String> iter = geneAnnots.getGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();
            String className = iter.next();
            GeneSetResult res = cpv.classPval( className, geneToScoreMap, probeToScoreMap );
            if ( res != null ) {
                results.put( className, res );
            }
        }
        return results;
    }

    /**
     * Same thing as class_pval_generator, but returns a collection of scores (pvalues) (see below) instead of adding
     * them to the results object. This is used to get class pvalues for permutation analysis.
     */
    public Map<String, Double> class_v_pval_generator( Map<String, Double> group_pval_map,
            Map<String, Double> probesToPvals ) {
        Map<String, Double> results;
        results = new HashMap<String, Double>();
        ExperimentScoreQuickPvalGenerator cpv = new ExperimentScoreQuickPvalGenerator( settings, geneAnnots, csc,
                goName, hist );

        for ( Iterator<String> iter = geneAnnots.getGeneSets().iterator(); iter.hasNext(); ) {
            String className = iter.next();
            double pval = cpv.classPvalue( className, group_pval_map, probesToPvals );

            log.debug( "pval: " + pval );

            if ( pval >= 0.0 ) {
                results.put( className, pval );
            }
        }
        return results;
    }

}