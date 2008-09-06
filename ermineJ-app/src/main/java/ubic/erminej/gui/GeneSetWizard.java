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

import java.awt.event.ActionEvent;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;
import ubic.erminej.data.UserDefinedGeneSetManager;

/**
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @version $Id$
 */

public class GeneSetWizard extends Wizard {
    /**
     * 
     */
    private static final long serialVersionUID = 7175277380197599151L;
    private static Log log = LogFactory.getLog( GeneSetWizard.class.getName() );
    Settings settings;
    GeneAnnotations geneData;
    GONames goData;
    GeneSetWizardStep1 step1; // case 1 (manual creating) and case 2 (new from file)
    GeneSetWizardStep1A step1A; // case 3 (modifying existing)
    GeneSetWizardStep2 step2; // step 2 for cases 1-3 and step 1 for case 4
    GeneSetWizardStep3 step3;

    int step;
    boolean makingNewGeneSet;
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
    public GeneSetWizard( GeneSetScoreFrame callingframe, GeneAnnotations geneData, GONames goData, boolean makingNew ) {
        super( callingframe, 550, 350 );
        this.callingframe = callingframe;
        this.settings = callingframe.getSettings();
        this.geneData = geneData;
        this.goData = goData;
        this.makingNewGeneSet = makingNew;
        newGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );
        oldGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );

        geneData.resetSelectedProbes();
        step = 1;
        if ( makingNew ) {
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
        step3 = new GeneSetWizardStep3( this, newGeneSet, makingNew );
        this.addStep( step3 );

        finishButton.setEnabled( false );
    }

    /**
     * Use this constructor when you know the gene set to be looked at. (modify a gene set)
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
        this.makingNewGeneSet = false;

        newGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );
        oldGeneSet = new UserDefinedGeneSetManager( geneData, settings, "" );
        this.setTitle( "Modify Gene Set - Step 2 of 3" );
        step = 2;
        backButton.setEnabled( false );

        newGeneSet.setId( geneSetId );
        newGeneSet.setDesc( goData.getNameForId( geneSetId ) );
        newGeneSet.setAspect( goData.getAspectForId( geneSetId ) );
        newGeneSet.setDefinition( goData.getDefinitionForId( geneSetId ) );
        newGeneSet.setModified( false );
        if ( geneData.geneSetExists( geneSetId ) )
            newGeneSet.getProbes().addAll( geneData.getClassToProbes( geneSetId ) );

        oldGeneSet.setId( geneSetId );
        oldGeneSet.setDesc( goData.getNameForId( geneSetId ) );
        oldGeneSet.setAspect( goData.getAspectForId( geneSetId ) );
        oldGeneSet.setDefinition( goData.getDefinitionForId( geneSetId ) );
        oldGeneSet.setModified( false );
        if ( geneData.geneSetExists( geneSetId ) )
            oldGeneSet.getProbes().addAll( geneData.getClassToProbes( geneSetId ) );

        this.repaint();
        step2 = new GeneSetWizardStep2( this, geneData, newGeneSet );
        this.addStep( step2, true ); // hack for starting at step 2
        this.addStep( step2 );
        step2.updateCountLabel();
        step3 = new GeneSetWizardStep3( this, newGeneSet, makingNewGeneSet );
        this.addStep( step3 );
    }

    @Override
    protected void nextButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 1 ) {
            if ( makingNewGeneSet || step1A.isReady() ) { // not (case 3 with no class picked)
                if ( makingNewGeneSet && step1.getInputMethod() == 1 ) { // case 2, load from file
                    try {
                        // newGeneSet.loadUserGeneSet( step1.getLoadFile() );
                        newGeneSet.loadPlainGeneList( step1.getLoadFile() );
                    } catch ( IOException e1 ) {
                        GuiUtil.error( "Error loading gene set information. Please check the file format and make sure"
                                + " the file is readable." );
                    }
                }

                if ( makingNewGeneSet ) { // cases 1 & 2
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
            if ( makingNewGeneSet ) {
                this.setTitle( "Define New Gene Set - Step 3 of 3" );
                step3.setIdFieldEnabled( true );
            } else {
                this.setTitle( "Modify Gene Set - Step 3 of 3" );
                // if ( ( ( GeneSetScoreFrame ) callingframe ).userOverWrote( geneSetId ) )
                step3.setIdFieldEnabled( false );
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
                this.getContentPane().add( step1A );
                newGeneSet.clear();
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
        geneData.resetSelectedProbes();
        geneData.resetSelectedSets();
        dispose();
    }

    @Override
    protected void finishButton_actionPerformed( ActionEvent e ) {
        if ( step == 2 ) {
            // pass the name and description onto step 3.
            step3.update();
        }

        step3.nameNewGeneSet();
        String id = null;
        if ( makingNewGeneSet ) {
            id = newGeneSet.getId();
        } else {
            id = oldGeneSet.getId();
        }

        log.debug( "Got modified or new gene set: " + id );
        if ( id == null || id.length() == 0 ) {
            showError( "The gene set ID must be specified." );
            return;
        }

        if ( geneData.geneSetExists( id ) && makingNewGeneSet ) {
            showError( "A gene set with the ID " + id + " already exists." );
            return;
        }

        if ( newGeneSet.getDesc() == null || newGeneSet.getDesc().length() == 0 ) {
            newGeneSet.setDesc( "No description given" );
        }

        if ( makingNewGeneSet ) {
            log.debug( "Adding new or modified gene set to maps" );
            newGeneSet.addGeneSet( goData );
            ( ( GeneSetScoreFrame ) callingframe ).getTreePanel().addNode( id, newGeneSet.getDesc() );
            ( ( GeneSetScoreFrame ) callingframe ).addedNewGeneSet();

            try {
                log.debug( "Saving new gene set" );
                newGeneSet.saveGeneSet( 0 );
            } catch ( IOException e1 ) {
                GuiUtil.error( "Error writing the new gene set to file:", e1 );
            }

        } else if ( modifiedTheGeneSet() ) {
            log.debug( "Gene set was changed" );
            newGeneSet.modifyGeneSet( goData );
            ( ( GeneSetScoreFrame ) callingframe ).addUserOverwritten( id );
            ( ( GeneSetScoreFrame ) callingframe ).addedNewGeneSet();
            try {
                log.debug( "Saving modified gene set" );
                newGeneSet.saveGeneSet( 0 );
            } catch ( IOException e1 ) {
                GuiUtil.error( "Error writing the modified gene set to file:", e1 );
            }
        } else {
            log.debug( "Gene set was not created or changed, nothing to do." );
        }

        dispose();

    }

    /**
     * @return
     */
    private boolean modifiedTheGeneSet() {
        return !makingNewGeneSet && isChanged( newGeneSet, oldGeneSet );
    }

    /**
     * @param a
     * @param b
     * @return
     */
    private boolean isChanged( UserDefinedGeneSetManager a, UserDefinedGeneSetManager b ) {
        return a.compare( b );
    }
}
