/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jenkinsci.plugins.neoload.integration.supporting;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/** This does not extend any type of "Graph" class for easier compatibility with Hudson and Jenkins.
 * i.e. we can more easily use the same code for both. */
public class NeoLoadGraph implements Serializable {

	/** Generated. */
	private static final long serialVersionUID = 8130335080125920473L;

	/** We use the same size as the default junit trend graph. */
	private static final int IMAGE_HEIGHT = 200;

	/** We use the same size as the default junit trend graph. */
	private static final int IMAGE_HEIGHT_CUSTOM = 250;

	/** We use the same size as the default junit trend graph. */
	private static final int IMAGE_WIDTH = 500;

	/** We use the same size as the default junit trend legend. */
	private static final int LEGEND_HEIGHT = 20;

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(NeoLoadGraph.class.getName());

	/** data to plot */
	private final CategoryDataset dataset;

	/** y label */
	private final String yAxisLabel;

	/** Line color to use. */
	private final Color lineColor;
	
	private final List<Color> availableColors;
	
	/** Title of the graph. */
	private final String title;
	
	/** The number of color you need to used. */
	private final int numberOfColor;

	public NeoLoadGraph(final CategoryDataset dataset, final String yAxisLabel, final Color lineColor) {
		this.dataset = dataset;
		this.yAxisLabel = yAxisLabel;
		this.lineColor = lineColor;
		this.numberOfColor = 0;
		this.title = null;
		availableColors = new ArrayList<Color>();
	}

	public NeoLoadGraph(final CategoryDataset dataset, final String yAxisLabel, final int numberOfColor, final String title) {
		this.dataset = dataset;
		this.yAxisLabel = yAxisLabel;
		this.lineColor = null;
		this.numberOfColor = numberOfColor;
		this.title = title;
		availableColors = new ArrayList<Color>();
		fillAllColors();
	}
	
	private void fillAllColors() {
		availableColors.add(Color.BLUE);
		availableColors.add(Color.GREEN);
		availableColors.add(Color.RED);
		availableColors.add(Color.MAGENTA);
		availableColors.add(Color.CYAN);
		availableColors.add(Color.PINK);
		availableColors.add(Color.ORANGE);
		availableColors.add(Color.gray);
		availableColors.add(Color.YELLOW);
		availableColors.add(Color.darkGray);
		availableColors.add(Color.lightGray);
	}

	public JFreeChart createGraph() {
		final JFreeChart chart = ChartFactory.createLineChart(null, // chart title
				null, // categoryAxisLabel
				yAxisLabel, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				lineColor == null, // include legend
				true, // tooltips
				false // urls
				);
		if (title != null) {
			final TextTitle textTitle = new TextTitle(title, new Font("Helvetica", Font.BOLD, 16));
			chart.setTitle(textTitle);
		}
		chart.setBackgroundPaint(Color.white);
		if (lineColor == null) {
			chart.getLegend().setBorder(0, 0, 0, 0); // To haven't border for the legend.
		}
		final CategoryPlot plot = chart.getCategoryPlot();

		// turn the y labels sideways
		final CategoryAxis axis = plot.getDomainAxis();
		axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

		if (lineColor == null) {
			for (int i=0; i<numberOfColor; i++) {
				Color colorToUse = availableColors.get(i % availableColors.size());
				for (int j=0; j < (i / availableColors.size()); j++) {
					colorToUse.brighter();
				}
				plot.getRenderer().setSeriesPaint(i, colorToUse);
			}
		}
		else {
			plot.getRenderer().setSeriesPaint(0, lineColor);
		}
		plot.setBackgroundPaint(Color.white);

		return chart;
	}

	/**
	 * This function permet to know how many line are used for the legend.
	 * @param lstLegend
	 * @param fontMetrics
	 * @return
	 */
	private int searchHowManyLineForLegend(final List<String> lstLegend, final FontMetrics fontMetrics) {
		//TODO Maybe upgrade it to have better performance.
		int numberOfLine = 0;
		Collections.sort(lstLegend, new ComparatorListByLength(false));
		final List<String> lstCounted = new ArrayList<String>();
		for (int i=0; i<lstLegend.size(); i++) {
			if (!lstCounted.contains(lstLegend.get(i))) {
				if (fontMetrics.stringWidth(lstLegend.get(i)) <= IMAGE_WIDTH) {
					String strCumul = lstLegend.get(i);
					for (int j=(lstLegend.size() - 1); j>i; j--) {
						if (!lstCounted.contains(lstLegend.get(j))) {
							// Here we try to calculate if it's possible to add the new String in the cumul to be under the width.
							if (fontMetrics.stringWidth(strCumul + lstLegend.get(j)) <= IMAGE_WIDTH) {
								strCumul += lstLegend.get(j);
								lstCounted.add(lstLegend.get(j));
							}
						}
					}
				}
				numberOfLine++;
				lstCounted.add(lstLegend.get(i));
			}
		}
		return numberOfLine;
	}

	/** This is the method Hudson uses when a dynamic png is referenced in a jelly file.
	 * @param req
	 * @param rsp
	 * @throws IOException
	 */
	public void doPng(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
		rsp.setContentType("image/png");
		final ServletOutputStream os = rsp.getOutputStream();
		final BufferedImage image = createImage(IMAGE_WIDTH, (lineColor == null)?IMAGE_HEIGHT_CUSTOM:IMAGE_HEIGHT);
		ImageIO.write(image, "PNG", os);
		os.close();
	}

	public BufferedImage createImage(final int width, final int height) {
		final JFreeChart chart = createGraph();
		if (lineColor == null) {
			final List<String> lstLegend = new ArrayList<String>();
			for (final Object obj : dataset.getRowKeys()) {
				lstLegend.add(obj.toString());
			}
			final BufferedImage buffImg = chart.createBufferedImage(width, height);
			final int heightLegend = searchHowManyLineForLegend(lstLegend, buffImg.getGraphics().getFontMetrics())*LEGEND_HEIGHT;
			return chart.createBufferedImage(width, height + heightLegend);
		}
		else {
			return chart.createBufferedImage(width, height);
		}
	}

	/** @return the yAxisLabel */
	public String getyAxisLabel() {
		return yAxisLabel;
	}

	/** @return the lineColor */
	public Color getLineColor() {
		return lineColor;
	}

}
