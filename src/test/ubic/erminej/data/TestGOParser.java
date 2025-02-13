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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import ubic.basecode.dataStructure.graph.DirectedGraphNode;

/**
 * @author Paul Pavlidis
 */
public class TestGOParser {

    private GOParser goParser = null;

    @Test
    public void testGOOBO1() throws Exception {
        try (InputStream z = new GZIPInputStream( TestGOParser.class.getResourceAsStream( "/data/goslim_generic.obo.txt.gz" ) )) {
            goParser = new GOOBOParser( z );

            assertNotNull( goParser.getGraph().getRoot() );
            assertEquals( 146, goParser.getGraph().getItems().size() );

            for ( GeneSetTerm t : goParser.getGraph().getValues() ) {
                assertNotNull( t.getAspect() );
            }

            assertNotNull( goParser.getGraph().get( "GO:0042393" ) );
            assertNotNull( goParser.getGraph().get( "GO:0043167" ) );

            assertEquals( 3, goParser.getGraph().getRoot().getChildNodes().size() );
            DirectedGraphNode<String, GeneSetTerm> testnode = goParser.getGraph().get( "GO:0140014" );

            assertNotNull( testnode );
            Set<String> parentKeys = testnode.getParentKeys();
            assertTrue( parentKeys.size() > 0 );

        }
    }

}