/*
 * Copyright (c) 2018, Neotys
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

import hudson.model.AbstractBuild;
import hudson.model.Run;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeoloadGraphsStatistics {
	private static final Logger LOGGER = Logger.getLogger(NeoloadGraphsStatistics.class.getName());


	private List<NeoloadGraphXPathStat> neoloadGraphXPathStats = new ArrayList<>();

	private static final List<Color> availableColors = Arrays.asList(
			Color.BLUE,
			Color.GREEN,
			Color.RED,
			Color.MAGENTA,
			Color.CYAN,
			Color.PINK,
			Color.ORANGE,
			Color.gray,
			Color.YELLOW,
			Color.darkGray,
			Color.lightGray
	);

	private static Color colorFromIndex(int index) {
		return availableColors.get(index % availableColors.size());
	}


	public NeoloadGraphsStatistics(NeoLoadPluginOptions neoLoadPluginOptions) {
		if (neoLoadPluginOptions.isShowTrendAverageResponse()) {

			final NeoloadCurvesXPathStat stat = new NeoloadCurvesXPathStat(
					"Time",
					new Color(237, 184, 0),
					"/report/summary/all-summary/statistic-item[@type='httppage']/@avg"
			);
			neoloadGraphXPathStats.add(
					new NeoloadGraphXPathStat("Avg. Resp. Time (pages)", "Avg Resp Time (secs)", stat)
			);
		}
		if (neoLoadPluginOptions.isShowTrendErrorRate()) {
			final NeoloadCurvesXPathStat stat = new NeoloadCurvesXPathStat(
					"Time",
					new Color(200, 0, 0),
					"/report/summary/statistics/statistic[@name='error_percentile']/@value"
			);
			neoloadGraphXPathStats.add(
					new NeoloadGraphXPathStat("Error Rate", "Error Rate %", stat)
			);
		}

		final List<GraphOptionsInfo> graphOptionsInfos = neoLoadPluginOptions.getGraphOptionsInfo();
		if (graphOptionsInfos != null) {
			for (GraphOptionsInfo graphOptionsInfo : graphOptionsInfos) {
				neoloadGraphXPathStats.add(convertInfo(graphOptionsInfo));
			}
		}
	}

	private static NeoloadGraphXPathStat convertInfo(final GraphOptionsInfo graphOptionsInfo) {

		return new NeoloadGraphXPathStat(
				graphOptionsInfo.getName(),
				graphOptionsInfo.getStatistic(),
				listCurves(graphOptionsInfo)
		);
	}

	private static List<NeoloadCurvesXPathStat> listCurves(final GraphOptionsInfo graphOptionsInfo) {
		final List<NeoloadCurvesXPathStat> curves = new ArrayList<>();
		int curveCount = 0;
		for (GraphOptionsCurveInfo curveInfo : graphOptionsInfo.getCurve()) {
			curves.add(
					new NeoloadCurvesXPathStat(
							curveInfo.getPath(),
							colorFromIndex(curveCount++),
							NeoLoadReportDoc.getXPathForCustomGraph(curveInfo.getPath(), graphOptionsInfo.getStatistic()),
							NeoLoadReportDoc.getXPathForCustomMonitorOrLGGraph(curveInfo.getPath(), graphOptionsInfo.getStatistic())
					)
			);
		}
		return curves;
	}

	public void addReport(final File xmlFilePath, final int buildNumber) {
		if (xmlFilePath != null) {
			try {
				final Document document = XMLUtilities.readXmlFile(xmlFilePath.getAbsolutePath());
				for (NeoloadGraphXPathStat neoloadGraphXPathStat : neoloadGraphXPathStats) {
					neoloadGraphXPathStat.addBuild(buildNumber, document);
				}
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Exception during open file " + xmlFilePath.getAbsolutePath(), e);
			}
		}
	}

	public void addBuild(final AbstractBuild<?, ?> build) {
		addReport(getReportArtifactXML(build), build.getNumber());
	}

	public void addBuild(final Run<?, ?> build) {
		addReport(getReportArtifactXML(build), build.getNumber());
	}

	private File getReportArtifactXML(final AbstractBuild<?, ?> build) {
		final Run.Artifact artifact = PluginUtils.findArtifact(PluginUtils.getXMLReportPaths(build), build);
		return artifact == null ? null : artifact.getFile();
	}

	private File getReportArtifactXML(final Run<?, ?> build) {
		final Run.Artifact artifact = PluginUtils.findArtifact(PluginUtils.getXMLReportPaths(build), build);
		return artifact == null ? null : artifact.getFile();
	}

	public void writePng(File path) throws IOException {
		int i = 0;
		for (NeoloadGraphXPathStat neoloadGraphXPathStat : neoloadGraphXPathStats) {
			neoloadGraphXPathStat.writePng(new File(path, "stat" + String.format("%02d", (i++)) + ".png"));
		}
	}

	/**
	 * get List stat for tests
	 * @return
	 */
	List<NeoloadGraphXPathStat> getNeoloadGraphXPathStats() {
		return neoloadGraphXPathStats;
	}
}
