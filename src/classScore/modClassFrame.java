package classScore;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

//import com.borland.jbcl.layout.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class modClassFrame extends JDialog {
   JPanel jPanel1;

   //holds bottom buttons
   JPanel BottomPanel = new JPanel();
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
   JRadioButton fileInputButton = new JRadioButton();
   JRadioButton manInputButton;
   //step 1 bottom
   JPanel jPanel3 = new JPanel(); //outer file details
   JPanel jPanel2 = new JPanel(); // holds file chooser
   JButton browseButton = new JButton();
   JTextField classFile = new JTextField();
   JFileChooser chooser = new JFileChooser();
   File startPath; // holds file type stuff//  'choose file type'//  holds radio buttons
   ButtonGroup buttonGroup2 = new ButtonGroup();

   //panels for step 1M
   JPanel step1MPanel = new JPanel();
   JScrollPane oldClassScrollPane;
   JTable oldClassTable;

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
   JPanel ncDescPanel = new JPanel();
   JPanel ncInfo2Panel = new JPanel();
   JPanel ncInfo1Panel = new JPanel();
   JTextArea classDescTA = new JTextArea();
   JTextField classIDTF;
   JPanel modifyPanel = new JPanel();
   JLabel modifyLabel = new JLabel();
   JPanel mLabelPanel = new JPanel();
   JLabel modifyClassLabel = new JLabel();

   //logic
   int step = 1;
   int inputMethod = 0;
   boolean makenew;

   NewGeneSet newGeneSet;
   InitialMaps imaps;
   ClassPanel classpanel;
   String folder;
   String cid;

   public modClassFrame(boolean makenew, InitialMaps imap,
                        ClassPanel classpanel, String saveFolder, String cid) {
/*
      setModal(true);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.makenew = makenew;
      this.imaps = imap;
      this.classpanel = classpanel;
      this.folder = saveFolder;
      this.cid = cid;
      try {
         jbInit();
         populateTables();
      } catch (Exception e) {
         e.printStackTrace();
      }
*/
   }
/*
   //Component initialization
   private void jbInit() throws Exception {
      this.getContentPane().setBackground(Color.white);
      setResizable(true);
      jPanel1 = (JPanel)this.getContentPane();
      jPanel1.setBackground(SystemColor.control);
      jPanel1.setAlignmentX((float) 0.5);
      jPanel1.setAlignmentY((float) 0.5);
      jPanel1.setMaximumSize(new Dimension(32767, 32767));
      jPanel1.setPreferredSize(new Dimension(550, 300));

      /////////////////////////
      //bottom buttons
      BottomPanel.setBackground(SystemColor.control);
      BottomPanel.setPreferredSize(new Dimension(200, 40));
      nextButton.setText("Next >");
      nextButton.addActionListener(new nextButton_actionAdapter(this));
      backButton.setText("< Back");
      backButton.addActionListener(new backButton_actionAdapter(this));
      backButton.setEnabled(false);
      cancelButton.setText("Cancel");
      cancelButton.addActionListener(new cancelButton_actionAdapter(this));
      finishButton.setAlignmentY((float) 0.5);
      finishButton.setText("Finish");
      finishButton.addActionListener(new finishButton_actionAdapter(this));
      finishButton.setEnabled(false);
      jPanel4.setPreferredSize(new Dimension(354, 73));
      jPanel2.setPreferredSize(new Dimension(379, 35));
      BottomPanel.add(cancelButton, null);
      BottomPanel.add(backButton, null);
      BottomPanel.add(nextButton, null);
      BottomPanel.add(finishButton, null);
      jPanel1.add(BottomPanel, BorderLayout.SOUTH);

//      if(makenew)
//      {
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
      fileInputButton.setBackground(SystemColor.control);
      fileInputButton.setBorder(BorderFactory.createLineBorder(Color.black));
      fileInputButton.setText("File");
      fileInputButton.addActionListener(new
                                        modClassFrame_fileInputButton_actionAdapter(this));
      jPanel3.setPreferredSize(new Dimension(354, 50));
      buttonGroup1.add(fileInputButton);
      manInputButton = new JRadioButton("Manual", true);
      manInputButton.setBackground(SystemColor.control);
      manInputButton.setMaximumSize(new Dimension(91, 23));
      manInputButton.addActionListener(new
                                       modClassFrame_manInputButton_actionAdapter(this));
      manInputButton.setBorder(BorderFactory.createLineBorder(Color.black));
      buttonGroup1.add(manInputButton);
      jLabel4.setBorder(null);
      jLabel4.setText("- File with gene symbols or probe ids");
      jLabel5.setBorder(null);
      jLabel5.setText("- Enter using lists");
      jPanel4.add(jLabel5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(0, 16, 8, 10),
                                                  125, 10));
      jPanel4.add(jLabel4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(3, 16, 0, 10),
                                                  30, 10));
      jPanel4.add(manInputButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
              , GridBagConstraints.CENTER, GridBagConstraints.NONE,
              new Insets(0, 9, 8, 0), 8, 12));
      jPanel4.add(fileInputButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
              , GridBagConstraints.CENTER, GridBagConstraints.NONE,
              new Insets(3, 9, 0, 0), 26, 12));
      jPanel7.add(jLabel8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(6, 21, 0, 74),
                                                  0, 0));
      jPanel7.add(jPanel4, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                  , GridBagConstraints.CENTER,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(6, 10, 12, 16), -1,
                                                  8));

      /////////////////////////
      //step 1 bottom
      jPanel3.setBackground(SystemColor.control);
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
//      startPath = new File(System.getProperty("user.home"));
      chooser.setCurrentDirectory(new File(folder));
      jPanel2.add(browseButton, null);
      jPanel2.add(classFile, null);
      //file type stuff

      step1Panel.add(jPanel7, null);
      step1Panel.add(jPanel3, null);
      jPanel3.add(jPanel2, null);
      jPanel1.add(step1Panel, BorderLayout.CENTER);
      jPanel1.remove(step1Panel);
//      }
//      else
//      {
      //step 1M
      oldClassTable = new JTable();
      oldClassTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
      oldClassScrollPane = new JScrollPane(oldClassTable);
      oldClassScrollPane.setPreferredSize(new Dimension(250, 200));
      JButton pickClassButton = new JButton("Select");
      pickClassButton.setEnabled(true);
      modifyPanel.add(oldClassScrollPane, null);
      modifyPanel.add(pickClassButton, null);
      modifyPanel.add(mLabelPanel, null);
      modifyPanel.setPreferredSize(new Dimension(250, 250));
      modifyLabel.setText("Modify: ");
      modifyClassLabel.setPreferredSize(new Dimension(77, 15));
      modifyClassLabel.setText("No Class Picked");
      mLabelPanel.add(modifyLabel, null);
      mLabelPanel.add(modifyClassLabel, null);
      pickClassButton.addActionListener(new pickClassButton_actionAdapter(this));
      step1MPanel.add(modifyPanel, null);
//        jPanel1.add(step1MPanel, BorderLayout.CENTER);
//        jPanel1.remove(step1MPanel);
//      }

///////////////////////////////////////////////////////////////////////////////
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
      deleteButton.addActionListener(new
                                     modClassFrame_delete_actionPerformed_actionAdapter(this));
      jPanel9.add(jButton1, null);
      jPanel9.add(deleteButton, null);
      step2Panel.add(jPanel9, null);

//    jPanel1.add(step2Panel, BorderLayout.CENTER);
//    jPanel1.remove(step2Panel);

///////////////////////////////////////////////////////////////////////////////
      //panels for step 3
      step3Panel = new JPanel();
      JPanel ncIDPanel = new JPanel();
      JLabel classIDL = new JLabel("New Class ID: ");
      ncDescPanel.setPreferredSize(new Dimension(143, 180));
      ncInfo1Panel.setPreferredSize(new Dimension(150, 240));
      classIDTF = new JTextField();
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

//    jPanel1.add(step3Panel, BorderLayout.CENTER);
//    jPanel1.remove(step3Panel);

///////////////
      newGeneSet = new NewGeneSet(imaps.geneData);
      if (makenew) {
         jPanel1.add(step1Panel);
         this.setTitle("Define New Class - Step 1 of 3");
      } else {
         if (cid.compareTo("") == 0) {
            jPanel1.add(step1MPanel);
            this.setTitle("Modify Class - Step 1 of 3");
         } else {
            this.setTitle("Modify Class - Step 2 of 3");
            step = 2;
            backButton.setEnabled(true);
            jPanel1.add(step2Panel);
            gotoStep2();
         }
      }
   }

   public void error(String message) {
     JOptionPane.showMessageDialog(null, "Error: " + message + "\n");
  }

  private void populateTables() {
      if (!makenew) {
         SortFilterModel ocSorter = new SortFilterModel(imaps.toTableModel());
         oldClassTable.setModel(ocSorter);
         oldClassTable.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
               int tableColumn = oldClassTable.columnAtPoint(event.getPoint());
               int modelColumn = oldClassTable.convertColumnIndexToModel(
                       tableColumn);
               if (modelColumn == 0 || modelColumn == 2) {
                  ((SortFilterModel) oldClassTable.getModel()).sort(modelColumn);
               }
            }
         });
         oldClassTable.getColumnModel().getColumn(0).setPreferredWidth(40);
      }

      SortFilterModel sorter = new SortFilterModel(imaps.geneData.toTableModel());
      probeTable.setModel(sorter);
      probeTable.getTableHeader().addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent event) {
            int tableColumn = probeTable.columnAtPoint(event.getPoint());
            int modelColumn = probeTable.convertColumnIndexToModel(tableColumn);
            ((SortFilterModel) probeTable.getModel()).sort(modelColumn);
         }
      });
      probeTable.getColumnModel().getColumn(0).setPreferredWidth(40);

      ncTableModel = newGeneSet.toTableModel(false);
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

      finalTableModel = newGeneSet.toTableModel(true);
      finalTable.setModel(finalTableModel);
      newClassTable.getColumnModel().getColumn(0).setPreferredWidth(40);
   }

   void gotoStep2() {
      newGeneSet.id = cid;
      newGeneSet.desc = imaps.goName.getNameForId(cid);
      if (imaps.classToProbe.containsKey(cid)) {
         newGeneSet.probes.addAll((ArrayList) imaps.classToProbe.get(cid));
      }
      step2Panel.revalidate();
      updateCountLabel();
      this.repaint();
   }

   void manInputButton_actionPerformed(ActionEvent e) {
      classFile.setEditable(false);
      classFile.setEnabled(false);
      browseButton.setEnabled(false);
      inputMethod = 0;
   }

   void fileInputButton_actionPerformed(ActionEvent e) {
      classFile.setEditable(true);
      classFile.setEnabled(true);
      browseButton.setEnabled(true);
      inputMethod = 1;
   }

   void browseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         classFile.setText(chooser.getSelectedFile().toString());
      }
   }

   void pickClassButton_actionPerformed(ActionEvent e) {
      int n = oldClassTable.getSelectedRowCount();
      if (n != 1) {
         error("Only one class can be modified at a time.");
      }
      int row = oldClassTable.getSelectedRow();
      String id = (String) oldClassTable.getValueAt(row, 0);
      String desc = (String) oldClassTable.getValueAt(row, 1);
      newGeneSet.id = id;
      modifyClassLabel.setText(id);
      newGeneSet.desc = desc;
      if (imaps.classToProbe.containsKey(id)) {
         newGeneSet.probes.addAll((ArrayList) imaps.classToProbe.get(id));
      }
   }

   void delete_actionPerformed(ActionEvent e) {
      int n = newClassTable.getSelectedRowCount();
      int[] rows = newClassTable.getSelectedRows();
      for (int i = 0; i < n; i++) {
         newGeneSet.probes.remove(newClassTable.getValueAt(rows[i] - i, 0));
      }
      int s = newGeneSet.probes.size();
      ncTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void addButton_actionPerformed(ActionEvent e) {
      int n = probeTable.getSelectedRowCount();
      int[] rows = probeTable.getSelectedRows();
      for (int i = 0; i < n; i++) {
         //newGeneSet.probes.add(probeTable.getValueAt(rows[i], 0)); (for just deleting probes)
         String newGene;
         if ((newGene = imaps.geneData.getProbeGeneName((String) probeTable.
                 getValueAt(rows[i], 0))) != null) {
            addGene(newGene);
         }
      }
      HashSet noDupes = new HashSet(newGeneSet.probes);
      newGeneSet.probes.clear();
      newGeneSet.probes.addAll(noDupes);
      int s = newGeneSet.probes.size();
      ncTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void editorProbe_actionPerformed(ChangeEvent e) {
      String newProbe = (String) ((DefaultCellEditor) e.getSource()).
                        getCellEditorValue();
      String newGene;
      if ((newGene = imaps.geneData.getProbeGeneName(newProbe)) != null) {
         addGene(newGene);
      } else {
         error("Probe " + newProbe + " does not exist.");
         for adding specified probe
               if(smap.geneData.getProbeGeneName(newProbe) != null)
               {
                  newGeneSet.probes.add(newProbe);
                  int s = newGeneSet.probes.size();
                  ncTableModel.fireTableDataChanged();
                  updateCountLabel();
               }
               else
                  error("Probe " + newProbe + " does not exist.");
      }
   }

   void editorGene_actionPerformed(ChangeEvent e) {
      String newGene = (String) ((DefaultCellEditor) e.getSource()).
                       getCellEditorValue();
      addGene(newGene);
   }

   void addGene(String gene) {
      ArrayList probelist = imaps.geneData.getGeneProbeList(gene);
      if (probelist != null) {
         newGeneSet.probes.addAll(probelist);
         int s = newGeneSet.probes.size();
         ncTableModel.fireTableDataChanged();
         updateCountLabel();
      } else {
         error("Gene " + gene + " does not exist.");
      }
   }

   void updateCountLabel() {
      countLabel.setText("Number of Probes: " + newGeneSet.probes.size());
   }

   void classIDEditor_actionPerformed(ChangeEvent e) {
      String classID = (String) ((DefaultCellEditor) e.getSource()).
                       getCellEditorValue();
      if (imaps.geneData.numProbes(classID)>0 && makenew) {
         error("A class by the ID " + classID + " already exists.");
      } else {
         newGeneSet.id = classID;
         classIDFinal.setText(classID);
      }
   }

   void classDescListener_actionPerformed(DocumentEvent e) {
      Document doc = (Document) e.getDocument();
      int length = doc.getLength();
      try {newGeneSet.desc = doc.getText(0, length);
      } catch (BadLocationException be) {be.printStackTrace();
      }
   }

   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
         if (!makenew && newGeneSet.id.compareTo("") == 0) {
            error("Pick a class to be modified.");
         } else {
            if (makenew && inputMethod == 1) {
               newGeneSet.loadClassFile(classFile.getText());
            }
            if (!(inputMethod == 1 && newGeneSet.id.compareTo("") == 0)) {
               if (makenew) {
                  this.getContentPane().remove(step1Panel);
                  this.setTitle("Define New Class - Step 2 of 3");
               } else {
                  this.getContentPane().remove(step1MPanel);
                  this.setTitle("Modify Class - Step 2 of 3");
               }
               step = 2;
               backButton.setEnabled(true);
               this.getContentPane().add(step2Panel);
               step2Panel.revalidate();
               updateCountLabel();
               this.repaint();
            }
         }
      } else if (step == 2) {
         this.getContentPane().remove(step2Panel);
         step = 3;
         if (makenew) {
            this.setTitle("Define New Class - Step 3 of 3");
         } else {
            this.setTitle("Modify Class - Step 3 of 3");
         }
         backButton.setEnabled(true);
         classIDTF.setText(newGeneSet.id);
         classDescTA.setText(newGeneSet.desc);
         if (newGeneSet.id.compareTo("") != 0) {
            classIDFinal.setText(newGeneSet.id);
         }
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
         backButton.setEnabled(false);
         if (makenew) {
            this.setTitle("Define New Class - Step 1 of 3");
            this.getContentPane().add(step1Panel);
            step1Panel.revalidate();
         } else {
            this.setTitle("Modify Class - Step 1 of 3");
            this.getContentPane().add(step1MPanel);
            step1MPanel.revalidate();
         }
         this.repaint();
      }
      if (step == 3) {
         this.getContentPane().remove(step3Panel);
         step = 2;
         if (makenew) {
            this.setTitle("Define New Class - Step 2 of 3");
         } else {
            this.setTitle("Modify Class - Step 2 of 3");
         }
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
      String id = newGeneSet.id;
      String desc = newGeneSet.desc;
      if (id.compareTo("") == 0) {
         error("The class ID must be specified.");
      } else {
         if (makenew) {
            imaps.addClass(id, desc, newGeneSet.probes);
         } else {
            imaps.modifyClass(id, desc, newGeneSet.probes);
         }
         newGeneSet.saveClass(folder, 0);
         classpanel.setModel(imaps.toTableModel());
         dispose();
      }
   }
*/
}
/*

class modClassFrame_manInputButton_actionAdapter implements java.awt.event.
        ActionListener {
   modClassFrame adaptee;

   modClassFrame_manInputButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.manInputButton_actionPerformed(e);
   }
}


class modClassFrame_fileInputButton_actionAdapter implements java.awt.event.
        ActionListener {
   modClassFrame adaptee;

   modClassFrame_fileInputButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.fileInputButton_actionPerformed(e);
   }
}


class browseButton_actionAdapter implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   browseButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.browseButton_actionPerformed(e);
   }
}


class pickClassButton_actionAdapter implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   pickClassButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.pickClassButton_actionPerformed(e);
   }
}


class modClassFrame_delete_actionPerformed_actionAdapter implements java.awt.
        event.ActionListener {
   modClassFrame adaptee;

   modClassFrame_delete_actionPerformed_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.delete_actionPerformed(e);
   }
}


class modClassFrame_jButton1_actionAdapter implements java.awt.event.
        ActionListener {
   modClassFrame adaptee;

   modClassFrame_jButton1_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.addButton_actionPerformed(e);
   }
}


class editorProbeAdaptor implements CellEditorListener {
   modClassFrame adaptee;
   editorProbeAdaptor(modClassFrame adaptee) {this.adaptee = adaptee;
   }

   public void editingStopped(ChangeEvent e) {adaptee.
           editorProbe_actionPerformed(e);
   }

   public void editingCanceled(ChangeEvent e) {editingCanceled(e);
   }
}


class editorGeneAdaptor implements CellEditorListener {
   modClassFrame adaptee;
   editorGeneAdaptor(modClassFrame adaptee) {this.adaptee = adaptee;
   }

   public void editingStopped(ChangeEvent e) {adaptee.
           editorGene_actionPerformed(e);
   }

   public void editingCanceled(ChangeEvent e) {editingCanceled(e);
   }
}


class classIDEditorAdaptor implements CellEditorListener {
   modClassFrame adaptee;
   classIDEditorAdaptor(modClassFrame adaptee) {this.adaptee = adaptee;
   }

   public void editingStopped(ChangeEvent e) {adaptee.
           classIDEditor_actionPerformed(e);
   }

   public void editingCanceled(ChangeEvent e) {editingCanceled(e);
   }
}


class ClassDescListener implements DocumentListener {
   modClassFrame adaptee;
   ClassDescListener(modClassFrame adaptee) {this.adaptee = adaptee;
   }

   public void insertUpdate(DocumentEvent e) {adaptee.
           classDescListener_actionPerformed(e);
   }

   public void removeUpdate(DocumentEvent e) {adaptee.
           classDescListener_actionPerformed(e);
   }

   public void changedUpdate(DocumentEvent e) {}
}


class nextButton_actionAdapter implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   nextButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.nextButton_actionPerformed(e);
   }
}


class backButton_actionAdapter implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   backButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.backButton_actionPerformed(e);
   }
}


class cancelButton_actionAdapter implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   cancelButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.cancelButton_actionPerformed(e);
   }
}


class finishButton_actionAdapter implements java.awt.event.ActionListener {
   modClassFrame adaptee;

   finishButton_actionAdapter(modClassFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.finishButton_actionPerformed(e);
   }
}
*/
