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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import ubic.basecode.util.RegressionTesting;

import junit.framework.TestCase;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneScores;

/**
 * @author pavlidis
 * @version $Id$
 */
public class GeneScoreReaderTest extends TestCase {
    InputStream is = null;
    InputStream ism = null;
    protected GeneScores test = null;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        SettingsHolder s = new Settings( false );
        s.setUseUserDefined( false );

        ism = GeneScoreReaderTest.class.getResourceAsStream( "/data/test.an.txt" );

        is = GeneScoreReaderTest.class.getResourceAsStream( "/data/test.scores.txt" );

        GeneSetTerms geneSetTerms = new GeneSetTerms(
                GeneScoreReaderTest.class.getResourceAsStream( "/data/go-termdb-test.xml" ) );
        GeneAnnotationParser p = new GeneAnnotationParser( geneSetTerms );
        GeneAnnotations g = p.readDefault( ism, null, s, false );

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

    /**
     * 
     */
    public void testGet_probe_ids() {
        String[] expectedReturn = new String[] { "ProbeA", "ProbeB", "ProbeC", "ProbeD", "ProbeE", "ProbeF", "ProbeG",
                "ProbeH", "ProbeI", "ProbeJ", "ProbeK", "ProbeL", "ProbeM", "ProbeN", "ProbeO", "ProbeP", "ProbeQ",
                "ProbeR", "ProbeS", "ProbeT", "ProbeU" };

        Collection<Probe> actualReturn = test.getProbeToScoreMap().keySet();

        assertEquals( expectedReturn.length, actualReturn.size() );

        for ( String e : expectedReturn ) {
            assertTrue( "Did not contain " + e, actualReturn.contains( new Probe( e ) ) );
        }
    }

    /**
     * 
     */
    public void testGetPvalues() {
        double[] expectedReturn = new double[] { -Math.log10( 0.01 ), -Math.log10( 0.01 ), -Math.log10( 0.02 ),
                -Math.log10( 0.1 ), -Math.log10( 0.1 ), -Math.log10( 0.1 ), -Math.log10( 0.1 ), -Math.log10( 0.1 ),
                -Math.log10( 0.2 ), -Math.log10( 0.2 ), -Math.log10( 0.2 ), -Math.log10( 0.2 ), -Math.log10( 0.2 ),
                -Math.log10( 0.25 ), -Math.log10( 0.3 ), -Math.log10( 0.4 ), -Math.log10( 0.5 ), -Math.log10( 0.6 ),
                -Math.log10( 0.7 ), -Math.log10( 0.8 ), -Math.log10( 0.9 ) };
        Double[] actualReturn = test.getProbeScores();

        assertEquals( expectedReturn.length, actualReturn.length );

        assertEquals( 19, test.getGeneScores().length );

        assertTrue( RegressionTesting.closeEnough( expectedReturn, ArrayUtils.toPrimitive( actualReturn ), 0.001 ) );
    }

    public void testGet_numpvals() {
        int expectedReturn = 21;
        int actualReturn = test.getNumProbesUsed();
        assertEquals( "return value", expectedReturn, actualReturn );

        GeneAnnotations annots = test.getPrunedGeneAnnotations();
        for ( GeneSet gs : annots.getGeneSets() ) {
            assertTrue( gs.toString(), test.getGeneToScoreMap().keySet().containsAll( gs.getGenes() ) );
        }
    }

    /*
     * Class under test for Map getGeneToPvalMap()
     */
    public void testGetGeneToPvalMap() {
        Set<Gene> expectedReturn = new HashSet<Gene>();
        expectedReturn.add( new Gene( "GeneA" ) );
        expectedReturn.add( new Gene( "GeneB" ) );
        expectedReturn.add( new Gene( "GeneC" ) );
        expectedReturn.add( new Gene( "GeneD" ) );
        expectedReturn.add( new Gene( "GeneE" ) );
        expectedReturn.add( new Gene( "GeneH" ) );
        expectedReturn.add( new Gene( "GeneI" ) );
        expectedReturn.add( new Gene( "GeneJ" ) );
        expectedReturn.add( new Gene( "GeneK" ) );
        expectedReturn.add( new Gene( "GeneL" ) );
        expectedReturn.add( new Gene( "GeneM" ) );
        expectedReturn.add( new Gene( "GeneN" ) );
        expectedReturn.add( new Gene( "GeneO" ) );
        expectedReturn.add( new Gene( "GeneP" ) );
        expectedReturn.add( new Gene( "GeneQ" ) );
        expectedReturn.add( new Gene( "GeneR" ) );
        expectedReturn.add( new Gene( "GeneS" ) );
        expectedReturn.add( new Gene( "GeneT" ) );
        expectedReturn.add( new Gene( "GeneU" ) );

        Set<Gene> actualReturn = test.getGeneToScoreMap().keySet();

        assertTrue( RegressionTesting.containsSame( expectedReturn, actualReturn ) );
    }

}