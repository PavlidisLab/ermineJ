package classScore.gui;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import baseCode.gui.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SaveWizardStep1 extends WizardStep
{
   SaveWizard wiz;
   LinkedList rundata;
   JPanel runPanel;
   JComboBox runComboBox;
   JLabel runLabel;
   BorderLayout borderLayout;
   boolean runs_exist;

   public SaveWizardStep1(SaveWizard wiz, LinkedList rundata)
   {
      super(wiz);
      this.wiz=wiz;
      this.rundata=rundata;
      showChoices();
   }

   //Component initialization
   protected void jbInit() throws Exception{
      runPanel = new JPanel();
      borderLayout = new BorderLayout();
      runPanel.setLayout(borderLayout);
      JPanel topPanel = new JPanel();
      runLabel = new JLabel();
      runLabel.setText("Choose the analysis to save:");
      topPanel.add(runLabel);
      JPanel centerPanel = new JPanel();
      runComboBox = new JComboBox();
      runComboBox.addActionListener(new SaveWizardStep1_runComboBox_actionAdapter(this));
      centerPanel.add(runComboBox);
      runPanel.add(topPanel, BorderLayout.NORTH);
      runPanel.add(centerPanel, BorderLayout.CENTER);
      this.add(runPanel);
   }

   public boolean isReady() { return true; }

   void showChoices()
   {
      if(rundata==null || rundata.size()<1)
      {
         runComboBox.addItem("No runs available to save");
         runs_exist=false;
      }
      else
      {
          runs_exist=true;
          for(int i=0; i<rundata.size(); i++)
          {
             runComboBox.insertItemAt("Run "+(i+1),i);
          }
          runComboBox.setSelectedIndex(0);
      }
   }

   public int getSelectedRunNum() { return runComboBox.getSelectedIndex(); }

   public boolean runsExist() { return runs_exist; }

   void runComboBox_actionPerformed(ActionEvent e) {
      wiz.selectRun(runComboBox.getSelectedIndex());
   }

}

class SaveWizardStep1_runComboBox_actionAdapter implements java.awt.event.ActionListener {
  SaveWizardStep1 adaptee;
  SaveWizardStep1_runComboBox_actionAdapter(SaveWizardStep1 adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.runComboBox_actionPerformed(e);
  }
}
