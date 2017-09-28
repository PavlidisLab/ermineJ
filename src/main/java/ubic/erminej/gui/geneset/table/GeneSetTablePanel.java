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
package ubic.erminej.gui.geneset.table;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.geneset.GeneSetPanel;
import ubic.erminej.gui.geneset.GeneSetPanelPopupMenu;

/**
 * A table that lists the Gene Sets with their scores, and allows user interaction.
 *
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetTablePanel extends GeneSetPanel {

    /** Constant <code>GENE_COUNT_COLUMN_INDEX=2</code> */
    public static final int GENE_COUNT_COLUMN_INDEX = 2;
    /** Constant <code>MULTIFUNC_COLUMN_INDEX=3</code> */
    public static final int MULTIFUNC_COLUMN_INDEX = 3;
    private final static int GENESET_ID_COLUMN_WIDTH = 80;
    private final static int GENESET_NAME_COLUMN_WIDTH = 350;
    private static Log log = LogFactory.getLog( GeneSetTablePanel.class );
    private final static int NUMGENES_COLUMN_WIDTH = 40;
    private final static int RUN_COLUMN_START_WIDTH = 80;

    private static final long serialVersionUID = -1L;
    protected GeneSetTableModel model = null;

    protected List<String> resultToolTips = new LinkedList<>();

    protected JTable table = null;

    private TableRowSorter<GeneSetTableModel> sorter;

    /**
     * <p>
     * Constructor for GeneSetTablePanel.
     * </p>
     *
     * @param callingFrame a {@link ubic.erminej.gui.MainFrame} object.
     * @param settings a {@link ubic.erminej.Settings} object.
     */
    public GeneSetTablePanel( MainFrame callingFrame, Settings settings ) {
        super( settings, callingFrame );
    }

    /*
     * (non-Javadoc)
     *
     * @see ubic.erminej.gui.GeneSetPanel#addedGeneSet(ubic.erminej.data.GeneSetTerm)
     */
    /** {@inheritDoc} */
    @Override
    public void addedGeneSet( GeneSetTerm id ) {
        refreshView();
    }

    /** {@inheritDoc} */
    @Override
    public void addRun() {

        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                model.addRun();

                int c = model.getColumnCount() - 1;
                TableColumn col = new TableColumn( c );
                col.setIdentifier( model.getColumnName( c ) );

                table.addColumn( col );
                table.getColumnModel().getColumn( c ).setPreferredWidth( RUN_COLUMN_START_WIDTH );
                generateResultColumnHeadingTooltip( model.getColumnCount() - GeneSetTableModel.INIT_COLUMNS - 1 );

                int currentResultSetIndex = mainFrame.getNumResultSets() - 1;
                mainFrame.setCurrentResultSetIndex( currentResultSetIndex );
                table.revalidate();

                model.filter();

                List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                sortKeys.add( new RowSorter.SortKey( c, SortOrder.ASCENDING ) );
                sorter.setSortKeys( sortKeys );
            }
        } );
    }

    /*
     * (non-Javadoc)
     *
     * @see ubic.erminej.gui.geneset.GeneSetPanel#filter(boolean)
     */
    /** {@inheritDoc} */
    @Override
    public void filter( final boolean propagate ) {
        SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                model.setFilterEmpty( mainFrame.getHideEmpty() );
                model.setFilterEmptyResults( mainFrame.getHideNonSignificant() );
                model.filter();
                resortByCurrentResults();
                if ( propagate ) mainFrame.getTreePanel().filter( false );
                return null;
            }
        };
        r.execute();

        this.mainFrame.getStatusMessenger().showProgress( "Updating" );
        try {
            r.get();
        } catch ( Exception e ) {
            log.warn( e, e );
        }
        this.mainFrame.getStatusMessenger().showProgress( "Clear" );

    }

    /**
     * <p>
     * filter.
     * </p>
     *
     * @param selectedTerms a {@link java.util.Collection} object.
     */
    public void filter( final Collection<GeneSetTerm> selectedTerms ) {
        SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                if ( selectedTerms.isEmpty() ) {
                    clearRowFilter();
                    return null;
                }

                RowFilter<GeneSetTableModel, Object> rf = new RowFilter<GeneSetTableModel, Object>() {
                    @Override
                    public boolean include( RowFilter.Entry<? extends GeneSetTableModel, ? extends Object> entry ) {
                        GeneSetTerm term = ( GeneSetTerm ) entry.getValue( 0 );
                        return selectedTerms.contains( term );
                    }
                };
                sorter.setRowFilter( rf );
                resortByCurrentResults();
                return null;
            }
        };
        r.execute();
    }

    /**
     * <p>
     * filterByUserGeneSets.
     * </p>
     *
     * @param b a boolean.
     */
    public void filterByUserGeneSets( boolean b ) {
        this.model.setFilterNonUsers( b );
        filter( false );
    }

    /**
     * <p>
     * getRowCount.
     * </p>
     *
     * @return a int.
     */
    public int getRowCount() {
        return this.table.getRowCount();
    }

    // called when we first set up the table.
    /**
     * <p>
     * initialize.
     * </p>
     *
     * @param initialGoData a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public void initialize( GeneAnnotations initialGoData ) {
        this.geneData = initialGoData;
        setUpTable();
        setUpHeaderPopupMenus();
        setTableAttributes();
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add( new RowSorter.SortKey( 0, SortOrder.ASCENDING ) );
        sorter.setSortKeys( sortKeys );
    }

    /**
     * {@inheritDoc}
     *
     * Revalidate, refilter and (resort? NO) the table.
     */
    @Override
    public void refreshView() {

        this.messenger.showProgress( "Updating view" );

        List<? extends SortKey> sortKeys = new ArrayList<SortKey>( sorter.getSortKeys() );

        this.model.filter();
        this.messenger.showStatus( table.getRowCount() + " sets shown" );
        table.revalidate();

        sorter.setSortKeys( sortKeys );

    }

    /** {@inheritDoc} */
    @Override
    public void removeRun( GeneSetPvalRun runToRemove ) {
        // Perhaps nothing, since it was done locally? could refactor from removeRun(int);
    }

    // called if 'cancel', 'find' or 'reset' have been hit.
    /** {@inheritDoc} */
    @Override
    public void resetView() {
        filter( new HashSet<GeneSetTerm>() );
    }

    /**
     * Resort by the current result set, if there is one. Otherwise leave it alone.
     */
    public void resortByCurrentResults() {
        SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                int sortColumnIndex = 0;
                int currentResultSetIndex = mainFrame.getCurrentResultSetIndex();
                if ( currentResultSetIndex >= 0 ) {
                    sortColumnIndex = GeneSetTableModel.INIT_COLUMNS + currentResultSetIndex;

                    assert sortColumnIndex < table.getColumnCount();

                    List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                    sortKeys.add( new RowSorter.SortKey( sortColumnIndex, SortOrder.ASCENDING ) );

                    sorter.setSortKeys( sortKeys );

                }
                return null;
            }
        };
        r.execute();
    }

    /** {@inheritDoc} */
    @Override
    public void showPopupMenu( final MouseEvent e ) {

        GeneSetPanelPopupMenu popup = super.configurePopup( e );

        if ( popup == null ) return;

        final JMenuItem findInTreeMenuItem = new JMenuItem( "Find this set in the tree panel" );
        findInTreeMenuItem.addActionListener( new FindInTreeListener( this ) );
        popup.add( findInTreeMenuItem );
        popup.show( e.getComponent(), e.getX(), e.getY() );
    }

    /**
     * @param e MouseEvent
     */
    void table_mouseReleased( MouseEvent e ) {
        int i = table.getSelectedRow();
        int j = table.getSelectedColumn();

        int _runnum = -1;
        GeneSetPvalRun run = null;
        GeneSetTerm term = ( GeneSetTerm ) table.getValueAt( i, 0 );
        if ( table.getValueAt( i, j ) != null && j >= GeneSetTableModel.INIT_COLUMNS ) {
            _runnum = model.getRunIndex( j );
            run = mainFrame.getResultSet( _runnum );
            mainFrame.setCurrentResultSetIndex( _runnum );
        }

        messenger.showProgress( "Viewing details for " + term + "..." );

        showDetailsForGeneSet( term, run );

    }

    /** {@inheritDoc} */
    @Override
    protected boolean deleteUserGeneSet( GeneSetTerm classID ) {
        boolean deleted = super.deleteUserGeneSet( classID );
        table.revalidate();
        return deleted;

    }

    /**
     * Create the text shown when user hovers mouse over the heading of a result column
     *
     * @param runIndex a int.
     */
    protected void generateResultColumnHeadingTooltip( int runIndex ) {

        GeneSetPvalRun geneSetPvalRun = mainFrame.getResultSet( runIndex );
        assert geneSetPvalRun != null : "No results with index " + runIndex;
        SettingsHolder runSettings = geneSetPvalRun.getSettings();

        String tooltip = new String( "<html>" );
        String coda = new String();
        String mf = "";

        boolean usingPrecisionRecall = runSettings.getGeneSetResamplingScoreMethod().equals(
                SettingsHolder.GeneScoreMethod.PRECISIONRECALL );

        if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.ORA ) ) {
            tooltip += "ORA Analysis<br>";
            int numAboveThresh = geneSetPvalRun.getNumAboveThreshold();
            coda += "Score threshold: " + runSettings.getGeneScoreThreshold();
            coda += ", Genes meeting: " + numAboveThresh + "<br/>";
            // note use of enrichment in this case.
            mf = String.format( "Multifunct. bias (AUROC): %.2f p = %.2g<br>",
                    geneSetPvalRun.getMultifunctionalityEnrichment(),
                    geneSetPvalRun.getMultifunctionalityEnrichmentPvalue() );
        } else if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.GSR ) ) {

            if ( usingPrecisionRecall ) {
                tooltip += "Precision-recall analysis<br>";
            } else {
                tooltip += "Gene set score analysis<br>";
            }
            coda += runSettings.getIterations() + " resampling iterations<br>";
            coda += "Using score column: " + runSettings.getScoreCol();
            mf = String.format( "Multifunct. bias (corr): %.2f<br>", geneSetPvalRun.getMultifunctionalityCorrelation() );
        } else if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) ) {
            tooltip += "Correlation Analysis<br>";
            coda += runSettings.getIterations() + " iterations";
            // no multifunctionality issues dealt with
        } else if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.ROC ) ) {
            tooltip += "ROC Analysis<br>";
            mf = String.format( "Multifunct. bias (corr): %.2f<br>", geneSetPvalRun.getMultifunctionalityCorrelation() );
        }

        tooltip += mf;

        tooltip += new String( "Max set size: " + runSettings.getMaxClassSize() + "<br>" + "Min set size: "
                + runSettings.getMinClassSize() + "<br>" );
        if ( runSettings.getDoLog() ) tooltip += "Negative log transformed<br>";

        if ( runSettings.getGeneRepTreatment().equals( SettingsHolder.MultiElementHandling.MEAN ) ) {
            tooltip += "Gene Rep Treatment: Mean <br>";
        } else if ( runSettings.getGeneRepTreatment().equals( SettingsHolder.MultiElementHandling.BEST ) ) {
            tooltip += "Gene Rep Treatment: Best <br>";
        }

        if ( !usingPrecisionRecall && runSettings.getClassScoreMethod().equals( SettingsHolder.Method.GSR ) ) {
            if ( runSettings.getGeneSetResamplingScoreMethod().equals( SettingsHolder.GeneScoreMethod.MEAN ) ) {
                tooltip += "Class Raw Score Method: Mean <br>";
            } else if ( runSettings.getGeneSetResamplingScoreMethod().equals( SettingsHolder.GeneScoreMethod.QUANTILE ) ) {
                tooltip += "Class Raw Score Method: Median <br>";
            }

        }

        if ( StringUtils.isNotBlank( runSettings.getScoreFile() ) ) {
            String fileName = new File( runSettings.getScoreFile() ).getName();
            coda += "<br/>Scores: " + fileName + " col. " + runSettings.getScoreCol() + " "
                    + geneSetPvalRun.getGeneScoreColumnName() + "<br/>";
        }

        if ( StringUtils.isNotBlank( runSettings.getRawDataFileName() ) ) {
            String fileName = new File( runSettings.getRawDataFileName() ).getName();
            coda += "Profiles: " + fileName + "<br/>";
        }

        tooltip += coda;
        resultToolTips.add( runIndex, tooltip );
    }

    /**
     * <p>
     * getHeaderToolTip.
     * </p>
     *
     * @param index a int.
     * @return a {@link java.lang.String} object.
     */
    protected String getHeaderToolTip( int index ) {
        if ( index == 0 || index == 1 ) { // descriptions of the category.
            return "Classes shown: " + table.getRowCount();
        } else if ( index == GENE_COUNT_COLUMN_INDEX ) {
            return "How many genes are in the group (# of scored elements shown in brackets if different)";
        } else if ( index == MULTIFUNC_COLUMN_INDEX ) {
            return "<html>Measurement of how biased the category<br/> is towards multifunctional genes<br/>"
                    + "(1 = highest bias; very big groups not counted)</html>";
        } else if ( index >= GeneSetTableModel.INIT_COLUMNS ) {
            int runIndex = model.getRunIndex( index );
            return resultToolTips.get( runIndex );
        }
        return null;
    }

    /**
     * <p>
     * getRunName.
     * </p>
     *
     * @param runIndex a int.
     * @return a {@link java.lang.String} object.
     */
    protected String getRunName( int runIndex ) {
        return mainFrame.getResultSet( runIndex ).getName();
    }

    /** {@inheritDoc} */
    @Override
    protected GeneSetTerm popupRespondAndGetGeneSet( MouseEvent e ) {
        JTable source = ( JTable ) e.getSource();
        assert source != null;
        int r = source.rowAtPoint( e.getPoint() );
        GeneSetTerm id = ( GeneSetTerm ) source.getValueAt( r, 0 );
        return id;
    }

    /*
     * (non-Javadoc)
     *
     * @see ubic.erminej.gui.GeneSetPanel#removedGeneSet(ubic.erminej.data.GeneSetTerm)
     */
    /** {@inheritDoc} */
    @Override
    protected void removedGeneSet( GeneSetTerm addedTerm ) {
        refreshView();
    }

    /**
     * <p>
     * removeRunPopupMenu_actionPerformed.
     * </p>
     *
     * @param e a {@link java.awt.event.ActionEvent} object.
     */
    protected void removeRunPopupMenu_actionPerformed( ActionEvent e ) {
        EditRunPopupMenu sourcePopup = ( EditRunPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        int currentColumnIndex = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
        removeRun( currentColumnIndex );
    }

    /**
     * <p>
     * renameRun.
     * </p>
     *
     * @param runIndex a int.
     * @param newName a {@link java.lang.String} object.
     */
    protected void renameRun( int runIndex, String newName ) {
        TableColumn col = table.getColumn( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        model.renameRun( runIndex, newName );
        col.setIdentifier( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        GeneSetPvalRun resultSet = mainFrame.getResultSet( runIndex );
        resultSet.setName( newName );
        resultSet.setSavedToFile( false ); // invalidate

        List<? extends SortKey> sortKeys = new ArrayList<SortKey>( sorter.getSortKeys() );
        model.fireTableStructureChanged();
        sorter.setSortKeys( sortKeys );

        this.mainFrame.updateRunViewMenu();
    }

    /**
     * for the table heading
     */
    protected void setUpHeaderPopupMenus() {

        EditRunPopupMenu runPopupMenu = new EditRunPopupMenu();

        final JMenuItem removeRunMenuItem = new JMenuItem( "Remove this run ..." );
        removeRunMenuItem.addActionListener( new RemoveRunPopupMenu_actionAdapter( this ) );
        runPopupMenu.add( removeRunMenuItem );

        JMenuItem renameRunMenuItem = new JMenuItem( "Rename this run ..." );
        renameRunMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                EditRunPopupMenu sourcePopup = ( EditRunPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                int columnIndex = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
                int runIndex = model.getRunIndex( columnIndex );
                String newName = JOptionPane.showInputDialog( removeRunMenuItem, "Enter a new name",
                        getRunName( runIndex ) );

                if ( newName != null && newName.length() > 0 ) {
                    renameRun( runIndex, newName );
                }
            }
        } );

        runPopupMenu.add( renameRunMenuItem );

        JMenuItem saveRunMenuItem = new JMenuItem( "Save this run ..." );
        saveRunMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                EditRunPopupMenu sourcePopup = ( EditRunPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                int columnIndex = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
                int runIndex = model.getRunIndex( columnIndex );
                mainFrame.setCurrentResultSetIndex( runIndex );
                mainFrame.saveAnalysisAction();
            }
        } );

        runPopupMenu.add( saveRunMenuItem );

        MouseListener removeRunPopupListener = new EditRunPopupListener( runPopupMenu );
        table.getTableHeader().addMouseListener( removeRunPopupListener );

    }

    /**
     *
     */
    private void clearRowFilter() {
        SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                sorter.setRowFilter( null );
                resortByCurrentResults();
                return null;
            }
        };
        r.execute();
    }

    /**
     * @param currentColumnIndex
     */
    private void removeRun( final int currentColumnIndex ) {

        final String columnName = model.getColumnName( currentColumnIndex );

        int yesno = JOptionPane.showConfirmDialog( null, "Are you sure you want to remove " + columnName + "?",
                "Remove Run", JOptionPane.YES_NO_OPTION );
        if ( yesno == JOptionPane.YES_OPTION ) {

            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    int runIndex = model.getRunIndex( currentColumnIndex );
                    assert runIndex >= 0;
                    TableColumn col = table.getColumn( columnName );
                    assert col != null;

                    table.removeColumn( col );

                    model.removeRunData( currentColumnIndex );

                    model.fireTableStructureChanged();

                    mainFrame.setCurrentResultSetIndex( runIndex );
                    resultToolTips.remove( runIndex );
                    table.revalidate();

                    /*
                     * Now really try to remove the object.
                     */

                    mainFrame.removeRun( runIndex );

                    // Resort by a remaining run, after removing a run; otherwise, sort by the first column
                    List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                    int newSortIndex = 0;
                    if ( mainFrame.getNumResultSets() > 0 ) {
                        newSortIndex = GeneSetTableModel.INIT_COLUMNS;
                    }
                    sortKeys.add( new RowSorter.SortKey( newSortIndex, SortOrder.ASCENDING ) );
                    sorter.setSortKeys( sortKeys );

                }
            } );

        }

    }

    /**
     *
     */
    private void setTableAttributes() {
        table.setModel( model );
        this.getViewport().add( table, null );
        table.getColumnModel().getColumn( 0 ).setPreferredWidth( GENESET_ID_COLUMN_WIDTH );
        table.getColumnModel().getColumn( 1 ).setPreferredWidth( GENESET_NAME_COLUMN_WIDTH );
        // table.getColumnModel().getColumn( PROBE_COUNT_COLUMN_INDEX ).setPreferredWidth( NUMPROBES_COLUMN_WIDTH );
        table.getColumnModel().getColumn( GENE_COUNT_COLUMN_INDEX ).setPreferredWidth( NUMGENES_COLUMN_WIDTH );
        table.getColumnModel().getColumn( MULTIFUNC_COLUMN_INDEX ).setPreferredWidth( NUMGENES_COLUMN_WIDTH );

        table.setDefaultRenderer( String.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( Double.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( Integer.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( GeneSetTerm.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( GeneSetResult.class, new GeneSetTableCellRenderer( this.geneData ) );

        assert geneData != null;
        table.revalidate();
    }

    /**
     *
     */
    private void setUpTable() {
        table = new JTable() {
            private static final long serialVersionUID = 1L;

            // Implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader( columnModel ) {
                    private static final long serialVersionUID = -1L;

                    @Override
                    /** {@inheritDoc} */
                    public String getToolTipText( MouseEvent e ) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX( p.x );
                        int realIndex = columnModel.getColumn( index ).getModelIndex();
                        return getHeaderToolTip( realIndex );
                        /** {@inheritDoc} */
                    }
                };
            }
        };

        assert geneData != null;
        model = new GeneSetTableModel( geneData, mainFrame.getResultSets() );
        table.setModel( model );

        table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        MouseListener m = super.configurePopupListener();
        table.addMouseListener( m );

        sorter = new TableRowSorter<>( ( GeneSetTableModel ) table.getModel() );

        table.setRowSorter( sorter );
        table.addMouseListener( new GeneSetTableMouseAdapter( this ) );
        table.getTableHeader().setReorderingAllowed( false );
        table.getTableHeader().addMouseListener( new MouseAdapter() {
            @Override
            public void mouseEntered( MouseEvent e ) {
                setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                /**
                 * <p>
                 * getPoint.
                 * </p>
                 *
                 * @return a {@link java.awt.Point} object.
                 */
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                /**
                 * <p>
                 * Constructor for FindInTreeListener.
                 * </p>
                 */
                setCursor( Cursor.getDefaultCursor() );
            }
        } );
    }

}

class EditRunPopupListener extends MouseAdapter {
    EditRunPopupMenu popup;

    EditRunPopupListener( EditRunPopupMenu popupMenu ) {
        /** {@inheritDoc} */
        popup = popupMenu;
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed( MouseEvent e ) {
        maybeShowPopup( e );
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased( MouseEvent e ) {
        maybeShowPopup( e );
    }

    private void maybeShowPopup( MouseEvent e ) {
        if ( e.isPopupTrigger() ) {
            JTableHeader source = ( JTableHeader ) e.getSource();
            /** {@inheritDoc} */
            int c = source.columnAtPoint( e.getPoint() );
            if ( c >= GeneSetTableModel.INIT_COLUMNS ) {
                popup.show( e.getComponent(), e.getX(), e.getY() );
                popup.setPoint( e.getPoint() );
            }
        }
    }
}

// //////////////////////////////////////////////////////////////////////////////
class EditRunPopupMenu extends JPopupMenu {

    private static final long serialVersionUID = 1L;
    Point popupPoint;

    /**
     * <p>getPoint.</p>
     *
     * @return a {@link java.awt.Point} object.
     */
    public Point getPoint() {
        return popupPoint;
    }

    /**
     * <p>Constructor for FindInTreeListener.</p>
     */
    public void setPoint( Point point ) {
        popupPoint = point;
    }
}

class FindInTreeListener implements ActionListener {
    /** {@inheritDoc} */
    GeneSetPanel adaptee;

    /**
     * @param adaptee
     */
    /** {@inheritDoc} */
    public FindInTreeListener( GeneSetPanel adaptee ) {
        super();
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.findInTree( e );

    }

/** {@inheritDoc} */
}

class GeneSetTableMouseAdapter extends java.awt.event.MouseAdapter {
    GeneSetTablePanel adaptee;

    GeneSetTableMouseAdapter( GeneSetTablePanel adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        if ( e.getClickCount() < 2 ) {
            return;
        }
        adaptee.table_mouseReleased( e );
    }
}

class RemoveRunPopupMenu_actionAdapter implements java.awt.event.ActionListener {
    GeneSetTablePanel adaptee;

    RemoveRunPopupMenu_actionAdapter( GeneSetTablePanel adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.removeRunPopupMenu_actionPerformed( e );
    }
}
