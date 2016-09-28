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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jenkinsci.plugins.neoload.integration.supporting.CollabServerInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.GraphOptionsInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.NTSServerInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.NeoLoadPluginOptions;
import org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.ServerInfo;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.remoting.Callable;
import hudson.tasks.BatchFile;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

/**
 * This class adds the link to the html report to a build after the build has
 * completed. Extend Recorder instead of Notifier for Hudson compatability.
 * 
 * This class also holds the settings chosen by the user for the plugin.
 */
public class NeoBuildAction extends CommandInterpreter implements NeoLoadPluginOptions, Serializable {

	/** Generated. */
	private static final long serialVersionUID = 4651315889891892765L;

	// settings
	private final String executable;
	
	/** a local project or a shared/remote project. */
	private final String projectType;
	
	/** Default report file names or custom report file names. */
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

	private final boolean publishTestResults;
	private NTSServerInfo licenseServer;
	private ServerInfo sharedProjectServer;

	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendAverageResponse;
	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendErrorRate;
	
 	private final List<GraphOptionsInfo> graphOptionsInfo;
	
	/** This executes NeoLoad. It's an instance of a jenkins object for Windows or Linux. */
	private CommandInterpreter commandInterpreter = null;

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(NeoBuildAction.class.getName());

	/** This method and the annotation @DataBoundConstructor are required for jenkins 1.393 even if no params are passed in. */
	@DataBoundConstructor
	public NeoBuildAction(final String executable, final String projectType, final String reportType, final String localProjectFile, 
			final String sharedProjectName, final String scenarioName,
			final String htmlReport, final String xmlReport, final String pdfReport, final String junitReport, 
			final boolean displayTheGUI, final String testResultName,
			final String testDescription, final String licenseType,  
			final String licenseVUCount, final String licenseDuration, final String customCommandLineOptions, 
			final boolean publishTestResults, final ServerInfo sharedProjectServer, final NTSServerInfo licenseServer,
			final boolean showTrendAverageResponse, final boolean showTrendErrorRate, 
			final List<GraphOptionsInfo> graphOptionsInfo) {
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

		this.showTrendAverageResponse = showTrendAverageResponse;
		this.showTrendErrorRate = showTrendErrorRate;

		this.graphOptionsInfo = graphOptionsInfo;
	}

	/** Here we search the global config for settings that have the same uniqueID. If the same uniqueID is found then we use those
	 * settings instead of our own because they are more up to date. This is because all server info is stored and duplicated here.
	 * We don't ONLY store the uniqueID because we don't want the project to break if someone deletes the global config.
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
		for (final ServerInfo si: allServerInfo) {
			if (si.getUniqueID().equals(serverInfo.getUniqueID())) {
				// we found the same uniqueID so we return the copy from the global config.
				return (T)si;
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
		for (final String command: cleanedCommands) {
			sb.append(" " + command.replaceAll("\\r||\\n", ""));
		}
		return sb;
	}

	/** Find the password scrambler and use it to hash the passwords.
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
		if(launcher.getClass().toString().contains("EnhancerByMockitoWithCGLIB")){
			return map;
		}	

		// this executes on the slave, not on the master.
		final CallableForPasswordScrambler callableForPasswordScrambler = new CallableForPasswordScrambler(map, executable, isOsWindows(launcher));
 		Map<String, String> newMap;
		try {
			newMap = launcher.getChannel().call(callableForPasswordScrambler);
			map.putAll(newMap);
		} catch (final Exception e) {
			String errorMessage = "Issue executing password scrambler. ";
			if(e.getMessage() != null){
				errorMessage += e.getMessage();
			} else {
				errorMessage += e.toString();
			}			
			LOGGER.severe(errorMessage);
			throw new RuntimeException(errorMessage);
		}

		return map;
	}
	
	/** Runs the password scrambler on the slave machine. */
	static class CallableForPasswordScrambler implements Callable<Map<String, String>, Exception>, Serializable {

		/** Generated. */
		private static final long serialVersionUID = 4462660760602753013L;

		final Map<String, String> map;
		final String executable;
		final boolean osWindows;
		
		public CallableForPasswordScrambler(final HashMap<String, String> map, final String executable, boolean osWindows) {
			this.map = map;
			this.executable = executable;
			this.osWindows = osWindows;
		}
		
		@Override
		public Map<String, String> call() throws Exception {
			LOGGER.finest("Start password scrambler execution...");
			// look for the password scrambler. it should be next to the executable or one directory higher.
			final Path possibleFile = findThePasswordScrambler();
			if (Files.exists(possibleFile)) {
				LOGGER.finest("Path is: " + possibleFile.toString());
			} else {
				final String errorMessage = "Password scrambler not found : \"" + possibleFile + "\"";
				LOGGER.severe(errorMessage);
				throw new RuntimeException(errorMessage);				
			}

			for (final String plainPassword: map.keySet()) {
				// prepare the line to execute.
				final String line = "\"" + possibleFile.toString() + "\" -a \"" + plainPassword + "\"";
				
				// execute it and get the result.
				final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
				final CommandLine cmdLine = CommandLine.parse(line);
				final DefaultExecutor executor = new DefaultExecutor();
				final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				final PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
				executor.setStreamHandler(streamHandler);
				executor.setWorkingDirectory(possibleFile.getParent().toFile());
				executor.execute(cmdLine, resultHandler);
				resultHandler.waitFor(60000);
				final String result = outputStream.toString();
				if(resultHandler.getException() != null){
					LOGGER.severe("Error while executing password-scrambler: " + resultHandler.getException().getMessage());
					throw new RuntimeException(resultHandler.getException());
				}
				if(Strings.isNullOrEmpty(result)){
					final String errorMessage = "Error while executing password-scrambler: no result.";
					LOGGER.severe(errorMessage);
					throw new RuntimeException(errorMessage);
				}
				// parse the result.
				// example: AES128 ciphering result of nluser: 6RGXo/iJAai0tGuxtAih2Q== \n Copyright (c) 2016 Neotys, PasswordScrambler v-
				final String firstPart = result.substring(result.indexOf(plainPassword + ":"));
				final String secondPart = firstPart.substring(firstPart.indexOf(":") + 2);
				final String[] split = secondPart.split("\r|\n");
				final String hashedPassword = split[0];
				LOGGER.finest("hashedPassword : " + hashedPassword);
				
				map.put(plainPassword, hashedPassword);
			}
			LOGGER.finest(map.size() + " passwords has been hashed");
			return map;
		}

		private Path findThePasswordScrambler() {
			Path parent = Paths.get(executable).getParent();
			Path possibleFile = null;
			for (int i = 0; i < 2; i++) {
				if (parent == null) {
					break;
				}
				
				if (this.osWindows) {
					possibleFile = parent.resolve("password-scrambler.bat");
					if (Files.exists(possibleFile)) {
						break;
					} else {
						possibleFile = parent.resolve("password-scrambler.exe");
					}
				} else {
					possibleFile = parent.resolve("password-scrambler");
					if (Files.exists(possibleFile)) {
						break;
					} else {
						possibleFile = parent.resolve("password-scrambler.sh");
					}
				}

				if (Files.exists(possibleFile)) {
					LOGGER.log(Level.FINEST, "Found password-scrambler path at: " + possibleFile);
					break;
				}
				parent = parent.getParent();
			}
			return possibleFile;
		}
	}

	private void setupReports(final List<String> commands, final Launcher launcher) {
		final String workspaceVariable = isOsWindows(launcher) ? "%WORKSPACE%" : "${WORKSPACE}";
		if (Boolean.valueOf(isReportType("reportTypeCustom"))) {
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
	
	private static boolean isOsWindows(final Launcher launcher) {
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

	public String isReportType(final String type) {
		if (StringUtils.trimToNull(reportType) == null) {
			return "reportTypeDefault".equalsIgnoreCase(type) == true ? "true" : "false";
		}

		return reportType.equalsIgnoreCase(type) ? "true" : "false";
	}

	public String isLicenseType(final String type) {
		if (StringUtils.trimToNull(licenseType) == null) {
			return "licenseTypeLocal".equalsIgnoreCase(type) == true ? "true" : "false";
		}

		return licenseType.equalsIgnoreCase(type) ? "true" : "false";
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

		/** @param preselected 
		 * @return the servers for sharing projects */
		private ListBoxModel getLicenseServerOptions(final ServerInfo preselected) {
			final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor = 
					(NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

			final ListBoxModel listBoxModel = new ListBoxModel();

			if (globalConfigDescriptor == null) {
				LOGGER.log(Level.FINEST, "No NeoLoad server settings found. Please add servers before configuring jobs. (getLicenseServerOptions)");
				
			} else {
				for (final NTSServerInfo server: globalConfigDescriptor.getNtsInfo()) {
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
		/** @param preselected 
		 * @return the servers for sharing projects */
		private ListBoxModel getProjectServerOptions(final ServerInfo preselected) {
			final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor = 
					(org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

			final ListBoxModel listBoxModel = new ListBoxModel();

			if (globalConfigDescriptor == null) {
				LOGGER.log(Level.FINEST, "No NeoLoad server settings found. Please add servers before configuring jobs. (getProjectServerOptions)");
				
			} else {
				for (final NTSServerInfo server: globalConfigDescriptor.getNtsInfo()) {
					final String displayName = buildNTSDisplayNameString(server, true);
					final String optionValue = server.getUniqueID();
					final Option option = new Option(displayName, optionValue);
	
					if (server.equals(preselected)) {
						option.selected = true;
					}
	
					listBoxModel.add(option);
				}
			}
			
			for (final CollabServerInfo server: globalConfigDescriptor.getCollabInfo()) {
				final String displayName;
				if (StringUtils.trimToEmpty(server.getLabel()).length() > 0) {
					displayName = server.getLabel();
				}
				else {
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

		private String buildNTSDisplayNameString(final NTSServerInfo server, final boolean isSharedProjectDisplay) {
			if (StringUtils.trimToEmpty(server.getLabel()).length() > 0) {
				return server.getLabel();
			}
			final StringBuilder displayName = new StringBuilder(server.getUrl());
			if (isSharedProjectDisplay) {
				displayName.append(", Repository: " + server.getCollabPath());
			}
			else {
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

	private class BatchFileMine extends BatchFile {
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
	private class ShellMine extends Shell {
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

	/** @return the showTrendAverageResponse */
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}
	/** @return the showTrendErrorRate */
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}

	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		return graphOptionsInfo;
	}
}
