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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.Histogram;

/**
 * Generates gene set p values using the resamplin-based 'experiment score' method of Pavlidis et al.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class ExperimentScorePvalGenerator extends AbstractGeneSetPvalGenerator {

    Histogram hist;

    /**
     * @param settings
     * @param a
     * @param csc
     * @param gon
     * @param hi
     */
    public ExperimentScorePvalGenerator( Settings settings, GeneAnnotations a, GeneSetSizeComputer csc, GONames gon,
            Histogram hi ) {
        super( settings, a, csc, gon );
        this.hist = hi;
    }

    /**
     * Get results for one class, based on class id. The other arguments are things that are not constant under
     * permutations of the data.
     * 
     * @param geneSetName
     * @param groupToPvalMap
     * @param probesToPvals
     * @return
     */
    public GeneSetResult classPval( String geneSetName, Map<String, Double> geneToPvalMap,
            Map<String, Double> probesToPvals ) {
        if ( !super.checkAspect( geneSetName ) ) return null;
        int effSize = ( ( Integer ) effectiveSizes.get( geneSetName ) ).intValue();
        if ( effSize < settings.getMinClassSize() || effSize > settings.getMaxClassSize() ) {
            return null;
        }

        Collection values = geneAnnots.getGeneSetProbes( geneSetName );
        Iterator classit = values.iterator();
        double[] groupPvalArr = new double[effSize]; // store pvalues for items in
        // the class.

        Set<String> record = new HashSet<String>();

        int v_size = 0;

        // foreach item in the class.
        while ( classit.hasNext() ) {
            ifInterruptedStop();
            String probe = ( String ) classit.next(); // probe id

            if ( probesToPvals.containsKey( probe ) ) { // if it is in the data
                // set. This is invariant
                // under permutations.

                if ( settings.getUseWeights() ) {

                    String gene = ( String ) geneAnnots.getProbeToGeneMap().get( probe );

                    if ( !record.contains( gene ) ) { // only count it once.

                        record.add( gene ); // mark
                        groupPvalArr[v_size] = ( ( Double ) geneToPvalMap.get( gene ) ).doubleValue();
                        v_size++;
                    }

                } else { // no weights - use raw p values for each probe

                    Double pbpval = ( Double ) probesToPvals.get( probe );
                    groupPvalArr[v_size] = pbpval.doubleValue();
                    v_size++;
                }
            } // if in data set
        } // end of while over items in the class.

        // get raw score and pvalue.
        double rawscore = ResamplingExperimentGeneSetScore.computeRawScore( groupPvalArr, effSize, settings
                .getRawScoreMethod() );
        double pval = scoreToPval( effSize, rawscore );

        if ( pval < 0.0 ) {
            throw new IllegalStateException( "A raw score (" + rawscore + ") yielded an invalid pvalue: Classname: "
                    + geneSetName );
        }

        // set up the return object.
        String nameForId = geneSetName;
        if ( goName != null ) {
            nameForId = goName.getNameForId( geneSetName );
        }
        GeneSetResult res = new GeneSetResult( geneSetName, nameForId, ( ( Integer ) actualSizes.get( geneSetName ) )
                .intValue(), effSize );
        res.setScore( rawscore );
        res.setPValue( pval );
        return res;

    }

    /* scoreClass */

    /**
     * convert a raw score into a pvalue, based on random background distribution
     * 
     * @param in_size int
     * @param rawscore double
     * @throws IllegalStateException
     * @return double
     */
    protected double scoreToPval( int in_size, double rawscore ) throws IllegalStateException {

        if ( hist == null ) throw new IllegalStateException( "Histogram is null" );

        double pval = hist.getValue( in_size, rawscore, settings.upperTail() );

        if ( pval < 0.0 ) {
            throw new IllegalStateException( "P value less than zero. Upper tail?" + settings.upperTail()
                    + " Raw score: " + rawscore );
        }

        if ( Double.isNaN( pval ) ) {
            throw new IllegalStateException( "Warning, a pvalue was not a number: raw score = " + rawscore );
        }
        return pval;
    }

}