package classScore;

import java.awt.*;
import java.awt.event.*;
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

public class SaveWizardStep2 extends WizardStep
{
   SaveWizard wiz;
   String folder;
   JFileChooser chooser;
   JPanel jPanel11;
   JLabel jLabel3;
   JTextField saveFile;
   JButton saveBrowseButton;

   public SaveWizardStep2(SaveWizard wiz, String folder)
   {
      super(wiz);
      this.wiz=wiz;
      this.folder=folder;
   }

   //Component initialization
   void jbInit(){
      jPanel11 = new JPanel();
      jPanel11.setPreferredSize(new Dimension(330, 50));
      jPanel11.setBackground(SystemColor.control);
      jLabel3 = new JLabel();
      jLabel3.setText("Save file:");
      jLabel3.setPreferredSize(new Dimension(320, 15));
      saveFile = new JTextField();
      saveFile.setPreferredSize(new Dimension(230, 19));
      saveFile.setMinimumSize(new Dimension(4, 19));
      saveFile.setEnabled(false);
      saveFile.setRequestFocusEnabled(true);
      saveBrowseButton = new JButton();
      saveBrowseButton.setEnabled(true);
      saveBrowseButton.setFocusPainted(true);
      saveBrowseButton.addActionListener(new SaveWizardStep2_saveBrowseButton_actionAdapter(this));
      saveBrowseButton.setText("Browse....");
      chooser = new JFileChooser();
      //chooser.setCurrentDirectory(new File(folder));
      jPanel11.add(jLabel3, null);
      jPanel11.add(saveFile, null);
      jPanel11.add(saveBrowseButton, null);
      this.add(jPanel11);
   }

   void saveFile_actionPerformed(ActionEvent e) {

   }

   void saveBrowseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this.wiz);
      if (result == JFileChooser.APPROVE_OPTION) {
         saveFile.setText(chooser.getSelectedFile().toString());
      }
   }
}

class SaveWizardStep2_saveBrowseButton_actionAdapter implements java.awt.event.ActionListener {
   SaveWizardStep2 adaptee;
   SaveWizardStep2_saveBrowseButton_actionAdapter(SaveWizardStep2 adaptee) {
      this.adaptee = adaptee; }
   public void actionPerformed(ActionEvent e) {
      adaptee.saveBrowseButton_actionPerformed(e);
   }
}

class SaveWizardStep2_saveFile_actionAdapter implements java.awt.event.ActionListener {
   SaveWizardStep2 adaptee;
   SaveWizardStep2_saveFile_actionAdapter(SaveWizardStep2 adaptee) {
      this.adaptee = adaptee; }
   public void actionPerformed(ActionEvent e) {
      adaptee.saveFile_actionPerformed(e);
   }
}