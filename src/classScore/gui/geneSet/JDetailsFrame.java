package classScore.gui.geneSet;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.reader.DoubleMatrixReader;
import baseCode.graphics.text.Util;
import baseCode.gui.ColorMap;
import baseCode.gui.JGradientBar;
import baseCode.gui.JMatrixDisplay;
import baseCode.gui.table.JHorizontalTableHeaderRenderer;
import baseCode.gui.table.JMatrixTableCellRenderer;
import baseCode.gui.table.JVerticalTableHeaderRenderer;
import classScore.GeneAnnotations;
import classScore.Settings;
import classScore.SortFilterModel;
import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * @version $Id$
 * @todo show how many rows there are in the table (I think this is done classPvalRun, line 906)
 * @todo add a graphical display of the data
 */
public class JDetailsFrame
    extends JFrame {

   final int PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN = 6;
   final int MIN_WIDTH_MATRIXDISPLAY_COLUMN = 1;
   final int MAX_WIDTH_MATRIXDISPLAY_COLUMN = 19;
   final int PREFERRED_WIDTH_COLUMN_0 = 75;
   final int PREFERRED_WIDTH_COLUMN_1 = 75;
   final int PREFERRED_WIDTH_COLUMN_2 = 75;
   final int PREFERRED_WIDTH_COLUMN_3 = 300;
   final int COLOR_RANGE_SLIDER_RESOLUTION = 10;

   public JMatrixDisplay m_matrixDisplay = null;
   protected JScrollPane m_tableScrollPane = new JScrollPane();
   protected JTable m_table = new JTable();
   protected BorderLayout borderLayout1 = new BorderLayout();
   protected JToolBar m_toolbar = new JToolBar();
   JSlider m_matrixDisplayCellWidthSlider = new JSlider();
   JMenuBar m_menuBar = new JMenuBar();
   JMenu m_fileMenu = new JMenu();
   JRadioButtonMenuItem m_greenredColormapMenuItem = new JRadioButtonMenuItem();
   JMenu m_viewMenu = new JMenu();
   JRadioButtonMenuItem m_blackbodyColormapMenuItem = new JRadioButtonMenuItem();
   JMenuItem m_saveImageMenuItem = new JMenuItem();
   JCheckBoxMenuItem m_normalizeMenuItem = new JCheckBoxMenuItem();
   DecimalFormat m_nf = new DecimalFormat( "0.##E0" );
   JLabel jLabel1 = new JLabel();
   JLabel jLabel2 = new JLabel();
   JLabel jLabel3 = new JLabel();
   JSlider m_colorRangeSlider = new JSlider();
   JGradientBar m_gradientBar = new JGradientBar();
   JMenuItem m_saveDataMenuItem = new JMenuItem();

   public JDetailsFrame( ArrayList values, Map pvals, Map classToProbe, String id,
                         GeneAnnotations geneData, Settings settings ) {

      try {
         m_nf.setMaximumFractionDigits( 3 );
         createDetailsTable( values, pvals, classToProbe, id, geneData, settings );
         jbInit();

         boolean isNormalized = m_matrixDisplay.getStandardizedEnabled();
         m_normalizeMenuItem.setSelected( isNormalized );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {

      setSize( 800, m_table.getHeight() );
      setResizable( false );
      setLocation( 200, 100 );
      getContentPane().setLayout( borderLayout1 );
      setDefaultCloseOperation( DISPOSE_ON_CLOSE );

      m_gradientBar.setMaximumSize(new Dimension(200, 30));
      m_gradientBar.setPreferredSize(new Dimension(120, 30));
      m_gradientBar.setColorMap( m_matrixDisplay.getColorMap() );

      double min = m_matrixDisplay.getDisplayMin();
      double max = m_matrixDisplay.getDisplayMax();
      m_gradientBar.setLabels( min, max );

      // Enable the horizontal scroll bar
      m_table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

      // Prevent user from moving tables around
      m_table.getTableHeader().setReorderingAllowed( false );

      // Make sure the matrix display doesn't have a grid separating color cells.
      m_table.setIntercellSpacing( new Dimension( 0, 0 ) );

      // The rest of the table (text and value) should have a light gray grid
      m_table.setGridColor( Color.lightGray );

      // add a viewport with a table inside it
      m_toolbar.setFloatable( false );
      this.setJMenuBar( m_menuBar );
      m_fileMenu.setText( "File" );
      m_greenredColormapMenuItem.setSelected( false );
      m_greenredColormapMenuItem.setText( "Green-Red" );
      m_greenredColormapMenuItem.addActionListener( new
          JDetailsFrame_m_greenredColormapMenuItem_actionAdapter( this ) );
      m_greenredColormapMenuItem.addActionListener( new
          JDetailsFrame_m_greenredColormapMenuItem_actionAdapter( this ) );
      m_viewMenu.setText( "View" );
      m_blackbodyColormapMenuItem.setSelected( true );
      m_blackbodyColormapMenuItem.setText( "Blackbody" );
      m_blackbodyColormapMenuItem.addActionListener( new
          JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter( this ) );
      m_blackbodyColormapMenuItem.addActionListener( new
          JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter( this ) );
      m_saveImageMenuItem.setActionCommand("SaveImage");
      m_saveImageMenuItem.setText("Save Image..." );
      m_saveImageMenuItem.addActionListener( new JDetailsFrame_m_saveImageMenuItem_actionAdapter( this ) );
      m_normalizeMenuItem.setText( "Normalize" );
      m_normalizeMenuItem.addActionListener( new JDetailsFrame_m_normalizeMenuItem_actionAdapter( this ) );
      m_matrixDisplayCellWidthSlider.setInverted( false );
      m_matrixDisplayCellWidthSlider.setMajorTickSpacing( 0 );
      m_matrixDisplayCellWidthSlider.setMaximum( MAX_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setMinimum( MIN_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setValue( PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setMinorTickSpacing( 3 );
      m_matrixDisplayCellWidthSlider.setPaintLabels( false );
      m_matrixDisplayCellWidthSlider.setPaintTicks( true );
      m_matrixDisplayCellWidthSlider.setMaximumSize( new Dimension( 90, 24 ) );
      m_matrixDisplayCellWidthSlider.setPreferredSize( new Dimension( 90, 24 ) );
      m_matrixDisplayCellWidthSlider.addChangeListener( new
          JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter( this ) );
      this.setResizable( true );
      jLabel1.setText( "Cell Width:" );
      jLabel2.setText( "    " );
      jLabel3.setText( "Color Range:" );
      m_colorRangeSlider.setMaximum( 6 * COLOR_RANGE_SLIDER_RESOLUTION );
      m_colorRangeSlider.setMinimum( 1 );
      m_colorRangeSlider.setValue( ( int ) ( m_matrixDisplay.getDisplayRange() *
                                             COLOR_RANGE_SLIDER_RESOLUTION ) );
      m_colorRangeSlider.setMaximumSize( new Dimension( 50, 24 ) );
      m_colorRangeSlider.setPreferredSize( new Dimension( 50, 24 ) );
      m_colorRangeSlider.addChangeListener( new JDetailsFrame_m_colorRangeSlider_changeAdapter( this ) );
      m_saveDataMenuItem.setActionCommand("SaveData");
      m_saveDataMenuItem.setText("Save Data...");
      m_saveDataMenuItem.addActionListener(new JDetailsFrame_m_saveDataMenuItem_actionAdapter(this));
      m_tableScrollPane.getViewport().add( m_table, null );

      // Reposition the table inside the scrollpane
      int x = m_table.getSize().width; // should probably subtract the size of the viewport, but it gets trimmed anyway, so it's okay to be lazy here
      m_tableScrollPane.getViewport().setViewPosition( new Point( x, 0 ) );

      this.getContentPane().add( m_tableScrollPane, BorderLayout.CENTER );
      this.getContentPane().add( m_toolbar, BorderLayout.NORTH );
      m_toolbar.add( jLabel1, null );
      m_toolbar.add( m_matrixDisplayCellWidthSlider, null );
      m_toolbar.add( jLabel2, null );
      m_toolbar.add( jLabel3, null );
      m_toolbar.add( m_colorRangeSlider, null );
      m_toolbar.add( m_gradientBar, null );

      m_menuBar.add( m_fileMenu );
      m_menuBar.add( m_viewMenu );

     // Color map menu items (radio button group -- only one can be selected at one time)
      ButtonGroup group = new ButtonGroup();
      group.add( m_greenredColormapMenuItem );
      group.add( m_blackbodyColormapMenuItem );

      m_viewMenu.add( m_normalizeMenuItem );
      m_viewMenu.addSeparator();
      m_viewMenu.add( m_greenredColormapMenuItem );
      m_viewMenu.add( m_blackbodyColormapMenuItem );
      m_fileMenu.add( m_saveImageMenuItem );
      m_fileMenu.add(m_saveDataMenuItem);
      m_matrixDisplayCellWidthSlider.setPaintTrack( true );
      m_matrixDisplayCellWidthSlider.setPaintTicks( false );
   }

   private void createDetailsTable(
       ArrayList values,
       Map pvals,
       Map classToProbe,
       String id,
       GeneAnnotations geneData,
       Settings settings ) {

      //
      // Create a matrix display
      //

      // compile the matrix data
      String filename = settings.getRawFile();
      String[] geneProbes = getProbes( classToProbe, id, values.size() );
      DoubleMatrixReader matrixReader = new DoubleMatrixReader();
      DenseDoubleMatrix2DNamed matrix = null;
      try {
         matrix = ( DenseDoubleMatrix2DNamed ) matrixReader.read( filename,
             geneProbes );
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
          m_matrixDisplay, values, pvals, classToProbe, id, geneData, m_nf
          );
      SortFilterModel sorter = new SortFilterModel( m, m_matrixDisplay );
      m_table.setModel( new DefaultTableModel() ); // bug in JTable (Manju said so) -- if called repeatedly, this line should be here... as-is, makes no difference
      m_table.setModel( sorter );

      m_table.getTableHeader().addMouseListener( new MouseAdapter() {
         public void mouseClicked( MouseEvent event ) {
            int tableColumn = m_table.columnAtPoint( event.getPoint() );
            int modelColumn = m_table.convertColumnIndexToModel( tableColumn );
            ( ( SortFilterModel ) m_table.getModel() ).sort( modelColumn );
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
      } );

      //
      // Set up the matrix display part of the table
      //

      // Make the columns in the matrix display not too wide (cell-size)
      // and set a custom cell renderer
      JMatrixTableCellRenderer cellRenderer = new JMatrixTableCellRenderer(
          m_matrixDisplay
          ); // create one instance that will be used to draw each cell

      JVerticalTableHeaderRenderer verticalHeaderRenderer =
          new JVerticalTableHeaderRenderer(); // create only one instance
      int matrixColumnCount = m_matrixDisplay.getColumnCount();

      // Set each column
      for ( int i = 0; i < matrixColumnCount; i++ ) {
         TableColumn col = m_table.getColumnModel().getColumn( i );
         col.setResizable( false );
         col.setPreferredWidth( PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN );
         col.setMinWidth( MIN_WIDTH_MATRIXDISPLAY_COLUMN ); // no narrower than this
         col.setMaxWidth( MAX_WIDTH_MATRIXDISPLAY_COLUMN ); // no wider than this
         col.setCellRenderer( cellRenderer );
         col.setHeaderRenderer( verticalHeaderRenderer );
      }

      //
      // Set up the rest of the table
      //
      JHorizontalTableHeaderRenderer horizontalHeaderRenderer =
          new JHorizontalTableHeaderRenderer(); // create only one instance
      TableColumn col;

      // The columns containing text or values (not matrix display) should be a bit wider
      col = m_table.getColumnModel().getColumn( matrixColumnCount + 0 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_0 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      col = m_table.getColumnModel().getColumn( matrixColumnCount + 1 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_1 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      col = m_table.getColumnModel().getColumn( matrixColumnCount + 2 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_2 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      col = m_table.getColumnModel().getColumn( matrixColumnCount + 3 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_3 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      //
      // Sort initially by the pvalue column
      //
      int modelColumn = m_table.convertColumnIndexToModel( matrixColumnCount + 1 );
      ( ( SortFilterModel ) m_table.getModel() ).sort( modelColumn );

      //
      // Save the dimensions of the table just in case
      //
      int width =
          matrixColumnCount * PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN +
          PREFERRED_WIDTH_COLUMN_0 +
          PREFERRED_WIDTH_COLUMN_1 +
          PREFERRED_WIDTH_COLUMN_2 +
          PREFERRED_WIDTH_COLUMN_3;
      int height = m_table.getPreferredScrollableViewportSize().height;

      Dimension d = new Dimension( width, height );
      m_table.setSize( d );

   } // end createDetailsTable

   protected String[] getProbes( Map classToProbe, String id, int count ) {

      // Compile a list of gene probe ID's in this probe class
      String[] probes = new String[count];
      for ( int i = 0; i < count; i++ ) {
         probes[i] = ( String ) ( ( ArrayList ) classToProbe.get( id ) ).get( i );
      }
      return probes;

   }

   void m_greenredColormapMenuItem_actionPerformed( ActionEvent e ) {

      try {
         Color[] colorMap = ColorMap.GREENRED_COLORMAP;
         m_matrixDisplay.setColorMap( colorMap );
         m_gradientBar.setColorMap( colorMap );
      }
      catch ( Exception ex ) {
      }

   }

   void m_blackbodyColormapMenuItem_actionPerformed( ActionEvent e ) {

      try {
         Color[] colorMap = ColorMap.BLACKBODY_COLORMAP;
         m_matrixDisplay.setColorMap( colorMap );
         m_gradientBar.setColorMap( colorMap );
      }
      catch ( Exception ex ) {
      }

   }

   void m_saveImageMenuItem_actionPerformed( ActionEvent e ) {

      // Create a file chooser
      final JImageFileChooser fc = new JImageFileChooser();

      int returnVal = fc.showSaveDialog( this );
      if ( returnVal == JFileChooser.APPROVE_OPTION ) {

         File file = fc.getSelectedFile();

         // Make sure the filename has an image extension
         String filename = file.getPath();
         if ( !Util.hasImageExtension( filename ) ) {
            filename = Util.addImageExtension( filename );
         }
         // Save the color matrix image
         try {
            boolean includeLabels = fc.includeLabels();
            boolean normalize = fc.normalized();
            m_matrixDisplay.saveToFile( filename, includeLabels, normalize );
         }
         catch ( IOException ex ) {
            System.err.println( "IOException error saving png to " + filename );
         }
      }
      // else canceled by user
   }

   void m_saveDataMenuItem_actionPerformed(ActionEvent e) {

      // Create a file chooser
      final JFileChooser fc = new JFileChooser();
      DataFileFilter dataFileFilter = new DataFileFilter();
      fc.setFileFilter( dataFileFilter );
      fc.setAcceptAllFileFilterUsed( false );

      int returnVal = fc.showSaveDialog( this );
      if ( returnVal == JFileChooser.APPROVE_OPTION ) {

         File file = fc.getSelectedFile();

         // Make sure the filename has an image extension
         String filename = file.getPath();
         if ( !Util.hasDataExtension( filename ) ) {
            filename = Util.addDataExtension( filename );
         }
         // Save the color matrix image
         try {
            saveTableToFile( filename );
         }
         catch ( IOException ex ) {
            System.err.println( "IOException error saving data to " + filename );
         }
      }
      // else canceled by user
   }

   /**
    * saveTableToFile
    *
    * @param filename String
    */
   protected void saveTableToFile( String filename ) throws IOException {

      BufferedWriter out = new BufferedWriter( new FileWriter( filename ) );

      int totalRows = m_table.getRowCount();
      int totalColumns = m_table.getColumnCount();
      int matrixColumns = m_matrixDisplay.getColumnCount();

      // Make sure we are writing out non-normalized values
      boolean isStandardized = m_matrixDisplay.getStandardizedEnabled();
      m_matrixDisplay.setStandardizedEnabled( false );

      try {
         // write out the matrix values
         int r = 0;
         {
         //for ( int r = 0; r < totalRows; r++ ) {

            // get values in that row
            String probeID = getProbeID( r );
            out.write( probeID + "\t" );

            // write out values in the entire row
            double[] row = m_matrixDisplay.getRowByName( probeID );
            for ( int c = 0; c < row.length; c++ ) {
               out.write( row[c] + "\t" );
            }

            out.write( "\r\n" );
         }
      } catch ( IOException exception ) { }

      m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state

      // close the file
      out.close();

   } // end saveTableToFile

   private String getProbeID( int row ) {
      int offset = m_matrixDisplay.getColumnCount(); // matrix display ends
      return (String) m_table.getValueAt( row, offset + 0 );
   }

   void m_normalizeMenuItem_actionPerformed( ActionEvent e ) {

      boolean normalize = m_normalizeMenuItem.isSelected();
      m_matrixDisplay.setStandardizedEnabled( normalize );

      double min = m_matrixDisplay.getDisplayMin();
      double max = m_matrixDisplay.getDisplayMax();
      m_gradientBar.setLabels( min, max );
      m_table.repaint();
   }

   void m_matrixDisplayCellWidthSlider_stateChanged( ChangeEvent e ) {

      JSlider source = ( JSlider ) e.getSource();

      //if ( ! source.getValueIsAdjusting() ) {

      // Adjust the width of every matrix display column
      int width = ( int ) source.getValue();
      if ( width >= MIN_WIDTH_MATRIXDISPLAY_COLUMN && width <= MAX_WIDTH_MATRIXDISPLAY_COLUMN ) {

         m_table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

         int matrixColumnCount = m_matrixDisplay.getColumnCount();
         for ( int i = 0; i < matrixColumnCount; i++ ) {
            TableColumn col = m_table.getColumnModel().getColumn( i );
            col.setResizable( false );
            col.setPreferredWidth( width );
         }
      }
   }

   void m_colorRangeSlider_stateChanged( ChangeEvent e ) {

      JSlider source = ( JSlider ) e.getSource();
      double range = source.getValue();
      double max = + ( ( range / 2 ) / COLOR_RANGE_SLIDER_RESOLUTION );
      double min = - ( ( range / 2 ) / COLOR_RANGE_SLIDER_RESOLUTION );
      m_gradientBar.setLabels( min, max );
      m_matrixDisplay.setDisplayRange( min, max );
      m_table.repaint();
   }

} // end class JDetailsFrame

class JDetailsFrame_m_greenredColormapMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_greenredColormapMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_greenredColormapMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_blackbodyColormapMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_saveImageMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_saveImageMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_saveImageMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_normalizeMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_normalizeMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_normalizeMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter
    implements javax.swing.event.ChangeListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void stateChanged( ChangeEvent e ) {
      adaptee.m_matrixDisplayCellWidthSlider_stateChanged( e );
   }
}

class JDetailsFrame_m_colorRangeSlider_changeAdapter
    implements javax.swing.event.ChangeListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_colorRangeSlider_changeAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void stateChanged( ChangeEvent e ) {
      adaptee.m_colorRangeSlider_stateChanged( e );
   }
}

class JDetailsFrame_m_saveDataMenuItem_actionAdapter implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_saveDataMenuItem_actionAdapter(JDetailsFrame adaptee) {
      this.adaptee = adaptee;
   }
   public void actionPerformed(ActionEvent e) {
      adaptee.m_saveDataMenuItem_actionPerformed(e);
   }
}
