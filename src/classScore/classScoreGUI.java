package classScore;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import baseCode.gui.GuiUtil;

import classScore.gui.GeneSetScoreFrame;
import classScore.gui.StartupDialog;

/**
 * Main for GUI
 * <p>
 * Copyright (c) 2003 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class classScoreGUI {
    boolean packFrame = false;

    public classScoreGUI() throws IOException {
        // mainFrame frame = new mainFrame();
        GeneSetScoreFrame frame = new GeneSetScoreFrame();

        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their
        // layout
        if ( packFrame ) {
            frame.pack();
        } else {
            frame.validate();
        }
        GuiUtil.centerFrame( frame );
        frame.disableMenusForLoad();
        frame.setVisible( true );
        frame.showStatus( "Waiting for startup dialog box" );
        frame.setIconImage( new ImageIcon( this.getClass().getResource( "gui/resources/logoIcon64.gif" ) ).getImage() );
        StartupDialog sdlog = new StartupDialog( frame );
        sdlog.setModal( true );
        sdlog.setResizable( true );
        sdlog.showDialog();

        frame.showStatus( "Starting up..." );
    }

    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            new classScoreGUI();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}