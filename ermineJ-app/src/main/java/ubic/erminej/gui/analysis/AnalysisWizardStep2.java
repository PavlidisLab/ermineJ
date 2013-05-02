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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.commons.lang.StringUtils;

import ubic.basecode.dataStructure.matrix.StringMatrix;
import ubic.basecode.util.FileTools;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneScores;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.MatrixPreviewer;
import ubic.erminej.gui.util.WizardStep;

/**
 * Set the inputs for the analysis - score file, raw data file.
 * 
 * @author Kiran Keshav
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class AnalysisWizardStep2 extends WizardStep implements KeyListener {

    private static final long serialVersionUID = -1L;

    // test method
    public static void main( String[] args ) throws Exception {

        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {

        }
        JFrame f = new JFrame();
        f.setSize( new Dimension( 400, 400 ) );

        AnalysisWizardStep2 s = new AnalysisWizardStep2( null, new Settings() );
        s.setSize( new Dimension( 400, 400 ) );
        f.add( s );
        f.pack();
        GuiUtil.centerContainer( f );
        f.setVisible( true );
    }

    private AnalysisWizard wiz;
    private Settings settings;
    private JFileChooser chooser = new JFileChooser();
    private JTextField rawFileTextField;
    private JTextField scoreFileTextField;
    private JTextField scoreColTextField;
    private JPanel quickPickPanel = new JPanel();
    private JTextField dataColTextField;

    public AnalysisWizardStep2( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.wiz = wiz;
        this.jbInit();

        this.settings = settings;
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
        setValues();

        if ( wiz != null ) wiz.clearStatus();
    }

    public void dataPreviewScoresActionPerformed() {
        String filename = rawFileTextField.getText();
        if ( filename.length() != 0 && !FileTools.testFile( filename ) ) {
            wiz.showError( "You must choose a valid file to preview" );
            return;
        }

        try {
            MatrixPreviewer.previewMatrix( wiz, rawFileTextField.getText(), -1 );
        } catch ( IOException e ) {
            GuiUtil.error( "Error previewing data: " + e.getMessage(), e );
        }

    }

    public String getDataFileName() {
        return rawFileTextField.getText();
    }

    public Integer getDataStartColumn() {
        return Integer.valueOf( this.dataColTextField.getText() );
    }

    @Override
    public boolean isReady() {

        if ( wiz.getAnalysisType().equals( SettingsHolder.Method.CORR )
                && StringUtils.isBlank( rawFileTextField.getText() ) ) {
            wiz.showError( "Correlation analyses require a raw data file." );
            return false;
        } else if ( !wiz.getAnalysisType().equals( SettingsHolder.Method.CORR )
                && StringUtils.isBlank( scoreFileTextField.getText() ) ) {
            wiz.showError( "ORA, resampling and pre-re methods require a gene score file." );
            return false;
        }

        if ( ( !wiz.getAnalysisType().equals( SettingsHolder.Method.CORR ) )
                && Integer.valueOf( scoreColTextField.getText() ).intValue() < 2 ) {
            wiz.showError( "The score column must be 2 or higher" );
            return false;
        }

        // make sure we got at least one file that's readable.
        if ( rawFileTextField.getText().length() != 0 && !FileTools.testFile( rawFileTextField.getText() ) ) {
            wiz.showError( "The raw data file is not valid." );
            return false;
        }

        if ( scoreFileTextField.getText().length() != 0 && !FileTools.testFile( scoreFileTextField.getText() ) ) {
            wiz.showError( "The gene score file is not valid." );
            return false;
        }

        wiz.clearStatus();
        return true;
    }

    @Override
    public void keyPressed( KeyEvent e ) {
    }

    @Override
    public void keyReleased( KeyEvent e ) {
    }

    @Override
    public void keyTyped( KeyEvent e ) {
        wiz.clearStatus();
    }

    /**
     * Show a preview of the dat
     */
    public void previewScoresActionPerformed() {

        String filename = scoreFileTextField.getText();
        if ( filename.length() != 0 && !FileTools.testFile( filename ) ) {
            wiz.showError( "You must choose a valid file to preview" );
            return;
        }

        try {
            StringMatrix<String, String> testMatrix = MatrixPreviewer.previewMatrix( wiz, scoreFileTextField.getText(),
                    -1 );
            testMatrix.getColNames();
        } catch ( IOException e ) {
            GuiUtil.error( "Error previewing data: " + e.getMessage(), e );
        }

    }

    public void saveValues() {
        settings.setScoreCol( Integer.valueOf( scoreColTextField.getText() ) );
        settings.setScoreFile( scoreFileTextField.getText() );
        settings.setRawFile( rawFileTextField.getText() );
        settings.setDataCol( Integer.valueOf( dataColTextField.getText() ) );
        settings.setDataDirectory( chooser.getCurrentDirectory().toString() );
    }

    /**
     * @return
     */
    private JPanel setUpDataColumnField() {
        JPanel dataColumnPanel = new JPanel();
        dataColumnPanel.setPreferredSize( new Dimension( 120, 40 ) );
        JLabel jLabel4 = new JLabel();
        jLabel4.setText( "First data column:" );
        jLabel4.setLabelFor( dataColumnPanel );

        dataColTextField = new JTextField();
        dataColTextField.setPreferredSize( new Dimension( 50, 19 ) );
        dataColTextField.setHorizontalAlignment( SwingConstants.LEFT ); // moves textbox text to the right
        dataColTextField.setText( "2" );
        dataColTextField
                .setToolTipText( "Column of the data file where the data starts. This must be a value of 2 or higher." );
        dataColTextField.setEditable( true );

        dataColTextField.addKeyListener( this );
        dataColumnPanel.add( jLabel4 );
        dataColumnPanel.add( dataColTextField, BorderLayout.WEST );
        JButton dataPreviewButton = new JButton( "Preview" );
        dataPreviewButton.setToolTipText( "Preview the data to be imported; limited to first few rows and columns" );
        dataPreviewButton.addActionListener( new DataPreviewButtonAdapter( this ) );
        dataColumnPanel.add( dataPreviewButton, BorderLayout.EAST );
        return dataColumnPanel;
    }

    private JPanel setupDataMatrixFileChooser() {
        // panel 4
        JPanel rawDataPanel = new JPanel();

        TitledBorder rawDataFileTitleBorder = BorderFactory
                .createTitledBorder( "Data profiles file (optional for all but CORR, but used for visualization):" );
        rawDataPanel.setBorder( rawDataFileTitleBorder );
        rawDataPanel.setLayout( new GridLayout( 2, 1 ) );
        // file browser
        JPanel rawDataBrowsePanel = new JPanel();
        JButton rawFileBrowseButton = new JButton();
        rawFileBrowseButton.setEnabled( true );
        rawFileBrowseButton.setText( "Browse" );
        rawFileBrowseButton.addActionListener( new RawFileBrowse( this ) );
        rawFileTextField = new JTextField();
        rawFileTextField.setPreferredSize( new Dimension( 325, 19 ) );
        rawFileTextField.setMaximumSize( new Dimension( 500, 19 ) );

        rawDataBrowsePanel.setLayout( new BoxLayout( rawDataBrowsePanel, BoxLayout.X_AXIS ) );
        rawDataBrowsePanel.add( rawFileTextField );
        rawDataBrowsePanel.add( rawFileBrowseButton );

        // / --------------------------------------------

        JPanel dataColumnPanel = setUpDataColumnField();

        rawDataPanel.add( rawDataBrowsePanel );
        rawDataPanel.add( dataColumnPanel );
        return rawDataPanel;
    }

    JButton scoreFileBrowseButton = new JButton();

    @Override
    protected void jbInit() {
        // initialization
        JPanel step2Panel = new JPanel();
        step2Panel.setLayout( new GridLayout( 2, 1 ) );

        // top panel with score file setup.

        JPanel scoreFilePanel = new JPanel();
        scoreFilePanel.setLayout( new GridLayout( 2, 1 ) );

        TitledBorder geneScoreFileTitleBorder = BorderFactory
                .createTitledBorder( "Gene score file (optional for CORR):" );

        scoreFilePanel.setBorder( geneScoreFileTitleBorder );
        scoreFileBrowseButton.setEnabled( true );
        scoreFileBrowseButton.setText( "Browse" );
        scoreFileBrowseButton.addActionListener( new ScoreFileBrowse( this ) );
        scoreFileTextField = new JTextField();
        scoreFileTextField.setMaximumSize( new Dimension( 500, 19 ) );
        scoreFileTextField.setPreferredSize( new Dimension( 225, 19 ) );

        JPanel scoreFileBrowsePanel = new JPanel();
        scoreFileBrowsePanel.setLayout( new BoxLayout( scoreFileBrowsePanel, BoxLayout.X_AXIS ) );
        scoreFileBrowsePanel.add( scoreFileTextField, null );
        scoreFileBrowsePanel.add( scoreFileBrowseButton, null );

        /*
         * Column choice. Label, field, button in a row.
         */
        JPanel scoreColumnPanel = new JPanel();
        scoreColumnPanel.setPreferredSize( new Dimension( 120, 40 ) );
        JLabel jLabel3 = new JLabel();
        jLabel3.setText( "Column:" );
        jLabel3.setLabelFor( scoreColumnPanel );

        scoreColTextField = new JTextField();
        scoreColTextField.setPreferredSize( new Dimension( 50, 19 ) );
        scoreColTextField.setHorizontalAlignment( SwingConstants.LEFT ); // moves textbox text to the right
        scoreColTextField.setText( "2" );
        scoreColTextField
                .setToolTipText( "Column of the score file containing the scores. This must be a value of 2 or higher." );
        scoreColTextField.setEditable( true );

        scoreColTextField.addKeyListener( this );
        scoreColumnPanel.add( jLabel3, null );
        scoreColumnPanel.add( scoreColTextField, BorderLayout.WEST );
        JButton previewButton = new JButton( "Preview" );
        previewButton.setToolTipText( "Preview the scores to be imported" );
        previewButton.addActionListener( new PreviewButtonAdapter( this ) );
        scoreColumnPanel.add( previewButton, BorderLayout.EAST );

        // -----------------------------------------------------

        /*
         * FIXME we have to set this after we know what the analysis is.
         */

        scoreFilePanel.setLayout( new GridLayout( 3, 1 ) );

        // quickPickPanel.setLayout( new BoxLayout( quickPickPanel, BoxLayout.X_AXIS ) );
        JButton quickPickPanelButton = new JButton();
        quickPickPanelButton.setEnabled( true );
        quickPickPanelButton.setText( "Quick list" );
        quickPickPanelButton.addActionListener( new QuickPickEnter( this ) );
        quickPickPanel.add( quickPickPanelButton );
        scoreFilePanel.add( quickPickPanel );
        scoreFilePanel.add( scoreFileBrowsePanel );
        scoreFilePanel.add( scoreColumnPanel );
        quickPickPanel.setVisible( false );
        // -----------------------------------------------------

        JPanel rawDataPanel = setupDataMatrixFileChooser();

        // ------------------------------------------

        step2Panel.add( scoreFilePanel );
        step2Panel.add( rawDataPanel );
        this.addHelp( "<html><b>Choose the data files to use</b><br>"
                + "&quot;Gene scores&quot; refer to a score or p value "
                + " associated with each gene in your data set. This "
                + "file can have as few as two columns. &quot;Data profiles&quot;"
                + " refers to the expression or similar profile data, usually a large matrix. "
                + "Files must be tab-delimited text. For details, see the user manual." );
        this.addMain( step2Panel );

    }

    void rawBrowseButton_actionPerformed() {
        chooser.setCurrentDirectory( new File( settings.getRawDataFileDirectory() ) );
        chooser.setDialogTitle( "Choose Raw Data File" );
        wiz.clearStatus();
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            rawFileTextField.setText( chooser.getSelectedFile().toString() );
            settings.setDataDirectory( chooser.getCurrentDirectory().toString() );
        }
    }

    void scoreBrowseButton_actionPerformed() {
        chooser.setCurrentDirectory( new File( settings.getGeneScoreFileDirectory() ) );
        chooser.setDialogTitle( "Choose Gene Score File" );
        wiz.clearStatus();
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            scoreFileTextField.setText( chooser.getSelectedFile().toString() );
            settings.setDataDirectory( chooser.getCurrentDirectory().toString() );
        }

        validateFile();

    }

    /**
     * Check the score file
     */
    private void validateFile() {

        SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
            private GeneScores gs;
            AnalysisWizard w = ( AnalysisWizard ) getOwner();

            @Override
            protected Object doInBackground() {
                w.getStatusField().clear();
                try {
                    if ( gs == null ) {

                        String scoreFile = settings.getScoreFile();
                        gs = new GeneScores( scoreFile, settings, w.getStatusField(), w.getGeneAnnots() );
                    }
                } catch ( IOException e ) {
                    w.getStatusField().showError( "Error reading scores: " + e.getMessage(), e );
                }

                return null;
            }
        };

        sw.execute();
    }

    /**
     * Show a box for pasting in a list of genes.
     */
    void quickpickButton_actionPerformed() {
        wiz.getStatusField().clear();

        final JDialog frame = new JDialog( this.getOwner() );
        final JTextPane textPane = new JTextPane();
        textPane.setPreferredSize( new Dimension( 500, 500 ) );
        textPane.setCaretPosition( 0 );

        JPanel nameP = new JPanel();
        nameP.setLayout( new GridLayout( 1, 2 ) );
        final JTextField groupNameField = new JTextField();
        groupNameField.setPreferredSize( new Dimension( 300, 20 ) );
        nameP.add( new JLabel( "Group name (optional, used in file name)" ) );
        nameP.add( groupNameField );

        Style def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
        StyleConstants.setFontFamily( def, "SansSerif" );

        frame.setLayout( new BorderLayout() );
        frame.getContentPane().add( new JScrollPane( textPane ), BorderLayout.CENTER );
        frame.getContentPane().add( nameP, BorderLayout.NORTH );
        frame.setTitle( "Type or paste in a list of identifiers" );
        frame.setLocation( GuiUtil.chooseChildLocation( frame, this ) );

        JButton ok = new JButton( "OK" );
        JButton cancel = new JButton( "Cancel" );
        ok.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                getGeneScoresFromQuickList( textPane, groupNameField.getText() );
                frame.dispose();
            }
        } );

        cancel.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                frame.dispose();
            }
        } );

        /*
         * TODO make prettier
         */
        JPanel p = new JPanel();
        p.setLayout( new GridLayout( 1, 2 ) );
        p.add( ok );
        p.add( cancel );

        frame.add( p, BorderLayout.SOUTH );

        frame.pack();
        frame.setIconImage( new ImageIcon( this.getClass().getResource(
                MainFrame.RESOURCE_LOCATION + "logoInverse32.gif" ) ).getImage() );
        frame.setAlwaysOnTop( true );

        frame.setSize( new Dimension( 500, 500 ) );
        frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        frame.setVisible( true );
        textPane.requestFocusInWindow();

    }

    void setValues() {
        scoreColTextField.setText( String.valueOf( settings.getScoreCol() ) );
        scoreFileTextField.setText( settings.getScoreFile() );
        rawFileTextField.setText( settings.getRawDataFileName() );
        dataColTextField.setText( String.valueOf( settings.getDataCol() ) );

    }

    /**
     * TODO refactor out.
     * 
     * @param textPane
     * @param name optional
     */
    final private void getGeneScoresFromQuickList( final JTextPane textPane, String name ) {
        try {
            /*
             * Get the text, parse into a list of elements
             */
            String t = textPane.getDocument().getText( 0, textPane.getDocument().getLength() );

            if ( t.isEmpty() ) {
                wiz.showError( "You didn't provide any identifiers" );
                scoreFileTextField.setText( null );
                settings.setScoreFile( null );
                return;
            }

            String[] fa = StringUtils.split( t, "\n\t ,|" );

            if ( fa.length == 0 ) {
                scoreFileTextField.setText( null );
                settings.setScoreFile( null );
                wiz.showError( "You didn't provide any identifiers" );
                return;
            }

            if ( fa.length < 2 ) {
                scoreFileTextField.setText( null );
                settings.setScoreFile( null );
                wiz.showError( "Your quick list must have at least 2 items" );
                return;
            }

            Collection<String> fields = Arrays.asList( fa );

            GeneScores gs = new GeneScores( fields, settings, wiz.getStatusField(), wiz.getGeneAnnots() );

            // definitely have to do this now.
            settings.setGeneScoreThreshold( 0.0 );
            if ( settings.getDoLog() ) {
                settings.setGeneScoreThreshold( 1.0 ); // becomes 0.
            }

            int i = gs.numGenesAboveThreshold( settings.getGeneScoreThreshold() );

            if ( i == 0 ) {
                scoreFileTextField.setText( null );
                settings.setScoreFile( null );
                wiz.showError( "None of the genes were recognized" );
                return;
            } else if ( i == 1 ) {
                scoreFileTextField.setText( null );
                settings.setScoreFile( null );
                wiz.showError( "Your quick list must have at least 2 items" );
                return;
            } else {
                wiz.showStatus( i + " genes were recognized out of " + fa.length + " ids." );
            }

            File file;
            String cleanName = "";

            if ( StringUtils.isNotBlank( name ) ) {
                cleanName = name;
                cleanName = cleanName.replaceAll( "[\\s\\\\\\/]*", "" );
                cleanName = cleanName.replaceAll( "^\\.", "" );
                cleanName = cleanName.replaceAll( "\\.$", "" );
            }
            if ( StringUtils.isNotBlank( cleanName ) ) {
                file = new File( new File( settings.getGeneScoreFileDirectory() ), "QuickList." + cleanName + ".txt" );

            } else {
                file = File.createTempFile( "QuickList.", ".txt", new File( settings.getGeneScoreFileDirectory() ) );
            }
            gs.write( file );

            // display
            scoreColTextField.setText( "2" );
            scoreFileTextField.setText( file.getAbsolutePath() );

            // do we do this now? Might be okay to wait?
            settings.setScoreFile( file.getAbsolutePath() );
            settings.setScoreCol( 2 );

            setValues();
        } catch ( BadLocationException e1 ) {
            log.error( e1, e1 );
        } catch ( IOException ioe ) {
            log.error( ioe, ioe );
            wiz.getStatusField().showError( ioe.getMessage() );
        } catch ( Exception e ) {
            log.error( e, e );
            wiz.getStatusField().showError( e.getMessage() );
        }
    }

    public void updateView() {
        assert wiz != null;
        quickPickPanel.setVisible( wiz.getAnalysisType().equals( SettingsHolder.Method.ORA ) );

        if ( StringUtils.isNotBlank( settings.getScoreFile() ) ) {
            validateFile();
        }
    }

}

class DataPreviewButtonAdapter implements ActionListener {
    AnalysisWizardStep2 adaptee;

    DataPreviewButtonAdapter( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent e ) {

        adaptee.dataPreviewScoresActionPerformed();

    }

}

class PreviewButtonAdapter implements ActionListener {
    AnalysisWizardStep2 adaptee;

    PreviewButtonAdapter( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent e ) {

        adaptee.previewScoresActionPerformed();

    }

}

class RawFileBrowse implements java.awt.event.ActionListener {
    AnalysisWizardStep2 adaptee;

    RawFileBrowse( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.rawBrowseButton_actionPerformed();
    }
}

class ScoreFileBrowse implements java.awt.event.ActionListener {
    AnalysisWizardStep2 adaptee;

    ScoreFileBrowse( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.scoreBrowseButton_actionPerformed();
    }
}

class QuickPickEnter implements java.awt.event.ActionListener {

    AnalysisWizardStep2 adaptee;

    QuickPickEnter( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.quickpickButton_actionPerformed();
    }
}
