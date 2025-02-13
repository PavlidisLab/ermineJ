/*
 * The ermineJ project
 *
 * Copyright (c) 2006-2013 University of British Columbia
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.jet.math.Arithmetic;
import ubic.erminej.ResultsFileReader;
import ubic.erminej.ResultsPrinter;
import ubic.erminej.data.GeneSetResult;

/**
 * @author  pavlidis
 * @version $Id$
 */
public class OraPvalGeneratorTest extends AbstractPvalGeneratorTest {

    /*
     * @see TestCase#setUp()
     */
    @Override
    @Before
    public void setUp() throws Exception {

        super.setUp();

        test = new OraPvalGenerator( s, super.scores, annotations, null );

    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        test = null;
    }

    @Test
    public void testClassPval1() {
        double expectedReturn = 0.3216374; // checked // dhyper(2, 11, 8, 2)

        assertEquals( 11, scores.getPrunedGeneAnnotations().getGeneSetGenes( annotations.findTerm( "GO:1" ) ).size() );

        GeneSetResult r = test.classPval( test.getGenesAboveThreshold(), annotations.findTerm( "GO:1" ) );

        assertNotNull( r );
        double actualReturn = r.getPvalue();

        assertEquals( expectedReturn, actualReturn, 0.0001 );
    }

    @Test
    public void testClassPval2() {

        // there are 19 genes; GO:2 has 4 members, so it should be 15 not in the group

        //
        // strange, phyper(1, 4, 15, 2) gives the wrong answer of zero.
        // there is a 0.2 prob of a hit per trial; total should be just shy of 0.4

        double expectedReturn = 0.38596; // checked / dhyper(1, 4, 15,2) + dhyper(2, 4, 15,2)

        double t = test.getGeneScoreThreshold();
        assertEquals( -Arithmetic.log10( 0.015 ), t, 0.001 );

        assertEquals( 2, test.getNumGenesOverThreshold() ); // checked
        assertEquals( 17, test.getNumGenesUnderThreshold() ); // checked

        GeneSetResult r = test.classPval( test.getGenesAboveThreshold(), annotations.findTerm( "GO:2" ) );
        assertNotNull( r );
        double actualReturn = r.getPvalue();

        assertEquals( expectedReturn, actualReturn, 0.0001 );
    }

    @Test
    public void testClassPval3() {
        double expectedReturn = 0.7894737; // checked // dhyper(1, 10,9, 2) + dhyper(2, 10,9, 2)

        GeneSetResult r = test.classPval( test.getGenesAboveThreshold(), annotations.findTerm( "GO:3" ) );
        assertNotNull( r );
        double actualReturn = r.getPvalue();

        assertEquals( expectedReturn, actualReturn, 0.0001 );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testReadWrite() throws Exception {
        GeneSetPvalRun results = new GeneSetPvalRun( s, scores );

        File tmp = File.createTempFile( "erminejtest.", ".txt" );
        ResultsPrinter.write( tmp.getAbsolutePath(), results, false );

        FileReader fr = new FileReader( tmp );
        try (BufferedReader br = new BufferedReader( fr )) {
            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            while ( br.ready() ) {
                String line = br.readLine();
                if ( line.startsWith( "numAboveThreshold" ) ) {
                    found1 = true;
                } else if ( line.startsWith( "maxClassSize" ) ) {
                    found2 = true;
                } else if ( line.startsWith( "useUserDefinedGroups" ) ) {
                    found3 = true;
                }

                // System.err.println( line );
            }

            assertTrue( found1 && found2 && found3 );

            Collection<GeneSetPvalRun> loadedresults = ResultsFileReader.load( this.annotations, tmp.getAbsolutePath(),
                    null );

            assertEquals( 1, loadedresults.size() );
        }
    }

}