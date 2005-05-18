package classScore.gui;

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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
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
public class GeneSetTreePanel extends GeneSetsResultsScrollPane {
    private static Log log = LogFactory.getLog( GeneSetTreePanel.class.getName() );
    private JTree goTree = null;
    private MutableTreeNode userRoot = null;

    private GeneSetPvalRun currentResultSet;
    protected String currentlySelectedGeneSet = null;
    protected int currentlySelectedResultSetIndex = -1;

    public GeneSetTreePanel( GeneSetScoreFrame callingFrame, List results, Settings settings ) {
        super( settings, results, callingFrame );
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addedNewGeneSet()
     */
    public void addedNewGeneSet() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addRun()
     */
    public void addRun() {
        if ( callingFrame != null && this.goTree != null ) {
            currentlySelectedResultSetIndex = callingFrame.getCurrentResultSet();
            ( ( BaseCellRenderer ) this.goTree.getCellRenderer() )
                    .setCurrentResultSet( currentlySelectedResultSetIndex );
        }

        currentResultSet = ( GeneSetPvalRun ) results.get( currentlySelectedResultSetIndex );
        updateNodeStyles();

        // for ( Iterator iter = currentResultSet.getResults().keySet().iterator(); iter.hasNext(); ) {
        // String id = ( String ) iter.next();
        // GeneSetResult result = ( GeneSetResult ) currentResultSet.getResults().get( id );
        // if ( result.getPvalue_corr() < 0.05 ) { // FIXME
        // log.debug( "Coloring " + id );
        // // this.setBackground( Colors.LIGHTBLUE4 );
        // TreePath path = goTree.getNextMatch( id, 0, Position.Bias.Forward );
        //
        // }
        // }
        goTree.revalidate();
    }

    /**
     * 
     */
    public void updateNodeStyles() {
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
            if ( result == null ) return;
            if ( result.getPvalue_corr() < 0.05 ) { // FIXME
                // log.debug( n.getKey() + " is a good node" );
                node.setHasGoodChild( true );
            }
        }

        GeneSetTreeNode parent = ( GeneSetTreeNode ) node.getParent();
        if ( parent != null ) {
            if ( node.isHasGoodChild() ) {
                // log.debug( n.getKey() + " has a good child" );
                parent.setHasGoodChild( true );
                // recurse
                if ( parent.getParent() != null ) {
                    hasGoodPValue( parent );
                }
            }

        }
    }

    /**
     * @return
     */
    public void hasUsableChildren( GeneSetTreeNode node ) {
        DirectedGraphNode n = ( DirectedGraphNode ) node.getUserObject();
        // if ( n.isVisited() ) return;
        // n.mark();
        for ( int i = 0; i < node.getChildCount(); i++ ) {
            GeneSetTreeNode child = ( GeneSetTreeNode ) node.getChildAt( i );
            String id = ( String ) ( ( DirectedGraphNode ) child.getUserObject() ).getKey();
            if ( geneData.getGeneSetToProbeMap().containsKey( id ) ) {
                // log.debug( id + "has usable child" );
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
        this.goTree = goData.getGraph().treeView( GeneSetTreeNode.class );
        this.goData = goData;
        addUserNode();
        setRenderer();
        goTree.setRootVisible( true );
        ToolTipManager.sharedInstance().registerComponent( goTree );
        MouseListener popupListener = configurePopupMenu();
        goTree.addMouseListener( popupListener );
        goTree.addMouseListener( new GeneSetTreePanel_mouseListener( this ) );
        goTree.addTreeSelectionListener( new TreeSelectionListener() {
            public void valueChanged( TreeSelectionEvent e ) {
                TreePath path = e.getPath();
                GeneSetTreeNode currentNode = ( GeneSetTreeNode ) path.getLastPathComponent();
                if ( currentNode.getUserObject() instanceof GraphNode ) { // FIXME - what about user node.
                    currentlySelectedGeneSet = ( String ) ( ( GraphNode ) ( currentNode ).getUserObject() ).getKey();
                } else {
                    log.debug( currentNode.getUserObject().getClass().getName() );
                }
            }
        } );
        this.getViewport().add( goTree );

        goTree.setVisible( true );
        goTree.revalidate();
    }

    /**
     * @param e
     */
    public void mousePressed( MouseEvent e ) {
        // TODO Auto-generated method stub
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
            // left button
        } else if ( e.getButton() == MouseEvent.BUTTON3 ) {
            // right button

        }
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
        // TODO Auto-generated method stub

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
        // node is visited exactly once
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
     * 
     */
    private void addUserNode() {
        log.debug( "Adding user node" );
        userRoot = new GeneSetTreeNode( new DirectedGraphNode( "User-defined", "User-defined", goData.getGraph() ) );
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode ) goTree.getModel().getRoot();
        root.add( userRoot );
        goTree = new JTree( root );
        goTree.revalidate();
    }

    /**
     * @param expand If false, collapses all nodes. If true, expands them all.
     */
    private void expandAll( boolean expand ) {
        TreeNode root = ( TreeNode ) goTree.getModel().getRoot();

        // Traverse tree from root
        expandAll( new TreePath( root ), expand );
    }

    private void expandAll( TreePath parent, boolean expand ) {
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

        // Icon openIcon = new ImageIcon(this.getClass().getResource("resources/goOpenIcon.gif"));
        // Icon closedIcon = new ImageIcon(this.getClass().getResource("resources/goClosedIcon.gif"));
        // Icon leafIcon = new ImageIcon(this.getClass().getResource("resources/goLeafIcon.gif"));

        Icon openIcon = new ImageIcon( this.getClass().getResource( "resources/Play16.gif" ) );
        Icon closedIcon = new ImageIcon( this.getClass().getResource( "resources/Play16.gif" ) );
        Icon leafIcon = new ImageIcon( this.getClass().getResource( "resources/Play16.gif" ) );

        BaseCellRenderer rend = new BaseCellRenderer( goData, geneData, results );
        // DefaultTreeCellRenderer rend = new DefaultTreeCellRenderer();
        rend.setOpenIcon( openIcon );
        rend.setLeafIcon( leafIcon );
        rend.setClosedIcon( closedIcon );
        this.goTree.setCellRenderer( rend );
    }

    protected MouseListener configurePopupMenu() {
        OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
        JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );
        JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );
        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );
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
        this.currentlySelectedGeneSet = currentlySelectedGeneSet;
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
    }

    /**
     * @param resultSet
     */
    public void setCurrentResultSet( GeneSetPvalRun resultSet ) {
        this.currentResultSet = resultSet;
    }

}

class BaseCellRenderer extends DefaultTreeCellRenderer {

    private static Log log = LogFactory.getLog( BaseCellRenderer.class.getName() );
    private int currentlySelectedResultSet = -1;
    private GeneAnnotations geneData;
    private final GONames goData;
    private Icon goodChildIcon;
    private Font italic = new Font( "SansSerif", Font.ITALIC, 11 );
    private Format nf = new Format( "%g" ); // for the gene set p value.
    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score
    private Font plain = new Font( "SansSerif", Font.PLAIN, 11 );

    private Icon regularIcon;
    private final List results;
    private boolean selected;

    public BaseCellRenderer( GONames goData, GeneAnnotations geneData, List results ) {
        super();
        this.results = results;
        this.goData = goData;
        this.geneData = geneData;
        nff.setMaximumFractionDigits( 4 );
        regularIcon = new ImageIcon( this.getClass().getResource( "resources/Play16.gif" ) );
        goodChildIcon = new ImageIcon( this.getClass().getResource( "resources/Play16-green.gif" ) );
    }

    /**
     * TODO
     * <ul>
     * <li>Make non-searched-for nodes greyed out
     * <li>Make customized nodes pink.(text color?)
     * <li>Add the pvalue to each node
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

        if ( this.selected ) {
            this.setBackground( Color.LIGHT_GRAY );
        } else {
            this.setBackground( Color.WHITE );
        }

        displayedText = name;

        displayedText = addGeneSetSizeInformation( name, id, displayedText, node );

        if ( node.isHasGoodChild() ) {
            this.setIcon( goodChildIcon );
        } else {
            this.setIcon( regularIcon );
        }

        if ( currentlySelectedResultSet >= 0 && results.size() >= currentlySelectedResultSet + 1 ) {
            GeneSetPvalRun res = ( GeneSetPvalRun ) results.get( currentlySelectedResultSet );
            assert res != null;
            assert res.getResults() != null;
            if ( res.getResults().get( id ) != null
                    && ( ( GeneSetResult ) res.getResults().get( id ) ).getPvalue() < 1.0 ) {
                GeneSetResult result = ( GeneSetResult ) res.getResults().get( id );
                double pvalue = result.getPvalue();
                displayedText = displayedText + " -- p = " + nf.format( pvalue ) + "--" + " effective size = "
                        + result.getEffectiveSize();
                if ( result.getPvalue_corr() < 0.05 ) {
                    this.setBackground( Colors.LIGHTBLUE5 );
                } else {
                    this.setBackground( Color.WHITE );
                }

            }
        }

        this.setText( displayedText );
        return this;

    }

    /**
     * @param currentlySelectedResultSet
     */
    public void setCurrentResultSet( int currentlySelectedResultSet ) {
        this.currentlySelectedResultSet = currentlySelectedResultSet;
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
            if ( !node.isHasUsableChild() ) {
                this.setEnabled( false );
                setText( name + " >>>>>> No usable children" );
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