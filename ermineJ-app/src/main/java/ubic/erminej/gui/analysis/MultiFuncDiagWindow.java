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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
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

import ubic.basecode.dataStructure.matrix.MatrixUtil;
import ubic.basecode.math.Distance;
import ubic.basecode.math.LeastSquaresFit;
import ubic.basecode.math.Rank;
import ubic.basecode.math.Smooth; 
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Multifunctionality;
import ubic.erminej.gui.MainFrame;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Visualizations of multifunctionality statistics across all the gene sets. TODO refactor non-visualization code.
 * 
 * @author paul
 * @version $Id$
 */
public class MultiFuncDiagWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog( MultiFuncDiagWindow.class );

    /**
     * @param geneAnnots
     * @param geneScores
     */
    public MultiFuncDiagWindow( GeneAnnotations geneAnnots, GeneScores geneScores ) {
        super( "Multifunc. diagnostics" );

        this.setIconImage( new ImageIcon( this.getClass().getResource(
                MainFrame.RESOURCE_LOCATION + "logoInverse32.gif" ) ).getImage() );

        JTabbedPane tabs = new JTabbedPane( SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );

        tabs.addTab( "Set sizes.", getGenesPerGroupDistribution( geneAnnots ) );
        tabs.addTab( "Sets per gene", getTermsPerGeneDistribution( geneAnnots ) );
        tabs.addTab( "Set multifunc.", getGroupMultifunctionalityDistribution( geneAnnots ) );

        String annotFileName = new File( geneAnnots.getSettings().getAnnotFile() ).getName();
        if ( geneScores != null ) {
            tabs.addTab( "Score bias I", multifunctionalityBias( geneAnnots, geneScores ) );
            tabs.addTab( "Score bias II", regressed( geneAnnots, geneScores ) );
        }

        String title = getTitle() + "  | Annotations: " + StringUtils.abbreviate( annotFileName, 50 );

        if ( geneScores != null ) {
            String scoreFileInfo = " Scores: " + new File( geneAnnots.getSettings().getScoreFile() ).getName()
                    + " col: " + geneAnnots.getSettings().getScoreCol() + ", " + geneScores.getScoreColumnName();
            title = title + scoreFileInfo;
        }

        this.setTitle( StringUtils.abbreviate( title, 400 ) );

        this.add( tabs );
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

        JFreeChart histogram = ChartFactory.createHistogram( "Gene multifunctionality", "Number of sets gene is in",
                "Number of genes", series, PlotOrientation.VERTICAL, false, false, false );

        return Plotting.plotHistogram( "Gene multifunctionality: How many sets each gene is in", histogram );
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

}
