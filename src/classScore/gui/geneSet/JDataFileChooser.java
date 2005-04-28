package classScore.gui.geneSet;

// for JDetailsFileChooser
// for JDetailsFileChooserOptions (the file chooser accessory)
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import baseCode.gui.file.DataFileFilter;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class JDataFileChooser extends JFileChooser {

    // fields
    JDataFileChooserOptions m_options;

    public JDataFileChooser( boolean includeEverything, boolean normalize ) {

        super();

        // Create a file filter for the file chooser
        DataFileFilter dataFileFilter = new DataFileFilter();
        super.setFileFilter( dataFileFilter );
        super.setAcceptAllFileFilterUsed( false );

        // Create other save options (e.g. include row and column labels in image)
        m_options = new JDataFileChooserOptions( includeEverything, normalize );
        super.setAccessory( m_options );
    }

    public boolean normalized() {
        return m_options.normalized();
    }

    public boolean includeEverything() {
        return m_options.includeEverything();
    }

    /**
     * The accessory component for the file chooser.
     * <p>
     * Lets the user change save options and might also in the future include a preview of what the matrix image will
     * look like with the chosen options.
     */
    private class JDataFileChooserOptions extends JComponent {

        JCheckBox m_includeEverything = new JCheckBox( "Include annotations and scores" );

        JCheckBox m_normalize = new JCheckBox( "Normalize" );

        GridLayout gridLayout1 = new GridLayout( 12, 2 );
        JLabel m_titleLabel = new JLabel();
        JLabel m_spacerLabel = new JLabel();

        public JDataFileChooserOptions( boolean includeEverything, boolean normalize ) throws HeadlessException {
            try {
                m_includeEverything.setSelected( includeEverything );
                m_normalize.setSelected( normalize );
                jbInit();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        private void jbInit() throws Exception {
            setLayout( gridLayout1 );
            m_includeEverything
                    .setToolTipText( "Leave this box unchecked if you just want the expression value matrix" );
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
            this.add( m_includeEverything, null );
            this.add( m_normalize, null );
        }

        public boolean normalized() {
            return m_normalize.isSelected();
        }

        public boolean includeEverything() {
            return m_includeEverything.isSelected();
        }

    } // end private class JDataFileChooserOptions

} // end class JDetailsFileChooser

