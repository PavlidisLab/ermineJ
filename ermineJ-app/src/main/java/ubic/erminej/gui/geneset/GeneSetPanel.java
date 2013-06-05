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

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.BrowserLauncher;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetDetails;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.geneset.details.GeneSetDetailsFrame;
import ubic.erminej.gui.geneset.edit.GeneSetWizard;
import ubic.erminej.gui.util.GuiUtil;

/**
 * A generic class to support the display of lists or trees Gene Sets and analysis results.
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class GeneSetPanel extends JScrollPane implements PropertyChangeListener {

    public static final String DELETED = "DELETED";

    public static final double FDR_THRESHOLD_FOR_FILTER = 0.1;

    public static final int MAX_DEFINITION_LENGTH = 300;

    public static final String NOACTION = "NOACTION";

    public static final String RESOURCE_LOCATION = "/ubic/erminej/";

    public static final String RESTORED = "RESTORED";

    public static final Color USER_NODE_COLOR = Color.decode( "#FAFABB" );

    public static final Color USER_NODE_TEXT_COLOR = Color.BLACK;
    static final String AMIGO_URL_BASE = "http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?"
            + "view=details&search_constraint=terms&depth=0&query=";
    static Log log = LogFactory.getLog( GeneSetPanel.class.getName() );

    protected static boolean hideEmpty = true;

    @Override
    public void propertyChange( PropertyChangeEvent evt ) {
        if ( evt.getPropertyName().equals( "hideNonSignificant" ) ) {
            filter( false );
        }
    }

    protected static boolean hideNonCustom = false;
    private static final long serialVersionUID = 1L;
    protected MainFrame mainFrame;

    protected GeneAnnotations geneData;

    protected StatusViewer messenger = new StatusStderr();

    protected Settings settings;

    private Collection<GeneSetPanel> dependentPanels = new HashSet<GeneSetPanel>();

    public GeneSetPanel( Settings settings, MainFrame callingFrame ) {
        this.settings = settings;
        this.mainFrame = callingFrame;
    }

    public void addDependentPanel( GeneSetPanel panel ) {
        if ( panel == this ) return;
        this.dependentPanels.add( panel );
    }

    public abstract void addRun();

    /**
     * @param propagate to the
     */
    public abstract void filter( boolean propagate );

    /**
     * @param e
     */
    public void findInTree( ActionEvent e ) {
        GeneSetPanelPopupMenu sourcePopup = ( GeneSetPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        GeneSetTerm classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        mainFrame.findGeneSetInTree( classID );
    }

    /**
     * Updating, reapplying filters
     */
    public abstract void refreshView();

    /**
     * Do any extra cleanup after a run has been deleted.
     * 
     * @param runToRemove
     */
    public abstract void removeRun( GeneSetPvalRun runToRemove );

    /**
     * Restore view
     */
    public abstract void resetView();

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
    /**
     * @param e
     * @return
     */
    protected GeneSetPanelPopupMenu configurePopup( MouseEvent e ) {

        final GeneSetTerm classID = popupRespondAndGetGeneSet( e );

        GeneSetPanelPopupMenu popup = new GeneSetPanelPopupMenu( classID );

        JMenuItem modMenuItem = new JMenuItem( "Modify this gene set..." );
        modMenuItem.addActionListener( new ModifySetActionAdapter( this ) );

        final JMenuItem visitAmigoMenuItem = new JMenuItem( "Go to GO web site" );
        visitAmigoMenuItem.addActionListener( new UrlActionAdapter( this, AMIGO_URL_BASE ) );

        final JMenuItem deleteGeneSetMenuItem = new JMenuItem( "Delete this gene set" );
        deleteGeneSetMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e1 ) {
                deleteUserGeneSet( classID ); //
            }
        } );

        final JCheckBoxMenuItem hideEmptyMenuItem = new JCheckBoxMenuItem( "Hide empty", hideEmpty );

        hideEmptyMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e1 ) {
                hideEmpty = hideEmptyMenuItem.getState();
                filter( true );
            }

        } );
        popup.add( visitAmigoMenuItem );
        popup.add( modMenuItem );
        popup.add( deleteGeneSetMenuItem );
        popup.add( hideEmptyMenuItem );

        if ( classID == null ) return null;
        if ( classID.isUserDefined() ) {
            modMenuItem.setEnabled( true );
            deleteGeneSetMenuItem.setEnabled( true );
            visitAmigoMenuItem.setEnabled( false ); // won't be a GO term.
        } else {
            modMenuItem.setEnabled( false );
            deleteGeneSetMenuItem.setEnabled( false );
            visitAmigoMenuItem.setEnabled( true );
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

        if ( geneData.deleteUserGeneSet( classID ) ) {
            messenger.showStatus( "Permanantly deleted " + classID );
            this.removedGeneSet( classID );
            return true;
        }
        GuiUtil.error( "Could not delete data on disk for " + classID
                + ". Please delete the file (or part of file) manually (look in " + settings.getUserGeneSetDirectory()
                + ")" );
        return false;

    }

    protected void modMenuItem_actionPerformed( ActionEvent e ) {
        GeneSetPanelPopupMenu sourcePopup = ( GeneSetPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        GeneSetTerm classID = null;
        classID = sourcePopup.getSelectedItem();
        if ( classID == null ) return;
        GeneSetWizard cwiz = new GeneSetWizard( mainFrame, geneData, classID );
        cwiz.showWizard();
    }

    /**
     * Forms a url like url + term.getId().
     * 
     * @param url
     * @param term
     */
    protected void openUrlForGeneSet( String url, GeneSetTerm term ) {

        String clID = term.getId();

        try {
            BrowserLauncher.openURL( url + clID );
        } catch ( Exception e1 ) {
            log.error( e1, e1 );
            GuiUtil.error( "Could not open a web browser window" );
        }
    }

    protected abstract GeneSetTerm popupRespondAndGetGeneSet( MouseEvent e );

    /**
     * Update the view to reflect changes
     * 
     * @param addedTerm
     */
    protected abstract void removedGeneSet( GeneSetTerm addedTerm );

    /**
     * Create the popup window with the visualization for a specific gene set and results.
     * 
     * @param id
     * @param run can be null
     */
    protected void showDetailsForGeneSet( final GeneSetTerm id, final GeneSetPvalRun run ) {

        new Thread() {
            @Override
            public void run() {
                try {
                    if ( id == null ) {
                        log.debug( "Got null geneset id" );
                        return;
                    }

                    GeneAnnotations prunedGeneAnnots = null;
                    GeneScores geneScores = null;
                    if ( run != null ) {
                        prunedGeneAnnots = run.getGeneData();
                    } else if ( StringUtils.isNotBlank( settings.getScoreFile() ) ) {
                        // SLOW, if we don't already have results. Possibly store in a field?
                        geneScores = new GeneScores( settings.getScoreFile(), settings, messenger, geneData );

                        if ( geneScores.getProbeToScoreMap().isEmpty() ) {
                            geneScores = null;
                            prunedGeneAnnots = geneData;
                        } else {
                            prunedGeneAnnots = geneScores.getPrunedGeneAnnotations();
                        }

                    } else {
                        prunedGeneAnnots = geneData;
                    }

                    GeneSet geneSet = prunedGeneAnnots.getGeneSet( id );
                    if ( geneSet == null ) {
                        messenger.showWarning( "No gene set with ID " + id + " was available for viewing." );
                        return; // aspect etc.
                    }

                    int numGenes = geneSet.getGenes().size();
                    if ( numGenes > GeneSetDetailsFrame.MAX_GENES_FOR_DETAIL_VIEWING ) {
                        messenger.showError( StringUtils.abbreviate( id.getId(), 30 )
                                + " has too many genes to display (" + numGenes + ", max is set to "
                                + GeneSetDetailsFrame.MAX_GENES_FOR_DETAIL_VIEWING + ")" );
                        return;
                    }

                    messenger.showStatus( "Viewing details of data for " + id + " ..." );

                    log.debug( "Request for details of gene set: " + id + ", run: " + run );
                    if ( !prunedGeneAnnots.hasGeneSet( id ) ) {
                        mainFrame.getStatusMessenger().showWarning( id + " is not available for viewing in your data." );
                        return;
                    }

                    GeneSetResult result = null; // might stay this way.
                    if ( run != null ) {
                        result = run.getResults().get( id );
                    }

                    // assert geneScores.getGeneToScoreMap().keySet().containsAll( geneSet.getGenes() );

                    GeneSetDetails details = new GeneSetDetails( id, result, prunedGeneAnnots, settings,
                            geneScores /* could be null */, messenger );

                    GeneSetDetailsFrame detailsFrame = new GeneSetDetailsFrame( details, messenger );
                    detailsFrame.setVisible( true );
                    messenger.clear();
                } catch ( Exception ex ) {
                    GuiUtil.error( "There was an unexpected error while trying to display the gene "
                            + "set details.\nSee the log file for details.\nThe summary message was:\n"
                            + ex.getMessage() );
                    messenger
                            .showError( "There was an unexpected error while trying to display the gene set details.\n"
                                    + "See the log file for details.\nThe summary message was:\n" + ex.getMessage() );
                }
            }
        }.start();
    }

    protected abstract void showPopupMenu( MouseEvent e );
}

class ModifySetActionAdapter implements java.awt.event.ActionListener {
    GeneSetPanel adaptee;

    ModifySetActionAdapter( GeneSetPanel adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.modMenuItem_actionPerformed( e );
    }
}

class UrlActionAdapter implements java.awt.event.ActionListener {
    private GeneSetPanel adaptee;
    private String urlBase;

    UrlActionAdapter( GeneSetPanel adaptee, String urlBase ) {
        this.adaptee = adaptee;
        this.urlBase = urlBase;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        GeneSetPanelPopupMenu sourcePopup = ( GeneSetPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        GeneSetTerm classID = sourcePopup.getSelectedItem();
        adaptee.openUrlForGeneSet( urlBase, classID );
    }
}
