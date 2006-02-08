/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScore.gui;

import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import baseCode.gui.AppDialog;
import baseCode.gui.GuiUtil;
import classScore.Settings;

/**
 * @author pavlidis
 * @version $Id$
 */
public class DataDirectoryDialog extends AppDialog {

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

    public DataDirectoryDialog( GeneSetScoreFrame callingframe ) {
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

    void dataDirBrowseButton_actionPerformed( ActionEvent e ) {
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
    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.AppDialog#actionButton_actionPerformed(java.awt.event.ActionEvent)
     */
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
    protected void helpButton_actionPerformed( ActionEvent e ) {

    }

}

class DataDirectoryDialog_dataDirBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    DataDirectoryDialog adaptee;

    DataDirectoryDialog_dataDirBrowseButton_actionAdapter( DataDirectoryDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.dataDirBrowseButton_actionPerformed( e );
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