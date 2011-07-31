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

import ubic.basecode.util.StatusViewer;
import ubic.erminej.ResultsPrinter;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.Wizard;

/**
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class SaveWizard extends Wizard {

    private static final long serialVersionUID = -1L;

    private int step = 1;

    private List<GeneSetPvalRun> rundata;
    private SaveWizardStep1 step1;
    private SaveWizardStep2 step2;

    private StatusViewer statusMessenger;

    public SaveWizard( MainFrame callingframe, List<GeneSetPvalRun> rundata ) {
        super( callingframe, 400, 200 );
        enableEvents( AWTEvent.WINDOW_EVENT_MASK );
        this.callingframe = callingframe;
        this.rundata = rundata;

        this.statusMessenger = callingframe.getStatusMessenger();
        step1 = new SaveWizardStep1( this, rundata );
        this.addStep( step1, true );
        step2 = new SaveWizardStep2( this, callingframe.getSettings().getDataDirectory() );
        this.addStep( step2 );
        this.setTitle( "Save Analysis - Step 1 of 2" );
        finishButton.setEnabled( false );
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
    protected void finishEditing( ActionEvent e ) {
        GeneSetPvalRun runToSave = rundata.get( step1.getSelectedRunNum() );
        SettingsHolder saveSettings = runToSave.getSettings();
        String saveFileName = step2.getSaveFileName();
        try {

            /* first we stream the prefs to the file. */
            Settings.writeAnalysisSettings( saveSettings, saveFileName, statusMessenger );

            /* then we pile on the results. */
            ResultsPrinter rp = new ResultsPrinter( saveFileName, runToSave, step2.getShouldSaveGeneNames() );
            rp.printResults( true );
        } catch ( IOException ioe ) {
            GuiUtil.error( "Could not write results to the file. " + ioe );
        }
        dispose();
    }

}