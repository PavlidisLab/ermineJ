package classScore;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
//import com.borland.jbcl.layout.*;

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
   JPanel step1Panel = new JPanel();
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
   JPanel step2Panel;
   JLabel countLabel = new JLabel();
   JPanel jPanel10 = new JPanel();
   JScrollPane probeScrollPane;
//   JList probeList;
//   DefaultListModel listModel;
   JTable probeTable;
   JScrollPane newClassScrollPane;
   JTable newClassTable;
   SortFilterModel ncsorter;
   AbstractTableModel ncTableModel;
   JPanel jPanel9 = new JPanel();
   JButton jButton1 = new JButton();
   JButton deleteButton = new JButton();

   //panels for step three
   JPanel step3Panel;
   JTable finalTable;
   JLabel classDescL = new JLabel("New Class ID: ");
   JLabel classIDFinal = new JLabel("New Class ID: ");
   AbstractTableModel finalTableModel;

   //logic
   int inputMethod = 0;
   int step = 1;

   NewClass newclass;
   SetupMaps smap;
   ClassPanel classpanel;
  JPanel ncDescPanel = new JPanel();
  JPanel ncInfo2Panel = new JPanel();
  JPanel ncInfo1Panel = new JPanel();
  JTextArea classDescTA = new JTextArea();

   public modClassFrame(SetupMaps smap, ClassPanel classpanel) {
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.smap=smap;
      this.classpanel=classpanel;
      try {
         jbInit();
         populateTables();
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
      jPanel1.setMaximumSize(new Dimension(32767, 32767));
      jPanel1.setPreferredSize(new Dimension(550, 300));

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
      jPanel4.setLayout(gridBagLayout1);
      jRadioButton1.setBackground(SystemColor.control);
      jRadioButton1.setBorder(BorderFactory.createLineBorder(Color.black));
      jRadioButton1.setText("File");
      jRadioButton1.addActionListener(new modClassFrame_jRadioButton1_actionAdapter(this));
      jPanel5.setPreferredSize(new Dimension(200, 40));
      jPanel3.setPreferredSize(new Dimension(380, 133));
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
    jPanel1.add(jPanel5, BorderLayout.SOUTH);
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
      jPanel3.setLayout(gridBagLayout3);
      //file chooser stuff
      jPanel2.setBackground(SystemColor.control);
      browseButton.setText("Browse....");
      browseButton.addActionListener(new browseButton_actionAdapter(this));
      browseButton.setEnabled(false);
      classFile.setEditable(false);
      classFile.setMinimumSize(new Dimension(4, 19));
      classFile.setPreferredSize(new Dimension(230, 19));
      classFile.setToolTipText("File containing class members");
      classFile.setText("File containing class members");
      startPath = new File(System.getProperty("user.home"));
      chooser.setCurrentDirectory(startPath);
      jPanel2.add(browseButton, null);
      jPanel2.add(classFile, null);
      jPanel3.add(jPanel8,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 3, 0, 103), -181, 21));
      //file type stuff
      jPanel8.setBackground(SystemColor.control);
      jPanel8.setBorder(null);
      jPanel6.setBorder(null);
      jLabel3.setMaximumSize(new Dimension(999, 15));
      jLabel3.setMinimumSize(new Dimension(209, 15));
      jLabel3.setPreferredSize(new Dimension(209, 15));
      jLabel3.setToolTipText("");
      jLabel3.setText("Choose the file type:");
      jPanel6.setLayout(gridBagLayout2);
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
      jRadioButton4.setSelected(true);
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
      step1Panel.add(jPanel7, null);
      step1Panel.add(jPanel3, null);
      jPanel3.add(jPanel2,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 7, 7), 40, -1));
      jPanel8.add(jLabel3, null);
      jPanel8.add(jPanel6, null);
      jPanel1.add(step1Panel, BorderLayout.CENTER);
      jPanel1.remove(step1Panel);

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
      finishButton.setEnabled(false);
      jPanel5.add(cancelButton, null);
      jPanel5.add(backButton, null);
      jPanel5.add(nextButton, null);
      jPanel5.add(finishButton, null);

      //step 2
      step2Panel = new JPanel();
      step2Panel.setBorder(BorderFactory.createEtchedBorder());
      countLabel.setForeground(Color.black);
      countLabel.setText("Number of Probes: 0");
      step2Panel.add(countLabel, null);

      probeTable = new JTable();
      probeTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
      probeScrollPane = new JScrollPane(probeTable);
      probeScrollPane.setMaximumSize(new Dimension(32767, 32767));
      probeScrollPane.setPreferredSize(new Dimension(250, 150));
      jPanel10.add(probeScrollPane, null);

      newClassTable = new JTable();
      newClassTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
      newClassScrollPane = new JScrollPane(newClassTable);
      newClassScrollPane.setMaximumSize(new Dimension(32767, 32767));
      newClassScrollPane.setPreferredSize(new Dimension(250, 150));
      jPanel10.add(newClassScrollPane, null);
      step2Panel.add(jPanel10, null);

      jPanel9.setMinimumSize(new Dimension(1, 1));
      jPanel9.setPreferredSize(new Dimension(200, 30));
      jButton1.setSelected(false);
      jButton1.setText("Add >");
      jButton1.addActionListener(new modClassFrame_jButton1_actionAdapter(this));
      deleteButton.setSelected(false);
      deleteButton.setText("Delete");
      deleteButton.addActionListener(new modClassFrame_delete_actionPerformed_actionAdapter(this));
      jPanel9.add(jButton1, null);
      jPanel9.add(deleteButton, null);
      step2Panel.add(jPanel9, null);

      jPanel1.add(step2Panel, BorderLayout.CENTER);
      jPanel1.remove(step2Panel);

      //panels for step 3
      step3Panel = new JPanel();
      JPanel ncIDPanel = new JPanel();
      JLabel classIDL = new JLabel("New Class ID: ");
      ncDescPanel.setPreferredSize(new Dimension(143, 180));
      ncInfo1Panel.setPreferredSize(new Dimension(150, 240));
      JTextField classIDTF = new JTextField();
      classIDTF.setPreferredSize(new Dimension(100, 19));
      classIDTF.setToolTipText("New Class ID");
      DefaultCellEditor classIDEditor = new DefaultCellEditor(classIDTF);
      classIDEditor.addCellEditorListener(new classIDEditorAdaptor(this));
      ncIDPanel.add(classIDL);
      ncIDPanel.add(classIDTF);
      ncInfo1Panel.add(ncIDPanel, null);
      ncInfo1Panel.add(ncDescPanel, null);
      classDescL.setRequestFocusEnabled(true);
      classDescL.setText("New Class Description: ");
      ncDescPanel.add(classDescL);
      classDescTA.setToolTipText("New Class ID");
      classDescTA.getDocument().addDocumentListener(new ClassDescListener(this));
      classDescTA.setLineWrap(true);
      JScrollPane classDTAScroll = new JScrollPane(classDescTA);
      classDTAScroll.setBorder(BorderFactory.createLoweredBevelBorder());
      classDTAScroll.setPreferredSize(new Dimension(130, 140));
      ncDescPanel.add(classDTAScroll, null);
      step3Panel.add(ncInfo1Panel, null);
      step3Panel.add(ncInfo2Panel, null);
      classIDFinal.setText("No Class Name");
      classIDFinal.setRequestFocusEnabled(true);
      finalTable = new JTable();
      finalTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
      JScrollPane finalScrollPane = new JScrollPane(finalTable);
      finalScrollPane.setPreferredSize(new Dimension(200, 200));
      ncInfo2Panel.add(classIDFinal, null);
      ncInfo2Panel.add(finalScrollPane, null);
      ncInfo2Panel.setPreferredSize(new Dimension(220, 240));
      ncIDPanel.setPreferredSize(new Dimension(128, 51));
      classIDTF.setBorder(BorderFactory.createLoweredBevelBorder());

      jPanel1.add(step3Panel, BorderLayout.CENTER);
      jPanel1.remove(step3Panel);

///////////////
      newclass = new NewClass(this);
      jPanel1.add(step1Panel);
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

   private void populateTables() {
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

      ncTableModel = newclass.toTableModel(false);
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
      newClassTable.getColumnModel().getColumn(0).setPreferredWidth(40);

      finalTableModel = newclass.toTableModel(true);
      finalTable.setModel(finalTableModel);
      newClassTable.getColumnModel().getColumn(0).setPreferredWidth(40);
   }

   void delete_actionPerformed(ActionEvent e) {
      int n = newClassTable.getSelectedRowCount();
      int[] rows = newClassTable.getSelectedRows();
      for (int i = 0; i < n; i++) {
         newclass.probes.remove(newClassTable.getValueAt(rows[i]-i, 0));
      }
      int s = newclass.probes.size();
      ncTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void jButton1_actionPerformed(ActionEvent e) {
      int n = probeTable.getSelectedRowCount();
      int[] rows = probeTable.getSelectedRows();
      for (int i = 0; i < n; i++) {
         newclass.probes.add(probeTable.getValueAt(rows[i], 0));
      }
      HashSet noDupes=new HashSet(newclass.probes);
      newclass.probes.clear();
      newclass.probes.addAll(noDupes);
      int s = newclass.probes.size();
      ncTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void editorProbe_actionPerformed(ChangeEvent e)
   {
      String newProbe = (String)((DefaultCellEditor) e.getSource()).getCellEditorValue();
      System.err.println(newProbe);
      if(smap.geneData.getProbeGeneName(newProbe) != null)
      {
         newclass.probes.add(newProbe);
         int s = newclass.probes.size();
         ncTableModel.fireTableDataChanged();
         updateCountLabel();
      }
      else
         errorPopUp("Probe " + newProbe + " does not exist.");
   }

   void editorGene_actionPerformed(ChangeEvent e)
   {
      String newGene = (String)((DefaultCellEditor) e.getSource()).getCellEditorValue();
      System.err.println(newGene);
      ArrayList probelist = smap.geneData.getGeneProbeList(newGene);
      if(probelist != null)
      {
         newclass.probes.addAll(probelist);
         int s = newclass.probes.size();
         ncTableModel.fireTableDataChanged();
         updateCountLabel();
      }
      else
         errorPopUp("Gene " + newGene + " does not exist.");
   }


   void updateCountLabel()
   {
      countLabel.setText("Number of Probes: "+newclass.probes.size());
   }

   void classIDEditor_actionPerformed(ChangeEvent e)
   {
      String classID = (String)((DefaultCellEditor) e.getSource()).getCellEditorValue();
      if(smap.geneData.classToProbeMapContains(classID))
         errorPopUp("A class by the ID " + classID + " already exists.");
      else
      {
         newclass.id=classID;
         classIDFinal.setText(classID);
      }
   }

   void classDescListener_actionPerformed(DocumentEvent e)
   {
      Document doc = (Document)e.getDocument();
      int length = doc.getLength();
      try{newclass.desc=doc.getText(0,length);}
      catch(BadLocationException be) { be.printStackTrace();}
   }

   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
         this.getContentPane().remove(step1Panel);
         step = 2;
         this.setTitle("Define New Class - Step 2 of 3");
         backButton.setEnabled(true);
         if (inputMethod == 0) {
            this.getContentPane().add(step2Panel);
            step2Panel.revalidate();
         }
         this.repaint();
      }
      else if(step == 2)
      {
         this.getContentPane().remove(step2Panel);
         step = 3;
         this.setTitle("Define New Class - Step 3 of 3");
         nextButton.setEnabled(false);
         finishButton.setEnabled(true);
         this.getContentPane().add(step3Panel);
         step3Panel.revalidate();
         this.repaint();
      }
   }

   void backButton_actionPerformed(ActionEvent e) {
      if (step == 2) {
         this.getContentPane().remove(step2Panel);
         step = 1;
         this.setTitle("Define New Class - Step 1 of 3");
         backButton.setEnabled(false);
         this.getContentPane().add(step1Panel);
         step1Panel.revalidate();
         this.repaint();
      }
      if (step == 3) {
         this.getContentPane().remove(step3Panel);
         step = 2;
         this.setTitle("Define New Class - Step 2 of 3");
         nextButton.setEnabled(true);
         finishButton.setEnabled(false);
         this.getContentPane().add(step2Panel);
         step2Panel.revalidate();
         this.repaint();
      }
   }

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {
      String id=newclass.id;
      String desc=newclass.desc;
      if(id.compareTo("")==0)
      {
         errorPopUp("The class ID must be specified.");
      }
      else
      {
          smap.addClass(id, desc, newclass.probes);
          classpanel.setModel(smap.toTableModel());
          dispose();
      }
   }

}

class NewClass {
   modClassFrame outerframe;
   String id;
   String desc;
   ArrayList probes;

   public NewClass(modClassFrame outerframe) {
      this.outerframe=outerframe;
      id = new String();
      desc = new String();
      probes = new ArrayList();
   }

   public AbstractTableModel toTableModel(boolean editable) {
      final boolean finalized=editable;

      return new AbstractTableModel() {

         private String[] columnNames = {"Probe", "Gene", "Description"};

         public String getColumnName(int i) { return columnNames[i]; }

         public int getRowCount() {
            int windowrows;
            if(finalized)
               windowrows=11;
            else
               windowrows=8;
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
            if(!finalized && (c==0 || c==1))
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

class modClassFrame_delete_actionPerformed_actionAdapter
    implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   modClassFrame_delete_actionPerformed_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.delete_actionPerformed(e);
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

class classIDEditorAdaptor implements CellEditorListener
{
   modClassFrame adaptee;
   classIDEditorAdaptor(modClassFrame adaptee) { this.adaptee=adaptee; }
   public void editingStopped(ChangeEvent e) { adaptee.classIDEditor_actionPerformed(e); }
   public void editingCanceled(ChangeEvent e) { editingCanceled(e); }
}

class ClassDescListener implements DocumentListener
{
   modClassFrame adaptee;
   ClassDescListener(modClassFrame adaptee) { this.adaptee=adaptee; }
   public void insertUpdate(DocumentEvent e) { adaptee.classDescListener_actionPerformed(e); }
   public void removeUpdate(DocumentEvent e) { adaptee.classDescListener_actionPerformed(e); }
   public void changedUpdate(DocumentEvent e) { }
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
