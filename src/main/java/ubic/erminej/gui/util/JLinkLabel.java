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
package ubic.erminej.gui.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import ubic.basecode.util.BrowserLauncher;

/**
 * <p>
 * JLinkLabel class.
 * </p>
 *
 * @author Will Braynen
 * @version $Id$
 */
public class JLinkLabel extends JLabel implements Comparable<JLinkLabel> {

    private static final long serialVersionUID = -1L;

    protected String url = null;

    /**
     * <p>
     * Constructor for JLinkLabel.
     * </p>
     *
     * @param text a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     */
    public JLinkLabel( String text, String url ) {
        super();
        this.setText( text );
        this.url = url;
        configure();
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo( JLinkLabel o ) {
        return this.getText().compareTo( o.getText() );
    }

    /**
     * <p>
     * getURL.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getURL() {
        return url;
    }

    /**
     * If this is to be used outside of a JTable rendering context, you should call this method.
     */
    public void makeMouseListener() {
        this.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                openUrl();
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
                setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );

            }

            @Override
            public void mouseExited( MouseEvent e ) {
                setCursor( Cursor.getDefaultCursor() );
            }

            @Override
            public void mousePressed( MouseEvent e ) {
            }

            @Override
            public void mouseReleased( MouseEvent e ) {

            }
        } );
    }

    /**
     * <p>
     * openUrl.
     * </p>
     */
    public void openUrl() {
        if ( url != null ) {
            try {
                BrowserLauncher.openURL( url );
            } catch ( Exception ex ) {
                GuiUtil.error( "Could not open a web browser window:\n" + ex.getMessage() );
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "<html><a href=\"" + url + "\">" + getText() + "</a></html>";
    }

    @SuppressWarnings("unchecked")
    private void configure() {
        this.setBackground( Color.WHITE );
        this.setForeground( Color.BLUE );
        this.setOpaque( true );
        this.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
        @SuppressWarnings("rawtypes")
        Map attrs = this.getFont().getAttributes();
        attrs.put( TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL );
        this.setFont( this.getFont().deriveFont( attrs ) );

    }

}
