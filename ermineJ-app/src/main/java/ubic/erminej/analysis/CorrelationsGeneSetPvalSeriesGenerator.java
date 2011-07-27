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
import ubic.basecode.util.StatusViewer;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm; 
import ubic.erminej.data.Histogram;
import ubic.erminej.data.Probe;

/**
 * Calculates the raw average class correlations using a background distribution.
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class CorrelationsGeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    private CorrelationPvalGenerator classScoreGenerator;

    /**
     * @param settings
     * @param geneAnnots
     * @param csc
     * @param gon
     * @param rawData
     * @param hist
     */
    public CorrelationsGeneSetPvalSeriesGenerator( Settings settings, GeneAnnotations geneAnnots,
            GeneSetSizeComputer csc, DoubleMatrix<Probe, String> rawData, Histogram hist ) {
        super( settings, geneAnnots, csc );

        this.classScoreGenerator = new CorrelationPvalGenerator( settings, geneAnnots, csc, rawData );
        this.geneAnnots = geneAnnots;

        classScoreGenerator.setHistogram( hist );
        classScoreGenerator.setGeneRepTreatment( settings.getGeneRepTreatment() );
        classScoreGenerator.set_class_max_size( settings.getMaxClassSize() );
        classScoreGenerator.set_class_min_size( settings.getMinClassSize() );

    }

    /**
     * @param messenger
     */
    public Map<GeneSetTerm, GeneSetResult> classPvalGenerator( StatusViewer messenger ) {
        Map<GeneSetTerm, GeneSetResult> results = new HashMap<GeneSetTerm, GeneSetResult>();

        int count = 0;
        classScoreGenerator.setTests( 0 );
        classScoreGenerator.setCacheHits( 0 );

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getActiveGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();

            GeneSetTerm geneSetName = iter.next();
            GeneSetResult res = classScoreGenerator.classPval( geneSetName );
            if ( res != null ) {
                results.put( geneSetName, res );
            }
            count++;
            if ( messenger != null && count % 100 == 0 ) {
                messenger.showStatus( count + " gene sets analyzed" );
            }
        }

        log.debug( "Tests: " + classScoreGenerator.getTests() );
        log.debug( "Cache hits: " + classScoreGenerator.getCacheHits() );
        return results;
    }

}