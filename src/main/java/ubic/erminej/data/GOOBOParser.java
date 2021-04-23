/*
 * The ermineJ project
 * 
 * Copyright (c) 2018-2021 University of British Columbia
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubic.basecode.dataStructure.graph.DirectedGraph;

/**
 * 
 * @author paul
 */
public class GOOBOParser extends GOParser {

    private static Logger log = LoggerFactory.getLogger( GOOBOParser.class );

    /**
     * @param  i
     * @param  oldFormat   if true, use the old-fashioned scheme
     * @throws IOException
     */
    public GOOBOParser( InputStream i ) throws IOException {

        if ( i.available() == 0 ) {
            throw new IOException( "OBO stream contains no data." );
        }

        termGraph = new DirectedGraph<>();
        init();

        BufferedReader in = new BufferedReader( new InputStreamReader( i ) );
        String line = null;

        GeneSetTerm currentTerm = null;
        while ( ( line = in.readLine() ) != null ) {

            line = StringUtils.strip( line );

            if ( line.isEmpty() ) {
                continue;
            }

            if ( line.equals( "[Term]" ) ) {

                // start a new term. We might not use it if we find we already have it in the graph (we'll just fill it in)
                currentTerm = new GeneSetTerm( null, "[No name provided]", "[No definition]" );

            } else if ( line.equals( "[Typedef]" ) ) {
                // irrelevant
                currentTerm = null;
            } else {
                if ( currentTerm == null ) continue;

                String[] keval = StringUtils.split( line, ":", 2 );
                String key = StringUtils.strip( keval[0] );
                String value = StringUtils.strip( keval[1] );

                if ( key.equals( "id" ) ) {
                    if ( termGraph.containsKey( value ) ) {
                        currentTerm = termGraph.get( value ).getItem();
                    } else {
                        currentTerm.setId( value );
                        this.termGraph.addNode( value, currentTerm );
                    }

                } else if ( key.equals( "namespace" ) ) {
                    currentTerm.setAspect( value );
                } else if ( key.equals( "def" ) ) {

                    String def = value.replaceAll( "\\[.*\\]", "" ).trim(); // trim off stuff like [GOC:ai]
                    def = def.replaceAll( "^\"", "" ).replaceAll( "\"$", "" ); // trim quotation marks

                    currentTerm.setDefinition( def );
                } else if ( key.equals( "is_a" ) ) {
                    //GO:0006886 ! intracellular protein transport
                    String parentTerm = StringUtils.split( value, " ", 2 )[0];
                    addRelationship( currentTerm, parentTerm );
                } else if ( key.equals( "is_obsolete" ) ) {
                    this.termGraph.deleteLeaf( currentTerm.getId() );
                    currentTerm = null;
                    continue;
                } else if ( key.equals( "relationship" ) ) {
                    //  part_of GO:0006606 ! protein import into nucleus

                    /*
                     * We shouldn't use these to define the DAG because "relationships" seem to add "extras".
                     */

                    //                    String[] tv = StringUtils.split( value, " ", 3 );
                    //
                    //                    String relType = tv[0];
                    //                    String parentTerm = tv[1];
                    //                    if ( relType.equals( "part_of" ) || relType.equals( "is_a" ) ) {
                    //                        addRelationship( currentTerm, parentTerm );
                    //                    }
                } else if ( key.equals( "synonym" ) ) {
                    // no-op
                } else if ( key.equals( "name" ) ) {
                    currentTerm.setName( value );
                } else if ( key.equals( "subset" ) ) {
                    // no-op
                } // there are other possible fields but we just ignore them

            }
        }

        /*
         * Attach the aspects to the root.
         */

        this.getGraph().addParentTo( "GO:0003674", ALL );

        this.getGraph().addParentTo( "GO:0008150", ALL );

        this.getGraph().addParentTo( "GO:0005575", ALL );

        populateAspect();
    }

    /**
     * @param currentTerm
     * @param value
     */
    public void addRelationship( GeneSetTerm currentTerm, String parentTerm ) {

        if ( !termGraph.containsKey( parentTerm ) ) {
            termGraph.addNode( parentTerm, new GeneSetTerm( parentTerm ) );
        }

        if ( log.isDebugEnabled() ) log.debug( "Adding parent " + parentTerm + " to " + currentTerm );
        this.termGraph.addParentTo( currentTerm.getId(), parentTerm );

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.erminej.data.GOParser#getGraph()
     */
    @Override
    public DirectedGraph<String, GeneSetTerm> getGraph() {
        return termGraph;
    }

    private Collection<String> forbiddenParents = new HashSet<>();

    /**
    *
    */
    private void init() {
        initializeNewNode( ALL );

        /*
         * This is a workaround for a change in GO: the terms obsolete_molecular_function etc. are never defined.
         * 
         * Seemingly not relevant to OBO, put here in case for now.
         */

        // not sure if relevant to OBO (or even to XML, any more)
        forbiddenParents.add( "obsolete_molecular_function" );
        forbiddenParents.add( "obsolete_biological_process" );
        forbiddenParents.add( "obsolete_cellular_component" );
    }

    /**
     * @param id
     */
    private void initializeNewNode( String id ) {
        GeneSetTerm item = new GeneSetTerm( id, "[No name provided]", "[No definition]" );
        termGraph.addNode( id, item );
    }

}
