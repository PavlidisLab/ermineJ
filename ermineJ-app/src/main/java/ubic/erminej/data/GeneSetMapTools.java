/*
 * The baseCode project
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
package ubic.erminej.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusViewer;

/**
 * Utility methods
 * <p>
 * Note that in ErmineJ 3 these are no longer used.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetMapTools {

    protected static final Log log = LogFactory.getLog( GeneSetMapTools.class );

    /**
     * @param classId
     * @param classesToSimilarMap
     * @return
     */
    public static Collection<String> getSimilarities( String classId,
            Map<String, Collection<String>> classesToSimilarMap ) {
        if ( classesToSimilarMap != null && classesToSimilarMap.containsKey( classId ) ) {
            return classesToSimilarMap.get( classId );
        }
        return null;
    }

    /**
     * <p>
     * Remove classes which are too similar to some other class. In addition, the user can select a penalty for large
     * gene sets. Thus when two gene sets are found to be similar, the decision of which one to keep can be tuned based
     * on the size penalty. We find it useful to penalize large gene sets so we tend to keep smaller ones (but not too
     * small). Useful values of the penalty are above 1 (a value of 1 will result in the larger class always being
     * retained).
     * </p>
     * <p>
     * The amount of similarity to be tolerated is set by the parameter fractionSameThreshold, representing the fraction
     * of genes in the smaller class which are also found in the larger class. Thus, setting this threshold to be 0.0
     * means that no overlap is tolerated. Setting it to 1 means that classes will never be discarded.
     * </p>
     * 
     * @param fractionSameThreshold A value between 0 and 1, indicating how similar a class must be before it gets
     *        ditched.
     * @param ga
     * @param messenger For updating a log.
     * @param maxClassSize Large class considered. (that doesn't mean they are removed)
     * @param minClassSize Smallest class considered. (that doesn't mean they are removed)
     * @param bigClassPenalty A value greater or equal to one, indicating the cost of retaining a larger class in favor
     *        of a smaller one. The penalty is scaled with the difference in sizes of the two classes being considered,
     *        so very large classes are more heavily penalized.
     */
    public static void ignoreSimilar( double fractionSameThreshold, GeneAnnotations ga, StatusViewer messenger,
            int maxClassSize, int minClassSize, double bigClassPenalty ) {

        Map<GeneSetTerm, Collection<GeneSetTerm>> classesToSimilarMap = new LinkedHashMap<GeneSetTerm, Collection<GeneSetTerm>>();
        Collection<GeneSetTerm> seenit = new HashSet<GeneSetTerm>();
        Collection<GeneSetTerm> deleteUs = new HashSet<GeneSetTerm>();

        if ( messenger != null ) {
            messenger.showStatus( "...Highly (" + fractionSameThreshold * 100
                    + "%)  similar classes are being removed..." + ga.numGeneSets() + " to start..." );
        }

        // iterate over all the classes, starting from the smallest one.
        // List sortedList = ga.sortGeneSetsBySize();
        List<GeneSetTerm> sortedList = new ArrayList<GeneSetTerm>( ga.getNonEmptyGeneSets() );
        Collections.shuffle( sortedList );

        // OUTER - compare all classes to each other.

        for ( GeneSetTerm queryClassId : sortedList ) {
            Collection<Gene> queryClassMembers = ga.getGeneSetGenes( queryClassId );

            int querySize = queryClassMembers.size();

            if ( seenit.contains( queryClassId ) || querySize > maxClassSize || querySize < minClassSize ) {
                continue;
            }

            seenit.add( queryClassId );

            // INNER
            for ( GeneSetTerm targetClassId : sortedList ) {

                // / skip self comparisons and also symmetric comparisons.
                if ( seenit.contains( targetClassId ) || targetClassId.equals( queryClassId ) ) {
                    continue;
                }

                Collection<Gene> targetClass = ga.getGeneSetGenes( targetClassId );

                int targetSize = targetClass.size();
                if ( targetSize < querySize || targetSize > maxClassSize || targetSize < minClassSize ) {
                    continue;
                }

                double sizeScore;

                if ( areSimilarClasses( targetClass, queryClassMembers, fractionSameThreshold ) ) {

                    sizeScore = ( ( double ) targetClass.size() / ( double ) queryClassMembers.size() )
                            / bigClassPenalty;

                    if ( sizeScore < 1.0 ) { // delete the larget class.
                        deleteUs.add( targetClassId );
                        seenit.add( targetClassId );
                    } else {
                        deleteUs.add( queryClassId );
                        seenit.add( queryClassId );
                        break; // query is no longer relevant, go to the next one.
                    }

                    storeSimilarSets( classesToSimilarMap, queryClassId, targetClassId );
                }

            } /* inner while */
        }
        /* end while ... */

        /* remove the ones we don't want to keep */
        for ( GeneSetTerm deleteMe : deleteUs ) {
            ga.deleteGeneSet( deleteMe );
        }

        if ( messenger != null ) {
            messenger.showStatus( "There are now " + ga.numGeneSets() + " classes represented on the chip ("
                    + deleteUs.size() + " were ignored)" );
        }
    }

    /**
     * @param ga
     * @param gon
     * @param messenger
     * @param aspect
     */
    public static void removeAspect( GeneAnnotations ga, GeneSetTerms gon, StatusViewer messenger, String aspect ) {
        if ( !( aspect.equals( "molecular_function" ) || aspect.equals( "biological_process" )
                || aspect.equals( "cellular_component" ) || aspect.equals( GeneSetTerms.USER_DEFINED ) ) ) {
            throw new IllegalArgumentException( "Unknown aspect requested" );
        }

        Collection<GeneSetTerm> geneSets = ga.getNonEmptyGeneSets();

        Collection<GeneSetTerm> removeUs = new HashSet<GeneSetTerm>();
        for ( GeneSetTerm geneSet : geneSets ) {
            if ( geneSet.getAspect() == null ) {
                log.warn( "No aspect for " + geneSet );
                continue;
            }
            if ( geneSet.getAspect().equals( aspect ) ) {
                removeUs.add( geneSet );
            }
        }

        for ( GeneSetTerm geneSet : removeUs ) {
            ga.deleteGeneSet( geneSet );
        }

        if ( messenger != null ) {
            messenger.showStatus( "There are now " + ga.numGeneSets() + " sets remaining after removing aspect "
                    + aspect );
        }
    }

    /**
     * Remove gene sets that don't meet certain criteria.
     * 
     * @param ga
     * @param messenger
     * @param minClassSize
     * @param maxClassSize
     */
    public static void removeBySize( GeneAnnotations ga, StatusViewer messenger, int minClassSize, int maxClassSize ) {

        Collection<GeneSetTerm> geneSets = ga.getNonEmptyGeneSets();

        Collection<GeneSetTerm> removeUs = new HashSet<GeneSetTerm>();
        for ( GeneSetTerm geneSet : geneSets ) {
            Collection<Gene> element = ga.getGeneSetGenes( geneSet );
            if ( element.size() < minClassSize || element.size() > maxClassSize ) {
                removeUs.add( geneSet );
            }
        }

        for ( GeneSetTerm geneSet : removeUs ) {
            ga.deleteGeneSet( geneSet );
        }

        if ( messenger != null ) {
            messenger.showStatus( "There are now " + ga.numGeneSets()
                    + " sets remaining after removing sets with excluded sizes." );
        }
    }

    /**
     * Helper function for ignoreSimilar.
     */
    private static boolean areSimilarClasses( Collection<Gene> biggerClass, Collection<Gene> smallerClass,
            double fractionSameThreshold ) {

        if ( biggerClass.size() < smallerClass.size() ) {
            throw new IllegalArgumentException( "Invalid sizes" );
        }

        /*
         * Threshold of how many items from the smaller class must NOT be in the bigger class, before we consider the
         * classes different.
         */
        int notInThresh = ( int ) Math.ceil( fractionSameThreshold * smallerClass.size() );

        int notin = 0;

        int overlap = 0;
        for ( Iterator<Gene> iter = smallerClass.iterator(); iter.hasNext(); ) {

            Gene gene = iter.next();
            if ( !biggerClass.contains( gene ) ) {
                notin++;
            } else {
                overlap++;
            }
            if ( notin > notInThresh ) {
                // return false;
            }
        }

        if ( ( double ) overlap / ( double ) smallerClass.size() > fractionSameThreshold ) {
            // log.warn( "Small class of size " + smallerClass.size()
            // + " too much contained (overlap = " + overlap
            // + ") in large class of size " + biggerClass.size() );
            return true;
        }

        /* return true is the count is high enough */
        // return true;
        return false;
    }

    /**
     * @param classesToSimilarMap
     * @param queryClassId
     * @param targetClassId
     */
    private static void storeSimilarSets( Map<GeneSetTerm, Collection<GeneSetTerm>> classesToSimilarMap,
            GeneSetTerm queryClassId, GeneSetTerm targetClassId ) {
        if ( !classesToSimilarMap.containsKey( targetClassId ) ) {
            classesToSimilarMap.put( targetClassId, new HashSet<GeneSetTerm>() );
        }
        if ( !classesToSimilarMap.containsKey( queryClassId ) ) {
            classesToSimilarMap.put( queryClassId, new HashSet<GeneSetTerm>() );
        }
        classesToSimilarMap.get( queryClassId ).add( targetClassId );
        classesToSimilarMap.get( targetClassId ).add( queryClassId );
    }

} // end of class
