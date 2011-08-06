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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CancellationException;

import javax.help.UnsupportedOperationException;

import ubic.basecode.util.StatusViewer;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;

/**
 * Perform multiple test correction on class scores. Multiple test correction is based on the non-redundant set of gene
 * sets, to avoid overcorrecting.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class MultipleTestCorrector extends AbstractLongTask {
    private static final int DEFAULT_WY_TRIALS = 10000;
    protected static final Log log = LogFactory.getLog( MultipleTestCorrector.class );
    private List<GeneSetTerm> sortedclasses;
    private Map<GeneSetTerm, GeneSetResult> results;
    private Histogram hist;
    private GeneAnnotations geneData;
    private NumberFormat nf = NumberFormat.getInstance();
    private GeneScores geneScores;
    private SettingsHolder settings;
    private StatusViewer messenger;
    List<GeneSetTerm> toUseForMTC;
    Map<GeneSetTerm, Collection<GeneSetTerm>> usedToSkipped = new HashMap<GeneSetTerm, Collection<GeneSetTerm>>();

    public MultipleTestCorrector( SettingsHolder set, List<GeneSetTerm> sc, Histogram h, GeneAnnotations geneData,
            GeneScores geneScores, Map<GeneSetTerm, GeneSetResult> results, StatusViewer messenger ) {
        this.settings = set;
        this.sortedclasses = sc;
        this.results = results;
        this.hist = h;
        this.geneData = geneData;
        this.geneScores = geneScores;
        this.messenger = messenger;

        /*
         * Deal with redundancy. Make it so we can find the redundant ones at the end to put their corrected pvalues in.
         */
        toUseForMTC = new ArrayList<GeneSetTerm>(); // same order as sorted.

        Set<GeneSetTerm> skip = new HashSet<GeneSetTerm>(); // need this for fast lookup.
        for ( GeneSetTerm t : sortedclasses ) {

            assert t != null;

            if ( skip.contains( t ) ) continue;

            Collection<GeneSet> redundantGroups = geneData.getGeneSet( t ).getRedundantGroups();

            if ( !redundantGroups.isEmpty() ) {
                if ( !usedToSkipped.containsKey( t ) ) {
                    usedToSkipped.put( t, new HashSet<GeneSetTerm>() );
                }
                for ( GeneSet r : redundantGroups ) {
                    usedToSkipped.get( t ).add( r.getTerm() );
                    skip.add( r.getTerm() );
                }
            }

            toUseForMTC.add( t );
        }

        messenger.showStatus( toUseForMTC.size() + " sets will be used for multiple test correction; " + skip.size()
                + " redundant ones are lumped in." );

    }

    /**
     * Benjamini-Hochberg correction of pvalues. Default method, used for GUI
     * 
     * @param fdr double desired false discovery rate.
     */
    public void benjaminihochberg() {
        int numclasses = toUseForMTC.size();
        int n = numclasses;

        Collections.reverse( toUseForMTC ); // start from the worst class.

        for ( GeneSetTerm nextclass : toUseForMTC ) {
            GeneSetResult res = results.get( nextclass );
            double actual_p = res.getPvalue();

            double thisFDR = Math.min( actual_p * numclasses / n, 1.0 );

            res.setCorrectedPvalue( thisFDR ); // this is slightly broken when there are tied pvals.

            // fill in
            if ( usedToSkipped.containsKey( nextclass ) ) {
                for ( GeneSetTerm redund : usedToSkipped.get( nextclass ) ) {
                    res = results.get( redund );
                    if ( res == null ) {
                        log.warn( "No results for: " + redund );
                        continue;
                    }
                    res.setCorrectedPvalue( thisFDR );
                }
            }

            n--;
        }
        Collections.reverse( toUseForMTC ); // put it back
    }

    /**
     * Bonferroni correction of class pvalues.
     */
    public void bonferroni() {
        int numclasses = toUseForMTC.size();
        double corrected_p;
        for ( Iterator<GeneSetTerm> it = toUseForMTC.iterator(); it.hasNext(); ) {
            if ( Thread.currentThread().isInterrupted() ) break;
            GeneSetTerm nextclass = it.next();
            GeneSetResult res = results.get( nextclass );
            double actual_p = res.getPvalue();
            corrected_p = actual_p * numclasses;
            if ( corrected_p > 1.0 ) {
                corrected_p = 1.0;
            }

            res.setCorrectedPvalue( corrected_p );

            // fill in
            for ( GeneSetTerm redund : usedToSkipped.get( nextclass ) ) {
                res = results.get( redund );
                res.setCorrectedPvalue( corrected_p );
            }
        }
    }

    /**
     * Run WY with a default number of trials.
     * 
     * @see westfallyoung(numtrials)
     */
    public void westfallyoung() {
        if ( geneScores == null )
            throw new UnsupportedOperationException( "Can't run WY correction on correlated method results." );
        westfallyoung( DEFAULT_WY_TRIALS ); // default number of trials.
    }

    /**
     * Westfall-Young pvalue correction. Based on algorithm 2.8, pg 66 of 'Resampling-based Multiple Testing'.
     * <ol>
     * <li>Sort the pvalues for the real data (assume worst pvalue is first)
     * <li>Make an array of count variables, one for each class, intialize to zero. loop: (n=10,000).
     * <li>Generate class pvalues for randomized values (see above); 3. Iterate over this in the same order as the
     * actual order.
     * <li>Define successive minima: (q is the trial; p is real, already ranked)
     * <ol>
     * <li>a. qk = pk (class with worst pvalue)
     * <li>b. qk-1 = min (qk, pk-1) ...
     * </ol>
     * <li>at each step a.... if qi <= pi, count_i++ end loop.
     * <li>p_i* = count_i/n 7. enforce monotonicity by using successive maximization.
     * </ol>
     * 
     * @param trials How many random trials to do. According to W-Y, it should be >=10,000.
     * @todo get this working with the other types of scoring methods (ORA, ROC for example)
     */
    public void westfallyoung( int trials ) {

        int[] counts = new int[toUseForMTC.size()];
        for ( int i = 0; i < toUseForMTC.size(); i++ ) {
            counts[i] = 0;
        }

        Collections.reverse( toUseForMTC ); // start from the worst class.
        Map<GeneSetTerm, Double> permscores;

        GeneSetResamplingPvalGenerator cver = new GeneSetResamplingPvalGenerator( settings, geneData, hist, messenger );

        for ( int i = 0; i < trials; i++ ) {
            // System.err.println("Trial: " + i );

            // shuffle the association of pvalues to genes.
            // FIXME these should be multifunctionality corrected!!
            Map<Gene, Double> scgroup_pval_map = geneScores.getGeneToScoreMap( true );

            // / permscores contains a list of the p values for the shuffled data.
            permscores = cver.classPvalGeneratorRaw( scgroup_pval_map ); // end of step 1.

            int j = 0;
            double permp = 0.0;
            Double m = new Double( 1.0 );
            // Double m = (Double)permscores.get(j); // first sim value (for worst
            // class in real data)
            double q = m.doubleValue(); // pvalue for the previous permutation,
            // initialized here.
            double qprev = q;
            double actual_p = 0.0;
            GeneSetTerm nextclass = null;

            // successive minima of step 2, pg 66. Also does step 3.
            for ( Iterator<GeneSetTerm> it = toUseForMTC.iterator(); it.hasNext(); ) {

                ifInterruptedStop();

                /*
                 * going in the correct order for the 'real' data, starting from the worst class.
                 */
                if ( Thread.currentThread().isInterrupted() ) {
                    throw new CancellationException();
                }
                nextclass = it.next();

                GeneSetResult res = results.get( nextclass );
                actual_p = res.getPvalue(); // pvalue for this class on real data.

                m = permscores.get( nextclass );
                permp = m.doubleValue(); // randomized pvalue for this class.

                /*
                 * The best values for permp for this trial bubbles up. The way this works is that if two classes are
                 * highly correlated, their permuted pvalues will tend to be the same. Then, whatever decision is made
                 * here will tend to be the same decision made for the next (correlated class). That is how the
                 * resulting corrected p values for correlated classes are correlated.
                 */
                q = Math.min( qprev, permp );

                /* step 3 */
                if ( q <= actual_p ) { // for bad classes, this will often be true.
                    // Otherwise we see it less.
                    counts[j]++;
                }

                /*
                 * the following tests two classes which are very similar. Their permutation p values should be
                 * correlated
                 */
                /*
                 * if (nextclass.equals("GO:0006956")) { System.err.print("\tGO:0006956\t" + nf.format(permp) + "\n"); }
                 * if (nextclass.equals("GO:0006958")) { System.err.print("\tGO:0006958\t" + nf.format(permp)); }
                 */

                if ( log.isDebugEnabled() && j == toUseForMTC.size() - 1 ) {
                    /*
                     * monitor what happens to the best class.
                     */
                    System.err.println( "Sim " + i + " class# " + j + " " + nextclass + " size=" + res.getNumGenes()
                            + " q=" + nf.format( q ) + " qprev=" + nf.format( qprev ) + " pperm=" + nf.format( permp )
                            + " actp=" + nf.format( actual_p ) + " countj=" + counts[j] + " currentp="
                            + ( double ) counts[j] / ( i + 1 ) );

                }
                j++;
                qprev = q;
            }

            if ( i % 100 == 0 ) {
                ifInterruptedStop();
                try {
                    Thread.sleep( 10 );
                } catch ( InterruptedException e ) {
                    log.debug( "Interrupted" );
                    throw new CancellationException();
                }
            }

            if ( 0 == i % 100 ) {
                if ( messenger != null )
                    messenger.showStatus( i + " Westfall-Young trials, " + ( trials - i ) + " to go." );
            }

        }

        // now the best class is first.
        Collections.reverse( toUseForMTC );

        // index of the best class (last one
        // tested above).
        int j = toUseForMTC.size() - 1;

        /*
         * pvalue for the best class.
         */
        double corrected_p = counts[toUseForMTC.size() - 1] / trials;

        double previous_p = corrected_p;

        /*
         * Step 4 and enforce monotonicity, pg 67 (step 5) starting from the best class.
         */
        for ( Iterator<GeneSetTerm> it = toUseForMTC.iterator(); it.hasNext(); ) {
            if ( Thread.currentThread().isInterrupted() ) throw new CancellationException();
            GeneSetResult res = results.get( it.next() );
            corrected_p = Math.max( ( double ) counts[j] / ( double ) trials, previous_p ); // first iteration, these
            // are the same.

            if ( log.isDebugEnabled() ) log.debug( j + " " + counts[j] + " " + trials + " " + corrected_p );

            res.setCorrectedPvalue( corrected_p );

            // fill in the redundant ones.
            for ( GeneSetTerm redund : usedToSkipped.get( res.getGeneSetId() ) ) {
                res = results.get( redund );
                res.setCorrectedPvalue( corrected_p );
            }

            previous_p = corrected_p;
            j--;
        }
    }

}