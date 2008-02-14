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
import ubic.basecode.gui.JLinkLabel;
import ubic.basecode.gui.JMatrixDisplay;
import ubic.erminej.Settings;

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

public class GeneSetTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = -8155800933946966811L;
    private static final String URL_REPLACE_TAG = "@@";
    private JMatrixDisplay m_matrixDisplay;
    private List probeIDs;
    private Map m_pvalues;
    private Map m_pvaluesOrdinalPosition;
    private GeneAnnotations geneData;
    private DecimalFormat m_nf;
    private Settings settings;
    private Map<String, JLinkLabel> linkLabels;
    private String[] m_columnNames = { "Probe", "Score", "Score", "Symbol", "Name" };
    private String urlbase = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=search&term=" + URL_REPLACE_TAG;
    protected static final Log log = LogFactory.getLog( GeneSetTableModel.class );

    /**
     * @param matrixDisplay
     * @param probeIDs
     * @param pvalues
     * @param pvaluesOrdinalPosition
     * @param geneData
     * @param nf
     */
    public GeneSetTableModel( JMatrixDisplay matrixDisplay, List probeIDs, Map pvalues, Map pvaluesOrdinalPosition,
            GeneAnnotations geneData, DecimalFormat nf, Settings settings ) {

        m_matrixDisplay = matrixDisplay;
        this.probeIDs = probeIDs;
        m_pvalues = pvalues;
        this.settings = settings;
        m_pvaluesOrdinalPosition = pvaluesOrdinalPosition;
        this.geneData = geneData;
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
        for ( Iterator iter = probeIDs.iterator(); iter.hasNext(); ) {
            String probe = ( String ) iter.next();
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

    public String getColumnName( int column ) {

        // matrix display ends
        int offset = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;

        if ( column < offset ) {
            return m_matrixDisplay.getColumnName( column ).toString();
        }
        return m_columnNames[column - offset];

    } // end getColumnName

    public int getRowCount() {
        return probeIDs.size();
    }

    public int getColumnCount() {
        int matrixColumnCount = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;
        return m_columnNames.length + matrixColumnCount;
    }

    public Object getValueAt( int row, int column ) {

        // matrix display ends
        int offset = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;

        // get the probeID for the current row
        String probeID = ( String ) probeIDs.get( row );

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
                        Double actualValue = ( Double ) m_pvalues.get( probeID );
                        values.add( 0, actualValue );
                        // expected p value, but only if we had an actual pvalue
                        if ( !Double.isNaN( actualValue.doubleValue() ) ) {
                            int position = ( ( Integer ) m_pvaluesOrdinalPosition.get( probeID ) ).intValue();
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
            default:
                return "";
        }
    } // end getValueAt
} // end class DetailsTableModel
