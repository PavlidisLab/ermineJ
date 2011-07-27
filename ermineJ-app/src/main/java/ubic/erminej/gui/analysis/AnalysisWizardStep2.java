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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

import ubic.basecode.util.FileTools;
import ubic.erminej.Settings;
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
    private AnalysisWizard wiz;
    private Settings settings;
    private JFileChooser chooser = new JFileChooser();
    private JTextField rawFileTextField;
    private JTextField scoreFileTextField;

    private JTextField jTextFieldScoreCol;

    public AnalysisWizardStep2( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.jbInit();
        this.wiz = wiz;
        this.settings = settings;
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
        setValues();
        wiz.clearStatus();
    }

    @Override
    protected void jbInit() {
        // initialization
        JPanel step2Panel = new JPanel();
        step2Panel.setLayout( new GridLayout( 2, 1 ) );

        // panel 2

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

        jTextFieldScoreCol = new JTextField();
        jTextFieldScoreCol.setPreferredSize( new Dimension( 50, 19 ) );
        jTextFieldScoreCol.setHorizontalAlignment( SwingConstants.LEFT ); // moves textbox text to the right
        jTextFieldScoreCol.setText( "2" );
        jTextFieldScoreCol
                .setToolTipText( "Column of the gene score file containing the scores. This must be a value of 2 or higher." );
        jTextFieldScoreCol.setEditable( true );

        jTextFieldScoreCol.addKeyListener( this );
        scoreColumnPanel.add( jLabel3, null );
        scoreColumnPanel.add( jTextFieldScoreCol, BorderLayout.WEST );
        JButton previewButton = new JButton( "Preview" );
        previewButton.setToolTipText( "Preview the scores to be imported" );
        previewButton.addActionListener( new PreviewButtonAdapter( this ) );
        scoreColumnPanel.add( previewButton, BorderLayout.EAST );

        // -----

        // The top panel.
        scoreFilePanel.add( scoreFileBrowsePanel );
        scoreFilePanel.add( scoreColumnPanel );

        // -----------------------------------------------------

        // panel 4
        JPanel rawDataPanel = new JPanel();

        TitledBorder rawDataFileTitleBorder = BorderFactory
                .createTitledBorder( "Data profiles file (optional for ROC, ORA or GSR):" );
        rawDataPanel.setBorder( rawDataFileTitleBorder );
        rawDataPanel.setLayout( new GridLayout( 1, 1 ) );
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

        rawDataPanel.add( rawDataBrowsePanel );

        // MAIN PANEL = GridLayout(BorderLayouts); GridLayout is (1,6)

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

    @Override
    public boolean isReady() {

        if ( wiz.getAnalysisType().equals( Settings.Method.CORR ) && rawFileTextField.getText().compareTo( "" ) == 0 ) {
            wiz.showError( "Correlation analyses require a raw data file." );
            return false;
        } else if ( ( wiz.getAnalysisType().equals( Settings.Method.GSR ) || wiz.getAnalysisType().equals(
                Settings.Method.ORA ) )
                && scoreFileTextField.getText().compareTo( "" ) == 0 ) {
            wiz.showError( "ORA and resampling analyses require a gene score file." );
            return false;
        }

        if ( ( !wiz.getAnalysisType().equals( Settings.Method.CORR ) )
                && Integer.valueOf( jTextFieldScoreCol.getText() ).intValue() < 2 ) {
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
        jTextFieldScoreCol.setText( String.valueOf( settings.getScoreCol() ) );
        scoreFileTextField.setText( settings.getScoreFile() );
        rawFileTextField.setText( settings.getRawDataFileName() );
    }

    public void saveValues() {
        settings.setScoreCol( Integer.valueOf( jTextFieldScoreCol.getText() ).intValue() );

        settings.setScoreFile( scoreFileTextField.getText() );
        settings.setRawFile( rawFileTextField.getText() );
        if ( rawFileTextField.getText() != null || rawFileTextField.getText().length() > 0 ) {
            settings.userSetRawFile( true );
        }
        settings.setDataDirectory( chooser.getCurrentDirectory().toString() );
    }

    /**
     * Show a preview of the dat
     */
    public void previewScoresActionPerformed() {

        if ( scoreFileTextField.getText().length() != 0 && !FileTools.testFile( scoreFileTextField.getText() ) ) {
            wiz.showError( "You must choose a valid file to preview" );
            return;
        }

        if ( StringUtils.isBlank( jTextFieldScoreCol.getText() ) ) {
            wiz.showError( "You must valid column" );
            return;
        }

        int column = Integer.valueOf( jTextFieldScoreCol.getText() ).intValue();

        try {
            FileInputStream fis = new FileInputStream( scoreFileTextField.getText() );
            BufferedInputStream bis = new BufferedInputStream( fis );
            BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );

            String line = null;

            int lineNum = 0;

            List<Object[]> table = new ArrayList<Object[]>();

            while ( ( line = dis.readLine() ) != null ) {
                if ( line.length() == 0 ) continue;

                String[] toks = StringUtils.splitPreserveAllTokens( line, '\t' );

                if ( toks.length < column ) {
                    wiz.showError( "The file does not have enough fields at line " + lineNum );
                    break;
                }

                String[] toUseToks = new String[2];
                toUseToks[0] = toks[0];
                toUseToks[1] = toks[column - 1];

                table.add( toUseToks );

                lineNum++;

                if ( lineNum > 100 ) break;
            }

            dis.close();

            Object[][] tab = new Object[table.size()][];
            for ( int i = 0; i < tab.length; i++ ) {
                tab[i] = table.get( i );
            }

            TableModel tableModel = new DefaultTableModel( tab, new Object[] { "Identifier", "Score" } );

            final JDialog previewPanel = new JDialog( wiz );

            Container content = previewPanel.getContentPane();

            previewPanel.setTitle( "Preview of scores" );
            previewPanel.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
            previewPanel.setFocusable( true );

            JTable tableView = new JTable( tableModel );
            JScrollPane scrollPane = new JScrollPane( tableView );
            content.add( scrollPane, BorderLayout.CENTER );

            JButton closeButton = new JButton( "Close" );

            closeButton.addActionListener( new ActionListener() {

                @Override
                public void actionPerformed( ActionEvent e ) {
                    previewPanel.dispose();
                    wiz.clearStatus();

                }
            } );

            JPanel p2 = new JPanel();

            p2.add( closeButton );
            content.add( p2, BorderLayout.SOUTH );

            previewPanel.pack();
            previewPanel.setSize( 400, 300 );

            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension screenSize = tk.getScreenSize();
            int screenHeight = screenSize.height;
            int screenWidth = screenSize.width;

            previewPanel.setLocation( screenWidth / 2, screenHeight / 2 );

            previewPanel.setVisible( true );
            previewPanel.requestFocus();

        } catch ( IOException e ) {
            wiz.showError( "The file could not be previewed: " + e.getMessage() );
            return;
        }

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
