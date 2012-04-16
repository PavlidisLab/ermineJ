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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.gui.util.JLinkLabel;

/**
 * Displays 'about' information for the software.
 * 
 * @author Kiran Keshav
 * @author Paul Pavlidis
 * @version $Id$
 */
public class AboutBox extends JDialog {

    private static Log log = LogFactory.getLog( AboutBox.class.getName() );

    private static final int TOTAL_HEIGHT = 460;
    private static final int PREFERRED_WIDTH = 450;
    private String version = "3.0";

    private final static String COPYRIGHT = "<html>Copyright &copy; University of British Columbia</html>";
    private static final String SOFTWARENAME = "ermineJ";
    private static String homepageURL = "http://erminej.chibi.ubc.ca/";

    public AboutBox( Frame parent ) {
        super( parent, "About " + SOFTWARENAME, true );
        this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        Point centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        // this.setLocationRelativeTo( parent );
        this.setLocation( ( int ) centerPoint.getX() - ( PREFERRED_WIDTH / 2 ), ( int ) centerPoint.getY()
                - ( TOTAL_HEIGHT / 2 ) );
        jbInit();

    }

    // Component initialization
    private void jbInit() {

        this.getContentPane().setBackground( Color.white );
        this.setSize( new Dimension( PREFERRED_WIDTH + 20, TOTAL_HEIGHT + 20 ) );

        JPanel mainPanel = new JPanel();

        mainPanel.setLayout( new BorderLayout() );
        mainPanel.setBackground( Color.white );

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment( SwingConstants.CENTER );
        imageLabel.setHorizontalTextPosition( SwingConstants.CENTER );
        URL icon = this.getClass().getResource( "/ubic/erminej/logo1small.gif" );
        imageLabel.setIcon( new ImageIcon( icon ) );

        JLabel versionLabel = new JLabel();
        versionLabel.setPreferredSize( new Dimension( PREFERRED_WIDTH, 30 ) );
        versionLabel.setHorizontalAlignment( SwingConstants.CENTER );
        versionLabel.setHorizontalTextPosition( SwingConstants.LEFT );
        versionLabel.setText( "Version " + getVersion() );

        JLabel copyrightLabel = new JLabel();
        copyrightLabel.setPreferredSize( new Dimension( PREFERRED_WIDTH, 30 ) );
        copyrightLabel.setHorizontalAlignment( SwingConstants.CENTER );
        copyrightLabel.setText( COPYRIGHT );

        JLabel labelAuthors = new JLabel();
        labelAuthors.setPreferredSize( new Dimension( PREFERRED_WIDTH, 60 ) );
        labelAuthors.setHorizontalAlignment( SwingConstants.CENTER );
        labelAuthors.setHorizontalTextPosition( SwingConstants.CENTER );
        labelAuthors.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        labelAuthors
                .setText( "<html>By: Paul Pavlidis, Homin Lee, Will Braynen, Shahmil Merchant, Kiran Keshav, Kelsey Hamer and others</html>" );

        JLinkLabel labelHomepage = new JLinkLabel( homepageURL, homepageURL );
        labelHomepage.setHorizontalAlignment( SwingConstants.CENTER );
        // labelHomepage.setPreferredSize( new Dimension( 200, 20 ) );
        labelHomepage.makeMouseListener();

        JPanel blurbsPanel = new JPanel();
        blurbsPanel.setBackground( Color.white );
        blurbsPanel.setPreferredSize( new Dimension( PREFERRED_WIDTH, 160 ) );
        blurbsPanel.setRequestFocusEnabled( true );
        blurbsPanel.add( versionLabel );
        blurbsPanel.add( copyrightLabel );
        blurbsPanel.add( labelAuthors );
        blurbsPanel.add( labelHomepage );

        JTextPane licensePanel = new JTextPane();
        licensePanel.setBackground( Color.white );
        licensePanel.setPreferredSize( new Dimension( PREFERRED_WIDTH, 150 ) );
        licensePanel.setEditable( false );
        licensePanel.setMargin( new Insets( 10, 10, 10, 10 ) );
        licensePanel.setContentType( "text/html" );
        licensePanel
                .setText( "<html><p>ErmineJ is licensed under the Apache 2 Public License.</p><p>Direct questions to "
                        + "erminej@chibi.ubc.ca</p><p>If you use this software for your work, please cite:<br/> "

                        + "Gillis J, Mistry M, Pavlidis P. (2010)"
                        + " Gene function analysis in complex data sets using ErmineJ. Nature Protocols 5:1148-59"
                        + "</p></html>" );

        JPanel centerPanel = new JPanel( new BorderLayout() );
        centerPanel.setBackground( Color.white );
        centerPanel.add( blurbsPanel, BorderLayout.NORTH );
        centerPanel.add( licensePanel, BorderLayout.SOUTH );

        JButton closeButton = new JButton( "Ok" );

        closeButton.setAction( new AbstractAction( "Ok" ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        } );

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground( Color.white );
        buttonPanel.setBorder( BorderFactory.createEtchedBorder() );
        buttonPanel.add( closeButton );

        mainPanel.add( imageLabel, BorderLayout.NORTH );
        mainPanel.add( centerPanel, BorderLayout.CENTER );
        mainPanel.add( buttonPanel, BorderLayout.SOUTH );

        this.add( mainPanel );
        pack();
    }

    private String getVersion() {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream( "/ubic/erminej/version" );
            return ( new BufferedReader( new InputStreamReader( resourceAsStream ) ) ).readLine();
        } catch ( Exception e ) {
            log.error( "Could not determine version number: " + e );
            return this.version;
        }
    }

}