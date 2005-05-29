package classScore.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.AppDialog;
import baseCode.gui.GuiUtil;
import baseCode.gui.file.DataFileFilter;
import baseCode.gui.file.XMLFileFilter;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */

public class StartupDialog extends AppDialog {
    private static Log log = LogFactory.getLog( StartupDialog.class.getName() );
    JFileChooser chooser;
    JPanel centerPanel = new JPanel();
    JPanel classPanel = new JPanel();
    JLabel classLabel = new JLabel();
    JTextField classFileTextField = new JTextField();
    JLabel annotFileFormatLabel = new JLabel();
    JButton annotBrowseButton = new JButton();
    JLabel annotLabel = new JLabel();
    JPanel annotPanel = new JPanel();
    JTextField annotFileTextField = new JTextField();
    JComboBox annotFormat = new JComboBox();

    Settings settings;
    JButton classBrowseButton = new JButton();

    public StartupDialog( GeneSetScoreFrame callingframe ) {
        super( callingframe, 550, 350 );
        this.settings = callingframe.getSettings();
        jbInit();
        setValues();
    }

    // Component initialization
    private void jbInit() {

        chooser = new JFileChooser( settings.getDataDirectory() );
        this.addWindowListener( new StartupDialog_this_windowAdapter( this ) );

        annotFileFormatLabel.setText( "Annotation file format" );
        annotFileFormatLabel.setLabelFor( annotFormat );
        annotFormat.setEditable( false );
        annotFormat.addItem( "ErmineJ" );
        annotFormat.addItem( "Affy CSV" );

        if ( settings.getAnnotFormat() == GeneAnnotations.AFFYCSV ) {
            annotFormat.setSelectedItem( "Affy CSV" );
        } else {
            annotFormat.setSelectedItem( "ErmineJ" );
        }

        annotBrowseButton.setText( "Browse..." );
        annotBrowseButton.addActionListener( new StartupDialog_annotBrowseButton_actionAdapter( this ) );
        annotLabel.setPreferredSize( new Dimension( 390, 15 ) );
        annotLabel.setRequestFocusEnabled( true );
        annotLabel.setText( "Probe annotation file:" );
        annotPanel.setPreferredSize( new java.awt.Dimension( 400, 70 ) );
        annotFileTextField.setPreferredSize( new Dimension( 300, 19 ) );
        classBrowseButton.addActionListener( new StartupDialog_classBrowseButton_actionAdapter( this ) );
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
        centerPanel.setPreferredSize( new java.awt.Dimension( 500, 300 ) );
        centerPanel.add( annotPanel, null );

        setActionButtonText( "Start" );
        setCancelButtonText( "Quit" );
        setHelpButtonText( "Help" );
        this
                .addHelp( "<html><head><style type=\"text/css\"> body { font-family:sanserif; font-size:10px} </style></head><b >Starting up the program</b><br>Please confirm "
                        + "the settings below are correct; they cannot be changed during "
                        + "analysis.<p>The probe annotation file you select "
                        + "must match the microarray design you are using. "
                        + "For updated annotation files, visit "
                        + "<a href=\"http://microarray.cpmc.columbia.edu/annots/\">http://microarray.cpmc.columbia.edu/annots</a></html>" );
        addMain( centerPanel );
        this.setTitle( "ErmineJ startup" );

        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpButton );
    }

    private void setValues() {
        if ( settings.getClassFile() == null ) {
            // see if there is a file available.
            String testPath = settings.getDataDirectory() + System.getProperty( "file.separator" )
                    + "go_daily-termdb.rdf-xml.gz"; // FIXME - move this.
            File testFile = new File( testPath );
            if ( testFile.exists() && testFile.canRead() && testFile.isFile() && testFile.length() > 0 ) {
                settings.setClassFile( testPath );
            }
        }
        classFileTextField.setText( settings.getClassFile() );
        annotFileTextField.setText( settings.getAnnotFile() );
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
    }

    private void saveValues() {
        settings.setClassFile( classFileTextField.getText() );
        settings.setAnnotFile( annotFileTextField.getText() );
        settings.setAnnotFormat( ( String ) annotFormat.getSelectedItem() );
        // try {
        // settings.writePrefs();
        // } catch ( org.apache.commons.configuration.ConfigurationException e ) {
        // GuiUtil.error( "Could not write preferences to a file:" + e );
        // }
    }

    void annotBrowseButton_actionPerformed( ActionEvent e ) {
        chooser.setDialogTitle( "Choose the annotation file:" );
        DataFileFilter fileFilter = new DataFileFilter();
        chooser.setFileFilter( fileFilter ); // JFileChooser method
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            annotFileTextField.setText( chooser.getSelectedFile().toString() );
        }
    }

    void classBrowseButton_actionPerformed( ActionEvent e ) {
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
            GuiUtil.error( "You must enter the annotation file location for your microarray design" );
        } else if ( !annotFile.exists() || !annotFile.canRead() ) {
            GuiUtil.error( "Could not read file: " + annotFileName );
        } else if ( !goFile.exists() || !goFile.canRead() ) {
            GuiUtil.error( "Could not read file: " + goFileName );
        } else {
            log.debug( "Saving configuration" );
            saveValues();
            class runthread extends Thread {
                public void run() {
                    ( ( GeneSetScoreFrame ) callingframe ).initialize();
                }
            }
            ;
            Thread aFrameRunner = new runthread();
            aFrameRunner.start();
            dispose();
        }
    }

    protected void helpButton_actionPerformed( ActionEvent e ) {

    }

    void this_windowClosed( WindowEvent e ) {
        System.exit( 0 );
    }

}

class StartupDialog_annotBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    StartupDialog adaptee;

    StartupDialog_annotBrowseButton_actionAdapter( StartupDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.annotBrowseButton_actionPerformed( e );
    }
}

class StartupDialog_this_windowAdapter extends java.awt.event.WindowAdapter {
    StartupDialog adaptee;

    StartupDialog_this_windowAdapter( StartupDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void windowClosing( WindowEvent e ) {
        adaptee.this_windowClosed( e );
    }
}

class StartupDialog_classBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    StartupDialog adaptee;

    StartupDialog_classBrowseButton_actionAdapter( StartupDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.classBrowseButton_actionPerformed( e );
    }
}
