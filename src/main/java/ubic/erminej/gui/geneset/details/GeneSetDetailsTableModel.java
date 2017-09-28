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

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hep.aida.bin.QuantileBin1D;
import ubic.basecode.graphics.MatrixDisplay;
import ubic.basecode.math.Rank;
import ubic.erminej.SettingsHolder;
import ubic.erminej.analysis.ScoreQuantiles;
import ubic.erminej.data.Element;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetDetails;
import ubic.erminej.data.Multifunctionality;
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
    /** Constant <code>DEFAULT_GENE_URL_BASE="http://www.ncbi.nlm.nih.gov/entrez/quer"{trunked}</code> */
    public static final String DEFAULT_GENE_URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=search&term="
            + URL_REPLACE_TAG;
    private static final Log log = LogFactory.getLog( GeneSetDetailsTableModel.class );
    private MatrixDisplay<Element, String> matrixDisplay;
    private List<Element> probeIDs;
    private Map<Element, Double> scoresForProbesInSet;
    private Map<Element, Double> scoreRanks;
    private GeneAnnotations geneData;
    private SettingsHolder settings;

    // private String[] tableColumnTooltips = { "", "", "", "", "", "" };

    private Map<Gene, JLinkLabel> linkLabels;

    private String[] tableColumnNames = { "Element", "Score", "QQ Score", "Symbol", "Name", "Multifunc", "QQ Multifunc" };

    private String urlbase = DEFAULT_GENE_URL_BASE;

    private QuantileBin1D scoreQuantiles = new QuantileBin1D( 0.01 );
    private QuantileBin1D mfQuantiles = new QuantileBin1D( 0.01 );
    private Map<Element, Double> multifuncForProbesInSet = new HashMap<>();
    private Map<Gene, Double> multifuncForGenesInSet = new HashMap<>();
    private Map<Gene, Double> mfGeneRanks = new HashMap<>();

    /**
     * <p>
     * Constructor for GeneSetDetailsTableModel.
     * </p>
     *
     * @param matrixDisplay a {@link ubic.basecode.graphics.MatrixDisplay} object.
     * @param geneSetDetails a {@link ubic.erminej.data.GeneSetDetails} object.
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     */
    public GeneSetDetailsTableModel( MatrixDisplay<Element, String> matrixDisplay, GeneSetDetails geneSetDetails,
            SettingsHolder settings ) {

        this.matrixDisplay = matrixDisplay;
        this.probeIDs = new ArrayList<>( geneSetDetails.getProbes() );

        scoresForProbesInSet = geneSetDetails.getProbeScores();
        this.settings = settings;
        this.geneData = geneSetDetails.getGeneData();

        if ( scoresForProbesInSet != null && !scoresForProbesInSet.isEmpty() ) {
            scoreRanks = Rank.rankTransform( scoresForProbesInSet, settings.getBigIsBetter() );

            for ( Element p : scoreRanks.keySet() ) {
                multifuncForProbesInSet.put( p, geneSetDetails.getGeneData().getMultifunctionality()
                        .getMultifunctionalityScore( p.getGene() ) );
                multifuncForGenesInSet.put( p.getGene(), geneSetDetails.getGeneData().getMultifunctionality()
                        .getMultifunctionalityScore( p.getGene() ) );
            }

            // mfRanks = Rank.rankTransform( multifuncForProbesInSet, true );
            mfGeneRanks = Rank.rankTransform( multifuncForGenesInSet, true );
            // this is a wee bit wasteful
            GeneScores geneScores = geneSetDetails.getSourceGeneScores();
            this.scoreQuantiles = ScoreQuantiles.computeQuantiles( settings, geneScores );

            this.mfQuantiles = geneSetDetails.getGeneData().getMultifunctionality()
                    .getGeneMultifunctionalityQuantiles();
        }

        configure();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass( int columnIndex ) {

        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;

        if ( columnIndex < offset ) {
            return Double.class; // matrix
        } else if ( columnIndex - offset == 0 ) {
            return Element.class; // probe
        } else if ( columnIndex - offset == 1 ) {
            return Double.class; // score
        } else if ( columnIndex - offset == 2 ) {
            return Object.class; // actually a List, which is not comparable.
        } else if ( columnIndex - offset == 3 ) {
            return JLinkLabel.class; // symbol
        } else if ( columnIndex - offset == 4 ) {
            return String.class; // description
        } else if ( columnIndex - offset == 5 ) {
            return Double.class; // score
        }
        // return String.class; // mf.
        return Object.class;// QQ plot for mf.
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        int matrixColumnCount = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;
        return tableColumnNames.length + matrixColumnCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    /** {@inheritDoc} */
    @Override
    public String getColumnName( int column ) {

        // matrix display ends
        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;

        if ( column < offset ) {
            return matrixDisplay.getColumnName( column ).toString();
        }
        return tableColumnNames[column - offset];

    } // end getColumnName

    /**
     * <p>
     * getProbeAtRow.
     * </p>
     *
     * @param r a int.
     * @return a {@link ubic.erminej.data.Element} object.
     */
    public Element getProbeAtRow( int r ) {
        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;
        return ( Element ) getValueAt( r, offset );
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getRowCount()
     */
    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return probeIDs.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    /** {@inheritDoc} */
    @Override
    public Object getValueAt( int row, int column ) {

        // matrix display ends
        int offset = ( matrixDisplay != null ) ? matrixDisplay.getColumnCount() : 0;

        // get the elementId for the current row
        Element elementId = probeIDs.get( row );

        // If this is part of the matrix display
        if ( matrixDisplay != null && column < offset ) {
            return new MatrixPoint( matrixDisplay.getRowIndexByName( elementId ), column, matrixDisplay.getValue(
                    matrixDisplay.getRowIndexByName( elementId ), column ) ); // coords into JMatrixDisplay
        }
        column -= offset;
        Gene gene = elementId.getGene();
        Multifunctionality multifunctionality = geneData.getMultifunctionality();
        switch ( column ) { // after it's been offset
            case 0:
                // probe ID
                return elementId;
            case 1:
                // scores
                if ( scoresForProbesInSet == null || !scoresForProbesInSet.containsKey( elementId ) )
                    return new Double( Double.NaN );
                return scoresForProbesInSet.get( elementId );
            case 2:
                List<Double> values = new ArrayList<>();
                if ( scoresForProbesInSet == null || !scoresForProbesInSet.containsKey( elementId ) ) {
                    values.add( 0, 1.0 );
                    values.add( 1, 1.0 );
                } else {

                    // this is the quantile of the scores in the full data set.(but reverse so large is better)
                    double quantile = Math.max( 1.0 / scoreQuantiles.size(),
                            scoreQuantiles.quantileInverse( scoresForProbesInSet.get( elementId ) ) );

                    Double position = scoreRanks.get( elementId );
                    double expectedQuantile = ( position + 1 ) / scoresForProbesInSet.size();

                    values.add( 0, -Math.log10( expectedQuantile ) );
                    values.add( 1, -Math.log10( quantile ) );
                }
                return values;
            case 3:
                // gene symbols displayed nicely
                return linkLabels.get( gene );
            case 4:
                // description
                return geneData == null ? "" : elementId.getDescription();
            case 5:
                // // multifunctionality. ugly.
                if ( geneData == null ) return "";
                gene = elementId.getGene();
                return String.format( "%.3f (%d)",
                        Math.max( 0.0, multifunctionality.getMultifunctionalityRank( gene ) ),
                        multifunctionality.getNumGoTerms( gene ) );

            case 6:
                // multifunctionality graphic.
                List<Double> mfv = new ArrayList<>();
                if ( scoresForProbesInSet == null || !scoresForProbesInSet.containsKey( elementId ) ) {
                    mfv.add( 0, 1.0 );
                    mfv.add( 1, 1.0 );
                } else {
                    double mfQuantile = 1.0 - Math.max( 1.0 / mfQuantiles.size(),
                            mfQuantiles.quantileInverse( multifunctionality.getMultifunctionalityScore( gene ) ) );

                    Double position = mfGeneRanks.get( gene );
                    double expectedQuantile = ( position + 1 ) / multifuncForGenesInSet.size();

                    mfv.add( 0, -Math.log10( expectedQuantile ) );
                    mfv.add( 1, -Math.log10( mfQuantile ) );
                }

                return mfv;
            default:
                return "";
        }
    } // end getValueAt

    /**
     * <p>
     * configure.
     * </p>
     */
    protected void configure() {
        String candidateUrlBase = settings.getStringProperty( SettingsHolder.GENE_URL_BASE );
        if ( candidateUrlBase != null && candidateUrlBase.indexOf( URL_REPLACE_TAG ) >= 0 ) {
            this.urlbase = candidateUrlBase;
            log.debug( "Setting urlbase to " + urlbase );
        } else {
            this.urlbase = "no setting found";
            log.warn( "No gene tag in user's url base" );
        }
        this.createLinkLabels();
    }

    /**
     *
     */
    private void createLinkLabels() {
        assert probeIDs != null;
        this.linkLabels = new HashMap<>();
        for ( Iterator<Element> iter = probeIDs.iterator(); iter.hasNext(); ) {
            final Element probe = iter.next();
            Gene gene = probe.getGene();
            if ( gene == null ) {
                continue;
            }
            String url = urlbase.replaceFirst( URL_REPLACE_TAG, gene.getSymbol() );
            JLinkLabel baseLink = new JLinkLabel( gene.getSymbol(), url );
            linkLabels.put( gene, baseLink );
        }
        this.fireTableDataChanged();
    }
} // end class DetailsTableModel
