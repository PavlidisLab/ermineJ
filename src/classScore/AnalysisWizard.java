package classScore;

import java.awt.event.*;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AnalysisWizard extends Wizard
{
   //logic
   int step = 1;
   int analysisType = 1;

   Settings settings;
   AnalysisWizardStep1 step1;
   AnalysisWizardStep2 step2;
   AnalysisWizardStep3 step3;
   AnalysisWizardStep4 step4;

   public AnalysisWizard(classScoreFrame callingframe) {
      super(callingframe,550,350);
      //enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.callingframe = callingframe;
      this.settings = new Settings(callingframe.getSettings());

      step1 = new AnalysisWizardStep1(this);
      this.addStep(1,step1);
      step2 = new AnalysisWizardStep2(this,settings);
      this.addStep(2,step2);
      step3 = new AnalysisWizardStep3(this,settings);
      this.addStep(3,step3);
      step4 = new AnalysisWizardStep4(this,settings);
      this.addStep(4,step4);
      this.setTitle("Create New Analysis - Step 1 of 4");
   }

   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
         step = 2;
         this.getContentPane().remove(step1);
         this.setTitle("Create New Analysis - Step 2 of 4");
         this.getContentPane().add(step2);
         step2.revalidate();
         backButton.setEnabled(true);
         this.repaint();
         nextButton.grabFocus();
      } else if (step == 2 && step2.isReady())
         {
            step = 3;
            this.getContentPane().remove(step2);
            this.setTitle("Create New Analysis - Step 3 of 4");
            this.getContentPane().add(step3);
            step3.revalidate();
            this.repaint();
         }
      else if (step == 3) {
         step = 4;
         this.getContentPane().remove(step3);
         step4.addVarPanel(analysisType);
         this.setTitle("Create New Analysis - Step 4 of 4");
         this.getContentPane().add(step4);
         step4.revalidate();
         nextButton.setEnabled(false);
         this.repaint();
      }
   }

   void backButton_actionPerformed(ActionEvent e) {
      if (step == 2) {
         step = 1;
         this.getContentPane().remove(step2);
         this.setTitle("Create New Analysis - Step 1 of 4");
         this.getContentPane().add(step1);
         step1.revalidate();
         backButton.setEnabled(false);
         this.repaint();
      } else if (step == 3) {
         step = 2;
         this.getContentPane().remove(step3);
         this.setTitle("Create New Analysis - Step 2 of 4");
         this.getContentPane().add(step2);
         step2.revalidate();
         this.repaint();
      } else if (step == 4) {
         step = 3;
         step4.removeVarPanel(analysisType);
         this.getContentPane().remove(step4);
         this.setTitle("Create New Analysis - Step 3 of 4");
         this.getContentPane().add(step3);
         step3.revalidate();
         nextButton.setEnabled(true);
         this.repaint();
      }
   }

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {
      saveValues();
      ((classScoreFrame)callingframe).startAnalysis(settings);
      dispose();
   }

   void saveValues(){
      step2.saveValues();
      step4.saveValues();
      try{
         settings.writePrefs();
      } catch (IOException ex) {
         System.err.println("Could not write prefs:" + ex);
         ex.printStackTrace();
      }
   }

   public void setAnalysisType(int val) { this.analysisType = val; }
   public int getAnalysisType() { return this.analysisType; }


}
