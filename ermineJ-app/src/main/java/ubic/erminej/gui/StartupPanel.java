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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.data.AnnotationFileFetcher;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.gui.file.DataFileFilter;
import ubic.erminej.gui.file.XMLFileFilter;
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
            + "<a href=\"http://www.chibi.ubc.ca/Gemma/showAllArrayDesigns.html/\">http://www.chibi.ubc.ca/Gemma/showAllArrayDesigns.html</a><br/> or"
            + " <a href=\"http://www.chibi.ubc.ca/microannots/\">http://www.chibi.ubc.ca/microannots/</a></html>.";

    private static Log log = LogFactory.getLog( StartupPanel.class );
    private static final String DEFAULT_GO_TERM_FILE_NAME = "go_daily-termdb.rdf-xml.gz";

    // for testing.
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

    private JComboBox annotFormat = new JComboBox();

    private JTextField projectFileTextField = new JTextField();

    private Settings settings;

    JButton locateAnnotsButton;

    private StatusViewer statusMessenger = new StatusStderr();

    public StartupPanel( Settings settings, StatusViewer statusMessenger ) {
        this.settings = settings;
        if ( statusMessenger != null ) this.statusMessenger = statusMessenger;
        jbInit();
        setValues();
    }

    public void addActionListener( ActionListener listener ) {
        listenerList.add( ActionListener.class, listener );
    }

    public void removeActionListener( ActionListener listener ) {
        listenerList.remove( ActionListener.class, listener );
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
        tfp.setAutoCreateContainerGaps( true );
        tfp.setAutoCreateGaps( true );
        twoFilePanel.setLayout( tfp );
        tfp.setHorizontalGroup( tfp.createParallelGroup().addComponent( classPanel ).addComponent( annotPanel ) );
        tfp.setVerticalGroup( tfp.createSequentialGroup().addComponent( classPanel ).addComponent( annotPanel ) );

        // / holds both of the two subform panels.
        JPanel formPanel = new JPanel();
        GroupLayout gl = new GroupLayout( formPanel );
        formPanel.setLayout( gl );
        formPanel.setMaximumSize( new Dimension( 700, 1000 ) );
        formPanel.setBorder( BorderFactory.createEmptyBorder( 10, 50, 10, 50 ) );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        gl.setHorizontalGroup( gl.createParallelGroup( Alignment.CENTER ).addComponent( projectPanel )
                .addComponent( twoFilePanel ) );
        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( projectPanel ).addComponent( twoFilePanel ) );

        this.setLayout( new BorderLayout() );
        this.add( logoPanel, BorderLayout.NORTH );
        this.add( formPanel, BorderLayout.CENTER );
        this.add( buttonPanel, BorderLayout.SOUTH );

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
        hh.initHelp( helpButton, settings );

        JPanel buttonPanel = new JPanel();
        buttonPanel.add( helpButton );
        buttonPanel.add( cancelButton );
        buttonPanel.add( actionButton );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 20, 2 ) );
        return buttonPanel;
    }

    private JPanel makeAnnotFilePickerPanel() {
        // /// panel to hold the annotation file 'browse' and format setting
        final JPanel annotPanel = new JPanel();
        TitledBorder annotPanelBorder = BorderFactory.createTitledBorder( "Gene annotation file" );
        annotPanel.setBorder( annotPanelBorder );
        this.annotFileTextField = GuiUtil.fileBrowsePanel( annotPanel, new AnnotFilePickListener( this ) );

        // / configure drop-down for picking the annotation file format.
        JLabel annotFileFormatLabel = new JLabel();
        annotFileFormatLabel.setText( "Annotation file format" );
        annotFileFormatLabel.setLabelFor( annotFormat );
        annotFormat.setEditable( false );
        annotFormat.addItem( "ErmineJ" );
        annotFormat.addItem( "Affy CSV" );
        annotFormat.addItem( "Agilent" );
        annotFormat.setMaximumSize( new Dimension( 200, 25 ) );
        if ( settings.getAnnotFormat() == Format.AFFYCSV ) {
            annotFormat.setSelectedItem( "Affy CSV" );
        } else if ( settings.getAnnotFormat() == Format.AGILENT ) {
            annotFormat.setSelectedItem( "Agilent" );
        } else {
            annotFormat.setSelectedItem( "ErmineJ" );
        }

        locateAnnotsButton = new JButton( "Get from Gemma" );
        locateAnnotsButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                helpLocateAnnotations();
            }
        } );

        annotFormat.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                String formatS = ( String ) annotFormat.getSelectedItem();
                if ( formatS.equals( "ErmineJ" ) ) {
                    locateAnnotsButton.setEnabled( true );
                } else if ( formatS.equals( "Affy CSV" ) ) {
                    locateAnnotsButton.setEnabled( false );
                } else {
                    locateAnnotsButton.setEnabled( false );
                }
            }
        } );

        JPanel formatPanel = new JPanel();
        GroupLayout fpL = new GroupLayout( formatPanel );
        formatPanel.setLayout( fpL );
        fpL.setAutoCreateContainerGaps( true );
        fpL.setAutoCreateGaps( true );
        fpL.setHorizontalGroup( fpL.createSequentialGroup().addComponent( annotFileFormatLabel )
                .addComponent( annotFormat ).addComponent( locateAnnotsButton ) );
        fpL.setVerticalGroup( fpL.createParallelGroup( GroupLayout.Alignment.BASELINE )
                .addComponent( annotFileFormatLabel ).addComponent( annotFormat ).addComponent( locateAnnotsButton ) );

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

    /**
     * 
     */
    protected void helpLocateAnnotations() {

        /*
         * provide a list of gene annotation files to download.
         */
        try {
            final AnnotationFileFetcher f = new AnnotationFileFetcher( this.settings );
            final ArrayDesignValueObject result = f.pickAnnotation();

            if ( result == null ) {
                return;
            }
            final StartupPanel owner = this;
            SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    statusMessenger.showProgress( "Looking for annotation file ..." );

                    try {

                        final String testPath = settings.getDataDirectory() + File.separator + result.getShortName()
                                + ".an.txt.gz";

                        annotFileTextField.setText( "Fetching annots for " + result.getShortName() + " ..." );

                        URL urlPattern = new URL( settings.getStringProperty( "annotation.file.fetch.rest.url.base" )
                                + "/" + result.getShortName() );

                        // FIXME use proper REST client, this doesn't handle errors well at all.
                        // Client c = Client.create();

                        //
                        InputStream inputStream = new BufferedInputStream( urlPattern.openStream() );
                        //
                        OutputStream outputStream = new FileOutputStream( new File( testPath ) );

                        final byte[] buffer = new byte[65536];
                        int read = -1;
                        int totalRead = 0;

                        while ( ( read = inputStream.read( buffer ) ) > -1 ) {
                            outputStream.write( buffer, 0, read );
                            totalRead += read;
                            statusMessenger.showProgress( "Annotations: " + totalRead + " bytes read ..." );
                        }
                        outputStream.close();

                        statusMessenger.clear();

                        settings.setAnnotFile( testPath );
                        annotFileTextField.setText( settings.getAnnotFile() );
                    } catch ( Exception e ) {
                        log.error( e, e );
                        annotFileTextField.setText( "" );
                        JOptionPane.showMessageDialog( owner, INSTRUCTIONS, "Unable to fetch annotations",
                                JOptionPane.INFORMATION_MESSAGE );
                    } finally {
                        // ...
                    }
                    return null;
                }

            };

            sw.execute();

        } catch ( IOException e ) {
            /*
             * Fall back: unable to obtain the listing dynamically.
             */
            JOptionPane.showMessageDialog( this, INSTRUCTIONS, "Unable to automatically get annotations",
                    JOptionPane.INFORMATION_MESSAGE );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog( this, INSTRUCTIONS, "Unable to automatically get annotations",
                    JOptionPane.INFORMATION_MESSAGE );
        }

    }

    private JPanel makeGOFilePickerPanel() {
        // /// panel to hold GO file browser
        JPanel classPanel = new JPanel();
        TitledBorder classPanelBorder = BorderFactory.createTitledBorder( "Gene Ontology XML file" );
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
        ppL.setAutoCreateContainerGaps( true );
        ppL.setAutoCreateGaps( true );
        return projectPanel;
    }

    /**
     * 
     */
    private void saveSettings() {
        settings.setClassFile( classFileTextField.getText() );
        settings.setAnnotFile( annotFileTextField.getText() );
        String formatS = ( String ) annotFormat.getSelectedItem();

        if ( formatS.equals( "ErmineJ" ) ) {
            settings.setAnnotFormat( Format.DEFAULT );
        } else if ( formatS.equals( "Affy CSV" ) ) {
            settings.setAnnotFormat( Format.AFFYCSV );
        } else {
            settings.setAnnotFormat( Format.AGILENT );
        }
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

        String formatS = ( String ) annotFormat.getSelectedItem();
        if ( formatS.equals( "ErmineJ" ) ) {
            locateAnnotsButton.setEnabled( true );
        } else if ( formatS.equals( "Affy CSV" ) ) {
            locateAnnotsButton.setEnabled( false );
        } else {
            locateAnnotsButton.setEnabled( false );
        }
    }

    private void fetchGOFile() {

        SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
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

    protected void actionButton_actionPerformed( ActionEvent e ) {
        String annotFileName = annotFileTextField.getText();
        String goFileName = classFileTextField.getText();
        String projectFile = projectFileTextField.getText();

        File annotFile = new File( annotFileName );
        File goFile = new File( goFileName );

        if ( goFileName.length() == 0 ) {
            GuiUtil.error( "You must enter the Gene Ontology XML file location" );
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
        }
    }

    void classBrowseButton_actionPerformed() {
        JFileChooser chooser = new JFileChooser( settings.getDataDirectory() );
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
        chooser.setDialogTitle( "Choose the GO XML file:" );
        XMLFileFilter fileFilter = new XMLFileFilter();
        chooser.setFileFilter( fileFilter ); // JFileChooser method
        chooser.setAcceptAllFileFilterUsed( false );
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            classFileTextField.setText( chooser.getSelectedFile().toString() );
            settings.setDataDirectory( chooser.getSelectedFile().getParent() );
            projectFileTextField.setText( "" );
        }
    }

    void projectChooseActionPerformed() {

        JFileChooser projectPathChooser = new JFileChooser( settings.getDataDirectory() );
        projectPathChooser.setFileFilter( new FileFilter() {
            @Override
            public boolean accept( File pathname ) {
                return pathname.getPath().endsWith( ".project" );
            }

            @Override
            public String getDescription() {
                return "ErmineJ project files (*.project)";
            }
        } );

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
                this.classFileTextField.setEnabled( false ); // FIXME have to disable the entire chooser.

            } catch ( ConfigurationException e ) {
                GuiUtil.error( "The project file was invalid:\n" + e.getMessage() );
                enableIndividualFilePickers();
            } catch ( IOException e ) {
                GuiUtil.error( "The project file was invalid:\n" + e.getMessage() );
                enableIndividualFilePickers();
            }

        }
    }

    private void enableIndividualFilePickers() {
        this.annotFileTextField.setEnabled( true );
        this.classFileTextField.setEnabled( true );
    }

    public String getProjectFileName() {
        return this.projectFileTextField.getText();
    }

}

class AnnotFilePickListener implements ActionListener {
    StartupPanel adaptee;

    AnnotFilePickListener( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

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

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.actionButton_actionPerformed( e );
    }
}

class StartupPanel_cancelButton_actionAdapter implements java.awt.event.ActionListener {

    @Override
    public void actionPerformed( ActionEvent e ) {
        System.exit( 0 );
    }
}
