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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.google.common.collect.Iterables;

/** This does not extend any type of "Graph" class for easier compatibility with Hudson and Jenkins.
 * i.e. we can more easily use the same code for both. */
public class NeoLoadGraphCustom implements Serializable {

	/** Generated. */
	private static final long serialVersionUID = 8130335080125920473L;

	/** We use the same size as the default junit trend graph. */
	private static final int IMAGE_HEIGHT = 200;

	/** We use the same size as the default junit trend legend. */
	private static final int LEGEND_HEIGHT = 25;

	/** We use the same size as the default junit trend graph. */
	private static final int IMAGE_WIDTH = 500;

	/** customGraphInfo */
	private final Map<String, Map<String, Float>> datas;

	/** yAxisLabel */
	private final String yAxisLabel;

	/** The title of the graph */
	private final String title;

	public NeoLoadGraphCustom(final Map<String, Map<String, Float>> datas, final String yAxisLabel, final String title) {
		this.datas = datas;
		this.yAxisLabel = yAxisLabel;
		this.title = title;
	}

	public JFreeChart createGraph() {
		JFreeChart chart = ChartFactory.createXYLineChart(title, null, getyAxisLabel(), generateData(), 
				PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(Color.WHITE);
		final TextTitle textTitle = new TextTitle(title, new Font("Helvetica", Font.BOLD, 16));
		chart.setTitle(textTitle);
		chart.getXYPlot().setDomainGridlinePaint(Color.WHITE); // Remove the vertical gridline
		chart.getLegend().setBorder(0, 0, 0, 0); // To haven't border for the legend.
		return chart;
	}

	/**
	 * This function generate all curves of the graph.
	 * @return
	 */
	private XYSeriesCollection generateData() {
		XYSeriesCollection data = new XYSeriesCollection();
		for (final String curve : datas.keySet()) {
			data.addSeries(generateSeries(curve, datas.get(curve)));
		}
		return data;
	}
	
	/**
	 * This function create 1 curve of the graph.
	 * @param name the name of the curve (displayed in the legend).
	 * @param curve the map containing all datas for this curve.
	 * @return
	 */
	private static XYSeries generateSeries(final String name, final Map<String, Float> curve) {
		final XYSeries series = new XYSeries(name);
		// reverse the keys so that they appear in the correct order in the graphs.
		final List<String> keys = new ArrayList<String>(curve.keySet());
		for (final String buildName: Iterables.reverse(keys)) {
			series.add(Integer.parseInt(buildName.substring(1)), curve.get(buildName));
		}
		return series;
	}

	/** This is the method Hudson uses when a dynamic png is referenced in a jelly file.
	 * @param req
	 * @param rsp
	 * @throws IOException
	 */
	public void doPng(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
		rsp.setContentType("image/png");
		final ServletOutputStream os = rsp.getOutputStream();
		final BufferedImage image = createImage(IMAGE_WIDTH, IMAGE_HEIGHT + datas.size()*LEGEND_HEIGHT);
		ImageIO.write(image, "PNG", os);
		os.close();
	}

	public BufferedImage createImage(final int width, final int height) {
		return createGraph().createBufferedImage(width, height);
	}

	/** @return the yAxisLabel */
	public String getyAxisLabel() {
		return yAxisLabel;
	}
}
