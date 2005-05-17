package classScore.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import classScore.Settings;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.graph.DirectedGraphNode;
import baseCode.dataStructure.graph.GraphNode;
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
    private JTree goTree;
    private MutableTreeNode userRoot;
    protected String currentlySelectedGeneSet;

    public GeneSetTreePanel( GeneSetScoreFrame callingFrame, List results, Settings settings ) {
        super( settings, results, callingFrame );
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
        this.goTree = goData.getGraph().treeView();
        this.goData = goData;
        addUserNode();
        setRenderer();
        goTree.setRootVisible( true );
        MouseListener popupListener = configurePopupMenu();
        goTree.addMouseListener( popupListener );
        goTree.addMouseListener( new GeneSetTreePanel_mouseListener( this ) );
        goTree.addTreeSelectionListener( new TreeSelectionListener() {
            public void valueChanged( TreeSelectionEvent e ) {
                log.debug( "value changed" );
                TreePath path = e.getPath();
                // FIXME - user classes causes npe
                currentlySelectedGeneSet = ( String ) ( ( GraphNode ) ( ( DefaultMutableTreeNode ) path
                        .getLastPathComponent() ).getUserObject() ).getKey();
            }
        } );
        this.getViewport().add( goTree );

        goTree.setVisible( true );
        goTree.revalidate();
    }

    protected MouseListener configurePopupMenu() {
        OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
        JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );
        JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );
        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );
        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        MouseListener popupListener = new GeneSetTree_PopupListener( popup );
        return popupListener;
    }

    /**
     * 
     */
    private void addUserNode() {
        log.debug( "Adding user node" );
        userRoot = new DefaultMutableTreeNode( "User-defined" );
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode ) goTree.getModel().getRoot();
        root.add( userRoot );
        goTree = new JTree( root );
        goTree.revalidate();
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

        CellRenderer rend = new CellRenderer( goData, geneData, results );
        // DefaultTreeCellRenderer rend = new DefaultTreeCellRenderer();
        rend.setOpenIcon( openIcon );
        rend.setLeafIcon( leafIcon );
        rend.setClosedIcon( closedIcon );
        this.goTree.setCellRenderer( rend );
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

    /*
     * (non-Javadoc)
     * 
     * @see classScore.gui.GeneSetsResultsScrollPane#addRun()
     */
    public void addRun() {
        // TODO Auto-generated method stub

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
     * @see classScore.gui.GeneSetsResultsScrollPane#resetView()
     */
    public void resetView() {
        // TODO Auto-generated method stub

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

class GeneSetTree_PopupListener extends MouseAdapter {
    OutputPanelPopupMenu popup;
    private static Log log = LogFactory.getLog( GeneSetTree_PopupListener.class.getName() );

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

            DefaultMutableTreeNode selectedNode = ( DefaultMutableTreeNode ) path.getLastPathComponent();
            String id = ( String ) ( ( GraphNode ) selectedNode.getUserObject() ).getKey();
            popup.show( e.getComponent(), e.getX(), e.getY() );
            popup.setSelectedItem( id );
        }
    }
}

class CellRenderer extends DefaultTreeCellRenderer {

    private final List results;
    private boolean selected;
    private final GONames goData;
    private Format nf = new Format( "%g" ); // for the gene set p value.
    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score
    private GeneAnnotations geneData;
    private Font italic = new Font( "SansSerif", Font.ITALIC, 11 );
    private Font plain = new Font( "SansSerif", Font.PLAIN, 11 );

    public CellRenderer( GONames goData, GeneAnnotations geneData, List results ) {
        super();
        this.results = results;
        this.goData = goData;
        this.geneData = geneData;
        nff.setMaximumFractionDigits( 4 );
    }

    /**
     * TODO
     * <ul>
     * <li>Make non-searched-for nodes greyed out
     * <li>Make customized nodes pink.
     * <li>Add the pvalue and size to each node
     * <li>Color node by pvalue.
     * <li>Nodes at higher levels that are not significant, but which have significant classes under them, should be
     * shown in contrasting color.
     * </ul>
     * 
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean,
     *      boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );
        DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) value;
        String name;
        String id = "";
        if ( node.getUserObject() instanceof DirectedGraphNode ) {
            DirectedGraphNode nodeObj = ( DirectedGraphNode ) node.getUserObject();
            id = ( String ) nodeObj.getKey();
            name = nodeObj.toString();
        } else {
            Object nodeObj = node.getUserObject();
            name = nodeObj.toString();
        }
        this.selected = selected;
        this.hasFocus = hasFocus;

        setOpaque( true );
        if ( this.selected ) {
            this.setBackground( Color.LIGHT_GRAY );
        } else {
            this.setBackground( Color.WHITE );
        }

        if ( !geneData.getGeneSetToProbeMap().containsKey( id ) ) {
            this.setFont( italic );
            this.setForeground( Color.GRAY );
            if ( !hasUsableChildren( node ) ) {
                this.setEnabled( false );
            }
        } else {
            setText( name + " -- " + ( ( Collection ) geneData.getGeneSetToProbeMap().get( id ) ).size() + " probes, "
                    + ( ( Collection ) geneData.getGeneSetToGeneMap().get( id ) ).size() + " genes" );
            this.setFont( plain );
            this.setForeground( Color.BLACK );
        }

        return this;
    }

    /**
     * @return
     */
    private boolean hasUsableChildren( DefaultMutableTreeNode node ) {
        for ( int i = 0; i < node.getChildCount(); i++ ) {
            DefaultMutableTreeNode child = ( DefaultMutableTreeNode ) node.getChildAt( i );
            String id = ( String ) ( ( DirectedGraphNode ) child.getUserObject() ).getKey();
            if ( geneData.getGeneSetToProbeMap().containsKey( id ) ) {
                return true;
            }
            return hasUsableChildren( child );
        }
        return false;
    }
}