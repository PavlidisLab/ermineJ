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
package ubic.erminej.gui.geneset;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager; 

import ubic.erminej.Settings;
import ubic.erminej.gui.geneset.JGeneSetFrame;

/**
 * @author Will Braynen
 * @version $Id$
 */
public class GeneSetApp {

    /**
     * @throws IOException
     * @param filename the raw data file which contains the data for the probe ID's
     */
    public GeneSetApp( String filename ) throws IOException {

        Settings settings = new Settings();
        settings.setRawFile( filename );

        final String[] PROBES = { "31946_s_at", "31947_r_at", "31948_at", "31949_at", "31950_at" };
        Map<String, Double> pvalues = new HashMap<String, Double>();

        List<String> probeIDs = new ArrayList<String>();
        for ( int i = 0; i < PROBES.length; i++ ) {
            probeIDs.add( i, PROBES[i] );
            pvalues.put( PROBES[i], new Double( 0.5 - 0.02 * i ) ); // fake p values.
        }

        JGeneSetFrame frame = new JGeneSetFrame( "foo", null, probeIDs, pvalues, null, settings );
        frame.setSize( new Dimension( 800, 600 ) );
        frame.setVisible( true );
    }

    /**
     * @param args[0] the name of the raw data file, as an absolute path, where we look up the microarray data for each
     *        gene in the current gene set.
     */
    public static void main( String[] args ) throws Exception {

        // Make sure the filename was passed in
        if ( args.length < 1 ) {
            System.err.println( "Please specify the name of the data file as a program argument" );
            return;
        }
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        new GeneSetApp( args[0] );

    } // end main
} // end class
