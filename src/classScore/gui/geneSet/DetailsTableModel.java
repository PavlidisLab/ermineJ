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
   private ArrayList m_probeIDs;
   private Map m_pvalues;
   private GeneAnnotations m_geneData;
   private DecimalFormat m_nf;
   private String[] m_columnNames = {
       "Probe", "P value", "Name", "Description"};

   /** constructor */
   public DetailsTableModel(
       JMatrixDisplay matrixDisplay,
       ArrayList probeIDs,
       Map pvalues,
       GeneAnnotations geneData,
       DecimalFormat nf ) {

      m_matrixDisplay = matrixDisplay;
      m_probeIDs = probeIDs;
      m_pvalues = pvalues;
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
      return m_probeIDs.size();
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

      // get the probeID for the current row
      String probeID = ( String ) m_probeIDs.get( row );

      switch ( column ) { // after it's been offset
         case 0:
            return probeID;
         case 1:
            return m_pvalues == null ? new Double( Double.NaN ) :
                new Double( m_nf.format( m_pvalues.get( probeID ) ) );
         case 2:
            return m_geneData == null ? "" :
                m_geneData.getProbeGeneName( probeID );
         case 3:
            return m_geneData == null ? "" :
                m_geneData.getProbeDescription( probeID );
         default:
            return "";
         }
   } // end getValueAt

} // end class DetailsTableModel
