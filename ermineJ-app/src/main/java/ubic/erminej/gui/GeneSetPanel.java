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

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.BrowserLauncher;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.GeneSetPvalRun;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.gui.geneset.GeneSetDetails;

/**
 * A generic class to support the display of Gene Set results. Could be made even more generic.
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class GeneSetPanel extends JScrollPane {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    static Log log = LogFactory.getLog( GeneSetPanel.class.getName() );
    static final String AMIGO_URL_BASE = "http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?"
            + "view=details&search_constraint=terms&depth=0&query=";
    protected GeneSetScoreFrame callingFrame;
    protected GeneAnnotations geneData;
    protected GONames goData;
    protected StatusViewer messenger;
    protected List<GeneSetPvalRun> results;
    protected int selectedRun;
    protected Settings settings;
    protected OutputPanelPopupMenu popup;
    public static final String NOACTION = "NOACTION";
    public static final String RESTORED = "RESTORED";
    public static final String DELETED = "DELETED";
    public static final int MAX_DEFINITION_LENGTH = 200;

    public GeneSetPanel( Settings settings, List<GeneSetPvalRun> results, GeneSetScoreFrame callingFrame ) {
        this.settings = settings;
        this.results = results;
        this.callingFrame = callingFrame;
    }

    public abstract void addedNewGeneSet();

    protected abstract String popupRespondAndGetGeneSet( MouseEvent e );

    public void addInitialData( GONames initialGoData ) {
        assert callingFrame.getOriginalGeneData() != null : "Gene data is still null";
        assert initialGoData != null : "GO data is still null";
        this.geneData = callingFrame.getOriginalGeneData();
        this.goData = initialGoData;
    }

    public abstract void addRun();

    /**
     * @param e
     */
    public void findInTreeMenuItem_actionAdapter( ActionEvent e ) {
        OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        String classID = sourcePopup.getSelectedItem();
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
            BrowserLauncher.openURL( AMIGO_URL_BASE + classID );
        } catch ( Exception e1 ) {
            GuiUtil.error( "Could not open a web browser window" );
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

    /**
     * Create the popup window with the visualization for a specific gene set.
     * 
     * @param runnum
     * @param id
     * @throws IllegalStateException
     */
    protected void showDetailsForGeneSet( final int runnum, final String id ) throws IllegalStateException {
        if ( messenger != null ) messenger.showStatus( "Viewing data for " + id + "..." );

        new Thread() {
            @Override
            public void run() {
                try {
                    if ( id == null ) {
                        log.debug( "Got null geneset id" );
                        return;
                    }
                    log.debug( "Request for details of gene set: " + id + ", run number " + runnum );
                    if ( !geneData.hasGeneSet( id ) ) {
                        callingFrame.getStatusMessenger()
                                .showError( id + " is not available for viewing in your data." );
                        return;
                    }
                    GeneSetDetails details = new GeneSetDetails( messenger, goData, geneData, settings, id );
                    if ( runnum < 0 ) {
                        details.show();
                    } else {
                        GeneSetPvalRun run = results.get( runnum );
                        GeneSetResult res = run.getResults().get( id );
                        details.show( run.getName(), res, run.getGeneScores() );
                    }
                    if ( messenger != null ) messenger.clear();
                } catch ( Exception ex ) {
                    GuiUtil
                            .error( "There was an unexpected error while trying to display the gene set details.\nSee the log file for details.\nThe summary message was:\n"
                                    + ex.getMessage() );
                    log.error( ex, ex );
                    if ( messenger != null ) messenger.clear();
                }
            }
        }.start();
    }

    /**
     * @param classID
     * @return
     */
    protected String deleteAndResetGeneSet( String classID ) {
        if ( classID == null ) return NOACTION;
        if ( !goData.isUserDefined( classID ) ) return NOACTION;

        if ( callingFrame.userOverWrote( classID ) ) {
            int yesno = JOptionPane.showConfirmDialog( this, "Are you sure you want to restore the original \""
                    + classID + "\" and delete the file?", "Confirm", JOptionPane.YES_NO_OPTION );
            if ( yesno == JOptionPane.NO_OPTION ) return NOACTION;
            geneData.restoreGeneSet( classID );
            goData.resetGeneSet( classID );
            callingFrame.restoreUserGeneSet( classID );
            callingFrame.deleteUserGeneSetFile( classID );
            return DELETED;
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

    protected String resetGeneSet( String classID ) {
        if ( classID == null ) return NOACTION;
        if ( !goData.isUserDefined( classID ) ) return NOACTION;
        int yesno = JOptionPane.showConfirmDialog( this, "Are you sure you want to restore the original \"" + classID
                + "\"?", "Confirm", JOptionPane.YES_NO_OPTION );
        if ( yesno == JOptionPane.NO_OPTION ) return NOACTION;
        geneData.restoreGeneSet( classID );
        goData.resetGeneSet( classID );
        callingFrame.restoreUserGeneSet( classID );
        return RESTORED;
    }

    protected MouseListener configurePopupMenu() {
        popup = new OutputPanelPopupMenu();
        JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );
        final JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );
        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );

        final JMenuItem deleteGeneSetMenuItem = new JMenuItem( "Delete this gene set" ); // reset and always delete
        // user version.
        deleteGeneSetMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                String classID = null;
                classID = sourcePopup.getSelectedItem();
                deleteAndResetGeneSet( classID );
            }
        } );

        final JMenuItem restoreGeneSetMenuItem = new JMenuItem( "Reset this gene set" ); // reset without deleting.
        restoreGeneSetMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                String classID = null;
                classID = sourcePopup.getSelectedItem();
                resetGeneSet( classID );
            }
        } );

        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        popup.add( deleteGeneSetMenuItem );
        popup.add( restoreGeneSetMenuItem );

        MouseListener popupListener = new MouseAdapter() {
            @Override
            public void mousePressed( MouseEvent e ) {
                maybeShowPopup( e );
            }

            @Override
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
                        deleteGeneSetMenuItem.setText( "Reset and delete customized version" );
                        restoreGeneSetMenuItem.setEnabled( true );
                    } else {
                        deleteGeneSetMenuItem.setText( "Delete this gene set" );
                        restoreGeneSetMenuItem.setEnabled( false );
                    }

                    popup.show( e.getComponent(), e.getX(), e.getY() );
                    popup.setSelectedItem( classID );

                }
            }
        };

        return popupListener;
    }

    /**
     * @param messenger
     */
    public void setMessenger( StatusViewer messenger ) {
        this.messenger = messenger;
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
    /**
     * 
     */
    private static final long serialVersionUID = 8600729411722169908L;
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
