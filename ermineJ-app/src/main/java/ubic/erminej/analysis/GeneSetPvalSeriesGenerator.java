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
 
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;
import ubic.erminej.data.Probe;

/**
 * Generate gene set p values for a bunch of gene sets.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    private Histogram hist;

    public GeneSetPvalSeriesGenerator( SettingsHolder settings, GeneAnnotations geneData, Histogram hi,
            GeneSetSizesForAnalysis csc ) {
        super( settings, geneData, csc );
        this.hist = hi;

    }

    /**
     * Generate a complete set of class results. The arguments are not constant under pemutations.
     * 
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     * @return
     */
    public Map<GeneSetTerm, GeneSetResult> classPvalGenerator( Map<Gene, Double> geneToScoreMap,
            Map<Probe, Double> probeToScoreMap ) {
        Map<GeneSetTerm, GeneSetResult> results;
        results = new HashMap<GeneSetTerm, GeneSetResult>();
        ExperimentScorePvalGenerator cpv = new ExperimentScorePvalGenerator( settings, geneAnnots, csc, hist );

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getNonEmptyGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();
            GeneSetTerm className = iter.next();
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
    public Map<GeneSetTerm, Double> class_v_pval_generator( Map<Gene, Double> group_pval_map,
            Map<Probe, Double> probesToPvals ) {
        Map<GeneSetTerm, Double> results;
        results = new HashMap<GeneSetTerm, Double>();
        ExperimentScoreQuickPvalGenerator cpv = new ExperimentScoreQuickPvalGenerator( settings, geneAnnots, csc, hist );

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getNonEmptyGeneSets().iterator(); iter.hasNext(); ) {
            GeneSetTerm className = iter.next();
            double pval = cpv.classPvalue( className, group_pval_map, probesToPvals );

            log.debug( "pval: " + pval );

            if ( pval >= 0.0 ) {
                results.put( className, pval );
            }
        }
        return results;
    }

}