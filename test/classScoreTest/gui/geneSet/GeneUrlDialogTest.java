package classScoreTest.gui.geneSet;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import classScore.gui.geneSet.GeneUrlDialog;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneUrlDialogTest {

    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
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
        JFrame foo = new JFrame();

        GeneUrlDialog app = new GeneUrlDialog(foo, null, null);
  
       // app.show();
    }

}
