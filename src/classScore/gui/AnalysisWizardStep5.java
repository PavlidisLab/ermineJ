package classScore.gui;

import java.awt.Dimension;
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

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Homin Lee
 * @version $Id$
 * @todo this panel doesn't show up right for correlation analysis - shows the resampling one instead.
 */

public class AnalysisWizardStep5 extends WizardStep
{
   AnalysisWizard wiz;
   Settings settings;
   JPanel step5Panel;
   JRadioButton jRadioButtonMedian;
   JRadioButton jRadioButtonMean;
   JTextField jTextFieldPValueThreshold;
   JTextField jTextFieldScoreCol;
   JTextField jTextFieldIterations;
   JPanel oraPanel;
   JPanel resampPanel;
   JPanel corrPanel;
   JPanel jPanel13;

   public AnalysisWizardStep5(AnalysisWizard wiz, Settings settings)
   {
      super(wiz);
      this.wiz=wiz;
      this.settings=settings;
      wiz.clearStatus();
      setValues();
   }

   //Component initialization
   protected void jbInit(){
      JPanel step4TopPanel = new JPanel();
      JPanel jPanelAnalysisFrameMethods = new JPanel();
      JLabel jLabelAnalysisFrameMethod = new JLabel();
      ButtonGroup buttonGroup2 = new ButtonGroup();
      jRadioButtonMedian = new JRadioButton();
      jRadioButtonMean = new JRadioButton();

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

      step5Panel = new JPanel();
      step5Panel.setPreferredSize(new Dimension(550, 280));

      //oraPanel stuff//////////////////////////////////////////////////////////
      oraPanel.setPreferredSize(new Dimension(335, 150));
      oraTitledBorder = new TitledBorder("ORA");
      oraPanel.setBorder(oraTitledBorder);
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
      jPanelAnalysisFrameMethods.add(jLabelAnalysisFrameMethod, null);
      jPanelAnalysisFrameMethods.add(jRadioButtonMean, null);
      jPanelAnalysisFrameMethods.add(jRadioButtonMedian, null);
      step5Panel.add(step4TopPanel, null);
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
      resampPanel.add(jPanelAnalysisFrameMethods, null);
      oraPanel.add(jPanel15, null);

      //resampPanel stuff///////////////////////////////////////////////////////
      resampPanel.setPreferredSize(new Dimension(355, 150));
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
      corrPanel.setPreferredSize(new Dimension(335, 150));
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

      this.addHelp("<html><b>Adjust settings specific for your analysis method.</b><br>"+
                   " ");
      this.addMain(step5Panel);
   }

   public void addVarPanel ( int analysisType ){
      if ( analysisType == Settings.ORA ) {
         step5Panel.add( oraPanel, null );
      } else if ( analysisType == Settings.RESAMP ) {
         resampPanel.add( jPanel13, null );
         step5Panel.add( resampPanel, null );
      } else if ( analysisType == Settings.CORR ) {
         corrPanel.add( jPanel13, null );
         step5Panel.add( corrPanel, null );
      }
   }

   public void removeVarPanel( int analysisType ) {
      if ( analysisType == Settings.ORA ) {
         step5Panel.remove( oraPanel );
      } else if ( analysisType == Settings.RESAMP ) {
         resampPanel.remove( jPanel13 );
         step5Panel.remove( resampPanel );
      } else if ( analysisType == Settings.CORR ) {
         corrPanel.remove( jPanel13 );
         step5Panel.remove( corrPanel );
      }
   }

   private void setValues() {
      jTextFieldIterations.setText(String.valueOf(settings.getIterations()));
      jTextFieldScoreCol.setText(String.valueOf(settings.getScorecol()));
      if(settings.getRawScoreMethod()==Settings.MEAN_METHOD)
         jRadioButtonMean.setSelected(true);
      else
         jRadioButtonMedian.setSelected(true);
      jTextFieldPValueThreshold.setText(String.valueOf(settings.getPValThreshold()));
   }

   public void saveValues(){
      settings.setIterations(Integer.valueOf(jTextFieldIterations.getText()).intValue());
      settings.setScorecol(Integer.valueOf(jTextFieldScoreCol.getText()).intValue());
      if(jRadioButtonMean.isSelected())
         settings.setRawScoreMethod(Settings.MEAN_METHOD);
      else
         settings.setRawScoreMethod(Settings.QUANTILE_METHOD);
      settings.setPValThreshold(Double.valueOf(jTextFieldPValueThreshold.getText()).doubleValue());
   }

   public boolean isReady() { return true; }
}
