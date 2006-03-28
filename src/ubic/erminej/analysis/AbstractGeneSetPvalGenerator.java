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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;

/**
 * Base implementation of pvalue generator
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public abstract class AbstractGeneSetPvalGenerator extends AbstractLongTask {
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

        if ( aspect == null && !this.goName.isUserDefined( geneSetName ) ) {
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
            log.debug( "Found user-defined gene set " + geneSetName + " for analysis" );
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

}