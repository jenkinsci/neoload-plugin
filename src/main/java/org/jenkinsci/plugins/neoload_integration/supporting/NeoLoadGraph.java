package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * 
 * @author jbrazdil
 */
public class NeoLoadGraph extends Graph {
	/** data to plot */
	CategoryDataset dataset;
	
	/** y label */
	String yAxisLabel;
	
	/** Line color to use. */
	Color lineColor = null;

	public NeoLoadGraph(CategoryDataset dataset, String yAxisLabel, Color lineColor) {
		super(-1, 350, 150);
		this.dataset = dataset;
		this.yAxisLabel = yAxisLabel;
		this.lineColor = lineColor;
	}
	
	@Override
	public void doPng(StaplerRequest req, StaplerResponse rsp)
			throws IOException {

		final JFreeChart chart = createChart();
		
		// use this size to draw the graph (pixels)
		BufferedImage bi = chart.createBufferedImage(500, 200);
		
        rsp.setContentType("image/png");
        ServletOutputStream os = rsp.getOutputStream();
        ImageIO.write(bi, "PNG", os);
        os.close();
	}

	@Override
	protected JFreeChart createGraph() {
		return createChart();
	}

	/**
	 * @return
	 */
	protected JFreeChart createChart() {
		final JFreeChart chart = ChartFactory.createLineChart(null, // chart title
				null, // categoryAxisLabel
				yAxisLabel, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				true, // tooltips
				false // urls
				);

		chart.setBackgroundPaint(Color.white);
		
		final CategoryPlot plot = chart.getCategoryPlot();
//		plot.getRenderer().setSeriesVisibleInLegend(0, false, false); // hide the chart legend at the bottom

		// turn the x labels sideways
		final CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		
		plot.getRenderer().setSeriesPaint(0, lineColor);
		
		return chart;
	}
}
