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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.data.GeneScores;

/**
 * Class for computing the actual and effective sizes of gene sets.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetSizeComputer {
    protected Map<String, Integer> effectiveSizes = null;
    protected Map<String, Integer> actualSizes = null;
    protected boolean weight_on = true;

    protected GeneScores geneScores;
    protected Collection<String> activeProbes;
    private GeneAnnotations geneData;

    public GeneSetSizeComputer( Collection<String> activeProbes, GeneAnnotations geneData, GeneScores geneScores,
            boolean w ) {
        this.weight_on = w;
        this.activeProbes = activeProbes;
        this.geneData = geneData;

        this.geneScores = geneScores;
        effectiveSizes = new HashMap<String, Integer>();
        actualSizes = new HashMap<String, Integer>();
        getClassSizes();
    }

    /**
     * Calculate class sizes for all classes - both effective and actual size
     */
    private void getClassSizes() {
        Set<String> record = new HashSet<String>();
        int size;
        int v_size;

        // assert !( activeProbes == null || activeProbes.size() == 0 ) : "ActiveProbes was not initialized or was
        // empty";
        // assert !( geneScores == null ) : "GeneScores was not initialized";
        // assert !( geneScores.getGeneToPvalMap() == null ) : "getGroupToPvalMap was not initialized";

        boolean gotAtLeastOneNonZero = false;

        for ( Iterator<String> iter = geneData.getGeneSets().iterator(); iter.hasNext(); ) {

            String className = iter.next(); // id of the class
            // (GO:XXXXXX)
            Collection<String> values = geneData.getGeneSetProbes( className );
            Iterator<String> I = values.iterator();

            record.clear();
            size = 0;
            v_size = 0;

            while ( I.hasNext() ) { // foreach item in the class.
                String probe = I.next();
                String gene = geneData.probeToGene( probe );
                if ( probe != null ) {
                    if ( activeProbes.contains( probe ) ) { // if it is in the data
                        // set
                        size++;

                        if ( weight_on ) { // routine for weights
                            // compute pval for every replicate group
                            if ( ( geneScores == null || geneScores.getGeneToPvalMap().containsKey( gene ) ) // FIXME,
                                    // this
                                    // doesn't
                                    // work
                                    // right
                                    // if
                                    // geneScores
                                    // is
                                    // null.F

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
                } // end of null check
            } // end of while over items in the class.

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
    public Map<String, Integer> getEffectiveSizes() {
        return effectiveSizes;
    }

    /**
     * @return Map
     */
    public Map<String, Integer> getActualSizes() {
        return actualSizes;
    }

}