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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.gui.util.WizardStep;

/**
 * Pick the analysis results set to save.
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class SaveWizardStep1 extends WizardStep {

    private static final long serialVersionUID = -1L;
    private List<GeneSetPvalRun> rundata = null;
    private JPanel runPanel = null;
    private JComboBox runComboBox = null;
    private JLabel runLabel = null;

    boolean runs_exist = false;

    public SaveWizardStep1( SaveWizard wiz, List<GeneSetPvalRun> rundata ) {
        super( wiz );
        this.rundata = rundata;
        this.jbInit();
        showChoices();
        wiz.clearStatus();
    }

    // Component initialization
    @Override
    protected void jbInit() {
        
    }

    @Override
    public boolean isReady() {
        return true;
    }

    void showChoices() {
        if ( rundata == null || rundata.size() < 1 ) {
            runComboBox.addItem( "No runs available to save" );
            runs_exist = false;
        } else {
            runs_exist = true;
            for ( int i = 0; i < rundata.size(); i++ ) {
                runComboBox.insertItemAt( rundata.get( i ).getName(), i );
            }
            runComboBox.setSelectedIndex( 0 );
        }
    }

    public int getSelectedRunNum() {
        return runComboBox.getSelectedIndex();
    }

    public boolean runsExist() {
        return runs_exist;

    }
}