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
package org.jenkinsci.plugins.neoload.integration;

import static com.neotys.nl.controller.report.transform.NeoLoadReportDoc.AVG;
import static com.neotys.nl.controller.report.transform.NeoLoadReportDoc.ERROR_RATE;
import static com.neotys.nl.controller.report.transform.NeoLoadReportDoc.PERCENTILE2;
import static com.neotys.nl.controller.report.transform.NeoLoadReportDoc.VAL;
import static com.neotys.nl.controller.report.transform.NeoLoadReportDoc.getXPathForCustomGraph;
import static com.neotys.nl.controller.report.transform.NeoLoadReportDoc.getXPathForCustomMonitorOrLGGraph;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.GraphOptionsCurveInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.GraphOptionsInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.NeoLoadGraph;
import org.jenkinsci.plugins.neoload.integration.supporting.NeoLoadPluginOptions;
import org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.XMLUtilities;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.neotys.nl.controller.report.transform.NeoLoadReportDoc;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run.Artifact;

/** Along with the jelly file and the Factory class, this class adds the two trend graphs to a job page. */
public class ProjectSpecificAction implements ProminentProjectAction, Serializable {

	/** Generated. */
	private static final long serialVersionUID = 8435405474316375996L;

	/** A link to the Jenkins job. */
	private final AbstractProject<?, ?> project;

	/** Key is the build. Value is the NeoLoad xml report file. */
	private Map<AbstractBuild<?, ?>, NeoLoadReportDoc> buildsAndDocs = new LinkedHashMap<AbstractBuild<?, ?>, NeoLoadReportDoc>();

	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendAverageResponse;
	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendErrorRate;
	
 	private final List<GraphOptionsInfo> graphOptionsInfo;

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(ProjectSpecificAction.class.getName());
	
	public ProjectSpecificAction(final AbstractProject<?, ?> project) {
		this.project = project;
		final NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(project);
		if (npo == null) {
			LOGGER.finest("Plugin options were not found for " + project.getDisplayName());
			graphOptionsInfo = new ArrayList<GraphOptionsInfo>();
			showTrendAverageResponse = false;
			showTrendErrorRate = false;
		}
		else {
			graphOptionsInfo = npo.getGraphOptionsInfo();
			showTrendAverageResponse = npo.isShowTrendAverageResponse();
			showTrendErrorRate = npo.isShowTrendErrorRate();
			refreshGraphData();
		}
	}

	/** This corresponds to the url of the image files displayed on the job page.
	 * @see hudson.model.Action#getUrlName()
	 */
	public String getUrlName() {
		return "neoload";
	}

	/** Function to convert a NeoLoadReportDoc to an average response time. */
	private transient final Function<NeoLoadReportDoc, Float> averageResponseTimeFunction = new Function<NeoLoadReportDoc, Float>() {
		public Float apply(final NeoLoadReportDoc nlrd) {
			try {
				return nlrd.getAverageResponseTime();
			} catch (final XPathExpressionException e) {
				LOGGER.log(Level.FINE, "Error reading average response time from " + nlrd.getDoc().getDocumentURI() + ". " + e.getMessage(), e);
			}
			return null;
		}
	};

	/** Function to convert a NeoLoadReportDoc to an average response time. */
	private transient final Function<NeoLoadReportDoc, Float> errorRateFunction = new Function<NeoLoadReportDoc, Float>() {
		public Float apply(final NeoLoadReportDoc nlrd) {
			try {
				return nlrd.getErrorRatePercentage();
			} catch (final XPathExpressionException e) {
				LOGGER.log(Level.FINE, "Error reading error rate from " + nlrd.getDoc().getDocumentURI() + ". " + e.getMessage(), e);
			}
			return null;
		}
	};
	
	private static String getTypeByStatistic(final String statistic) {
		final String value;
		switch (statistic) {
		case "percentile":
			value = PERCENTILE2;
			break;
		case "average":
			value = AVG;
			break;
		case "error":
			value = ERROR_RATE;
			break;
		default:
			value = VAL;
			break;
		}
		return value;
	}

	/** Find data to graph. */
	public void refreshGraphData() {
		LOGGER.finest("Finding builds to use for NeoLoad graphs. Currently I see " + buildsAndDocs.size());
		try {
			NeoLoadReportDoc doc = null;
			final Map<AbstractBuild<?, ?>, NeoLoadReportDoc> newBuildsAndDocs = new LinkedHashMap<AbstractBuild<?, ?>, NeoLoadReportDoc>();

			// look through all builds of the job
			for (final AbstractBuild<?, ?> build: project.getBuilds()) {
				doc = findXMLResultsFile(build);

				// if the correct file was found, and
				// only include successful builds.
				if (build != null && build.getResult() != null && build.getResult().isBetterThan(Result.FAILURE)) {

					// add the html results link to the build if it's not already there.
					// this is done here as well for builds that exist and don't already have the action.
					// this covers the case when the plugin is uninstalled, the action is lost,
					// and then the plugin is reinstalled.
					NeoResultsAction.addActionIfNotExists(build);

					// find the xml results file.
					doc = findXMLResultsFile(build);

					// if the correct file was found
					if (doc != null) {
						newBuildsAndDocs.put(build, doc);
					}
				}
			}

			// switch out the data for the new view
			final Map<AbstractBuild<?, ?>, NeoLoadReportDoc> oldBuildsAndDocs = buildsAndDocs;
			buildsAndDocs = newBuildsAndDocs;
			oldBuildsAndDocs.clear();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error finding NeoLoad xml results. " + e.getMessage(), e);
		}

		LOGGER.finer("Found " + buildsAndDocs.size() + " builds to use for NeoLoad graphs.");
	}

	/**
	 * @return true if we should show the graph
	 */
	public boolean showAvgGraph() {
		if (!isShowTrendAverageResponse()) {
			LOGGER.finer("Plugin options say the avg graph is OFF.");
			return false;
		}

		final boolean graphDataExists = graphDataExists();
		LOGGER.finest("avg graph. Graph data exists: " + graphDataExists);
		return graphDataExists;
	}

	/**
	 * @return true if we should show the graph
	 */
	public boolean showErrGraph() {
		if (!isShowTrendErrorRate()) {
			LOGGER.finer("Plugin options say the error graph is OFF.");
			return false;
		}

		return graphDataExists();
	}

	/**
	 * @return the number of custom graph
	 */
	public int sizeCustomGraph() {
		if (graphDataExists()) {
			return getGraphOptionsInfo().size();
		}

		LOGGER.finer("Plugin options : no graph data.");
		return 0;
	}

	/**
	 * @return the height of custom graph (in px).
	 */
	public String heightCustomGraph() {
		if (graphDataExists()) {
			return ((int) Math.floor(getGraphOptionsInfo().size()/2)) * 350 + "px";
		}
		return 0 + "px";
	}

	/**
	 * @param index the index of the graph.
	 * @return the name of the graph.
	 */
	public String displayCustomGraphName(final int index) {
		if (graphDataExists()) {
			LOGGER.finer("Display Graph name at (" + index + ") : " + getGraphOptionsInfo().get(index).getName());
			return getGraphOptionsInfo().get(index).getName();
		}

		LOGGER.finer("Plugin options : no graph data.");
		return null;
	}

	/**
	 * @return true if enough data exists to create a graph
	 */
	public boolean graphDataExists() {
		// there must be at least two results to create the graph
		return buildsAndDocs.size() > 1;
	}

	private final class GraphDataGrabber {
		/** Convert a graph into an average response time or an error rate. */
		private final Function<NeoLoadReportDoc, Float> dataConverter;

		private final String successMessage;
		private final String yAxisLabel;
		private final Color lineColor;
		
		/**
		 * @param neoLoadReportDoc
		 * @param dataConverter
		 * @param successMessage
		 * @param yAxisLabel
		 * @param lineColor
		 */
		public GraphDataGrabber(final Function<NeoLoadReportDoc, Float> dataConverter, final String successMessage, final String yAxisLabel, final Color lineColor) {
			this.dataConverter = dataConverter;
			this.successMessage = successMessage;
			this.yAxisLabel = yAxisLabel;
			this.lineColor = lineColor;
		}

		public NeoLoadGraph go() {
			final DefaultCategoryDataset ds = new DefaultCategoryDataset();
			Float value = null;
			final Map<String, Float> nums = new LinkedHashMap<String, Float>(); // linked hash maps keep the order of their keys

			// get the number we want from all builds that we found earlier
			for (final Entry<AbstractBuild<?, ?>, NeoLoadReportDoc> entry: buildsAndDocs.entrySet()) {
				final AbstractBuild<?, ?> build = entry.getKey();
				final NeoLoadReportDoc nlrd = entry.getValue();
				final String buildName = build.getDisplayName() == null ? "#" + build.number : build.getDisplayName();

				value = dataConverter.apply(nlrd);

				if (value != null) {
					LOGGER.log(Level.FINEST, successMessage + buildName + ": " + value);
				}

				if (value != null) {
					// use the custom name, otherwise use the default name.
					nums.put(buildName, value);
				}
			}
			final List<String> keys = new ArrayList<String>(nums.keySet());

			// reverse the keys so that they appear in the correct order in the graphs.
			for (final String buildName: Iterables.reverse(keys)) {
				ds.addValue(nums.get(buildName), "Time", buildName);
			}

			// color from ColorTable.java
			return new NeoLoadGraph(ds, yAxisLabel, lineColor);
		}
	}
	
	private final class GraphDataGrabberCustom {
		private final GraphOptionsInfo customGraphInfo;
		
		/**
		 * @param customGraphInfo
		 */
		public GraphDataGrabberCustom(final GraphOptionsInfo customGraphInfo) {
			this.customGraphInfo = customGraphInfo;
		}

		public NeoLoadGraph go() {
			final DefaultCategoryDataset ds = new DefaultCategoryDataset();
			int numberOfColor = 0;
			// get the number we want from all builds that we found earlier
			for (final GraphOptionsCurveInfo curve : customGraphInfo.getCurve()) {
				final Map<String, Float> nums = new LinkedHashMap<String, Float>(); // linked hash maps keep the order of their keys
				numberOfColor++;
				for (final Entry<AbstractBuild<?, ?>, NeoLoadReportDoc> entry: buildsAndDocs.entrySet()) {
					final AbstractBuild<?, ?> build = entry.getKey();
					final NeoLoadReportDoc nlrd = entry.getValue();
					final String buildName = build.getDisplayName() == null ? "#" + build.number : build.getDisplayName();
					Float value;
					
					try {
						value = nlrd.getCustom(getXPathForCustomGraph(curve.getPath(), getTypeByStatistic(customGraphInfo.getStatistic())));
						if (value != null) {
							// use the custom name, otherwise use the default name.
							nums.put(buildName, value);
						}
						else {
							value = nlrd.getCustom(getXPathForCustomMonitorOrLGGraph(curve.getPath(), getTypeByStatistic(customGraphInfo.getStatistic())));
							if (value != null) {
								// use the custom name, otherwise use the default name.
								nums.put(buildName, value);
							}
							else {
								LOGGER.warning("No custom value for XPath : " + curve.getPath());
							}
						}
					} catch (XPathExpressionException e) {
						LOGGER.finest("Error XPATH : " + e.getStackTrace());
					}
				}
				final List<String> keys = new ArrayList<String>(nums.keySet());

				// reverse the keys so that they appear in the correct order in the graphs.
				for (final String buildName: Iterables.reverse(keys)) {
					ds.addValue(nums.get(buildName), curve.getPath(), buildName);
				}
			}

			// color from ColorTable.java
			return new NeoLoadGraph(ds, customGraphInfo.getStatistic(), numberOfColor, customGraphInfo.getName());
		}
	}
	
	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	public NeoLoadGraph getErrGraph() {
		return new GraphDataGrabber(errorRateFunction, "Error rate found for build ", "Error Rate %", new Color(200, 0, 0)).go();
	}

	/** Generates a graph
	 * @throws XPathExpressionException
	 */
	public NeoLoadGraph getAvgGraph() {
		return new GraphDataGrabber(averageResponseTimeFunction, "Average response time found for build ", "Avg Resp Time (secs)", new Color(237, 184, 0)).go();
	}

	/** Generates a graph
	 * @param index
	 * @throws XPathExpressionException
	 */
	public NeoLoadGraph getCustomGraph(final int index) {
		return new GraphDataGrabberCustom(graphOptionsInfo.get(index)).go();
	}

	/**
	 * @param build
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	@SuppressWarnings("rawtypes")
	static NeoLoadReportDoc findXMLResultsFile(final AbstractBuild<?, ?> build) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		Artifact artifact = null;
		final Iterator<Artifact> it = new ArrayList<Artifact>(build.getArtifacts()).iterator();
		NeoLoadReportDoc correctDoc = null;

		// remove files that don't match
		while (it.hasNext()) {
			artifact = it.next();

			final String fileNameAbsolutePath = artifact.getFile().getAbsolutePath();
			final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(fileNameAbsolutePath);

			// if the file is valid and was created during this build
			if (!"xml".equalsIgnoreCase(FileUtils.extension(fileNameAbsolutePath))) {
				it.remove();

			} else if (!nlrd.isValidReportDoc()) {
				LOGGER.finest("Non-trend graph xml file found. File " + fileNameAbsolutePath);
				it.remove();
			} else if (findBuildNumberUsingPattern(nlrd) > -1) {
				correctDoc = nlrd;
				break;

			} else if (!nlrd.hasCorrespondingDate(build)) {
				// it's a valid report file but it's too old
				LOGGER.finest("Build " + build.number + ", Valid report file does not have a corresponding date. File " + fileNameAbsolutePath +
						", Internal time must be after " + DateFormatUtils.format(build.getTimestamp(), NeoLoadReportDoc.STANDARD_TIME_FORMAT));
				it.remove();
			} else {
				LOGGER.finest("Build " + build.number + ", Valid report file found. File " + fileNameAbsolutePath +
						", Internal time is after " + DateFormatUtils.format(build.getTimestamp(), NeoLoadReportDoc.STANDARD_TIME_FORMAT));
				correctDoc = nlrd;
				break;
			}
		}

		return correctDoc;
	}

	private static int findBuildNumberUsingPattern(final NeoLoadReportDoc nlrd) {
		try {
			final Node node = XMLUtilities.findFirstByExpression("/report/summary/test/@build_number", nlrd.getDoc());
			final String nodeValue = node.getNodeValue();
			return PluginUtils.findBuildNumberUsingPattern(nodeValue);
		} catch (final Exception e) {
			// ignored. it's not that important.
		}

		return PluginUtils.findBuildNumberUsingPattern("");
	}

	/* (non-Javadoc)
	 * @see hudson.model.Action#getIconFileName()
	 */
	public String getIconFileName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see hudson.model.Action#getDisplayName()
	 */
	public String getDisplayName() {
		return "!" + this.getClass().getSimpleName() + "!";
	}

	/** @return the showTrendAverageResponse */
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	/** @return the showTrendErrorRate */
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}
	
	/** @return the graphOptionsInfo */
	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		return graphOptionsInfo;
	}
}
