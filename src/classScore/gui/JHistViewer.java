package classScore.gui;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;

import classScore.data.Histogram;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class JHistViewer extends JFrame {

   Histogram hist;

   private ChartPanel jChartPanel = null;
   private int inSize;
   private double rawScore;

   /**
    * This is the default constructor
    */
   public JHistViewer( Histogram hist, int inSize, double rawScore ) {
      super();

      this.hist = hist;
      this.inSize = inSize;
      this.rawScore = rawScore;
      initialize();
   }

   /**
    * This method initializes this
    * 
    * @return void
    */
   private void initialize() {

      this
            .setDefaultCloseOperation( javax.swing.WindowConstants.DISPOSE_ON_CLOSE );
      this.setName( "Histogram viewer" );
      this.setSize( 300, 200 );
      this.makePlot();
      this.show();
   }

   /**
    * 
    */
   private void makePlot() {

      final XYSeries distribution = new XYSeries( "Distribution" );

      double[] bins = hist.getBins();
      double[] data = hist.getHistogram( inSize );

      for ( int i = 0; i < bins.length; i += 2 ) {
         double d = data[i];
         double b = bins[i];
         distribution.add( b, d );
         
         if ( d > rawScore + 1.0 ) {
            break;
         } 
      }
      final XYSeriesCollection xydata = new XYSeriesCollection( distribution );
      
      final XYSeries actualScore = new XYSeries( "This gene set" );
      actualScore.add(rawScore, 0.1);
      actualScore.add(rawScore, 0.3);
      xydata.addSeries(actualScore);
      
      final JFreeChart chart = ChartFactory.createXYLineChart(
            "Random score distribution for gene set size " + inSize,
            "Raw Score", "Cumulative Probability", xydata,
            PlotOrientation.VERTICAL, true, true, false );
      
      XYPlot plot = (XYPlot) chart.getPlot();
      plot.getDomainAxis().setUpperBound(rawScore + 1);
      
      jChartPanel = new ChartPanel( chart );
      jChartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
      setContentPane( jChartPanel );

   }

}