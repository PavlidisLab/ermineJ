package classScore.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import baseCode.gui.*;
import classScore.data.*;
import classScore.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 */

public class ClassWizardStep1A extends WizardStep
{
   ClassWizard wiz;
   GeneAnnotations geneData;
   GONames goData;
   NewGeneSet newGeneSet;
   JTable oldClassTable;
   JLabel modifyClassLabel;

   public ClassWizardStep1A( ClassWizard wiz, GeneAnnotations geneData,
                             GONames goData, NewGeneSet newGeneSet ) {
      super( wiz );
      this.wiz = wiz;
      this.geneData = geneData;
      this.goData = goData;
      this.newGeneSet = newGeneSet;
      populateTables();
   }

   //Component initialization
   protected void jbInit() {
      BorderLayout borderLayout1 = new BorderLayout();
      this.setLayout(borderLayout1);

      JPanel step1MPanel = new JPanel();
      BorderLayout borderLayout3 = new BorderLayout();
      step1MPanel.setLayout(borderLayout3);

      oldClassTable = new JTable();
      oldClassTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
      oldClassTable.getTableHeader().setReorderingAllowed( false );
      JScrollPane oldClassScrollPane = new JScrollPane(oldClassTable);
      oldClassScrollPane.setPreferredSize(new Dimension(250, 200));

      JPanel bottomPanel=new JPanel();
      JButton pickClassButton = new JButton("Select");
      pickClassButton.setEnabled(true);
      JPanel modifyPanel = new JPanel();
      modifyPanel.setPreferredSize(new Dimension(250, 250));
      BorderLayout borderLayout2 = new BorderLayout();
      modifyPanel.setLayout(borderLayout2);
      JLabel modifyLabel = new JLabel();
      modifyLabel.setText("Modify: ");
      modifyClassLabel = new JLabel();
      modifyClassLabel.setPreferredSize(new Dimension(77, 15));
      modifyClassLabel.setText("No Class Picked");
      JPanel mLabelPanel = new JPanel();
      bottomPanel.setPreferredSize(new Dimension(210, 50));
      mLabelPanel.add(modifyLabel, null);
      mLabelPanel.add(modifyClassLabel, null);
      pickClassButton.addActionListener(new ClassWizardStep1A_pickClassButton_actionAdapter(this));
      bottomPanel.add(pickClassButton, null);
      bottomPanel.add(mLabelPanel, null);
      modifyPanel.add(oldClassScrollPane, BorderLayout.CENTER);
      modifyPanel.add(bottomPanel, BorderLayout.SOUTH);
      step1MPanel.add(modifyPanel, BorderLayout.CENTER);

      this.add( step1MPanel );
   }

   public boolean isReady() {
      return (newGeneSet.getId().compareTo("")!=0);
   }

   void pickClassButton_actionPerformed(ActionEvent e) {
      int n = oldClassTable.getSelectedRowCount();
      if (n != 1) {
         GuiUtil.error("Only one class can be modified at a time.");
      }
      int row = oldClassTable.getSelectedRow();
      String id = (String) oldClassTable.getValueAt(row, 0);
      String desc = (String) oldClassTable.getValueAt(row, 1);
      newGeneSet.setId(id);
      modifyClassLabel.setText(id);
      newGeneSet.setDesc(desc);
      if (geneData.classExists(id)) {
         newGeneSet.getProbes().addAll((ArrayList) geneData.getClassToProbes(id));
      }
   }

   private void populateTables() {
      ModClassTableModel model = new ModClassTableModel(geneData, goData);
      TableSorter sorter = new TableSorter(model);
      oldClassTable.setModel(sorter);
      sorter.setTableHeader(oldClassTable.getTableHeader());
      oldClassTable.getColumnModel().getColumn(0).setPreferredWidth(30);
      oldClassTable.getColumnModel().getColumn(2).setPreferredWidth(30);
      oldClassTable.getColumnModel().getColumn(3).setPreferredWidth(30);
      oldClassTable.revalidate();
   }
}

class ClassWizardStep1A_pickClassButton_actionAdapter implements java.awt.event.ActionListener {
   ClassWizardStep1A adaptee;

   ClassWizardStep1A_pickClassButton_actionAdapter(ClassWizardStep1A adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.pickClassButton_actionPerformed(e);
   }
}

class ModClassTableModel extends AbstractTableModel {
   GeneAnnotations geneData;
   GONames goData;
   Vector columnNames = new Vector();
   private NumberFormat nf = NumberFormat.getInstance();

   public ModClassTableModel(GeneAnnotations geneData, GONames goData) {
      this.geneData = geneData;
      this.goData = goData;
      nf.setMaximumFractionDigits(8);
      columnNames.add("Name");
      columnNames.add("Description");
      columnNames.add("# of Probes");
      columnNames.add("# of Genes");
   }

   public String getColumnName(int i) {return (String) columnNames.get(i);
   }

   public int getColumnCount() {return columnNames.size();
   }

   public int getRowCount() {
      return geneData.numClasses();
   }

   public Object getValueAt(int i, int j) {
      String classid=geneData.getClass(i);
      switch (j) {
         case 0:
            return classid;
         case 1:
            return goData.getNameForId(classid);
         case 2:
            return new Integer(geneData.numProbes(classid));
         case 3:
            return new Integer(geneData.numGenes(classid));
         default:
            return "";
      }
   }
}
