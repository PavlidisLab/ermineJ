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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.list.DoubleArrayList;

import ubic.basecode.graphics.MatrixDisplay;
import ubic.basecode.math.Rank;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.Probe;
import ubic.erminej.gui.table.MatrixPoint;
import ubic.erminej.gui.util.JLinkLabel;

/**
 * Our table model for one gene set.
 * 
 * @author Will Braynen
 * @version $Id$
 * @see GeneSetDetailsFrame for the renderer
 */
public class GeneSetDetailsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -1L;
    private static final String URL_REPLACE_TAG = "@@";
    private MatrixDisplay<Probe, String> matrixDisplay;
    private List<Probe> probeIDs;
    private Map<Probe, Double> probeScores;
    private Map<Probe, Integer> scoreRanks;
    private GeneAnnotations geneData;
    private Settings settings;
    private Map<Gene, JLinkLabel> linkLabels;
    private String[] tableColumnNames = { "Probe", "Score", "Vis. score", "Symbol", "Name", "Multifunc" };
    public static final String DEFAULT_GENE_URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=search&term="
            + URL_REPLACE_TAG;

    private String urlbase = DEFAULT_GENE_URL_BASE;

    protected static final Log log = LogFactory.getLog( GeneSetDetailsTableModel.class );

    private static final String RESOURCE_LOCATION = "/ubic/erminej/";

    private final Icon gemmaIcon = new ImageIcon( this.getClass().getResource( RESOURCE_LOCATION + "/gemmaTiny.gif" ) );

    /**
     * @param matrixDisplay
     * @param probeIDs
     * @param pvalues
     * @param geneData
     * @param nf
     */
    public GeneSetDetailsTableModel( MatrixDisplay<Probe, String> matrixDisplay, GeneSetDetails geneSetDetails,
            Settings settings ) {

        this.matrixDisplay = matrixDisplay;
        this.probeIDs = new ArrayList<Probe>( geneSetDetails.getProbes() );

        probeScores = geneSetDetails.getProbeScores();
        this.settings = settings;

        if ( probeScores != null && !probeScores.isEmpty() ) {
            scoreRanks = Rank.rankTransform( probeScores, settings.getBigIsBetter() );
        }
        this.geneData = geneSetDetails.getGeneData();

        // all ranks.

        Double[] geneScores = geneSetDetails.getSourceGeneScores().getGeneScores();
        DoubleArrayList allScoreRanks = Rank.rankTransform(
                new DoubleArrayList( ArrayUtils.toPrimitive( geneScores ) ), settings.getBigIsBetter() );
        for ( int i = 0; i < allScoreRanks.size(); i++ ) {
            double rank = allScoreRanks.get( i );
            double score = geneScores[i];

            // FIXME compute permiles (probably overkill...)

            // also has to be done for multifunctionality.

        }

        configure();
    }

    @Override
    public Class<?> getColumnClass( int columnIndex ) {

        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;

        if ( columnIndex < offset ) {
            return Double.class; // matrix, or pvals.
        } else if ( columnIndex - offset == 0 ) {
            return Probe.class; // probe
        } else if ( columnIndex - offset == 1 ) {
            return Double.class; // score
        } else if ( columnIndex - offset == 2 ) {
            return Object.class; // actually a List, which is not comparable.
        } else if ( columnIndex - offset == 3 ) {
            return JLinkLabel.class; // symbol
        } else if ( columnIndex - offset == 4 ) {
            return String.class; // description
        }

        return String.class; // mf.
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        int matrixColumnCount = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;
        return tableColumnNames.length + matrixColumnCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName( int column ) {

        // matrix display ends
        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;

        if ( column < offset ) {
            return matrixDisplay.getColumnName( column ).toString();
        }
        return tableColumnNames[column - offset];

    } // end getColumnName

    public Probe getProbeAtRow( int r ) {
        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;
        return ( Probe ) getValueAt( r, offset );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return probeIDs.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int row, int column ) {

        // matrix display ends
        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;

        // get the probeID for the current row
        Probe probeID = probeIDs.get( row );

        // If this is part of the matrix display
        if ( matrixDisplay != null && column < offset ) {
            return new MatrixPoint( matrixDisplay.getRowIndexByName( probeID ), column, matrixDisplay.getValue(
                    matrixDisplay.getRowIndexByName( probeID ), column ) ); // coords into JMatrixDisplay
        }
        column -= offset;
        Gene gene_name = probeID.getGene();
        switch ( column ) { // after it's been offset
            case 0:
                // probe ID
                return probeID;
            case 1:
                // scores
                if ( probeScores == null || !probeScores.containsKey( probeID ) ) return new Double( Double.NaN );
                return probeScores.get( probeID );
            case 2:
                // p value bar - only displayed if we have pvalues.
                List<Double> values = new ArrayList<Double>();
                if ( !settings.getDoLog() || probeScores == null ) { // kludgy way to figure out if we have pvalues.
                    values.add( 0, new Double( Double.NaN ) );
                    values.add( 1, new Double( Double.NaN ) );
                } else {

                    if ( probeScores == null || !probeScores.containsKey( probeID ) ) {
                        values.add( 0, new Double( 1.0 ) );
                        values.add( 1, new Double( 1.0 ) );
                    } else {
                        // actual p value
                        Double actualValue = probeScores.get( probeID );
                        values.add( 0, actualValue );
                        // expected p value, but only if we had an actual pvalue
                        if ( !Double.isNaN( actualValue.doubleValue() ) && scoreRanks != null
                                && scoreRanks.containsKey( probeID ) ) {
                            int position = scoreRanks.get( probeID );
                            Double expectedValue = new Double( 1.0f / getRowCount() * ( position + 1 ) );
                            values.add( 1, expectedValue );
                        }
                    }
                }
                return values;
            case 3:
                // gene symbols displayed nicely
                return linkLabels.get( gene_name );
            case 4:
                // description
                return geneData == null ? "" : probeID.getDescription();
            case 5:
                // multifunctionality.
                if ( geneData == null ) return "";
                gene_name = probeID.getGene();
                return String.format( "%.3f (%d)", Math.max( 0.0, geneData.getMultifunctionality()
                        .getMultifunctionalityRank( gene_name ) ), geneData.getMultifunctionality().getNumGoTerms(
                        gene_name ) );
            default:
                return "";
        }
    } // end getValueAt

    private void createLinkLabels() {
        assert probeIDs != null;

        /*
         * Each is a little panel that has multiple labels in a
         */

        this.linkLabels = new HashMap<Gene, JLinkLabel>();
        for ( Iterator<Probe> iter = probeIDs.iterator(); iter.hasNext(); ) {
            final Probe probe = iter.next();
            Gene gene = probe.getGene();
            if ( gene == null ) {
                continue;
            }
            String url = urlbase.replaceFirst( URL_REPLACE_TAG, gene.getSymbol() );

            // JPanel p = new JPanel();
            // p.setName( "Panel for " + probe );
            // p.setBackground( Color.WHITE );
            // p.setLayout( new BoxLayout( p, BoxLayout.LINE_AXIS ) );
            // p.setAlignmentY( Component.BOTTOM_ALIGNMENT );
            // p.setOpaque( false );

            JLinkLabel baseLink = new JLinkLabel( gene.getSymbol(), url );
            // p.add( );
            // p.add( new JLinkLabel( gemmaIcon, "" ) );

            linkLabels.put( gene, baseLink );
        }
        this.fireTableDataChanged();
    }

    /**
     * 
     */
    protected void configure() {
        String candidateUrlBase = settings.getConfig().getString( SettingsHolder.GENE_URL_BASE );
        if ( candidateUrlBase != null && candidateUrlBase.indexOf( URL_REPLACE_TAG ) >= 0 ) {
            this.urlbase = candidateUrlBase;
            log.debug( "Setting urlbase to " + urlbase );
        } else {
            this.urlbase = "no setting found";
            log.warn( "No gene tag in user's url base" );
        }
        this.createLinkLabels();
    }
} // end class DetailsTableModel
