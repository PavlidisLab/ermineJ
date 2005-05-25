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

import baseCode.bio.GOEntry;
import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.graph.DirectedGraphNode;
import baseCode.dataStructure.graph.GraphNode;
import baseCode.util.StringUtil;
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
    public void expandToGeneSet( String classID ) {
        TreePath path = this.findByGeneSetId( classID );

        if ( path == null ) {
            this.callingFrame.getStatusMessenger().showError( "Could not find " + classID );
        } else {
            this.callingFrame.getStatusMessenger().showStatus( "Showing " + classID );
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
    public void hasGoodChild( GeneSetTreeNode node ) {
        DirectedGraphNode n = ( DirectedGraphNode ) node.getUserObject();
        GeneSetResult result = null;
        if ( currentResultSet == null ) {
            node.setHasGoodChild( false );
        } else {
            result = ( GeneSetResult ) currentResultSet.getResults().get( n.getKey() );
        }
        GeneSetTreeNode parent = ( GeneSetTreeNode ) node.getParent();
        if ( parent != null
                && ( ( result != null && result.getCorrectedPvalue() < fdrThreshold ) || node.hasGoodChild() ) ) {
            parent.setHasGoodChild( true );
            hasGoodChild( parent );
        }
    }

    /**
     * @return
     */
    public void hasUsableChildren( GeneSetTreeNode node ) {
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
        showDetailsForGeneSet( this.currentlySelectedResultSetIndex, this.currentlySelectedGeneSet );
    }

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#resetView()
     */
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
    private void setRenderer() {
        rend = new BaseCellRenderer( goData, geneData, results );
        this.goTree.setCellRenderer( rend );
    }

    protected MouseListener configurePopupMenu() {
        assert goData != null;
        final OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
        JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );
        final JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );
        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );

        final JMenuItem deleteGeneSetMenuItem = new JMenuItem( "Delete this gene set" );
        deleteGeneSetMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                String classID = null;
                classID = sourcePopup.getSelectedItem();
                deleteGeneSet( classID );
            }
        } );

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
        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        popup.add( deleteGeneSetMenuItem );
        popup.add( collapseNodeMenuItem );
        popup.add( expandNodeMenuItem );
        MouseListener popupListener = new MouseAdapter() {

            public void mousePressed( MouseEvent e ) {
                maybeShowPopup( e );
            }

            public void mouseReleased( MouseEvent e ) {
                maybeShowPopup( e );
            }

            private void maybeShowPopup( MouseEvent e ) {
                if ( e.isPopupTrigger() ) {
                    JTree source = ( JTree ) e.getSource();
                    int x = e.getX();
                    int y = e.getY();
                    TreePath path = source.getPathForLocation( x, y );
                    if ( path == null ) return;

                    source.setSelectionPath( path );
                    source.scrollPathToVisible( path );

                    GeneSetTreeNode selectedNode = ( GeneSetTreeNode ) path.getLastPathComponent();
                    String classID = ( String ) ( ( GraphNode ) selectedNode.getUserObject() ).getKey();
                    if ( !goData.getUserDefinedGeneSets().contains( classID ) ) {
                        deleteGeneSetMenuItem.setEnabled( false );
                    } else {
                        deleteGeneSetMenuItem.setEnabled( true );
                    }

                    popup.show( e.getComponent(), e.getX(), e.getY() );
                    popup.setSelectedItem( classID );

                }
            }

        };

        return popupListener;
    }

    /**
     * @return Returns the currentlySelectedGeneSet.
     */
    public String getCurrentlySelectedGeneSet() {
        return this.currentlySelectedGeneSet;
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
            this.currentResultSet = ( GeneSetPvalRun ) results.get( this.currentlySelectedResultSetIndex );
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
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#deleteGeneSet(java.lang.String)
     */
    protected boolean deleteGeneSet( String classID ) {
        if ( super.deleteGeneSet( classID ) ) {
            this.removeNode( classID );
            return true;
        }
        return false;
    }

    /**
     * Add a new node to the graph under the user-defined group.
     * 
     * @param id
     * @param desc
     */
    public void addNode( String id, String desc ) {
        DirectedGraphNode newNode = new DirectedGraphNode( id, new GOEntry( id, desc, desc, "No aspect defined" ),
                goData.getGraph() );
        GeneSetTreeNode newTreeNode = new GeneSetTreeNode( newNode );
        GeneSetTreeNode userNode = getUserNode();
        ( ( DefaultTreeModel ) this.goTree.getModel() )
                .insertNodeInto( newTreeNode, userNode, userNode.getChildCount() );
        goTree.revalidate();
    }

    public void deleteNode( String id ) {
        assert id != null;
        GeneSetTreeNode node = ( GeneSetTreeNode ) this.findByGeneSetId( id ).getLastPathComponent();
        if ( node.getChildCount() != 0 ) {
            throw new UnsupportedOperationException( "Can't delete node that has children, sorry" );
        }
        ( ( DefaultTreeModel ) this.goTree.getModel() ).removeNodeFromParent( node );
    }

    public void removeNode( String id ) {
        assert id != null;
        log.debug( "Removing tree node " + id );
        TreePath path = this.findByGeneSetId( id );
        if ( path == null ) return;
        GeneSetTreeNode gstn = ( GeneSetTreeNode ) path.getLastPathComponent();
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

    /**
     * 
     */
    private static final String GOODPVAL_ICON = "resources/goldCircle.gif";

    /**
     * 
     */
    private static final String GOODPVAL_GOODCHILD_ICON = "resources/goldCirclePurpleDot.gif";

    private static Log log = LogFactory.getLog( BaseCellRenderer.class.getName() );
    private int currentlySelectedResultSet = -1;
    private GeneAnnotations geneData;
    private final GONames goData;
    private Icon goodChildIcon;
    private Icon regularIcon;
    private Icon goodPvalueIcon;
    private Icon goodPvalueGoodChildIcon;
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
        this.goodPvalueIcon = new ImageIcon( this.getClass().getResource( GOODPVAL_ICON ) );
        goodPvalueGoodChildIcon = new ImageIcon( this.getClass().getResource( GOODPVAL_GOODCHILD_ICON ) );
        this.setOpenIcon( regularIcon );
        this.setLeafIcon( regularIcon );
        this.setClosedIcon( regularIcon );
    }

    /**
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
                + StringUtil.wrap( definition.substring( 0, Math.min( definition.length(), 200 ) ), 50, "<br>" ) );
    }

    /**
     * @param id
     * @param displayedText
     * @return
     */
    private String addResultsFlags( GeneSetTreeNode node, String id, String displayedText ) {
        GeneSetPvalRun res = ( GeneSetPvalRun ) results.get( currentlySelectedResultSet );
        assert res != null;
        assert res.getResults() != null;
        if ( res.getResults().get( id ) != null && ( ( GeneSetResult ) res.getResults().get( id ) ).getPvalue() < 1.0 ) {
            GeneSetResult result = ( GeneSetResult ) res.getResults().get( id );
            double pvalue = result.getPvalue();
            displayedText = displayedText + " -- p = " + nf.format( pvalue ) + " -- effective size = "
                    + result.getEffectiveSize();
            double pvalCorr = result.getCorrectedPvalue();
            Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );
            this.setBackground( bgColor );
            if ( pvalCorr < 0.1 ) {
                if ( node.hasGoodChild() ) {
                    this.setIcon( this.goodPvalueGoodChildIcon );
                } else {
                    this.setIcon( goodPvalueIcon );
                }
            }
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
        this.validate();
        this.revalidate();
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