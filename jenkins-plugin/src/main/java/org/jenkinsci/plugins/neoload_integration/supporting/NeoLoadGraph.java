package org.jenkinsci.plugins.neoload_integration.supporting;

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

/** This does not extend any type of "Graph" class for easier compatibility with Hudson and Jenkins. 
 * i.e. we can more easily use the same code for both. */
public class NeoLoadGraph {
	
	/** We use the same size as the default junit trend graph. */
	private static final int IMAGE_HEIGHT = 200;

	/** We use the same size as the default junit trend graph. */
	private static final int IMAGE_WIDTH = 500;

	/** data to plot */
	private CategoryDataset dataset;

	/** y label */
	private String yAxisLabel;

	/** Line color to use. */
	private Color lineColor = null;

	public NeoLoadGraph(CategoryDataset dataset, String yAxisLabel, Color lineColor) {
		this.dataset = dataset;
		this.yAxisLabel = yAxisLabel;
		this.lineColor = lineColor;
	}

	public JFreeChart createGraph() {
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

		// turn the y labels sideways
		CategoryAxis axis = plot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        
		plot.getRenderer().setSeriesPaint(0, lineColor);
		plot.setBackgroundPaint(Color.white);
		
		return chart;
	}
	
	/** This is the method Hudson uses when a dynamic png is referenced in a jelly file.
	 * @param req
	 * @param rsp
	 * @throws IOException
	 */
	public void doPng(StaplerRequest req, StaplerResponse rsp) throws IOException {
		rsp.setContentType("image/png");
        ServletOutputStream os = rsp.getOutputStream();
        BufferedImage image = createImage(IMAGE_WIDTH, IMAGE_HEIGHT);
        ImageIO.write(image, "PNG", os);
        os.close();
	}
	
	public BufferedImage createImage(int width, int height) {
		return createGraph().createBufferedImage(width, height);
	}

}
