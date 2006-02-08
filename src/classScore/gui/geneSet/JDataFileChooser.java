/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScore.gui.geneSet;

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

import baseCode.gui.file.DataFileFilter;

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

