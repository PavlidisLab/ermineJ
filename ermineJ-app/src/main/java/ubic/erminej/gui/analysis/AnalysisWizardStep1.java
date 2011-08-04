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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.EmptyBorder;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.gui.StartupPanel;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.WizardStep;

/**
 * Choose the method for the analysis.
 * 
 * @author homin
 * @version $Id$
 */
public class AnalysisWizardStep1 extends WizardStep {
    private static final long serialVersionUID = -8558401501649026545L;

    /*
     * For tests.
     */
    public static void main( String[] args ) throws Exception {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {

        }
        JFrame f = new JFrame();
        f.setSize( new Dimension( 400, 600 ) );
        AnalysisWizardStep1 p = new AnalysisWizardStep1( null, new Settings() );
        f.add( p );
        f.pack();
        GuiUtil.centerContainer( f );
        f.setVisible( true );
    }

    private AnalysisWizard wiz;
    private Settings settings;
    private JRadioButton oraButton;
    private JRadioButton resampButton;
    private JRadioButton corrButton;

    private JRadioButton rocButton;

    public AnalysisWizardStep1( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.jbInit();
        this.wiz = wiz;
        this.settings = settings;
        setValues();

    }

    @Override
    public boolean isReady() {
        return true;
    }

    public void saveValues() {
        if ( oraButton.isSelected() ) {
            settings.setClassScoreMethod( SettingsHolder.Method.ORA );
        } else if ( resampButton.isSelected() ) {
            settings.setClassScoreMethod( SettingsHolder.Method.GSR );
        } else if ( corrButton.isSelected() ) {
            settings.setClassScoreMethod( SettingsHolder.Method.CORR );
        } else if ( rocButton.isSelected() ) {
            settings.setClassScoreMethod( SettingsHolder.Method.ROC );
        }
    }

    public void setValues() {
        if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.ORA ) )
            oraButton.setSelected( true );
        else if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.GSR ) )
            resampButton.setSelected( true );
        else if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) )
            corrButton.setSelected( true );

        else if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.ROC ) ) rocButton.setSelected( true );
    }

    // Component initialization
    @Override
    protected void jbInit() {
        JPanel step1Panel = new JPanel();

        BoxLayout boxLayout = new BoxLayout( step1Panel, BoxLayout.Y_AXIS );
        step1Panel.setLayout( boxLayout );

        ButtonGroup buttonGroup1 = new ButtonGroup();
        oraButton = new JRadioButton();
        resampButton = new JRadioButton();
        corrButton = new JRadioButton();
        rocButton = new JRadioButton();

        // JPanel oraPanel = new JPanel();
        // oraPanel.setLayout( new BoxLayout( oraPanel, BoxLayout.X_AXIS ) );
        // oraPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        // oraPanel.setPreferredSize( new Dimension( 400, 19 ) );
        oraButton.setText( "ORA" );
        oraButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        oraButton.addActionListener( new AnalysisWizardStep1_oraButton_actionAdapter( this ) );
        buttonGroup1.add( oraButton );
        // oraPanel.add( oraButton );

        // JPanel gsrPanel = new JPanel();
        // gsrPanel.setLayout( new BoxLayout( gsrPanel, BoxLayout.X_AXIS ) );
        // gsrPanel.setPreferredSize( new Dimension( 400, 19 ) );
        // gsrPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        resampButton.setText( "Gene score resampling" );
        resampButton.setSelected( true );
        resampButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        resampButton.addActionListener( new AnalysisWizardStep1_resampButton_actionAdapter( this ) );
        buttonGroup1.add( resampButton );
        // gsrPanel.add( resampButton );

        // JPanel rocPanel = new JPanel();
        // rocPanel.setLayout( new BoxLayout( rocPanel, BoxLayout.X_AXIS ) );
        // rocPanel.setPreferredSize( new Dimension( 400, 19 ) );
        // rocPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        rocButton.setText( "ROC" );
        rocButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        rocButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                wiz.setAnalysisType( SettingsHolder.Method.ROC );
            }
        } );
        buttonGroup1.add( rocButton );
        // rocPanel.add( rocButton );

        // JPanel corrPanel = new JPanel();
        // corrPanel.setLayout( new BoxLayout( corrPanel, BoxLayout.X_AXIS ) );
        // corrPanel.setPreferredSize( new Dimension( 400, 19 ) );
        // corrPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        corrButton.setText( "Correlation" );
        corrButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        corrButton.addActionListener( new AnalysisWizardStep1_corrButton_actionAdapter( this ) );
        buttonGroup1.add( corrButton );
        // corrPanel.add( corrButton );

        JLabel gsrLabel = new JLabel();
        JLabel corrLabel = new JLabel();
        JLabel oraLabel = new JLabel();
        JLabel rocLabel = new JLabel();

        oraLabel.setText( "Over-representation analysis" );
        oraLabel.setPreferredSize( new Dimension( 200, 17 ) );
        oraLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        // oraLabel.setLabelFor( oraButton );
        // oraPanel.add( oraLabel );

        gsrLabel.setText( "Examines distribution of gene scores" );
        gsrLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        gsrLabel.setPreferredSize( new Dimension( 200, 17 ) );
        // gsrLabel.setLabelFor( resampButton );
        // gsrPanel.add( gsrLabel );

        rocLabel.setText( "Uses ranks of gene scores" );
        rocLabel.setPreferredSize( new Dimension( 200, 17 ) );
        rocLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        // rocLabel.setLabelFor( rocButton );
        // rocPanel.add( rocLabel );

        corrLabel.setText( "Uses correlation of expression profiles" );
        corrLabel.setPreferredSize( new Dimension( 200, 17 ) );
        corrLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        // corrLabel.setLabelFor( corrButton );
        // corrPanel.add( corrLabel );

        GroupLayout gl = new GroupLayout( step1Panel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        step1Panel.setLayout( gl );
        step1Panel.setBorder( BorderFactory.createEmptyBorder( 20, 40, 20, 40 ) );
        gl.setHorizontalGroup( gl.createSequentialGroup().addGroup(
                gl.createParallelGroup( Alignment.LEADING ).addComponent( resampButton ).addComponent( oraButton )
                        .addComponent( rocButton ).addComponent( corrButton ) ).addGroup(
                gl.createParallelGroup( Alignment.LEADING ).addComponent( gsrLabel ).addComponent( oraLabel )
                        .addComponent( rocLabel ).addComponent( corrLabel ) ) );

        gl
                .setVerticalGroup( gl.createSequentialGroup().addGroup(
                        gl.createParallelGroup( Alignment.BASELINE ).addComponent( resampButton ).addComponent(
                                gsrLabel ) )
                        .addGroup(
                                gl.createParallelGroup( Alignment.BASELINE ).addComponent( oraButton ).addComponent(
                                        oraLabel ) ).addGroup(
                                gl.createParallelGroup( Alignment.BASELINE ).addComponent( rocButton ).addComponent(
                                        rocLabel ) ).addGroup(
                                gl.createParallelGroup( Alignment.BASELINE ).addComponent( corrButton ).addComponent(
                                        corrLabel ) ) );

        // step1Panel.setBorder( new EmptyBorder( 20, 20, 20, 20 ) );
        // step1Panel.add( oraPanel );
        // step1Panel.add( Box.createVerticalStrut( 10 ) );
        // step1Panel.add( gsrPanel );
        // step1Panel.add( Box.createVerticalStrut( 10 ) );
        // step1Panel.add( rocPanel );
        // step1Panel.add( Box.createVerticalStrut( 10 ) );
        // step1Panel.add( corrPanel );

        this.addHelp( "<html><b>Select the method to " + "use for scoring gene sets.</b><br>" + "</html>" );
        this.addMain( step1Panel );
    }

    void corrButton_actionPerformed() {
        wiz.setAnalysisType( SettingsHolder.Method.CORR );
    }

    void oraButton_actionPerformed() {
        wiz.setAnalysisType( SettingsHolder.Method.ORA );
    }

    void resampButton_actionPerformed() {
        wiz.setAnalysisType( SettingsHolder.Method.GSR );
    }
}

class AnalysisWizardStep1_corrButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep1 adaptee;

    AnalysisWizardStep1_corrButton_actionAdapter( AnalysisWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.corrButton_actionPerformed();
    }
}

class AnalysisWizardStep1_oraButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep1 adaptee;

    AnalysisWizardStep1_oraButton_actionAdapter( AnalysisWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.oraButton_actionPerformed();
    }
}

class AnalysisWizardStep1_resampButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep1 adaptee;

    AnalysisWizardStep1_resampButton_actionAdapter( AnalysisWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.resampButton_actionPerformed();
    }
}