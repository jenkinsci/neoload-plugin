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

import com.google.common.base.Joiner;
import com.neotys.nls.security.tools.PasswordEncoder;
import hudson.*;
import hudson.model.*;
import hudson.tasks.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jenkinsci.plugins.neoload.integration.steps.NeoloadRunStep;
import org.jenkinsci.plugins.neoload.integration.supporting.*;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class adds the link to the html report to a build after the build has
 * completed. Extend Recorder instead of Notifier for Hudson compatability.
 * <p>
 * This class also holds the settings chosen by the user for the plugin.
 */
public class NeoBuildAction extends Builder implements SimpleBuildStep, NeoLoadPluginOptions {

    /**
     * Generated.
     */
    private static final long serialVersionUID = 4651315889891892765L;
    /**
     * Log various messages.
     */
    private static final Logger LOGGER = Logger.getLogger(NeoBuildAction.class.getName());
    public static final String PROJECT_LOCAL = "local";
    public static final String PROJECT_SHARED = "shared";
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
    private final String licenseVUSAPCount;
    private final String licenseDuration;
    private final String customCommandLineOptions;
    private final int maxTrends;
    private final boolean publishTestResults;
    /**
     * User option presented in the GUI. Show the average response time.
     */
    private final boolean showTrendAverageResponse;
    /**
     * User option presented in the GUI. Show the average response time.
     */
    private final boolean showTrendErrorRate;
    private final boolean scanAllBuilds;
    private final List<GraphOptionsInfo> graphOptionsInfo;
    private NTSServerInfo licenseServer;
    private ServerInfo sharedProjectServer;
    /**
     * This executes NeoLoad. It's an instance of a jenkins object for Windows or Linux.
     */
    private CommandInterpreter commandInterpreter = null;
    private boolean archiveAndBuildTrends;


    /**
     * This method and the annotation @DataBoundConstructor are required for jenkins 1.393 even if no params are passed in.
     *
     * @param executable               the executable
     * @param projectType              the project type
     * @param reportType               the report type
     * @param localProjectFile         the local project file
     * @param sharedProjectName        the shared project name
     * @param scenarioName             the scenario name
     * @param htmlReport               the html report
     * @param xmlReport                the xml report
     * @param pdfReport                the pdf report
     * @param junitReport              the junit report
     * @param scanAllBuilds            the scan all builds
     * @param displayTheGUI            the display the gui
     * @param testResultName           the test result name
     * @param testDescription          the test description
     * @param licenseType              the license type
     * @param licenseVUCount           the license vu count
     * @param licenseDuration          the license duration
     * @param customCommandLineOptions the custom command line options
     * @param publishTestResults       the publish test results
     * @param sharedProjectServer      the shared project server
     * @param licenseServer            the license server
     * @param showTrendAverageResponse the show trend average response
     * @param showTrendErrorRate       the show trend error rate
     * @param graphOptionsInfo         the graph options info
     * @param maxTrends                the max trends
     */
    @DataBoundConstructor
    public NeoBuildAction(final String executable,
                          final String projectType,
                          final String reportType,
                          final String localProjectFile,
                          final String sharedProjectName,
                          final String scenarioName,
                          final String htmlReport,
                          final String xmlReport,
                          final String pdfReport,
                          final String junitReport,
                          final boolean scanAllBuilds,
                          final boolean displayTheGUI,
                          final String testResultName,
                          final String testDescription,
                          final String licenseType,
                          final String licenseVUCount,
                          final String licenseVUSAPCount,
                          final String licenseDuration,
                          final String customCommandLineOptions,
                          final boolean publishTestResults,
                          final ServerInfo sharedProjectServer,
                          final NTSServerInfo licenseServer,
                          final boolean showTrendAverageResponse,
                          final boolean showTrendErrorRate,
                          final List<GraphOptionsInfo> graphOptionsInfo,
                          final int maxTrends) {


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
        this.licenseVUSAPCount = licenseVUSAPCount;
        this.licenseDuration = licenseDuration;
        this.customCommandLineOptions = customCommandLineOptions;
        this.sharedProjectServer = updateUsingUniqueID(sharedProjectServer);
        this.publishTestResults = publishTestResults;
        this.licenseServer = updateUsingUniqueID(licenseServer);


        // Because of backward compatibility we keep the following lines within the constructor
        this.showTrendAverageResponse = showTrendAverageResponse;
        this.showTrendErrorRate = showTrendErrorRate;

        this.graphOptionsInfo = graphOptionsInfo;
        this.maxTrends = maxTrends;
        this.scanAllBuilds = scanAllBuilds;
    }


    public NeoBuildAction(NeoloadRunStep step) {
        this(
                step.getExecutable(),
                step.getProjectType(),
                step.getReportType(),
                step.getLocalProjectFile(),
                step.getSharedProjectName(),
                step.getScenarioName(),
                step.getHtmlReport(),
                step.getXmlReport(),
                step.getPdfReport(),
                step.getJunitReport(),
                false,
                step.isDisplayGUI(),
                step.getTestResultName(),
                step.getTestDescription(),
                step.getLicenseType(),
                step.getLicenseVUCount(),
                step.getLicenseVUSAPCount(),
                step.getLicenseDuration(),
                step.getCustomCommandLineOptions(),
                step.isPublishTestResults(),
                step.getSharedProjectServer(),
                step.getLicenseServer(),
                step.isShowTrendAverageResponse(),
                step.isShowTrendErrorRate(),
                step.getGraphOptionsInfo(),
                step.getMaxTrends()
        );
        this.setArchiveAndBuildTrends(step.isAutoArchive());
    }

    /**
     * Is os windows boolean.
     *
     * @param launcher the launcher
     * @return the boolean
     */
    public static boolean isOsWindows(final Launcher launcher) {
        return !launcher.isUnix();
    }

    private static void addNTSArguments(final List<String> commands, final NTSServerInfo n, final Map<Secret, String> hashedPasswords) {
        commands.add("-NTS \"" + n.getUrl() + "\"");
        commands.add("-NTSLogin \"" + n.getLoginUser() + ":" +
                hashedPasswords.get(n.getLoginPassword()) + "\"");
    }


    /**
     * Here we search the global config for settings that have the same uniqueID. If the same uniqueID is found then we use those
     * settings instead of our own because they are more up to date. This is because all server info is stored and duplicated here.
     * We don't ONLY store the uniqueID because we don't want the project to break if someone deletes the global config.
     *
     * @param <T>        the type parameter
     * @param serverInfo the server info
     * @return t t
     */
    @SuppressWarnings("unchecked")
    <T extends ServerInfo> T updateUsingUniqueID(final T serverInfo) {
        if (serverInfo == null || StringUtils.trimToNull(serverInfo.getUniqueID()) == null) {
            return serverInfo;
        }

        if (Jenkins.getInstance() == null) {
            return serverInfo;
        }
        final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor = PluginUtils.getNeoGlobalConfig();

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

    /**
     * Gets required monitor service.
     *
     * @return the required monitor service
     */
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Perform boolean.
     *
     * @param build    the build
     * @param launcher the launcher
     * @param listener the listener
     * @return the boolean
     * @throws InterruptedException the interrupted exception
     */
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException {

        if (arePasswordsSafe()) {

            final StringBuilder sb = prepareCommandLine(launcher, null, false);
            build.addAction(new NeoResultsAction(build, getXMLReportArtifactPath(), getHTMLReportArtifactPath()));
            return runTheCommand(sb.toString(), build, launcher, listener);

        } else {
            listener.getLogger().println("\nWARNING: a non-ciphered password has been detected. Please follow the procedure below:" );
            listener.getLogger().println("   1. Retype and save this password in the Configure System section of Jenkins Manager.");
            listener.getLogger().println("   2. Go to the configuration of this job and save it again.");
            listener.getLogger().println();
        }

        return false;
    }

    /**
     * Prepare command line string builder.
     *
     * @param launcher         runs code on the slave machine.
     * @param currentWorkspace the current workspace
     * @return string builder
     */
    protected StringBuilder prepareCommandLine(final Launcher launcher, final FilePath currentWorkspace, final boolean withoutShell) {
        // update server settings from the main config.
        sharedProjectServer = updateUsingUniqueID(sharedProjectServer);
        licenseServer = updateUsingUniqueID(licenseServer);

        final Map<Secret, String> hashedPasswords = getHashedPasswords();

        // verify that the executable exists
        final String executable = getExecutable();
        if (Files.isDirectory(Paths.get(executable)) || !Files.exists(Paths.get(executable))) {
            LOGGER.log(Level.WARNING, "Can't find NeoLoad executable: " + executable);
        }
        // build the command line.
        final List<String> commands = new ArrayList<>();

        // get the project
        setupProjectType(commands, hashedPasswords);

        setupTestInfo(commands, launcher, withoutShell);

        setupLicenseInfo(commands, hashedPasswords);

        setupReports(commands, launcher, currentWorkspace);

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
     * @return key is the plain text version, value is the hashed version.
     */
    Map<Secret, String> getHashedPasswords() {
        final HashMap<Secret, String> map = getPasswordsMap();

        // if there are no passwords or the executable doesn't exist then give up.
        if (map.size() == 0) {
            LOGGER.finest("No passwords to scramble.");
            return map;
        }

        final Map<Secret, String> resultMap = new HashMap<>();
        try {
            for (Map.Entry<Secret, String> entry : map.entrySet()) {
                resultMap.put(entry.getKey(), PasswordEncoder.encode(entry.getKey().getPlainText()));
            }
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, "Exception during password encryption", e);
            throw new RuntimeException(e);
        }

        return resultMap;
    }

    private HashMap<Secret, String> getPasswordsMap() {
        final HashMap<Secret, String> map = new HashMap<>();

        if (sharedProjectServer != null && StringUtils.trimToNull(sharedProjectServer.getLoginPassword().getPlainText()) != null) {
            map.put(sharedProjectServer.getLoginPassword(), "## use the password-scrambler to resolve this issue ##");
        }
        if (licenseServer != null && StringUtils.trimToNull(licenseServer.getLoginPassword().getPlainText()) != null) {
            map.put(licenseServer.getLoginPassword(), "## use the password-scrambler to resolve this issue ##");
        }

        return map;
    }

    public boolean arePasswordsSafe() {
        final HashMap<Secret, String> map = getPasswordsMap();

        // https://neotys.atlassian.net/browse/LOAD-19039
        // test if this is an unsafe password
        // if true, emit exception to stop the build
        for (Map.Entry<Secret, String> entry : map.entrySet()) {
            if (entry.getKey().getPlainText().startsWith("=?UTF-8")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Runs the password scrambler on the slave machine.
     */
    private void setupReports(final List<String> commands, final Launcher launcher, final FilePath currentWorkspace) {
        String workspaceVariable;
        if (currentWorkspace == null) {
            workspaceVariable = isOsWindows(launcher) ? "%WORKSPACE%" : "${WORKSPACE}";
        } else {
            //Default value for workflows
            workspaceVariable = currentWorkspace.getRemote();
        }
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

    private String computeVuCount() {
        if (PluginUtils.isSAP(licenseVUSAPCount)) {
            return licenseVUCount + ";" + licenseVUSAPCount;
        } else {
            return licenseVUCount;
        }
    }

    public String getLicenseVUSAPCount() {
        return licenseVUSAPCount;
    }

    private void setupLicenseInfo(final List<String> commands, final Map<Secret, String> hashedPasswords) {
        if (licenseType.toLowerCase().contains(PROJECT_LOCAL)) {
            // nothing to do
        } else if (licenseType.toLowerCase().contains(PROJECT_SHARED)) {
            // -NTS "http://10.0.5.11:18080" -NTSLogin "noure:QuM36humHJWA5uAvgKinWw=="
            addNTSArguments(commands, licenseServer, hashedPasswords);

            // -leaseLicense "<license id>:<virtual user count>:<duration in hours>"
            commands.add("-leaseLicense \"" + licenseServer.getLicenseID() + ":" + computeVuCount() + ":" + licenseDuration + "\"");

        } else {
            throw new RuntimeException("Unrecognized license type \"" + licenseType + "\" (expected local or shared).");
        }
    }

    private void setupTestInfo(final List<String> commands, Launcher launcher, boolean withoutShell) {
        commands.add("-launch \"" + scenarioName + "\"");

        // the $Date{.*} value in testResultName must be escaped if we're on linux so that NeoLoad is passed the $.
        final String escapedTestResultName;
        if (isOsWindows(launcher) || withoutShell) {
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

    private void setupProjectType(final List<String> commands, final Map<Secret, String> hashedPasswords) {
        if (projectType.toLowerCase().contains(PROJECT_LOCAL)) {
            commands.add("-project \"" + localProjectFile + "\"");

        } else if (projectType.toLowerCase().contains(PROJECT_SHARED)) {
            commands.add("-checkoutProject \"" + sharedProjectName + "\"");
            if (sharedProjectServer instanceof NTSServerInfo) {
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

    /**
     * Sets collab login.
     *
     * @param hashedPasswords the hashed passwords
     * @param csi             the csi
     * @return the collab login
     */
    StringBuilder setupCollabLogin(final Map<Secret, String> hashedPasswords, final CollabServerInfo csi) {
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

    /**
     * Is project type string.
     *
     * @param type the type
     * @return the string
     */
    public String isProjectType(final String type) {
        if (StringUtils.trimToNull(projectType) == null) {
            return "projectTypeLocal".equalsIgnoreCase(type) == true ? "true" : "false";
        }

        return projectType.equalsIgnoreCase(type) ? "true" : "false";
    }

    /**
     * Is report type boolean.
     *
     * @param type the type
     * @return the boolean
     */
    public boolean isReportType(final String type) {
        if (StringUtils.trimToNull(reportType) == null) {
            return "reportTypeDefault".equalsIgnoreCase(type);
        }

        return reportType.equalsIgnoreCase(type);
    }

    /**
     * Is repport custom path boolean.
     *
     * @return the boolean
     */
    public boolean isRepportCustomPath() {
        return isReportType("reportTypeCustom");
    }

    /**
     * Is license type string.
     *
     * @param type the type
     * @return the string
     */
    public String isLicenseType(final String type) {
        if (StringUtils.trimToNull(licenseType) == null) {
            return "licenseTypeLocal".equalsIgnoreCase(type) ? "true" : "false";
        }

        return licenseType.equalsIgnoreCase(type) ? "true" : "false";
    }

    public String getXMLReportArtifactPath() {
        return constructXMLReportArtifactPath();
    }

    /**
     * Construct xml report artifact path string.
     *
     * @return the string
     */
    public String constructXMLReportArtifactPath() {
        if (isRepportCustomPath()) {
            return PluginUtils.removeWorkspaceOrRelativePoint(xmlReport);
        }
        return "/neoload-report/report.xml";
    }

    public String getHTMLReportArtifactPath() {
        return constructHTMLReportArtifactPath();
    }

    /**
     * Construct html report artifact path string.
     *
     * @return the string
     */
    public String constructHTMLReportArtifactPath() {
        if (isRepportCustomPath()) {
            return PluginUtils.removeWorkspaceOrRelativePoint(htmlReport);
        }
        return "/neoload-report/report.html";
    }

    /**
     * Gets descriptor.
     *
     * @return the descriptor
     */
    @Override
    public Descriptor<Builder> getDescriptor() {
        final DescriptorImpl descriptor = (DescriptorImpl) super.getDescriptor();

        // setting this as an instance allows us to re-select the currently selected dropdown options.
        descriptor.setNeoBuildAction(this);

        return descriptor;
    }


    /**
     * Gets executable.
     *
     * @return the executable
     */
    public String getExecutable() {
        if (StringUtils.isEmpty(executable)) {
            return PluginUtils.getNeoGlobalConfig().getDefaultExecutable();
        } else {
            return executable;
        }
    }

    /**
     * Gets shared project name.
     *
     * @return the shared project name
     */
    public String getSharedProjectName() {
        return sharedProjectName;
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
     * Gets xml report.
     *
     * @return the xml report
     */
    public String getXmlReport() {
        return xmlReport;
    }

    public String getPdfReport() {
        return pdfReport;
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
     * Is display the gui boolean.
     *
     * @return the boolean
     */
    public boolean isDisplayTheGUI() {
        return displayTheGUI;
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
     * Gets test description.
     *
     * @return the test description
     */
    public String getTestDescription() {
        return testDescription;
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
     * Gets license vu count.
     *
     * @return the license vu count
     */
    public String getLicenseVUCount() {
        return licenseVUCount;
    }

    /**
     * Gets license duration.
     *
     * @return the license duration
     */
    public String getLicenseDuration() {
        return licenseDuration;
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
     * Gets local project file.
     *
     * @return the local project file
     */
    public String getLocalProjectFile() {
        return localProjectFile;
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
     * Gets report type.
     *
     * @return the report type
     */
    public String getReportType() {
        return reportType;
    }

    /**
     * Gets publish test results.
     *
     * @return the publish test results
     */
    public boolean getPublishTestResults() {
        return publishTestResults;
    }

    /**
     * Gets license server.
     *
     * @return the license server
     */
    public ServerInfo getLicenseServer() {
        return licenseServer;
    }

    /**
     * Sets license server.
     *
     * @param licenseServer the license server
     */
    public void setLicenseServer(final NTSServerInfo licenseServer) {
        this.licenseServer = licenseServer;
    }

    /**
     * Gets shared project server.
     *
     * @return the shared project server
     */
    public ServerInfo getSharedProjectServer() {
        return sharedProjectServer;
    }

    /**
     * Sets shared project server.
     *
     * @param sharedProjectServer the shared project server
     */
    public void setSharedProjectServer(final ServerInfo sharedProjectServer) {
        this.sharedProjectServer = sharedProjectServer;
    }

    public boolean isScanAllBuilds() {
        return scanAllBuilds;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return the showTrendAverageResponse
     */
    public boolean isShowTrendAverageResponse() {
        return showTrendAverageResponse;
    }

    /**
     * @return the showTrendErrorRate
     */
    public boolean isShowTrendErrorRate() {
        return showTrendErrorRate;
    }

    public List<GraphOptionsInfo> getGraphOptionsInfo() {
        return graphOptionsInfo;
    }

    public int getMaxTrends() {
        return maxTrends;
    }


    /**
     * Perform boolean.
     *
     * @param run      the build
     * @param ws       the ws
     * @param launcher the launcher
     * @param listener the listener
     * @return the boolean
     * @throws InterruptedException the interrupted exception
     */
    public void perform(Run<?, ?> run, FilePath ws, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        if (arePasswordsSafe()) {

            final StringBuilder sb = prepareCommandLine(launcher, ws, true);
            final SimpleBuildOption jobProp = SimpleBuildOption.fromNPO(this);
            final EnvVars env = run.getEnvironment(listener);

            run.getParent().removeProperty(SimpleBuildOption.class);
            run.getParent().addProperty(jobProp);
            run.addAction(new NeoResultsAction(run, xmlReport, htmlReport));

            Launcher.ProcStarter procStarter = launcher.launch();
            procStarter.cmdAsSingleString(env.expand(sb.toString()));
            procStarter.stdout(listener);
            Proc proc = launcher.launch(procStarter);
            int returnValue = proc.join();

            run.addAction(new ProjectSpecificAction(run));

            if (this.isArchiveAndBuildTrends()) {
                final String artifact = PluginUtils.forgeArtifactoryPath(this);
                listener.getLogger().println("Archiving " + artifact);
                ArtifactArchiver archiver = new ArtifactArchiver(artifact);

                archiver.perform(run, ws, launcher, listener);

                listener.getLogger().println("Building trends...");
                PluginUtils.buildGraph(run.getParent());
            }
            if (returnValue != 0) {
                throw new NeoloadException("Error occurred during the test.");
            }

        } else {
            listener.getLogger().println("\nWARNING: a non-ciphered password has been detected. Please follow the procedure below:" );
            listener.getLogger().println("   1. Retype and save this password in the Configure System section of Jenkins Manager.");
            listener.getLogger().println("   2. Go to the configuration of this job and save it again.");
            listener.getLogger().println();
        }
    }


    public boolean isArchiveAndBuildTrends() {
        return archiveAndBuildTrends;
    }

    public void setArchiveAndBuildTrends(final boolean archiveAndBuildTrends) {
        this.archiveAndBuildTrends = archiveAndBuildTrends;
    }

    /**
     * The type Descriptor.
     */
    @Extension(optional = true)
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private NeoBuildAction neoBuildAction = null;

        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() {
            super(NeoBuildAction.class);
            load();
        }

        /**
         * Gets license server options.
         *
         * @param preselected the preselected
         * @return the servers for sharing projects
         */
        public static ListBoxModel getLicenseServerOptions(final ServerInfo preselected) {
            final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor = PluginUtils.getNeoGlobalConfig();

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
         * Build nts display name string string.
         *
         * @param server                 the server
         * @param isSharedProjectDisplay the is shared project display
         * @return the string
         */
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

        /**
         * Sets neo build action.
         *
         * @param action the action
         */
        public void setNeoBuildAction(final NeoBuildAction action) {
            this.neoBuildAction = action;
        }

        /**
         * Gets display name.
         *
         * @return the display name
         */
        @Override
        public String getDisplayName() {
            return "Execute a NeoLoad Scenario";
        }

        /**
         * Configure boolean.
         *
         * @param req  the req
         * @param json the json
         * @return the boolean
         * @throws FormException the form exception
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
            save();
            return super.configure(req, json);
        }

        /**
         * Do fill shared project server items list box model.
         *
         * @param project the project
         * @return the list box model
         */
        public ListBoxModel doFillSharedProjectServerItems(@AncestorInPath final Item project) {
            ServerInfo preselected = null;
            if (neoBuildAction != null && neoBuildAction.sharedProjectServer != null) {
                preselected = neoBuildAction.sharedProjectServer;
            }

            return getProjectServerOptions(preselected);
        }

        /**
         * Do fill license server items list box model.
         *
         * @param project the project
         * @return the list box model
         */
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
        private ListBoxModel getProjectServerOptions(final ServerInfo preselected) {
            final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor = PluginUtils.getNeoGlobalConfig();

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
                final String displayName = server.getNonEmptyLabel(true);
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

        /**
         * Is applicable boolean.
         *
         * @param jobType the job type
         * @return the boolean
         */
        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Do check local project file form validation.
         *
         * @param localProjectFile the local project file
         * @return the form validation
         */
        public FormValidation doCheckLocalProjectFile(@QueryParameter("localProjectFile") final String localProjectFile) {
            final String[] extensions = {".nlp", ".yaml", ".yml", ".json"};
            return PluginUtils.validateFileExists(localProjectFile, extensions, true, false);
        }

        /**
         * Do check executable form validation.
         *
         * @param executable the executable
         * @return the form validation
         */
        public FormValidation doCheckExecutable(@QueryParameter final String executable) {
            final String[] extensions = {".exe"};
            return PluginUtils.validateFileExists(executable, extensions, false, true);
        }

        /**
         * Do check license vu count form validation.
         *
         * @param licenseVUCount the license vu count
         * @return the form validation
         */
        public FormValidation doCheckLicenseVUCount(@QueryParameter final String licenseVUCount) {
            return PluginUtils.formValidationErrorToWarning(FormValidation.validatePositiveInteger(licenseVUCount));
        }

        /**
         * Do check license duration form validation.
         *
         * @param licenseDuration the license duration
         * @return the form validation
         */
        public FormValidation doCheckLicenseDuration(@QueryParameter final String licenseDuration) {
            return PluginUtils.formValidationErrorToWarning(FormValidation.validatePositiveInteger(licenseDuration));
        }

        /**
         * Do check license id form validation.
         *
         * @param licenseID the license id
         * @return the form validation
         */
        public FormValidation doCheckLicenseID(@QueryParameter final String licenseID) {
            return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(licenseID));
        }

        /**
         * Do check shared project name form validation.
         *
         * @param sharedProjectName the shared project name
         * @return the form validation
         */
        public FormValidation doCheckSharedProjectName(@QueryParameter final String sharedProjectName) {
            return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(sharedProjectName));
        }

        /**
         * Do check xml report form validation.
         *
         * @param xmlReport the xml report
         * @return the form validation
         */
        public FormValidation doCheckXmlReport(@QueryParameter final String xmlReport) {
            return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(xmlReport));
        }

        /**
         * Do check scenario name form validation.
         *
         * @param scenarioName the scenario name
         * @return the form validation
         */
        public FormValidation doCheckScenarioName(@QueryParameter final String scenarioName) {
            return PluginUtils.formValidationErrorToWarning(FormValidation.validateRequired(scenarioName));
        }

        /**
         * Do check display the gui form validation.
         *
         * @param displayTheGUI the display the gui
         * @return the form validation
         */
        public FormValidation doCheckDisplayTheGUI(@QueryParameter final String displayTheGUI) {
            if (Boolean.valueOf(displayTheGUI)) {
                return FormValidation.warning("The user launching the process must be able to display a user interface "
                        + "(which is not always the case for the Jenkins user). Some errors or warning messages may prevent NeoLoad "
                        + "from closing automatically at the end of a test run. Thus this should only be used for testing purposes.");
            }

            return FormValidation.ok();
        }
    }

    private class BatchFileMine extends BatchFile {
        /**
         * Instantiates a new Batch file mine.
         */
        public BatchFileMine() {
            super("command");
        }

        /**
         * Gets contents.
         *
         * @return the contents
         */
        @Override
        public String getContents() {
            return super.getContents();
        }

        /**
         * Gets file extension.
         *
         * @return the file extension
         */
        @Override
        public String getFileExtension() {
            return super.getFileExtension();
        }
    }

    /**
     * The type Shell mine.
     */
    public class ShellMine extends Shell {
        /**
         * Instantiates a new Shell mine.
         */
        public ShellMine() {
            super("command");
        }

        /**
         * Gets contents.
         *
         * @return the contents
         */
        @Override
        public String getContents() {
            return super.getContents();
        }

        /**
         * Gets file extension.
         *
         * @return the file extension
         */
        @Override
        public String getFileExtension() {
            return super.getFileExtension();
        }
    }
}
