/*
 * The ermineJ project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.gui.geneset.details;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.DefaultXYDataset;

import cern.colt.list.DoubleArrayList;
import ubic.basecode.math.DescriptiveWithMissing;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetDetails;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.analysis.Plotting;

/**
 * Show ROC and Precision-recall curves -- things that show the gene set in the context of the full ranking.
 *
 * @author paul
 * @version $Id$
 */
public class GeneSetRankingContextWindow extends JFrame {

    private GeneAnnotations geneData;

    private GeneScores geneScores;

    private GeneSetTerm geneSetTerm;

    /**
     * <p>
     * Constructor for GeneSetRankingContextWindow.
     * </p>
     *
     * @param gsd a {@link ubic.erminej.data.GeneSetDetails} object.
     */
    public GeneSetRankingContextWindow( GeneSetDetails gsd ) {
        super( "Context" );

        this.setIconImage( new ImageIcon( this.getClass().getResource(
                MainFrame.RESOURCE_LOCATION + "logoInverse32.gif" ) ).getImage() );

        this.geneScores = gsd.getSourceGeneScores();

        if ( this.geneScores == null ) {
            throw new IllegalStateException( "You cannot create a gene set ranking without defining gene scores" );
        }

        this.geneSetTerm = gsd.getClassID();
        this.geneData = geneScores.getPrunedGeneAnnotations();

        JTabbedPane tabs = new JTabbedPane();
        tabs.add( "ROC", this.getRocPlot() );
        tabs.add( "PR", this.precisionRecallPlot() );
        tabs.add( "Hist", this.histograms() ); // could add one for multifunctionality.

        String annotFileName = new File( geneData.getSettings().getAnnotFile() ).getName();
        String scoreFileName = new File( geneData.getSettings().getScoreFile() ).getName();

        this.setTitle( "Context for " + geneSetTerm.getId() + "  | Annotations: "
                + StringUtils.abbreviate( annotFileName, 50 ) + " Scores: "
                + StringUtils.abbreviate( scoreFileName, 50 ) );

        this.add( tabs );
    }

    /**
     * @return
     */
    private double[][] getPrecisionRecallCurve() {
        List<Gene> rankedGenes = geneScores.getRankedGenes();

        Set<Gene> positives = geneScores.getPrunedGeneAnnotations().getGeneSetGenes( geneSetTerm );

        assert geneScores.getGeneToScoreMap().keySet().containsAll( positives );

        double[] precisions = new double[rankedGenes.size()];
        double[] recalls = new double[rankedGenes.size()];

        int currentPosCount = 0;
        int currentNegCount = 0;
        int i = 0;
        for ( Gene g : rankedGenes ) {
            if ( positives.contains( g ) ) {
                currentPosCount++;
            } else {
                currentNegCount++;
            }

            double recall = ( double ) currentPosCount / positives.size();
            double precision = ( double ) currentPosCount / ( currentNegCount + currentPosCount );

            precisions[i] = precision;
            recalls[i] = recall;
            i++;
        }

        // we should see all the positives!!
        // assert currentPosCount == positives.size();

        double[][] series = new double[][] { recalls, precisions };
        return series;
    }

    private double[][] getROCCurve() {
        List<Gene> rankedGenes = geneScores.getRankedGenes();

        Set<Gene> positives = geneData.getGeneSetGenes( geneSetTerm );

        double[] fps = new double[rankedGenes.size()];
        double[] tps = new double[rankedGenes.size()];

        int currentRecall = 0;
        int currentNegCount = 0;
        int i = 0;
        for ( Gene raw : rankedGenes ) {
            if ( positives.contains( raw ) ) {
                currentRecall++;
            } else {
                currentNegCount++;
            }

            tps[i] = currentRecall;
            fps[i] = currentNegCount;
            i++;
        }

        double[][] series = new double[][] { fps, tps };
        return series;
    }

    /**
     * Generate an ROC curve for the gene set
     *
     * @return
     */
    private ChartPanel getRocPlot() {

        double[][] series = getROCCurve();

        DefaultXYDataset ds = new DefaultXYDataset();
        ds.addSeries( "PR", series );

        JFreeChart c = ChartFactory.createScatterPlot( "ROC", "False positives", "True positives", ds,
                PlotOrientation.VERTICAL, false, false, false );
        c.setTitle( "ROC for " + geneSetTerm.getId() );
        Plotting.setChartTitleFont( c );
        XYPlot plot = c.getXYPlot();
        plot.setRangeGridlinesVisible( false );

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible( 0, true );
        renderer.setSeriesShapesVisible( 0, false );
        plot.setRenderer( renderer );

        plot.getRenderer().setSeriesPaint( 0, Color.GRAY );
        plot.setDomainGridlinesVisible( false );
        plot.setBackgroundPaint( Color.white );
        ChartPanel p = new ChartPanel( c );
        return p;
    }

    /**
     * @return
     */
    private Component histograms() {

        Double[] gs = geneScores.getGeneScores();

        Double[] geneSetScores = geneScores.getGeneSetScores( this.geneSetTerm );

        assert this.geneData != null;

        if ( geneScores.isNegativeLog10Transformed() ) {
            /*
             * de-log..
             */
            for ( int i = 0; i < gs.length; i++ ) {
                gs[i] = Math.pow( 10, -gs[i] );
            }
            for ( int i = 0; i < geneSetScores.length; i++ ) {
                geneSetScores[i] = Math.pow( 10, -geneSetScores[i] );
            }
        }

        double min = DescriptiveWithMissing.min( new DoubleArrayList( ArrayUtils.toPrimitive( gs ) ) );
        double max = DescriptiveWithMissing.max( new DoubleArrayList( ArrayUtils.toPrimitive( gs ) ) );

        HistogramDataset series = new HistogramDataset();
        series.setType( HistogramType.RELATIVE_FREQUENCY );
        int numBins = 39;

        assert min < max;

        series.addSeries( "Gene set", ArrayUtils.toPrimitive( geneSetScores ), numBins, min, max );
        series.addSeries( "Background", ArrayUtils.toPrimitive( gs ), numBins, min, max );

        JFreeChart histogram = ChartFactory.createHistogram( "Gene Score distributions", "Score", "Density", series,
                PlotOrientation.VERTICAL, false, false, false );

        return Plotting.plotHistogram( "Score distributions", histogram );
    }

    /**
     * Generate a precision-recall curve for the gene set
     *
     * @return
     */
    private ChartPanel precisionRecallPlot() {

        double[][] series = getPrecisionRecallCurve();

        DefaultXYDataset ds = new DefaultXYDataset();
        ds.addSeries( "PR", series );

        JFreeChart c = ChartFactory.createScatterPlot( "Precision-Recall", "Recall", "Precision", ds,
                PlotOrientation.VERTICAL, false, false, false );

        c.setTitle( "Precision-recall for " + geneSetTerm.getId() );
        Plotting.setChartTitleFont( c );
        XYPlot plot = c.getXYPlot();
        plot.setRangeGridlinesVisible( false );

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible( 0, true );
        renderer.setSeriesShapesVisible( 0, false );
        plot.setRenderer( renderer );

        plot.getRenderer().setSeriesPaint( 0, Color.GRAY );
        plot.setDomainGridlinesVisible( false );
        plot.setBackgroundPaint( Color.white );
        ChartPanel p = new ChartPanel( c );
        return p;
    }

}
