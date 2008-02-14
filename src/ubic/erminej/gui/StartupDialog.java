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
package ubic.erminej.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.gui.AppDialog;
import ubic.basecode.gui.GuiUtil;
import ubic.basecode.gui.file.DataFileFilter;
import ubic.basecode.gui.file.XMLFileFilter;
import ubic.erminej.Settings;

/**
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */

public class StartupDialog extends AppDialog {

    /**
     * 
     */
    private static final long serialVersionUID = -376725612513363691L;
    private static final String DEFAULT_GO_TERM_FILE_NAME = "go_daily-termdb.rdf-xml.gz";
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
        annotFormat.addItem( "Agilent" );

        if ( settings.getAnnotFormat() == GeneAnnotations.AFFYCSV ) {
            annotFormat.setSelectedItem( "Affy CSV" );
        } else if ( settings.getAnnotFormat() == GeneAnnotations.AGILENT ) {
            annotFormat.setSelectedItem( "Agilent" );
        } else {
            annotFormat.setSelectedItem( "ErmineJ" );
        }

        annotBrowseButton.setText( "Browse..." );
        annotBrowseButton.addActionListener( new StartupDialog_annotBrowseButton_actionAdapter( this ) );
        annotLabel.setPreferredSize( new Dimension( 390, 15 ) );
        annotLabel.setRequestFocusEnabled( true );
        annotLabel.setText( "Probe annotation file:" );
        annotPanel.setPreferredSize( new java.awt.Dimension( 400, 80 ) );
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
        centerPanel.setPreferredSize( new java.awt.Dimension( 500, 350 ) );
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
                        + "<a href=\"http://bioinformatics.ubc.ca/microannots/\">http://bioinformatics.ubc.ca/microannots</a></html>" );
        addMain( centerPanel );
        this.setTitle( "ErmineJ startup" );

        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpButton );
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

    @Override
    protected void cancelButton_actionPerformed( ActionEvent e ) {
        System.exit( 0 );
    }

    @Override
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
            new Thread() {
                @Override
                public void run() {
                    ( ( GeneSetScoreFrame ) callingframe ).initialize();
                }
            }.start();
            dispose();
        }
    }

    @Override
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

    @Override
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
