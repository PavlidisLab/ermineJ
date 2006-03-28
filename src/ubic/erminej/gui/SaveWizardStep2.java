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

import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ubic.basecode.gui.WizardStep;

/**
 * Choose the file name to save the results in.
 * 
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */

public class SaveWizardStep2 extends WizardStep {
    private SaveWizard wiz = null;
    private JFileChooser chooser = null;
    private JTextField saveFile = null;
    private final String folder;
    private JCheckBox saveAllGenes = null;

    public SaveWizardStep2( SaveWizard wiz, String folder ) {
        super( wiz );
        this.folder = folder;
        this.jbInit();
        this.wiz = wiz;

        wiz.clearStatus();
    }

    // Component initialization
    protected void jbInit() {
        JPanel jPanel11 = new JPanel();
        jPanel11.setPreferredSize( new Dimension( 330, 50 ) );
        jPanel11.setBackground( SystemColor.control );
        JLabel jLabel3 = new JLabel();
        jLabel3.setText( "Save file:" );
        jLabel3.setPreferredSize( new Dimension( 320, 15 ) );
        saveFile = new JTextField();
        saveFile.setPreferredSize( new Dimension( 280, 19 ) );
        JButton saveBrowseButton = new JButton();
        saveBrowseButton.addActionListener( new SaveWizardStep2_saveBrowseButton_actionAdapter( this ) );
        saveBrowseButton.setText( "Browse..." );
        chooser = new JFileChooser();
        chooser.setCurrentDirectory( new File( folder ) );
        chooser.setApproveButtonText( "OK" );
        chooser.setDialogTitle( "Save Analysis As:" );
        jPanel11.add( jLabel3, null );
        jPanel11.add( saveFile, null );
        jPanel11.add( saveBrowseButton, null );

        saveAllGenes = new JCheckBox();
        saveAllGenes.setSelected( false );
        JLabel saveAllGenesLabel = new JLabel();
        saveAllGenesLabel.setText( "Include all genes in output" );
        saveAllGenesLabel.setLabelFor( saveAllGenes );

        jPanel11.add( saveAllGenesLabel );
        jPanel11.add( saveAllGenes );

        this.addHelp( "<html><b>Choose the file to save the analysis in</b><br>" + "" );
        this.addMain( jPanel11 );
    }

    public boolean isReady() {
        return true;
    }

    public boolean getShouldSaveGeneNames() {
        return this.saveAllGenes.isSelected();
    }

    void saveBrowseButton_actionPerformed( ActionEvent e ) {
        int result = chooser.showOpenDialog( this.wiz );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            File f = new File( chooser.getSelectedFile().toString() );

            if ( f.exists() ) {
                int k = JOptionPane.showConfirmDialog( this, "That file exists. Overwrite?", "File exists",
                        JOptionPane.YES_NO_CANCEL_OPTION );
                if ( k == JOptionPane.YES_OPTION ) {
                    saveFile.setText( chooser.getSelectedFile().toString() );
                } else if ( k == JOptionPane.NO_OPTION ) { // go back to the chooser for another try.
                    saveBrowseButton_actionPerformed( null );
                } // otherwise, bail.
            } else {
                saveFile.setText( chooser.getSelectedFile().toString() );
            }

        }
    }

    public String getSaveFileName() {
        return saveFile.getText();
    }
}

class SaveWizardStep2_saveBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    SaveWizardStep2 adaptee;

    SaveWizardStep2_saveBrowseButton_actionAdapter( SaveWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.saveBrowseButton_actionPerformed( e );
    }
}