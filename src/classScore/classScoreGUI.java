package classScore;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.UIManager;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo Allow the user to input the data
 */

public class classScoreGUI {
   boolean packFrame = false;

   public classScoreGUI() {
      //mainFrame frame = new mainFrame();
      classScoreFrame frame = new classScoreFrame();

      //Validate frames that have preset sizes
      //Pack frames that have useful preferred size info, e.g. from their layout
      if (packFrame) {
         frame.pack();
      } else {
         frame.validate();
      }
      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = frame.getSize();
      if (frameSize.height > screenSize.height) {
         frameSize.height = screenSize.height;
      }
      if (frameSize.width > screenSize.width) {
         frameSize.width = screenSize.width;
      }
      frame.setLocation((screenSize.width - frameSize.width) / 2,
                        (screenSize.height - frameSize.height) / 2);
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         e.printStackTrace();
      }
      classScoreGUI classScoreGUI1 = new classScoreGUI();
   }

}
