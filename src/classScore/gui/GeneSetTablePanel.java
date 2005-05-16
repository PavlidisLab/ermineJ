package classScore.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.table.TableSorter;
import baseCode.util.StatusViewer;
import classScore.GeneSetPvalRun;
import classScore.Settings;
import classScore.data.GeneSetResult;
import corejava.Format;

/**
 * A table that lists the Gene Sets with their scores, and allows user interaction.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 * @todo deletion of geneDataSets when remove is used.
 */
public class GeneSetTablePanel extends GeneSetsResultsScrollPane {

    static Log log = LogFactory.getLog( GeneSetTablePanel.class.getName() );
    private final static int COL0WIDTH = 80;
    private final static int COL1WIDTH = 350;
    private final static int COL2WIDTH = 80;
    private final static int COL3WIDTH = 80;
    private final static int COLRWIDTH = 80;

    JTable table;
    private OutputTableModel model;
    private TableSorter sorter;
    List results;
    private LinkedList resultToolTips = new LinkedList();
    GeneAnnotations geneData = null;
    GONames goData;
    Settings settings;
    private String classColToolTip;
    private StatusViewer messenger;

    /**
     * @param messenger The messenger to set.
     */
    public void setMessenger( StatusViewer messenger ) {
        this.messenger = messenger;
    }

    public GeneSetTablePanel( GeneSetScoreFrame callingframe, LinkedList results, Settings settings ) {
        this.callingframe = callingframe;
        this.results = results;
        this.settings = settings;
        model = new OutputTableModel( results );

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

        MouseListener popupListener = configurePopupMenu();
        table.addMouseListener( popupListener );

        RemoveRunPopupMenu removeRunPopup = new RemoveRunPopupMenu();
        JMenuItem removeRunMenuItem = new JMenuItem( "Remove this run..." );
        removeRunMenuItem.addActionListener( new OutputPanel_removeRunPopupMenu_actionAdapter( this ) );
        removeRunPopup.add( removeRunMenuItem );
        MouseListener removeRunPopupListener = new OutputPanel_removeRunPopupListener( removeRunPopup );
        table.getTableHeader().addMouseListener( removeRunPopupListener );
    }
    
    protected MouseListener configurePopupMenu() {
        OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
        JMenuItem modMenuItem = new JMenuItem( "View/Modify this gene set..." );
        modMenuItem.addActionListener( new OutputPanel_modMenuItem_actionAdapter( this ) );
        JMenuItem htmlMenuItem = new JMenuItem( "Go to GO web site" );
        htmlMenuItem.addActionListener( new OutputPanel_htmlMenuItem_actionAdapter( this ) );
        popup.add( htmlMenuItem );
        popup.add( modMenuItem );
        MouseListener popupListener = new OutputPanel_PopupListener( popup );
        return popupListener;
    }

    /**
     * @param e MouseEvent
     */
    void table_mouseReleased( MouseEvent e ) {
        int i = table.getSelectedRow();
        int j = table.getSelectedColumn();
        int _runnum = -1;
        String _id = ( String ) table.getValueAt( i, 0 );
        if ( table.getValueAt( i, j ) != null && j >= OutputTableModel.INIT_COLUMNS ) {
            _runnum = model.getRunNum( j );
        } else if ( table.getValueAt( i, j ) != null && j < OutputTableModel.INIT_COLUMNS ) {
            _runnum = -1;
        } else { // FIXME does this ever get triggered?
            log.debug( "Seeking column to show" );
            for ( int k = model.getColumnCount() - 1; k >= 0; k-- ) {
                if ( table.getValueAt( i, k ) != null && k >= OutputTableModel.INIT_COLUMNS ) {
                    String message;
                    String shownRunName = model.getColumnName( k );
                    shownRunName = shownRunName.substring( 0, shownRunName.length() - 5 );
                    if ( j >= OutputTableModel.INIT_COLUMNS ) {
                        message = model.getColumnName( j );
                        message = message.substring( 0, message.length() - 5 );
                        message = message + " doesn't include the class " + _id + ". Showing " + shownRunName
                                + " instead.";
                        JOptionPane.showMessageDialog( this, message, "FYI", JOptionPane.PLAIN_MESSAGE );
                    }
                    _runnum = model.getRunNum( k );
                    break;
                }
            }
        }
        final String id = _id;
        final int runnum = _runnum;
        log.debug( "Showing details for " + id );
        if ( messenger != null ) messenger.setStatus( "Viewing data for " + id + "..." );
        new Thread() {
            public void run() {
                showDetailsForGeneSet( runnum, id );
            }
        }.start();
    }

    void removeRunPopupMenu_actionPerformed( ActionEvent e ) {
        RemoveRunPopupMenu sourcePopup = ( RemoveRunPopupMenu ) ( ( Container ) e.getSource() ).getParent();
        int c = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
        String colname = model.getColumnName( c );
        TableColumn col = table.getColumn( colname );
        int yesno = JOptionPane.showConfirmDialog( null, "Are you sure you want to remove "
                + colname.substring( 0, colname.length() - 5 ) + "?", "Remove Run", JOptionPane.YES_NO_OPTION );
        if ( yesno == JOptionPane.YES_OPTION ) {
            System.err.println( "remove popup for col: " + c );
            table.removeColumn( col );
            model.removeRunData( c );
            model.fireTableStructureChanged();
            int runnum = model.getRunNum( c );
            results.remove( runnum );
            resultToolTips.remove( runnum );
            table.revalidate();
        }
    }

    String getHeaderToolTip( int index ) {
        if ( index == 0 ) {
            return this.classColToolTip;
        } else if ( index >= OutputTableModel.INIT_COLUMNS ) {
            // int runnum=(int)Math.floor((index - OutputTableModel.init_cols) / OutputTableModel.cols_per_run);
            int runnum = model.getRunNum( index );
            return ( String ) resultToolTips.get( runnum );
        }
        return null;
    }

    void generateToolTip( int runnum ) {
        Settings runSettings = ( ( GeneSetPvalRun ) results.get( runnum ) ).getSettings();
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
        resultToolTips.add( runnum, tooltip );
    }

    // called if 'cancel', 'find' or 'reset' have been hit.
    public void resetView() {
        this.geneData = callingframe.getOriginalGeneData();
        model.setInitialData( geneData, goData );
        setTableAttributes();
        table.revalidate();
    }

    // called when we first set up the table.
    public void addInitialData( GONames initialGoData ) {
        this.geneData = callingframe.getOriginalGeneData();
        this.goData = initialGoData;
        model.addInitialData( geneData, initialGoData );

        setTableAttributes();
        sorter.cancelSorting();
        sorter.setSortingStatus( 1, TableSorter.ASCENDING );
        table.revalidate();
    }

    /**
     * 
     */
    private void setTableAttributes() {
        sorter = new TableSorter( model );
        table.setModel( sorter );

        sorter.setTableHeader( table.getTableHeader() );
        this.getViewport().add( table, null );
        table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL0WIDTH );
        table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL1WIDTH );
        table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL2WIDTH );
        table.getColumnModel().getColumn( 3 ).setPreferredWidth( COL3WIDTH );
        table.setDefaultRenderer( Object.class, new OutputPanelTableCellRenderer( goData, results ) );
        classColToolTip = new String( "Total classes shown: " + geneData.selectedSets() );
        table.revalidate();
    }

    public void addRun() {
        model.addRun();
        int c = model.getColumnCount() - 1;
        TableColumn col = new TableColumn( c );
        col.setIdentifier( model.getColumnName( c ) );

        table.addColumn( col );
        table.getColumnModel().getColumn( c ).setPreferredWidth( COLRWIDTH );
        generateToolTip( model.getColumnCount() - OutputTableModel.INIT_COLUMNS - 1 );
        sorter.cancelSorting();
        sorter.setSortingStatus( c, TableSorter.ASCENDING );
        if ( results.size() > 3 ) table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        table.revalidate();
    }

    public void addedNewGeneSet() {
        sorter.cancelSorting();
        sorter.setSortingStatus( 0, TableSorter.ASCENDING );
        table.revalidate();
    }

    /**
     * @return classScore.data.GONames
     */
    public GONames getGoData() {
        return goData;
    }

}

// //////////////////////////////////////////////////////////////////////////////

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

class OutputPanel_PopupListener extends MouseAdapter {
    OutputPanelPopupMenu popup;

    OutputPanel_PopupListener( OutputPanelPopupMenu popupMenu ) {
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
            JTable source = ( JTable ) e.getSource();
            int r = source.rowAtPoint( e.getPoint() );
            List id = ( Vector ) source.getValueAt( r, 0 );
            if ( id != null ) {
                popup.show( e.getComponent(), e.getX(), e.getY() );
                popup.setPoint( e.getPoint() );
                int row = source.rowAtPoint( popup.getPoint() );
                popup.setSelectedItem( ( String ) ( ( Vector ) source.getValueAt( row, 0 ) ).get( 0 ) );
            }
        }
    }
}

class OutputPanel_removeRunPopupMenu_actionAdapter implements java.awt.event.ActionListener {
    GeneSetTablePanel adaptee;

    OutputPanel_removeRunPopupMenu_actionAdapter( GeneSetTablePanel adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.removeRunPopupMenu_actionPerformed( e );
    }
}

class RemoveRunPopupMenu extends JPopupMenu {
    Point popupPoint;

    public Point getPoint() {
        return popupPoint;
    }

    public void setPoint( Point point ) {
        popupPoint = point;
    }
}

class OutputPanel_removeRunPopupListener extends MouseAdapter {
    RemoveRunPopupMenu popup;

    OutputPanel_removeRunPopupListener( RemoveRunPopupMenu popupMenu ) {
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
            if ( c >= OutputTableModel.INIT_COLUMNS ) {
                popup.show( e.getComponent(), e.getX(), e.getY() );
                popup.setPoint( e.getPoint() );
            }
        }
    }
}

/**
 * 
 * 
 *
 */
class OutputTableModel extends AbstractTableModel {
    private GeneAnnotations geneData;
    private GONames goData;
    private LinkedList results;
    private LinkedList columnNames = new LinkedList();

    private int state = -1;
    public static final int INIT_COLUMNS = 4;

    public OutputTableModel( LinkedList results ) {
        this.results = results;
        columnNames.add( "Name" );
        columnNames.add( "Description" );
        columnNames.add( "# of Probes" );
        columnNames.add( "# of Genes" );
    }

    /**
     * @return int
     */
    public int getState() {
        return state;
    }

    /**
     * Does not reset the state.
     * 
     * @param geneData GeneAnnotations
     * @param goData GONames
     */
    public void setInitialData( GeneAnnotations origGeneData, GONames origGoData ) {
        this.geneData = origGeneData;
        this.goData = origGoData;
    }

    public void addInitialData( GeneAnnotations origGeneData, GONames origGoData ) {
        state = 0;
        this.setInitialData( origGeneData, origGoData );
    }

    public void addRunColumns( int state ) {
        columnNames.add( ( ( GeneSetPvalRun ) results.get( state - 1 ) ).getName() );
    }

    public void addRunData( Map result ) {
        state++;
        addRunColumns( state );
        results.add( result );
    }

    public void removeRunData( int c ) {
        state--;
        columnNames.remove( c );
        System.err.println( "number of cols: " + columnNames.size() );
    }

    public void addRun() {
        state++;
        columnNames.add( ( ( GeneSetPvalRun ) results.get( state - 1 ) ).getName() + " Pval" );
    }

    public String getColumnName( int i ) {
        return ( String ) columnNames.get( i );
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        if ( state == -1 ) {
            return 20;
        }
        return geneData.selectedSets();
    }

    public int getRunNum( int c ) {
        return c - INIT_COLUMNS;
    }

    public Object getValueAt( int i, int j ) {

        String classid = ( String ) geneData.getSelectedSets().get( i );

        if ( state >= 0 && j < INIT_COLUMNS ) {
            switch ( j ) {
                case 0: {
                    Vector cid_vec = new Vector();
                    cid_vec.add( classid );
                    if ( goData.newSet( classid ) ) cid_vec.add( "M" );
                    return cid_vec;
                }
                case 1:
                    return goData.getNameForId( classid );
                case 2:
                    return new Integer( geneData.numProbesInGeneSet( classid ) );
                case 3:
                    return new Integer( geneData.numGenesInGeneSet( classid ) );

            }
        } else if ( state > 0 ) {
            ArrayList vals = new ArrayList();
            int runnum = getRunNum( j );
            Map data = ( ( GeneSetPvalRun ) results.get( runnum ) ).getResults();
            if ( data.containsKey( classid ) ) {
                GeneSetResult res = ( GeneSetResult ) data.get( classid );
                vals.add( new Double( res.getRank() ) );
                vals.add( new Double( res.getPvalue() ) );
                return vals;
            }
            return null;
        }
        return null;
    }
}

/**
 *  
 */
class OutputPanelTableCellRenderer extends DefaultTableCellRenderer {
    GONames goData;
    List results;

    private Format nf = new Format( "%g" ); // for the gene set p value.
    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score

    public OutputPanelTableCellRenderer( GONames goData, List results ) {
        super();
        this.goData = goData;
        this.results = results;

        nff.setMaximumFractionDigits( 4 );
    }

    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column ) {
        super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

        // set cell text
        if ( value != null ) {
            if ( value.getClass().equals( ArrayList.class ) ) // pvalues and ranks.
                setText( nf.format( ( ( Double ) ( ( ArrayList ) value ).get( 1 ) ).doubleValue() ) );
            else if ( value.getClass().equals( Vector.class ) ) // class ids.
                setText( ( String ) ( ( Vector ) value ).get( 0 ) );
            else
                setText( value.toString() );
        } else {
            setText( null );
        }

        // set cell background
        int runcol = column - OutputTableModel.INIT_COLUMNS;
        setOpaque( true );
        if ( isSelected )
            setOpaque( true );
        else if ( value == null )
            setOpaque( false );
        else if ( column == 0 && goData.newSet( ( String ) ( ( Vector ) value ).get( 0 ) ) ) {
            setBackground( Colors.PINK );
        } else if ( value.getClass().equals( ArrayList.class ) ) {
            String classid = ( String ) ( ( Vector ) table.getValueAt( row, 0 ) ).get( 0 );
            GeneSetPvalRun result = ( GeneSetPvalRun ) results.get( runcol );
            Map data = result.getResults();
            if ( data.containsKey( classid ) ) {
                GeneSetResult res = ( GeneSetResult ) data.get( classid );
                if ( res.getPvalue_corr() < 0.001 ) {
                    setBackground( Colors.LIGHTBLUE1 );
                } else if ( res.getPvalue_corr() < 0.01 ) {
                    setBackground( Colors.LIGHTBLUE2 );
                } else if ( res.getPvalue_corr() < 0.05 ) {
                    setBackground( Colors.LIGHTBLUE3 );
                } else if ( res.getPvalue_corr() < 0.1 ) {
                    setBackground( Colors.LIGHTBLUE5 );
                } else {
                    setBackground( Color.WHITE );
                }
            }
        } else if ( hasFocus ) {
            setBackground( Color.WHITE );
            setOpaque( true );
        } else {
            setOpaque( false );
        }

        // set tool tips
        if ( value != null && value.getClass().equals( ArrayList.class ) ) {
            String classid = ( String ) ( ( Vector ) table.getValueAt( row, 0 ) ).get( 0 );
            // String classid = ( String ) table.getValueAt( row, 0 );
            GeneSetPvalRun result = ( GeneSetPvalRun ) results.get( runcol );
            Map data = result.getResults();
            if ( data.containsKey( classid ) ) {
                GeneSetResult res = ( GeneSetResult ) data.get( classid );
                setToolTipText( "<html>Rank: " + res.getRank() + "<br>Score: " + nff.format( res.getScore() )
                        + "<br>Genes: " + res.getEffectiveSize() + "<br>Probes: " + res.getSize() );
            }
        } else {
            setToolTipText( null );
        }

        return this;
    }
}
