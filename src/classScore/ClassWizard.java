package classScore;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

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
//   ClassWizardStep1 step1;
//   ClassWizardStep2 step2;
//   ClassWizardStep3 step3;
//   ClassWizardStep4 step4;

   int step = 1;
   int inputMethod = 0;
   boolean makenew;
   NewClass newclass;
   InitialMaps imaps;
   ClassPanel classpanel;
   String folder;
   String cid;

   public ClassWizard(classScoreFrame callingframe, boolean makenew, InitialMaps imap,
                      ClassPanel classpanel, String saveFolder, String cid) {
      super(callingframe,550,300);
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();

      this.makenew = makenew;
      this.imaps = imap;
      this.classpanel = classpanel;
      this.folder = saveFolder;
      this.cid = cid;

/*
      step1 = new ClassWizardStep1(this);
      this.addStep(1,step1);
      step2 = new ClassWizardStep2(this,settings);
      this.addStep(2,step2);
      step3 = new ClassWizardStep3(this,settings);
      this.addStep(3,step3);
      step4 = new ClassWizardStep4(this,settings);
      this.addStep(4,step4);
*/
   }

   void gotoStep2() {
/*
      newclass.id = cid;
      newclass.desc = imaps.goName.getNameForId(cid);
      if (imaps.classToProbe.containsKey(cid)) {
         newclass.probes.addAll((ArrayList) imaps.classToProbe.get(cid));
      }
      step2Panel.revalidate();
      updateCountLabel();
      this.repaint();
*/
   }

   void nextButton_actionPerformed(ActionEvent e) {
      if (step == 1) {
/*
         if (!makenew && newclass.id.compareTo("") == 0) {
            error("Pick a class to be modified.");
         } else {
            if (makenew && inputMethod == 1) {
               newclass.loadClassFile(classFile.getText());
            }
            if (!(inputMethod == 1 && newclass.id.compareTo("") == 0)) {
               if (makenew) {
                  this.getContentPane().remove(step1Panel);
                  this.setTitle("Define New Class - Step 2 of 3");
               } else {
                  this.getContentPane().remove(step1MPanel);
                  this.setTitle("Modify Class - Step 2 of 3");
               }
               step = 2;
               backButton.setEnabled(true);
               this.getContentPane().add(step2Panel);
               step2Panel.revalidate();
               updateCountLabel();
               this.repaint();
            }
         }
*/
      }
   /*
       else if (step == 2) {
         this.getContentPane().remove(step2Panel);
         step = 3;
         if (makenew) {
            this.setTitle("Define New Class - Step 3 of 3");
         } else {
            this.setTitle("Modify Class - Step 3 of 3");
         }
         backButton.setEnabled(true);
         classIDTF.setText(newclass.id);
         classDescTA.setText(newclass.desc);
         if (newclass.id.compareTo("") != 0) {
            classIDFinal.setText(newclass.id);
         }
         nextButton.setEnabled(false);
         finishButton.setEnabled(true);
         this.getContentPane().add(step3Panel);
         step3Panel.revalidate();
         this.repaint();
      }
*/
   }

   void backButton_actionPerformed(ActionEvent e) {
  /*
      if (step == 2) {
         this.getContentPane().remove(step2Panel);
         step = 1;
         backButton.setEnabled(false);
         if (makenew) {
            this.setTitle("Define New Class - Step 1 of 3");
            this.getContentPane().add(step1Panel);
            step1Panel.revalidate();
         } else {
            this.setTitle("Modify Class - Step 1 of 3");
            this.getContentPane().add(step1MPanel);
            step1MPanel.revalidate();
         }
         this.repaint();
      }
      if (step == 3) {
         this.getContentPane().remove(step3Panel);
         step = 2;
         if (makenew) {
            this.setTitle("Define New Class - Step 2 of 3");
         } else {
            this.setTitle("Modify Class - Step 2 of 3");
         }
         nextButton.setEnabled(true);
         finishButton.setEnabled(false);
         this.getContentPane().add(step2Panel);
         step2Panel.revalidate();
         this.repaint();
      }
   */

   }

   void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   void finishButton_actionPerformed(ActionEvent e) {
/*
      String id = newclass.id;
      String desc = newclass.desc;
      if (id.compareTo("") == 0) {
         error("The class ID must be specified.");
      } else {
         if (makenew) {
            imaps.addClass(id, desc, newclass.probes);
         } else {
            imaps.modifyClass(id, desc, newclass.probes);
         }
         newclass.saveClass(folder, 0);
         classpanel.setModel(imaps.toTableModel());
         dispose();
      }
*/
   }

   void saveValues(){
/*
      step2.saveValues();
      step4.saveValues();
      try{
         settings.writePrefs();
      } catch (IOException ex) {
         System.err.println("Could not write prefs:" + ex);
         ex.printStackTrace();
      }
*/
   }
}


