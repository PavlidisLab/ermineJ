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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import ubic.erminej.Settings;
import ubic.erminej.gui.util.WizardStep;

/**
 * Choose the method for the analysis.
 * 
 * @author homin
 * @version $Id$
 */
public class AnalysisWizardStep1 extends WizardStep {
    private static final long serialVersionUID = -8558401501649026545L;
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

        JPanel oraPanel = new JPanel();
        oraPanel.setLayout( new BoxLayout( oraPanel, BoxLayout.X_AXIS ) );
        oraPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        oraPanel.setPreferredSize( new Dimension( 400, 19 ) );
        oraButton.setText( "ORA" );
        oraButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        oraButton.addActionListener( new AnalysisWizardStep1_oraButton_actionAdapter( this ) );
        buttonGroup1.add( oraButton );
        oraPanel.add( oraButton );

        JPanel gsrPanel = new JPanel();
        gsrPanel.setLayout( new BoxLayout( gsrPanel, BoxLayout.X_AXIS ) );
        gsrPanel.setPreferredSize( new Dimension( 400, 19 ) );
        gsrPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        resampButton.setText( "Gene score resampling" );
        resampButton.setSelected( true );
        resampButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        resampButton.addActionListener( new AnalysisWizardStep1_resampButton_actionAdapter( this ) );
        buttonGroup1.add( resampButton );
        gsrPanel.add( resampButton );

        JPanel rocPanel = new JPanel();
        rocPanel.setLayout( new BoxLayout( rocPanel, BoxLayout.X_AXIS ) );
        rocPanel.setPreferredSize( new Dimension( 400, 19 ) );
        rocPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        rocButton.setText( "ROC" );
        rocButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        rocButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                wiz.setAnalysisType( Settings.Method.ROC );
            }
        } );
        buttonGroup1.add( rocButton );
        rocPanel.add( rocButton );

        JPanel corrPanel = new JPanel();
        corrPanel.setLayout( new BoxLayout( corrPanel, BoxLayout.X_AXIS ) );
        corrPanel.setPreferredSize( new Dimension( 400, 19 ) );
        corrPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        corrButton.setText( "Correlation" );
        corrButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        corrButton.addActionListener( new AnalysisWizardStep1_corrButton_actionAdapter( this ) );
        buttonGroup1.add( corrButton );
        corrPanel.add( corrButton );

        JLabel gsrLabel = new JLabel();
        JLabel corrLabel = new JLabel();
        JLabel oraLabel = new JLabel();
        JLabel rocLabel = new JLabel();

        oraLabel.setText( "Over-representation analysis" );
        oraLabel.setPreferredSize( new Dimension( 200, 17 ) );
        oraLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        // oraLabel.setLabelFor( oraButton );
        oraPanel.add( oraLabel );

        gsrLabel.setText( "Examines distribution of gene scores" );
        gsrLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        gsrLabel.setPreferredSize( new Dimension( 200, 17 ) );
        // gsrLabel.setLabelFor( resampButton );
        gsrPanel.add( gsrLabel );

        rocLabel.setText( "Uses ranks of gene scores" );
        rocLabel.setPreferredSize( new Dimension( 200, 17 ) );
        rocLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        // rocLabel.setLabelFor( rocButton );
        rocPanel.add( rocLabel );

        corrLabel.setText( "Uses correlation of expression profiles" );
        corrLabel.setPreferredSize( new Dimension( 200, 17 ) );
        corrLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        // corrLabel.setLabelFor( corrButton );
        corrPanel.add( corrLabel );

        step1Panel.setBorder( new EmptyBorder( 20, 20, 20, 20 ) );
        step1Panel.add( oraPanel );
        step1Panel.add( Box.createVerticalStrut( 10 ) );
        step1Panel.add( gsrPanel );
        step1Panel.add( Box.createVerticalStrut( 10 ) );
        step1Panel.add( rocPanel );
        step1Panel.add( Box.createVerticalStrut( 10 ) );

        step1Panel.add( corrPanel );

        this.addHelp( "<html><b>Select the method to " + "use for scoring gene sets.</b><br>" + "</html>" );
        this.addMain( step1Panel );
    }

    @Override
    public boolean isReady() {
        return true;
    }

    void corrButton_actionPerformed() {
        wiz.setAnalysisType( Settings.Method.CORR );
    }

    void resampButton_actionPerformed() {
        wiz.setAnalysisType( Settings.Method.GSR );
    }

    void oraButton_actionPerformed() {
        wiz.setAnalysisType( Settings.Method.ORA );
    }

    public void setValues() {
        if ( settings.getClassScoreMethod().equals( Settings.Method.ORA ) )
            oraButton.setSelected( true );
        else if ( settings.getClassScoreMethod().equals( Settings.Method.GSR ) )
            resampButton.setSelected( true );
        else if ( settings.getClassScoreMethod().equals( Settings.Method.CORR ) )
            corrButton.setSelected( true );

        else if ( settings.getClassScoreMethod().equals( Settings.Method.ROC ) ) rocButton.setSelected( true );
    }

    public void saveValues() {
        if ( oraButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.Method.ORA );
        } else if ( resampButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.Method.GSR );
        } else if ( corrButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.Method.CORR );
        } else if ( rocButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.Method.ROC );
        }
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

class AnalysisWizardStep1_corrButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep1 adaptee;

    AnalysisWizardStep1_corrButton_actionAdapter( AnalysisWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.corrButton_actionPerformed();
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