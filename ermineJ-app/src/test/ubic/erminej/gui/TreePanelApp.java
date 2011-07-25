/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.gui;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.UIManager;

import org.xml.sax.SAXException;

import ubic.erminej.data.GOParser;
import ubic.erminej.gui.TreePanel;
import ubic.erminej.gui.geneset.tree.GeneSetTreeNode;

/**
 * Not a 'real' test.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class TreePanelApp {
    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            new TreePanelApp();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    private GOParser gOParser = null;

    /**
     * Constructor for TestTreePanel.
     * 
     * @throws IOException
     * @throws SAXException
     */
    public TreePanelApp() throws SAXException, IOException {

        InputStream i =
        // GOParser.class.getResourceAsStream("/data/go-termdb-sample.xml");
        GOParser.class.getResourceAsStream( "/data/go_daily-termdb.rdf-sample2.xml" );
        gOParser = new GOParser( i );
        final JTree t = gOParser.getGraph().treeView( GeneSetTreeNode.class );

        // Create and set up the window.
        JFrame frame = new JFrame( "GOTreeDemo" );
        frame.setSize( 200, 200 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        // Create and set up the content pane.
        TreePanel newContentPane = new TreePanel( t );
        newContentPane.setOpaque( true ); // content panes must be opaque
        frame.setContentPane( newContentPane );
        // Display the window.
        frame.pack();
        frame.setVisible( true );

    }

}