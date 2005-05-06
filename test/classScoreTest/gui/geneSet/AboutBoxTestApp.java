package classScoreTest.gui.geneSet;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import classScore.gui.AboutBox;
 
/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004-2005 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public class AboutBoxTestApp {

    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            AboutBox ab = new AboutBox(null);
            ab.show();
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
