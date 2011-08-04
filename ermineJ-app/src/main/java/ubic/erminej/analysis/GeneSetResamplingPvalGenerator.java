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

import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;

/**
 * Generate gene set p values for a bunch of gene sets.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetResamplingPvalGenerator extends AbstractGeneSetPvalGenerator {

    private Histogram hist;

    private StatusViewer messenger = new StatusStderr();

    /**
     * @param settings
     * @param geneToScoreMap Should already be multifunctionality corrected if desired.
     * @param geneData
     * @param messenger
     */
    public GeneSetResamplingPvalGenerator( SettingsHolder settings, Map<Gene, Double> geneToScoreMap,
            GeneAnnotations geneData, StatusViewer messenger ) {
        super( settings, geneData );

        if ( messenger != null ) this.messenger = messenger;

        NullDistributionGenerator probePvalMapper = new GeneSetResamplingBackgroundDistributionGenerator( settings,
                geneToScoreMap );

        hist = probePvalMapper.generateNullDistribution( messenger );

    }

    /**
     * @param settings
     * @param geneData
     * @param hist - Should be based on resampling of multifunctionality corrected scoers, if desired.
     * @param messenger
     */
    public GeneSetResamplingPvalGenerator( SettingsHolder settings, GeneAnnotations geneData, Histogram hist,
            StatusViewer messenger ) {
        super( settings, geneData );
        this.hist = hist;
        if ( messenger != null ) this.messenger = messenger;

    }

    /**
     * Generate a complete set of class results. The arguments are not constant under pemutations.
     * 
     * @param group_pval_map a <code>Map</code> value
     * @return
     */
    public Map<GeneSetTerm, GeneSetResult> classPvalGenerator( Map<Gene, Double> geneToScoreMap ) {
        Map<GeneSetTerm, GeneSetResult> results;
        results = new HashMap<GeneSetTerm, GeneSetResult>();
        ExperimentScorePvalGenerator cpv = new ExperimentScorePvalGenerator( settings, geneAnnots, hist );

        int i = 0;
        for ( Iterator<GeneSetTerm> iter = geneAnnots.getGeneSetTerms().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();
            GeneSetTerm className = iter.next();
            GeneSetResult res = cpv.classPval( className, geneToScoreMap );
            if ( res != null ) {
                results.put( className, res );
                if ( ++i % ALERT_UPDATE_FREQUENCY == 0 ) {
                    messenger.showStatus( i + " gene sets analyzed" );
                }
            }
        }
        return results;
    }

    /**
     * Same thing as class_pval_generator, but returns a collection of scores (pvalues) (see below) instead of adding
     * them to the results object. This is used to get class pvalues for permutation analysis (W-Y correction)
     */
    public Map<GeneSetTerm, Double> classPvalGeneratorRaw( Map<Gene, Double> group_pval_map ) {
        Map<GeneSetTerm, Double> results;
        results = new HashMap<GeneSetTerm, Double>();
        ExperimentScoreQuickPvalGenerator cpv = new ExperimentScoreQuickPvalGenerator( settings, geneAnnots, hist );

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getGeneSetTerms().iterator(); iter.hasNext(); ) {
            GeneSetTerm className = iter.next();
            double pval = cpv.classPvalue( className, group_pval_map );

            if ( log.isDebugEnabled() ) log.debug( "pval: " + pval );

            if ( pval >= 0.0 ) {
                results.put( className, pval );
            }
        }
        return results;
    }

}