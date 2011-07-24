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
package ubic.erminej.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JTextField;

import ubic.basecode.util.BrowserLauncher;

/**
 * A clickable link label that contains a URL. When a mouse pointer is placed over it, it turns into a hand.
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class JLinkLabel extends JTextField implements Comparable<JLinkLabel> {

    private static final long serialVersionUID = -1L;

    protected String m_url = null;

    protected String m_text = "";

    /** Creates a new instance of JLinkLabel */
    @SuppressWarnings("unchecked")
    public JLinkLabel() {
        super();
        this.setBackground( Color.WHITE );
        this.setForeground( Color.BLUE );
        this.setBorder( null );
        this.setEditable( false );
        Map attrs = this.getFont().getAttributes();
        attrs.put( TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL );
        this.setFont( this.getFont().deriveFont( attrs ) );
    }

    public JLinkLabel( String text ) {
        this();
        setText( text );
    }

    public JLinkLabel( String text, String url ) {
        this();
        setText( text, url );
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

    @Override
    public int compareTo( JLinkLabel o ) {
        return this.m_text.compareTo( o.m_text );
    }

    /**
     * @return
     */
    public String getURL() {
        return m_url;
    }

    public void openUrl() {
        if ( m_url != null ) {
            try {
                BrowserLauncher.openURL( m_url );
            } catch ( Exception ex ) {
                GuiUtil.error( "Could not open a web browser window." );
            }
        }
    }

    @Override
    public void setText( String text ) {
        if ( m_url != null ) {
            setText( text, m_url );
        } else {
            setText( text, text );
        }
    }

    public void setText( String text, String url ) {
        m_text = text;
        m_url = url;
        // super.setText( "<a href=\"" + m_url + "\">" + text + "</a>" );
        super.setText( text );
    }

    /**
     * @param url
     */
    public void setURL( String url ) {
        setText( m_text, url );
    }

    @Override
    public String toString() {
        return "<html><a href=\"" + m_url + "\">" + m_text + "</a></html>";
    }

}