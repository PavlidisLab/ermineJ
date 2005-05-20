package classScore.gui;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.graph.DirectedGraphNode;
import baseCode.dataStructure.graph.GraphNode;
import classScore.GeneSetPvalRun;
import classScore.Settings;
import classScore.data.GeneSetResult;
import corejava.Format;

/**
 * A Tree display that shows Gene Sets and their scores, and allows uer interaction.
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreePanel extends GeneSetPanel {

    private double fdrThreshold = 0.1;
    private static Log log = LogFactory.getLog( GeneSetTreePanel.class.getName() );
    private JTree goTree = null;
    private GeneSetPvalRun currentResultSet;
    protected String currentlySelectedGeneSet = null;
    protected int currentlySelectedResultSetIndex = -1;
    private BaseCellRenderer rend;

    public GeneSetTreePanel( GeneSetScoreFrame callingFrame, List results, Settings settings ) {
        super( settings, results, callingFrame );
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addedNewGeneSet()
     */
    public void addedNewGeneSet() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addRun()
     */
    public void addRun() {
        fireResultsChanged();
    }

    /**
     * 
     */
    public void updateNodeStyles() {
        log.debug( "Updating nodes" );
        try {
            goData.getGraph().unmarkAll();
            visitAllNodes( goTree, this.getClass().getMethod( "hasGoodPValue", new Class[] { GeneSetTreeNode.class } ) );
            goData.getGraph().unmarkAll();
            visitAllNodes( goTree, this.getClass().getMethod( "hasUsableChildren",
                    new Class[] { GeneSetTreeNode.class } ) );
        } catch ( Exception e ) {
            log.error( e, e );
        }
    }

    /**
     * @param classID
     */
    public void expandToGeneSet( String classID ) {
        TreePath path = this.findByGeneSetId( classID );

        if ( path == null ) {
            this.callingFrame.getStatusMessenger().setError( "Could not find " + classID );
        } else {
            this.callingFrame.getStatusMessenger().setStatus( "Showing " + classID );
        }
        log.debug( "Expanding to path for " + classID );
        goTree.expandPath( path );
        goTree.setSelectionPath( path );
        goTree.scrollPathToVisible( path );
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
    public void hasGoodPValue( GeneSetTreeNode node ) {
        DirectedGraphNode n = ( DirectedGraphNode ) node.getUserObject();
        // if ( n.isVisited() ) return;
        // n.mark();
        if ( currentResultSet == null ) {
            node.setHasGoodChild( false );
        } else {
            GeneSetResult result = ( GeneSetResult ) currentResultSet.getResults().get( n.getKey() );
            if ( result != null && result.getPvalue_corr() < fdrThreshold ) {
                node.setHasGoodChild( true );
            }
        }

        GeneSetTreeNode parent = ( GeneSetTreeNode ) node.getParent();
        if ( parent != null && node.hasGoodChild() ) {
            parent.setHasGoodChild( true );
            hasGoodPValue( parent );
        }

    }

    /**
     * @return
     */
    public void hasUsableChildren( GeneSetTreeNode node ) {
        // DirectedGraphNode n = ( DirectedGraphNode ) node.getUserObject();
        // if ( n.isVisited() ) return;
        // n.mark();
        for ( int i = 0; i < node.getChildCount(); i++ ) {
            GeneSetTreeNode child = ( GeneSetTreeNode ) node.getChildAt( i );
            String id = ( String ) ( ( DirectedGraphNode ) child.getUserObject() ).getKey();
            if ( geneData.getGeneSetToProbeMap().containsKey( id ) ) {
                node.setHasUsableChild( true );
                return;
            }
        }
        node.setHasUsableChild( false );
    }

    /**
     * Called after data files are read in.
     * 
     * @param goData
     * @param geneData
     */
    public void initialize( GONames goData, GeneAnnotations geneData ) {
        assert goData != null : "Go data is still null";
        assert geneData != null : "Gene data is still null";
        this.geneData = geneData;
        this.goData = goData;
        setUpTree( goData );

        this.getViewport().add( goTree );
        this.goTree.setVisible( true );
        this.goTree.revalidate();
    }

    /**
     * @param goData
     */
    private void setUpTree( GONames goData ) {
        this.goTree = goData.getGraph().treeView( GeneSetTreeNode.class );
        this.setRenderer();
        this.goTree.setRootVisible( true );
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
        // // TODO Auto-generated method stub
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
        showDetailsForGeneSet( -1, this.currentlySelectedGeneSet );
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#resetView()
     */
    public void resetView() {
        updateNodeStyles();
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
    private void setRenderer() {
        rend = new BaseCellRenderer( goData, geneData, results );
        this.goTree.setCellRenderer( rend );
    }

    protected MouseListener configurePopupMenu() {
        assert goData != null;
        OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
        JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );
        JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );
        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );

        JMenuItem deleteGeneSetMenuItem = new JMenuItem( "Delete this gene set" );
        deleteGeneSetMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                String classID = null;
                classID = sourcePopup.getSelectedItem();
                deleteGeneSet( classID );
            }
        } );

        JMenuItem collapseNodeMenuItem = new JMenuItem( "Collapse All" );
        collapseNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                expandAll( false );
            }
        } );
        JMenuItem expandNodeMenuItem = new JMenuItem( "Expand All (warning: slow)" );
        expandNodeMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                expandAll( true );
            }
        } );
        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        popup.add( collapseNodeMenuItem );
        popup.add( expandNodeMenuItem );
        MouseListener popupListener = new GeneSetTree_PopupListener( popup );
        return popupListener;
    }

    /**
     * @return Returns the currentlySelectedGeneSet.
     */
    public String getCurrentlySelectedGeneSet() {
        return this.currentlySelectedGeneSet;
    }

    /**
     * @param currentlySelectedGeneSet The currentlySelectedGeneSet to set.
     */
    public void setCurrentlySelectedGeneSet( String currentlySelectedGeneSet ) {
        log.debug( "Switching to " + currentlySelectedGeneSet );
        this.currentlySelectedGeneSet = currentlySelectedGeneSet;
        for ( int i = 0; i < results.size(); i++ ) {
            GeneSetPvalRun r = ( GeneSetPvalRun ) results.get( i );
            if ( r.getName().equals( currentlySelectedGeneSet ) ) {
                this.currentlySelectedResultSetIndex = i;
                this.updateNodeStyles();
                return;
            }
        }
        fireResultsChanged();
    }

    /**
     * 
     */
    private void fireResultsChanged() {
        log.debug( "Changing results" );
        if ( callingFrame != null && this.goTree != null ) {
            currentlySelectedResultSetIndex = callingFrame.getCurrentResultSet();
            ( ( BaseCellRenderer ) this.goTree.getCellRenderer() )
                    .setCurrentResultSet( currentlySelectedResultSetIndex );
            this.currentResultSet = ( GeneSetPvalRun ) results.get( this.currentlySelectedResultSetIndex );
            log.debug( "Fire change to " + callingFrame.getCurrentResultSet() + " run" );
        }
        updateNodeStyles();
        goTree.revalidate();
    }

    /**
     * @return Returns the currentlySelectedResultSetIndex.
     */
    public int getCurrentlySelectedResultSetIndex() {
        return this.currentlySelectedResultSetIndex;
    }

    /**
     * @param currentlySelectedResultSetIndex The currentlySelectedResultSetIndex to set.
     */
    public void setCurrentlySelectedResultSetIndex( int currentlySelectedResultSetIndex ) {
        this.currentlySelectedResultSetIndex = currentlySelectedResultSetIndex;
        this.currentlySelectedGeneSet = ( ( GeneSetPvalRun ) results.get( currentlySelectedResultSetIndex ) ).getName();

    }

    /**
     * @param resultSet
     */
    public void setCurrentResultSet( GeneSetPvalRun resultSet ) {
        this.currentResultSet = resultSet;
        fireResultsChanged();
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
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#deleteGeneSet(java.lang.String)
     */
    protected void deleteGeneSet( String classID ) {
        super.deleteGeneSet( classID );
        this.removeNode( classID );
    }

    /**
     * @param id
     * @param desc
     */
    public void addNode( String id, String desc ) {
        DirectedGraphNode dgn = new DirectedGraphNode( id, desc, goData.getGraph() );
        GeneSetTreeNode gstn = new GeneSetTreeNode( dgn );
        GeneSetTreeNode userNode = getUserNode();
        ( ( DefaultTreeModel ) this.goTree.getModel() ).insertNodeInto( gstn, userNode, userNode.getChildCount() );
        goTree.revalidate();
    }

    public void deleteNode( String id ) {
        GeneSetTreeNode node = ( GeneSetTreeNode ) this.findByGeneSetId( id ).getLastPathComponent();
        if ( node.getChildCount() != 0 ) {
            throw new UnsupportedOperationException( "Can't delete node that has children, sorry" );
        }
        ( ( DefaultTreeModel ) this.goTree.getModel() ).removeNodeFromParent( node );
    }

    public void removeNode( String id ) {
        GeneSetTreeNode gstn = ( GeneSetTreeNode ) this.findByGeneSetId( id ).getLastPathComponent();
        if ( gstn == null ) return;
        ( ( DefaultTreeModel ) this.goTree.getModel() ).removeNodeFromParent( gstn );
    }

    private GeneSetTreeNode getUserNode() {
        return ( GeneSetTreeNode ) this.findByGeneSetId( GONames.USER_DEFINED ).getLastPathComponent();
    }

}

class BaseCellRenderer extends DefaultTreeCellRenderer {

    /**
     * 
     */
    private static final String GOOD_CHILD_ICON = "resources/littleDiamond.gif";
    /**
     * 
     */
    private static final String REGULAR_ICON = "resources/littleSquare.gif";
    private static Log log = LogFactory.getLog( BaseCellRenderer.class.getName() );
    private int currentlySelectedResultSet = -1;
    private GeneAnnotations geneData;
    private final GONames goData;
    private Icon goodChildIcon;
    private Icon regularIcon;
    private Font italic = new Font( "SansSerif", Font.ITALIC, 11 );
    private Format nf = new Format( "%.3g" ); // for the gene set p value.
    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score
    private Font plain = new Font( "SansSerif", Font.PLAIN, 11 );

    private final List results;
    private boolean selected;

    public BaseCellRenderer( GONames goData, GeneAnnotations geneData, List results ) {
        super();
        this.results = results;
        this.goData = goData;
        this.geneData = geneData;
        nff.setMaximumFractionDigits( 4 );
        this.regularIcon = new ImageIcon( this.getClass().getResource( REGULAR_ICON ) );
        this.goodChildIcon = new ImageIcon( this.getClass().getResource( GOOD_CHILD_ICON ) );
        this.setOpenIcon( regularIcon );
        this.setLeafIcon( regularIcon );
        this.setClosedIcon( regularIcon );
    }

    /**
     * TODO
     * <ul>
     * <li>Make non-searched-for nodes greyed out
     * <li>Tool tip for node.
     * <li>Color node by pvalue.(background color?)
     * <li>Nodes at higher levels that are not significant, but which have significant classes under them, should be
     * shown in contrasting color, or something.
     * </ul>
     * 
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean,
     *      boolean, boolean, int, boolean)
     */
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

        displayedText = name;
        displayedText = addGeneSetSizeInformation( name, id, displayedText, node );

        if ( node.hasGoodChild() ) {
            this.setIcon( goodChildIcon );
        } else {
            this.setIcon( regularIcon );
        }

        if ( currentlySelectedResultSet >= 0 && results.size() >= currentlySelectedResultSet + 1 ) {
            displayedText = addResultsFlags( id, displayedText );
        }
        this.setText( displayedText );

        if ( this.selected ) {
            this.setBackground( Color.LIGHT_GRAY );
        }

        return this;
    }

    /**
     * @param id
     * @param displayedText
     * @return
     */
    private String addResultsFlags( String id, String displayedText ) {
        GeneSetPvalRun res = ( GeneSetPvalRun ) results.get( currentlySelectedResultSet );
        assert res != null;
        assert res.getResults() != null;
        if ( res.getResults().get( id ) != null && ( ( GeneSetResult ) res.getResults().get( id ) ).getPvalue() < 1.0 ) {
            GeneSetResult result = ( GeneSetResult ) res.getResults().get( id );
            double pvalue = result.getPvalue();
            displayedText = displayedText + " -- p = " + nf.format( pvalue ) + " -- effective size = "
                    + result.getEffectiveSize();
            double pvalCorr = result.getPvalue_corr();
            Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );
            this.setBackground( bgColor );
        } else {
            this.setBackground( Color.WHITE );
        }
        return displayedText;
    }

    /**
     * @param currentlySelectedResultSet
     */
    public void setCurrentResultSet( int currentlySelectedResultSet ) {
        log.debug( "Setting results to " + currentlySelectedResultSet );
        this.currentlySelectedResultSet = currentlySelectedResultSet;
        this.revalidate();
    }

    /**
     * @param name
     * @param id
     * @param displayedText
     * @param node
     * @return
     */
    private String addGeneSetSizeInformation( String name, String id, String displayedText, GeneSetTreeNode node ) {
        if ( !geneData.getGeneSetToProbeMap().containsKey( id ) || !geneData.getSelectedSets().contains( id ) ) {
            this.setFont( italic );
            this.setForeground( Color.GRAY );
            if ( !node.hasUsableChild() ) {
                // can do something else here.
                displayedText = name;
            } else {
                displayedText = name;
            }
        } else {
            displayedText = name + " -- " + ( ( Collection ) geneData.getGeneSetToProbeMap().get( id ) ).size()
                    + " probes, " + ( ( Collection ) geneData.getGeneSetToGeneMap().get( id ) ).size() + " genes";
            this.setFont( plain );
            this.setForeground( Color.BLACK );
        }
        return displayedText;
    }

}

class GeneSetTree_PopupListener extends MouseAdapter {
    private static Log log = LogFactory.getLog( GeneSetTree_PopupListener.class.getName() );
    OutputPanelPopupMenu popup;

    GeneSetTree_PopupListener( OutputPanelPopupMenu popupMenu ) {
        popup = popupMenu;
    }

    public void mousePressed( MouseEvent e ) {
        maybeShowPopup( e );
    }

    public void mouseReleased( MouseEvent e ) {
        maybeShowPopup( e );
    }

    private void maybeShowPopup( MouseEvent e ) {
        if ( e.isPopupTrigger() ) {
            log.debug( "Got popup trigger" );
            JTree source = ( JTree ) e.getSource();
            int x = e.getX();
            int y = e.getY();
            TreePath path = source.getPathForLocation( x, y );
            if ( path == null ) return;

            source.setSelectionPath( path );
            source.scrollPathToVisible( path );

            GeneSetTreeNode selectedNode = ( GeneSetTreeNode ) path.getLastPathComponent();
            String id = ( String ) ( ( GraphNode ) selectedNode.getUserObject() ).getKey();
            popup.show( e.getComponent(), e.getX(), e.getY() );
            popup.setSelectedItem( id );
        }
    }
}

class GeneSetTreePanel_mouseListener extends MouseAdapter {
    private GeneSetTreePanel adaptee;

    GeneSetTreePanel_mouseListener( GeneSetTreePanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void mousePressed( MouseEvent e ) {
        adaptee.mousePressed( e );
    }

    public void mouseReleased( MouseEvent e ) {
        adaptee.mouseReleased( e );
    }

}