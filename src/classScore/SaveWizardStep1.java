package classScore;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

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
   Vector rundata;
   JPanel runPanel;
   JComboBox runComboBox;
   JLabel runLabel;

   public SaveWizardStep1(SaveWizard wiz, Vector rundata)
   {
      super(wiz);
      this.wiz=wiz;
      this.rundata=rundata;
      showChoices();
      if(this.rundata==null)
         System.err.println("3 data null");
      else
         System.err.println("there are "+this.rundata.size()+ " runs");
   }

   //Component initialization
   void jbInit() throws Exception{
      runPanel = new JPanel();
      runLabel = new JLabel();
      runLabel.setText("Choose the analysis to save:");
      runComboBox = new JComboBox();
      runComboBox.addActionListener(new SaveWizardStep1_runComboBox_actionAdapter(this));

      runPanel.add(runLabel, null);
      runPanel.add(runComboBox, null);
      this.add(runPanel);
   }

   void showChoices()
   {
      if(rundata==null)
         System.err.println("data null");
      else
         System.err.println("there are "+rundata.size()+ " runs");
      if(rundata==null || rundata.size()<1)
         runComboBox.addItem("No runs available to save");
      else
      {
          for(int i=0; i<rundata.size(); i++)
          {
             Map data = (Map) rundata.get(i);
             runComboBox.addItem("Run "+(i+1));
          }
      }
   }

  void runComboBox_actionPerformed(ActionEvent e) {

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
