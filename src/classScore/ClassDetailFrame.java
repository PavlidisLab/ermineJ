package classScore;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Map;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.io.IOException;
import java.io.File;

import baseCode.gui.JMatrixDisplay;
import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.reader.DoubleMatrixReader;
import baseCode.graphics.text.Util;


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

   public JMatrixDisplay m_matrixDisplay = null;

   protected JScrollPane m_tableScrollPane = new JScrollPane();
   protected JTable m_table = new JTable();
   protected BorderLayout borderLayout1 = new BorderLayout();
   protected JToolBar m_toolBar = new JToolBar();
   protected JToggleButton m_normalizeButton = new JToggleButton();
   protected final String[] m_normalizeButtonLabels = { "Normalize ON", "Normalize OFF" };
   protected JButton m_saveButton = new JButton();

   public ClassDetailFrame(
       ArrayList values,
       Map pvals,
       Map classToProbe,
       String id,
       NumberFormat nf,
       GeneDataReader geneData ) {
      try {
         createDetailsTable( values, pvals, classToProbe, id, nf, geneData );
         jbInit();

         boolean isNormalized = m_matrixDisplay.getStandardizedEnabled();
         m_normalizeButton.setText( isNormalized ? m_normalizeButtonLabels[0] : m_normalizeButtonLabels[1]  );
         m_normalizeButton.setSelected( isNormalized );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {
      this.setSize( 500, 460 );
      this.setLocation( 200, 100 );
      this.getContentPane().setLayout( borderLayout1 );
      this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );

      // Enable the horizontal scroll bar
      m_table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

      // Prevent user from moving tables around
      m_table.getTableHeader().setReorderingAllowed( false );

      // Make sure the matrix display doesn't have a grid separating color cells.
      m_table.setIntercellSpacing( new Dimension( 0, 0 ) );

      // The rest of the table (text and value) should have a light gray grid
      m_table.setGridColor( Color.lightGray );

      // add a viewport with a table inside it
      m_normalizeButton.setToolTipText("Normalize to variance 1, mean 0" );
      m_normalizeButton.setActionCommand( "jToggleButton1" );
      m_normalizeButton.setText("Normalize OFF" );
      m_normalizeButton.addActionListener( new ClassDetailFrame_m_normalizeButton_actionAdapter( this ) );
      m_saveButton.setToolTipText("Save to file");
      m_saveButton.setText("Save to file...");
      m_saveButton.addActionListener(new ClassDetailFrame_m_saveButton_actionAdapter(this));
      m_toolBar.setFloatable(false);
      m_tableScrollPane.getViewport().add( m_table, null );
      this.getContentPane().add( m_tableScrollPane, BorderLayout.CENTER );
      m_toolBar.add(m_saveButton, null);
      m_toolBar.add( m_normalizeButton, null );
      this.getContentPane().add( m_toolBar, BorderLayout.NORTH );
   }

   private void createDetailsTable(
       ArrayList values,
       Map pvals,
       Map classToProbe,
       String id,
       NumberFormat nf,
       GeneDataReader geneData ) {

      //
      // Create a matrix display
      //

      // compile the matrix data
      String filename = "C:\\melanoma_and_sarcomaMAS5.txt";
      String[] geneProbes = getProbes( classToProbe, id, values.size() );
      DoubleMatrixReader matrixReader = new DoubleMatrixReader();
      DenseDoubleMatrix2DNamed matrix = null;
      try {
         matrix = ( DenseDoubleMatrix2DNamed )matrixReader.read( filename, geneProbes );
      }
      catch ( IOException e ) {
         System.err.println( "IOException: wrong filename for MatrixReader" );
      }

      // create the matrix display
      m_matrixDisplay = new JMatrixDisplay( matrix );
      m_matrixDisplay.setStandardizedEnabled( true );

      //
      // Create the rest of the table
      //

      DetailsTableModel m = new DetailsTableModel(
       m_matrixDisplay, values, pvals, classToProbe, id, nf, geneData
       );
      SortFilterModel sorter = new SortFilterModel( m, m_matrixDisplay );
      m_table.setModel( new DefaultTableModel() ); // bug in JTable (Manju said so) -- if called repeatedly, this line should be here... as-is, makes no difference
      m_table.setModel( sorter );

      m_table.getTableHeader().addMouseListener( new MouseAdapter() {
         public void mouseClicked( MouseEvent event ) {
            int tableColumn = m_table.columnAtPoint( event.getPoint() );
            int modelColumn = m_table.convertColumnIndexToModel( tableColumn );
            ( ( SortFilterModel )m_table.getModel() ).sort( modelColumn );
         }
      } );

      // Make the columns in the matrix display not too wide (cell-size)
      // and set a custom cell renderer
      MatrixDisplayCellRenderer cellRenderer = new MatrixDisplayCellRenderer( m_matrixDisplay ); // create one instance that will be used to draw each cell
      MatrixDisplayColumnHeaderRenderer columnHeaderRenderer = new MatrixDisplayColumnHeaderRenderer(); // create only one instance
      int matrixColumnCount = m_matrixDisplay.getColumnCount();
      for ( int i = 0; i < matrixColumnCount; i++ ) {
         TableColumn col = m_table.getColumnModel().getColumn( i );
         //col.setResizable( false );
         col.setPreferredWidth( 10 );
         col.setMaxWidth( 10 );
         col.setMinWidth( 10 );
         col.setCellRenderer( cellRenderer );
         col.setHeaderRenderer( columnHeaderRenderer );
      }

      // The columns containing text or values (not matrix display) should be a bit wider
      m_table.getColumnModel().getColumn( matrixColumnCount + 1 ).setPreferredWidth( 75 );
      m_table.getColumnModel().getColumn( matrixColumnCount + 2 ).setPreferredWidth( 125 );
      m_table.getColumnModel().getColumn( matrixColumnCount + 3 ).setPreferredWidth( 300 );

   }

   protected String[] getProbes( Map classToProbe, String id, int count ) {

      // Compile a list of gene probe ID's in this probe class
      String[] probes = new String[count];
      for ( int i = 0; i < count; i++ ) {
         probes[i] = ( String ) ( ( ArrayList )classToProbe.get( id ) ).get( i );
      }
      return probes;

   }

   void m_normalizeButton_actionPerformed( ActionEvent e ) {

      if ( m_normalizeButton.isSelected() ) {
         m_matrixDisplay.setStandardizedEnabled( true );
         m_normalizeButton.setText( m_normalizeButtonLabels[0] );
      }
      else {
         m_matrixDisplay.setStandardizedEnabled( false );
         m_normalizeButton.setText( m_normalizeButtonLabels[1] );
      }

      m_table.repaint();
   }

   void m_saveButton_actionPerformed(ActionEvent e) {

      //Create a file chooser
      final JFileChooser fc = new JFileChooser();
      ClassDetailFrame_JSaveFile options = new ClassDetailFrame_JSaveFile();
      fc.setAccessory( options );

      int returnVal = fc.showOpenDialog( this );

      if (returnVal == JFileChooser.APPROVE_OPTION) {

         File file = fc.getSelectedFile();

         //This is where a real application would open the file.
         String filename = file.getPath();
         try {
            m_matrixDisplay.saveToFile( filename );
         }
         catch ( IOException ex ) {
            System.err.println( "IOException error saving png to " + filename );
         }

         // assume extension for file is <name>.png (add a file filter to ensure this)
         // change filename to <name>.txt
         String[] s = filename.split( "." );
         String extension = s[ s.length - 1 ];
         String filenameWithoutExtension = filename.substring( filename.length() - extension.length() - 1, filename.length() - 1);
         filename = filenameWithoutExtension + ".txt";

         //saveTableToFile( filename );
      }
      // else canceled by user
   }

} // end class ClassDetailFrame

/**
 * Our table model.
 * <p>
 * The general picture is as follows:<br>
 * GUI -> Sort Filter -> Table Model
 */
class DetailsTableModel
    extends AbstractTableModel {

   private JMatrixDisplay m_matrixDisplay;
   private ArrayList m_values;
   private Map m_pvals;
   private Map m_classToProbe;
   private String m_id;
   private NumberFormat m_nf;
   private GeneDataReader m_geneData;
   private String[] m_columnNames = {
       "Probe", "P value", "Name", "Description"};

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

      if ( column < offset ) {
         return m_matrixDisplay.getColumnName( column );
      } else {
         return m_columnNames[column - offset];
      }
   }

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
            return ( String ) ( ( ArrayList )m_classToProbe.get( m_id ) ).get( row );
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

// This renderer extends a component. It is used each time a
// cell must be displayed.
class MatrixDisplayCellRenderer
    extends JLabel
    implements TableCellRenderer {

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
       int displayedColumn ) {
      // 'value' is value contained in the cell located at
      // (rowIndex, vColIndex)

      if ( isSelected ) {
         // cell (and perhaps other cells) are selected
      }

      if ( hasFocus ) {
         // this cell is the anchor and the table has the focus
      }

      Point coords = ( Point )tableCellValue;
      int row = coords.x;
      int column = coords.y;
      double matrixValue = m_matrixDisplay.getValue( row, column );
      Color matrixColor = m_matrixDisplay.getColor( row, column );

      // Configure the component with the specified value
      setBackground( matrixColor );

      // Set tool tip if desired

      setToolTipText( "" + format( matrixValue, 2 ) + " = " + matrixValue );

      // Since the renderer is a component, return itself
      return this;
   }

   static public double format( double value, int precision ) {

      int integerPart = (int) value;
      int fractionalPart = (int)( ( value - integerPart ) * Math.pow( 10, precision ));

      double fraction = fractionalPart / Math.pow( 10, precision );
      return integerPart + fraction;
   }

   // The following methods override the defaults for performance reasons
   public void validate() {}

   public void revalidate() {}

   protected void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {}

   public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue ) {}

} // end class MatrixDisplayCellRenderer


class MatrixDisplayColumnHeaderRenderer extends JButton implements TableCellRenderer {
   
    String m_columnName;
    final int PREFERRED_WIDTH  =  5;
    final int PREFERRED_HEIGHT = 80;
    final int MAX_TEXT_LENGTH  = 12;
       
    // This method is called each time a column header
    // using this renderer needs to be rendered.
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
        // 'value' is column header value of column 'vColIndex'
        // rowIndex is always -1
        // isSelected is always false
        // hasFocus is always false

        // Configure the component with the specified value
        m_columnName = value.toString();
                
        // Set tool tip if desired
        setToolTipText( m_columnName );

        // Since the renderer is a component, return itself
        return this;
    }
    
    protected void paintComponent( Graphics g ) {
       
        super.paintComponent( g );
        Font font = getFont();

        if (m_columnName.length() > MAX_TEXT_LENGTH)
           m_columnName = m_columnName.substring( 0, MAX_TEXT_LENGTH );
        
        int x = getSize().width  - 2;
        int y = getSize().height - 3;    
        Util.drawVerticalString( g, m_columnName, font, x, y );
    }
    
    public Dimension getPreferredSize() {
       
       return new Dimension( PREFERRED_WIDTH, PREFERRED_HEIGHT );
    }
    
    // The following methods override the defaults for performance reasons
    public void validate() {}
    public void revalidate() {}
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}


class ClassDetailFrame_m_normalizeButton_actionAdapter
    implements java.awt.event.ActionListener {
   ClassDetailFrame adaptee;

   ClassDetailFrame_m_normalizeButton_actionAdapter( ClassDetailFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_normalizeButton_actionPerformed( e );
   }
}

class ClassDetailFrame_m_saveButton_actionAdapter implements java.awt.event.ActionListener {
   ClassDetailFrame adaptee;

   ClassDetailFrame_m_saveButton_actionAdapter(ClassDetailFrame adaptee) {
      this.adaptee = adaptee;
   }
   public void actionPerformed(ActionEvent e) {
      adaptee.m_saveButton_actionPerformed(e);
   }
}

