package classScore.gui;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.util.StringUtil;
import classScore.GeneSetPvalRun;
import classScore.data.GeneSetResult;
import corejava.Format;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTableModel extends AbstractTableModel {

    private static Log log = LogFactory.getLog( GeneSetTableModel.class.getName() );

    /**
     * The number of columns that are always there, before runs are listed.
     */
    public static final int INIT_COLUMNS = 4;

    private List columnNames = new LinkedList();
    private GeneAnnotations geneData;
    private GONames goData;
    private List results;

    public GeneSetTableModel( List results ) {
        super();
        this.results = results;
        columnNames.add( "Name" );
        columnNames.add( "Description" );
        columnNames.add( "Probes" );
        columnNames.add( "Genes" );
    }

    /**
     * @param runIndex
     * @param newName
     */
    public void renameRun( int runIndex, String newName ) {
        log.debug( "Renaming run " + runIndex + " to " + newName );
        this.renameColumn( this.getColumnIndexForRun( runIndex ), newName );
    }

    public void addInitialData( GeneAnnotations origGeneData, GONames origGoData ) {
        this.setInitialData( origGeneData, origGoData );
    }

    public void addRun() {
        columnNames.add( ( ( GeneSetPvalRun ) results.get( results.size() - 1 ) ).getName() + " Pval" );
    }

    public void renameColumn( int columnNum, String newName ) {
        if ( newName == null || newName.length() == 0 ) return;
        if ( columnNum < 0 || columnNum > this.getColumnCount() - 1 )
            throw new IllegalArgumentException( "Invalid column " + columnNum );
        log.debug( "Renaming column " + columnNum + " to " + newName );
        columnNames.set( columnNum, newName );
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public String getColumnName( int columnNumber ) {
        if ( columnNumber > this.getColumnCount() - 1 || columnNumber < 0 ) return null;
        return ( String ) columnNames.get( columnNumber );
    }

    public int getRowCount() {
        if ( geneData == null ) {
            return 0;
        }
        return geneData.selectedSets();
    }

    /**
     * @param columnNumber
     * @return the index of the run in that column. -1 if the column does not contain a run.
     */
    public int getRunIndex( int columnNumber ) {
        if ( columnNumber < INIT_COLUMNS - 1 ) return -1;
        if ( columnNumber > this.getColumnCount() - 1 ) return -1;
        return columnNumber - INIT_COLUMNS;
    }

    /**
     * @param runIndex index (from 0) of the run.
     * @return the index of the column containing the run. -1 if there is no such run.
     */
    public int getColumnIndexForRun( int runIndex ) {
        if ( runIndex < 0 ) return -1;
        if ( INIT_COLUMNS + 1 + runIndex > this.getColumnCount() ) return -1;
        return INIT_COLUMNS + runIndex;
    }

    public Object getValueAt( int i, int j ) {

        String classid = ( String ) geneData.getSelectedSets().get( i );

        if ( j < INIT_COLUMNS ) {
            switch ( j ) {
                case 0: {
                    List cid_vec = new Vector();
                    cid_vec.add( classid );
                    if ( goData.isUserDefined( classid ) ) cid_vec.add( "M" );
                    return cid_vec;
                }
                case 1:
                    return goData.getNameForId( classid );
                case 2:
                    return new Integer( geneData.numProbesInGeneSet( classid ) );
                case 3:
                    return new Integer( geneData.numGenesInGeneSet( classid ) );

            }
        } else {
            List vals = new ArrayList();
            int runIndex = getRunIndex( j );
            assert runIndex >= 0;
            Map data = ( ( GeneSetPvalRun ) results.get( runIndex ) ).getResults();
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

    public void removeRunData( int runIndex ) {
        columnNames.remove( runIndex );
        log.debug( "number of cols: " + columnNames.size() );
    }

    /**
     * @param geneData GeneAnnotations
     * @param goData GONames
     */
    public void setInitialData( GeneAnnotations origGeneData, GONames origGoData ) {
        this.geneData = origGeneData;
        this.goData = origGoData;
    }
}

/**
 *  
 */
class OutputPanelTableCellRenderer extends DefaultTableCellRenderer {
    private static Log log = LogFactory.getLog( OutputPanelTableCellRenderer.class.getName() );
    private Format nf = new Format( "%.4g" ); // for the gene set p value.
    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score

    private GONames goData;
    private List results;

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
        int runcol = column - GeneSetTableModel.INIT_COLUMNS;
        setOpaque( true );
        if ( isSelected )
            setOpaque( true );
        else if ( value == null )
            setOpaque( false );
        else if ( column == 0 && goData.isUserDefined( ( String ) ( ( Vector ) value ).get( 0 ) ) ) {
            setBackground( Colors.LIGHTYELLOW );
        } else if ( value.getClass().equals( ArrayList.class ) ) {
            String classid = ( String ) ( ( Vector ) table.getValueAt( row, 0 ) ).get( 0 );
            GeneSetPvalRun result = ( GeneSetPvalRun ) results.get( runcol );
            Map data = result.getResults();
            if ( data.containsKey( classid ) ) {
                GeneSetResult res = ( GeneSetResult ) data.get( classid );
                double pvalCorr = res.getCorrectedPvalue();
                Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );
                setBackground( bgColor );
            }
        } else if ( hasFocus ) {
            setBackground( Color.WHITE );
            setOpaque( true );
        } else {
            setOpaque( false );
        }

        // set tool tips
        if ( value != null ) {
            String classid = ( String ) ( ( Vector ) table.getValueAt( row, 0 ) ).get( 0 );
            // String classid = ( String ) table.getValueAt( row, 0 );

            if ( column >= GeneSetTableModel.INIT_COLUMNS ) {
                GeneSetPvalRun result = ( GeneSetPvalRun ) results.get( runcol );
                Map data = result.getResults();
                if ( data.containsKey( classid ) ) {
                    GeneSetResult res = ( GeneSetResult ) data.get( classid );
                    setToolTipText( "<html>Rank: " + res.getRank() + "<br>Score: " + nff.format( res.getScore() )
                            + "<br>Corrected p: " + nf.format( res.getCorrectedPvalue() ) + "<br>Genes used: "
                            + res.getEffectiveSize() + "<br>Probes used: " + res.getSize() );
                }
            } else if ( column == 1 || column == 0 ) {
                String aspect = goData.getAspectForId( classid );
                String definition = goData.getDefinitionForId( classid );
                setToolTipText( "<html>Aspect: " + aspect + "<br>Definition: "
                        + StringUtil.wrap( definition.substring( 0, Math.min( definition.length(), 200 ) ), 50, "<br>" ) );
            }

        } else {
            setToolTipText( null );
        }

        return this;
    }
}
