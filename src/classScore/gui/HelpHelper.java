package classScore.gui;

import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractButton;

/**
 * Makes it easier to add help access wherever we want To use this, you can do the following, for example for a menu
 * item.
 * 
 * <pre>
 * HelpHelper hh = new HelpHelper();
 * hh.initHelp( helpMenuItem );
 * </pre>
 * 
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class HelpHelper {

   // JavaHelp
   private HelpBroker m_helpBroker = null;
   private final boolean ENABLE_HELP = true; // when help is fully implemented, delete this line

   /**
    * Initializes JavaHelp by creating HelpSet and HelpBroker objects and attaching an action listener an AbstractButton
    * 
    * @param c an AbstractButton (typically a JButton or JMenuItem) which will respond to help requests.
    * @return true if successful
    */
   public boolean initHelp( AbstractButton c ) {

      // Create HelpSet and HelpBroker objects
      HelpSet hs = ENABLE_HELP ? getHelpSet( "main.hs" ) : null;
      if ( hs != null ) {
         m_helpBroker = hs.createHelpBroker();
         // Assign help to components
         CSH.setHelpIDString( c, "top" );
         c.addActionListener( new CSH.DisplayHelpFromSource( m_helpBroker ) );

         // Handle events
         return true;
      }
      return false;

   }

   /**
    * Finds the helpset file and creates a HelpSet object.
    * 
    * @param helpsetFilename filename of the *.hs file relative to the classpath
    * @return the help set object created from the file; if the file was not loaded for whatever reason, returns null.
    */
   private HelpSet getHelpSet( String helpsetFilename ) {
      HelpSet hs = null;
      ClassLoader cl = this.getClass().getClassLoader();
      try {
         URL hsURL = HelpSet.findHelpSet( cl, helpsetFilename );
         hs = new HelpSet( null, hsURL );
      } catch ( Exception e ) {
         System.err.println( "HelpSet: " + e.getMessage() );
         System.err.println( "HelpSet: " + helpsetFilename + " not found" );
      }
      return hs;
   }

}