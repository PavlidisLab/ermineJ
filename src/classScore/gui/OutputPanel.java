package classScore.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import classScore.data.*;
import classScore.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo make columns start out better sizes
 * @todo integers don't sort correctly
 */

public class OutputPanel extends JScrollPane {
   JTable table;
   OutputTableModel model;
   classScoreFrame callingframe;
   Vector results;
   Vector resultToolTips = new Vector();
   GeneAnnotations geneData;
   GONames goData;

   public OutputPanel(classScoreFrame callingframe, Vector results) {
      this.callingframe=callingframe;
      this.results=results;
      model = new OutputTableModel(results);
      table = new JTable(){
         //Implement table header tool tips.
         protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
               public String getToolTipText(MouseEvent e) {
                  String tip = null;
                  java.awt.Point p = e.getPoint();
                  int index = columnModel.getColumnIndexAtX(p.x);
                  int realIndex = columnModel.getColumn(index).getModelIndex();
                  return getHeaderToolTip(realIndex);
               }
            };
         }
      };
      table.addMouseListener( new OutputPanel_mouseAdapter( this ) );
      table.getTableHeader().setReorderingAllowed( false );

      OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
      JMenuItem menuItem = new JMenuItem("Modify this class (Step 1 of 3)");
      menuItem.addActionListener(new OutputPanel_PopupMenu_actionAdapter(this));
      popup.add(menuItem);
      MouseListener popupListener = new OutputPanel_PopupListener(popup);
      table.addMouseListener(popupListener);
   }

   void table_mouseReleased( MouseEvent e ) {
      int i = table.getSelectedRow();
      int j = table.getSelectedColumn();
      if(table.getValueAt(i,j) != null && j>=OutputTableModel.init_cols)
      {
         int runnum=(int)Math.floor((j - OutputTableModel.init_cols) / OutputTableModel.cols_per_run);
         String id = (String)table.getValueAt(i,0);
         ((classPvalRun)results.get(runnum)).showDetails(id);
      }
   }

   void popupMenu_actionPerformed(ActionEvent e) {
      OutputPanelPopupMenu sourcePopup = (OutputPanelPopupMenu)
          ((Container) e.getSource()).getParent();
      int r = table.rowAtPoint(sourcePopup.getPoint());
      String id = (String) table.getValueAt(r, 0);
      ClassWizard cwiz = new ClassWizard(callingframe, geneData, goData, id);
      cwiz.showWizard();
   }

   String getHeaderToolTip(int index)
   {
      if(index>=OutputTableModel.init_cols)
      {
         int runnum=(int)Math.floor((index - OutputTableModel.init_cols) / OutputTableModel.cols_per_run);
         return (String) resultToolTips.get(runnum);
      }
      else
         return null;
   }

   void generateToolTip(int runnum)
   {
      Settings runSettings=((classPvalRun)results.get(runnum)).getSettings();
      String tooltip = new String("<html>");
      String coda = new String();
      if(runSettings.getAnalysisMethod()==Settings.ORA)
      {
         tooltip += "ORA Analysis<br>";
         coda += "P value threshold: " + runSettings.getPValThreshold();
      }
      else if(runSettings.getAnalysisMethod()==Settings.RESAMP)
      {
         tooltip += "Resampling Analysis<br>";
         coda += runSettings.getIterations() + " iterations<br>";
         coda += "Using score column: " + runSettings.getScorecol();
      }
      else if(runSettings.getAnalysisMethod()==Settings.CORR)
      {
         tooltip += "Correlation Analysis<br>";
         coda += runSettings.getIterations() + " iterations";
      }
         tooltip += new String("Max set size: " + runSettings.getMaxClassSize() + "<br>" +
                            "Min set size: " + runSettings.getMinClassSize() + "<br>");
      if(runSettings.getDoLog())
         tooltip+="Log normalized<br>";
      if(runSettings.getGeneRepTreatment()==Settings.MEAN_PVAL)
         tooltip+="Gene Rep Treatment: Mean <br>";
      else if(runSettings.getGeneRepTreatment()==Settings.BEST_PVAL)
         tooltip+="Gene Rep Treatment: Best <br>";
      if(runSettings.getRawScoreMethod()==Settings.MEAN_METHOD)
         tooltip+="Class Raw Score Method: Mean <br>";
      else if(runSettings.getRawScoreMethod()==Settings.QUANTILE_METHOD)
         tooltip+="Class Raw Score Method: Median <br>";
      tooltip+=coda;
      resultToolTips.add(runnum,tooltip);
   }

   public void addInitialData(GeneAnnotations geneData, GONames goData) {
      this.geneData=geneData;
      this.goData=goData;
      model.addInitialData(geneData, goData);
      TableSorter sorter = new TableSorter(model);
      table.setModel(sorter);
      sorter.setTableHeader(table.getTableHeader());
      this.getViewport().add(table, null);
      table.getColumnModel().getColumn(0).setPreferredWidth(30);
      table.getColumnModel().getColumn(2).setPreferredWidth(30);
      table.getColumnModel().getColumn(3).setPreferredWidth(30);
      table.setDefaultRenderer(Object.class,new OutputPanelTableCellRenderer(goData));
      table.revalidate();
   }

   public void addRun() {
      model.addRun();
      table.addColumn(new TableColumn(model.getColumnCount() - 3));
      table.addColumn(new TableColumn(model.getColumnCount() - 2));
      table.addColumn(new TableColumn(model.getColumnCount() - 1));
      table.getColumnModel().getColumn(model.getColumnCount() - 3).setPreferredWidth(30);
      table.getColumnModel().getColumn(model.getColumnCount() - 2).setPreferredWidth(30);
      table.getColumnModel().getColumn(model.getColumnCount() - 1).setPreferredWidth(30);
      generateToolTip(((model.getColumnCount()-OutputTableModel.init_cols)/OutputTableModel.cols_per_run)-1);
      table.revalidate();
   }
}

class OutputPanel_mouseAdapter
    extends java.awt.event.MouseAdapter {
   OutputPanel adaptee;

   OutputPanel_mouseAdapter( OutputPanel adaptee ) {
      this.adaptee = adaptee;
   }

   public void mouseReleased( MouseEvent e ) {
      if ( e.getClickCount() < 2 ) {
         return;
      }
      adaptee.table_mouseReleased( e );
   }
}

class OutputPanel_PopupMenu_actionAdapter implements java.awt.event.ActionListener {
   OutputPanel adaptee;

   OutputPanel_PopupMenu_actionAdapter(OutputPanel adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.popupMenu_actionPerformed(e);
   }
}


class OutputPanelPopupMenu extends JPopupMenu {
   Point popupPoint;
   public Point getPoint() {return popupPoint;
   }

   public void setPoint(Point point) {popupPoint = point;
   }
}

class OutputPanel_PopupListener extends MouseAdapter {
   OutputPanelPopupMenu popup;
   OutputPanel_PopupListener(OutputPanelPopupMenu popupMenu) {popup = popupMenu;
   }

   public void mousePressed(MouseEvent e) {maybeShowPopup(e);
   }

   public void mouseReleased(MouseEvent e) {maybeShowPopup(e);
   }

   private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
         JTable source = (JTable) e.getSource();
         int r = source.rowAtPoint(e.getPoint());
         String id = (String) source.getValueAt(r, 0);
         //if (id.compareTo("") != 0) {
         if (id != null) {
            popup.show(e.getComponent(), e.getX(), e.getY());
            popup.setPoint(e.getPoint());
         }
      }
   }
}

class OutputTableModel extends AbstractTableModel {
   GeneAnnotations geneData;
   GONames goData;
   Vector results;
   Vector columnNames = new Vector();
   private NumberFormat nf = NumberFormat.getInstance();
   int state = -1;
   public static final int init_cols = 4;
   public static final int cols_per_run = 3;

   public OutputTableModel(Vector results) {
      this.results=results;
      nf.setMaximumFractionDigits(2);
      columnNames.add("Name");
      columnNames.add("Description");
      columnNames.add("# of Probes");
      columnNames.add("# of Genes");
   }

   public void addInitialData(GeneAnnotations geneData, GONames goData) {
      state = 0;
      this.geneData = geneData;
      this.goData = goData;
   }

   public void addRunData(Map result) {
      state++;
      columnNames.add("Run " + state + " Rank");
      columnNames.add("Run " + state + " Score");
      columnNames.add("Run " + state + " Pval");
      results.add(result);
   }

   public void addRun() {
      state++;
      columnNames.add("Run " + state + " Rank");
      columnNames.add("Run " + state + " Score");
      columnNames.add("Run " + state + " Pval");
   }

   public String getColumnName(int i) {return (String) columnNames.get(i);
   }

   public int getColumnCount() {return columnNames.size();
   }

   public int getRowCount() {
      if (state == -1) {
         return 20;
      } else {
         return geneData.numClasses();
      }
   }

   public Object getValueAt(int i, int j) {
      if (state >= 0 && j < init_cols) {
         String classid;
            classid = geneData.getClass(i);
         switch (j) {
         case 0:
            return classid;
         case 1:
               return goData.getNameForId(classid);
         case 2:
               return new Integer(geneData.numProbes(classid));
         case 3:
               return new Integer(geneData.numGenes(classid));
         }
      } else if (state > 0) {
         String classid;
         classid = geneData.getClass(i);
         double runnum = Math.floor((j - init_cols) / cols_per_run);
         Map data = ((classPvalRun)results.get((int) runnum)).getResults();
         if (data.containsKey(classid)) {
            classresult res = (classresult) data.get(classid);
            if ((j - init_cols) % cols_per_run == 0) {
               //return new Integer(res.getRank());
               return new Integer(res.getRank());
            } else if ((j - init_cols) % cols_per_run == 1) {
               return new Double(nf.format(res.getScore()));
            } else {
               return new Double(nf.format(res.getPvalue()));
            }
         } else {
            return null;
         }
      }
      return null;
   }
}


class OutputPanelTableCellRenderer extends DefaultTableCellRenderer
{
   GONames goData;
   static Color spread1 = new Color(220,220,160);
   static Color spread2 = new Color(205,222,180);
   static Color spread3 = new Color(190,224,200);
   static Color spread4 = new Color(175,226,220);
   static Color spread5 = new Color(160,228,240);
   static Color modified = new Color(220,160,220);

   public OutputPanelTableCellRenderer(GONames goData)
   {
      super();
      this.goData=goData;
   }

   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                  boolean hasFocus, int row, int column)
   {
      super.getTableCellRendererComponent(table, value, isSelected,
                                          hasFocus, row, column);
      int runcol = column - OutputTableModel.init_cols;
      setOpaque(true);
      if(isSelected || hasFocus)
         setOpaque(true);
      else if(value==null)
         setOpaque(false);
      else if(column == 0 && goData.newSet((String)value))
      {
         setBackground(modified);
      }
      else if(runcol % OutputTableModel.cols_per_run == 2 && value.getClass().equals(Double.class))
      {
         if(((Double)value).doubleValue() > 0.8)
            setBackground(spread1);
         else if(((Double)value).doubleValue() > 0.6)
            setBackground(spread2);
         else if(((Double)value).doubleValue() > 0.4)
            setBackground(spread3);
         else if(((Double)value).doubleValue() > 0.2)
            setBackground(spread4);
         else
            setBackground(spread5);
      }
      else
         setOpaque(false);
      return this;
   }

}

