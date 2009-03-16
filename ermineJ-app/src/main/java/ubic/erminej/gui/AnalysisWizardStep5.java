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
package ubic.erminej.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemColor;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import ubic.erminej.Settings;

/**
 * The last step of the analysis wizard -- actually step 6 usually.
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class AnalysisWizardStep5 extends WizardStep {
    /**
     * 
     */
    private static final long serialVersionUID = 2682780215238903138L;
    private Settings settings;
    private JPanel step5Panel;
    private JRadioButton jRadioButtonMedian;
    private JRadioButton jRadioButtonMean;
    private JTextField jTextFieldPValueThreshold;
    // JTextField jTextFieldScoreCol;
    private JTextField jTextFieldIterations;
    private JCheckBox jCheckBoxDoLog;
    private JCheckBox jCheckBoxBigIsBetter;
    private JPanel oraPanel;
    private JPanel resampPanel;
    private JPanel rocPanel;
    private JPanel corrPanel;
    private JPanel numIterationsPanel;
    private JPanel useResamplingPanel;
    private JPanel subPanel;

    JCheckBox jCheckBoxUseEmpirical;
    private String help;
    private String extraHelp;

    public AnalysisWizardStep5( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.jbInit();
        this.settings = settings;
        wiz.clearStatus();
        setValues();
    }

    // Component initialization
    @Override
    protected void jbInit() {
        JPanel step4TopPanel = new JPanel();
        JPanel jPanelAnalysisFrameMethods = new JPanel();
        JLabel jLabelAnalysisFrameMethod = new JLabel();
        ButtonGroup buttonGroup2 = new ButtonGroup();
        jRadioButtonMedian = new JRadioButton();
        jRadioButtonMean = new JRadioButton();

        oraPanel = new JPanel();
        TitledBorder oraTitledBorder;
        JPanel jPanel15 = new JPanel();
        JLabel jLabel6 = new JLabel();
        jTextFieldPValueThreshold = new JTextField();
        resampPanel = new JPanel();
        TitledBorder resampTitledBorder;

        numIterationsPanel = new JPanel();
        JLabel numIterationsLabel = new JLabel();
        jTextFieldIterations = new JTextField();
        corrPanel = new JPanel();
        TitledBorder corrTitledBorder;
        JPanel corrMetricPanel = new JPanel();
        ButtonGroup corrButtonGroup = new ButtonGroup();
        JLabel corrMetricLabel = new JLabel();
        JRadioButton corrRadioButton1 = new JRadioButton();
        JRadioButton corrRadioButton2 = new JRadioButton();
        jCheckBoxDoLog = new JCheckBox();

        jCheckBoxBigIsBetter = new JCheckBox();

        step5Panel = new JPanel();
        step5Panel.setPreferredSize( new Dimension( 550, 280 ) );

        jCheckBoxDoLog.setBackground( SystemColor.control );
        jCheckBoxDoLog.setToolTipText( "If you are loading raw p values, you should check this box." );
        jCheckBoxDoLog.setSelected( true );
        jCheckBoxDoLog.setText( "Take the negative log of the gene scores" );

        jCheckBoxBigIsBetter.setToolTipText( "If you are loading raw p values, you should UNcheck this box." );
        jCheckBoxBigIsBetter.setSelected( false );
        jCheckBoxBigIsBetter.setText( "Larger scores in your gene score file are better." );

        // roc pane
        rocPanel = new JPanel();
        rocPanel.setPreferredSize( new Dimension( 335, 150 ) );
        rocPanel.setBorder( new TitledBorder( "ROC" ) );

        // oraPanel stuff//////////////////////////////////////////////////////////
        oraPanel.setPreferredSize( new Dimension( 335, 150 ) );
        oraTitledBorder = new TitledBorder( "ORA" );
        oraPanel.setBorder( oraTitledBorder );
        jPanelAnalysisFrameMethods.setBorder( null );
        jPanelAnalysisFrameMethods.setMinimumSize( new Dimension( 150, 37 ) );
        jPanelAnalysisFrameMethods.setPreferredSize( new Dimension( 150, 45 ) );
        jLabelAnalysisFrameMethod.setMaximumSize( new Dimension( 167, 18 ) );
        jLabelAnalysisFrameMethod.setMinimumSize( new Dimension( 167, 18 ) );
        jLabelAnalysisFrameMethod.setToolTipText( "Determines how the gene scores are combined to make a class score." );
        jLabelAnalysisFrameMethod.setText( "Class Raw Score Method" );
        jRadioButtonMedian.setText( "Median" );
        jRadioButtonMedian.setToolTipText( "The score for a class is the median of the score of genes in the "
                + "class." );
        jRadioButtonMedian.setBackground( SystemColor.control );
        jRadioButtonMean.setBackground( SystemColor.control );
        jRadioButtonMean.setToolTipText( "The raw score for the class is the mean of the scores for genes in "
                + "the class" );
        jRadioButtonMean.setSelected( true );
        jRadioButtonMean.setText( "Mean" );
        buttonGroup2.add( jRadioButtonMean );
        buttonGroup2.add( jRadioButtonMedian );
        jPanelAnalysisFrameMethods.add( jLabelAnalysisFrameMethod, null );
        jPanelAnalysisFrameMethods.add( jRadioButtonMean, null );
        jPanelAnalysisFrameMethods.add( jRadioButtonMedian, null );
        step5Panel.add( step4TopPanel, null );
        jPanel15.setMinimumSize( new Dimension( 180, 29 ) );

        // stuff to set pvalue threshold.
        jLabel6.setLabelFor( jTextFieldPValueThreshold );
        jLabel6.setText( "Gene score threshold" );
        jTextFieldPValueThreshold.setEditable( true );
        jTextFieldPValueThreshold.setPreferredSize( new Dimension( 50, 19 ) );
        jTextFieldPValueThreshold.setToolTipText( "Score Threshold used for Over-Representation analysis" );
        jTextFieldPValueThreshold.setText( "0.001" ); // default.
        jTextFieldPValueThreshold.setHorizontalAlignment( SwingConstants.RIGHT );
        jPanel15.add( jLabel6, null );
        jPanel15.add( jTextFieldPValueThreshold, null );
        oraPanel.add( jPanel15, null );

        // resampPanel stuff///////////////////////////////////////////////////////
        resampPanel.setPreferredSize( new Dimension( 380, 250 ) );
        resampPanel.add( jPanelAnalysisFrameMethods, null );
        resampTitledBorder = new TitledBorder( "Resampling" );
        resampPanel.setBorder( resampTitledBorder );

        subPanel = new JPanel();
        subPanel.setLayout( new BorderLayout() );

        numIterationsPanel.setBorder( null );
        numIterationsLabel.setMaximumSize( new Dimension( 100, 15 ) );
        numIterationsLabel.setLabelFor( jTextFieldIterations );
        numIterationsLabel.setText( "Maximum iterations to run" );
        jTextFieldIterations.setHorizontalAlignment( SwingConstants.RIGHT );
        jTextFieldIterations.setText( "10000" );
        jTextFieldIterations.setToolTipText( "Maximum number of iterations run per gene set size." );
        jTextFieldIterations.setPreferredSize( new Dimension( 70, 19 ) );
        jTextFieldIterations.setEditable( true );
        numIterationsPanel.add( numIterationsLabel, null );
        numIterationsPanel.add( jTextFieldIterations, null );

        useResamplingPanel = new JPanel();
        jCheckBoxUseEmpirical = new JCheckBox();
        JLabel useResamplingLabel = new JLabel();
        useResamplingLabel.setLabelFor( jCheckBoxUseEmpirical );
        jCheckBoxUseEmpirical.setSelected( false );
        jCheckBoxUseEmpirical.setHorizontalAlignment( SwingConstants.RIGHT );
        useResamplingLabel.setText( "Always use full resampling (slower)" );
        jCheckBoxUseEmpirical.setToolTipText( "If this box is unchecked, " + "some approximations are used which can"
                + " dramatically speed up the resampling," + " at a possible risk of reduced accuracy" );
        useResamplingPanel.add( useResamplingLabel, null );
        useResamplingPanel.add( jCheckBoxUseEmpirical, null );
        // subPanel.setPreferredSize( new java.awt.Dimension( 340, 80 ) );

        subPanel.add( numIterationsPanel, BorderLayout.NORTH );
        subPanel.add( useResamplingPanel, BorderLayout.SOUTH );

        // corrPanel stuff/////////////////////////////////////////////////////////
        corrPanel.setPreferredSize( new java.awt.Dimension( 380, 150 ) );
        corrTitledBorder = new TitledBorder( "Correlation" );
        corrPanel.setBorder( corrTitledBorder );
        corrMetricPanel.setPreferredSize( new Dimension( 150, 50 ) );
        corrMetricPanel.setMinimumSize( new Dimension( 150, 37 ) );
        corrMetricPanel.setBorder( null );
        corrMetricPanel.setBackground( SystemColor.control );
        corrMetricPanel.setBackground( SystemColor.control );
        corrMetricPanel.setToolTipText( "metric tool tip." );
        corrMetricLabel.setText( "Correlation Metric" );
        corrMetricLabel.setToolTipText( "metric tool tip." );
        corrMetricLabel.setMinimumSize( new Dimension( 167, 18 ) );
        corrMetricLabel.setMaximumSize( new Dimension( 167, 18 ) );
        corrRadioButton1.setText( "Metric 1" );
        corrRadioButton1.setSelected( true );
        corrRadioButton1.setBackground( SystemColor.control );
        corrRadioButton1.setToolTipText( "metric 1 tool tip" );
        corrRadioButton2.setText( "Metric 2" );
        useResamplingPanel.setPreferredSize( new java.awt.Dimension( 330, 30 ) );
        numIterationsPanel.setPreferredSize( new java.awt.Dimension( 234, 30 ) );
        corrButtonGroup.add( corrRadioButton1 );
        corrButtonGroup.add( corrRadioButton2 );
        corrMetricPanel.add( corrMetricLabel, null );
        corrMetricPanel.add( corrRadioButton1, null );
        corrMetricPanel.add( corrRadioButton2, null );
        // corrPanel.add(corrMetricPanel, null); // @todo disabled because there is no choice of metric.

        this.addHelp( help );
        help = "<html><b>Adjust settings specific for your analysis method.</b><br>";

        extraHelp = help
                + "Take special care to ensure the"
                + " log transformation and 'larger scores are better' settings are correct. 'larger scores are better' refers to your "
                + " original input file, and should be unchecked if your input is raw p-values.";

        this.addMain( step5Panel );
    }

    /**
     * @param analysisType
     */
    public void addVarPanel( int analysisType ) {
        if ( analysisType == Settings.ORA ) {
            oraPanel.add( jCheckBoxDoLog, null );
            oraPanel.add( jCheckBoxBigIsBetter, null );
            step5Panel.add( oraPanel, null );
            this.addHelp( extraHelp );
        } else if ( analysisType == Settings.RESAMP ) {
            resampPanel.add( jCheckBoxDoLog, null );
            resampPanel.add( jCheckBoxBigIsBetter, null );
            resampPanel.add( subPanel, null );
            this.addHelp( extraHelp );
            step5Panel.add( resampPanel, null );
        } else if ( analysisType == Settings.ROC ) {
            rocPanel.add( jCheckBoxDoLog, null );
            rocPanel.add( jCheckBoxBigIsBetter, null );
            this.addHelp( extraHelp );
            step5Panel.add( rocPanel, null );
        } else if ( analysisType == Settings.CORR ) {
            corrPanel.add( subPanel, null );
            this.addHelp( help );
            step5Panel.add( corrPanel, null );
        }
    }

    /**
     * @param analysisType
     */
    public void removeVarPanel( int analysisType ) {
        if ( analysisType == Settings.ORA ) {
            step5Panel.remove( oraPanel );
        } else if ( analysisType == Settings.RESAMP ) {
            resampPanel.remove( subPanel );
            step5Panel.remove( resampPanel );
        } else if ( analysisType == Settings.CORR ) {
            corrPanel.remove( subPanel );
            step5Panel.remove( corrPanel );
        } else if ( analysisType == Settings.ROC ) {
            step5Panel.remove( rocPanel );
        }
    }

    /**
     * 
     *
     */
    private void setValues() {
        jTextFieldIterations.setText( String.valueOf( settings.getIterations() ) );

        if ( settings.getRawScoreMethod() == Settings.MEAN_METHOD ) {
            jRadioButtonMean.setSelected( true );
        } else {
            jRadioButtonMedian.setSelected( true );
        }

        jTextFieldPValueThreshold.setText( String.valueOf( settings.getPValThreshold() ) );
        jCheckBoxDoLog.setSelected( settings.getDoLog() );
        jCheckBoxBigIsBetter.setSelected( settings.getBigIsBetter() );
        jCheckBoxUseEmpirical.setSelected( settings.getAlwaysUseEmpirical() );
    }

    /**
     * 
     *
     */
    public void saveValues() {
        settings.setIterations( Integer.valueOf( jTextFieldIterations.getText() ).intValue() );

        if ( jRadioButtonMean.isSelected() ) {
            settings.setRawScoreMethod( Settings.MEAN_METHOD );
        } else {
            settings.setRawScoreMethod( Settings.QUANTILE_METHOD );
        }

        settings.setPValThreshold( Double.valueOf( jTextFieldPValueThreshold.getText() ).doubleValue() );
        settings.setDoLog( jCheckBoxDoLog.isSelected() );
        settings.setBigIsBetter( jCheckBoxBigIsBetter.isSelected() );
        settings.setAlwaysUseEmpirical( jCheckBoxUseEmpirical.isSelected() );
    }

    @Override
    public boolean isReady() {
        return true;
    }
}