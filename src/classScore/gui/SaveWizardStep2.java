package classScore.gui;

import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import baseCode.gui.WizardStep;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
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
      chooser.setCurrentDirectory(new File(folder));
      chooser.setDialogTitle("Save Analysis As");
      wiz.clearStatus();
   }

   //Component initialization
   protected void jbInit(){
      jPanel11 = new JPanel();
      jPanel11.setPreferredSize(new Dimension(330, 50));
      jPanel11.setBackground(SystemColor.control);
      jLabel3 = new JLabel();
      jLabel3.setText("Save file:");
      jLabel3.setPreferredSize(new Dimension(320, 15));
      saveFile = new JTextField();
      saveFile.setPreferredSize(new Dimension(230, 19));
      saveBrowseButton = new JButton();
      saveBrowseButton.addActionListener(new SaveWizardStep2_saveBrowseButton_actionAdapter(this));
      saveBrowseButton.setText("Browse....");
      chooser = new JFileChooser();
      jPanel11.add(jLabel3, null);
      jPanel11.add(saveFile, null);
      jPanel11.add(saveBrowseButton, null);

      this.addHelp("<html>This is a place holder.<br>"+
                   "Blah, blah, blah, blah, blah.");
      this.addMain(jPanel11);
   }

   public boolean isReady() { return true; }

   void saveBrowseButton_actionPerformed(ActionEvent e) {
      int result = chooser.showOpenDialog(this.wiz);
      if (result == JFileChooser.APPROVE_OPTION) {
         saveFile.setText(chooser.getSelectedFile().toString());
      }
   }

   public String getSaveFileName() { return saveFile.getText(); }
}

class SaveWizardStep2_saveBrowseButton_actionAdapter implements java.awt.event.ActionListener {
   SaveWizardStep2 adaptee;
   SaveWizardStep2_saveBrowseButton_actionAdapter(SaveWizardStep2 adaptee) {
      this.adaptee = adaptee; }
   public void actionPerformed(ActionEvent e) {
      adaptee.saveBrowseButton_actionPerformed(e);
   }
}
