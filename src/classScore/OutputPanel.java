package classScore;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo make columns start out better sizes
 */

public class OutputPanel extends JScrollPane {
   JTable table;
   OutputTableModel model;

   public OutputPanel() {
      try {
         model = new OutputTableModel();
         table = new JTable(model);
         this.getViewport().add(table, null);
         table.getColumnModel().getColumn(0).setPreferredWidth(70);
         table.getColumnModel().getColumn(2).setPreferredWidth(50);
         table.getColumnModel().getColumn(3).setPreferredWidth(50);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   // public void addClassDetailsListener(ClassDetailsEventListener listener) {
   //   listenerList.add(ClassDetailsEventListener.class, listener);
   // }
   public void addInitialClassData(InitialMaps data)
   { model.addInitialClassData(data); }
   public void addRunData(classPvalRun data)
   { model.addRunData(data); }
}

class OutputTableModel extends AbstractTableModel
{
   InitialMaps imaps;
   ArrayList results=new ArrayList();
   ArrayList columnNames = new ArrayList();
   int state = -1;

   public OutputTableModel()
   {
      columnNames.add("Name");
      columnNames.add("Description");
      columnNames.add("# of Probes");
      columnNames.add("# of Genes");
   }

   public void addInitialClassData(InitialMaps imaps)
   {
      this.imaps=imaps;
      state=0;
   }

   public void addRunData(classPvalRun result)
   {
      results.add(result);
      columnNames.add("Run " + state + " Rank");
      columnNames.add("Run " + state + " Pval");
      state++;
   }

   public String getColumnName(int i) { return (String)columnNames.get(i); }
   public int getColumnCount() { return columnNames.size(); }

   public int getRowCount()
   {
      switch(state)
      {
          case 0:
             return imaps.numClasses();
          default:
             return 20;
      }
   }

   public Object getValueAt(int i, int j)
   {
      if(state>=0 && j<=3)
      {
         String classid = imaps.getClass(i);
         switch (j)
         {
             case 0:
                return classid;
             case 1:
                return imaps.getClassDesc(classid);
             case 2:
                return Integer.toString(imaps.numProbes(classid));
             case 3:
                return Integer.toString(imaps.numGenes(classid));
         }
      }
      else if(state>0)
      {
         if(j%2 == 0)
            return "Run " + state + " Rank";
         else
            return "Run " + state + " Pval";
      }
      return "";
   }
}
