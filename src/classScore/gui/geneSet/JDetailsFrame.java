package classScore.gui.details;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.JTableHeader;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.reader.DoubleMatrixReader;
import baseCode.graphics.text.Util;
import baseCode.gui.JMatrixDisplay;
import javax.swing.*;
import java.awt.event.*;
import baseCode.gui.ColorMap;
import javax.swing.event.*;

import classScore.GeneAnnotations;
import classScore.Settings;
import classScore.SortFilterModel;

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

public class JDetailsFrame extends JFrame {

   final int PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN = 12;
   final int MIN_WIDTH_MATRIXDISPLAY_COLUMN = 1;
   final int MAX_WIDTH_MATRIXDISPLAY_COLUMN = 19;
   final int PREFERRED_WIDTH_COLUMN_0 = 75;
   final int PREFERRED_WIDTH_COLUMN_1 = 75;
   final int PREFERRED_WIDTH_COLUMN_2 = 75;
   final int PREFERRED_WIDTH_COLUMN_3 = 300;

   public JMatrixDisplay m_matrixDisplay = null;
   protected JScrollPane m_tableScrollPane = new JScrollPane();
   protected JTable m_table = new JTable();
   protected BorderLayout borderLayout1 = new BorderLayout();
   protected JToolBar m_toolBar = new JToolBar();
   JSlider m_matrixDisplayCellWidthSlider = new JSlider();
   JMenuBar m_menuBar = new JMenuBar();
   JMenu m_fileMenu = new JMenu();
   JRadioButtonMenuItem m_greenredColormapMenuItem = new JRadioButtonMenuItem();
   JMenu m_viewMenu = new JMenu();
   JRadioButtonMenuItem m_blackbodyColormapMenuItem = new JRadioButtonMenuItem();
   JMenuItem m_saveFileMenuItem = new JMenuItem();
   JCheckBoxMenuItem m_normalizeMenuItem = new JCheckBoxMenuItem();
   DecimalFormat m_nf = new DecimalFormat( "0.##E0" );

   public JDetailsFrame(ArrayList values, Map pvals, Map classToProbe, String id, GeneAnnotations geneData, Settings settings) {

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

      // Enable the horizontal scroll bar
      m_table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

      // Prevent user from moving tables around
      m_table.getTableHeader().setReorderingAllowed( false );

      // Make sure the matrix display doesn't have a grid separating color cells.
      m_table.setIntercellSpacing( new Dimension( 0, 0 ) );

      // The rest of the table (text and value) should have a light gray grid
      m_table.setGridColor( Color.lightGray );

      // add a viewport with a table inside it
      m_toolBar.setFloatable( false );
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
      m_saveFileMenuItem.setText( "Save As..." );
      m_saveFileMenuItem.addActionListener( new JDetailsFrame_m_saveFileMenuItem_actionAdapter( this ) );
      m_normalizeMenuItem.setText( "Normalize" );
      m_normalizeMenuItem.addActionListener( new JDetailsFrame_m_normalizeMenuItem_actionAdapter( this ) );
      m_matrixDisplayCellWidthSlider.setInverted( false );
      m_matrixDisplayCellWidthSlider.setMajorTickSpacing(0);
      m_matrixDisplayCellWidthSlider.setMaximum( MAX_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setMinimum( MIN_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setValue( PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setMinorTickSpacing(3 );
      m_matrixDisplayCellWidthSlider.setPaintLabels( false );
      m_matrixDisplayCellWidthSlider.setPaintTicks( true );
      m_matrixDisplayCellWidthSlider.setMaximumSize( new Dimension( 60, 24 ) );
      m_matrixDisplayCellWidthSlider.setPreferredSize( new Dimension( 50, 24 ) );
      m_matrixDisplayCellWidthSlider.addChangeListener(new JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter(this));
      this.setResizable( true );
      m_tableScrollPane.getViewport().add( m_table, null );

      // Reposition the table inside the scrollpane
      int x = m_table.getSize().width; // should probably subtract the size of the viewport, but it gets trimmed anyway, so it's okay to be lazy here
      m_tableScrollPane.getViewport().setViewPosition( new Point( x, 0 ) );

      this.getContentPane().add( m_tableScrollPane, BorderLayout.CENTER );
      this.getContentPane().add( m_toolBar, BorderLayout.NORTH );
      m_toolBar.add( m_matrixDisplayCellWidthSlider, null );
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
      m_fileMenu.add( m_saveFileMenuItem );
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
          m_matrixDisplay,
          m_nf 
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
         m_matrixDisplay.setColorMap( ColorMap.GREENRED_COLORMAP );
      }
      catch ( Exception ex ) {
      }

   }

   void m_blackbodyColormapMenuItem_actionPerformed( ActionEvent e ) {

      try {
         m_matrixDisplay.setColorMap( ColorMap.BLACKBODY_COLORMAP );
      }
      catch ( Exception ex ) {
      }

   }

   void m_saveFileMenuItem_actionPerformed( ActionEvent e ) {
            
      // Create a file chooser
      final JDetailsFileChooser fc = new JDetailsFileChooser();
    
      int returnVal = fc.showSaveDialog( this );
      if ( returnVal == JFileChooser.APPROVE_OPTION ) {

         File file = fc.getSelectedFile();

         // Make sure the filename has an image extension
         String filename = file.getPath();
         if ( ! Util.hasImageExtension( filename ) ) {
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
         
         // Save the matrix values and the rest of the table to a data file         
         // change filename extension to .txt or whatever it is we use for data files
         filename = Util.changeExtension( filename, Util.DEFAULT_DATA_EXTENSION );
         //
         //saveTableToFile( filename );
         //
      }
      // else canceled by user
   }

   void m_normalizeMenuItem_actionPerformed( ActionEvent e ) {

      boolean normalize = m_normalizeMenuItem.isSelected();
      m_matrixDisplay.setStandardizedEnabled( normalize );
      m_table.repaint();
   }

   void m_matrixDisplayCellWidthSlider_stateChanged(ChangeEvent e) {

      JSlider source = ( JSlider ) e.getSource();

      //if ( ! source.getValueIsAdjusting() ) {

      // Adjust the width of every matrix display column
      int width = ( int ) source.getValue();
      if ( width >= MIN_WIDTH_MATRIXDISPLAY_COLUMN && width <= MAX_WIDTH_MATRIXDISPLAY_COLUMN ) {

         m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

         int matrixColumnCount = m_matrixDisplay.getColumnCount();
         for ( int i = 0; i < matrixColumnCount; i++ ) {
            TableColumn col = m_table.getColumnModel().getColumn( i );
            col.setResizable( false );
            col.setPreferredWidth( width );
         }
      }
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

class JDetailsFrame_m_saveFileMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_saveFileMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_saveFileMenuItem_actionPerformed( e );
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

class JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter implements javax.swing.event.ChangeListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter(JDetailsFrame adaptee) {
      this.adaptee = adaptee;
   }
   public void stateChanged(ChangeEvent e) {
      adaptee.m_matrixDisplayCellWidthSlider_stateChanged(e);
   }
}
