package test.classScoreTest.gui.geneSet;

import classScore.gui.geneSet.JDetailsFrame;
import java.util.ArrayList;
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

public class JDetailsFrameApp {

   /**
    * @param filename the raw data file which contains the data for the probe ID's
    */
   public JDetailsFrameApp( String filename ) {

      Settings settings = new Settings();
      settings.setRawFile( filename );

      final String[] PROBES = {
          "probe1" //,"probe2"
      };

      ArrayList probeIDs = new ArrayList();
      for ( int i = 0; i < PROBES.length; i++ ) {
         probeIDs.add( i, PROBES[i] );
      }

      JDetailsFrame frame = new JDetailsFrame( probeIDs, null, null, settings );
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
         JDetailsFrameApp app = new JDetailsFrameApp( args[0] );
      }
   } // end main
} // end class
