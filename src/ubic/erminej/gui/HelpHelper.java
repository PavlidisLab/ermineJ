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
 * @author Paul Pavlidis
 * @version $Id$
 */
public class HelpHelper {

    // JavaHelp
    private HelpBroker m_helpBroker = null;

    /**
     * Initializes JavaHelp by creating HelpSet and HelpBroker objects and attaching an action listener an
     * AbstractButton
     * 
     * @param c an AbstractButton (typically a JButton or JMenuItem) which will respond to help requests.
     * @return true if successful
     */
    public boolean initHelp( AbstractButton c ) {

        // Create HelpSet and HelpBroker objects
        HelpSet hs = getHelpSet( "classScore/main.hs" );
        if ( hs != null ) {
            m_helpBroker = hs.createHelpBroker();
            // Assign help to components
            CSH.setHelpIDString( c, "top" );
            c.addActionListener( new CSH.DisplayHelpFromSource( m_helpBroker ) );
            return true;
        }
        // GuiUtil.error( "Couldn't load help" );
        System.err.println( "Couldn't load help" );
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
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            // URL hsURL = HelpSet.findHelpSet( cl, helpsetFilename );
            URL hsURL = cl.getResource( helpsetFilename );
            hs = new HelpSet( cl, hsURL );
        } catch ( Exception e ) {
            System.err.println( "HelpSet: " + e.getMessage() );
            System.err.println( "HelpSet: " + helpsetFilename + " not found" );
            e.printStackTrace();
        }
        return hs;
    }

}
