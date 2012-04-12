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
package ubic.erminej.gui.geneset.edit;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.Probe;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.Wizard;

/**
 * For creating or editing gene sets.
 * 
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetWizard extends Wizard {

    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog( GeneSetWizard.class.getName() );
    private SettingsHolder settings;
    private GeneAnnotations geneData;
    private GeneSetWizardStep1 step1; // case 1 (manual creating) and case 2 (new from file)
    private GeneSetWizardStep1A step1A; // case 3 (modifying existing)
    private GeneSetWizardStep2 step2; // step 2 for cases 1-3 and step 1 for case 4
    private GeneSetWizardStep3 step3;

    private int step;
    private boolean makingNewGeneSet;
    private boolean nostep1 = false;

    private GeneSet oldGeneSet;
    private StatusViewer messenger = new StatusStderr();

    /**
     * Use this constructor to let the user choose which gene set to look at or to make a new one.
     * 
     * @param callingframe
     * @param geneData
     * @param makenew if true, the user is asked to create a new gene set. If false, they are asked to choose from a
     *        list of existing gene sets.
     */
    public GeneSetWizard( MainFrame callingframe, GeneAnnotations geneData, boolean makingNew ) {
        super( callingframe, 550, 350 );
        this.callingframe = callingframe;
        this.settings = callingframe.getSettings();
        this.geneData = geneData;
        this.makingNewGeneSet = makingNew;
        this.messenger = callingframe.getStatusMessenger();
        if ( this.messenger == null ) messenger = new StatusStderr();

        step = 1;
        if ( makingNew ) {
            this.setTitle( "Define New Gene Set - Step 1 of 3" );
            step1 = new GeneSetWizardStep1( this, settings );
            this.addStep( step1, true );
        } else {
            this.setTitle( "Modify Gene Set - Step 1 of 3" );
            step1A = new GeneSetWizardStep1A( this, geneData );
            this.addStep( step1A, true );
        }
        step2 = new GeneSetWizardStep2( this, geneData );
        this.addStep( step2 );
        step3 = new GeneSetWizardStep3( this );
        this.addStep( step3 );

        finishButton.setEnabled( false );
    }

    /**
     * Use this constructor when you know the gene set to be looked at. (modify a gene set). We basically go right to
     * step 2.
     * 
     * @param callingframe
     * @param geneData
     * @param goData
     * @param geneSetId
     */
    public GeneSetWizard( MainFrame callingframe, GeneAnnotations geneData, GeneSetTerm geneSetId ) {
        super( callingframe, 550, 350 );
        this.callingframe = callingframe;
        this.settings = callingframe.getSettings();
        this.geneData = geneData;

        this.makingNewGeneSet = false;

        this.oldGeneSet = geneData.getGeneSet( geneSetId );
        assert oldGeneSet != null;

        this.setTitle( "Modify Gene Set - Step 2 of 3" );
        step = 2;
        backButton.setEnabled( false );
        step2 = new GeneSetWizardStep2( this, geneData );
        this.addStep( step2, true ); // hack for starting at step 2
        this.addStep( step2 );
        step2.updateCountLabel();
        step3 = new GeneSetWizardStep3( this );
        this.addStep( step3 );

        step3.setIdText( oldGeneSet.getId() );
        step3.setDescText( oldGeneSet.getName() );
        step2.setStartingSet( this.oldGeneSet );
        this.repaint();
    }

    @Override
    protected void nextButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 1 ) {
            if ( makingNewGeneSet || step1A.isReady() ) { // not (case 3 with no class picked)
                if ( makingNewGeneSet && step1.getInputMethod() == 1 ) { // case 2, load from file
                    try {
                        this.oldGeneSet = geneData.loadPlainGeneList( step1.getLoadFile() );
                    } catch ( IOException e1 ) {
                        GuiUtil.error( "Error loading gene set information. Please check the file format and make sure"
                                + " the file is readable." );
                    }
                }

                if ( makingNewGeneSet ) { // cases 1 & 2
                    // at this point we don't have any information except the method they will use
                    this.getContentPane().remove( step1 );
                    this.setTitle( "Define New Gene Set - Step 2 of 3" );
                } else { // case 3 - editing an existing set.
                    assert this.oldGeneSet != null;
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
            if ( step2.getProbes().isEmpty() ) {
                showError( "You have not selected any genes/probes." );
                return;
            }

            this.getContentPane().remove( step2 );
            step = 3;
            step3.setIdFieldEnabled( true );
            if ( makingNewGeneSet ) {
                this.setTitle( "Define New Gene Set - Step 3 of 3" );
            } else {
                this.setTitle( "Modify Gene Set - Step 3 of 3" );
            }
            backButton.setEnabled( true );
            nextButton.setEnabled( false );
            finishButton.setEnabled( true );

            this.getContentPane().add( step3 );
            step3.revalidate();
            this.repaint();
        }
    }

    @Override
    protected void backButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 2 ) {
            this.getContentPane().remove( step2 );
            step = 1;
            backButton.setEnabled( false );
            finishButton.setEnabled( false );
            if ( makingNewGeneSet ) {
                this.setTitle( "Define New Gene Set - Step 1 of 3" );
                this.getContentPane().add( step1 );
                step1.revalidate();
            } else {
                this.setTitle( "Modify Gene Set - Step 1 of 3" );
                assert step1A != null;
                this.getContentPane().add( step1A );
                step1A.revalidate();
            }
            this.repaint();
        }
        if ( step == 3 ) {
            this.getContentPane().remove( step3 );
            step = 2;
            if ( makingNewGeneSet ) {
                this.setTitle( "Define New Gene Set - Step 2 of 3" );
            } else {
                this.setTitle( "Modify Gene Set - Step 2 of 3" );
                if ( nostep1 ) backButton.setEnabled( false );
            }
            nextButton.setEnabled( true );
            if ( makingNewGeneSet )
                finishButton.setEnabled( false );
            else
                finishButton.setEnabled( true );
            this.getContentPane().add( step2 );
            step2.revalidate();
            this.repaint();
        }
    }

    @Override
    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    @Override
    protected void finishEditing( ActionEvent e ) {

        messenger.showStatus( "Finishing ..." );

        Collection<Probe> probes = step2.getProbes();

        String id = step3.getGeneSetId();
        String desc = step3.getGeneSetName();

        if ( StringUtils.isBlank( id ) ) {
            showError( "The gene set ID must be specified." );
            return;
        }

        if ( geneData.geneSetExists( id ) && makingNewGeneSet ) {
            showError( "A gene set with the ID '" + id + "' already exists." );
            return;
        }

        if ( !geneData.geneSetExists( id ) && !makingNewGeneSet ) {
            showError( "Note that changing the ID of an existing gene set will result in creation of a new one." );
        }

        GeneSet toSave = null;

        boolean modified = false;

        if ( oldGeneSet != null ) {

            modified = modifiedTheGeneSet();

            if ( id.equals( oldGeneSet.getId() ) ) {
                // update members and description from the data we have
                toSave = oldGeneSet; // in this case, the GeneAnnotations will be okay automatically.

                toSave.getTerm().setName( desc );

            } else {
                // make a new one based on a copy.
                GeneSetTerm newGeneSetT = new GeneSetTerm( id, desc );
                toSave = new GeneSet( newGeneSetT );
            }

        } else {

            // make a new one.
            GeneSetTerm newGeneSetT = new GeneSetTerm( id, desc );
            toSave = new GeneSet( newGeneSetT );
        }

        toSave.setUserDefined( true );

        log.debug( "Got modified or new gene set: " + id );

        toSave.clearGenes();

        for ( Probe probe : probes ) {
            toSave.addGene( probe.getGene() );
        }

        // so we can save the updated version. Maybe.
        if ( oldGeneSet != null && StringUtils.isNotBlank( oldGeneSet.getSourceFile() ) ) {
            toSave.setSourceFile( oldGeneSet.getSourceFile() );
        }
        try {
            if ( makingNewGeneSet || !toSave.getId().equals( oldGeneSet.getId() ) ) {
                // showStatus( "Saving new gene set in its own file" );
                messenger.showStatus( "Saving new gene set in its own file" );
                geneData.addGeneSet( toSave );
                geneData.saveGeneSet( toSave );
            } else if ( modified ) {
                // showStatus( "Saving modified gene set " + toSave + "( based on " + oldGeneSet + ")" );
                messenger.showStatus( "Saving modified gene set " + toSave + "( based on " + oldGeneSet + ")" );
                geneData.saveGeneSet( toSave );
            } else {
                // showStatus( "Gene set was not created or changed, nothing to do." );
                messenger.showStatus( "Gene set was not created or changed, nothing to do." );
            }
            ( ( MainFrame ) callingframe ).addedNewGeneSet( toSave );
            dispose();
        } catch ( IOException e1 ) {
            GuiUtil.error( "Error writing the new gene set to file:", e1 );
        }

    }

    /**
     * @return
     */
    private boolean modifiedTheGeneSet() {
        assert oldGeneSet != null;
        return step2.getProbes().size() != oldGeneSet.getProbes().size()
                || !step2.getProbes().containsAll( oldGeneSet.getProbes() )
                || !step3.getGeneSetName().equals( oldGeneSet.getName() );
    }

    public boolean getMakingNew() {
        return makingNewGeneSet;
    }

    /**
     * @param geneSet
     */
    public void setOriginalGeneSet( GeneSet geneSet ) {
        this.oldGeneSet = geneSet;
    }

    public GeneSet getOriginalGeneSet() {
        return this.oldGeneSet;
    }

}
