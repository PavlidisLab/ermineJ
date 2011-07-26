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

    public StartupPanel( Settings settings ) {
        this.settings = settings;
        jbInit();
        setValues();
    }

    private JButton actionButton;
    private JTextField classFileTextField = new JTextField();
    // private

    private JTextField annotFileTextField = new JTextField();
    private JComboBox annotFormat = new JComboBox();

    private Settings settings;

    private void jbInit() {

        JPanel bottomPanel = new JPanel();
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
        bottomPanel.add( helpButton, null );
        bottomPanel.add( cancelButton, null );
        bottomPanel.add( actionButton, null );

        // ////////////////

        JLabel annotFileFormatLabel = new JLabel();
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
        JButton annotBrowseButton = new JButton();
        annotBrowseButton.setText( "Browse..." );
        annotBrowseButton.addActionListener( new StartupPanel_annotBrowseButton_actionAdapter( this ) );
        JLabel annotLabel = new JLabel();

        annotLabel.setPreferredSize( new Dimension( 390, 15 ) );
        annotLabel.setRequestFocusEnabled( true );
        annotLabel.setText( "Gene annotation file:" );

        annotFileTextField.setPreferredSize( new Dimension( 300, 19 ) );
        JPanel annotPanel = new JPanel();
        annotPanel.setPreferredSize( new java.awt.Dimension( 400, 80 ) );
        annotPanel.add( annotLabel, null );
        annotPanel.add( annotFileTextField, null );
        annotPanel.add( annotBrowseButton, null );
        annotPanel.add( annotFileFormatLabel, null );
        annotPanel.add( annotFormat, null );

        JPanel centerPanel = new JPanel();
        JPanel classPanel = new JPanel();
        classPanel.setPreferredSize( new java.awt.Dimension( 400, 70 ) );
        JLabel classLabel = new JLabel();
        classLabel.setPreferredSize( new Dimension( 390, 15 ) );
        classLabel.setText( "GO XML file:" );
        classFileTextField.setPreferredSize( new Dimension( 300, 19 ) );

        classPanel.add( classLabel, null );
        classPanel.add( classFileTextField, null );
        JButton classBrowseButton = new JButton();
        classPanel.add( classBrowseButton, null );
        classBrowseButton.addActionListener( new StartupPanel_classBrowseButton_actionAdapter( this ) );
        classBrowseButton.setText( "Browse..." );

        centerPanel.setLayout( new BorderLayout() );
        centerPanel.add( classPanel, BorderLayout.NORTH );
        centerPanel.add( annotPanel, BorderLayout.SOUTH );

        JEditorPane h = new JEditorPane();
        h.setEditable( false );
        h.setFont( new Font( "SansSerif", Font.PLAIN, 11 ) );
        h.setContentType( "text/html" );
        h.addHyperlinkListener( new HyperlinkListener() {

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
        h.setText( "<html> <strong>Starting up the program</strong><br>Please confirm "
                + "the settings below are correct; they cannot be changed during "
                + "analysis.<p>The annotation file you select " + "must match the experimental data you are using. "
                + "For updated annotation files, visit "
                + "<a href=\"http://www.chibi.ubc.ca/microannots/\">http://www.chibi.ubc.ca/microannots</a></html>" );

        this.setLayout( new BorderLayout() );
        this.add( h, BorderLayout.NORTH );
        this.add( centerPanel, BorderLayout.CENTER );
        this.add( bottomPanel, BorderLayout.SOUTH );

    }

    // for testing.
    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {

        }
        JFrame f = new JFrame();
        f.setSize( new Dimension( 400, 400 ) );
        StartupPanel p = new StartupPanel( new Settings() );
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
        }
    }

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
        }
    }

    public void addActionListener( ActionListener listener ) {
        listenerList.add( ActionListener.class, listener );
    }

    public void removeActionListener( ActionListener listener ) {
        listenerList.remove( ActionListener.class, listener );
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
            saveSettings();

            Object[] listeners = listenerList.getListenerList();
            for ( int i = 0; i < listeners.length; i += 2 ) {
                if ( listeners[i] == ActionListener.class ) {
                    ( ( ActionListener ) listeners[i + 1] ).actionPerformed( e );
                }
            }
        }
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
