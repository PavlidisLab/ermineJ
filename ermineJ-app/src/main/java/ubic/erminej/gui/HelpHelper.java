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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import ubic.basecode.util.BrowserLauncher;
import ubic.erminej.Settings;
import ubic.erminej.gui.util.GuiUtil;

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

    /**
     * @param c an AbstractButton (typically a JButton or JMenuItem) which will respond to help requests.
     * @return true if successful
     */
    public boolean initHelp( AbstractButton c ) {

        c.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    BrowserLauncher.openURL( Settings.HELPURL );
                } catch ( Exception e1 ) {
                    GuiUtil.error( "Could not open a web browser. For help visit " + Settings.HELPURL );
                }
            }
        } );

        return true;
    }

}
