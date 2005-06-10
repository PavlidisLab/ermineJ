package classScore.gui.geneSet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.gui.ColorMap;
import baseCode.gui.GuiUtil;
import baseCode.gui.JGradientBar;
import baseCode.gui.JLinkLabel;
import baseCode.gui.JMatrixDisplay;
import baseCode.gui.StatusJlabel;
import baseCode.gui.table.JBarGraphCellRenderer;
import baseCode.gui.table.JMatrixCellRenderer;
import baseCode.gui.table.JVerticalHeaderRenderer;
import baseCode.gui.table.TableSorter;
import baseCode.io.reader.DoubleMatrixReader;
import baseCode.util.FileTools;
import baseCode.util.StatusViewer;
import classScore.GeneSetPvalRun;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Paul Pavlidis
 * @author Kiran Keshav
 * @author Will Braynen
 * @version $Id$
 */
public class JGeneSetFrame extends JFrame {
    protected static final Log log = LogFactory.getLog( JGeneSetFrame.class );
    private static final int COLOR_RANGE_SLIDER_MIN = 1;
    private static final int COLOR_RANGE_SLIDER_RESOLUTION = 12;

    private static final String INCLUDEEVERYTHING = "detailsview.savedata.includeEverything";
    private static final String INCLUDELABELS = "detailsview.saveimage.includeImageLabels";
    private static final String NORMALIZE_SAVED_IMAGE = "detailsview.saveimage.normalize";
    private static final String NORMALIZE_SAVED_DATA = "detailsview.savedata.normalize";
    private static final String MATRIXCOLUMNWIDTH = "detailsview.ColumnWidth";
    private static final String WINDOWHEIGHT = "detailsview.WindowHeight";
    private static final String WINDOWWIDTH = "detailsview.WindowWidth";
    private static final String WINDOWPOSITIONX = "detailsview.WindowXPosition";
    private static final String WINDOWPOSITIONY = "detailsview.WindowYPosition";

    private static final int MAX_WIDTH_MATRIXDISPLAY_COLUMN = 19;
    private static final int MIN_WIDTH_MATRIXDISPLAY_COLUMN = 1;
    private static final int PREFERRED__WIDTH_MATRIXDISPLAY_COLUMN = 9;
    private static final int NORMALIZED_COLOR_RANGE_MAX = 12; // [-6,6] standard deviations out
    private static final int PREFERRED_WIDTH_DESCRIPTION_COLUMN = 300;
    private static final int PREFERRED_WIDTH_GENENAME_COLUMN = 75;
    private static final int PREFERRED_WIDTH_PROBEID_COLUMN = 75;

    private static final int PREFERRED_WIDTH_PVALUE_COLUMN = 75;
    private static final int PREFERRED_WIDTH_PVALUEBAR_COLUMN = 75;
    private static final String SAVESTARTPATH = "detailsview.startPath";

    public JMatrixDisplay m_matrixDisplay = null;
    private GeneSetPvalRun analysisResults;
    private int width;
    private int height;
    private boolean includeEverything = true;

    private boolean includeLabels = true; // whether when saving data we include the row/column labels.
    private int matrixColumnWidth; // how wide the color image columns are.
    private Settings settings;
    private GeneSetTableModel tableModel;

    protected JTable table = new JTable();
    protected JScrollPane tableScrollPane = new JScrollPane();
    protected JToolBar toolBar = new JToolBar();
    private JMenu analysisMenu = new JMenu();
    private JRadioButtonMenuItem blackbodyColormapMenuItem = new JRadioButtonMenuItem();
    private JDataFileChooser fileChooser = null;
    private JMenu fileMenu = new JMenu();

    private JRadioButtonMenuItem greenredColormapMenuItem = new JRadioButtonMenuItem();
    private JImageFileChooser imageChooser = null;
    private JLabel m_cellWidthLabel = new JLabel();
    private JSlider m_cellWidthSlider = new JSlider();
    private JLabel m_colorRangeLabel = new JLabel();
    private JSlider m_colorRangeSlider = new JSlider();
    private JGradientBar m_gradientBar = new JGradientBar();
    private DecimalFormat m_nf = new DecimalFormat( "0.##E0" );
    private JCheckBoxMenuItem m_normalizeMenuItem = new JCheckBoxMenuItem();
    private Map m_pvaluesOrdinalPosition = new HashMap();
    private JMenuItem saveDataMenuItem = new JMenuItem();
    private JLabel m_spacerLabel = new JLabel();
    private JMenuItem m_viewHistMenuItem = new JMenuItem();
    /** controls the width of the cells in the matrix display */
    private JMenuBar menuBar = new JMenuBar();
    private JMenu optionsMenu = new JMenu();
    private JMenuItem saveImageMenuItem = new JMenuItem();
    private JMenuItem setGeneUrlBaseMenuItem = new JMenuItem();
    private JMenuItem switchDataFileMenuItem = new JMenuItem();
    private JMenuItem switchGeneScoreFileMenuItem = new JMenuItem();
    private JMenu viewMenu = new JMenu();
    private JPanel jPanelStatus = new JPanel();
    private JLabel jLabelStatus = new JLabel();
    private StatusJlabel statusMessenger = null;
    private Collection probesInGeneSet = null;
    private int matrixColumnCount = 0;
    private JMatrixCellRenderer matrixCellRenderer = null;
    private JVerticalHeaderRenderer verticalHeaderRenderer = null;
    private List probeIDs = null;
    private Map pvalues = null;
    private GeneAnnotations geneData = null;

    // private StatusViewer callerStatusViewer = null;

    /**
     * @param probeIDs an array of probe ID's that has some order; the actual order is arbitrary, as long as it is some
     *        order.
     * @param pvalues a map of probeID's to p values.
     * @param geneData holds gene names and descriptions which can be retrieved by probe ID.
     * @param settings <code>getRawFile()</code> should return the microarray file which contains the microarray data
     *        for the probe ID's contained in <code>probeIDs</code>.
     */
    public JGeneSetFrame( StatusViewer callerStatusViewer, List probeIDs, Map pvalues, GeneAnnotations geneData,
            Settings settings ) {
        try {
            if ( settings == null ) {
                log.warn( "Loading new settings..." );
                this.settings = new Settings();
            } else {
                this.settings = settings;
            }

            this.readPrefs();

            this.probeIDs = probeIDs;
            this.pvalues = pvalues;
            this.geneData = geneData;
            createDetailsTable();
            initChoosers();
            jbInit();
        } catch ( Exception e ) {
            if ( callerStatusViewer != null )
                callerStatusViewer.showError(
                        "There was an error while setting up the details view. Check the logs for details", e );
            GuiUtil.error( "There was an error setting up the details view: " + e.getMessage() == null ? "(no message)"
                    : e.getMessage() );
        }
    }

    /**
     * @param e
     */
    protected void closeWindow_actionPerformed( WindowEvent e ) {
        writePrefs();
        this.dispose();
    }

    /**
     * @param probeIDs
     * @param pvalues
     * @param geneData
     * @param filename
     */
    protected void createDetailsTable() {

        // create a probe set from probeIDs
        probesInGeneSet = new HashSet( probeIDs );
        DenseDoubleMatrix2DNamed matrix = setUpMatrixData();

        tableModel = new GeneSetTableModel( m_matrixDisplay, probeIDs, pvalues, m_pvaluesOrdinalPosition, geneData,
                m_nf, settings );
        TableSorter sorter = new TableSorter( tableModel, m_matrixDisplay );
        table.setModel( sorter );
        sorter.setTableHeader( table.getTableHeader() );

        setColumnWidths( matrixColumnCount, matrixCellRenderer, matrix, verticalHeaderRenderer );

        // Sort initially by the pvalue column
        if ( settings.getBigIsBetter() ) {
            sorter.setSortingStatus( matrixColumnCount + 1, TableSorter.DESCENDING );
        } else {
            sorter.setSortingStatus( matrixColumnCount + 1, TableSorter.ASCENDING );
        }

        // For the pvalue bar graph we need to know the ordinal position of each
        // pvalue in our list of pvalues, and now is the perfect time because
        // the table is sorted by pvalues
        for ( int i = 0; i < table.getRowCount(); i++ ) {
            String probeID = ( String ) table.getValueAt( i, matrixColumnCount );
            m_pvaluesOrdinalPosition.put( probeID, new Integer( i ) );
        }

        // Save the dimensions of the table just in case
        int totalWidth = matrixColumnCount * matrixColumnWidth + PREFERRED_WIDTH_PROBEID_COLUMN
                + PREFERRED_WIDTH_PVALUE_COLUMN + PREFERRED_WIDTH_GENENAME_COLUMN + PREFERRED_WIDTH_DESCRIPTION_COLUMN;
        int totalheight = table.getPreferredScrollableViewportSize().height;

        Dimension d = new Dimension( totalWidth, totalheight );
        table.setSize( d );

    } // end createDetailsTable

    /**
     * @param filename
     * @param probesInGeneSet
     * @return
     */
    private DenseDoubleMatrix2DNamed setUpMatrixData() {
        // Read the matrix data
        DoubleMatrixReader matrixReader = new DoubleMatrixReader();
        DenseDoubleMatrix2DNamed matrix = null;

        if ( ( settings.getRawDataFileName() == null || settings.getRawDataFileName().length() == 0 )
                && settings.getUserSetRawFile() ) {
            this.switchRawDataFile( true );
        }

        log.debug( "User set the raw data file? " + settings.getUserSetRawFile() );
        log.debug( "Raw data file is " + settings.getRawDataFileName() );

        /* I apologize for the convoluted logic here */
        if ( settings.getUserSetRawFile()
                || ( settings.getRawDataFileName() != null && settings.getRawDataFileName().length() > 0 ) ) {
            String filename = settings.getRawDataFileName();
            if ( ( new File( filename ) ).canRead() ) {
                try {
                    matrix = ( DenseDoubleMatrix2DNamed ) matrixReader.read( filename, probesInGeneSet );
                } catch ( IOException e ) {
                    GuiUtil.error( "Error loading raw microarray data from file " + filename + "\n"
                            + "Please make sure this file exists and the filename and directory path are correct,\n"
                            + "and that it is a valid raw data file (tab-delimited).\n" );
                }

                if ( matrix == null ) {
                    if ( statusMessenger != null )
                        statusMessenger.showError( "None of the probes in this gene set were in the data file." );
                } else {
                    m_matrixDisplay = new JMatrixDisplay( matrix );
                    m_matrixDisplay.setStandardizedEnabled( true );
                    // Make the columns in the matrix display not too wide (cell-size)
                    // and set a custom cell renderer
                    matrixCellRenderer = new JMatrixCellRenderer( m_matrixDisplay ); // create one instance
                    // that will be used to
                    // draw each cell
                }
            } else {
                GuiUtil.error( "The data file \"" + filename + "\" was not readable." + "\n"
                        + "Please make sure this file exists and the filename and directory path are correct,\n"
                        + "and that it is a valid raw data file (tab-delimited).\n" );
            }
        } else {
            m_matrixDisplay = null;
        }
        verticalHeaderRenderer = new JVerticalHeaderRenderer(); // create only one instance
        matrixColumnCount = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;

        return matrix;
    }

    /**
     * @param verticalHeaderRenderer
     * @param matrix
     * @param matrixCellRenderer
     * @param matrixColumnCount
     */
    private void setColumnWidths( int matrixColumnCount, TableCellRenderer matrixCellRenderer, Object matrix,
            TableCellRenderer verticalHeaderRenderer ) {

        // Set each column width and renderer.
        for ( int i = 0; i < matrixColumnCount; i++ ) {
            TableColumn col = table.getColumnModel().getColumn( i );
            col.setMinWidth( MIN_WIDTH_MATRIXDISPLAY_COLUMN );
            col.setMaxWidth( MAX_WIDTH_MATRIXDISPLAY_COLUMN );
            col.setCellRenderer( matrixCellRenderer );
            col.setHeaderRenderer( verticalHeaderRenderer );
            col.setPreferredWidth( PREFERRED__WIDTH_MATRIXDISPLAY_COLUMN );
        }

        if ( matrix != null ) // keshav added if (but not the guts)
            resizeMatrixColumns( matrixColumnWidth );

        //
        // Set up the rest of the table
        //
        TableColumn col;
        // probe ID
        col = table.getColumnModel().getColumn( matrixColumnCount + 0 );
        col.setPreferredWidth( PREFERRED_WIDTH_PROBEID_COLUMN );
        // p value
        col = table.getColumnModel().getColumn( matrixColumnCount + 1 );
        col.setPreferredWidth( PREFERRED_WIDTH_PVALUE_COLUMN );
        // p value bar
        col = table.getColumnModel().getColumn( matrixColumnCount + 2 );
        col.setPreferredWidth( PREFERRED_WIDTH_PVALUEBAR_COLUMN );
        col.setCellRenderer( new JBarGraphCellRenderer() );
        // name
        col = table.getColumnModel().getColumn( matrixColumnCount + 3 );
        col.setPreferredWidth( PREFERRED_WIDTH_GENENAME_COLUMN );
        // description
        col = table.getColumnModel().getColumn( matrixColumnCount + 4 );
        col.setPreferredWidth( PREFERRED_WIDTH_DESCRIPTION_COLUMN );
    }

    private String getProbeID( int row ) {
        int offset = m_matrixDisplay.getColumnCount(); // matrix display ends
        return ( String ) table.getValueAt( row, offset + 0 );
    }

    /**
     * 
     */
    private void initChoosers() {
        if ( m_matrixDisplay == null ) return;

        imageChooser = new JImageFileChooser( settings.getConfig().getBoolean( INCLUDELABELS, true ), settings
                .getConfig().getBoolean( NORMALIZE_SAVED_IMAGE, true ) );
        fileChooser = new JDataFileChooser( settings.getConfig().getBoolean( INCLUDEEVERYTHING, true ), settings
                .getConfig().getBoolean( NORMALIZE_SAVED_DATA, false ) );
        readPathPrefs();
    }

    private void initColorRangeWidget() {

        // init the slider
        m_colorRangeSlider.setMinimum( COLOR_RANGE_SLIDER_MIN );
        m_colorRangeSlider.setMaximum( COLOR_RANGE_SLIDER_RESOLUTION );

        double rangeMax;
        boolean normalized = m_matrixDisplay.getStandardizedEnabled();
        if ( normalized ) {
            rangeMax = NORMALIZED_COLOR_RANGE_MAX;
        } else {
            rangeMax = m_matrixDisplay.getMax() - m_matrixDisplay.getMin();
        }
        double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
        m_colorRangeSlider.setValue( ( int ) ( m_matrixDisplay.getDisplayRange() * zoomFactor ) );

        // init gradient bar
        double min = m_matrixDisplay.getDisplayMin();
        double max = m_matrixDisplay.getDisplayMax();
        m_gradientBar.setLabels( min, max );
    }

    private void jbInit() throws Exception {

        // Listener for window closing events.
        this.addWindowListener( new JGeneSetFrame_windowListenerAdapter( this ) );

        setupTable();
        setupMenus();
        setupToolBar();
        setUpStatusBar();
        setupWindow();
        repositionViewport();

        this.getContentPane().add( tableScrollPane, BorderLayout.CENTER );
        this.getContentPane().add( toolBar, BorderLayout.NORTH );
        this.getContentPane().add( jPanelStatus, BorderLayout.SOUTH );

        m_nf.setMaximumFractionDigits( 3 );
        if ( m_matrixDisplay != null ) {
            boolean isNormalized = m_matrixDisplay.getStandardizedEnabled();
            m_normalizeMenuItem.setSelected( isNormalized );
        } else {
            // matrixDisplay is null! Disable the menu
            setDisplayMatrixGUIEnabled( false );
        }
    }

    /**
     * 
     */
    private void setupTable() {
        // Enable the horizontal scroll bar
        table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        // Prevent user from moving columns around
        table.getTableHeader().setReorderingAllowed( false );
        // For html links on gene names
        table.addMouseListener( new JGeneSetFrame_m_mouseAdapter( this ) );
        table.addMouseMotionListener( new JGeneSetFrame_m_mouseMotionListener( this ) );
        // change the cursor to a hand over a header
        table.getTableHeader().addMouseListener( new JGeneSetFrameTableHeader_mouseAdapterCursorChanger( this ) );
        // Make sure the matrix display doesn't have a grid separating color cells.
        table.setIntercellSpacing( new Dimension( 0, 0 ) );
        // The rest of the table (text and value) should have a light gray grid
        table.setGridColor( Color.lightGray );
    }

    /**
     * 
     */
    private void repositionViewport() {
        tableScrollPane.getViewport().add( table, null );
        // Reposition the table inside the scrollpane
        int x = table.getSize().width;
        // should probably subtract the size of the viewport, but it gets trimmed
        // anyway,
        // so it's okay to be lazy here
        tableScrollPane.getViewport().setViewPosition( new Point( x, 0 ) );
        statusMessenger
                .showError( "You may need to scroll horizontally or adjust the column width to see all the data" );
    }

    /**
     * 
     */
    private void setupWindow() {

        this.setSize( this.width, this.height );
        log.debug( "Size: " + this.getWidth() + " X " + this.getHeight() );
        this.setResizable( true );

        try {
            int startX = ( int ) settings.getConfig().getDouble( WINDOWPOSITIONX );
            int startY = ( int ) settings.getConfig().getDouble( WINDOWPOSITIONY );
            this.setLocation( new Point( startX, startY ) );
        } catch ( NoSuchElementException e ) {
            this.setLocation( new Point( 100, 100 ) );
        }
        this.getContentPane().setLayout( new BorderLayout() );
        this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        this.setTitle( "Gene set details" );
    }

    /**
     * 
     */
    private void setUpStatusBar() {
        // status bar
        jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
        jPanelStatus.setPreferredSize( new Dimension( this.width, 33 ) );
        jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        jLabelStatus.setPreferredSize( new Dimension( this.width - 30, 19 ) );
        jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
        jPanelStatus.add( jLabelStatus, null );
        statusMessenger = new StatusJlabel( jLabelStatus );
    }

    /**
     * 
     */
    private void setupToolBar() {
        toolBar.setFloatable( false );
        m_cellWidthSlider.setInverted( false );
        m_cellWidthSlider.setMajorTickSpacing( 0 );
        m_cellWidthSlider.setMaximum( MAX_WIDTH_MATRIXDISPLAY_COLUMN );
        m_cellWidthSlider.setMinimum( MIN_WIDTH_MATRIXDISPLAY_COLUMN );
        m_cellWidthSlider.setValue( matrixColumnWidth );
        m_cellWidthSlider.setMinorTickSpacing( 3 );
        m_cellWidthSlider.setPaintLabels( false );
        m_cellWidthSlider.setPaintTicks( true );
        m_cellWidthSlider.setPaintTrack( true );
        m_cellWidthSlider.setPaintTicks( false );
        m_cellWidthSlider.setMaximumSize( new Dimension( 90, 24 ) );
        m_cellWidthSlider.setPreferredSize( new Dimension( 90, 24 ) );
        m_cellWidthSlider.addChangeListener( new JGeneSetFrame_m_cellWidthSlider_changeAdapter( this ) );
        this.setResizable( true );
        m_cellWidthLabel.setText( "Cell Width:" );
        m_spacerLabel.setText( "    " );
        m_colorRangeLabel.setText( "Color Range:" );
        m_gradientBar.setMaximumSize( new Dimension( 200, 30 ) );
        m_gradientBar.setPreferredSize( new Dimension( 120, 30 ) );
        if ( m_matrixDisplay != null ) {
            m_gradientBar.setColorMap( m_matrixDisplay.getColorMap() );
            initColorRangeWidget();
        }
        m_colorRangeSlider.setMaximumSize( new Dimension( 90, 24 ) );
        m_colorRangeSlider.setPreferredSize( new Dimension( 90, 24 ) );
        m_colorRangeSlider.addChangeListener( new JGeneSetFrame_m_colorRangeSlider_changeAdapter( this ) );
        toolBar.add( m_cellWidthLabel, null );
        toolBar.add( m_cellWidthSlider, null );
        toolBar.add( m_spacerLabel, null );
        toolBar.add( m_colorRangeLabel, null );
        toolBar.add( m_colorRangeSlider, null );
        toolBar.add( m_gradientBar, null );
    }

    /**
     * 
     */
    private void setupMenus() {

        this.setJMenuBar( menuBar );
        fileMenu.setText( "File" );
        fileMenu.add( saveImageMenuItem );
        fileMenu.add( saveDataMenuItem );

        greenredColormapMenuItem.setSelected( false );
        greenredColormapMenuItem.setText( "Green-Red" );
        greenredColormapMenuItem.addActionListener( new JGeneSetFrame_m_greenredColormapMenuItem_actionAdapter( this ) );
        greenredColormapMenuItem.addActionListener( new JGeneSetFrame_m_greenredColormapMenuItem_actionAdapter( this ) );
        viewMenu.setText( "View" );
        blackbodyColormapMenuItem.setSelected( true );
        blackbodyColormapMenuItem.setText( "Blackbody" );
        blackbodyColormapMenuItem
                .addActionListener( new JGeneSetFrame_m_blackbodyColormapMenuItem_actionAdapter( this ) );
        blackbodyColormapMenuItem
                .addActionListener( new JGeneSetFrame_m_blackbodyColormapMenuItem_actionAdapter( this ) );
        // Color map menu items (radio button group -- only one can be selected at one time)
        ButtonGroup group = new ButtonGroup();
        group.add( greenredColormapMenuItem );
        group.add( blackbodyColormapMenuItem );

        saveImageMenuItem.setActionCommand( "SaveImage" );
        saveImageMenuItem.setText( "Save Image..." );
        saveImageMenuItem.addActionListener( new JGeneSetFrame_m_saveImageMenuItem_actionAdapter( this ) );

        m_normalizeMenuItem.setText( "Normalize" );
        m_normalizeMenuItem.addActionListener( new JGeneSetFrame_m_normalizeMenuItem_actionAdapter( this ) );
        optionsMenu.setText( "Options" );
        setGeneUrlBaseMenuItem.setActionCommand( "Change gene name URL" );
        setGeneUrlBaseMenuItem.setText( "Change gene name URL..." );
        setGeneUrlBaseMenuItem.addActionListener( new JGeneSetFrame_viewGeneUrlDialog_actionAdapter( this ) );

        viewMenu.add( m_normalizeMenuItem );
        viewMenu.addSeparator();
        viewMenu.add( greenredColormapMenuItem );
        viewMenu.add( blackbodyColormapMenuItem );

        optionsMenu.add( setGeneUrlBaseMenuItem );
        optionsMenu.add( switchDataFileMenuItem );
        optionsMenu.add( switchGeneScoreFileMenuItem );

        analysisMenu.setText( "Analysis" );
        m_viewHistMenuItem.setActionCommand( "View Distribution" );
        m_viewHistMenuItem.setText( "View distribution" );
        m_viewHistMenuItem.addActionListener( new JGeneSetFrame_m_viewHistMenuItem_actionAdapter( this ) );
        analysisMenu.add( m_viewHistMenuItem );

        saveDataMenuItem.setActionCommand( "SaveData" );
        saveDataMenuItem.setText( "Save Data..." );
        saveDataMenuItem.addActionListener( new JGeneSetFrame_m_saveDataMenuItem_actionAdapter( this ) );

        switchDataFileMenuItem.setActionCommand( "Switch Data Shown" );
        switchDataFileMenuItem.setText( "Change Dataset..." );
        switchDataFileMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                switchRawDataFile();
                createDetailsTable();
                table.revalidate();
            }
        } );

        switchGeneScoreFileMenuItem.setActionCommand( "Change gene score file" );
        switchGeneScoreFileMenuItem.setText( "Change gene score file..." );
        switchGeneScoreFileMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                switchGeneScoreFile();
                createDetailsTable();
                table.revalidate();
            }
        } );

        if ( analysisResults == null || analysisResults.getHist() == null ) {
            analysisMenu.setEnabled( false );
        }

        menuBar.add( fileMenu );
        menuBar.add( viewMenu );
        menuBar.add( optionsMenu );
        menuBar.add( analysisMenu );
    }

    /**
     * 
     */
    protected void switchGeneScoreFile() {
        JFileChooser fchooser = new JFileChooser( settings.getDataDirectory() );
        fchooser.setDialogTitle( "Choose the gene score file or cancel." );
        int yesno = fchooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION )
            settings.setScoreFile( fchooser.getSelectedFile().getAbsolutePath() );

    }

    protected void switchRawDataFile() {
        this.switchRawDataFile( false );
    }

    /**
     * 
     */
    protected void switchRawDataFile( boolean showHelp ) {

        if ( showHelp ) {
            JOptionPane
                    .showMessageDialog(
                            this,
                            "You have requested to view the details of a gene set without first"
                                    + " setting an expression data file to display.\n After you close this window, you will be prompted to"
                                    + " choose one if you want, or proceed without selecting one.\n"
                                    + " You can select or change the data file used"
                                    + " from the 'Options' menu of the details view.\n", "Selecting a data file",
                            JOptionPane.INFORMATION_MESSAGE );
        }

        JFileChooser fc = new JFileChooser( settings.getDataDirectory() );
        fc.setDialogTitle( "Choose the expression data file or cancel." );
        int yesno = fc.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            settings.setRawFile( fc.getSelectedFile().getAbsolutePath() );
            settings.userSetRawFile( true );
        } else {
            settings.userSetRawFile( false );
        }
    }

    /**
     * @param out
     * @param matrixColumnCount
     * @param r
     * @throws IOException
     */
    private void printAnnotationsForRow( BufferedWriter out, int matrixColumnCount, int r ) throws IOException {
        int interestingStuffStartsAt = matrixColumnCount;
        int scoreColumn = interestingStuffStartsAt + 1;
        int symbolColumn = interestingStuffStartsAt + 3;
        int nameColumn = interestingStuffStartsAt + 4;
        out.write( "\t" + table.getValueAt( r, scoreColumn ).toString() );
        out.write( stripHtml( "\t" + table.getValueAt( r, symbolColumn ).toString() ) );
        out.write( "\t" + table.getValueAt( r, nameColumn ).toString() );
    }

    /**
     * @param includeMatrixValues
     * @param includeAnnotations
     * @param out
     * @param matrixColumnCount
     * @throws IOException
     */
    private void printHeader( boolean includeMatrixValues, boolean includeAnnotations, BufferedWriter out,
            int matrixColumnCount ) throws IOException {
        out.write( "Probe" );
        if ( includeAnnotations ) {// FIXME - this is not maintainable!
            out.write( "\tScore\tSymbol\tName" );
        }
        // write out column names
        if ( includeMatrixValues ) {
            for ( int c = 0; c < matrixColumnCount; c++ ) {
                String columnName = m_matrixDisplay.getColumnName( c );
                out.write( "\t" + columnName );
            }
        }
        out.write( System.getProperty( "line.separator" ) );
    }

    /**
     * @param out
     * @param nf
     * @param probeID
     * @throws IOException
     */
    private void printMatrixValueForRow( BufferedWriter out, DecimalFormat nf, String probeID ) throws IOException {
        double[] row = m_matrixDisplay.getRowByName( probeID );
        for ( int c = 0; c < row.length; c++ ) {
            out.write( "\t" + nf.format( row[c] ) );
        }
    }

    /**
     * 
     *
     */
    private void readPathPrefs() {
        if ( settings == null ) return;
        if ( settings.getConfig().containsKey( SAVESTARTPATH ) && m_matrixDisplay != null ) {
            if ( fileChooser == null || imageChooser == null ) initChoosers();
            this.fileChooser.setCurrentDirectory( new File( settings.getConfig().getString( SAVESTARTPATH ) ) );
            this.imageChooser.setCurrentDirectory( new File( settings.getConfig().getString( SAVESTARTPATH ) ) );
        }
    }

    /**
     *
     *
     */
    private void readPrefs() {

        if ( settings == null ) {
            return;
        }

        width = 800;
        height = table.getHeight();
        matrixColumnWidth = PREFERRED__WIDTH_MATRIXDISPLAY_COLUMN;

        if ( settings.getConfig() == null ) return;

        if ( settings.getConfig().containsKey( WINDOWWIDTH ) ) {
            this.width = Integer.parseInt( settings.getConfig().getString( WINDOWWIDTH ) );
            log.debug( "Got: " + width );
        }

        if ( settings.getConfig().containsKey( WINDOWHEIGHT ) ) {
            this.height = Integer.parseInt( settings.getConfig().getString( WINDOWHEIGHT ) );
            log.debug( "Got: " + height );
        }
        if ( settings.getConfig().containsKey( MATRIXCOLUMNWIDTH ) ) {
            this.matrixColumnWidth = Integer.parseInt( settings.getConfig().getString( MATRIXCOLUMNWIDTH ) );
            log.debug( "Got: " + matrixColumnWidth );
        }
        if ( settings.getConfig().containsKey( INCLUDELABELS ) ) {
            this.includeLabels = Boolean.getBoolean( settings.getConfig().getString( INCLUDELABELS ) );
            log.debug( "Got: " + includeLabels );
        }
        if ( settings.getConfig().containsKey( INCLUDEEVERYTHING ) ) {
            this.includeEverything = Boolean.getBoolean( settings.getConfig().getString( INCLUDEEVERYTHING ) );
            log.debug( "Got: " + includeEverything );
        }

    }

    private void setDisplayMatrixGUIEnabled( boolean enabled ) {
        if ( settings == null ) return;
        // the menu
        menuBar.setEnabled( enabled );
        fileMenu.setEnabled( enabled );
        viewMenu.setEnabled( enabled );
        optionsMenu.setEnabled( true );
        if ( settings.getClassScoreMethod() == Settings.ORA ) {
            analysisMenu.setEnabled( false );
        } else {
            analysisMenu.setEnabled( true );
        }

        // the toolbar
        toolBar.setEnabled( enabled );

        // the sliders
        m_cellWidthSlider.setEnabled( enabled );
        m_cellWidthLabel.setEnabled( enabled );
        m_colorRangeSlider.setEnabled( enabled );
        m_colorRangeLabel.setEnabled( enabled );
        m_gradientBar.setVisible( enabled );
    }

    /**
     * Ridiculously simple to remove tags.
     * 
     * @param s
     * @return
     */
    private String stripHtml( String s ) {
        String m = s.replaceAll( "<.+?>", "" );
        m = m.replaceAll( "</.+?>", "" );
        return m;
    }

    /**
     * Writes setting values to file.
     */
    private void writePrefs() {
        settings.getConfig().setProperty( WINDOWWIDTH, String.valueOf( this.getWidth() ) );
        settings.getConfig().setProperty( WINDOWHEIGHT, String.valueOf( this.getHeight() ) );
        settings.getConfig().setProperty( MATRIXCOLUMNWIDTH, String.valueOf( this.matrixColumnWidth ) );
        settings.getConfig().setProperty( INCLUDELABELS, String.valueOf( this.includeLabels ) );
        settings.getConfig().setProperty( INCLUDEEVERYTHING, String.valueOf( this.includeEverything ) );
        settings.getConfig().setProperty( WINDOWPOSITIONX, new Double( this.getLocation().getX() ) );
        settings.getConfig().setProperty( WINDOWPOSITIONY, new Double( this.getLocation().getY() ) );
        try {
            settings.getConfig().save();
        } catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Creates new row keys for the JMatrixDisplay object (m_matrixDisplay). You would probably want to call this method
     * to print out the matrix in the order in which it is displayed in the table. In this case, you will want to do
     * something like this: <br>
     * <br>
     * <code>m_matrixDisplay.setRowKeys( getCurrentMatrixDisplayRowOrder() );</code>
     * <p>
     * However, do not forget to call
     * </p>
     * <code>m_matrixDisplay.resetRowKeys()</code>
     * <p>
     * when you are done because the table sorter filter does its own mapping, so the matrix rows have to remain in
     * their original order (or it might not be displayed correctly inside the table).
     */
    protected int[] getCurrentMatrixDisplayRowOrder() {

        int matrixRowCount = m_matrixDisplay.getRowCount();
        int[] rowKeys = new int[matrixRowCount];

        // write out the table, one row at a time
        for ( int r = 0; r < matrixRowCount; r++ ) {
            // for this row: write out matrix values
            String probeID = getProbeID( r );
            rowKeys[r] = m_matrixDisplay.getRowIndexByName( probeID );
        }

        return rowKeys;

    } // end createRowKeys

    /**
     * fixme - this should not be in the gui.
     * 
     * @param filename
     * @param includeMatrixValues
     * @param includeNonMatrix
     * @param normalized
     * @throws IOException
     */
    protected void saveData( String filename, boolean includeMatrixValues, boolean includeAnnotations,
            boolean normalized ) throws IOException {

        final String NEWLINE = System.getProperty( "line.separator" );

        File outputFile = new File( filename );

        BufferedWriter out = new BufferedWriter( new FileWriter( outputFile ) );

        boolean isStandardized = m_matrixDisplay.getStandardizedEnabled();
        m_matrixDisplay.setStandardizedEnabled( normalized );

        int totalRowCount = table.getRowCount();
        int matrixColumnCount = m_matrixDisplay.getColumnCount();

        printHeader( includeMatrixValues, includeAnnotations, out, matrixColumnCount );

        DecimalFormat nf;
        nf = new DecimalFormat();
        nf.setMaximumFractionDigits( 8 );
        nf.setMinimumFractionDigits( 3 );
        // write out the table, one row at a time
        for ( int r = 0; r < totalRowCount; r++ ) {

            String probeID = getProbeID( r );
            out.write( probeID );

            if ( includeAnnotations ) {
                printAnnotationsForRow( out, matrixColumnCount, r );
            }

            if ( includeMatrixValues ) {
                printMatrixValueForRow( out, nf, probeID );
            }
            out.write( NEWLINE );
        }

        m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state

        // close the file
        out.close();

    } // end saveData

    protected void saveImage( String filename, boolean includeLabels, boolean normalized ) throws IOException {

        boolean isStandardized = m_matrixDisplay.getStandardizedEnabled();
        m_matrixDisplay.setStandardizedEnabled( normalized );
        m_matrixDisplay.setRowKeys( getCurrentMatrixDisplayRowOrder() );
        try {
            m_matrixDisplay.saveImage( filename, includeLabels, normalized );
        } catch ( IOException e ) {
            // clean up
            m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
            m_matrixDisplay.resetRowKeys();
            throw e;
        }

        // clean up

        m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
        m_matrixDisplay.resetRowKeys();

    } // end saveImage

    void m_blackbodyColormapMenuItem_actionPerformed( ActionEvent e ) {

        try {
            Color[] colorMap = ColorMap.BLACKBODY_COLORMAP;
            m_matrixDisplay.setColorMap( colorMap );
            m_gradientBar.setColorMap( colorMap );
            table.repaint();
        } catch ( Exception ex ) {
        }

    }

    void m_cellWidthSlider_stateChanged( ChangeEvent e ) {
        JSlider source = ( JSlider ) e.getSource();
        int v = source.getValue();
        resizeMatrixColumns( v );
    }

    void m_colorRangeSlider_stateChanged( ChangeEvent e ) {

        JSlider source = ( JSlider ) e.getSource();
        double value = source.getValue();

        double displayMin, displayMax;
        boolean normalized = m_matrixDisplay.getStandardizedEnabled();
        if ( normalized ) {
            double rangeMax = NORMALIZED_COLOR_RANGE_MAX;
            double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
            double range = value / zoomFactor;
            displayMin = -( range / 2 );
            displayMax = +( range / 2 );
        } else {
            double rangeMax = m_matrixDisplay.getMax() - m_matrixDisplay.getMin();
            double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
            double range = value / zoomFactor;
            double midpoint = m_matrixDisplay.getMax() - ( rangeMax / 2 );
            displayMin = midpoint - ( range / 2 );
            displayMax = midpoint + ( range / 2 );
        }

        m_gradientBar.setLabels( displayMin, displayMax );
        m_matrixDisplay.setDisplayRange( displayMin, displayMax );
        table.repaint();
    }

    void m_greenredColormapMenuItem_actionPerformed( ActionEvent e ) {

        try {
            Color[] colorMap = ColorMap.GREENRED_COLORMAP;
            m_matrixDisplay.setColorMap( colorMap );
            m_gradientBar.setColorMap( colorMap );
            table.repaint();
        } catch ( Exception ex ) {
        }

    }

    void m_normalizeMenuItem_actionPerformed( ActionEvent e ) {

        boolean normalize = m_normalizeMenuItem.isSelected();
        m_matrixDisplay.setStandardizedEnabled( normalize );

        initColorRangeWidget();
        table.repaint();
    }

    void m_saveDataMenuItem_actionPerformed( ActionEvent e ) {
        initChoosers();
        int returnVal = fileChooser.showSaveDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {

            File file = fileChooser.getSelectedFile();

            if ( file.exists() ) {
                returnVal = JOptionPane.showConfirmDialog( null, "File exists. Overwrite?" );
                if ( returnVal != JOptionPane.OK_OPTION ) return;
            }

            includeEverything = fileChooser.includeEverything();
            boolean normalize = fileChooser.normalized();
            settings.setProperty( INCLUDEEVERYTHING, new Boolean( includeEverything ) );
            settings.setProperty( NORMALIZE_SAVED_DATA, new Boolean( normalize ) );
            settings.writePrefs();

            String filename = file.getPath();

            // Save the values
            try {
                saveData( filename, true, includeEverything, normalize );
            } catch ( IOException ex ) {
                GuiUtil.error( "There was an error saving the data to " + filename + "." );
            }
            settings.getConfig().setProperty( SAVESTARTPATH, fileChooser.getCurrentDirectory().getAbsolutePath() );
        }
        // else canceled by user
    }

    void m_saveImageMenuItem_actionPerformed( ActionEvent e ) {
        initChoosers();
        int returnVal = imageChooser.showSaveDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {

            File file = imageChooser.getSelectedFile();

            if ( file.exists() ) {
                returnVal = JOptionPane.showConfirmDialog( null, "File exists. Overwrite?" );
                if ( returnVal != JOptionPane.OK_OPTION ) return;
            }

            includeLabels = imageChooser.includeLabels();
            boolean normalize = imageChooser.normalized();
            settings.setProperty( INCLUDELABELS, new Boolean( includeLabels ) );
            settings.setProperty( NORMALIZE_SAVED_IMAGE, new Boolean( normalize ) );
            settings.writePrefs();
            
            // Make sure the filename has an image extension
            String filename = file.getPath();

            if ( !FileTools.hasImageExtension( filename ) ) {
                filename = FileTools.addImageExtension( filename );
            }
            // Save the color matrix image
            try {
                saveImage( filename, includeLabels, normalize );
            } catch ( IOException ex ) {
                GuiUtil.error( "There was an error saving the data to " + filename + "." );
            }
            settings.getConfig().setProperty( SAVESTARTPATH, imageChooser.getCurrentDirectory().getAbsolutePath() );
        }
        // else canceled by user
    }

    /**
     * @param e
     */
    void m_viewHistMenuItem_actionPerformed( ActionEvent e ) {
        // if ( analysisResults != null ) {
        // JHistViewer f = new JHistViewer( analysisResults.getHist(), classResults.getEffectiveSize(), classResults
        // .getScore() );
        // f.setTitle( this.getTitle() + " histogram" );
        // f.pack();
        // f.show();
        // }
    }

    /**
     * Adjust the width of every matrix display column
     * 
     * @param desiredCellWidth
     */
    void resizeMatrixColumns( int desiredCellWidth ) {
        if ( desiredCellWidth >= MIN_WIDTH_MATRIXDISPLAY_COLUMN && desiredCellWidth <= MAX_WIDTH_MATRIXDISPLAY_COLUMN ) {

            table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

            int matrixColumnCount = m_matrixDisplay.getColumnCount();
            for ( int i = 0; i < matrixColumnCount; i++ ) {
                TableColumn col = table.getColumnModel().getColumn( i );
                col.setResizable( false );
                col.setPreferredWidth( desiredCellWidth );
            }
            this.matrixColumnWidth = desiredCellWidth; // copy value into our parameter.
        }
    }

    void table_mouseExited( MouseEvent e ) {
        resetUrl();
    }

    /**
     * 
     */
    private void resetUrl() {
        setCursor( Cursor.getDefaultCursor() );
        statusMessenger.clear();
    }

    void table_mouseMoved( MouseEvent e ) {
        int i = table.rowAtPoint( e.getPoint() );
        int j = table.columnAtPoint( e.getPoint() );
        if ( onGeneSymbolCell( i, j ) ) {
            setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            if ( table.getValueAt( i, j ) instanceof JLinkLabel ) {
                JLinkLabel geneLink = ( JLinkLabel ) table.getValueAt( i, j );
                if ( geneLink.getURL() != null ) statusMessenger.setStatus( geneLink.getURL(), false );
            } else {
                statusMessenger.setStatus( table.getValueAt( i, j ).toString(), false );
            }

        } else {
            resetUrl();
        }
    }

    void table_mouseReleased( MouseEvent e ) {
        if ( SwingUtilities.isLeftMouseButton( e ) ) {
            int i = table.getSelectedRow();
            int j = table.getSelectedColumn();
            if ( onGeneSymbolCell( i, j ) ) {
                if ( table.getValueAt( i, j ) instanceof JLinkLabel ) {
                    JLinkLabel geneLink = ( JLinkLabel ) table.getValueAt( i, j );
                    if ( geneLink != null ) geneLink.mouseClicked( e );
                } else {
                    statusMessenger.setStatus( table.getValueAt( i, j ).toString(), false );
                }
            }
        }
    }

    /**
     * @param i
     * @param j
     * @return
     */
    private boolean onGeneSymbolCell( int i, int j ) {
        if ( i < 0 || j < 0 ) return false;
        return table.getValueAt( i, j ) != null && j == table.getColumnCount() - 2;
    }

    /**
     * @param e
     */
    void viewGeneUrlDialogMenuItem_actionPerformed( ActionEvent e ) {
        GeneUrlDialog d = new GeneUrlDialog( this, settings, this.tableModel );
        d.setVisible( true );
    }

} // end class JGeneSetFrame

class JGeneSetFrame_m_blackbodyColormapMenuItem_actionAdapter implements java.awt.event.ActionListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_blackbodyColormapMenuItem_actionAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.m_blackbodyColormapMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_m_cellWidthSlider_changeAdapter implements javax.swing.event.ChangeListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_cellWidthSlider_changeAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void stateChanged( ChangeEvent e ) {
        adaptee.m_cellWidthSlider_stateChanged( e );
    }
}

class JGeneSetFrame_m_colorRangeSlider_changeAdapter implements javax.swing.event.ChangeListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_colorRangeSlider_changeAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void stateChanged( ChangeEvent e ) {
        adaptee.m_colorRangeSlider_stateChanged( e );
    }
}

class JGeneSetFrame_m_greenredColormapMenuItem_actionAdapter implements java.awt.event.ActionListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_greenredColormapMenuItem_actionAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.m_greenredColormapMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_m_mouseAdapter extends java.awt.event.MouseAdapter {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_mouseAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void mouseEntered( MouseEvent e ) {
        // adaptee.table_mouseEntered(e);
    }

    public void mouseExited( MouseEvent e ) {
        adaptee.table_mouseExited( e );
    }

    public void mouseReleased( MouseEvent e ) {
        adaptee.table_mouseReleased( e );
    }

}

class JGeneSetFrame_m_mouseMotionListener implements java.awt.event.MouseMotionListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_mouseMotionListener( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void mouseDragged( MouseEvent e ) {
    }

    public void mouseMoved( MouseEvent e ) {
        adaptee.table_mouseMoved( e );
    }
}

class JGeneSetFrame_m_normalizeMenuItem_actionAdapter implements java.awt.event.ActionListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_normalizeMenuItem_actionAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.m_normalizeMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_m_saveDataMenuItem_actionAdapter implements java.awt.event.ActionListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_saveDataMenuItem_actionAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.m_saveDataMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_m_saveImageMenuItem_actionAdapter implements java.awt.event.ActionListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_saveImageMenuItem_actionAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.m_saveImageMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_m_viewHistMenuItem_actionAdapter implements java.awt.event.ActionListener {
    JGeneSetFrame adaptee;

    JGeneSetFrame_m_viewHistMenuItem_actionAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.m_viewHistMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_viewGeneUrlDialog_actionAdapter implements java.awt.event.ActionListener {
    JGeneSetFrame adaptee;

    /**
     * @param adaptee
     */
    public JGeneSetFrame_viewGeneUrlDialog_actionAdapter( JGeneSetFrame adaptee ) {
        super();
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.viewGeneUrlDialogMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_windowListenerAdapter extends java.awt.event.WindowAdapter {
    JGeneSetFrame adaptee;

    JGeneSetFrame_windowListenerAdapter( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void windowClosing( WindowEvent e ) {
        adaptee.closeWindow_actionPerformed( e );
    }
}

class JGeneSetFrameTableHeader_mouseAdapterCursorChanger extends java.awt.event.MouseAdapter {
    JGeneSetFrame adaptee;

    JGeneSetFrameTableHeader_mouseAdapterCursorChanger( JGeneSetFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void mouseEntered( MouseEvent e ) {
        adaptee.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
    }

    public void mouseExited( MouseEvent e ) {
        adaptee.setCursor( Cursor.getDefaultCursor() );
    }

}
