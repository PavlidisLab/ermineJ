package test.classScoreTest.gui.geneSet;

import classScore.gui.geneSet.JGeneSetFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Dimension;
import classScore.Settings;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Institution:: Columbia University</p>
 * @Will Braynen
 * @version 1.0
 */

public class JGeneSetFrameApp {

   /**
    * @param filename the raw data file which contains the data for the probe ID's
    */
   public JGeneSetFrameApp(String filename) {

      Settings settings = new Settings();
      settings.setRawFile( filename );

      final String[] PROBES = { "probe1", "probe2", "probe3", "probe4", "probe5" };
      HashMap pvalues = new HashMap();

      ArrayList probeIDs = new ArrayList();
      for ( int i = 0; i < PROBES.length; i++ ) {
         probeIDs.add( i, PROBES[i] );
         pvalues.put( PROBES[i], new Double( 0.5 ) );
      }

      JGeneSetFrame frame = new JGeneSetFrame( probeIDs, pvalues, null, settings );
      frame.setSize( new Dimension( 800, 600 ) );
      frame.show();
   }


   /**
    * @param  args[0]  the name of the raw data file, as an absolute path,
    *                  where we look up the microarray data for each gene in
    *                  the current gene set.
    */
   public static void main( String[] args ) {

      // Make sure the filename was passed in
      if ( args.length < 1 ) {
         System.err.println( "Please specify the name of the data file as a program argument" );
         return;
      }
      else {
         JGeneSetFrameApp app = new JGeneSetFrameApp( args[0] );
      }
   } // end main
} // end class
