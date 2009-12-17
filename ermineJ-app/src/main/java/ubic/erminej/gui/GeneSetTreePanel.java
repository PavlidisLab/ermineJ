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
import java.util.List;

import javax.help.UnsupportedOperationException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ubic.basecode.util.StringUtil;

import ubic.basecode.bio.GOEntry;
import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.basecode.dataStructure.graph.GraphNode;
import ubic.erminej.GeneSetPvalRun;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;
import corejava.Format;

/**
 * A Tree display that shows Gene Sets and their scores, and allows uer interaction.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreePanel extends GeneSetPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 0L;
    private double fdrThreshold = 0.1;
    private JTree goTree = null;
    private GeneSetPvalRun currentResultSet;
    protected String currentlySelectedGeneSet = null;
    protected int currentlySelectedResultSetIndex = -1;
    private BaseCellRenderer rend;
    private Collection<String> geneSets;

    public GeneSetTreePanel( GeneSetScoreFrame callingFrame, List<GeneSetPvalRun> results, Settings settings ) {
        super( settings, results, callingFrame );
    }

    /*
     * (non-Javadoc)
     * @see classScore.gui.GeneSetsResultsScrollPane#addedNewGeneSet()
     */
    @Override
    public void addedNewGeneSet() {
        log.debug( "no-op" );
    }

    /*
     * (non-Javadoc)
     * @see classScore.gui.GeneSetsResultsScrollPane#addRun()
     */
    @Override
    public void addRun() {
        fireResultsChanged();
    }

    /**
     * 
     */
    public void updateNodeStyles() {
        log.debug( "Updating nodes" );
        try {
            this.geneSets = null;
            visitAllNodes( goTree, this.getClass().getMethod( "clearNode", new Class[] { GeneSetTreeNode.class } ) );
            visitAllNodes( goTree, this.getClass().getMethod( "hasGoodChild", new Class[] { GeneSetTreeNode.class } ) );
            visitAllNodes( goTree, this.getClass().getMethod( "hasUsableChildren",
                    new Class[] { GeneSetTreeNode.class } ) );
        } catch ( Exception e ) {
            log.error( e, e );
        }
    }

    /**
     * @param classID
     */
    public boolean expandToGeneSet( String classID ) {
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
     * @param tree
     * @param names
     * @return
     */
    public TreePath findByGeneSetId( String id ) {
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();
        return find( new TreePath( root ), id, 0 );
    }

    /**
     * @param id
     */
    public void hasGoodChild( GeneSetTreeNode node ) {
        if ( currentResultSet == null ) {
            node.setHasGoodChild( false );
            return;
        }
        Enumeration e = node.breadthFirstEnumeration();
        e.nextElement(); // the first node is this node.
        while ( e.hasMoreElements() ) {
            GeneSetTreeNode childNode = ( GeneSetTreeNode ) e.nextElement();
            DirectedGraphNode n = ( DirectedGraphNode ) childNode.getUserObject();
            GeneSetResult result = currentResultSet.getResults().get( n.getKey() );
            if ( result != null && result.getCorrectedPvalue() <= fdrThreshold ) {
                node.setHasGoodChild( true );
                return;
            }
        }
    }

    /**
     * @param node
     */
    public void clearNode( GeneSetTreeNode node ) {
        node.setHasGoodChild( false );
        node.setHasUsableChild( false );
    }

    /**
     * @return
     */
    public void hasUsableChildren( GeneSetTreeNode node ) {
        Enumeration e = node.breadthFirstEnumeration();
        e.nextElement(); // the first node is this node.
        if ( geneSets == null ) geneSets = geneData.getGeneSets();
        while ( e.hasMoreElements() ) {
            GeneSetTreeNode childNode = ( GeneSetTreeNode ) e.nextElement();
            String id = ( String ) ( ( DirectedGraphNode ) childNode.getUserObject() ).getKey();
            if ( geneSets.contains( id ) ) {
                node.setHasUsableChild( true );
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
    public void initialize( GONames go, GeneAnnotations gd ) {
        // assert goData != null : "Go data is still null";
        // assert geneData != null : "Gene data is still null";
        this.geneData = gd;
        this.goData = go;
        setUpTree( go );

        this.getViewport().add( goTree );
        this.goTree.setVisible( true );
        this.goTree.revalidate();
    }

    /**
     * @param goData
     */
    private void setUpTree( GONames goData ) {
        assert ( goData.getGraph() != null ) : "GO Graph cannot be null to use tree panel";
        this.goTree = goData.getGraph().treeView( GeneSetTreeNode.class );
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
                TreePath path = e.getPath();
                GeneSetTreeNode currentNode = ( GeneSetTreeNode ) path.getLastPathComponent();
                if ( currentNode.getUserObject() instanceof GraphNode ) {
                    currentlySelectedGeneSet = ( String ) ( ( GraphNode ) ( currentNode ).getUserObject() ).getKey();
                } else {
                    log.debug( currentNode.getUserObject().getClass().getName() );
                }
            }
        } );
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
        showDetailsForGeneSet( currentlySelectedResultSetIndex, currentlySelectedGeneSet );
    }

    /*
     * (non-Javadoc)
     * @see classScore.gui.GeneSetsResultsScrollPane#resetView()
     */
    @Override
    public void resetView() {
        updateNodeStyles();
        goTree.revalidate();
        goTree.repaint();
    }

    /**
     * http://javaalmanac.com/egs/javax.swing.tree/GetNodes.html
     * 
     * @param tree
     */
    public void visitAllNodes( JTree tree, Method process ) {
        TreeNode root = ( TreeNode ) tree.getModel().getRoot();
        visitAllNodes( root, process );
    }

    /**
     * http://javaalmanac.com/egs/javax.swing.tree/GetNodes.html
     * 
     * @param node
     * @param process
     */
    public void visitAllNodes( TreeNode node, Method process ) {
        if ( process != null ) {
            try {
                process.invoke( this, new Object[] { node } );
            } catch ( Exception e ) {
                log.error( e, e );
            }
        }

        if ( node.getChildCount() >= 0 ) {
            for ( Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = ( TreeNode ) e.nextElement();
                visitAllNodes( n, process );
            }
        }
    }

    /**
     * @param expand If false, collapses all nodes. If true, expands them all.
     */
    protected void expandAll( boolean expand ) {
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();
        expandAll( new TreePath( root ), expand );

    }

    /**
     * @param parent
     * @param expand
     */
    protected void expandAll( TreePath parent, boolean expand ) {
        // Traverse children
        TreeNode node = ( TreeNode ) parent.getLastPathComponent();
        if ( node.getChildCount() >= 0 ) {
            for ( Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = ( TreeNode ) e.nextElement();
                TreePath path = parent.pathByAddingChild( n );
                expandAll( path, expand );
            }
        }

        // Expansion or collapse must be done bottom-up
        if ( expand ) {
            goTree.expandPath( parent );
        } else {
            // do not collapse the root, as we have disabled double-clicks to open it.
            if ( parent.getParentPath() == null ) return;
            goTree.collapsePath( parent );
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
    private TreePath find( TreePath parent, String id, int depth ) {
        GeneSetTreeNode node = ( GeneSetTreeNode ) parent.getLastPathComponent();
        String o = ( String ) ( ( DirectedGraphNode ) node.getUserObject() ).getKey();

        // If equal, go down the branch
        if ( o.equals( id ) ) {
            return parent;
        }

        // Traverse children
        if ( node.getChildCount() >= 0 ) {
            for ( Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = ( TreeNode ) e.nextElement();
                TreePath path = parent.pathByAddingChild( n );
                TreePath result = find( path, id, depth + 1 );
                // Found a match
                if ( result != null ) {
                    return result;
                }
            }
        }

        // No match at this branch
        return null;
    }

    /**
     * 
     */
    @Override
    protected MouseListener configurePopupMenu() {
        MouseListener m = super.configurePopupMenu();
        JMenuItem collapseNodeMenuItem = new JMenuItem( "Collapse All (slow)" );
        collapseNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                expandAll( false );
            }
        } );
        JMenuItem expandNodeMenuItem = new JMenuItem( "Expand All (slow)" );
        expandNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                expandAll( true );
            }
        } );
        popup.add( collapseNodeMenuItem );
        popup.add( expandNodeMenuItem );
        return m;
    }

    /**
     * 
     */
    private void setRenderer() {
        rend = new BaseCellRenderer( goData, geneData, this, results );
        this.goTree.setCellRenderer( rend );
    }

    /**
     * @return Returns the currentlySelectedGeneSet.
     */
    public String getCurrentlySelectedGeneSet() {
        return this.currentlySelectedGeneSet;
    }

    /**
     * @param e
     * @return
     */
    @Override
    protected String popupRespondAndGetGeneSet( MouseEvent e ) {
        JTree source = ( JTree ) e.getSource();
        int x = e.getX();
        int y = e.getY();
        TreePath path = source.getPathForLocation( x, y );
        if ( path == null )
        ;

        source.setSelectionPath( path );
        source.scrollPathToVisible( path );

        GeneSetTreeNode selectedNode = ( GeneSetTreeNode ) path.getLastPathComponent();
        String classID = ( String ) ( ( GraphNode ) selectedNode.getUserObject() ).getKey();
        return classID;
    }

    /**
     * 
     */
    public void fireResultsChanged() {
        log.debug( "Changing results" );
        if ( callingFrame != null && this.goTree != null ) {
            currentlySelectedResultSetIndex = callingFrame.getCurrentResultSet();
            ( ( BaseCellRenderer ) this.goTree.getCellRenderer() )
                    .setCurrentResultSet( currentlySelectedResultSetIndex );
            this.currentResultSet = results.get( this.currentlySelectedResultSetIndex );
            log.debug( "Fire change to " + callingFrame.getCurrentResultSet() + " run" );
        }
        resetView();
    }

    /**
     * @return Returns the fdrThreshold.
     */
    public double getFdrThreshold() {
        return this.fdrThreshold;
    }

    /**
     * @param fdrThreshold The fdrThreshold to set.
     */
    public void setFdrThreshold( double fdrThreshold ) {
        this.fdrThreshold = fdrThreshold;
    }

    /*
     * (non-Javadoc)
     * @see classScore.gui.GeneSetsResultsScrollPane#deleteGeneSet(java.lang.String)
     */
    @Override
    protected String deleteAndResetGeneSet( String classID ) {
        String action = super.deleteAndResetGeneSet( classID );
        if ( action == GeneSetPanel.DELETED ) {
            this.removeUserDefinedNode( classID );
        } else if ( action.equals( GeneSetPanel.RESTORED ) ) {
            goTree.revalidate();
        }
        return action;
    }

    /**
     * Add a new node to the graph under the user-defined group.
     * 
     * @param id
     * @param desc
     */
    public void addNode( String id, String desc ) {

        /* already exists, don't add it */
        if ( isInUserDefined( id ) ) {
            return;
        }
        GeneSetTreeNode userNode = getUserNode();
        DirectedGraphNode<String, GOEntry> newNode = new DirectedGraphNode<String, GOEntry>( id, new GOEntry( id, desc,
                desc, "No aspect defined" ), goData.getGraph() );
        GeneSetTreeNode newTreeNode = new GeneSetTreeNode<String, GOEntry>( newNode );

        ( ( DefaultTreeModel ) this.goTree.getModel() )
                .insertNodeInto( newTreeNode, userNode, userNode.getChildCount() );
        goTree.revalidate();
    }

    public void removeNode( String id ) {
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

    public void removeUserDefinedNode( String id ) {
        if ( !isInUserDefined( id ) ) return;
        GeneSetTreeNode node = ( GeneSetTreeNode ) find( this.findByGeneSetId( GONames.USER_DEFINED ), id, 0 )
                .getLastPathComponent();
        ( ( DefaultTreeModel ) this.goTree.getModel() ).removeNodeFromParent( node );

    }

    private boolean isInUserDefined( final String id ) {
        if ( this.findByGeneSetId( id ) == null ) return false;
        return find( this.findByGeneSetId( GONames.USER_DEFINED ), id, 0 ) != null;
    }

    private GeneSetTreeNode getUserNode() {
        return ( GeneSetTreeNode ) this.findByGeneSetId( GONames.USER_DEFINED ).getLastPathComponent();
    }

}

class BaseCellRenderer extends DefaultTreeCellRenderer {

    private static final String RESOURCE_LOCATION = "/ubic/erminej/";

    /**
     * 
     */
    private static final long serialVersionUID = -2038921719858424598L;

    /**
     * 
     */
    private static final String GOOD_CHILD_ICON = RESOURCE_LOCATION + "littleDiamond.gif";

    /**
     * 
     */
    private static final String REGULAR_ICON = RESOURCE_LOCATION + "littleSquare.gif";

    /**
     * 
     */
    private static final String GOODPVAL_ICON = RESOURCE_LOCATION + "goldCircle.gif";

    /**
     * 
     */
    private static final String GOODPVAL_GOODCHILD_ICON = RESOURCE_LOCATION + "goldCirclePurpleDot.gif";

    private int currentlySelectedResultSet = -1;
    private GeneAnnotations geneData;
    private GeneSetTreePanel panel;
    private final GONames goData;
    private final Icon goodChildIcon = new ImageIcon( this.getClass().getResource( GOOD_CHILD_ICON ) );
    private final Icon regularIcon = new ImageIcon( this.getClass().getResource( REGULAR_ICON ) );
    private final Icon goodPvalueIcon = new ImageIcon( this.getClass().getResource( GOODPVAL_ICON ) );
    private final Icon goodPvalueGoodChildIcon = new ImageIcon( this.getClass().getResource( GOODPVAL_GOODCHILD_ICON ) );
    private Format nf = new Format( "%.3g" ); // for the gene set p value.
    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score
    private Font plain = new Font( "SansSerif", Font.PLAIN, 11 );

    private final List<GeneSetPvalRun> results;
    private boolean selected;

    public BaseCellRenderer( GONames goData, GeneAnnotations geneData, GeneSetTreePanel panel,
            List<GeneSetPvalRun> results ) {
        super();
        this.results = results;
        this.panel = panel;
        this.goData = goData;
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
    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );
        this.selected = selected;
        this.hasFocus = hasFocus;
        setOpaque( true );

        String name;
        String id = "";
        String displayedText = "";
        GeneSetTreeNode node = ( GeneSetTreeNode ) value;

        if ( node.getUserObject() instanceof DirectedGraphNode ) {
            DirectedGraphNode nodeObj = ( DirectedGraphNode ) node.getUserObject();
            id = ( String ) nodeObj.getKey();
            name = nodeObj.toString();
        } else {
            Object nodeObj = node.getUserObject();
            if ( nodeObj == null ) return this;
            name = nodeObj.toString();
        }

        setupToolTip( id );

        displayedText = name;
        displayedText = addGeneSetSizeInformation( name, id, displayedText, node );

        if ( node.hasGoodChild() ) {
            this.setIcon( goodChildIcon );
        } else {
            this.setIcon( regularIcon );
        }

        this.setBackground( Color.WHITE );
        if ( currentlySelectedResultSet >= 0 && results.size() >= currentlySelectedResultSet + 1 ) {
            displayedText = addResultsFlags( node, id, displayedText );
        }
        this.setText( displayedText );

        if ( goData.isUserDefined( id ) ) {
            this.setBackground( Colors.LIGHTYELLOW );
        }

        if ( this.selected ) {
            this.setBackground( Color.LIGHT_GRAY );
        }

        return this;
    }

    /**
     * @param id
     */
    private void setupToolTip( String id ) {
        // code also in the tree renderer
        String aspect = goData.getAspectForId( id );
        String definition = goData.getDefinitionForId( id );
        setToolTipText( "<html>Aspect: " + aspect + "<br>Definition: "
                + StringUtil.wrap( definition.substring( 0, Math.min( definition.length(), 200 ) ), 50, "<br>" )
                + ( definition.length() > GeneSetPanel.MAX_DEFINITION_LENGTH ? "..." : "" ) );
    }

    /**
     * @param id
     * @param displayedText
     * @return
     */
    private String addResultsFlags( GeneSetTreeNode node, String id, String displayedText ) {
        GeneSetPvalRun res = results.get( currentlySelectedResultSet );
        assert res != null;
        assert res.getResults() != null;
        if ( hasResults( id, res ) ) {
            GeneSetResult result = res.getResults().get( id );
            double pvalue = result.getPvalue();
            displayedText = displayedText + " -- p = " + nf.format( pvalue );
            double pvalCorr = result.getCorrectedPvalue();
            Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );
            this.setBackground( bgColor );
            if ( pvalCorr < this.panel.getFdrThreshold() ) {
                if ( node.hasGoodChild() ) {
                    this.setIcon( this.goodPvalueGoodChildIcon );
                } else {
                    this.setIcon( goodPvalueIcon );
                }
            } else {
                if ( node.hasGoodChild() ) {
                    this.setIcon( goodChildIcon );
                } else {
                    this.setIcon( regularIcon );
                }
            }
        } else {
            this.setBackground( Color.WHITE );
        }
        return displayedText;
    }

    /**
     * @param id
     * @param res
     * @return
     */
    private boolean hasResults( String id, GeneSetPvalRun res ) {
        return res.getResults().get( id ) != null && res.getResults().get( id ).getPvalue() < 1.0;
    }

    /**
     * @param currentlySelectedResultSet
     */
    public void setCurrentResultSet( int currentlySelectedResultSet ) {
        this.currentlySelectedResultSet = currentlySelectedResultSet;
        this.validate();
        this.repaint();
    }

    /**
     * @param name
     * @param id
     * @param displayedText
     * @param node
     * @return
     */
    private String addGeneSetSizeInformation( String name, String id, String displayedText, GeneSetTreeNode node ) {
        if ( node.hasUsableChild() ) {
            // this.setFont( plain );
            this.setForeground( Color.BLACK );
        } else {
            // this.setFont( italic );
            this.setForeground( Color.GRAY );
        }
        if ( !geneData.getGeneSets().contains( id ) || !geneData.getSelectedSets().contains( id ) ) {
            // this.setFont( italic );
            this.setForeground( Color.GRAY );
        } else {
            displayedText = name + " -- " + geneData.numProbesInGeneSet( id ) + " probes, "
                    + geneData.numGenesInGeneSet( id ) + " genes";
            this.setFont( plain );
            this.setForeground( Color.BLACK );
        }
        return displayedText;
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