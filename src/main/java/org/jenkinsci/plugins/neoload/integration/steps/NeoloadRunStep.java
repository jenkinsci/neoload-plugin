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
import org.jenkinsci.plugins.neoload.integration.supporting.GraphOptionsInfo;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Wrapper for NeoLoad Launch with pipeline.
 */
public class NeoloadRunStep extends AbstractStepImpl {

	/**
	 * Default Values are used for the snippet generator to not generate fields when not needed
	 */

	private final String executable;
	private String projectType;
	private String reportType;
	private String localProjectFile;
	private SharedNeoLoadProject neoSharedProject;
	private String scenarioName;
	private String htmlReport = DescriptorImpl.defaultHtmlReport;
	private String xmlReport = DescriptorImpl.defaultXmlReport;
	private String pdfReport = DescriptorImpl.defaultEmptyStringValue;
	private String junitReport = DescriptorImpl.defaultJunitReport;
	private boolean displayTheGUI;
	private String testResultName = DescriptorImpl.defaultResultName;
	private SharedNeoLoadLicense neoSharedLicence;
	private String testDescription = DescriptorImpl.defaultEmptyStringValue;
	private String customCommandLineOptions = DescriptorImpl.defaultEmptyStringValue;
	private int maxTrends;
	private boolean showTrendAverageResponse = DescriptorImpl.defaultShowTrendAverageResponse;
	private boolean showTrendErrorRate = DescriptorImpl.defaultShowTrendErrorRate;
	private List<GraphOptionsInfo> graphOptionsInfo;
	private String licenseType = DescriptorImpl.defaultLicenceType;

	public String getLicenseType() {
		return licenseType;
	}

	@DataBoundSetter
	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}

	public String getProjectType() {
		return projectType;
	}

	public String getReportType() {
		return reportType;
	}

	@DataBoundSetter
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getLocalProjectFile() {
		return localProjectFile;
	}

	@DataBoundSetter
	public void setLocalProjectFile(String localProjectFile) {
		this.localProjectFile = localProjectFile;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public String getHtmlReport() {
		return htmlReport;
	}

	@DataBoundSetter
	public void setHtmlReport(String htmlReport) {
		this.htmlReport = htmlReport;
	}

	public String getXmlReport() {
		return xmlReport;
	}

	@DataBoundSetter
	public void setXmlReport(String xmlReport) {
		this.xmlReport = xmlReport;
	}

	public String getPdfReport() {
		return pdfReport;
	}

	@DataBoundSetter
	public void setPdfReport(String pdfReport) {
		this.pdfReport = pdfReport;
	}

	public String getJunitReport() {
		return junitReport;
	}

	@DataBoundSetter
	public void setJunitReport(String junitReport) {
		this.junitReport = junitReport;
	}

	public boolean isDisplayTheGUI() {
		return displayTheGUI;
	}

	@DataBoundSetter
	public void setDisplayTheGUI(boolean displayTheGUI) {
		this.displayTheGUI = displayTheGUI;
	}

	public String getTestResultName() {
		return testResultName;
	}

	@DataBoundSetter
	public void setTestResultName(String testResultName) {
		this.testResultName = testResultName;
	}

	@CheckForNull
	public String getTestDescription() {
		return testDescription;
	}

	@DataBoundSetter
	public void setTestDescription(@CheckForNull String testDescription) {
		this.testDescription = Util.fixNull(testDescription);
	}

	@DataBoundSetter
	public void setNeoSharedLicence(final SharedNeoLoadLicense neoLicence){
		this.neoSharedLicence = neoLicence;
	}

	public SharedNeoLoadLicense getNeoSharedLicence() {
		return neoSharedLicence;
	}

	public String getCustomCommandLineOptions() {
		return customCommandLineOptions;
	}

	@DataBoundSetter
	public void setCustomCommandLineOptions(String customCommandLineOptions) {
		this.customCommandLineOptions = customCommandLineOptions;
	}

	public int getMaxTrends() {
		return maxTrends;
	}

	@DataBoundSetter
	public void setMaxTrends(int maxTrends) {
		this.maxTrends = maxTrends;
	}

	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	@DataBoundSetter
	public void setShowTrendAverageResponse(boolean showTrendAverageResponse) {
		this.showTrendAverageResponse = showTrendAverageResponse;
	}

	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}

	@DataBoundSetter
	public void setShowTrendErrorRate(boolean showTrendErrorRate) {
		this.showTrendErrorRate = showTrendErrorRate;
	}

	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		return graphOptionsInfo;
	}

	@DataBoundSetter
	public void setGraphOptionsInfo(List<GraphOptionsInfo> graphOptionsInfo) {
		this.graphOptionsInfo = graphOptionsInfo;
	}

	@DataBoundSetter
	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}


	public SharedNeoLoadProject getNeoSharedProject() {
		return neoSharedProject;
	}

	@DataBoundSetter
	public void setNeoSharedProject(SharedNeoLoadProject neoSharedProject) {
		this.neoSharedProject = neoSharedProject;
	}

	public String getExecutable() {
		return executable;
	}

	@DataBoundConstructor
	public NeoloadRunStep(final String executable, final String scenarioName) {
		this.executable = executable;
		this.scenarioName = scenarioName;
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {
		public DescriptorImpl() {
			super(NeoloadRunStepExecution.class);
		}

		public static final boolean defaultShowTrendAverageResponse = true;
		public static final boolean defaultShowTrendErrorRate = true;
		public static final String defaultLicenceType = "licenseTypeLocal";
		public static final String defaultProjectType = "projectTypeLocal";
		public static final String sharedProjectType = "projectTypeShared";
		public static final String defaultEmptyStringValue = "";
		public static final String defaultHtmlReport = "./neoload-report/report.html";
		public static final String defaultJunitReport = "./neoload-report/junit-sla-results.xml";
		public static final String defaultXmlReport = "./neoload-report/report.xml";
		public static final String defaultResultName = "$Date{hh:mm - dd MMM yyyy} (build ${BUILD_NUMBER})";

		@Override
		public String getFunctionName() {
			return "neoloadRun";
		}

		@Nonnull
		@Override
		public String getDisplayName() {
			return "Run a NeoLoad scenario";
		}
	}
}
