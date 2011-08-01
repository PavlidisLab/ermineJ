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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSetTerms;

/**
 * Base implementation of pvalue generator
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public abstract class AbstractGeneSetPvalGenerator extends AbstractLongTask {
    protected static final Log log = LogFactory.getLog( AbstractGeneSetPvalGenerator.class );

    protected static final int ALERT_UPDATE_FREQUENCY = 300;

    protected SettingsHolder settings;

    protected GeneAnnotations geneAnnots;

    public int numGenesInSet( GeneSetTerm t ) {
        return geneAnnots.getGeneSetGenes( t ).size();
    }

    public int numProbesInSet( GeneSetTerm t ) {
        return geneAnnots.getGeneSetProbes( t ).size();
    }

    private int maxGeneSetSize;
    private int minGeneSetSize;

    private boolean globalMissingAspectTreatedAsUsable = false;

    public AbstractGeneSetPvalGenerator( SettingsHolder set, GeneAnnotations annots ) {
        this.settings = set;
        this.geneAnnots = annots;
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
     * If GO data isn't initialized, this returns true. If there is no aspect associated with the gene set, return
     * false.
     * 
     * @param geneSetName
     * @return
     */
    protected boolean checkAspectAndRedundancy( GeneSetTerm geneSetName ) {
        return this.checkAspectAndRedundancy( geneSetName, false );
    }

    /**
     * @param geneSetName
     * @param missingAspectTreatedAsUsable Whether gene sets missing an aspect should be treated as usable or not. This
     *        parameter is provided partly for testing. Global setting can override this if set to true.
     * @return true if the set should be retained (e.g. it is in the correct aspect )
     */
    protected boolean checkAspectAndRedundancy( GeneSetTerm geneSetName, boolean missingAspectTreatedAsUsable ) {

        String aspect = geneSetName.getAspect();

        /*
         * If there is no aspect, we don't use it, unless it's user-defined (though that should have an aspect ... )
         */
        if ( aspect == null && !geneSetName.isUserDefined() ) {
            return missingAspectTreatedAsUsable || this.globalMissingAspectTreatedAsUsable;
        }

        if ( aspect != null ) {
            if ( aspect.equalsIgnoreCase( "biological_process" ) && this.settings.getUseBiologicalProcess() ) {
                return true;
            } else if ( aspect.equalsIgnoreCase( "cellular_component" ) && this.settings.getUseCellularComponent() ) {
                return true;
            } else if ( aspect.equalsIgnoreCase( "molecular_function" ) && this.settings.getUseMolecularFunction() ) {
                return true;
            } else if ( aspect.equalsIgnoreCase( GeneSetTerms.USER_DEFINED ) && this.settings.getUseUserDefined() ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Thisis to deal with the possibility of genesets that lack an aspect -- but this should not happen (any more). It
     * was a problem for obsolete GO Terms, but we ignore those anyway.
     * 
     * @param globalMissingAspectTreatedAsUsable The globalMissingAspectTreatedAsUsable to set.
     */
    public void setGlobalMissingAspectTreatedAsUsable( boolean globalMissingAspectTreatedAsUsable ) {
        this.globalMissingAspectTreatedAsUsable = globalMissingAspectTreatedAsUsable;
    }

}