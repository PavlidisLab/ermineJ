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
package ubic.erminej.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;

/**
 * FIXME this nominally extends JDialog but it isn't set up right.
 * 
 * @author Homin Lee
 * @author pavlidis
 * @version $Id$
 */
public class FindDialog extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = 8991412162421697305L;
    private static final int MAINWIDTH = 550;
    private JPanel mainPanel;
    private Dimension dlgSize = new Dimension( MAINWIDTH, 100 );
    private JPanel bottomPanel = new JPanel();
    private JButton cancelButton = new JButton();
    private JButton findButton = new JButton();
    private JPanel centerPanel = new JPanel();
    protected JTextField searchTextField;
    private JLabel jLabelStatus = new JLabel();
    private JPanel jPanelStatus = new JPanel();
    private JPanel BottomPanelWrap = new JPanel();
    protected GeneSetScoreFrame callingframe;
    protected GeneAnnotations geneData;
    protected StatusViewer statusMessenger;
    private JButton resetButton;
    protected GONames goData;

    public FindDialog( GeneSetScoreFrame callingframe, GeneAnnotations geneData, GONames goData ) {
        setModal( false );
        this.callingframe = callingframe;
        this.geneData = geneData;
        this.goData = goData;

        try {
            jbInit();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation( ( screenSize.width - dlgSize.width ) / 2, ( screenSize.height - dlgSize.height ) / 2 );
            pack();
            searchTextField.requestFocusInWindow();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    // Component initialization
    private void jbInit() throws Exception {
        setResizable( true );
        mainPanel = ( JPanel ) this.getContentPane();
        this.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent evt ) {
                cancelButton_actionPerformed();
            }
        } );
        mainPanel.setPreferredSize( new Dimension( MAINWIDTH, 150 ) );
        mainPanel.setLayout( new BorderLayout() );

        centerPanel.setPreferredSize( new Dimension( 200, 50 ) );

        searchTextField = new JTextField();
        searchTextField.setPreferredSize( new Dimension( 180, 19 ) );
        searchTextField.addKeyListener( new FindFieldActionAdapter( this ) );
        centerPanel.add( searchTextField, null );

        bottomPanel.setPreferredSize( new Dimension( 200, 40 ) );

        resetButton = new JButton();
        resetButton.setText( "Reset" );
        resetButton.addActionListener( new FindDialog_resetButton_actionAdapter( this ) );

        cancelButton.setText( "Close this window (resets display)" );
        cancelButton.addActionListener( new FindDialog_cancelButton_actionAdapter( this ) );
        cancelButton.setToolTipText( "You can leave this window open while you continue analysis." );

        findButton.setText( "Find" );
        findButton.addActionListener( new FindDialog_findButton_actionAdapter( this ) );
        bottomPanel.add( findButton, null );
        bottomPanel.add( resetButton, null );
        bottomPanel.add( cancelButton, null );

        // status bar
        jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
        jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        jLabelStatus.setPreferredSize( new Dimension( MAINWIDTH - 40, 19 ) );
        jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
        jPanelStatus.add( jLabelStatus, null );
        statusMessenger = new StatusJlabel( jLabelStatus );
        statusMessenger.showStatus( geneData.selectedSets() + " sets listed." );
        BottomPanelWrap.setLayout( new BorderLayout() );
        BottomPanelWrap.add( bottomPanel, BorderLayout.NORTH );
        BottomPanelWrap.add( jPanelStatus, BorderLayout.SOUTH );

        mainPanel.add( centerPanel, BorderLayout.NORTH );
        mainPanel.add( BottomPanelWrap, BorderLayout.SOUTH );
        this.setTitle( "Find Gene Set" );
    }

    void cancelButton_actionPerformed() {
        geneData.resetSelectedSets();
        resetViews();
        dispose();
    }

    /**
     * 
     */
    protected void resetViews() {
        callingframe.getOPanel().resetView();
        callingframe.getTreePanel().resetView();
    }

    void findActionPerformed() {
        String searchOn = searchTextField.getText();
        statusMessenger.showStatus( "Searching '" + searchOn + "'" );

        if ( searchOn.equals( "" ) ) {
            geneData.resetSelectedSets();
        } else {
            geneData.selectSets( searchOn, goData );
        }

        statusMessenger.showStatus( geneData.selectedSets() + " matching gene sets found." );
        resetViews();

    }

    public void resetButton_actionPerformed( ActionEvent e ) {
        searchTextField.setText( "" );
        geneData.resetSelectedSets();
        statusMessenger.showStatus( geneData.selectedSets() + " matching gene sets found." );
        resetViews();
    }
}

class FindFieldActionAdapter implements KeyListener {
    FindDialog adaptee;

    FindFieldActionAdapter( FindDialog adaptee ) {
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed( KeyEvent e ) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased( KeyEvent e ) {
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) adaptee.findActionPerformed();
        // adaptee.findActionPerformed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped( KeyEvent e ) {
    }
}

class FindDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
    FindDialog adaptee;

    FindDialog_cancelButton_actionAdapter( FindDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.cancelButton_actionPerformed();
    }
}

class FindDialog_resetButton_actionAdapter implements java.awt.event.ActionListener {
    FindDialog adaptee;

    FindDialog_resetButton_actionAdapter( FindDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.resetButton_actionPerformed( e );
    }
}

class FindDialog_findButton_actionAdapter implements java.awt.event.ActionListener {
    FindDialog adaptee;

    FindDialog_findButton_actionAdapter( FindDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.findActionPerformed();
    }
}