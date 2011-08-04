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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import ubic.basecode.util.FileTools;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
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

    private JTextField dataColTextField;

    public AnalysisWizardStep2( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.jbInit();
        this.wiz = wiz;
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
                && rawFileTextField.getText().compareTo( "" ) == 0 ) {
            wiz.showError( "Correlation analyses require a raw data file." );
            return false;
        } else if ( ( wiz.getAnalysisType().equals( SettingsHolder.Method.GSR ) || wiz.getAnalysisType().equals(
                SettingsHolder.Method.ORA ) )
                && scoreFileTextField.getText().compareTo( "" ) == 0 ) {
            wiz.showError( "ORA and resampling analyses require a gene score file." );
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
            MatrixPreviewer.previewMatrix( wiz, rawFileTextField.getText(), -1 );
        } catch ( IOException e ) {
            GuiUtil.error( "Error previewing data: " + e.getMessage(), e );
        }

        // GeneScores test;
        // try {
        // test = new GeneScores( filename, settings, wiz.getStatusField(), wiz.getGeneAnnots(), 100 );
        // } catch ( IOException e1 ) {
        // wiz.showError( "The file could not be previewed: " + e1.getMessage() );
        // return;
        // }
        //
        // List<Object[]> table = new ArrayList<Object[]>();
        //
        // for ( Probe p : test.getProbeToScoreMap().keySet() ) {
        // table.add( new Object[] { p.getName(), test.getProbeToScoreMap().get( p ) } );
        // }
        //
        // MatrixPreviewer.previewMatrix( wiz, table, new Object[] { "ID", "Score" } );

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
                .createTitledBorder( "Data profiles file (optional for ROC, ORA or GSR, but used for visualization):" );
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

    @Override
    protected void jbInit() {
        // initialization
        JPanel step2Panel = new JPanel();
        step2Panel.setLayout( new GridLayout( 2, 1 ) );

        // top panel with score file setup.

        JPanel scoreFilePanel = new JPanel();
        scoreFilePanel.setLayout( new GridLayout( 2, 1 ) );

        TitledBorder geneScoreFileTitleBorder = BorderFactory
                .createTitledBorder( "Gene score file (optional for correlation score):" );

        scoreFilePanel.setBorder( geneScoreFileTitleBorder );
        JButton scoreFileBrowseButton = new JButton();
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

        scoreFilePanel.add( scoreFileBrowsePanel );
        scoreFilePanel.add( scoreColumnPanel );

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
    }

    void setValues() {
        scoreColTextField.setText( String.valueOf( settings.getScoreCol() ) );
        scoreFileTextField.setText( settings.getScoreFile() );
        rawFileTextField.setText( settings.getRawDataFileName() );
        dataColTextField.setText( String.valueOf( settings.getDataCol() ) );
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

    public void actionPerformed( ActionEvent e ) {
        adaptee.rawBrowseButton_actionPerformed();
    }
}

class ScoreFileBrowse implements java.awt.event.ActionListener {
    AnalysisWizardStep2 adaptee;

    ScoreFileBrowse( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.scoreBrowseButton_actionPerformed();
    }
}
