/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.gui.geneset.details;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;

import ubic.erminej.gui.file.DataFileFilter;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.MatrixPreviewer;

/**
 * Similar to JGeneScoreFileChooser
 * 
 * @author paul
 * @version $Id$
 */
public class JRawFileChooser extends JFileChooser {

    JRawFileChooserOptions m_options;

    JTextField dataColTextField = new JTextField();

    public JRawFileChooser( String startPath, int currentSettingForDataColumn ) {
        super( startPath );

        // Create a file filter for the file chooser
        DataFileFilter dataFileFilter = new DataFileFilter();
        super.setFileFilter( dataFileFilter );
        super.setAcceptAllFileFilterUsed( false );
        m_options = new JRawFileChooserOptions( this, currentSettingForDataColumn );
        super.setAccessory( m_options );

        if ( StringUtils.isNotBlank( startPath ) && new File( startPath ).exists() ) {
            this.setSelectedFile( new File( startPath ) );
        }
    }

    /**
     * The column number where the data to import starts.
     * 
     * @return
     */
    public int getStartColumn() {
        return Integer.valueOf( dataColTextField.getText() );
    }

    /**
     * The accessory component for the file chooser.
     * <p>
     * Lets the user change save options
     */
    private class JRawFileChooserOptions extends JComponent {

        private GridLayout gridLayout1 = new GridLayout( 2, 2 );

        private JRawFileChooser fileChooser;

        private int skipColumn;

        public JRawFileChooserOptions( JRawFileChooser fileChooser, int skipColumn ) throws HeadlessException {
            try {
                this.fileChooser = fileChooser;
                this.skipColumn = skipColumn; // initial guess.
                jbInit();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        private void jbInit() {
            setLayout( gridLayout1 );
            JLabel m_titleLabel = new JLabel();
            m_titleLabel.setHorizontalAlignment( SwingConstants.CENTER );
            m_titleLabel.setText( "Options:" );

            this.add( m_titleLabel );

            this.add( setUpDataColumnField() );
        }

        /**
         * @return
         */
        private JPanel setUpDataColumnField() {
            JPanel dataColumnPanel = new JPanel();
            dataColumnPanel.setPreferredSize( new Dimension( 120, 220 ) );
            JLabel jLabel4 = new JLabel();
            jLabel4.setText( "First data column:" );
            jLabel4.setLabelFor( dataColumnPanel );

            dataColTextField = new JTextField();
            dataColTextField.setPreferredSize( new Dimension( 50, 19 ) );
            dataColTextField.setHorizontalAlignment( SwingConstants.LEFT ); // moves textbox text to the right
            dataColTextField.setText( skipColumn + "" );
            dataColTextField
                    .setToolTipText( "Column of the data file where the data starts. This must be a value of 2 or higher." );
            dataColTextField.setEditable( true );

            dataColumnPanel.add( jLabel4 );
            dataColumnPanel.add( dataColTextField, BorderLayout.NORTH );
            JButton dataPreviewButton = new JButton( "Preview" );
            dataPreviewButton.setToolTipText( "Preview the data to be imported; limited to first few rows and columns" );
            dataPreviewButton.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed( ActionEvent e ) {
                    if ( fileChooser.getSelectedFile() == null ) {
                        GuiUtil.error( "You must choose a file to preview" );
                        return;
                    }

                    String file = fileChooser.getSelectedFile().getAbsolutePath();

                    try {
                        MatrixPreviewer.previewMatrix( null, file, -1 );
                    } catch ( Exception e1 ) {
                        GuiUtil.error( "Error previewing data: " + e1.getMessage(), e1 );
                        return;
                    }
                }
            } );
            dataColumnPanel.add( dataPreviewButton, BorderLayout.SOUTH );
            return dataColumnPanel;
        }
    } // end private class JDataFileChooserOptions

} // end class JDetailsFileChooser

