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
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
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
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.Multifunctionality;
import ubic.erminej.gui.MainFrame;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Visualizations of multifunctionality statistics. TODO refactor non-visualization code.
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
        super( "Multifunctionality analysis" );

        this.setIconImage( new ImageIcon( this.getClass().getResource(
                MainFrame.RESOURCE_LOCATION + "logoInverse32.gif" ) ).getImage() );

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab( "Set size dist.", getTermsPerGeneDistribution( geneAnnots ) );
        tabs.addTab( "Gene multifunc. dist.", getGenesPerGroupDistribution( geneAnnots ) );

        String annotFileName = new File( geneAnnots.getSettings().getAnnotFile() ).getName();
        String scoreFileName = "  Scores: ";
        if ( geneScores != null ) {
            tabs.addTab( "Score bias (Ranks)", multifunctionalityBias( geneAnnots, geneScores ) );
            tabs.addTab( "Score bias (Raw)", regressed( geneAnnots, geneScores ) );
            scoreFileName = scoreFileName + new File( geneAnnots.getSettings().getScoreFile() ).getName();
        }

        this.setTitle( getTitle() + "  | Annotations: " + StringUtils.abbreviate( annotFileName, 50 )
                + StringUtils.abbreviate( scoreFileName, 50 ) );

        this.add( tabs );
    }

    /**
     * @param geneAnnots
     * @param geneScores
     * @return
     */
    private JPanel regressed( GeneAnnotations geneAnnots, GeneScores geneScores ) {
        Map<Gene, Double> geneToScoreMap = geneScores.getGeneToScoreMap();

        DoubleMatrix1D scores = new DenseDoubleMatrix1D( geneToScoreMap.size() );
        DoubleMatrix1D mfs = new DenseDoubleMatrix1D( geneToScoreMap.size() );

        int i = 0;
        for ( Gene g : geneToScoreMap.keySet() ) {
            Double mf = ( double ) geneAnnots.getMultifunctionality().getNumGoTerms( g );

            Double s = geneToScoreMap.get( g );
            scores.set( i, s );
            mfs.set( i, mf );
            i++;
        }
        LeastSquaresFit fit = new LeastSquaresFit( mfs, scores );

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

        JFreeChart c = ChartFactory.createScatterPlot( "Multifuntionality corr.", "Multifunctionality", "Score", ds,
                PlotOrientation.VERTICAL, false, false, false );
        Shape circle = new Ellipse2D.Float( -1.0f, -1.0f, 1.0f, 1.0f );
        c.setTitle( "How biased are the gene scores\n(raw; regression)" );
        setChartTitleFont( c );
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

    private void setChartTitleFont( JFreeChart c ) {
        c.getTitle().setFont( c.getTitle().getFont().deriveFont( Font.PLAIN, 12 ) );
    }

    /**
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
        scoreRanks.assign( Functions.div( scoreRanks.size() ) );

        double r = Distance.spearmanRankCorrelation( new DoubleArrayList( array[0] ), scores );

        // todo: put this in a status bar.
        log.info( String.format( "Correlation is %.2f for %d values", r, array[0].length ) );

        ser.clear();

        for ( int i = 0; i < array[1].length; i++ ) {
            ser.add( array[0][i], scoreRanks.get( i ) );
            // ser.add( array[0][i], scores.get( i ) );
        }

        DefaultXYDataset ds = new DefaultXYDataset();
        ds.addSeries( "Raw", ser.toArray() );

        DoubleMatrix1D movingAverage = Smooth.movingAverage( new DenseDoubleMatrix1D( ser.toArray()[1] ), scoreRanks
                .size() / 10 );

        XYSeries smoothed = new XYSeries( ser.getKey() + " - Smoothed" );

        for ( int i = 0; i < array[1].length; i++ ) {
            array[1][i] = scoreRanks.getQuick( i ) / array[1].length; // relative ranks.
            ser.add( array[0][i], array[1][i] );
            smoothed.add( array[0][i], movingAverage.get( i ) );
        }

        ds.addSeries( "Smoothed", smoothed.toArray() );

        JFreeChart c = ChartFactory.createScatterPlot( "Multifuntionality bias", "Multifunctionality rank",
                "Score rank", ds, PlotOrientation.VERTICAL, false, false, false );
        Shape circle = new Ellipse2D.Float( -1.0f, -1.0f, 1.0f, 1.0f );
        c.setTitle( "How biased are the gene scores\n(Ranks, smoothed trend)" );
        setChartTitleFont( c );
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
        series.addSeries( "sizes", vec.elements(), 50, 2, 200 );

        JFreeChart histogram = ChartFactory.createHistogram( "Gene multifuntionality", "Number of sets gene has",
                "Number of genes", series, PlotOrientation.VERTICAL, false, false, false );

        histogram.setTitle( "How many sets are genes in" );
        setChartTitleFont( histogram );

        XYPlot plot = histogram.getXYPlot();
        plot.setRangeGridlinesVisible( false );
        plot.setDomainGridlinesVisible( false );
        plot.setBackgroundPaint( Color.white );

        XYBarRenderer renderer = ( XYBarRenderer ) plot.getRenderer();
        renderer.setBasePaint( Color.white );
        renderer.setSeriesPaint( 0, Color.DARK_GRAY );

        renderer.setDrawBarOutline( false );
        renderer.setShadowVisible( false );

        ChartPanel p = new ChartPanel( histogram );
        return p;
    }

    /**
     * @param annots
     * @return
     */
    private ChartPanel getGenesPerGroupDistribution( GeneAnnotations annots ) {
        /*
         * Show histogram of GO group sizes; allow filtering for user-defined, or by aspect?
         */
        DoubleArrayList vec = new DoubleArrayList();
        for ( GeneSet g : annots.getAllGeneSets() ) {
            vec.add( g.getGenes().size() );
        }

        HistogramDataset series = new HistogramDataset();
        series.addSeries( "sizes", vec.elements(), 50, 2, 200 );

        JFreeChart histogram = ChartFactory.createHistogram( "Gene set sizes", "Number of genes in set",
                "Number of sets", series, PlotOrientation.VERTICAL, false, false, false );

        histogram.setTitle( "How big are the sets" );
        setChartTitleFont( histogram );

        XYPlot plot = histogram.getXYPlot();
        plot.setRangeGridlinesVisible( false );
        plot.setDomainGridlinesVisible( false );
        plot.setBackgroundPaint( Color.white );

        XYBarRenderer renderer = ( XYBarRenderer ) plot.getRenderer();
        renderer.setBasePaint( Color.white );
        renderer.setSeriesPaint( 0, Color.DARK_GRAY );

        renderer.setDrawBarOutline( false );
        renderer.setShadowVisible( false );

        ChartPanel p = new ChartPanel( histogram );
        return p;
    }
}
