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
package ubic.erminej.gui.geneset;

// for JDetailsFileChooser
// for JDetailsFileChooserOptions (the file chooser accessory)
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ubic.erminej.gui.file.DataFileFilter;

/**
 * @author Will Braynen
 * @version $Id$
 */
public class JDataFileChooser extends JFileChooser {

    // fields
    JDataFileChooserOptions m_options;

    public JDataFileChooser( boolean includeEverything, boolean normalize, String initialFileName ) {

        super();

        // Create a file filter for the file chooser
        DataFileFilter dataFileFilter = new DataFileFilter();
        super.setFileFilter( dataFileFilter );
        super.setAcceptAllFileFilterUsed( false );
        if ( initialFileName != null ) super.setSelectedFile( new File( initialFileName ) );

        // Create other save options (e.g. include row and column labels in image)
        m_options = new JDataFileChooserOptions( includeEverything, normalize );
        super.setAccessory( m_options );
    }

    public boolean normalized() {
        return m_options.normalized();
    }

    public boolean includeAnnotations() {
        return m_options.includeAnnotations();
    }

    /**
     * The accessory component for the file chooser.
     * <p>
     * Lets the user change save options and might also in the future include a preview of what the matrix image will
     * look like with the chosen options.
     */
    private class JDataFileChooserOptions extends JComponent {

        private JCheckBox includeMatrixValuesCheckBox = new JCheckBox( "Include annotations and scores" );

        private JCheckBox m_normalize = new JCheckBox( "Normalize" );

        private GridLayout gridLayout1 = new GridLayout( 12, 2 );
        private JLabel m_titleLabel = new JLabel();
        private JLabel m_spacerLabel = new JLabel();

        public JDataFileChooserOptions( boolean includeEverything, boolean normalize ) throws HeadlessException {
            try {
                includeMatrixValuesCheckBox.setSelected( includeEverything );
                m_normalize.setSelected( normalize );
                jbInit();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        private void jbInit() throws Exception {
            setLayout( gridLayout1 );
            includeMatrixValuesCheckBox
                    .setToolTipText( "Leave this box unchecked if you just want the expression value matrix" );
            m_normalize.setToolTipText( "Check this box to normalize the rows of the data matrix." );
            this.setMaximumSize( new Dimension( 264, 63 ) );
            this.setMinimumSize( new Dimension( 264, 63 ) );
            m_titleLabel.setHorizontalAlignment( SwingConstants.CENTER );
            m_titleLabel.setText( "Save Options:" );
            m_spacerLabel.setText( "" );
            gridLayout1.setColumns( 2 );
            gridLayout1.setHgap( 5 );
            gridLayout1.setRows( 9 );
            gridLayout1.setVgap( 5 );
            this.add( m_spacerLabel, null );
            this.add( m_titleLabel, null );
            this.add( includeMatrixValuesCheckBox, null );
            this.add( m_normalize, null );
        }

        public boolean normalized() {
            return m_normalize.isSelected();
        }

        public boolean includeAnnotations() {
            return includeMatrixValuesCheckBox.isSelected();
        }

    } // end private class JDataFileChooserOptions

} // end class JDetailsFileChooser

