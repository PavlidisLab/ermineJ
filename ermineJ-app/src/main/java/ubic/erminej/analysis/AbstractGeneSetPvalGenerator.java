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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.math.Rank;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
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

    private StatusViewer messenger = new StatusStderr();

    protected SettingsHolder settings;

    protected Map<Gene, Double> geneRanks;
    protected Map<Gene, Double> geneToScoreMap;
    protected GeneAnnotations geneAnnots;
    protected int numGenesUsed = 0;

    private int maxGeneSetSize;

    private int minGeneSetSize;

    private boolean globalMissingAspectTreatedAsUsable = false;

    /**
     * @param set
     * @param annots
     * @param geneToScoreMap can be null if the method doesn't use gene scores.
     * @param messenger2
     */
    public AbstractGeneSetPvalGenerator( SettingsHolder set, GeneAnnotations annots, Map<Gene, Double> geneToScoreMap,
            StatusViewer messenger ) {
        this.settings = set;
        this.geneAnnots = annots;
        this.geneToScoreMap = geneToScoreMap;

        if ( messenger != null ) this.messenger = messenger;
    }

    public abstract Map<GeneSetTerm, GeneSetResult> generateGeneSetResults();

    /**
     */
    public int getMaxClassSize() {
        return maxGeneSetSize;
    }

    public StatusViewer getMessenger() {
        return messenger;
    }

    /**
     * @return
     */
    public int getMinGeneSetSize() {
        return minGeneSetSize;
    }

    public int getNumGenesUsed() {
        return numGenesUsed;
    }

    public int numGenesInSet( GeneSetTerm t ) {
        return geneAnnots.getGeneSetGenes( t ).size();
    }

    public int numProbesInSet( GeneSetTerm t ) {
        return geneAnnots.getGeneSetProbes( t ).size();
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
     * Thisis to deal with the possibility of genesets that lack an aspect -- but this should not happen (any more). It
     * was a problem for obsolete GO Terms, but we ignore those anyway.
     * 
     * @param globalMissingAspectTreatedAsUsable The globalMissingAspectTreatedAsUsable to set.
     */
    public void setGlobalMissingAspectTreatedAsUsable( boolean globalMissingAspectTreatedAsUsable ) {
        this.globalMissingAspectTreatedAsUsable = globalMissingAspectTreatedAsUsable;
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
     * @param genesInSet
     * @return 1-based ranks of genes in the set, taking into account "bigger is better" etc.
     */
    protected List<Double> ranksOfGenesInSet( Collection<Gene> genesInSet ) {
        boolean invert = ( settings.getDoLog() && !settings.getBigIsBetter() )
                || ( !settings.getDoLog() && settings.getBigIsBetter() );

        if ( this.geneRanks == null ) {
            geneRanks = Rank.rankTransform( geneToScoreMap );
        }

        int totalSize = geneRanks.size();
        List<Double> targetRanks = new ArrayList<Double>();
        for ( Gene g : genesInSet ) {

            Double rank = geneRanks.get( g );

            if ( rank == null ) continue;

            /* if the values are log-transformed, and bigger is not better, we need to invert the rank */
            if ( invert ) {
                rank = totalSize - rank;
                assert rank >= 0;
            }

            targetRanks.add( rank + 1.0 ); // make ranks 1-based.
        }
        return targetRanks;
    }

}