package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import baseCode.gui.WizardStep;
import classScore.Settings;
import javax.swing.JCheckBox;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Homin Lee
 * @version $Id$
 */

public class AnalysisWizardStep5 extends WizardStep {
   AnalysisWizard wiz;
   Settings settings;
   JPanel step5Panel;
   JRadioButton jRadioButtonMedian;
   JRadioButton jRadioButtonMean;
   JTextField jTextFieldPValueThreshold;
   //  JTextField jTextFieldScoreCol;
   JTextField jTextFieldIterations;
   JCheckBox jCheckBoxDoLog;
   JCheckBox jCheckBoxBigIsBetter;
   JPanel oraPanel;
   JPanel resampPanel;
   JPanel corrPanel;
   JPanel jPanel13;
   JPanel jPanel16;
   JPanel subPanel;

   JCheckBox jCheckBoxUseEmpirical;

   public AnalysisWizardStep5( AnalysisWizard wiz, Settings settings ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      wiz.clearStatus();
      setValues();
   }

   //Component initialization
   protected void jbInit() {
      JPanel step4TopPanel = new JPanel();
      JPanel jPanelAnalysisFrameMethods = new JPanel();
      JLabel jLabelAnalysisFrameMethod = new JLabel();
      ButtonGroup buttonGroup2 = new ButtonGroup();
      jRadioButtonMedian = new JRadioButton();
      jRadioButtonMean = new JRadioButton();

      oraPanel = new JPanel();
      TitledBorder oraTitledBorder;
      JPanel jPanel15 = new JPanel();
      JLabel jLabel6 = new JLabel();
      jTextFieldPValueThreshold = new JTextField();
      resampPanel = new JPanel();
      TitledBorder resampTitledBorder;

      jPanel13 = new JPanel();
      JLabel jLabel13 = new JLabel();
      jTextFieldIterations = new JTextField();
      corrPanel = new JPanel();
      TitledBorder corrTitledBorder;
      JPanel corrMetricPanel = new JPanel();
      ButtonGroup corrButtonGroup = new ButtonGroup();
      JLabel corrMetricLabel = new JLabel();
      JRadioButton corrRadioButton1 = new JRadioButton();
      JRadioButton corrRadioButton2 = new JRadioButton();
      jCheckBoxDoLog = new JCheckBox();

      jCheckBoxBigIsBetter = new JCheckBox();

      step5Panel = new JPanel();
      step5Panel.setPreferredSize( new Dimension( 550, 280 ) );

      jCheckBoxDoLog.setBackground( SystemColor.control );
      jCheckBoxDoLog
            .setToolTipText( "If you are loading raw p values, you should check this box." );
      jCheckBoxDoLog.setSelected( true );
      jCheckBoxDoLog.setText( "Take the negative log of the gene scores" );

      jCheckBoxBigIsBetter
            .setToolTipText( "If you are loading raw p values, you should not check this box." );
      jCheckBoxBigIsBetter.setSelected( false );
      jCheckBoxBigIsBetter
            .setText( "Larger scores in your gene score file are better." );

      //oraPanel stuff//////////////////////////////////////////////////////////
      oraPanel.setPreferredSize( new Dimension( 335, 150 ) );
      oraTitledBorder = new TitledBorder( "ORA" );
      oraPanel.setBorder( oraTitledBorder );
      jPanelAnalysisFrameMethods.setBackground( SystemColor.control );
      jPanelAnalysisFrameMethods.setBorder( null );
      jPanelAnalysisFrameMethods.setMinimumSize( new Dimension( 150, 37 ) );
      jPanelAnalysisFrameMethods.setPreferredSize( new Dimension( 150, 45 ) );
      jLabelAnalysisFrameMethod.setMaximumSize( new Dimension( 167, 18 ) );
      jLabelAnalysisFrameMethod.setMinimumSize( new Dimension( 167, 18 ) );
      jLabelAnalysisFrameMethod
            .setToolTipText( "Determines how the gene scores are combined to make a class score." );
      jLabelAnalysisFrameMethod.setText( "Class Raw Score Method" );
      jRadioButtonMedian.setText( "Median" );
      jRadioButtonMedian
            .setToolTipText( "The score for a class is the median of the score of genes in the "
                  + "class." );
      jRadioButtonMedian.setBackground( SystemColor.control );
      jRadioButtonMean.setBackground( SystemColor.control );
      jRadioButtonMean
            .setToolTipText( "The raw score for the class is the mean of the scores for genes in "
                  + "the class" );
      jRadioButtonMean.setSelected( true );
      jRadioButtonMean.setText( "Mean" );
      buttonGroup2.add( jRadioButtonMean );
      buttonGroup2.add( jRadioButtonMedian );
      jPanelAnalysisFrameMethods.add( jLabelAnalysisFrameMethod, null );
      jPanelAnalysisFrameMethods.add( jRadioButtonMean, null );
      jPanelAnalysisFrameMethods.add( jRadioButtonMedian, null );
      step5Panel.add( step4TopPanel, null );
      jPanel15.setMinimumSize( new Dimension( 180, 29 ) );
      jPanel15.setBackground( SystemColor.control );
      jLabel6.setLabelFor( jTextFieldPValueThreshold );
      jLabel6.setText( "Gene score threshold" );
      jTextFieldPValueThreshold.setEditable( true );
      jTextFieldPValueThreshold.setPreferredSize( new Dimension( 50, 19 ) );
      jTextFieldPValueThreshold
            .setToolTipText( "Score Threshold used for Over-Representation analysis" );
      jTextFieldPValueThreshold.setText( "0.001" );
      jTextFieldPValueThreshold.setHorizontalAlignment( SwingConstants.RIGHT );
      jPanel15.add( jLabel6, null );
      jPanel15.add( jTextFieldPValueThreshold, null );
      resampPanel.add( jPanelAnalysisFrameMethods, null );
      oraPanel.add( jPanel15, null );

      //resampPanel stuff///////////////////////////////////////////////////////
      resampPanel.setPreferredSize( new Dimension( 380, 200 ) );
      resampTitledBorder = new TitledBorder( "Resampling" );
      resampPanel.setBorder( resampTitledBorder );

      subPanel = new JPanel();
      subPanel.setLayout( new FlowLayout() );

      jPanel13.setBorder( null );
      jLabel13.setMaximumSize( new Dimension( 100, 15 ) );
      jLabel13.setLabelFor( jTextFieldIterations );
      jLabel13.setText( "Maximum iterations to run" );
      jTextFieldIterations.setHorizontalAlignment( SwingConstants.RIGHT );
      jTextFieldIterations.setText( "10000" );
      jTextFieldIterations
            .setToolTipText( "Maximum number of iterations run per gene set size." );
      jTextFieldIterations.setPreferredSize( new Dimension( 70, 19 ) );
      jTextFieldIterations.setEditable( true );
      jPanel13.add( jLabel13, null );
      jPanel13.add( jTextFieldIterations, null );

      jPanel16 = new JPanel();
      jCheckBoxUseEmpirical = new JCheckBox();
      JLabel jLabel14 = new JLabel();
      jLabel14.setLabelFor( jCheckBoxUseEmpirical );
      jCheckBoxUseEmpirical.setSelected( false );
      jCheckBoxUseEmpirical.setHorizontalAlignment( SwingConstants.RIGHT );
      jLabel14.setText( "Always use full resampling (slower)" );
      jCheckBoxUseEmpirical.setToolTipText( "If this box is unchecked, "
            + "some approximations are used which can"
            + " dramatically speed up the resampling,"
            + " at a possible risk of reduced accuracy" );
      jPanel16.add( jLabel14, null );
      jPanel16.add( jCheckBoxUseEmpirical, null );
      subPanel.setPreferredSize( new java.awt.Dimension( 340, 80 ) );

      subPanel.add( jPanel13, BorderLayout.WEST );
      subPanel.add( jPanel16, BorderLayout.EAST );

      //corrPanel stuff/////////////////////////////////////////////////////////
      corrPanel.setPreferredSize( new java.awt.Dimension( 380, 150 ) );
      corrTitledBorder = new TitledBorder( "Correlation" );
      corrPanel.setBorder( corrTitledBorder );
      corrMetricPanel.setPreferredSize( new Dimension( 150, 50 ) );
      corrMetricPanel.setMinimumSize( new Dimension( 150, 37 ) );
      corrMetricPanel.setBorder( null );
      corrMetricPanel.setBackground( SystemColor.control );
      corrMetricPanel.setBackground( SystemColor.control );
      corrMetricPanel.setToolTipText( "metric tool tip." );
      corrMetricLabel.setText( "Correlation Metric" );
      corrMetricLabel.setToolTipText( "metric tool tip." );
      corrMetricLabel.setMinimumSize( new Dimension( 167, 18 ) );
      corrMetricLabel.setMaximumSize( new Dimension( 167, 18 ) );
      corrRadioButton1.setText( "Metric 1" );
      corrRadioButton1.setSelected( true );
      corrRadioButton1.setBackground( SystemColor.control );
      corrRadioButton1.setToolTipText( "metric 1 tool tip" );
      corrRadioButton2.setText( "Metric 2" );
      jPanel16.setPreferredSize( new java.awt.Dimension( 330, 30 ) );
      jPanel13.setPreferredSize( new java.awt.Dimension( 234, 30 ) );
      corrButtonGroup.add( corrRadioButton1 );
      corrButtonGroup.add( corrRadioButton2 );
      corrMetricPanel.add( corrMetricLabel, null );
      corrMetricPanel.add( corrRadioButton1, null );
      corrMetricPanel.add( corrRadioButton2, null );
      //   corrPanel.add(corrMetricPanel, null); // @todo disabled because there is no choice of metric.

      this
            .addHelp( "<html><b>Adjust settings specific for your analysis method.</b><br>"
                  + " " );
      this.addMain( step5Panel );
   }

   public void addVarPanel( int analysisType ) {
      if ( analysisType == Settings.ORA ) {
         oraPanel.add( jCheckBoxDoLog, null );
         oraPanel.add( jCheckBoxBigIsBetter, null );
         step5Panel.add( oraPanel, null );
      } else if ( analysisType == Settings.RESAMP ) {
         resampPanel.add( jCheckBoxDoLog, null );
         resampPanel.add(jCheckBoxBigIsBetter, null);
         resampPanel.add( subPanel, null );

         step5Panel.add( resampPanel, null );
      } else if ( analysisType == Settings.CORR ) {
         corrPanel.add( subPanel, null );

         step5Panel.add( corrPanel, null );
      }
   }

   public void removeVarPanel( int analysisType ) {
      if ( analysisType == Settings.ORA ) {
         step5Panel.remove( oraPanel );
      } else if ( analysisType == Settings.RESAMP ) {
         resampPanel.remove( subPanel );
         step5Panel.remove( resampPanel );
      } else if ( analysisType == Settings.CORR ) {
         corrPanel.remove( subPanel );
         step5Panel.remove( corrPanel );
      }
   }

   private void setValues() {
      jTextFieldIterations.setText( String.valueOf( settings.getIterations() ) );

      if ( settings.getRawScoreMethod() == Settings.MEAN_METHOD ) {
         jRadioButtonMean.setSelected( true );
      } else {
         jRadioButtonMedian.setSelected( true );
      }

      jTextFieldPValueThreshold.setText( String.valueOf( settings
            .getPValThreshold() ) );
      jCheckBoxDoLog.setSelected( settings.getDoLog() );
      jCheckBoxBigIsBetter.setSelected( settings.getBigIsBetter() );
      jCheckBoxUseEmpirical.setSelected( settings.getAlwaysUseEmpirical() );
   }

   public void saveValues() {
      settings.setIterations( Integer.valueOf( jTextFieldIterations.getText() )
            .intValue() );

      if ( jRadioButtonMean.isSelected() ) {
         settings.setRawScoreMethod( Settings.MEAN_METHOD );
      } else {
         settings.setRawScoreMethod( Settings.QUANTILE_METHOD );
      }

      settings.setPValThreshold( Double.valueOf(
            jTextFieldPValueThreshold.getText() ).doubleValue() );
      settings.setDoLog( jCheckBoxDoLog.isSelected() );
      settings.setBigIsBetter( jCheckBoxBigIsBetter.isSelected() );
      settings.setAlwaysUseEmpirical( jCheckBoxUseEmpirical.isSelected() );
   }

   public boolean isReady() {
      return true;
   }
}