package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import baseCode.gui.WizardStep;
import baseCode.util.FileTools;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Kiran Keshav
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

   protected void jbInit() {
      //  initialization
      JPanel step2Panel = new JPanel();
      step2Panel.setLayout( new GridLayout( 5, 1 ) );
      //  panel 1
      JPanel jPanel1 = new JPanel();
      JLabel jLabel1 = new JLabel();
      jLabel1.setText( "Gene score file (optional for correlation score):" );
      jPanel1.add( jLabel1, null );

      // panel 2
      JPanel jPanel2 = new JPanel();
      JButton jButton1 = new JButton();
      jButton1.setEnabled( true );
      jButton1.setText( "Browse" );
      jButton1
            .addActionListener( new AnalysisWizardStep2_scoreBrowseButton_actionAdapter(
                  this ) );
      scoreFile = new JTextField();
      scoreFile.setPreferredSize( new Dimension( 325, 19 ) );
      jPanel2.add( scoreFile, null );
      jPanel2.add( jButton1, null );

      // panel 3
      JPanel jPanel3 = new JPanel();
      JLabel jLabel3 = new JLabel();
      jLabel3.setText( "Score column:" );
      jTextFieldScoreCol = new JTextField();
      jTextFieldScoreCol.setPreferredSize( new Dimension( 30, 19 ) );
      jTextFieldScoreCol.setHorizontalAlignment( SwingConstants.RIGHT ); //moves textbox text to the right
      jTextFieldScoreCol.setText( "2" );
      jTextFieldScoreCol
            .setToolTipText( "Column of the gene score file containing the scores. This must be a value of 2 or higher." );
      jTextFieldScoreCol.setEditable( true );
      JLabel jDummyLabel = new JLabel();
      jPanel3.add( jLabel3, null );
      jPanel3.add( jTextFieldScoreCol, null );

      // panel 4
      JPanel jPanel4 = new JPanel();
      JLabel jLabel4 = new JLabel();
      jLabel4.setText( "Raw data file (optional for ORA or resampling):" );
      jPanel4.add( jLabel4, null );

      // panel 5
      JPanel jPanel5 = new JPanel();
      JButton jButton4 = new JButton();
      jButton4.setEnabled( true );
      jButton4.setText( "Browse" );
      jButton4
            .addActionListener( new AnalysisWizardStep2_rawBrowseButton_actionAdapter(
                  this ) );
      rawFile = new JTextField();
      rawFile.setPreferredSize( new Dimension( 325, 19 ) );
      jPanel5.add( rawFile, null );
      jPanel5.add( jButton4, null );

      // Now create border layouts for each of the rows to get them aligned.
      JPanel jPanelA = new JPanel();
      jPanelA.setLayout( new BorderLayout() );
      jPanelA.add( jPanel1, BorderLayout.WEST );

      JPanel jPanelB = new JPanel();
      jPanelB.setLayout( new BorderLayout() );
      jPanelB.add( jPanel2, BorderLayout.WEST );
      jPanelB.add( jPanel3, BorderLayout.EAST );

      JPanel jPanelC = new JPanel();
      jPanelC.setLayout( new BorderLayout() );
      jPanelC.add( jPanel4, BorderLayout.WEST );

      JPanel jPanelD = new JPanel();
      jPanelD.setLayout( new BorderLayout() );
      jPanelD.add( jPanel5, BorderLayout.WEST );

      //MAIN PANEL = GridLayout(BorderLayouts); GridLayout is (1,6)
      step2Panel.add( jPanelA, null );
      step2Panel.add( jPanelB, null );
      step2Panel.add( jPanelC, null );
      step2Panel.add( jPanelD, null );

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
         settings.setDataFolder( chooser.getCurrentDirectory().toString() );
      }
   }

   void scoreBrowseButton_actionPerformed( ActionEvent e ) {
      chooser.setDialogTitle( "Choose Gene Score File" );
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         scoreFile.setText( chooser.getSelectedFile().toString() );
         settings.setDataFolder( chooser.getCurrentDirectory().toString() );
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
      settings.setDataFolder( chooser.getCurrentDirectory().toString() );
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