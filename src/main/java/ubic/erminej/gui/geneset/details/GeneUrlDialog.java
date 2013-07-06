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
package ubic.erminej.gui.geneset.details;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.gui.util.GuiUtil;

/**
 * @author pavlidis
 * @version $Id$
 */
public class GeneUrlDialog extends JDialog {
    private static final String GENEPLACEHOLDER = "@@";
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog( GeneUrlDialog.class );
    private JPanel mainPanel;
    private JPanel bottomPanel = new JPanel();
    private JButton cancelButton = new JButton();
    private JButton setButton = new JButton();
    private JTextField urlTextField;
    private StatusViewer statusMessenger;
    private SettingsHolder settings;

    /**
     * Get the url that was set.
     * 
     * @return
     */
    public String getUrl() {
        return StringUtils.strip( this.urlTextField.getText() );
    }

    /**
     * @param settings
     */
    public GeneUrlDialog( SettingsHolder settings ) {
        this.settings = settings;

        if ( this.settings == null ) {
            try {
                this.settings = new Settings( true ).getSettingsHolder();
            } catch ( IOException e1 ) {
                throw new RuntimeException( e1 );
            }
        }

        this.setModal( true );
        try {
            jbInit();
            GuiUtil.centerContainer( this );
            urlTextField.requestFocusInWindow();
            this.setVisible( true );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void jbInit() {
        setResizable( true );
        this.setMinimumSize( new Dimension( 700, 120 ) );

        mainPanel = ( JPanel ) this.getContentPane();
        mainPanel.setLayout( new BorderLayout() );
        urlTextField = new JTextField();
        initializeFieldText();
        urlTextField.setMinimumSize( new Dimension( 200, 19 ) );

        cancelButton.setText( "Cancel" );
        cancelButton.addActionListener( new CancelButton_actionAdapter( this ) );
        setButton.setText( "Save" );
        setButton.addActionListener( new SetButton_actionAdapter( this ) );
        bottomPanel.add( setButton );
        bottomPanel.add( cancelButton );

        JButton resetToDefaultbutton = new JButton( "Restore default" );
        resetToDefaultbutton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                urlTextField.setText( settings.getDefaultGeneUrl() );
            }
        } );

        bottomPanel.add( resetToDefaultbutton );

        mainPanel.add( urlTextField, BorderLayout.CENTER );
        mainPanel.add( bottomPanel, BorderLayout.SOUTH );
        this.setTitle( "URL for gene links ('" + GENEPLACEHOLDER + "' replaced by gene)" );

    }

    private void initializeFieldText() {
        assert settings != null;
        String oldUrlBase = this.settings.getGeneUrlBase();
        log.debug( "Found url base " + oldUrlBase );
        urlTextField.setText( oldUrlBase );
    }

    void cancelButton_actionPerformed() {
        dispose();
    }

    /**
     * This is the business
     */
    protected void setActionPerformed() {
        String candidateUrlBase = urlTextField.getText().trim();

        if ( StringUtils.isBlank( candidateUrlBase ) ) {
            statusMessenger.showError( "URL must not be blank." );
            return;
        }

        if ( candidateUrlBase.indexOf( GENEPLACEHOLDER ) < 0 ) {
            statusMessenger.showError( "URL must contain the string '" + GENEPLACEHOLDER
                    + "' for substitution with the gene name" );
            return;
        }

        if ( StringUtils.deleteWhitespace( candidateUrlBase ).length() < candidateUrlBase.length() ) {
            statusMessenger.showError( "URL must not contain any spaces" );
            return;
        }

        dispose();
    }
}

class SetButton_actionAdapter implements java.awt.event.ActionListener {
    GeneUrlDialog adaptee;

    SetButton_actionAdapter( GeneUrlDialog adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.setActionPerformed();
    }
}

class CancelButton_actionAdapter implements java.awt.event.ActionListener {
    GeneUrlDialog adaptee;

    CancelButton_actionAdapter( GeneUrlDialog adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.cancelButton_actionPerformed();
    }
}
