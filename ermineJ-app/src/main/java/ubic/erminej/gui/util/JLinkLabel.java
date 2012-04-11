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
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;

import ubic.basecode.util.BrowserLauncher;

/**
 * Note that this cannot be a mouselistener because of limitations in how Java makes tables -- the cells is not a
 * component (for performance reasons). Thus clicks on this have to be handled by a container.
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class JLinkLabel extends JLabel implements Comparable<JLinkLabel> {

    private static final long serialVersionUID = -1L;

    protected String m_url = null;

    protected String m_text = "";

    /** Creates a new instance of JLinkLabel */
    public JLinkLabel() {
        super();
        configure();
    }

    public JLinkLabel( Icon icon, String url ) {
        super();
        this.setIcon( icon );
        setURL( url );
        configure();
    }

    public JLinkLabel( String text, String url ) {
        super();
        this.setText( text );
        setURL( url );
        configure();
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
                GuiUtil.error( "Could not open a web browser window:\n" + ex.getMessage() );
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
        m_url = url.replaceFirst( "\\|.+", "" ); // multigene.
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

    @SuppressWarnings("unchecked")
    private void configure() {
        this.setBackground( Color.WHITE );
        this.setForeground( Color.BLUE );
        this.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
        this.setOpaque( false );
        @SuppressWarnings("rawtypes")
        Map attrs = this.getFont().getAttributes();
        attrs.put( TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL );
        this.setFont( this.getFont().deriveFont( attrs ) );

    }

}