package classScore.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 * @author Homin Lee
 * @version $Id$
 */

public class AnalysisWizard extends Wizard {
    // logic
    int step = 1;
    int analysisType = Settings.ORA;

    Settings settings;
    GeneAnnotations geneData;
    GONames goData;
    AnalysisWizardStep1 step1;
    AnalysisWizardStep2 step2;
    AnalysisWizardStep3 step3;
    AnalysisWizardStep4 step4;
    AnalysisWizardStep5 step5;

    public AnalysisWizard( GeneSetScoreFrame callingframe, Map geneDataSets, GONames goData ) {
        super( callingframe, 550, 350 );
        // enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.callingframe = callingframe;
        this.settings = new Settings( callingframe.getSettings() ); // own copy of settings
        this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer( "original".hashCode() ) );
        this.goData = goData;

        step1 = new AnalysisWizardStep1( this, settings );
        this.addStep( 1, step1 );
        step2 = new AnalysisWizardStep2( this, settings );
        this.addStep( 2, step2 );
        step3 = new AnalysisWizardStep3( this, settings, goData );
        this.addStep( 3, step3 );
        step4 = new AnalysisWizardStep4( this, settings );
        this.addStep( 4, step4 );
        step5 = new AnalysisWizardStep5( this, settings );
        this.addStep( 5, step5 );
        this.setTitle( "Create New Analysis - Step 1 of 5" );

        // determine if the "finish" button should be disabled or not
        if ( settings.getRawFile().length() == 0 && settings.getScoreFile().length() == 0 ) {
            setFinishDisabled();
        } else {
            setFinishEnabled();
        }
    }

    protected void nextButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 1 && step1.isReady() ) {
            step = 2;
            step1.saveValues();
            this.getContentPane().remove( step1 );
            this.setTitle( "Create New Analysis - Step 2 of 5" );
            this.getContentPane().add( step2 );
            step2.revalidate();
            backButton.setEnabled( true );
            setFinishEnabled();
            this.repaint();
            nextButton.grabFocus();
            this.analysisType = settings.getAnalysisMethod();
        } else if ( step == 2 && step2.isReady() ) {
            step = 3;
            this.getContentPane().remove( step2 );
            this.setTitle( "Create New Analysis - Step 3 of 5" );
            this.getContentPane().add( step3 );
            step3.revalidate();
            this.repaint();
        } else if ( step == 3 ) {
            step = 4;
            this.getContentPane().remove( step3 );
            this.setTitle( "Create New Analysis - Step 4 of 5" );
            this.getContentPane().add( step4 );
            step4.revalidate();
            this.repaint();
        } else if ( step == 4 ) {
            step = 5;
            this.getContentPane().remove( step4 );
            step5.addVarPanel( analysisType );
            this.setTitle( "Create New Analysis - Step 5 of 5" );
            this.getContentPane().add( step5 );
            step5.revalidate();
            nextButton.setEnabled( false );
            this.repaint();
        }
    }

    protected void backButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 2 ) {
            step = 1;
            this.getContentPane().remove( step2 );
            this.setTitle( "Create New Analysis - Step 1 of 5" );
            this.getContentPane().add( step1 );
            step1.revalidate();
            backButton.setEnabled( false );
            this.repaint();
        } else if ( step == 3 ) {
            step = 2;
            this.getContentPane().remove( step3 );
            this.setTitle( "Create New Analysis - Step 2 of 5" );
            this.getContentPane().add( step2 );
            step2.revalidate();
            this.repaint();
        } else if ( step == 4 ) {
            step = 3;
            this.getContentPane().remove( step4 );
            this.setTitle( "Create New Analysis - Step 3 of 5" );
            this.getContentPane().add( step3 );
            step3.revalidate();
            this.repaint();
        } else if ( step == 5 ) {
            step = 4;
            step5.removeVarPanel( analysisType );
            this.getContentPane().remove( step5 );
            this.setTitle( "Create New Analysis - Step 4 of 5" );
            this.getContentPane().add( step4 );
            step4.revalidate();
            nextButton.setEnabled( true );
            this.repaint();
        }
    }

    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    protected void finishButton_actionPerformed( ActionEvent e ) {
        if ( step2.isReady() ) {
            try {
                loadAddedClasses();
            } catch ( IOException e1 ) {
                GuiUtil.error( "Could not load the custom classes: " + e + "\n"
                        + "If this problem persists, please contact the software vendor. " + "Press OK to quit." );
                System.exit( 1 );
            }
            ( ( GeneSetScoreFrame ) callingframe ).startAnalysis( settings );

            saveValues();
            dispose();
        }
        dispose();
    }

    void saveValues() {
        step1.saveValues();
        step2.saveValues();
        step4.saveValues();
        step5.saveValues();
        try {
            settings.writePrefs();
        } catch ( IOException e ) {
            GuiUtil.error( "Could not save preferences: " + e + "\n"
                    + "If this problem persists, please contact the software vendor. " + "Press OK to quit." );
            System.exit( 1 );
        }
    }

    void loadAddedClasses() throws IOException {
        Iterator it = step3.getAddedClasses().iterator();
        while ( it.hasNext() ) {
            String id = ( String ) ( ( HashMap ) it.next() ).get( "id" );
            if ( !goData.newSet( id ) ) {
                NewGeneSet newGeneSet = new NewGeneSet( geneData );
                String filename = NewGeneSet.getFileName( settings.getClassFolder(), id );
                newGeneSet.loadClassFile( filename );
                newGeneSet.addToMaps( goData );
            }
        }
    }

    /**
     * @param val
     */
    public void setAnalysisType( int val ) {
        this.analysisType = val;
    }

    /**
     * @return
     */
    public int getAnalysisType() {
        return this.analysisType;
    }

}