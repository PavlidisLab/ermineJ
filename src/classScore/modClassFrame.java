package classScore;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
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
    extends JFrame {
   JPanel jPanel1;

   //holds bottom buttons
   JPanel jPanel5 = new JPanel();
   JButton nextButton = new JButton();
   JButton backButton = new JButton();
   JButton cancelButton = new JButton();
   JButton finishButton = new JButton();

   //panels for step 1
   //step 1 top
   JPanel jPanel7 = new JPanel(); //outer method choice
   GridBagLayout gridBagLayout4 = new GridBagLayout();
   JLabel jLabel8 = new JLabel(); // 'choose method'
   JPanel jPanel4 = new JPanel(); // holds radio buttons
   GridBagLayout gridBagLayout1 = new GridBagLayout();
   JLabel jLabel4 = new JLabel();
   JLabel jLabel5 = new JLabel();
   ButtonGroup buttonGroup1 = new ButtonGroup();
   JRadioButton jRadioButton1 = new JRadioButton();
   JRadioButton jRadioButton2;
   //step 1 bottom
   JPanel jPanel3 = new JPanel(); //outer file details
   GridBagLayout gridBagLayout3 = new GridBagLayout();
   JPanel jPanel2 = new JPanel(); // holds file chooser
   JButton browseButton = new JButton();
   JTextField classFile = new JTextField();
   JFileChooser chooser = new JFileChooser();
   File startPath;
   JPanel jPanel8 = new JPanel(); // holds file type stuff
   JLabel jLabel3 = new JLabel(); //  'choose file type'
   JPanel jPanel6 = new JPanel(); //  holds radio buttons
   GridBagLayout gridBagLayout2 = new GridBagLayout();
   ButtonGroup buttonGroup2 = new ButtonGroup();
   JRadioButton jRadioButton3 = new JRadioButton();
   JRadioButton jRadioButton4 = new JRadioButton();
   JLabel jLabel6 = new JLabel();
   JLabel jLabel7 = new JLabel();

   //panels for step two
   JPanel step2topPanel;
   JScrollPane probeScrollPane;
   JList probeList;
   DefaultListModel listModel;
   JTable probeTable;
   JScrollPane newClassScrollPane;
   JTable newClassTable;
   SortFilterModel ncsorter;
   AbstractTableModel ncTableModel;

   //logic
   int inputMethod = 0;
   int step = 1;

   NewClass newclass;
   JPanel jPanel9 = new JPanel();
   JButton jButton1 = new JButton();
   JButton jButton2 = new JButton();
   GridBagLayout gridBagLayout5 = new GridBagLayout();
   SetupMaps smap;

   public modClassFrame(SetupMaps smap) {
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.smap=smap;
      try {
         jbInit();
         populateList();
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
      this.setTitle("Define New Class - Step 1 of 3");
      this.getContentPane().setBackground(Color.white);
      setResizable(true);
      jPanel1 = (JPanel)this.getContentPane();
      jPanel1.setBackground(SystemColor.control);
      jPanel1.setAlignmentX( (float) 0.5);
      jPanel1.setAlignmentY( (float) 0.5);
      //jPanel1.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION);
      jPanel1.setMaximumSize(new Dimension(32767, 32767));
      jPanel1.setMinimumSize(new Dimension(505, 333));
      jPanel1.setPreferredSize(new Dimension(505, 333));
      jPanel1.setLayout(gridBagLayout5);

      //step 1 top
      jPanel7.setBackground(SystemColor.control);
      jPanel7.setLayout(gridBagLayout4);
      jLabel8.setText("Choose the method of data entry:");
      jLabel8.setMaximumSize(new Dimension(999, 15));
      jLabel8.setMinimumSize(new Dimension(259, 15));
      jLabel8.setPreferredSize(new Dimension(259, 15));
      jPanel4.setBackground(SystemColor.control);
      jPanel4.setForeground(Color.black);
      jPanel4.setBorder(BorderFactory.createEtchedBorder());
      //jPanel4.setDebugGraphicsOptions(0);
      jPanel4.setLayout(gridBagLayout1);
      jRadioButton1.setBackground(SystemColor.control);
      jRadioButton1.setBorder(BorderFactory.createLineBorder(Color.black));
      jRadioButton1.setText("File");
      jRadioButton1.addActionListener(new modClassFrame_jRadioButton1_actionAdapter(this));
      buttonGroup1.add(jRadioButton1);
      jRadioButton2 = new JRadioButton("Manual", true);
      jRadioButton2.setBackground(SystemColor.control);
      jRadioButton2.setMaximumSize(new Dimension(91, 23));
      jRadioButton2.addActionListener(new modClassFrame_jRadioButton2_actionAdapter(this));
      jRadioButton2.setBorder(BorderFactory.createLineBorder(Color.black));
      buttonGroup1.add(jRadioButton2);
      jLabel4.setBorder(null);
      jLabel4.setText("- File with gene symbols or probe ids");
      jLabel5.setBorder(null);
      jLabel5.setText("- Enter using lists");
      jPanel4.add(jLabel5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE, new Insets(0, 16, 8, 10),
                                                  125, 10));
      jPanel4.add(jLabel4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE, new Insets(3, 16, 0, 10),
                                                  30, 10));
      jPanel4.add(jRadioButton2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 9, 8, 0), 8, 12));
      jPanel4.add(jRadioButton1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 9, 0, 0), 26, 12));
      jPanel7.add(jLabel8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE, new Insets(6, 21, 0, 74),
                                                  0, 0));
      jPanel7.add(jPanel4, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                  , GridBagConstraints.CENTER,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(6, 10, 12, 36), -1, 8));

      /////////////////////////
      //step 1 bottom
      jPanel3.setBackground(SystemColor.control);
      //jPanel3.setDebugGraphicsOptions(0);
      jPanel3.setLayout(gridBagLayout3);
      //file chooser stuff
      jPanel2.setBackground(SystemColor.control);
      browseButton.setText("Browse....");
      browseButton.addActionListener(new browseButton_actionAdapter(this));
      browseButton.setEnabled(false);
      jPanel2.add(browseButton, null);
      classFile.setEditable(false);
      classFile.setMinimumSize(new Dimension(4, 19));
      classFile.setPreferredSize(new Dimension(280, 19));
      classFile.setToolTipText("File containing class members");
      classFile.setText("File containing class members");
      startPath = new File(System.getProperty("user.home"));
      chooser.setCurrentDirectory(startPath);
      jPanel2.add(classFile, null);
      jPanel3.add(jPanel2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                  , GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH, new Insets(0, 3, 7, 7),
                                                  6, -1));
      //file type stuff
      jPanel8.setBackground(SystemColor.control);
      //jPanel8.setDebugGraphicsOptions(0);
      jPanel8.setBorder(null);
      jPanel6.setBorder(null);
      jLabel3.setMaximumSize(new Dimension(999, 15));
      jLabel3.setMinimumSize(new Dimension(209, 15));
      jLabel3.setPreferredSize(new Dimension(209, 15));
      jLabel3.setToolTipText("");
      jLabel3.setText("Choose the file type:");
      jPanel8.add(jLabel3, null);
      jPanel6.setLayout(gridBagLayout2);
      //jPanel6.setDebugGraphicsOptions(0);
      jPanel6.setForeground(Color.black);
      jPanel6.setBackground(SystemColor.control);
      jLabel6.setText("- e.g. 36735_f_at");
      jLabel7.setText("- e.g. KIR3DL2");
      jRadioButton3.setText("Gene symbols");
      jRadioButton3.setBackground(SystemColor.control);
      jRadioButton3.setEnabled(false);
      jRadioButton4.setText("Probe IDs");
      jRadioButton4.setMaximumSize(new Dimension(91, 23));
      jRadioButton4.setBackground(SystemColor.control);
      jRadioButton4.setEnabled(false);
      buttonGroup2.add(jRadioButton3);
      buttonGroup2.add(jRadioButton4);
      jPanel6.add(jLabel6, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE, new Insets(0, 23, 0, 9),
                                                  15, 12));
      jPanel6.add(jLabel7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE, new Insets(2, 23, 0, 9),
                                                  32, 12));
      jPanel6.add(jRadioButton4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 25, 6));
      jPanel6.add(jRadioButton3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 6));
      jPanel8.add(jPanel6, null);
      jPanel3.add(jPanel8, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                  , GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH, new Insets(1, 3, 0, 155),
                                                  -216, 21));
      /////////////////////////
      //bottom buttons
      jPanel5.setBackground(SystemColor.control);
      nextButton.setText("Next >");
      nextButton.addActionListener(new nextButton_actionAdapter(this));
      backButton.setText("< Back");
      backButton.addActionListener(new backButton_actionAdapter(this));
      backButton.setEnabled(false);
      cancelButton.setText("Cancel");
      cancelButton.addActionListener(new cancelButton_actionAdapter(this));
      finishButton.setAlignmentY( (float) 0.5);
      finishButton.setText("Finish");
      finishButton.addActionListener(new finishButton_actionAdapter(this));
      jPanel5.add(cancelButton, null);
      jPanel5.add(backButton, null);
      jPanel5.add(nextButton, null);
      jPanel5.add(finishButton, null);
//      jPanel1.add(jPanel3,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
//          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 9, 0, 96), 0, 0));
//      jPanel1.add(jPanel7,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
//          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 0, 141), 0, -4));
//      jPanel1.add(jPanel5,   new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
//          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 107, 13, 110), 0, 0));

      //step 2
      step2topPanel = new JPanel();
      step2topPanel.setBorder(BorderFactory.createEtchedBorder());
      listModel = new DefaultListModel();
      probeList = new JList(listModel);
      probeList.setAutoscrolls(true);
      //probeScrollPane=new JScrollPane(probeList);
      //probeTable.setModel(newclass.toTableModel());

      probeTable = new JTable();
      //probeTable.setMaximumSize(new Dimension(32767, 32767));
      //probeTable.setMinimumSize(new Dimension(200, 150));
      //probeTable.setPreferredSize(new Dimension(200, 150));
      probeTable.setPreferredScrollableViewportSize(new Dimension(200, 150));
      probeScrollPane = new JScrollPane(probeTable);
      probeScrollPane.setMaximumSize(new Dimension(32767, 32767));
      probeScrollPane.setPreferredSize(new Dimension(200, 150));

      newClassTable = new JTable();
      newClassTable.setPreferredScrollableViewportSize(new Dimension(200, 150));
      newClassScrollPane = new JScrollPane(newClassTable);
      newClassScrollPane.setMaximumSize(new Dimension(32767, 32767));
      newClassScrollPane.setPreferredSize(new Dimension(200, 150));

      jPanel9.setMinimumSize(new Dimension(1, 1));
      jPanel9.setPreferredSize(new Dimension(200, 30));
      jButton1.setSelected(false);
      jButton1.setText("Add >");
      jButton1.addActionListener(new modClassFrame_jButton1_actionAdapter(this));
      jButton2.setSelected(false);
      jButton2.setText("Delete");
      jButton2.addActionListener(new modClassFrame_jButton2_actionAdapter(this));
      jPanel9.add(jButton1, null);
      jPanel9.add(jButton2, null);

      step2topPanel.add(probeScrollPane, null);
      step2topPanel.add(newClassScrollPane, null);
      step2topPanel.add(jPanel9, null);
      //step2topPanel.add(probeTable);

      jPanel1.add(jPanel5, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                  , GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                                                  219, 0));
      jPanel1.add(step2topPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), -118, 140));
      //////////

      newclass = new NewClass(this);
   }

   void errorPopUp(String msg)
   {
      ErrorFrame ef = new ErrorFrame(this,msg);
      Dimension efSize = ef.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      ef.setLocation( (frmSize.width - efSize.width) / 2 + loc.x,
                       (frmSize.height - efSize.height) / 2 + loc.y);
      ef.setModal(true);
      ef.pack();
      ef.show();
   }

   void jRadioButton2_actionPerformed(ActionEvent e) {
      classFile.setEditable(false);
      jRadioButton3.setEnabled(false);
      jRadioButton4.setEnabled(false);
      classFile.setEnabled(false);
      browseButton.setEnabled(false);
      inputMethod = 0;
   }

   void jRadioButton1_actionPerformed(ActionEvent e) {
      classFile.setEditable(true);
      jRadioButton3.setEnabled(true);
      jRadioButton4.setEnabled(true);
      classFile.setEnabled(true);
      browseButton.setEnabled(true);
      inputMethod = 1;
   }

   public void browse(JTextField target) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         target.setText(chooser.getSelectedFile().toString());
      }
   }

   void browseButton_actionPerformed(ActionEvent e) {
      browse(classFile);
   }

   private void steptwo(int inputMethod) {
      step = 2;
      this.getContentPane().remove(jPanel7);
      this.getContentPane().remove(jPanel3);
      this.setTitle("Define New Class - Step 2 of 3");
      backButton.setEnabled(true);
      if (inputMethod == 0) {
         this.getContentPane().add(step2topPanel);
         System.err.println("added probe list");
      }
      this.repaint();
   }

   private void populateList() {
      System.err.println("populating list");
      //Vector probe_list=new Vector(smap.geneData.getProbeGroupMap().keySet());
      //Collections.sort(probe_list);
      //Iterator probes_it = probe_list.iterator();
      //while (probes_it.hasNext())
      //{
      //  listModel.addElement((String) probes_it.next());
      //}
      //probeList.repaint();

      SortFilterModel sorter = new SortFilterModel(smap.toSelectTableModel());
      probeTable.setModel(sorter);
      probeTable.getTableHeader().addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent event) {
            int tableColumn = probeTable.columnAtPoint(event.getPoint());
            int modelColumn = probeTable.convertColumnIndexToModel(tableColumn);
            ( (SortFilterModel) probeTable.getModel()).sort(modelColumn);
         }
      });
      probeTable.getColumnModel().getColumn(0).setPreferredWidth(40);

      ncTableModel = newclass.toTableModel();
      newClassTable.setModel(ncTableModel);
      JTextField editProbe = new JTextField();
      editProbe.setBorder(BorderFactory.createEmptyBorder());
      DefaultCellEditor editorProbe = new DefaultCellEditor(editProbe);
      editorProbe.addCellEditorListener(new editorProbeAdaptor(this));
      newClassTable.getColumnModel().getColumn(0).setCellEditor(editorProbe);
      JTextField editGene = new JTextField();
      editGene.setBorder(BorderFactory.createEmptyBorder());
      DefaultCellEditor editorGene = new DefaultCellEditor(editGene);
      editorGene.addCellEditorListener(new editorGeneAdaptor(this));
      newClassTable.getColumnModel().getColumn(1).setCellEditor(editorGene);

      /*
           ncsorter = new SortFilterModel(ncTableModel);
           newClassTable.setModel(ncsorter);

           newClassTable.getTableHeader().addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent event) {
                 int tableColumn = newClassTable.columnAtPoint(event.getPoint());
                 int modelColumn = newClassTable.convertColumnIndexToModel(tableColumn);
                 ( (SortFilterModel) newClassTable.getModel()).sort(modelColumn);
              }
           });
       */
      newClassTable.getColumnModel().getColumn(0).setPreferredWidth(40);

   }

   void jButton2_actionPerformed(ActionEvent e) {

   }

   void jButton1_actionPerformed(ActionEvent e) {
      int n = probeTable.getSelectedRowCount();
      int[] rows = probeTable.getSelectedRows();
      for (int i = 0; i < n; i++) {
         newclass.probes.add(probeTable.getValueAt(rows[i], 0));
      }
      int s = newclass.probes.size();
      System.err.println("originially: " + (s - 1) + " now: " + (s + n - 1));
      ncTableModel.fireTableDataChanged();
   }

   void editorProbe_actionPerformed(ChangeEvent e)
   {
      String newProbe = (String)((DefaultCellEditor) e.getSource()).getCellEditorValue();
      System.err.println(newProbe);
      if(smap.geneData.getProbeGeneName(newProbe) != null)
      {
         newclass.probes.add(newProbe);
         int s = newclass.probes.size();
         System.err.println("originially: " + (s - 1) + " now: " + s);
         ncTableModel.fireTableDataChanged();
      }
      else
         errorPopUp("Probe " + newProbe + " does not exist.");
   }

   void editorGene_actionPerformed(ChangeEvent e)
   {
      String newGene = (String)((DefaultCellEditor) e.getSource()).getCellEditorValue();
      System.err.println(newGene);
      if(smap.geneData.getProbeGeneName(newGene) != null)
      {
         //newclass.probes.add(newGene);
         //int s = newclass.probes.size();
         //System.err.println("originially: " + (s - 1) + " now: " + s);
         //ncTableModel.fireTableDataChanged();
      }
      else
         errorPopUp("Gene " + newGene + " does not exist.");
   }


   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
         steptwo(inputMethod);
      }
   }

   void backButton_actionPerformed(ActionEvent e) {
      if (step == 2) {
         step = 1;
         this.setTitle("Define New Class - Step 1 of 3");
         this.getContentPane().add(jPanel7);
         this.getContentPane().add(jPanel3);
         backButton.setEnabled(false);
         this.repaint();
      }
   }

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {
      dispose();
   }

}

class NewClass {
   modClassFrame outerframe;
   String id;
   String name;
   ArrayList probes;

   public NewClass(modClassFrame outerframe) {
      this.outerframe=outerframe;
      id = new String();
      name = new String();
      probes = new ArrayList();
   }

   public AbstractTableModel toTableModel() {
      return new AbstractTableModel() {
         private String[] columnNames = {"Probe", "Gene", "Description"};

         public String getColumnName(int i) { return columnNames[i]; }

         public int getRowCount() {
            int windowrows=8;
            int extra=1;
            if(probes.size()<windowrows)
               extra=windowrows-probes.size();
            return probes.size() + extra;
         }

         public int getColumnCount() {
            return 3;
         }

         public Object getValueAt(int r, int c) {
             if (r < probes.size())
            {
               String probeid = (String) probes.get(r);
               GeneDataReader geneData=outerframe.smap.geneData;
               switch (c) {
                  case 0:
                     return probeid;
                  case 1:
                     return geneData.getProbeGeneName(probeid);
                  case 2:
                     return geneData.getProbeDescription(probeid);
                  default:
                     return "";
               }
            }
            else {
               return "";
            }
         }

         public boolean isCellEditable(int r, int c) {
            if(c==0 || c==1)
               return true;
            else
               return false;
         }
      };
   }
}

class modClassFrame_jRadioButton2_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   modClassFrame_jRadioButton2_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jRadioButton2_actionPerformed(e);
   }
}

class modClassFrame_jRadioButton1_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   modClassFrame_jRadioButton1_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jRadioButton1_actionPerformed(e);
   }
}

class browseButton_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   browseButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.browseButton_actionPerformed(e);
   }
}

class modClassFrame_jButton2_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   modClassFrame_jButton2_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButton2_actionPerformed(e);
   }
}

class modClassFrame_jButton1_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   modClassFrame_jButton1_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButton1_actionPerformed(e);
   }
}

class editorProbeAdaptor implements CellEditorListener
{
   modClassFrame adaptee;
   editorProbeAdaptor(modClassFrame adaptee) { this.adaptee=adaptee; }
   public void editingStopped(ChangeEvent e) { adaptee.editorProbe_actionPerformed(e); }
   public void editingCanceled(ChangeEvent e) { editingCanceled(e); }
}

class editorGeneAdaptor implements CellEditorListener
{
   modClassFrame adaptee;
   editorGeneAdaptor(modClassFrame adaptee) { this.adaptee=adaptee; }
   public void editingStopped(ChangeEvent e) { adaptee.editorGene_actionPerformed(e); }
   public void editingCanceled(ChangeEvent e) { editingCanceled(e); }
}

class nextButton_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   nextButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.nextButton_actionPerformed(e);
   }
}

class backButton_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   backButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.backButton_actionPerformed(e);
   }
}

class cancelButton_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   cancelButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.cancelButton_actionPerformed(e);
   }
}

class finishButton_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   finishButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.finishButton_actionPerformed(e);
   }
}
