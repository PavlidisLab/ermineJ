package classScore;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StartupDialog extends JDialog {
   JPanel mainPanel;
   JFileChooser chooser = new JFileChooser();

   //holds bottom buttons
   JPanel BottomPanel = new JPanel();
   JButton cancelButton = new JButton();
   JButton finishButton = new JButton();

   JPanel step2Panel = new JPanel();
   JPanel jPanel7 = new JPanel();
   JLabel step2NameFile = new JLabel();
   JTextField nameFile = new JTextField();
   JButton probeBrowseButton = new JButton();
   JLabel step2ProbeLabel = new JLabel();
   JPanel step2ProbePanel = new JPanel();
   JTextField probeFile = new JTextField();


   classScoreFrame callingframe;
   Settings settings;

   public StartupDialog(classScoreFrame callingframe) {
      setModal(true);
      //enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();
      try {
         jbInit();
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
      cancelButton.setText("Cancel");
      cancelButton.addActionListener(new
                                     StartupDialog_cancelButton_actionAdapter(this));
      finishButton.setAlignmentY((float) 0.5);
      finishButton.setText("Finish");
      finishButton.addActionListener(new
                                     StartupDialog_finishButton_actionAdapter(this));
      nameFile.setEditable(false);
      BottomPanel.add(cancelButton, null);
      BottomPanel.add(finishButton, null);
      mainPanel.add(BottomPanel, BorderLayout.SOUTH);


      jPanel7.setBackground(SystemColor.control);
      jPanel7.setPreferredSize(new Dimension(330, 50));
      step2NameFile.setPreferredSize(new Dimension(320, 15));
      step2NameFile.setText("Gene name file:");
      nameFile.setMinimumSize(new Dimension(4, 19));
      nameFile.setEnabled(false);
      nameFile.setPreferredSize(new Dimension(315, 19));
      nameFile.setToolTipText("");
      jPanel7.add(step2NameFile, null);
      jPanel7.add(nameFile, null);
      probeBrowseButton.setEnabled(true);
      probeBrowseButton.setText("Browse....");
      probeBrowseButton.addActionListener(new
                                          StartupDialog_probeBrowseButton_actionAdapter(this));
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
      step2Panel.add(jPanel7, null);
      step2Panel.add(step2ProbePanel, null);


      mainPanel.add(step2Panel);
      this.getRootPane().setDefaultButton(finishButton);
      this.setTitle("Create New Analysis - Step 1 of 4");
   }

   private void setValues() {
      nameFile.setText( settings.getClassFile());
      probeFile.setText(settings.getAnnotFile());
      chooser.setCurrentDirectory(new File(settings.getDataFolder()));
   }

   private void saveValues(){
      settings.setClassFile(nameFile.getText());
      settings.setAnnotFile(probeFile.getText());
      try{
         settings.writePrefs();
      } catch (IOException ex) {
         System.err.println("Could not write prefs:" + ex);
         ex.printStackTrace();
      }
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

   void nameBrowseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
         nameFile.setText(chooser.getSelectedFile().toString());
      }
   }



   //bottom button actions//////////////////////////////////////////////////////

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {
      saveValues();
      dispose();
   }
}


class StartupDialog_rawFile_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_rawFile_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.rawFile_actionPerformed(e);
   }
}


class StartupDialog_scoreFile_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_scoreFile_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.scoreFile_actionPerformed(e);
   }
}


class StartupDialog_nameFile_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_nameFile_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.nameFile_actionPerformed(e);
   }
}


class StartupDialog_outputFile_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_outputFile_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.outputFile_actionPerformed(e);
   }
}

class StartupDialog_probeFile_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_probeFile_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.probeFile_actionPerformed(e);
   }
}


class StartupDialog_probeBrowseButton_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_probeBrowseButton_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.probeBrowseButton_actionPerformed(e);
   }
}



class StartupDialog_cancelButton_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_cancelButton_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.cancelButton_actionPerformed(e);
   }
}


class StartupDialog_finishButton_actionAdapter implements java.awt.event.
        ActionListener {
   StartupDialog adaptee;

   StartupDialog_finishButton_actionAdapter(StartupDialog adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.finishButton_actionPerformed(e);
   }
}
