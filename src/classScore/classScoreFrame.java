package classScore;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class classScoreFrame
    extends JFrame {

   JPanel GeneBasedScorePanel = new JPanel();
   JButton jButtonRun = new JButton();
   JButton jButtonQuit = new JButton();
   JTextField jTextFieldGeneScoreFile = new JTextField();
   JTextField jTextFieldOutPutFileName = new JTextField();
   JTextField jTextFieldGONames = new JTextField();
   JTextField jTextFieldProbeAnnot = new JTextField();
   JRadioButton jRadioButtonMeanReplicates = new JRadioButton();
   JRadioButton jRadioButtonBestReplicates = new JRadioButton();
   JRadioButton jRadioButtonSeparateReplicates = new JRadioButton();
   JRadioButton jRadioButtonMean = new JRadioButton();
   JRadioButton jRadioButtonMedian = new JRadioButton();
   JTextField jTextFieldMinClassSize = new JTextField();
   JTextField jTextFieldIterations = new JTextField();
   JTextField jTextFieldPValueThreshold = new JTextField();
   JButton jButtonGeneScoreFileBrowse = new JButton();
   JButton jButtonOutputFileNameBrowse = new JButton();
   JButton jButtonGONamesBrowse = new JButton();
   JButton jButtonProbeAnnotsBrowse = new JButton();
   JTextField jTextFieldMaxClassSize = new JTextField();
   JPanel jPanelFilesGroup = new JPanel();
   JPanel jPanelGeneScoreGroup = new JPanel();
   FlowLayout flowLayout1 = new FlowLayout();
   FlowLayout flowLayout3 = new FlowLayout();
   JPanel jPanelOuputGroup = new JPanel();
   FlowLayout flowLayout4 = new FlowLayout();
   JPanel jPanelGONamesGroup = new JPanel();
   FlowLayout flowLayout5 = new FlowLayout();
   JPanel jPanelProbeMapGroup = new JPanel();
   JPanel jPanelParameterGroup = new JPanel();
   JLabel jLabel1 = new JLabel();
   JLabel jLabel2 = new JLabel();
   JLabel jLabel3 = new JLabel();
   JLabel jLabel4 = new JLabel();
   ButtonGroup buttonGroup1 = new ButtonGroup();
   ButtonGroup buttonGroup2 = new ButtonGroup();

   File startPath;
   JCheckBox jCheckBoxDoLog = new JCheckBox();
   JPanel jPanelReplicateTreaments = new JPanel();
   JPanel jPanelClassScoreMethods = new JPanel();
   JLabel jLabelReplicateTreament = new JLabel();
   JLabel jLabelClassScoreMethod = new JLabel();
   JPanel jPanel10 = new JPanel();
   JPanel jPanel11 = new JPanel();
   JPanel jPanel12 = new JPanel();
   JPanel jPanel13 = new JPanel();
   FlowLayout flowLayout7 = new FlowLayout();
   JLabel jLabel6 = new JLabel();
   JLabel jLabel7 = new JLabel();
   JLabel jLabel8 = new JLabel();
   JLabel jLabel9 = new JLabel();
   FlowLayout flowLayout8 = new FlowLayout();
   JPanel jPanelMainControls = new JPanel();
   boolean doLog;
   JButton jButtonAbout = new JButton();
   JFileChooser chooser = new JFileChooser();
   JLabel jLabelStatus = new JLabel();
   JButton jButtonCancel = new JButton();
   JButton jButtonSavePrefs = new JButton();
   JPanel jPanelStatus = new JPanel();
   FlowLayout flowLayout6 = new FlowLayout();
   Thread runner;
   boolean done = false;

   JPanel jPanel1 = (JPanel)this.getContentPane();
   BorderLayout borderLayout1 = new BorderLayout();

   JButton jButtonLoadResults = new JButton();

   boolean loadResults = false;
   JTextField jTextFieldScoreCol = new JTextField();
   JPanel jPanel14 = new JPanel();
   JLabel jLabel10 = new JLabel();

   public classScoreFrame() {
      try {
         jbInit();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {
      ConsoleWindow.init();
      this.setDefaultCloseOperation(EXIT_ON_CLOSE);
      this.setSize(new Dimension(550, 600));
      this.setTitle("Functional Class Scoring");

      GeneBasedScorePanel.setPreferredSize(new Dimension(500, 530));
      jButtonSavePrefs.addActionListener(new
                                         classScoreFrame_jButtonSavePrefs_actionAdapter(this));
      jLabelStatus.setBorder(null);
      jPanelStatus.setPreferredSize(new Dimension(534, 33));

      jTextFieldPValueThreshold.setEditable(true);
      jTextFieldMinClassSize.setEditable(true);
      jTextFieldMaxClassSize.setEditable(true);
      jTextFieldIterations.setEditable(true);

      jTextFieldGONames.setEditable(true);
      jTextFieldOutPutFileName.setEditable(true);
      jTextFieldProbeAnnot.setEditable(true);

      jPanel1.setLayout(borderLayout1);

      jButtonSavePrefs.setToolTipText(
          "Save the names of the five files listed in a file that will be loaded " +
          "next time you run the software.");
      jLabelClassScoreMethod.setToolTipText(
          "Determines how the gene scores are combined to make a class score.");
      jButtonRun.setToolTipText("");
      jButtonAbout.setToolTipText("Please click here!");
      jPanel1.setBackground(UIManager.getColor("control"));
      jPanel1.setMinimumSize(new Dimension(100, 0));
      jPanel1.setPreferredSize(new Dimension(500, 600));
      borderLayout1.setVgap(5);
      jButtonLoadResults.setToolTipText(
          "Click to load an existing results file from disk");
      jButtonLoadResults.setActionCommand("jButtonLoad");
      jButtonLoadResults.setText("Load Results");
      jButtonLoadResults.addActionListener(new
                                           classScoreFrame_jButtonLoadResults_actionAdapter(this));
      jLabel2.setToolTipText("File data will be written to or read from.");
      jTextFieldScoreCol.setHorizontalAlignment(SwingConstants.RIGHT);
      jTextFieldScoreCol.setText("2");
      jTextFieldScoreCol.setToolTipText(
          "Column of the gene score file containing the scores");
      jTextFieldScoreCol.setMaximumSize(new Dimension(2147483647, 2147483647));
      jTextFieldScoreCol.setOpaque(true);
      jTextFieldScoreCol.setPreferredSize(new Dimension(30, 19));
      jTextFieldScoreCol.setRequestFocusEnabled(true);
      jTextFieldScoreCol.setEditable(true);
      jPanel14.setPreferredSize(new Dimension(180, 29));
      jLabel10.setMaximumSize(new Dimension(39, 15));
      jLabel10.setMinimumSize(new Dimension(76, 15));
      jLabel10.setRequestFocusEnabled(true);
      jLabel10.setToolTipText("");
      jLabel10.setLabelFor(jTextFieldScoreCol);
      jLabel10.setText("Score column");
      jPanelOuputGroup.add(jLabel2, null);
      jPanelOuputGroup.add(jTextFieldOutPutFileName, null);
      jPanelOuputGroup.add(jButtonOutputFileNameBrowse, null);

      jPanelGeneScoreGroup.add(jLabel3, null);
      jPanelGeneScoreGroup.add(jTextFieldGeneScoreFile, null);
      jPanelGeneScoreGroup.add(jButtonGeneScoreFileBrowse, null);

      jPanelFilesGroup.add(jPanelGeneScoreGroup, null);
      jPanelFilesGroup.add(jPanelGONamesGroup, null);
      jPanelFilesGroup.add(jPanelProbeMapGroup, null);
      jPanelFilesGroup.add(jPanelOuputGroup, null);
      jPanelFilesGroup.add(jButtonSavePrefs, null);

      jPanelProbeMapGroup.add(jLabel1, null);
      jPanelProbeMapGroup.add(jTextFieldProbeAnnot, null);
      jPanelProbeMapGroup.add(jButtonProbeAnnotsBrowse, null);

      GeneBasedScorePanel.add(jPanelClassScoreMethods, null);

      jPanelGONamesGroup.add(jLabel4, null);
      jPanelGONamesGroup.add(jTextFieldGONames, null);
      jPanelGONamesGroup.add(jButtonGONamesBrowse, null);

      jPanelMainControls.add(jButtonQuit, null);
      jPanelMainControls.add(jButtonLoadResults, null);
      jPanelMainControls.add(jButtonRun, null);
      jPanelMainControls.add(jButtonCancel, null);
      jPanelMainControls.add(jButtonAbout, null);

      GeneBasedScorePanel.add(jPanelFilesGroup, null);

      jPanelClassScoreMethods.add(jLabelClassScoreMethod, null);
      jPanelClassScoreMethods.add(jRadioButtonMean, null);
      jPanelClassScoreMethods.add(jRadioButtonMedian, null);
      GeneBasedScorePanel.add(jPanelReplicateTreaments, null);

      jButtonRun.setText("Run Analysis");
      jButtonRun.addActionListener(new classScoreFrame_jButtonRun_actionAdapter(this));
      jButtonQuit.setText("Quit Program");
      jButtonQuit.addActionListener(new classScoreFrame_jButtonQuit_actionAdapter(this));

      jTextFieldGeneScoreFile.setMinimumSize(new Dimension(4, 190));
      jTextFieldGeneScoreFile.setPreferredSize(new Dimension(280, 19));
      jTextFieldGeneScoreFile.setToolTipText("Enter the name of your file");
      jTextFieldGeneScoreFile.setText("Enter the name of your file");

      jTextFieldOutPutFileName.setPreferredSize(new Dimension(280, 19));
      jTextFieldOutPutFileName.setToolTipText("Path to a file for output");
      jTextFieldOutPutFileName.setText("Path to a file for output");

      jTextFieldGONames.setMinimumSize(new Dimension(4, 19));
      jTextFieldGONames.setPreferredSize(new Dimension(280, 19));
      jTextFieldGONames.setToolTipText(
          "File containing GO Id to description mapping");
      jTextFieldGONames.setText("File containing GO Id to description mapping");

      jTextFieldProbeAnnot.setPreferredSize(new Dimension(280, 19));
      jTextFieldProbeAnnot.setToolTipText("File for your microarray design");
      jTextFieldProbeAnnot.setText("File for your microarray design");

      jRadioButtonMeanReplicates.setToolTipText(
          "If a gene occurs more than once, the gene is only counted once and " +
          "the score is the mean of all occurrences.");
      jRadioButtonMeanReplicates.setSelected(false);
      jRadioButtonMeanReplicates.setText("Use Mean of replicates");
      jRadioButtonBestReplicates.setToolTipText(
          "If a gene occurs more than once, it is counted only once and the " +
          "score used is that of the highest-scoring occurrence.");
      jRadioButtonBestReplicates.setSelected(true);
      jRadioButtonBestReplicates.setText("Use Best scoring replicate");
      jRadioButtonSeparateReplicates.setToolTipText(
          "Genes occurring more than once are counted more than once.");
      jRadioButtonSeparateReplicates.setText("Count all occurrences separately");
      jRadioButtonMean.setToolTipText(
          "The raw score for the class is the mean of the scores for genes in " +
          "the class");
      jRadioButtonMean.setSelected(true);
      jRadioButtonMean.setText("Mean");
      jRadioButtonMedian.setToolTipText(
          "The score for a class is the median of the score of genes in the " +
          "class.");

      jRadioButtonMedian.setText("Median");
      jTextFieldMinClassSize.setBackground(Color.white);
      jTextFieldMinClassSize.setPreferredSize(new Dimension(30, 19));
      jTextFieldMinClassSize.setToolTipText(
          "Smallest class size to be considered");
      jTextFieldMinClassSize.setText("8");
      jTextFieldMinClassSize.setHorizontalAlignment(SwingConstants.RIGHT);

      jTextFieldIterations.setPreferredSize(new Dimension(70, 19));
      jTextFieldIterations.setToolTipText(
          "Number of iterations program will run for.");
      jTextFieldIterations.setText("100000");
      jTextFieldIterations.setHorizontalAlignment(SwingConstants.RIGHT);

      jTextFieldPValueThreshold.setPreferredSize(new Dimension(50, 19));
      jTextFieldPValueThreshold.setToolTipText(
          "Score Threshold used for Over-Representation analysis");
      jTextFieldPValueThreshold.setText("0.001");
      jTextFieldPValueThreshold.setHorizontalAlignment(SwingConstants.RIGHT);

      jButtonGeneScoreFileBrowse.setText("Browse....");
      jButtonGeneScoreFileBrowse.addActionListener(new
          classScoreFrame_jButtonGeneScoreFileBrowse_actionAdapter(this));
      jButtonOutputFileNameBrowse.setText("Browse....");
      jButtonOutputFileNameBrowse.addActionListener(new
          classScoreFrame_jButtonOutputFileNameBrowse_actionAdapter(this));
      jButtonGONamesBrowse.setText("Browse....");
      jButtonGONamesBrowse.addActionListener(new
                                             classScoreFrame_jButtonGONamesBrowse_actionAdapter(this));
      jButtonProbeAnnotsBrowse.setText("Browse....");
      jButtonProbeAnnotsBrowse.addActionListener(new
                                                 classScoreFrame_jButtonProbeAnnotsBrowse_actionAdapter(this));

      jTextFieldMaxClassSize.setPreferredSize(new Dimension(30, 19));
      jTextFieldMaxClassSize.setToolTipText("Largest class size to be considered");
      jTextFieldMaxClassSize.setText("150");
      jTextFieldMaxClassSize.setHorizontalAlignment(SwingConstants.RIGHT);

      jPanelFilesGroup.setBorder(BorderFactory.createEtchedBorder());
      jPanelFilesGroup.setDebugGraphicsOptions(0);
      jPanelFilesGroup.setPreferredSize(new Dimension(530, 225));
      jPanelFilesGroup.setToolTipText("");
      jPanelFilesGroup.setBounds(new Rectangle(7, 11, 520, 238));
      jPanelFilesGroup.setLayout(flowLayout6);

      jPanelGeneScoreGroup.setBackground(UIManager.getColor("control"));
      jPanelGeneScoreGroup.setPreferredSize(new Dimension(510, 35));
      jPanelGeneScoreGroup.setLayout(flowLayout1);
      jPanelOuputGroup.setLayout(flowLayout3);
      jPanelOuputGroup.setBackground(UIManager.getColor("control"));
      jPanelOuputGroup.setMinimumSize(new Dimension(250, 35));
      jPanelOuputGroup.setPreferredSize(new Dimension(510, 35));
      jPanelGONamesGroup.setBackground(UIManager.getColor("control"));
      jPanelGONamesGroup.setPreferredSize(new Dimension(510, 35));
      jPanelGONamesGroup.setLayout(flowLayout4);
      jPanelProbeMapGroup.setBackground(UIManager.getColor("control"));
      jPanelProbeMapGroup.setMinimumSize(new Dimension(250, 35));
      jPanelProbeMapGroup.setPreferredSize(new Dimension(510, 35));
      jPanelProbeMapGroup.setLayout(flowLayout5);
      jPanelParameterGroup.setBorder(BorderFactory.createEtchedBorder());
      jPanelParameterGroup.setPreferredSize(new Dimension(200, 200));
      jPanelParameterGroup.setBounds(new Rectangle(272, 264, 200, 202));
      jPanelParameterGroup.setLayout(flowLayout7);
      jLabel1.setPreferredSize(new Dimension(120, 15));
      jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel1.setText("Probe annnotations");
      jLabel2.setPreferredSize(new Dimension(120, 15));
      jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel2.setText("Results File");
      jLabel3.setPreferredSize(new Dimension(120, 15));
      jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel3.setText("Gene Score File");
      jLabel4.setPreferredSize(new Dimension(120, 15));
      jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel4.setText("GO Names");
      jCheckBoxDoLog.setToolTipText(
          "If you are using p values, you should check this box.");
      jCheckBoxDoLog.setSelected(true);
      jCheckBoxDoLog.setText("Take log of Gene scores");
      jCheckBoxDoLog.addActionListener(new
                                       classScoreFrame_jCheckBoxDoLog_actionAdapter(this));
      jPanelReplicateTreaments.setBorder(BorderFactory.createEtchedBorder());
      jPanelReplicateTreaments.setPreferredSize(new Dimension(220, 120));
      jPanelReplicateTreaments.setToolTipText(
          "How will replicates of the same gene be treated?");
      jPanelReplicateTreaments.setBounds(new Rectangle(27, 348, 220, 120));
      jPanelReplicateTreaments.setLayout(flowLayout8);
      jPanelClassScoreMethods.setBorder(BorderFactory.createEtchedBorder());
      jPanelClassScoreMethods.setMinimumSize(new Dimension(301, 37));
      jPanelClassScoreMethods.setPreferredSize(new Dimension(170, 60));
      jPanelClassScoreMethods.setBounds(new Rectangle(28, 286, 220, 60));
      jLabelReplicateTreament.setFont(new java.awt.Font("Dialog", 1, 13));
      jLabelReplicateTreament.setToolTipText(
          "How will replicates of the same gene be treated?");
      jLabelReplicateTreament.setText("Gene replicate treatment");
      jLabelClassScoreMethod.setFont(new java.awt.Font("Dialog", 1, 13));
      jLabelClassScoreMethod.setText("Class Raw Score Method");
      jPanel10.setMinimumSize(new Dimension(180, 29));
      jPanel11.setPreferredSize(new Dimension(180, 29));
      jPanel12.setPreferredSize(new Dimension(180, 29));
      jPanel13.setPreferredSize(new Dimension(180, 29));
      jLabel6.setLabelFor(jTextFieldPValueThreshold);
      jLabel6.setText("ORA score threshold");
      jLabel7.setLabelFor(jTextFieldMinClassSize);
      jLabel7.setText("Minimum class size");
      jLabel8.setLabelFor(jTextFieldMaxClassSize);
      jLabel8.setText("Largest class size");
      jLabel9.setMaximumSize(new Dimension(39, 15));
      jLabel9.setRequestFocusEnabled(true);
      jLabel9.setLabelFor(jTextFieldIterations);
      jLabel9.setText("Iterations to run");
      flowLayout7.setAlignment(FlowLayout.CENTER);
      GeneBasedScorePanel.setMinimumSize(new Dimension(1, 1));
      GeneBasedScorePanel.setLayout(null);
      jButtonAbout.setText("About the software");
      jButtonAbout.addActionListener(new
                                     classScoreFrame_jButtonAbout_actionAdapter(this));
      jLabelStatus.setBackground(UIManager.getColor("control"));
      jLabelStatus.setFont(new java.awt.Font("Dialog", 0, 11));
      jLabelStatus.setForeground(Color.black);
      jLabelStatus.setPreferredSize(new Dimension(500, 19));
      jLabelStatus.setHorizontalAlignment(SwingConstants.LEFT);
      jLabelStatus.setText("Status");
      jButtonCancel.setToolTipText("Cancel the current run");
      jButtonCancel.setText("Stop");
      jButtonCancel.addActionListener(new
                                      classScoreFrame_jButtonCancel_actionAdapter(this));
      jButtonSavePrefs.setSelectedIcon(null);
      jButtonSavePrefs.setText("Save File Preferences");
      jPanelStatus.setBorder(BorderFactory.createEtchedBorder());
      jPanelMainControls.setBounds(new Rectangle(15, 476, 520, 35));
      jPanelReplicateTreaments.add(jLabelReplicateTreament, null);
      jPanelReplicateTreaments.add(jRadioButtonSeparateReplicates, null);
      jPanelReplicateTreaments.add(jRadioButtonBestReplicates, null);
      jPanelReplicateTreaments.add(jRadioButtonMeanReplicates, null);
      jPanel1.add(GeneBasedScorePanel, BorderLayout.NORTH);
      jPanel10.add(jLabel6, null);
      jPanel10.add(jTextFieldPValueThreshold, null);
      jPanel14.add(jLabel10, null);
      jPanel14.add(jTextFieldScoreCol, null);
      jPanelParameterGroup.add(jPanel12, null);
      jPanel12.add(jLabel7, null);
      jPanel12.add(jTextFieldMinClassSize, null);
      jPanelParameterGroup.add(jPanel13, null);
      jPanelParameterGroup.add(jCheckBoxDoLog, null);
      jPanelParameterGroup.add(jPanel10, null);
      jPanel13.add(jLabel8, null);
      jPanel13.add(jTextFieldMaxClassSize, null);
      jPanelParameterGroup.add(jPanel11, null);
      jPanel11.add(jLabel9, null);
      jPanel11.add(jTextFieldIterations, null);
      jPanelParameterGroup.add(jPanel14, null);
      GeneBasedScorePanel.add(jPanelMainControls, null);
      jPanel1.add(jPanelStatus, BorderLayout.SOUTH);
      jPanelStatus.add(jLabelStatus, null);
      GeneBasedScorePanel.add(jPanelParameterGroup, null);

      startPath = new File(System.getProperty("user.home"));
      buttonGroup2.add(jRadioButtonMean);
      buttonGroup2.add(jRadioButtonMedian);
      buttonGroup1.add(jRadioButtonMeanReplicates);
      buttonGroup1.add(jRadioButtonBestReplicates);
      buttonGroup1.add(jRadioButtonSeparateReplicates);
      chooser.setCurrentDirectory(startPath);
      readPrefs();
      showStatus("Please see 'About this software' for license information.");
   }

   /* init */

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

   private String getUseWeights() {

      if (!testfile(jTextFieldProbeAnnot.getText())) {
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

   private String getUseLog() {
      return Boolean.toString(jCheckBoxDoLog.isSelected());
   }

   void jButtonRun_actionPerformed(ActionEvent e) {

      final double oraThresh = Double.parseDouble(jTextFieldPValueThreshold.
                                                  getText());
      final int maxClassSize = Integer.parseInt(jTextFieldMaxClassSize.getText());
      final int minClassSize = Integer.parseInt(jTextFieldMinClassSize.getText());
      final int numIter = Integer.parseInt(jTextFieldIterations.getText());

      final String classScoreMethod = getClassScoreMethod();
      final String groupMethod = getGroupMethod();
      final String useWeights = getUseWeights();
      final String takeLog = getUseLog();

      boolean ok = true;
      ok = testfile(jTextFieldGeneScoreFile.getText());
      ok = ok && testfile(jTextFieldGONames.getText());
      ok = ok && testfile(jTextFieldProbeAnnot.getText());

      if (loadResults) {
         ok = ok && testfile(jTextFieldOutPutFileName.getText());
      }

      if (!ok) {
         return;
      }

      writePrefs();

      showStatus("Running");

      classScoreStatus m = new classScoreStatus(jLabelStatus);

      class runthread
          extends Thread {
         classScoreStatus m;

         public runthread(classScoreStatus m) {
            this.m = m;
         }

         public void run() {
            try {
               classPvalRun results = new classPvalRun(jTextFieldGeneScoreFile.
                   getText(),
                   jTextFieldProbeAnnot.getText(),
                   jTextFieldGONames.getText(),
                   jTextFieldOutPutFileName.
                   getText(),
                   classScoreMethod,
                   groupMethod,
                   maxClassSize,
                   minClassSize,
                   numIter, 50,
                   oraThresh,
                   useWeights,
                   Integer.parseInt(
                   jTextFieldScoreCol.getText()),
                   takeLog, "bh", m, loadResults);

               ResultFrame r = new ResultFrame(results);
               r.setTitle(jTextFieldOutPutFileName.getText());
               //        r.addClassDetailsListener(class_details_action_listener);
               r.setModel(results.toTableModel());
               r.show();
            }
            catch (IllegalArgumentException e) {
               error(e, "During class score calculation");
            }
            catch (IOException e) {
               error(e, "File reading or writing");
            }
            showStatus("Done");
            done = true;
            loadResults = false;

         }
      };

      runner = new runthread(m);
      runner.start();
   }

   /* quit */
   void jButtonQuit_actionPerformed(ActionEvent e) {
      System.exit(0);
   }

   void jButtonCancel_actionPerformed(ActionEvent e) {

      if (runner != null) {
         runner.stop();
      }
      try {
         Thread.sleep(200);
      }
      catch (InterruptedException ex) {
         Thread.currentThread().interrupt();
      }
      showStatus("Ready");
      System.err.println("Ready");
   }

   void jButtonGeneScoreFileBrowse_actionPerformed(ActionEvent e) {
      browse(jTextFieldGeneScoreFile);
   }

   void jButtonGONamesBrowse_actionPerformed(ActionEvent e) {
      browse(jTextFieldGONames);
   }

   void jButtonOutputFileNameBrowse_actionPerformed(ActionEvent e) {
      browse(jTextFieldOutPutFileName);
   }

   void jButtonProbeAnnotsBrowse_actionPerformed(ActionEvent e) {
      browse(jTextFieldProbeAnnot);
   }

   void jButtonSavePrefs_actionPerformed(ActionEvent e) {
      writePrefs();
   }

   /* About box */
   void jCheckBoxDoLog_actionPerformed(ActionEvent e) {
      if (jCheckBoxDoLog.isSelected()) {
         doLog = true;
      } else {
         doLog = false;
      }
   }

   /**
    *
    * @param e
    * @param message
    */
   public void error(Exception e, String message) {
      showStatus("Error: " + message + " (" + e.toString() + ")");
      JOptionPane.showMessageDialog(null,
                                    "Error: " + message + "\n" + e.toString() +
                                    "\n" + e.getStackTrace());
   }

   /**
    *
    * @param comp
    * @return
    */
   public String getString(JComboBox comp) {

      String selectedPath = (String) comp.getSelectedItem();
      if (selectedPath == null ||
          !selectedPath.equals(comp.getEditor().getItem())) {
         selectedPath = (String) comp.getEditor().getItem();
      }
      return selectedPath;
   }

   /**
    *
    * @param target
    */
   public void browse(JTextField target) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         target.setText(chooser.getSelectedFile().toString());
      }
   }

   /**
    *
    * @param inFilename
    * @return
    */
   public Vector comboReader(String inFilename) {
      File file = new File(inFilename);

      showStatus("Reading " + inFilename);

      if (file.exists() && file.isFile() && file.canRead()) {

         Vector fileList = new Vector();
         try {
            FileInputStream fis = new FileInputStream(inFilename);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
            while (dis.ready()) {
               String line = dis.readLine();
               fileList.add(line);
               //         System.err.println(line);
            }
            dis.close();
         }
         catch (IOException e) {
            // catch possible io errors from readLine()
            error(e, "Reading preferences.");
         }

         clearStatus();
         return fileList;
      } else {
         return null;
      }
   }

   /**
    *
    * @param a
    */
   private void showStatus(String a) {
      jLabelStatus.setText(a);
   }

   /**
    *
    */
   private void clearStatus() {
      jLabelStatus.setText("");
   }

   /**
    *
    * @param outFilename
    * @param names
    */
   public void comboWriter(String outFilename, String names) {
      try {
         BufferedWriter out = new BufferedWriter(new FileWriter(outFilename, false));
         showStatus("Writing preferences to " + outFilename);
         out.write(names + "\n");
         out.close();
      }
      catch (IOException e) {
         error(e, "Writing preferences");
      }
      clearStatus();
   }

   /**
    *
    * @param in
    * @return
    */
   private String getCanonical(String in) {
      if (in == null || in.length() == 0) {
         return in;
      }
      File outFile = new File(in);
      try {
         return outFile.getCanonicalPath();
      }
      catch (Exception e) {
         error(e, "Getting path for preferences file");
         return null;
      }
   }

   /**
    * About
    * @param e
    */
   void jButtonAbout_actionPerformed(ActionEvent e) {
      classScoreFrameAboutBox dlg = new classScoreFrameAboutBox(this);
      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                      (frmSize.height - dlgSize.height) / 2 + loc.y);
      dlg.setModal(true);
      dlg.pack();
      dlg.show();
   }

   /**
    *
    * @param e
    */
   void jButtonLoadResults_actionPerformed(ActionEvent e) {
      loadResults = true;
      this.jButtonRun_actionPerformed(e);
   }

   /**
    *
    * @param filename
    * @return
    */
   private boolean testfile(String filename) {
      if (filename != null && filename.length() > 0) {
         File f = new File(filename);
         if (f.exists()) {
            return true;
         } else {
            JOptionPane.showMessageDialog(null,
                                          "File " + filename + " doesn't exist.  ");
         }
         return false;
      } else {
         JOptionPane.showMessageDialog(null, "A required file field is blank.");
         return false;
      }
   }

   /**
    *
    */
   /*  private void writePrefs() {
    comboWriter(getCanonical("scoreFile.pref"), jTextFieldGeneScoreFile.getText()); // gene scores
       comboWriter(getCanonical("outputFile.pref"),
                   jTextFieldOutPutFileName.getText()); // output file
    comboWriter(getCanonical("nameFile.pref"), jTextFieldGONames.getText()); // biological names for go
    comboWriter(getCanonical("annotFile.pref"), jTextFieldProbeAnnot.getText()); // probe to ug map
     }
    */

   private void writePrefs(Properties s, String filename) {
      try {
         OutputStream f = new FileOutputStream(filename);
         s.store(f, "");
      }
      catch (IOException ex) {
         System.err.println("Error writing prefs.");
      }

   }

   private void writePrefs() {
      Properties settings = new Properties();
      settings.setProperty("scoreFile", jTextFieldGeneScoreFile.getText());
      settings.setProperty("outputFile", jTextFieldOutPutFileName.getText());
      settings.setProperty("GOnameFile", jTextFieldGONames.getText());
      settings.setProperty("annotFile", jTextFieldProbeAnnot.getText());
      settings.setProperty("scoreColumn", jTextFieldScoreCol.getText());
      settings.setProperty("minClassSize", jTextFieldMinClassSize.getText());
      settings.setProperty("maxClassSize", jTextFieldMaxClassSize.getText());
      settings.setProperty("pvalueThreshold", this.jTextFieldPValueThreshold.getText());
      settings.setProperty("doLog", getUseLog());
      writePrefs(settings, "classScore.prefs"); // default name.
   }

   /**
    *
    */
   private void readPrefs() {

      Properties settings = new Properties();
      try {
         File fi = new File("classScore.prefs");
         if (fi.canRead()) {
            InputStream f = new FileInputStream("classScore.prefs");
            settings.load(f);
         }
      }
      catch (IOException ex) {
         System.err.println("Could not find preferences file."); // no big deal.
      }

      if (settings.size() > 0) {
         jTextFieldGeneScoreFile.setText( (String) settings.get("scoreFile"));
         jTextFieldOutPutFileName.setText( (String) settings.get("outputFile"));
         jTextFieldGONames.setText( (String) settings.get("GOnameFile"));
         jTextFieldProbeAnnot.setText( (String) settings.get("annotFile"));
         this.jTextFieldMaxClassSize.setText( (String) settings.get("maxClassSize"));
         this.jTextFieldMinClassSize.setText( (String) settings.get("minClassSize"));
         this.jTextFieldScoreCol.setText( (String) settings.get("scoreColumn"));
         this.jTextFieldPValueThreshold.setText( (String) settings.get("pvalueThreshold"));
      }
   }

}

/* end class */

class classScoreFrame_jButtonRun_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonRun_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonRun_actionPerformed(e);
   }
}

class classScoreFrame_jButtonQuit_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonQuit_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonQuit_actionPerformed(e);
   }
}

class classScoreFrame_jButtonGONamesBrowse_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonGONamesBrowse_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonGONamesBrowse_actionPerformed(e);
   }
}

class classScoreFrame_jButtonOutputFileNameBrowse_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonOutputFileNameBrowse_actionAdapter(classScoreFrame
       adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonOutputFileNameBrowse_actionPerformed(e);
   }
}

class classScoreFrame_jButtonProbeAnnotsBrowse_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonProbeAnnotsBrowse_actionAdapter(classScoreFrame
       adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonProbeAnnotsBrowse_actionPerformed(e);
   }
}

class classScoreFrame_jCheckBoxDoLog_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jCheckBoxDoLog_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jCheckBoxDoLog_actionPerformed(e);
   }
}

class classScoreFrame_jButtonAbout_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonAbout_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonAbout_actionPerformed(e);
   }
}

class classScoreFrame_jButtonCancel_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonCancel_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonCancel_actionPerformed(e);
   }
}

class classScoreFrame_jButtonGeneScoreFileBrowse_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonGeneScoreFileBrowse_actionAdapter(classScoreFrame
       adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonGeneScoreFileBrowse_actionPerformed(e);
   }
}

class classScoreFrame_jButtonSavePrefs_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonSavePrefs_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonSavePrefs_actionPerformed(e);
   }
}

class classScoreFrame_jButtonLoadResults_actionAdapter
    implements java.awt.event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonLoadResults_actionAdapter(classScoreFrame adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.jButtonLoadResults_actionPerformed(e);
   }
}
