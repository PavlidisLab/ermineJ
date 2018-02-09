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

import ubic.basecode.math.Rank;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;

/**
 * Base implementation of pvalue generator
 *
 * @author Paul Pavlidis
 * @version $Id$
 */
public abstract class AbstractGeneSetPvalGenerator extends AbstractLongTask {

    /** Constant <code>ALERT_UPDATE_FREQUENCY=300</code> */
    protected static final int ALERT_UPDATE_FREQUENCY = 300;

    protected StatusViewer messenger = new StatusStderr();

    protected SettingsHolder settings;

    /*
     * 1-based ranks, taking into account bigger is better and log-transform.
     */
    private Map<Gene, Double> geneRanks;

    /*
     * Log-transformed (if requested)
     */
    protected Map<Gene, Double> geneToScoreMap;

    protected GeneAnnotations geneAnnots;

    protected int numGenesUsed = 0;

    private int maxGeneSetSize;

    private int minGeneSetSize;

    /**
     * <p>
     * Constructor for AbstractGeneSetPvalGenerator.
     * </p>
     *
     * @param set a {@link ubic.erminej.SettingsHolder} object.
     * @param annots a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param geneToScoreMap can be null if the method doesn't use gene scores; must already be log-transformed, if
     *        requested.
     * @param messenger a {@link ubic.basecode.util.StatusViewer} object.
     */
    public AbstractGeneSetPvalGenerator( SettingsHolder set, GeneAnnotations annots, Map<Gene, Double> geneToScoreMap,
            StatusViewer messenger ) {
        this.settings = set;
        this.geneAnnots = annots;
        this.geneToScoreMap = geneToScoreMap;

        if ( messenger != null ) this.messenger = messenger;
    }

    /**
     * Compute the results for all the gene sets under consideration.
     *
     * @return A map of GeneSetTerm to GeneSetResult.
     */
    public abstract Map<GeneSetTerm, GeneSetResult> generateGeneSetResults();

    /**
     * <p>
     * getMaxClassSize.
     * </p>
     *
     * @return a int.
     */
    public int getMaxClassSize() {
        return maxGeneSetSize;
    }

    /**
     * <p>
     * Getter for the field <code>messenger</code>.
     * </p>
     *
     * @return a {@link ubic.basecode.util.StatusViewer} object.
     */
    public StatusViewer getMessenger() {
        return messenger;
    }

    /**
     * <p>
     * Getter for the field <code>minGeneSetSize</code>.
     * </p>
     *
     * @return a int.
     */
    public int getMinGeneSetSize() {
        return minGeneSetSize;
    }

    /**
     * <p>
     * Getter for the field <code>numGenesUsed</code>.
     * </p>
     *
     * @return a int.
     */
    public int getNumGenesUsed() {
        return numGenesUsed;
    }

    /**
     * The number of elements in the given term. Note that this reports what is in the annotations, and may not reflect
     * what is actually being used in the analysis.
     *
     * @param t a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a int.
     */
    public int numElementsInSet( GeneSetTerm t ) {
        return geneAnnots.getGeneSetElements( t ).size();
    }

    /**
     * <p>
     * numGenesInSet.
     * </p>
     *
     * @param t a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a int.
     */
    public int numGenesInSet( GeneSetTerm t ) {
        return geneAnnots.getGeneSetGenes( t ).size();
    }

    /**
     * <p>
     * setClassMaxSize.
     * </p>
     *
     * @param value a int.
     */
    public void setClassMaxSize( int value ) {
        maxGeneSetSize = value;
    }

    /**
     * <p>
     * setClassMinSize.
     * </p>
     *
     * @param value a int.
     */
    public void setClassMinSize( int value ) {
        minGeneSetSize = value;
    }

    /**
     * <p>
     * checkAspect.
     * </p>
     *
     * @param geneSetName a {@link ubic.erminej.data.GeneSetTerm} object.
     * @param missingAspectTreatedAsUsable Whether gene sets missing an aspect should be treated as usable or not. This
     *        parameter is provided partly for testing. Global setting can override this if set to true.
     * @return true if the set should be retained (e.g. it is in the correct aspect )
     */
    protected boolean checkAspect( GeneSetTerm geneSetName, boolean missingAspectTreatedAsUsable ) {
        return this.geneAnnots.hasUsableAspect( geneSetName, missingAspectTreatedAsUsable );
    }

    /**
     * If GO data isn't initialized, this returns true. If there is no aspect associated with the gene set, return
     * false.
     *
     * @param geneSetName a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a boolean.
     */
    protected boolean checkAspectAndRedundancy( GeneSetTerm geneSetName ) {
        return this.checkAspect( geneSetName, false );
    }

    /**
     * Perform multiple test correction during the multifunctionality correction. DOES NOT SUPPORT WESTFALL-YOUNG
     * CORRECTION
     *
     * @param sortedClasses a {@link java.util.List} object.
     * @param results a {@link java.util.Map} object.
     */
    protected void multipleTestCorrect( List<GeneSetTerm> sortedClasses, Map<GeneSetTerm, GeneSetResult> results ) {
        MultipleTestCorrector mt = new MultipleTestCorrector( settings, sortedClasses, geneAnnots, null, results,
                getMessenger() );
        Settings.MultiTestCorrMethod multipleTestCorrMethod = settings.getMtc();
        if ( multipleTestCorrMethod.equals( SettingsHolder.MultiTestCorrMethod.BONFERRONI ) ) {
            mt.bonferroni();
        } else if ( multipleTestCorrMethod.equals( SettingsHolder.MultiTestCorrMethod.BENJAMINIHOCHBERG ) ) {
            mt.benjaminihochberg();
        } else {
            throw new UnsupportedOperationException( multipleTestCorrMethod
                    + " is not supported for this analysis method" );
        }
    }

    /**
     * <p>
     * ranksOfGenesInSet.
     * </p>
     *
     * @param genesInSet a {@link java.util.Collection} object.
     * @return 1-based ranks of genes in the set, taking into account "bigger is better" and log-transformation.
     */
    protected List<Double> ranksOfGenesInSet( Collection<Gene> genesInSet ) {

        boolean invert = ( settings.getDoLog() && !settings.getBigIsBetter() )
                || ( !settings.getDoLog() && settings.getBigIsBetter() );

        if ( this.geneRanks == null ) {
            geneRanks = Rank.rankTransform( geneToScoreMap, invert );
            for ( Gene g : geneRanks.keySet() ) {
                geneRanks.put( g, geneRanks.get( g ) + 1.0 );
            }
        }

        List<Double> targetRanks = new ArrayList<>();
        for ( Gene g : genesInSet ) {
            Double rank = geneRanks.get( g );
            if ( rank == null ) continue;
            targetRanks.add( rank );
        }
        return targetRanks;
    }

}
