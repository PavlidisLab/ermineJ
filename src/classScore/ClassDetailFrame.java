package classScore;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Map;
import java.util.ArrayList;
import java.text.NumberFormat;

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

   public ClassDetailFrame( 
     ArrayList values, 
     Map pvals, 
     Map classToProbe, 
     String id, 
     NumberFormat nf,
     GeneDataReader geneData ) 
   {
      try {
         jbInit();
         createDetailsTable( values, pvals, classToProbe, id, nf, geneData );
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

      // add a viewport with a table inside it
      this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
      jScrollPane1.getViewport().add(jTable1, null);
   }

   private void createDetailsTable( 
        ArrayList values, 
        Map pvals, 
        Map classToProbe, 
        String id, 
        NumberFormat nf,
        GeneDataReader geneData ) 
   {

      DetailsTableModel m = new DetailsTableModel( values, pvals, classToProbe, id, nf, geneData );
      
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
      
//      // Install the custom renderer on the first visible column
//      int vColIndex = 0;
//      TableColumn col = jTable1.getColumnModel().getColumn(vColIndex);
//      col.setCellRenderer( new MatrixDisplayCellRenderer() );      
   }
}

class DetailsTableModel extends AbstractTableModel {

   private ArrayList m_values;
   private Map m_pvals;
   private Map m_classToProbe;
   private String m_id;
   private NumberFormat m_nf;
   private GeneDataReader m_geneData;
   private String[] m_columnNames = { "Probe", "P value", "Name", "Description" };

   /** constructor */
   public DetailsTableModel( 
     ArrayList values, 
     Map pvals, 
     Map classToProbe, 
     String id, 
     NumberFormat nf,
     GeneDataReader geneData ) {
      
      m_values = values;
      m_pvals = pvals;
      m_classToProbe = classToProbe;
      m_id = id;
      m_nf = nf;
      m_geneData = geneData;
   }
   
   public String getColumnName(int i) {
      return m_columnNames[i];
   }

   public int getRowCount() {
      return m_values.size();
   }

   public int getColumnCount() {
      return 4;
   }

   public Object getValueAt(int i, int j) {
      
      switch (j) {
         case 0:
            return (String) ( (ArrayList) m_classToProbe.get(m_id)).get(i);
         case 1:
            return new Double(m_nf.format(m_pvals.get( (String) ( (ArrayList)
                m_classToProbe.get(m_id)).get(i))));
         case 2:
            return m_geneData.getProbeGeneName( (String) ( (ArrayList)
                m_classToProbe.get(m_id)).get(i));
         case 3:
            return m_geneData.getProbeDescription( (String) ( (ArrayList)
                m_classToProbe.get(m_id)).get(i));
         default:
            return "";
      }
   }
} // end class DetailsTableModel


// This renderer extends a component. It is used each time a
// cell must be displayed.
class MatrixDisplayCellRenderer extends JLabel implements TableCellRenderer {
   
     public MatrixDisplayCellRenderer() {
        
        setOpaque( true );
     }
   
     // This method is called each time a cell in a column
     // using this renderer needs to be rendered.
     public Component getTableCellRendererComponent(JTable table, Object value,
             boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
         // 'value' is value contained in the cell located at
         // (rowIndex, vColIndex)

         if (isSelected) {
             // cell (and perhaps other cells) are selected
         }

         if (hasFocus) {
             // this cell is the anchor and the table has the focus
         }

         // Configure the component with the specified value
         //setText(value.toString());

         // Set tool tip if desired
         setToolTipText((String)value);
         setBackground( Color.red );

         // Since the renderer is a component, return itself
         return this;
     }

     // The following methods override the defaults for performance reasons
     public void validate() {}
     public void revalidate() {}
     protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
     public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
 }

