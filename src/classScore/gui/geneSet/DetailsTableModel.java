package classScore.gui.geneSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import java.awt.Point;
import javax.swing.table.AbstractTableModel;

import baseCode.gui.JMatrixDisplay;
import classScore.GeneAnnotations;

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
   private Map m_pvals;
   private Map m_classToProbe;
   private String m_classID;
   private DecimalFormat m_nf;
   private GeneAnnotations m_geneData;
   private String[] m_columnNames = {
       "Probe", "P value", "Name", "Description"};

   /** constructor */
   public DetailsTableModel(
       JMatrixDisplay matrixDisplay,
       Map pvals,
       Map classToProbe,
       String classID,
       GeneAnnotations geneData,
       DecimalFormat nf ) {

      m_matrixDisplay = matrixDisplay;
      m_pvals = pvals;
      m_classToProbe = classToProbe;
      m_classID = classID;
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
      return (( ArrayList ) m_classToProbe.get( m_classID )).size();
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
            return ( String ) ( ( ArrayList ) m_classToProbe.get( m_classID ) ).get( row ); // probe ID
         case 1:
            return new Double( m_nf.format( m_pvals.get( ( String ) ( ( ArrayList )
                m_classToProbe.get( m_classID ) ).get( row ) ) ) );
         case 2:
            return m_geneData.getProbeGeneName( ( String ) ( ( ArrayList )
                m_classToProbe.get( m_classID ) ).get( row ) );
         case 3:
            return m_geneData.getProbeDescription( ( String ) ( ( ArrayList )
                m_classToProbe.get( m_classID ) ).get( row ) );
         default:
            return "";
      }

   } // end getValueAt

} // end class DetailsTableModel
