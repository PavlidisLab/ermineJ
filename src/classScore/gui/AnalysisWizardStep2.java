package classScore.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;


import baseCode.gui.WizardStep;
import baseCode.util.FileTools;
import classScore.Settings;
import javax.swing.*;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 *
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class AnalysisWizardStep2 extends WizardStep {
   AnalysisWizard wiz;
   Settings settings;
   JFileChooser chooser = new JFileChooser();
   JTextField rawFile;
   JTextField scoreFile;

   private JTextField jTextFieldScoreCol;

   public AnalysisWizardStep2( AnalysisWizard wiz, Settings settings ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
      setValues();
      wiz.clearStatus();
   }

   //Component initialization
   protected void jbInit() {
      JPanel step2Panel = new JPanel();
      step2Panel.setPreferredSize( new Dimension( 340, 250 ) );

      JPanel jPanel2 = new JPanel();
      //    choose the score file
      JPanel jPanel8 = new JPanel();
      jPanel8.setPreferredSize(new Dimension(380, 50) );
      JLabel jLabel2 = new JLabel();
      jLabel2.setText( "Gene score file (optional for correlation score):" );
      jLabel2.setPreferredSize( new Dimension( 370, 15 ) );
      JButton scoreBrowseButton = new JButton();
      scoreBrowseButton.setEnabled( true );
      scoreBrowseButton.setText( "Browse...." );
      scoreBrowseButton
            .addActionListener( new AnalysisWizardStep2_scoreBrowseButton_actionAdapter(
                  this ) );
      scoreFile = new JTextField();
      scoreFile.setPreferredSize( new Dimension( 280, 19 ) );
      scoreFile.setMinimumSize( new Dimension( 4, 19 ) );
      jPanel8.add( jLabel2, null );
      jPanel8.add( scoreFile, null );
      jPanel8.add( scoreBrowseButton, null );

      // choose the column the scores are in.
      JPanel jPanel1 = new JPanel();
      jPanel1.setPreferredSize(new Dimension(111, 50));
      jTextFieldScoreCol = new JTextField();
      JLabel jLabel10 = new JLabel();
      jLabel10.setMaximumSize( new Dimension( 39, 15 ) );
      jLabel10.setMinimumSize( new Dimension( 76, 15 ) );
      jLabel10.setLabelFor( jTextFieldScoreCol );
      jLabel10.setText( "Score column" );
      jTextFieldScoreCol.setHorizontalAlignment( SwingConstants.RIGHT );
      jTextFieldScoreCol.setText( "2" );
      jTextFieldScoreCol
            .setToolTipText( "Column of the gene score file containing the scores. This must be a value of 2 or higher." );
      jTextFieldScoreCol.setPreferredSize( new Dimension( 30, 19 ) );
      jTextFieldScoreCol.setEditable( true );
      JPanel jPanel23 = new JPanel();
      jPanel23.setPreferredSize(new Dimension(111, 18));
      jPanel1.add(jPanel23, null);
      jPanel1.add(jLabel10, null);
      jPanel1.add(jTextFieldScoreCol, null);

      jPanel2.add(jPanel8, null);
      jPanel2.add(jPanel1, null);

      JPanel jPanel4 = new JPanel();
      JPanel jPanel11 = new JPanel();
      jPanel11.setPreferredSize(new Dimension(380, 50) );
      JLabel jLabel3 = new JLabel();
      jLabel3.setText( "Raw data file (optional for ORA or resampling):" );
      jLabel3.setPreferredSize( new Dimension( 370, 15 ) );
      rawFile = new JTextField();
      rawFile.setPreferredSize( new Dimension( 280, 19 ) );
      rawFile.setMinimumSize( new Dimension( 4, 19 ) );
      JButton rawBrowseButton = new JButton();
      rawBrowseButton.setEnabled( true );
      rawBrowseButton
            .addActionListener( new AnalysisWizardStep2_rawBrowseButton_actionAdapter(
                  this ) );
      rawBrowseButton.setText( "Browse...." );
      jPanel11.add( jLabel3, null );
      jPanel11.add( rawFile, null );
      jPanel11.add( rawBrowseButton, null );
      JPanel jPanel3 = new JPanel();
      jPanel3.setPreferredSize(new Dimension(111, 30));

      jPanel4.add(jPanel11, null);
      jPanel4.add(jPanel3, null);

      step2Panel.add(jPanel2, null);
      step2Panel.add(jPanel4, null);

      this
            .addHelp( "<html><b>Choose the data files to use</b><br>"
                  + "&quot;Gene scores&quot; refer to a score or p value "
                  + " associated with each gene in your data set. This "
                  + "file can have as few as two columns. &quot;Raw data&quot;"
                  + " refers to the expression profile data, usually a large matrix. "
                  + "Files must be tab-delimited text. For details, see the user manual." );
      this.addMain( step2Panel );
   }

   public boolean isReady() {

      if ( wiz.getAnalysisType() == Settings.CORR
            && rawFile.getText().compareTo( "" ) == 0 ) {
         wiz.showError( "Correlation analyses require a raw data file." );
         return false;
      } else if ( ( wiz.getAnalysisType() == Settings.RESAMP || wiz
            .getAnalysisType() == Settings.ORA )
            && scoreFile.getText().compareTo( "" ) == 0 ) {
         wiz
               .showError( "ORA and resampling analyses require a gene score file." );
         return false;
      }

      if ( ( wiz.getAnalysisType() == 0 || wiz.getAnalysisType() == 1 )
            && Integer.valueOf( jTextFieldScoreCol.getText() ).intValue() < 2 ) {
         wiz.showError( "The score column must be 2 or higher" );
         // @todo test that the score column exists.
         return false;
      }

      // make sure we got at least one file that's readable.
      if ( rawFile.getText().length() != 0
            && !FileTools.testFile( rawFile.getText() ) ) {
         wiz.showError( "The raw data file is not valid." );
         return false;
      }

      if ( scoreFile.getText().length() != 0
            && !FileTools.testFile( scoreFile.getText() ) ) {
         wiz.showError( "The gene score file is not valid." );
         return false;
      }
      return true;
   }

   void rawBrowseButton_actionPerformed( ActionEvent e ) {
      chooser.setDialogTitle( "Choose Raw Data File" );
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         rawFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void scoreBrowseButton_actionPerformed( ActionEvent e ) {
      chooser.setDialogTitle( "Choose Gene Score File" );
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         scoreFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void setValues() {
      jTextFieldScoreCol.setText( String.valueOf( settings.getScorecol() ) );
      scoreFile.setText( settings.getScoreFile() );
      rawFile.setText( settings.getRawFile() );
   }

   public void saveValues() {
      settings.setScorecol( Integer.valueOf( jTextFieldScoreCol.getText() )
            .intValue() );
      settings.setScoreFile( scoreFile.getText() );
      settings.setRawFile( rawFile.getText() );
   }

}

class AnalysisWizardStep2_rawBrowseButton_actionAdapter implements
      java.awt.event.ActionListener {
   AnalysisWizardStep2 adaptee;

   AnalysisWizardStep2_rawBrowseButton_actionAdapter(
         AnalysisWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.rawBrowseButton_actionPerformed( e );
   }
}

class AnalysisWizardStep2_scoreBrowseButton_actionAdapter implements
      java.awt.event.ActionListener {
   AnalysisWizardStep2 adaptee;

   AnalysisWizardStep2_scoreBrowseButton_actionAdapter(
         AnalysisWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.scoreBrowseButton_actionPerformed( e );
   }
}
