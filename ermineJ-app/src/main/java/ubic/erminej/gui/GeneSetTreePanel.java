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
package ubic.erminej.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ubic.basecode.dataStructure.graph.DirectedGraph;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.basecode.util.StringUtil;
import ubic.erminej.GeneSetPvalRun;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSetTerms;

/**
 * A Tree display that shows Gene Sets and their scores, and allows uer interaction.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreePanel extends GeneSetPanel {

    private static final long serialVersionUID = 0L;
    protected static final double FDR_THRESHOLD_FOR_TREE = 0.1;
    private JTree goTree = null;
    protected GeneSetTerm currentlySelectedGeneSet = null;

    private GeneSetTreeNodeRenderer rend;

    private TreeModel geneSetTreeModel;

    private FilteredGeneSetTreeModel filteredTreeModel;

    private TreePath currentlySelectedTreePath = null;

    private boolean hideEmpty = true;
    private boolean hideInsignificant = false;
    private boolean hideRedundant = true;

    public GeneSetTreePanel( GeneSetScoreFrame callingFrame, List<GeneSetPvalRun> results, Settings settings ) {
        super( settings, results, callingFrame );
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addedNewGeneSet()
     */
    @Override
    public void addedNewGeneSet() {
        log.debug( "no-op" );
    }

    public void addNode( GeneSetTerm id ) {

        /* already exists, don't add it */
        if ( id.isUserDefined() ) { // FIXME
            return;
        }

        GeneSetTreeNode userNode = getUserNode();
        if ( userNode != null ) {
            DirectedGraphNode<String, GeneSetTerm> newNode = new DirectedGraphNode<String, GeneSetTerm>( id.getId(),
                    id, goData.getGraph() );
            GeneSetTreeNode newTreeNode = new GeneSetTreeNode( newNode );

            ( ( DefaultTreeModel ) this.goTree.getModel() ).insertNodeInto( newTreeNode, userNode, userNode
                    .getChildCount() );
            goTree.revalidate();
        }
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

        TreePath path = this.findByGeneSetId( classID );
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
            goTree.expandPath( path );
            goTree.setSelectionPath( path );
            goTree.scrollPathToVisible( path );
        }
        return foundIt;
    }

    /**
     * @param hideEmpty
     * @param hideInsignificant
     * @param hideRedundant
     */
    public void filter( boolean he, boolean hi, boolean hr ) {

        this.hideEmpty = he;
        this.hideInsignificant = hi;
        this.hideRedundant = hr;

        filteredTreeModel = new FilteredGeneSetTreeModel( this.geneData, geneSetTreeModel );
        this.goTree.setModel( filteredTreeModel );

        filteredTreeModel.setFilterBySize( hideEmpty );
        filteredTreeModel.setFilterByRedundancy( hideRedundant );
        filteredTreeModel.setResults( getCurrentResultSet() );
        filteredTreeModel.setFilterBySignificance( hideInsignificant );

        refreshView();
    }

    /**
     * @param selectedTerms
     */
    public void filter( Collection<GeneSetTerm> selectedTerms ) {
        filteredTreeModel.setFilterSelectedTerms( selectedTerms );
        refreshView();
    }

    /**
     * @param tree
     * @param names
     * @return
     */
    public TreePath findByGeneSetId( GeneSetTerm id ) {
        this.hideEmpty = true;
        this.hideInsignificant = false;
        this.hideRedundant = false;
        filter( false, false, false ); // needed in case it's buried under a redundant or non-sig node. There may be
                                       // ways
        // around this, but it doesn't work right now unless you show everything
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
     * Called via reflection == has to be public.
     * 
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void hasSignificantChild( GeneSetTreeNode node ) {
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
            if ( result != null && result.getCorrectedPvalue() <= GeneSetTreePanel.FDR_THRESHOLD_FOR_TREE ) {
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
    public void initialize( GeneSetTerms go, GeneAnnotations gd ) {
        // assert goData != null : "Go data is still null";
        // assert geneData != null : "Gene data is still null";
        this.geneData = gd;
        this.goData = go;
        setUpTree( goData.getGraph() );
        filter( true, false, true );
        this.getViewport().add( goTree );
        this.goTree.setVisible( true );
        this.goTree.revalidate();
    }

    /**
     * @param e
     */
    public void mousePressed( MouseEvent e ) {

        // if ( e.getButton() == MouseEvent.BUTTON1 ) {
        // // left button
        // } else if ( e.getButton() == MouseEvent.BUTTON3 ) {
        // // right button
        //
        // }
    }

    /**
     * @param e
     */
    public void mouseReleased( MouseEvent e ) {
        if ( e.getClickCount() < 2 ) {
            return;
        }
        showDetailsForGeneSet( getCurrentResultSet(), currentlySelectedGeneSet );
    }

    /**
     * @param id
     */
    public void removeNode( GeneSetTerm id ) {
        assert id != null;
        log.debug( "Removing tree node " + id );
        TreePath path = this.findByGeneSetId( id );
        if ( path == null ) {
            log.debug( "No node for " + id );
            return;
        }
        GeneSetTreeNode node = ( GeneSetTreeNode ) path.getLastPathComponent();
        if ( node.getChildCount() != 0 ) {
            throw new UnsupportedOperationException( "Can't delete node that has children, sorry" );
        }
        ( ( DefaultTreeModel ) this.goTree.getModel() ).removeNodeFromParent( node );
    }

    public void removeUserDefinedNode( GeneSetTerm id ) {
        if ( !id.isUserDefined() ) return;
        GeneSetTreeNode node = ( GeneSetTreeNode ) find(
                this.findByGeneSetId( new GeneSetTerm( GeneSetTerms.USER_DEFINED ) ), id, 0 ).getLastPathComponent();
        ( ( DefaultTreeModel ) this.goTree.getModel() ).removeNodeFromParent( node );

    }

    @Override
    public void resetView() {
        // anyting else we need to do?
        filter( new HashSet<GeneSetTerm>() );
    }

    /**
     * Force to repaint etc.
     */
    private void refreshView() {
        updateNodeStyles();
        goTree.revalidate();
        goTree.repaint();
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

        DefaultTreeModel treeModel = this.goData.getTreeModel();
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
        TreePath path = this.findByGeneSetId( new GeneSetTerm( GeneSetTerms.USER_DEFINED ) );
        if ( path == null ) return null;
        return ( GeneSetTreeNode ) path.getLastPathComponent();
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
    private void setUpTree( DirectedGraph<String, GeneSetTerm> graph ) {

        this.goTree = graph.treeView( GeneSetTreeNode.class );
        geneSetTreeModel = goTree.getModel();
        filteredTreeModel = new FilteredGeneSetTreeModel( this.geneData, geneSetTreeModel );

        this.setRenderer();
        this.goTree.setRootVisible( true );

        // effectively disable the double-click
        this.goTree.setToggleClickCount( 10 );

        ToolTipManager.sharedInstance().registerComponent( goTree );
        MouseListener popupListener = configurePopupMenu();
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
        log.debug( "Updating nodes" );
        try {
            visitAllNodes( goTree, this.getClass().getMethod( "hasSignificantChild",
                    new Class[] { GeneSetTreeNode.class } ) );
        } catch ( Exception e ) {
            log.error( e, e );
        }
    }

    /**
     * http://javaalmanac.com/egs/javax.swing.tree/GetNodes.html
     * 
     * @param tree
     */
    private void visitAllNodes( JTree tree, Method process ) {
        TreeNode root = ( TreeNode ) tree.getModel().getRoot();
        visitAllNodes( root, process );
    }

    /**
     * http://javaalmanac.com/egs/javax.swing.tree/GetNodes.html
     * 
     * @param node
     * @param process
     */
    @SuppressWarnings("unchecked")
    private void visitAllNodes( TreeNode node, Method process ) {
        if ( process != null ) {
            try {
                process.invoke( this, new Object[] { node } );
            } catch ( Exception e ) {
                log.error( e, e );
            }
        }

        if ( node.getChildCount() >= 0 ) {
            for ( Enumeration<GeneSetTreeNode> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = e.nextElement();
                visitAllNodes( n, process );
            }
        }
    }

    /**
     * 
     */
    @Override
    protected MouseListener configurePopupMenu() {
        MouseListener m = super.configurePopupMenu();

        JMenuItem collapseNodeMenuItem = new JMenuItem( "Collapse this node" );
        collapseNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                GeneSetTerm classID = null;
                classID = sourcePopup.getSelectedItem();
                if ( classID == null ) return;
                expandNode( currentlySelectedTreePath, false );
            }
        } );

        JMenuItem expandNodeMenuItem = new JMenuItem( "Expand this node" );
        expandNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                GeneSetTerm classID = null;
                classID = sourcePopup.getSelectedItem();
                if ( classID == null ) return;
                expandNode( currentlySelectedTreePath, true );
            }
        } );

        JMenuItem collapseAllMenuItem = new JMenuItem( "Collapse All (slow)" );
        collapseAllMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                expandAll( false );
            }
        } );

        JMenuItem expandAllMenuItem = new JMenuItem( "Expand All (slow)" );
        expandAllMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                expandAll( true );
            }
        } );

        final JCheckBoxMenuItem hideEmptyMenuItem = new JCheckBoxMenuItem( "Hide empty", hideEmpty );
        final JCheckBoxMenuItem hideInsig = new JCheckBoxMenuItem( "Hide non-significant", hideInsignificant );
        final JCheckBoxMenuItem hideRedund = new JCheckBoxMenuItem( "Hide redundant", hideRedundant );

        hideEmptyMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                filter( hideEmptyMenuItem.getState(), hideInsig.getState(), hideRedund.getState() );
            }

        } );

        hideInsig.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                filter( hideEmptyMenuItem.getState(), hideInsig.getState(), hideRedund.getState() );

            }

        } );

        hideRedund.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                filter( hideEmptyMenuItem.getState(), hideInsig.getState(), hideRedund.getState() );
            }

        } );

        popup.add( collapseNodeMenuItem );
        // popup.add( collapseAllMenuItem );
        popup.add( expandNodeMenuItem );
        // popup.add( expandAllMenuItem );
        popup.add( hideEmptyMenuItem );
        popup.add( hideRedund );
        popup.add( hideInsig );
        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#deleteGeneSet(java.lang.String)
     */
    @Override
    protected String deleteAndResetGeneSet( GeneSetTerm classID ) {
        String action = super.deleteAndResetGeneSet( classID );
        if ( action == GeneSetPanel.DELETED ) {
            this.removeUserDefinedNode( classID );
        } else if ( action.equals( GeneSetPanel.RESTORED ) ) {
            goTree.revalidate();
        }
        return action;
    }

    /**
     * @param expand If false, collapses all nodes. If true, expands them all.
     */
    protected void expandAll( boolean expand ) {
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();
        expandNode( new TreePath( root ), expand );
    }

    /**
     * @param parent
     * @param expand if true expand, otherwise collapse.
     */
    protected void expandNode( TreePath parent, boolean expand ) {
        // Traverse children
        GeneSetTreeNode node = ( GeneSetTreeNode ) parent.getLastPathComponent();

        TreeModel treeModel = this.goTree.getModel();

        for ( int i = 0; i < treeModel.getChildCount( node ); i++ ) {
            TreeNode n = ( GeneSetTreeNode ) treeModel.getChild( node, i );
            TreePath path = parent.pathByAddingChild( n );

            expandNode( path, expand );

        }

        // expand from leaves up.
        if ( expand ) {
            goTree.expandPath( parent );
        } else {
            if ( parent.getParentPath() == null ) return;
            goTree.collapsePath( parent );
        }
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

}

/**
 * @author paul
 * @version $Id$
 */
class GeneSetTreeNodeRenderer extends DefaultTreeCellRenderer {

    private static final String RESOURCE_LOCATION = "/ubic/erminej/";

    private static final long serialVersionUID = -1L;

    private static final String GOOD_CHILD_ICON = RESOURCE_LOCATION + "littleDiamond.gif";

    private static final String REGULAR_ICON = RESOURCE_LOCATION + "littleSquare.gif";

    private static final String GOODPVAL_ICON = RESOURCE_LOCATION + "goldCircle.gif";

    private static final String GOODPVAL_GOODCHILD_ICON = RESOURCE_LOCATION + "goldCirclePurpleDot.gif";

    private static final String REDUNDANT_ICON = RESOURCE_LOCATION + "littleGreySquare.gif";

    private static final String EMPTYSET_ICON = RESOURCE_LOCATION + "littleLighterGreySquare.gif";

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

    private final Icon redundantIcon = new ImageIcon( this.getClass().getResource( REDUNDANT_ICON ) );

    private final Icon emptySetIcon = new ImageIcon( this.getClass().getResource( EMPTYSET_ICON ) );

    /**
     * Represents a node that is statistically significant and also has significant children.
     */
    private final Icon goodPvalueGoodChildIcon = new ImageIcon( this.getClass().getResource( GOODPVAL_GOODCHILD_ICON ) );

    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score
    private Font plain = new Font( "SansSerif", Font.PLAIN, 11 );

    private boolean selected;

    public GeneSetTreeNodeRenderer( GeneAnnotations geneData ) {
        super();
        this.geneData = geneData;
        nff.setMaximumFractionDigits( 4 );
        this.setOpenIcon( regularIcon );
        this.setLeafIcon( regularIcon );
        this.setClosedIcon( regularIcon );
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
            this.setBackground( Colors.LIGHTYELLOW );
        }

        if ( this.selected ) {
            this.setBackground( Color.LIGHT_GRAY );
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
        String textToDisplay = id.getName() + " [" + id.getId() + "]";

        this.setFont( this.getFont().deriveFont( Font.ITALIC ) );
        this.setForeground( Color.GRAY );
        int numGenesInGeneSet = geneData.numGenesInGeneSet( id );
        if ( id.isAspect() || id.getId().equals( "all" ) ) {
            this.setIcon( emptySetIcon );
            this.setFont( this.getFont().deriveFont( Font.BOLD ) );
            this.setForeground( Color.DARK_GRAY );
        } else if ( geneData.skipDueToRedundancy( id ) ) {
            this.setIcon( redundantIcon );
            textToDisplay += " (Redundant)";
        } else if ( !geneData.getActiveGeneSets().contains( id ) || numGenesInGeneSet == 0 ) {
            this.setIcon( emptySetIcon );
            textToDisplay += " (No genes in your data)";
        } else {
            textToDisplay += " -- " + geneData.numProbesInGeneSet( id ) + " probes, " + numGenesInGeneSet
                    + " genes, multifunc. "
                    + String.format( "%.2f", this.geneData.getMultifunctionality().getGOTermMultifunctionality( id ) );
            this.setFont( plain );
            this.setIcon( regularIcon );
            this.setForeground( Color.BLACK );
        }
        if ( id.isUserDefined() ) {
            this.setForeground( Color.DARK_GRAY );
        }
        return textToDisplay;
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

            if ( pvalCorr < GeneSetTreePanel.FDR_THRESHOLD_FOR_TREE ) {
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
        String redund = getToolTipTextForRedundancy( id );

        setToolTipText( "<html>" + id.getName() + " (" + id.getId() + ")<br/>" + "Aspect: " + aspect + "<br/>" + redund
                + StringUtil.wrap( definition.substring( 0, Math.min( definition.length(), 200 ) ), 50, "<br/>" )
                + ( definition.length() > GeneSetPanel.MAX_DEFINITION_LENGTH ? "..." : "" ) );
    }

    /**
     * @param id
     * @return
     */
    protected String getToolTipTextForRedundancy( GeneSetTerm id ) {
        boolean redundant = geneData.skipDueToRedundancy( id );

        String redund = "";
        if ( redundant ) {
            redund = "<strong>Redundant</strong> with:<br/>";
            Collection<GeneSet> redundantGroups = geneData.getGeneSet( id ).getRedundantGroups();
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
        adaptee.mousePressed( e );
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        adaptee.mouseReleased( e );
    }

}