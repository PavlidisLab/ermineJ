package classScore.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import baseCode.gui.GuiUtil;
import baseCode.gui.WizardStep;
import baseCode.util.FileTools;
import classScore.Settings;

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
      JPanel jPanel11 = new JPanel();
      JLabel jLabel3 = new JLabel();
      rawFile = new JTextField();
      JButton rawBrowseButton = new JButton();
      JPanel jPanel8 = new JPanel();
      JLabel jLabel2 = new JLabel();
      JLabel jLabel10 = new JLabel();
      scoreFile = new JTextField();
      JButton scoreBrowseButton = new JButton();

      step2Panel.setPreferredSize( new Dimension( 340, 250 ) );

      jPanel11.setPreferredSize( new Dimension( 380, 50 ) );
      jLabel3.setText( "Raw data file (optional for ORA or resampling):" );
      jLabel3.setPreferredSize( new Dimension( 370, 15 ) );
      rawFile.setPreferredSize( new Dimension( 280, 19 ) );
      rawFile.setMinimumSize( new Dimension( 4, 19 ) );
      rawBrowseButton.setEnabled( true );
      rawBrowseButton
            .addActionListener( new AnalysisWizardStep2_rawBrowseButton_actionAdapter(
                  this ) );
      rawBrowseButton.setText( "Browse...." );
      jPanel11.add( jLabel3, null );
      jPanel11.add( rawFile, null );
      jPanel11.add( rawBrowseButton, null );
      step2Panel.add( jPanel8, null );
      jPanel8.setPreferredSize( new Dimension( 490, 50 ) );
      jLabel2.setText( "Gene score file (optional for correlation score):" );
      jLabel2.setPreferredSize( new Dimension( 370, 15 ) );
      scoreFile.setPreferredSize( new Dimension( 280, 19 ) );
      scoreFile.setMinimumSize( new Dimension( 4, 19 ) );

      // choose the column the scores are in.
      jTextFieldScoreCol = new JTextField();
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

      scoreBrowseButton.setEnabled( true );
      scoreBrowseButton.setText( "Browse...." );
      scoreBrowseButton
            .addActionListener( new AnalysisWizardStep2_scoreBrowseButton_actionAdapter(
                  this ) );
      jPanel8.add( jLabel2, null );
      jPanel8.add( scoreFile, null );
      jPanel8.add( scoreBrowseButton, null );
      jPanel8.add( jLabel10, null );
      jPanel8.add( jTextFieldScoreCol, null );

      step2Panel.add( jPanel11, null );

      //this.add( step2Panel );
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