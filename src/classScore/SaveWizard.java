package classScore;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SaveWizard extends Wizard
{
   //logic
   int step = 1;
   int selected_run;
   classScoreFrame callingframe;
   Thread aFrameRunner;
   String saveFolder;
   SaveWizardStep1 step1;
   SaveWizardStep2 step2;

   public SaveWizard(classScoreFrame callingframe, Vector rundata) {
      super(callingframe,400,200);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.callingframe = callingframe;
      System.err.println(callingframe.settings.get("saveFile"));

      step1 = new SaveWizardStep1(this,rundata);
      this.addStep(1,step1);
      step2 = new SaveWizardStep2(this,(String)callingframe.settings.get("saveFile"));
      this.addStep(2,step2);
      this.setTitle("Save Analysis - Step 1 of 2");
   }

   void selectRun(int i)
   {
      selected_run=i;
   }

   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
         if(step1.runsExist())
         {
            step = 2;
            this.getContentPane().remove(step1);
            this.setTitle("Save Analysis - Step 2 of 2");
            this.getContentPane().add(step2);
            step2.revalidate();
            backButton.setEnabled(true);
            nextButton.setEnabled(false);
            finishButton.setEnabled(true);
            finishButton.grabFocus();
            this.repaint();
         }
         else
         {
             error("No analyses to save.");
         }
      }
   }

   void backButton_actionPerformed(ActionEvent e) {
      if (step == 2) {
         step = 1;
         this.getContentPane().remove(step2);
         this.setTitle("Save Analysis - Step 1 of 2");
         this.getContentPane().add(step1);
         step1.revalidate();
         backButton.setEnabled(false);
         finishButton.setEnabled(false);
         nextButton.setEnabled(true);
         this.repaint();
      }
   }

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {
      dispose();
   }

}


