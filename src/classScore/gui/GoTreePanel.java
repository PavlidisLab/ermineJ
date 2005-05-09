package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import baseCode.bio.geneset.GONames;
import baseCode.dataStructure.graph.DirectedGraphNode;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GoTreePanel extends JScrollPane {

    private JTree goTree;
    private final LinkedList results;
    private final GeneSetScoreFrame callingframe;
    private GONames goData;

    public GoTreePanel( GeneSetScoreFrame callingframe, LinkedList results ) {
        this.callingframe = callingframe;
        this.results = results;

    }

    public void initialize( GONames goData ) {
        this.goTree = goData.getGraph().treeView();
        this.goData = goData;
        setRenderer();
        goTree.addMouseListener( new GoTreePanelMouseAdapter( this ) );
        this.getViewport().add( goTree );

        goTree.setVisible( true );
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

        // CellRenderer rend = new CellRenderer( results, goData );
        DefaultTreeCellRenderer rend = new DefaultTreeCellRenderer();
        rend.setOpenIcon( openIcon );
        rend.setLeafIcon( leafIcon );
        rend.setClosedIcon( closedIcon );
        this.goTree.setCellRenderer( rend );
    }

    /**
     * @param e
     */
    public void mouseReleased( MouseEvent e ) {

    }

}

class GoTreePanelMouseAdapter extends MouseAdapter {
    GoTreePanel adaptee;

    /**
     * @param panel
     */
    public GoTreePanelMouseAdapter( GoTreePanel panel ) {
        adaptee = panel;
    }

    public void mouseReleased( MouseEvent e ) {
        adaptee.mouseReleased( e );
    }

}

class CellRenderer extends DefaultTreeCellRenderer {

    private final LinkedList results;
    private boolean selected;
    private final GONames goData;

    public CellRenderer( LinkedList results, GONames goData ) {
        this.results = results;
        this.goData = goData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean,
     *      boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) value;
        DirectedGraphNode nodeObj = ( DirectedGraphNode ) node.getUserObject();
        String name = nodeObj.toString();
        this.selected = selected;
        setText( name );

        if ( goData.newSet( ( String ) nodeObj.getKey() ) ) {
            this.setBackground( Colors.PINK );
        }

        if ( leaf ) {
            this.setBackground( Color.LIGHT_GRAY );
        }

        return this;
    }

}