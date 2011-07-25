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
import java.awt.SystemColor;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ubic.erminej.Settings;
import ubic.erminej.gui.util.GuiUtil;

/**
 * @author pavlidis
 * @version $Id$
 */
public class DataDirectoryDialog extends AppDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 745890257151041233L;
    JFileChooser chooser = new JFileChooser();
    JButton loadBrowseButton = new JButton();
    Settings settings;
    JTextField dataDir = new JTextField();
    JPanel loadPanel = new JPanel();
    JLabel annotLabel = new JLabel();
    JPanel centerPanel = new JPanel();

    public DataDirectoryDialog( Settings settings ) {
        this.settings = settings;
        this.callingframe = this.getOwner();
        chooser.setDialogTitle( "Locate the data directory" );
        jbInit();
    }

    public DataDirectoryDialog( MainFrame callingframe ) {
        super( callingframe, 400, 200 );
        this.settings = callingframe.getSettings();
        // chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
        chooser.setDialogTitle( "Locate the data directory" );
        jbInit();

    }

    // Component initialization
    private void jbInit() {
        loadBrowseButton.setEnabled( true );
        loadBrowseButton.setText( "Browse...." );
        loadBrowseButton.addActionListener( new DataDirectoryDialog_dataDirBrowseButton_actionAdapter( this ) );
        annotLabel.setPreferredSize( new Dimension( 320, 15 ) );
        annotLabel.setText( "Load file:" );
        loadPanel.setBackground( SystemColor.control );
        loadPanel.setPreferredSize( new Dimension( 330, 50 ) );
        dataDir.setPreferredSize( new Dimension( 230, 19 ) );
        loadPanel.add( annotLabel, null );
        loadPanel.add( dataDir, null );
        loadPanel.add( loadBrowseButton, null );
        centerPanel.add( loadPanel, null );

        setActionButtonText( "OK" );
        addHelp( "<html><b>The data directory needs to be located</b>"
                + "This directory is where software settings, annotation files "
                + "and custom gene sets are stored.<br></html>" );
        addMain( centerPanel );
        this.setTitle( "Locate the data directory" );
        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpButton );
    }

    void dataDirBrowseButton_actionPerformed(  ) {
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            dataDir.setText( chooser.getSelectedFile().toString() );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.AppDialog#cancelButton_actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.AppDialog#actionButton_actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    protected void actionButton_actionPerformed( ActionEvent e ) {
        if ( GuiUtil.testDir( dataDir.getText() ) ) {
            settings.setDataDirectory( dataDir.getText() );
            dispose();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.AppDialog#helpButton_actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    protected void helpButton_actionPerformed( ActionEvent e ) {

    }

}

class DataDirectoryDialog_dataDirBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    DataDirectoryDialog adaptee;

    DataDirectoryDialog_dataDirBrowseButton_actionAdapter( DataDirectoryDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.dataDirBrowseButton_actionPerformed(  );
    }
}

class DataDirectoryDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
    DataDirectoryDialog adaptee;

    DataDirectoryDialog_cancelButton_actionAdapter( DataDirectoryDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.cancelButton_actionPerformed( e );
    }
}

class DataDirectoryDialog_actionButton_actionAdapter implements java.awt.event.ActionListener {
    DataDirectoryDialog adaptee;

    DataDirectoryDialog_actionButton_actionAdapter( DataDirectoryDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.actionButton_actionPerformed( e );
    }
}