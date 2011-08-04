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
package ubic.erminej.gui.analysis;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ubic.erminej.Settings;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.WizardStep;

/**
 * Choose the aspects to use.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class AnalysisWizardStep3 extends WizardStep {

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
    public AnalysisWizardStep3( AnalysisWizard wizard, Settings settings ) {
        super( wizard );
        this.settings = settings;
        this.jbInit();
        if ( wizard != null ) wizard.clearStatus();
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

    public static void main( String[] args ) throws Exception {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {

        }
        JFrame f = new JFrame();
        f.setSize( new Dimension( 400, 600 ) );
        AnalysisWizardStep3 p = new AnalysisWizardStep3( null, new Settings() );
        f.add( p );
        f.pack();
        GuiUtil.centerContainer( f );
        f.setVisible( true );
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
        biologicalProcessButton.setBorder( BorderFactory.createEmptyBorder( 15, 5, 2, 20 ) );
        molecularFunctionButton = new JCheckBox();
        molecularFunctionButton.setBorder( BorderFactory.createEmptyBorder( 15, 5, 2, 20 ) );
        cellularComponentButton = new JCheckBox();
        cellularComponentButton.setBorder( BorderFactory.createEmptyBorder( 15, 5, 2, 20 ) );
        userDefinedButton = new JCheckBox();
        userDefinedButton.setBorder( BorderFactory.createEmptyBorder( 15, 5, 2, 20 ) );

        biologicalProcessButton.setText( "Biological Process" );
        buttonPanel.add( biologicalProcessButton, null );

        molecularFunctionButton.setText( "Molecular Function" );
        buttonPanel.add( molecularFunctionButton, null );

        cellularComponentButton.setText( "Cellular Component" );
        buttonPanel.add( cellularComponentButton, null );

        userDefinedButton.setText( "Your custom groups" );
        buttonPanel.add( userDefinedButton, null );

        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 20, 40, 20, 40 ) );

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
