/*
 * The baseCode project
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
package ubic.erminej.gui.table;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ubic.basecode.graphics.MatrixDisplay;
import ubic.erminej.data.Probe;

/**
 * For rendering heatmaps in tables.
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class JMatrixCellRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = 120496422792943100L;

    MatrixDisplay<Probe, String> m_matrixDisplay;

    public JMatrixCellRenderer( MatrixDisplay<Probe, String> matrixDisplay ) {
        m_matrixDisplay = matrixDisplay;
        setOpaque( true );
    }

    // This method is called each time a cell in a column
    // using this renderer needs to be rendered.
    @Override
    public Component getTableCellRendererComponent( JTable table, Object tableCellValue, boolean isSelected,
            boolean hasFocus, int displayedRow, int displayedColumn ) {
        // 'value' is value contained in the cell located at
        // (rowIndex, vColIndex)

        MatrixPoint coords = ( MatrixPoint ) tableCellValue;
        int row = coords.x;
        int column = coords.y;

        Color matrixColor;
        try {
            matrixColor = m_matrixDisplay.getColor( row, column );
        } catch ( ArrayIndexOutOfBoundsException e ) {
            matrixColor = m_matrixDisplay.getMissingColor();
        }
        if ( isSelected || hasFocus ) {
            // this cell is the anchor and the table has the focus
            if ( isSelected || hasFocus ) {
                // blend colours
                float[] col1comps = new float[3];
                col1comps = table.getSelectionBackground().getColorComponents( col1comps );
                float[] col2comps = new float[3];
                col2comps = matrixColor.getColorComponents( col2comps );

                float r = 0.2f;
                matrixColor = new Color( col1comps[0] * r + col2comps[0] * ( 1.0f - r ), col1comps[1] * r
                        + col2comps[1] * ( 1.0f - r ), col1comps[2] * r + col2comps[2] * ( 1.0f - r ) );
            }

        }

        // Set the color

        this.setBackground( matrixColor );

        setToolTip( row, column );

        // Since the renderer is a component, return itself
        return this;
    }

    /**
     * @param row
     * @param column
     */
    private void setToolTip( int row, int column ) {
        // The tooltip should always show the actual (non-normalized) value
        double matrixValue = m_matrixDisplay.getRawValue( row, column );

        // Only very small and very large numbers should be displayed in
        // scientific notation
        String value;
        if ( Math.abs( matrixValue ) < 0.01 || Math.abs( matrixValue ) > 100000 ) {
            value = String.format( "%.3g", matrixValue );
        } else {
            value = String.format( "%.2f", matrixValue );
        }
        setToolTipText( value );
    }

    // The following methods override the defaults for performance reasons
    @Override
    public void validate() {
    }

    @Override
    public void revalidate() {
    }

    @Override
    protected void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {
    }

    @Override
    public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue ) {
    }

} // end class MatrixDisplayCellRenderer
