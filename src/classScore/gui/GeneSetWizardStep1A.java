package classScore.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import baseCode.gui.table.TableSorter;

import baseCode.gui.WizardStep;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.NewGeneSet;
import java.awt.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 */

public class GeneSetWizardStep1A extends WizardStep
{
   GeneSetWizard wiz;
   GeneAnnotations geneData;
   GONames goData;
   NewGeneSet newGeneSet;
   JTable oldClassTable;

   public GeneSetWizardStep1A( GeneSetWizard wiz, GeneAnnotations geneData,
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
      oldClassScrollPane.setPreferredSize(new Dimension(250, 230));

      step1MPanel.setPreferredSize(new Dimension(250, 250));
      step1MPanel.add(oldClassScrollPane,  BorderLayout.CENTER);

      this.addHelp("<html>This is a place holder.<br>"+
                   "Blah, blah, blah, blah, blah.");
      this.addMain(step1MPanel);
   }

   public boolean isReady() {
      int n = oldClassTable.getSelectedRowCount();
      if (n > 1) {
         JOptionPane.showMessageDialog(wiz, "Only one class can be modified at a time.",
                                       "Error", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      else if(n<1)
      {
          JOptionPane.showMessageDialog(wiz, "Pick a class to be modified.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
          return false;
      }
      else
      {
         int row = oldClassTable.getSelectedRow();
         String id = ( String ) oldClassTable.getValueAt( row, 0 );
         String desc = ( String ) oldClassTable.getValueAt( row, 1 );
         newGeneSet.setId( id );
         newGeneSet.setDesc( desc );
         if ( geneData.classExists( id ) ) {
            newGeneSet.getProbes().addAll( ( ArrayList ) geneData.getClassToProbes( id ) );
         }
         return true;
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
