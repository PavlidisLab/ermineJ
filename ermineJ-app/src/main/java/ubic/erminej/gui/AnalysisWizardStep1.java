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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import ubic.erminej.Settings;

/**
 * @author not attributable
 * @version $Id$
 */
public class AnalysisWizardStep1 extends WizardStep {
    /**
     * 
     */
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
        JPanel jPanel4 = new JPanel();
        JLabel jLabel8 = new JLabel();
        JPanel jPanel5 = new JPanel();
        ButtonGroup buttonGroup1 = new ButtonGroup();
        oraButton = new JRadioButton();
        resampButton = new JRadioButton();
        corrButton = new JRadioButton();
        rocButton = new JRadioButton();

        JPanel jPanel12 = new JPanel();
        JLabel jLabel4 = new JLabel();
        JLabel jLabel5 = new JLabel();
        JLabel jLabel9 = new JLabel();
        step1Panel.setPreferredSize( new Dimension( 550, 120 ) );
        jPanel4.setBorder( BorderFactory.createEtchedBorder() );
        jPanel4.setLayout( new BorderLayout() );
        jPanel4.setPreferredSize( new Dimension( 400, 180 ) );
        jLabel8.setText( "" );
        jLabel8.setPreferredSize( new Dimension( 274, 17 ) );
        step1Panel.add( jLabel8, null );
        jPanel5.setPreferredSize( new Dimension( 150, 180 ) );

        oraButton.setText( "ORA" );
        oraButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        oraButton.setPreferredSize( new Dimension( 140, 17 ) );
        oraButton.addActionListener( new AnalysisWizardStep1_oraButton_actionAdapter( this ) );
        buttonGroup1.add( oraButton );
        jPanel5.add( oraButton, null );

        resampButton.setText( "Gene score resampling" );
        resampButton.setSelected( true );
        resampButton.setPreferredSize( new Dimension( 140, 17 ) );
        resampButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        resampButton.addActionListener( new AnalysisWizardStep1_resampButton_actionAdapter( this ) );
        buttonGroup1.add( resampButton );
        jPanel5.add( resampButton, null );

        corrButton.setText( "Correlation" );
        corrButton.setPreferredSize( new Dimension( 140, 17 ) );
        corrButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        corrButton.addActionListener( new AnalysisWizardStep1_corrButton_actionAdapter( this ) );
        buttonGroup1.add( corrButton );
        jPanel5.add( corrButton, null );

        rocButton.setText( "ROC" );
        rocButton.setPreferredSize( new Dimension( 140, 17 ) );
        rocButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        rocButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                wiz.setAnalysisType( Settings.ROC );
            }
        } );
        buttonGroup1.add( rocButton );
        jPanel5.add( rocButton, null );

        jPanel4.add( jPanel5, BorderLayout.WEST );
        jPanel12.setPreferredSize( new Dimension( 210, 80 ) );
        jLabel9.setText( " Over-representation analysis" );
        jLabel9.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel9, null );
        jLabel4.setText( " Examines distribution of gene scores" );
        jLabel4.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel4, null );
        jLabel5.setText( " Uses correlation of expression profiles" );
        jLabel5.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel5, null );

        JLabel jLabel6 = new JLabel();
        jLabel6.setText( " Uses ranks of gene scores" );
        jLabel6.setPreferredSize( new Dimension( 200, 17 ) );
        jPanel12.add( jLabel6, null );

        jPanel4.add( jPanel12, null );
        step1Panel.add( jPanel4, BorderLayout.EAST );

        this.addHelp( "<html><b>Select the method to " + "use for scoring gene sets.</b><br>" + "</html>" );
        this.addMain( step1Panel );
    }

    @Override
    public boolean isReady() {
        return true;
    }

    void corrButton_actionPerformed( ActionEvent e ) {
        wiz.setAnalysisType( Settings.CORR );
    }

    void resampButton_actionPerformed( ActionEvent e ) {
        wiz.setAnalysisType( Settings.RESAMP );
    }

    void oraButton_actionPerformed( ActionEvent e ) {
        wiz.setAnalysisType( Settings.ORA );
    }

    public void setValues() {
        if ( settings.getClassScoreMethod() == Settings.ORA )
            oraButton.setSelected( true );
        else if ( settings.getClassScoreMethod() == Settings.RESAMP )
            resampButton.setSelected( true );
        else if ( settings.getClassScoreMethod() == Settings.CORR )
            corrButton.setSelected( true );

        else if ( settings.getClassScoreMethod() == Settings.ROC ) rocButton.setSelected( true );
    }

    public void saveValues() {
        if ( oraButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.ORA );
        } else if ( resampButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.RESAMP );
        } else if ( corrButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.CORR );
        } else if ( rocButton.isSelected() ) {
            settings.setClassScoreMethod( Settings.ROC );
        }
    }
}

class AnalysisWizardStep1_oraButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep1 adaptee;

    AnalysisWizardStep1_oraButton_actionAdapter( AnalysisWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.oraButton_actionPerformed( e );
    }
}

class AnalysisWizardStep1_corrButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep1 adaptee;

    AnalysisWizardStep1_corrButton_actionAdapter( AnalysisWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.corrButton_actionPerformed( e );
    }
}

class AnalysisWizardStep1_resampButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep1 adaptee;

    AnalysisWizardStep1_resampButton_actionAdapter( AnalysisWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.resampButton_actionPerformed( e );
    }
}