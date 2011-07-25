/*
 * The ermineJ project
 * 
 * Copyright (c) 2006-2011 University of British Columbia
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

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StringUtil;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.EmptyGeneSetResult;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.geneset.GeneSetPanel;
import ubic.erminej.gui.util.Colors;
import corejava.Format;

/**
 * Model for displaying lists of gene sets.
 * 
 * @author pavlidis
 * @version $Id$
 * @see GeneSetDetailsTableModel for model used for displaying the genes in a single gene set.
 */
public class GeneSetTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 4190174776749215486L;

    private static Log log = LogFactory.getLog( GeneSetTableModel.class.getName() );

    @Override
    public Class<?> getColumnClass( int columnIndex ) {
        if ( columnIndex == 0 ) {
            return GeneSetTerm.class;
        } else if ( columnIndex == 1 ) {
            return String.class;
        } else if ( columnIndex == 2 ) {
            return Integer.class;
        } else if ( columnIndex == 3 ) {
            return Integer.class;
        } else if ( columnIndex == 4 ) {
            return Double.class;
        } else {
            return GeneSetResult.class;
        }
    }

    /**
     * The number of columns that are always there, before runs are listed.
     */
    public static final int INIT_COLUMNS = 5;

    private GeneAnnotations geneData;
    private List<GeneSetPvalRun> results = new ArrayList<GeneSetPvalRun>();

    private List<String> columnIdentifiers = new Vector<String>();
    private List<GeneSetTerm> gsl;

    @Override
    public void fireTableDataChanged() {
        gsl = new ArrayList<GeneSetTerm>( geneData.getAllTerms() );
        super.fireTableDataChanged();
    }

    private boolean filterRedundant = true;

    private boolean filterEmpty = true;

    private boolean filterEmptyResults = true;

    public GeneSetTableModel( GeneAnnotations geneData, List<GeneSetPvalRun> results ) {
        super();
        this.results = results;
        this.geneData = geneData;
        addColumn( "Name" );
        addColumn( "Description" );
        addColumn( "Probes" );
        addColumn( "Genes" );
        addColumn( "Multifunc" );
        assert INIT_COLUMNS == this.getColumnCount();
        gsl = new ArrayList<GeneSetTerm>( geneData.getAllTerms() );
        filter();
    }

    public void addRun() {
        addColumn( results.get( results.size() - 1 ).getName() + " Pval" );
    }

    public void setFilterEmpty( boolean b ) {
        this.filterEmpty = b;
        filter();
    }

    public void setFilterRedundant( boolean b ) {
        this.filterRedundant = b;
        filter();
    }

    public void setFilterEmptyResults( boolean b ) {
        this.filterEmptyResults = b;
        filter();
    }

    public void filter() {

        // reset.
        gsl = new ArrayList<GeneSetTerm>( geneData.getAllTerms() );
        int beforeCount = gsl.size();

        if ( filterRedundant || filterEmpty || filterEmptyResults ) {

            for ( Iterator<GeneSetTerm> it = gsl.iterator(); it.hasNext(); ) {
                GeneSetTerm t = it.next();
                if ( filterRedundant && geneData.skipDueToRedundancy( t ) ) {
                    it.remove();
                } else if ( filterEmpty && geneData.getGeneSetGenes( t ).isEmpty() ) {
                    it.remove();
                } else if ( filterEmptyResults && !this.results.isEmpty() ) {
                    boolean keep = false;
                    for ( GeneSetPvalRun r : results ) {
                        if ( r.getResults().containsKey( t ) ) {
                            keep = true;
                            break;
                        }
                    }
                    if ( !keep ) it.remove();
                }
            }
            super.fireTableStructureChanged();
        }

        log.info( gsl.size() + " gene sets (out of " + beforeCount + ")" );
    }

    @Override
    public int getColumnCount() {
        return columnIdentifiers.size();
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

    @Override
    public String getColumnName( int column ) {
        return columnIdentifiers.get( column );
    }

    @Override
    public int getRowCount() {
        if ( gsl == null ) return this.geneData.numGeneSets();
        return gsl.size();
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt( int rowIndex, int colIndex ) {

        GeneSetTerm classid = gsl.get( rowIndex );

        if ( colIndex < INIT_COLUMNS ) {
            switch ( colIndex ) {
                case 0:
                    return classid;
                case 1:
                    return classid.getName();
                case 2:
                    return new Integer( geneData.numProbesInGeneSet( classid ) );
                case 3:
                    return new Integer( geneData.numGenesInGeneSet( classid ) );
                case 4:
                    return geneData.getMultifunctionality().getGOTermMultifunctionality( classid );

            }
        } else { // results
            int runIndex = getRunIndex( colIndex );
            assert runIndex >= 0;
            Map<GeneSetTerm, GeneSetResult> data = results.get( runIndex ).getResults();

            if ( !data.containsKey( classid ) ) {
                return new EmptyGeneSetResult( classid );
            }

            return data.get( classid );
        }
        return null;
    }

    public void removeRunData( int runIndex ) {
        columnIdentifiers.remove( runIndex ); // CHECK
        log.debug( "number of cols: " + columnIdentifiers.size() );
    }

    /**
     * @param runIndex
     * @param newName
     */
    public void renameRun( int runIndex, String newName ) {
        log.debug( "Renaming run " + runIndex + " to " + newName );
        this.renameColumn( this.getColumnIndexForRun( runIndex ), newName );
    }

    private void addColumn( String string ) {
        columnIdentifiers.add( string );
    }

    private void renameColumn( int columnNum, String newName ) {
        if ( newName == null || newName.length() == 0 ) return;
        if ( columnNum < 0 || columnNum > this.getColumnCount() - 1 )
            throw new IllegalArgumentException( "Invalid column " + columnNum );
        log.debug( "Renaming column " + columnNum + " to " + newName );
        columnIdentifiers.set( columnNum, newName );
    }
}

/**
 *  
 */
class GeneSetTableCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -1L;

    private Format nf = new Format( "%.4g" ); // for the gene set p value.
    private DecimalFormat nff = new DecimalFormat(); // for the tool tip score
    private GeneAnnotations geneData;

    public GeneSetTableCellRenderer( GeneAnnotations goData ) {
        super();
        this.geneData = goData;

        nff.setMaximumFractionDigits( 4 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     * java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column ) {
        super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

        // set cell text

        if ( value == null ) {
            setText( "" );
        } else if ( column == 0 ) {
            setText( ( ( GeneSetTerm ) value ).getId() );
        } else if ( column == 1 ) {
            setText( ( String ) value );
        } else if ( value instanceof EmptyGeneSetResult ) {
            setText( "[not run]" );
        } else if ( value instanceof GeneSetResult ) {
            setText( nf.format( ( ( GeneSetResult ) value ).getPvalue() ) );
        } else if ( value instanceof Double ) {
            setText( String.format( "%.2f", ( Double ) value ) );
        } else {
            setText( value.toString() ); // integers, whatever.
        }

        setCellBackgroundColor( value, isSelected, hasFocus, column );

        // set tool tips etc.
        if ( value != null ) {

            Object o = table.getValueAt( row, 0 );

            if ( !( o instanceof GeneSetTerm ) ) {
                System.err.println( "Got: " + o );
                return this;
            }

            GeneSetTerm term = ( GeneSetTerm ) o;

            boolean redundant = geneData.skipDueToRedundancy( term );

            if ( redundant ) {
                setForeground( Color.GRAY );
            } else {
                setForeground( Color.BLACK );
            }

            if ( column >= GeneSetTableModel.INIT_COLUMNS ) {
                GeneSetResult res = ( GeneSetResult ) value;
                setToolTipText( "<html>Rank: " + res.getRank() + "<br>Score: " + nff.format( res.getScore() )
                        + "<br>Corrected p: " + nf.format( res.getCorrectedPvalue() ) + "<br>Genes used: "
                        + res.getEffectiveSize() + "<br>Probes used: " + res.getSize() );
            } else {

                String aspect = term.getAspect();
                String definition = term.getDefinition();

                String redund = this.getToolTipTextForRedundancy( term );
                setToolTipText( "<html>"
                        + term.getName()
                        + " ("
                        + term.getId()
                        + ")<br/>"
                        + "Aspect: "
                        + aspect
                        + "<br/>"
                        + redund
                        + StringUtil
                                .wrap( definition.substring( 0, Math.min( definition.length(), 200 ) ), 50, "<br/>" )
                        + ( definition.length() > GeneSetPanel.MAX_DEFINITION_LENGTH ? "..." : "" ) );

            }

        } else {
            setToolTipText( null );
        }

        return this;
    }

    private void chooseMultifunctionalityIndicatorColor( double auc ) {
        if ( auc >= 0.99 ) {
            setBackground( Colors.LIGHTRED1 );
        } else if ( auc >= 0.95 ) {
            setBackground( Colors.LIGHTRED2 );
        } else if ( auc >= 0.9 ) {
            setBackground( Colors.LIGHTRED3 );
        } else if ( auc >= 0.85 ) {
            setBackground( Colors.LIGHTRED4 );
        } else if ( auc >= 0.8 ) {
            setBackground( Colors.LIGHTRED5 );
        } else {
            setBackground( Color.WHITE );
        }

    }

    private void setCellBackgroundColor( Object value, boolean isSelected, boolean hasFocus, int column ) {
        setBackground( Color.WHITE );
        // set cell background
        setOpaque( true );
        if ( isSelected || hasFocus ) {
            setOpaque( true );
            setBackground( Color.LIGHT_GRAY );
        }

        if ( column == GeneSetTablePanel.MULTIFUNC_COLUMN_INDEX ) {
            chooseMultifunctionalityIndicatorColor( ( Double ) value );
        } else if ( column == 0 && ( ( GeneSetTerm ) value ).isUserDefined() ) {
            setBackground( Colors.LIGHTYELLOW );
        } else if ( value instanceof GeneSetResult ) {
            double pvalCorr = ( ( GeneSetResult ) value ).getCorrectedPvalue();
            Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );
            setBackground( bgColor );
        }
    }

    protected String getToolTipTextForRedundancy( GeneSetTerm id ) {
        boolean redundant = geneData.skipDueToRedundancy( id );

        String redund = "";
        if ( redundant ) {
            redund = "<strong>Redundant</strong> with:<br/>";
            Collection<GeneSet> redundantGroups = geneData.getGeneSet( id ).getRedundantGroups();
            for ( GeneSet geneSet : redundantGroups ) {
                redund += geneSet + "<br/>";
            }
            redund += "<br/>";
        }
        return redund;
    }

}
