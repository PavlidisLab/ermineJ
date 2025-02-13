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
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ubic.basecode.dataStructure.graph.DirectedGraph;

/**
 * Read in the GO XML file provided by the Gene Ontology Consortium. Only kept for compatibility of some old tests that
 * would be a pain to rewrite!
 * 
 * @author Paul Pavlidis
 */
public class GOXMLParser extends GOParser {

    public GOXMLParser( InputStream i ) throws IOException, SAXException {
        this( i, false );
    }

    /**
     * @param  i
     * @param  oldFormat    if true, use the old-fashioned scheme
     * @throws IOException
     * @throws SAXException
     */
    public GOXMLParser( InputStream i, boolean oldFormat ) throws IOException, SAXException {

        if ( i.available() == 0 ) {
            throw new IOException( "XML stream contains no data." );
        }

        System.setProperty( "org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser" );

        XMLReader xr = XMLReaderFactory.createXMLReader();
        GOHandler handler;
        if ( oldFormat ) {
            handler = new OldGOHandler();
        } else {
            handler = new GOHandler();
        }
        xr.setFeature( "http://xml.org/sax/features/validation", false );
        xr.setFeature( "http://xml.org/sax/features/external-general-entities", false );
        xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

        xr.setContentHandler( handler );
        xr.setErrorHandler( handler );
        xr.setEntityResolver( handler );
        xr.setDTDHandler( handler );
        InputSource inputSource = new InputSource( i );

        xr.parse( inputSource );

        this.termGraph = handler.getResults();

        this.getGraph().addParentTo( "GO:0003674", ALL );
        this.getGraph().addParentTo( "GO:0008150", ALL );
        this.getGraph().addParentTo( "GO:0005575", ALL );

        populateAspect();

        i.close();

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

}

class GOHandler extends DefaultHandler {
    protected DirectedGraph<String, GeneSetTerm> termGraph;

    protected Collection<String> forbiddenParents = new HashSet<>();

    protected boolean inTerm = false;

    protected boolean inDef = false;
    protected boolean inAcc = false;
    protected boolean inName = false;
    protected StringBuffer nameBuf;
    protected StringBuffer accBuf = null;

    protected StringBuffer defBuf;

    public GOHandler() {
        super();
        termGraph = new DirectedGraph<>();
        init();
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
        } else if ( name.equals( "is_a" ) || name.equals( "part_of" ) || name.equals( "part-of" )
                || name.equals( "isa" ) ) {

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
     *
     */
    protected void init() {
        /*
         * This is a workaround for a change in GO: the terms obsolete_molecular_function etc. are never defined.
         * 
         */
        initializeNewNode( "all" );

        forbiddenParents.add( "obsolete_molecular_function" );
        forbiddenParents.add( "obsolete_biological_process" );
        forbiddenParents.add( "obsolete_cellular_component" );
    }

    /**
     * @param id
     */
    protected void initializeNewNode( String id ) {
        GeneSetTerm item = new GeneSetTerm( id, "[No name provided]", "[No definition]" );
        termGraph.addNode( id, item );
    }

}

/**
 * For GO rdf-xml before ~ 2004 or so, it was even more different.
 */
class OldGOHandler extends GOHandler {

    private String currentAspect;

    private String currentTerm;

    public OldGOHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see ubic.erminej.data.GOHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement( String uri, String name, String qName ) {
        if ( name.equals( "term" ) ) {
            inTerm = false;
        } else if ( name.equals( "accession" ) ) {
            inAcc = false;
            currentTerm = accBuf.toString();
            initializeNewNode( currentTerm );
        } else if ( name.equals( "definition" ) ) {
            if ( StringUtils.isBlank( currentTerm ) ) currentTerm = accBuf.toString();
            termGraph.getNodeContents( currentTerm ).setDefinition( defBuf.toString() );
            inDef = false;
        } else if ( name.equals( "name" ) ) {
            inName = false;

            if ( StringUtils.isBlank( currentTerm ) ) currentTerm = accBuf.toString();
            String currentName = nameBuf.toString();

            GeneSetTerm term = termGraph.getNodeContents( currentTerm );
            assert term != null;

            term.setName( currentName );

            if ( currentName.equalsIgnoreCase( "molecular_function" )
                    || currentName.equalsIgnoreCase( "biological_process" )
                    || currentName.equalsIgnoreCase( "cellular_component" )
                    || currentName.equalsIgnoreCase( "obsolete_molecular_function" )
                    || currentName.equalsIgnoreCase( "obsolete_biological_process" )
                    || currentName.equalsIgnoreCase( "obsolete_cellullar_component" ) ) {
                currentAspect = currentName;
                termGraph.getNodeContents( currentTerm ).setAspect( currentAspect );
            }

        }
    }

    @Override
    public void startElement( String uri, String name, String qName, Attributes atts ) {

        if ( name.equals( "term" ) ) {
            inTerm = true;

            /*
             * Some very old GO XML files lack the go:accession element. This is probably a bug in the generation of
             * those files, but it means we have to get the accession from the rdf:about part. This will get clobbered
             * if there is an "accession" element.
             */
            String res = atts.getValue( "rdf:about" );
            currentTerm = res.substring( res.lastIndexOf( '#' ) + 1, res.length() );

            initializeNewNode( currentTerm );

        } else if ( name.equals( "accession" ) ) {
            accBuf = new StringBuffer();
            inAcc = true;
        } else if ( name.equals( "definition" ) ) {
            defBuf = new StringBuffer();
            inDef = true;
        } else if ( name.equals( "is_a" ) || name.equals( "part_of" ) || name.equals( "part-of" )
                || name.equals( "isa" ) ) {

            String res = atts.getValue( "rdf:resource" );
            String parent = res.substring( res.lastIndexOf( '#' ) + 1, res.length() );

            if ( !termGraph.containsKey( parent ) ) {
                initializeNewNode( parent );
            }
            if ( StringUtils.isBlank( currentTerm ) ) currentTerm = accBuf.toString();

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

}
