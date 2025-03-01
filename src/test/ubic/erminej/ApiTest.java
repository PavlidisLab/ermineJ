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
package ubic.erminej;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * @author pavlidis
 * @version $Id$
 */
public class ApiTest {

    private static Log log = LogFactory.getLog( ApiTest.class.getName() );

    @Test
    public final void test() {
        // List of identifiers to be analyzed
        List<String> elements = new ArrayList<>();
        elements.add( "a" );
        elements.add( "b" );
        elements.add( "c" );
        elements.add( "c_1" );

        // List of genes corresponding to the elements. Indicates the Many-to-one mapping
        // of elements to genes
        List<String> genes = new ArrayList<>();
        genes.add( "aGene" );
        genes.add( "bGene" );
        genes.add( "cGene" );
        genes.add( "cGene" ); // two elements with the same gene.

        // List of Collections of go terms for the elements.
        List<Collection<String>> goAssociations = new ArrayList<>();
        Collection<String> gotermsA = new HashSet<>();
        gotermsA.add( "foo" );
        gotermsA.add( "bar" );
        Collection<String> gotermsB = new HashSet<>();
        gotermsB.add( "foo" );
        Collection<String> gotermsC = new HashSet<>();
        gotermsC.add( "foo" );
        Collection<String> gotermsC1 = new HashSet<>();
        gotermsC1.add( "foo" );

        goAssociations.add( gotermsA );
        goAssociations.add( gotermsB );
        goAssociations.add( gotermsC );
        goAssociations.add( gotermsC1 );

        // List of Doubles
        List<Double> geneScores = new ArrayList<>();
        geneScores.add( new Double( "0.1" ) );
        geneScores.add( new Double( "0.1" ) );
        geneScores.add( new Double( "0.01" ) );
        geneScores.add( new Double( "0.01" ) );

        /* ... code to initialize these data structures goes here ... */

        ClassScoreSimple css = new ClassScoreSimple( elements, genes, goAssociations );

        // in our raw data, smaller values are better (like pvalues, unlike fold
        // change)
        css.setBigGeneScoreIsBetter( false );

        // set range of sizes of gene sets to consider.
        css.setMaxGeneSetSize( 100 );
        css.setMinGeneSetSize( 2 );

        // use this pvalue threshold for selecting genes. (before taking logs)
        css.setGeneScoreThreshold( 0.15 );

        // use over-representation analysis.
        css.setClassScoreMethod( 0 );
        /* ... etc. Reasonable defaults (?) are set for all parameters if you don't set them. */

        css.run( geneScores ); // might want to run in a separate thread.

        // You should iterate over your tested gene sets.
        double fooPvalue = css.getGeneSetPvalue( "foo" );
        double barPvalue = css.getGeneSetPvalue( "bar" );

        // The results are nonsensical because there are too few genes...
        log.info( "Foo got " + fooPvalue );
        log.info( "Bar got " + barPvalue );
    }
}
