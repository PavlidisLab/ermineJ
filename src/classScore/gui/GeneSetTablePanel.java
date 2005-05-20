package classScore.gui;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.gui.table.TableSorter;
import baseCode.util.StatusViewer;
import classScore.GeneSetPvalRun;
import classScore.Settings;

/**
 * A table that lists the Gene Sets with their scores, and allows user interaction.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetTablePanel extends GeneSetPanel {

    private final static int GENESET_ID_COLUMN_WIDTH = 80;
    private final static int GENESET_NAME_COLUMN_WIDTH = 350;
    private final static int NUMPROBES_COLUMN_WIDTH = 40;
    private final static int NUMGENES_COLUMN_WIDTH = 40;
    private final static int RUN_COLUMN_START_WIDTH = 80;
    static Log log = LogFactory.getLog( GeneSetTablePanel.class.getName() );

    private String classColToolTip;
    private int currentResultSetIndex = -1;
    protected GeneSetTableModel model = null;
    protected List resultToolTips = new LinkedList();
    private TableSorter sorter;
    protected JTable table = null;

    public GeneSetTablePanel( GeneSetScoreFrame callingFrame, LinkedList results, Settings settings ) {
        super( settings, results, callingFrame );
        model = new GeneSetTableModel( results );
        setUpTable();
        setUpPopupMenus();
    }

    /**
     * 
     */
    private void setUpTable() {
        table = new JTable() {
            // Implement table header tool tips.
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader( columnModel ) {
                    public String getToolTipText( MouseEvent e ) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX( p.x );
                        int realIndex = columnModel.getColumn( index ).getModelIndex();
                        return getHeaderToolTip( realIndex );
                    }
                };
            }
        };
        table.addMouseListener( new OutputPanel_mouseAdapter( this ) );
        table.getTableHeader().setReorderingAllowed( false );
        table.getTableHeader().addMouseListener( new TableHeader_mouseAdapterCursorChanger( this ) );
    }

    /**
     * 
     */
    private void setUpPopupMenus() {

        MouseListener popupListener = configurePopupMenu();
        table.addMouseListener( popupListener );

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
        return ( ( GeneSetPvalRun ) results.get( runIndex ) ).getName();
    }

    /**
     * @param runIndex
     * @param newName
     */
    protected void renameRun( int runIndex, String newName ) {
        TableColumn col = table.getColumn( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        model.renameRun( runIndex, newName );
        col.setIdentifier( model.getColumnName( model.getColumnIndexForRun( runIndex ) ) );
        ( ( GeneSetPvalRun ) results.get( runIndex ) ).setName( newName );
        model.fireTableStructureChanged();
    }

    public void addedNewGeneSet() {
        sorter.cancelSorting();
        sorter.setSortingStatus( 0, TableSorter.ASCENDING );
        table.revalidate();
    }

    // called when we first set up the table.
    public void addInitialData( GONames initialGoData ) {
        super.addInitialData( initialGoData );
        model.addInitialData( geneData, initialGoData );
        setTableAttributes();
        sorter.cancelSorting();
        sorter.setSortingStatus( 1, TableSorter.ASCENDING );
        table.revalidate();
    }

    public void addRun() {
        model.addRun();
        int c = model.getColumnCount() - 1;
        TableColumn col = new TableColumn( c );
        col.setIdentifier( model.getColumnName( c ) );

        table.addColumn( col );
        table.getColumnModel().getColumn( c ).setPreferredWidth( RUN_COLUMN_START_WIDTH );
        generateToolTip( model.getColumnCount() - GeneSetTableModel.INIT_COLUMNS - 1 );
        sorter.cancelSorting();
        sorter.setSortingStatus( c, TableSorter.ASCENDING );
        if ( results.size() > 3 ) table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        currentResultSetIndex = results.size() - 1;
        table.revalidate();
    }

    /**
     * @return
     */
    public int getCurrentResultSet() {
        return this.currentResultSetIndex;
    }

    /**
     * @return classScore.data.GONames
     */
    public GONames getGoData() {
        return goData;
    }

    // called if 'cancel', 'find' or 'reset' have been hit.
    public void resetView() {
        this.geneData = callingFrame.getOriginalGeneData();
        model.setInitialData( geneData, goData );
        setTableAttributes();
        table.revalidate();
    }

    /**
     * @param messenger The messenger to set.
     */
    public void setMessenger( StatusViewer messenger ) {
        this.messenger = messenger;
    }

    /**
     * 
     */
    private void setTableAttributes() {
        sorter = new TableSorter( model );
        table.setModel( sorter );

        sorter.setTableHeader( table.getTableHeader() );
        this.getViewport().add( table, null );
        table.getColumnModel().getColumn( 0 ).setPreferredWidth( GENESET_ID_COLUMN_WIDTH );
        table.getColumnModel().getColumn( 1 ).setPreferredWidth( GENESET_NAME_COLUMN_WIDTH );
        table.getColumnModel().getColumn( 2 ).setPreferredWidth( NUMPROBES_COLUMN_WIDTH );
        table.getColumnModel().getColumn( 3 ).setPreferredWidth( NUMGENES_COLUMN_WIDTH );
        table.setDefaultRenderer( Object.class, new OutputPanelTableCellRenderer( goData, results ) );
        assert geneData != null;
        classColToolTip = new String( "Total classes shown: " + geneData.selectedSets() );
        table.revalidate();
    }

    protected MouseListener configurePopupMenu() {
        
        final OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
        final JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );
        final JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );
        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );
        final JMenuItem findInTreeMenuItem = new JMenuItem( "Find this set in the tree panel" );
        findInTreeMenuItem.addActionListener( new OutputPanel_findInTreeMenuItem_actionAdapter( this ) );

        final JMenuItem deleteGeneSetMenuItem = new JMenuItem( "Delete this gene set" );
        deleteGeneSetMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e.getSource() ).getParent();
                String classID = null;
                classID = sourcePopup.getSelectedItem();
                deleteGeneSet( classID );
            }
        } );

        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        popup.add( findInTreeMenuItem );
        popup.add( deleteGeneSetMenuItem );
        MouseListener popupListener = new MouseAdapter() {
            public void mousePressed( MouseEvent e ) {
                maybeShowPopup( e );
            }

            public void mouseReleased( MouseEvent e ) {
                maybeShowPopup( e );
            }

            private void maybeShowPopup( MouseEvent e ) {
                if ( e.isPopupTrigger() ) {
                    JTable source = ( JTable ) e.getSource();
                    assert source != null;
                    int r = source.rowAtPoint( e.getPoint() );
                    List id = ( Vector ) source.getValueAt( r, 0 );
                    if ( id != null ) {
                        assert popup != null;
                        if ( popup == null ) throw new NullPointerException( "popup is null" );
                        int row = source.rowAtPoint( e.getPoint() );
                        String classID = ( String ) ( ( Vector ) source.getValueAt( row, 0 ) ).get( 0 );
                        assert goData != null;
                        if ( !goData.getUserDefinedGeneSets().contains( classID ) ) {
                            deleteGeneSetMenuItem.setEnabled( false );
                            log.debug( "Won't show." );
                        } else {
                            deleteGeneSetMenuItem.setEnabled( true );
                        }
                        popup.show( e.getComponent(), e.getX(), e.getY() );
                        popup.setPoint( e.getPoint() );
                        popup.setSelectedItem( classID );
                    }
                }
            }
        };
        return popupListener;
    }

    protected void deleteGeneSet( String classID ) {
        super.deleteGeneSet( classID );
        model.fireTableStructureChanged();
    }

    void generateToolTip( int runIndex ) {
        assert results != null : "Null results";
        assert results.get( runIndex ) != null : "No results with index " + runIndex;
        log.debug( "Generating tooltip for run #" + runIndex );
        Settings runSettings = ( ( GeneSetPvalRun ) results.get( runIndex ) ).getSettings();
        String tooltip = new String( "<html>" );
        String coda = new String();

        if ( runSettings.getAnalysisMethod() == Settings.ORA ) {
            tooltip += "ORA Analysis<br>";
            coda += "P value threshold: " + runSettings.getPValThreshold();
        } else if ( runSettings.getAnalysisMethod() == Settings.RESAMP ) {
            tooltip += "Resampling Analysis<br>";
            coda += runSettings.getIterations() + " iterations<br>";
            coda += "Using score column: " + runSettings.getScorecol();
        } else if ( runSettings.getAnalysisMethod() == Settings.CORR ) {
            tooltip += "Correlation Analysis<br>";
            coda += runSettings.getIterations() + " iterations";
        }

        tooltip += new String( "Max set size: " + runSettings.getMaxClassSize() + "<br>" + "Min set size: "
                + runSettings.getMinClassSize() + "<br>" );
        if ( runSettings.getDoLog() ) tooltip += "Log normalized<br>";

        if ( runSettings.getGeneRepTreatment() == Settings.MEAN_PVAL )
            tooltip += "Gene Rep Treatment: Mean <br>";
        else if ( runSettings.getGeneRepTreatment() == Settings.BEST_PVAL ) tooltip += "Gene Rep Treatment: Best <br>";

        if ( runSettings.getAnalysisMethod() == Settings.RESAMP || runSettings.getAnalysisMethod() == Settings.ORA ) {
            if ( runSettings.getRawScoreMethod() == Settings.MEAN_METHOD )
                tooltip += "Class Raw Score Method: Mean <br>";
            else if ( runSettings.getRawScoreMethod() == Settings.QUANTILE_METHOD )
                tooltip += "Class Raw Score Method: Median <br>";
        }
        tooltip += coda;
        resultToolTips.add( runIndex, tooltip );
    }

    String getHeaderToolTip( int index ) {
        if ( index == 0 ) {
            return this.classColToolTip;
        } else if ( index >= GeneSetTableModel.INIT_COLUMNS ) {
            // int runIndex=(int)Math.floor((index - OutputTableModel.init_cols) / OutputTableModel.cols_per_run);
            int runIndex = model.getRunIndex( index );
            return ( String ) resultToolTips.get( runIndex );
        }
        return null;
    }

    void removeRunPopupMenu_actionPerformed( ActionEvent e ) {
        EditRunPopupMenu sourcePopup = ( EditRunPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        int currentColumnIndex = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
        removeRun( currentColumnIndex );
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
            table.removeColumn( col );
            model.removeRunData( currentColumnIndex );
            model.fireTableStructureChanged();

            if ( runIndex < 0 )
                throw new IllegalStateException( "Runindex was not found for column " + currentColumnIndex );

            // FIXME deletion of corresponding GeneAnnotations when remove is used, if there is a copy made.
            results.remove( runIndex );

            int treeRunSelected = callingFrame.getTreePanel().getCurrentlySelectedResultSetIndex();
            if ( treeRunSelected >= 1 && results.size() > 0 ) {
                log.debug( "Setting tree to show results " + ( treeRunSelected - 1 ) );
                callingFrame.getTreePanel().setCurrentlySelectedResultSetIndex( treeRunSelected - 1 );
                callingFrame.getTreePanel().setCurrentResultSet( ( GeneSetPvalRun ) results.get( treeRunSelected - 1 ) );
            } else {
                log.debug( "No results, resetting tree" );
                callingFrame.getTreePanel().setCurrentlySelectedResultSetIndex( -1 );
                callingFrame.getTreePanel().setCurrentResultSet( null );
            }
            callingFrame.getTreePanel().updateNodeStyles();

            resultToolTips.remove( runIndex );
            table.revalidate();
        }
    }

    /**
     * @param e MouseEvent
     */
    void table_mouseReleased( MouseEvent e ) {
        int i = table.getSelectedRow();
        int j = table.getSelectedColumn();

        int _runnum = -1;
        String _id = ( String ) ( ( Vector ) table.getValueAt( i, 0 ) ).get( 0 );
        if ( table.getValueAt( i, j ) != null && j >= GeneSetTableModel.INIT_COLUMNS ) {
            _runnum = model.getRunIndex( j );
        } else if ( table.getValueAt( i, j ) != null && j < GeneSetTableModel.INIT_COLUMNS ) {
            _runnum = -1;
        } else {
            log.debug( "Seeking column to show" ); // this code is never reached?
            // for ( int k = model.getColumnCount() - 1; k >= 0; k-- ) {
            // if ( table.getValueAt( i, k ) != null && k >= OutputTableModel.INIT_COLUMNS ) {
            // String message;
            // String shownRunName = model.getColumnName( k );
            // shownRunName = shownRunName.substring( 0, shownRunName.length() - 5 );
            // if ( j >= OutputTableModel.INIT_COLUMNS ) {
            // message = model.getColumnName( j );
            // message = message.substring( 0, message.length() - 5 );
            // message = message + " doesn't include the class " + _id + ". Showing " + shownRunName
            // + " instead.";
            // JOptionPane.showMessageDialog( this, message, "FYI", JOptionPane.PLAIN_MESSAGE );
            // }
            // _runnum = model.getRunNum( k );
            // break;
            // }
            // }
        }
        this.currentResultSetIndex = _runnum;
        final String id = _id;
        final int runIndex = _runnum;
        log.debug( "Showing details for " + id );
        if ( messenger != null ) messenger.setStatus( "Viewing data for " + id + "..." );
        new Thread() {
            public void run() {
                showDetailsForGeneSet( runIndex, id );
            }
        }.start();
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

class TableHeader_mouseAdapterCursorChanger extends java.awt.event.MouseAdapter {
    GeneSetTablePanel adaptee;

    TableHeader_mouseAdapterCursorChanger( GeneSetTablePanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void mouseEntered( MouseEvent e ) {
        Container c = adaptee.getParent();
        c.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
    }

    public void mouseExited( MouseEvent e ) {
        Container c = adaptee.getParent();
        c.setCursor( Cursor.getDefaultCursor() );
    }

}

class OutputPanel_findInTreeMenuItem_actionAdapter implements ActionListener {
    GeneSetPanel adaptee;

    /**
     * @param adaptee
     */
    public OutputPanel_findInTreeMenuItem_actionAdapter( GeneSetPanel adaptee ) {
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

    public void mousePressed( MouseEvent e ) {
        maybeShowPopup( e );
    }

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