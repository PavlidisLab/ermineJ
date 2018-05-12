/*
 * The ermineJ project
 * 
 * Copyright (c) 2018 University of British Columbia
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

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ubic.basecode.dataStructure.graph.DirectedGraph;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;

/**
 * 
 * @author paul
 */
public abstract class GOParser {

    protected static final String ROOT = "GO:0003673"; // obsolete
    protected static final String ALL = "all"; // made up.

    protected DirectedGraph<String, GeneSetTerm> termGraph;

    /**
     * Get the graph that was created.
     *
     * @return a DirectedGraph. Nodes contain OntologyEntry instances.
     */
    public DirectedGraph<String, GeneSetTerm> getGraph() {
        return this.termGraph;
    }

    /**
     * 
     * In the new GO, aspects are not represented the way they used to be, so we have to explicitly set them to be part
     * of the aspect themselves.
     */
    protected void populateAspect() {
        DirectedGraphNode<String, GeneSetTerm> root = this.getGraph().getRoot();
        root.getItem().setAspect( "Gene_Ontology" );

        if ( !root.getKey().equals( ROOT ) && !root.getKey().equals( ALL ) ) {
            for ( DirectedGraphNode<String, GeneSetTerm> node : root.getChildNodes() ) {
                if ( node.getKey().equals( ROOT ) || node.getKey().equals( ALL ) ) {
                    this.termGraph = node.getChildGraph();
                    populateAspect();
                    return;
                }
            }
            throw new IllegalStateException( ROOT
                    + " is not the root and none of the children of the root were either. (instead it was '"
                    + root.getKey() + "')" );
        }

        Set<DirectedGraphNode<String, GeneSetTerm>> childNodes = root.getChildNodes();

        for ( DirectedGraphNode<String, GeneSetTerm> n : childNodes ) {
            if ( n.getKey().equals( "GO:0003674" ) ) {
                n.getItem().setAspect( "molecular_function" );
                for ( DirectedGraphNode<String, GeneSetTerm> t : n.getAllChildNodes() ) {
                    t.getItem().setAspect( "molecular_function" );
                    fillAspect( t );
                }
            } else if ( n.getKey().equals( "GO:0008150" ) ) {
                n.getItem().setAspect( "biological_process" );
                for ( DirectedGraphNode<String, GeneSetTerm> t : n.getAllChildNodes() ) {
                    t.getItem().setAspect( "biological_process" );
                    fillAspect( t );
                }
            } else if ( n.getKey().equals( "GO:0005575" ) ) {
                n.getItem().setAspect( "cellular_component" );
                for ( DirectedGraphNode<String, GeneSetTerm> t : n.getAllChildNodes() ) {
                    t.getItem().setAspect( "cellular_component" );
                    fillAspect( t );
                }
            } else if ( n.getKey().equals( ROOT ) || n.getKey().equals( ALL ) ) {
                /*
                 * This is erroneous and should be the actual root, instead of 'top'.
                 */
                throw new IllegalStateException( "Root is a child!" );
            } else {
                throw new IllegalStateException( "Unrecognized aspect: " + n.getKey() );
            }
        }

    }

    /**
     * Make double-extra sure, recursively.
     *
     * @param n
     * @param aspect
     */
    void fillAspect( DirectedGraphNode<String, GeneSetTerm> n ) {
        String aspect = n.getItem().getAspect();
        for ( DirectedGraphNode<String, GeneSetTerm> c : n.getChildNodes() ) {
            GeneSetTerm item = c.getItem();
            assert StringUtils.isBlank( item.getAspect() ) || item.getAspect().equals( aspect );
            item.setAspect( aspect );
            fillAspect( c );
        }
    }

}