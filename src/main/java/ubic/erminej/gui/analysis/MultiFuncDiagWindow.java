/*
 * The ermineJ project
 *
 * Copyright (c) 2011 University of British Columbia
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;
import ubic.basecode.dataStructure.matrix.MatrixUtil;
import ubic.basecode.math.Distance;
import ubic.basecode.math.Rank;
import ubic.basecode.math.Smooth;
import ubic.basecode.math.linearmodels.LeastSquaresFit;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Multifunctionality;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.util.GuiUtil;

/**
 * Visualizations of multifunctionality statistics across all the gene sets. TODO refactor non-visualization code.
 *
 * @author paul
 * @version $Id$
 */
public class MultiFuncDiagWindow extends JFrame {

    private static Log log = LogFactory.getLog( MultiFuncDiagWindow.class );

    private static final long serialVersionUID = 1L;

    private int currentScoreColumn = -1;

    private String currentScoreFile = null;
    private final StatusViewer statusMessenger;

    /**
     * <p>
     * Constructor for MultiFuncDiagWindow.
     * </p>
     *
     * @param mainFrame a {@link ubic.erminej.gui.MainFrame} object.
     * @throws java.lang.Exception if any.
     */
    public MultiFuncDiagWindow( final MainFrame mainFrame ) throws Exception {
        super( "Multifunc. diagnostics" );

        if ( mainFrame.getStatusMessenger() != null ) {
            this.statusMessenger = mainFrame.getStatusMessenger();
        } else {
            this.statusMessenger = new StatusStderr();
        }

        this.setIconImage( new ImageIcon( this.getClass().getResource(
                MainFrame.RESOURCE_LOCATION + "logoInverse32.gif" ) ).getImage() );

        final JTabbedPane tabs = new JTabbedPane( SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );

        /*
         * Should allow user to pick.
         */
        GeneAnnotations geneAnnots = mainFrame.getOriginalGeneData();
        GeneScores geneScores = null;
        try {
            if ( mainFrame.getCurrentResultSetIndex() >= 0 && !mainFrame.getResultSets().isEmpty() ) {
                GeneSetPvalRun currentResultSet = mainFrame.getCurrentResultSet();
                geneAnnots = currentResultSet.getGeneData();

                geneScores = new GeneScores( currentResultSet.getSettings().getScoreFile(), mainFrame.getSettings(),
                        statusMessenger, geneAnnots );
                currentScoreFile = currentResultSet.getSettings().getScoreFile();
                currentScoreColumn = currentResultSet.getSettings().getScoreCol();
            } else if ( StringUtils.isNotBlank( mainFrame.getSettings().getScoreFile() ) ) {
                // no results yet, but we do have a score file.
                geneScores = new GeneScores( mainFrame.getSettings().getScoreFile(), mainFrame.getSettings(),
                        statusMessenger, geneAnnots );
                geneAnnots = geneScores.getPrunedGeneAnnotations();
                currentScoreFile = mainFrame.getSettings().getScoreFile();
                currentScoreColumn = mainFrame.getSettings().getScoreCol();
            } else {
                // we don't need to show the tabs
            }
            // if we just have 'matrix data', this isn't relevant (yet)
        } catch ( IOException e ) {
            GuiUtil.error( "Data for multifunctionality could not be read", e );
            throw ( e );
        }

        tabs.addTab( "Set sizes.", getGenesPerGroupDistribution( geneAnnots ) );
        tabs.addTab( "Sets per gene", getTermsPerGeneDistribution( geneAnnots ) );
        tabs.addTab( "Set multifunc.", getGroupMultifunctionalityDistribution( geneAnnots ) );

        if ( geneScores != null ) {
            tabs.addTab( "Score bias I", multifunctionalityBias( geneAnnots, geneScores ) );
            tabs.addTab( "Score bias II", regressed( geneAnnots, geneScores ) );
        }

        setTitle( geneAnnots, geneScores );

        this.getContentPane().add( tabs, BorderLayout.CENTER );

        addToolBar( mainFrame, tabs );

        this.statusMessenger.clear();
    }

    /**
     * @param mainFrame
     * @param tabs
     */
    private void addToolBar( final MainFrame mainFrame, final JTabbedPane tabs ) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable( false );
        GroupLayout gl = new GroupLayout( toolBar );
        toolBar.setLayout( gl );
        JButton chooseGeneScoreButton = new JButton( "Choose Scores" );
        if ( currentScoreFile == null || mainFrame.getResultSets().size() < 2 ) {
            /*
             * currentScoreFile is null if we're not even showing the tabs at the moment. Wait for user to do this on
             * the main frame.
             */
            chooseGeneScoreButton.setEnabled( false );
        }
        final MultiFuncDiagWindow w = this;
        chooseGeneScoreButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                switchScoresUsed( mainFrame, w, tabs );
            }

        } );

        gl.setHorizontalGroup( gl.createSequentialGroup().addComponent( chooseGeneScoreButton ) );
        gl.setVerticalGroup( gl.createParallelGroup().addComponent( chooseGeneScoreButton ) );
        this.getContentPane().add( toolBar, BorderLayout.NORTH );
    }

    /**
     * @param annots
     * @return
     */
    private ChartPanel getGenesPerGroupDistribution( GeneAnnotations annots ) {
        /*
         * Show histogram of GO group sizes
         */
        DoubleArrayList vec = new DoubleArrayList();
        for ( GeneSetTerm g : annots.getGeneSetTerms() ) {
            int numGenes = annots.numGenesInGeneSet( g );
            assert numGenes > 0;
            vec.add( numGenes );
        }

        HistogramDataset series = new HistogramDataset();
        int numBins = 39;
        series.addSeries( "sizes", MatrixUtil.fromList( vec ).toArray(), numBins, 2, 200 );

        JFreeChart histogram = ChartFactory.createHistogram( "Gene set sizes", "Number of genes in set",
                "Number of sets", series, PlotOrientation.VERTICAL, false, false, false );

        return Plotting.plotHistogram( "How big are the gene sets", histogram );
    }

    /**
     * @param annots
     * @return
     */
    private ChartPanel getGroupMultifunctionalityDistribution( GeneAnnotations annots ) {
        /*
         * Show histogram of GO group multifunctionality
         */
        DoubleArrayList vec = new DoubleArrayList();
        for ( GeneSetTerm g : annots.getGeneSetTerms() ) {
            // vec.add( g.getGenes().size() );

            assert annots.getGeneSet( g ).size() > 0;
            // double mf = -Math.log10( annots.getMultifunctionality().getGOTermMultifunctionalityPvalue( g ) );
            double mf = annots.getMultifunctionality().getGOTermMultifunctionality( g );
            if ( mf <= 0 ) continue; // missing
            vec.add( mf );
        }

        HistogramDataset series = new HistogramDataset();
        int numBins = 39;
        series.addSeries( "", MatrixUtil.fromList( vec ).toArray(), numBins, 0.0, 1.0 );

        /*
         * The value plotted here is based on the AU-ROC, but see Multifunctionality to see how it is computed for
         * display (we have changed it a few times).
         */
        JFreeChart histogram = ChartFactory.createHistogram( "Gene set multifunctionalities",
                "Bias towards multifunctional genes", "Number of sets", series, PlotOrientation.VERTICAL, false, false,
                false );

        String title = "How multifunctional are the sets?";
        return Plotting.plotHistogram( title, histogram );
    }

    /**
     * @param geneAnnots
     * @return
     */
    private JPanel getTermsPerGeneDistribution( GeneAnnotations geneAnnots ) {
        DoubleArrayList vec = new DoubleArrayList();

        Multifunctionality mf = geneAnnots.getMultifunctionality();

        for ( Gene g : geneAnnots.getGenes() ) {
            double mfs = mf.getNumGoTerms( g );
            vec.add( mfs );
        }

        HistogramDataset series = new HistogramDataset();
        int numBins = 39;
        series.addSeries( "sizes", MatrixUtil.fromList( vec ).toArray(), numBins, 2, 200 );

        // Map<Gene, Double> padogWeights = mf.padogWeights();
        // double[] dpw = ArrayUtils.toPrimitive( padogWeights.values().toArray( new Double[] {} ) );
        // series.addSeries( "PADOG", dpw, numBins, 1, 2 );

        JFreeChart histogram = ChartFactory.createHistogram( "Gene multifunctionality", "Number of sets gene is in",
                "Number of genes", series, PlotOrientation.VERTICAL, false, false, false );

        return Plotting.plotHistogram( "Gene multifunctionality: How many sets each gene is in", histogram );
    }

    /**
     * Plot of ranks, smoothed.
     *
     * @param geneAnnots
     * @param geneScores
     * @return
     */
    private JPanel multifunctionalityBias( GeneAnnotations geneAnnots, GeneScores gs ) {

        Map<Gene, Double> geneToScoreMap = gs.getGeneToScoreMap();
        XYSeries ser = new XYSeries( "Gene scores" );

        for ( Gene g : geneToScoreMap.keySet() ) {
            Double mf = geneAnnots.getMultifunctionality().getMultifunctionalityRank( g );
            Double s = geneToScoreMap.get( g );
            ser.add( mf, s );
        }

        double[][] array = ser.toArray();

        DoubleArrayList scores = new DoubleArrayList( array[1] );
        DoubleMatrix1D scoreRanks = MatrixUtil.fromList( Rank.rankTransform( scores ) );

        assert scoreRanks != null;

        scoreRanks.assign( Functions.div( scoreRanks.size() ) );

        double r = Distance.spearmanRankCorrelation( new DoubleArrayList( array[0] ), scores );

        log.info( String.format( "Correlation is %.2f for %d values", r, array[0].length ) );

        ser.clear();

        // replace with ranks.
        for ( int i = 0; i < array[1].length; i++ ) {
            ser.add( array[0][i] /* mf */, scoreRanks.get( i ) );
            // debug
            // ser.add( array[0][i], scores.get( i ) );
        }

        DefaultXYDataset ds = new DefaultXYDataset();
        ds.addSeries( "Raw", ser.toArray() );

        int windowSize = scoreRanks.size() / 10;
        DoubleMatrix1D movingAverage = Smooth.movingAverage( new DenseDoubleMatrix1D( ser.toArray()[1] ), windowSize );

        XYSeries smoothed = new XYSeries( ser.getKey() + String.format( " - Smoothed (r=%.2f)", r ) );

        for ( int i = 0; i < array[1].length; i++ ) {
            array[1][i] = scoreRanks.getQuick( i ) / array[1].length; // relative ranks.
            ser.add( array[0][i], array[1][i] );

            if ( i < windowSize / 2 ) {
                smoothed.add( array[0][i], Double.NaN ); // skip messy start
            } else {
                smoothed.add( array[0][i], movingAverage.get( i ) );
            }
        }

        ds.addSeries( "Smoothed", smoothed.toArray() );

        JFreeChart c = ChartFactory.createScatterPlot( "Multifuntionality bias", "Gene multifunctionality rank",
                "Gene score rank", ds, PlotOrientation.VERTICAL, false, false, false );
        Shape circle = new Ellipse2D.Float( -1.0f, -1.0f, 1.0f, 1.0f );
        c.setTitle( String.format( "How biased are the gene scores\n(Ranks, smoothed trend; r=%.2f)", r ) );
        Plotting.setChartTitleFont( c );
        XYPlot plot = c.getXYPlot();
        plot.setRangeGridlinesVisible( false );
        plot.getRenderer().setSeriesPaint( 0, Color.GRAY );
        plot.getRenderer().setSeriesPaint( 1, Color.black );
        plot.getRenderer().setSeriesShape( 0, circle );
        plot.getRenderer().setSeriesShape( 1, circle );
        plot.setDomainGridlinesVisible( false );
        plot.setBackgroundPaint( Color.white );
        ChartPanel p = new ChartPanel( c );
        return p;

    }

    /**
     * @param geneAnnots
     * @param geneScores must be already log-transformed (if requested)
     * @return
     */
    private JPanel regressed( GeneAnnotations geneAnnots, GeneScores geneScores ) {
        Map<Gene, Double> geneToScoreMap = geneScores.getGeneToScoreMap();

        // copies code from Multifunctionality, using weighted regression.
        // SettingsHolder settings = geneAnnots.getSettings();
        // boolean doLog = settings.getDoLog(); // note scores would already have been log-transformed.
        // boolean invert = ( doLog && !settings.getBigIsBetter() ) || ( !doLog && settings.getBigIsBetter() );

        DoubleMatrix1D scores = new DenseDoubleMatrix1D( geneToScoreMap.size() );
        DoubleMatrix1D mfs = new DenseDoubleMatrix1D( geneToScoreMap.size() );
        int i = 0;
        for ( Gene g : geneToScoreMap.keySet() ) {
            // Double mf = ( double ) geneAnnots.getMultifunctionality().getNumGoTerms( g );
            // Double mf = geneAnnots.getMultifunctionality().getMultifunctionalityScore( g );
            Double mf = geneAnnots.getMultifunctionality().getMultifunctionalityRank( g );

            Double s = geneToScoreMap.get( g );
            scores.set( i, s );
            mfs.set( i, mf );
            i++;
        }

        // DoubleMatrix1D weights = MatrixUtil.fromList( Rank.rankTransform( MatrixUtil.toList( scores ), invert ) );
        // weights.assign( Functions.inv ).assign( Functions.sqrt ); // FIXME make sure we do this the same way as in
        // // Multifunctionality.java.

        LeastSquaresFit fit = new LeastSquaresFit( mfs, scores ); // , weights

        double slope = fit.getCoefficients().get( 1, 0 );

        DoubleMatrix1D fittedValues = fit.getFitted().viewRow( 0 );

        double[][] rawSeries = new double[2][fittedValues.size()];
        double[][] fittedSeries = new double[2][fittedValues.size()];
        for ( i = 0; i < fittedValues.size(); i++ ) {

            // X values are the same.
            rawSeries[0][i] = mfs.get( i );
            fittedSeries[0][i] = mfs.get( i );

            // plotting residuals not so useful
            rawSeries[1][i] = scores.get( i );
            fittedSeries[1][i] = fittedValues.get( i );

        }

        DefaultXYDataset ds = new DefaultXYDataset();
        ds.addSeries( "Raw", rawSeries );
        ds.addSeries( "Fit", fittedSeries );

        JFreeChart c = ChartFactory.createScatterPlot( "Multifuntionality corr.", "Gene multifunctionality"/* rank */,
                "Gene score", ds, PlotOrientation.VERTICAL, false, false, false );
        Shape circle = new Ellipse2D.Float( -1.0f, -1.0f, 1.0f, 1.0f );
        c.setTitle( String.format( "How biased are the gene scores\n(slope=%.2g)", slope ) );
        Plotting.setChartTitleFont( c );
        XYPlot plot = c.getXYPlot();
        plot.setRangeGridlinesVisible( false );
        plot.getRenderer().setSeriesPaint( 0, Color.GRAY );
        plot.getRenderer().setSeriesPaint( 1, Color.black );
        plot.getRenderer().setSeriesShape( 0, circle );
        plot.getRenderer().setSeriesShape( 1, circle );
        plot.setDomainGridlinesVisible( false );
        plot.setBackgroundPaint( Color.white );
        ChartPanel p = new ChartPanel( c );
        return p;

    }

    /**
     * @param geneAnnots
     * @param geneScores
     */
    private void setTitle( GeneAnnotations geneAnnots, GeneScores geneScores ) {
        String annotFileName = new File( geneAnnots.getSettings().getAnnotFile() ).getName();

        String title = "Multifunc. diagnostics | Annotations: " + StringUtils.abbreviate( annotFileName, 50 );
        if ( geneScores != null ) {
            String scoreFileInfo = " Scores: "
                    + new File( geneAnnots.getSettings().getScoreFile() ).getName()
                    + " col: "
                    + geneAnnots.getSettings().getScoreCol()
                    + ( StringUtils.isBlank( geneScores.getScoreColumnName() ) ? "" : ", "
                            + geneScores.getScoreColumnName() );
            title = title + scoreFileInfo;
        }

        this.setTitle( StringUtils.abbreviate( title, 400 ) );
    }

    /**
     * @param mainFrame
     * @param w
     * @param tabs
     */
    private void switchScoresUsed( final MainFrame mainFrame, final MultiFuncDiagWindow w, final JTabbedPane tabs ) {
        final JDialog d = new JDialog( w, true );
        d.setTitle( "Choose which gene scores to display" );
        d.setMinimumSize( new Dimension( 200, 100 ) );
        GuiUtil.centerContainer( d );
        final JComboBox<String> runComboBox = new JComboBox<>();
        runComboBox.setMinimumSize( new Dimension( 140, 19 ) );

        final Map<String, GeneSetPvalRun> scoreSettings = new HashMap<>();
        int i = 1;
        for ( GeneSetPvalRun r : mainFrame.getResultSets() ) {
            // FIXME really we need to do this for different scores, not different runs (which could all use the
            // same scores)
            String label = r.getName() + " [" + i + "]";
            runComboBox.addItem( label );
            scoreSettings.put( label, r );
            i++;
        }

        if ( mainFrame.getCurrentResultSetIndex() >= 0 ) {
            runComboBox.setSelectedIndex( mainFrame.getCurrentResultSetIndex() );
        }

        d.add( runComboBox, BorderLayout.CENTER );

        JButton cancelButton = new JButton();
        JButton okButton = new JButton();
        cancelButton.setText( "Cancel" );
        cancelButton.setMnemonic( 'c' );
        okButton.setText( "OK" );
        JPanel buttonPanel = new JPanel();

        // OK on the left is 'standard'
        buttonPanel.add( okButton );
        buttonPanel.add( cancelButton );

        cancelButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent ae ) {
                d.dispose();
            }
        } );

        okButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent ae ) {
                String selection = ( String ) runComboBox.getSelectedItem();
                GeneSetPvalRun s = scoreSettings.get( selection );
                assert s != null;

                d.dispose();

                /*
                 * Update the tabs that show the score bias; make one active.
                 */
                GeneAnnotations ga = s.getGeneData();

                try {

                    if ( s.getSettings().getScoreFile().equals( currentScoreFile )
                            && s.getSettings().getScoreCol() == currentScoreColumn ) {
                        /*
                         * Don't need to change
                         */
                        statusMessenger.showStatus( "No recomputation needed" );
                        return;
                    }

                    currentScoreFile = s.getSettings().getScoreFile();
                    currentScoreColumn = s.getSettings().getScoreCol();
                    GeneScores gs = new GeneScores( s.getSettings().getScoreFile(), s.getSettings(), statusMessenger,
                            ga );

                    // remove the old tabs, if we have them.
                    if ( tabs.getTabCount() > 3 ) {
                        tabs.removeTabAt( 4 );
                        tabs.removeTabAt( 3 );
                    }

                    tabs.addTab( "Score bias I", multifunctionalityBias( ga, gs ) );
                    tabs.addTab( "Score bias II", regressed( ga, gs ) );
                    tabs.setSelectedIndex( 3 );
                    w.setTitle( ga, gs );
                    mainFrame.setCurrentResultSet( s ); // to be consistent.

                    statusMessenger.showStatus( "Switched to active: " + s.getName() );

                } catch ( IOException ioe ) {
                    statusMessenger.showStatus( "Failed to switch scores" );
                    // TODO Auto-generated catch block
                    ioe.printStackTrace();
                }

            }
        } );

        d.add( buttonPanel, BorderLayout.SOUTH );

        d.setVisible( true );
    }

}
