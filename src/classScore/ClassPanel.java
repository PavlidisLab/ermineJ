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
   classScoreFrame csFrame;

   public ClassPanel(classScoreFrame csFrame)
   {
      this.csFrame=csFrame;
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

      modclassPopupMenu popup = new modclassPopupMenu();
      JMenuItem menuItem = new JMenuItem("Modify this class (Step 1 of 3)");
      menuItem.addActionListener(new popupMenu_actionAdapter(this));
      popup.add(menuItem);
      MouseListener popupListener = new PopupListener(popup);
      jTable1.addMouseListener(popupListener);
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


   void popupMenu_actionPerformed(ActionEvent e)
   {
      modclassPopupMenu sourcePopup=(modclassPopupMenu)((Container)e.getSource()).getParent();
      int r =jTable1.rowAtPoint(sourcePopup.getPoint());
      String id=(String)jTable1.getValueAt(r,0);
      System.err.println("Doing some action: " + id);
      csFrame.makeModClassFrame(false,id);
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

   public void mouseClicked(MouseEvent e) {
//    if((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
//       adaptee.jTable1_mouseRightClicked(e);
   }

   public void mouseReleased(MouseEvent e) {
      if (e.getClickCount() < 2) {
         return;
      }
      adaptee.jTable1_mouseReleased(e);
   }
}

class PopupListener extends MouseAdapter {
   modclassPopupMenu popup;
   PopupListener(modclassPopupMenu popupMenu) { popup = popupMenu; }
   public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
   public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
   private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
         JTable source = (JTable) e.getSource();
         int r =source.rowAtPoint(e.getPoint());
         String id=(String)source.getValueAt(r,0);
         if(id.compareTo("")!=0)
         {
            popup.show(e.getComponent(), e.getX(), e.getY());
            popup.setPoint(e.getPoint());
         }
      }
   }
}

class popupMenu_actionAdapter implements java.awt.event.ActionListener {
   ClassPanel adaptee;

   popupMenu_actionAdapter(ClassPanel adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.popupMenu_actionPerformed(e);
   }
}

class modclassPopupMenu extends JPopupMenu
{
   Point popupPoint;
   public Point getPoint() {return popupPoint;}
   public void setPoint(Point point) {popupPoint=point;}
}
