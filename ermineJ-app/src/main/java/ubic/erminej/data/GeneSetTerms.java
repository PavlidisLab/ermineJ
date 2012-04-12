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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import ubic.basecode.dataStructure.graph.DirectedGraph;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.basecode.util.FileTools;

/**
 * Maintain information on GeneSetTerms (including but not limited GO terms). Note that this does not keep track of
 * which genes are annotated with these terms.
 * 
 * @author Paul Pavlidis
 * @author Homin Lee
 * @version $Id$
 * @see GeneAnnotations
 * @see GeneSet which represents the actual annotations (and Gene and Probes also keep track of their own)
 * @see GeneSets which provides convenient methods to work with the sets themselves.
 * @see UserDefinedGeneSetManager which helps deal with ones the user has stored separately from GO
 */
public class GeneSetTerms {

    /**
     * Name for aspect when none is defined.
     */
    static public final String NO_ASPECT_AVAILABLE = "[No aspect available]";

    /**
     * Name for root of tree representing user-defined gene sets.
     */
    public static final String USER_DEFINED = "User-defined";

    protected static final Log log = LogFactory.getLog( GeneSetTerms.class );

    private DirectedGraph<String, GeneSetTerm> graph;

    /**
     * @param inputStream assumed to be in recent format.
     * @throws IOException
     * @throws SAXException
     */
    public GeneSetTerms( InputStream inputStream ) throws IOException, SAXException {
        if ( inputStream == null ) {
            throw new IOException( "Input stream was null" );
        }
        this.initialize( inputStream, false );
        inputStream.close();
    }

    /**
     * @param fileNamefilename <code>String</code> The XML file containing class to name mappings. First column is the
     *        class id, second is a description that will be used init program output.
     * @throws SAXException
     * @throws IOException
     */
    public GeneSetTerms( String fileName ) throws SAXException, IOException {
        this( fileName, false );
    }

    /**
     * @param filename <code>String</code> The XML file containing class to name mappings. First column is the class id,
     *        second is a description that will be used init program output.
     * @param oldFormat set to true to indicate that the RDF is 'old style' (pre ~2008)
     * @throws IOException
     * @throws SAXException
     */
    public GeneSetTerms( String fileName, boolean oldFormat ) throws SAXException, IOException {
        if ( fileName == null || fileName.length() == 0 ) {
            throw new IllegalArgumentException( "Invalid filename " + fileName + " or no filename was given" );
        }

        InputStream i = FileTools.getInputStreamFromPlainOrCompressedFile( fileName );
        this.initialize( i, oldFormat );
        i.close();
    }

    /**
     * Create based only on 'user defined', without reading in GO. Unless you construct the terms specially, this will
     * be a flat set (not a hierarchy)
     * 
     * @param terms
     */
    public GeneSetTerms( Collection<GeneSetTerm> terms ) {
        this.graph = new DirectedGraph<String, GeneSetTerm>();

        GeneSetTerm item = new GeneSetTerm( "all", "[No name provided]", "[No definition]" );
        item.setAspect( "all" );
        graph.addNode( "all", item );

        DirectedGraphNode<String, GeneSetTerm> root = this.getGraph().getRoot();
        GeneSetTerm newChild = new GeneSetTerm( USER_DEFINED, "", "Gene sets modified or created by the user" );
        newChild.setAspect( USER_DEFINED );
        this.getGraph().addChildTo( root.getKey(), USER_DEFINED, newChild );

        for ( GeneSetTerm t : terms ) {
            t.setUserDefined( true );
            this.addUserDefinedTerm( t );
        }
    }

    /**
     * @param id
     */
    protected void removeUserDefined( GeneSetTerm id ) {
        if ( id.isUserDefined() ) {
            this.graph.deleteLeaf( id.getId() );
        }
    }

    /**
     * Note that it is probably preferable to use findTerm from GeneAnnotations; this is used while setting that up.
     * 
     * @param geneSetId
     * @return GeneSetTerm
     */
    protected GeneSetTerm get( String geneSetId ) {
        DirectedGraphNode<String, GeneSetTerm> directedGraphNode = this.graph.getItems().get( geneSetId );
        if ( directedGraphNode == null ) {
            return null;
        }
        return directedGraphNode.getItem();
    }

    // /**
    // * @param id
    // * @return The length of the shortest path to the root
    // */
    // public int depth( GeneSetTerm id ) {
    // Collection<GeneSetTerm> parents = getParents( id );
    // return depth( id, parents, 0 );
    // }
    //
    // /*
    // * FIXME
    // */
    // private int depth( GeneSetTerm id, Collection<GeneSetTerm> parents, int i ) {
    // for ( GeneSetTerm p : parents ) {
    // depth( p, getParents( p ), i );
    // }
    // return i;
    // }

    /**
     * @param id
     * @return true if this term has no children
     */
    public boolean isLeaf( GeneSetTerm id ) {
        return getChildren( id ).isEmpty();
    }

    /**
     * @param id
     * @return a Set containing the ids of geneSets which are immediately below the selected one in the hierarchy.
     */
    public Set<GeneSetTerm> getChildren( GeneSetTerm id ) {
        if ( getGraph() == null ) return null;
        Set<GeneSetTerm> returnVal = new HashSet<GeneSetTerm>();
        DirectedGraphNode<String, GeneSetTerm> node = getGraph().get( id.getId() );

        assert node != null;

        Set<DirectedGraphNode<String, GeneSetTerm>> children = node.getChildNodes();
        for ( DirectedGraphNode<String, GeneSetTerm> child : children ) {
            GeneSetTerm childKey = child.getItem();
            returnVal.add( childKey );
        }
        return returnVal;
    }

    /**
     * @return all the geneset terms.
     */
    public Set<GeneSetTerm> getGeneSets() {
        return Collections.unmodifiableSet( new HashSet<GeneSetTerm>( this.getGraph().getValues() ) );
    }

    /**
     * @return graph representation of the term hierarchy
     */
    public DirectedGraph<String, GeneSetTerm> getGraph() {
        return graph;
    }

    /**
     * @param id GO id
     * @return a Set containing the ids of geneSets which are immediately above the selected one in the hierarchy - but
     *         excluding the aspect or the root. Thus if the parameter is an aspect, nothing will be returned.
     */
    public Collection<GeneSetTerm> getParents( GeneSetTerm id ) {
        assert id != null;
        if ( getGraph() == null ) return null;
        Collection<GeneSetTerm> returnVal = new HashSet<GeneSetTerm>();

        if ( !getGraph().containsKey( id.getId() ) ) {
            log.debug( "GeneSet " + id + " doesn't exist in graph" ); // this is not really a problem.
            return returnVal;
        }

        Set<DirectedGraphNode<String, GeneSetTerm>> parents = getGraph().get( id.getId() ).getParentNodes();
        for ( DirectedGraphNode<String, GeneSetTerm> parent : parents ) {
            if ( parent == null ) continue;
            GeneSetTerm goEntry = parent.getItem();
            if ( goEntry == null ) continue;
            if ( goEntry.getId().equals( "all" ) ) continue;
            if ( goEntry.isAspect() ) continue;
            returnVal.add( goEntry );
        }
        return returnVal;
    }

    /**
     * @param id
     * @return all parents EXCEPT for obsolete terms, aspect and root.
     */
    public Collection<GeneSetTerm> getAllParents( GeneSetTerm id ) {
        assert id != null;
        if ( getGraph() == null ) return null;
        Collection<GeneSetTerm> returnVal = new HashSet<GeneSetTerm>();

        if ( !getGraph().containsKey( id.getId() ) ) {
            // log.debug( "GeneSet " + id + " doesn't exist in graph" ); // this is not really a problem.
            return returnVal;
        }

        Set<DirectedGraphNode<String, GeneSetTerm>> parents = getGraph().get( id.getId() ).getAllParentNodes();
        for ( DirectedGraphNode<String, GeneSetTerm> parent : parents ) {
            if ( parent == null ) continue;
            GeneSetTerm goEntry = parent.getItem();
            if ( goEntry == null ) continue;
            if ( goEntry.getId().equals( "all" ) ) continue;
            if ( goEntry.isAspect() ) continue;
            if ( goEntry.getDefinition().startsWith( "OBSOLETE" ) ) continue;
            returnVal.add( goEntry );
        }
        return returnVal;
    }

    /**
     * @return
     */
    public Collection<GeneSetTerm> getTerms() {
        return Collections.unmodifiableCollection( getGraph().getValues() );
    }

    /*
     * 
     */
    public DefaultTreeModel getTreeModel() {
        if ( getGraph() == null ) return null;
        return this.getGraph().getTreeModel();
    }

    /**
     * Add a gene set to the graph, so it can be shown in tree views etc.
     * 
     * @param id
     * @param name
     */
    void addUserDefinedTerm( GeneSetTerm id ) {
        assert id.isUserDefined();

        // FIXME I think this happens already by this point, so it's already present. This is a (minor) bug.
        // assert this.getGraph().get( id.getId() ) == null : "Programming error: annots already contains " + id;
        if ( this.getGraph().get( USER_DEFINED ).hasChild( id.getId() ) ) {
            return;
        }

        assert this.getGraph().get( USER_DEFINED ) != null;
        id.setAspect( USER_DEFINED ); // defensive.
        this.getGraph().addChildTo( USER_DEFINED, id.getId(), id );
    }

    /***
     * @param term
     * @param goNames
     * @return
     */
    private String getAspect( GeneSetTerm term ) {
        if ( term.getAspect() != null ) return term.getAspect();

        Collection<GeneSetTerm> parents = getParents( term );
        if ( parents.isEmpty() ) {
            return null;
        }
        for ( GeneSetTerm parent : parents ) {
            String a = getAspect( parent );
            if ( a != null ) {
                return a;
            }
        }
        return null;
    }

    /**
     * @param inputStream
     * @param oldFormat
     * @throws IOException
     * @throws SAXException
     */
    private void initialize( InputStream inputStream, boolean oldFormat ) throws IOException, SAXException {
        GOParser parser = new GOParser( inputStream, oldFormat );
        this.graph = parser.getGraph();

        assert graph != null;

        /*
         * Add the 'user-defined' node.
         */
        DirectedGraphNode<String, GeneSetTerm> root = this.getGraph().getRoot();
        assert root != null;

        GeneSetTerm newChild = new GeneSetTerm( USER_DEFINED, "Custom", "Gene sets created by the user" );
        newChild.setAspect( USER_DEFINED );
        this.getGraph().addChildTo( root.getKey(), USER_DEFINED, newChild );

        /*
         * Populate the aspects for all nodes.
         */
        for ( GeneSetTerm geneSet : this.getGraph().getValues() ) {
            if ( geneSet.getAspect() == null ) {
                geneSet.setAspect( this.getAspect( geneSet ) );
            }
        }

    }

    /**
     * @param term
     * @return
     */
    public Set<GeneSetTerm> getAllChildren( GeneSetTerm term ) {
        if ( getGraph() == null ) return null;
        Set<GeneSetTerm> returnVal = new HashSet<GeneSetTerm>();
        Set<DirectedGraphNode<String, GeneSetTerm>> children = getGraph().get( term.getId() ).getAllChildNodes();
        for ( DirectedGraphNode<String, GeneSetTerm> child : children ) {
            GeneSetTerm childKey = child.getItem();
            returnVal.add( childKey );
        }
        return returnVal;
    }

    /**
     * @param p
     * @param c
     * @return true if p is a parent of c (somewhere in the hierarchy.
     */
    public boolean isParent( GeneSetTerm p, GeneSetTerm c ) {
        return this.getAllParents( c ).contains( p );
    }

}