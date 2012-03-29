/*
 * The ermineJ project
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneAnnotationParser.Format;

/**
 * @author paul
 * @version $Id$
 */
public class TestMultifunctionality extends TestCase {

    public void testMf1() throws Exception {

        /*
         * JG was provided with this file for cross-checking.
         */
        InputStream is = TestMultifunctionality.class
                .getResourceAsStream( "/data/multfunc.annot.testfile.withoutdups.txt" );

        ZipInputStream z = new ZipInputStream(
                TestMultifunctionality.class.getResourceAsStream( "/data/go_daily-termdb.rdf-xml.zip" ) );
        z.getNextEntry();
        GeneSetTerms geneSets = new GeneSetTerms( z );

        assertEquals( 32508, geneSets.getGeneSets().size() ); // rechecked (includes the roots)

        assertNotNull( geneSets.getGraph().getRoot() );

        GeneAnnotationParser p = new GeneAnnotationParser( geneSets );

        SettingsHolder settings = new Settings();
        settings.setUseUserDefined( false );

        GeneAnnotations ga = p.read( is, Format.DEFAULT, settings );

        assertEquals( 211, ga.getGeneSets().size() ); // jesse confirms

        assertEquals( 81, ga.getGenes().size() ); // jesse confirms

        Multifunctionality mf = ga.getMultifunctionality();

        assertEquals( "Wrong number of genes in multifunctionality", 81, mf.getNumGenes() ); // Jesse confirmed.

        assertEquals( "Wrong number of GO terms for gene", 80, mf.getNumGoTerms( new Gene( "PAX8" ) ) ); // Jesse
        // confirmed.

        // System.err.println( "-------------------------" );
        // for ( GeneSetTerm gs : ga.findGene( "PAX8" ).getGeneSets() ) {
        // System.err.println( gs.getId() );
        // }
        // System.err.println( "-------------------------" );

        // it's the second-most multifunctional (rank = 1 by our reckoning)
        assertEquals( 1.0, mf.getRawGeneMultifunctionalityRank( new Gene( "PAX8" ) ), 0.00001 ); // Jesse confirmed.

        assertEquals( 1.0 - 2.0 / 81.0, mf.getMultifunctionalityRank( new Gene( "PAX8" ) ), 0.01 );

        assertEquals( 0.23961, mf.getMultifunctionalityScore( new Gene( "PAX8" ) ), 0.001 ); // Jesse confirmed it is
                                                                                             // 0.2396

        // from Jesse's results.
        assertEquals( 0.038061, mf.getMultifunctionalityScore( new Gene( "VMD2L2" ) ), 0.001 );
        assertEquals( 0, mf.getMultifunctionalityScore( new Gene( "ARMCX4" ) ), 0.0001 );
        assertEquals( 0.015758, mf.getMultifunctionalityScore( new Gene( "CRYZL1" ) ), 0.0001 );
        assertEquals( 0.0067805, mf.getMultifunctionalityScore( new Gene( "LOC201158" ) ), 0.0001 );
        assertEquals( 0.13531, mf.getMultifunctionalityScore( new Gene( "CCL5" ) ), 0.0001 );

        // actual rank should be 124. I checked this in R with (see test_mf.roc_scores.gillis.txt)
        // cbind(test_mf.roc_scores.gillis,rank(-test_mf.roc_scores.gillis[,2]))
        assertEquals( 1.0 - 124.0 / 211.0, mf.getGOTermMultifunctionalityRank( new GeneSetTerm( "GO:0005634" ) ), 0.001 );

        // more tests from Jesse's output.
        assertEquals( 0.8552036, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0005634" ) ), 0.001 );
        assertEquals( 0.86056, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0001882" ) ), 0.001 );
        assertEquals( 0.78394, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0019538" ) ), 0.001 );
        assertEquals( 0.53797, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0044421" ) ), 0.001 );

        List<Gene> li = new ArrayList<Gene>();
        li.add( new Gene( "EYA3" ) );
        li.add( new Gene( "EPHB3" ) );
        li.add( new Gene( "MAPK1" ) );
        li.add( new Gene( "PTPN21" ) );
        li.add( new Gene( "CYP2A6" ) );
        li.add( new Gene( "CCL5" ) );
        li.add( new Gene( "GSC" ) );
        li.add( new Gene( "PAX8" ) );
        li.add( new Gene( "THRA" ) );
        li.add( new Gene( "PXK" ) );
        li.add( new Gene( "GUCA1A" ) );
        li.add( new Gene( "DDR1" ) );
        li.add( new Gene( "RFC2" ) );
        li.add( new Gene( "HSPA6" ) );
        li.add( new Gene( "LOC201158" ) );
        li.add( new Gene( "SLC39A5" ) );
        li.add( new Gene( "ALG10" ) );
        li.add( new Gene( "UBE1L" ) );
        li.add( new Gene( "C6orf199" ) );
        li.add( new Gene( "foonotagene" ) );
        assertEquals( 0.707017, mf.correlationWithGeneMultifunctionality( li ), 0.001 ); // not checked by hand.
    }
}
