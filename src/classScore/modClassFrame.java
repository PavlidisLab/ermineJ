package classScore;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class modClassFrame
    extends JFrame
{
   JPanel jPanel1;
   JLabel jLabel3 = new JLabel();
   JPanel jPanel4 = new JPanel();

   DefaultListModel listModel;
   JList probeList;


   NewClass newclass;
  JPanel jPanel5 = new JPanel();
  JButton jButton1 = new JButton();
  JButton jButton2 = new JButton();
  JButton jButton3 = new JButton();
  JButton jButton4 = new JButton();

  JRadioButton jRadioButton1 = new JRadioButton();
  JRadioButton jRadioButton2;
  ButtonGroup buttonGroup1 = new ButtonGroup();
  int inputMethod = 0;

  ButtonGroup buttonGroup2 = new ButtonGroup();
  JLabel jLabel4 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JRadioButton jRadioButton3 = new JRadioButton();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JRadioButton jRadioButton4 = new JRadioButton();
  JLabel jLabel6 = new JLabel();
  JLabel jLabel7 = new JLabel();
  JPanel jPanel6 = new JPanel();
  JLabel jLabel8 = new JLabel();
  JButton jButtonGONamesBrowse = new JButton();
  JTextField classFile = new JTextField();
  JPanel jPanel2 = new JPanel();
  JPanel jPanel3 = new JPanel();
  JPanel jPanel7 = new JPanel();
  JPanel jPanel8 = new JPanel();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  GridBagLayout gridBagLayout5 = new GridBagLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  JPanel step2Panel;

  JFileChooser chooser = new JFileChooser();
  File startPath;
  int step=1;

   public modClassFrame(SetupMaps smap) {
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try {
         jbInit();
         //populateList(smap);

      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   public modClassFrame() {
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
      this.setTitle("Add New Class - Step 1 of 3");
      this.getContentPane().setBackground(Color.white);
      setResizable(true);
      jPanel1 = (JPanel)this.getContentPane();
      jPanel1.setBackground(Color.white);
      jPanel1.setAlignmentX((float) 0.5);
      jPanel1.setAlignmentY((float) 0.5);
      jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
      jPanel1.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION);
      jPanel1.setMaximumSize(new Dimension(32767, 32767));
      jPanel1.setMinimumSize(new Dimension(505, 333));
      jPanel1.setPreferredSize(new Dimension(505, 333));
      jPanel1.setLayout(gridBagLayout5);
      jLabel3.setMaximumSize(new Dimension(999, 15));
      jLabel3.setMinimumSize(new Dimension(209, 15));
      jLabel3.setPreferredSize(new Dimension(209, 15));
      jLabel3.setToolTipText("");
      jLabel3.setText("Choose the file type:");
      jPanel4.setBackground(Color.white);
      jPanel4.setForeground(Color.black);
      jPanel4.setBorder(BorderFactory.createEtchedBorder());
      jPanel4.setDebugGraphicsOptions(0);
      jPanel4.setLayout(gridBagLayout1);

      newclass = new NewClass();
      //probeTable.setModel(newclass.toTableModel());

      listModel = new DefaultListModel();
      probeList = new JList(listModel);
      probeList.setAutoscrolls(true);


      jButton1.setText("Next >");
      jButton1.addActionListener(new modClassFrame_jButton1_actionAdapter(this));
      jButton2.setText("< Back");
      jButton2.addActionListener(new modClassFrame_jButton2_actionAdapter(this));
      jButton3.setText("Cancel");
      jButton3.addActionListener(new modClassFrame_jButton3_actionAdapter(this));
      jButton4.setAlignmentY((float) 0.5);
      jButton4.setText("Finish");
      jButton4.addActionListener(new modClassFrame_jButton4_actionAdapter(this));

      jPanel3.setBackground(Color.white);
      jPanel3.setDebugGraphicsOptions(0);
      jPanel3.setLayout(gridBagLayout3);
      jLabel8.setMaximumSize(new Dimension(999, 15));
      jLabel8.setMinimumSize(new Dimension(259, 15));
      jLabel8.setPreferredSize(new Dimension(259, 15));
      jPanel7.setBackground(Color.white);
      jPanel7.setLayout(gridBagLayout4);
      jPanel5.setBackground(Color.white);
      jPanel6.setBorder(null);
      jPanel8.setBackground(Color.white);
      jPanel8.setBorder(null);
      jPanel8.setDebugGraphicsOptions(0);
      jLabel5.setBorder(null);
      jLabel4.setBorder(null);

      jRadioButton1.setBackground(Color.white);
      jRadioButton1.setBorder(BorderFactory.createLineBorder(Color.black));
      jRadioButton1.setText("File");
      jRadioButton1.addActionListener(new modClassFrame_jRadioButton1_actionAdapter(this));
      buttonGroup1.add(jRadioButton1);

      jRadioButton2 = new JRadioButton("Manual",true);
      jRadioButton2.setBackground(Color.white);
      jRadioButton2.setMaximumSize(new Dimension(91, 23));
      jRadioButton2.addActionListener(new modClassFrame_jRadioButton2_actionAdapter(this));
      jRadioButton2.setBorder(BorderFactory.createLineBorder(Color.black));
      buttonGroup1.add(jRadioButton2);

      jLabel4.setText("- File with gene symbols or probe ids");
      jLabel5.setText("- Enter using lists");

      jRadioButton3.setText("Gene symbols");
      jRadioButton3.setBackground(Color.white);
      jRadioButton3.setEnabled(false);
      jRadioButton4.setText("Probe IDs");
      jRadioButton4.setMaximumSize(new Dimension(91, 23));
      jRadioButton4.setBackground(Color.white);
      jRadioButton4.setEnabled(false);
      buttonGroup2.add(jRadioButton3);
      buttonGroup2.add(jRadioButton4);

      jLabel6.setText("- e.g. 36735_f_at");
      jLabel7.setText("- e.g. KIR3DL2");
      jPanel6.setLayout(gridBagLayout2);
      jPanel6.setDebugGraphicsOptions(0);
      jPanel6.setForeground(Color.black);
      jPanel6.setBackground(Color.white);
      jLabel8.setText("Choose the method of data entry:");

      jButtonGONamesBrowse.setText("Browse....");
      jButtonGONamesBrowse.addActionListener(new modClassFrame_jButtonGONamesBrowse_actionAdapter(this));
      jPanel2.add(jButtonGONamesBrowse, null);

      classFile.setEditable(false);
      classFile.setMinimumSize(new Dimension(4, 19));
      classFile.setPreferredSize(new Dimension(280, 19));
      classFile.setToolTipText("File containing class members");
      classFile.setText("File containing class members");

      jPanel2.setBackground(Color.white);
      jPanel5.add(jButton3, null);
      jPanel5.add(jButton2, null);
      jPanel5.add(jButton1, null);
      jPanel5.add(jButton4, null);
      jPanel4.add(jLabel5,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 16, 8, 10), 125, 10));
      jPanel4.add(jLabel4,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 16, 0, 10), 30, 10));
      jPanel4.add(jRadioButton2,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 9, 8, 0), 8, 12));
      jPanel4.add(jRadioButton1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 9, 0, 0), 26, 12));
      jPanel1.add(jPanel3,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 9, 0, 96), 0, 0));
      jPanel7.add(jLabel8,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 21, 0, 74), 0, 0));
      jPanel7.add(jPanel4,       new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 10, 12, 36), -1, 8));
      jPanel6.add(jLabel6,   new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 23, 0, 9), 15, 12));
      jPanel6.add(jLabel7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                                  ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 23, 0, 9), 32, 12));
      jPanel6.add(jRadioButton4,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 25, 6));
      jPanel6.add(jRadioButton3,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 6));
      jPanel3.add(jPanel2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                  ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 7, 7), 6, -1));
      jPanel3.add(jPanel8, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                  ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 3, 0, 155), -216, 21));
      jPanel8.add(jLabel3, null);
      jPanel8.add(jPanel6, null);
      jPanel2.add(classFile, null);
      jPanel1.add(jPanel7,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 0, 141), 0, -4));
      jPanel1.add(jPanel5,   new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 107, 13, 110), 0, 0));

      startPath = new File(System.getProperty("user.home"));
      chooser.setCurrentDirectory(startPath);
   }

private void steptwo(int inputMethod)
{
   step=2;
   this.getContentPane().remove(jPanel3);
   this.getContentPane().remove(jPanel7);
   this.setTitle("Define New Class - Step 2 of 3");
   setResizable(true);


}

 private void populateList(SetupMaps smap)
 {
    System.err.println("populating list");
    Vector probe_list=new Vector(smap.geneData.getProbeGroupMap().keySet());
    Collections.sort(probe_list);
    Iterator probes_it = probe_list.iterator();
    while (probes_it.hasNext())
    {
       listModel.addElement((String) probes_it.next());
    }
    probeList.repaint();
 }


  void jButton1_actionPerformed(ActionEvent e) {
     //System.err.println(probeList.getSelectedValues());
     //newclass.probes.addAll(Arrays.asList(probeList.getSelectedValues()));
     if(step==1)
        steptwo(inputMethod);
  }

  void jButton2_actionPerformed(ActionEvent e) {
     if(step==2)
     {
        step=1;
        this.getContentPane().add(jPanel3);
        this.getContentPane().add(jPanel7);
        this.setTitle("Define New Class - Step 1 of 3");
        this.getContentPane().add(jPanel3, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 9, 0, 96), 0, 0));
        this.getContentPane().add(jPanel7, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 0, 141), 0, -4));
        this.getContentPane().repaint();
     }
  }

  void jButton3_actionPerformed(ActionEvent e) {

  }

  void jRadioButton2_actionPerformed(ActionEvent e) {
     classFile.setEditable(false);
     jRadioButton3.setEnabled(false);
     jRadioButton4.setEnabled(false);
     classFile.setEnabled(false);
     inputMethod=0;
  }

  void jRadioButton1_actionPerformed(ActionEvent e) {
     classFile.setEditable(true);
     jRadioButton3.setEnabled(true);
     jRadioButton4.setEnabled(true);
     classFile.setEnabled(true);
     inputMethod=1;
  }

  public void browse(JTextField target) {
     int result = chooser.showOpenDialog(this);
     if (result == JFileChooser.APPROVE_OPTION) {
        target.setText(chooser.getSelectedFile().toString());
     }
  }

  void jButtonGONamesBrowse_actionPerformed(ActionEvent e) {
     browse(classFile);
  }


  void jButton4_actionPerformed(ActionEvent e) {

  }

}

class NewClass
{
   String id;
   String name;
   ArrayList probes;
   public void newClass()
   {
      id = new String();
      name = new String();
      probes = new ArrayList();
   }

   public TableModel toTableModel()
   {
      return new AbstractTableModel()
      {
         public int getRowCount() {
            System.err.println(probes.size());
            return probes.size();
         }

         public int getColumnCount() {
            return 1;
         }

         public Object getValueAt(int r, int c) {
            return probes.get(r);
         }
      };
   }
}

class modClassFrame_jButton3_actionAdapter implements java.awt.event.ActionListener {
  modClassFrame adaptee;

  modClassFrame_jButton3_actionAdapter(modClassFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButton3_actionPerformed(e);
  }
}

class modClassFrame_jRadioButton2_actionAdapter implements java.awt.event.ActionListener {
  modClassFrame adaptee;

  modClassFrame_jRadioButton2_actionAdapter(modClassFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jRadioButton2_actionPerformed(e);
  }
}

class modClassFrame_jRadioButton1_actionAdapter implements java.awt.event.ActionListener {
  modClassFrame adaptee;

  modClassFrame_jRadioButton1_actionAdapter(modClassFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jRadioButton1_actionPerformed(e);
  }
}

class modClassFrame_jButtonGONamesBrowse_actionAdapter implements java.awt.event.ActionListener {
  modClassFrame adaptee;

  modClassFrame_jButtonGONamesBrowse_actionAdapter(modClassFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonGONamesBrowse_actionPerformed(e);
  }
}

class modClassFrame_jButton1_actionAdapter implements java.awt.event.ActionListener {
  modClassFrame adaptee;

  modClassFrame_jButton1_actionAdapter(modClassFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButton1_actionPerformed(e);
  }
}

class modClassFrame_jButton2_actionAdapter implements java.awt.event.ActionListener {
  modClassFrame adaptee;

  modClassFrame_jButton2_actionAdapter(modClassFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButton2_actionPerformed(e);
  }
}

class modClassFrame_jButton4_actionAdapter implements java.awt.event.ActionListener {
  modClassFrame adaptee;

  modClassFrame_jButton4_actionAdapter(modClassFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButton4_actionPerformed(e);
  }
}

