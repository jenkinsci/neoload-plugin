package org.jenkinsci.plugins.neoload_integration;

import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run.Artifact;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadGraph;
import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadPluginOptions;
import org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.xml.sax.SAXException;

import com.neotys.nl.controller.report.transform.NeoLoadReportDoc;

/** Along with the jelly file and the Factory class, this class adds the two trend graphs to a job page. */
public class ProjectSpecificAction implements ProminentProjectAction {

	/** A link to the Jenkins job. */
	private AbstractProject<?, ?> project;

	/** Key is the build. Value is the NeoLoad xml report file. */
	private Map<AbstractBuild<?, ?>, NeoLoadReportDoc> buildsAndDocs = new LinkedHashMap<AbstractBuild<?, ?>, NeoLoadReportDoc>();

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(ProjectSpecificAction.class.getName());

	public ProjectSpecificAction(AbstractProject<?, ?> project) {
		this.project = project;
	}

    /** This corresponds to the url of the image files displayed on the job page.
	 * @see hudson.model.Action#getUrlName()
	 */
	public String getUrlName() {
		return "neoload";
	}
	
	/** Find data to graph. */
	public void refreshGraphData() {
		LOGGER.finest("Finding builds to use for NeoLoad graphs. Currently I see " + buildsAndDocs.size());
		try {
			NeoLoadReportDoc doc = null;
			Map<AbstractBuild<?, ?>, NeoLoadReportDoc> newBuildsAndDocs = new LinkedHashMap<AbstractBuild<?, ?>, NeoLoadReportDoc>();

			// look through all builds of the job
			for (AbstractBuild<?, ?> build : project.getBuilds()) {
				// only include successful builds.
				if (build != null && build.getResult() != null && build.getResult().isBetterThan(Result.FAILURE)) {
					doc = findXMLResultsFile(build);
					
					// if the correct file was found
					if (doc != null) {
						newBuildsAndDocs.put(build, doc);
					}
				}
			}

			// switch out the data for the new view
			Map<AbstractBuild<?, ?>, NeoLoadReportDoc> oldBuildsAndDocs = buildsAndDocs;
			buildsAndDocs = newBuildsAndDocs;
			oldBuildsAndDocs.clear();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error finding NeoLoad xml results. " + e.getMessage(), e);
		}
		
		LOGGER.finer("Found " + buildsAndDocs.size() + " builds to use for NeoLoad graphs.");
	}

	/**
	 * @return true if we should show the graph
	 */
	public boolean showAvgGraph() {
		NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(project);
		if ((npo == null) || (!npo.isShowTrendAverageResponse())) {
			LOGGER.finer("Plugin options say the avg graph is OFF.");
			return false;
		}

		final boolean graphDataExists = getAvgGraphPoints().size() > 1;
		LOGGER.finest("avg graph. Graph data exists: " + graphDataExists);
		return graphDataExists;
	}

	/**
	 * @return true if we should show the graph
	 */
	public boolean showErrGraph() {
		NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(project);
		if ((npo == null) || (!npo.isShowTrendErrorRate())) {
			LOGGER.finer("Plugin options say the error graph is OFF.");
			return false;
		}

		final boolean graphDataExists = getErrGraphPoints().size() > 1;
		LOGGER.finest("err graph. Graph data exists: " + graphDataExists);
		return graphDataExists;
	}

	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	public NeoLoadGraph getErrGraph() {
		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		// linked hash maps keep the order of their keys
		Map<String, Float> graphPoints = getErrGraphPoints();
		
		List<String> reverseKeys = new ArrayList<String>(graphPoints.keySet());
		Collections.reverse(reverseKeys);
		
		for (String b: reverseKeys) {
			ds.addValue(graphPoints.get(b), "Time", b);
		}
		// color from ColorTable.java
		return new NeoLoadGraph(ds, "Error Rate %", new Color(200, 0, 0));
	}

	/**
	 * @return
	 */
	private Map<String, Float> getErrGraphPoints() {
		Float errorRate;
		NeoLoadReportDoc nlrd = null;
		Map<String, Float> graphPoints = new LinkedHashMap<String, Float>();

		// get the number we want from all builds that we found earlier
		for (AbstractBuild<?, ?> build : buildsAndDocs.keySet()) {
			errorRate = null;
			try {
				nlrd = buildsAndDocs.get(build);
				errorRate = nlrd.getErrorRatePercentage();
				LOGGER.log(Level.FINE, "Error rate found for build " + build.number + ": " + errorRate);
			} catch (XPathExpressionException e) {
				LOGGER.log(Level.FINE, "Error reading error rate from " + nlrd.getDoc().getDocumentURI() + ". " + e.getMessage(), e);
			}

			if (errorRate != null) {
				// use the custom name, otherwise use the default name.
				String buildName = build.getDisplayName() == null ? "#" + build.number : build.getDisplayName();
				graphPoints.put(buildName, errorRate);
			}
		}
		
		return graphPoints;
	}

	/** Generates a graph 
	 * @throws XPathExpressionException
	 */
	public NeoLoadGraph getAvgGraph() {
		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		
		// linked hash maps keep the order of their keys
		Map<String, Float> nums = getAvgGraphPoints();
		List<String> reverseKeys = new ArrayList<String>(nums.keySet());
		Collections.reverse(reverseKeys);
		
		for (String b: reverseKeys) {
			ds.addValue(nums.get(b), "Time", b);
		}

		// color from ColorTable.java
		return new NeoLoadGraph(ds, "Avg Resp Time (secs)", new Color(237, 184, 0));
	}

	/**
	 * @return
	 */
	private Map<String, Float> getAvgGraphPoints() {
		Float avgResponseTime;
		NeoLoadReportDoc nlrd = null;
		// linked hash maps keep the order of their keys
		Map<String, Float> nums = new LinkedHashMap<String, Float>(); 

		// get the number we want from all builds that we found earlier
		for (AbstractBuild<?, ?> build : buildsAndDocs.keySet()) {
			avgResponseTime = null;
			try {
				nlrd = buildsAndDocs.get(build);
				avgResponseTime = nlrd.getAverageResponseTime();
				LOGGER.log(Level.FINE, "Average response time found for build " + build.number + ": " + avgResponseTime);
			} catch (XPathExpressionException e) {
				LOGGER.log(Level.FINE, "Error reading average response time from " + nlrd.getDoc().getDocumentURI() + ". " + e.getMessage(), e);
			}

			if (avgResponseTime != null) {
				// use the custom name, otherwise use the default name.
				String buildName = build.getDisplayName() == null ? "#" + build.number : build.getDisplayName();
				nums.put(buildName, avgResponseTime);
			}
		}
		return nums;
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
	private static NeoLoadReportDoc findXMLResultsFile(final AbstractBuild build) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		Artifact artifact = null;
		Iterator<Artifact> it = build.getArtifacts().iterator();
		NeoLoadReportDoc correctDoc = null;

		// remove files that don't match
		while (it.hasNext()) {
			artifact = it.next();

			final String fileNameAbsolutePath = artifact.getFile().getAbsolutePath();

			// if the file is valid and was created during this build
			if (!"xml".equalsIgnoreCase(FileUtils.extension(fileNameAbsolutePath))) {
				it.remove();
				continue;
			}
			
			NeoLoadReportDoc nlrd = new NeoLoadReportDoc(fileNameAbsolutePath);
			if (!nlrd.isValidReportDoc()) {
				LOGGER.finest("Non-trend graph xml file found. File " + fileNameAbsolutePath);
				it.remove();
				
			} else if (!nlrd.isNewerThan(build.getTimestamp())) {
				// it's a valid report file but it's too old
				LOGGER.finest("Valid report file is too old. File " + fileNameAbsolutePath + " must have internal start time after " +
						DateFormatUtils.format(build.getTimestamp(), "yyyy-MM-dd kk:mm:ss"));
				it.remove();
			} else {
				LOGGER.finest("Valid report file found. " + fileNameAbsolutePath);
				correctDoc = nlrd;
				break;
			}
		}

		return correctDoc;
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
}
