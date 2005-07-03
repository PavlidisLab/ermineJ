package classScore;

import java.io.IOException;
import java.util.List;

import baseCode.bio.geneset.GeneAnnotations;
import classScore.data.GeneScores;
import classScore.data.GeneSetResult;

/**
 * Simple API to run ermineJ analyses.
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class ClassScoreSimple {

    /**
     * When more than one probe is found for the same gene, use the best score to represent the gene.
     */
    public static final int BEST_GENE_SCORE = Settings.BEST_PVAL;

    /**
     * When summarizing a gene set during resampling, use the mean gene score
     */
    public static final int MEAN = Settings.MEAN_METHOD;

    /**
     * When more than one probe is found for the same gene, use the mean score to represent the gene
     */
    public static final int MEAN_GENE_SCORE = Settings.MEAN_PVAL;

    /**
     * When sumarizing a gene set during resampling, use the median gene score
     */
    public static final int MEDIAN = Settings.QUANTILE_METHOD;

    /**
     * Over-representation analysis.
     */
    public static final int ORA = Settings.ORA;

    /**
     * Receiver operator characteristic analysis
     */
    public static final int ROC = Settings.ROC;

    /**
     * Gene score resampling
     */
    public static final int RESAMPLING = Settings.RESAMP;

    // List of genes corresponding to the probes. Indicates the Many-to-one mapping of
    // probes to
    // genes.
    private List genes = null;

    // List of Collections of go terms for the probes.
    private List goAssociations = null;

    // List of identifiers to be analyzed
    private List probes = null;

    private GeneSetPvalRun results;

    private Settings settings = null;

    /**
     * Note that these Lists must all be in the same order with respect to the probes.
     * 
     * @param probes List of identifiers to be analyzed
     * @param genes List of genes corresponding to the probes. Indicates the Many-to-one mapping of probes to genes.
     * @param goAssociations List of Collections of go terms for the probes.
     */
    public ClassScoreSimple( List probes, List genes, List goAssociations ) {
        this.probes = probes;
        this.genes = genes;
        this.goAssociations = goAssociations;
        try {
            settings = new Settings( false );
            settings.setQuantile( 50 );
            settings.setMtc( Settings.BENJAMINIHOCHBERG );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Gene the resulting gene set pvalue for a given id.
     * 
     * @param id The id of the gene set, e.g,. GO:0000232
     * @return -1 if the id is not in the results. Otherwise, the pvalue for the gene set.
     */
    public double getGeneSetPvalue( String id ) {
        if ( results == null ) throw new IllegalStateException( "You must call 'run' before you can get results" );

        if ( !results.getResults().containsKey( id ) ) return -1;

        return ( ( GeneSetResult ) results.getResults().get( id ) ).getPvalue();
    }

    /**
     * Run an analysis using the current configuration.
     */
    public void run( List geneScores ) {
        GeneAnnotations geneData = new GeneAnnotations( probes, genes, null, goAssociations );
        GeneScores scores = new GeneScores( probes, geneScores, geneData.getGeneToProbeMap(), geneData
                .getProbeToGeneMap(), settings );
        results = new GeneSetPvalRun( settings, geneData, null, scores );
    }

    /**
     * Indicate that in the original gene scores, whether big values are better. If your inputs are p-values this should
     * be set to false. If you are using fold-changes, set to true.
     * 
     * @param b
     */
    public void setBigGeneScoreIsBetter( boolean b ) {
        this.settings.setBigIsBetter( b );
    }

    /**
     * Set the type of anlaysis to run. ORA is over-representation analysis.
     * 
     * @param val either ClassScoreSimple.ORA or ClassScoreSimple.RESAMPLING
     */
    public void setClassScoreMethod( int val ) {
        if ( val != ORA && val != RESAMPLING && val != ROC )
            throw new IllegalArgumentException(
                    "Value must be one of ClassScoreSimple.ORA or ClassScoreSimple.RESAMPLING" );
        this.settings.setClassScoreMethod( val );
    }

    /**
     * How to handle situations when more than one probe corresponds to the same gene.
     * 
     * @param val either BEST_GENE_SCORE or MEAN_GENE_SCORE
     */
    public void setGeneReplicateTreatment( int val ) {
        if ( val != BEST_GENE_SCORE && val != MEAN_GENE_SCORE )
            throw new IllegalArgumentException( "Value must be either  BEST_GENE_SCORE or MEAN_GENE_SCORE" );
        this.settings.setGeneRepTreatment( val );
    }

    /**
     * Set the method to be used to summarize gene sets during resampling analysis. This is ignored otherwise.
     * 
     * @param val Either ClassScoreSimple.MEAN or ClassScoreSimple.MEDIAN.
     */
    public void setGeneScoreSummaryMethod( int val ) {
        if ( val != MEAN && val != MEDIAN )
            throw new IllegalArgumentException( "Summary method must be either MEAN or MEDIAN" );
        this.settings.setRawScoreMethod( val );
    }

    /**
     * Set the threshold to use for ORA analysis. This is ignored otherwise.
     * 
     * @param val
     */
    public void setGeneScoreThreshold( double val ) {
        this.settings.setPValThreshold( val );
    }

    /**
     * The number of iterations to be used during resampling. This is ignored for ORA analysis.
     * 
     * @param val
     */
    public void setIterations( int val ) {
        if ( val < 1 ) throw new IllegalArgumentException( "Value must be positive" );
        this.settings.setIterations( val );
    }

    /**
     * Set to true if your inputs are p-values.
     * 
     * @param val If true, your gene scores will be transformed by -log base 10.
     */
    public void setLogTransformGeneScores( boolean val ) {
        this.settings.setDoLog( val );
    }

    /**
     * The maximum gene set size to be considered.
     * 
     * @param val
     */
    public void setMaxGeneSetSize( int val ) {
        if ( val < 1 ) throw new IllegalArgumentException( "Value must be positive" );
        this.settings.setMaxClassSize( val );
    }

    /**
     * The minimum gene set size to be considered.
     * 
     * @param val
     */
    public void setMinGeneSetSize( int val ) {
        if ( val < 1 ) throw new IllegalArgumentException( "Value must be positive" );
        this.settings.setMinClassSize( val );
    }

}
