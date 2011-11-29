/*
 * The baseCode project
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
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.basecode.util.RegressionTesting;
import ubic.erminej.data.GOParser;

/**
 * @author Paul Pavlidis
 * @version $Id$
 */
public class TestGOParser extends TestCase {

    private GOParser gOParser = null;

    /**
     * This sample is of a rather old version of the ontology
     * 
     * @throws Exception
     */
    public void testGOParser() throws Exception {

        InputStream i = TestGOParser.class.getResourceAsStream( "/data/go-termdb-sample.xml" );

        if ( i == null ) {
            throw new Exception( "Couldn't read the sample file" );
        }
        gOParser = new GOParser( i );

        String actualReturn = gOParser.getGraph().toString();

        String expectedReturn = RegressionTesting.readTestResult( "/data/goparsertestoutput.txt" );
        assertEquals( "return", expectedReturn, actualReturn );

        assertTrue( gOParser.getGraph().getRoot().toString().startsWith( "all" ) );
        /*
         * assertEquals( "Diffs: " + RegressionTesting.regress( expectedReturn, actualReturn ), expectedReturn,
         * actualReturn );
         */
    }

    /**
     * @throws Exception
     */
    public void testGOParserB() throws Exception {

        InputStream i = TestGOParser.class.getResourceAsStream( "/data/go_daily-termdb.rdf-sample2.xml" );

        if ( i == null ) {
            throw new Exception( "Couldn't read the sample file" );
        }

        gOParser = new GOParser( i );

        String actualReturn = gOParser.getGraph().toString();
        String expectedReturn = RegressionTesting.readTestResult( "/data/goparsertestoutput.2.txt" );
        assertEquals( "return", expectedReturn, actualReturn );

        assertTrue( gOParser.getGraph().getRoot().toString().startsWith( "all" ) );

        assertNotNull( gOParser.getGraph().getRoot() );
    }

    public void testGOParserCBig() throws Exception {
        ZipInputStream z = new ZipInputStream(
                TestGOParser.class.getResourceAsStream( "/data/go_daily-termdb.rdf-xml.zip" ) );
        z.getNextEntry();
        gOParser = new GOParser( z );

        assertNotNull( gOParser.getGraph().getRoot() );

    }

    /**
     * Old rdf format. Really old.
     * 
     * @throws Exception
     */
    public void testGOParserOld() throws Exception {
        InputStream z = new GZIPInputStream( TestGOParser.class.getResourceAsStream( "/data/go_200212-termdb.xml.gz" ) );
        gOParser = new GOParser( z, true );
        System.err.println( gOParser.getGraph().toString() );

        assertNotNull( gOParser.getGraph().getRoot() );
        assertEquals( 3, gOParser.getGraph().getRoot().getChildNodes().size() );
        DirectedGraphNode<String, GeneSetTerm> testnode = gOParser.getGraph().get( "GO:0045034" );

        assertNotNull( testnode );
        Set<String> parentKeys = testnode.getParentKeys();
        assertTrue( parentKeys.size() > 0 );
    }

    //
    // /**
    // * Old rdf format.
    // *
    // * @throws Exception
    // */
    // public void testGOParserOld2() throws Exception {
    // InputStream z = new GZIPInputStream( TestGOParser.class.getResourceAsStream( "/data/go_200407-termdb.xml.gz" ) );
    // gOParser = new GOParser( z, true );
    // // System.err.println( gOParser.getGraph().toString() );
    //
    // assertNotNull( gOParser.getGraph().getRoot() );
    // assertEquals( 3, gOParser.getGraph().getRoot().getChildNodes().size() );
    //
    // DirectedGraphNode<String, GeneSetTerm> testnode = gOParser.getGraph().get( "GO:0045034" );
    //
    // assertNotNull( testnode );
    // Set<String> parentKeys = testnode.getParentKeys();
    // assertTrue( parentKeys.size() > 0 );
    //
    // }

    // /**
    // * Old rdf format.
    // *
    // * @throws Exception
    // */
    // public void testGOParserOld3() throws Exception {
    // InputStream z = new GZIPInputStream(
    // TestGOParser.class.getResourceAsStream( "/data/go_200808-termdb.rdf-xml.gz" ) );
    // gOParser = new GOParser( z, true );
    //
    // assertEquals( 3, gOParser.getGraph().getRoot().getChildNodes().size() );
    //
    // assertNotNull( gOParser.getGraph().getRoot() );
    //
    // DirectedGraphNode<String, GeneSetTerm> testnode = gOParser.getGraph().get( "GO:0045034" );
    // assertNotNull( testnode );
    // Set<String> parentKeys = testnode.getParentKeys();
    // assertTrue( parentKeys.size() > 0 );
    //
    // }
    //
    // /**
    // * New rdf format.
    // *
    // * @throws Exception
    // */
    // public void testGOParserNewFormat() throws Exception {
    // InputStream z = new GZIPInputStream(
    // TestGOParser.class.getResourceAsStream( "/data/go_201012-termdb.rdf-xml.gz" ) );
    // gOParser = new GOParser( z, false );
    // assertEquals( 3, gOParser.getGraph().getRoot().getChildNodes().size() );
    // assertNotNull( gOParser.getGraph().getRoot() );
    //
    // DirectedGraphNode<String, GeneSetTerm> testnode = gOParser.getGraph().get( "GO:0002643" );
    // assertNotNull( testnode );
    // Set<String> parentKeys = testnode.getParentKeys();
    // assertTrue( parentKeys.size() > 0 );
    //
    // }

    // public void testGOParserNewFormatLatest() throws Exception {
    // InputStream z = new GZIPInputStream(
    // TestGOParser.class.getResourceAsStream( "/data/go_daily-termdb.rdf-xml.gz" ) );
    // gOParser = new GOParser( z, false );
    //
    // assertNotNull( gOParser.getGraph().getRoot() );
    //
    // DirectedGraphNode<String, GeneSetTerm> testnode = gOParser.getGraph().get( "GO:2001172" );
    // assertNotNull( testnode );
    // Set<String> parentKeys = testnode.getParentKeys();
    // assertTrue( parentKeys.size() > 0 );
    //
    // }

}