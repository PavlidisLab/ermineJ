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

import hep.aida.bin.QuantileBin1D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;
import cern.jet.stat.Probability;

import ubic.basecode.dataStructure.matrix.MatrixUtil;
import ubic.basecode.math.Distance;
import ubic.basecode.math.LeastSquaresFit;
import ubic.basecode.math.ROC;
import ubic.basecode.math.Rank;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;

/**
 * Implementation of multifunctionality computations as described in Gillis and Pavlidis (2011) PLoS ONE 6:2:e17258.
 * This is designed with ErmineJ in mind.
 * 
 * @author paul
 * @version $Id$
 */
public class Multifunctionality {

    /**
     * Should we just use the plain old AU-ROC (which ignores the gene set size), or the pvalue for the AU-ROC (which
     * needs to be scaled in an ad-hoc way to be reasonable)
     */
    private static final boolean USE_AUC_FOR_GENE_SET_MF = true;

    /**
     * Do we count genes that don't have GO terms? .
     */
    private static final boolean USE_UNANNOTATED_GENES = true;

    private static Log log = LogFactory.getLog( Multifunctionality.class );

    private Map<Gene, Double> geneMultifunctionality = new HashMap<Gene, Double>();

    private Map<GeneSetTerm, Integer> goGroupSizes = new HashMap<GeneSetTerm, Integer>();

    private Map<Gene, Integer> numGoTerms = new HashMap<Gene, Integer>();

    private Map<GeneSetTerm, Double> goTermMultifunctionality = new HashMap<GeneSetTerm, Double>();

    private Map<GeneSetTerm, Double> goTermMultifunctionalityRank = new HashMap<GeneSetTerm, Double>();

    private Map<Gene, Double> geneMultifunctionalityRank = new HashMap<Gene, Double>();

    private GeneAnnotations geneAnnots;

    private Collection<Gene> genesWithGoTerms;

    private StatusViewer messenger = new StatusStderr();

    private AtomicBoolean stale = new AtomicBoolean( true );

    private QuantileBin1D quantiles = null;

    private Map<Gene, Double> rawGeneMultifunctionalityRanks;

    /**
     * Construct Multifunctionality information based on the state of the GO annotations -- this accounts only for the
     * probes in the annotations. Genes with no GO terms are completely ignored.
     * 
     * @param go These annotations should already be pruned down to those used in analysis.
     */
    public Multifunctionality( GeneAnnotations go, StatusViewer m ) {
        this.geneAnnots = go;
        if ( m != null ) this.messenger = m;
        init();
    }

    /**
     * @param geneToScoreMap
     * @param useRanks If true, the ranks of the gene scores will be used for regression.
     * @return
     */
    public Map<Gene, Double> adjustScores( Map<Gene, Double> geneToScoreMap, boolean useRanks ) {

        DoubleMatrix1D scores = new DenseDoubleMatrix1D( geneToScoreMap.size() );
        DoubleMatrix1D mfs = new DenseDoubleMatrix1D( geneToScoreMap.size() );

        List<Gene> genesInSomeOrder = new ArrayList<Gene>( geneToScoreMap.keySet() );

        int i = 0;
        for ( Gene g : genesInSomeOrder ) {
            Double mf = this.getMultifunctionalityRank( g );
            Double s = geneToScoreMap.get( g );
            scores.set( i, s );
            mfs.set( i, mf );
            i++;
        }

        LeastSquaresFit fit;
        if ( useRanks ) {
            DoubleMatrix1D scoreRanks = MatrixUtil.fromList( Rank.rankTransform( MatrixUtil.toList( scores ) ) );

            if ( scoreRanks == null ) {
                throw new IllegalStateException( "Ranks were null" );
            }

            scoreRanks.assign( Functions.div( scoreRanks.size() ) );
            fit = new LeastSquaresFit( mfs, scoreRanks );
        } else {
            fit = new LeastSquaresFit( mfs, scores );
        }

        // log.info( fit.getCoefficients() );
        if ( fit.getCoefficients().get( 1, 0 ) < 0 ) {
            messenger.showStatus( "Multifunctionality correction skipped: correlation is negative" );
            return geneToScoreMap;
        }

        // This does not deal with missing values.
        DoubleMatrix1D residuals = fit.getStudentizedResiduals().viewRow( 0 );

        Map<Gene, Double> result = new HashMap<Gene, Double>();

        for ( i = 0; i < residuals.size(); i++ ) {
            result.put( genesInSomeOrder.get( i ), residuals.get( i ) );
        }

        return result;

    }

    /**
     * @param rankedGenes, with the "best" gene first.
     * @return the rank correlation of the given list with the ranks of the multifunctionality of the genes. A positive
     *         correlation means the given list is "multifunctionality-biased". Genes lacking GO terms are ignored.
     */
    public double correlationWithGeneMultifunctionality( List<Gene> rankedGenes ) {

        DoubleArrayList rawVals = new DoubleArrayList();
        for ( Gene gene : rankedGenes ) {
            if ( !this.geneMultifunctionality.containsKey( gene ) ) continue;
            double mf = this.getMultifunctionalityScore( gene );
            rawVals.add( mf );
        }

        /*
         * Note that the multifunctionality scores are "bigger is better". Thus, because we are iterating over the genes
         * in "bigger to smaller" sort order, we are expecting a positive correlation if there is bias. Thus we have to
         * take the negative.
         */

        double r = -Distance.spearmanRankCorrelation( rawVals );
        return r;
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
     * This is like correlationWithGeneMultifunctionality(List<Gene>), but without an order list to start with, and
     * intended for cases where the number of genes is smaller than the total number of genes. We do this the same we we
     * do for GO groups. Implementation of algorithm for computing AUC, described in Section 1 of the supplement to
     * Gillis and Pavlidis; see {@link http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U}.
     * 
     * @param genesInSet
     * @return the ROC AUC for the genes in the set compared to the multifunctionality ranking
     * @see ROC.java in baseCode for a generic implementation
     */
    public double enrichmentForMultifunctionality( Collection<Gene> genesInSet ) {

        double sumOfRanks = 0.0;
        int inGroup = 0;
        for ( Gene gene : genesInSet ) {
            if ( rawGeneMultifunctionalityRanks.containsKey( gene ) ) {
                inGroup++;
            }
        }

        int numGenes = USE_UNANNOTATED_GENES ? rawGeneMultifunctionalityRanks.size() : genesWithGoTerms.size();

        int outGroup = numGenes - inGroup;

        if ( outGroup <= 0 ) return 0.0;

        double t1 = inGroup * ( inGroup + 1.0 ) / 2.0;
        double t2 = inGroup * outGroup;

        for ( Gene gene : genesInSet ) {
            if ( rawGeneMultifunctionalityRanks.containsKey( gene ) ) {
                double rank = rawGeneMultifunctionalityRanks.get( gene ) + 1; // +1 cuz ranks are zero-based.
                sumOfRanks += rank;
            }
        }

        double t3 = sumOfRanks - t1;

        double auc = Math.max( 0.0, 1.0 - t3 / t2 );

        assert auc >= 0.0 && auc <= 1.0 : "AUC was " + auc;

        return auc;
    }

    /**
     * @param genesInSet
     * @return The pvalue associated with the ROC AUC for the genes in the set compared to the multifunctionality
     *         ranking, based on the Mann-Whitney U test / Wilcoxon
     */
    public double enrichmentForMultifunctionalityPvalue( Collection<Gene> genesInSet ) {

        List<Double> ranksOfGeneInSet = new Vector<Double>();

        for ( Gene gene : genesInSet ) {
            if ( rawGeneMultifunctionalityRanks.containsKey( gene ) ) {
                double rank = rawGeneMultifunctionalityRanks.get( gene ) + 1; // +1 cuz ranks are zero-based.
                ranksOfGeneInSet.add( rank );
            }
        }

        return ROC.rocpval( genesWithGoTerms.size(), ranksOfGeneInSet );
    }

    /**
     * Get QuantileBin1D, which can tell you the quantile for a given value, or the expected value for a given quantile.
     * 
     * @return
     */
    public QuantileBin1D getGeneMultifunctionalityQuantiles() {
        if ( this.quantiles == null ) {

            this.quantiles = new QuantileBin1D( true, this.geneMultifunctionality.size(), 0.0, 0.0, 1000,
                    new cern.jet.random.engine.DRand() );

            quantiles.addAllOf( new DoubleArrayList( ArrayUtils.toPrimitive( this.geneMultifunctionality.values()
                    .toArray( new Double[] {} ) ) ) );

        }
        return quantiles;
    }

    /**
     * @param goId
     * @return the computed multifunctionality score for the GO term.
     *         <p>
     *         This is computed from the area under the ROC curve for the genes in the group, in the ranking of all
     *         genes for multifunctionality. The exact method for computing this is defined by
     *         computeGoTermMultifunctionalityRanks
     */
    public double getGOTermMultifunctionality( GeneSetTerm goId ) {
        if ( stale.get() ) init();
        if ( !this.goTermMultifunctionality.containsKey( goId ) ) {
            return -1;
        }

        return this.goTermMultifunctionality.get( goId );
    }

    /**
     * @param goId
     * @return the relative rank of the GO group in multifunctionality, where 1 is the highest multifunctionality, 0 is
     *         lowest. <strong>WARNING</strong>, this does not correct for the presence of multiple GO groups with the
     *         same genes (redundancy)
     */
    public double getGOTermMultifunctionalityRank( GeneSetTerm goId ) {
        if ( stale.get() ) init();
        if ( !this.goTermMultifunctionalityRank.containsKey( goId ) ) {
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
        if ( !this.geneMultifunctionalityRank.containsKey( gene ) ) {
            // throw new IllegalArgumentException( "Gene: " + gene + " not found" );
            return 0.0;
        }

        return this.geneMultifunctionalityRank.get( gene );
    }

    /**
     * @param gene
     * @return multifunctionality score. Note that this score by itself is not all that useful; use the rank instead; or
     *         for a "human-readable" version use the number of GO terms, which this approximates (in terms of ranks).
     *         Higher values indicate higher multifunctionality
     */
    public double getMultifunctionalityScore( Gene gene ) {
        if ( stale.get() ) init();
        if ( !this.geneMultifunctionality.containsKey( gene ) ) {
            // throw new IllegalArgumentException( "Gene: " + gene + " not found" );
            return 0.0;
        }

        return this.geneMultifunctionality.get( gene );
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
     * 
     */
    private void computeGoTermMultifunctionalityRanks() {

        /*
         * This value is picked to be small enough to give us a spread of values that go above this...
         */
        double minPvalue = 1e-100;
        double maxProbit = Math.abs( Probability.normalInverse( minPvalue ) );

        StopWatch timer = new StopWatch();
        timer.start();

        int numGenes = USE_UNANNOTATED_GENES ? rawGeneMultifunctionalityRanks.size() : genesWithGoTerms.size();

        int numGoGroups = geneAnnots.getGeneSetTerms().size();

        /*
         * For each go term, compute it's AUC w.r.t. the multifunctionality ranking.. We work with the
         * multifunctionality ranks, rawGeneMultifunctionalityRanks
         */
        for ( GeneSetTerm goset : geneAnnots.getGeneSetTerms() ) {

            if ( !goGroupSizes.containsKey( goset ) ) {
                log.debug( "No size recorded for: " + goset );
                continue;
            }

            Set<Gene> genesInSet = geneAnnots.getGeneSetGenes( goset );
            int inGroup = genesInSet.size();
            int outGroup = numGenes - inGroup;

            assert inGroup >= GeneAnnotations.ABSOLUTE_MINIMUM_GENESET_SIZE;

            // check for pathological condition
            if ( outGroup == 0 ) {
                continue;
            }

            double mfScore = 0.0;
            if ( USE_AUC_FOR_GENE_SET_MF ) {
                /*
                 * This method doesn't take into account the size of the gene set, but it _does_ reflect how high the
                 * genes are ranked.
                 */
                double auc = enrichmentForMultifunctionality( genesInSet );
                mfScore = auc;
            } else {
                /*
                 * Note that this is really rather difficult to "get right". Large groups very easily get good p-values.
                 */
                double aucp = enrichmentForMultifunctionalityPvalue( genesInSet );
                assert aucp >= 0.0 && aucp <= 1.0;

                double probit = 1.0;
                if ( aucp == 1.0 ) {
                    probit = 0.0;
                } else if ( aucp > minPvalue ) {
                    probit = Math.abs( Probability.normalInverse( aucp ) ) / maxProbit;
                }

                if ( log.isDebugEnabled() ) log.debug( String.format( "%.3f\t%.2g", probit, aucp ) );

                mfScore = probit;
            }

            goTermMultifunctionality.put( goset, mfScore );
        }

        if ( timer.getTime() > 1000 ) {
            log.info( "Multifunctionality enrichment of GO groups computed in " + timer.getTime() + "ms" );
        }

        // convert to relative ranks, where 1.0 is the most multifunctional; ties are broken by averaging.
        Map<GeneSetTerm, Double> rankedGOMf = Rank.rankTransform( this.goTermMultifunctionality, true );
        for ( GeneSetTerm goTerm : rankedGOMf.keySet() ) {
            double rankRatio = ( rankedGOMf.get( goTerm ) + 1 ) / numGoGroups;
            this.goTermMultifunctionalityRank.put( goTerm, Math.max( 0.0, 1 - rankRatio ) );
        }
    }

    /**
     * @param geneAnnots
     */
    private synchronized void init() {

        if ( !this.isStale() ) return;

        try {
            StopWatch timer = new StopWatch();

            timer.start();

            genesWithGoTerms = new HashSet<Gene>();
            for ( GeneSetTerm goset : geneAnnots.getGeneSetTerms() ) {
                Collection<Gene> geneSetGenes = geneAnnots.getGeneSetGenes( goset );
                if ( geneSetGenes.isEmpty() ) continue;
                genesWithGoTerms.addAll( geneSetGenes );
                goGroupSizes.put( goset, geneSetGenes.size() );
            }

            int numGenes = USE_UNANNOTATED_GENES ? this.geneAnnots.getGenes().size() : genesWithGoTerms.size();

            for ( Gene gene : geneAnnots.getGenes() ) {

                boolean geneHasAnnots = genesWithGoTerms.contains( gene );

                if ( !geneHasAnnots && !USE_UNANNOTATED_GENES ) continue;

                double mf = 0.0;
                Collection<GeneSetTerm> sets = gene.getGeneSets();
                this.numGoTerms.put( gene, sets.size() );
                for ( GeneSetTerm goset : sets ) {
                    int inGroup = 0;
                    if ( !goGroupSizes.containsKey( goset ) ) {
                        if ( !USE_UNANNOTATED_GENES ) {
                            continue;
                        }
                    } else {
                        inGroup = goGroupSizes.get( goset );
                    }

                    int outGroup = numGenes - inGroup;

                    if ( outGroup == 0 ) {
                        continue;
                    }

                    assert inGroup > 0 || USE_UNANNOTATED_GENES;

                    mf += 1.0 / ( inGroup * outGroup );
                    // mf += 1.0; // count of go terms ONLY, if you ever want to compare ...
                }
                this.geneMultifunctionality.put( gene, mf );
            }

            rawGeneMultifunctionalityRanks = Rank.rankTransform( this.geneMultifunctionality, true );
            assert numGenes == rawGeneMultifunctionalityRanks.size();
            for ( Gene gene : rawGeneMultifunctionalityRanks.keySet() ) {
                // 1-base the rank before calculating ratio
                double geneMultifuncationalityRelativeRank = ( rawGeneMultifunctionalityRanks.get( gene ) + 1 )
                        / numGenes;
                assert geneMultifuncationalityRelativeRank >= 0.0 && geneMultifuncationalityRelativeRank <= 1.0;
                this.geneMultifunctionalityRank.put( gene, Math.max( 0.0, 1.0 - geneMultifuncationalityRelativeRank ) );
            }

            computeGoTermMultifunctionalityRanks();

            if ( timer.getTime() > 1000 ) {
                log.info( "Multifunctionality computation: " + timer.getTime() + "ms" );
            }
        } finally {
            stale.set( false );
        }
    }

    /**
     * @param gene
     * @return rank. Where zero is the highest rank, ties accounted for.
     */
    public Double getRawGeneMultifunctionalityRank( Gene gene ) {
        return this.rawGeneMultifunctionalityRanks.get( gene );
    }

    /**
     * How many genes have multifunctionality scores.
     * 
     * @return
     */
    public int getNumGenes() {
        return new Double( rawGeneMultifunctionalityRanks.size() ).intValue();
    }

}
