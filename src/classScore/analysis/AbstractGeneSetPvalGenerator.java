package classScore.analysis;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import classScore.Settings;

/**
 * Base implementation of pvalue generator
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public abstract class AbstractGeneSetPvalGenerator {
    protected static final Log log = LogFactory.getLog( AbstractGeneSetPvalGenerator.class );
    protected Map effectiveSizes = null;
    protected Map actualSizes = null;
    protected GONames goName;
    protected Settings settings;
    protected GeneAnnotations geneAnnots;
    protected GeneSetSizeComputer csc;
    private int maxGeneSetSize;
    private int minGeneSetSize;

    public AbstractGeneSetPvalGenerator( Settings set, GeneAnnotations annots, GeneSetSizeComputer csc, GONames gon ) {

        this.settings = set;
        this.geneAnnots = annots;
        this.effectiveSizes = csc.getEffectiveSizes();
        this.actualSizes = csc.getActualSizes();
        this.csc = csc;
        this.goName = gon;
    }

    /**
     * @param value
     */
    public void set_class_max_size( int value ) {
        maxGeneSetSize = value;
    }

    /**
     * @param value
     */
    public void set_class_min_size( int value ) {
        minGeneSetSize = value;
    }

    /**
     */
    public int getMaxClassSize() {
        return maxGeneSetSize;
    }

    /**
     * @return
     */
    public int getMinGeneSetSize() {
        return minGeneSetSize;
    }

    /**
     * If GO data isn't initialized, this returns true.
     * 
     * @param geneSetName
     * @return
     */
    protected boolean checkAspect( String geneSetName ) {
        if ( goName == null ) return true;

        String aspect = this.goName.getAspectForId( geneSetName );

        if ( aspect == null ) {
            log.debug( "Null aspect for " + geneSetName + ", skipping" );
            return false;
        }

        if ( ( aspect.equalsIgnoreCase( "biological_process" ) || aspect
                .equalsIgnoreCase( "obsolete_biological_process" ) )
                && this.settings.getUseBiologicalProcess() ) {
            return true;
        } else if ( ( aspect.equalsIgnoreCase( "cellular_component" ) || aspect
                .equalsIgnoreCase( "obsolete_cellular_component" ) )
                && this.settings.getUseCellularComponent() ) {
            return true;
        } else if ( ( aspect.equalsIgnoreCase( "molecular_function" ) || aspect
                .equalsIgnoreCase( "obsolete_molecular_function" ) )
                && this.settings.getUseMolecularFunction() ) {
            return true;
        } else if ( aspect.equalsIgnoreCase( GONames.USER_DEFINED ) ) {
            return true; // user-defined - always use.
        }
        return false;
    }

    /**
     * Test whether a score meets a threshold. This might not really belong here.
     * 
     * @param geneScore
     * @param geneScoreThreshold
     * @return
     */
    protected boolean scorePassesThreshold( double geneScore, double geneScoreThreshold ) {
        return ( settings.upperTail() && geneScore >= geneScoreThreshold )
                || ( !settings.upperTail() && geneScore <= geneScoreThreshold );
    }

    /**
     * @return Returns the isInterrupted.
     */
    public boolean isInterrupted() {
        // log.debug( Thread.currentThread().getName() + " " + Thread.currentThread().isInterrupted() );
        return Thread.currentThread().isInterrupted();
    }

}