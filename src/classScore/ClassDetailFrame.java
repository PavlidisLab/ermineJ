package classScore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.reader.DoubleMatrixReader;
import baseCode.graphics.text.Util;
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

public class ClassDetailFrame extends JFrame {

   final int PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN = 12;
   final int MIN_WIDTH_MATRIXDISPLAY_COLUMN = 1;
   final int MAX_WIDTH_MATRIXDISPLAY_COLUMN = 20;
   final int PREFERRED_WIDTH_COLUMN_1 = 75;
   final int PREFERRED_WIDTH_COLUMN_2 = 125;
   final int PREFERRED_WIDTH_COLUMN_3 = 300;

   public JMatrixDisplay m_matrixDisplay = null;
   protected JScrollPane m_tableScrollPane = new JScrollPane();
   protected JTable m_table = new JTable();
   protected BorderLayout borderLayout1 = new BorderLayout();
   protected JToolBar m_toolBar = new JToolBar();
   protected JToggleButton m_normalizeButton = new JToggleButton();
   protected final String[] m_normalizeButtonLabels = {
           "Normalize ON", "Normalize OFF"};
   protected JButton m_saveButton = new JButton();

   public ClassDetailFrame(
           ArrayList values,
           Map pvals,
           Map classToProbe,
           String id,
           NumberFormat nf,
           GeneDataReader geneData,
           Properties settings) {

      try {
         createDetailsTable(values, pvals, classToProbe, id, nf, geneData,
                            settings);
         jbInit();

         boolean isNormalized = m_matrixDisplay.getStandardizedEnabled();
         m_normalizeButton.setText(isNormalized ? m_normalizeButtonLabels[0] :
                                   m_normalizeButtonLabels[1]);
         m_normalizeButton.setSelected(isNormalized);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {

      setSize(800, 460);
      setResizable(false);
      setLocation(200, 100);
      getContentPane().setLayout(borderLayout1);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      // Enable the horizontal scroll bar
      m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      // Prevent user from moving tables around
      m_table.getTableHeader().setReorderingAllowed(false);

      // Make sure the matrix display doesn't have a grid separating color cells.
      m_table.setIntercellSpacing(new Dimension(0, 0));

      // The rest of the table (text and value) should have a light gray grid
      m_table.setGridColor(Color.lightGray);

      // add a viewport with a table inside it
      m_normalizeButton.setToolTipText("Normalize to variance 1, mean 0");
      m_normalizeButton.setActionCommand("jToggleButton1");
      m_normalizeButton.setText("Normalize OFF");
      m_normalizeButton.addActionListener(new
                                          ClassDetailFrame_m_normalizeButton_actionAdapter(this));
      m_saveButton.setToolTipText("Save to file");
      m_saveButton.setText("Save to file...");
      m_saveButton.addActionListener(new
                                     ClassDetailFrame_m_saveButton_actionAdapter(this));
      m_toolBar.setFloatable(false);
      m_tableScrollPane.getViewport().add(m_table, null);

      // Reposition the table inside the scrollpane
      int x = m_table.getSize().width; // should probably subtract the size of the viewport, but it gets trimmed anyway, so it's okay to be lazy here
      m_tableScrollPane.getViewport().setViewPosition(new Point(x, 0));

      this.getContentPane().add(m_tableScrollPane, BorderLayout.CENTER);
      m_toolBar.add(m_saveButton, null);
      m_toolBar.add(m_normalizeButton, null);
      this.getContentPane().add(m_toolBar, BorderLayout.NORTH);
   }

   private void createDetailsTable(
           ArrayList values,
           Map pvals,
           Map classToProbe,
           String id,
           NumberFormat nf,
           GeneDataReader geneData,
           Properties settings) {

      //
      // Create a matrix display
      //

      // compile the matrix data
      String filename = settings.getProperty("rawFile");
      String[] geneProbes = getProbes(classToProbe, id, values.size());
      DoubleMatrixReader matrixReader = new DoubleMatrixReader();
      DenseDoubleMatrix2DNamed matrix = null;
      try {
         matrix = (DenseDoubleMatrix2DNamed) matrixReader.read(filename,
                 geneProbes);
      } catch (IOException e) {
         System.err.println("IOException: wrong filename for MatrixReader");
      }

      // create the matrix display
      m_matrixDisplay = new JMatrixDisplay(matrix);
      m_matrixDisplay.setStandardizedEnabled(true);

      //
      // Create the rest of the table
      //

      DetailsTableModel m = new DetailsTableModel(
              m_matrixDisplay, values, pvals, classToProbe, id, nf, geneData
              );
      SortFilterModel sorter = new SortFilterModel(m, m_matrixDisplay);
      m_table.setModel(new DefaultTableModel()); // bug in JTable (Manju said so) -- if called repeatedly, this line should be here... as-is, makes no difference
      m_table.setModel(sorter);

      m_table.getTableHeader().addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent event) {
            int tableColumn = m_table.columnAtPoint(event.getPoint());
            int modelColumn = m_table.convertColumnIndexToModel(tableColumn);
            ((SortFilterModel) m_table.getModel()).sort(modelColumn);
         }
         /*
                   public void mouseReleased( MouseEvent event ) {
            // make all the matrix display columns equally wide
            int matrixColumnCount = m_matrixDisplay.getColumnCount();
            for ( int i = 0; i < matrixColumnCount; i++ ) {
               TableColumn col = m_table.getColumnModel().getColumn( i );

               int width = col.getPreferredWidth();

               //String s = evt.getNewValue().toString();
               //int width = (new Integer( s )).intValue();

               col.setPreferredWidth( width );
            }
            m_table.revalidate();
                   }
          */
      });

      // Make the columns in the matrix display not too wide (cell-size)
      // and set a custom cell renderer
      MatrixDisplayCellRenderer cellRenderer = new MatrixDisplayCellRenderer(
              m_matrixDisplay); // create one instance that will be used to draw each cell
      MatrixDisplayColumnHeaderRenderer columnHeaderRenderer =
              new MatrixDisplayColumnHeaderRenderer(); // create only one instance
      int matrixColumnCount = m_matrixDisplay.getColumnCount();

      for (int i = 0; i < matrixColumnCount; i++) {
         TableColumn col = m_table.getColumnModel().getColumn(i);
         col.setResizable(false);
         col.setPreferredWidth(PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN);
         col.setMinWidth(MIN_WIDTH_MATRIXDISPLAY_COLUMN); // no narrower than this
         col.setMaxWidth(MAX_WIDTH_MATRIXDISPLAY_COLUMN); // no wider than this
         col.setCellRenderer(cellRenderer);
         col.setHeaderRenderer(columnHeaderRenderer);
      }

      // The columns containing text or values (not matrix display) should be a bit wider
      m_table.getColumnModel().getColumn(matrixColumnCount + 1).
              setPreferredWidth(PREFERRED_WIDTH_COLUMN_1);
      m_table.getColumnModel().getColumn(matrixColumnCount + 2).
              setPreferredWidth(PREFERRED_WIDTH_COLUMN_2);
      m_table.getColumnModel().getColumn(matrixColumnCount + 3).
              setPreferredWidth(PREFERRED_WIDTH_COLUMN_3);

      // Save the dimensions of the table just in case
      int width =
              PREFERRED_WIDTH_COLUMN_1 +
              PREFERRED_WIDTH_COLUMN_2 +
              PREFERRED_WIDTH_COLUMN_3 +
              matrixColumnCount * PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN;
      int height = 0; // the height shouldn't be zero of course, but for now all we really need is the width (to position the viewport)

      Dimension d = new Dimension(width, height);
      m_table.setSize(d);
   }

   protected String[] getProbes(Map classToProbe, String id, int count) {

      // Compile a list of gene probe ID's in this probe class
      String[] probes = new String[count];
      for (int i = 0; i < count; i++) {
         probes[i] = (String) ((ArrayList) classToProbe.get(id)).get(i);
      }
      return probes;

   }

   void m_normalizeButton_actionPerformed(ActionEvent e) {

      if (m_normalizeButton.isSelected()) {
         m_matrixDisplay.setStandardizedEnabled(true);
         m_normalizeButton.setText(m_normalizeButtonLabels[0]);
      } else {
         m_matrixDisplay.setStandardizedEnabled(false);
         m_normalizeButton.setText(m_normalizeButtonLabels[1]);
      }

      m_table.repaint();
   }

   void m_saveButton_actionPerformed(ActionEvent e) {

      //Create a file chooser
      final JFileChooser fc = new JFileChooser();
      ClassDetailFrame_JSaveFile options = new ClassDetailFrame_JSaveFile();
      fc.setAccessory(options);

      int returnVal = fc.showOpenDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {

         File file = fc.getSelectedFile();

         //This is where a real application would open the file.
         String filename = file.getPath();
         try {
            m_matrixDisplay.saveToFile(filename);
         } catch (IOException ex) {
            System.err.println("IOException error saving png to " + filename);
         }

         // assume extension for file is <name>.png (add a file filter to ensure this)
         // change filename to <name>.txt
         String[] s = filename.split(".");
         String extension = s[s.length - 1];
         String filenameWithoutExtension = filename.substring(filename.length() -
                 extension.length() -
                 1, filename.length() - 1);
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
class DetailsTableModel extends AbstractTableModel {

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
           GeneDataReader geneData) {

      m_matrixDisplay = matrixDisplay;
      m_values = values;
      m_pvals = pvals;
      m_classToProbe = classToProbe;
      m_id = id;
      m_nf = nf;
      m_geneData = geneData;
   }

   public String getColumnName(int column) {

      int offset = m_matrixDisplay.getColumnCount(); // matrix display ends

      if (column < offset) {
         return m_matrixDisplay.getColumnName(column);
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

   public Object getValueAt(int row, int column) {

      int offset = m_matrixDisplay.getColumnCount(); // matrix display ends

      if (column < offset) {
         return new Point(row, column); // coords into JMatrixDisplay
      } else {
         column -= offset;

      }
      switch (column) { // after it's been offset
      case 0:
         return (String) ((ArrayList) m_classToProbe.get(m_id)).get(row);
      case 1:
         return new Double(m_nf.format(m_pvals.get((String) ((ArrayList)
                 m_classToProbe.get(m_id)).get(row))));
      case 2:
         return m_geneData.getProbeGeneName((String) ((ArrayList)
                 m_classToProbe.get(m_id)).get(row));
      case 3:
         return m_geneData.getProbeDescription((String) ((ArrayList)
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

   public MatrixDisplayCellRenderer(JMatrixDisplay matrixDisplay) {

      m_matrixDisplay = matrixDisplay;
      setOpaque(true);
   }

   // This method is called each time a cell in a column
   // using this renderer needs to be rendered.
   public Component getTableCellRendererComponent(
           JTable table,
           Object tableCellValue,
           boolean isSelected,
           boolean hasFocus,
           int displayedRow,
           int displayedColumn) {
      // 'value' is value contained in the cell located at
      // (rowIndex, vColIndex)

      if (isSelected) {
         // cell (and perhaps other cells) are selected
      }

      if (hasFocus) {
         // this cell is the anchor and the table has the focus
      }

      Point coords = (Point) tableCellValue;
      int row = coords.x;
      int column = coords.y;
      double matrixValue = m_matrixDisplay.getValue(row, column);
      Color matrixColor = m_matrixDisplay.getColor(row, column);

      // Configure the component with the specified value
      setBackground(matrixColor);

      // Set tool tip if desired

      setToolTipText("" + format(matrixValue, 2) + " = " + matrixValue);

      // Since the renderer is a component, return itself
      return this;
   }

   static public double format(double value, int precision) {

      int integerPart = (int) value;
      int fractionalPart = (int) ((value - integerPart) *
                                  Math.pow(10, precision));

      double fraction = fractionalPart / Math.pow(10, precision);
      return integerPart + fraction;
   }

   // The following methods override the defaults for performance reasons
   public void validate() {}

   public void revalidate() {}

   protected void firePropertyChange(String propertyName, Object oldValue,
                                     Object newValue) {}

   public void firePropertyChange(String propertyName, boolean oldValue,
                                  boolean newValue) {}

} // end class MatrixDisplayCellRenderer


class MatrixDisplayColumnHeaderRenderer extends JButton implements
        TableCellRenderer {

   String m_columnName;
   final int PREFERRED_HEIGHT = 80;
   final int MAX_TEXT_LENGTH = 12;

   // This method is called each time a column header
   // using this renderer needs to be rendered.
   public Component getTableCellRendererComponent(JTable table, Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int rowIndex, int vColIndex) {
      // 'value' is column header value of column 'vColIndex'
      // rowIndex is always -1
      // isSelected is always false
      // hasFocus is always false

      // Configure the component with the specified value
      m_columnName = value.toString();

      // Set tool tip if desired
      setToolTipText(m_columnName);

      // Since the renderer is a component, return itself
      return this;
   }

   protected void paintComponent(Graphics g) {

      super.paintComponent(g);
      Font font = getFont();

      if (m_columnName.length() > MAX_TEXT_LENGTH) {
         m_columnName = m_columnName.substring(0, MAX_TEXT_LENGTH);

      }
      int x = getSize().width - 4;
      int y = getSize().height - 4;
      Util.drawVerticalString(g, m_columnName, font, x, y);
   }

   public Dimension getPreferredSize() {

      return new Dimension(super.getPreferredSize().width, PREFERRED_HEIGHT);
   }

   // The following methods override the defaults for performance reasons
   public void validate() {}

   public void revalidate() {}

   protected void firePropertyChange(String propertyName, Object oldValue,
                                     Object newValue) {}

   public void firePropertyChange(String propertyName, boolean oldValue,
                                  boolean newValue) {}
}


class ClassDetailFrame_m_normalizeButton_actionAdapter implements java.awt.
        event.ActionListener {
   ClassDetailFrame adaptee;

   ClassDetailFrame_m_normalizeButton_actionAdapter(ClassDetailFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.m_normalizeButton_actionPerformed(e);
   }
}


class ClassDetailFrame_m_saveButton_actionAdapter implements java.awt.event.
        ActionListener {
   ClassDetailFrame adaptee;

   ClassDetailFrame_m_saveButton_actionAdapter(ClassDetailFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.m_saveButton_actionPerformed(e);
   }
}
