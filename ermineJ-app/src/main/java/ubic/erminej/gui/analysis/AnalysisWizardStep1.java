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
import ubic.erminej.SettingsHolder.GeneScoreMethod;
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
    private JRadioButton prButton;
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
            if ( settings.getGeneSetResamplingScoreMethod().equals( GeneScoreMethod.PRECISIONRECALL ) ) {
                settings.setGeneSetResamplingScoreMethod( GeneScoreMethod.MEAN ); // default.
            }
        } else if ( corrButton.isSelected() ) {
            settings.setClassScoreMethod( SettingsHolder.Method.CORR );
        } else if ( rocButton.isSelected() ) {
            settings.setClassScoreMethod( SettingsHolder.Method.ROC );
        } else if ( prButton.isSelected() ) {
            settings.setClassScoreMethod( SettingsHolder.Method.GSR );
            settings.setGeneSetResamplingScoreMethod( GeneScoreMethod.PRECISIONRECALL );
        }
    }

    public void setValues() {
        if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.ORA ) ) {
            oraButton.setSelected( true );
        } else if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.GSR ) ) {
            if ( settings.getGeneSetResamplingScoreMethod().equals( GeneScoreMethod.PRECISIONRECALL ) ) {
                prButton.setSelected( true );
            } else {
                resampButton.setSelected( true );
            }
        } else if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) ) {
            corrButton.setSelected( true );
        } else if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.ROC ) ) {
            rocButton.setSelected( true );
        }
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
        prButton = new JRadioButton();

        oraButton.setText( "ORA" );
        oraButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        oraButton.addActionListener( new AnalysisWizardStep1_oraButton_actionAdapter( this ) );
        buttonGroup1.add( oraButton );

        resampButton.setText( "GSR" );
        resampButton.setSelected( true );
        resampButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        resampButton.addActionListener( new AnalysisWizardStep1_resampButton_actionAdapter( this ) );
        buttonGroup1.add( resampButton );

        rocButton.setText( "ROC" );
        rocButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        rocButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                wiz.setAnalysisType( SettingsHolder.Method.ROC );
            }
        } );
        buttonGroup1.add( rocButton );

        prButton.setText( "PREREC" );
        prButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        prButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                wiz.setAnalysisType( SettingsHolder.Method.GSR );
                // FIXME
            }
        } );
        buttonGroup1.add( prButton );

        corrButton.setText( "CORR" );
        corrButton.setBorder( new EmptyBorder( 0, 0, 0, 20 ) );
        corrButton.addActionListener( new AnalysisWizardStep1_corrButton_actionAdapter( this ) );
        buttonGroup1.add( corrButton );
        // corrPanel.add( corrButton );

        JLabel gsrLabel = new JLabel();
        JLabel corrLabel = new JLabel();
        JLabel oraLabel = new JLabel();
        JLabel rocLabel = new JLabel();
        JLabel prLabel = new JLabel();

        oraLabel.setText( "Over-representation analysis" );
        oraLabel.setPreferredSize( new Dimension( 200, 17 ) );
        oraLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );

        gsrLabel.setText( "Examines distribution of gene scores in each set" );
        gsrLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );
        gsrLabel.setPreferredSize( new Dimension( 200, 17 ) );

        rocLabel.setText( "Uses ranks of gene scores to compute ROC" );
        rocLabel.setPreferredSize( new Dimension( 200, 17 ) );
        rocLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );

        prLabel.setText( "Use ranks of gene scores to compute precision-recall" );
        prLabel.setPreferredSize( new Dimension( 200, 17 ) );
        prLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );

        corrLabel.setText( "Uses correlation of expression profiles" );
        corrLabel.setPreferredSize( new Dimension( 200, 17 ) );
        corrLabel.setAlignmentX( Component.RIGHT_ALIGNMENT );

        GroupLayout gl = new GroupLayout( step1Panel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        step1Panel.setLayout( gl );
        step1Panel.setBorder( BorderFactory.createEmptyBorder( 20, 40, 20, 40 ) );
        gl.setHorizontalGroup( gl
                .createSequentialGroup()
                .addGroup(
                        gl.createParallelGroup( Alignment.LEADING ).addComponent( resampButton )
                                .addComponent( oraButton ).addComponent( rocButton ).addComponent( prButton )
                                .addComponent( corrButton ) )
                .addGroup(
                        gl.createParallelGroup( Alignment.LEADING ).addComponent( gsrLabel ).addComponent( oraLabel )
                                .addComponent( rocLabel ).addComponent( prLabel ).addComponent( corrLabel ) ) );

        gl.setVerticalGroup( gl
                .createSequentialGroup()
                .addGroup(
                        gl.createParallelGroup( Alignment.BASELINE ).addComponent( resampButton )
                                .addComponent( gsrLabel ) )
                .addGroup(
                        gl.createParallelGroup( Alignment.BASELINE ).addComponent( oraButton ).addComponent( oraLabel ) )
                .addGroup(
                        gl.createParallelGroup( Alignment.BASELINE ).addComponent( rocButton ).addComponent( rocLabel ) )
                .addGroup( gl.createParallelGroup( Alignment.BASELINE ).addComponent( prButton ).addComponent( prLabel ) )
                .addGroup(
                        gl.createParallelGroup( Alignment.BASELINE ).addComponent( corrButton )
                                .addComponent( corrLabel ) ) );

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
        wiz.setGeneScoreMethod( GeneScoreMethod.MEAN ); // this isn't actually used, it's just to let us know that it's
                                                        // NOT precision-recall.
    }

    void prButton_actionPerformed() {
        wiz.setAnalysisType( SettingsHolder.Method.GSR );
        wiz.setGeneScoreMethod( GeneScoreMethod.PRECISIONRECALL );
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
