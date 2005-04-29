package classScore.gui.geneSet;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.JLinkLabel;
import baseCode.gui.JMatrixDisplay;
import classScore.Settings;

/**
 * Our table model.
 * <p>
 * The general picture is as follows: <br>
 * GUI -> Sort Filter -> Table Model
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Will Braynen
 * @version $Id$
 */

public class GeneSetTableModel extends AbstractTableModel {

    private static final String URL_REPLACE_TAG = "@@";
    private JMatrixDisplay m_matrixDisplay;
    private List m_probeIDs;
    private Map m_pvalues;
    private Map m_pvaluesOrdinalPosition;
    private GeneAnnotations m_geneData;
    private DecimalFormat m_nf;
    private Settings settings;
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
        m_probeIDs = probeIDs;
        m_pvalues = pvalues;
        this.settings = settings;
        m_pvaluesOrdinalPosition = pvaluesOrdinalPosition;
        m_geneData = geneData;
        m_nf = nf;
        configure();
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
            return m_matrixDisplay.getColumnName( column );
        }
        return m_columnNames[column - offset];

    } // end getColumnName

    public int getRowCount() {
        return m_probeIDs.size();
    }

    public int getColumnCount() {
        int matrixColumnCount = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;
        return m_columnNames.length + matrixColumnCount;
    }

    public Object getValueAt( int row, int column ) {

        // matrix display ends
        int offset = ( m_matrixDisplay != null ) ? m_matrixDisplay.getColumnCount() : 0;

        // get the probeID for the current row
        String probeID = ( String ) m_probeIDs.get( row );

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
                return m_pvalues == null ? new Double( Double.NaN ) : new Double( m_nf
                        .format( m_pvalues.get( probeID ) ) );
            case 2:
                // p value bar
                ArrayList values = new ArrayList();
                if ( !settings.getDoLog() || m_pvalues == null ) { // kludgy way to figure out if we have pvalues.
                    values.add( 0, new Double( Double.NaN ) );
                } else {
                    // actual p value
                    Double actualValue = ( Double ) m_pvalues.get( probeID );
                    values.add( 0, actualValue );
                    // expected p value
                    int position = ( ( Integer ) m_pvaluesOrdinalPosition.get( probeID ) ).intValue();
                    Double expectedValue = new Double( 1.0f / getRowCount() * ( position + 1 ) );
                    values.add( 1, expectedValue );
                }
                return values;
            case 3:
                if ( m_geneData == null ) return "No data available";
                String gene_name = m_geneData.getProbeGeneName( probeID );
                String url = urlbase.replaceFirst( URL_REPLACE_TAG, gene_name );
                return m_geneData == null ? null : new JLinkLabel( gene_name, url );
            case 4:
                // description
                return m_geneData == null ? "" : m_geneData.getProbeDescription( probeID );
            default:
                return "";
        }
    } // end getValueAt

} // end class DetailsTableModel
