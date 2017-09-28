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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;
import cern.jet.stat.Descriptive;
import hep.aida.bin.QuantileBin1D;
import ubic.basecode.dataStructure.matrix.MatrixUtil;
import ubic.basecode.math.Distance;
import ubic.basecode.math.ROC;
import ubic.basecode.math.Rank;
import ubic.basecode.math.linearmodels.LeastSquaresFit;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.SettingsHolder;

/**
 * Implementation of multifunctionality computations as described in Gillis and Pavlidis (2011) PLoS ONE 6:2:e17258.
 * <p>
 * Note: This computes multifunctionality at 'startup' based on all the annotations available, not necessarily the ones
 * selected for analysis. For example, if you load KEGG groups along with GO, multifunctionality is computed based on
 * both of them. But if the user selects only to analyze KEGG, multifunctionality correction is based on the bias in
 * KEGG+GO. This could be fixed by changing the 'subclone' method of GeneAnnotations to trim aspects, not just genes.
 *
 * see Gemma for another implementation of parts of this.
 *
 * @author paul
 * @version $Id: $Id
 */
public class Multifunctionality {
    //
    // /**
    // * Should we just use the plain old AU-ROC (which ignores the gene set size), or the pvalue for the AU-ROC (which
    // * needs to be scaled in an ad-hoc way to be reasonable)
    // */
    // private static final boolean USE_AUC_FOR_GENE_SET_MF = false;

    /**
     * Largest group that we consider for computing multifunctionality. This mostly affects how we compute rankings of
     * the terms.
     */
    private static final int MAX_GROUP_SIZE_FOR_MF = 2000;

    /**
     * Do we count genes that don't have GO terms? .
     */
    // private static final boolean USE_UNANNOTATED_GENES = true;

    private static Log log = LogFactory.getLog( Multifunctionality.class );

    private Map<Gene, Double> geneMultifunctionality = new HashMap<>();

    private Map<GeneSetTerm, Integer> goGroupSizes = new HashMap<>();

    private Map<Gene, Integer> numGoTerms = new HashMap<>();

    private Map<GeneSetTerm, Double> goTermMultifunctionality = new HashMap<>();

    private Map<GeneSetTerm, Double> goTermMultifunctionalityPvalue = new HashMap<>();

    private Map<GeneSetTerm, Double> goTermMultifunctionalityRank = new HashMap<>();

    private Map<Gene, Double> geneMultifunctionalityRank = new HashMap<>();

    private GeneAnnotations geneAnnots;

    private Collection<Gene> genesWithGoTerms;

    private StatusViewer messenger = new StatusStderr();

    private AtomicBoolean stale = new AtomicBoolean( true );

    private QuantileBin1D quantiles = null;

    private Map<Gene, Double> rawGeneMultifunctionalityRanks;

    /**
     * Construct Multifunctionality information based on the state of the GO annotations -- this accounts only for the
     * elements in the annotations. Genes with no GO terms are completely ignored.
     *
     * @param go These annotations should already be pruned down to those used in analysis.
     * @param m a {@link ubic.basecode.util.StatusViewer} object.
     */
    public Multifunctionality( GeneAnnotations go, StatusViewer m ) {
        this.geneAnnots = go;
        if ( m != null ) this.messenger = m;
        init();
    }

    /**
     * Given a set of scores for genes, adjust them to correct for multifunctionality, using a regression approach. If
     * the regression coefficient (slope) is negative, no correction will be done.
     *
     * @param geneToScoreMap Should already be log transformed, if requested.
     * @param useRanks If true, the ranks of the gene scores will be used for regression.
     * @param weight If true, the regression will be weighted (current implementation is by 1/sqrt(rank))
     * @return residuals from the regression, which are to be used as the new scores.
     */
    public Map<Gene, Double> adjustScores( Map<Gene, Double> geneToScoreMap, boolean useRanks, boolean weight ) {

        DoubleMatrix1D scores = new DenseDoubleMatrix1D( geneToScoreMap.size() );
        DoubleMatrix1D mfs = new DenseDoubleMatrix1D( geneToScoreMap.size() );

        SettingsHolder settings = geneAnnots.getSettings();
        boolean doLog = settings.getDoLog(); // note scores would already have been log-transformed.
        boolean invert = ( doLog && !settings.getBigIsBetter() ) || ( !doLog && settings.getBigIsBetter() );

        List<Gene> genesInSomeOrder = new ArrayList<>( geneToScoreMap.keySet() );

        int i = 0;
        for ( Gene g : genesInSomeOrder ) {
            Double mf = this.getMultifunctionalityRank( g );
            Double s = geneToScoreMap.get( g );

            scores.set( i, s );
            mfs.set( i, mf );
            i++;
        }

        LeastSquaresFit fit;
        DoubleArrayList rawRanks = Rank.rankTransform( MatrixUtil.toList( scores ), invert );

        // experimenting with weightings. 1/rank is too much. We've tried a few things:

        // 1/Rank
        // DoubleMatrix1D weights = MatrixUtil.fromList( rawRanks ).assign( Functions.inv );

        // 1 - Rank/N
        // DoubleMatrix1D weights = MatrixUtil.fromList( rawRanks ).assign( Functions.div( rawRanks.size() ) )
        // .assign( Functions.sign ).assign( Functions.plus( 1.0 ) );

        /*
         * 1/sqrt(rank). Jesse's argument for this is that this mimics the effect of uncertainty in the area under the
         * PR curve, which (asymptotically) has variance proportional to sqrt(n).
         */
        DoubleMatrix1D weights = MatrixUtil.fromList( rawRanks ).assign( Functions.inv ).assign( Functions.sqrt );

        /*
         * DEBUGGING CODE
         */
        // i = 0;
        // for ( Gene g : genesInSomeOrder ) {
        // double w = weights.get( i );
        // double s = scores.get( i );
        // double rr = rawRanks.get( i );
        // Double mf = mfs.get( i );
        // if ( s > 20 ) {
        // System.err.println( String.format( "%s\t%.4f\t%.4f\t%.8f\t%.8f", g.getSymbol(), mf, s, w, rr ) );
        // }
        // i++;
        // }

        if ( useRanks ) {
            DoubleMatrix1D ranks = MatrixUtil.fromList( rawRanks );
            if ( weight ) {
                fit = new LeastSquaresFit( mfs, ranks, weights );
            } else {
                fit = new LeastSquaresFit( mfs, ranks );
            }
        } else {
            if ( weight ) {
                fit = new LeastSquaresFit( mfs, scores, weights );
            } else {
                fit = new LeastSquaresFit( mfs, scores );
            }
        }

        // log.info( fit.getCoefficients() );
        if ( fit.getCoefficients().get( 1, 0 ) < 0 ) {
            messenger.showStatus( String.format(
                    "Multifunctionality correction skipped: coefficient is negative: %.2f ",
                    fit.getCoefficients().get( 1, 0 ) ) );
            return geneToScoreMap;
        }

        /*
         * The studentized residuals are normalized; this doesn't make _that_ much of a difference.
         */
        DoubleMatrix1D residuals = fit.getStudentizedResiduals().viewRow( 0 );
        // DoubleMatrix1D residuals = fit.getResiduals().viewRow( 0 );

        Map<Gene, Double> result = new HashMap<>();

        assert residuals.size() == genesInSomeOrder.size();

        for ( i = 0; i < residuals.size(); i++ ) {
            result.put( genesInSomeOrder.get( i ), residuals.get( i ) );
        }

        return result;

    }

    /**
     * <p>
     * correlationWithGeneMultifunctionality.
     * </p>
     *
     * @return the rank correlation of the given list with the ranks of the multifunctionality of the genes. A positive
     *         correlation means the given list is "multifunctionality-biased". Genes lacking GO terms are ignored.
     * @param rankedGenes a {@link java.util.List} object.
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
     * <p>
     * correlationWithGoTermMultifunctionality.
     * </p>
     *
     * @return the rank correlation of the given list with the ranks of the GO term multifunctionality of the terms. A
     *         positive correlation means the given list of terms is "multifunctionality-biased".
     * @param rankedGoTerms a {@link java.util.List} object.
     */
    public double correlationWithGoTermMultifunctionality( List<GeneSetTerm> rankedGoTerms ) {
        DoubleArrayList rawVals = new DoubleArrayList();
        for ( GeneSetTerm goTerm : rankedGoTerms ) {
            double mf = -Math.log10( this.getGOTermMultifunctionalityPvalue( goTerm ) );
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
     * @param genesInSet a {@link java.util.Collection} object.
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

        // int numGenes = USE_UNANNOTATED_GENES ? rawGeneMultifunctionalityRanks.size() : genesWithGoTerms.size();
        int numGenes = rawGeneMultifunctionalityRanks.size();

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
     * <p>
     * enrichmentForMultifunctionalityPvalue.
     * </p>
     *
     * @param genesInSet a {@link java.util.Collection} object.
     * @return The pvalue associated with the ROC AUC for the genes in the set compared to the multifunctionality
     *         ranking, based on the Mann-Whitney U test / Wilcoxon
     */
    public double enrichmentForMultifunctionalityPvalue( Collection<Gene> genesInSet ) {

        List<Double> ranksOfGeneInSet = new Vector<>();

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
     * @return a {@link hep.aida.bin.QuantileBin1D} object.
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
     * <p>
     * getGOTermMultifunctionality.
     * </p>
     *
     * @param goId a {@link ubic.erminej.data.GeneSetTerm} object.
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
     * <p>
     * getGOTermMultifunctionalityPvalue.
     * </p>
     *
     * @param goId a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return the pvalue associated with the multifunctionality of the gene set
     * @see getGOTermMultifunctionality
     */
    public double getGOTermMultifunctionalityPvalue( GeneSetTerm goId ) {
        if ( stale.get() ) init();
        if ( !this.goTermMultifunctionalityPvalue.containsKey( goId ) ) {
            return -1;
        }

        return this.goTermMultifunctionalityPvalue.get( goId );
    }

    /**
     * <p>
     * getGOTermMultifunctionalityRank.
     * </p>
     *
     * @param goId a {@link ubic.erminej.data.GeneSetTerm} object.
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
     * Convenience method.
     *
     * @param genes a {@link java.util.Collection} object.
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
     * <p>
     * getMultifunctionalityRank.
     * </p>
     *
     * @param gene a {@link ubic.erminej.data.Gene} object.
     * @return relative rank of the gene in multifunctionality where 1 is the highest multifunctionality, 0 is lowest
     */
    public double getMultifunctionalityRank( Gene gene ) {
        if ( stale.get() ) init();
        if ( !this.geneMultifunctionalityRank.containsKey( gene ) ) {
            return 0.0;
        }

        return this.geneMultifunctionalityRank.get( gene );
    }

    /**
     * <p>
     * getMultifunctionalityScore.
     * </p>
     *
     * @param gene a {@link ubic.erminej.data.Gene} object.
     * @return multifunctionality score. Note that this score by itself is not all that useful; use the rank instead; or
     *         for a "human-readable" version use the number of GO terms, which this approximates (in terms of ranks).
     *         Higher values indicate higher multifunctionality
     */
    public double getMultifunctionalityScore( Gene gene ) {
        if ( stale.get() ) init();
        if ( !this.geneMultifunctionality.containsKey( gene ) ) {
            return 0.0;
        }

        return this.geneMultifunctionality.get( gene );
    }

    /**
     * How many genes have multifunctionality scores.
     *
     * @return a int.
     */
    public int getNumGenes() {
        return new Double( rawGeneMultifunctionalityRanks.size() ).intValue();
    }

    /**
     * <p>
     * Getter for the field <code>numGoTerms</code>.
     * </p>
     *
     * @param gene a {@link ubic.erminej.data.Gene} object.
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

    /**
     * <p>
     * getRawGeneMultifunctionalityRank.
     * </p>
     *
     * @param gene a {@link ubic.erminej.data.Gene} object.
     * @return rank. Where zero is the highest rank, ties accounted for.
     */
    public Double getRawGeneMultifunctionalityRank( Gene gene ) {
        return this.rawGeneMultifunctionalityRanks.get( gene );
    }

    /**
     * <p>
     * isStale.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isStale() {
        return stale.get();
    }

    /**
     * <p>
     * padogWeights.
     * </p>
     *
     * @return weights computed on multifunctionality as per Tarca et al. (2012)
     */
    public Map<Gene, Double> padogWeights() {

        Map<Gene, Double> weig = new HashMap<>();
        Collection<Integer> numgt = numGoTerms.values();

        DoubleArrayList gotermcounts = new DoubleArrayList();
        for ( Integer gtv : numgt ) {
            gotermcounts.add( gtv.doubleValue() );
        }

        double min = Descriptive.min( gotermcounts );
        // double min = 1;

        double max = Descriptive.max( gotermcounts );

        // double max = 300;

        assert min < max;

        for ( Gene g : geneAnnots.getGenes() ) {
            double mfg = Math.min( max, numGoTerms.get( g ) );
            double weight = 1.0 + Math.sqrt( ( max - mfg ) / ( max - min ) );
            weig.put( g, weight );
        }

        return weig;
    }

    /**
     * <p>
     * Setter for the field <code>stale</code>.
     * </p>
     *
     * @param stale a boolean.
     */
    public void setStale( boolean stale ) {
        this.stale.set( stale );
    }

    /**
     * Populate the multifunctionality of each gene set. This is computed by looking at how the genes in the set compare
     * to the gene multifuncttionality ranking, using ROC.
     */
    private void computeGoTermMultifunctionalityRanks() {

        StopWatch timer = new StopWatch();
        timer.start();

        // int numGenes = USE_UNANNOTATED_GENES ? rawGeneMultifunctionalityRanks.size() : genesWithGoTerms.size();
        int numGenes = rawGeneMultifunctionalityRanks.size();
        // int numGoGroups = geneAnnots.getGeneSetTerms().size();

        /*
         * For each go term, compute its AUC w.r.t. the multifunctionality ranking.. We work with the multifunctionality
         * ranks, rawGeneMultifunctionalityRanks
         */
        Map<GeneSetTerm, MFV> tmp = new HashMap<>();
        for ( GeneSetTerm goset : geneAnnots.getGeneSetTerms() ) {

            if ( !goGroupSizes.containsKey( goset ) ) {
                log.debug( "No size recorded for: " + goset );
                continue;
            }

            Set<Gene> genesInSet = geneAnnots.getGeneSetGenes( goset );
            int inGroup = genesInSet.size();
            int outGroup = numGenes - inGroup;

            assert inGroup >= geneAnnots.getMinimumGeneSetSize();

            if ( inGroup > MAX_GROUP_SIZE_FOR_MF ) {
                continue;
            }

            // check for pathological condition
            if ( outGroup == 0 ) {
                continue;
            }

            double auc = enrichmentForMultifunctionality( genesInSet );
            double aucp = enrichmentForMultifunctionalityPvalue( genesInSet );
            assert aucp >= 0.0 && aucp <= 1.0;
            goTermMultifunctionality.put( goset, auc );
            goTermMultifunctionalityPvalue.put( goset, aucp );
            tmp.put( goset, new MFV( auc, aucp ) );
        }

        if ( timer.getTime() > 1000 ) {
            log.info( "Multifunctionality enrichment of GO groups computed in " + timer.getTime() + "ms" );
        }

        // convert to relative ranks, where 1.0 is the most multifunctional; ties are broken by averaging.
        Map<GeneSetTerm, Double> rankedGOMf = Rank.rankTransform( tmp );

        for ( GeneSetTerm goTerm : rankedGOMf.keySet() ) {
            double rankRatio = rankedGOMf.get( goTerm ) / rankedGOMf.size();
            this.goTermMultifunctionalityRank.put( goTerm, Math.max( 0.0, 1.0 - rankRatio ) );
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

            genesWithGoTerms = new HashSet<>();
            for ( GeneSetTerm goset : geneAnnots.getGeneSetTerms() ) {
                Collection<Gene> geneSetGenes = geneAnnots.getGeneSetGenes( goset );
                if ( geneSetGenes.isEmpty() ) {
                    log.debug( "No genes for: " + goset );
                    continue;
                }
                genesWithGoTerms.addAll( geneSetGenes );
                goGroupSizes.put( goset, geneSetGenes.size() );
            }

            // int numGenes = USE_UNANNOTATED_GENES ? this.geneAnnots.getGenes().size() : genesWithGoTerms.size();
            int numGenes = this.geneAnnots.getGenes().size();

            for ( Gene gene : geneAnnots.getGenes() ) {

                // boolean geneHasAnnots = genesWithGoTerms.contains( gene );

                // if ( !geneHasAnnots && !USE_UNANNOTATED_GENES ) continue;

                double mf = 0.0;
                Collection<GeneSetTerm> sets = gene.getGeneSets();
                this.numGoTerms.put( gene, sets.size() );
                for ( GeneSetTerm goset : sets ) {

                    if ( !goGroupSizes.containsKey( goset ) ) {
                        // if ( !USE_UNANNOTATED_GENES ) {
                        // continue;
                        // }
                        // inGroup = 0;
                        // log.info( this.geneAnnots.getGeneSet( goset ).getGenes() );
                        if ( this.geneAnnots.getGeneSet( goset ) == null ) {
                            log.warn( "? Annotations don't contain gene set: " + goset + ", size was not computed." );
                        } else {
                            log.warn( "Set size not available for " + goset );
                        }
                        continue;
                    }
                    Integer inGroup = goGroupSizes.get( goset );

                    int outGroup = numGenes - inGroup;

                    if ( outGroup == 0 ) {
                        continue;
                    }

                    // assert inGroup > 0 || USE_UNANNOTATED_GENES;
                    /**
                     * <p>
                     * Constructor for MFV.
                     * </p>
                     *
                     * @param a a {@link java.lang.Double} object.
                     * @param p a {@link java.lang.Double} object.
                     */

                    mf += 1.0 / ( inGroup * outGroup );
                    // mf += 1.0; // count of go terms ONLY, if you ever want to compare ...
                }
                this.geneMultifunctionality.put( gene, mf );
                /** {@inheritDoc} */
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

}

class MFV implements Comparable<MFV> {
    private Double p;
    private Double a;

    /**
     * <p>Constructor for MFV.</p>
     *
     * @param a a {@link java.lang.Double} object.
     * @param p a {@link java.lang.Double} object.
     */
    public MFV( Double a, Double p ) {
        this.a = a;
        this.p = p;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo( MFV o ) {

        if ( this.p.equals( o.p ) ) {
            // large AUC better.
            return -this.a.compareTo( o.a );
        }

        // small P better.
        return this.p.compareTo( o.p );

    }
}
