package classScore.gui;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public class HelpFrame extends JFrame {

   /**
    * @param callingframe
    */
   
   private JEditorPane helpPane;
   
   public HelpFrame( GeneSetScoreFrame callingframe ) {
      // TODO Auto-generated constructor stub
      init();
   }

   private void init() {
      helpPane = new JEditorPane();
   }
   
}
