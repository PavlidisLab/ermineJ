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
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
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

    private static Log log = LogFactory.getLog( GeneSetTablePanel.class );

    private static final long serialVersionUID = -1L;
    private final static int GENESET_ID_COLUMN_WIDTH = 80;
    private final static int GENESET_NAME_COLUMN_WIDTH = 350;
    private final static int NUMPROBES_COLUMN_WIDTH = 40;
    private final static int NUMGENES_COLUMN_WIDTH = 40;
    private final static int RUN_COLUMN_START_WIDTH = 80;
    public static final int PROBE_COUNT_COLUMN_INDEX = 2;
    public static final int GENE_COUNT_COLUMN_INDEX = 3;
    public static final int MULTIFUNC_COLUMN_INDEX = 4;

    protected GeneSetTableModel model = null;
    protected List<String> resultToolTips = new LinkedList<String>();

    protected JTable table = null;

    public int getRowCount() {
        return this.table.getRowCount();
    }

    public GeneSetTablePanel( MainFrame callingFrame, Settings settings ) {
        super( settings, callingFrame );
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
        model = new GeneSetTableModel( geneData, callingFrame.getResultSets() );
        table.setModel( model );

        MouseListener m = super.configurePopupListener();
        table.addMouseListener( m );

        sorter = new TableRowSorter<GeneSetTableModel>( ( GeneSetTableModel ) table.getModel() );

        table.setRowSorter( sorter );
        table.addMouseListener( new GeneSetTableMouseAdapter( this ) );
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

        EditRunPopupMenu runPopupMenu = new EditRunPopupMenu();

        JMenuItem removeRunMenuItem = new JMenuItem( "Remove this run ..." );
        removeRunMenuItem.addActionListener( new RemoveRunPopupMenu_actionAdapter( this ) );
        runPopupMenu.add( removeRunMenuItem );

        JMenuItem renameRunMenuItem = new JMenuItem( "Rename this run ..." );
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

        runPopupMenu.add( renameRunMenuItem );

        JMenuItem saveRunMenuItem = new JMenuItem( "Save this run ..." );
        saveRunMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                callingFrame.saveAnalysisAction();
            }
        } );

        runPopupMenu.add( saveRunMenuItem );

        MouseListener removeRunPopupListener = new EditRunPopupListener( runPopupMenu );
        table.getTableHeader().addMouseListener( removeRunPopupListener );

    }

    /**
     * @param runIndex
     * @return
     */
    protected String getRunName( int runIndex ) {
        return callingFrame.getResultSet( runIndex ).getName();
    }

    /**
     * @param runIndex
     * @param newName
     */
    protected void renameRun( int runIndex, String newName ) {
        TableColumn col = table.getColumn( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        model.renameRun( runIndex, newName );
        col.setIdentifier( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        callingFrame.getResultSet( runIndex ).setName( newName );
        model.fireTableStructureChanged();
        this.callingFrame.updateRunViewMenu();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.erminej.gui.GeneSetPanel#addedGeneSet(ubic.erminej.data.GeneSetTerm)
     */
    @Override
    public void addedGeneSet( GeneSetTerm id ) {
        refreshView();
    }

    /**
     * Revalidate, refilter and (resort? NO) the table.
     */
    @Override
    public void refreshView() {

        this.messenger.showStatus( "Updating view" );
        //       
        // List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        // sortKeys.add( new RowSorter.SortKey( 0, SortOrder.ASCENDING ) );
        // sorter.setSortKeys( sortKeys );
        this.model.filter();
        this.messenger.showStatus( table.getRowCount() + " sets shown" );
        table.revalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.erminej.gui.GeneSetPanel#removedGeneSet(ubic.erminej.data.GeneSetTerm)
     */
    @Override
    protected void removedGeneSet( GeneSetTerm addedTerm ) {
        refreshView();
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
        generateResultColumnHeadingTooltip( model.getColumnCount() - GeneSetTableModel.INIT_COLUMNS - 1 );

        int currentResultSetIndex = callingFrame.getNumResultSets() - 1;
        this.callingFrame.setCurrentResultSetIndex( currentResultSetIndex );
        table.revalidate();

        this.model.filter();

        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add( new RowSorter.SortKey( c, SortOrder.ASCENDING ) );
        sorter.setSortKeys( sortKeys );
    }

    // called if 'cancel', 'find' or 'reset' have been hit.
    @Override
    public void resetView() {
        filter( new HashSet<GeneSetTerm>() );
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
        table.revalidate();
    }

    @Override
    protected boolean deleteUserGeneSet( GeneSetTerm classID ) {
        boolean deleted = super.deleteUserGeneSet( classID );
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
    protected void generateResultColumnHeadingTooltip( int runIndex ) {

        GeneSetPvalRun geneSetPvalRun = callingFrame.getResultSet( runIndex );
        assert geneSetPvalRun != null : "No results with index " + runIndex;
        log.debug( "Generating tooltip for run #" + runIndex );
        SettingsHolder runSettings = geneSetPvalRun.getSettings();
        GeneScores gs = geneSetPvalRun.getGeneScores();
        String tooltip = new String( "<html>" );
        String coda = new String();

        // int numProbes = gs.getNumProbesUsed();
        int numGenes = gs.getNumGenesUsed();

        if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.ORA ) ) {
            tooltip += "ORA Analysis<br>";
            coda += "Score threshold: " + runSettings.getGeneScoreThreshold();
            coda += ", Genes meeting: " + gs.numGenesAboveThreshold( runSettings.getGeneScoreThreshold() ) + "<br/>";
        } else if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.GSR ) ) {
            tooltip += "Resampling Analysis<br>";
            coda += runSettings.getIterations() + " iterations<br>";
            coda += "Using score column: " + runSettings.getScoreCol();
        } else if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) ) {
            tooltip += "Correlation Analysis<br>";
            coda += runSettings.getIterations() + " iterations";
        } else if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.ROC ) ) {
            tooltip += "ROC Analysis<br>";
        }

        tooltip += String.format( "Multifunct. bias: %.2f, %d genes<br>", geneSetPvalRun
                .getMultifunctionalityCorrelation(), numGenes );

        tooltip += new String( "Max set size: " + runSettings.getMaxClassSize() + "<br>" + "Min set size: "
                + runSettings.getMinClassSize() + "<br>" );
        if ( runSettings.getDoLog() ) tooltip += "Negative log transformed<br>";

        if ( runSettings.useMultifunctionalityCorrection() ) {
            tooltip += "Multifunctionality corrected<br>";
        }

        if ( runSettings.getGeneRepTreatment().equals( SettingsHolder.MultiProbeHandling.MEAN ) )
            tooltip += "Gene Rep Treatment: Mean <br>";
        else if ( runSettings.getGeneRepTreatment().equals( SettingsHolder.MultiProbeHandling.BEST ) )
            tooltip += "Gene Rep Treatment: Best <br>";

        if ( runSettings.getClassScoreMethod().equals( SettingsHolder.Method.GSR ) ) {
            if ( runSettings.getGeneSetResamplingScoreMethod().equals( SettingsHolder.GeneScoreMethod.MEAN ) )
                tooltip += "Class Raw Score Method: Mean <br>";
            else if ( runSettings.getGeneSetResamplingScoreMethod().equals( SettingsHolder.GeneScoreMethod.QUANTILE ) )
                tooltip += "Class Raw Score Method: Median <br>";
        }

        if ( runSettings.getScoreFile() != null ) {
            String fileName = new File( runSettings.getScoreFile() ).getName();
            coda += "Scores: " + fileName + "<br/>";
        }

        if ( runSettings.getRawDataFileName() != null ) {
            String fileName = new File( runSettings.getRawDataFileName() ).getName();
            coda += "Profiles: " + fileName + "<br/>";
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
            return "Classes shown: " + table.getRowCount();
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
            int runIndex = model.getRunIndex( currentColumnIndex );
            assert runIndex >= 0;

            table.removeColumn( col );
            model.removeRunData( currentColumnIndex );
            model.fireTableStructureChanged();
            callingFrame.setCurrentResultSetIndex( runIndex );
            resultToolTips.remove( runIndex );
            table.revalidate();

            /*
             * Now really try to remove the object.
             */
            callingFrame.removeRun( runIndex );

            // Resort by a remaining run, after removing a run.
            List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
            int newSortIndex = 0;
            if ( callingFrame.getNumResultSets() > 0 ) {
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
            run = callingFrame.getResultSet( _runnum );
            callingFrame.setCurrentResultSetIndex( _runnum );
        }

        messenger.showStatus( "Viewing details for " + term + "..." );

        showDetailsForGeneSet( term, run );

    }

    public void filterByUserGeneSets( boolean b ) {
        this.model.setFilterNonUsers( b );
        filter( false );
    }

    /**
     * 
     */
    private void clearRowFilter() {
        this.sorter.setRowFilter( null );
        resortByCurrentResults();
    }

    public void filter( final Collection<GeneSetTerm> selectedTerms ) {

        if ( selectedTerms.isEmpty() ) {
            clearRowFilter();
            return;
        }

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

    @Override
    public void filter( boolean propagate ) {
        this.model.setFilterEmpty( hideEmpty );
        this.model.setFilterEmptyResults( hideInsignificant );
        this.model.filter();

        if ( propagate ) this.callingFrame.getTreePanel().filter( false );

    }

    /**
     * Resort by the current result set, if there is one. Otherwise leave it alone.
     */
    private void resortByCurrentResults() {
        int sortColumn = 0;
        int currentResultSetIndex = callingFrame.getCurrentResultSetIndex();
        if ( currentResultSetIndex > 0 ) {
            sortColumn = GeneSetTableModel.INIT_COLUMNS + currentResultSetIndex;

            assert sortColumn < this.table.getColumnCount();

            List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
            sortKeys.add( new RowSorter.SortKey( sortColumn, SortOrder.ASCENDING ) );

            sorter.setSortKeys( sortKeys );
        }
    }

    @Override
    public void removeRun( GeneSetPvalRun runToRemove ) {
        // Perhaps nothing, since it was done locally? could refactor from removeRun(int);
    }

}

// //////////////////////////////////////////////////////////////////////////////
class EditRunPopupMenu extends JPopupMenu {

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
        adaptee.findInTree( e );

    }

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
