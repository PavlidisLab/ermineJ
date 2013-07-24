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
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.StringUtils;

import ubic.erminej.gui.util.Colors;

/**
 * Renders the "pvalue bars" in the details views.
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class JBarGraphCellRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = 3914501898335944322L;

    protected List<Double> m_values = null;
    protected final static int LINE_WIDTH = 2;
    protected final static Color[] COLORS = { Color.GRAY, Colors.LIGHTBLUE2, Colors.LIGHTRED3, Color.GREEN, Color.CYAN,
            Color.MAGENTA, Color.ORANGE };
    protected static Border m_noFocusBorder = new EmptyBorder( 1, 1, 1, 1 );
    protected static Color m_selectionBackground;
    protected boolean m_isSelected = false;
    protected boolean m_isBarGraph = false;
    DecimalFormat m_regular = new DecimalFormat();

    private double maxValue;

    private Color[] colors;
    private static final double defaultMaxValue = 5;

    public JBarGraphCellRenderer() {
        this( defaultMaxValue, COLORS );
    }

    public JBarGraphCellRenderer( double maxValue, Color[] colors ) {
        super();
        setOpaque( false );
        setBorder( m_noFocusBorder );
        this.maxValue = maxValue;
        this.colors = colors;
    }

    /**
     * This method is called each time a cell in a column using this renderer needs to be rendered.
     * 
     * @param table the <code>JTable</code>
     * @param value the value to assign to the cell at <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     */
    @Override
    @SuppressWarnings("unchecked")
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column ) {

        // set background
        m_isSelected = isSelected;
        if ( isSelected ) {
            super.setBackground( m_selectionBackground = table.getSelectionBackground() );
        } else {
            super.setBackground( table.getBackground() );
            // or force a white background instead
        }

        if ( hasFocus ) {
            setBorder( UIManager.getBorder( "Table.focusCellHighlightBorder" ) );
        } else {
            setBorder( m_noFocusBorder );
        }

        m_isBarGraph = false;
        if ( value instanceof List ) {
            // bar graphF
            m_isBarGraph = true;
            m_values = ( List<Double> ) value;
            List<String> formatted = new Vector<String>();
            for ( Double v : m_values ) {
                formatted.add( String.format( "%.4g", v ) );
            }
            this.setToolTipText( StringUtils.join( formatted, "," ) );
            // setText( m_values.get( 1 ).toString() );
        } else if ( value instanceof Double ) {
            // just double value, no bar graph
            setText( value.toString() );
            setFont( table.getFont() );
        }

        // Since the renderer is a component, return itself
        return this;
    }

    protected void paintBackground( Graphics g ) {
        g.setColor( m_selectionBackground );
        g.fillRect( 0, 0, getWidth(), getHeight() );
    }

    @Override
    protected void paintComponent( Graphics g ) {

        if ( m_isSelected ) {
            paintBackground( g );
        }

        super.paintComponent( g );

        if ( !m_isBarGraph ) return;
        if ( m_values == null ) return;

        final int width = getWidth() - 2; // leave a little gap on each side.
        final int height = getHeight();
        final int y = 0;

        for ( int i = 0; i < m_values.size(); i++ ) {

            double val = m_values.get( i );

            if ( Double.isNaN( val ) ) {
                continue;
            }

            int x = ( int ) ( width * val / maxValue );

            // what color to use?
            if ( i < colors.length ) {
                g.setColor( colors[i] );
            } else {
                // ran out of colors!
                g.setColor( Color.LIGHT_GRAY );
            }

            // draw the vertical bar line
            if ( x > width ) x = width - LINE_WIDTH;
            g.fillRect( x + 1 /* gap */, y, LINE_WIDTH, height );
        }

    }

}
