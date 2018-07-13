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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.data.AnnotationFileFetcher;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.gui.file.DataFileFilter;
import ubic.erminej.gui.file.GOFileFilter;
import ubic.erminej.gui.util.GuiUtil;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;

/**
 * Panel shown on initial startup of the application.
 *
 * @author paul
 * @version $Id$
 */
public class StartupPanel extends JPanel {

    private static final String GO_ARCHIVE_DIR = "http://archive.geneontology.org/latest-termdb";

    private static final String INSTRUCTIONS = "<html>For annotation files, visit "
            + "<a href=\"https://gemma.msl.ubc.ca/arrays/showAllArrayDesigns.html/\">https://gemma.msl.ubc.ca/arrays/showAllArrayDesigns.html</a><br/> or"
            + " <a href=\"https://gemma.msl.ubc.ca/annots/\">https://gemma.msl.ubc.ca/annots/</a></html>.";

    private static Log log = LogFactory.getLog( StartupPanel.class );
    private static final String DEFAULT_GO_TERM_FILE_NAME = "go_daily-termdb.rdf-xml.gz";

    // for testing.
    /**
     * <p>
     * main.
     * </p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main( String[] args ) throws Exception {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {
            //
        }
        JFrame f = new JFrame();
        f.setSize( new Dimension( 400, 600 ) );
        StartupPanel p = new StartupPanel( new Settings(), null );
        f.add( p );
        f.pack();
        GuiUtil.centerContainer( f );
        f.setVisible( true );

    }

    private JButton actionButton;

    private JTextField classFileTextField = new JTextField();

    private JTextField annotFileTextField = new JTextField();

    //  private JComboBox<String> annotFormat = new JComboBox<>();

    private JTextField projectFileTextField = new JTextField();

    private Settings settings;

    JButton locateAnnotsButton;

    private StatusViewer statusMessenger = new StatusStderr();

    /**
     * <p>
     * Constructor for StartupPanel.
     * </p>
     *
     * @param settings a {@link ubic.erminej.Settings} object.
     * @param statusMessenger a {@link ubic.basecode.util.StatusViewer} object.
     */
    public StartupPanel( Settings settings, StatusViewer statusMessenger ) {
        this.settings = settings;
        if ( statusMessenger != null ) this.statusMessenger = statusMessenger;
        jbInit();
        setValues();
    }

    /**
     * <p>
     * addActionListener.
     * </p>
     *
     * @param listener a {@link java.awt.event.ActionListener} object.
     */
    public void addActionListener( ActionListener listener ) {
        listenerList.add( ActionListener.class, listener );
    }

    /**
     * <p>
     * getProjectFileName.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProjectFileName() {
        return this.projectFileTextField.getText();
    }

    /**
     * <p>
     * removeActionListener.
     * </p>
     *
     * @param listener a {@link java.awt.event.ActionListener} object.
     */
    public void removeActionListener( ActionListener listener ) {
        listenerList.remove( ActionListener.class, listener );
    }

    void annotBrowseButton_actionPerformed() {
        JFileChooser chooser = new JFileChooser( settings.getDataDirectory() );
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
        chooser.setDialogTitle( "Choose the annotation file:" );
        DataFileFilter fileFilter = new DataFileFilter();
        chooser.setFileFilter( fileFilter ); // JFileChooser method
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            annotFileTextField.setText( chooser.getSelectedFile().toString() );
            settings.setDataDirectory( chooser.getSelectedFile().getParent() );
            projectFileTextField.setText( "" );
            this.annotFileTextField.setEnabled( true );
            this.classFileTextField.setEnabled( true );
        }
    }

    void classBrowseButton_actionPerformed() {
        JFileChooser chooser = new JFileChooser( settings.getDataDirectory() );
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
        chooser.setDialogTitle( "Choose the GO file (XML or OBO):" );
        GOFileFilter fileFilter = new GOFileFilter();
        chooser.setFileFilter( fileFilter ); // JFileChooser method
        chooser.setAcceptAllFileFilterUsed( false );
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            classFileTextField.setText( chooser.getSelectedFile().toString() );
            settings.setDataDirectory( chooser.getSelectedFile().getParent() );
            projectFileTextField.setText( "" );
            this.annotFileTextField.setEnabled( true );
            this.classFileTextField.setEnabled( true );
        }
    }

    void projectChooseActionPerformed() {

        JFileChooser projectPathChooser = new JFileChooser( settings.getDataDirectory() );
        projectPathChooser.setFileFilter( new FileNameExtensionFilter( "Project", "project", "PROJECT" ) );

        int yesno = projectPathChooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            File projectFile = projectPathChooser.getSelectedFile();

            if ( !projectFile.canRead() ) {
                GuiUtil.error( "The project file was invalid:\nCannot read" );
                enableIndividualFilePickers();
                return;
            } else if ( projectFile.length() == 0 ) {
                GuiUtil.error( "The project file was invalid:\nEmpty file" );
                enableIndividualFilePickers();
                return;
            }

            projectFileTextField.setText( projectFile.toString() );
            settings.setDataDirectory( projectFile.getParent() );

            /*
             * Now set the values in the xml and annotation file fields as well ...
             */
            Settings projectSettings;
            try {
                projectSettings = new Settings( projectFile.getAbsolutePath() );
                this.settings = projectSettings;
                this.classFileTextField.setText( settings.getClassFile() );
                this.annotFileTextField.setText( settings.getAnnotFile() );
                this.annotFileTextField.setEnabled( false );
                this.classFileTextField.setEnabled( false );

            } catch ( ConfigurationException e ) {
                GuiUtil.error( "The project file was invalid:\n" + e.getMessage() );
                enableIndividualFilePickers();
            } catch ( IOException e ) {
                GuiUtil.error( "The project file was invalid:\n" + e.getMessage() );
                enableIndividualFilePickers();
            }

        }
    }

    /**
     * <p>
     * actionButton_actionPerformed.
     * </p>
     *
     * @param e a {@link java.awt.event.ActionEvent} object.
     */
    protected void actionButton_actionPerformed( ActionEvent e ) {
        String annotFileName = annotFileTextField.getText();
        String goFileName = classFileTextField.getText();
        String projectFile = projectFileTextField.getText();

        File annotFile = new File( annotFileName );
        File goFile = new File( goFileName );

        if ( goFileName.length() == 0 ) {
            GuiUtil.error( "You must enter the Gene Ontology file (XML or OBO) location" );
        } else if ( annotFileName.length() == 0 ) {
            GuiUtil.error( "You must enter the annotation file location for your experiment" );
        } else if ( !annotFile.exists() || !annotFile.canRead() ) {
            GuiUtil.error( "Could not read file: " + annotFileName );
        } else if ( !goFile.exists() || !goFile.canRead() ) {
            GuiUtil.error( "Could not read file: " + goFileName );
        } else {
            log.debug( "Saving configuration" );

            if ( projectFile.isEmpty() ) saveSettings();

            Object[] listeners = listenerList.getListenerList();
            for ( int i = 0; i < listeners.length; i += 2 ) {
                if ( listeners[i] == ActionListener.class ) {
                    ( ( ActionListener ) listeners[i + 1] ).actionPerformed( e );
                }
            }
        }
    }

    /**
     * <p>
     * helpLocateAnnotations.
     * </p>
     */
    protected void helpLocateAnnotations() {

        /*
         * provide a list of gene annotation files to download.
         */
        statusMessenger.showProgress( "Looking for annotation files ..." );

        final StartupPanel owner = this;

        SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                URL urlPattern = null;
                try {
                    final AnnotationFileFetcher f = new AnnotationFileFetcher();
                    final ArrayDesignValueObject result = f.pickAnnotation();

                    if ( result == null ) {
                        statusMessenger.clear();
                        return null;
                    }

                    // this is kind of lame, but it's the same as in Gemma's ArrayDesignAnnotationServiceImpl.
                    String cleanedShortName = result.getShortName().replaceAll( Pattern.quote( "/" ), "_" );
                    final String outputFilePath = settings.getDataDirectory() + File.separator + cleanedShortName
                            + "_noParents.an.txt.gz";

                    annotFileTextField.setText( "Fetching annots for " + result.getShortName() );

                    urlPattern = new URL( String.format( Settings.ANNOTATION_FILE_FETCH_RESTURL, result.getId() ) );

                    InputStream inputStream = new BufferedInputStream( urlPattern.openStream() );

                    OutputStream outputStream = new FileOutputStream( new File( outputFilePath ) );

                    final byte[] buffer = new byte[65536];
                    int read = -1;
                    int totalRead = 0;

                    StopWatch timer = new StopWatch();
                    timer.start();
                    while ( ( read = inputStream.read( buffer ) ) > -1 ) {
                        outputStream.write( buffer, 0, read );
                        totalRead += read;
                        if ( timer.getTime() > 500 ) {
                            statusMessenger.showProgress( "Annotations: " + totalRead + " bytes read" );
                            timer.reset();
                            timer.start();
                        }
                    }
                    statusMessenger.showStatus( "Annotations: " + totalRead + " bytes read" );

                    outputStream.close();

                    try {
                        GZIPInputStream gf = new GZIPInputStream( new FileInputStream( new File( outputFilePath ) ) );
                        gf.read();
                    } catch ( IOException e ) {
                        annotFileTextField.setText( "" );
                        statusMessenger.clear();
                        JOptionPane.showMessageDialog( owner, INSTRUCTIONS, "File downloaded was not a valid archive",
                                JOptionPane.INFORMATION_MESSAGE );
                    }

                    statusMessenger.clear();

                    settings.setAnnotFile( outputFilePath );
                    annotFileTextField.setText( settings.getAnnotFile() );
                } catch ( Exception e ) {
                    log.error( e, e );
                    annotFileTextField.setText( "" );
                    statusMessenger.clear();
                    JOptionPane.showMessageDialog( owner, INSTRUCTIONS, "Unable to fetch from " + urlPattern,
                            JOptionPane.INFORMATION_MESSAGE );
                } finally {
                    // ...
                }
                return null;
            }

        };

        sw.execute();

    }

    private void enableIndividualFilePickers() {
        this.annotFileTextField.setEnabled( true );
        this.classFileTextField.setEnabled( true );
    }

    private void fetchGOFile() {

        SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() {
                statusMessenger.showProgress( "Looking for GO file ..." );

                try {
                    final String testPath = settings.getDataDirectory() + File.separator + DEFAULT_GO_TERM_FILE_NAME;

                    classFileTextField.setText( "Attempting to locate ..." );

                    URL urlPattern = new URL( GO_ARCHIVE_DIR + "/go_daily-termdb.rdf-xml.gz" );

                    InputStream inputStream = new BufferedInputStream( urlPattern.openStream() );
                    String localGoFileName = testPath;

                    OutputStream outputStream = new FileOutputStream( new File( localGoFileName ) );

                    final byte[] buffer = new byte[65536];
                    int read = -1;
                    int totalRead = 0;

                    while ( ( read = inputStream.read( buffer ) ) > -1 ) {
                        outputStream.write( buffer, 0, read );
                        totalRead += read;
                        statusMessenger.showProgress( "GO: " + totalRead + " bytes read ..." );
                    }
                    outputStream.close();

                    statusMessenger.clear();

                    settings.setClassFile( localGoFileName );
                    classFileTextField.setText( settings.getClassFile() );
                } catch ( Exception e ) {
                    classFileTextField.setText( "" );
                } finally {
                    // ...
                }
                return null;
            }

        };

        sw.execute();
    }

    private void jbInit() {

        JPanel logoPanel = makeLogoPanel();

        JPanel buttonPanel = makeActionButtons();

        JPanel projectPanel = makeProjectPickerPanel();

        JPanel classPanel = makeGOFilePickerPanel();

        JPanel annotPanel = makeAnnotFilePickerPanel();

        JPanel twoFilePanel = new JPanel(); // holds the GO and annotations.
        twoFilePanel.setBorder( BorderFactory.createTitledBorder( "... OR choose the starting files individually." ) );
        GroupLayout tfp = new GroupLayout( twoFilePanel );
        // tfp.setAutoCreateContainerGaps( true );
        // tfp.setAutoCreateGaps( true );
        twoFilePanel.setLayout( tfp );
        tfp.setHorizontalGroup( tfp.createParallelGroup().addComponent( classPanel ).addComponent( annotPanel ) );
        tfp.setVerticalGroup( tfp.createSequentialGroup().addComponent( classPanel ).addComponent( annotPanel ) );

        // / holds both of the two subform panels.
        JPanel formPanel = new JPanel();
        GroupLayout gl = new GroupLayout( formPanel );
        formPanel.setLayout( gl );
        formPanel.setMaximumSize( new Dimension( 700, 1000 ) );
        formPanel.setBorder( BorderFactory.createEmptyBorder( 10, 20, 10, 20 ) );
        // gl.setAutoCreateContainerGaps( true );
        // gl.setAutoCreateGaps( true );
        gl.setHorizontalGroup( gl.createParallelGroup( Alignment.CENTER ).addComponent( projectPanel )
                .addComponent( twoFilePanel ) );
        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( projectPanel ).addComponent( twoFilePanel ) );

        this.setLayout( new BorderLayout() );
        this.add( logoPanel, BorderLayout.NORTH );
        this.add( formPanel, BorderLayout.CENTER );
        this.add( buttonPanel, BorderLayout.SOUTH );

    }

    private JPanel makeActionButtons() {
        actionButton = new JButton();
        JButton cancelButton = new JButton();
        JButton helpButton = new JButton();
        cancelButton.setText( "Cancel" );
        cancelButton.setMnemonic( 'c' );
        cancelButton.addActionListener( new StartupPanel_cancelButton_actionAdapter() );
        actionButton.addActionListener( new StartupPanel_actionButton_actionAdapter( this ) );
        actionButton.setText( "Start" );
        cancelButton.setText( "Quit" );
        helpButton.setText( "Help" );
        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpButton );

        JPanel buttonPanel = new JPanel();
        buttonPanel.add( helpButton );
        buttonPanel.add( cancelButton );
        buttonPanel.add( actionButton );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 15, 2 ) );
        return buttonPanel;
    }

    private JPanel makeAnnotFilePickerPanel() {
        // /// panel to hold the annotation file 'browse' and format setting
        final JPanel annotPanel = new JPanel();
        TitledBorder annotPanelBorder = BorderFactory.createTitledBorder( "Gene annotation file" );
        annotPanel.setBorder( annotPanelBorder );
        this.annotFileTextField = GuiUtil.fileBrowsePanel( annotPanel, new AnnotFilePickListener( this ) );

        // / configure drop-down for picking the annotation file format.
        // JLabel annotFileFormatLabel = new JLabel();
        // annotFileFormatLabel.setText( "Annotation file format" );
        //annotFileFormatLabel.setLabelFor( annotFormat );
        // annotFormat.setEditable( false );
        // annotFormat.addItem( "ErmineJ" );
        //   annotFormat.addItem( "Affy CSV" );
        //  annotFormat.addItem( "Agilent" );
        //  annotFormat.setMaximumSize( new Dimension( 200, 25 ) );
        // if ( settings.getAnnotFormat() == Format.AFFYCSV ) {
        //     annotFormat.setSelectedItem( "Affy CSV" );
        // } else if ( settings.getAnnotFormat() == Format.AGILENT ) {
        //     annotFormat.setSelectedItem( "Agilent" );
        // } else {
        //     annotFormat.setSelectedItem( "ErmineJ" );
        // }

        locateAnnotsButton = new JButton( "Get from Gemma" );
        locateAnnotsButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                helpLocateAnnotations();
            }
        } );

        //        annotFormat.addActionListener( new ActionListener() {
        //            @Override
        //            public void actionPerformed( ActionEvent e ) {
        //                String formatS = ( String ) annotFormat.getSelectedItem();
        //                if ( formatS.equals( "ErmineJ" ) ) {
        //                    locateAnnotsButton.setEnabled( true );
        //                } else if ( formatS.equals( "Affy CSV" ) ) {
        //                    locateAnnotsButton.setEnabled( false );
        //                } else {
        //                    locateAnnotsButton.setEnabled( false );
        //                }
        //            }
        //        } );

        JPanel formatPanel = new JPanel();
        GroupLayout fpL = new GroupLayout( formatPanel );
        formatPanel.setLayout( fpL );
        fpL.setAutoCreateContainerGaps( true );
        fpL.setAutoCreateGaps( true );
        fpL.setHorizontalGroup( fpL.createSequentialGroup().addComponent( locateAnnotsButton ) );
        fpL.setVerticalGroup( fpL.createParallelGroup( GroupLayout.Alignment.BASELINE ).addComponent( locateAnnotsButton ) );

        GroupLayout apL = new GroupLayout( annotPanel );
        annotPanel.setLayout( apL );
        apL.setAutoCreateContainerGaps( true );
        apL.setAutoCreateGaps( true );
        apL.setHorizontalGroup( apL.createParallelGroup( GroupLayout.Alignment.LEADING )
                .addComponent( annotFileTextField.getParent() ).addComponent( formatPanel ) );
        apL.setVerticalGroup( apL.createSequentialGroup().addComponent( annotFileTextField.getParent() )
                .addComponent( formatPanel ) );
        return annotPanel;
    }

    private JPanel makeGOFilePickerPanel() {
        // /// panel to hold GO file browser
        JPanel classPanel = new JPanel();
        TitledBorder classPanelBorder = BorderFactory.createTitledBorder( "Gene Ontology file (XML or OBO)" );
        classPanel.setBorder( classPanelBorder );
        this.classFileTextField = GuiUtil.fileBrowsePanel( classPanel, new GOFilePickListener( this ) );
        GroupLayout cpL = new GroupLayout( classPanel );
        classPanel.setLayout( cpL );
        cpL.setHorizontalGroup( cpL.createParallelGroup().addComponent( classFileTextField.getParent() ) );
        cpL.setVerticalGroup( cpL.createSequentialGroup().addComponent( classFileTextField.getParent() ) );
        cpL.setAutoCreateContainerGaps( true );
        cpL.setAutoCreateGaps( true );
        return classPanel;
    }

    private JPanel makeLogoPanel() {
        // decoration
        JLabel logoLabel = new JLabel();
        logoLabel
                .setIcon( new ImageIcon( MainFrame.class.getResource( MainFrame.RESOURCE_LOCATION + "logo1small.gif" ) ) );
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground( Color.WHITE );
        logoPanel.add( logoLabel );
        return logoPanel;
    }

    private JPanel makeProjectPickerPanel() {
        // Panel to hold the Project browser.
        JPanel projectPanel = new JPanel();
        projectPanel.setBorder( BorderFactory.createTitledBorder( "Select a project file ..." ) );
        this.projectFileTextField = GuiUtil.fileBrowsePanel( projectPanel, new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                projectChooseActionPerformed();
            }
        } );
        GroupLayout ppL = new GroupLayout( projectPanel );
        projectPanel.setLayout( ppL );
        ppL.setHorizontalGroup( ppL.createParallelGroup().addComponent( projectFileTextField.getParent() ) );
        ppL.setVerticalGroup( ppL.createSequentialGroup().addComponent( projectFileTextField.getParent() ) );

        // ppL.setAutoCreateContainerGaps( true );
        // ppL.setAutoCreateGaps( true );
        return projectPanel;
    }

    /**
     *
     */
    private void saveSettings() {
        settings.setClassFile( classFileTextField.getText() );
        settings.setAnnotFile( annotFileTextField.getText() );
        //        String formatS = ( String ) annotFormat.getSelectedItem();
        //
        //        if ( formatS.equals( "ErmineJ" ) ) {
        settings.setAnnotFormat( Format.DEFAULT );
        //        } else if ( formatS.equals( "Affy CSV" ) ) {
        //            settings.setAnnotFormat( Format.AFFYCSV );
        //        } else {
        //            settings.setAnnotFormat( Format.AGILENT );
        //        }
    }

    private void setValues() {

        if ( StringUtils.isNotBlank( settings.getClassFile() ) ) {
            classFileTextField.setText( settings.getClassFile() );

            File testFile = new File( settings.getClassFile() );
            if ( testFile.exists() && testFile.canRead() && testFile.isFile() && testFile.length() > 0 ) {
                classFileTextField.setText( settings.getClassFile() );
            } else {
                fetchGOFile();
            }

        } else {
            fetchGOFile();
        }

        annotFileTextField.setText( settings.getAnnotFile() );

        String dataDirectory = settings.getDataDirectory();
        if ( dataDirectory == null ) {
            settings.setDataDirectory( System.getProperty( "user.dir" ) );
        }

        //        String formatS = ( String ) annotFormat.getSelectedItem();
        //        if ( formatS.equals( "ErmineJ" ) ) {
        locateAnnotsButton.setEnabled( true );
        //        } else if ( formatS.equals( "Affy CSV" ) ) {
        //            locateAnnotsButton.setEnabled( false );
        //        } else {
        //            locateAnnotsButton.setEnabled( false );
        //        }
    }

}

class AnnotFilePickListener implements ActionListener {
    StartupPanel adaptee;

    AnnotFilePickListener( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.annotBrowseButton_actionPerformed();
    }
}

class GOFilePickListener implements ActionListener {
    StartupPanel adaptee;

    GOFilePickListener( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.classBrowseButton_actionPerformed();
    }
}

class StartupPanel_actionButton_actionAdapter implements java.awt.event.ActionListener {
    StartupPanel adaptee;

    StartupPanel_actionButton_actionAdapter( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.actionButton_actionPerformed( e );
    }
}

/** {@inheritDoc} */

class StartupPanel_cancelButton_actionAdapter implements java.awt.event.ActionListener {

    @Override
    /** {@inheritDoc} */
    public void actionPerformed( ActionEvent e ) {
        System.exit( 0 );
    }
}
