package classScore;

import java.util.*;
import javax.swing.table.*;
import baseCode.gui.JMatrixDisplay;
import java.awt.Point;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SortFilterModel extends AbstractTableModel {

   private TableModel model;
   private int sortColumn;
   private Row[] rows;
   private JMatrixDisplay m_matrixDisplay;

   public SortFilterModel( TableModel m ) {

      this( m, null );
   }

   public SortFilterModel( TableModel m, JMatrixDisplay matrixDisplay ) {

      m_matrixDisplay = matrixDisplay;

      model = m;
      rows = new Row[model.getRowCount()];

      // Initially, underlying row order is the same as displayed row order
      for ( int i = 0; i < rows.length; i++ ) {
         rows[i] = new Row();
         rows[i].index = i;
      }
   }

   public void sort( int c ) {
      sortColumn = c;
      Arrays.sort( rows );
      //     fireTableDataChanged();
   }

   /**
    * Translate from the row order that is displayed
    * to the underlying row order.
    */
   public Object getValueAt( int r, int c ) {
      return model.getValueAt( rows[r].index, c );
   }

   public int getRowCount() {
      return model.getRowCount();
   }

   public int getColumnCount() {
      return model.getColumnCount();
   }

   public String getColumnName( int c ) {
      return model.getColumnName( c );
   }

   private class Row implements Comparable {

      public int index;
      public int compareTo( Object other ) {
         Row otherRow = ( Row )other;
         Object a = model.getValueAt( index, sortColumn );
         Object b = model.getValueAt( otherRow.index, sortColumn );

         // If sortColumn is in the matrix display, then model.getValueAt()
         // returns a Point object that represents a coordinate into the
         // display matrix.  This is done so that the display matrix object
         // can be asked for both the color and the value.  We are here only
         // interested in the value.
         if (m_matrixDisplay != null  &&  sortColumn < m_matrixDisplay.getColumnCount()) {

            Point p1 = (Point) a;
            Point p2 = (Point) b;

            a = new Double( m_matrixDisplay.getValue( p1.x, p1.y ));
            b = new Double( m_matrixDisplay.getValue( p2.x, p2.y ));
         }

         if ( a instanceof Comparable ) {
            return ( ( Comparable )a ).compareTo( b );
         } else {
            return a.toString().compareTo( b.toString() );
         }
      }
   }
}
