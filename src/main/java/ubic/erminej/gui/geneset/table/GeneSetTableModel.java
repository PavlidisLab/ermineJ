/*
 * The ermineJ project/
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import corejava.Format;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.EmptyGeneSetResult;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.geneset.GeneSetPanel;
import ubic.erminej.gui.geneset.details.GeneSetDetailsTableModel;
import ubic.erminej.gui.util.Colors;

/**
 * Model for displaying list of gene sets and results in the main frame.
 *
 * @author pavlidis
 * @see GeneSetDetailsTableModel for model used for displaying the genes in a single gene set.
 */
public class GeneSetTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 4190174776749215486L;

    protected static Log log = LogFactory.getLog( GeneSetTableModel.class.getName() );

    /**
     * The number of columns that are always there, before runs are listed.
     */
    public static final int INIT_COLUMNS = 4;

    private GeneAnnotations geneData;

    private List<GeneSetPvalRun> results;
    private List<String> columnIdentifiers = new Vector<>();

    private List<GeneSetTerm> gsl;
    private boolean filterEmpty = true;

    private boolean filterInsignificant = false;

    private boolean filterNonUsers;

    public GeneSetTableModel( GeneAnnotations geneData, List<GeneSetPvalRun> results ) {
        super();
        this.results = results;
        this.geneData = geneData;
        addColumn( "Name" );
        addColumn( "Description" );
        addColumn( "Size" );
        addColumn( "Multifunc" );
        assert INIT_COLUMNS == this.getColumnCount();
        gsl = new ArrayList<>( geneData.getAllTerms() );
        filter();
    }

    public void addRun() {
        addColumn( results.get( results.size() - 1 ).getName() );
    }

    /**
     * Changes state with user's settings such as "hide non-significant".
     */
    public void filter() {

        // reset.
        gsl = new ArrayList<>( geneData.getAllTerms() );
        int beforeCount = gsl.size();

        if ( filterEmpty || filterInsignificant || filterNonUsers ) {

            for ( Iterator<GeneSetTerm> it = gsl.iterator(); it.hasNext(); ) {
                GeneSetTerm t = it.next();

                // order matters.
                if ( t.isAspect() ) {
                    it.remove(); // never show aspect.
                } else if ( filterEmpty && geneData.getGeneSetGenes( t ).isEmpty() ) {
                    it.remove();
                } else if ( filterNonUsers && !t.isUserDefined() ) {
                    it.remove();
                } else if ( filterInsignificant && !this.results.isEmpty() ) {
                    // keep if there is at least one significant result.
                    boolean keep = false;
                    for ( GeneSetPvalRun r : results ) {
                        GeneSetResult res = r.getResults().get( t );
                        if ( res != null && res.getCorrectedPvalue() < GeneSetPanel.FDR_THRESHOLD_FOR_FILTER ) {
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
    public void fireTableDataChanged() {
        gsl = new ArrayList<>( geneData.getAllTerms() );
        super.fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass( int columnIndex ) {
        if ( columnIndex == 0 ) {
            return GeneSetTerm.class;
        } else if ( columnIndex == 1 ) { // name
            return String.class;
        } else if ( columnIndex == 2 ) { // description
            return Integer.class;
        } else if ( columnIndex == 3 ) { // size
            return Double.class;
        } else if ( columnIndex == 3 ) { // mf
            return Double.class;
        } else {
            return GeneSetResult.class;
        }
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
        // double minPvalue = 1e-50;
        // double maxLoggedPvalue = -Math.log10( minPvalue );

        if ( colIndex < INIT_COLUMNS ) {
            switch ( colIndex ) {
                case 0: // group
                    return classid;
                case 1: // description
                    return classid.getName();
                case 2: // size
                    int elements = geneData.numElementsInGeneSet( classid );
                    int genes = geneData.numGenesInGeneSet( classid );
                    return new GeneSetSize( genes, elements );
                case 3: // mf
                    return geneData.getMultifunctionality().getGOTermMultifunctionalityRank( classid );
                default:
                    return null;
            }
        }

        // results
        int runIndex = getRunIndex( colIndex );
        assert runIndex >= 0;
        Map<GeneSetTerm, GeneSetResult> data = results.get( runIndex ).getResults();

        if ( !data.containsKey( classid ) ) {
            return new EmptyGeneSetResult( classid );
        }

        return data.get( classid );

    }

    public void removeRunData( int runIndex ) {
        columnIdentifiers.remove( runIndex );
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

    public void setFilterEmpty( boolean b ) {
        this.filterEmpty = b;
    }

    public void setFilterEmptyResults( boolean b ) {
        this.filterInsignificant = b;
    }

    public void setFilterNonUsers( boolean filterNonUsers ) {
        this.filterNonUsers = filterNonUsers;
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
 * Helper for sorting and display of gene set sizes.
 *
 * @author Paul
 * @version $Id$
 */
class GeneSetSize implements Comparable<GeneSetSize> {

    private Integer numGenes = 0;

    private Integer numElements = 0;

    public GeneSetSize( Integer numGenes, Integer numElements ) {
        super();
        this.numGenes = numGenes;
        this.numElements = numElements;
    }

    @Override
    public int compareTo( GeneSetSize o ) {
        return this.numGenes.compareTo( o.numGenes );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        GeneSetSize other = ( GeneSetSize ) obj;
        if ( numGenes == null ) {
            if ( other.numGenes != null ) return false;
        } else if ( !numGenes.equals( other.numGenes ) ) return false;
        if ( numElements == null ) {
            if ( other.numElements != null ) return false;
        } else if ( !numElements.equals( other.numElements ) ) return false;
        return true;
    }

    public Integer getNumGenes() {
        return numGenes;
    }

    public Integer getNumProbes() {
        return numElements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( numGenes == null ) ? 0 : numGenes.hashCode() );
        result = prime * result + ( ( numElements == null ) ? 0 : numElements.hashCode() );
        return result;
    }

    public void setNumElements( Integer numElements ) {
        this.numElements = numElements;
    }

    public void setNumGenes( Integer numGenes ) {
        this.numGenes = numGenes;
    }

    @Override
    public String toString() {
        return "<html>"
                + numGenes
                + ""
                + ( numElements.equals( numGenes ) ? "" : "&nbsp;<font color=\"#777777\">[ " + numElements
                        + " ]</font>" )
                + "</html>";
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

    private final String mfIndicatorChar = "&#9830;"; // diamond

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
        } else if ( column == 0 ) { /* ID */

            if ( value instanceof GeneSetTerm ) {
                boolean redundant = geneData.hasRedundancy( ( GeneSetTerm ) value );
                if ( redundant ) {
                    setText( "<html>" + ( ( GeneSetTerm ) value ).getId() + "&nbsp;&nbsp;&bull;</html>" );
                } else {
                    setText( ( ( GeneSetTerm ) value ).getId() );
                }
            }
        } else if ( column == 1 && value instanceof String ) { /* description */
            setText( ( String ) value );
        } else if ( value instanceof EmptyGeneSetResult ) {
            setText( "not run" );
            setForeground( Color.LIGHT_GRAY );
        } else if ( value instanceof GeneSetResult ) {
            GeneSetResult result = ( GeneSetResult ) value;

            String mfstring = "";
            int size = 4;
            String textColor = "#000000";
            if ( result.getMultifunctionalityCorrectedRankDelta() != null ) {
                String col = Integer.toHexString( Colors.chooseColorForMultifunctionalityEffect( result ).getRGB() )
                        .substring( 2, 8 );
                mfstring = "<font size=" + size + " + color=#" + col + ">" + mfIndicatorChar + "</font>";

                // make it more obvious when shifts are large - show symbol twice (could make it bigger, etc.)
                // if ( result.getMultifunctionalityCorrectedRankDelta() > 200 ) {
                // mfstring += mfstring;
                // }
                if ( result.getMfCorrectedFdr() > 0.1 ) {
                    textColor = "#777777";
                }
            }
            setText( String.format( "<html><font color=\"" + textColor + "\">%.3g</font>&nbsp;" + mfstring + "</html>",
                    result.getPvalue() ) );
        } else if ( column == 3 && ( Double ) value < 0 ) { // MF score
            setText( "" ); // didn't compute it.
        } else if ( value instanceof Double ) {
            if ( ( ( Double ) value ).isNaN() ) {
                setText( "" );
            } else {
                setText( String.format( "%.2f", ( Double ) value ) );
            }
        } else if ( column == 2 ) { // gene count
            setText( value.toString() );
        } else {
            setText( value.toString() ); // integers, whatever.
        }

        setCellBackgroundColor( value, column );

        if ( isSelected || hasFocus ) {
            // blend colours
            float[] col1comps = new float[3];
            col1comps = table.getSelectionBackground().getColorComponents( col1comps );
            float[] col2comps = new float[3];
            col2comps = getBackground().getColorComponents( col2comps );

            float r = 0.2f;
            setBackground( new Color( col1comps[0] * r + col2comps[0] * ( 1.0f - r ), col1comps[1] * r + col2comps[1]
                    * ( 1.0f - r ), col1comps[2] * r + col2comps[2] * ( 1.0f - r ) ) );
        }

        // set tool tips etc.
        if ( value != null ) {

            Object o = table.getValueAt( row, 0 );

            if ( !( o instanceof GeneSetTerm ) ) {
                System.err.println( "Got: " + o );
                return this;
            }

            GeneSetTerm term = ( GeneSetTerm ) o;

            if ( geneData.numGenesInGeneSet( term ) == 0 || value instanceof EmptyGeneSetResult ) {
                setForeground( Color.GRAY );
            } else {
                setForeground( Color.BLACK );
            }

            configureToolTip( column, value, term );

        } else {
            setToolTipText( null );
        }

        return this;
    }

    protected String getToolTipTextForRedundancy( GeneSetTerm id ) {

        if ( id.isAspect() || id.getId().equals( "all" ) || geneData.getGeneSet( id ) == null ) return "";

        Collection<GeneSet> redundantGroups = geneData.getGeneSet( id ).getRedundantGroups();

        String redund = "";
        if ( !redundantGroups.isEmpty() ) {
            redund = "<strong>Redundant</strong> with:<br/>";

            for ( GeneSet geneSet : redundantGroups ) {
                redund += geneSet + "<br/>";
            }
            redund += "<br/>";
        }
        return redund;
    }

    /**
     * @param auc; because so many values are high, this is quite steep.
     */
    private void chooseMultifunctionalityIndicatorColor( double auc ) {
        if ( auc >= 0.999 ) {
            setBackground( Colors.LIGHTRED1 );
        } else if ( auc >= 0.99 ) {
            setBackground( Colors.LIGHTRED2 );
        } else if ( auc >= 0.95 ) {
            setBackground( Colors.LIGHTRED3 );
        } else if ( auc >= 0.9 ) {
            setBackground( Colors.LIGHTRED4 );
        } else if ( auc >= 0.85 ) {
            setBackground( Colors.LIGHTRED5 );
        } else {
            setBackground( Color.WHITE );
        }

    }

    /**
     * @param column
     * @param value
     * @param term
     */
    private void configureToolTip( int column, Object value, GeneSetTerm term ) {
        if ( column >= GeneSetTableModel.INIT_COLUMNS ) {
            GeneSetResult res = ( GeneSetResult ) value;

            if ( res instanceof EmptyGeneSetResult ) {
                setToolTipText( "[No result]" );
                return;
            }

            String mfString = "";
            if ( res.getMultifunctionalityCorrectedRankDelta() != null ) {
                mfString = " [MF corrected: " + ( res.getRank() + res.getMultifunctionalityCorrectedRankDelta() ) + "]";
            }

            setToolTipText( "<html>Rank: "
                    + res.getRank()
                    + mfString
                    + "<br>Score: "
                    + nff.format( res.getScore() )
                    + "<br>Corrected p: "
                    + nf.format( res.getCorrectedPvalue() )
                    +

                    ( ( res.getMultifunctionalityCorrectedRankDelta() != null ) ? "<br>p after MFcorr: "
                            + nf.format( res.getMfCorrectedPvalue() ) : "" )

                    + "<br>Genes used: " + res.getNumGenes() + "<br>Elements used: " + res.getNumProbes() );
        } else {

            /*
             * Tool tip for the gene set
             */
            String aspect = term.getAspect();
            String definition = term.getDefinition();

            double mfRank = geneData.getMultifunctionality().getGOTermMultifunctionalityRank( term );
            double mfScore = geneData.getMultifunctionality().getGOTermMultifunctionality( term );
            double mfPvalue = geneData.getMultifunctionality().getGOTermMultifunctionalityPvalue( term );

            String redund = this.getToolTipTextForRedundancy( term );
            setToolTipText( "<html>"
                    + term.getName()
                    + " ("
                    + term.getId()
                    + ")<br/>"
                    + "Aspect: "
                    + aspect
                    + "<br/>"
                    + "Multifunctionality rank= "
                    + String.format( "%.2f (AUC=%.2f, p=%.3g)", mfRank, mfScore, mfPvalue )
                    + "<br/>"
                    + redund
                    + WordUtils.wrap( StringUtils.abbreviate( definition, GeneSetPanel.MAX_DEFINITION_LENGTH ), 50,
                            "<br/>", true ) );
        }
    }

    private void setCellBackgroundColor( Object value, int column ) {

        setBackground( Color.WHITE );
        // set cell background
        setOpaque( true );

        if ( column == GeneSetTablePanel.MULTIFUNC_COLUMN_INDEX ) {
            chooseMultifunctionalityIndicatorColor( ( Double ) value );
        } else if ( value instanceof EmptyGeneSetResult || value instanceof String ) {
            // nothing to do. This is weird that this happens, only when we are adding result sets; race condition
        } else if ( column == 0 && value instanceof GeneSetTerm && ( ( GeneSetTerm ) value ).isUserDefined() ) {
            setBackground( GeneSetPanel.USER_NODE_COLOR );
        } else if ( value instanceof GeneSetResult ) {
            double pvalCorr = ( ( GeneSetResult ) value ).getCorrectedPvalue();
            Color bgColor = Colors.chooseBackgroundColorForPvalue( pvalCorr );
            setBackground( bgColor );

            // Color mfColor = Colors.chooseColorForMultifunctionalityEffect( ( GeneSetResult ) value );
            // this.setBorder( BorderFactory.createLineBorder( mfColor, 1 ) );
        }
    }

}