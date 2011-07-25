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
package ubic.erminej.gui;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import ubic.erminej.GeneSetPvalRun;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;

/**
 * A table that lists the Gene Sets with their scores, and allows user interaction.
 * 
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetTablePanel extends GeneSetPanel {

    private static final long serialVersionUID = -1L;
    private final static int GENESET_ID_COLUMN_WIDTH = 80;
    private final static int GENESET_NAME_COLUMN_WIDTH = 350;
    private final static int NUMPROBES_COLUMN_WIDTH = 40;
    private final static int NUMGENES_COLUMN_WIDTH = 40;
    private final static int RUN_COLUMN_START_WIDTH = 80;
    public static final int PROBE_COUNT_COLUMN_INDEX = 2;
    public static final int GENE_COUNT_COLUMN_INDEX = 3;
    public static final int MULTIFUNC_COLUMN_INDEX = 4;

    private String classColToolTip;
    private int currentResultSetIndex = -1;
    protected GeneSetTableModel model = null;
    protected List<String> resultToolTips = new LinkedList<String>();

    protected JTable table = null;

    public int getRowCount() {
        return this.table.getRowCount();
    }

    public GeneSetTablePanel( GeneSetScoreFrame callingFrame, List<GeneSetPvalRun> results, Settings settings ) {
        super( settings, results, callingFrame );
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
                    public String getToolTipText( MouseEvent e ) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX( p.x );
                        int realIndex = columnModel.getColumn( index ).getModelIndex();
                        return getHeaderToolTip( realIndex );
                    }
                };
            }
        };

        assert geneData != null;
        model = new GeneSetTableModel( geneData, results );
        table.setModel( model );

        MouseListener m = super.configurePopupListener();
        table.addMouseListener( m );

        sorter = new TableRowSorter<GeneSetTableModel>( ( GeneSetTableModel ) table.getModel() );

        table.setRowSorter( sorter );
        table.addMouseListener( new OutputPanel_mouseAdapter( this ) );
        table.getTableHeader().setReorderingAllowed( false );
        table.getTableHeader().addMouseListener( new MouseAdapter() {
            @Override
            public void mouseEntered( MouseEvent e ) {
                setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                setCursor( Cursor.getDefaultCursor() );
            }
        } );
    }

    private TableRowSorter<GeneSetTableModel> sorter;

    /**
     * for the table heading
     */
    protected void setUpHeaderPopupMenus() {

        EditRunPopupMenu removeRunPopup = new EditRunPopupMenu();

        JMenuItem removeRunMenuItem = new JMenuItem( "Remove this run..." );
        removeRunMenuItem.addActionListener( new RemoveRunPopupMenu_actionAdapter( this ) );
        removeRunPopup.add( removeRunMenuItem );

        JMenuItem renameRunMenuItem = new JMenuItem( "Rename this run..." );
        renameRunMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                EditRunPopupMenu sourcePopup = ( EditRunPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                int columnIndex = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
                int runIndex = model.getRunIndex( columnIndex );
                String newName = JOptionPane.showInputDialog( table, "Enter a new name", getRunName( runIndex ) );

                if ( newName != null && newName.length() > 0 ) {
                    renameRun( runIndex, newName );
                }
            }
        } );
        removeRunPopup.add( renameRunMenuItem );
        MouseListener removeRunPopupListener = new EditRunPopupListener( removeRunPopup );
        table.getTableHeader().addMouseListener( removeRunPopupListener );
    }

    /**
     * @param runIndex
     * @return
     */
    protected String getRunName( int runIndex ) {
        return results.get( runIndex ).getName();
    }

    /**
     * @param runIndex
     * @param newName
     */
    protected void renameRun( int runIndex, String newName ) {
        TableColumn col = table.getColumn( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        model.renameRun( runIndex, newName );
        col.setIdentifier( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        results.get( runIndex ).setName( newName );
        model.fireTableStructureChanged();
        this.callingFrame.updateRunViewMenu();
    }

    @Override
    public void addedNewGeneSet() {
        table.revalidate();
    }

    // called when we first set up the table.
    public void initialize( GeneAnnotations initialGoData ) {
        this.geneData = initialGoData;
        setUpTable();
        setUpHeaderPopupMenus();
        setTableAttributes();
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add( new RowSorter.SortKey( 0, SortOrder.ASCENDING ) );
        sorter.setSortKeys( sortKeys );
    }

    @Override
    public void addRun() {
        model.addRun();
        int c = model.getColumnCount() - 1;
        TableColumn col = new TableColumn( c );
        col.setIdentifier( model.getColumnName( c ) );

        table.addColumn( col );
        table.getColumnModel().getColumn( c ).setPreferredWidth( RUN_COLUMN_START_WIDTH );
        generateToolTip( model.getColumnCount() - GeneSetTableModel.INIT_COLUMNS - 1 );

        if ( results.size() > 3 ) table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        currentResultSetIndex = results.size() - 1;
        this.callingFrame.setCurrentResultSet( currentResultSetIndex );
        table.revalidate();

        this.model.filter();

        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add( new RowSorter.SortKey( c, SortOrder.ASCENDING ) );
        sorter.setSortKeys( sortKeys );
    }

    // called if 'cancel', 'find' or 'reset' have been hit.
    @Override
    public void resetView() {
        this.geneData = callingFrame.getOriginalGeneData();
        setTableAttributes();
        clearRowFilter();
    }

    /**
     * 
     */
    private void setTableAttributes() {
        table.setModel( model );
        this.getViewport().add( table, null );
        table.getColumnModel().getColumn( 0 ).setPreferredWidth( GENESET_ID_COLUMN_WIDTH );
        table.getColumnModel().getColumn( 1 ).setPreferredWidth( GENESET_NAME_COLUMN_WIDTH );
        table.getColumnModel().getColumn( PROBE_COUNT_COLUMN_INDEX ).setPreferredWidth( NUMPROBES_COLUMN_WIDTH );
        table.getColumnModel().getColumn( GENE_COUNT_COLUMN_INDEX ).setPreferredWidth( NUMGENES_COLUMN_WIDTH );
        table.getColumnModel().getColumn( MULTIFUNC_COLUMN_INDEX ).setPreferredWidth( NUMGENES_COLUMN_WIDTH );

        table.setDefaultRenderer( String.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( Double.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( Integer.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( GeneSetTerm.class, new GeneSetTableCellRenderer( this.geneData ) );
        table.setDefaultRenderer( GeneSetResult.class, new GeneSetTableCellRenderer( this.geneData ) );

        assert geneData != null;
        classColToolTip = new String( "Total classes shown: " + table.getRowCount() );
        table.revalidate();
    }

    @Override
    protected boolean deleteUserGeneSet( GeneSetTerm classID ) {
        boolean deleted = super.deleteUserGeneSet( classID );
        /*
         * Since the table model is backed by the geneAnnots, this should work?
         */
        table.revalidate();
        return deleted;

    }

    /**
     * @param e
     * @return
     */
    @Override
    protected GeneSetTerm popupRespondAndGetGeneSet( MouseEvent e ) {
        JTable source = ( JTable ) e.getSource();
        assert source != null;
        int r = source.rowAtPoint( e.getPoint() );
        GeneSetTerm id = ( GeneSetTerm ) source.getValueAt( r, 0 );
        return id;
    }

    /**
     * Create the text shown when user hovers mouse over the heading of a result column
     * 
     * @param runIndex
     */
    protected void generateToolTip( int runIndex ) {
        assert results != null : "Null results";
        assert results.get( runIndex ) != null : "No results with index " + runIndex;
        log.debug( "Generating tooltip for run #" + runIndex );
        Settings runSettings = results.get( runIndex ).getSettings();
        String tooltip = new String( "<html>" );
        String coda = new String();

        if ( runSettings.getClassScoreMethod().equals( Settings.Method.ORA ) ) {
            tooltip += "ORA Analysis<br>";
            coda += "P value threshold: " + runSettings.getGeneScoreThreshold();
        } else if ( runSettings.getClassScoreMethod().equals( Settings.Method.GSR ) ) {
            tooltip += "Resampling Analysis<br>";
            coda += runSettings.getIterations() + " iterations<br>";
            coda += "Using score column: " + runSettings.getScoreCol();
        } else if ( runSettings.getClassScoreMethod().equals( Settings.Method.CORR ) ) {
            tooltip += "Correlation Analysis<br>";
            coda += runSettings.getIterations() + " iterations";
        } else if ( runSettings.getClassScoreMethod().equals( Settings.Method.ROC ) ) {
            tooltip += "ROC Analysis<br>";
        }

        tooltip += String.format( "Multifunct. bias: %.2f<br>", results.get( runIndex )
                .getMultifunctionalityCorrelation() );

        tooltip += new String( "Max set size: " + runSettings.getMaxClassSize() + "<br>" + "Min set size: "
                + runSettings.getMinClassSize() + "<br>" );
        if ( runSettings.getDoLog() ) tooltip += "Log normalized<br>";

        if ( runSettings.getGeneRepTreatment().equals( Settings.MultiProbeHandling.MEAN ) )
            tooltip += "Gene Rep Treatment: Mean <br>";
        else if ( runSettings.getGeneRepTreatment().equals( Settings.MultiProbeHandling.BEST ) )
            tooltip += "Gene Rep Treatment: Best <br>";
        if ( runSettings.getClassScoreMethod().equals( Settings.Method.GSR )
                || runSettings.getClassScoreMethod().equals( Settings.Method.ORA ) ) {
            if ( runSettings.getGeneSetResamplingScoreMethod().equals( Settings.GeneScoreMethod.MEAN ) )
                tooltip += "Class Raw Score Method: Mean <br>";
            else if ( runSettings.getGeneSetResamplingScoreMethod().equals( Settings.GeneScoreMethod.QUANTILE ) )
                tooltip += "Class Raw Score Method: Median <br>";
        }

        tooltip += coda;
        resultToolTips.add( runIndex, tooltip );
    }

    /**
     * @param index
     * @return
     */
    protected String getHeaderToolTip( int index ) {
        if ( index == 0 || index == 1 ) { // descriptions of the category.
            return this.classColToolTip;
        } else if ( index == PROBE_COUNT_COLUMN_INDEX ) {
            return "How many probes are in the group (there can be more than one probe per gene)";
        } else if ( index == GENE_COUNT_COLUMN_INDEX ) {
            return "How many genes are in the group";
        } else if ( index == MULTIFUNC_COLUMN_INDEX ) {
            return "Measurement of how biased the category is towards multifunctional genes";
        } else if ( index >= GeneSetTableModel.INIT_COLUMNS ) {
            int runIndex = model.getRunIndex( index );
            return resultToolTips.get( runIndex );
        }
        return null;
    }

    protected void removeRunPopupMenu_actionPerformed( ActionEvent e ) {
        EditRunPopupMenu sourcePopup = ( EditRunPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        int currentColumnIndex = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
        removeRun( currentColumnIndex );
    }

    @Override
    public void showPopupMenu( final MouseEvent e ) {

        GeneSetPanelPopupMenu popup = super.configurePopup( e );

        if ( popup == null ) return;

        final JCheckBoxMenuItem hideRedund = new JCheckBoxMenuItem( "Hide redundant", true );
        hideRedund.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                filterRedundant( hideRedund.getState() );
            }
        } );

        // / this is a bit redundant as it seems they are filtered out earlier.
        final JCheckBoxMenuItem hideEmpty = new JCheckBoxMenuItem( "Hide empty sets", true );
        hideEmpty.setToolTipText( "Hide sets lacking members" );
        hideEmpty.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                filterEmpty( hideEmpty.getState() );
            }
        } );

        final JCheckBoxMenuItem hideNonResults = new JCheckBoxMenuItem( "Hide non-results", true );
        hideNonResults.setToolTipText( "Hide rows lacking any results" );
        hideNonResults.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e1 ) {
                filterNonResults( hideNonResults.getState() );
            }

        } );

        popup.add( hideRedund );
        popup.add( hideEmpty );
        popup.add( hideNonResults );

        final JMenuItem findInTreeMenuItem = new JMenuItem( "Find this set in the tree panel" );
        findInTreeMenuItem.addActionListener( new FindInTreeListener( this ) );
        popup.add( findInTreeMenuItem );
        popup.show( e.getComponent(), e.getX(), e.getY() );
    }

    /**
     * @param currentColumnIndex
     */
    private void removeRun( int currentColumnIndex ) {
        String columnName = model.getColumnName( currentColumnIndex );
        TableColumn col = table.getColumn( columnName );
        assert col != null;

        int yesno = JOptionPane.showConfirmDialog( null, "Are you sure you want to remove " + columnName + "?",
                "Remove Run", JOptionPane.YES_NO_OPTION );
        if ( yesno == JOptionPane.YES_OPTION ) {
            log.debug( "remove popup for col: " + currentColumnIndex );
            int runIndex = model.getRunIndex( currentColumnIndex );
            assert runIndex >= 0;
            table.removeColumn( col );
            model.removeRunData( currentColumnIndex );

            model.fireTableStructureChanged();
            callingFrame.setCurrentResultSet( runIndex );

            results.remove( runIndex ); // This should be done by the parent.
            resultToolTips.remove( runIndex );

            table.revalidate();

            // bug 159! pretty sure this is working .. trying to get the table to resort by a remaining run, after
            // removing a run.
            List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
            int newSortIndex = 0;
            if ( results.size() > 0 ) {
                newSortIndex = GeneSetTableModel.INIT_COLUMNS;
            }
            sortKeys.add( new RowSorter.SortKey( newSortIndex, SortOrder.ASCENDING ) );
            sorter.setSortKeys( sortKeys );
        }
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
            run = this.results.get( _runnum );
            this.currentResultSetIndex = _runnum;
        }

        messenger.showStatus( "Viewing details for " + term + "..." );

        showDetailsForGeneSet( run, term );

    }

    public void filterByUserGeneSets( boolean b ) {
        if ( b ) {
            RowFilter<GeneSetTableModel, Object> rf = new RowFilter<GeneSetTableModel, Object>() {

                @Override
                public boolean include( RowFilter.Entry<? extends GeneSetTableModel, ? extends Object> entry ) {

                    GeneSetTerm term = ( GeneSetTerm ) entry.getValue( 0 );
                    return term.isUserDefined();
                }
            };
            this.sorter.setRowFilter( rf );

        } else {
            this.sorter.setRowFilter( null );

        }
        resortByCurrentResults();
    }

    public void clearRowFilter() {
        this.sorter.setRowFilter( null );
        resortByCurrentResults();
    }

    public void filter( final Collection<GeneSetTerm> selectedTerms ) {
        RowFilter<GeneSetTableModel, Object> rf = new RowFilter<GeneSetTableModel, Object>() {
            @Override
            public boolean include( RowFilter.Entry<? extends GeneSetTableModel, ? extends Object> entry ) {
                GeneSetTerm term = ( GeneSetTerm ) entry.getValue( 0 );
                return selectedTerms.contains( term );
            }
        };
        this.sorter.setRowFilter( rf );
        resortByCurrentResults();
    }

    public void filterRedundant( boolean b ) {
        this.model.setFilterRedundant( b );
        table.revalidate();
        resortByCurrentResults();
    }

    public void filterEmpty( boolean b ) {
        this.model.setFilterEmpty( b );
        table.revalidate();
        resortByCurrentResults();
    }

    private void filterNonResults( boolean b ) {
        if ( currentResultSetIndex < 0 ) return;
        this.model.setFilterEmptyResults( b );
        table.revalidate();

        resortByCurrentResults();
    }

    /**
     * 
     */
    private void resortByCurrentResults() {
        int sortColumn = 0;
        if ( currentResultSetIndex > 0 ) {
            sortColumn = GeneSetTableModel.INIT_COLUMNS + currentResultSetIndex;
        }
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

        sortKeys.add( new RowSorter.SortKey( sortColumn, SortOrder.ASCENDING ) );
        sorter.setSortKeys( sortKeys );
    }
}

// //////////////////////////////////////////////////////////////////////////////
class EditRunPopupMenu extends JPopupMenu {
    /**
     * 
     */
    private static final long serialVersionUID = -6328435511579332465L;
    Point popupPoint;

    public Point getPoint() {
        return popupPoint;
    }

    public void setPoint( Point point ) {
        popupPoint = point;
    }
}

class FindInTreeListener implements ActionListener {
    GeneSetPanel adaptee;

    /**
     * @param adaptee
     */
    public FindInTreeListener( GeneSetPanel adaptee ) {
        super();
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e ) {
        adaptee.findInTreeMenuItem_actionAdapter( e );

    }

}

class OutputPanel_mouseAdapter extends java.awt.event.MouseAdapter {
    GeneSetTablePanel adaptee;

    OutputPanel_mouseAdapter( GeneSetTablePanel adaptee ) {
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

class EditRunPopupListener extends MouseAdapter {
    EditRunPopupMenu popup;

    EditRunPopupListener( EditRunPopupMenu popupMenu ) {
        popup = popupMenu;
    }

    @Override
    public void mousePressed( MouseEvent e ) {
        maybeShowPopup( e );
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        maybeShowPopup( e );
    }

    private void maybeShowPopup( MouseEvent e ) {
        if ( e.isPopupTrigger() ) {
            JTableHeader source = ( JTableHeader ) e.getSource();
            int c = source.columnAtPoint( e.getPoint() );
            if ( c >= GeneSetTableModel.INIT_COLUMNS ) {
                popup.show( e.getComponent(), e.getX(), e.getY() );
                popup.setPoint( e.getPoint() );
            }
        }
    }
}

class RemoveRunPopupMenu_actionAdapter implements java.awt.event.ActionListener {
    GeneSetTablePanel adaptee;

    RemoveRunPopupMenu_actionAdapter( GeneSetTablePanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.removeRunPopupMenu_actionPerformed( e );
    }
}
