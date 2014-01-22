/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej.gui.analysis;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.gui.util.WizardStep;

/**
 * Generic settings common to all methods (range of gene set sizes, replicate probe handling)
 * 
 * @author Homin K Lee
 * @version $Id$
 */
public class AnalysisWizardStep4 extends WizardStep {
    private static final long serialVersionUID = -5916400713486392185L;
    private Settings settings;
    private JTextField jTextFieldMaxClassSize;
    private JTextField jTextFieldMinClassSize;
    private JRadioButton jRadioButtonBestReplicates;
    private JRadioButton jRadioButtonMeanReplicates;

    private KeyListener countNumGroupsKeyListener = new KeyAdapter() {
        @Override
        public void keyReleased( KeyEvent e ) {
            updateNumGeneSetsActive();
        }
    };

    public AnalysisWizardStep4( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.jbInit();
        this.settings = settings;
        // wiz.clearStatus();
        setValues();
    }

    @Override
    public boolean isReady() {
        AnalysisWizard wiz = ( AnalysisWizard ) getOwner();

        if ( wiz.getGeneAnnots().numActiveGeneSets() <= 0 ) {
            return false;
        }
        return true;
    }

    public void saveValues() {
        try {
            settings.setMaxClassSize( Integer.valueOf( jTextFieldMaxClassSize.getText() ).intValue() );
            settings.setMinClassSize( Integer.valueOf( jTextFieldMinClassSize.getText() ).intValue() );
        } catch ( NumberFormatException e ) {
            throw new IllegalStateException( "Size ranges must be numbers" );
        }

        if ( jRadioButtonBestReplicates.isSelected() ) {
            settings.setGeneRepTreatment( SettingsHolder.MultiElementHandling.BEST );
        } else if ( jRadioButtonMeanReplicates.isSelected() ) {
            settings.setGeneRepTreatment( SettingsHolder.MultiElementHandling.MEAN );
        } else {
            throw new IllegalStateException( "Invalid gene rep treatment method" );
        }
    }

    // Component initialization
    @Override
    protected void jbInit() {
        JPanel step4TopPanel = new JPanel();
        JPanel step4LeftPanel = new JPanel();
        JPanel jPanel17 = new JPanel();
        JLabel jLabel11 = new JLabel();
        jTextFieldMaxClassSize = new JTextField( 4 );
        JPanel jPanel16 = new JPanel();
        JLabel jLabel12 = new JLabel();
        jTextFieldMinClassSize = new JTextField( 4 );
        JPanel jPanelReplicateTreatmentsPanel = new JPanel();

        ButtonGroup replicateButtonGroup = new ButtonGroup();
        jRadioButtonBestReplicates = new JRadioButton();
        jRadioButtonMeanReplicates = new JRadioButton();

        jTextFieldMaxClassSize.addKeyListener( countNumGroupsKeyListener );
        jTextFieldMinClassSize.addKeyListener( countNumGroupsKeyListener );

        JPanel step4Panel = new JPanel();
        step4Panel.setPreferredSize( new Dimension( 550, 280 ) );

        step4LeftPanel.setPreferredSize( new Dimension( 200, 160 ) );
        jPanel17.setPreferredSize( new Dimension( 185, 90 ) );
        jLabel11.setText( "Maximum gene set size" );
        jLabel11.setLabelFor( jTextFieldMaxClassSize );
        jTextFieldMaxClassSize.setEditable( true );
        jTextFieldMaxClassSize.setMinimumSize( new Dimension( 50, 19 ) );
        jTextFieldMaxClassSize.setToolTipText( "Largest gene set size to be considered" );
        jTextFieldMaxClassSize.setText( "150" );
        jTextFieldMaxClassSize.setHorizontalAlignment( SwingConstants.RIGHT );
        jPanel17.add( jLabel11, null );
        jPanel17.add( jTextFieldMaxClassSize, null );
        jPanel16.setPreferredSize( new Dimension( 180, 90 ) );
        jLabel12.setLabelFor( jTextFieldMinClassSize );
        jLabel12.setText( "Minimum gene set size" );
        jTextFieldMinClassSize.setEditable( true );
        jTextFieldMinClassSize.setMinimumSize( new Dimension( 50, 19 ) );
        jTextFieldMinClassSize.setToolTipText( "Smallest gene set size to be considered" );
        jTextFieldMinClassSize.setText( "8" );
        jTextFieldMinClassSize.setHorizontalAlignment( SwingConstants.RIGHT );
        jPanel16.add( jLabel12, null );
        jPanel16.add( jTextFieldMinClassSize, null );
        step4LeftPanel.add( jPanel17, null );
        step4LeftPanel.add( jPanel16, null );

        jPanelReplicateTreatmentsPanel
                .setToolTipText( "How will multiple values for the same gene be treated? (doesn't apply to correlation analysis)" );
        jPanelReplicateTreatmentsPanel.setLayout( new GridLayout( 2, 1 ) );
        jPanelReplicateTreatmentsPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(),
                "Gene replicate treatment" ) );
        jPanelReplicateTreatmentsPanel.setMinimumSize( new Dimension( 175, 120 ) );
        // todo replicate treatments for correlation tooltip.
        jRadioButtonBestReplicates.setMinimumSize( new Dimension( 171, 23 ) );
        jRadioButtonBestReplicates
                .setToolTipText( "<html>If a gene occurs more than once, it is counted only once and the<br/>"
                        + "score used is that of the highest-scoring occurrence.</html>" );
        jRadioButtonBestReplicates.setSelected( true );
        jRadioButtonBestReplicates.setText( "Use Best scoring replicate" );

        jRadioButtonMeanReplicates.setMinimumSize( new Dimension( 171, 23 ) );
        jRadioButtonMeanReplicates
                .setToolTipText( "<html>If a gene occurs more than once, the gene is only counted once and<br/> "
                        + "the score is the mean of all occurrences.</html>" );
        jRadioButtonMeanReplicates.setSelected( false );
        jRadioButtonMeanReplicates.setText( "Use Mean of replicates" );
        replicateButtonGroup.add( jRadioButtonBestReplicates );
        replicateButtonGroup.add( jRadioButtonMeanReplicates );

        jPanelReplicateTreatmentsPanel.add( jRadioButtonBestReplicates );
        jPanelReplicateTreatmentsPanel.add( jRadioButtonMeanReplicates );

        this.addHelp( "<html>" + "<b>Choose"
                + " the range of class sizes to be considered and how genes occuring more than once are handled.</b>" );

        step4TopPanel.setLayout( new GridLayout( 1, 2 ) );
        step4TopPanel.add( step4LeftPanel );
        step4TopPanel.add( jPanelReplicateTreatmentsPanel );
        step4Panel.add( step4TopPanel );

        this.addMain( step4Panel );
    }

    /**
     * 
     */
    protected int updateNumGeneSetsActive() {
        AnalysisWizard wiz = ( AnalysisWizard ) getOwner();
        try {
            int maxSize = Integer.valueOf( jTextFieldMaxClassSize.getText() ).intValue();
            int minSize = Integer.valueOf( jTextFieldMinClassSize.getText() ).intValue();
            saveValues();
            int numActiveGeneSets = wiz.getGeneAnnots().numActiveGeneSets( minSize, maxSize );
            if ( numActiveGeneSets <= 0 ) {
                wiz.showError( numActiveGeneSets + " sets selected" );
            } else {
                wiz.showStatus( numActiveGeneSets + " sets selected" );
            }
            return numActiveGeneSets;
        } catch ( NumberFormatException e ) {
            // ok, fall back. Possibly just bail.

            wiz.showError( "? sets selected" );
            return wiz.getGeneAnnots().numActiveGeneSets();
        }
    }

    private void setValues() {
        jTextFieldMaxClassSize.setText( String.valueOf( settings.getMaxClassSize() ) );
        jTextFieldMinClassSize.setText( String.valueOf( settings.getMinClassSize() ) );

        if ( settings.getGeneRepTreatment().equals( SettingsHolder.MultiElementHandling.BEST ) ) {
            jRadioButtonBestReplicates.setSelected( true );
        } else if ( settings.getGeneRepTreatment().equals( SettingsHolder.MultiElementHandling.MEAN ) ) {
            jRadioButtonMeanReplicates.setSelected( true );
        } else {
            throw new IllegalStateException( "Invalid gene rep treatment method" );
        }
    }
}