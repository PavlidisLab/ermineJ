package classScore;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.UIManager;

import classScore.gui.GeneSetScoreFrame;
import classScore.gui.StartupDialog;

/**
 * Main for GUI
 * <p>
 * Copyright (c) 2003 Columbia University
 * </p>
 *
 * @author Paul Pavlidis
 * @version $Id$
 */

public class classScoreGUI {
   boolean packFrame = false;

   public classScoreGUI() {
      //mainFrame frame = new mainFrame();
      GeneSetScoreFrame frame = new GeneSetScoreFrame();

      //Validate frames that have preset sizes
      //Pack frames that have useful preferred size info, e.g. from their
      // layout
      if ( packFrame ) {
         frame.pack();
      } else {
         frame.validate();
      }
      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = frame.getSize();
      if ( frameSize.height > screenSize.height ) {
         frameSize.height = screenSize.height;
      }
      if ( frameSize.width > screenSize.width ) {
         frameSize.width = screenSize.width;
      }
      frame.setLocation( ( screenSize.width - frameSize.width ) / 2,
            ( screenSize.height - frameSize.height ) / 2 );
      frame.disableMenusForLoad();
      frame.setVisible( true );

      frame.showStatus("Waiting for startup dialog box");
      StartupDialog sdlog = new StartupDialog( frame );
      sdlog.showDialog();
      frame.showStatus("Starting up...");
   }

   public static void main( String[] args ) {
      try {
         UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      } catch ( Exception e ) {
         e.printStackTrace();
      }
      new classScoreGUI();
   }

}
