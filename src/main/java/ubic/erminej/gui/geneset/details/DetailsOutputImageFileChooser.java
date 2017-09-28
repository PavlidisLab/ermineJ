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
package ubic.erminej.gui.geneset.details;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ubic.erminej.gui.file.ImageFileFilter;

/**
 * <p>
 * DetailsOutputImageFileChooser class.
 * </p>
 *
 * @author Will Braynen
 * @version $Id$
 */
public class DetailsOutputImageFileChooser extends JFileChooser {

    /**
     * The accessory component for the file chooser.
     * <p>
     * Lets the user change save options and might also in the future include a preview of what the matrix image will
     * look like with the chosen options.
     */
    class JDetailsFileChooserOptions extends JComponent {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        JCheckBox m_includeLabels = new JCheckBox( "Include row and column labels" );
        JCheckBox m_normalize = new JCheckBox( "Standardize" );

        GridLayout gridLayout1 = new GridLayout( 12, 2 );
        JLabel m_titleLabel = new JLabel();
        JLabel m_spacerLabel = new JLabel();

        public JDetailsFileChooserOptions( boolean includeLabels, boolean standardize ) throws HeadlessException {
            try {
                m_includeLabels.setSelected( includeLabels );
                m_normalize.setSelected( standardize );
                jbInit();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        public boolean includeLabels() {
            return m_includeLabels.isSelected();
        }

        public boolean normalized() {
            return m_normalize.isSelected();
        }

        private void jbInit() {
            setLayout( gridLayout1 );
            this.setMinimumSize( new Dimension( 264, 63 ) );
            m_includeLabels.setToolTipText( "Leave this box unchecked if you just want the data matrix without labels" );
            m_normalize
                    .setToolTipText( "Check this box to standardize the rows of the data matrix before creating the image." );
            m_titleLabel.setHorizontalAlignment( SwingConstants.CENTER );
            m_titleLabel.setText( "Save Options:" );
            m_spacerLabel.setText( "" );
            gridLayout1.setColumns( 2 );
            gridLayout1.setHgap( 5 );
            gridLayout1.setRows( 9 );
            gridLayout1.setVgap( 5 );
            this.add( m_spacerLabel, null );
            this.add( m_titleLabel, null );
            this.add( m_includeLabels, null );
            this.add( m_normalize, null );
        }
    }

    private static final long serialVersionUID = -1L;

    JDetailsFileChooserOptions m_options;

    /**
     * Creates a new instance of JDetailsFileChooser
     *
     * @param includeLabels a boolean.
     * @param normalize a boolean.
     * @param initialFileName a {@link java.lang.String} object.
     */
    public DetailsOutputImageFileChooser( boolean includeLabels, boolean normalize, String initialFileName ) {

        super();
        // Create a file filter for the file chooser
        ImageFileFilter imageFileFilter = new ImageFileFilter();
        imageFileFilter.setDescription( "PNG files" );
        super.setFileFilter( imageFileFilter );
        super.setAcceptAllFileFilterUsed( false );
        if ( initialFileName != null ) this.setSelectedFile( new File( initialFileName ) );
        // Create other save options (e.g. include row and column labels in image)
        m_options = new JDetailsFileChooserOptions( includeLabels, normalize );
        super.setAccessory( m_options );
    }

    /**
     * <p>
     * includeLabels.
     * </p>
     *
     * @return a boolean.
     */
    public boolean includeLabels() {
        return m_options.includeLabels();
    }

    /**
     * <p>
     * normalized.
     * </p>
     *
     * @return a boolean.
     */
    public boolean normalized() {
        return m_options.normalized();
    }
}
