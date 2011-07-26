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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ubic.basecode.dataStructure.graph.DirectedGraph;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;

/**
 * Read in the GO XML file provided by the Gene Ontology Consortium.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GOParser {

    private DirectedGraph<String, GeneSetTerm> termGraph;

    /**
     * @param i
     * @throws IOException
     * @throws SAXException
     */
    public GOParser( InputStream i ) throws IOException, SAXException {

        if ( i.available() == 0 ) {
            throw new IOException( "XML stream contains no data." );
        }

        System.setProperty( "org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser" );

        XMLReader xr = XMLReaderFactory.createXMLReader();
        GOHandler handler = new GOHandler();
        xr.setFeature( "http://xml.org/sax/features/validation", false );
        xr.setFeature( "http://xml.org/sax/features/external-general-entities", false );
        xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
        xr.setContentHandler( handler );
        xr.setErrorHandler( handler );
        xr.setEntityResolver( handler );
        xr.setDTDHandler( handler );
        xr.parse( new InputSource( i ) );

        termGraph = handler.getResults();

        populateAspect();

        i.close();

    }

    /**
     * Get a simple Map that contains keys that are the GO ids, values are the names. This can replace the functionality
     * of the GONameReader in classScore.
     * 
     * @return Map
     */
    public Map<String, GeneSetTerm> getGONameMap() {
        Map<String, DirectedGraphNode<String, GeneSetTerm>> nodes = termGraph.getItems();
        Map<String, GeneSetTerm> result = new HashMap<String, GeneSetTerm>();
        for ( DirectedGraphNode<String, GeneSetTerm> node : nodes.values() ) {
            GeneSetTerm e = node.getItem();
            result.put( e.getId(), e );
        }
        return result;
    }

    /**
     * Get the graph that was created.
     * 
     * @return a DirectedGraph. Nodes contain OntologyEntry instances.
     */
    public DirectedGraph<String, GeneSetTerm> getGraph() {
        return termGraph;
    }

    private void populateAspect() {
        DirectedGraphNode<String, GeneSetTerm> root = this.getGraph().getRoot();

        Set<DirectedGraphNode<String, GeneSetTerm>> childNodes = root.getChildNodes();

        for ( DirectedGraphNode<String, GeneSetTerm> n : childNodes ) {
            n.getItem().setAspect( "Root" );
            if ( n.getKey().equals( "GO:0003674" ) ) {
                for ( DirectedGraphNode<String, GeneSetTerm> t : n.getAllChildNodes() ) {
                    t.getItem().setAspect( "molecular_function" );
                }
            } else if ( n.getKey().equals( "GO:0008150" ) ) {
                for ( DirectedGraphNode<String, GeneSetTerm> t : n.getAllChildNodes() ) {
                    t.getItem().setAspect( "biological_process" );
                }
            } else if ( n.getKey().equals( "GO:0005575" ) ) {
                for ( DirectedGraphNode<String, GeneSetTerm> t : n.getAllChildNodes() ) {
                    t.getItem().setAspect( "cellular_component" );
                }
            } else {
                throw new IllegalStateException( "Unrecognized aspect: " + n.getKey() );
            }
        }

    }

}

class GOHandler extends DefaultHandler {
    private DirectedGraph<String, GeneSetTerm> termGraph;

    private Collection<String> forbiddenParents = new HashSet<String>();

    private boolean inTerm = false;

    private boolean inDef = false;
    private boolean inAcc = false;
    private boolean inName = false;
    // private String currentAspect;
    private StringBuffer nameBuf;
    private StringBuffer accBuf;

    private StringBuffer defBuf;

    public GOHandler() {
        super();
        termGraph = new DirectedGraph<String, GeneSetTerm>();

        /*
         * This is a workaround for a change in GO: the terms obsolete_molecular_function etc. are never defined. See
         * bug
         */
        initializeNewNode( "all" );

        forbiddenParents.add( "obsolete_molecular_function" );
        forbiddenParents.add( "obsolete_biological_process" );
        forbiddenParents.add( "obsolete_cellular_component" );

    }

    @Override
    public void characters( char ch[], int start, int length ) {

        if ( inTerm ) {
            if ( inAcc ) {
                accBuf.append( ch, start, length );
            } else if ( inDef ) {
                defBuf.append( ch, start, length );
            } else if ( inName ) {
                nameBuf.append( ch, start, length );
            }
        }
    }

    @Override
    public void endElement( String uri, String name, String qName ) {
        if ( name.equals( "term" ) ) {
            inTerm = false;
        } else if ( name.equals( "accession" ) ) {
            inAcc = false;
            String currentTerm = accBuf.toString();
            initializeNewNode( currentTerm );
        } else if ( name.equals( "definition" ) ) {
            String currentTerm = accBuf.toString();
            termGraph.getNodeContents( currentTerm ).setDefinition( defBuf.toString() );
            inDef = false;
        } else if ( name.equals( "is_a" ) ) {

        } else if ( name.equals( "part_of" ) ) {

        } else if ( name.equals( "synonym" ) ) {

        } else if ( name.equals( "negatively_regulates" ) ) {

        } else if ( name.equals( "positively_regulates" ) ) {

        } else if ( name.equals( "regulates" ) ) {

        } else if ( name.equals( "name" ) ) {
            inName = false;
            String currentTerm = accBuf.toString();

            String currentName = nameBuf.toString();

            GeneSetTerm term = termGraph.getNodeContents( currentTerm );
            term.setName( currentName );
        }
    }

    public DirectedGraph<String, GeneSetTerm> getResults() {
        return termGraph;
    }

    @Override
    public void startElement( String uri, String name, String qName, Attributes atts ) {

        if ( name.equals( "term" ) ) {
            inTerm = true;
        } else if ( name.equals( "accession" ) ) {
            accBuf = new StringBuffer();
            inAcc = true;
        } else if ( name.equals( "definition" ) ) {
            defBuf = new StringBuffer();
            inDef = true;
        } else if ( name.equals( "is_a" ) || name.equals( "part_of" ) ) {

            String res = atts.getValue( "rdf:resource" );
            String parent = res.substring( res.lastIndexOf( '#' ) + 1, res.length() );

            if ( !termGraph.containsKey( parent ) ) {
                initializeNewNode( parent );
            }
            String currentTerm = accBuf.toString();

            if ( !forbiddenParents.contains( parent ) ) {
                termGraph.addParentTo( currentTerm, parent );
            }

        } else if ( name.equals( "synonym" ) ) {
            // inSyn = true;
        } else if ( name.equals( "name" ) ) {
            nameBuf = new StringBuffer();
            inName = true;
        }
    }

    /**
     * @param id
     */
    private void initializeNewNode( String id ) {
        GeneSetTerm item = new GeneSetTerm( id, "[No name provided]", "[No definition]" );
        termGraph.addNode( id, item );
    }

}