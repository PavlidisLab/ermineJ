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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneAnnotationParser.Format;

/**
 * @author paul
 * @version $Id$
 */
public class TestMultifunctionality extends TestCase {

    public void testMf1() throws Exception {
        InputStream is = TestMultifunctionality.class.getResourceAsStream( "/data/HG-U133_Plus_2_annot_sample.csv" );
        ZipInputStream z = new ZipInputStream( TestMultifunctionality.class
                .getResourceAsStream( "/data/go_daily-termdb.rdf-xml.zip" ) );
        z.getNextEntry();
        GeneSetTerms geneSets = new GeneSetTerms( z );

        assertEquals( 32508, geneSets.getGeneSets().size() );

        assertNotNull( geneSets.getGraph().getRoot() );

        GeneAnnotationParser p = new GeneAnnotationParser( geneSets );

        GeneAnnotations ga = p.read( is, Format.AFFYCSV );

        Multifunctionality mf = ga.getMultifunctionality();

        double actual = mf.getMultifunctionalityScore( new Gene( "PAX8" ) );
        assertEquals( 0.083, actual, 0.001 ); // not checked by hand.

        int actualNumG = mf.getNumGoTerms( new Gene( "PAX8" ) );
        assertEquals( 80, actualNumG );// not checked by hand.

        double actualR = mf.getMultifunctionalityRank( new Gene( "PAX8" ) );
        assertEquals( 0.86, actualR, 0.01 );// not checked by hand.

        double actualGoMF = mf.getGOTermMultifunctionalityRank( new GeneSetTerm( "GO:0005634" ) );
        assertEquals( 0.3118, actualGoMF, 0.001 );// not checked by hand.

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
        double cgm = mf.correlationWithGeneMultifunctionality( li );
        assertEquals( 0.742, cgm, 0.001 );
    }

}
