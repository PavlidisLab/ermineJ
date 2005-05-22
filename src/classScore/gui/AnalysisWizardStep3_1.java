package classScore.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import baseCode.gui.WizardStep;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
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
        try {
            this.jbInit();
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        wizard.clearStatus();
        setValues();
    }

    /**
     * 
     */
    private void setValues() {
        if ( settings.getUseBiologicalProcess() )
            this.biologicalProcessButton.setSelected( true );
        else if ( settings.getUseMolecularFunction() )
            this.molecularFunctionButton.setSelected( true );
        else if ( settings.getUseCellularComponent() ) this.cellularComponentButton.setSelected( true );

    }

    /**
     * 
     */
    public void saveValue() {
        settings.setUseBiologicalProcess( this.biologicalProcessButton.isSelected() );
        settings.setUseCellularComponent( this.cellularComponentButton.isSelected() );
        settings.setUseMolecularFunction( this.molecularFunctionButton.isSelected() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.WizardStep#jbInit()
     */
    protected void jbInit() throws Exception {
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
        // biologicalProcessButton
        // .addActionListener( new AnalysisWizardStep31_biologicalProcessButton_actionAdapter( this ) );
        // buttonGroup1.add( biologicalProcessButton );
        jPanel5.add( biologicalProcessButton, null );
        molecularFunctionButton.setText( "Molecular Function" );
        molecularFunctionButton.setSelected( true );
        molecularFunctionButton.setPreferredSize( new Dimension( 140, 17 ) );
        molecularFunctionButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        // molecularFunctionButton
        // .addActionListener( new AnalysisWizardStep1_molecularFunctionButton_actionAdapter( this ) );
        // buttonGroup1.add( molecularFunctionButton );
        jPanel5.add( molecularFunctionButton, null );
        cellularComponentButton.setText( "Cellular Component" );
        cellularComponentButton.setPreferredSize( new Dimension( 140, 17 ) );
        cellularComponentButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        // cellularComponentButton
        // .addActionListener( new AnalysisWizardStep1_cellularComponentButton_actionAdapter( this ) );
        // buttonGroup1.add( cellularComponentButton );
        jPanel5.add( cellularComponentButton, null );
        jPanel4.add( jPanel5, null );
        jPanel12.setPreferredSize( new Dimension( 210, 80 ) );
        // jLabel9.setText( " Biological Process" );
        jLabel9.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel9, null );
        // jLabel4.setText( " Molecular Function" );
        jLabel4.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel4, null );
        // jLabel5.setText( " Cellular Component" );
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
        // TODO Auto-generated method stub
        return false;
    }

}
