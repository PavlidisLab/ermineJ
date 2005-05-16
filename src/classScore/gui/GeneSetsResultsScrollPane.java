package classScore.gui;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.GuiUtil;
import baseCode.util.BrowserLauncher;

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
public abstract class GeneSetsResultsScrollPane extends JScrollPane {
    private static Log log = LogFactory.getLog( GeneSetsResultsScrollPane.class.getName() );
    protected GeneAnnotations geneData;
    protected Settings settings;
    protected GONames goData;
    protected List results;
    protected int selectedRun;
    protected GeneSetScoreFrame callingframe;
    static final String AMIGO_URL_BASE = "http://www.godatabase.org/cgi-bin/amigo/go.cgi?"
            + "view=details&search_constraint=terms&depth=0&query=";

    protected void showDetailsForGeneSet( int runnum, String id ) {
        log.debug( "Request for details of gene set: " + id );
        GeneSetDetails details = new GeneSetDetails( goData, geneData, settings, id );
        if ( runnum < 0 ) {
            details.show();
        } else {
            GeneSetPvalRun run = ( GeneSetPvalRun ) results.get( runnum );
            GeneSetResult res = ( GeneSetResult ) run.getResults().get( id );
            details.show( res, run.getGeneScores() );
        }
    }

    public abstract void addRun();

    public abstract void addedNewGeneSet();

    public abstract void resetView();

    public void setSelectedRun( int selectedRun ) {
        this.selectedRun = selectedRun;
    }

   

    protected void modMenuItem_actionPerformed( ActionEvent e ) {
        OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        String classID = null;
        classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        GeneSetWizard cwiz = new GeneSetWizard( callingframe, geneData, goData, classID );
        cwiz.showWizard();
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
}

class OutputPanelPopupMenu extends JPopupMenu {
    Point popupPoint;
    String selectedItem = null;

    /**
     * @return Returns the selectedItem.
     */
    public String getSelectedItem() {
        return this.selectedItem;
    }

    /**
     * @param selectedItem The selectedItem to set.
     */
    public void setSelectedItem( String selectedItem ) {
        this.selectedItem = selectedItem;
    }

    public Point getPoint() {
        return popupPoint;
    }

    public void setPoint( Point point ) {
        popupPoint = point;
    }
}

class OutputPanel_modMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetsResultsScrollPane adaptee;

    OutputPanel_modMenuItem_actionAdapter( GeneSetsResultsScrollPane adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.modMenuItem_actionPerformed( e );
    }
}

class OutputPanel_htmlMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetsResultsScrollPane adaptee;

    OutputPanel_htmlMenuItem_actionAdapter( GeneSetsResultsScrollPane adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.htmlMenuItem_actionPerformed( e );
    }
}
