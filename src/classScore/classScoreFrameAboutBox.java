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

public class classScoreFrameAboutBox
    extends JDialog
    implements ActionListener {

   JPanel panel1 = new JPanel();
   JPanel panel2 = new JPanel();
   JPanel insetsPanel1 = new JPanel();
   JPanel insetsPanel3 = new JPanel();
   JButton button1 = new JButton();
//  JLabel imageLabel = new JLabel();
   JLabel label1 = new JLabel();
   JLabel label2 = new JLabel();
   JLabel label3 = new JLabel();
   JLabel label4 = new JLabel();
//  ImageIcon image1 = new ImageIcon();
   BorderLayout borderLayout2 = new BorderLayout();
   String product = "Class Scoring Software";
   String version = "1.1beta";
   String copyright = "Copyright (c) 2003";
   String comments = "Functional Class Scoring";
   JTextPane jTextPane1 = new JTextPane();
   GridLayout gridLayout1 = new GridLayout();
   JPanel jPanel1 = new JPanel();
   BorderLayout borderLayout1 = new BorderLayout();

   public classScoreFrameAboutBox(Frame parent) {
      super(parent);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try {
         jbInit();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
   private void jbInit() throws Exception {
//    image1 = new ImageIcon(classScoreFrame.class.getResource("about.png"));
//    imageLabel.setIcon(image1);
      this.setTitle("About");
      panel1.setLayout(borderLayout1);
      panel2.setLayout(borderLayout2);
      label1.setText(product);
      label2.setText("1.0.1  Free for academic use only.");
      label3.setText("Copyright (c) 2003 Columbia University");
      label4.setText("Direct questions to Paul Pavlidis: pp175@columbia.edu");
      insetsPanel3.setLayout(gridLayout1);
      insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
      insetsPanel3.setPreferredSize(new Dimension(369, 125));
      insetsPanel3.setToolTipText("");
      button1.setText("Ok");
      button1.addActionListener(this);
//    insetsPanel2.add(imageLabel, null);
      jTextPane1.setBackground(Color.white);
      jTextPane1.setFont(new java.awt.Font("Dialog", 1, 11));
      jTextPane1.setMinimumSize(new Dimension(211, 150));
      jTextPane1.setPreferredSize(new Dimension(211, 150));
      jTextPane1.setDisabledTextColor(Color.black);
      jTextPane1.setEditable(false);
      jTextPane1.setMargin(new Insets(0, 0, 0, 0));
      jTextPane1.setText("If you use this software for your work, please cite Pavlidis, P., " +
                         "Lewis, D.P., and Noble, W.S. (2002) Exploring gene expression data " +
                         "with class scores.Proceedings of the Pacific Symposium on Biocomputing " +
                         "7. pp 474-485");
      jTextPane1.setBounds(new Rectangle(62, 20, 232, 146));
      panel1.setPreferredSize(new Dimension(369, 300));
      panel2.setPreferredSize(new Dimension(369, 100));
      gridLayout1.setRows(5);
      jPanel1.setLayout(null);
      insetsPanel3.add(label1, null);
      insetsPanel3.add(label2, null);
      insetsPanel3.add(label3, null);
      insetsPanel3.add(label4, null);
      panel1.add(jPanel1, BorderLayout.CENTER);
      jPanel1.add(jTextPane1, null);
      panel1.add(insetsPanel1, BorderLayout.SOUTH);
      insetsPanel1.add(button1, null);
      panel1.add(panel2, BorderLayout.NORTH);
      panel2.add(insetsPanel3, BorderLayout.NORTH);
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
