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
 * @todo show how many rows there are in the table (I think this is done classPvalRun, line 906)
* @todo add a graphical display of the data
 */

public class ClassDetailFrame
    extends JFrame {
   JScrollPane jScrollPane1 = new JScrollPane();
   JTable jTable1 = new JTable();

   public ClassDetailFrame() {
      try {
         jbInit();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {
      this.setSize(500, 460);
      this.setLocation(200, 100);
      this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      jTable1.setGridColor(Color.lightGray);
      this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
      jScrollPane1.getViewport().add(jTable1, null);
   }

   public void setModel(TableModel m) {
      SortFilterModel sorter = new SortFilterModel(m);
      jTable1.setModel(sorter);
      jTable1.getTableHeader().addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent event) {
            int tableColumn = jTable1.columnAtPoint(event.getPoint());
            int modelColumn = jTable1.convertColumnIndexToModel(tableColumn);
            ( (SortFilterModel) jTable1.getModel()).sort(modelColumn);
         }
      });

      jTable1.getColumnModel().getColumn(1).setPreferredWidth(75);
      jTable1.getColumnModel().getColumn(2).setPreferredWidth(125);
      jTable1.getColumnModel().getColumn(3).setPreferredWidth(300);
   }

}
