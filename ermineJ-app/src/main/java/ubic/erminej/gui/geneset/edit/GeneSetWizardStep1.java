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
package ubic.erminej.gui.geneset.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ubic.erminej.SettingsHolder;
import ubic.erminej.gui.util.WizardStep;

/**
 * Choose where the user is getting the information for the new gene set: either a file, or manual entry.
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class GeneSetWizardStep1 extends WizardStep {

    private static final long serialVersionUID = -2352380414699946183L;
    private JButton browseButton;
    private JTextField classFile;
    private JFileChooser chooser;
    int inputMethod;

    public GeneSetWizardStep1( GeneSetWizard wiz, SettingsHolder settings ) {
        super( wiz );
        this.jbInit();
        chooser = new JFileChooser();
        chooser.setCurrentDirectory( new File( settings.getUserGeneSetDirectory() ) );
        chooser.setDialogTitle( "Choose Gene Set File" );
        wiz.clearStatus();
    }

    // Component initialization
    @Override
    protected void jbInit() {
        JPanel step1Panel = new JPanel();
        step1Panel.setLayout( new BorderLayout() );

        // top
        JPanel jPanel1 = new JPanel();
        JPanel jPanel7 = new JPanel(); // outer method choice

        jPanel7.setLayout( new GridBagLayout() );
        JLabel jLabel8 = new JLabel(); // 'choose method'
        jLabel8.setText( "Choose the method of data entry:" );
        jLabel8.setMaximumSize( new Dimension( 999, 15 ) );
        jLabel8.setMinimumSize( new Dimension( 259, 15 ) );
        jLabel8.setPreferredSize( new Dimension( 259, 15 ) );

        JPanel jPanel4 = new JPanel(); // holds radio buttons
        jPanel4.setBorder( BorderFactory.createEtchedBorder() );
        jPanel4.setLayout( new GridBagLayout() );
        JRadioButton fileInputButton = new JRadioButton();
        fileInputButton.setBackground( SystemColor.control );
        fileInputButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        fileInputButton.setText( "File" );
        fileInputButton.addActionListener( new GeneSetWizardStep1_fileInputButton_actionAdapter( this ) );
        JRadioButton manInputButton = new JRadioButton( "Manual", true );
        manInputButton.setBackground( SystemColor.control );
        manInputButton.setMaximumSize( new Dimension( 91, 23 ) );
        manInputButton.addActionListener( new GeneSetWizardStep1_manInputButton_actionAdapter( this ) );
        manInputButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
        ButtonGroup buttonGroup1 = new ButtonGroup();
        jPanel1.setPreferredSize( new Dimension( 364, 130 ) );
        buttonGroup1.add( fileInputButton );
        buttonGroup1.add( manInputButton );
        JLabel jLabel4 = new JLabel();
        jLabel4.setText( "File with gene or probe symbols" );
        JLabel jLabel5 = new JLabel();
        jLabel5.setText( "Enter manually in the next step" );
        jPanel4.add( jLabel5, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets( 0, 16, 8, 10 ), 125, 10 ) );
        jPanel4.add( jLabel4, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets( 3, 16, 0, 10 ), 30, 10 ) );
        jPanel4.add( manInputButton, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets( 0, 9, 8, 0 ), 8, 12 ) );
        jPanel4.add( fileInputButton, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets( 3, 9, 0, 0 ), 26, 12 ) );
        jPanel7.add( jLabel8, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets( 6, 21, 0, 74 ), 0, 0 ) );
        jPanel7.add( jPanel4, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets( 6, 10, 12, 16 ), -1, 8 ) );
        jPanel1.add( jPanel7, null );

        // bottom
        JPanel jPanel2 = new JPanel(); // holds file chooser
        jPanel2.setPreferredSize( new Dimension( 354, 100 ) );
        browseButton = new JButton();
        browseButton.setText( "Browse ..." );
        browseButton.addActionListener( new GeneSetWizardStep1_browseButton_actionAdapter( this ) );
        browseButton.setEnabled( false );
        classFile = new JTextField();
        classFile.setEditable( false );
        classFile.setPreferredSize( new Dimension( 230, 19 ) );
        classFile.setToolTipText( "File containing gene set members" );
        classFile.setText( "File containing gene set members" );
        jPanel2.add( browseButton, null );
        jPanel2.add( classFile, null );

        step1Panel.add( jPanel1, BorderLayout.CENTER );
        step1Panel.add( jPanel2, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Choose the source of the genes for the new class</b><br>"
                + "You can load them in from a file, or add them from the list of available probes." );
        this.addMain( step1Panel );
    }

    @Override
    public boolean isReady() {
        return true;
    }

    void manInputButton_actionPerformed() {
        classFile.setEditable( false );
        classFile.setEnabled( false );
        browseButton.setEnabled( false );
        inputMethod = 0;
    }

    void fileInputButton_actionPerformed() {
        classFile.setEditable( true );
        classFile.setEnabled( true );
        browseButton.setEnabled( true );
        inputMethod = 1;
    }

    void browseButton_actionPerformed() {
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            classFile.setText( chooser.getSelectedFile().toString() );
        }
    }

    public int getInputMethod() {
        return inputMethod;
    }

    public String getLoadFile() {
        return classFile.getText();
    }
}

class GeneSetWizardStep1_manInputButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep1 adaptee;

    GeneSetWizardStep1_manInputButton_actionAdapter( GeneSetWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.manInputButton_actionPerformed();
    }
}

class GeneSetWizardStep1_fileInputButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep1 adaptee;

    GeneSetWizardStep1_fileInputButton_actionAdapter( GeneSetWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.fileInputButton_actionPerformed();
    }
}

class GeneSetWizardStep1_browseButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep1 adaptee;

    GeneSetWizardStep1_browseButton_actionAdapter( GeneSetWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.browseButton_actionPerformed();
    }
}
