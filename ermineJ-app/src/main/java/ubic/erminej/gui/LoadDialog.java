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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ubic.basecode.util.FileTools;

import ubic.erminej.Settings;
import ubic.erminej.gui.util.GuiUtil;

/**
 * @author Kiran Keshav
 * @author Homin K Lee
 * @version $Id$
 */
public class LoadDialog extends AppDialog {
    /**
     * 
     */
    private static final long serialVersionUID = -5573931795878079049L;
    private JFileChooser chooser = new JFileChooser();
    private JPanel centerPanel = new JPanel();
    private JButton loadBrowseButton = new JButton();
    private JLabel annotLabel = new JLabel();
    private JPanel loadPanel = new JPanel();
    JTextField loadFile = new JTextField();

    protected Settings settings;
    private static final String RESULTS_LOAD_LOCATION = "resultsLoadPath";

    public LoadDialog( MainFrame callingframe ) {
        super( callingframe, 550, 250 );
        this.settings = callingframe.getSettings();
        chooser.setCurrentDirectory( new File( settings.getConfig().getString( RESULTS_LOAD_LOCATION,
                settings.getDataDirectory() ) ) );
        chooser.setDialogTitle( "Open Saved Analysis" );
        jbInit();
    }

    // Component initialization
    private void jbInit() {
        centerPanel.setLayout( new BorderLayout() ); // new
        loadBrowseButton.setEnabled( true );
        loadBrowseButton.setText( "Browse...." );
        loadBrowseButton.addActionListener( new LoadDialog_loadBrowseButton_actionAdapter( this ) );
        annotLabel.setPreferredSize( new Dimension( 320, 15 ) );
        annotLabel.setText( "Load file:" );
        loadPanel.setBackground( SystemColor.control );
        loadPanel.setPreferredSize( new Dimension( 330, 100 ) );
        loadFile.setPreferredSize( new Dimension( 230, 19 ) );
        loadPanel.add( annotLabel, null );
        loadPanel.add( loadFile, null );
        loadPanel.add( loadBrowseButton, null );
        centerPanel.add( loadPanel, BorderLayout.CENTER );
        setActionButtonText( "Load" );
        setCancelButtonText( "Cancel" );
        setHelpButtonText( "Help" );
        addHelp( "<html><b>Load a previous analysis</b> "
                + "The file selected must be an analysis file saved from this software, "
                + "using the current annotationsS.<br></html>" );
        addMain( centerPanel );
        this.setTitle( "Load Results from File" );
        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpButton );
    }

    void loadBrowseButton_actionPerformed() {
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            loadFile.setText( chooser.getSelectedFile().toString() );
            settings.getConfig().setProperty( RESULTS_LOAD_LOCATION, chooser.getSelectedFile().getAbsolutePath() );
        }
    }

    @Override
    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    @Override
    protected void actionButton_actionPerformed( ActionEvent e ) {
        if ( FileTools.testFile( loadFile.getText() ) ) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        ( ( MainFrame ) callingframe ).loadAnalysis( loadFile.getText() );
                        ( ( MainFrame ) callingframe ).setSettings( settings );
                        ( ( MainFrame ) callingframe ).enableMenusForAnalysis();
                    } catch ( IOException e ) {
                        GuiUtil.error( "There was an error: " + e.getMessage() );
                    }
                }
            }.start();
            dispose();
        } else {
            GuiUtil.error( "File is not readable." );
        }
    } /*
       * (non-Javadoc)
       * 
       * @see baseCode.gui.AppDialog#helpButton_actionPerformed(java.awt.event.ActionEvent)
       */

    @Override
    protected void helpButton_actionPerformed( ActionEvent e ) {
        //
    }

}

class LoadDialog_loadBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    LoadDialog adaptee;

    LoadDialog_loadBrowseButton_actionAdapter( LoadDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.loadBrowseButton_actionPerformed();
    }
}

class LoadDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
    LoadDialog adaptee;

    LoadDialog_cancelButton_actionAdapter( LoadDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.cancelButton_actionPerformed( e );
    }
}

class LoadDialog_actionButton_actionAdapter implements java.awt.event.ActionListener {
    LoadDialog adaptee;

    LoadDialog_actionButton_actionAdapter( LoadDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.actionButton_actionPerformed( e );
    }
}
