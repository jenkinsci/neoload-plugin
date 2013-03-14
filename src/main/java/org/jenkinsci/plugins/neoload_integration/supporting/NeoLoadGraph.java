package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

public class NeoLoadGraph extends Graph {
	
	/** data to plot */
	private CategoryDataset dataset;

	/** y label */
	private String yAxisLabel;

	/** Line color to use. */
	private Color lineColor = null;

	public NeoLoadGraph(CategoryDataset dataset, String yAxisLabel, Color lineColor) {
		super(-1, 500, 200);
		this.dataset = dataset;
		this.yAxisLabel = yAxisLabel;
		this.lineColor = lineColor;
	}

	@Override
	protected JFreeChart createGraph() {
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

		// turn the x labels sideways
		final CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

		plot.getRenderer().setSeriesPaint(0, lineColor);

		return chart;
	}

}
