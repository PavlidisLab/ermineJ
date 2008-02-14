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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.dataStructure.matrix.DoubleMatrixNamed;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.Histogram;

/**
 * Calculates the raw average class correlations using a background distribution.
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class CorrelationsGeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    protected static final Log log = LogFactory.getLog( CorrelationsGeneSetPvalSeriesGenerator.class );

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
            GeneSetSizeComputer csc, GONames gon, DoubleMatrixNamed<String, String> rawData, Histogram hist ) {
        super( settings, geneAnnots, csc, gon );

        this.classScoreGenerator = new CorrelationPvalGenerator( settings, geneAnnots, csc, gon, rawData );
        this.geneAnnots = geneAnnots;

        classScoreGenerator.setProbeToGeneMap( geneAnnots.getProbeToGeneMap() );
        classScoreGenerator.setHistogram( hist );
        classScoreGenerator.setGeneRepTreatment( settings.getGeneRepTreatment() );
        classScoreGenerator.set_class_max_size( settings.getMaxClassSize() );
        classScoreGenerator.set_class_min_size( settings.getMinClassSize() );

    }

    /**
     * @param messenger
     */
    public Map<String, GeneSetResult> classPvalGenerator( StatusViewer messenger ) {
        Map<String, GeneSetResult> results = new HashMap<String, GeneSetResult>();
        ;
        int count = 0;
        classScoreGenerator.setTests( 0 );
        classScoreGenerator.setCacheHits( 0 );

        for ( Iterator iter = geneAnnots.getGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();

            String geneSetName = ( String ) iter.next();
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