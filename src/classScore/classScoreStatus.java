package classScore;

import java.awt.EventQueue;
import javax.swing.JLabel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class classScoreStatus {
  private JLabel l;

  public classScoreStatus(JLabel l) {
    this.l = l;
  }

  public void setStatus(String s) {
    final String message = s;

    try {
      Thread.sleep(100);
    }
    catch (InterruptedException ex) {
    }

    EventQueue.invokeLater(new Runnable()
    {
      public void run() {
        if (l != null) {
          l.setText(message);
        }
      }
    });
  }
}