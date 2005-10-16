/*
 * The ermineJ project
 * 
 * Copyright (c) 2005 Columbia University
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
package classScore;

import java.io.IOException;

import javax.swing.ImageIcon;
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
        GuiUtil.centerContainer( frame );
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