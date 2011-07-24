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
package ubic.erminej.gui.geneset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.graphics.MatrixDisplay;
import ubic.erminej.Settings;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.Probe;
import ubic.erminej.gui.JLinkLabel;
import ubic.erminej.gui.table.MatrixPoint;

/**
 * Our table model for one gene set.
 * 
 * @author Will Braynen
 * @version $Id$
 * @see GeneSetDetailsFrame
 */
public class GeneSetDetailsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -1L;
    private static final String URL_REPLACE_TAG = "@@";
    private MatrixDisplay<Probe, String> m_matrixDisplay;
    private List<Probe> probeIDs;
    private Map<Probe, Double> m_pvalues;
    private Map<Probe, Integer> m_pvaluesOrdinalPosition;
    private GeneAnnotations geneData;
    private Settings settings;
    private Map<Gene, JLinkLabel> linkLabels;
    private String[] m_columnNames = { "Probe", "Score", "Score", "Symbol", "Name", "Multifunc" };
    public static final String DEFAULT_GENE_URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=search&term="
            + URL_REPLACE_TAG;

    private String urlbase = DEFAULT_GENE_URL_BASE;

    protected static final Log log = LogFactory.getLog( GeneSetDetailsTableModel.class );

    @Override
    public Class<?> getColumnClass( int columnIndex ) {

        int offset = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;

        if ( columnIndex < offset ) {
            return Double.class; // matrix, or pvals.
        } else if ( columnIndex - offset == 0 ) {
            return String.class; // probe
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

    /**
     * @param matrixDisplay
     * @param probeIDs
     * @param pvalues
     * @param pvaluesOrdinalPosition
     * @param geneData
     * @param nf
     */
    public GeneSetDetailsTableModel( MatrixDisplay<Probe, String> matrixDisplay, Collection<Probe> probeIDs,
            Map<Probe, Double> pvalues, Map<Probe, Integer> pvaluesOrdinalPosition, GeneAnnotations geneData,
            Settings settings ) {

        m_matrixDisplay = matrixDisplay;
        this.probeIDs = new ArrayList<Probe>( probeIDs );
        m_pvalues = pvalues;
        this.settings = settings;
        m_pvaluesOrdinalPosition = pvaluesOrdinalPosition;
        this.geneData = geneData;

        configure();
        createLinkLabels();
    }

    /**
     * 
     */
    void createLinkLabels() {
        assert probeIDs != null;
        this.linkLabels = new HashMap<Gene, JLinkLabel>();
        for ( Iterator<Probe> iter = probeIDs.iterator(); iter.hasNext(); ) {
            Probe probe = iter.next();
            Gene gene = probe.getGene();
            if ( gene != null ) {
                String url = urlbase.replaceFirst( URL_REPLACE_TAG, gene.getSymbol() );
                linkLabels.put( gene, new JLinkLabel( gene.getSymbol(), url ) );
            }
        }
        this.fireTableDataChanged();
    }

    /**
     * 
     */
    protected void configure() {
        String candidateUrlBase = settings.getConfig().getString( Settings.GENE_URL_BASE );
        if ( candidateUrlBase != null && candidateUrlBase.indexOf( URL_REPLACE_TAG ) >= 0 ) {
            this.urlbase = candidateUrlBase;
            log.debug( "Setting urlbase to " + urlbase );
        } else {
            this.urlbase = "no setting found";
            log.warn( "No gene tag in user's url base" );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName( int column ) {

        // matrix display ends
        int offset = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;

        if ( column < offset ) {
            return m_matrixDisplay.getColumnName( column ).toString();
        }
        return m_columnNames[column - offset];

    } // end getColumnName

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
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        int matrixColumnCount = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;
        return m_columnNames.length + matrixColumnCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int row, int column ) {

        // matrix display ends
        int offset = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;

        // get the probeID for the current row
        Probe probeID = probeIDs.get( row );

        // If this is part of the matrix display
        if ( column < offset ) {
            return new MatrixPoint( m_matrixDisplay.getRowIndexByName( probeID ), column, m_matrixDisplay.getValue(
                    m_matrixDisplay.getRowIndexByName( probeID ), column ) ); // coords into JMatrixDisplay
        }
        column -= offset;

        switch ( column ) { // after it's been offset
            case 0:
                // probe ID
                return probeID.getName();
            case 1:
                // p value

                if ( m_pvalues == null || !m_pvalues.containsKey( probeID ) ) return new Double( Double.NaN );

                return m_pvalues.get( probeID );

            case 2:
                // p value bar
                List<Double> values = new ArrayList<Double>();
                if ( !settings.getDoLog() || m_pvalues == null ) { // kludgy way to figure out if we have pvalues.
                    values.add( 0, new Double( Double.NaN ) );
                    values.add( 1, new Double( Double.NaN ) );
                } else {

                    if ( !m_pvalues.containsKey( probeID ) ) {
                        values.add( 0, new Double( 1.0 ) );
                        values.add( 1, new Double( 1.0 ) );
                    } else {
                        // actual p value
                        Double actualValue = m_pvalues.get( probeID );
                        values.add( 0, actualValue );
                        // expected p value, but only if we had an actual pvalue
                        if ( !Double.isNaN( actualValue.doubleValue() ) ) {
                            int position = m_pvaluesOrdinalPosition.get( probeID );
                            Double expectedValue = new Double( 1.0f / getRowCount() * ( position + 1 ) );
                            values.add( 1, expectedValue );
                        }
                    }
                }
                return values;
            case 3:
                // gene symbol.
                if ( geneData == null ) return "No data available";
                Gene gene_name = probeID.getGene();

                return geneData == null ? null : linkLabels.get( gene_name );
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
} // end class DetailsTableModel
