package classScore.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.GuiUtil;
import baseCode.gui.Wizard;
import classScore.Settings;
import classScore.data.NewGeneSet;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin K Lee @ $Id$
 */

public class GeneSetWizard extends Wizard {
   Settings settings;
   GeneAnnotations geneData;
   GONames goData;
   GeneSetWizardStep1 step1; // case 1 (manual creating) and case 2 (new from file)
   GeneSetWizardStep1A step1A; // case 3 (modifying existing)
   GeneSetWizardStep2 step2; // step 2 for cases 1-3 and step 1 for case 4
   GeneSetWizardStep3 step3;

   int step;
   boolean makenew;
   boolean nostep1 = false;
   NewGeneSet newGeneSet;
   NewGeneSet oldGeneSet;
   String cid;

   public GeneSetWizard( GeneSetScoreFrame callingframe,
         GeneAnnotations geneData, GONames goData, boolean makenew ) {
      super( callingframe, 550, 350 );
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();
      this.geneData = geneData;
      this.goData = goData;
      this.makenew = makenew;
      newGeneSet = new NewGeneSet( geneData );
      oldGeneSet = new NewGeneSet( geneData );

      geneData.resetSelectedProbes();
      step = 1;
      if ( makenew ) {
         this.setTitle( "Define New Gene Set - Step 1 of 3" );
         step1 = new GeneSetWizardStep1( this, settings );
         this.addStep( 1, step1 );
      } else {
         this.setTitle( "Modify Gene Set - Step 1 of 3" );
         step1A = new GeneSetWizardStep1A( this, geneData, goData, newGeneSet,
               oldGeneSet );
         this.addStep( 1, step1A );
      }
      step2 = new GeneSetWizardStep2( this, geneData, newGeneSet );
      this.addStep( 2, step2 );
      step3 = new GeneSetWizardStep3( this, settings, geneData, newGeneSet,
            makenew );
      this.addStep( 3, step3 );

      finishButton.setEnabled( false );
   }

   public GeneSetWizard( GeneSetScoreFrame callingframe,
         GeneAnnotations geneData, GONames goData, String cid ) {
      super( callingframe, 550, 350 );
      this.callingframe = callingframe;
      this.settings = callingframe.getSettings();
      this.geneData = geneData;
      this.goData = goData;
      this.cid = cid;
      makenew = false;
      nostep1 = true;
      newGeneSet = new NewGeneSet( geneData );
      oldGeneSet = new NewGeneSet( geneData );
      this.setTitle( "Modify Gene Set - Step 2 of 3" );
      step = 2;
      backButton.setEnabled( false );
      newGeneSet.setId( cid );
      newGeneSet.setDesc( goData.getNameForId( cid ) );
      newGeneSet.setModified( true );
      if ( geneData.geneSetExists( cid ) )
            newGeneSet.getProbes().addAll( geneData.getClassToProbes( cid ) );
      oldGeneSet.setId( cid );
      oldGeneSet.setDesc( goData.getNameForId( cid ) );
      oldGeneSet.setModified( true );
      if ( geneData.geneSetExists( cid ) )
            oldGeneSet.getProbes().addAll( geneData.getClassToProbes( cid ) );
      this.repaint();
      step2 = new GeneSetWizardStep2( this, geneData, newGeneSet );
      this.addStep( 1, step2 ); //hack for starting at step 2
      this.addStep( 2, step2 );
      step2.updateCountLabel();
      step3 = new GeneSetWizardStep3( this, settings, geneData, newGeneSet,
            makenew );
      this.addStep( 3, step3 );
   }

   protected void nextButton_actionPerformed( ActionEvent e ) {
      clearStatus();
      if ( step == 1 ) {
         if ( makenew || step1A.isReady() ) { //not (case 3 with no class picked)
            if ( makenew && step1.getInputMethod() == 1 ) { //case 2, load from file
               try {
                  newGeneSet.loadClassFile( step1.getLoadFile() );
               } catch ( IOException e1 ) {
                  GuiUtil.error( "Error loading class files info." );
               }
            }
            if ( !( makenew && step1.getInputMethod() == 1 && //all cases (except case 2 and bad file)
            newGeneSet.getId().compareTo( "" ) == 0 ) ) {
               if ( makenew ) { //cases 1 & 2
                  this.getContentPane().remove( step1 );
                  this.setTitle( "Define New Gene Set - Step 2 of 3" );
               } else { //case 3
                  this.getContentPane().remove( step1A );
                  this.setTitle( "Modify Gene Set - Step 2 of 3" );
               }
               step = 2;
               backButton.setEnabled( true );
               finishButton.setEnabled( false );
               this.getContentPane().add( step2 );
               step2.revalidate();
               step2.updateCountLabel();
               this.repaint();
            }
         }
      } else if ( step == 2 ) {
         if ( newGeneSet.getProbes().size() == 0 ) {
            showError( "You have not selected any genes/probes." );
            return;
         }

         this.getContentPane().remove( step2 );
         step = 3;
         if ( makenew ) {
            this.setTitle( "Define New Gene Set - Step 3 of 3" );
         } else {
            this.setTitle( "Modify Gene Set - Step 3 of 3" );
         }
         backButton.setEnabled( true );
         nextButton.setEnabled( false );
         finishButton.setEnabled( true );
         step3.update();
         this.getContentPane().add( step3 );
         step3.revalidate();
         this.repaint();
      }
   }

   protected void backButton_actionPerformed( ActionEvent e ) {
      clearStatus();
      if ( step == 2 ) {
         this.getContentPane().remove( step2 );
         step = 1;
         backButton.setEnabled( false );
         finishButton.setEnabled( false );
         if ( makenew ) {
            this.setTitle( "Define New Gene Set - Step 1 of 3" );
            this.getContentPane().add( step1 );
            step1.revalidate();
         } else {
            this.setTitle( "Modify Gene Set - Step 1 of 3" );
            this.getContentPane().add( step1A );
            newGeneSet.clear();
            step1A.revalidate();
         }
         this.repaint();
      }
      if ( step == 3 ) {
         this.getContentPane().remove( step3 );
         step = 2;
         if ( makenew ) {
            this.setTitle( "Define New Gene Set - Step 2 of 3" );
         } else {
            this.setTitle( "Modify Gene Set - Step 2 of 3" );
            if ( nostep1 ) backButton.setEnabled( false );
         }
         nextButton.setEnabled( true );
         finishButton.setEnabled( false );
         this.getContentPane().add( step2 );
         step2.revalidate();
         this.repaint();
      }
   }

   protected void cancelButton_actionPerformed( ActionEvent e ) {
      geneData.resetSelectedProbes();
      geneData.resetSelectedSets();
      dispose();
   }

   protected void finishButton_actionPerformed( ActionEvent e ) {
      step3.nameNewGeneSet();
      String id = newGeneSet.getId();
      if ( id.compareTo( "" ) == 0 ) {
         showError( "The gene set ID must be specified." );
      } else if ( geneData.geneSetExists( id ) && makenew ) {
         showError( "A gene set with the ID " + id + " already exists." );
      } else {
         if ( makenew || !newGeneSet.modified() )
            newGeneSet.addToMaps( goData );
         else {
            if ( newGeneSet.compare( oldGeneSet ) != 0 )
                  newGeneSet.modifyClass( goData );
         }
         try {
            newGeneSet.saveClass( settings.getClassFolder(), 0 );
         } catch ( IOException e1 ) {
            GuiUtil.error( "Error writing the new gene set to file:", e1 );
         }
         ( ( GeneSetScoreFrame ) callingframe ).addedNewGeneSet();
         dispose();
      }
   }
}

