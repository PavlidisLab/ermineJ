/*
 * The ermineJ project
 * 
 * Copyright (c) 2013 University of British Columbia
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import ubic.basecode.graphics.ColorMap;
import ubic.basecode.graphics.JGradientBar;
import ubic.erminej.gui.util.GuiUtil;

/**
 * Test of gradient bar
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class GradientBarApp {

    // Main method: args[0] can contain the name of the data file
    @SuppressWarnings("unused")
    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        new GradientBarApp();
    }

    boolean packFrame = false;

    /** Creates a new instance of GradientBarApp */
    public GradientBarApp() {

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout( new FlowLayout() );
        frame.setSize( new Dimension( 300, 300 ) );
        frame.setTitle( "JGradientBar Test" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        Color[] colorMap = ColorMap.GREENRED_COLORMAP;
        JGradientBar gradientBar = new JGradientBar( colorMap );
        gradientBar.setColorMap( colorMap );
        gradientBar.setBorder( new EmptyBorder( 2, 2, 2, 2 ) );
        // gradientBar.setBackground( Color.LIGHT_GRAY );
        gradientBar.setLabels( -2.0, 2.0 );

        frame.getContentPane().add( gradientBar );

        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their
        // layout
        if ( packFrame ) {
            frame.pack();
        } else {
            frame.validate();
        }
        GuiUtil.centerContainer( frame );

        frame.setVisible( true );

    }
}