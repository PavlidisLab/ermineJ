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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ubic.basecode.gui.WizardStep;
import ubic.erminej.Settings;

/**
 * @author pavlidis
 * @version $Id$
 */
public class AnalysisWizardStep3_1 extends WizardStep {

    private final Settings settings;
    private JCheckBox biologicalProcessButton;
    private JCheckBox molecularFunctionButton;
    private JCheckBox cellularComponentButton;

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
    }

    /**
     * 
     */
    public void saveValues() {
        settings.setUseBiologicalProcess( this.biologicalProcessButton.isSelected() );
        settings.setUseCellularComponent( this.cellularComponentButton.isSelected() );
        settings.setUseMolecularFunction( this.molecularFunctionButton.isSelected() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.WizardStep#jbInit()
     */
    protected void jbInit() {
        JPanel step1Panel = new JPanel();
        JPanel jPanel4 = new JPanel();
        JLabel jLabel8 = new JLabel();
        JPanel jPanel5 = new JPanel();
        // ButtonGroup buttonGroup1 = new ButtonGroup();
        biologicalProcessButton = new JCheckBox();
        molecularFunctionButton = new JCheckBox();
        cellularComponentButton = new JCheckBox();
        JPanel jPanel12 = new JPanel();
        JLabel jLabel4 = new JLabel();
        JLabel jLabel5 = new JLabel();
        JLabel jLabel9 = new JLabel();
        step1Panel.setPreferredSize( new Dimension( 550, 120 ) );
        jPanel4.setBorder( BorderFactory.createEtchedBorder() );
        jPanel4.setPreferredSize( new Dimension( 400, 94 ) );
        jLabel8.setText( "" );
        jLabel8.setPreferredSize( new Dimension( 274, 17 ) );
        step1Panel.add( jLabel8, null );
        jPanel5.setPreferredSize( new Dimension( 150, 80 ) );
        biologicalProcessButton.setText( "Biological Process" );
        biologicalProcessButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        biologicalProcessButton.setPreferredSize( new Dimension( 140, 17 ) );

        jPanel5.add( biologicalProcessButton, null );
        molecularFunctionButton.setText( "Molecular Function" );
        molecularFunctionButton.setSelected( true );
        molecularFunctionButton.setPreferredSize( new Dimension( 140, 17 ) );
        molecularFunctionButton.setBorder( BorderFactory.createLineBorder( Color.black ) );

        jPanel5.add( molecularFunctionButton, null );
        cellularComponentButton.setText( "Cellular Component" );
        cellularComponentButton.setPreferredSize( new Dimension( 140, 17 ) );
        cellularComponentButton.setBorder( BorderFactory.createLineBorder( Color.black ) );

        jPanel5.add( cellularComponentButton, null );
        jPanel4.add( jPanel5, null );
        jPanel12.setPreferredSize( new Dimension( 210, 80 ) );
        jLabel9.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel9, null );
        jLabel4.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel4, null );
        jLabel5.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel5, null );
        jPanel4.add( jPanel12, null );
        step1Panel.add( jPanel4, null );

        this.addHelp( "<html><b>Select the Gene Ontology aspects to include in the analysis.</b><br>" + "</html>" );
        this.addMain( step1Panel );

    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.WizardStep#isReady()
     */
    public boolean isReady() {
        return false;
    }

}
