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
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;

import ubic.basecode.dataStructure.matrix.MatrixUtil;
import ubic.basecode.math.Distance;
import ubic.basecode.math.Rank;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.Multifunctionality;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
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

    public MultiFuncDiagWindow( GeneAnnotations geneAnnots, GeneScores geneScores ) {
        super( "Multifunctionality analysis" );

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab( "Sizes", getTermsPerGeneDistribution( geneAnnots ) );
        tabs.addTab( "Multifun", getGenesPerGroupDistribution( geneAnnots ) );

        if ( geneScores != null ) {
            tabs.addTab( "Bias", multifunctionalityBias( geneAnnots, geneScores ) );
        }

        this.add( tabs );
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
        }

        DefaultXYDataset ds = new DefaultXYDataset();
        ds.addSeries( "Raw", ser.toArray() );

        double pointsToAverage = 0.1; // like LOWESS we might use. Even 0.2?
        double skip = 0.001;
        XYSeries ma = MovingAverage.createMovingAverage( ds, 0, " Smoothed", pointsToAverage, skip );

        // ds.addSeries( "Smoothed", smoothed.toArray() );
        ds.addSeries( "Smoothed", ma.toArray() );

        JFreeChart c = ChartFactory.createScatterPlot( "Multifuntionality bias", "Multifunctionality rank",
                "Score rank", ds, PlotOrientation.VERTICAL, false, false, false );
        Shape circle = new Ellipse2D.Float( -1.0f, -1.0f, 1.0f, 1.0f );
        c.setTitle( "How biased are the gene scores" );
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

        JFreeChart histogram = ChartFactory.createHistogram( "Gene multifuntionality", "Number of groups",
                "Number of genes", series, PlotOrientation.VERTICAL, false, false, false );

        histogram.setTitle( "How many genes have how many annotations" );

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

        JFreeChart histogram = ChartFactory.createHistogram( "Gene set sizes", "Number of genes", "Number of groups",
                series, PlotOrientation.VERTICAL, false, false, false );

        histogram.setTitle( "How big are the groups" );

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
