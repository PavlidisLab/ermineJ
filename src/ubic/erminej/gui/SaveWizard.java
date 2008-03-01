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

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import ubic.basecode.bio.geneset.GONames;
import ubic.erminej.GeneSetPvalRun;
import ubic.erminej.ResultsPrinter;
import ubic.erminej.Settings;

/**
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class SaveWizard extends Wizard {
    /**
     * 
     */
    private static final long serialVersionUID = -1308261612895233801L;
    // logic
    int step = 1;
    int selected_run;
    GeneSetScoreFrame callingframe;
    List rundata;
    GONames goData;
    String saveFolder;
    SaveWizardStep1 step1;
    SaveWizardStep2 step2;

    public SaveWizard( GeneSetScoreFrame callingframe, List rundata, GONames goData ) {
        super( callingframe, 400, 200 );
        enableEvents( AWTEvent.WINDOW_EVENT_MASK );
        this.callingframe = callingframe;
        this.rundata = rundata;
        this.goData = goData;

        step1 = new SaveWizardStep1( this, rundata );
        this.addStep( step1, true );
        step2 = new SaveWizardStep2( this, callingframe.getSettings().getDataDirectory() );
        this.addStep( step2 );
        this.setTitle( "Save Analysis - Step 1 of 2" );
        finishButton.setEnabled( false );
    }

    void selectRun( int i ) {
        selected_run = i;
    }

    @Override
    protected void nextButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 1 ) {
            if ( step1.runsExist() ) {
                step = 2;
                this.getContentPane().remove( step1 );
                this.setTitle( "Save Analysis - Step 2 of 2" );
                this.getContentPane().add( step2 );
                step2.revalidate();
                backButton.setEnabled( true );
                nextButton.setEnabled( false );
                finishButton.setEnabled( true );
                finishButton.grabFocus();
                this.repaint();
            } else {
                showError( "No analyses to save." );
            }
        }
    }

    @Override
    protected void backButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 2 ) {
            step = 1;
            this.getContentPane().remove( step2 );
            this.setTitle( "Save Analysis - Step 1 of 2" );
            this.getContentPane().add( step1 );
            step1.revalidate();
            backButton.setEnabled( false );
            finishButton.setEnabled( false );
            nextButton.setEnabled( true );
            this.repaint();
        }
    }

    @Override
    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    @Override
    protected void finishButton_actionPerformed( ActionEvent e ) {
        GeneSetPvalRun runToSave = ( GeneSetPvalRun ) rundata.get( step1.getSelectedRunNum() );
        Settings saveSettings = runToSave.getSettings();
        String saveFileName = step2.getSaveFileName();
        try {

            /* first we stream the prefs to the file. */
            saveSettings.writeAnalysisSettings( saveFileName );

            /* then we pile on the results. */
            ResultsPrinter rp = new ResultsPrinter( saveFileName, runToSave, goData, step2.getShouldSaveGeneNames() );
            rp.printResults( true );
        } catch ( IOException ioe ) {
            GuiUtil.error( "Could not write results to the file. " + ioe );
        }
        dispose();
    }

}