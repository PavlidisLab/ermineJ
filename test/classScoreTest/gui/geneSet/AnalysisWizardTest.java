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
package classScoreTest.gui.geneSet;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import classScore.gui.AnalysisWizard;
import classScore.gui.GeneSetScoreFrame;

/**
 * Test the analysis wizard.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class AnalysisWizardTest {

    private static Log log = LogFactory.getLog( AnalysisWizardTest.class.getName() );

    /**
     * @param args
     */
    public static void main( String[] args ) {

        GeneSetScoreFrame gssf;
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            gssf = new GeneSetScoreFrame();
            gssf.setSize( new Dimension( 500, 500 ) );
            AnalysisWizard aw = new AnalysisWizard( gssf, null, null );
            aw.setSize( new Dimension( 500, 500 ) );
            log.info( "Created wizard" );
            aw.setVisible( true );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        } catch ( UnsupportedLookAndFeelException e ) {
            e.printStackTrace();
        }

    }

}
