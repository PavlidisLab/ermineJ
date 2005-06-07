package classScore.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * Generate gene set p values for a bunch of gene sets.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class GeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    private Map results;
    private Histogram hist;

    public GeneSetPvalSeriesGenerator( Settings settings, GeneAnnotations geneData, Histogram hi,
            GeneSetSizeComputer csc, GONames gon ) {
        super( settings, geneData, csc, gon );
        this.hist = hi;

        results = new HashMap();
    }

    public Map getResults() {
        return results;
    }

    /**
     * Generate a complete set of class results. The arguments are not constant under pemutations.
     * 
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     */
    public void classPvalGenerator( Map group_pval_map, Map probesToPvals ) {

        ExperimentScorePvalGenerator cpv = new ExperimentScorePvalGenerator( settings, geneAnnots, csc, goName, hist );

        // For each class.
        for ( Iterator iter = geneAnnots.getGeneSetToProbeMap().keySet().iterator(); iter.hasNext(); ) {
            if ( isInterrupted() ) {
                break;
            }
            String className = ( String ) iter.next();
            GeneSetResult res = cpv.classPval( className, group_pval_map, probesToPvals );
            if ( res != null ) {
                results.put( className, res );
            }
        }
    }

    /* class_pval_generator */

    /**
     * Same thing as class_pval_generator, but returns a collection of scores (pvalues) (see below) instead of adding
     * them to the results object. This is used to get class pvalues for permutation analysis.
     */
    public Map class_v_pval_generator( Map group_pval_map, Map probesToPvals ) {

        ExperimentScoreQuickPvalGenerator cpv = new ExperimentScoreQuickPvalGenerator( settings, geneAnnots, csc,
                goName, hist );

        // For each class.
        for ( Iterator iter = geneAnnots.getGeneSetToProbeMap().keySet().iterator(); iter.hasNext(); ) {
            String className = ( String ) iter.next();
            double pval = cpv.classPvalue( className, group_pval_map, probesToPvals );

            if ( pval >= 0.0 ) {
                results.put( className, new Double( pval ) );
            }
        }
        return results;
    }

}