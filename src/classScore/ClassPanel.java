package classScore;

import java.awt.*;
import java.awt.event.*;
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

public class ClassPanel extends JScrollPane
{
   JTable jTable1 = new JTable();
   SetupMaps dataHolder = null;
   // EventListenerList listenerList = null;

   public ClassPanel()
   {
      try
      {
         //     listenerList = new EventListenerList();
         jbInit();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   public ClassPanel(SetupMaps dataHolder) {
      try {
         //     listenerList = new EventListenerList();
         this.dataHolder = dataHolder;
         jbInit();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   // public void addClassDetailsListener(ClassDetailsEventListener listener) {
   //   listenerList.add(ClassDetailsEventListener.class, listener);
   // }

   private void jbInit() throws Exception {
      jTable1.addMouseListener(new ClassPanel_jTable1_mouseAdapter(this));
      jTable1.setGridColor(Color.lightGray);
      jTable1.setRowSelectionAllowed(true);
      this.getViewport().add(jTable1, null);
   }

   public void setModel(TableModel m) {
      //jTable1.setModel(m);
      SortFilterModel sorter = new SortFilterModel(m);
      jTable1.setModel(sorter);
      jTable1.getTableHeader().addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent event) {
            int tableColumn = jTable1.columnAtPoint(event.getPoint());
            int modelColumn = jTable1.convertColumnIndexToModel(tableColumn);
            if(modelColumn == 0 || modelColumn == 2)
               ( (SortFilterModel) jTable1.getModel()).sort(modelColumn);
         }
      });

      jTable1.getColumnModel().getColumn(0).setPreferredWidth(70);
      jTable1.getColumnModel().getColumn(1).setPreferredWidth(200);
   }

   void jTable1_mouseReleased(MouseEvent e) {
      int j = jTable1.getSelectedRow();
      //   System.err.println(j);

      //dataHolder.showDetails(j); FIX THIS!!!

      //  EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
      //  ClassDetailsEvent event = new ClassDetailsEvent(this);
      //  event.setDataSource(dataHolder);
      //  queue.postEvent(event);
   }


}

class ClassPanel_jTable1_mouseAdapter
    extends java.awt.event.MouseAdapter {
   ClassPanel adaptee;

   ClassPanel_jTable1_mouseAdapter(ClassPanel adaptee) {
      this.adaptee = adaptee;
   }

   public void mouseReleased(MouseEvent e) {
      if (e.getClickCount() < 2) {
         return;
      }
      adaptee.jTable1_mouseReleased(e);
   }
}
