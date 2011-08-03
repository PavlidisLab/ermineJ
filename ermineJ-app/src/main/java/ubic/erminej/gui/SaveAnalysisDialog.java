/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ubic.erminej.Settings;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.gui.util.GuiUtil;

/**
 * Choose the run and whether the gene symbols should be included in the output.
 * 
 * @author paul
 * @version $Id$
 */
public class SaveAnalysisDialog extends JDialog {

    private Settings settings;

    private List<GeneSetPvalRun> runData;

    private JComboBox runComboBox = new JComboBox();

    private JCheckBox saveAllGenes = new JCheckBox();

    public SaveAnalysisDialog( Frame owner, List<GeneSetPvalRun> runData, Settings settings, int initialSelection ) {
        super( owner );
        this.setModal( true );
        this.runData = runData;
        this.settings = settings;
        jbInit();
        GuiUtil.centerContainer( this );
        pack();
        runComboBox.setSelectedIndex( initialSelection );
        this.setVisible( true );
    }

    public int getSelectedRunNum() {
        return runComboBox.getSelectedIndex();
    }

    public boolean isSaveAllGenes() {
        return this.saveAllGenes.isSelected();
    }

    public boolean wasCancelled() {
        return this.cancelled;
    }

    private boolean cancelled = true;

    private void jbInit() {
        this.setTitle( "Set options for saving" );
        this.setSize( new Dimension( 400, 400 ) );
        this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );

        JPanel runPanel = new JPanel();
        runPanel.setLayout( new BorderLayout() );

        JPanel topPanel = new JPanel();
        topPanel.setBorder( BorderFactory.createTitledBorder( "Choose the analysis to save:" ) );

        runComboBox.setPreferredSize( new Dimension( 250, 19 ) );
        topPanel.add( runComboBox );

        runPanel.add( topPanel, BorderLayout.NORTH );

        JPanel jPanel11 = new JPanel();

        saveAllGenes.setSelected( false ); // FIXME save this setting.
        JLabel saveAllGenesLabel = new JLabel();
        saveAllGenesLabel.setText( "Include all genes in output" );
        saveAllGenesLabel.setLabelFor( saveAllGenes );

        jPanel11.add( saveAllGenesLabel );
        jPanel11.add( saveAllGenes );

        JPanel buttonPanel = makeButtonPanel();

        this.add( runPanel, BorderLayout.NORTH );
        this.add( jPanel11, BorderLayout.CENTER );
        this.add( buttonPanel, BorderLayout.SOUTH );

        showChoices();
    }

    private JPanel makeButtonPanel() {
        JButton cancelButton = new JButton();
        JButton okButton = new JButton();
        cancelButton.setText( "Cancel" );
        cancelButton.setMnemonic( 'c' );
        okButton.setText( "OK" );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add( cancelButton );
        buttonPanel.add( okButton );
        cancelButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                cancelled = true;
                dispose();
            }
        } );

        okButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                cancelled = false;
                dispose();
            }
        } );
        return buttonPanel;
    }

    void showChoices() {
        if ( runData == null || runData.size() < 1 ) {
            runComboBox.addItem( "No runs available to save" );
        } else {
            for ( int i = 0; i < runData.size(); i++ ) {
                runComboBox.insertItemAt( runData.get( i ).getName(), i );
            }
            runComboBox.setSelectedIndex( 0 );
        }
    }

}
