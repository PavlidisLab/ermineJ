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
package ubic.erminej;

import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.gui.GeneSetScoreFrame;
import ubic.erminej.gui.GuiUtil;
import ubic.erminej.gui.StartupDialog;

/**
 * Main for GUI
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class classScoreGUI {
    private static Log log = LogFactory.getLog( classScoreGUI.class.getName() );

    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            new classScoreGUI();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

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
        log.debug( "Showing startup dialog" );
        sdlog.showDialog();
        frame.showStatus( "Starting up..." );
    }

}