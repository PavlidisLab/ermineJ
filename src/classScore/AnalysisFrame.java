package classScore;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AnalysisFrame extends JDialog {
   JPanel mainPanel;
   JFileChooser chooser = new JFileChooser();
   File startPath;

   //holds bottom buttons
   JPanel BottomPanel = new JPanel();
   JButton nextButton = new JButton();
   JButton backButton = new JButton();
   JButton cancelButton = new JButton();
   JButton finishButton = new JButton();

   //panels for step 1
   JPanel step1Panel = new JPanel();
   JPanel jPanel4 = new JPanel(); // holds stuff
   JLabel jLabel8 = new JLabel();
   JPanel jPanel5 = new JPanel();
   ButtonGroup buttonGroup1 = new ButtonGroup();
   JRadioButton oraButton = new JRadioButton();
   JRadioButton resampButton = new JRadioButton();
   JRadioButton corrButton = new JRadioButton();
   JPanel jPanel12 = new JPanel();
   JLabel jLabel4 = new JLabel();
   JLabel jLabel5 = new JLabel();
   JLabel jLabel9 = new JLabel();

   //panels for step 2
   JPanel step2Panel = new JPanel();
   JPanel jPanel11 = new JPanel();
   JLabel jLabel3 = new JLabel();
   JTextField rawFile = new JTextField();
   JButton rawBrowseButton = new JButton();
   JPanel jPanel8 = new JPanel();
   JLabel jLabel2 = new JLabel();
   JTextField scoreFile = new JTextField();
   JButton scoreBrowseButton = new JButton();
   JPanel jPanel7 = new JPanel();
   JLabel step2NameFile = new JLabel();
   JTextField nameFile = new JTextField();
   JButton nameBrowseButton = new JButton();
   JButton probeBrowseButton = new JButton();
   JLabel step2ProbeLabel = new JLabel();
   JPanel step2ProbePanel = new JPanel();
   JTextField probeFile = new JTextField();

   //panels for step 3
   JPanel step3Panel;
   JLabel countLabel = new JLabel();
   JPanel jPanel10 = new JPanel();
   JScrollPane customClassScrollPane;
   JTable customClassTable;
   CustomClassList customClasses;
   HashMap ccHash;
   AbstractTableModel ccTableModel;
   JScrollPane addedClassScrollPane;
   JTable addedClassTable;
   CustomClassList addedClasses;
   HashMap acHash;
   AbstractTableModel acTableModel;
   JPanel jPanel9 = new JPanel();
   JButton addButton = new JButton();
   JButton deleteButton = new JButton();

   //panels for step 4
   JPanel step4Panel;
   JPanel step4TopPanel = new JPanel();
   JPanel step4LeftPanel = new JPanel();
   JPanel jPanel17 = new JPanel();
   JLabel jLabel11 = new JLabel();
   JTextField jTextFieldMaxClassSize = new JTextField();
   JPanel jPanel16 = new JPanel();
   JLabel jLabel12 = new JLabel();
   JTextField jTextFieldMinClassSize = new JTextField();
   JCheckBox jCheckBoxDoLog = new JCheckBox();
   JPanel jPanelAnalysisFrameMethods = new JPanel();
   JLabel jLabelAnalysisFrameMethod = new JLabel();
   ButtonGroup buttonGroup2 = new ButtonGroup();
   JRadioButton jRadioButtonMedian = new JRadioButton();
   JRadioButton jRadioButtonMean = new JRadioButton();
   JPanel step4RightPanel = new JPanel();
   JPanel jPanelReplicateTreaments = new JPanel();
   JLabel jLabelReplicateTreament = new JLabel();
   JList jList1 = new JList();
   ButtonGroup replicateButtonGroup = new ButtonGroup();
   JRadioButton jRadioButtonBestReplicates = new JRadioButton();
   JRadioButton jRadioButtonSeparateReplicates = new JRadioButton();
   JRadioButton jRadioButtonMeanReplicates = new JRadioButton();
   //panels for step 4 bottom
   JPanel oraPanel = new JPanel();
   TitledBorder oraTitledBorder;
   JPanel jPanel15 = new JPanel();
   JLabel jLabel6 = new JLabel();
   JTextField jTextFieldPValueThreshold = new JTextField();
   JPanel resampPanel = new JPanel();
   TitledBorder resampTitledBorder;
   JPanel jPanel14 = new JPanel();
   JLabel jLabel10 = new JLabel();
   JTextField jTextFieldScoreCol = new JTextField();
   JPanel jPanel13 = new JPanel();
   JLabel jLabel13 = new JLabel();
   JTextField jTextFieldIterations = new JTextField();
   JPanel corrPanel = new JPanel();
   TitledBorder corrTitledBorder;
   JPanel corrMetricPanel = new JPanel();
   ButtonGroup corrButtonGroup = new ButtonGroup();
   JLabel corrMetricLabel = new JLabel();
   JRadioButton corrRadioButton1 = new JRadioButton();
   JRadioButton corrRadioButton2 = new JRadioButton();

   //logic
   int step = 1;
   int analysisType = 1;
   int filetype = 0;
   boolean doLog;
   classScoreFrame callingframe;
   Thread aFrameRunner;

   public AnalysisFrame(classScoreFrame callingframe) {
      setModal(true);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.callingframe = callingframe;
      try {
         jbInit();
         getClasses();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
   private void jbInit() throws Exception {
      setResizable(true);
      mainPanel = (JPanel)this.getContentPane();
      mainPanel.setBackground(SystemColor.control);
      mainPanel.setAlignmentX((float) 0.5);
      mainPanel.setAlignmentY((float) 0.5);
      mainPanel.setMaximumSize(new Dimension(32767, 32767));
      mainPanel.setPreferredSize(new Dimension(550, 350));

      //bottom buttons/////////////////////////////////////////////////////////
      BottomPanel.setBackground(SystemColor.control);
      BottomPanel.setPreferredSize(new Dimension(200, 40));
      nextButton.setText("Next >");
      nextButton.addActionListener(new AnalysisFrame_nextButton_actionAdapter(this));
      backButton.setText("< Back");
      backButton.addActionListener(new AnalysisFrame_backButton_actionAdapter(this));
      backButton.setEnabled(false);
      cancelButton.setText("Cancel");
      cancelButton.addActionListener(new
                                     AnalysisFrame_cancelButton_actionAdapter(this));
      finishButton.setAlignmentY((float) 0.5);
      finishButton.setText("Finish");
      finishButton.addActionListener(new
                                     AnalysisFrame_finishButton_actionAdapter(this));
      step4TopPanel.setPreferredSize(new Dimension(410, 170));
      BottomPanel.add(cancelButton, null);
      BottomPanel.add(backButton, null);
      BottomPanel.add(nextButton, null);
      BottomPanel.add(finishButton, null);
      mainPanel.add(BottomPanel, BorderLayout.SOUTH);

      //step 1 panel///////////////////////////////////////////////////////////
      step1Panel.setPreferredSize(new Dimension(550, 104));
      jPanel4.setBackground(SystemColor.control);
      jPanel4.setForeground(Color.black);
      jPanel4.setBorder(BorderFactory.createEtchedBorder());
      jLabel8.setText("Choose the method of analysis:");
      jLabel8.setMaximumSize(new Dimension(999, 15));
      jLabel8.setMinimumSize(new Dimension(259, 15));
      jLabel8.setPreferredSize(new Dimension(274, 17));
      step1Panel.add(jLabel8, null);
      jPanel5.setPreferredSize(new Dimension(80, 80));
      oraButton.setText("ORA");
      oraButton.setBorder(BorderFactory.createLineBorder(Color.black));
      oraButton.setPreferredSize(new Dimension(75, 17));
      oraButton.setBackground(SystemColor.control);
      oraButton.addActionListener(new AnalysisFrame_oraButton_actionAdapter(this));
      buttonGroup1.add(oraButton);
      jPanel5.add(oraButton, null);
      resampButton.setText("Resampling");
      resampButton.setSelected(true);
      resampButton.setBackground(SystemColor.control);
      resampButton.setPreferredSize(new Dimension(75, 17));
      resampButton.setBorder(BorderFactory.createLineBorder(Color.black));
      resampButton.addActionListener(new
                                     AnalysisFrame_resampButton_actionAdapter(this));
      buttonGroup1.add(resampButton);
      jPanel5.add(resampButton, null);
      corrButton.setText("Correlation");
      corrButton.setBackground(SystemColor.control);
      corrButton.setPreferredSize(new Dimension(75, 17));
      corrButton.setBorder(BorderFactory.createLineBorder(Color.black));
      corrButton.addActionListener(new AnalysisFrame_corrButton_actionAdapter(this));
      buttonGroup1.add(corrButton);
      jPanel5.add(corrButton, null);
      jPanel4.add(jPanel5, null);
      jPanel12.setPreferredSize(new Dimension(175, 80));
      jLabel9.setText("- Description of ORA analysis");
      jLabel9.setBorder(null);
      jLabel9.setPreferredSize(new Dimension(172, 17));
      jPanel12.add(jLabel9, null);
      jLabel4.setText("- Description of resampling analysis");
      jLabel4.setBorder(null);
      jLabel4.setPreferredSize(new Dimension(172, 17));
      jPanel12.add(jLabel4, null);
      jLabel5.setText("- Description of correlation analysis");
      jLabel5.setBorder(null);
      jLabel5.setPreferredSize(new Dimension(172, 17));
      jPanel12.add(jLabel5, null);
      jPanel4.add(jPanel12, null);
      step1Panel.add(jPanel4, null);

      //step 2 panel////////////////////////////////////////////////////////////
      jPanel11.setPreferredSize(new Dimension(330, 50));
      jPanel11.setBackground(SystemColor.control);
      jLabel3.setText("Raw data file (optional for ORA or resampling):");
      jLabel3.setPreferredSize(new Dimension(320, 15));
      rawFile.setToolTipText("");
      rawFile.setPreferredSize(new Dimension(230, 19));
      rawFile.setMinimumSize(new Dimension(4, 19));
      rawFile.setEnabled(false);
      rawBrowseButton.setEnabled(true);
      rawBrowseButton.addActionListener(new
                                        AnalysisFrame_rawBrowseButton_actionAdapter(this));
      rawBrowseButton.setText("Browse....");
      jPanel11.add(jLabel3, null);
      jPanel11.add(rawFile, null);
      jPanel11.add(rawBrowseButton, null);
      jPanel8.setPreferredSize(new Dimension(330, 50));
      jPanel8.setBackground(SystemColor.control);
      jLabel2.setText("Gene score file (optional for correlation score):");
      jLabel2.setPreferredSize(new Dimension(320, 15));
      scoreFile.setToolTipText("");
      scoreFile.setEnabled(false);
      scoreFile.setPreferredSize(new Dimension(230, 19));
      scoreFile.setMinimumSize(new Dimension(4, 19));
      scoreBrowseButton.setEnabled(true);
      scoreBrowseButton.setText("Browse....");
      scoreBrowseButton.addActionListener(new
                                          AnalysisFrame_scoreBrowseButton_actionAdapter(this));
      jPanel8.add(jLabel2, null);
      jPanel8.add(scoreFile, null);
      jPanel8.add(scoreBrowseButton, null);
      jPanel7.setBackground(SystemColor.control);
      jPanel7.setPreferredSize(new Dimension(330, 50));
      step2NameFile.setPreferredSize(new Dimension(320, 15));
      step2NameFile.setText("Gene name file:");
      nameFile.setMinimumSize(new Dimension(4, 19));
      nameFile.setEnabled(false);
      nameFile.setPreferredSize(new Dimension(230, 19));
      nameFile.setToolTipText("");
      nameBrowseButton.setEnabled(true);
      nameBrowseButton.setText("Browse....");
      nameBrowseButton.addActionListener(new
                                         AnalysisFrame_nameBrowseButton_actionAdapter(this));
      jPanel7.add(step2NameFile, null);
      jPanel7.add(nameFile, null);
      jPanel7.add(nameBrowseButton, null);
      probeBrowseButton.setEnabled(true);
      probeBrowseButton.setText("Browse....");
      probeBrowseButton.addActionListener(new
                                          AnalysisFrame_probeBrowseButton_actionAdapter(this));
      step2ProbeLabel.setPreferredSize(new Dimension(320, 15));
      step2ProbeLabel.setText("Probe annotation file:");
      step2ProbePanel.setBackground(SystemColor.control);
      step2ProbePanel.setPreferredSize(new Dimension(330, 50));
      probeFile.setToolTipText("");
      probeFile.setPreferredSize(new Dimension(230, 19));
      probeFile.setEnabled(false);
      probeFile.setMinimumSize(new Dimension(4, 19));
      step2ProbePanel.add(step2ProbeLabel, null);
      step2ProbePanel.add(probeFile, null);
      step2ProbePanel.add(probeBrowseButton, null);
      step2Panel.add(step2ProbePanel, null);
      step2Panel.add(jPanel7, null);
      step2Panel.add(jPanel11, null);
      step2Panel.add(jPanel8, null);

      //step 3 panel////////////////////////////////////////////////////////////
      step3Panel = new JPanel();
      step3Panel.setBorder(BorderFactory.createEtchedBorder());
      countLabel.setForeground(Color.black);
      countLabel.setText("Number of Classes: 0");
      customClassTable = new JTable();
      customClassTable.setPreferredScrollableViewportSize(new Dimension(250,
              150));
      customClassScrollPane = new JScrollPane(customClassTable);
      customClassScrollPane.setMaximumSize(new Dimension(32767, 32767));
      customClassScrollPane.setPreferredSize(new Dimension(250, 150));
      addedClassTable = new JTable();
      addedClassTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
      addedClassScrollPane = new JScrollPane(addedClassTable);
      addedClassScrollPane.setMaximumSize(new Dimension(32767, 32767));
      addedClassScrollPane.setPreferredSize(new Dimension(250, 150));
      jPanel10.add(customClassScrollPane, null);
      jPanel10.add(addedClassScrollPane, null);
      jPanel9.setMinimumSize(new Dimension(1, 1));
      jPanel9.setPreferredSize(new Dimension(200, 30));
      addButton.setSelected(false);
      addButton.setText("Add >");
      addButton.addActionListener(new AnalysisFrame_addButton_actionAdapter(this));
      deleteButton.setSelected(false);
      deleteButton.setText("Delete");
      deleteButton.addActionListener(new
                                     AnalysisFrame_delete_actionPerformed_actionAdapter(this));
      jPanel9.add(addButton, null);
      jPanel9.add(deleteButton, null);
      step3Panel.add(countLabel, null);
      step3Panel.add(jPanel10, null);
      step3Panel.add(jPanel9, null);

      //step 4 panel////////////////////////////////////////////////////////////
      step4Panel = new JPanel();
      step4Panel.setPreferredSize(new Dimension(550, 260));
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
      jCheckBoxDoLog.addActionListener(new
                                       AnalysisFrame_jCheckBoxDoLog_actionAdapter(this));
      jCheckBoxDoLog.setBackground(SystemColor.control);
      jCheckBoxDoLog.setToolTipText(
              "If you are using p values, you should check this box.");
      jCheckBoxDoLog.setSelected(true);
      jCheckBoxDoLog.setText("Log tranform the gene scores");
      jCheckBoxDoLog.addActionListener(new
                                       AnalysisFrame_jCheckBoxDoLog_actionAdapter(this));
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
      //step4Panel.add(oraPanel, null);
      //step4Panel.add(resampPanel, null);
      //step4Panel.add(corrPanel, null);

      //Finally, start things off///////////////////////////////////////////////
      mainPanel.add(step1Panel);
      this.getRootPane().setDefaultButton(nextButton);
      this.setTitle("Create New Analysis - Step 1 of 4");
      readPrefs();
      String folder = (String) nameFile.getText();
      int end = folder.lastIndexOf(File.separatorChar);
      folder = folder.substring(0, end + 1);
      chooser.setCurrentDirectory(new File(folder));
   }

   private void readPrefs() {
      Properties settings = callingframe.settings;
      try {
         String filename = "ClassScore.prefs";
         String dir = "C:\\jbproject\\ermineJ\\";
         String path = dir + filename;
         File file = new File( path );
         if (file.canRead()) {
            InputStream f = new FileInputStream( file );
            settings.load( f );
         }
      } catch (IOException ex) {
         System.err.println("Could not find preferences file."); // no big deal.
      }
      if (settings.size() > 0) {
         scoreFile.setText( settings.getProperty("scoreFile"));
         nameFile.setText( settings.getProperty("nameFile"));
         probeFile.setText( settings.getProperty("probeFile"));
         rawFile.setText( settings.getProperty("rawFile"));
         jTextFieldMaxClassSize.setText( settings.getProperty("maxClassSize"));
         jTextFieldMinClassSize.setText( settings.getProperty("minClassSize"));
         jCheckBoxDoLog.setSelected(Boolean.valueOf( settings.getProperty(
                 "doLog")).booleanValue());
         jTextFieldPValueThreshold.setText( settings.getProperty("pValTheshold"));
         jTextFieldIterations.setText( settings.getProperty("iterations"));
         jTextFieldScoreCol.setText( settings.getProperty("scorecol"));
      }
   }

   private void writePrefs() throws IOException {
      Properties settings = callingframe.settings;
      settings.setProperty("scoreFile", scoreFile.getText());
      settings.setProperty("nameFile", nameFile.getText());
      settings.setProperty("probeFile", probeFile.getText());
      settings.setProperty("rawFile", rawFile.getText());
      settings.setProperty("maxClassSize", jTextFieldMaxClassSize.getText());
      settings.setProperty("minClassSize", jTextFieldMinClassSize.getText());
      settings.setProperty("doLog", Boolean.toString(jCheckBoxDoLog.isSelected()));
      settings.setProperty("pValTheshold", jTextFieldPValueThreshold.getText());
      settings.setProperty("iterations", jTextFieldIterations.getText());
      settings.setProperty("scorecol", jTextFieldScoreCol.getText());

      String filename = "ClassScore.prefs";
      String dir = "C:\\jbproject\\ermineJ\\";
      String path = dir + filename;      
      OutputStream f = new FileOutputStream( path );
      settings.store(f, "");
   }

   private boolean testfile(String filename) {
      if (filename != null && filename.length() > 0) {
         File f = new File(filename);
         if (f.exists()) {
            return true;
         } else {
            JOptionPane.showMessageDialog(null,
                                          "File " + filename +
                                          " doesn't exist.  ");
         }
         return false;
      } else {
         JOptionPane.showMessageDialog(null, "A required file field is blank.");
         return false;
      }
   }

   public void error(Exception e, String message) {
      JOptionPane.showMessageDialog(null,
                                    "Error: " + message + "\n" + e.toString() +
                                    "\n" + e.getStackTrace());
   }

   public void error(String message) {
      JOptionPane.showMessageDialog(null, "Error: " + message + "\n");
   }

   void getClasses() {
      File dir = new File(chooser.getCurrentDirectory().getName() +
                          File.separator + "classes");
      if (dir.exists()) {
         String[] classFiles = dir.list(new ClassFileFilter("-class.txt"));
         customClasses = new CustomClassList();
         ccHash = new HashMap();
         for (int i = 0; i < classFiles.length; i++) {
            File classFile = new File(dir.getPath(), classFiles[i]);
            HashMap cfi = NewClass.getClassFileInfo(classFile.getAbsolutePath());
            customClasses.add(cfi);
            ccHash.put(cfi.get("id"), cfi);
         }
         ccTableModel = customClasses.toTableModel();
         customClassTable.setModel(ccTableModel);
         addedClasses = new CustomClassList();
         acTableModel = addedClasses.toTableModel();
         addedClassTable.setModel(acTableModel);
         acHash = new HashMap();
      }
   }

   //step 1 actions/////////////////////////////////////////////////////////////
   void corrButton_actionPerformed(ActionEvent e) {
      analysisType = 2;
   }

   void resampButton_actionPerformed(ActionEvent e) {
      analysisType = 1;
   }

   void oraButton_actionPerformed(ActionEvent e) {
      analysisType = 0;
   }

   //step 2 actions/////////////////////////////////////////////////////////////
   void rawFile_actionPerformed(ActionEvent e) {

   }

   void scoreFile_actionPerformed(ActionEvent e) {

   }

   void nameFile_actionPerformed(ActionEvent e) {

   }

   void probeFile_actionPerformed(ActionEvent e) {

   }

   void outputFile_actionPerformed(ActionEvent e) {

   }

   void probeBrowseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         probeFile.setText(chooser.getSelectedFile().toString());
      }
   }

   void rawBrowseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         rawFile.setText(chooser.getSelectedFile().toString());
      }
   }

   void scoreBrowseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         scoreFile.setText(chooser.getSelectedFile().toString());
      }
   }

   void nameBrowseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         nameFile.setText(chooser.getSelectedFile().toString());
      }
   }

   //step 3 actions/////////////////////////////////////////////////////////////
   void addButton_actionPerformed(ActionEvent e) {
      int n = customClassTable.getSelectedRowCount();
      int[] rows = customClassTable.getSelectedRows();
      for (int i = 0; i < n; i++) {
         String id = (String) customClassTable.getValueAt(rows[i], 0);
         if (id.compareTo("") != 0) {
            HashMap cfi = (HashMap) ccHash.get(id);
            if (!acHash.containsKey(cfi.get("id"))) {
               addedClasses.add(cfi);
               acHash.put(cfi.get("id"), cfi);
            }
         }
      }
      acTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void delete_actionPerformed(ActionEvent e) {
      int n = addedClassTable.getSelectedRowCount();
      int[] rows = addedClassTable.getSelectedRows();
      for (int i = 0; i < n; i++) {
         String id = (String) addedClassTable.getValueAt(rows[i] - i, 0);
         System.err.println(id);
         if (id.compareTo("") != 0) {
            HashMap cfi = (HashMap) ccHash.get(id);
            acHash.remove(cfi.get("id"));
            addedClasses.remove(cfi);
         }
      }
      acTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void updateCountLabel() {
      countLabel.setText("Number of Classes: " + addedClasses.size());
   }

   //step 4 actions/////////////////////////////////////////////////////////////
   void jCheckBoxDoLog_actionPerformed(ActionEvent e) {
      if (jCheckBoxDoLog.isSelected()) {
         doLog = true;
      } else {
         doLog = false;
      }
   }

   //bottom button actions//////////////////////////////////////////////////////
   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
         step = 2;
         this.getContentPane().remove(step1Panel);
         this.setTitle("Create New Analysis - Step 2 of 4");
         this.getContentPane().add(step2Panel);
         step2Panel.revalidate();
         backButton.setEnabled(true);
         this.repaint();
         nextButton.grabFocus();
      } else if (step == 2) {
         if (analysisType == 2 && rawFile.getText().compareTo("") == 0) {
            error("Correlation analyses require a raw data file.");
         } else if ((analysisType == 0 || analysisType == 1) &&
                    scoreFile.getText().compareTo("") == 0) {
            error("ORA and resampling analyses require a raw data file.");
         } else if (nameFile.getText().compareTo("") == 0) {
            error("Gene name files are required.");
         } else {
            step = 3;
            this.getContentPane().remove(step2Panel);
            this.setTitle("Create New Analysis - Step 3 of 4");
            this.getContentPane().add(step3Panel);
            step3Panel.revalidate();
            this.repaint();
         }
      } else if (step == 3) {
         step = 4;
         this.getContentPane().remove(step3Panel);
         if (analysisType == 0) {
            step4Panel.add(oraPanel, null);
         } else if (analysisType == 1) {
            resampPanel.add(jPanel13, null);
            step4Panel.add(resampPanel, null);
         } else if (analysisType == 2) {
            corrPanel.add(jPanel13, null);
            step4Panel.add(corrPanel, null);
         }
         this.setTitle("Create New Analysis - Step 4 of 4");
         this.getContentPane().add(step4Panel);
         step4Panel.revalidate();
         nextButton.setEnabled(false);
         this.repaint();
      }
   }

   void backButton_actionPerformed(ActionEvent e) {
      if (step == 2) {
         step = 1;
         this.getContentPane().remove(step2Panel);
         this.setTitle("Create New Analysis - Step 1 of 4");
         this.getContentPane().add(step1Panel);
         step1Panel.revalidate();
         backButton.setEnabled(false);
         this.repaint();
      } else if (step == 3) {
         step = 2;
         this.getContentPane().remove(step3Panel);
         this.setTitle("Create New Analysis - Step 2 of 4");
         this.getContentPane().add(step2Panel);
         step2Panel.revalidate();
         this.repaint();
      } else if (step == 4) {
         step = 3;
         if (analysisType == 0) {
            step4Panel.remove(oraPanel);
         } else if (analysisType == 1) {
            resampPanel.remove(jPanel13);
            step4Panel.remove(resampPanel);
         } else if (nameFile.getText().compareTo("") == 0) {
            corrPanel.remove(jPanel13);
            step4Panel.remove(corrPanel);
         }
         this.getContentPane().remove(step4Panel);
         this.setTitle("Create New Analysis - Step 3 of 4");
         this.getContentPane().add(step3Panel);
         step3Panel.revalidate();
         nextButton.setEnabled(true);
         this.repaint();
      }
   }

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {


      try {
         writePrefs();
      } catch (IOException ex) {
         System.err.println("Could not write prefs:" + ex);
         ex.printStackTrace();
      }

      class runthread extends Thread {
         public runthread() {}

         public void run() {
            callingframe.analyze(Integer.parseInt(jTextFieldMaxClassSize.
                                                  getText()),
                                 Integer.parseInt(jTextFieldMinClassSize.
                                                  getText()),
                                 Integer.parseInt(jTextFieldIterations.getText()),
                                 getClassScoreMethod(),
                                 getGroupMethod(),
                                 getUseWeights(),
                                 getUseLog(),
                                 scoreFile.getText(),
                                 probeFile.getText(),
                                 nameFile.getText(),
                                 new classScoreStatus(callingframe.jLabelStatus),
                                 Double.parseDouble(jTextFieldPValueThreshold.getText()),
                                 Integer.parseInt(jTextFieldScoreCol.getText()),
                                 "");
         }
      };

      aFrameRunner = new runthread();
      aFrameRunner.start();
      dispose();
   }

   private String getUseWeights() {
      if (!testfile(probeFile.getText())) {
         return "false";
      }

      if (jRadioButtonMeanReplicates.isSelected()) {
         return "true";
      } else if (jRadioButtonBestReplicates.isSelected()) {
         return "true";
      } else {
         return "false";
      }
   }

   private String getClassScoreMethod() {
      if (jRadioButtonMean.isSelected()) {
         return "MEAN_METHOD";
      } else {
         return "QUANTILE_METHOD"; // note that quantile is hard-coded to be 50 for the gui.
      }
   }

   private String getGroupMethod() {
      if (jRadioButtonMeanReplicates.isSelected()) {
         return "MEAN_PVAL";
      } else if (jRadioButtonBestReplicates.isSelected()) {
         return "BEST_PVAL";
      } else {
         return "MEAN_PVAL"; // dummy. It won't be used.
      }
   }

   private String getUseLog() {
      return Boolean.toString(jCheckBoxDoLog.isSelected());
   }
}


//classes///////////////////////////////////////////////////////////////////////
class ClassFileFilter implements FilenameFilter {
   private String extension;
   public ClassFileFilter(String ext) {extension = ext;
   }

   public boolean accept(File dir, String name) {return name.endsWith(extension);
   }
}


class CustomClassList extends ArrayList {
   public AbstractTableModel toTableModel() {
      return new AbstractTableModel() {
         private String[] columnNames = {"ID", "Description", "Members"};
         public String getColumnName(int i) {return columnNames[i];
         }

         public int getColumnCount() {return columnNames.length;
         }

         public int getRowCount() {
            int windowrows = 8;
            int extra = 1;
            if (size() < windowrows) {
               extra = windowrows - size();
            }
            return size() + extra;
         }

         public Object getValueAt(int i, int j) {
            if (i < size()) {
               HashMap cinfo = (HashMap) get(i);
               switch (j) {
               case 0:
                  return cinfo.get("id");
               case 1:
                  return cinfo.get("desc");
               case 2: {
                  String type = (String) cinfo.get("type");
                  ArrayList members = (ArrayList) cinfo.get("members");
                  return (Integer.toString(members.size()) + " " + type + "s");
               }
               default:
                  return "";
               }
            } else {
               return "";
            }
         }
      };
   };
}


//step 1 classes////////////////////////////////////////////////////////////////
class AnalysisFrame_oraButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_oraButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.oraButton_actionPerformed(e);
   }
}


class AnalysisFrame_corrButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_corrButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.corrButton_actionPerformed(e);
   }
}


class AnalysisFrame_resampButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_resampButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.resampButton_actionPerformed(e);
   }
}


//step 2 classes////////////////////////////////////////////////////////////////
class AnalysisFrame_rawBrowseButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_rawBrowseButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.rawBrowseButton_actionPerformed(e);
   }
}


class AnalysisFrame_rawFile_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_rawFile_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.rawFile_actionPerformed(e);
   }
}


class AnalysisFrame_scoreBrowseButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_scoreBrowseButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.scoreBrowseButton_actionPerformed(e);
   }
}


class AnalysisFrame_scoreFile_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_scoreFile_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.scoreFile_actionPerformed(e);
   }
}


class AnalysisFrame_nameFile_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_nameFile_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.nameFile_actionPerformed(e);
   }
}


class AnalysisFrame_nameBrowseButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_nameBrowseButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.nameBrowseButton_actionPerformed(e);
   }
}


class AnalysisFrame_outputFile_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_outputFile_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.outputFile_actionPerformed(e);
   }
}

class AnalysisFrame_probeFile_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_probeFile_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.probeFile_actionPerformed(e);
   }
}


class AnalysisFrame_probeBrowseButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_probeBrowseButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.probeBrowseButton_actionPerformed(e);
   }
}


//step 3 classes////////////////////////////////////////////////////////////////
class AnalysisFrame_delete_actionPerformed_actionAdapter implements java.awt.
        event.ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_delete_actionPerformed_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.delete_actionPerformed(e);
   }
}


class AnalysisFrame_addButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_addButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.addButton_actionPerformed(e);
   }
}


//setp 4 classes/////////////////////////////////////////////////////////////
class AnalysisFrame_jCheckBoxDoLog_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_jCheckBoxDoLog_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jCheckBoxDoLog_actionPerformed(e);
   }
}


//bottom button classes//////////////////////////////////////////////////////
class AnalysisFrame_nextButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_nextButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.nextButton_actionPerformed(e);
   }
}


class AnalysisFrame_backButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_backButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.backButton_actionPerformed(e);
   }
}


class AnalysisFrame_cancelButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_cancelButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.cancelButton_actionPerformed(e);
   }
}


class AnalysisFrame_finishButton_actionAdapter implements java.awt.event.
        ActionListener {
   AnalysisFrame adaptee;

   AnalysisFrame_finishButton_actionAdapter(AnalysisFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.finishButton_actionPerformed(e);
   }
}
