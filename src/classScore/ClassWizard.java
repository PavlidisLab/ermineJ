package classScore;

import java.awt.event.*;
import java.util.*;
import baseCode.gui.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @ $Id$
 */

public class ClassWizard extends Wizard {
   Settings settings;
   GeneAnnotations geneData;
   GONames goData;
   ClassWizardStep1 step1;  // case 1 (manual creating) and case 2 (new from file)
   ClassWizardStep1A step1A; // case 3 (modifying existing)
   ClassWizardStep2 step2;   // step 2 for cases 1-3 and step 1 for case 4
   ClassWizardStep3 step3;

   int step;
   boolean makenew;
   boolean nostep1=false;
   NewGeneSet newGeneSet;
   String cid;

   public ClassWizard(classScoreFrame callingframe, GeneAnnotations geneData,
                      GONames goData, boolean makenew) {
      super(callingframe,550,350);
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();
      this.geneData = geneData;
      this.goData = goData;
      this.makenew = makenew;
      newGeneSet = new NewGeneSet(geneData);

      step=1;
      if (makenew) {
         this.setTitle("Define New Class - Step 1 of 3");
         step1 = new ClassWizardStep1(this,settings);
         this.addStep(1,step1);
      }
      else {
         this.setTitle("Modify Class - Step 1 of 3");
         step1A = new ClassWizardStep1A(this,geneData,goData,newGeneSet);
         this.addStep(1,step1A);
      }
      step2 = new ClassWizardStep2(this,geneData,newGeneSet);
      this.addStep(2,step2);
      step3 = new ClassWizardStep3(this,settings,geneData,newGeneSet,makenew);
      this.addStep(3,step3);

   }

   public ClassWizard(classScoreFrame callingframe, GeneAnnotations geneData,
                      GONames goData, String cid) {
      super(callingframe,550,350);
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();
      this.geneData = geneData;
      this.goData = goData;
      this.cid = cid;
      makenew=false;
      nostep1=true;
      newGeneSet = new NewGeneSet(geneData);
      this.setTitle("Modify Class - Step 2 of 3");
      step = 2;
      backButton.setEnabled(false);
      newGeneSet.setId(cid);
      newGeneSet.setDesc(goData.getNameForId(cid));
      if (geneData.classExists(cid))
         newGeneSet.getProbes().addAll((ArrayList) geneData.getClassToProbes(cid));
      this.repaint();
      step2 = new ClassWizardStep2(this,geneData,newGeneSet);
      this.addStep(1,step2); //hack for starting at step 2
      this.addStep(2,step2);
      step2.updateCountLabel();
      step3 = new ClassWizardStep3(this,settings,geneData,newGeneSet,makenew);
      this.addStep(3,step3);
   }

   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
         if (!makenew && !step1A.isReady()) {                       //case 3 and no class picked
            GuiUtil.error("Pick a class to be modified.");
         }
         else {
            if (makenew && step1.getInputMethod() == 1) {           //case 2, load from file
               newGeneSet.loadClassFile(step1.getLoadFile());
            }
            if (!(makenew && step1.getInputMethod() == 1 &&         //all cases (except case 2 and bad file)
                  newGeneSet.getId().compareTo("") == 0)) {
               if (makenew) {                                       //cases 1 & 2
                  this.getContentPane().remove(step1);
                  this.setTitle("Define New Class - Step 2 of 3");
               } else {                                             //case 3
                  this.getContentPane().remove(step1A);
                  this.setTitle("Modify Class - Step 2 of 3");
               }
               step = 2;
               backButton.setEnabled(true);
               this.getContentPane().add(step2);
               step2.revalidate();
               step2.updateCountLabel();
               this.repaint();
            }
         }
      }
       else if (step == 2) {
         this.getContentPane().remove(step2);
         step = 3;
         if (makenew) {
            this.setTitle("Define New Class - Step 3 of 3");
         } else {
            this.setTitle("Modify Class - Step 3 of 3");
         }
         backButton.setEnabled(true);
         nextButton.setEnabled(false);
         finishButton.setEnabled(true);
         step3.update();
         this.getContentPane().add(step3);
         step3.revalidate();
         this.repaint();
      }
   }

   void backButton_actionPerformed(ActionEvent e) {
      if (step == 2) {
         this.getContentPane().remove(step2);
         step = 1;
         backButton.setEnabled(false);
         if (makenew) {
            this.setTitle("Define New Class - Step 1 of 3");
            this.getContentPane().add(step1);
            step1.revalidate();
         } else {
            this.setTitle("Modify Class - Step 1 of 3");
            this.getContentPane().add(step1A);
            step1A.revalidate();
         }
         this.repaint();
      }
      if (step == 3) {
         this.getContentPane().remove(step3);
         step = 2;
         if (makenew) {
            this.setTitle("Define New Class - Step 2 of 3");
         } else {
            this.setTitle("Modify Class - Step 2 of 3");
            if(nostep1)
               backButton.setEnabled(false);
         }
         nextButton.setEnabled(true);
         finishButton.setEnabled(false);
         this.getContentPane().add(step2);
         step2.revalidate();
         this.repaint();
      }
   }

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {
      String id = newGeneSet.getId();
      String desc = newGeneSet.getDesc();
      if (id.compareTo("") == 0) {
         GuiUtil.error("The class ID must be specified.");
      } else {
         if (makenew) {
            System.err.println("adding " + id + " to setupmap");
            geneData.addClass(id, newGeneSet.getProbes());
            goData.addClass(id, desc);
            geneData.sortGeneSets();
         } else {
            //geneData.modifyClass(id, desc, newGeneSet.probes);
         }
         newGeneSet.saveClass(settings.getClassFolder(), 0);
         dispose();
      }
   }
}


