/*
 * blah.java
 *
 * Created on June 19, 2004, 12:22 AM
 */

package classScore.details;

import javax.swing.table.AbstractTableModel;
import baseCode.gui.JMatrixDisplay;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Map;
import java.text.DecimalFormat;
import classScore.GeneAnnotations;

/**
 * Our table model.
 * <p>
 * The general picture is as follows:<br>
 * GUI -> Sort Filter -> Table Model
 */
public class DetailsTableModel
    extends AbstractTableModel {

   private JMatrixDisplay m_matrixDisplay;
   private ArrayList m_values;
   private Map m_pvals;
   private Map m_classToProbe;
   private String m_id;
   private DecimalFormat m_nf;
   private GeneAnnotations m_geneData;
   private String[] m_columnNames = {
       "Probe", "P value", "Name", "Description"};

   /** constructor */
   public DetailsTableModel(
       JMatrixDisplay matrixDisplay,
       ArrayList values,
       Map pvals,
       Map classToProbe,
       String id,
       GeneAnnotations geneData,
       DecimalFormat nf ) {

      m_matrixDisplay = matrixDisplay;
      m_values = values;
      m_pvals = pvals;
      m_classToProbe = classToProbe;
      m_id = id;
      m_geneData = geneData;
      m_nf = nf;
   }

   public String getColumnName( int column ) {

      int offset = m_matrixDisplay.getColumnCount(); // matrix display ends

      if ( column < offset ) {
         return m_matrixDisplay.getColumnName( column );
      } else {
         return m_columnNames[column - offset];
      }
   } // end getColumnName

   public int getRowCount() {
      return m_values.size();
   }

   public int getColumnCount() {
      return m_columnNames.length + m_matrixDisplay.getColumnCount();
   }

   public Object getValueAt( int row, int column ) {

      int offset = m_matrixDisplay.getColumnCount(); // matrix display ends

      if ( column < offset ) {
         return new Point( row, column ); // coords into JMatrixDisplay
      } else {
         column -= offset;

      }
      switch ( column ) { // after it's been offset
         case 0:
            return ( String ) ( ( ArrayList ) m_classToProbe.get( m_id ) ).get( row );
         case 1:
            return new Double( m_nf.format( m_pvals.get( ( String ) ( ( ArrayList )
                m_classToProbe.get( m_id ) ).get( row ) ) ) );
         case 2:
            return m_geneData.getProbeGeneName( ( String ) ( ( ArrayList )
                m_classToProbe.get( m_id ) ).get( row ) );
         case 3:
            return m_geneData.getProbeDescription( ( String ) ( ( ArrayList )
                m_classToProbe.get( m_id ) ).get( row ) );
         default:
            return "";
      }

   } // end getValueAt

} // end class DetailsTableModel
