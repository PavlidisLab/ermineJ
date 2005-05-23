package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.StatusJlabel;
import baseCode.util.StatusViewer;

/**
 * FIXME this nominally extends JDialog but it isn't set up right.
 * <hr>
 * <p>
 * Copyright (c) 2003-2005 Columbia University
 * 
 * @author Homin Lee
 * @author pavlidis
 * @version $Id$
 */
public class FindDialog extends JDialog {
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
            show();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    // Component initialization
    private void jbInit() throws Exception {
        setResizable( true );
        mainPanel = ( JPanel ) this.getContentPane();
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
        statusMessenger.setStatus( geneData.selectedSets() + " sets listed." );
        BottomPanelWrap.setLayout( new BorderLayout() );
        BottomPanelWrap.add( bottomPanel, BorderLayout.NORTH );
        BottomPanelWrap.add( jPanelStatus, BorderLayout.SOUTH );

        mainPanel.add( centerPanel, BorderLayout.NORTH );
        mainPanel.add( BottomPanelWrap, BorderLayout.SOUTH );
        this.setTitle( "Find Gene Set" );
    }

    void cancelButton_actionPerformed( ActionEvent e ) {
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
        statusMessenger.setStatus( "Searching '" + searchOn + "'" );

        if ( searchOn.equals( "" ) ) {
            geneData.resetSelectedSets();
        } else {
            geneData.selectSets( searchOn, goData );
        }

        statusMessenger.setStatus( geneData.selectedSets() + " matching gene sets found." );
        resetViews();

    }

    public void resetButton_actionPerformed( ActionEvent e ) {
        searchTextField.setText( "" );
        geneData.resetSelectedSets();
        statusMessenger.setStatus( geneData.selectedSets() + " matching gene sets found." );
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
        adaptee.cancelButton_actionPerformed( e );
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