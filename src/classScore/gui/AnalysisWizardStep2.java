package classScore.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import baseCode.gui.GuiUtil;
import baseCode.gui.WizardStep;
import classScore.Settings;

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
      scoreFile = new JTextField();
      JButton scoreBrowseButton = new JButton();

      step2Panel.setPreferredSize( new Dimension( 340, 250 ) );

      jPanel11.setPreferredSize(new Dimension(380, 50) );
      jLabel3.setText( "Raw data file (optional for ORA or resampling):" );
      jLabel3.setPreferredSize(new Dimension(370, 15) );
      rawFile.setPreferredSize(new Dimension(280, 19) );
      rawFile.setMinimumSize( new Dimension( 4, 19 ) );
      rawBrowseButton.setEnabled( true );
      rawBrowseButton.addActionListener( new
                                         AnalysisWizardStep2_rawBrowseButton_actionAdapter( this ) );
      rawBrowseButton.setText( "Browse...." );
      jPanel11.add( jLabel3, null );
      jPanel11.add( rawFile, null );
      jPanel11.add( rawBrowseButton, null );
      step2Panel.add(jPanel8, null);
      jPanel8.setPreferredSize(new Dimension(380, 50) );
      jLabel2.setText( "Gene score file (optional for correlation score):" );
      jLabel2.setPreferredSize(new Dimension(370, 15) );
      scoreFile.setPreferredSize(new Dimension(280, 19) );
      scoreFile.setMinimumSize( new Dimension( 4, 19 ) );
      scoreBrowseButton.setEnabled( true );
      scoreBrowseButton.setText( "Browse...." );
      scoreBrowseButton.addActionListener( new
                                           AnalysisWizardStep2_scoreBrowseButton_actionAdapter( this ) );
      jPanel8.add( jLabel2, null );
      jPanel8.add( scoreFile, null );
      jPanel8.add( scoreBrowseButton, null );
      step2Panel.add(jPanel11, null);

      //this.add( step2Panel );
      this.addHelp("<html><b>Choose the data files to use</b><br>"+
                   "&quot;Gene scores&quot; refer to a score or p value " +
                   " associated with each gene in your data set. This " +
                   "file can have as few as two columns. &quot;Raw data&quot;" +
                   " refers to the expression profile data, usually a large matrix. " +
                   "Files must be tab-delimited text. For details, see the user manual.");
      this.addMain(step2Panel);
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
      chooser.setDialogTitle("Choose Raw Data File");
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         rawFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   void scoreBrowseButton_actionPerformed( ActionEvent e ) {
      chooser.setDialogTitle("Choose Gene Score File");
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
