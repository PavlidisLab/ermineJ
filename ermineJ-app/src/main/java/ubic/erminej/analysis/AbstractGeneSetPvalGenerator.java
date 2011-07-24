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

import ubic.erminej.Settings;
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

    /**
     * How many genes are in each set (conditioned on the current data available)
     */
    protected Map<GeneSetTerm, Integer> effectiveSizes = null;

    /**
     * How many probes are in each set (conditioned on the current data available)
     */
    protected Map<GeneSetTerm, Integer> actualSizes = null;

    protected Settings settings;

    protected GeneAnnotations geneAnnots;

    protected GeneSetSizeComputer csc;

    private int maxGeneSetSize;
    private int minGeneSetSize;

    private boolean globalMissingAspectTreatedAsUsable = false;

    public AbstractGeneSetPvalGenerator( Settings set, GeneAnnotations annots, GeneSetSizeComputer csc ) {

        this.settings = set;
        this.geneAnnots = annots;
        this.effectiveSizes = csc.getEffectiveSizes();
        this.actualSizes = csc.getActualSizes();
        this.csc = csc;
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
     * @return true if the set should be retained (in the correct aspect and not considered redundant with another set)
     */
    protected boolean checkAspectAndRedundancy( GeneSetTerm geneSetName, boolean missingAspectTreatedAsUsable ) {

        if ( geneAnnots.skipDueToRedundancy( geneSetName ) ) {
            return false;
        }

        String aspect = geneSetName.getAspect();

        /*
         * If there is no aspect, we don't use it, unless it's user-defined (though that should have an aspect ....)
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
     * @param globalMissingAspectTreatedAsUsable The globalMissingAspectTreatedAsUsable to set.
     */
    public void setGlobalMissingAspectTreatedAsUsable( boolean globalMissingAspectTreatedAsUsable ) {
        this.globalMissingAspectTreatedAsUsable = globalMissingAspectTreatedAsUsable;
    }

}