/*
 * The baseCode project
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
package ubic.erminej.gui.util;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;

/**
 * Little oft-used functions.
 * 
 * @version $Id$
 */
public class GuiUtil {

    protected static final Log log = LogFactory.getLog( GuiUtil.class );

    /**
     * Build a standardized file browse field+button.
     * 
     * @param container
     * @param h actionListener that should be wired to a file browser.
     * @return field that will contain the file name.
     */
    public static JTextField fileBrowsePanel( Container container, ActionListener h ) {
        JPanel panel = new JPanel();
        JTextField textField = new JTextField();

        /*
         * TODO this really should return a JPanel subclass that has a getFileName method.
         */

        GroupLayout gl = new GroupLayout( panel );
        panel.setLayout( gl );
        JButton button = new JButton();
        button.setText( "Browse..." );
        button.addActionListener( h );
        textField.setPreferredSize( new Dimension( 400, 19 ) );
        textField.setMaximumSize( new Dimension( 800, 19 ) );

        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        gl.setHorizontalGroup( gl.createSequentialGroup().addComponent( textField ).addComponent( button ) );
        gl.setVerticalGroup( gl.createParallelGroup().addComponent( textField ).addComponent( button ) );

        panel.setBorder( BorderFactory.createEmptyBorder( 8, 8, 6, 6 ) );

        if ( container != null ) container.add( panel );

        return textField;
    }

    /**
     * Center a frame on the screen.
     * 
     * @param frame
     */
    public static void centerContainer( Container frame ) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if ( frameSize.height > screenSize.height ) {
            frameSize.height = screenSize.height;
        }
        if ( frameSize.width > screenSize.width ) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
    }

    /**
     * @return
     */
    public static Point chooseChildLocation( Container child, Container parent ) {
        Dimension childSize = child.getPreferredSize();
        Dimension parentSize = parent.getSize();
        Point loc = parent.getLocation();
        Point childLoc = new Point( ( parentSize.width - childSize.width ) / 2 + loc.x,
                ( parentSize.height - childSize.height ) / 2 + loc.y );
        return childLoc;
    }

    /**
     * @param message
     */
    public static void error( String message ) {
        JOptionPane.showMessageDialog( null, "Error: " + message + "\n", "Error", JOptionPane.ERROR_MESSAGE );
        log.error( message );
    }

    /**
     * @param message
     * @param e
     */
    public static void error( String message, Throwable e ) {
        JOptionPane.showMessageDialog( null, "Error: " + message + "\n" + e, "Error", JOptionPane.ERROR_MESSAGE );
        log.error( e );
        e.printStackTrace();
    }

    /**
     * @param text
     * @return
     */
    public static boolean testDir( String filename ) {
        if ( !FileTools.testDir( filename ) ) {
            error( "A required directory field is not valid." );
            return false;
        }
        return true;
    }

    /**
     * @param filename
     * @return
     * @see basecode.util.FileTools#checkPathIsReadableFile(String)
     */
    public static boolean testFile( String filename ) {
        if ( !FileTools.testFile( filename ) ) {
            error( "A required file field is not valid." );
            return false;
        }
        return false;
    }

}