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

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.bio.geneset.Multifunctionality;
import ubic.basecode.graphics.MatrixDisplay;
import ubic.erminej.Settings;
import ubic.erminej.gui.JLinkLabel;

/**
 * Our table model.
 * <p>
 * The general picture is as follows: <br>
 * GUI -> Sort Filter -> Table Model
 * <hr>
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class GeneSetDetailsTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = -8155800933946966811L;
    private static final String URL_REPLACE_TAG = "@@";
    private MatrixDisplay m_matrixDisplay;
    private List<String> probeIDs;
    private Map<String, Double> m_pvalues;
    private Map<String, Integer> m_pvaluesOrdinalPosition;
    private GeneAnnotations geneData;
    private DecimalFormat m_nf;
    private Settings settings;
    private Map<String, JLinkLabel> linkLabels;
    private String[] m_columnNames = { "Probe", "Score", "Score", "Symbol", "Name", "Multifunc" };
    private String urlbase = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=search&term=" + URL_REPLACE_TAG;
    private Multifunctionality multifunctionality;
    protected static final Log log = LogFactory.getLog( GeneSetDetailsTableModel.class );

    /**
     * @param matrixDisplay
     * @param probeIDs
     * @param pvalues
     * @param pvaluesOrdinalPosition
     * @param geneData
     * @param nf
     */
    public GeneSetDetailsTableModel( MatrixDisplay matrixDisplay, List<String> probeIDs, Map<String, Double> pvalues,
            Map<String, Integer> pvaluesOrdinalPosition, GeneAnnotations geneData, DecimalFormat nf, Settings settings ) {

        m_matrixDisplay = matrixDisplay;
        this.probeIDs = probeIDs;
        m_pvalues = pvalues;
        this.settings = settings;
        m_pvaluesOrdinalPosition = pvaluesOrdinalPosition;
        this.geneData = geneData;

        this.multifunctionality = new Multifunctionality( geneData );

        m_nf = nf;
        configure();
        createLinkLabels();
    }

    /**
     * 
     */
    private void createLinkLabels() {
        assert probeIDs != null;
        this.linkLabels = new HashMap<String, JLinkLabel>();
        for ( Iterator<String> iter = probeIDs.iterator(); iter.hasNext(); ) {
            String probe = iter.next();
            String gene = geneData.getProbeGeneName( probe );
            if ( gene != null ) {
                String url = urlbase.replaceFirst( URL_REPLACE_TAG, gene );
                linkLabels.put( gene, new JLinkLabel( gene, url ) );
            }
        }

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
        String probeID = probeIDs.get( row );

        // If this is part of the matrix display
        if ( column < offset ) {
            return new Point( m_matrixDisplay.getRowIndexByName( probeID ), column ); // coords into JMatrixDisplay
        }
        column -= offset;

        switch ( column ) { // after it's been offset
            case 0:
                // probe ID
                return probeID;
            case 1:
                // p value
                try {
                    if ( m_pvalues == null || !m_pvalues.containsKey( probeID ) ) return new Double( Double.NaN );
                    return new Double( m_nf.format( m_pvalues.get( probeID ) ) );

                } catch ( NumberFormatException e ) {
                    return new Double( Double.NaN );
                }
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
                String gene_name = geneData.getProbeGeneName( probeID );

                return geneData == null ? null : linkLabels.get( gene_name );
            case 4:
                // description
                return geneData == null ? "" : geneData.getProbeDescription( probeID );
            case 5:
                if ( geneData == null ) return "";
                gene_name = geneData.getProbeGeneName( probeID );
                return String.format( "%.3f (%d)", Math.max( 0.0, multifunctionality
                        .getMultifunctionalityRank( gene_name ) ), multifunctionality.getNumGoTerms( gene_name ) );
            default:
                return "";
        }
    } // end getValueAt
} // end class DetailsTableModel
