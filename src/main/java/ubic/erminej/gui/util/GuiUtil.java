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
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;

/**
 * Little oft-used functions.
 *
 * @author paul
 */
public class GuiUtil {

    /** Constant <code>log</code> */
    protected static final Log log = LogFactory.getLog( GuiUtil.class );

    /**
     * Center a frame on the screen.
     *
     * @param frame a {@link java.awt.Container} object.
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
     * <p>
     * chooseChildLocation.
     * </p>
     *
     * @param child a {@link java.awt.Container} object.
     * @param parent a {@link java.awt.Container} object.
     * @return a {@link java.awt.Point} object.
     */
    public static Point chooseChildLocation( Container child, Container parent ) {
        Dimension childSize = child.getPreferredSize();
        Dimension parentSize = parent.getSize();
        Point loc = parent.getLocation();
        Point childLoc = new Point( Math.max( 0, ( parentSize.width - childSize.width ) / 2 + loc.x ), Math.max( 0,
                ( parentSize.height - childSize.height ) / 2 + loc.y ) );
        return childLoc;
    }

    /**
     * Platform-independent chooser for saving files. For opening files you can just use a JFileChooser. This solves the
     * problem on MacOS where the user has no way to enter the name of the file they want to save in.
     *
     * @param owner the frame that will own the dialog
     * @param startingDirectory a {@link java.lang.String} object.
     * @param startingFileName a {@link java.lang.String} object.
     * @param statusMessenger a {@link ubic.basecode.util.StatusViewer} object.
     * @return a {@link java.io.File} object.
     */
    public static File chooseOutputFile( Frame owner, String startingDirectory, String startingFileName,
            StatusViewer statusMessenger ) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        chooser.setCurrentDirectory( new File( startingDirectory ) );
        chooser.setSelectedFile( new File( startingFileName ) );

        chooser.setApproveButtonText( "OK" );
        chooser.setDialogTitle( "Save As:" );

        int result = chooser.showSaveDialog( owner );

        if ( result == JFileChooser.APPROVE_OPTION ) {
            return chooser.getSelectedFile();
        }
        statusMessenger.showStatus( "Save cancelled." );
        return null;

    }

    /**
     * <p>
     * error.
     * </p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public static void error( String message ) {
        try {
            JOptionPane.showMessageDialog( null, "Error: " + message + "\n", "Error", JOptionPane.ERROR_MESSAGE );
            log.error( message );
        } catch ( HeadlessException e ) {
            // must be using the CLI actually.
            log.error( message );
        }
    }

    /**
     * <p>
     * error.
     * </p>
     *
     * @param message a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public static void error( String message, Throwable e ) {
        JOptionPane.showMessageDialog( null, "Error: " + message + "\n" + e, "Error", JOptionPane.ERROR_MESSAGE );
        log.error( e );
        e.printStackTrace();
    }

    /**
     * Build a standardized file browse field+button.
     *
     * @param container a {@link java.awt.Container} object.
     * @param h actionListener that should be wired to a file browser.
     * @return field that will contain the file name.
     */
    public static JTextField fileBrowsePanel( Container container, ActionListener h ) {
        JPanel panel = new JPanel();
        JTextField textField = new JTextField();

        /*
         * this really should return a JPanel subclass that has a getFileName method.
         */

        GroupLayout gl = new GroupLayout( panel );
        panel.setLayout( gl );
        JButton button = new JButton();
        button.setText( "Browse..." );
        button.addActionListener( h );
        textField.setPreferredSize( new Dimension( 400, 19 ) );
        textField.setMaximumSize( new Dimension( 700, 19 ) );

        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        gl.setHorizontalGroup( gl.createSequentialGroup().addComponent( textField ).addComponent( button ) );
        gl.setVerticalGroup( gl.createParallelGroup().addComponent( textField ).addComponent( button ) );

        if ( container != null ) container.add( panel );

        return textField;
    }

    /**
     * <p>
     * testDir.
     * </p>
     *
     * @param filename a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean testDir( String filename ) {
        if ( !FileTools.testDir( filename ) ) {
            error( "A required directory field is not valid." );
            return false;
        }
        return true;
    }

    /**
     * <p>
     * testFile. see {@link ubic.basecode.util.FileTools#checkPathIsReadableFile(String)}
     * </p>
     *
     * @param filename a {@link java.lang.String} object.
     * 
     * @return a boolean.
     */
    public static boolean testFile( String filename ) {
        if ( !FileTools.testFile( filename ) ) {
            error( "A required file field is not valid." );
            return false;
        }
        return false;
    }

}
