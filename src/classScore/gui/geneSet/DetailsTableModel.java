package classScore.gui.geneSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import java.awt.Point;
import javax.swing.table.AbstractTableModel;

import baseCode.gui.JMatrixDisplay;
import classScore.data.*;

/**
 * Our table model.
 * <p>
 * The general picture is as follows:<br>
 * GUI -> Sort Filter -> Table Model
 *
 * @author  Will Braynen
 * @version $Id$
 */
public class DetailsTableModel
    extends AbstractTableModel {

   private JMatrixDisplay m_matrixDisplay;
   private Double[] m_pvalues;
   private String[] m_probes;
   private String[] m_geneNames;
   private String[] m_probeDescriptions;
   private DecimalFormat m_nf;
   private String[] m_columnNames = {
       "Probe", "P value", "Name", "Description"};

   /** constructor */
   public DetailsTableModel(
       JMatrixDisplay matrixDisplay,
       Double[] pvalues,
       String[] probes,
       String[] geneNames,
       String[] probeDescriptions,
       DecimalFormat nf ) {

      m_matrixDisplay = matrixDisplay;
      m_pvalues = pvalues;
      m_probes = probes;
      m_geneNames = geneNames;
      m_probeDescriptions = probeDescriptions;
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
      return m_probes.length;
   }

   public int getColumnCount() {
      return m_columnNames.length + m_matrixDisplay.getColumnCount();
   }

   public Object getValueAt( int row, int column ) {

      try {
         int offset = m_matrixDisplay.getColumnCount(); // matrix display ends

         if ( column < offset ) {
            return new Point( row, column ); // coords into JMatrixDisplay
         } else {
            column -= offset;

         }
         switch ( column ) { // after it's been offset
            case 0:
               return m_probes[row];
            case 1:
               // Must return a Double rather than a String so that the sorter
               // filter would sort the table correctly by this column
               return new Double( m_nf.format( m_pvalues[row] ) );
            case 2:
               return m_geneNames[row];
            case 3:
               return m_probeDescriptions[row];
            default:
               return "";
         }
      } catch( Exception e ) {
         return "";
      }

   } // end getValueAt

} // end class DetailsTableModel
