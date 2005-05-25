package classScore.gui;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.GuiUtil;
import baseCode.util.BrowserLauncher;
import baseCode.util.StatusViewer;
import classScore.GeneSetPvalRun;
import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.gui.geneSet.GeneSetDetails;

/**
 * A generic class to support the display of Gene Set results. Could be made even more generic.
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class GeneSetPanel extends JScrollPane {
    private static Log log = LogFactory.getLog( GeneSetPanel.class.getName() );
    static final String AMIGO_URL_BASE = "http://www.godatabase.org/cgi-bin/amigo/go.cgi?"
            + "view=details&search_constraint=terms&depth=0&query=";
    protected GeneSetScoreFrame callingFrame;
    protected GeneAnnotations geneData;
    protected GONames goData;
    protected StatusViewer messenger;
    protected List results;
    protected int selectedRun;
    protected Settings settings;
    protected OutputPanelPopupMenu popup;
    public static final String NOACTION = "NOACTION";
    public static final String RESTORED = "RESTORED";
    public static final String DELETED = "DELETED";

    public GeneSetPanel( Settings settings, List results, GeneSetScoreFrame callingFrame ) {
        this.settings = settings;
        this.results = results;
        this.callingFrame = callingFrame;
    }

    public abstract void addedNewGeneSet();

    protected abstract String popupRespondAndGetGeneSet( MouseEvent e );

    public void addInitialData( GONames goData ) {
        assert callingFrame.getOriginalGeneData() != null : "Gene data is still null";
        assert goData != null : "GO data is still null";
        this.geneData = callingFrame.getOriginalGeneData();
        this.goData = goData;
    }

    public abstract void addRun();

    /**
     * @param e
     */
    public void findInTreeMenuItem_actionAdapter( ActionEvent e ) {
        OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        String classID = null;
        classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        callingFrame.findGeneSetInTree( classID );
    }

    public abstract void resetView();

    public void setSelectedRun( int selectedRun ) {
        this.selectedRun = selectedRun;
    }

    protected void htmlMenuItem_actionPerformed( ActionEvent e ) {
        OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        String classID = null;
        classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        // create the URL and show it
        try {
            // new JWebBrowser( URL );
            BrowserLauncher.openURL( AMIGO_URL_BASE + classID );
        } catch ( IOException e1 ) {
            GuiUtil.error( "Could not open a web browser window." );
        }
    }

    protected void modMenuItem_actionPerformed( ActionEvent e ) {
        OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        String classID = null;
        classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        GeneSetWizard cwiz = new GeneSetWizard( callingFrame, geneData, goData, classID );
        cwiz.showWizard();
    }

    protected void showDetailsForGeneSet( int runnum, String id ) {
        if ( id == null ) {
            log.debug( "Got null geneset id" );
            return;
        }
        log.debug( "Request for details of gene set: " + id );
        if ( !geneData.getGeneSetToProbeMap().containsKey( id ) ) {
            callingFrame.getStatusMessenger().showError( id + " is not available for viewing in your data." );
            return;
        }
        GeneSetDetails details = new GeneSetDetails( this.callingFrame.getStatusMessenger(), goData, geneData,
                settings, id );
        if ( runnum < 0 ) {
            details.show();
        } else {
            GeneSetPvalRun run = ( GeneSetPvalRun ) results.get( runnum );
            GeneSetResult res = ( GeneSetResult ) run.getResults().get( id );
            details.show( run.getName(), res, run.getGeneScores() );
        }
    }

    /**
     * @param classID
     * @return
     */
    protected String deleteOrResetGeneSet( String classID ) {
        if ( classID == null ) return NOACTION;
        if ( !goData.isUserDefined( classID ) ) return NOACTION;

        if ( callingFrame.userOverWrote( classID ) ) {
            int yesno = JOptionPane.showConfirmDialog( this, "Are you sure you want to restore the original \""
                    + classID + "\"?", "Confirm", JOptionPane.YES_NO_OPTION );
            if ( yesno == JOptionPane.NO_OPTION ) return NOACTION;
            geneData.restoreGeneSet( classID );
            callingFrame.restoreUserGeneSet( classID );
            return RESTORED;
        }
        // else
        int yesno = JOptionPane.showConfirmDialog( this, "Are you sure you want to delete \"" + classID + "\"?",
                "Confirm", JOptionPane.YES_NO_OPTION );

        if ( yesno == JOptionPane.NO_OPTION ) return NOACTION;
        geneData.removeClassFromMaps( classID );
        goData.deleteGeneSet( classID );
        callingFrame.deleteUserGeneSet( classID );
        return DELETED;
    }

    protected MouseListener configurePopupMenu() {
        popup = new OutputPanelPopupMenu();
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
                deleteOrResetGeneSet( classID );
            }
        } );

        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        popup.add( deleteGeneSetMenuItem );

        MouseListener popupListener = new MouseAdapter() {
            public void mousePressed( MouseEvent e ) {
                maybeShowPopup( e );
            }

            public void mouseReleased( MouseEvent e ) {
                maybeShowPopup( e );
            }

            private void maybeShowPopup( MouseEvent e ) {
                if ( e.isPopupTrigger() ) {
                    String classID = popupRespondAndGetGeneSet( e );
                    if ( !goData.getUserDefinedGeneSets().contains( classID ) ) {
                        deleteGeneSetMenuItem.setEnabled( false );
                    } else {
                        deleteGeneSetMenuItem.setEnabled( true );
                    }

                    if ( callingFrame.userOverWrote( classID ) ) {
                        deleteGeneSetMenuItem.setText( "Reset this gene set" );
                    } else {
                        deleteGeneSetMenuItem.setText( "Delete this gene set" );
                    }

                    popup.show( e.getComponent(), e.getX(), e.getY() );
                    popup.setSelectedItem( classID );

                }
            }
        };

        return popupListener;
    }
}

class OutputPanel_htmlMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetPanel adaptee;

    OutputPanel_htmlMenuItem_actionAdapter( GeneSetPanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.htmlMenuItem_actionPerformed( e );
    }
}

class OutputPanel_modMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetPanel adaptee;

    OutputPanel_modMenuItem_actionAdapter( GeneSetPanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.modMenuItem_actionPerformed( e );
    }
}

class OutputPanelPopupMenu extends JPopupMenu {
    Point popupPoint;
    String selectedItem = null;
    GONames goData = null;

    public Point getPoint() {
        return popupPoint;
    }

    /**
     * @return Returns the selectedItem.
     */
    public String getSelectedItem() {
        return this.selectedItem;
    }

    public void setPoint( Point point ) {
        popupPoint = point;
    }

    /**
     * @param selectedItem The selectedItem to set.
     */
    public void setSelectedItem( String selectedItem ) {
        this.selectedItem = selectedItem;
    }

    /**
     * @return Returns the goData.
     */
    public GONames getGoData() {
        return this.goData;
    }
}
