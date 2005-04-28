package classScore.gui.geneSet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.gui.StatusJlabel;
import baseCode.util.StatusViewer;
import classScore.Settings;

/**
 * FIXME this should extend jDialog, implement actionlistener, and be modal.
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneUrlDialog extends JFrame {
    private static final Log log = LogFactory.getLog( GeneUrlDialog.class );
    private JFrame callingframe;
    private static final int MAINWIDTH = 550;
    private JPanel mainPanel;
    private Dimension dlgSize = new Dimension( MAINWIDTH, 100 );
    private JPanel bottomPanel = new JPanel();
    private JButton cancelButton = new JButton();
    private JButton setButton = new JButton();
    private JPanel centerPanel = new JPanel();
    private JTextField urlTextField;
    private JLabel jLabelStatus = new JLabel();
    private JPanel jPanelStatus = new JPanel();
    private JPanel BottomPanelWrap = new JPanel();
    private StatusViewer statusMessenger;
    private boolean hasOld = false;
    private Settings settings;
    private final GeneSetTableModel tableModel;
    private boolean firstTime = true;

    public GeneUrlDialog( JFrame parent, Settings settings, GeneSetTableModel model ) {
        this.settings = settings;
        this.tableModel = model;
        this.callingframe = parent;
       // this.setModal( true );
        try {
            jbInit();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation( ( screenSize.width - dlgSize.width ) / 2, ( screenSize.height - dlgSize.height ) / 2 );
            pack();
            urlTextField.requestFocusInWindow();
            show();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void jbInit() {
        setResizable( true );
        mainPanel = ( JPanel ) this.getContentPane();
        mainPanel.setPreferredSize( new Dimension( MAINWIDTH, 150 ) );
        mainPanel.setLayout( new BorderLayout() );

        centerPanel.setPreferredSize( new Dimension( 200, 50 ) );

        urlTextField = new JTextField();
        urlTextField.setPreferredSize( new Dimension( 500, 19 ) );

        initField();

        urlTextField.addMouseListener( new SetTextMouseButton_actionAdapter( this ) );
        centerPanel.add( urlTextField, null );

        bottomPanel.setPreferredSize( new Dimension( 200, 40 ) );

        cancelButton.setText( "Cancel" );
        cancelButton.addActionListener( new CancelButton_actionAdapter( this ) );

        setButton.setText( "Save" );
        setButton.addActionListener( new SetButton_actionAdapter( this ) );
        bottomPanel.add( setButton, null );

        bottomPanel.add( cancelButton, null );

        // status bar
        jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
        jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        jLabelStatus.setPreferredSize( new Dimension( MAINWIDTH - 40, 19 ) );
        jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
        jPanelStatus.add( jLabelStatus, null );
        statusMessenger = new StatusJlabel( jLabelStatus );
        BottomPanelWrap.setLayout( new BorderLayout() );
        BottomPanelWrap.add( bottomPanel, BorderLayout.NORTH );
        BottomPanelWrap.add( jPanelStatus, BorderLayout.SOUTH );

        mainPanel.add( centerPanel, BorderLayout.NORTH );
        mainPanel.add( BottomPanelWrap, BorderLayout.SOUTH );
        this.setTitle( "Set the url used to create links for genes." );

    }

    /**
     * 
     */
    private void initField() {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration( "ermineJ.properties" );
            if ( config != null ) {
                String oldUrlBase = config.getString( Settings.GENE_URL_BASE );
                log.debug("Found url base " + oldUrlBase);
                urlTextField.setText( oldUrlBase );
                hasOld = true;
            }

        } catch ( org.apache.commons.configuration.ConfigurationException e ) {
            urlTextField.setText( "Click here and type a URL containing '@@' where the gene name will go." );
            hasOld = false;
        }
    }

    void cancelButton_actionPerformed() {
        log.debug("got cancel");
        dispose();
    }

    void setActionPerformed() {
        String candidateUrlBase = urlTextField.getText();

        if ( candidateUrlBase.length() == 0) {
            statusMessenger.setError( "URL must not be blank." );
            return;
        }
        
        if ( candidateUrlBase.indexOf( "@@" ) < 0 ) {
            statusMessenger.setError( "URL must contain the string '@@' for substitution with the gene name" );
            return;
        }

        if ( candidateUrlBase.indexOf( " " ) > 0 ) {
            statusMessenger.setError( "URL should not contain spaces" );
            return;
        }

        settings.getConfig().setProperty( Settings.GENE_URL_BASE, candidateUrlBase );
//        try {
//            settings.getConfig().save();
//            log.debug("Saved configuration");
//        } catch ( ConfigurationException e ) {
//            e.printStackTrace();
//        }
        tableModel.configure();
        dispose();
    }

    /**
     * 
     */
    public void urlMouseActionAdapter() {
        if ( !hasOld && firstTime ) this.urlTextField.setText( "" );
        firstTime = false;
    }

}

class SetButton_actionAdapter implements java.awt.event.ActionListener {
    GeneUrlDialog adaptee;

    SetButton_actionAdapter( GeneUrlDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.setActionPerformed();
    }
}

class CancelButton_actionAdapter implements java.awt.event.ActionListener {
    GeneUrlDialog adaptee;

    CancelButton_actionAdapter( GeneUrlDialog adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.cancelButton_actionPerformed();
    }
}

class SetTextMouseButton_actionAdapter implements MouseListener {

    GeneUrlDialog adaptee;

    /**
     * @param adaptee
     */
    public SetTextMouseButton_actionAdapter( GeneUrlDialog adaptee ) {
        super();
        // TODO Auto-generated constructor stub
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased( MouseEvent e ) {
        adaptee.urlMouseActionAdapter();
    }

}