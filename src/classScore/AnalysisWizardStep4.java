package classScore;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 */

public class AnalysisWizardStep4 extends WizardStep
{
   AnalysisWizard wiz;
   Settings settings;
   JPanel step4Panel;
   JTextField jTextFieldMaxClassSize;
   JTextField jTextFieldMinClassSize;
   JCheckBox jCheckBoxDoLog;
   JRadioButton jRadioButtonSeparateReplicates;
   JRadioButton jRadioButtonBestReplicates;
   JRadioButton jRadioButtonMeanReplicates;
   JRadioButton jRadioButtonMedian;
   JRadioButton jRadioButtonMean;
   JTextField jTextFieldPValueThreshold;
   JTextField jTextFieldScoreCol;
   JTextField jTextFieldIterations;
   JPanel oraPanel;
   JPanel resampPanel;
   JPanel corrPanel;
   JPanel jPanel13;

   public AnalysisWizardStep4(AnalysisWizard wiz, Settings settings)
   {
      super(wiz);
      this.wiz=wiz;
      this.settings=settings;
      setValues();
   }

   //Component initialization
   void jbInit(){
      JPanel step4TopPanel = new JPanel();
      JPanel step4LeftPanel = new JPanel();
      JPanel jPanel17 = new JPanel();
      JLabel jLabel11 = new JLabel();
      jTextFieldMaxClassSize = new JTextField();
      JPanel jPanel16 = new JPanel();
      JLabel jLabel12 = new JLabel();
      jTextFieldMinClassSize = new JTextField();
      jCheckBoxDoLog = new JCheckBox();
      JPanel jPanelAnalysisFrameMethods = new JPanel();
      JLabel jLabelAnalysisFrameMethod = new JLabel();
      ButtonGroup buttonGroup2 = new ButtonGroup();
      jRadioButtonMedian = new JRadioButton();
      jRadioButtonMean = new JRadioButton();
      JPanel step4RightPanel = new JPanel();
      JPanel jPanelReplicateTreaments = new JPanel();
      JLabel jLabelReplicateTreament = new JLabel();
      JList jList1 = new JList();
      ButtonGroup replicateButtonGroup = new ButtonGroup();
      jRadioButtonBestReplicates = new JRadioButton();
      jRadioButtonSeparateReplicates = new JRadioButton();
      jRadioButtonMeanReplicates = new JRadioButton();
      //panels for step 4 bottom
      oraPanel = new JPanel();
      TitledBorder oraTitledBorder;
      JPanel jPanel15 = new JPanel();
      JLabel jLabel6 = new JLabel();
      jTextFieldPValueThreshold = new JTextField();
      resampPanel = new JPanel();
      TitledBorder resampTitledBorder;
      JPanel jPanel14 = new JPanel();
      JLabel jLabel10 = new JLabel();
      jTextFieldScoreCol = new JTextField();
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

      step4Panel = new JPanel();
      step4Panel.setPreferredSize(new Dimension(550, 280));
      step4LeftPanel.setPreferredSize(new Dimension(200, 160));
      jPanel17.setBackground(SystemColor.control);
      jPanel17.setPreferredSize(new Dimension(180, 29));
      jLabel11.setText("Maximum class size");
      jLabel11.setLabelFor(jTextFieldMaxClassSize);
      jTextFieldMaxClassSize.setEditable(true);
      jTextFieldMaxClassSize.setPreferredSize(new Dimension(30, 19));
      jTextFieldMaxClassSize.setToolTipText(
              "Largest class size to be considered");
      jTextFieldMaxClassSize.setText("150");
      jTextFieldMaxClassSize.setHorizontalAlignment(SwingConstants.RIGHT);
      jPanel17.add(jLabel11, null);
      jPanel17.add(jTextFieldMaxClassSize, null);
      step4LeftPanel.add(jCheckBoxDoLog, null);
      step4LeftPanel.add(jPanelAnalysisFrameMethods, null);
      jPanel16.setPreferredSize(new Dimension(180, 29));
      jPanel16.setBackground(SystemColor.control);
      jLabel12.setLabelFor(jTextFieldMinClassSize);
      jLabel12.setText("Minimum class size");
      jTextFieldMinClassSize.setEditable(true);
      jTextFieldMinClassSize.setPreferredSize(new Dimension(30, 19));
      jTextFieldMinClassSize.setToolTipText(
              "Smallest class size to be considered");
      jTextFieldMinClassSize.setText("8");
      jTextFieldMinClassSize.setHorizontalAlignment(SwingConstants.RIGHT);
      jPanel16.add(jLabel12, null);
      jPanel16.add(jTextFieldMinClassSize, null);
      step4LeftPanel.add(jPanel17, null);
      jCheckBoxDoLog.setBackground(SystemColor.control);
      jCheckBoxDoLog.setToolTipText(
              "If you are using p values, you should check this box.");
      jCheckBoxDoLog.setSelected(true);
      jCheckBoxDoLog.setText("Log tranform the gene scores");
      jPanelAnalysisFrameMethods.setBackground(SystemColor.control);
      jPanelAnalysisFrameMethods.setBorder(null);
      jPanelAnalysisFrameMethods.setMinimumSize(new Dimension(150, 37));
      jPanelAnalysisFrameMethods.setPreferredSize(new Dimension(150, 45));
      jLabelAnalysisFrameMethod.setMaximumSize(new Dimension(167, 18));
      jLabelAnalysisFrameMethod.setMinimumSize(new Dimension(167, 18));
      jLabelAnalysisFrameMethod.setToolTipText(
              "Determines how the gene scores are combined to make a class score.");
      jLabelAnalysisFrameMethod.setText("Class Raw Score Method");
      jRadioButtonMedian.setText("Median");
      jRadioButtonMedian.setToolTipText(
              "The score for a class is the median of the score of genes in the " +
              "class.");
      jRadioButtonMedian.setBackground(SystemColor.control);
      jRadioButtonMean.setBackground(SystemColor.control);
      jRadioButtonMean.setToolTipText(
              "The raw score for the class is the mean of the scores for genes in " +
              "the class");
      jRadioButtonMean.setSelected(true);
      jRadioButtonMean.setText("Mean");
      buttonGroup2.add(jRadioButtonMean);
      buttonGroup2.add(jRadioButtonMedian);
      step4LeftPanel.add(jPanel16, null);
      step4RightPanel.setPreferredSize(new Dimension(200, 160));
      jPanelAnalysisFrameMethods.add(jLabelAnalysisFrameMethod, null);
      jPanelAnalysisFrameMethods.add(jRadioButtonMean, null);
      jPanelAnalysisFrameMethods.add(jRadioButtonMedian, null);
      jLabelReplicateTreament.setToolTipText(
              "How will replicates of the same gene be treated?");
      jLabelReplicateTreament.setText("Gene replicate treatment");
      jPanelReplicateTreaments.setBackground(SystemColor.control);
      jPanelReplicateTreaments.setBorder(null);
      jPanelReplicateTreaments.setPreferredSize(new Dimension(175, 100));
      jPanelReplicateTreaments.setToolTipText(
              "How will replicates of the same gene be treated?");
      jRadioButtonBestReplicates.setBackground(SystemColor.control);
      jRadioButtonBestReplicates.setPreferredSize(new Dimension(171, 23));
      jRadioButtonBestReplicates.setToolTipText(
              "If a gene occurs more than once, it is counted only once and the " +
              "score used is that of the highest-scoring occurrence.");
      jRadioButtonBestReplicates.setSelected(true);
      jRadioButtonBestReplicates.setText("Use Best scoring replicate");
      jRadioButtonSeparateReplicates.setBackground(SystemColor.control);
      jRadioButtonSeparateReplicates.setToolTipText(
              "Genes occurring more than once are counted more than once.");
      jRadioButtonSeparateReplicates.setText("Count all replicates separately");
      jRadioButtonMeanReplicates.setBackground(SystemColor.control);
      jRadioButtonMeanReplicates.setPreferredSize(new Dimension(171, 23));
      jRadioButtonMeanReplicates.setToolTipText(
              "If a gene occurs more than once, the gene is only counted once and " +
              "the score is the mean of all occurrences.");
      jRadioButtonMeanReplicates.setSelected(false);
      jRadioButtonMeanReplicates.setText("Use Mean of replicates");
      replicateButtonGroup.add(jRadioButtonBestReplicates);
      replicateButtonGroup.add(jRadioButtonSeparateReplicates);
      replicateButtonGroup.add(jRadioButtonMeanReplicates);
      jPanelReplicateTreaments.add(jLabelReplicateTreament, null);
      jPanelReplicateTreaments.add(jRadioButtonSeparateReplicates, null);
      jPanelReplicateTreaments.add(jRadioButtonBestReplicates, null);
      jPanelReplicateTreaments.add(jRadioButtonMeanReplicates, null);
      jPanelReplicateTreaments.add(jList1, null);
      step4RightPanel.add(jPanelReplicateTreaments, null);
      step4TopPanel.add(step4LeftPanel, null);
      step4TopPanel.add(step4RightPanel, null);
      step4Panel.add(step4TopPanel, null);

      //oraPanel stuff//////////////////////////////////////////////////////////
      oraPanel.setPreferredSize(new Dimension(335, 100));
      oraTitledBorder = new TitledBorder("ORA");
      oraPanel.setBorder(oraTitledBorder);
      jPanel15.setMinimumSize(new Dimension(180, 29));
      jPanel15.setBackground(SystemColor.control);
      jLabel6.setLabelFor(jTextFieldPValueThreshold);
      jLabel6.setText("P value threshold");
      jTextFieldPValueThreshold.setEditable(true);
      jTextFieldPValueThreshold.setPreferredSize(new Dimension(50, 19));
      jTextFieldPValueThreshold.setToolTipText(
              "Score Threshold used for Over-Representation analysis");
      jTextFieldPValueThreshold.setText("0.001");
      jTextFieldPValueThreshold.setHorizontalAlignment(SwingConstants.RIGHT);
      jPanel15.add(jLabel6, null);
      jPanel15.add(jTextFieldPValueThreshold, null);
      oraPanel.add(jPanel15, null);
      //resampPanel stuff///////////////////////////////////////////////////////
      resampPanel.setPreferredSize(new Dimension(355, 100));
      resampTitledBorder = new TitledBorder("Resampling");
      resampPanel.setBorder(resampTitledBorder);
      jPanel14.setPreferredSize(new Dimension(110, 29));
      jPanel14.setBackground(SystemColor.control);
      jLabel10.setMaximumSize(new Dimension(39, 15));
      jLabel10.setMinimumSize(new Dimension(76, 15));
      jLabel10.setLabelFor(jTextFieldScoreCol);
      jLabel10.setText("Score column");
      jTextFieldScoreCol.setHorizontalAlignment(SwingConstants.RIGHT);
      jTextFieldScoreCol.setText("2");
      jTextFieldScoreCol.setToolTipText(
              "Column of the gene score file containing the scores");
      jTextFieldScoreCol.setMaximumSize(new Dimension(2147483647, 2147483647));
      jTextFieldScoreCol.setPreferredSize(new Dimension(30, 19));
      jTextFieldScoreCol.setEditable(true);
      jPanel14.add(jLabel10, null);
      jPanel14.add(jTextFieldScoreCol, null);
      jPanel13.setBorder(null);
      jPanel13.setPreferredSize(new Dimension(160, 29));
      jLabel13.setMaximumSize(new Dimension(39, 15));
      jLabel13.setLabelFor(jTextFieldIterations);
      jLabel13.setText("Iterations to run");
      jTextFieldIterations.setHorizontalAlignment(SwingConstants.RIGHT);
      jTextFieldIterations.setText("10");
      jTextFieldIterations.setToolTipText(
              "Number of iterations program will run for.");
      jTextFieldIterations.setPreferredSize(new Dimension(70, 19));
      jTextFieldIterations.setEditable(true);
      jPanel13.add(jLabel13, null);
      jPanel13.add(jTextFieldIterations, null);
      resampPanel.add(jPanel13, null);
      resampPanel.add(jPanel14, null);
      //corrPanel stuff/////////////////////////////////////////////////////////
      corrPanel.setPreferredSize(new Dimension(335, 100));
      corrTitledBorder = new TitledBorder("Correlation");
      corrPanel.setBorder(corrTitledBorder);
      corrMetricPanel.setPreferredSize(new Dimension(150, 50));
      corrMetricPanel.setMinimumSize(new Dimension(150, 37));
      corrMetricPanel.setBorder(null);
      corrMetricPanel.setBackground(SystemColor.control);
      corrMetricPanel.setBackground(SystemColor.control);
      corrMetricPanel.setToolTipText("metric tool tip.");
      corrMetricLabel.setText("Correlation Metric");
      corrMetricLabel.setToolTipText("metric tool tip.");
      corrMetricLabel.setMinimumSize(new Dimension(167, 18));
      corrMetricLabel.setMaximumSize(new Dimension(167, 18));
      corrRadioButton1.setText("Metric 1");
      corrRadioButton1.setSelected(true);
      corrRadioButton1.setBackground(SystemColor.control);
      corrRadioButton1.setToolTipText("metric 1 tool tip");
      corrRadioButton2.setText("Metric 2");
      corrButtonGroup.add(corrRadioButton1);
      corrButtonGroup.add(corrRadioButton2);
      corrMetricPanel.add(corrMetricLabel, null);
      corrMetricPanel.add(corrRadioButton1, null);
      corrMetricPanel.add(corrRadioButton2, null);
      corrPanel.add(jPanel13, null);
      corrPanel.add(corrMetricPanel, null);
      //step4Panel.add(oraPanel);
      //step4Panel.add(resampPanel);
      //step4Panel.add(corrPanel);

      this.add(step4Panel);
   }

   public void addVarPanel ( int analysisType ){
      if ( analysisType == 0 ) {
         step4Panel.add( oraPanel, null );
      } else if ( analysisType == 1 ) {
         resampPanel.add( jPanel13, null );
         step4Panel.add( resampPanel, null );
      } else if ( analysisType == 2 ) {
         corrPanel.add( jPanel13, null );
         step4Panel.add( corrPanel, null );
      }
   }

   public void removeVarPanel( int analysisType ) {
      if ( analysisType == 0 ) {
         step4Panel.remove( oraPanel );
      } else if ( analysisType == 1 ) {
         resampPanel.remove( jPanel13 );
         step4Panel.remove( resampPanel );
      } else if ( analysisType == 2 ) {
         corrPanel.remove( jPanel13 );
         step4Panel.remove( corrPanel );
      }
   }

   private void setValues() {
      jTextFieldMaxClassSize.setText(String.valueOf(settings.getMaxClassSize()));
      jTextFieldMinClassSize.setText(String.valueOf(settings.getMinClassSize()));
      jTextFieldIterations.setText(String.valueOf(settings.getIterations()));
      jTextFieldScoreCol.setText(String.valueOf(settings.getScorecol()));
      if(settings.getGeneRepTreatment()==0)
         jRadioButtonSeparateReplicates.setSelected(true);
      else if(settings.getGeneRepTreatment()==1)
         jRadioButtonBestReplicates.setSelected(true);
      else if(settings.getGeneRepTreatment()==2)
         jRadioButtonMeanReplicates.setSelected(true);
      if(settings.rawScoreMethod==settings.MEAN_METHOD)
         jRadioButtonMean.setSelected(true);
      else
         jRadioButtonMedian.setSelected(true);
      jCheckBoxDoLog.setSelected(settings.getDoLog());
      jTextFieldPValueThreshold.setText(String.valueOf(settings.getPValThreshold()));
   }

   public void saveValues(){
      settings.setMaxClassSize(Integer.valueOf(jTextFieldMaxClassSize.getText()).intValue());
      settings.setMinClassSize(Integer.valueOf(jTextFieldMinClassSize.getText()).intValue());
      settings.setIterations(Integer.valueOf(jTextFieldIterations.getText()).intValue());
      settings.setScorecol(Integer.valueOf(jTextFieldScoreCol.getText()).intValue());
      if(jRadioButtonSeparateReplicates.isSelected()) {
         settings.setGeneRepTreatment(0);
      } else if (jRadioButtonBestReplicates.isSelected()) {
         settings.setGeneRepTreatment(1);
      } else if (jRadioButtonMeanReplicates.isSelected()) {
         settings.setGeneRepTreatment(2);
      }
      if(jRadioButtonMean.isSelected())
         settings.setRawScoreMethod(settings.MEAN_METHOD);
      else
         settings.setRawScoreMethod(settings.QUANTILE_METHOD);
      settings.setDoLog(jCheckBoxDoLog.isSelected());
      settings.setPValThreshold(Double.valueOf(jTextFieldPValueThreshold.getText()).doubleValue());
   }

   public boolean isReady() { return true; }
}
