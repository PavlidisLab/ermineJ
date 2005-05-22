package classScoreTest;

import java.io.IOException;
import java.util.List;

import classScore.GeneSetPvalRun;
import classScore.Settings;
import classScore.data.GeneScores;
import classScore.data.GeneSetResult;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class ApiExample {

    public static void main( String[] args ) {

        List probes = null; // List of identifiers to be analyzed
        List genes = null; // List of genes corresponding to the probes. Indicates the Many-to-one mapping of probes to genes.
        List goAssociations = null; // List of Collections of go terms for the probes.
        List geneScores = null; // List of Doubles 
        List goIds = null; // list of all GO Ids, e.g., "GO:0000001"
        List goTerms = null; // List of all GO terms, e.g., "ribosome" (java.lang.String)

        /* ... code to initialize these data structures goes here ... */

        Settings settings = null;
        try {
            settings = new Settings( false );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        settings.setBigIsBetter( false ); // in our raw data, smaller values are better (like pvalues, unlike fold
                                            // change)
        settings.setDoLog( true ); // take the -log of the gene scores.
        settings.setMaxClassSize( 100 );
        settings.setMinClassSize( 5 );
        settings.setPValThreshold( 0.001 ); // use this pvalue threshold for selecting genes.
        settings.setClassScoreMethod( Settings.ORA ); // use over-representation analysis.
        /* ... etc. Reasonable defaults (?) are set for all parameters. */

        GeneAnnotations geneData = new GeneAnnotations( probes, genes, null, goAssociations );
        GeneScores scores = new GeneScores( probes, geneScores, geneData.getGeneToProbeList(), geneData
                .getProbeToGeneMap(), settings );
        GONames goData = new GONames( goIds, goTerms );
        GeneSetPvalRun run = new GeneSetPvalRun( settings, geneData, goData, scores );

        /*
         * Now we can get the results for any given gene set. Typically we just iterate over the results.
         */
        double pvalue = ( ( GeneSetResult ) run.getResults().get( "GO:0000032" ) ).getPvalue();

    }

}
