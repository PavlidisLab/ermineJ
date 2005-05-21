package classScore.gui;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

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

    public GeneSetPanel( Settings settings, List results, GeneSetScoreFrame callingFrame ) {
        this.settings = settings;
        this.results = results;
        this.callingFrame = callingFrame;
    }

    public abstract void addedNewGeneSet();

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
            callingFrame.getStatusMessenger().setError( id + " is not available for viewing in your data." );
            return;
        }
        GeneSetDetails details = new GeneSetDetails( goData, geneData, settings, id );
        if ( runnum < 0 ) {
            details.show();
        } else {
            GeneSetPvalRun run = ( GeneSetPvalRun ) results.get( runnum );
            GeneSetResult res = ( GeneSetResult ) run.getResults().get( id );
            details.show( res, run.getGeneScores() );
        }
    }

    protected boolean deleteGeneSet( String classID ) {
        if ( classID == null ) return false;
        if ( !goData.isUserDefined( classID ) ) return false;

        int yesno = JOptionPane.showConfirmDialog( this, "Are you sure you want to permanently delete \"" + classID
                + "\"?", "Confirm", JOptionPane.YES_NO_OPTION );

        if ( yesno == JOptionPane.NO_OPTION ) return false;
        geneData.removeClassFromMaps( classID );
        goData.deleteGeneSet( classID );
        callingFrame.deleteUserGeneSet( classID );
        return true;
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
