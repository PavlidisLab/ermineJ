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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.BrowserLauncher;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.gui.file.DataFileFilter;
import ubic.erminej.gui.file.XMLFileFilter;
import ubic.erminej.gui.util.GuiUtil;

/**
 * Replaces old Startup dialog
 * 
 * @author paul
 * @version $Id$
 */
public class StartupPanel extends JPanel {

    private static final String INSTRUCTIONS = "<html>For annotation files, visit "
            + "<a href=\"http://www.chibi.ubc.ca/microannots/\">http://www.chibi.ubc.ca/microannots</a></html>";

    private static Log log = LogFactory.getLog( StartupPanel.class );
    private static final String DEFAULT_GO_TERM_FILE_NAME = "go_daily-termdb.rdf-xml.gz";

    // for testing.
    public static void main( String[] args ) throws Exception {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {

        }
        JFrame f = new JFrame();
        f.setSize( new Dimension( 400, 600 ) );
        StartupPanel p = new StartupPanel( new Settings() );
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

    public StartupPanel( Settings settings ) {
        this.settings = settings;
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

        JEditorPane instructions = makeInstructionsPane();

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
        formPanel.setMaximumSize( new Dimension( 800, 1000 ) );
        formPanel.setBorder( BorderFactory.createEmptyBorder( 10, 50, 10, 50 ) );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        gl.setHorizontalGroup( gl.createParallelGroup( Alignment.CENTER ).addComponent( projectPanel ).addComponent(
                twoFilePanel ) );
        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( projectPanel ).addComponent( twoFilePanel ) );

        JPanel centerPanel = new JPanel(); // holds help and the forms
        GroupLayout cgl = new GroupLayout( centerPanel );
        centerPanel.setLayout( cgl );
        cgl.setHorizontalGroup( cgl.createParallelGroup( Alignment.CENTER ).addComponent( instructions ).addComponent(
                formPanel ) );
        cgl.setVerticalGroup( cgl.createSequentialGroup().addComponent( instructions ).addComponent( formPanel ) );

        // centerPanel.add( instructions, BorderLayout.NORTH );
        // centerPanel.add( formPanel, BorderLayout.CENTER );

        this.setLayout( new BorderLayout() );
        this.add( logoPanel, BorderLayout.NORTH );
        this.add( centerPanel, BorderLayout.CENTER );
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
        hh.initHelp( helpButton );

        JPanel buttonPanel = new JPanel();
        buttonPanel.add( helpButton );
        buttonPanel.add( cancelButton );
        buttonPanel.add( actionButton );
        return buttonPanel;
    }

    private JPanel makeAnnotFilePickerPanel() {
        // /// panel to hold the annotation file 'browse' and format setting
        JPanel annotPanel = new JPanel();
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

        JPanel formatPanel = new JPanel();
        GroupLayout fpL = new GroupLayout( formatPanel );
        formatPanel.setLayout( fpL );
        fpL.setAutoCreateContainerGaps( true );
        fpL.setAutoCreateGaps( true );
        fpL.setHorizontalGroup( fpL.createSequentialGroup().addComponent( annotFileFormatLabel ).addComponent(
                annotFormat ) );
        fpL.setVerticalGroup( fpL.createParallelGroup( GroupLayout.Alignment.BASELINE ).addComponent(
                annotFileFormatLabel ).addComponent( annotFormat ) );

        GroupLayout apL = new GroupLayout( annotPanel );
        annotPanel.setLayout( apL );
        apL.setAutoCreateContainerGaps( true );
        apL.setAutoCreateGaps( true );
        apL.setHorizontalGroup( apL.createParallelGroup( GroupLayout.Alignment.LEADING ).addComponent(
                annotFileTextField.getParent() ).addComponent( formatPanel ) );
        apL.setVerticalGroup( apL.createSequentialGroup().addComponent( annotFileTextField.getParent() ).addComponent(
                formatPanel ) );
        return annotPanel;
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

    private JEditorPane makeInstructionsPane() {
        JEditorPane instructions = new JEditorPane();
        instructions.setEditable( false );
        instructions.setFont( new Font( "SansSerif", Font.PLAIN, 11 ) );
        instructions.setContentType( "text/html" );
        instructions.setBorder( BorderFactory.createEmptyBorder( 0, 20, 10, 20 ) );
        instructions.addHyperlinkListener( new HyperlinkListener() {
            public void hyperlinkUpdate( HyperlinkEvent e ) {
                if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                    try {
                        BrowserLauncher.openURL( e.getURL().toExternalForm() );
                    } catch ( Exception e1 ) {
                        GuiUtil.error( "Could not open link" );
                    }
                }
            }
        } );
        instructions.setText( INSTRUCTIONS );
        return instructions;
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
        if ( settings.getClassFile() == null ) {
            // see if there is a file available.
            String testPath = settings.getDataDirectory() + System.getProperty( "file.separator" )
                    + DEFAULT_GO_TERM_FILE_NAME;
            File testFile = new File( testPath );
            if ( testFile.exists() && testFile.canRead() && testFile.isFile() && testFile.length() > 0 ) {
                settings.setClassFile( testPath );
            }
        }
        classFileTextField.setText( settings.getClassFile() );
        annotFileTextField.setText( settings.getAnnotFile() );
        String dataDirectory = settings.getDataDirectory();
        if ( dataDirectory == null ) {
            settings.setDataDirectory( System.getProperty( "user.dir" ) );
        }
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
                this.classFileTextField.setEnabled( false ); // FIXME have to disable the entire choose.

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

    public void actionPerformed( ActionEvent e ) {
        adaptee.annotBrowseButton_actionPerformed();
    }
}

class GOFilePickListener implements ActionListener {
    StartupPanel adaptee;

    GOFilePickListener( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.classBrowseButton_actionPerformed();
    }
}

class StartupPanel_actionButton_actionAdapter implements java.awt.event.ActionListener {
    StartupPanel adaptee;

    StartupPanel_actionButton_actionAdapter( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.actionButton_actionPerformed( e );
    }
}

class StartupPanel_cancelButton_actionAdapter implements java.awt.event.ActionListener {

    public void actionPerformed( ActionEvent e ) {
        System.exit( 0 );
    }
}
