/*
 * The ermineJ project
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
package ubic.erminej.gui.geneset.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import javax.help.UnsupportedOperationException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.basecode.util.StringUtil;
import ubic.erminej.Settings;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSetTerms;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.geneset.GeneSetPanel;
import ubic.erminej.gui.geneset.GeneSetPanelPopupMenu;
import ubic.erminej.gui.util.Colors;

/**
 * A Tree display that shows Gene Sets and their scores, and allows uer interaction.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreePanel extends GeneSetPanel {

    private static Log log = LogFactory.getLog( GeneSetTreePanel.class );

    private static final long serialVersionUID = 0L;
    private JTree goTree = null;
    protected GeneSetTerm currentlySelectedGeneSet = null;

    private GeneSetTreeNodeRenderer rend;

    private TreeModel geneSetTreeModel;

    private FilteredGeneSetTreeModel filteredTreeModel;

    private TreePath currentlySelectedTreePath = null;

    private Collection<GeneSetTerm> currentSelectedSets = new HashSet<GeneSetTerm>();

    public GeneSetTreePanel( MainFrame callingFrame, List<GeneSetPvalRun> results, Settings settings ) {
        super( settings, results, callingFrame );
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addedNewGeneSet()
     */
    @Override
    public void addedGeneSet( GeneSetTerm id ) {
        /*
         * It's already in the graph, we just need to rebuild the node?
         */

        goTree.revalidate();

        TreePath existing = find( id );

        if ( existing != null ) {
            log.warn( "Node was already there: " + id );
            return;
        }

        GeneSetTreeNode userNode = getUserNode();
        assert userNode != null;

        DirectedGraphNode<String, GeneSetTerm> nodeThatWasAdded = this.geneData.getGeneSetGraph().get( id.getId() );

        if ( nodeThatWasAdded == null ) {
            log.warn( "THE NODE IS NOT IN THE TREE!! " + id );
            return;
        }

        GeneSetTreeNode newTreeNode = new GeneSetTreeNode( nodeThatWasAdded );

        userNode.insert( newTreeNode, 0 );

        goTree.revalidate();

    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addRun()
     */
    @Override
    public void addRun() {
        fireResultsChanged();
    }

    /**
     * @param classID a node somewhere in the tree; we expand the path
     */
    public boolean expandToGeneSet( GeneSetTerm classID ) {

        if ( classID == null ) return false;

        final TreePath path = this.find( classID );
        boolean foundIt = false;
        if ( path == null ) {
            this.callingFrame.getStatusMessenger().showError(
                    "Could not find " + classID + " in any aspect, the term may be obsolete." );
            foundIt = false;
        } else {
            this.callingFrame.getStatusMessenger().showStatus( "Showing " + classID );
            foundIt = true;
        }
        if ( foundIt ) {
            log.debug( "Expanding to path for " + classID );

            SwingWorker<Object, Object> w = new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    goTree.expandPath( path );
                    goTree.setSelectionPath( path );
                    goTree.scrollPathToVisible( path );
                    return null;
                }
            };
            w.execute();

        }
        return foundIt;
    }

    /**
     * @param selectedTerms
     */
    public void filter( Collection<GeneSetTerm> selectedTerms ) {

        this.currentSelectedSets = selectedTerms;
        this.rend.setSelectedTerms( selectedTerms );
        log.info( selectedTerms.size() + "selected" );
        refreshView();

        if ( !selectedTerms.isEmpty() ) {

            // try to show just the selected terms, within reason.
            if ( selectedTerms.size() < 200 ) {
                expandAll( true );
            } else {
                // just expand a few... not very nice.
                int i = 0;
                for ( GeneSetTerm t : selectedTerms ) {
                    expandNode( find( t ), true );
                    if ( ++i > 5 ) break;
                }
            }
        }
    }

    /**
     * @param tree
     * @param names
     * @return
     */
    public TreePath find( GeneSetTerm id ) {
        if ( id == null ) return null;
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();
        return find( new TreePath( root ), id, 0 );
    }

    /**
     * 
     */
    public void fireResultsChanged() {
        log.debug( "Changing results" );
        if ( callingFrame != null && this.goTree != null ) {

            ( ( GeneSetTreeNodeRenderer ) this.goTree.getCellRenderer() ).setCurrentResultSet( getCurrentResultSet() );
        }
        refreshView();
    }

    /**
     * @return Returns the currentlySelectedGeneSet.
     */
    public GeneSetTerm getCurrentlySelectedGeneSet() {
        return this.currentlySelectedGeneSet;
    }

    /**
     * Called via reflection == has to be public. DO NOT REMOVE
     * 
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void hasSelectedChild( GeneSetTreeNode node ) {
        node.setHasSelectedChild( false );

        if ( getCurrentSelectedSets().isEmpty() ) return;

        Enumeration<GeneSetTreeNode> e = node.breadthFirstEnumeration();
        e.nextElement();
        while ( e.hasMoreElements() ) {
            GeneSetTreeNode childNode = e.nextElement();
            GeneSetTerm t = childNode.getTerm();
            if ( getCurrentSelectedSets().contains( t ) ) {
                node.setHasSelectedChild( true );
                return;
            }
        }
    }

    private Collection<GeneSetTerm> getCurrentSelectedSets() {
        return this.currentSelectedSets;
    }

    /**
     * @return
     */
    public Collection<GeneSetTreeNode> getLeaves() {
        GeneSetTreeNode node = ( GeneSetTreeNode ) goTree.getModel().getRoot();
        return getLeaves( node );
    }

    /**
     * @param node
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<GeneSetTreeNode> getLeaves( GeneSetTreeNode node ) {
        Collection<GeneSetTreeNode> result = new HashSet<GeneSetTreeNode>();
        Enumeration<GeneSetTreeNode> dfe = node.depthFirstEnumeration();
        int i = 0;
        while ( dfe.hasMoreElements() ) {
            GeneSetTreeNode no = dfe.nextElement();
            if ( no.isLeaf() ) {
                result.add( no );
            }
            ++i;
        }

        return result;
    }

    /**
     * 
     */
    private void setNodeStatesForFilter() {
        Collection<GeneSetTreeNode> leaves = this.getLeaves();
        // log.info( leaves.size() + " leaves" ); // 22000!
        // int i = 0;
        for ( GeneSetTreeNode le : leaves ) {
            if ( getCurrentResultSet() != null ) {
                markHasSignificantChild( le, false );
            }
            markHasSelectedChild( le, false );
            // if ( ++i % 2000 == 0 ) {
            // log.info( "Checking branches " + i );
            // }
        }

    }

    /**
     * @param node
     * @param set
     */
    private void markHasSignificantChild( GeneSetTreeNode node, boolean set ) {

        if ( node == null ) return;

        if ( set ) {
            // we already found a path, so all are true.
            node.setHasSignificantChild( true );
            markHasSignificantChild( ( GeneSetTreeNode ) node.getParent(), true );
            return;
        }

        // check the node.
        boolean s1 = false;
        GeneSetResult result = getCurrentResultSet().getResults().get( node.getTerm() );
        if ( result != null && result.getCorrectedPvalue() <= GeneSetPanel.FDR_THRESHOLD_FOR_FILTER ) {
            s1 = true;
            node.setHasSignificantChild( true );
        } else {
            // node.setHasSignificantChild( false );
        }
        markHasSignificantChild( ( GeneSetTreeNode ) node.getParent(), s1 );

    }

    /**
     * @param node
     * @param set
     */
    private void markHasSelectedChild( GeneSetTreeNode node, boolean set ) {

        if ( node == null ) return;
        // log.info( node + " __>" );

        if ( set ) {
            // we already found a path, so all parents are true, we don't need to check.
            node.setHasSelectedChild( true );
      //      log.info( "marking: " + node );

            markHasSelectedChild( ( GeneSetTreeNode ) node.getParent(), true );
            return;
        }

        // check the node.
        boolean s1 = false;
        if ( this.getCurrentSelectedSets().contains( node.getTerm() ) ) {
            s1 = true;
            node.setHasSelectedChild( true );
        //    log.info( "end: " + node );
        } else {
            // node.setHasSelectedChild( false );
        }
        markHasSelectedChild( ( GeneSetTreeNode ) node.getParent(), s1 );
    }

    /**
     * Called via reflection == has to be public. DO NOT REMOVE
     * 
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void hasSignificantChild( GeneSetTreeNode node ) {

        // this is inefficient, because this called over the whole tree (see also hasSelectedChild). I can't think of a
        // way to do this with less than two full tree traversals. 1) find all the leaves 2) walk back to the root. JDK
        // recommends this approach.

        node.setHasSignificantChild( false );

        if ( getCurrentResultSet() == null ) {
            return;
        }

        Enumeration<GeneSetTreeNode> e = node.breadthFirstEnumeration();
        e.nextElement();
        while ( e.hasMoreElements() ) {
            GeneSetTreeNode childNode = e.nextElement();
            GeneSetTerm t = childNode.getTerm();
            GeneSetResult result = getCurrentResultSet().getResults().get( t );
            if ( result != null && result.getCorrectedPvalue() <= GeneSetPanel.FDR_THRESHOLD_FOR_FILTER ) {
                node.setHasSignificantChild( true );
                return;
            }
        }
    }

    /**
     * Called after data files are read in.
     * 
     * @param goData
     * @param geneData
     */
    public void initialize( GeneAnnotations gd ) {

        this.geneData = gd;
        setUpTree();

        filter( false );
        this.getViewport().add( goTree );
        this.goTree.setVisible( true );
        this.goTree.revalidate();

        /*
         * open up the first couple of nodes? (can't do more than one at the moment as filter recollapses everything.
         */
        this.expandToGeneSet( this.geneData.findTerm( "GO:0008150" ) ); // bio proc
        // this.expandToGeneSet( this.geneData.findTerm( "GO:0005575" ) ); //
        // this.expandToGeneSet( this.geneData.findTerm( "GO:0003674" ) ); //
    }

    /**
     * @param e
     */
    public void mouseReleased( MouseEvent e ) {
        if ( e.getClickCount() < 2 ) {
            return;
        }
        showDetailsForGeneSet( currentlySelectedGeneSet, getCurrentResultSet() );
    }

    @Override
    public void resetView() {
        // anyting else we need to do?
        // filter( new HashSet<GeneSetTerm>() );
    }

    /**
     * http://javaalmanac.com/egs/javax.swing.tree/FindNode.html
     * 
     * @param tree
     * @param parent
     * @param nodes
     * @param depth
     * @param byName
     * @return
     */
    private TreePath find( TreePath parent, GeneSetTerm id, int depth ) {
        GeneSetTreeNode node = ( GeneSetTreeNode ) parent.getLastPathComponent();
        GeneSetTerm o = node.getTerm();

        // If equal, go down the branch
        if ( o.equals( id ) ) {
            return parent;
        }

        DefaultTreeModel treeModel = this.geneData.getGeneSetGraph().getTreeModel();
        for ( int i = 0; i < treeModel.getChildCount( node ); i++ ) {
            TreeNode n = ( GeneSetTreeNode ) treeModel.getChild( node, i );
            TreePath path = parent.pathByAddingChild( n );
            TreePath result = find( path, id, depth + 1 );
            // Found a match
            if ( result != null ) {
                return result;
            }
        }

        // No match at this branch
        return null;
    }

    /**
     * @return
     */
    private GeneSetTreeNode getUserNode() {
        TreePath path = this.find( new GeneSetTerm( GeneSetTerms.USER_DEFINED ) );
        if ( path == null ) return null;
        return ( GeneSetTreeNode ) path.getLastPathComponent();
    }

    /**
     * Force to repaint, reapply filters.
     */
    @Override
    public void refreshView() {

        // SwingWorker<Object, Object> w = new SwingWorker<Object, Object>() {
        // @Override
        // protected Object doInBackground() throws Exception {
        updateNodeStyles();

        log.info( "filter" );
        filter( false );

        goTree.revalidate();
        goTree.repaint();
        messenger.clear();
        // return null;
        // }
        // };
        // w.execute();
    }

    /**
     * 
     */
    private void setRenderer() {
        rend = new GeneSetTreeNodeRenderer( geneData );
        this.goTree.setCellRenderer( rend );
    }

    /**
     * @param goData
     */
    private void setUpTree() {
        assert this.geneData != null;
        this.goTree = this.geneData.getGeneSetGraph().treeView( GeneSetTreeNode.class );
        geneSetTreeModel = goTree.getModel();
        filteredTreeModel = new FilteredGeneSetTreeModel( this.geneData, geneSetTreeModel );

        this.setRenderer();
        this.goTree.setRootVisible( true );

        // effectively disable the double-click
        this.goTree.setToggleClickCount( 10 );

        ToolTipManager.sharedInstance().registerComponent( goTree );
        MouseListener popupListener = super.configurePopupListener();

        this.goTree.addMouseListener( popupListener );
        this.goTree.addMouseListener( new GeneSetTreePanel_mouseListener( this ) );
        this.goTree.addTreeSelectionListener( new TreeSelectionListener() {
            public void valueChanged( TreeSelectionEvent e ) {
                currentlySelectedTreePath = e.getPath();
                GeneSetTreeNode currentNode = ( GeneSetTreeNode ) currentlySelectedTreePath.getLastPathComponent();
                currentlySelectedGeneSet = currentNode.getTerm();

            }
        } );

    }

    /**
     * 
     */
    private void updateNodeStyles() {

        // StopWatch timer = new StopWatch();
        // timer.start();
        setNodeStatesForFilter();
        // log.info( "Visit nodes: " + timer.getTime() );

        // try {
        // visitAllNodes( goTree, new Method[] {
        // this.getClass().getMethod( "hasSignificantChild", new Class[] { GeneSetTreeNode.class } ),
        // this.getClass().getMethod( "hasSelectedChild", new Class[] { GeneSetTreeNode.class } ) } );
        //
        // } catch ( Exception e ) {
        // log.error( e, e );
        // }

    }

    // /**
    // * http://javaalmanac.com/egs/javax.swing.tree/GetNodes.html
    // *
    // * @param tree
    // */
    // private void visitAllNodes( JTree tree, Method[] methods ) {
    // GeneSetTreeNode root = ( GeneSetTreeNode ) tree.getModel().getRoot();
    // visitAllNodes( root, methods );
    // }
    //
    // /**
    // * http://javaalmanac.com/egs/javax.swing.tree/GetNodes.html
    // *
    // * @param node
    // * @param process
    // */
    // @SuppressWarnings("unchecked")
    // private void visitAllNodes( GeneSetTreeNode node, Method[] methods ) {
    //
    // if ( methods.length != 0 ) {
    // try {
    // for ( int i = 0; i < methods.length; i++ ) {
    // methods[i].invoke( this, new Object[] { node } );
    // }
    // } catch ( Exception e ) {
    // log.error( e, e );
    // }
    // }
    //
    // if ( node.getChildCount() >= 0 ) {
    // for ( Enumeration<GeneSetTreeNode> e = node.children(); e.hasMoreElements(); ) {
    // GeneSetTreeNode n = e.nextElement();
    // visitAllNodes( n, methods );
    // }
    // }
    // }

    @Override
    protected boolean deleteUserGeneSet( GeneSetTerm classID ) {
        boolean deleted = super.deleteUserGeneSet( classID );
        // if ( deleted ) this.removedGeneSet( classID );
        return deleted;
    }

    /**
     * @param expand If false, collapses all nodes. If true, expands them all.
     */
    protected void expandAll( final boolean expand ) {
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();
        expandNode( new TreePath( root ), expand );
    }

    protected void expandNode( final TreePath parent, final boolean expand ) {
        /*
         * For big parts of the tree, it is actually faster to do this in the EDT, unless there is a trick.
         */
        // SwingWorker<Object, Object> w = new SwingWorker<Object, Object>() {

        // @Override
        // protected Object doInBackground() throws Exception {
        doExpandNode( parent, expand );
        // return null;
        // }
        //
        // @Override
        // protected void done() {
        // super.done();
        // goTree.repaint();
        // }
        //
        // };
        // w.execute();
    }

    /**
     * @param parent
     * @param expand if true expand, otherwise collapse.
     */
    private void doExpandNode( TreePath parent, boolean expand ) {
        // http://www.exampledepot.com/egs/javax.swing.tree/ExpandAll.html
        GeneSetTreeNode node = ( GeneSetTreeNode ) parent.getLastPathComponent();

        TreeModel treeModel = this.goTree.getModel();

        for ( int i = 0; i < treeModel.getChildCount( node ); i++ ) {
            TreeNode n = ( GeneSetTreeNode ) treeModel.getChild( node, i );
            TreePath path = parent.pathByAddingChild( n );
            doExpandNode( path, expand );
        }

        // expand from leaves up.
        if ( expand ) {
            goTree.expandPath( parent );
            goTree.scrollPathToVisible( parent );
        } else {
            if ( parent.getParentPath() == null ) return;
            goTree.collapsePath( parent );
        }
    }

    @Override
    public void filter( boolean propagate ) {

        filteredTreeModel = new FilteredGeneSetTreeModel( this.geneData, geneSetTreeModel );
        this.goTree.setModel( filteredTreeModel );

        // Enumeration<GeneSetTreeNode> e = ( ( GeneSetTreeNode ) this.goTree.getModel().getRoot() )
        // .breadthFirstEnumeration();
        // int i = 0;
        // while ( e.hasMoreElements() ) {
        // GeneSetTreeNode nextElement = e.nextElement();
        // if ( nextElement.hasSelectedChild() ) {
        // log.info( "yah " + nextElement );
        // // assert this.currentSelectedSets.contains( nextElement.getTerm() );
        // }
        // i++;
        // }

        filteredTreeModel.setFilterBySize( hideEmpty );
        filteredTreeModel.setResults( getCurrentResultSet() );
        filteredTreeModel.setFilterBySignificance( hideInsignificant );
        filteredTreeModel.setFilterSelectedTerms( this.currentSelectedSets );

        if ( propagate ) this.callingFrame.getTablePanel().filter( false );
    }

    /**
     * @param e
     * @return
     */
    @Override
    protected GeneSetTerm popupRespondAndGetGeneSet( MouseEvent e ) {
        JTree source = ( JTree ) e.getSource();
        int x = e.getX();
        int y = e.getY();
        TreePath path = source.getPathForLocation( x, y );

        if ( path == null ) return null;

        source.setSelectionPath( path );
        source.scrollPathToVisible( path );

        GeneSetTreeNode selectedNode = ( GeneSetTreeNode ) path.getLastPathComponent();
        return selectedNode.getTerm();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.erminej.gui.GeneSetPanel#removedGeneSet(ubic.erminej.data.GeneSetTerm)
     */
    @Override
    protected void removedGeneSet( GeneSetTerm id ) {
        assert id.isUserDefined();

        // find it in the tree
        TreePath p = find( id );

        if ( p == null ) {
            log.warn( "Path to set wasn't found. Already gone?" );
            return;
        }

        GeneSetTreeNode node = ( GeneSetTreeNode ) p.getLastPathComponent();

        if ( node == null ) {
            log.warn( "Node to delete wasn't found" );
            return;
        }

        if ( node.getChildCount() != 0 ) {
            throw new UnsupportedOperationException( "Can't delete node that has children, sorry" );
        }

        // remove it from the model.
        ( ( DefaultTreeModel ) this.goTree.getModel() ).removeNodeFromParent( node );

    }

    /**
     * 
     */
    @Override
    protected void showPopupMenu( MouseEvent e ) {

        GeneSetPanelPopupMenu popup = super.configurePopup( e );

        if ( popup == null ) return;

        JMenuItem collapseNodeMenuItem = new JMenuItem( "Collapse this node" );
        collapseNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                expandNode( currentlySelectedTreePath, false );
            }
        } );

        JMenuItem expandNodeMenuItem = new JMenuItem( "Expand this node" );
        expandNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                expandNode( currentlySelectedTreePath, true );
            }
        } );

        popup.add( collapseNodeMenuItem );
        popup.add( expandNodeMenuItem );

        popup.show( e.getComponent(), e.getX(), e.getY() );
    }

}

/**
 * @author paul
 * @version $Id$
 */
class GeneSetTreeNodeRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = -1L;

    private static final String GOOD_CHILD_ICON = GeneSetPanel.RESOURCE_LOCATION + "littleDiamond.gif";

    private static final String REGULAR_ICON = GeneSetPanel.RESOURCE_LOCATION + "littleSquare.gif";

    private static final String GOODPVAL_ICON = GeneSetPanel.RESOURCE_LOCATION + "goldCircle.gif";

    private static final String GOODPVAL_GOODCHILD_ICON = GeneSetPanel.RESOURCE_LOCATION + "goldCirclePurpleDot.gif";

    private static final String EMPTYSET_ICON = GeneSetPanel.RESOURCE_LOCATION + "littleLighterGreySquare.gif";

    private GeneSetPvalRun currentResultSet = null;

    private GeneAnnotations geneData;

    /**
     * Represents a node that itself is not statistically significant, but has a child somewhere that is.
     */
    private final Icon goodChildIcon = new ImageIcon( this.getClass().getResource( GOOD_CHILD_ICON ) );

    /**
     * Represents node that is not statistically significant and neither are any of its children.
     */
    private final Icon regularIcon = new ImageIcon( this.getClass().getResource( REGULAR_ICON ) );

    /**
     * Represents a node that is statistically significant but which lacks any significant children.
     */
    private final Icon goodPvalueIcon = new ImageIcon( this.getClass().getResource( GOODPVAL_ICON ) );

    private final Icon emptySetIcon = new ImageIcon( this.getClass().getResource( EMPTYSET_ICON ) );

    /**
     * Represents a node that is statistically significant and also has significant children.
     */
    private final Icon goodPvalueGoodChildIcon = new ImageIcon( this.getClass().getResource( GOODPVAL_GOODCHILD_ICON ) );

    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score
    private Font plain = new Font( "SansSerif", Font.PLAIN, 11 );

    private boolean selected;

    private Collection<GeneSetTerm> selectedTerms = new HashSet<GeneSetTerm>();

    public GeneSetTreeNodeRenderer( GeneAnnotations geneData ) {
        super();
        this.geneData = geneData;
        nff.setMaximumFractionDigits( 4 );
        this.setOpenIcon( regularIcon );
        this.setLeafIcon( regularIcon );
        this.setClosedIcon( regularIcon );
    }

    public void setSelectedTerms( Collection<GeneSetTerm> selectedTerms ) {
        this.selectedTerms = selectedTerms;
    }

    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean,
     *      boolean, boolean, int, boolean)
     */
    @Override
    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean s, boolean expanded, boolean leaf,
            int row, boolean f ) {
        super.getTreeCellRendererComponent( tree, value, s, expanded, leaf, row, hasFocus );

        this.selected = s;
        this.hasFocus = f;
        setOpaque( true );

        GeneSetTreeNode node = ( GeneSetTreeNode ) value;

        GeneSetTerm id = node.getTerm();

        setupToolTip( id );

        String displayedText = addGeneSetSizeInformation( id, node );

        this.setBackground( Color.WHITE );
        if ( currentResultSet != null ) {
            displayedText = addResultsFlags( node, id, displayedText );
        }
        this.setText( displayedText );

        if ( id.isUserDefined() ) {
            this.setBackground( GeneSetPanel.USER_NODE_COLOR );
        }

        if ( this.selected ) {
            this.setBackground( Color.LIGHT_GRAY );
        }

        if ( !this.selectedTerms.isEmpty() && !selectedTerms.contains( id ) ) {
            this.setForeground( Color.LIGHT_GRAY ); // leaves should also be hidden.
        }

        return this;
    }

    /**
     * @param geneSetPvalRun
     */
    public void setCurrentResultSet( GeneSetPvalRun geneSetPvalRun ) {
        this.currentResultSet = geneSetPvalRun;
        this.validate();
        this.repaint();
    }

    /**
     * @param name
     * @param id
     * @param node
     * @return
     */
    private String addGeneSetSizeInformation( GeneSetTerm id, GeneSetTreeNode node ) {

        boolean redund = geneData.hasRedundancy( id );

        String textToDisplay = "<html>" + id.getName() + " [ " + id.getId() + ( redund ? "&nbsp;&bull;" : "" ) + " ]";

        this.setFont( this.getFont().deriveFont( Font.ITALIC ) );
        this.setForeground( Color.GRAY );
        int numGenesInGeneSet = geneData.numGenesInGeneSet( id );

        if ( id.isAspect() || id.getId().equals( "all" ) ) {
            this.setIcon( emptySetIcon );
            this.setFont( this.getFont().deriveFont( Font.BOLD ) );
            this.setForeground( Color.DARK_GRAY );
        } else if ( !geneData.getNonEmptyGeneSets().contains( id ) || numGenesInGeneSet == 0 ) {
            this.setIcon( emptySetIcon );
            textToDisplay += " (No genes in your data)";
        } else {
            textToDisplay += " &mdash; " + numGenesInGeneSet + " genes, multifunc. "
                    + String.format( "%.2f", this.geneData.getMultifunctionality().getGOTermMultifunctionality( id ) );
            this.setFont( plain );
            this.setIcon( regularIcon );
            this.setForeground( Color.BLACK );
        }
        if ( id.isUserDefined() ) {
            this.setForeground( GeneSetPanel.USER_NODE_TEXT_COLOR );
        }
        return textToDisplay + "</html>";
    }

    /**
     * @param node
     * @param id
     * @param displayedText
     * @return
     */
    private String addResultsFlags( GeneSetTreeNode node, GeneSetTerm id, String displayedText ) {

        assert currentResultSet != null;
        assert currentResultSet.getResults() != null;

        GeneSetResult result = currentResultSet.getResults().get( id );

        if ( result != null ) {
            double pvalue = result.getPvalue();
            displayedText = displayedText + " -- p = " + String.format( "%.3g", pvalue );
            double pvalCorr = result.getCorrectedPvalue();
            Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );
            this.setBackground( bgColor );

            if ( pvalCorr < GeneSetPanel.FDR_THRESHOLD_FOR_FILTER ) {
                if ( node.hasSignificantChild() ) {
                    this.setIcon( goodPvalueGoodChildIcon );
                } else {
                    this.setIcon( goodPvalueIcon );
                }
            } else if ( node.hasSignificantChild() ) {
                this.setIcon( goodChildIcon );
            } else {
                this.setIcon( regularIcon );
            }

        } else {
            this.setBackground( Color.WHITE );

            if ( node.hasSignificantChild() ) {
                this.setIcon( goodChildIcon );
            } else {
                this.setIcon( regularIcon );
            }
        }
        return displayedText;
    }

    /**
     * @param id
     */
    private void setupToolTip( GeneSetTerm id ) {
        String aspect = id.getAspect();
        String definition = id.getDefinition();
        String size = geneData.getGeneSet( id ) == null ? "?" : "" + geneData.getGeneSet( id ).size();
        String probeSize = geneData.getGeneSet( id ) == null ? "?" : "" + geneData.getGeneSet( id ).getProbes().size();
        String redund = getToolTipTextForRedundancy( id );

        /*
         * FIXME add information on the result.
         */

        setToolTipText( "<html>"
                + id.getName()
                + " ("
                + id.getId()
                + ")<br/>"
                + "Aspect: "
                + aspect
                + "<br/>"
                + size
                + " Genes, "
                + probeSize
                + " probes.<br />"
                + redund
                + StringUtil.wrap( StringUtils.abbreviate( definition, GeneSetPanel.MAX_DEFINITION_LENGTH ), 50,
                        "<br/>" ) );
    }

    /**
     * @param id
     * @return
     */
    protected String getToolTipTextForRedundancy( GeneSetTerm id ) {

        if ( id.isAspect() || id.getId().equals( "all" ) ) return "";

        // boolean redundant = geneData.skipDueToRedundancy( id );
        GeneSet qGeneSet = geneData.getGeneSet( id );

        if ( qGeneSet == null ) {
            System.err.println( "No set " + id );
            return "";
        }

        Collection<GeneSet> redundantGroups = qGeneSet.getRedundantGroups();

        String redund = "";
        if ( !redundantGroups.isEmpty() ) {
            redund = "<strong>Redundant</strong> with:<br/>";

            for ( GeneSet geneSet : redundantGroups ) {
                redund += geneSet + "<br/>";
            }
            redund += "<br/>";
        }
        return redund;
    }
}

class GeneSetTreePanel_mouseListener extends MouseAdapter {
    private GeneSetTreePanel adaptee;

    GeneSetTreePanel_mouseListener( GeneSetTreePanel adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void mousePressed( MouseEvent e ) {

    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        adaptee.mouseReleased( e );
    }

}