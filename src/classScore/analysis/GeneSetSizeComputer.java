package classScore.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.bio.geneset.GeneAnnotations;
import classScore.data.GeneScores;

/**
 * Class for computing the actual and effective sizes of gene sets.
 * <p>
 * Copyright: Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class GeneSetSizeComputer {
    protected Map effectiveSizes = null;
    protected Map actualSizes = null;
    protected boolean weight_on = true;

    protected GeneScores geneScores;
    protected Collection activeProbes;
    private GeneAnnotations geneData;

    public GeneSetSizeComputer( Collection activeProbes, GeneAnnotations geneData, GeneScores geneScores, boolean w ) {
        this.weight_on = w;
        this.activeProbes = activeProbes;
        this.geneData = geneData;

        this.geneScores = geneScores;
        effectiveSizes = new HashMap();
        actualSizes = new HashMap();
        getClassSizes();
    }

    /**
     * Calculate class sizes for all classes - both effective and actual size
     */
    private void getClassSizes() {
        Map record = new HashMap();
        int size;
        int v_size;

        // assert !( activeProbes == null || activeProbes.size() == 0 ) : "ActiveProbes was not initialized or was
        // empty";
        // assert !( geneScores == null ) : "GeneScores was not initialized";
        // assert !( geneScores.getGeneToPvalMap() == null ) : "getGroupToPvalMap was not initialized";

        boolean gotAtLeastOneNonZero = false;

        for ( Iterator iter = geneData.getGeneSets().iterator(); iter.hasNext(); ) {

            String className = ( String ) iter.next(); // id of the class
            // (GO:XXXXXX)
            Collection values = geneData.getGeneSetProbes( className );
            Iterator I = values.iterator();

            record.clear();
            size = 0;
            v_size = 0;

            while ( I.hasNext() ) { // foreach item in the class.
                String probe = ( String ) I.next();
                String gene = geneData.probeToGene( probe );
                if ( probe != null ) {
                    if ( activeProbes.contains( probe ) ) { // if it is in the data
                        // set
                        size++;

                        if ( weight_on ) { // routine for weights
                            // compute pval for every replicate group
                            if ( geneScores.getGeneToPvalMap().containsKey( gene )

                            /*
                             * if we haven't done this probe already.
                             */
                            && !record.containsKey( gene ) ) {

                                /*
                                 * mark it as done for this class.
                                 */
                                record.put( gene, null );
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
    public Map getEffectiveSizes() {
        return effectiveSizes;
    }

    /**
     * @return Map
     */
    public Map getActualSizes() {
        return actualSizes;
    }

}