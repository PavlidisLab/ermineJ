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
import baseCode.gui.GuiUtil;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Homin Lee
 * @version $Id$
 */

public class LoadDialog
    extends JDialog {
   JPanel mainPanel;
   JFileChooser chooser = new JFileChooser();

   //holds bottom buttons
   JPanel BottomPanel = new JPanel();
   JButton cancelButton = new JButton();
   JButton loadButton = new JButton();

   JPanel centerPanel = new JPanel();
   JButton loadBrowseButton = new JButton();
   JLabel annotLabel = new JLabel();
   JPanel loadPanel = new JPanel();
   JTextField loadFile = new JTextField();

   GeneSetScoreFrame callingframe;
   Settings settings;
   GeneSetScoreStatus messenger;

   public LoadDialog( GeneSetScoreFrame callingframe ) {
      setModal( true );
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();
      chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
      chooser.setDialogTitle("Open Saved Analysis");
      try {
         jbInit();
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension dlgSize = getPreferredSize();
         setLocation((screenSize.width - dlgSize.width) / 2,
                     (screenSize.height - dlgSize.height) / 2);
         pack();
         loadButton.requestFocusInWindow();
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
      mainPanel.setPreferredSize( new Dimension( 400, 150 ) );

      loadBrowseButton.setEnabled( true );
      loadBrowseButton.setText( "Browse...." );
      loadBrowseButton.addActionListener( new LoadDialog_loadBrowseButton_actionAdapter( this ) );
      annotLabel.setPreferredSize( new Dimension( 320, 15 ) );
      annotLabel.setText("Load file:" );
      loadPanel.setBackground( SystemColor.control );
      loadPanel.setPreferredSize( new Dimension( 330, 50 ) );
      loadFile.setPreferredSize( new Dimension( 230, 19 ) );
      loadPanel.add( annotLabel, null );
      loadPanel.add( loadFile, null );
      loadPanel.add( loadBrowseButton, null );
      centerPanel.add( loadPanel, null );

      BottomPanel.setPreferredSize( new Dimension( 200, 40 ) );
      cancelButton.setText("Cancel" );
      cancelButton.addActionListener( new
                                    LoadDialog_cancelButton_actionAdapter( this ) );
      loadButton.setText("Load" );
      loadButton.addActionListener( new
                                      LoadDialog_loadButton_actionAdapter( this ) );
      BottomPanel.add( cancelButton, null );
      BottomPanel.add( loadButton, null );
      mainPanel.add( BottomPanel, BorderLayout.SOUTH );

      mainPanel.add( centerPanel );
      this.setTitle( "Load Results from File" );
   }

   void loadBrowseButton_actionPerformed( ActionEvent e ) {
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         loadFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void cancelButton_actionPerformed( ActionEvent e ) {
      dispose();
   }

   void loadButton_actionPerformed( ActionEvent e ) {
      if(GuiUtil.testfile(loadFile.getText()))
      {
         callingframe.loadAnalysis(loadFile.getText());
         dispose();
      }
   }

}

class LoadDialog_loadBrowseButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   LoadDialog adaptee;

   LoadDialog_loadBrowseButton_actionAdapter( LoadDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.loadBrowseButton_actionPerformed( e );
   }
}

class LoadDialog_cancelButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   LoadDialog adaptee;

   LoadDialog_cancelButton_actionAdapter( LoadDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.cancelButton_actionPerformed( e );
   }
}

class LoadDialog_loadButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   LoadDialog adaptee;

   LoadDialog_loadButton_actionAdapter( LoadDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.loadButton_actionPerformed( e );
   }
}
