package classScore.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import baseCode.gui.AppDialog;
import baseCode.gui.GuiUtil;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 *
 * @author Homin Lee
 * @version $Id$
 * @todo  We should probably attach a file filter to this class.  When you 
 *        browser to the names (to set the annots and the go file), it shows 
 *        "all files". Files can be filtered based on extension (or something 
 *        else).  Here's an outline of how to add file filters:<p>
 *        <code>
 *          // Subclass FileFilter and implement its 'accept' method.   <br>
 *          // Assume this subclass is called 'XMLFileFilter'           <br>
 *          XMLFileFilter fileFilter = new XMLFileFilter();             <br>
 *          chooser.setFileFilter( fileFilter ); // JFileChooser method <br>
 *          chooser.setAcceptAllFileFilterUsed( false );                <br>
 *        </code>
 */

public class StartupDialog extends AppDialog {
   JFileChooser chooser = new JFileChooser();
   JPanel centerPanel = new JPanel();
   JPanel classPanel = new JPanel();
   JLabel classLabel = new JLabel();
   JTextField classFile = new JTextField();
   JButton annotBrowseButton = new JButton();
   JLabel annotLabel = new JLabel();
   JPanel annotPanel = new JPanel();
   JTextField annotFile = new JTextField();

   // @todo we need to use these.
   JLabel askAgainLabel = new JLabel();
   JCheckBox askAgain = new JCheckBox();
   
   Settings settings;
   JButton classBrowseButton = new JButton();

   public StartupDialog( GeneSetScoreFrame callingframe ) {
      super( callingframe, 550, 350 );
      this.settings = callingframe.getSettings();
      jbInit();
      setValues();
   }

   //Component initialization
   private void jbInit() {
      this.addWindowListener( new StartupDialog_this_windowAdapter( this ) );

      annotBrowseButton.setText( "Browse..." );
      annotBrowseButton
            .addActionListener( new StartupDialog_annotBrowseButton_actionAdapter(
                  this ) );
      annotLabel.setPreferredSize( new Dimension( 390, 15 ) );
      annotLabel.setRequestFocusEnabled( true );
      annotLabel.setText( "Probe annotation file:" );
      annotPanel.setPreferredSize( new Dimension( 400, 50 ) );
      annotFile.setPreferredSize( new Dimension( 300, 19 ) );
      classBrowseButton
            .addActionListener( new StartupDialog_classBrowseButton_actionAdapter(
                  this ) );
      classBrowseButton.setText( "Browse..." );
      annotPanel.add( annotLabel, null );
      annotPanel.add( annotFile, null );
      annotPanel.add( annotBrowseButton, null );
      classPanel.setPreferredSize( new Dimension( 400, 50 ) );
      classLabel.setPreferredSize( new Dimension( 390, 15 ) );
      classLabel.setText( "GO XML file:" );
      classFile.setPreferredSize( new Dimension( 300, 19 ) );
      classPanel.add( classLabel, null );
      classPanel.add( classFile, null );
      classPanel.add( classBrowseButton, null );
      centerPanel.add( classPanel, null );
      centerPanel.add( annotPanel, null );

      setActionButtonText( "Start" );
      setCancelButtonText( "Quit" );
      addHelp( "<html><b>Starting up the program</b><br>Please confirm " +
            "the settings below are correct; they cannot be changed during " +
            "analysis.<p>The probe annotation file you select " +
            "must match the microarray design you are using. " +
            "For updated annotation files, visit " +
            "<a href=\"http://microarray.cpmc.columbia.edu/annots/\">http://microarray.cpmc.columbia.edu/annots</a></html>" );
      addMain( centerPanel );
      this.setTitle( "ErmineJ startup" );
   }

   private void setValues() {
      classFile.setText( settings.getClassFile() );
      annotFile.setText( settings.getAnnotFile() );
      chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
   }

   private void saveValues() {
      settings.setClassFile( classFile.getText() );
      settings.setAnnotFile( annotFile.getText() );
      try {
         settings.writePrefs();
      } catch ( IOException ex ) {
         GuiUtil.error( "Could not write prefs." );
      }
   }

   void annotBrowseButton_actionPerformed( ActionEvent e ) {
      chooser.setDialogTitle("Choose the annotation file:");
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         annotFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void classBrowseButton_actionPerformed( ActionEvent e ) {
      chooser.setDialogTitle("Choose the GO XML file:");
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         classFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   protected void cancelButton_actionPerformed( ActionEvent e ) {
      System.exit( 0 );
   }

   protected void actionButton_actionPerformed( ActionEvent e ) {
      String file = annotFile.getText();
      File infile = new File( file );
      if ( !infile.exists() || !infile.canRead() ) {
         GuiUtil.error( "Could not find file: " + file );
      } else {
         saveValues();
         class runthread extends Thread {
            public runthread() {
            }

            public void run() {
               ( ( GeneSetScoreFrame ) callingframe ).initialize();
            }
         }
         ;
         Thread aFrameRunner = new runthread();
         aFrameRunner.start();
         dispose();
      }
   }

   void this_windowClosed( WindowEvent e ) {
      System.exit( 0 );
   }

}

class StartupDialog_annotBrowseButton_actionAdapter implements
      java.awt.event.ActionListener {
   StartupDialog adaptee;

   StartupDialog_annotBrowseButton_actionAdapter( StartupDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.annotBrowseButton_actionPerformed( e );
   }
}

class StartupDialog_this_windowAdapter extends java.awt.event.WindowAdapter {
   StartupDialog adaptee;

   StartupDialog_this_windowAdapter( StartupDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void windowClosing( WindowEvent e ) {
      adaptee.this_windowClosed( e );
   }
}

class StartupDialog_classBrowseButton_actionAdapter implements
      java.awt.event.ActionListener {
   StartupDialog adaptee;

   StartupDialog_classBrowseButton_actionAdapter( StartupDialog adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.classBrowseButton_actionPerformed( e );
   }
}

