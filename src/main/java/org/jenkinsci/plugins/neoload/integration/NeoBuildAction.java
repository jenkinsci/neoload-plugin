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
package org.jenkinsci.plugins.neoload.integration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.neotys.nls.security.tools.PasswordEncoder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;

import hudson.tasks.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jenkinsci.plugins.neoload.integration.supporting.*;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Joiner;

import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

/**
 * This class adds the link to the html report to a build after the build has
 * completed. Extend Recorder instead of Notifier for Hudson compatability.
 * <p>
 * This class also holds the settings chosen by the user for the plugin.
 */
public class NeoBuildAction extends CommandInterpreter implements NeoLoadPluginOptions{

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 4651315889891892765L;

	// settings
	private final String executable;

	/**
	 * a local project or a shared/remote project.
	 */
	private final String projectType;

	/**
	 * Default report file names or custom report file names.
	 */
	private final String reportType;
	private final String localProjectFile;
	private final String sharedProjectName;
	private final String scenarioName;
	private final String htmlReport;
	private final String xmlReport;
	private final String pdfReport;
	private final String junitReport;
	private final boolean displayTheGUI;
	private final String testResultName;
	private final String testDescription;
	private final String licenseType;
	private final String licenseVUCount;
	private final String licenseDuration;
	private final String customCommandLineOptions;

	private final int maxTrends;

	private final boolean publishTestResults;
	private NTSServerInfo licenseServer;
	private ServerInfo sharedProjectServer;

	/**
	 * User option presented in the GUI. Show the average response time.
	 */
	private final boolean showTrendAverageResponse;
	/**
	 * User option presented in the GUI. Show the average response time.
	 */
	private final boolean showTrendErrorRate;

	private final List<GraphOptionsInfo> graphOptionsInfo;

	/**
	 * This executes NeoLoad. It's an instance of a jenkins object for Windows or Linux.
	 */
	private CommandInterpreter commandInterpreter = null;

	/**
	 * Log various messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(NeoBuildAction.class.getName());

	/**
	 * replace the implementation of NeoLoadPluginOptions for pipeline
	 * the implementation has been kept for compatibility purpose with the previous plugin
	 */
	private NeoLoadPluginOptions npo;

	/**
	 * This method and the annotation @DataBoundConstructor are required for jenkins 1.393 even if no params are passed in.
	 */
	@DataBoundConstructor
	public NeoBuildAction(final String executable, final String projectType, final String reportType, final String localProjectFile,
	                      final String sharedProjectName, final String scenarioName,
	                      final String htmlReport, final String xmlReport, final String pdfReport, final String junitReport,
	                      final boolean displayTheGUI, final String testResultName,
	                      final String testDescription, final String licenseType,
	                      final String licenseVUCount, final String licenseDuration, final String customCommandLineOptions,
	                      final boolean publishTestResults, final ServerInfo sharedProjectServer, final NTSServerInfo licenseServer,
	                      final boolean showTrendAverageResponse, final boolean showTrendErrorRate,
	                      final List<GraphOptionsInfo> graphOptionsInfo,
	                      final int maxTrends) {
		super(NeoBuildAction.class.getName() + " (command)");

		this.executable = executable;
		this.projectType = StringUtils.trimToEmpty(projectType);
		this.reportType = StringUtils.trimToEmpty(reportType);
		this.localProjectFile = localProjectFile;
		this.sharedProjectName = sharedProjectName;
		this.scenarioName = scenarioName;

		this.htmlReport = htmlReport;
		this.xmlReport = xmlReport;
		this.pdfReport = pdfReport;
		this.junitReport = junitReport;

		this.displayTheGUI = displayTheGUI;
		this.testResultName = testResultName;
		this.testDescription = testDescription;
		this.licenseType = StringUtils.trimToEmpty(licenseType);
		this.licenseVUCount = licenseVUCount;
		this.licenseDuration = licenseDuration;
		this.customCommandLineOptions = customCommandLineOptions;
		this.sharedProjectServer = updateUsingUniqueID(sharedProjectServer);
		this.publishTestResults = publishTestResults;
		this.licenseServer = updateUsingUniqueID(licenseServer);

		this.npo = new SimpleBuildOption(showTrendAverageResponse, showTrendErrorRate,graphOptionsInfo,maxTrends, constructXMLReportArtifactPath(), constructHTMLReportArtifactPath());

		// Because of backward compatibility we keep the following lines within the constructor
		this.showTrendAverageResponse = showTrendAverageResponse;
		this.showTrendErrorRate = showTrendErrorRate;

		this.graphOptionsInfo = graphOptionsInfo;
		this.maxTrends = maxTrends;
	}

	/**
	 * Here we search the global config for settings that have the same uniqueID. If the same uniqueID is found then we use those
	 * settings instead of our own because they are more up to date. This is because all server info is stored and duplicated here.
	 * We don't ONLY store the uniqueID because we don't want the project to break if someone deletes the global config.
	 *
	 * @param serverInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	<T extends ServerInfo> T updateUsingUniqueID(final T serverInfo) {
		if (serverInfo == null || StringUtils.trimToNull(serverInfo.getUniqueID()) == null) {
			return serverInfo;
		}

		if (Jenkins.getInstance() == null) {
			return serverInfo;
		}
		final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
				(NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

		if (globalConfigDescriptor == null) {
			// no change necessary.
			return serverInfo;
		}

		// search for the same uniqueID
		final Collection<ServerInfo> allServerInfo =
				CollectionUtils.union(globalConfigDescriptor.getNtsInfo(), globalConfigDescriptor.getCollabInfo());
		for (final ServerInfo si : allServerInfo) {
			if (si.getUniqueID().equals(serverInfo.getUniqueID())) {
				// we found the same uniqueID so we return the copy from the global config.
				return (T) si;
			}
		}

		return serverInfo;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException {
		final StringBuilder sb = prepareCommandLine(launcher);
		if (npo == null){
			build.addAction(new NeoResultsAction(build, this));
		}else{
			build.addAction(new NeoResultsAction(build,  npo));
		}

		return runTheCommand(sb.toString(), build, launcher, listener);
	}

	/**
	 * @param launcher runs code on the slave machine.
	 * @return
	 */
	protected StringBuilder prepareCommandLine(final Launcher launcher) {
		// update server settings from the main config.
		sharedProjectServer = updateUsingUniqueID(sharedProjectServer);
		licenseServer = updateUsingUniqueID(licenseServer);

		final Map<String, String> hashedPasswords = getHashedPasswords(launcher);

		// verify that the executable exists
		if (Files.isDirectory(Paths.get(executable)) || !Files.exists(Paths.get(executable))) {
			LOGGER.log(Level.WARNING, "Can't find NeoLoad executable: " + executable);
		}
		// build the command line.
		final List<String> commands = new ArrayList<>();

		// get the project
		setupProjectType(commands, hashedPasswords);

		setupTestInfo(commands, launcher);

		setupLicenseInfo(commands, hashedPasswords);

		setupReports(commands, launcher);

		if (!displayTheGUI) {
			commands.add("-noGUI");
		} else {
			commands.add("-exit");
		}

		// additional user options
		commands.add(customCommandLineOptions);

		// remove duplicate commands. this is for the -NTS argument to make sure it doesn't appear twice when checking out 
		// a project and leasing a license.
		final ArrayList<String> cleanedCommands = new ArrayList<>(new LinkedHashSet<>(commands));

		// build the command on one line.
		final StringBuilder sb = new StringBuilder();
		sb.append("\"" + executable + "\"");
		for (final String command : cleanedCommands) {
			sb.append(" " + command.replaceAll("\\r||\\n", ""));
		}
		return sb;
	}

	/**
	 * Find the password scrambler and use it to hash the passwords.
	 *
	 * @param launcher runs code on the slave machine.
	 * @return key is the plain text version, value is the hashed version.
	 */
	Map<String, String> getHashedPasswords(final Launcher launcher) {
		final HashMap<String, String> map = new HashMap<>();

		if (sharedProjectServer != null && StringUtils.trimToNull(sharedProjectServer.getLoginPassword()) != null) {
			map.put(sharedProjectServer.getLoginPassword(), "## use the password-scrambler to resolve this issue ##");
		}
		if (licenseServer != null && StringUtils.trimToNull(licenseServer.getLoginPassword()) != null) {
			map.put(licenseServer.getLoginPassword(), "## use the password-scrambler to resolve this issue ##");
		}

		// if there are no passwords or the executable doesn't exist then give up.
		if (map.size() == 0) {
			LOGGER.finest("No passwords to scramble.");
			return map;
		}

		// Special hack for JUnit (we don't have the password-scrambler embbeded with Jenkins.
		if (launcher.getClass().toString().contains("EnhancerByMockitoWithCGLIB")) {
			return map;
		}

		Map<String,String> resultMap = new HashMap<>();
		try {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				resultMap.put(entry.getKey(), PasswordEncoder.encode(entry.getKey()));
			}
		} catch (UnsupportedEncodingException | GeneralSecurityException e) {
			LOGGER.log(Level.SEVERE,"Exception during password encryption",e);
			throw new RuntimeException(e);
		}

		return resultMap;
	}

	/**
	 * Runs the password scrambler on the slave machine.
	 */
	private void setupReports(final List<String> commands, final Launcher launcher) {
		final String workspaceVariable = isOsWindows(launcher) ? "%WORKSPACE%" : "${WORKSPACE}";
		if (isRepportCustomPath()) {
			final List<String> reportPaths = PluginUtils.removeAllEmpties(htmlReport, xmlReport, pdfReport);
			final String reportFileNames = Joiner.on(",").skipNulls().join(reportPaths);
			if (StringUtils.trimToEmpty(reportFileNames).length() > 0) {
				commands.add("-report \"" + reportFileNames + "\"");
			}

			if (StringUtils.trimToEmpty(junitReport).length() > 0) {
				commands.add("-SLAJUnitResults \"" + junitReport + "\"");
			}

		} else {
			commands.add("-report \"" + workspaceVariable + "/neoload-report/report.html," + workspaceVariable + "/neoload-report/report.xml\"");
			commands.add("-SLAJUnitResults \"" + workspaceVariable + "/neoload-report/junit-sla-results.xml\"");
		}
	}

	private void setupLicenseInfo(final List<String> commands, final Map<String, String> hashedPasswords) {
		if (licenseType.toLowerCase().contains("local")) {
			// nothing to do
		} else if (licenseType.toLowerCase().contains("shared")) {
			// -NTS "http://10.0.5.11:18080" -NTSLogin "noure:QuM36humHJWA5uAvgKinWw=="
			addNTSArguments(commands, licenseServer, hashedPasswords);

			// -leaseLicense "<license id>:<virtual user count>:<duration in hours>"
			commands.add("-leaseLicense \"" + licenseServer.getLicenseID() + ":" + licenseVUCount + ":" + licenseDuration + "\"");

		} else {
			throw new RuntimeException("Unrecognized license type \"" + licenseType + "\" (expected local or shared).");
		}
	}

	private void setupTestInfo(final List<String> commands, Launcher launcher) {
		commands.add("-launch \"" + scenarioName + "\"");

		// the $Date{.*} value in testResultName must be escaped if we're on linux so that NeoLoad is passed the $.
		final String escapedTestResultName;
		if (isOsWindows(launcher)) {
			escapedTestResultName = testResultName;
		} else {
			escapedTestResultName = testResultName.replaceAll(
					Pattern.quote("$Date{") + "(.*?)" + Pattern.quote("}"),
					Matcher.quoteReplacement("\\$Date{") + "$1" + Matcher.quoteReplacement("}"));
		}
		if (StringUtils.trimToNull(escapedTestResultName) != null) {
			commands.add("-testResultName \"" + escapedTestResultName + "\"");
		}
		if (StringUtils.trimToNull(testDescription) != null) {
			commands.add("-description \"" + testDescription + "\"");
		}
	}

	private void setupProjectType(final List<String> commands, final Map<String, String> hashedPasswords) {
		if (projectType.toLowerCase().contains("local")) {
			commands.add("-project \"" + localProjectFile + "\"");

		} else if (projectType.toLowerCase().contains("shared")) {
			commands.add("-checkoutProject \"" + sharedProjectName + "\"");
			if (sharedProjectServer instanceof NTSServerInfo) {
				// -NTS "http://10.0.5.11:18080" -NTSLogin "noure:QuM36humHJWA5uAvgKinWw=="
				addNTSArguments(commands, (NTSServerInfo) sharedProjectServer, hashedPasswords);
				commands.add("-NTSCollabPath \"" + ((NTSServerInfo) sharedProjectServer).getCollabPath() + "\"");

			} else if (sharedProjectServer instanceof CollabServerInfo) {
				final CollabServerInfo csi = (CollabServerInfo) sharedProjectServer;
				commands.add("-Collab \"" + csi.getUrl() + "\"");

				final StringBuilder sb = setupCollabLogin(hashedPasswords, csi);

				commands.add(sb.toString());

			} else {
				throw new RuntimeException("Unrecognized ServerInfo type: " + sharedProjectServer.getClass().getName());
			}
		} else {
			throw new RuntimeException("Unrecognized project type \"" + projectType + "\" (expected local or shared).");
		}

		if (publishTestResults) {
			commands.add("-publishTestResult");
		}
	}

	StringBuilder setupCollabLogin(final Map<String, String> hashedPasswords, final CollabServerInfo csi) {
		final StringBuilder sb = new StringBuilder();
		// -CollabLogin "<login>:<hashed password>", or
		// -CollabLogin "<login>:<private key>:<hashed passphrase>", or
		// -CollabLogin "<login>:<hashed password>:<private key>:<hashed passphrase>"
		if (StringUtils.trimToNull(csi.getLoginUser()) != null) {
			sb.append(csi.getLoginUser());
		}
		if (StringUtils.trimToNull(hashedPasswords.get(csi.getLoginPassword())) != null) {
			if (sb.length() > 0) {
				sb.append(":");
			}
			sb.append(hashedPasswords.get(csi.getLoginPassword()));
		}
		if (StringUtils.trimToNull(csi.getPrivateKey()) != null) {
			if (sb.length() > 0) {
				sb.append(":");
			}
			sb.append(csi.getPrivateKey());
		}
		if (StringUtils.trimToNull(csi.getPassphrase()) != null) {
			if (sb.length() > 0) {
				sb.append(":");
			}
			sb.append(csi.getPassphrase());
		}
		sb.insert(0, "-CollabLogin ");

		return sb;
	}

	private boolean runTheCommand(final String command, final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
			throws InterruptedException {

		if (isOsWindows(launcher)) {
			commandInterpreter = new BatchFile(command);
		} else {
			commandInterpreter = new Shell(command);
		}

		LOGGER.log(Level.FINEST, "Executing command: " + command);

		return commandInterpreter.perform(build, launcher, listener);
	}

	public static boolean isOsWindows(final Launcher launcher) {
		return !launcher.isUnix();
	}

	private static void addNTSArguments(final List<String> commands, final NTSServerInfo n, final Map<String, String> hashedPasswords) {
		commands.add("-NTS \"" + n.getUrl() + "\"");
		commands.add("-NTSLogin \"" + n.getLoginUser() + ":" +
				hashedPasswords.get(n.getLoginPassword()) + "\"");
	}

	public String isProjectType(final String type) {
		if (StringUtils.trimToNull(projectType) == null) {
			return "projectTypeLocal".equalsIgnoreCase(type) == true ? "true" : "false";
		}

		return projectType.equalsIgnoreCase(type) ? "true" : "false";
	}

	public boolean isReportType(final String type) {
		if (StringUtils.trimToNull(reportType) == null) {
			return "reportTypeDefault".equalsIgnoreCase(type);
		}

		return reportType.equalsIgnoreCase(type);
	}

	public boolean isRepportCustomPath() {
		return isReportType("reportTypeCustom");
	}

	public String isLicenseType(final String type) {
		if (StringUtils.trimToNull(licenseType) == null) {
			return "licenseTypeLocal".equalsIgnoreCase(type) == true ? "true" : "false";
		}

		return licenseType.equalsIgnoreCase(type) ? "true" : "false";
	}

	public String getXMLReportArtifactPath(){
		if (npo != null){
			return npo.getXMLReportArtifactPath();
		}
		return constructXMLReportArtifactPath();
	}

	public String constructXMLReportArtifactPath() {
		if (isRepportCustomPath()) {
			return PluginUtils.removeWorkspaceOrRelativePoint(xmlReport);
		}
		return "/neoload-report/report.xml";
	}

	public String getHTMLReportArtifactPath(){
		if (npo != null){
			return npo.getHTMLReportArtifactPath();
		}
		return constructHTMLReportArtifactPath();
	}

	public String constructHTMLReportArtifactPath() {
		if (isRepportCustomPath()) {
			return PluginUtils.removeWorkspaceOrRelativePoint(htmlReport);
		}
		return "/neoload-report/report.html";
	}

	@Override
	public Descriptor<Builder> getDescriptor() {
		final DescriptorImpl descriptor = (DescriptorImpl) super.getDescriptor();

		// setting this as an instance allows us to re-select the currently selected dropdown options.
		descriptor.setNeoBuildAction(this);

		return descriptor;
	}

	@Extension(optional = true)
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		private NeoBuildAction neoBuildAction = null;

		public DescriptorImpl() {
			super(NeoBuildAction.class);
			load();
		}

		public void setNeoBuildAction(final NeoBuildAction action) {
			this.neoBuildAction = action;
		}

		@Override
		public String getDisplayName() {
			return "Execute a NeoLoad Scenario";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
			save();
			return super.configure(req, json);
		}

		public ListBoxModel doFillSharedProjectServerItems(@AncestorInPath final Item project) {
			ServerInfo preselected = null;
			if (neoBuildAction != null && neoBuildAction.sharedProjectServer != null) {
				preselected = neoBuildAction.sharedProjectServer;
			}

			return getProjectServerOptions(preselected);
		}


		public ListBoxModel doFillLicenseServerItems(@AncestorInPath final Item project) {
			ServerInfo preselected = null;
			if (neoBuildAction != null && neoBuildAction.licenseServer != null) {
				preselected = neoBuildAction.licenseServer;
			}

			return getLicenseServerOptions(preselected);
		}

		/**
		 * @param preselected
		 * @return the servers for sharing projects
		 */
		public static ListBoxModel getLicenseServerOptions(final ServerInfo preselected) {
			final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
					(NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

			final ListBoxModel listBoxModel = new ListBoxModel();

			if (globalConfigDescriptor == null) {
				LOGGER.log(Level.FINEST, "No NeoLoad server settings found. Please add servers before configuring jobs. (getLicenseServerOptions)");

			} else {
				for (final NTSServerInfo server : globalConfigDescriptor.getNtsInfo()) {
					final String displayName = buildNTSDisplayNameString(server, false);
					final String optionValue = server.getUniqueID();
					final Option option = new Option(displayName, optionValue);

					if (server.equals(preselected)) {
						option.selected = true;
					}

					listBoxModel.add(option);
				}
			}

			if (listBoxModel.isEmpty()) {
				LOGGER.finest("There is no NTS Server configured !");
				listBoxModel.add(new Option("Please configure Jenkins System Settings for NeoLoad to add an NTS server.",
						null));
			}
			return listBoxModel;
		}

		/**
		 * @param preselected
		 * @return the servers for sharing projects
		 */
		private ListBoxModel getProjectServerOptions(final ServerInfo preselected) {
			final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
					(org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

			final ListBoxModel listBoxModel = new ListBoxModel();

			if (globalConfigDescriptor == null) {
				LOGGER.log(Level.FINEST, "No NeoLoad server settings found. Please add servers before configuring jobs. (getProjectServerOptions)");

			} else {
				for (final NTSServerInfo server : globalConfigDescriptor.getNtsInfo()) {
					final String displayName = buildNTSDisplayNameString(server, true);
					final String optionValue = server.getUniqueID();
					final Option option = new Option(displayName, optionValue);

					if (server.equals(preselected)) {
						option.selected = true;
					}

					listBoxModel.add(option);
				}
			}

			for (final CollabServerInfo server : globalConfigDescriptor.getCollabInfo()) {
				final String displayName;
				if (StringUtils.trimToEmpty(server.getLabel()).length() > 0) {
					displayName = server.getLabel();
				} else {
					displayName = server.getUrl() + ", User: " + server.getLoginUser();
				}
				final String optionValue = server.getUniqueID();
				final Option option = new Option(displayName, optionValue);

				if (server.equals(preselected)) {
					option.selected = true;
				}

				listBoxModel.add(option);
			}

			if (listBoxModel.isEmpty()) {
				LOGGER.finest("There is no Server configured !");
				listBoxModel.add(new Option("Please configure Jenkins System Settings for NeoLoad to add a server.",
						null));
			}
			return listBoxModel;
		}

		public static String buildNTSDisplayNameString(final NTSServerInfo server, final boolean isSharedProjectDisplay) {
			if (StringUtils.trimToEmpty(server.getLabel()).length() > 0) {
				return server.getLabel();
			}
			final StringBuilder displayName = new StringBuilder(server.getUrl());
			if (isSharedProjectDisplay) {
				displayName.append(", Repository: " + server.getCollabPath());
			} else {
				if (StringUtils.trimToNull(server.getLicenseID()) != null) {
					displayName.append(", LicenseID: " + StringUtils.left(server.getLicenseID(), 4) + "..." +
							StringUtils.right(server.getLicenseID(), 4));
					displayName.append(" (NTS)");
				}
			}
			return displayName.toString();
		}

		@Override
		public boolean isApplicable(
				@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
			return true;
		}

		public FormValidation doCheckLocalProjectFile(@QueryParameter("localProjectFile") final String localProjectFile) {
			return PluginUtils.validateFileExists(localProjectFile, ".nlp", true, false);
		}

		public FormValidation doCheckExecutable(@QueryParameter final String executable) {
			return PluginUtils.validateFileExists(executable, ".exe", false, true);
		}

		public FormValidation doCheckLicenseVUCount(@QueryParameter final String licenseVUCount) {
			return PluginUtils.formValidationErrorToWarning(FormValidation.validatePositiveInteger(licenseVUCount));
		}

		public FormValidation doCheckLicenseDuration(@QueryParameter final String licenseDuration) {
			return PluginUtils.formValidationErrorToWarning(FormValidation.validatePositiveInteger(licenseDuration));
		}

		public FormValidation doCheckLicenseID(@QueryParameter final String licenseID) {
			return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(licenseID));
		}

		public FormValidation doCheckSharedProjectName(@QueryParameter final String sharedProjectName) {
			return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(sharedProjectName));
		}

		public FormValidation doCheckXmlReport(@QueryParameter final String xmlReport) {
			return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(xmlReport));
		}

		public FormValidation doCheckScenarioName(@QueryParameter final String scenarioName) {
			return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(scenarioName));
		}

		public FormValidation doCheckDisplayTheGUI(@QueryParameter final String displayTheGUI) {
			if (Boolean.valueOf(displayTheGUI)) {
				return FormValidation.warning("The user launching the process must be able to display a user interface "
						+ "(which is not always the case for the Jenkins user). Some errors or warning messages may prevent NeoLoad "
						+ "from closing automatically at the end of a test run. Thus this should only be used for testing purposes.");
			}

			return FormValidation.ok();
		}
	}

	@Override
	public String[] buildCommandLine(final FilePath script) {
		return commandInterpreter.buildCommandLine(script);
	}

	@Override
	protected String getContents() {
		if (SystemUtils.IS_OS_WINDOWS) {
			new BatchFileMine().getContents();
		}

		return new ShellMine().getContents();
	}

	@Override
	protected String getFileExtension() {
		if (SystemUtils.IS_OS_WINDOWS) {
			new BatchFileMine().getFileExtension();
		}

		return new ShellMine().getFileExtension();
	}

	public class BatchFileMine extends BatchFile {
		public BatchFileMine() {
			super("command");
		}

		@Override
		public String getContents() {
			return super.getContents();
		}

		@Override
		public String getFileExtension() {
			return super.getFileExtension();
		}
	}

	public class ShellMine extends Shell {
		public ShellMine() {
			super("command");
		}

		@Override
		public String getContents() {
			return super.getContents();
		}

		@Override
		public String getFileExtension() {
			return super.getFileExtension();
		}
	}

	public String getExecutable() {
		return executable;
	}

	public String getSharedProjectName() {
		return sharedProjectName;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public String getHtmlReport() {
		return htmlReport;
	}

	public String getXmlReport() {
		return xmlReport;
	}

	public String getPdfReport() {
		return pdfReport;
	}

	public String getJunitReport() {
		return junitReport;
	}

	public boolean isDisplayTheGUI() {
		return displayTheGUI;
	}

	public String getTestResultName() {
		return testResultName;
	}

	public String getTestDescription() {
		return testDescription;
	}

	public String getLicenseType() {
		return licenseType;
	}

	public String getLicenseVUCount() {
		return licenseVUCount;
	}

	public String getLicenseDuration() {
		return licenseDuration;
	}

	public String getCustomCommandLineOptions() {
		return customCommandLineOptions;
	}

	public String getLocalProjectFile() {
		return localProjectFile;
	}

	public String getProjectType() {
		return projectType;
	}

	public String getReportType() {
		return reportType;
	}

	public boolean getPublishTestResults() {
		return publishTestResults;
	}

	public ServerInfo getLicenseServer() {
		return licenseServer;
	}

	public void setLicenseServer(final NTSServerInfo licenseServer) {
		this.licenseServer = licenseServer;
	}

	public ServerInfo getSharedProjectServer() {
		return sharedProjectServer;
	}

	public void setSharedProjectServer(final ServerInfo sharedProjectServer) {
		this.sharedProjectServer = sharedProjectServer;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return the showTrendAverageResponse
	 */
	public boolean isShowTrendAverageResponse() {
		if(npo != null) {
			return this.npo.isShowTrendAverageResponse();
		}
		return showTrendAverageResponse;
	}

	/**
	 * @return the showTrendErrorRate
	 */
	public boolean isShowTrendErrorRate() {
		if(npo != null) {
			return this.npo.isShowTrendErrorRate();
		}
		return false;
	}

	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		if(npo != null) {
			return this.npo.getGraphOptionsInfo();
		}
		return null;
	}

	public int getMaxTrends() {
		if(npo != null) {
			return this.npo.getMaxTrends();
		}
		return 0;
	}

	public boolean perform(Run<?,?> build, FilePath ws, Launcher launcher, TaskListener listener) throws InterruptedException {
		final StringBuilder sb = prepareCommandLine(launcher);
		build.addAction(new NeoResultsAction(build, npo));

		boolean returnedValue = (new NeoloadRunLauncher(sb.toString(), launcher)).perform(build, ws, launcher, listener);

		ArtifactArchiver archiver = new ArtifactArchiver("neoload-report/**");
		archiver.perform(build, ws,launcher, listener);

		try {
			NeoPostBuildTask neoPostTask = new NeoPostBuildTask();
			neoPostTask.setNpo(npo);
			neoPostTask.perform(build, ws, launcher, listener);
			build.addAction(new ProjectSpecificAction(build));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error while creating graphs: " + e);
			e.printStackTrace();
		}
		return returnedValue;
	}
}
