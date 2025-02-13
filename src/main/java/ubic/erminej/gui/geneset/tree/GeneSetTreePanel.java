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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.erminej.Settings;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.EmptyGeneSetResult;
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
 * @version $Id: $Id
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

    private Collection<GeneSetTerm> currentSelectedSets = new HashSet<>();

    private AtomicBoolean expansionCancel = new AtomicBoolean( false );

    /**
     * <p>Constructor for GeneSetTreePanel.</p>
     *
     * @param callingFrame a {@link ubic.erminej.gui.MainFrame} object.
     * @param settings a {@link ubic.erminej.Settings} object.
     */
    public GeneSetTreePanel( MainFrame callingFrame, Settings settings ) {
        super( settings, callingFrame );
    }

    /*
     * (non-Javadoc)
     *
     * @see classScore.gui.GeneSetsResultsScrollPane#addedNewGeneSet()
     */
    /** {@inheritDoc} */
    @Override
    public void addedGeneSet( GeneSetTerm id ) {
        /*
         * It's already in the graph, we just need to rebuild the node?
         */

        goTree.revalidate();

        TreePath existing = find( id );

        if ( existing != null ) {
            // no problem, we are just editing it.
            log.debug( "Node was already there: " + id );
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
    /** {@inheritDoc} */
    @Override
    public void addRun() {
        fireResultsChanged();
    }

    /**
     * <p>expandToGeneSet.</p>
     *
     * @param classID a node somewhere in the tree; we expand the path
     * @return a boolean.
     */
    public boolean expandToGeneSet( GeneSetTerm classID ) {

        if ( classID == null ) return false;

        final TreePath path = this.find( classID );
        boolean foundIt = false;
        if ( path == null ) {
            this.mainFrame.getStatusMessenger().showWarning(
                    "Could not find " + classID + " in any aspect, the term may be obsolete." );
            foundIt = false;
        } else {
            // this.callingFrame.getStatusMessenger().showStatus( "Showing " + classID );
            foundIt = true;
        }
        if ( foundIt ) {
            log.debug( "Expanding to path for " + classID );

            SwingWorker<Object, Object> w = new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() {
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

    /*
     * (non-Javadoc)
     *
     * @see ubic.erminej.gui.geneset.GeneSetPanel#filter(boolean)
     */
    /** {@inheritDoc} */
    @Override
    public void filter( final boolean propagate ) {
        SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                filteredTreeModel = new FilteredGeneSetTreeModel( geneData, geneSetTreeModel );
                filteredTreeModel.setFilterBySize( mainFrame.getHideEmpty() );
                filteredTreeModel.setResults( mainFrame.getCurrentResultSet() );
                filteredTreeModel.setFilterBySignificance( mainFrame.getHideNonSignificant() );
                filteredTreeModel.setFilterSelectedTerms( currentSelectedSets );
                goTree.setModel( filteredTreeModel );

                // should probably do away with this and do it by events.
                if ( propagate ) mainFrame.getTablePanel().filter( false );

                return null;
            }
        };
        r.execute();

        this.mainFrame.getStatusMessenger().showProgress( "Updating" );
        try {
            r.get();
        } catch ( Exception e ) {
            log.warn( e, e );
        }
        this.mainFrame.getStatusMessenger().clear();

    }

    /**
     * <p>filter.</p>
     *
     * @param selectedTerms a {@link java.util.Collection} object.
     */
    public void filter( Collection<GeneSetTerm> selectedTerms ) {

        this.currentSelectedSets = selectedTerms;
        this.rend.setSelectedTerms( selectedTerms );
        log.debug( selectedTerms.size() + " selected" );
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
                    if ( ++i > 0 ) break;
                }
            }
        }
    }

    /**
     * <p>find.</p>
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a {@link javax.swing.tree.TreePath} object.
     */
    public TreePath find( GeneSetTerm id ) {
        if ( id == null ) return null;
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();
        return find( new TreePath( root ), id, 0 );
    }

    /**
     * <p>fireResultsChanged.</p>
     */
    public void fireResultsChanged() {
        log.debug( "Changing results" );
        if ( mainFrame != null && this.goTree != null ) {

            ( ( GeneSetTreeNodeRenderer ) this.goTree.getCellRenderer() ).setCurrentResultSet( mainFrame
                    .getCurrentResultSet() );
        }
        refreshView();
    }

    /**
     * <p>Getter for the field <code>currentlySelectedGeneSet</code>.</p>
     *
     * @return Returns the currentlySelectedGeneSet.
     */
    public GeneSetTerm getCurrentlySelectedGeneSet() {
        return this.currentlySelectedGeneSet;
    }

    /**
     * <p>getLeaves.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<GeneSetTreeNode> getLeaves() {
        GeneSetTreeNode node = ( GeneSetTreeNode ) goTree.getModel().getRoot();
        return getLeaves( node );
    }

    /**
     * Called after data files are read in.
     *
     * @param gd a {@link ubic.erminej.data.GeneAnnotations} object.
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
     * <p>mouseReleased.</p>
     *
     * @param e a {@link java.awt.event.MouseEvent} object.
     */
    public void mouseReleased( MouseEvent e ) {
        if ( e.getClickCount() < 2 ) {
            return;
        }
        showDetailsForGeneSet( currentlySelectedGeneSet, mainFrame.getCurrentResultSet() );
    }

    /**
     * {@inheritDoc}
     *
     * Force to repaint, reapply filters.
     */
    @Override
    public void refreshView() {
        setNodeStatesForFilter();
        log.debug( "filter" );
        filter( false );

        goTree.revalidate();
        goTree.repaint();
        messenger.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void removeRun( GeneSetPvalRun runToRemove ) {
        ( ( FilteredGeneSetTreeModel ) this.goTree.getModel() ).removeResults();
        ( ( GeneSetTreeNodeRenderer ) this.goTree.getCellRenderer() ).clearRun();
    }

    /** {@inheritDoc} */
    @Override
    public void resetView() {
        // anyting else we need to do?
        // filter( new HashSet<GeneSetTerm>() );
    }

    /** {@inheritDoc} */
    @Override
    protected boolean deleteUserGeneSet( GeneSetTerm classID ) {
        boolean deleted = super.deleteUserGeneSet( classID );
        return deleted;
    }

    /**
     * <p>expandAll.</p>
     *
     * @param expand If false, collapses all nodes. If true, expands them all.
     */
    protected void expandAll( final boolean expand ) {
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();
        expandNode( new TreePath( root ), expand );
    }

    /**
     * Fully expand or collapse a node.
     *
     * @param parent a {@link javax.swing.tree.TreePath} object.
     * @param expand a boolean.
     */
    protected synchronized void expandNode( final TreePath parent, final boolean expand ) {

        if ( expand )
            messenger.showProgress( "Expanding" );
        else
            messenger.showProgress( "Collapsing" );

        doExpandNode( parent, expand );

        messenger.clear();

    }

    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    protected void showPopupMenu( MouseEvent e ) {

        GeneSetPanelPopupMenu popup = super.configurePopup( e );

        if ( popup == null ) return;

        JMenuItem collapseNodeMenuItem = new JMenuItem( "Fully collapse this node" );
        collapseNodeMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e1 ) {
                expandNode( currentlySelectedTreePath, false );
            }
        } );

        JMenuItem expandNodeMenuItem = new JMenuItem( "Fully expand this node" );
        expandNodeMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e1 ) {
                expandNode( currentlySelectedTreePath, true );
            }
        } );

        popup.add( collapseNodeMenuItem );
        popup.add( expandNodeMenuItem );

        popup.show( e.getComponent(), e.getX(), e.getY() );
    }

    /**
     * @param parent
     * @param expand if true expand, otherwise collapse.
     */
    private void doExpandNode( TreePath parent, boolean expand ) {
        if ( expansionCancel.get() ) {
            return;
        }

        // http://www.exampledepot.com/egs/javax.swing.tree/ExpandAll.html
        GeneSetTreeNode node = ( GeneSetTreeNode ) parent.getLastPathComponent();

        TreeModel treeModel = this.goTree.getModel();

        for ( int i = 0; i < treeModel.getChildCount( node ); i++ ) {
            TreeNode n = ( GeneSetTreeNode ) treeModel.getChild( node, i );
            TreePath path = parent.pathByAddingChild( n );

            doExpandNode( path, expand );
        }

        // expand from leaves up.
        if ( !expansionCancel.get() ) {
            if ( expand ) {
                goTree.expandPath( parent );
            } else {
                if ( parent.getParentPath() == null ) return;
                goTree.collapsePath( parent );
            }
        }

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

    private Collection<GeneSetTerm> getCurrentSelectedSets() {
        return this.currentSelectedSets;
    }

    /**
     * @param node
     * @return
     */
    private Collection<GeneSetTreeNode> getLeaves( GeneSetTreeNode node ) {
        Collection<GeneSetTreeNode> result = new HashSet<>();
        Enumeration<GeneSetTreeNode> dfe = node.depthFirstEnumeration();
        while ( dfe.hasMoreElements() ) {
            GeneSetTreeNode no = dfe.nextElement();
            if ( no.isLeaf() ) {
                result.add( no );
            }
        }

        return result;
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
     * @param node
     * @param set
     */
    private void markHasSelectedChild( GeneSetTreeNode node, boolean set ) {

        if ( node == null ) return;

        if ( set ) {
            // we already found a path, so all parents are true, we don't need to check.
            assert !node.isLeaf();
            node.setHasSelectedChild( true );

            // continue up the tree.
            markHasSelectedChild( ( GeneSetTreeNode ) node.getParent(), true );
            return;
        }

        // check the node.
        boolean isSel = this.getCurrentSelectedSets().contains( node.getTerm() );
        markHasSelectedChild( ( GeneSetTreeNode ) node.getParent(), isSel );

    }

    /**
     * @param node
     * @param set
     */
    private void markHasSignificantChild( GeneSetTreeNode node, boolean set ) {

        if ( node == null ) return;

        if ( set ) {
            assert !node.isLeaf();
            // we already found a path, so all are true.
            node.setHasSignificantChild( true );

            // continue up the tree
            markHasSignificantChild( ( GeneSetTreeNode ) node.getParent(), true );
            return;
        }

        // check the node.
        GeneSetResult result = mainFrame.getCurrentResultSet().getResults().get( node.getTerm() );
        boolean isSig = result != null && result.getCorrectedPvalue() < GeneSetPanel.FDR_THRESHOLD_FOR_FILTER;
        markHasSignificantChild( ( GeneSetTreeNode ) node.getParent(), isSig );
    }

    /**
     *
     */
    private void setNodeStatesForFilter() {
        Collection<GeneSetTreeNode> leaves = this.getLeaves();

        /*
         * Reset all nodes
         */
        Enumeration<GeneSetTreeNode> e = ( ( GeneSetTreeNode ) this.goTree.getModel().getRoot() )
                .breadthFirstEnumeration();
        while ( e.hasMoreElements() ) {
            GeneSetTreeNode n = e.nextElement();
            n.setHasSelectedChild( false );
            n.setHasSignificantChild( false );
        }

        for ( GeneSetTreeNode le : leaves ) {

            assert le.isLeaf();

            le.setHasSelectedChild( false );
            le.setHasSignificantChild( false );

            if ( mainFrame.getCurrentResultSet() != null ) {
                markHasSignificantChild( le, false );
            }
            markHasSelectedChild( le, false );
        }
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
            @Override
            public void valueChanged( TreeSelectionEvent e ) {
                if ( e == null ) return;
                currentlySelectedTreePath = e.getPath();
                GeneSetTreeNode currentNode = ( GeneSetTreeNode ) currentlySelectedTreePath.getLastPathComponent();
                currentlySelectedGeneSet = currentNode.getTerm();

            }
        } );

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

    private static final Font plain = new Font( "SansSerif", Font.PLAIN, 11 );

    private static final Font italicFont = new Font( "SansSerif", Font.ITALIC, 11 );

    private static final Font boldFont = new Font( "SansSerif", Font.BOLD, 11 );

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

    private Collection<GeneSetTerm> selectedTerms = new HashSet<>();

    /**
     * <p>Constructor for GeneSetTreeNodeRenderer.</p>
     *
     * @param geneData a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public GeneSetTreeNodeRenderer( GeneAnnotations geneData ) {
        super();
        this.setOpaque( true );
        this.geneData = geneData;
    }

    /**
     * <p>clearRun.</p>
     */
    public void clearRun() {
        this.currentResultSet = null;
        this.validate();
        this.repaint();
    }

    /** {@inheritDoc} */
    @Override
    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean foc ) {
        super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );

        GeneSetTreeNode node = ( GeneSetTreeNode ) value;

        if ( node.getTerm().isUserDefined() ) {
            this.setBackground( GeneSetPanel.USER_NODE_COLOR );
        } else {
            this.setBackground( Color.WHITE );
        }

        setupToolTip( node );

        String displayedText = addGeneSetSizeInformation( node );

        if ( currentResultSet != null ) {
            displayedText = addResultsFlags( node, displayedText );
        }
        this.setText( "<html>" + displayedText + "</html>" );

        if ( !this.selectedTerms.isEmpty() && !selectedTerms.contains( node.getTerm() ) ) {
            this.setForeground( Color.LIGHT_GRAY ); // leaves should also be hidden.
        }

        if ( sel || foc ) {

            float[] col1comps = new float[3];
            Color.decode( "#6688FF" ).getColorComponents( col1comps );
            float[] col2comps = new float[3];
            getBackground().getColorComponents( col2comps );

            float r = 0.2f;
            setBackground( new Color( col1comps[0] * r + col2comps[0] * ( 1.0f - r ), col1comps[1] * r + col2comps[1]
                    * ( 1.0f - r ), col1comps[2] * r + col2comps[2] * ( 1.0f - r ) ) );
        }

        return this;
    }

    /**
     * <p>Setter for the field <code>currentResultSet</code>.</p>
     *
     * @param geneSetPvalRun a {@link ubic.erminej.analysis.GeneSetPvalRun} object.
     */
    public void setCurrentResultSet( GeneSetPvalRun geneSetPvalRun ) {
        this.currentResultSet = geneSetPvalRun;
        this.validate();
        this.repaint();
    }

    /**
     * <p>Setter for the field <code>selectedTerms</code>.</p>
     *
     * @param selectedTerms a {@link java.util.Collection} object.
     */
    public void setSelectedTerms( Collection<GeneSetTerm> selectedTerms ) {
        this.selectedTerms = selectedTerms;
    }

    /**
     * <p>getToolTipTextForRedundancy.</p>
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a {@link java.lang.String} object.
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
            // redund += "<br/>";
        }
        return redund;
    }

    /**
     * @param node
     * @return
     */
    private String addGeneSetSizeInformation( GeneSetTreeNode node ) {

        GeneSetTerm id = node.getTerm();
        boolean redund = geneData.hasRedundancy( id );
        StringBuilder buf = new StringBuilder();
        buf.append( id.getName() + " [ " + id.getId() + ( redund ? "&nbsp;&bull;" : "" ) + " ]" );

        this.setFont( italicFont );
        this.setForeground( Color.GRAY );
        int numGenesInGeneSet = geneData.numGenesInGeneSet( id );

        if ( id.isAspect() || id.getId().equals( "all" ) ) {
            this.setIcon( emptySetIcon );
            this.setFont( boldFont );
            this.setForeground( Color.DARK_GRAY );
        } else if ( numGenesInGeneSet == 0 ) {
            this.setIcon( emptySetIcon );
            buf.append( " (No genes in your data)" );
        } else {
            buf.append( " &mdash; "
                    + numGenesInGeneSet
                    + " genes, multifunc rank="
                    + String.format( "%.2f", this.geneData.getMultifunctionality().getGOTermMultifunctionalityRank( id ) ) );
            this.setFont( plain );
            this.setIcon( regularIcon );
            this.setForeground( Color.BLACK );
        }

        return buf.toString();
    }

    /**
     * @param node
     * @param id
     * @param displayedText
     * @return
     */
    private String addResultsFlags( GeneSetTreeNode node, String displayedText ) {

        assert currentResultSet != null;
        assert currentResultSet.getResults() != null;

        GeneSetResult result = currentResultSet.getResults().get( node.getTerm() );
        String resultString = displayedText;
        this.setBorder( null );
        boolean isSig = false;
        if ( result != null ) {
            double pvalue = result.getPvalue();
            double pvalCorr = result.getCorrectedPvalue();
            Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );

            this.setBackground( bgColor );

            isSig = pvalCorr < GeneSetPanel.FDR_THRESHOLD_FOR_FILTER;

            String mfstring = "";
            int size = 4;

            if ( result.getMultifunctionalityCorrectedRankDelta() != null ) {
                String col = Integer.toHexString( Colors.chooseColorForMultifunctionalityEffect( result ).getRGB() )
                        .substring( 2, 8 );

                mfstring = "&nbsp;<font size=" + size + " + color=#" + col + ">&#9830;</font>";
            }

            resultString = resultString + String.format( " &mdash; p = %.3g", pvalue ) + mfstring;

        }

        if ( node.hasSignificantChild() ) {
            assert !node.isLeaf();
            if ( isSig ) {
                this.setIcon( goodPvalueGoodChildIcon );
            } else {
                this.setIcon( goodChildIcon );
            }

        } else {
            if ( isSig ) {
                this.setIcon( goodPvalueIcon );
            } else {
                this.setIcon( regularIcon );
            }
        }

        return resultString;

    }

    /**
     * @param node
     */
    private void setupToolTip( GeneSetTreeNode node ) {

        GeneSetTerm term = node.getTerm();

        String aspect = term.getAspect();
        String definition = term.getDefinition();
        String size = geneData.getGeneSet( term ) == null ? "?" : "" + geneData.getGeneSet( term ).size();
        String probeSize = geneData.getGeneSet( term ) == null ? "?" : ""
                + geneData.getGeneSet( term ).getProbes().size();
        String redund = getToolTipTextForRedundancy( term );

        DirectedGraphNode<String, GeneSetTerm> termNode = geneData.getGeneSetGraph().get( term.getId() );

        boolean showParents = false;

        String parentTerms = "";

        if ( termNode != null ) {
            GeneSetTreeNode jtreeParent = ( ( GeneSetTreeNode ) node.getParent() );

            if ( jtreeParent != null ) {

                Set<String> parentKeys = termNode.getParentKeys();

                if ( parentKeys.size() > 1 ) {
                    showParents = true;
                    for ( String p : parentKeys ) {
                        if ( p.equals( jtreeParent.getTerm().getId() ) ) {
                            continue;
                        }
                        DirectedGraphNode<String, GeneSetTerm> parentNode = termNode.getGraph().get( p );

                        parentTerms = parentTerms + "<br/>" + parentNode.getItem();
                    }
                }
                parentTerms = "<strong>Other parents:</strong> " + parentTerms + "<br/>";
            }
        }

        /*
    private void setupToolTip( GeneSetTreeNode node ) {

        GeneSetTerm term = node.getTerm();

        String aspect = term.getAspect();
        String definition = term.getDefinition();
        String size = geneData.getGeneSet( term ) == null ? "?" : "" + geneData.getGeneSet( term ).size();
        String probeSize = geneData.getGeneSet( term ) == null ? "?" : ""
                + geneData.getGeneSet( term ).getProbes().size();
        String redund = getToolTipTextForRedundancy( term );

        DirectedGraphNode<String, GeneSetTerm> termNode = geneData.getGeneSetGraph().get( term.getId() );

        boolean showParents = false;

        String parentTerms = "";

        if ( termNode != null ) {
            GeneSetTreeNode jtreeParent = ( ( GeneSetTreeNode ) node.getParent() );

            if ( jtreeParent != null ) {

                Set<String> parentKeys = termNode.getParentKeys();

                if ( parentKeys.size() > 1 ) {
                    showParents = true;
                    for ( String p : parentKeys ) {
                        if ( p.equals( jtreeParent.getTerm().getId() ) ) {
                            continue;
                        }
                        DirectedGraphNode<String, GeneSetTerm> parentNode = termNode.getGraph().get( p );

                        parentTerms = parentTerms + "<br/>" + parentNode.getItem();
                    }
                }
                parentTerms = "<strong>Other parents:</strong> " + parentTerms + "<br/>";
            }
        }

        /*
         * add information on the result.
         */
        boolean showResult = false;
        String resultStr = "";
        if ( this.currentResultSet != null ) {
            GeneSetResult result = this.currentResultSet.getResults().get( term );
            if ( result != null && !( result instanceof EmptyGeneSetResult ) ) {
                /** {@inheritDoc} */
                showResult = true;
                resultStr = String.format( "Corrected P = %.2g<br/>", result.getCorrectedPvalue() );
            }

        }
/** {@inheritDoc} */

        setToolTipText( "<html>"
                + term.getName()
                + " ("
                + term.getId()
                + ")<br/>"
                + ( showResult ? resultStr : "" )
                + "Aspect: "
                + aspect
                + "<br/>"
                + size
                + " Genes, "
                + probeSize
                + " elements.<br />"
                + redund
                + ( showParents ? parentTerms : "" )
                + "<br/>"
                + WordUtils.wrap( StringUtils.abbreviate( definition, GeneSetPanel.MAX_DEFINITION_LENGTH ), 50,
                        "<br/>", true ) );
    }
}

/**
 *
  *
  */
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
