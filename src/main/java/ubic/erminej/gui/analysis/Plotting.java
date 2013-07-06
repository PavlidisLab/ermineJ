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
package ubic.erminej.gui.analysis;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;

/**
 * Utilities for generating plots in a consistent way.
 * 
 * @author paul
 * @version $Id$
 */
public class Plotting {

    /**
     * @param title
     * @param histogram
     * @return
     */
    public static ChartPanel plotHistogram( String title, JFreeChart histogram ) {
        histogram.setTitle( title );
        setChartTitleFont( histogram );

        XYPlot plot = histogram.getXYPlot();
        plot.setRangeGridlinesVisible( false );
        plot.setDomainGridlinesVisible( false );
        plot.setBackgroundPaint( Color.white );

        XYBarRenderer renderer = ( XYBarRenderer ) plot.getRenderer();
        renderer.setBasePaint( Color.white );
        renderer.setSeriesPaint( 0, Color.decode( "#888888" ) );

        if ( histogram.getXYPlot().getSeriesCount() > 1 ) {
            renderer.setSeriesPaint( 1, Color.decode( "#BBBBBB" ) );
        }

        renderer.setBarPainter( new StandardXYBarPainter() );
        renderer.setDrawBarOutline( false );
        renderer.setShadowVisible( false );

        ChartPanel p = new ChartPanel( histogram );
        return p;
    }

    /**
     * @param c
     */
    public static void setChartTitleFont( JFreeChart c ) {
        c.getTitle().setFont( c.getTitle().getFont().deriveFont( Font.PLAIN, 12 ) );
    }
}
