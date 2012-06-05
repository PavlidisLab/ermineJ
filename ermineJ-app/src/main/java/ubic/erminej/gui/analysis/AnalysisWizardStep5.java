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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.StringUtils;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.SettingsHolder.GeneScoreMethod;
import ubic.erminej.SettingsHolder.Method;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneScores;
import ubic.erminej.gui.util.WizardStep;
import cern.jet.math.Arithmetic;

/**
 * The last step of the analysis wizard, picking method-specific settings.
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class AnalysisWizardStep5 extends WizardStep {

    private static final long serialVersionUID = 2682780215238903138L;
    private Settings settings;
    private JPanel step5Panel;
    private JRadioButton jRadioButtonMedian;
    private JRadioButton jRadioButtonMean;

    private JPanel oraPanel;
    private JPanel resampPanel;
    private JPanel prPanel;
    private JPanel rocPanel;
    private JPanel corrPanel;

    private String help;
    private String extraHelp;
    boolean enableMultifuncCheckbox = true;

    // start with reasonable defaults.
    private AtomicInteger numIterations = new AtomicInteger( 10000 );

    private boolean doFullEmpirical = false;

    private AtomicReference<Double> oraThreshold = new AtomicReference<Double>( 0.001 );

    private boolean bigIsBetter = false;

    private boolean doLog = true;

    private boolean doMfCorr = true;

    public AnalysisWizardStep5( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.settings = settings;
        this.jbInit();
        wiz.clearStatus();
        setValues();
    }

    /**
     * @param analysisType
     */
    public void addVarPanel( Method analysisType, GeneScoreMethod scoreMethod ) {

        /*
         * this is for during development, so we don't expose this functionality prematurely: set to false to turn it
         * off. It doesn't turn off multifunctionality 'correction', just the ability of the user to set it.
         */
        enableMultifuncCheckbox = settings.getConfig().getBoolean( "multifunc.correct.enabled", false );

        if ( analysisType.equals( Method.ORA ) ) {
            step5Panel.add( oraPanel );
            this.addHelp( extraHelp );
            this.checkOraThresholdEffects();
        } else if ( analysisType.equals( Method.GSR ) ) {
            this.addHelp( extraHelp );

            if ( scoreMethod != null && scoreMethod.equals( GeneScoreMethod.PRECISIONRECALL ) ) {
                step5Panel.add( prPanel, null );
            } else {
                step5Panel.add( resampPanel, null );
            }

        } else if ( analysisType.equals( Method.ROC ) ) {
            this.addHelp( extraHelp );
            step5Panel.add( rocPanel, null );
        } else if ( analysisType.equals( Method.CORR ) ) {
            this.addHelp( help );
            step5Panel.add( corrPanel, null );
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * @param analysisType
     */
    public void removeVarPanel( Settings.Method analysisType ) {
        if ( analysisType.equals( SettingsHolder.Method.ORA ) ) {
            step5Panel.remove( oraPanel );
        } else if ( analysisType.equals( SettingsHolder.Method.GSR ) ) {
            step5Panel.remove( resampPanel );
            step5Panel.remove( prPanel );
        } else if ( analysisType.equals( SettingsHolder.Method.CORR ) ) {
            step5Panel.remove( corrPanel );
        } else if ( analysisType.equals( SettingsHolder.Method.ROC ) ) {
            step5Panel.remove( rocPanel );
        }
    }

    /**
     * Save the values to the configuration.
     */
    public void saveValues() {

        if ( !settings.getGeneSetResamplingScoreMethod().equals( GeneScoreMethod.PRECISIONRECALL ) ) {
            if ( jRadioButtonMean.isSelected() ) {
                settings.setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.MEAN );
            } else {
                settings.setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.QUANTILE );
            }
        }

        settings.setGeneScoreThreshold( oraThreshold.get() );
        settings.setDoLog( doLog );
        settings.setBigIsBetter( bigIsBetter );
        settings.setAlwaysUseEmpirical( doFullEmpirical );
        settings.setUseMultifunctionalityCorrection( doMfCorr );
        settings.setIterations( numIterations.get() );

    }

    public boolean upperTail() {
        return doLog && !bigIsBetter || !doLog && bigIsBetter;
    }

    // Component initialization
    @Override
    protected void jbInit() {
        JPanel topPanel = new JPanel();

        jRadioButtonMedian = new JRadioButton();
        jRadioButtonMean = new JRadioButton();

        step5Panel = new JPanel();
        // step5Panel.setPreferredSize( new Dimension( 550, 280 ) );

        createROCSettingsPanel();
        createORASettingsPanel();
        createPRSettingsPanel();
        createGSRSettingsPanel();
        createCorrelationSettingsPanel();

        this.addHelp( help );
        help = "<html><b>Adjust settings specific for your analysis method.</b><br>";

        extraHelp = help
                + "<p>Take special care to ensure the"
                + " log transformation and 'larger scores are better' settings are correct. 'larger scores are better' refers to your "
                + " original input file, and should be unchecked if your input is raw p-values.</p>";
        step5Panel.add( topPanel, null );
        this.addMain( step5Panel );
    }

    /**
     * ORA only: Figure out how many genes are selected at the given threshold, to give the user guidance.
     */
    private void checkOraThresholdEffects() {

        if ( settings.getClassScoreMethod() != Method.ORA ) {
            return;
        }

        SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

            private GeneScores gs;

            @Override
            protected Object doInBackground() {

                try {

                    double thresh = oraThreshold.get();
                    if ( doLog ) {
                        thresh = -Arithmetic.log10( thresh );
                    }

                    AnalysisWizard w = ( AnalysisWizard ) getOwner();
                    if ( gs == null ) {

                        Settings settingsTemp = new Settings( w.getSettings() );
                        settingsTemp.setDoLog( doLog );
                        settingsTemp.setBigIsBetter( bigIsBetter );

                        String scoreFile = settingsTemp.getScoreFile();
                        log.info( "Checking ORA threshold effect using " + scoreFile );
                        gs = new GeneScores( scoreFile, settingsTemp, null, w.getGeneAnnots() );

                        w.getStatusField().clear();
                    }

                    int n = 0;
                    Map<Gene, Double> geneToScoreMap = gs.getGeneToScoreMap();
                    Collection<Gene> keptGenes = new HashSet<Gene>();
                    for ( Gene g : geneToScoreMap.keySet() ) {
                        Double s = geneToScoreMap.get( g );
                        if ( scorePassesThreshold( s, thresh ) ) {
                            n++;
                            keptGenes.add( g );
                        }
                    }
                    if ( n == 0 ) {
                        w.getStatusField().showError( "No genes selected at that threshold" );
                    } else {
                        double auc = w.getGeneAnnots().getMultifunctionality()
                                .enrichmentForMultifunctionality( keptGenes );
                        double p = w.getGeneAnnots().getMultifunctionality()
                                .enrichmentForMultifunctionalityPvalue( keptGenes );
                        w.getStatusField().showStatus(
                                String.format( " %d genes selected. MF bias (AUROC) = %.2f, p = %.2g", n, auc, p ) );

                    }

                } catch ( NumberFormatException ex ) {
                    return null;
                } catch ( IOException ex ) {
                    return null;
                }
                return null;
            }
        };

        sw.execute();
    }

    private void createCorrelationSettingsPanel() {
        corrPanel = new JPanel();
        corrPanel.setBorder( new TitledBorder( "Correlation" ) );

        JPanel corrMetricPanel = new JPanel(); // not using this yet.

        // ButtonGroup corrButtonGroup = new ButtonGroup();
        // JLabel corrMetricLabel = new JLabel();
        // JRadioButton corrRadioButton1 = new JRadioButton();
        // JRadioButton corrRadioButton2 = new JRadioButton();
        // corrMetricPanel.setBorder( null );
        // corrMetricPanel.setBackground( SystemColor.control );
        // corrMetricPanel.setBackground( SystemColor.control );
        // corrMetricPanel.setToolTipText( "metric tool tip." );
        // corrMetricLabel.setText( "Correlation Metric" );
        // corrMetricLabel.setToolTipText( "metric tool tip." );
        //
        // corrRadioButton1.setText( "Metric 1" );
        // corrRadioButton1.setSelected( true );
        // corrRadioButton1.setBackground( SystemColor.control );
        // corrRadioButton1.setToolTipText( "metric 1 tool tip" );
        // corrRadioButton2.setText( "Metric 2" );
        //
        // corrButtonGroup.add( corrRadioButton1 );
        // corrButtonGroup.add( corrRadioButton2 );
        // corrMetricPanel.add( corrMetricLabel, null );
        // corrMetricPanel.add( corrRadioButton1, null );
        // corrMetricPanel.add( corrRadioButton2, null );

        JPanel resampSettingsPanel = createResamplingSettingsPanel();
        corrPanel.add( resampSettingsPanel, null );
        corrPanel.add( corrMetricPanel, null );
        JCheckBox mfCorrectionCheckBox = getMfCorrectionCheckBox();

        GroupLayout gl = new GroupLayout( corrPanel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        corrPanel.setLayout( gl );
        gl.setHorizontalGroup( gl.createParallelGroup().addComponent( resampSettingsPanel )
                .addComponent( corrMetricPanel ).addComponent( mfCorrectionCheckBox ) );

        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( resampSettingsPanel )
                .addComponent( corrMetricPanel ).addComponent( mfCorrectionCheckBox ) );

    }

    private void createGSRSettingsPanel() {
        resampPanel = new JPanel();
        resampPanel.setBorder( new TitledBorder( "GSR" ) );

        // panel that describes how we combine scores for genes; does not apply to precision-recall.
        JPanel jPanelAnalysisFrameMethods = new JPanel();
        JLabel jLabelAnalysisFrameMethod = new JLabel();
        jPanelAnalysisFrameMethods.setBorder( null );

        jLabelAnalysisFrameMethod.setToolTipText( "Determines how the gene scores are combined to make a class score." );
        jLabelAnalysisFrameMethod.setText( "Class Scoring Method" );
        jRadioButtonMedian.setText( "Median" );
        jRadioButtonMedian.setToolTipText( "The score for a class is the median of the score of genes in the "
                + "class." );
        jRadioButtonMedian.setBackground( SystemColor.control );
        jRadioButtonMean.setBackground( SystemColor.control );
        jRadioButtonMean.setToolTipText( "The raw score for the class is the mean of the scores for genes in "
                + "the class" );
        jRadioButtonMean.setSelected( true );
        jRadioButtonMean.setText( "Mean" );
        ButtonGroup buttonGroup2 = new ButtonGroup();
        buttonGroup2.add( jRadioButtonMean );
        buttonGroup2.add( jRadioButtonMedian );
        jPanelAnalysisFrameMethods.add( jLabelAnalysisFrameMethod, null );
        jPanelAnalysisFrameMethods.add( jRadioButtonMean, null );
        jPanelAnalysisFrameMethods.add( jRadioButtonMedian, null );

        JPanel resamplingSettingsPanel = createResamplingSettingsPanel();

        final JCheckBox jCheckBoxDoLog = getDoLogCheckBox();
        final JCheckBox jCheckBoxBigIsBetter = getBigIsBetterCheckBox();
        final JCheckBox mfCheckBox = getMfCorrectionCheckBox();
        GroupLayout gl = new GroupLayout( resampPanel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        resampPanel.setLayout( gl );
        gl.setHorizontalGroup( gl.createParallelGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( jPanelAnalysisFrameMethods )
                .addComponent( resamplingSettingsPanel ).addComponent( mfCheckBox ) );

        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( jPanelAnalysisFrameMethods )
                .addComponent( resamplingSettingsPanel ).addComponent( mfCheckBox ) );

    }

    /**
     * @return a panel with a field for "number of iterations".
     */
    private JPanel createNumIterationsPanel() {
        JPanel numIterationsPanel = new JPanel();

        // we reuse this for resampling.
        assert numIterationsPanel != null;
        numIterationsPanel.setBorder( null );
        final JTextField jTextFieldIterations = new JTextField();
        JLabel numIterationsLabel = new JLabel();
        numIterationsLabel.setLabelFor( jTextFieldIterations );
        numIterationsLabel.setText( "Maximum iterations to run" );

        jTextFieldIterations.setHorizontalAlignment( SwingConstants.RIGHT );
        jTextFieldIterations.setToolTipText( "Maximum number of iterations run per gene set size." );
        jTextFieldIterations.setPreferredSize( new Dimension( 100, 19 ) );
        jTextFieldIterations.setEditable( true );

        GroupLayout gl = new GroupLayout( numIterationsPanel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        numIterationsPanel.setLayout( gl );
        gl.setHorizontalGroup( gl.createSequentialGroup().addComponent( numIterationsLabel )
                .addComponent( jTextFieldIterations ) );

        gl.setVerticalGroup( gl.createParallelGroup().addComponent( numIterationsLabel )
                .addComponent( jTextFieldIterations ) );

        numIterations.set( settings.getIterations() );
        if ( numIterations.get() > 0 ) {
            jTextFieldIterations.setText( String.valueOf( numIterations.get() ) );
        }

        // not sure both of these are needed ... probably just the key listener.
        jTextFieldIterations.addActionListener( new AbstractAction() {
            @Override
            public void actionPerformed( ActionEvent arg0 ) {
                getNumIterationsFromField( jTextFieldIterations );
            }

        } );

        jTextFieldIterations.addKeyListener( new KeyAdapter() {
            @Override
            public void keyTyped( KeyEvent e ) {
                getNumIterationsFromField( jTextFieldIterations );
            }

            @Override
            public void keyPressed( KeyEvent e ) {
                getNumIterationsFromField( jTextFieldIterations );
            }

            @Override
            public void keyReleased( KeyEvent e ) {
                getNumIterationsFromField( jTextFieldIterations );
            }
        } );

        return numIterationsPanel;
    }

    /**
     */
    private void createORASettingsPanel() {
        JPanel jPanel15 = new JPanel();
        oraPanel = new JPanel();
        oraPanel.setBorder( new TitledBorder( "ORA" ) );
        final JTextField geneScoreThresholdTextField = new JTextField();
        // stuff to set gene score threshold.
        JLabel jLabel6 = new JLabel();
        jLabel6.setLabelFor( geneScoreThresholdTextField );
        jLabel6.setText( "Gene score threshold" );
        geneScoreThresholdTextField.setEditable( true );
        geneScoreThresholdTextField.setToolTipText( "Score Threshold used for Over-Representation analysis" );
        geneScoreThresholdTextField.setHorizontalAlignment( SwingConstants.RIGHT );
        geneScoreThresholdTextField.setText( String.valueOf( settings.getGeneScoreThreshold() ) );
        oraThreshold.set( settings.getGeneScoreThreshold() );
        jPanel15.add( jLabel6, null );
        jPanel15.add( geneScoreThresholdTextField, null );

        final JCheckBox jCheckBoxDoLog = getDoLogCheckBox();
        final JCheckBox jCheckBoxBigIsBetter = getBigIsBetterCheckBox();
        final JCheckBox mfCheckBox = getMfCorrectionCheckBox();
        GroupLayout gl = new GroupLayout( oraPanel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        oraPanel.setLayout( gl );
        gl.setHorizontalGroup( gl.createParallelGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( jPanel15 ).addComponent( mfCheckBox ) );

        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( jPanel15 ).addComponent( mfCheckBox ) );

        geneScoreThresholdTextField.addKeyListener( new KeyAdapter() {
            @Override
            public void keyReleased( KeyEvent e ) {
                String threshText = geneScoreThresholdTextField.getText();
                try {
                    oraThreshold.set( Double.valueOf( threshText ).doubleValue() );
                    checkOraThresholdEffects();
                } catch ( NumberFormatException e1 ) {
                    //
                }

            }

        } );
    }

    /**
     * @param jTextFieldIterations
     */
    private void getNumIterationsFromField( final JTextField jTextFieldIterations ) {
        try {
            numIterations.set( Integer.valueOf( StringUtils.strip( jTextFieldIterations.getText() ) ).intValue() );
        } catch ( NumberFormatException e1 ) {
            log.debug( "Could not parse integer: " + jTextFieldIterations.getText() );
        }
    }

    /**
     * Precision-recall stuff
     */
    private void createPRSettingsPanel() {
        prPanel = new JPanel();
        prPanel.setBorder( new TitledBorder( "Precision-recall" ) );

        JPanel numIterationsPanel = createNumIterationsPanel();
        GroupLayout gl = new GroupLayout( prPanel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        prPanel.setLayout( gl );

        final JCheckBox jCheckBoxDoLog = getDoLogCheckBox();
        final JCheckBox jCheckBoxBigIsBetter = getBigIsBetterCheckBox();
        final JCheckBox mfCheckBox = getMfCorrectionCheckBox();

        gl.setHorizontalGroup( gl.createParallelGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( numIterationsPanel ).addComponent( mfCheckBox ) );

        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( numIterationsPanel ).addComponent( mfCheckBox ) );

    }

    /**
     * 
     */
    private JPanel createResamplingSettingsPanel() {

        JPanel subPanel = new JPanel();
        subPanel.setLayout( new BorderLayout() );

        JPanel useResamplingPanel = new JPanel();
        final JCheckBox jCheckBoxUseEmpirical = new JCheckBox();
        jCheckBoxUseEmpirical.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent arg0 ) {
                doFullEmpirical = jCheckBoxUseEmpirical.isSelected();
            }
        } );

        JLabel useResamplingLabel = new JLabel();
        useResamplingLabel.setLabelFor( jCheckBoxUseEmpirical );

        doFullEmpirical = settings.getAlwaysUseEmpirical();
        jCheckBoxUseEmpirical.setSelected( doFullEmpirical );

        jCheckBoxUseEmpirical.setHorizontalAlignment( SwingConstants.RIGHT );
        useResamplingLabel.setText( "Always use full resampling (slower)" );
        jCheckBoxUseEmpirical.setToolTipText( "If this box is unchecked, " + "some approximations are used which can"
                + " dramatically speed up the resampling," + " at a possible risk of reduced accuracy" );
        useResamplingPanel.add( useResamplingLabel, null );
        useResamplingPanel.add( jCheckBoxUseEmpirical, null );

        subPanel.add( createNumIterationsPanel(), BorderLayout.NORTH );
        subPanel.add( useResamplingPanel, BorderLayout.SOUTH );

        // FIXME: use a better layout method.

        return subPanel;
    }

    /**
     * 
     */
    private void createROCSettingsPanel() {
        rocPanel = new JPanel();
        rocPanel.setBorder( new TitledBorder( "ROC" ) );

        GroupLayout gl = new GroupLayout( rocPanel );
        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );
        rocPanel.setLayout( gl );
        final JCheckBox jCheckBoxDoLog = getDoLogCheckBox();
        final JCheckBox jCheckBoxBigIsBetter = getBigIsBetterCheckBox();
        final JCheckBox mfCheckBox = getMfCorrectionCheckBox();
        gl.setHorizontalGroup( gl.createParallelGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( mfCheckBox ) );

        gl.setVerticalGroup( gl.createSequentialGroup().addComponent( jCheckBoxDoLog )
                .addComponent( jCheckBoxBigIsBetter ).addComponent( mfCheckBox ) );

    }

    /**
     * @return
     */
    private JCheckBox getBigIsBetterCheckBox() {
        final JCheckBox jCheckBoxBigIsBetter = new JCheckBox();
        jCheckBoxBigIsBetter.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                bigIsBetter = jCheckBoxBigIsBetter.isSelected();
                checkOraThresholdEffects();
            }
        } );

        jCheckBoxBigIsBetter.setToolTipText( "If you are loading raw p values, you should UNcheck this box." );
        jCheckBoxBigIsBetter.setSelected( false );
        jCheckBoxBigIsBetter.setText( "Larger scores in your gene score file are better." );

        this.bigIsBetter = settings.getBigIsBetter();

        jCheckBoxBigIsBetter.setSelected( this.bigIsBetter );

        return jCheckBoxBigIsBetter;
    }

    /**
     * @return
     */
    private JCheckBox getDoLogCheckBox() {
        final JCheckBox jCheckBoxDoLog = new JCheckBox();
        jCheckBoxDoLog.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                doLog = jCheckBoxDoLog.isSelected();
                checkOraThresholdEffects();
            }
        } );

        jCheckBoxDoLog.setBackground( SystemColor.control );
        jCheckBoxDoLog.setToolTipText( "If you are loading raw p values, you should check this box." );
        jCheckBoxDoLog.setSelected( true );
        jCheckBoxDoLog.setText( "Take the negative log of the gene scores" );

        this.doLog = settings.getDoLog();
        jCheckBoxDoLog.setSelected( this.doLog );

        return jCheckBoxDoLog;
    }

    private JCheckBox getMfCorrectionCheckBox() {
        final JCheckBox jCheckBoxDoMultiFuncCorr = new JCheckBox();
        jCheckBoxDoMultiFuncCorr.setSelected( true );
        jCheckBoxDoMultiFuncCorr.setText( "Test the effect of multifunctional genes" );

        jCheckBoxDoMultiFuncCorr.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent arg0 ) {
                doMfCorr = jCheckBoxDoMultiFuncCorr.isSelected();
            }
        } );

        if ( enableMultifuncCheckbox ) {
            jCheckBoxDoMultiFuncCorr.setEnabled( true );
            jCheckBoxDoMultiFuncCorr.setSelected( settings.useMultifunctionalityCorrection() );
        } else {
            jCheckBoxDoMultiFuncCorr.setEnabled( false );
            jCheckBoxDoMultiFuncCorr.setVisible( false );
        }

        return jCheckBoxDoMultiFuncCorr;
    }

    private boolean scorePassesThreshold( double geneScore, double threshold ) {
        return ( upperTail() && geneScore >= threshold ) || ( !upperTail() && geneScore <= threshold );
    }

    /**
     * Setup
     */
    private void setValues() {

        if ( settings.getGeneSetResamplingScoreMethod().equals( SettingsHolder.GeneScoreMethod.MEAN ) ) {
            jRadioButtonMean.setSelected( true );
        } else {
            jRadioButtonMedian.setSelected( true );
        }

    }
}