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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Probe;

/**
 * Class for computing the actual and effective sizes of gene sets.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetSizesForAnalysis {
    protected Map<GeneSetTerm, Integer> effectiveSizes = null;
    protected Map<GeneSetTerm, Integer> actualSizes = null;
    protected boolean weight_on = true;

    protected GeneScores geneScores;

    private GeneAnnotations geneData;

    /**
     * @param geneData
     * @param geneScores
     * @param w
     */
    public GeneSetSizesForAnalysis( GeneAnnotations geneData, GeneScores geneScores, SettingsHolder settings ) {
        this.geneData = geneData;
        this.geneScores = geneScores;
        effectiveSizes = new HashMap<GeneSetTerm, Integer>();
        actualSizes = new HashMap<GeneSetTerm, Integer>();

        this.weight_on = settings.getUseWeights();
        getClassSizes();

    }

    /**
     * Calculate class sizes for all classes - both effective and actual size
     */
    private void getClassSizes() {
        Set<Gene> record = new HashSet<Gene>();
        int size;
        int v_size;

        boolean gotAtLeastOneNonZero = false;

        for ( GeneSetTerm className : geneData.getNonEmptyGeneSets() ) {

            Collection<Probe> values = geneData.getGeneSetProbes( className );

            record.clear();
            size = 0;
            v_size = 0;

            for ( Probe probe : values ) {
                Gene gene = probe.getGene();

                size++;

                if ( weight_on ) { // routine for weights
                    // compute pval for every replicate group

                    // really we shouldn't have to check geneScores. The annotations provide should be trimmed down.
                    if ( ( geneScores == null || geneScores.getGeneToScoreMap().containsKey( gene ) )

                    /*
                     * if we haven't done this probe already.
                     */
                    && !record.contains( gene ) ) {

                        /*
                         * mark it as done for this class.
                         */
                        record.add( gene );
                        v_size++; // this is used in any case.
                    }
                }

            }

            if ( !weight_on ) {
                v_size = size;
            }

            gotAtLeastOneNonZero = gotAtLeastOneNonZero || v_size > 0;

            effectiveSizes.put( className, new Integer( v_size ) );
            actualSizes.put( className, new Integer( size ) );
        }

        // assert gotAtLeastOneNonZero;

    }

    /**
     * @return Map
     */
    public Map<GeneSetTerm, Integer> getEffectiveSizes() {
        return effectiveSizes;
    }

    /**
     * @return Map
     */
    public Map<GeneSetTerm, Integer> getActualSizes() {
        return actualSizes;
    }

}