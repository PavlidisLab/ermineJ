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
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout( new BorderLayout() );
        mainPanel.setBackground( Color.BLUE );

        JRadioButton fileInputButton = new JRadioButton();
        fileInputButton.setText( "File" );
        fileInputButton.addActionListener( new GeneSetWizardStep1_fileInputButton_actionAdapter( this ) );
        JRadioButton manInputButton = new JRadioButton( "Manual", true );
        manInputButton.addActionListener( new GeneSetWizardStep1_manInputButton_actionAdapter( this ) );
        ButtonGroup buttonGroup1 = new ButtonGroup();

        buttonGroup1.add( fileInputButton );
        buttonGroup1.add( manInputButton );

        JLabel jLabel4 = new JLabel();
        jLabel4.setText( "File with gene or probe symbols" );
        JLabel jLabel5 = new JLabel();
        jLabel5.setText( "Enter manually in the next step" );

        JPanel methodChooserPanel = new JPanel(); // holds radio buttons
        methodChooserPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(),
                "Choose the method of data entry" ) );

        methodChooserPanel.setMinimumSize( new Dimension( 260, 240 ) );
        GroupLayout gl = new GroupLayout( methodChooserPanel );
        methodChooserPanel.setLayout( gl );
        gl.setHorizontalGroup( gl
                .createSequentialGroup()
                .addGap( 5 )
                .addGroup(
                        gl.createParallelGroup( Alignment.LEADING ).addComponent( fileInputButton )
                                .addComponent( manInputButton ) )
                .addGroup( gl.createParallelGroup( Alignment.LEADING ).addComponent( jLabel4 ).addComponent( jLabel5 ) )
                .addGap( 5 ) );
        gl.setVerticalGroup( gl
                .createSequentialGroup()
                .addGap( 5 )
                .addGroup(
                        gl.createParallelGroup( Alignment.BASELINE ).addComponent( fileInputButton )
                                .addComponent( jLabel4 ) )
                .addGroup(
                        gl.createParallelGroup( Alignment.BASELINE ).addComponent( manInputButton )
                                .addComponent( jLabel5 ) ).addGap( 5 ) );

        // bottom
        JPanel fileBrowsePanel = new JPanel(); // holds file chooser
        fileBrowsePanel.setPreferredSize( new Dimension( 354, 100 ) );
        browseButton = new JButton();
        browseButton.setText( "Browse ..." );
        browseButton.addActionListener( new GeneSetWizardStep1_browseButton_actionAdapter( this ) );
        browseButton.setEnabled( false );
        classFile = new JTextField();
        classFile.setEditable( false );
        classFile.setMinimumSize( new Dimension( 230, 19 ) );
        classFile.setToolTipText( "File containing gene set members" );
        classFile.setText( "File containing gene set members" );
        fileBrowsePanel.add( browseButton, null );
        fileBrowsePanel.add( classFile, null );

        mainPanel.add( methodChooserPanel, BorderLayout.CENTER );
        mainPanel.add( fileBrowsePanel, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Choose the source of the genes for the new class</b><br>"
                + "You can load them in from a file, or add them from the list of available probes." );
        this.addMain( mainPanel );
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

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.manInputButton_actionPerformed();
    }
}

class GeneSetWizardStep1_fileInputButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep1 adaptee;

    GeneSetWizardStep1_fileInputButton_actionAdapter( GeneSetWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.fileInputButton_actionPerformed();
    }
}

class GeneSetWizardStep1_browseButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep1 adaptee;

    GeneSetWizardStep1_browseButton_actionAdapter( GeneSetWizardStep1 adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.browseButton_actionPerformed();
    }
}
