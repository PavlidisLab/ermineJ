package classScore;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Map;
import java.util.ArrayList;
import java.text.NumberFormat;
import baseCode.gui.JMatrixDisplay;

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
      
	//
	// Create a matrix display
	//
	JMatrixDisplay matrixDisplay = null;
	String filename = "C:\\GO_0005853_partial.txt"; //GO_0005853.txt";
	try {
	    matrixDisplay = new JMatrixDisplay( filename );
	} catch (java.io.IOException e) {
	    System.err.println( "IOException: wrong filename for JMatrixDisplay");
	}
	matrixDisplay.setStandardizedEnabled( true );
      
	//
	// Create the rest of the table
	//
	DetailsTableModel m = new DetailsTableModel( matrixDisplay, values, pvals, classToProbe, id, nf, geneData );
	SortFilterModel sorter = new SortFilterModel( m );
	jTable1.setModel( sorter );

	jTable1.getTableHeader().addMouseListener(new MouseAdapter() {
		public void mouseClicked( MouseEvent event ) {
		    int tableColumn = jTable1.columnAtPoint( event.getPoint() );
		    int modelColumn = jTable1.convertColumnIndexToModel( tableColumn );
		    ( (SortFilterModel) jTable1.getModel() ).sort( modelColumn );
		}
	    });
      
	// Make the columns in the matrix display not too wide (cell-size)
	// and set a custom cell renderer
	MatrixDisplayCellRenderer cellRenderer = new MatrixDisplayCellRenderer( matrixDisplay );
	int matrixColumnCount = matrixDisplay.getColumnCount();
	for (int i = 0;  i < matrixColumnCount;  i++)
	    {
		jTable1.getColumnModel().getColumn( i ).setPreferredWidth( 10 );
		jTable1.getColumnModel().getColumn( i ).setMaxWidth( 10 );
		jTable1.getColumnModel().getColumn( i ).setMinWidth( 10 );
		jTable1.getColumnModel().getColumn( i ).setResizable( false );
		TableColumn col = jTable1.getColumnModel().getColumn( i );
		col.setCellRenderer( cellRenderer );
	    }

	// The columns containing text or values (not matrix display) should be a bit wider
	jTable1.getColumnModel().getColumn( matrixColumnCount + 1 ).setPreferredWidth(75);
	jTable1.getColumnModel().getColumn( matrixColumnCount + 2 ).setPreferredWidth(125);
	jTable1.getColumnModel().getColumn( matrixColumnCount + 3 ).setPreferredWidth(300);
      
    }
}

class DetailsTableModel extends AbstractTableModel {

    private JMatrixDisplay m_matrixDisplay;
    private ArrayList m_values;
    private Map m_pvals;
    private Map m_classToProbe;
    private String m_id;
    private NumberFormat m_nf;
    private GeneDataReader m_geneData;
    private String[] m_columnNames = { "Probe", "P value", "Name", "Description" };

    /** constructor */
    public DetailsTableModel( 
			     JMatrixDisplay matrixDisplay,
			     ArrayList values,
			     Map pvals,
			     Map classToProbe,
			     String id,
			     NumberFormat nf,
			     GeneDataReader geneData ) {
      
	m_matrixDisplay = matrixDisplay;
	m_values = values;
	m_pvals = pvals;
	m_classToProbe = classToProbe;
	m_id = id;
	m_nf = nf;
	m_geneData = geneData;
    }
   
    public String getColumnName( int column ) {
      
	int offset = m_matrixDisplay.getColumnCount(); // matrix display ends
      
	if (column < offset)
	    return ""; //m_matrixDisplay.getColumnName( column );
	else
	    return m_columnNames[ column - offset ];
    }

    public int getRowCount() {
	return m_values.size();
    }

    public int getColumnCount() {
	return m_columnNames.length + m_matrixDisplay.getColumnCount();
    }

    public Object getValueAt( int row, int column ) {

	int offset = m_matrixDisplay.getColumnCount(); // matrix display ends
      
	if (column < offset)
	    return new Point( row, column ); // coords into JMatrixDisplay
	else
	    column -= offset;

	switch( column ) // after it's been offset
	    {
	    case 0:
		return (String) ( (ArrayList) m_classToProbe.get(m_id)).get(row);
	    case 1:
		return new Double(m_nf.format(m_pvals.get( (String) ( (ArrayList)
								      m_classToProbe.get(m_id)).get(row))));
	    case 2:
		return m_geneData.getProbeGeneName( (String) ( (ArrayList)
							       m_classToProbe.get(m_id)).get(row));
	    case 3:
		return m_geneData.getProbeDescription( (String) ( (ArrayList)
								  m_classToProbe.get(m_id)).get(row));
	    default:
		return "";
	    }
      
    } // end getValueAt
   
} // end class DetailsTableModel


// This renderer extends a component. It is used each time a
// cell must be displayed.
class MatrixDisplayCellRenderer extends JLabel implements TableCellRenderer {
   
    JMatrixDisplay m_matrixDisplay;
   
    public MatrixDisplayCellRenderer( JMatrixDisplay matrixDisplay ) {

	m_matrixDisplay = matrixDisplay;
	setOpaque( true );
    }
   
    // This method is called each time a cell in a column
    // using this renderer needs to be rendered.
    public Component getTableCellRendererComponent(
						   JTable table, 
						   Object tableCellValue,
						   boolean isSelected, 
						   boolean hasFocus, 
						   int displayedRow, 
						   int displayedColumn) 
    {
	// 'value' is value contained in the cell located at
	// (rowIndex, vColIndex)

	if (isSelected) {
	    // cell (and perhaps other cells) are selected
	}

	if (hasFocus) {
	    // this cell is the anchor and the table has the focus
	}

	Point coords = (Point) tableCellValue;
	int row    = coords.x;
	int column = coords.y;
	double matrixValue = m_matrixDisplay.getValue( row, column );
	Color matrixColor = m_matrixDisplay.getColor( row, column );
            
	// Configure the component with the specified value
	setBackground( matrixColor );

	// Set tool tip if desired
	setToolTipText( "" + matrixValue );

	// Since the renderer is a component, return itself
	return this;
    }

    // The following methods override the defaults for performance reasons
    //public void validate() {}
    //public void revalidate() {}
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

} // end class MatrixDisplayCellRenderer
