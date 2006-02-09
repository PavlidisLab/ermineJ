/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScoreTest.gui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import classScore.gui.AboutBox;

/**
 * @author pavlidis
 * @version $Id$
 */
public class AboutBoxTestApp {

    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            AboutBox ab = new AboutBox( null );
            ab.setVisible( true );
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

    }

}
