package classScore.gui;

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

import baseCode.gui.AppDialog;
import baseCode.gui.GuiUtil;
import baseCode.util.FileTools;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Kiran Keshav
 * @author Homin K Lee
 * @version $Id$
 */

public class LoadDialog extends AppDialog {
    JFileChooser chooser = new JFileChooser();
    JPanel centerPanel = new JPanel();
    JButton loadBrowseButton = new JButton();
    JLabel annotLabel = new JLabel();
    JPanel loadPanel = new JPanel();
    JTextField loadFile = new JTextField();

    Settings settings;

    public LoadDialog( GeneSetScoreFrame callingframe ) {
        super( callingframe, 550, 250 );
        this.settings = callingframe.getSettings();
        chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
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
        addHelp( "<html><b>Load a previous analysis into the system.</b>"
                + "The file selected must be an analysis file saved from this " + "software.<br></html>" );
        addMain( centerPanel );
        this.setTitle( "Load Results from File" );
        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpButton );
    }

    void loadBrowseButton_actionPerformed( ActionEvent e ) {
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            loadFile.setText( chooser.getSelectedFile().toString() );
        }
    }

    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    protected void actionButton_actionPerformed( ActionEvent e ) {
        if ( FileTools.testFile( loadFile.getText() ) ) {
            try {
                ( ( GeneSetScoreFrame ) callingframe ).loadAnalysis( loadFile.getText() );
            } catch ( IOException e1 ) {
               GuiUtil.error("There was a problem: " + e1.getMessage());
            }
            dispose();
        } else {
            GuiUtil.error( "File is not readable." );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.gui.AppDialog#helpButton_actionPerformed(java.awt.event.ActionEvent)
     */
    protected void helpButton_actionPerformed( ActionEvent e ) {
        // TODO Auto-generated method stub
    }

}

class LoadDialog_loadBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    LoadDialog adaptee;

    LoadDialog_loadBrowseButton_actionAdapter( LoadDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.loadBrowseButton_actionPerformed( e );
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
