package classScore;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ErrorFrame
    extends JDialog
    implements ActionListener {

   JPanel panel1 = new JPanel();
   JPanel insetsPanel1 = new JPanel();
   JButton button1 = new JButton();
   JLabel label1 = new JLabel();
  // String error = "Class Scoring Software";
   BorderLayout borderLayout1 = new BorderLayout();

   public ErrorFrame(Frame parent,String error) {
      super(parent);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try {
         jbInit(error);
//jbInit();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
//   private void jbInit() throws Exception {
   private void jbInit(String error) throws Exception {
      this.setTitle("Error");
      panel1.setLayout(borderLayout1);
      label1.setFont(new java.awt.Font("MS Sans Serif", 0, 11));
      label1.setForeground(Color.black);
      label1.setAlignmentX((float) 0.5);
      label1.setHorizontalAlignment(SwingConstants.CENTER);
      label1.setText(error);
      button1.setText("Ok");
      button1.addActionListener(this);
//    insetsPanel2.add(imageLabel, null);
      panel1.setPreferredSize(new Dimension(250, 200));
      panel1.add(insetsPanel1, BorderLayout.SOUTH);
      insetsPanel1.add(button1, null);
      panel1.add(label1, BorderLayout.CENTER);
      this.getContentPane().add(panel1, BorderLayout.CENTER);
      setResizable(true);
   }

   //Overridden so we can exit when window is closed
   protected void processWindowEvent(WindowEvent e) {
      if (e.getID() == WindowEvent.WINDOW_CLOSING) {
         cancel();
      }
      super.processWindowEvent(e);
   }

   //Close the dialog
   void cancel() {
      dispose();
   }

   //Close the dialog on a button event
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == button1) {
         cancel();
      }
   }
}
