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
package ubic.erminej.gui.geneset;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.BrowserLauncher;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;

import ubic.erminej.Settings;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSetTerms;
import ubic.erminej.data.UserDefinedGeneSetManager;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.geneset.details.GeneSetDetails;
import ubic.erminej.gui.geneset.wiz.GeneSetWizard;
import ubic.erminej.gui.util.GuiUtil;

/**
 * A generic class to support the display of lists or trees Gene Sets and analysis results.
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class GeneSetPanel extends JScrollPane {

    public static final double FDR_THRESHOLD_FOR_FILTER = 0.1;
    private static final long serialVersionUID = 1L;
    static Log log = LogFactory.getLog( GeneSetPanel.class.getName() );
    static final String AMIGO_URL_BASE = "http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?"
            + "view=details&search_constraint=terms&depth=0&query=";
    protected MainFrame callingFrame;
    protected GeneAnnotations geneData;
    protected StatusViewer messenger = new StatusStderr();
    protected List<GeneSetPvalRun> results = new ArrayList<GeneSetPvalRun>();

    protected Settings settings;
    public static final String NOACTION = "NOACTION";
    public static final String RESTORED = "RESTORED";
    public static final String DELETED = "DELETED";
    public static final int MAX_DEFINITION_LENGTH = 200;

    protected static boolean hideEmpty = true;
    protected static boolean hideInsignificant = false;
    protected static boolean hideRedundant = true;
    protected static boolean hideNonCustom = false;

    private Collection<GeneSetPanel> dependentPanels = new HashSet<GeneSetPanel>();

    public GeneSetPanel( Settings settings, List<GeneSetPvalRun> results, MainFrame callingFrame ) {
        this.settings = settings;
        this.results = results;
        this.callingFrame = callingFrame;
    }

    public void addDependentPanel( GeneSetPanel panel ) {
        if ( panel == this ) return;
        this.dependentPanels.add( panel );
    }

    public abstract void addRun();

    /**
     * @param e
     */
    public void findInTreeMenuItem_actionAdapter( ActionEvent e ) {
        GeneSetPanelPopupMenu sourcePopup = ( GeneSetPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        GeneSetTerm classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        callingFrame.findGeneSetInTree( classID );
    }

    public GeneSetPvalRun getCurrentResultSet() {
        int i = callingFrame.getCurrentResultSet();
        if ( i < 0 ) return null;
        return this.results.get( i );
    }

    /**
     * Restore view
     */
    public abstract void resetView();

    /**
     * Updating, reapplying filters
     */
    public abstract void refreshView();

    /**
     * @param messenger
     */
    public void setMessenger( StatusViewer messenger ) {
        if ( messenger == null ) return;
        this.messenger = messenger;
    }

    /**
     * @param addedTerm
     */
    protected abstract void addedGeneSet( GeneSetTerm addedTerm );

    /**
     * Configure the base popup common to any compoment showing gene sets.
     * 
     * @param e
     * @return popup
     */
    protected GeneSetPanelPopupMenu configurePopup( MouseEvent e ) {

        final GeneSetTerm classID = popupRespondAndGetGeneSet( e );

        GeneSetPanelPopupMenu popup = new GeneSetPanelPopupMenu( classID );

        JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );

        final JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );

        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );

        final JMenuItem deleteGeneSetMenuItem = new JMenuItem( "Delete this gene set" );
        deleteGeneSetMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                deleteUserGeneSet( classID ); //
            }
        } );

        final JCheckBoxMenuItem hideEmptyMenuItem = new JCheckBoxMenuItem( "Hide empty", hideEmpty );
        final JCheckBoxMenuItem hideInsig = new JCheckBoxMenuItem( "Hide non-significant", hideInsignificant );
        final JCheckBoxMenuItem hideRedund = new JCheckBoxMenuItem( "Hide redundant", hideRedundant );

        hideEmptyMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                hideEmpty = hideEmptyMenuItem.getState();
                filter( true );
            }

        } );

        hideInsig.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                hideInsignificant = hideInsig.getState();
                filter( true );
            }

        } );

        hideRedund.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                hideRedundant = hideRedund.getState();
                filter( true );
            }

        } );

        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        popup.add( deleteGeneSetMenuItem );
        popup.add( hideEmptyMenuItem );
        popup.add( hideRedund );
        popup.add( hideInsig );

        if ( classID == null ) return null;
        if ( geneData.getUserDefined().contains( classID ) ) {
            // user-defineda
            assert classID.isUserDefined();
            modMenuItem.setEnabled( true );
            deleteGeneSetMenuItem.setEnabled( true );
            htmlMenuItem.setEnabled( false ); // won't be a GO term.
        } else {
            modMenuItem.setEnabled( false );
            deleteGeneSetMenuItem.setEnabled( false );
            htmlMenuItem.setEnabled( true );
        }

        return popup;

    }

    /**
     * @return listener for popup on a gene set.
     */
    protected MouseListener configurePopupListener() {

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
                    showPopupMenu( e );
                }
            }
        };

        return popupListener;
    }

    /**
     * @param classID
     * @return
     */
    protected boolean deleteUserGeneSet( GeneSetTerm classID ) {
        if ( classID == null ) return false;
        if ( !classID.isUserDefined() ) return false;

        int yesno = JOptionPane.showConfirmDialog( this, "Are you sure you want to delete \"" + classID + "\"?",
                "Confirm", JOptionPane.YES_NO_OPTION );
        if ( yesno == JOptionPane.NO_OPTION ) return false;

        if ( UserDefinedGeneSetManager.deleteUserGeneSet( classID, messenger ) ) {
            messenger.showStatus( "Permanantly deleted " + classID );
            this.removedGeneSet( classID );
            return true;
        }
        GuiUtil.error( "Could not delete data on disk for " + classID
                + ". Please delete the file (or part of file) manually from " + settings.getUserGeneSetDirectory() );
        return false;

    }

    public abstract void filter( boolean propagate );

    protected void htmlMenuItem_actionPerformed( ActionEvent e ) {
        GeneSetPanelPopupMenu sourcePopup = ( GeneSetPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        GeneSetTerm classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        // create the URL and show it

        String clID = classID.getId();

        try {
            BrowserLauncher.openURL( AMIGO_URL_BASE + clID );
        } catch ( Exception e1 ) {
            log.error( e1, e1 );
            GuiUtil.error( "Could not open a web browser window" );
        }
    }

    protected void modMenuItem_actionPerformed( ActionEvent e ) {
        GeneSetPanelPopupMenu sourcePopup = ( GeneSetPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        GeneSetTerm classID = null;
        classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        GeneSetWizard cwiz = new GeneSetWizard( callingFrame, geneData, classID );
        cwiz.showWizard();
    }

    protected abstract GeneSetTerm popupRespondAndGetGeneSet( MouseEvent e );

    /**
     * Update the view to reflect changes
     * 
     * @param addedTerm
     */
    protected abstract void removedGeneSet( GeneSetTerm addedTerm );

    /**
     * Create the popup window with the visualization for a specific gene set.
     * 
     * @param runnum
     * @param id
     * @throws IllegalStateException
     */
    protected void showDetailsForGeneSet( final GeneSetPvalRun run, final GeneSetTerm id ) throws IllegalStateException {
        messenger.showStatus( "Viewing data for " + id + "..." );

        new Thread() {
            @Override
            public void run() {
                try {
                    if ( id == null ) {
                        log.debug( "Got null geneset id" );
                        return;
                    }
                    log.debug( "Request for details of gene set: " + id + ", run: " + run );
                    if ( !geneData.hasGeneSet( id ) ) {
                        callingFrame.getStatusMessenger()
                                .showError( id + " is not available for viewing in your data." );
                        return;
                    }
                    GeneSetDetails details = new GeneSetDetails( messenger, geneData, settings, id );
                    if ( run == null ) {
                        details.show();
                    } else {
                        GeneSetResult res = run.getResults().get( id );
                        details.show( run.getName(), res, run.getGeneScores() );
                    }
                    messenger.clear();
                } catch ( Exception ex ) {
                    GuiUtil
                            .error( "There was an unexpected error while trying to display the gene set details.\nSee the log file for details.\nThe summary message was:\n"
                                    + ex.getMessage() );
                    log.error( ex, ex );
                    messenger.clear();
                }
            }
        }.start();
    }

    protected abstract void showPopupMenu( MouseEvent e );
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
