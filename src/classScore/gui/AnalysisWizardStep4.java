package classScore.gui;

import java.awt.Dimension;
import java.awt.SystemColor;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import baseCode.gui.WizardStep;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin K Lee
 * @version $Id$
 */

public class AnalysisWizardStep4 extends WizardStep {
   AnalysisWizard wiz;
   Settings settings;
   JPanel step4Panel;
   JTextField jTextFieldMaxClassSize;
   JTextField jTextFieldMinClassSize;
   JRadioButton jRadioButtonBestReplicates;
   JRadioButton jRadioButtonMeanReplicates;

   public AnalysisWizardStep4( AnalysisWizard wiz, Settings settings ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      wiz.clearStatus();
      setValues();
   }

   //Component initialization
   protected void jbInit() {
      JPanel step4TopPanel = new JPanel();
      JPanel step4LeftPanel = new JPanel();
      JPanel jPanel17 = new JPanel();
      JLabel jLabel11 = new JLabel();
      jTextFieldMaxClassSize = new JTextField();
      JPanel jPanel16 = new JPanel();
      JLabel jLabel12 = new JLabel();
      jTextFieldMinClassSize = new JTextField();
      //     ButtonGroup buttonGroup2 = new ButtonGroup();
      JPanel step4RightPanel = new JPanel();
      JPanel jPanelReplicateTreaments = new JPanel();
      JLabel jLabelReplicateTreament = new JLabel();
      JList jList1 = new JList();
      ButtonGroup replicateButtonGroup = new ButtonGroup();
      jRadioButtonBestReplicates = new JRadioButton();
      jRadioButtonMeanReplicates = new JRadioButton();

      step4Panel = new JPanel();
      step4Panel.setPreferredSize( new Dimension( 550, 280 ) );
      step4LeftPanel.setPreferredSize( new Dimension( 200, 160 ) );
      jPanel17.setBackground( SystemColor.control );
      jPanel17.setPreferredSize( new Dimension( 180, 29 ) );
      jLabel11.setText( "Maximum gene set size" );
      jLabel11.setLabelFor( jTextFieldMaxClassSize );
      jTextFieldMaxClassSize.setEditable( true );
      jTextFieldMaxClassSize.setPreferredSize( new Dimension( 30, 19 ) );
      jTextFieldMaxClassSize
            .setToolTipText( "Largest gene set size to be considered" );
      jTextFieldMaxClassSize.setText( "150" );
      jTextFieldMaxClassSize.setHorizontalAlignment( SwingConstants.RIGHT );
      jPanel17.add( jLabel11, null );
      jPanel17.add( jTextFieldMaxClassSize, null );
      jPanel16.setPreferredSize( new Dimension( 180, 29 ) );
      jPanel16.setBackground( SystemColor.control );
      jLabel12.setLabelFor( jTextFieldMinClassSize );
      jLabel12.setText( "Minimum gene set size" );
      jTextFieldMinClassSize.setEditable( true );
      jTextFieldMinClassSize.setPreferredSize( new Dimension( 30, 19 ) );
      jTextFieldMinClassSize
            .setToolTipText( "Smallest gene set size to be considered" );
      jTextFieldMinClassSize.setText( "8" );
      jTextFieldMinClassSize.setHorizontalAlignment( SwingConstants.RIGHT );
      jPanel16.add( jLabel12, null );
      jPanel16.add( jTextFieldMinClassSize, null );
      step4LeftPanel.add( jPanel17, null );
      step4LeftPanel.add( jPanel16, null );
      step4RightPanel.setPreferredSize( new Dimension( 200, 160 ) );

      jLabelReplicateTreament
            .setToolTipText( "How will replicates of the same gene be treated?" );
      jLabelReplicateTreament.setText( "Gene replicate treatment" );
      jPanelReplicateTreaments.setBackground( SystemColor.control );
      jPanelReplicateTreaments.setBorder( null );
      jPanelReplicateTreaments.setPreferredSize( new Dimension( 175, 100 ) );
      jPanelReplicateTreaments
            .setToolTipText( "How will replicates of the same gene be treated? (this setting doesn't apply to correlation analysis)" );
      // todo replicate treatments for correlation tooltip.
      jRadioButtonBestReplicates.setBackground( SystemColor.control );
      jRadioButtonBestReplicates.setPreferredSize( new Dimension( 171, 23 ) );
      jRadioButtonBestReplicates
            .setToolTipText( "If a gene occurs more than once, it is counted only once and the "
                  + "score used is that of the highest-scoring occurrence." );
      jRadioButtonBestReplicates.setSelected( true );
      jRadioButtonBestReplicates.setText( "Use Best scoring replicate" );

      jRadioButtonMeanReplicates.setBackground( SystemColor.control );
      jRadioButtonMeanReplicates.setPreferredSize( new Dimension( 171, 23 ) );
      jRadioButtonMeanReplicates
            .setToolTipText( "If a gene occurs more than once, the gene is only counted once and "
                  + "the score is the mean of all occurrences." );
      jRadioButtonMeanReplicates.setSelected( false );
      jRadioButtonMeanReplicates.setText( "Use Mean of replicates" );
      replicateButtonGroup.add( jRadioButtonBestReplicates );

      replicateButtonGroup.add( jRadioButtonMeanReplicates );
      jPanelReplicateTreaments.add( jLabelReplicateTreament, null );

      jPanelReplicateTreaments.add( jRadioButtonBestReplicates, null );
      jPanelReplicateTreaments.add( jRadioButtonMeanReplicates, null );
      jPanelReplicateTreaments.add( jList1, null );

      step4RightPanel.add( jPanelReplicateTreaments, null );
      this
            .addHelp( "<html>"
                  + "<b>Choose"
                  + " the range of class sizes to be considered and how genes occuring more than once are handled.</b>" );

      step4TopPanel.add( step4LeftPanel, null );
      step4TopPanel.add( step4RightPanel, null );
      step4Panel.add( step4TopPanel, null );

      this.addMain( step4Panel );
   }

   private void setValues() {
      jTextFieldMaxClassSize.setText( String.valueOf( settings
            .getMaxClassSize() ) );
      jTextFieldMinClassSize.setText( String.valueOf( settings
            .getMinClassSize() ) );

      if ( settings.getGeneRepTreatment() == Settings.BEST_PVAL ) {
         jRadioButtonBestReplicates.setSelected( true );
      } else if ( settings.getGeneRepTreatment() == Settings.MEAN_PVAL ) {
         jRadioButtonMeanReplicates.setSelected( true );
      } else {
         throw new IllegalStateException( "Invalid gene rep treatment method" );
      }
   }

   public void saveValues() {
      settings.setMaxClassSize( Integer.valueOf(
            jTextFieldMaxClassSize.getText() ).intValue() );
      settings.setMinClassSize( Integer.valueOf(
            jTextFieldMinClassSize.getText() ).intValue() );

      if ( jRadioButtonBestReplicates.isSelected() ) {
         settings.setGeneRepTreatment( Settings.BEST_PVAL );
      } else if ( jRadioButtonMeanReplicates.isSelected() ) {
         settings.setGeneRepTreatment( Settings.MEAN_PVAL );
      } else {
         throw new IllegalStateException( "Invalid gene rep treatment method" );
      }
   }

   public boolean isReady() {
      return true;
   }
}