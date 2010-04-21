/*
 * The Gemma project
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
package ubic.erminej.data;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import ubic.basecode.util.RegressionTesting;

import junit.framework.TestCase;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneScores;

/**
 * @author pavlidis
 * @version $Id$
 */
public class GeneScoreReaderTest extends TestCase {
    InputStream is = null;
    InputStream ism = null;
    GeneScores test = null;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        Settings s = new Settings();

        ism = GeneScoreReaderTest.class.getResourceAsStream( "/data/test.an.txt" );

        is = GeneScoreReaderTest.class.getResourceAsStream( "/data/test.scores.txt" );

        GeneAnnotations g = new GeneAnnotations( ism, null, null, null );

        test = new GeneScores( is, s, null, g );
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ism.close();
        is.close();
    }

    public void testGet_probe_ids() {
        String[] expectedReturn = new String[] { "ProbeA", "ProbeB", "ProbeC", "ProbeD", "ProbeE", "ProbeF", "ProbeG",
                "ProbeH", "ProbeI", "ProbeJ", "ProbeK", "ProbeL", "ProbeM", "ProbeN", "ProbeO", "ProbeP", "ProbeQ",
                "ProbeR", "ProbeS", "ProbeT", "ProbeU" };

        String[] actualReturn = test.getProbeIds();

        assertEquals( expectedReturn.length, actualReturn.length );

        assertTrue( RegressionTesting.containsSame( expectedReturn, actualReturn ) );
    }

    public void testGetPvalues() {
        double[] expectedReturn = new double[] { -Math.log10( 0.01 ), -Math.log10( 0.01 ), -Math.log10( 0.02 ),
                -Math.log10( 0.1 ), -Math.log10( 0.1 ), -Math.log10( 0.1 ), -Math.log10( 0.1 ), -Math.log10( 0.1 ),
                -Math.log10( 0.2 ), -Math.log10( 0.2 ), -Math.log10( 0.2 ), -Math.log10( 0.2 ), -Math.log10( 0.2 ),
                -Math.log10( 0.25 ), -Math.log10( 0.3 ), -Math.log10( 0.4 ), -Math.log10( 0.5 ), -Math.log10( 0.6 ),
                -Math.log10( 0.7 ), -Math.log10( 0.8 ), -Math.log10( 0.9 ) };
        double[] actualReturn = test.getPvalues();

        assertEquals( expectedReturn.length, actualReturn.length );

        assertTrue( RegressionTesting.closeEnough( expectedReturn, actualReturn, 0.001 ) );
    }

    public void testGet_numpvals() {
        int expectedReturn = 21;
        int actualReturn = test.getNumGeneScores();
        assertEquals( "return value", expectedReturn, actualReturn );
    }

    /*
     * Class under test for Map getGeneToPvalMap()
     */
    public void testGetGeneToPvalMap() {
        Set<String> expectedReturn = new HashSet<String>();
        expectedReturn.add( "GeneA" );
        expectedReturn.add( "GeneB" );
        expectedReturn.add( "GeneC" );
        expectedReturn.add( "GeneD" );
        expectedReturn.add( "GeneE" );
        expectedReturn.add( "GeneH" );
        expectedReturn.add( "GeneI" );
        expectedReturn.add( "GeneJ" );
        expectedReturn.add( "GeneK" );
        expectedReturn.add( "GeneL" );
        expectedReturn.add( "GeneM" );
        expectedReturn.add( "GeneN" );
        expectedReturn.add( "GeneO" );
        expectedReturn.add( "GeneP" );
        expectedReturn.add( "GeneQ" );
        expectedReturn.add( "GeneR" );
        expectedReturn.add( "GeneS" );
        expectedReturn.add( "GeneT" );
        expectedReturn.add( "GeneU" );

        Set<String> actualReturn = test.getGeneToPvalMap().keySet();

        assertTrue( RegressionTesting.containsSame( expectedReturn, actualReturn ) );
    }

}