package classScore.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import baseCode.gui.*;
import classScore.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 */

public class AnalysisWizardStep2
    extends WizardStep {
   AnalysisWizard wiz;
   Settings settings;
   JFileChooser chooser = new JFileChooser();

   JTextField rawFile;
   JTextField scoreFile;

   public AnalysisWizardStep2( AnalysisWizard wiz, Settings settings ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
      setValues();
   }

   //Component initialization
   protected void jbInit() {
      JPanel step2Panel = new JPanel();
      JPanel jPanel11 = new JPanel();
      JLabel jLabel3 = new JLabel();
      rawFile = new JTextField();
      JButton rawBrowseButton = new JButton();
      JPanel jPanel8 = new JPanel();
      JLabel jLabel2 = new JLabel();
      scoreFile = new JTextField();
      JButton scoreBrowseButton = new JButton();

      step2Panel.setPreferredSize( new Dimension( 340, 250 ) );

      jPanel11.setPreferredSize( new Dimension( 330, 50 ) );
      jPanel11.setBackground( SystemColor.control );
      jLabel3.setText( "Raw data file (optional for ORA or resampling):" );
      jLabel3.setPreferredSize( new Dimension( 320, 15 ) );
      rawFile.setToolTipText( "" );
      rawFile.setPreferredSize( new Dimension( 230, 19 ) );
      rawFile.setMinimumSize( new Dimension( 4, 19 ) );
      rawFile.setEnabled( false );
      rawBrowseButton.setEnabled( true );
      rawBrowseButton.addActionListener( new
                                         AnalysisWizardStep2_rawBrowseButton_actionAdapter( this ) );
      rawBrowseButton.setText( "Browse...." );
      jPanel11.add( jLabel3, null );
      jPanel11.add( rawFile, null );
      jPanel11.add( rawBrowseButton, null );
      jPanel8.setPreferredSize( new Dimension( 330, 50 ) );
      jPanel8.setBackground( SystemColor.control );
      jLabel2.setText( "Gene score file (optional for correlation score):" );
      jLabel2.setPreferredSize( new Dimension( 320, 15 ) );
      scoreFile.setToolTipText( "" );
      scoreFile.setEnabled( false );
      scoreFile.setPreferredSize( new Dimension( 230, 19 ) );
      scoreFile.setMinimumSize( new Dimension( 4, 19 ) );
      scoreBrowseButton.setEnabled( true );
      scoreBrowseButton.setText( "Browse...." );
      scoreBrowseButton.addActionListener( new
                                           AnalysisWizardStep2_scoreBrowseButton_actionAdapter( this ) );
      jPanel8.add( jLabel2, null );
      jPanel8.add( scoreFile, null );
      jPanel8.add( scoreBrowseButton, null );
      step2Panel.add( jPanel11, null );
      step2Panel.add( jPanel8, null );

      this.add( step2Panel );
   }

   public boolean isReady() {
      if ( wiz.getAnalysisType() == 2 && rawFile.getText().compareTo( "" ) == 0 ) {
         GuiUtil.error( "Correlation analyses require a raw data file." );
         return false;
      } else if ( ( wiz.getAnalysisType() == 0 || wiz.getAnalysisType() == 1 ) &&
                  scoreFile.getText().compareTo( "" ) == 0 ) {
         GuiUtil.error( "ORA and resampling analyses require a raw data file." );
         return false;
      }
      else
         return true;
   }

   void rawBrowseButton_actionPerformed( ActionEvent e ) {
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         rawFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void scoreBrowseButton_actionPerformed( ActionEvent e ) {
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         scoreFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void setValues() {
      scoreFile.setText( settings.getScoreFile() );
      rawFile.setText( settings.getRawFile() );
   }

   public void saveValues(){
      settings.setScoreFile(scoreFile.getText());
      settings.setRawFile(rawFile.getText());
   }

}

class AnalysisWizardStep2_rawBrowseButton_actionAdapter
    implements java.awt.event.ActionListener {
   AnalysisWizardStep2 adaptee;

   AnalysisWizardStep2_rawBrowseButton_actionAdapter( AnalysisWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.rawBrowseButton_actionPerformed( e );
   }
}

class AnalysisWizardStep2_scoreBrowseButton_actionAdapter
    implements java.awt.event.
    ActionListener {
   AnalysisWizardStep2 adaptee;

   AnalysisWizardStep2_scoreBrowseButton_actionAdapter( AnalysisWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.scoreBrowseButton_actionPerformed( e );
   }
}