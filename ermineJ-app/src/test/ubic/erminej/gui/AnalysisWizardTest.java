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

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.analysis.AnalysisWizard;

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

        MainFrame gssf;
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            gssf = new MainFrame();
            gssf.setSize( new Dimension( 500, 500 ) );
            AnalysisWizard aw = new AnalysisWizard( gssf, null );
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
