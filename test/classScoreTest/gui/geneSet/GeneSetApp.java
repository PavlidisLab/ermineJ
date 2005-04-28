package classScoreTest.gui.geneSet;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import classScore.Settings;
import classScore.gui.geneSet.JGeneSetFrame;

/**
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Institution:: Columbia University
 * </p>
 * 
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
        HashMap pvalues = new HashMap();

        List probeIDs = new ArrayList();
        for ( int i = 0; i < PROBES.length; i++ ) {
            probeIDs.add( i, PROBES[i] );
            pvalues.put( PROBES[i], new Double( 0.5 - 0.02 * i ) ); // fake p values.
        }

        JGeneSetFrame frame = new JGeneSetFrame( probeIDs, pvalues, null, settings, null, null );
        frame.setSize( new Dimension( 800, 600 ) );
        frame.show();
    }

    /**
     * @param args[0] the name of the raw data file, as an absolute path, where we look up the microarray data for each
     *        gene in the current gene set.
     */
    public static void main( String[] args ) {

        // Make sure the filename was passed in
        if ( args.length < 1 ) {
            System.err.println( "Please specify the name of the data file as a program argument" );
            return;
        }
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            GeneSetApp app = new GeneSetApp( args[0] );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ClassNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnsupportedLookAndFeelException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    } // end main
} // end class
