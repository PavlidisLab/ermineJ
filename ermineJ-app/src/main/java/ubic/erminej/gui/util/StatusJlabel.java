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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import ubic.basecode.util.StatusDebugLogger;

/**
 * @author pavlidis
 * @version $Id$
 */
public class StatusJlabel extends StatusDebugLogger {

    private static final int MAX_LABEL_TEXT = 200;

    protected JLabel jlabel;

    private static Icon errorIcon = new ImageIcon( StatusJlabel.class.getResource( "/ubic/erminej/alert.gif" ) );

    /*
     * How long we display error messages for by default. too short, user can't read it; too long, slows things donw.
     */
    private static final int MESSAGE_DELAY = 1300; // milliseconds

    public StatusJlabel( JLabel l ) {
        this.jlabel = l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.util.StatusViewer#clear()
     */
    @Override
    public void clear() {
        if ( SwingUtilities.isEventDispatchThread() ) {
            setLabel( "", null );
        } else {
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        setLabel( "", null );
                    }
                } );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            } catch ( InvocationTargetException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param s
     * @param callSuper
     */
    @Override
    public void showStatus( String s, boolean callSuper ) {
        final String m = StringUtils.abbreviate( s, MAX_LABEL_TEXT );

        if ( SwingUtilities.isEventDispatchThread() ) {
            setLabel( m, null );
        } else {

            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        setLabel( m, null );
                    }
                } );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            } catch ( InvocationTargetException e ) {
                e.printStackTrace();
            }
        }

        if ( callSuper ) {
            super.showStatus( s );
        }
    }

    @Override
    public void showError( String s ) {

        final String m = StringUtils.abbreviate( s, MAX_LABEL_TEXT );

        if ( SwingUtilities.isEventDispatchThread() ) {
            setLabel( m, errorIcon );
        } else {
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        setLabel( m, errorIcon );
                        letUserReadMessage( MESSAGE_DELAY );
                    }
                } );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            } catch ( InvocationTargetException e ) {
                e.printStackTrace();
            }
        }
        super.showError( s );

    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.util.StatusViewer#setError(java.lang.String)
     */

    @Override
    public void showError( String message, Throwable e ) {
        super.showError( message, e );
        this.showError( message );
    }

    /**
     * 
     */
    @Override
    public void showError( Throwable e ) {

        String m = "There was an error: " + e.getMessage() + "; See logs for details.";

        if ( e instanceof CancellationException ) {
            m = "Cancelled";
        }

        if ( SwingUtilities.isEventDispatchThread() ) {
            setLabel( m, errorIcon );
        } else {
            final String mf = m;
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        setLabel( mf, errorIcon );
                    }
                } );
            } catch ( InterruptedException ex ) {
                ex.printStackTrace();
            } catch ( InvocationTargetException ex ) {
                ex.printStackTrace();
            }
        }
        super.showError( e );
    }

    @Override
    public void showStatus( String s ) {
        this.showStatus( s, true );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.util.StatusDebugLogger#showStatus(java.lang.String, int)
     */
    @Override
    public void showStatus( String s, int sleepSeconds ) {
        super.showStatus( s, sleepSeconds );
    }

    /** 
     */
    private void letUserReadMessage( int mswait ) {
        try {
            Thread.sleep( mswait );
        } catch ( InterruptedException e ) {
            return;
        }
    }

    /**
     * @param m
     */
    protected void setLabel( final String m, final Icon icon ) {
        jlabel.setText( StringUtils.abbreviate( m, 300 ) );
        jlabel.setIcon( icon );
        jlabel.repaint();
    }
}