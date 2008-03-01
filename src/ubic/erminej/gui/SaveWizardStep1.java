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
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ubic.erminej.GeneSetPvalRun;

/**
 * Pick the analysis results set to save.
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class SaveWizardStep1 extends WizardStep {
    /**
     * 
     */
    private static final long serialVersionUID = -8622689726831187005L;
    private SaveWizard wiz = null;
    private List rundata = null;
    private JPanel runPanel = null;
    private JComboBox runComboBox = null;
    private JLabel runLabel = null;

    boolean runs_exist = false;

    public SaveWizardStep1( SaveWizard wiz, List rundata ) {
        super( wiz );
        this.wiz = wiz;
        this.rundata = rundata;
        this.jbInit();
        showChoices();
        wiz.clearStatus();
    }

    // Component initialization
    @Override
    protected void jbInit() {
        runPanel = new JPanel();
        runPanel.setLayout( new BorderLayout() );
        JPanel topPanel = new JPanel();
        runLabel = new JLabel();
        runLabel.setText( "Choose the analysis to save:" );
        topPanel.add( runLabel );
        JPanel centerPanel = new JPanel();
        runComboBox = new JComboBox();
        runComboBox.setPreferredSize( new Dimension( 150, 15 ) );
        runComboBox.addActionListener( new SaveWizardStep1_runComboBox_actionAdapter( this ) );
        centerPanel.add( runComboBox );
        runPanel.add( topPanel, BorderLayout.NORTH );
        runPanel.add( centerPanel, BorderLayout.CENTER );

        this.addHelp( "<html><b>You may save " + "the results of an analysis in a file.</b><br>" + "This file"
                + " can be used in other software (e.g. Excel) or loaded"
                + " back into this application to be viewed later." );
        this.addMain( runPanel );
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
                runComboBox.insertItemAt( ( ( GeneSetPvalRun ) rundata.get( i ) ).getName(), i );
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

    void runComboBox_actionPerformed( ActionEvent e ) {
        wiz.selectRun( runComboBox.getSelectedIndex() );
    }

}

// /
class SaveWizardStep1_runComboBox_actionAdapter implements java.awt.event.ActionListener {
    SaveWizardStep1 adaptee;

    SaveWizardStep1_runComboBox_actionAdapter( SaveWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.runComboBox_actionPerformed( e );
    }
}