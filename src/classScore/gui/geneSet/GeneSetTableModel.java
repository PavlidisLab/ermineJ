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
public class GeneSetTableModel extends AbstractTableModel {

   private JMatrixDisplay m_matrixDisplay;
   private ArrayList m_probeIDs;
   private Map m_pvalues;
   private Map m_pvaluesOrdinalPosition;
   private GeneAnnotations m_geneData;
   private DecimalFormat m_nf;
   private String[] m_columnNames = {
       "Probe", "P value", "P value", "Name", "Description"};

   /** constructor */
       public GeneSetTableModel(JMatrixDisplay matrixDisplay, ArrayList probeIDs, Map pvalues, Map pvaluesOrdinalPosition, GeneAnnotations geneData, DecimalFormat nf) {

      m_matrixDisplay = matrixDisplay;
      m_probeIDs = probeIDs;
      m_pvalues = pvalues;
      m_pvaluesOrdinalPosition = pvaluesOrdinalPosition;
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
            // probe ID
            return probeID;
         case 1:
            // p value
            return m_pvalues == null ? new Double( Double.NaN ) :
                new Double( m_nf.format( m_pvalues.get( probeID ) ) );
         case 2:
            // p value bar
            if ( m_pvalues == null ) {
               return null; 
            }
            else {
               double[] values = new double[2];
               // actual p value
               values[0] = Double.valueOf( m_pvalues.get( probeID ).toString() ).doubleValue();
               // expected p value
               int position = ( ( Integer ) m_pvaluesOrdinalPosition.get( probeID ) ).intValue();
               values[1] = 1.0f / getRowCount() * position;
               return values;
            }
         case 3:
            // gene namne
            return m_geneData == null ? "" :
                m_geneData.getProbeGeneName( probeID );
         case 4:
            // description
            return m_geneData == null ? "" :
                m_geneData.getProbeDescription( probeID );
         default:
            return "";
         }
   } // end getValueAt

} // end class DetailsTableModel
