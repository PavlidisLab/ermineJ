package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import baseCode.gui.StatusJlabel;
import baseCode.util.StatusViewer;

import classScore.data.GONames;
import classScore.data.GeneAnnotations;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Homin Lee
 * @author Paul Pavlidis
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
   private JTextField searchTextField;
   private JLabel jLabelStatus = new JLabel();
   private JPanel jPanelStatus = new JPanel();
   private JPanel BottomPanelWrap = new JPanel();
   private GeneSetScoreFrame callingframe;
   private GeneAnnotations geneData;
   private StatusViewer statusMessenger;
   private JButton resetButton;
   private GONames goData;

   public FindDialog( GeneSetScoreFrame callingframe, GeneAnnotations geneData, GONames goData ) {
      setModal( true );
      this.callingframe = callingframe;
      this.geneData = geneData;
      this.goData = goData;

      try {
         jbInit();
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         setLocation( ( screenSize.width - dlgSize.width ) / 2,
               ( screenSize.height - dlgSize.height ) / 2 );
         pack();
         findButton.requestFocusInWindow();
         show();
      } catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   //Component initialization
   private void jbInit() throws Exception {
      setResizable( true );
      mainPanel = ( JPanel ) this.getContentPane();
      mainPanel.setPreferredSize( new Dimension( MAINWIDTH, 150 ) );
      mainPanel.setLayout( new BorderLayout() );

      centerPanel.setPreferredSize( new Dimension( 200, 50 ) );
      
      searchTextField = new JTextField();
      searchTextField.setPreferredSize( new Dimension( 80, 19 ) );
      
      centerPanel.add(searchTextField, null);
      
      bottomPanel.setPreferredSize( new Dimension( 200, 40 ) );

      resetButton = new JButton();
      resetButton.setText( "Reset" );
      resetButton.addActionListener( new FindDialog_resetButton_actionAdapter(
            this ) );

      cancelButton.setText( "Close this window" );
      cancelButton
            .addActionListener( new FindDialog_cancelButton_actionAdapter( this ) );

      findButton.setText( "Find" );
      findButton.addActionListener( new FindDialog_findButton_actionAdapter(
            this ) );
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

      mainPanel.add(centerPanel, BorderLayout.NORTH);
      mainPanel.add( BottomPanelWrap, BorderLayout.SOUTH );
      this.setTitle( "Find Class" );
   }

   void cancelButton_actionPerformed( ActionEvent e ) {
      dispose();
   }

   void findButton_actionPerformed( ActionEvent e ) {
      String searchOn = searchTextField.getText();
      statusMessenger.setStatus("Searching '" + searchOn + "'");
      
      if ( searchOn.equals( "" ) ) {
         geneData.resetSelectedSets();
      } else {
         geneData.selectSets( searchOn, goData );
      }
      
      statusMessenger.setStatus( geneData.selectedSets()
            + " matching gene sets found." );
      
      callingframe.getOPanel().resetTable( );

   }

   public void resetButton_actionPerformed( ActionEvent e ) {
      searchTextField.setText("");
      geneData.resetSelectedSets();
      
      statusMessenger.setStatus( geneData.selectedSets()
            + " matching gene sets found." );
      
      callingframe.getOPanel().resetTable( );
   }
}

class FindDialog_cancelButton_actionAdapter implements
      java.awt.event.ActionListener {
   FindDialog adaptee;

   FindDialog_cancelButton_actionAdapter( FindDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.cancelButton_actionPerformed( e );
   }
}

class FindDialog_resetButton_actionAdapter implements
      java.awt.event.ActionListener {
   FindDialog adaptee;

   FindDialog_resetButton_actionAdapter( FindDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.resetButton_actionPerformed( e );
   }
}

class FindDialog_findButton_actionAdapter implements
      java.awt.event.ActionListener {
   FindDialog adaptee;

   FindDialog_findButton_actionAdapter( FindDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.findButton_actionPerformed( e );
   }
}