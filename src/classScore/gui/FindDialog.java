package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import classScore.Settings;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Homin Lee
 * @version $Id$
 */

public class FindDialog
    extends JDialog {
   JPanel mainPanel;
   Dimension dlgSize = new Dimension(550,100);

   //holds bottom buttons
   JPanel BottomPanel = new JPanel();
   JButton cancelButton = new JButton();
   JButton findButton = new JButton();
   JPanel centerPanel = new JPanel();
   JLabel annotLabel = new JLabel();
   JPanel loadPanel = new JPanel();
   JTextField loadFile = new JTextField();

   GeneSetScoreFrame callingframe;

   public FindDialog( GeneSetScoreFrame callingframe ) {
      setModal( true );
      this.callingframe = callingframe;
      try {
         jbInit();
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         setLocation((screenSize.width - dlgSize.width) / 2,
                     (screenSize.height - dlgSize.height) / 2);
         pack();
         findButton.requestFocusInWindow();
         show();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   //Component initialization
   private void jbInit() throws Exception {
      setResizable( true );
      mainPanel = ( JPanel )this.getContentPane();
      mainPanel.setPreferredSize( new Dimension( 550, 350 ) );

      annotLabel.setPreferredSize(new Dimension(50, 15) );
      annotLabel.setRequestFocusEnabled(true);
      annotLabel.setText("Find text:" );
      loadPanel.setBackground( SystemColor.control );
      loadPanel.setPreferredSize(new Dimension(330, 30) );
      loadFile.setToolTipText( "" );
      loadFile.setPreferredSize( new Dimension( 230, 19 ) );
      loadFile.setMinimumSize( new Dimension( 4, 19 ) );
      centerPanel.setPreferredSize(new Dimension(200, 50));
      loadPanel.add( annotLabel, null );
      loadPanel.add( loadFile, null );
      centerPanel.add( loadPanel, null );

      BottomPanel.setPreferredSize( new Dimension( 200, 40 ) );
      cancelButton.setText("Cancel" );
      cancelButton.addActionListener( new
                                    FindDialog_cancelButton_actionAdapter( this ) );
      findButton.setText("Find" );
      findButton.addActionListener( new
                                    FindDialog_findButton_actionAdapter( this ) );
      BottomPanel.add( findButton, null );
      BottomPanel.add( cancelButton, null );
      mainPanel.add( BottomPanel, BorderLayout.SOUTH );

      mainPanel.add( centerPanel );
      this.setTitle( "Find Class" );
   }

   void cancelButton_actionPerformed( ActionEvent e ) {
      dispose();
   }

   void findButton_actionPerformed( ActionEvent e ) {
      System.err.println("Finding" + loadFile.getText());

      dispose();
   }

}

class FindDialog_cancelButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   FindDialog adaptee;

   FindDialog_cancelButton_actionAdapter( FindDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.cancelButton_actionPerformed( e );
   }
}

class FindDialog_findButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   FindDialog adaptee;

   FindDialog_findButton_actionAdapter( FindDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.findButton_actionPerformed( e );
   }
}
