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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import ubic.basecode.graphics.text.Util;

/**
 * Create text going up vertically for the headings.
 *
 * @author Will Braynen
 * @version $Id$
 */
public class JVerticalHeaderRenderer extends JTableHeader implements TableCellRenderer {

    private static final long serialVersionUID = -236801351350339640L;
    String m_columnName;
    final int PREFERRED_HEIGHT = 80;
    final int MAX_TEXT_LENGTH = 12;

    /** {@inheritDoc} */
    @Override
    public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue ) {
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredSize() {

        return new Dimension( super.getPreferredSize().width, PREFERRED_HEIGHT );
    }

    /**
     * {@inheritDoc}
     *
     * This method is called each time a column header using this renderer needs to be rendered.
     */
    @Override
    public Component getTableCellRendererComponent( JTable t, Object value, boolean isSelected, boolean hasFocus,
            int rowIndex, int vColIndex ) {
        // 'value' is column header value of column 'vColIndex'
        // rowIndex is always -1
        // isSelected is always false
        // hasFocus is always false

        // Configure the component with the specified value
        m_columnName = value.toString();

        // Set tool tip if desired
        setToolTipText( m_columnName );

        // Since the renderer is a component, return itself
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void revalidate() {
    }

    // The following methods override the defaults for performance reasons
    /** {@inheritDoc} */
    @Override
    public void validate() {
    }

    /** {@inheritDoc} */
    @Override
    protected void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {
    }

    /** {@inheritDoc} */
    @Override
    protected void paintComponent( Graphics g ) {

        super.paintComponent( g );
        Font font = getFont();

        if ( m_columnName.length() > MAX_TEXT_LENGTH ) {
            m_columnName = m_columnName.substring( 0, MAX_TEXT_LENGTH );

        }
        int x = getSize().width - 4;
        int y = getSize().height - 4;
        Util.drawVerticalString( g, m_columnName, font, x, y );
    }
}
