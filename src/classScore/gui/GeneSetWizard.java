package classScore.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.GuiUtil;
import baseCode.gui.Wizard;
import classScore.Settings;
import classScore.data.UserDefinedGeneSetManager;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @version $Id$
 */

public class GeneSetWizard extends Wizard {
    private static Log log = LogFactory.getLog( GeneSetWizard.class.getName() );
    Settings settings;
    GeneAnnotations geneData;
    GONames goData;
    GeneSetWizardStep1 step1; // case 1 (manual creating) and case 2 (new from file)
    GeneSetWizardStep1A step1A; // case 3 (modifying existing)
    GeneSetWizardStep2 step2; // step 2 for cases 1-3 and step 1 for case 4
    GeneSetWizardStep3 step3;

    int step;
    boolean makenew;
    boolean nostep1 = false;
    UserDefinedGeneSetManager newGeneSet;
    UserDefinedGeneSetManager oldGeneSet;
    String geneSetId;

    /**
     * Use this constructor to let the user choose which gene set to look at or to make a new one.
     * 
     * @param callingframe
     * @param geneData
     * @param goData
     * @param makenew if true, the user is asked to create a new gene set. If false, they are asked to choose from a
     *        list of existing gene sets.
     */
    public GeneSetWizard( GeneSetScoreFrame callingframe, GeneAnnotations geneData, GONames goData, boolean makenew ) {
        super( callingframe, 550, 350 );
        this.callingframe = callingframe;
        this.settings = callingframe.getSettings();
        this.geneData = geneData;
        this.goData = goData;
        this.makenew = makenew;
        newGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );
        oldGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );

        geneData.resetSelectedProbes();
        step = 1;
        if ( makenew ) {
            this.setTitle( "Define New Gene Set - Step 1 of 3" );
            step1 = new GeneSetWizardStep1( this, settings );
            this.addStep( step1, true );
        } else {
            this.setTitle( "Modify Gene Set - Step 1 of 3" );
            step1A = new GeneSetWizardStep1A( this, geneData, goData, newGeneSet, oldGeneSet );
            this.addStep( step1A, true );
        }
        step2 = new GeneSetWizardStep2( this, geneData, newGeneSet );
        this.addStep( step2 );
        step3 = new GeneSetWizardStep3( this, settings, geneData, newGeneSet, makenew );
        this.addStep( step3 );

        finishButton.setEnabled( false );
    }

    /**
     * Use this constructor when you know the gene set to be looked at.
     * 
     * @param callingframe
     * @param geneData
     * @param goData
     * @param geneSetId
     */
    public GeneSetWizard( GeneSetScoreFrame callingframe, GeneAnnotations geneData, GONames goData, String geneSetId ) {
        super( callingframe, 550, 350 );
        this.callingframe = callingframe;
        this.settings = callingframe.getSettings();
        this.geneData = geneData;
        this.goData = goData;
        this.geneSetId = geneSetId;
        makenew = false;
        nostep1 = true;
        newGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );
        oldGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );
        this.setTitle( "Modify Gene Set - Step 2 of 3" );
        step = 2;
        backButton.setEnabled( false );
        newGeneSet.setId( geneSetId );
        newGeneSet.setDesc( goData.getNameForId( geneSetId ) );
        newGeneSet.setModified( true );
        if ( geneData.geneSetExists( geneSetId ) )
            newGeneSet.getProbes().addAll( geneData.getClassToProbes( geneSetId ) );
        oldGeneSet.setId( geneSetId );
        oldGeneSet.setDesc( goData.getNameForId( geneSetId ) );
        oldGeneSet.setModified( true );
        if ( geneData.geneSetExists( geneSetId ) )
            oldGeneSet.getProbes().addAll( geneData.getClassToProbes( geneSetId ) );
        this.repaint();
        step2 = new GeneSetWizardStep2( this, geneData, newGeneSet );
        this.addStep( step2, true ); // hack for starting at step 2
        this.addStep( step2 );
        step2.updateCountLabel();
        step3 = new GeneSetWizardStep3( this, settings, geneData, newGeneSet, makenew );
        this.addStep( step3 );
    }

    protected void nextButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 1 ) {
            if ( makenew || step1A.isReady() ) { // not (case 3 with no class picked)
                if ( makenew && step1.getInputMethod() == 1 ) { // case 2, load from file
                    try {
                        // newGeneSet.loadUserGeneSet( step1.getLoadFile() );
                        newGeneSet.loadPlainGeneList( step1.getLoadFile() );
                    } catch ( IOException e1 ) {
                        GuiUtil.error( "Error loading gene set information. Please check the file format and make sure"
                                + " the file is readable." );
                    }
                }

                if ( makenew ) { // cases 1 & 2
                    this.getContentPane().remove( step1 );
                    this.setTitle( "Define New Gene Set - Step 2 of 3" );
                } else { // case 3
                    this.getContentPane().remove( step1A );
                    this.setTitle( "Modify Gene Set - Step 2 of 3" );
                }
                step = 2;
                backButton.setEnabled( true );
                finishButton.setEnabled( false );
                this.getContentPane().add( step2 );
                step2.revalidate();
                step2.updateCountLabel();
                this.repaint();

            }
        } else if ( step == 2 ) {
            if ( newGeneSet.getProbes().size() == 0 ) {
                showError( "You have not selected any genes/probes." );
                return;
            }

            this.getContentPane().remove( step2 );
            step = 3;
            if ( makenew ) {
                this.setTitle( "Define New Gene Set - Step 3 of 3" );
            } else {
                this.setTitle( "Modify Gene Set - Step 3 of 3" );
            }
            backButton.setEnabled( true );
            nextButton.setEnabled( false );

            finishButton.setEnabled( true );

            step3.update();
            this.getContentPane().add( step3 );
            step3.revalidate();
            this.repaint();
        }
    }

    protected void backButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 2 ) {
            this.getContentPane().remove( step2 );
            step = 1;
            backButton.setEnabled( false );
            finishButton.setEnabled( false );
            if ( makenew ) {
                this.setTitle( "Define New Gene Set - Step 1 of 3" );
                this.getContentPane().add( step1 );
                step1.revalidate();
            } else {
                this.setTitle( "Modify Gene Set - Step 1 of 3" );
                this.getContentPane().add( step1A );
                newGeneSet.clear();
                step1A.revalidate();
            }
            this.repaint();
        }
        if ( step == 3 ) {
            this.getContentPane().remove( step3 );
            step = 2;
            if ( makenew ) {
                this.setTitle( "Define New Gene Set - Step 2 of 3" );
            } else {
                this.setTitle( "Modify Gene Set - Step 2 of 3" );
                if ( nostep1 ) backButton.setEnabled( false );
            }
            nextButton.setEnabled( true );
            if ( makenew )
                finishButton.setEnabled( false );
            else
                finishButton.setEnabled( true );
            this.getContentPane().add( step2 );
            step2.revalidate();
            this.repaint();
        }
    }

    protected void cancelButton_actionPerformed( ActionEvent e ) {
        geneData.resetSelectedProbes();
        geneData.resetSelectedSets();
        dispose();
    }

    protected void finishButton_actionPerformed( ActionEvent e ) {
        if ( step == 2 ) {
            // pass the name and description onto step 3.
            step3.update();
        }
        step3.nameNewGeneSet();
        String id = null;
        if ( makenew ) {
            id = newGeneSet.getId();
        } else {
            id = oldGeneSet.getId();
        }

        log.debug( "Got modified or new gene set: " + id );
        if ( id == null || id.length() == 0 ) {
            showError( "The gene set ID must be specified." );
            return;
        }

        if ( geneData.geneSetExists( id ) && makenew ) {
            showError( "A gene set with the ID " + id + " already exists." );
            return;
        }

        if ( newGeneSet.getDesc() == null || newGeneSet.getDesc().length() == 0 ) {
            newGeneSet.setDesc( "No description given" );
        }

        if ( makenew || !newGeneSet.modified() ) {
            newGeneSet.addToMaps( goData );
        } else {
            if ( newGeneSet.compare( oldGeneSet ) != 0 ) newGeneSet.modifyClass( goData );
        }

        try {
            newGeneSet.saveGeneSet( 0 );
        } catch ( IOException e1 ) {
            GuiUtil.error( "Error writing the new gene set to file:", e1 );
        }
        ( ( GeneSetScoreFrame ) callingframe ).getTreePanel().addNode( id, newGeneSet.getDesc() );
        ( ( GeneSetScoreFrame ) callingframe ).addedNewGeneSet();
        if ( newGeneSet.modified() ) {
            ( ( GeneSetScoreFrame ) callingframe ).addUserOverwritten( id );
        }
        dispose();

    }
}
