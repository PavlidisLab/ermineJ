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

import java.io.File;
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
import ubic.erminej.Settings;

/**
 * Maintain information on GeneSetTerms (including but not limited GO terms). Note that this does not keep track of
 * which genes are annotated with these terms.
 * 
 * Related: {@link ubic.erminej.data.GeneSet}, which represents the actual annotations (and Gene and Probes also keep
 * track of their own)
 * and {@link ubic.erminej.data.UserDefinedGeneSetManager} which helps deal with ones the user has stored separately
 * from
 * GO
 *
 * @author Paul Pavlidis
 * @author Homin Lee
 * @see    GeneAnnotations
 * 
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

    /** Constant <code>log</code> */
    protected static final Log log = LogFactory.getLog( GeneSetTerms.class );

    private DirectedGraph<String, GeneSetTerm> graph;

    /**
     * Create based only on 'user defined', without reading in GO. Unless you construct the terms specially, this will
     * be a flat set (not a hierarchy)
     *
     * @param terms a {@link java.util.Collection} object.
     */
    public GeneSetTerms( Collection<GeneSetTerm> terms ) {
        if ( terms == null || terms.isEmpty() ) {
            throw new IllegalArgumentException( "Terms cannot be missing" );
        }

        this.graph = new DirectedGraph<>();

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
     * <p>
     * Constructor for GeneSetTerms.
     * </p>
     *
     * @param  i                   a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public GeneSetTerms( InputStream i ) throws IOException {
        this.initialize( i, false );
        i.close();
    }

    /**
     * Only kept for compatibility with some tests that are annoying to rewrite, and which use the old XML-RDF format.
     * 
     * @param  i
     * @param  oldrdf
     * @throws IOException
     */
    public GeneSetTerms( InputStream i, boolean oldrdf ) throws IOException {
        this.initialize( i, oldrdf );
    }

    /**
     * <p>
     * Constructor for GeneSetTerms. File type is detected by the name.
     * </p>
     *
     * @throws java.io.IOException if any.
     * @param  fileName            a {@link java.lang.String} object.
     */
    public GeneSetTerms( String fileName ) throws IOException {
        if ( fileName == null || fileName.length() == 0 ) {
            throw new IllegalArgumentException( "Invalid filename " + fileName + " or no filename was given" );
        }

        try (InputStream i = FileTools.getInputStreamFromPlainOrCompressedFile( fileName )) {
            this.initialize( i, false );
        }
    }

    /**
     * <p>
     * Constructor for GeneSetTerms.
     * </p>
     *
     * @throws org.xml.sax.SAXException if any.
     * @throws java.io.IOException      if any.
     * @param  fileName                 a {@link java.lang.String} object.
     * @param  settings                 a {@link ubic.erminej.Settings} object.
     */
    public GeneSetTerms( String fileName, Settings settings ) throws IOException {
        this( fileName );
        // Ensure the settings are updated, if we are calling this with the bare API
        settings.setClassFile( new File( fileName ).getAbsolutePath() );
    }

    /**
     * <p>
     * getAllChildren.
     * </p>
     *
     * @param  term a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return      a {@link java.util.Set} object.
     */
    public Set<GeneSetTerm> getAllChildren( GeneSetTerm term ) {
        if ( getGraph() == null ) return null;
        Set<GeneSetTerm> returnVal = new HashSet<>();
        Set<DirectedGraphNode<String, GeneSetTerm>> children = getGraph().get( term.getId() ).getAllChildNodes();
        for ( DirectedGraphNode<String, GeneSetTerm> child : children ) {
            GeneSetTerm childKey = child.getItem();
            returnVal.add( childKey );
        }
        return returnVal;
    }

    /**
     * <p>
     * getAllParents.
     * </p>
     *
     * @param  id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return    all parents EXCEPT for obsolete terms, aspect and root.
     */
    public Collection<GeneSetTerm> getAllParents( GeneSetTerm id ) {
        assert id != null;
        if ( getGraph() == null ) return null;
        Collection<GeneSetTerm> returnVal = new HashSet<>();

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
     * <p>
     * getChildren.
     * </p>
     *
     * @param  id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return    a Set containing the ids of geneSets which are immediately below the selected one in the hierarchy.
     */
    public Set<GeneSetTerm> getChildren( GeneSetTerm id ) {
        if ( getGraph() == null ) return null;
        Set<GeneSetTerm> returnVal = new HashSet<>();
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
     * <p>
     * getGeneSets.
     * </p>
     *
     * @return all the geneset terms.
     */
    public Set<GeneSetTerm> getGeneSets() {
        return Collections.unmodifiableSet( new HashSet<>( this.getGraph().getValues() ) );
    }

    /**
     * <p>
     * Getter for the field <code>graph</code>.
     * </p>
     *
     * @return graph representation of the term hierarchy
     */
    public DirectedGraph<String, GeneSetTerm> getGraph() {
        assert graph != null;
        return graph;
    }

    /**
     * Get direct parent terms
     * 
     * @param  id         term id to get parents for
     * @param  keepAspect whether to retain the aspect as a parent
     * @return            ids of geneSets which are immediately above the selected one in the hierarchy
     */
    private Collection<GeneSetTerm> getParents( GeneSetTerm id, boolean keepAspect ) {
        assert id != null;
        if ( getGraph() == null ) return null;
        Collection<GeneSetTerm> returnVal = new HashSet<>();

        if ( !getGraph().containsKey( id.getId() ) ) {
            log.debug( "GeneSet " + id + " doesn't exist in graph" ); // this is not really a problem.
            return returnVal;
        }

        Set<DirectedGraphNode<String, GeneSetTerm>> parents = getGraph().get( id.getId() ).getParentNodes();
        if ( parents.isEmpty() ) {
            log.debug( "No parents for " + id );
        }
        for ( DirectedGraphNode<String, GeneSetTerm> parent : parents ) {
            if ( parent == null ) continue;
            GeneSetTerm goEntry = parent.getItem();
            if ( goEntry == null ) continue;
            if ( goEntry.getId().equals( "all" ) ) continue;
            if ( goEntry.getId().equals( "top" ) ) continue; // very old GO
            if ( goEntry.isAspect() && !keepAspect ) {
                continue;
            }
            returnVal.add( goEntry );
        }
        return returnVal;
    }

    /**
     * <p>
     * Get direct parent terms
     * </p>
     *
     * @param  id GO id
     * @return    ids of geneSets which are immediately above the selected one in the hierarchy -
     *            but
     *            excluding the aspect or the root. Thus if the parameter is an aspect, nothing will be returned.
     */
    public Collection<GeneSetTerm> getParents( GeneSetTerm id ) {
        return getParents( id, false );
    }

    /**
     * <p>
     * getTerms.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<GeneSetTerm> getTerms() {
        return Collections.unmodifiableCollection( getGraph().getValues() );
    }

    /*
     *
     */
    /**
     * <p>
     * getTreeModel.
     * </p>
     *
     * @return a {@link javax.swing.tree.DefaultTreeModel} object.
     */
    public DefaultTreeModel getTreeModel() {
        if ( getGraph() == null ) return null;
        return this.getGraph().getTreeModel();
    }

    /**
     * <p>
     * isLeaf.
     * </p>
     *
     * @param  id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return    true if this term has no children
     */
    public boolean isLeaf( GeneSetTerm id ) {
        return getChildren( id ).isEmpty();
    }

    /**
     * <p>
     * isParent.
     * </p>
     *
     * @param  p a {@link ubic.erminej.data.GeneSetTerm} object.
     * @param  c a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return   true if p is a parent of c (somewhere in the hierarchy.
     */
    public boolean isParent( GeneSetTerm p, GeneSetTerm c ) {
        return this.getAllParents( c ).contains( p );
    }

    /**
     * Add a gene set to the graph, so it can be shown in tree views etc.
     *
     * @param id
     * @param name
     */
    void addUserDefinedTerm( GeneSetTerm id ) {

        if ( id.getAspect() == null ) id.setAspect( USER_DEFINED );

        // FIXME I think this happens already by this point, so it's already present. This is a (minor) bug.
        // assert this.getGraph().get( id.getId() ) == null : "Programming error: annots already contains " + id;

        if ( this.getGraph().get( USER_DEFINED ).hasChild( id.getId() ) ) {
            return;
        }

        assert this.getGraph().get( USER_DEFINED ) != null;
        this.getGraph().addChildTo( USER_DEFINED, id.getId(), id );
    }

    /**
     * Note that it is probably preferable to use findTerm from GeneAnnotations; this is used while setting that up.
     *
     * @param  geneSetId a {@link java.lang.String} object.
     * @return           GeneSetTerm
     */
    protected GeneSetTerm get( String geneSetId ) {
        DirectedGraphNode<String, GeneSetTerm> directedGraphNode = this.graph.getItems().get( geneSetId );
        if ( directedGraphNode == null ) {
            return null;
        }
        return directedGraphNode.getItem();
    }

    /**
     * <p>
     * removeUserDefined.
     * </p>
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     */
    protected void removeUserDefined( GeneSetTerm id ) {
        if ( id.isUserDefined() && this.graph.containsKey( id.getId() ) ) {
            this.graph.deleteLeaf( id.getId() );
        }
    }

    /***
     * @param  term
     * @param  goNames
     * @return
     */
    private String getAspect( GeneSetTerm term ) {
        if ( term.getAspect() != null ) return term.getAspect();

        if ( term.isAspect() ) {
            return term.getName();
        }

        Collection<GeneSetTerm> parents = getParents( term, true );
        if ( parents.isEmpty() ) {

            log.info( "no parents for " + term );
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
     * @param  inputStream
     * @param  boolean     oldrdf maintained only for compatibility of one test
     * @throws IOException
     */
    private void initialize( InputStream inputStream, boolean oldrdf ) throws IOException {
        GOParser parser = null;

        if ( oldrdf ) {
            try {
                // only kept for compatibility of some old tests that would be a pain to rewrite
                parser = new GOXMLParser( inputStream );
            } catch ( SAXException e ) {
                throw new IllegalStateException( e );
            }
        } else {
            parser = new GOOBOParser( inputStream );
        }

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
                String aspect = this.getAspect( geneSet );
                if ( aspect == null && !geneSet.getDefinition().startsWith( "OBSOLETE" ) ) {
                    log.info( "Could not locate aspect for " + geneSet );
                    continue;
                }
                geneSet.setAspect( aspect );
            }
        }

    }

}
