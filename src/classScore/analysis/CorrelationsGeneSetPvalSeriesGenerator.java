package classScore.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.util.StatusViewer;
import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * Calculates the raw average class correlations using a background distribution.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class CorrelationsGeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    protected static final Log log = LogFactory.getLog( CorrelationsGeneSetPvalSeriesGenerator.class );

    private CorrelationPvalGenerator classScoreGenerator;
    private Map results;

    /**
     * @return
     */
    public Map getResults() {
        return results;
    }

    /**
     * @param settings
     * @param geneAnnots
     * @param csc
     * @param gon
     * @param rawData
     * @param hist
     */
    public CorrelationsGeneSetPvalSeriesGenerator( Settings settings, GeneAnnotations geneAnnots,
            GeneSetSizeComputer csc, GONames gon, DenseDoubleMatrix2DNamed rawData, Histogram hist ) {
        super( settings, geneAnnots, csc, gon );

        this.classScoreGenerator = new CorrelationPvalGenerator( settings, geneAnnots, csc, gon, rawData );
        this.geneAnnots = geneAnnots;

        classScoreGenerator.setProbeToGeneMap( geneAnnots.getProbeToGeneMap() );
        classScoreGenerator.setHistogram( hist );
        classScoreGenerator.setGeneRepTreatment( settings.getGeneRepTreatment() );
        classScoreGenerator.set_class_max_size( settings.getMaxClassSize() );
        classScoreGenerator.set_class_min_size( settings.getMinClassSize() );
        results = new HashMap();
    }

    /**
     * @param messenger
     */
    public void classPvalGenerator( StatusViewer messenger ) {
        int count = 0;
        classScoreGenerator.setTests( 0 );
        classScoreGenerator.setUsedCache( 0 );

        for ( Iterator iter = geneAnnots.getGeneSets().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();

            String geneSetName = ( String ) iter.next();
            GeneSetResult res = classScoreGenerator.classPval( geneSetName );
            if ( res != null ) {
                results.put( geneSetName, res );
            }
            count++;
            if ( messenger != null && count % 20 == 0 ) {
                messenger.showStatus( count + " gene sets analyzed" );
            }
        }

        log.debug( "Tests: " + classScoreGenerator.getTests() );
        log.debug( "Cache hits: " + classScoreGenerator.getUsedCache() );

    }

}