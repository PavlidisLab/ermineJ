package classScore;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.EventListenerList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo make columns start out better sizes
 */

public class ResultFrame
    extends JFrame {
  JScrollPane jScrollPane1 = new JScrollPane();
  JTable jTable1 = new JTable();
  classPvalRun dataHolder = null;
  // EventListenerList listenerList = null;

  public ResultFrame(classPvalRun dataHolder) {
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
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setSize(660, 460);
    this.setLocation(100, 100);
    this.setTitle("Results summary");
    jTable1.addMouseListener(new ResultFrame_jTable1_mouseAdapter(this));
    jTable1.setGridColor(Color.lightGray);
    jTable1.setRowSelectionAllowed(true);
    this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTable1, null);
  }

  public void setModel(TableModel m) {
    jTable1.setModel(m);
    jTable1.getColumnModel().getColumn(0).setPreferredWidth(40);
    jTable1.getColumnModel().getColumn(2).setPreferredWidth(200);
    jTable1.getColumnModel().getColumn(3).setPreferredWidth(30);
    jTable1.getColumnModel().getColumn(4).setPreferredWidth(30);
  }

  public static void main(String[] args) {
    JFrame frame = new ResultFrame(null);
    frame.show();
  }

  void jTable1_mouseReleased(MouseEvent e) {
    int j = jTable1.getSelectedRow();
    //   System.err.println(j);

    dataHolder.showDetails(j);

    //  EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    //  ClassDetailsEvent event = new ClassDetailsEvent(this);
    //  event.setDataSource(dataHolder);
    //  queue.postEvent(event);
  }

}

class ResultFrame_jTable1_mouseAdapter
    extends java.awt.event.MouseAdapter {
  ResultFrame adaptee;

  ResultFrame_jTable1_mouseAdapter(ResultFrame adaptee) {
    this.adaptee = adaptee;
  }

  public void mouseReleased(MouseEvent e) {
    if (e.getClickCount() < 2) {
      return;
    }
    adaptee.jTable1_mouseReleased(e);
  }
}