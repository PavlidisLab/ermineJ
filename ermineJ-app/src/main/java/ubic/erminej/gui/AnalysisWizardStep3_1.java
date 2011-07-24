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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox; 
import javax.swing.JPanel;

import ubic.erminej.Settings;

/**
 * Choose the aspects to use.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class AnalysisWizardStep3_1 extends WizardStep {

    private static final long serialVersionUID = 9064542224892172L;
    private final Settings settings;
    private JCheckBox biologicalProcessButton;
    private JCheckBox molecularFunctionButton;
    private JCheckBox cellularComponentButton;
    private JCheckBox userDefinedButton;

    /**
     * @param wizard
     * @param settings
     */
    public AnalysisWizardStep3_1( AnalysisWizard wizard, Settings settings ) {
        super( wizard );
        this.settings = settings;
        this.jbInit();
        wizard.clearStatus();
        setValues();
    }

    /**
     * 
     */
    private void setValues() {
        this.biologicalProcessButton.setSelected( settings.getUseBiologicalProcess() );
        this.molecularFunctionButton.setSelected( settings.getUseMolecularFunction() );
        this.cellularComponentButton.setSelected( settings.getUseCellularComponent() );
        this.userDefinedButton.setSelected( settings.getUseUserDefined() );
    }

    /**
     * 
     */
    public void saveValues() {
        settings.setUseBiologicalProcess( this.biologicalProcessButton.isSelected() );
        settings.setUseCellularComponent( this.cellularComponentButton.isSelected() );
        settings.setUseMolecularFunction( this.molecularFunctionButton.isSelected() );
        settings.setUseUserDefined( this.userDefinedButton.isSelected() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.WizardStep#jbInit()
     */
    @Override
    protected void jbInit() {
        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.Y_AXIS ) );

        buttonPanel.setPreferredSize( new Dimension( 550, 140 ) );

        biologicalProcessButton = new JCheckBox();
        molecularFunctionButton = new JCheckBox();
        cellularComponentButton = new JCheckBox();
        userDefinedButton = new JCheckBox();

        biologicalProcessButton.setText( "Biological Process" );
        biologicalProcessButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        biologicalProcessButton.setPreferredSize( new Dimension( 140, 17 ) );
        buttonPanel.add( biologicalProcessButton, null );

        molecularFunctionButton.setText( "Molecular Function" );
        molecularFunctionButton.setSelected( true );
        molecularFunctionButton.setPreferredSize( new Dimension( 140, 17 ) );
        molecularFunctionButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        buttonPanel.add( molecularFunctionButton, null );

        cellularComponentButton.setText( "Cellular Component" );
        cellularComponentButton.setPreferredSize( new Dimension( 140, 17 ) );
        cellularComponentButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        buttonPanel.add( cellularComponentButton, null );

        userDefinedButton.setText( "Your custom groups" );
        userDefinedButton.setPreferredSize( new Dimension( 140, 17 ) );
        userDefinedButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        buttonPanel.add( userDefinedButton, null );

        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

        this.addHelp( "<html><b>Select the aspects to include in the analysis.</b><br>" + "</html>" );
        this.addMain( buttonPanel );

    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.WizardStep#isReady()
     */
    @Override
    public boolean isReady() {
        return false;
    }

}
