package classScore.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;

import baseCode.gui.*;
import classScore.*;
import classScore.gui.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StartupDialog
    extends JDialog {
   JPanel mainPanel;
   JFileChooser chooser = new JFileChooser();

   //holds bottom buttons
   JPanel BottomPanel = new JPanel();
   JButton quitButton = new JButton();
   JButton startButton = new JButton();

   JPanel centerPanel = new JPanel();
   JPanel classPanel = new JPanel();
   JLabel classLabel = new JLabel();
   JTextField classFile = new JTextField();
   JButton annotBrowseButton = new JButton();
   JLabel annotLabel = new JLabel();
   JPanel annotPanel = new JPanel();
   JTextField annotFile = new JTextField();

   classScoreFrame callingframe;
   Settings settings;

   public StartupDialog( classScoreFrame callingframe ) {
      setModal( true );
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();
      try {
         jbInit();
         setValues();
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension dlgSize = getPreferredSize();
         setLocation((screenSize.width - dlgSize.width) / 2,
                           (screenSize.height - dlgSize.height) / 2);
         pack();
         startButton.requestFocusInWindow();
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

      annotBrowseButton.setEnabled( true );
      annotBrowseButton.setText( "Browse...." );
      annotBrowseButton.addActionListener( new StartupDialog_annotBrowseButton_actionAdapter( this ) );
      annotLabel.setPreferredSize( new Dimension( 320, 15 ) );
      annotLabel.setText( "Probe annotation file:" );
      annotPanel.setBackground( SystemColor.control );
      annotPanel.setPreferredSize( new Dimension( 330, 50 ) );
      annotFile.setToolTipText( "" );
      annotFile.setPreferredSize( new Dimension( 230, 19 ) );
      annotFile.setEnabled( false );
      annotFile.setMinimumSize( new Dimension( 4, 19 ) );
      annotPanel.add( annotLabel, null );
      annotPanel.add( annotFile, null );
      annotPanel.add( annotBrowseButton, null );
      centerPanel.add( classPanel, null );
      centerPanel.add( annotPanel, null );

      BottomPanel.setPreferredSize( new Dimension( 200, 40 ) );
      quitButton.setText( "Quit" );
      quitButton.addActionListener( new
                                    StartupDialog_quitButton_actionAdapter( this ) );
      startButton.setText( "Start" );
      startButton.addActionListener( new
                                      StartupDialog_startButton_actionAdapter( this ) );
      BottomPanel.add( quitButton, null );
      BottomPanel.add( startButton, null );
      mainPanel.add( BottomPanel, BorderLayout.SOUTH );

      classFile.setEditable( false );
      classPanel.setBackground( SystemColor.control );
      classPanel.setPreferredSize( new Dimension( 330, 50 ) );
      classLabel.setPreferredSize( new Dimension( 320, 15 ) );
      classLabel.setText( "Gene name file:" );
      classFile.setMinimumSize( new Dimension( 4, 19 ) );
      classFile.setEnabled( false );
      classFile.setPreferredSize( new Dimension( 315, 19 ) );
      classFile.setToolTipText( "" );
      classPanel.add( classLabel, null );
      classPanel.add( classFile, null );
      mainPanel.add( centerPanel );
      this.setTitle( "Specify Probe Annotation File" );
   }

   private void setValues() {
      classFile.setText( settings.getClassFile() );
      annotFile.setText( settings.getAnnotFile() );
      chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
   }

   private void saveValues() {
      settings.setAnnotFile( annotFile.getText() );
      try {
         settings.writePrefs();
      }
      catch ( IOException ex ) {
         GuiUtil.error(ex,"Could not write prefs: ");
      }
   }

   void annotBrowseButton_actionPerformed( ActionEvent e ) {
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         annotFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void quitButton_actionPerformed( ActionEvent e ) {
      System.exit( 0 );
   }

   void startButton_actionPerformed( ActionEvent e ) {
      saveValues();
      class runthread extends Thread {
         public runthread() {}
         public void run() {
            callingframe.initialize();
         }
      };
      Thread aFrameRunner = new runthread();
      aFrameRunner.start();
      dispose();
   }

}

class StartupDialog_annotBrowseButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   StartupDialog adaptee;

   StartupDialog_annotBrowseButton_actionAdapter( StartupDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.annotBrowseButton_actionPerformed( e );
   }
}

class StartupDialog_quitButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   StartupDialog adaptee;

   StartupDialog_quitButton_actionAdapter( StartupDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.quitButton_actionPerformed( e );
   }
}

class StartupDialog_startButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   StartupDialog adaptee;

   StartupDialog_startButton_actionAdapter( StartupDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.startButton_actionPerformed( e );
   }
}
