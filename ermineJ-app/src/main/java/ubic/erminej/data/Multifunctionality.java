/*
 * The baseCode project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.list.DoubleArrayList;

import ubic.basecode.math.Distance;
import ubic.basecode.math.Rank;

/**
 * Implementation of multifunctionality computations as described in Gillis and Pavlidis (2011) PLoS ONE 6:2:e17258.
 * This is designed with ErmineJ in mind.
 * 
 * @author paul
 * @version $Id$
 */
public class Multifunctionality {

    private static Log log = LogFactory.getLog( Multifunctionality.class );

    private Map<Gene, Double> multifunctionality = new HashMap<Gene, Double>();

    private Map<GeneSetTerm, Integer> goGroupSizes = new HashMap<GeneSetTerm, Integer>();

    private Map<Gene, Integer> numGoTerms = new HashMap<Gene, Integer>();

    private Map<GeneSetTerm, Double> goTermMultifunctionality = new HashMap<GeneSetTerm, Double>();

    private Map<GeneSetTerm, Double> goTermMultifunctionalityRank = new HashMap<GeneSetTerm, Double>();

    private Map<Gene, Double> multifunctionalityRank = new HashMap<Gene, Double>();

    private GeneAnnotations geneAnnotations;

    private Collection<Gene> genesWithGoTerms;

    private AtomicBoolean stale = new AtomicBoolean( true );

    /**
     * Construct Multifunctionality information based on the state of the GO annotations -- this accounts only for the
     * 'active' (used) probes in the annotations. Genes with no GO terms are completely ignored.
     * 
     * @param go
     */
    public Multifunctionality( GeneAnnotations go ) {
        this.geneAnnotations = go;
        init();
    }

    /**
     * @param rankedGenes, with the "best" gene first.
     * @return the rank correlation of the given list with the ranks of the multifunctionality of the genes. A positive
     *         correlation means the given list is "multifunctionality-biased". Genes lacking GO terms are ignored.
     */
    public double correlationWithGeneMultifunctionality( List<Gene> rankedGenes ) {

        DoubleArrayList rawVals = new DoubleArrayList();
        for ( Gene gene : rankedGenes ) {
            if ( !this.multifunctionality.containsKey( gene ) ) continue;
            double mf = this.getMultifunctionalityScore( gene );
            rawVals.add( mf );
        }

        return -Distance.spearmanRankCorrelation( rawVals );
    }

    /**
     * @param rankedGoTerms, with the "best" GO term first.
     * @return the rank correlation of the given list with the ranks of the GO term multifunctionality of the terms. A
     *         positive correlation means the given list of terms is "multifunctionality-biased".
     */
    public double correlationWithGoTermMultifunctionality( List<GeneSetTerm> rankedGoTerms ) {
        DoubleArrayList rawVals = new DoubleArrayList();
        for ( GeneSetTerm goTerm : rankedGoTerms ) {
            double mf = this.getGOTermMultifunctionality( goTerm );
            rawVals.add( mf );
        }
        return -Distance.spearmanRankCorrelation( rawVals );
    }

    /**
     * @param goId
     * @return the computed multifunctionality score for the GO term. This is the area under the ROC curve for the genes
     *         in the group, in the ranking of all genes for multifunctionality. Higher values indicate higher
     *         multifunctionality
     */
    public double getGOTermMultifunctionality( GeneSetTerm goId ) {
        if ( stale.get() ) init();
        if ( !this.goTermMultifunctionality.containsKey( goId ) ) {
            // log.warn( "GO term: " + goId + " not found" );
            return -1;
        }

        return this.goTermMultifunctionality.get( goId );
    }

    /**
     * @param goId
     * @return the relative rank of the GO group in multifunctionality, where 1 is the highest multifunctionality, 0 is
     *         lowest
     */
    public double getGOTermMultifunctionalityRank( GeneSetTerm goId ) {
        if ( stale.get() ) init();
        if ( !this.goTermMultifunctionalityRank.containsKey( goId ) ) {
            // throw new IllegalArgumentException( "GO term: " + goId + " not found" );
            return -1;
        }

        return this.goTermMultifunctionalityRank.get( goId );
    }

    /**
     * Convenience method
     * 
     * @param genes
     * @return the gene with the highest multifunctionality
     */
    public Gene getMostMultifunctional( Collection<Gene> genes ) {
        if ( genes.isEmpty() ) throw new IllegalArgumentException();
        if ( genes.size() == 1 ) return genes.iterator().next();

        double maxMf = -1.0;
        Gene maxMfGene = null;
        for ( Gene g : genes ) {
            double mf = this.getMultifunctionalityRank( g );
            if ( mf > maxMf ) {
                maxMf = mf;
                maxMfGene = g;
            }
        }
        return maxMfGene;
    }

    /**
     * @param gene
     * @return relative rank of the gene in multifunctionality where 1 is the highest multifunctionality, 0 is lowest
     */
    public double getMultifunctionalityRank( Gene gene ) {
        if ( stale.get() ) init();
        if ( !this.multifunctionalityRank.containsKey( gene ) ) {
            // throw new IllegalArgumentException( "Gene: " + gene + " not found" );
            return 0.0;
        }

        return this.multifunctionalityRank.get( gene );
    }

    /**
     * @param gene
     * @return multifunctionality score. Note that this score by itself is not all that useful; use the rank instead.
     *         Higher values indicate higher multifunctionality
     */
    public double getMultifunctionalityScore( Gene gene ) {
        if ( stale.get() ) init();
        if ( !this.multifunctionality.containsKey( gene ) ) {
            // throw new IllegalArgumentException( "Gene: " + gene + " not found" );
            return 0.0;
        }

        return this.multifunctionality.get( gene );
    }

    /**
     * @param gene
     * @return number of GO terms for the given gene.
     */
    public int getNumGoTerms( Gene gene ) {
        if ( stale.get() ) init();
        if ( !this.numGoTerms.containsKey( gene ) ) {
            // throw new IllegalArgumentException( "Gene: " + gene + " not found" );
            return 0;
        }
        return this.numGoTerms.get( gene );
    }

    public boolean isStale() {
        return stale.get();
    }

    public void setStale( boolean stale ) {
        this.stale.set( stale );
    }

    /**
     * Implementation of algorithm for computing AUC, described in Section 1 of the supplement to Gillis and Pavlidis;
     * see {@link http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U}.
     * 
     * @param rawGeneMultifunctionalityRanks in descending order
     */
    private void computeGoTermMultifunctionalityRanks( Map<Gene, Integer> rawGeneMultifunctionalityRanks ) {
        int numGenes = genesWithGoTerms.size();
        int numGoGroups = geneAnnotations.getActiveGeneSets().size();
        /*
         * For each go term, compute it's AUC w.r.t. the multifunctionality ranking.. We work with the
         * multifunctionality ranks, rawGeneMultifunctionalityRanks
         */
        for ( GeneSetTerm goset : geneAnnotations.getActiveGeneSets() ) {

            if ( !goGroupSizes.containsKey( goset ) ) {
                log.debug( "No size recorded for: " + goset );
                continue;
            }

            int inGroup = goGroupSizes.get( goset );
            int outGroup = numGenes - inGroup;

            if ( outGroup == 0 ) {
                continue;
            }

            double t1 = inGroup * ( inGroup + 1.0 ) / 2.0;

            double t2 = inGroup * outGroup;

            /*
             * Extract the ranks of the genes in the goset, where highest ranking is the best.
             */
            double sumOfRanks = 0.0;
            for ( Gene gene : geneAnnotations.getGeneSetGenes( goset ) ) {
                int rank = rawGeneMultifunctionalityRanks.get( gene ) + 1; // +1 cuz ranks are zero-based.
                sumOfRanks += rank;
            }

            double t3 = sumOfRanks - t1;

            double auc = Math.max( 0.0, 1.0 - t3 / t2 );

            assert auc >= 0.0 && auc <= 1.0 : "AUC was " + auc;
            goTermMultifunctionality.put( goset, auc );
        }

        // convert to relative ranks, where 1.0 is the most multifunctional
        Map<GeneSetTerm, Integer> rankedGOMf = Rank.rankTransform( this.goTermMultifunctionality, true );
        for ( GeneSetTerm goTerm : rankedGOMf.keySet() ) {
            double rankRatio = ( rankedGOMf.get( goTerm ) + 1 ) / ( double ) numGoGroups;
            this.goTermMultifunctionalityRank.put( goTerm, Math.max( 0.0, 1 - rankRatio ) );
        }
    }

    /**
     * @param geneAnnotations
     */
    private synchronized void init() {

        if ( !this.isStale() ) return;

        try {
            StopWatch timer = new StopWatch();

            timer.start();

            genesWithGoTerms = new HashSet<Gene>();
            for ( GeneSetTerm goset : geneAnnotations.getActiveGeneSets() ) {
                Collection<Gene> geneSetGenes = geneAnnotations.getGeneSetGenes( goset );
                if ( geneSetGenes.isEmpty() ) continue;
                genesWithGoTerms.addAll( geneSetGenes );
                goGroupSizes.put( goset, geneSetGenes.size() );
            }

            int numGenes = genesWithGoTerms.size();

            for ( Gene gene : geneAnnotations.getGenes() ) {
                if ( !genesWithGoTerms.contains( gene ) ) continue;

                double mf = 0.0;
                Collection<GeneSetTerm> sets = gene.getGeneSets();
                this.numGoTerms.put( gene, sets.size() ); // genes with no go terms are ignored.
                for ( GeneSetTerm goset : sets ) {
                    if ( !goGroupSizes.containsKey( goset ) ) {
                        // log.debug( "No size recorded for " + goset );
                        continue;
                    }
                    int inGroup = goGroupSizes.get( goset );
                    int outGroup = numGenes - inGroup;

                    if ( outGroup == 0 ) {
                        // log.debug( "GO group '" + goset
                        // + "' that all genes belong to detected, skipping in multifunctionality computation" );
                        continue;
                    }

                    assert inGroup > 0;

                    mf += 1.0 / ( inGroup * outGroup );
                }
                this.multifunctionality.put( gene, mf );
            }

            Map<Gene, Integer> rawGeneMultifunctionalityRanks = Rank.rankTransform( this.multifunctionality, true );
            for ( Gene gene : rawGeneMultifunctionalityRanks.keySet() ) {
                // 1-base the rank before calculating ratio
                double geneMultifunctionalityRankRatio = ( rawGeneMultifunctionalityRanks.get( gene ) + 1 )
                        / ( double ) numGenes;
                this.multifunctionalityRank.put( gene, Math.max( 0.0, 1.0 - geneMultifunctionalityRankRatio ) );
            }

            computeGoTermMultifunctionalityRanks( rawGeneMultifunctionalityRanks );

            if ( timer.getTime() > 1000 ) {
                log.info( "Multifunctionality computation: " + timer.getTime() + "ms" );
            }
        } finally {
            stale.set( false );
        }
    }
}
