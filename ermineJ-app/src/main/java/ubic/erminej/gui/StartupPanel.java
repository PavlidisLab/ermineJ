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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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

    private static Log log = LogFactory.getLog( StartupPanel.class );
    private static final String DEFAULT_GO_TERM_FILE_NAME = "go_daily-termdb.rdf-xml.gz";
    private final Container callingframe;
    protected JButton actionButton = new JButton();
    protected JButton cancelButton = new JButton();
    protected JButton helpButton = new JButton();

    JPanel bottomPanel = new JPanel();

    public StartupPanel( Container callingframe, Settings settings ) {
        this.callingframe = callingframe;
        this.settings = settings;
        jbInit( 550, 350 );
        setValues();
    }

    private JFileChooser chooser;
    private JPanel centerPanel = new JPanel();
    private JPanel classPanel = new JPanel();
    private JLabel classLabel = new JLabel();
    private JTextField classFileTextField = new JTextField();
    private JLabel annotFileFormatLabel = new JLabel();
    private JButton annotBrowseButton = new JButton();
    private JLabel annotLabel = new JLabel();
    private JPanel annotPanel = new JPanel();
    private JTextField annotFileTextField = new JTextField();
    private JComboBox annotFormat = new JComboBox();

    private Settings settings;
    private JButton classBrowseButton = new JButton();

    private void jbInit( int width, int height ) {

        bottomPanel.setPreferredSize( new Dimension( width, 40 ) );
        cancelButton.setText( "Cancel" );
        cancelButton.setMnemonic( 'c' );
        cancelButton.addActionListener( new StartupPanel_cancelButton_actionAdapter( this ) );
        actionButton.addActionListener( new StartupPanel_actionButton_actionAdapter( this ) );

        helpButton.setText( "Help" );

        bottomPanel.add( helpButton, null );
        bottomPanel.add( cancelButton, null );
        bottomPanel.add( actionButton, null );

        // ////////////////

        chooser = new JFileChooser( settings.getDataDirectory() );

        annotFileFormatLabel.setText( "Annotation file format" );
        annotFileFormatLabel.setLabelFor( annotFormat );
        annotFormat.setEditable( false );
        annotFormat.addItem( "ErmineJ" );
        annotFormat.addItem( "Affy CSV" );
        annotFormat.addItem( "Agilent" );

        if ( settings.getAnnotFormat() == Format.AFFYCSV ) {
            annotFormat.setSelectedItem( "Affy CSV" );
        } else if ( settings.getAnnotFormat() == Format.AGILENT ) {
            annotFormat.setSelectedItem( "Agilent" );
        } else {
            annotFormat.setSelectedItem( "ErmineJ" );
        }

        annotBrowseButton.setText( "Browse..." );
        annotBrowseButton.addActionListener( new StartupPanel_annotBrowseButton_actionAdapter( this ) );
        annotLabel.setPreferredSize( new Dimension( 390, 15 ) );
        annotLabel.setRequestFocusEnabled( true );
        annotLabel.setText( "Probe annotation file:" );
        annotPanel.setPreferredSize( new java.awt.Dimension( 400, 80 ) );
        annotFileTextField.setPreferredSize( new Dimension( 300, 19 ) );

        classBrowseButton.addActionListener( new StartupPanel_classBrowseButton_actionAdapter( this ) );
        classBrowseButton.setText( "Browse..." );

        annotPanel.add( annotLabel, null );
        annotPanel.add( annotFileTextField, null );
        annotPanel.add( annotBrowseButton, null );
        annotPanel.add( annotFileFormatLabel, null );
        annotPanel.add( annotFormat, null );

        classPanel.setPreferredSize( new java.awt.Dimension( 400, 70 ) );
        classLabel.setPreferredSize( new Dimension( 390, 15 ) );
        classLabel.setText( "GO XML file:" );
        classFileTextField.setPreferredSize( new Dimension( 300, 19 ) );

        classPanel.add( classLabel, null );
        classPanel.add( classFileTextField, null );
        classPanel.add( classBrowseButton, null );

        centerPanel.add( classPanel, null );
        centerPanel.setPreferredSize( new java.awt.Dimension( 500, 350 ) );
        centerPanel.add( annotPanel, null );

        this.actionButton.setText( "Start" );
        this.cancelButton.setText( "Quit" );
        this.helpButton.setText( "Help" );
        this
                .addHelp( "<html><head><style type=\"text/css\"> body { font-family:sanserif; font-size:10px} </style></head><b >Starting up the program</b><br>Please confirm "
                        + "the settings below are correct; they cannot be changed during "
                        + "analysis.<p>The annotation file you select "
                        + "must match the experimental data you are using. "
                        + "For updated annotation files, visit "
                        + "<a href=\"http://www.chibi.ubc.ca/microannots/\">http://www.chibi.ubc.ca/microannots</a></html>" );

        this.add( centerPanel, BorderLayout.NORTH );
        this.add( bottomPanel, BorderLayout.SOUTH );

        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpButton );

    }

    // for testing.
    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {

        }
        JFrame f = new JFrame();
        f.setSize( new Dimension( 400, 400 ) );
        StartupPanel p = new StartupPanel( f, new Settings() );
        p.setSize( new Dimension( 200, 200 ) );
        f.add( p );
        f.pack();
        GuiUtil.centerContainer( f );
        f.setVisible( true );

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
            chooser.setCurrentDirectory( new File( System.getProperty( "user.dir" ) ) );
        } else {
            chooser.setCurrentDirectory( new File( dataDirectory ) );
        }
    }

    private void saveValues() {
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

    void annotBrowseButton_actionPerformed() {
        chooser.setDialogTitle( "Choose the annotation file:" );
        DataFileFilter fileFilter = new DataFileFilter();
        chooser.setFileFilter( fileFilter ); // JFileChooser method
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            annotFileTextField.setText( chooser.getSelectedFile().toString() );
        }
    }

    void classBrowseButton_actionPerformed() {
        chooser.setDialogTitle( "Choose the GO XML file:" );
        XMLFileFilter fileFilter = new XMLFileFilter();
        chooser.setFileFilter( fileFilter ); // JFileChooser method
        chooser.setAcceptAllFileFilterUsed( false );
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            classFileTextField.setText( chooser.getSelectedFile().toString() );
        }
    }

    protected void cancelButton_actionPerformed( ActionEvent e ) {
        System.exit( 0 );
    }

    protected void actionButton_actionPerformed( ActionEvent e ) {
        String annotFileName = annotFileTextField.getText();
        String goFileName = classFileTextField.getText();

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
            saveValues();
            new Thread() {
                @Override
                public void run() {
                    ( ( MainFrame ) callingframe ).initialize();
                }
            }.start();
        }
    }

    // Slightly specialized editor pane.
    class HelpEditorPane extends JEditorPane {
        /**
         * 
         */
        private static final long serialVersionUID = -5734511581620275891L;

        HelpEditorPane( String text ) {
            super();
            this.setEditable( false );
            this.setFont( new Font( "SansSerif", Font.PLAIN, 11 ) );
            this.setContentType( "text/html" );
            this.setText( text );
            this.addHyperlinkListener( new LinkFollower() );
        }
    }

    // helper to respond to links.
    class LinkFollower implements HyperlinkListener {

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
         */
        public void hyperlinkUpdate( HyperlinkEvent e ) {
            if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                try {
                    BrowserLauncher.openURL( e.getURL().toExternalForm() );
                } catch ( Exception e1 ) {
                    GuiUtil.error( "Could not open link" );
                }
            }
        }
    }

    protected void addHelp( String text ) {

        HelpEditorPane helpArea = null;

        helpArea = new HelpEditorPane( text );
        JLabel jLabel1 = new JLabel( "      " );
        JLabel jLabel2 = new JLabel( " " );
        JLabel jLabel3 = new JLabel( " " );
        JLabel jLabel4 = new JLabel( "      " );
        BorderLayout borderLayout2 = new BorderLayout();
        JPanel labelPanel = new JPanel();
        labelPanel.setBackground( Color.WHITE );
        labelPanel.setLayout( borderLayout2 );
        labelPanel.add( helpArea, BorderLayout.CENTER );
        labelPanel.add( jLabel1, BorderLayout.WEST );
        labelPanel.add( jLabel2, BorderLayout.NORTH );
        labelPanel.add( jLabel3, BorderLayout.SOUTH );
        labelPanel.add( jLabel4, BorderLayout.EAST );
        centerPanel.add( labelPanel, BorderLayout.NORTH );

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
    StartupPanel adaptee;

    StartupPanel_cancelButton_actionAdapter( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.cancelButton_actionPerformed( e );
    }
}

class StartupPanel_annotBrowseButton_actionAdapter implements ActionListener {
    StartupPanel adaptee;

    StartupPanel_annotBrowseButton_actionAdapter( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.annotBrowseButton_actionPerformed();
    }
}

class StartupPanel_classBrowseButton_actionAdapter implements ActionListener {
    StartupPanel adaptee;

    StartupPanel_classBrowseButton_actionAdapter( StartupPanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.classBrowseButton_actionPerformed();
    }
}
