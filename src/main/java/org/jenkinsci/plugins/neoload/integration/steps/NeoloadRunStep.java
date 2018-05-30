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
package org.jenkinsci.plugins.neoload.integration.steps;

import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.neoload.integration.supporting.*;
import org.jenkinsci.plugins.structs.describable.UninstantiatedDescribable;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.*;

/**
 * Wrapper for NeoLoad Launch with pipeline.
 */
public class NeoloadRunStep extends AbstractStepImpl implements NeoloadGraphDefinitionStep{

	/**
	 * The constant DEFAULT_SHOW_TREND_AVERAGE_RESPONSE.
	 */
	public static final boolean DEFAULT_SHOW_TREND_AVERAGE_RESPONSE =false;
	/**
	 * The constant DEFAULT_SHOW_TREND_ERROR_RATE.
	 */
	public static final boolean DEFAULT_SHOW_TREND_ERROR_RATE = false;
	/**
	 * The constant LICENSE_TYPE_LOCAL.
	 */
	public static final String LICENSE_TYPE_LOCAL = "licenseTypeLocal";
	public static final String LICENSE_TYPE_SHARED = "licenseTypeShared";
	/**
	 * The constant PROJECT_TYPE_LOCAL.
	 */
	public static final String PROJECT_TYPE_LOCAL = "projectTypeLocal";
	public static final String PROJECT_TYPE_SHARED ="projectTypeShared";

	/**
	 * The constant DEFAULT_HTML_REPORT.
	 */
	public static final String DEFAULT_HTML_REPORT = "neoload-report/report.html";
	/**
	 * The constant DEFAULT_JUNIT_REPORT.
	 */
	public static final String DEFAULT_JUNIT_REPORT = "neoload-report/junit-sla-results.xml";
	/**
	 * The constant DEFAULT_XML_REPORT.
	 */
	public static final String DEFAULT_XML_REPORT = "neoload-report/report.xml";
	/**
	 * The constant DEFAULT_TEST_NAME.
	 */
	public static final String DEFAULT_TEST_NAME = "$Date{hh:mm - dd MMM yyyy} (build ${BUILD_NUMBER})";

	public static final String DEFAULT_TEST_DESCRIPTION  = "";

	public static final String REPORT_TYPE_CUSTOM = "reportTypeCustom";
	public static final String REPORT_TYPE_DEFAULT = "reportTypeDefault";




	/**
	 * Default Values are used for the snippet generator to not generate fields when not needed
	 */

	private String executable;
	private String projectType;
	private String reportType = REPORT_TYPE_DEFAULT;
	private String localProjectFile;
	private ServerInfo sharedProjectServer;
	private String scenarioName;
	private String htmlReport = DEFAULT_HTML_REPORT;
	private String xmlReport = DEFAULT_XML_REPORT;
	private String pdfReport = "";
	private String junitReport = DEFAULT_JUNIT_REPORT;
	private boolean displayGUI;
	private String testResultName = DEFAULT_TEST_NAME;
	private String testDescription = "";
	private String customCommandLineOptions = "";
	private int maxTrends;
	private boolean showTrendAverageResponse = DEFAULT_SHOW_TREND_AVERAGE_RESPONSE;
	private boolean showTrendErrorRate = DEFAULT_SHOW_TREND_ERROR_RATE;
	private List<GraphOptionsInfo> graphs;
	private String licenseType = LICENSE_TYPE_LOCAL;
	private NTSServerInfo licenseServer;
	private String sharedProjectName;
	private String licenseVUCount;
	private String licenseDuration;
	private boolean publishTestResults;
	private boolean autoArchive = true;


	/**
	 * Instantiates a new Neoload run step.
	 *
	 * @param scenarioName the scenario name
	 */
	@DataBoundConstructor
	public NeoloadRunStep(final String scenarioName) {
		this.scenarioName = scenarioName;
	}

	@DataBoundSetter
	public void setExecutable(final String executable) {
		this.executable = executable;
	}

	/**
	 * Gets license type.
	 *
	 * @return the license type
	 */
	public String getLicenseType() {
		return licenseType;
	}

	/**
	 * Sets license type.
	 *
	 * @param licenseType the license type
	 */
	@DataBoundSetter
	public void setLicenseType(final String licenseType) {
		this.licenseType = licenseType;
	}

	/**
	 * Gets project type.
	 *
	 * @return the project type
	 */
	public String getProjectType() {
		return projectType;
	}

	/**
	 * Sets project type.
	 *
	 * @param projectType the project type
	 */
	@DataBoundSetter
	public void setProjectType(final String projectType) {
		this.projectType = projectType;
	}

	/**
	 * Gets report type.
	 *
	 * @return the report type
	 */
	public String getReportType() {
		return reportType;
	}

	/**
	 * Sets report type.
	 *
	 * @param reportType the report type
	 */
	@DataBoundSetter
	public void setReportType(final String reportType) {
		this.reportType = reportType;
	}

	/**
	 * Gets local project file.
	 *
	 * @return the local project file
	 */
	public String getLocalProjectFile() {
		return localProjectFile;
	}

	/**
	 * Sets local project file.
	 *
	 * @param localProjectFile the local project file
	 */
	@DataBoundSetter
	public void setLocalProjectFile(final String localProjectFile) {
		this.localProjectFile = localProjectFile;
	}

	/**
	 * Gets scenario name.
	 *
	 * @return the scenario name
	 */
	public String getScenarioName() {
		return scenarioName;
	}

	/**
	 * Gets html report.
	 *
	 * @return the html report
	 */
	public String getHtmlReport() {
		return htmlReport;
	}

	/**
	 * Sets html report.
	 *
	 * @param htmlReport the html report
	 */
	@DataBoundSetter
	public void setHtmlReport(final String htmlReport) {
		this.htmlReport = htmlReport;
	}

	/**
	 * Gets xml report.
	 *
	 * @return the xml report
	 */
	public String getXmlReport() {
		return xmlReport;
	}

	/**
	 * Sets xml report.
	 *
	 * @param xmlReport the xml report
	 */
	@DataBoundSetter
	public void setXmlReport(final String xmlReport) {
		this.xmlReport = xmlReport;
	}


	/**
	 * Gets license server.
	 *
	 * @return the license server
	 */
	public NTSServerInfo getLicenseServer() {
		return licenseServer;
	}

	/**
	 * Sets license server.
	 *
	 * @param licenseServer the license server
	 */
	@DataBoundSetter
	public void setLicenseServer(final NTSServerInfo licenseServer) {
		this.licenseServer = licenseServer;
	}


	/**
	 * Gets pdf report.
	 *
	 * @return the pdf report
	 */
	public String getPdfReport() {
		return pdfReport;
	}

	/**
	 * Sets pdf report.
	 *
	 * @param pdfReport the pdf report
	 */
	@DataBoundSetter
	public void setPdfReport(final String pdfReport) {
		this.pdfReport = pdfReport;
	}

	/**
	 * Gets junit report.
	 *
	 * @return the junit report
	 */
	public String getJunitReport() {
		return junitReport;
	}

	/**
	 * Sets junit report.
	 *
	 * @param junitReport the junit report
	 */
	@DataBoundSetter
	public void setJunitReport(final String junitReport) {
		this.junitReport = junitReport;
	}

	/**
	 * Is display the gui boolean.
	 *
	 * @return the boolean
	 */
	public boolean isDisplayGUI() {
		return displayGUI;
	}

	/**
	 * Sets display the gui.
	 *
	 * @param displayGUI the display the gui
	 */
	@DataBoundSetter
	public void setDisplayGUI(final boolean displayGUI) {
		this.displayGUI = displayGUI;
	}

	/**
	 * Gets test result name.
	 *
	 * @return the test result name
	 */
	public String getTestResultName() {
		return testResultName;
	}

	/**
	 * Sets test result name.
	 *
	 * @param testResultName the test result name
	 */
	@DataBoundSetter
	public void setTestResultName(final String testResultName) {
		this.testResultName = testResultName;
	}

	/**
	 * Gets test description.
	 *
	 * @return the test description
	 */
	@CheckForNull
	public String getTestDescription() {
		return testDescription;
	}

	/**
	 * Sets test description.
	 *
	 * @param testDescription the test description
	 */
	@DataBoundSetter
	public void setTestDescription(@CheckForNull final String testDescription) {
		this.testDescription = Util.fixNull(testDescription);
	}

	/**
	 * Gets custom command line options.
	 *
	 * @return the custom command line options
	 */
	public String getCustomCommandLineOptions() {
		return customCommandLineOptions;
	}

	/**
	 * Sets custom command line options.
	 *
	 * @param customCommandLineOptions the custom command line options
	 */
	@DataBoundSetter
	public void setCustomCommandLineOptions(final String customCommandLineOptions) {
		this.customCommandLineOptions = customCommandLineOptions;
	}

	/**
	 * Gets max trends.
	 *
	 * @return the max trends
	 */
	public int getMaxTrends() {
		return maxTrends;
	}

	/**
	 * Sets max trends.
	 *
	 * @param maxTrends the max trends
	 */
	@DataBoundSetter
	public void setMaxTrends(final int maxTrends) {
		this.maxTrends = maxTrends;
	}

	/**
	 * Is show trend average response boolean.
	 *
	 * @return the boolean
	 */
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	/**
	 * Sets show trend average response.
	 *
	 * @param showTrendAverageResponse the show trend average response
	 */
	@DataBoundSetter
	public void setShowTrendAverageResponse(final boolean showTrendAverageResponse) {
		this.showTrendAverageResponse = showTrendAverageResponse;
	}

	/**
	 * Is show trend error rate boolean.
	 *
	 * @return the boolean
	 */
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}

	/**
	 * Sets show trend error rate.
	 *
	 * @param showTrendErrorRate the show trend error rate
	 */
	@DataBoundSetter
	public void setShowTrendErrorRate(final boolean showTrendErrorRate) {
		this.showTrendErrorRate = showTrendErrorRate;
	}

	/**
	 * Gets graph options info.
	 *
	 * @return the graph options info
	 */
	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		return graphs;
	}

	/**
	 * Sets graph options info.
	 *
	 * @param graphOptionsInfo the graph options info
	 */
	@DataBoundSetter
	public void setGraphOptionsInfo(final List<GraphOptionsInfo> graphOptionsInfo) {
		this.graphs = graphOptionsInfo;
	}

	/**
	 * Gets neo shared project.
	 *
	 * @return the neo shared project
	 */
	public ServerInfo getSharedProjectServer() {
		return sharedProjectServer;
	}

	/**
	 * Sets neo shared project.
	 *
	 * @param sharedProjectServer the neo shared project
	 */
	@DataBoundSetter
	public void setSharedProjectServer(final ServerInfo sharedProjectServer) {
		this.sharedProjectServer = sharedProjectServer;
	}



	/**
	 * Gets executable.
	 *
	 * @return the executable
	 */
	public String getExecutable() {
		return executable;
	}


	@DataBoundSetter
	public void setScenarioName(final String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public String getSharedProjectName() {
		return sharedProjectName;
	}

	@DataBoundSetter
	public void setSharedProjectName(final String sharedProjectName) {
		this.sharedProjectName = sharedProjectName;
	}


	public String getLicenseVUCount() {
		return licenseVUCount;
	}

	@DataBoundSetter
	public void setLicenseVUCount(final String licenseVUCount) {
		this.licenseVUCount = licenseVUCount;
	}

	public String getLicenseDuration() {
		return licenseDuration;
	}

	@DataBoundSetter
	public void setLicenseDuration(final String licenseDuration) {
		this.licenseDuration = licenseDuration;
	}

	public boolean isPublishTestResults() {
		return publishTestResults;
	}

	@DataBoundSetter
	public void setPublishTestResults(final boolean publishTestResults) {
		this.publishTestResults = publishTestResults;
	}

	public boolean isAutoArchive() {
		return autoArchive;
	}

	@DataBoundSetter
	public void setAutoArchive(final boolean autoArchive) {
		this.autoArchive = autoArchive;
	}

	/**
	 * The type Descriptor.
	 */
	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {



		/**
		 * Instantiates a new Descriptor.
		 */
		public DescriptorImpl() {
			super(NeoloadRunStepExecution.class);
		}

		@Override
		public String getFunctionName() {
			return "neoloadRun";
		}

		@Nonnull
		@Override
		public String getDisplayName() {
			return "Run a NeoLoad scenario";
		}

		@Override
		public Map<String, Object> defineArguments(final Step step) {
			return PipelineAsCodeEncodeDecode.encode((NeoloadRunStep) step);
		}

		public UninstantiatedDescribable uninstantiate(final Step step) throws UnsupportedOperationException {
			return new UninstantiatedDescribable(defineArguments(step));
		}


		@Override
		public Step newInstance(final Map<String, Object> arguments) throws Exception {
			return PipelineAsCodeEncodeDecode.decode(arguments);
		}


		/**
		 * Do fill licence name items list box model.
		 *
		 * @param project the project
		 * @return the list box model
		 */
		public static ListBoxModel doFillLicenseServerItems(@AncestorInPath final Item project) {
			return PluginUtils.getServerInfosListBox(false);
		}

		/**
		 * Do fill project server name items list box model.
		 *
		 * @param project the project
		 * @return the list box model
		 */
		public static ListBoxModel doFillSharedProjectServerItems(@AncestorInPath final Item project) {
			return PluginUtils.getServerInfosListBox(true);
		}
	}
}
