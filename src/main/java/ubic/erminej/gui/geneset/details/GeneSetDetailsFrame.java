/*
 * The ermineJ project
 *
 * Copyright (c) 2006 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej.gui.geneset.details;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.graphics.ColorMap;
import ubic.basecode.graphics.JGradientBar;
import ubic.basecode.graphics.MatrixDisplay;
import ubic.basecode.util.BrowserLauncher;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Element;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetDetails;
import ubic.erminej.data.GeneSetDetailsImageWriter;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.table.JBarGraphCellRenderer;
import ubic.erminej.gui.table.JMatrixCellRenderer;
import ubic.erminej.gui.table.JVerticalHeaderRenderer;
import ubic.erminej.gui.util.Colors;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.JLinkLabel;
import ubic.erminej.gui.util.StatusJlabel;

/**
 * <p>
 * GeneSetDetailsFrame class.
 * </p>
 *
 * @author Paul Pavlidis
 * @author Kiran Keshav
 * @author Will Braynen
 * @version $Id$
 */
public class GeneSetDetailsFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    /** Constant <code>log</code> */
    protected static final Log log = LogFactory.getLog( GeneSetDetailsFrame.class );
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
    private static final int PREFERRED_WIDTH_GENENAME_COLUMN = 95;
    private static final int PREFERRED_WIDTH_PROBEID_COLUMN = 75;

    private static final int PREFERRED_WIDTH_PVALUE_COLUMN = 75;
    private static final int PREFERRED_WIDTH_PVALUEBAR_COLUMN = 75;
    private static final String SAVESTARTPATH = "detailsview.startPath";
    private static final int PREFERRED_WIDTH_MULTIFUNCTIONALITY_COLUMN = 75;
    private static final int PREFERRED_WIDTH_MULTIFUNCTIONALITY_QQCOLUMN = 55;

    /** Constant <code>MAX_GENES_FOR_DETAIL_VIEWING=1000</code> */
    public static final int MAX_GENES_FOR_DETAIL_VIEWING = 1000;
    /** Constant <code>GEMMA_GENE_SEARCH_URL_BASE="http://www.chibi.ubc.ca/Gemma/searcher."{trunked}</code> */
    protected static final String GEMMA_GENE_SEARCH_URL_BASE = "http://www.chibi.ubc.ca/Gemma/searcher.html?scope=G&query=";

    private int width;
    private int height;
    private boolean includeAnnotations = true;
    private boolean includeLabels = true; // whether when saving data we include the row/column labels.
    private boolean includeScalebar = false;
    private int matrixColumnWidth; // how wide the color image columns are.

    private final JTable table = new JTable();
    private JScrollPane tableScrollPane = new JScrollPane();
    private JToolBar toolBar = new JToolBar();

    private JRadioButtonMenuItem blackbodyColormapMenuItem = new JRadioButtonMenuItem();
    private DetailsOutputDataFileChooser fileChooser = null;
    private JMenu fileMenu = new JMenu();

    private JRadioButtonMenuItem greenredColormapMenuItem = new JRadioButtonMenuItem();
    private DetailsOutputImageFileChooser imageChooser = null;
    private JLabel m_cellWidthLabel = new JLabel();
    private JSlider m_cellWidthSlider = new JSlider();
    private JLabel m_colorRangeLabel = new JLabel();
    private JSlider m_colorRangeSlider = new JSlider();
    private JGradientBar colorScaleBar = new JGradientBar();
    private JCheckBoxMenuItem m_normalizeMenuItem = new JCheckBoxMenuItem();
    private JMenuItem saveDataMenuItem = new JMenuItem();

    private JMenuBar menuBar = new JMenuBar();
    private JMenu optionsMenu = new JMenu();
    private JMenuItem saveImageMenuItem = new JMenuItem();
    private JMenuItem setGeneUrlBaseMenuItem = new JMenuItem();
    private JMenuItem switchDataFileMenuItem = new JMenuItem();
    private JMenuItem switchGeneScoreFileMenuItem = new JMenuItem();
    private JMenu viewMenu = new JMenu();
    private JPanel jPanelStatus = new JPanel();
    private JLabel jLabelStatus = new JLabel();
    private StatusViewer statusMessenger = null;
    private JMatrixCellRenderer matrixCellRenderer = null;
    private JVerticalHeaderRenderer verticalHeaderRenderer = null;

    private Settings settings;
    private GeneSetDetails geneSetDetails;
    private int matrixColumnCount = 0;
    public MatrixDisplay<Element, String> matrixDisplay = null;
    private boolean normalizeSavedData = false;
    private boolean normalizeSavedImage = true;
    private GeneSetDetailsTableModel tableModel;
    private SettingsHolder runSettings;

    // private StatusViewer callerStatusViewer = null;

    // TODO make these a setting that persists across sessions
    private Color[] colorMap = ColorMap.BLACKBODY_COLORMAP;

    private boolean normalizeMatrixView = true;

    /**
     * <p>
     * Constructor for GeneSetDetailsFrame.
     * </p>
     *
     * @param geneSetDetails a {@link ubic.erminej.data.GeneSetDetails} object.
     * @param messenger a {@link ubic.basecode.util.StatusViewer} object.
     */
    public GeneSetDetailsFrame( GeneSetDetails geneSetDetails, StatusViewer messenger ) {
        this.geneSetDetails = geneSetDetails;
        this.statusMessenger = messenger;

        if ( geneSetDetails.getSettings() == null ) {
            log.warn( "Loading new settings..." );
            try {
                this.settings = new Settings( true );
            } catch ( IOException e ) {
                log.fatal( "Failed to get settings: " + e.getMessage() );
                log.debug( e, e );
                return;
            }
        } else {
            this.settings = geneSetDetails.getSettings();
        }

        this.runSettings = geneSetDetails.getRunSettings();

        readPrefs();
        createDetailsTable();
        initChoosers();
        jbInit();
    }

    void m_blackbodyColormapMenuItem_actionPerformed() {

        try {
            colorMap = ColorMap.BLACKBODY_COLORMAP;
            matrixDisplay.setColorMap( colorMap );
            colorScaleBar.setColorMap( colorMap );
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
        boolean normalized = matrixDisplay.getStandardizedEnabled();
        if ( normalized ) {
            double rangeMax = NORMALIZED_COLOR_RANGE_MAX;
            double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
            double range = value / zoomFactor;
            displayMin = -( range / 2 );
            displayMax = +( range / 2 );
        } else {
            double rangeMax = matrixDisplay.getMax() - matrixDisplay.getMin();
            double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
            double range = value / zoomFactor;
            double midpoint = matrixDisplay.getMax() - ( rangeMax / 2 );
            displayMin = midpoint - ( range / 2 );
            displayMax = midpoint + ( range / 2 );
        }

        colorScaleBar.setLabels( displayMin, displayMax );
        matrixDisplay.setDisplayRange( displayMin, displayMax );
        table.repaint();
    }

    void m_greenredColormapMenuItem_actionPerformed( @SuppressWarnings("unused") ActionEvent e ) {

        try {
            colorMap = ColorMap.GREENRED_COLORMAP;
            matrixDisplay.setColorMap( colorMap );
            colorScaleBar.setColorMap( colorMap );
            table.repaint();
        } catch ( Exception ex ) {
        }

    }

    void m_normalizeMenuItem_actionPerformed() {

        normalizeMatrixView = m_normalizeMenuItem.isSelected();
        matrixDisplay.setStandardizedEnabled( normalizeMatrixView );

        initColorRangeWidget();
        table.repaint();
    }

    void m_saveImageMenuItem_actionPerformed() {
        if ( matrixDisplay == null ) return;
        initChoosers();
        int returnVal = imageChooser.showSaveDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {

            File file = imageChooser.getSelectedFile();

            if ( file.exists() ) {
                returnVal = JOptionPane.showConfirmDialog( null, "File exists. Overwrite?" );
                if ( returnVal != JOptionPane.OK_OPTION ) return;
            }

            this.includeLabels = imageChooser.includeLabels();
            this.normalizeSavedImage = imageChooser.normalized();
            settings.setProperty( INCLUDELABELS, new Boolean( includeLabels ) );
            settings.setProperty( NORMALIZE_SAVED_IMAGE, new Boolean( normalizeSavedImage ) );
            settings.writePrefs();

            // Make sure the filename has an image extension
            String filename = file.getPath();

            if ( !FileTools.hasImageExtension( filename ) ) {
                filename = FileTools.addImageExtension( filename );
            }
            // Save the color matrix image
            try {
                saveImage( filename, normalizeSavedImage );
            } catch ( IOException ex ) {
                GuiUtil.error( "There was an error saving the data to " + filename + "." );
            }
            settings.getConfig().setProperty( SAVESTARTPATH, imageChooser.getCurrentDirectory().getAbsolutePath() );
        }
        // else canceled by user
    }

    /**
     * Adjust the width of every matrix display column
     *
     * @param desiredCellWidth
     */
    void resizeMatrixColumns( int desiredCellWidth ) {
        if ( matrixDisplay == null ) return;
        if ( desiredCellWidth >= MIN_WIDTH_MATRIXDISPLAY_COLUMN && desiredCellWidth <= MAX_WIDTH_MATRIXDISPLAY_COLUMN ) {

            // table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

            int numColumns = matrixDisplay.getColumnCount();
            for ( int i = 0; i < numColumns; i++ ) {
                TableColumn col = table.getColumnModel().getColumn( i );
                col.setResizable( false );
                col.setPreferredWidth( desiredCellWidth );
            }
            this.matrixColumnWidth = desiredCellWidth; // copy value into our parameter.
        }
    }

    void saveDataAction() {
        initChoosers();
        int returnVal = fileChooser.showSaveDialog( this );
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {

            File file = fileChooser.getSelectedFile();

            if ( file.exists() ) {
                returnVal = JOptionPane.showConfirmDialog( null, "File exists. Overwrite?" );
                if ( returnVal != JOptionPane.OK_OPTION ) return;
            }

            this.includeAnnotations = fileChooser.includeAnnotations() || matrixDisplay == null;// always save
            // _something_
            this.normalizeSavedData = fileChooser.normalized();
            settings.setProperty( INCLUDEEVERYTHING, new Boolean( includeAnnotations ) );
            settings.setProperty( NORMALIZE_SAVED_DATA, new Boolean( normalizeSavedData ) );
            settings.writePrefs();

            String filename = file.getPath();

            // Save the values
            try {
                saveData( filename, matrixDisplay != null, includeAnnotations, normalizeSavedData );
            } catch ( IOException ex ) {
                GuiUtil.error( "There was an error saving the data to " + filename + "." );
            }
            settings.getConfig().setProperty( SAVESTARTPATH, fileChooser.getCurrentDirectory().getAbsolutePath() );
        }
        // else canceled by user
    }

    void showPopupMenu( MouseEvent e ) {
        if ( e.getSource() instanceof JTable ) {
            JTable source = ( JTable ) e.getSource();
            assert source != null;
            int r = source.rowAtPoint( e.getPoint() );

            r = table.getRowSorter().convertRowIndexToModel( r );

            final Element p = ( ( GeneSetDetailsTableModel ) source.getModel() ).getProbeAtRow( r );

            JPopupMenu pm = new JPopupMenu();

            JMenuItem gemmaSearchMenuItem = new JMenuItem( "Search Gemma for " + p.getGene().getSymbol() );
            gemmaSearchMenuItem.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed( @SuppressWarnings("hiding") ActionEvent e ) {
                    Gene g = p.getGene();
                    g.getSymbol();
                    openUrlForGene( GEMMA_GENE_SEARCH_URL_BASE, g );
                }
            } );
            pm.add( gemmaSearchMenuItem );

            pm.show( e.getComponent(), e.getX(), e.getY() );

        }
    }

    void table_mouseExited() {
        resetUrl();
    }

    void table_mouseMoved( MouseEvent e ) {
        int i = table.rowAtPoint( e.getPoint() );
        int j = table.columnAtPoint( e.getPoint() );
        if ( onGeneSymbolCell( i, j ) ) {
            setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            Object values = table.getValueAt( i, j );
            if ( values instanceof JLinkLabel ) {
                JLinkLabel geneLink = ( JLinkLabel ) values;
                String url = geneLink.getURL();
                if ( StringUtils.isNotBlank( url ) ) statusMessenger.showStatus( url, false );
            } else {
                statusMessenger.showStatus( values.toString(), false );
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
                    // Events on cells are apparently not propagated to the children.
                    if ( geneLink != null ) geneLink.openUrl();
                } else {
                    // show the link.
                    statusMessenger.showStatus( table.getValueAt( i, j ).toString(), false );
                }
            } else if ( onMultifunctionalityCell( i, j ) ) {
                // possibly update status bar.
            }
        }
        /** {@inheritDoc} */
    }

    /**
     * @param e
     */
    void viewGeneUrlDialogMenuItem_actionPerformed() {
        GeneUrlDialog geneUrlDialog = new GeneUrlDialog( settings );
        String url = geneUrlDialog.getUrl();
        settings.setGeneUrlBase( url );
        tableModel.configure();
    }

    /**
     * <p>
     * closeWindow_actionPerformed.
     * </p>
     *
     * @param e a {@link java.awt.event.WindowEvent} object.
     */
    protected void closeWindow_actionPerformed( WindowEvent e ) {
        writePrefs();
        this.dispose();
    }

    /**
     * <p>
     * createDetailsTable.
     * </p>
     */
    protected void createDetailsTable() {

        setUpMatrixView(); // sets up matrixDisplay.

        tableModel = new GeneSetDetailsTableModel( matrixDisplay, geneSetDetails, settings );
        table.setModel( tableModel );

        // table.setAutoCreateRowSorter( true );
        TableRowSorter<GeneSetDetailsTableModel> sorter = new TableRowSorter<>(
                ( GeneSetDetailsTableModel ) table.getModel() );
        table.setRowSorter( sorter );

        setupColumns();

        // Sort initially by the score column, based on the user's original input (so we don't use -log-transformed
        // values)
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        if ( settings.getBigIsBetter() ) {
            sortKeys.add( new RowSorter.SortKey( matrixColumnCount + 1, SortOrder.DESCENDING ) );
        } else {
            sortKeys.add( new RowSorter.SortKey( matrixColumnCount + 1, SortOrder.ASCENDING ) );
        }
        table.getRowSorter().setSortKeys( sortKeys );

        // sorter for the 'qq plot' columns
        Comparator<List<Double>> quantileComparator = new Comparator<List<Double>>() {
            @Override
            public int compare( List<Double> o1, List<Double> o2 ) {
                if ( o1 == null || o2 == null || o1.size() < 2 || o2.size() < 2 ) return 0;

                if ( settings.getBigIsBetter() ) {
                    if ( o1.get( 1 ).equals( o2.get( 1 ) ) ) {
                        return -o1.get( 0 ).compareTo( o2.get( 0 ) );
                    }
                    return -o1.get( 1 ).compareTo( o2.get( 1 ) );
                }
                if ( o1.get( 1 ).equals( o2.get( 1 ) ) ) {
                    return o1.get( 0 ).compareTo( o2.get( 0 ) );
                }
                return o1.get( 1 ).compareTo( o2.get( 1 ) );
            }
        };
        sorter.setComparator( matrixColumnCount + 2, quantileComparator ); // probe score QQ
        sorter.setComparator( matrixColumnCount + 6, quantileComparator ); // MF QQ

        // Save the dimensions of the table just in case
        int totalWidth = matrixColumnCount * matrixColumnWidth + PREFERRED_WIDTH_PROBEID_COLUMN
                + PREFERRED_WIDTH_PVALUE_COLUMN + PREFERRED_WIDTH_GENENAME_COLUMN + PREFERRED_WIDTH_DESCRIPTION_COLUMN
                + PREFERRED_WIDTH_MULTIFUNCTIONALITY_COLUMN;
        int totalheight = table.getPreferredScrollableViewportSize().height;

        Dimension d = new Dimension( totalWidth, totalheight );
        table.setSize( d );

        /*
         * we could hide the score & multifunctionality columns if we don't have them, reshow after loading scores?
         */

    }// end createDetailsTable

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
     *
     * @return an array of int.
     */
    protected int[] getCurrentMatrixDisplayRowOrder() {

        /*
         * There must be a simpler way to get this from the tablerowsorter? like
         * table.getRowSorter().convertRowIndexToView( index )?
         */

        int matrixRowCount = matrixDisplay.getRowCount();
        int[] rowKeys = new int[matrixRowCount];

        // write out the table, one row at a time
        for ( int r = 0; r < matrixRowCount; r++ ) {
            // for this row: write out matrix values
            String elementId = getProbeID( r );
            Element probe = this.geneSetDetails.getGeneData().findElement( elementId );
            if ( probe == null ) {
                log.warn( " No element found in data matrix for: " + elementId );
                continue;
            }
            rowKeys[r] = matrixDisplay.getRowIndexByName( probe );
        }

        return rowKeys;

    } // end createRowKeys

    /**
     * fixme - this should not be in the gui.
     *
     * @param filename a {@link java.lang.String} object.
     * @param normalized a boolean.
     * @throws java.io.IOException if any.
     * @param includeMatrix a boolean.
     * @param addAnnots a boolean.
     */
    protected void saveData( String filename, boolean includeMatrix, boolean addAnnots, boolean normalized )
            throws IOException {

        final String NEWLINE = System.getProperty( "line.separator" );

        File outputFile = new File( filename );

        BufferedWriter out = new BufferedWriter( new FileWriter( outputFile ) );

        boolean isStandardized = matrixDisplay != null && matrixDisplay.getStandardizedEnabled();
        if ( matrixDisplay != null ) {
            matrixDisplay.setStandardizedEnabled( normalized );
        }

        int totalRowCount = table.getRowCount();
        matrixColumnCount = matrixDisplay == null ? 0 : matrixDisplay.getColumnCount();

        printHeader( includeMatrix, addAnnots, out, matrixColumnCount );

        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits( 8 );
        nf.setMinimumFractionDigits( 3 );
        nf.setGroupingUsed( false );

        // write out the table, one row at a time
        for ( int r = 0; r < totalRowCount; r++ ) {

            String elementId = getProbeID( r );
            out.write( elementId );

            if ( addAnnots ) {
                printAnnotationsForRow( out, matrixColumnCount, r );
            }

            if ( includeMatrix ) {
                printMatrixValueForRow( out, nf, elementId );
            }
            out.write( NEWLINE );
        }

        if ( matrixDisplay != null ) matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous
        // state

        // close the file
        out.close();

    } // end saveData

    /**
     * <p>
     * saveImage.
     * </p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param normalized a boolean.
     * @throws java.io.IOException if any.
     */
    protected void saveImage( String filename, boolean normalized ) throws IOException {
        if ( matrixDisplay == null ) return;
        boolean isStandardized = matrixDisplay.getStandardizedEnabled();
        matrixDisplay.setStandardizedEnabled( normalized );
        matrixDisplay.setRowKeys( getCurrentMatrixDisplayRowOrder() );
        try {
            GeneSetDetailsImageWriter.writePng( geneSetDetails, filename, colorMap, includeLabels, includeScalebar,
                    normalized );
        } catch ( IOException e ) {
            // clean up
            matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
            matrixDisplay.resetRowKeys();
            throw e;
        }

        matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
        matrixDisplay.resetRowKeys();

    }

    /**
     * Keep the same gene set, but change the scores.
     */
    protected void switchGeneScoreFile() {

        if ( runSettings != null ) {

        }

        JGeneScoreFileChooser fchooser = new JGeneScoreFileChooser( settings.getScoreFile(), settings.getScoreCol() );
        fchooser.setDialogTitle( "Choose the gene score file or cancel." );
        int yesno = fchooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            settings.setScoreFile( fchooser.getSelectedFile().getAbsolutePath() );
            settings.setScoreCol( fchooser.getStartColumn() );
            statusMessenger.showStatus( "Score file set to " + settings.getScoreFile()
                    + ", reading values from column " + settings.getScoreCol() );

            try {
                GeneScores scores = new GeneScores( settings.getScoreFile(), settings, statusMessenger,
                        this.geneSetDetails.getGeneData() );

                this.geneSetDetails.setGeneScores( scores );
                this.setTitle( geneSetDetails.getTitle() );

            } catch ( IOException e ) {
                statusMessenger
                        .showError( "Error during loading of " + settings.getScoreFile() + ": " + e.getMessage() );
            }

        }

    }

    /**
     * <p>
     * switchRawDataFile.
     * </p>
     */
    protected void switchRawDataFile() {
        this.switchRawDataFile( false );
    }

    /**
     * <p>
     * switchRawDataFile.
     * </p>
     *
     * @param showHelp a boolean.
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
                                    + " from the 'Options' menu of the details view.\n",
                            "Selecting a data file",
                            JOptionPane.INFORMATION_MESSAGE );
        }

        String rawDataFileName = settings.getRawDataFileName();
        if ( StringUtils.isBlank( rawDataFileName ) ) {
            rawDataFileName = this.settings.getDataDirectory();
        }

        JRawFileChooser fc = new JRawFileChooser( rawDataFileName, settings.getDataCol() );
        fc.setDialogTitle( "Choose the expression data file or cancel." );
        int yesno = fc.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            String rawdataFile = fc.getSelectedFile().getAbsolutePath();

            settings.setRawFile( rawdataFile );
            settings.setDataCol( fc.getStartColumn() );

            this.geneSetDetails.loadDataMatrix( rawdataFile );
            this.setTitle( this.geneSetDetails.getTitle() );

            /*
             * Have to rebuild the entire table.
             */
            createDetailsTable();
            this.validate();
            this.statusMessenger.showStatus( "Matrix file set to: " + rawdataFile );

            setDisplayMatrixGUIEnabled( true );
        }

    }

    /**
     * Pop up a chart showing ROC and PR curves for this gene set in the full ranking.
     */
    protected void viewContext() {
        if ( this.geneSetDetails.getSourceGeneScores() == null
                || this.geneSetDetails.getSourceGeneScores().getRankedGenes().isEmpty() ) {
            /*
             * FIXME prompt for the gene scores?
             */
            statusMessenger.showError( "You have to define non-empty gene scores" );
            return;
        }
        GeneSetRankingContextWindow w = new GeneSetRankingContextWindow( this.geneSetDetails );
        w.setSize( new Dimension( 500, 500 ) );
        GuiUtil.centerContainer( w );
        w.setVisible( true );
    }

    private String getProbeID( int row ) {
        int offset = matrixDisplay == null ? 0 : matrixDisplay.getColumnCount(); // matrix display ends
        return ( ( Element ) table.getValueAt( row, offset + 0 ) ).getName();
    }

    /**
     *
     */
    private void initChoosers() {
        // clean up the class id so it can be used to form file names (this is not foolproof)
        String fileNameBase = this.geneSetDetails.getClassID().getId()
                .replaceAll( "[:\\s\\(\\)\\*&^%$#@\\!\\`\\'\\\"]+", "_" );
        imageChooser = new DetailsOutputImageFileChooser( settings.getConfig().getBoolean( INCLUDELABELS, true ),
                settings.getConfig().getBoolean( NORMALIZE_SAVED_IMAGE, true ), fileNameBase + ".png" );
        fileChooser = new DetailsOutputDataFileChooser( settings.getConfig().getBoolean( INCLUDEEVERYTHING, true ),
                settings.getConfig().getBoolean( NORMALIZE_SAVED_DATA, false ), fileNameBase + ".txt" );
        readPathPrefs();
    }

    /**
     * A lil' scale bar implementation
     */
    private void initColorRangeWidget() {

        // init the slider
        m_colorRangeSlider.setMinimum( COLOR_RANGE_SLIDER_MIN );
        m_colorRangeSlider.setMaximum( COLOR_RANGE_SLIDER_RESOLUTION );

        double rangeMax;
        boolean normalized = matrixDisplay.getStandardizedEnabled();
        if ( normalized ) {
            rangeMax = NORMALIZED_COLOR_RANGE_MAX;
        } else {
            rangeMax = matrixDisplay.getMax() - matrixDisplay.getMin();
        }
        double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
        m_colorRangeSlider.setValue( ( int ) ( matrixDisplay.getDisplayRange() * zoomFactor ) );

        // init gradient bar
        double min = matrixDisplay.getDisplayMin();
        double max = matrixDisplay.getDisplayMax();
        colorScaleBar.setLabels( min, max );
    }

    /**
     * @throws Exception
     */
    private void jbInit() {

        // Listener for window closing events.
        this.addWindowListener( new JGeneSetFrame_windowListenerAdapter( this ) );

        setupTable();
        setupToolBar();

        setupMenus();
        setUpStatusBar();
        setupWindow();

        tableScrollPane.getViewport().add( table, null );

        repositionViewport();

        this.getContentPane().add( tableScrollPane, BorderLayout.CENTER );
        this.getContentPane().add( toolBar, BorderLayout.NORTH );
        this.getContentPane().add( jPanelStatus, BorderLayout.SOUTH );

        if ( matrixDisplay != null ) {
            boolean isNormalized = matrixDisplay.getStandardizedEnabled();
            saveImageMenuItem.setEnabled( true );
            m_normalizeMenuItem.setSelected( isNormalized );
            setDisplayMatrixGUIEnabled( true );
        } else {
            // matrixDisplay is null! Disable the menu
            saveImageMenuItem.setEnabled( false ); // no image to save.
            setDisplayMatrixGUIEnabled( false );
        }

        table.addMouseListener( new MouseAdapter() {
            @Override
            public void mousePressed( MouseEvent e ) {
                if ( e.isPopupTrigger() ) {
                    showPopupMenu( e );
                }
            }

            @Override
            public void mouseReleased( MouseEvent e ) {
                if ( e.isPopupTrigger() ) {
                    showPopupMenu( e );
                }
            }

        } );

    }

    /**
     * @param i
     * @param j
     * @return
     */
    private boolean onGeneSymbolCell( int i, int j ) {
        if ( i < 0 || j < 0 ) return false;
        return table.getValueAt( i, j ) != null && j == table.getColumnCount() - 4;
    }

    private boolean onMultifunctionalityCell( int i, int j ) {
        if ( i < 0 || j < 0 ) return false;
        return table.getValueAt( i, j ) != null && j == table.getColumnCount() - 1;
    }

    /**
     * @param urlBase
     * @param g
     */
    private void openUrlForGene( String urlBase, Gene g ) {
        String symbol = g.getSymbol();

        try {
            BrowserLauncher.openURL( urlBase + symbol );
        } catch ( Exception e1 ) {
            log.error( e1, e1 );
            GuiUtil.error( "Could not open a web browser window" );
        }
    }

    /**
     * @param out
     * @param c
     * @param r
     * @throws IOException
     */
    private void printAnnotationsForRow( BufferedWriter out, int c, int r ) throws IOException {
        int interestingStuffStartsAt = c;
        int scoreColumn = interestingStuffStartsAt + 1;
        int symbolColumn = interestingStuffStartsAt + 3;
        int nameColumn = interestingStuffStartsAt + 4;
        out.write( "\t" + table.getValueAt( r, scoreColumn ).toString() );
        out.write( stripHtml( "\t" + table.getValueAt( r, symbolColumn ).toString() ) );
        out.write( "\t" + table.getValueAt( r, nameColumn ).toString() );
    }

    /**
     * @param includeMatrixValues
     * @param includeAnnots
     * @param out
     * @param colCount
     * @throws IOException
     */
    private void printHeader( boolean includeMatrixValues, boolean includeAnnots, BufferedWriter out, int colCount )
            throws IOException {
        out.write( "Element" );
        if ( includeAnnots ) {
            out.write( "\tScore\tSymbol\tName" );
        }
        // write out column names
        if ( includeMatrixValues ) {
            for ( int c = 0; c < colCount; c++ ) {
                String columnName = matrixDisplay.getColumnName( c ).toString();
                out.write( "\t" + columnName );
            }
        }
        out.write( System.getProperty( "line.separator" ) );
    }

    /**
     * @param out
     * @param nf
     * @param elementId
     * @throws IOException
     */
    private void printMatrixValueForRow( BufferedWriter out, DecimalFormat nf, String elementId ) throws IOException {
        assert this.matrixDisplay != null;
        Element probe = this.geneSetDetails.getGeneData().findElement( elementId );
        if ( probe == null ) {
            log.warn( "No element found in data matrix for: " + elementId );
            return;
        }
        double[] row = matrixDisplay.getRowByName( probe );
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
        if ( settings.getConfig().containsKey( SAVESTARTPATH ) && matrixDisplay != null ) {
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
            this.width = settings.getConfig().getInt( WINDOWWIDTH );
            log.debug( "width: " + width );
        }

        if ( settings.getConfig().containsKey( WINDOWHEIGHT ) ) {
            this.height = settings.getConfig().getInt( WINDOWHEIGHT );
            log.debug( "height: " + height );
        }
        if ( settings.getConfig().containsKey( MATRIXCOLUMNWIDTH ) ) {
            this.matrixColumnWidth = settings.getConfig().getInt( MATRIXCOLUMNWIDTH );
            log.debug( "matrixColumnWidth: " + matrixColumnWidth );
        }
        if ( settings.getConfig().containsKey( INCLUDELABELS ) ) {
            this.includeLabels = settings.getConfig().getBoolean( INCLUDELABELS );
            log.debug( "includeLabels: " + includeLabels );
        }
        if ( settings.getConfig().containsKey( INCLUDEEVERYTHING ) ) {
            this.includeAnnotations = settings.getConfig().getBoolean( INCLUDEEVERYTHING );
            log.debug( "includeEverything: " + includeAnnotations );
        }
        if ( settings.getConfig().containsKey( NORMALIZE_SAVED_IMAGE ) ) {
            this.normalizeSavedImage = settings.getConfig().getBoolean( NORMALIZE_SAVED_IMAGE );
            log.debug( "normalizeSavedImage " + normalizeSavedImage );
        }

        if ( settings.getConfig().containsKey( NORMALIZE_SAVED_DATA ) ) {
            this.normalizeSavedData = settings.getConfig().getBoolean( NORMALIZE_SAVED_DATA );
            log.debug( "normalizeSavedData " + normalizeSavedData );
        }

    }

    /**
     *
     */
    private void repositionViewport() {
        // Reposition the table inside the scrollpane
        int x = table.getSize().width;
        // should probably subtract the size of the viewport, but it gets trimmed
        // anyway,
        // so it's okay to be lazy here
        // tableScrollPane.setSize( table.getSize() );
        tableScrollPane.getViewport().setViewPosition( new Point( x, 0 ) );
        // statusMessenger
        // .showError( "You may need to scroll horizontally or adjust the column width to see all the data" );
    }

    /**
     *
     */
    private void resetUrl() {
        setCursor( Cursor.getDefaultCursor() );
        statusMessenger.clear();
    }

    /**
     * @param enabled
     */
    private void setDisplayMatrixGUIEnabled( boolean enabled ) {
        if ( settings == null ) return;
        // the menu
        menuBar.setEnabled( enabled );
        // fileMenu.setEnabled( enabled ); /; show it, just don't allow image saves.
        viewMenu.setEnabled( enabled );
        optionsMenu.setEnabled( true );

        // the toolbar
        toolBar.setEnabled( enabled );

        // the sliders
        m_cellWidthSlider.setEnabled( enabled );
        m_cellWidthLabel.setEnabled( enabled );
        m_colorRangeSlider.setEnabled( enabled );
        m_colorRangeLabel.setEnabled( enabled );
        colorScaleBar.setVisible( enabled );

        if ( enabled ) {
            colorScaleBar.setColorMap( matrixDisplay.getColorMap() );
            initColorRangeWidget();
        }
    }

    /**
     * @param verticalHeaderRenderer
     * @param matrix
     * @param matrixCellRenderer
     * @param matrixColumnCount
     */
    private void setupColumns() {

        // Set each column width and renderer.
        for ( int i = 0; i < matrixColumnCount; i++ ) {
            TableColumn col = table.getColumnModel().getColumn( i );
            col.setMinWidth( MIN_WIDTH_MATRIXDISPLAY_COLUMN );
            col.setMaxWidth( MAX_WIDTH_MATRIXDISPLAY_COLUMN );
            col.setCellRenderer( matrixCellRenderer );
            col.setHeaderRenderer( verticalHeaderRenderer );
            col.setPreferredWidth( PREFERRED__WIDTH_MATRIXDISPLAY_COLUMN );
        }

        resizeMatrixColumns( matrixColumnWidth );

        //
        // Set up the rest of the table
        //
        TableColumn col;

        // probe ID
        col = table.getColumnModel().getColumn( matrixColumnCount + 0 );
        col.setPreferredWidth( PREFERRED_WIDTH_PROBEID_COLUMN );
        col.setCellRenderer( new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent( JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column ) {
                super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
                setText( ( ( Element ) value ).getName() );
                return this;
            }
        } );

        // probe score (p-value etc)
        col = table.getColumnModel().getColumn( matrixColumnCount + 1 );
        col.setPreferredWidth( PREFERRED_WIDTH_PVALUE_COLUMN );
        col.setCellRenderer( new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent( JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column ) {
                super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
                if ( ( ( Double ) value ).isNaN() ) {
                    setText( "" );
                } else {
                    setText( String.format( "%.3g", ( Double ) value ) );
                }
                return this;
            }
        } );

        // p value bar
        col = table.getColumnModel().getColumn( matrixColumnCount + 2 );
        col.setPreferredWidth( PREFERRED_WIDTH_PVALUEBAR_COLUMN );
        col.setCellRenderer( new JBarGraphCellRenderer( 4.0, new Color[] { Color.LIGHT_GRAY, Colors.LIGHTBLUE2 } ) );

        // name (gene)
        setupGeneSymbolColumn( table.getColumnModel().getColumn( matrixColumnCount + 3 ) );

        // gene description
        col = table.getColumnModel().getColumn( matrixColumnCount + 4 );
        col.setPreferredWidth( PREFERRED_WIDTH_DESCRIPTION_COLUMN );

        // multifunctionality Score
        setupMultifunctionalityColumn( table.getColumnModel().getColumn( matrixColumnCount + 5 ) );

        // QQ plot for multifunctionality.
        col = table.getColumnModel().getColumn( matrixColumnCount + 6 );
        col.setPreferredWidth( PREFERRED_WIDTH_MULTIFUNCTIONALITY_QQCOLUMN );
        double maxValue = 3.0; // values above this will be clipped for display.
        col.setCellRenderer( new JBarGraphCellRenderer( maxValue, new Color[] { Color.LIGHT_GRAY, Colors.LIGHTRED2 } ) );

    }

    /**
     * @param col
     */
    private void setupGeneSymbolColumn( TableColumn col ) {
        col.setPreferredWidth( PREFERRED_WIDTH_GENENAME_COLUMN );
        col.setCellRenderer( new DefaultTableCellRenderer() {
            public void fillColor( JTable t, JComponent l, boolean isSelected ) {
                if ( isSelected ) {
                    l.setBackground( t.getSelectionBackground() );
                    l.setForeground( t.getSelectionForeground() );
                } else {
                    l.setBackground( t.getBackground() );
                    l.setForeground( Color.BLUE );
                }
            }

            @Override
            public Component getTableCellRendererComponent( JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column ) {
                fillColor( table, ( JComponent ) value, isSelected );
                return ( JComponent ) value;
            }

        } );

    }

    /**
     * @param filename
     * @param probesInGeneSet
     * @return
     */
    private void setUpMatrixView() {

        DoubleMatrix<Element, String> matrix = this.geneSetDetails.getDataMatrix();

        if ( matrix == null || matrix.rows() == 0 ) {
            if ( StringUtils.isNotBlank( settings.getRawDataFileName() ) ) {
                statusMessenger.showError( "None of the elements in this gene set were in the data file." );
            }
        } else {
            matrixDisplay = new MatrixDisplay<>( matrix );
            matrixDisplay.setColorMap( this.colorMap );
            matrixDisplay.setStandardizedEnabled( this.normalizeMatrixView );
            // Make the columns in the matrix display not too wide (cell-size)
            // and set a custom cell renderer
            matrixCellRenderer = new JMatrixCellRenderer( matrixDisplay ); // create one instance
            // that will be used to
            // draw each cell
        }

        verticalHeaderRenderer = new JVerticalHeaderRenderer(); // create only one instance
        matrixColumnCount = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;

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

        m_normalizeMenuItem.setText( "Standardize" );
        m_normalizeMenuItem.addActionListener( new JGeneSetFrame_m_normalizeMenuItem_actionAdapter( this ) );
        optionsMenu.setText( "Options" );
        setGeneUrlBaseMenuItem.setText( "Change gene name URL pattern ..." );
        setGeneUrlBaseMenuItem.addActionListener( new JGeneSetFrame_viewGeneUrlDialog_actionAdapter( this ) );

        viewMenu.add( m_normalizeMenuItem );
        viewMenu.addSeparator();
        viewMenu.add( greenredColormapMenuItem );
        viewMenu.add( blackbodyColormapMenuItem );

        optionsMenu.add( setGeneUrlBaseMenuItem );
        optionsMenu.add( switchDataFileMenuItem );
        optionsMenu.add( switchGeneScoreFileMenuItem );

        saveDataMenuItem.setText( "Save Data ..." );
        saveDataMenuItem.addActionListener( new JGeneSetFrame_m_saveDataMenuItem_actionAdapter( this ) );

        switchDataFileMenuItem.setText( "Set Dataset ..." );
        switchDataFileMenuItem.setToolTipText( "Set or change the source of the data used for the heatmap" );
        switchDataFileMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                switchRawDataFile();
                table.revalidate();
            }
        } );

        switchGeneScoreFileMenuItem.setText( "Set gene score file ..." );
        switchGeneScoreFileMenuItem
                .setToolTipText( "Set or change the source of the data used for the 'score' columns" );
        switchGeneScoreFileMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                switchGeneScoreFile();
                table.revalidate();
            }
        } );

        /*
         * Add
         */
        JMenu analysisMenu = new JMenu( "Analysis" );
        JMenuItem contextMenuItem = new JMenuItem( "Context in score ranking" );
        contextMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                viewContext();
            }
        } );

        analysisMenu.add( contextMenuItem );

        menuBar.add( fileMenu );
        menuBar.add( viewMenu );
        menuBar.add( optionsMenu );
        menuBar.add( analysisMenu );
    }

    /**
     * @param col
     */
    private void setupMultifunctionalityColumn( TableColumn col ) {
        col.setPreferredWidth( PREFERRED_WIDTH_MULTIFUNCTIONALITY_COLUMN );
        col.setCellRenderer( new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent( JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column ) {

                super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

                if ( isSelected || hasFocus ) return this;

                String sv = ( String ) value;

                String ds = sv.split( " " )[0]; // kludgy. But it works.

                if ( StringUtils.isBlank( ds ) ) {
                    return this;
                }

                double v = Double.parseDouble( ds );
                // log.info( "" + v );
                if ( v >= 0.99 ) {
                    setBackground( Colors.LIGHTRED1 );
                } else if ( v >= 0.95 ) {
                    setBackground( Colors.LIGHTRED2 );
                } else if ( v >= 0.9 ) {
                    setBackground( Colors.LIGHTRED3 );
                } else if ( v >= 0.85 ) {
                    setBackground( Colors.LIGHTRED4 );
                } else if ( v >= 0.8 ) {
                    setBackground( Colors.LIGHTRED5 );
                } else {
                    setBackground( Color.WHITE );
                }

                setForeground( Color.BLACK );

                return this;
            }
        } );
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
    private void setupTable() {
        table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );

        // Prevent user from moving columns around
        table.getTableHeader().setReorderingAllowed( false );

        // For html links on gene names
        table.addMouseListener( new JGeneSetFrame_m_mouseAdapter( this ) );

        table.addMouseMotionListener( new JGeneSetFrame_m_mouseMotionListener( this ) );

        // change the cursor to a hand over a header
        table.getTableHeader().addMouseListener( new JGeneSetFrameTableHeader_mouseAdapterCursorChanger( this ) );

        // The rest of the table (text and value) should have a light gray grid
        table.setGridColor( Color.lightGray );
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
        m_cellWidthSlider.setMaximumSize( new Dimension( 90, 24 ) );
        m_cellWidthSlider.setPreferredSize( new Dimension( 90, 24 ) );
        m_cellWidthSlider.addChangeListener( new JGeneSetFrame_m_cellWidthSlider_changeAdapter( this ) );
        this.setResizable( true );
        m_cellWidthLabel.setText( "Cell Width:" );
        m_cellWidthLabel.setBorder( BorderFactory.createEmptyBorder( 5, 10, 0, 0 ) );
        m_cellWidthLabel.setToolTipText( "Change the width of the heatmap cells" );

        m_colorRangeLabel.setText( "Color Range:" );
        m_colorRangeLabel.setLabelFor( m_colorRangeSlider );
        m_colorRangeLabel.setBorder( BorderFactory.createEmptyBorder( 5, 10, 0, 0 ) );
        colorScaleBar.setMaximumSize( new Dimension( 200, 20 ) );
        colorScaleBar.setMinimumSize( new Dimension( 120, 15 ) );
        if ( matrixDisplay != null ) {
            colorScaleBar.setColorMap( matrixDisplay.getColorMap() );
            initColorRangeWidget();
        }
        m_colorRangeSlider.setMaximumSize( new Dimension( 90, 24 ) );
        m_colorRangeSlider.setPreferredSize( new Dimension( 90, 24 ) );
        m_colorRangeSlider.addChangeListener( new JGeneSetFrame_m_colorRangeSlider_changeAdapter( this ) );

        GroupLayout gl = new GroupLayout( toolBar );
        toolBar.setLayout( gl );
        gl.setHorizontalGroup( gl.createSequentialGroup().addComponent( m_cellWidthLabel )
                .addComponent( m_cellWidthSlider ).addComponent( m_colorRangeLabel ).addComponent( m_colorRangeSlider )
                .addComponent( colorScaleBar ) );
        gl.setVerticalGroup( gl.createParallelGroup().addComponent( m_cellWidthLabel ).addComponent( m_cellWidthSlider )
                .addComponent( m_colorRangeLabel ).addComponent( m_colorRangeSlider ).addComponent( colorScaleBar ) );

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

        String title = StringUtils.abbreviate( this.geneSetDetails.getTitle(), 200 );

        this.setTitle( title );

        this.setIconImage( new ImageIcon( this.getClass().getResource(
                MainFrame.RESOURCE_LOCATION + "logoInverse32.gif" ) ).getImage() );
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
        settings.getConfig().setProperty( WINDOWPOSITIONX, new Double( this.getLocation().getX() ) );
        settings.getConfig().setProperty( WINDOWPOSITIONY, new Double( this.getLocation().getY() ) );
        settings.getConfig().setProperty( NORMALIZE_SAVED_DATA, new Boolean( this.normalizeSavedData ) );
        settings.getConfig().setProperty( NORMALIZE_SAVED_IMAGE, new Boolean( this.normalizeSavedImage ) );
        settings.getConfig().setProperty( INCLUDEEVERYTHING, new Boolean( this.includeAnnotations ) );
        settings.getConfig().setProperty( INCLUDELABELS, new Boolean( this.includeLabels ) );
        settings.writePrefs();
    }

} // end class JGeneSetFrame

class JGeneSetFrame_m_blackbodyColormapMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_blackbodyColormapMenuItem_actionAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.m_blackbodyColormapMenuItem_actionPerformed();
    }
}

class JGeneSetFrame_m_cellWidthSlider_changeAdapter implements javax.swing.event.ChangeListener {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_cellWidthSlider_changeAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void stateChanged( ChangeEvent e ) {
        adaptee.m_cellWidthSlider_stateChanged( e );
    }
}

class JGeneSetFrame_m_colorRangeSlider_changeAdapter implements javax.swing.event.ChangeListener {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_colorRangeSlider_changeAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void stateChanged( ChangeEvent e ) {
        adaptee.m_colorRangeSlider_stateChanged( e );
    }
}

class JGeneSetFrame_m_greenredColormapMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_greenredColormapMenuItem_actionAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
        /** {@inheritDoc} */
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.m_greenredColormapMenuItem_actionPerformed( e );
    }
}

class JGeneSetFrame_m_mouseAdapter extends java.awt.event.MouseAdapter {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_mouseAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
        /** {@inheritDoc} */
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        adaptee.table_mouseExited();
        /** {@inheritDoc} */
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased( MouseEvent e ) {
        adaptee.table_mouseReleased( e );
    }

}

class JGeneSetFrame_m_mouseMotionListener extends java.awt.event.MouseAdapter {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_mouseMotionListener( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void mouseMoved( MouseEvent e ) {
        adaptee.table_mouseMoved( e );
    }
}

class JGeneSetFrame_m_normalizeMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_normalizeMenuItem_actionAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.m_normalizeMenuItem_actionPerformed();
    }
}

class JGeneSetFrame_m_saveDataMenuItem_actionAdapter implements java.awt.event.ActionListener {
    /** {@inheritDoc} */
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_saveDataMenuItem_actionAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.saveDataAction();
    }
}

class JGeneSetFrame_m_saveImageMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_m_saveImageMenuItem_actionAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.m_saveImageMenuItem_actionPerformed();
    }
}

class JGeneSetFrame_viewGeneUrlDialog_actionAdapter implements java.awt.event.ActionListener {
    GeneSetDetailsFrame adaptee;

    /**
     * <p>
     * Constructor for JGeneSetFrame_viewGeneUrlDialog_actionAdapter.
     * </p>
     *
     * @param adaptee a {@link ubic.erminej.gui.geneset.details.GeneSetDetailsFrame} object.
     */
    public JGeneSetFrame_viewGeneUrlDialog_actionAdapter( GeneSetDetailsFrame adaptee ) {
        super();
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.viewGeneUrlDialogMenuItem_actionPerformed();
    }
    /** {@inheritDoc} */
}

class JGeneSetFrame_windowListenerAdapter extends java.awt.event.WindowAdapter {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrame_windowListenerAdapter( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void windowClosing( WindowEvent e ) {
        adaptee.closeWindow_actionPerformed( e );
    }
}

// show the hand cursor on hover
/** {@inheritDoc} */
class JGeneSetFrameTableHeader_mouseAdapterCursorChanger extends java.awt.event.MouseAdapter {
    GeneSetDetailsFrame adaptee;

    JGeneSetFrameTableHeader_mouseAdapterCursorChanger( GeneSetDetailsFrame adaptee ) {
        this.adaptee = adaptee;
        /** {@inheritDoc} */
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
        adaptee.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        adaptee.setCursor( Cursor.getDefaultCursor() );
    }

}
