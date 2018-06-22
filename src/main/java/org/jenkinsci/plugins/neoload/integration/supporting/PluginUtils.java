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

import com.google.common.base.Charsets;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.FormValidation.FileValidator;
import hudson.util.ListBoxModel;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.BCodec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.neoload.integration.NeoBuildAction;
import org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig;
import org.jenkinsci.plugins.neoload.integration.NeoResultsAction;
import org.kohsuke.stapler.Stapler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * In this class numerous function has been duplicated with a different prototype to work with pipelines
 * The older methods had been kept for compatibility purpose with the old plugin
 */
public final class PluginUtils implements Serializable, Converter {

	/**
	 * The constant GRAPH_LOCK.
	 */
	public static final LockManager GRAPH_LOCK = new LockManager();
	/**
	 * Encode passwords so that they're not plain text on the disk.
	 */
	private static final BCodec BCODEC = new BCodec();
	/**
	 * Log various messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(PluginUtils.class.getName());
	/**
	 * Generated.
	 */
	private static final long serialVersionUID = -3063042074729452263L;
	public static final String EMPTY_SERVER_MESSAGE = "Please configure Jenkins System Settings for NeoLoad to add a server.";

	static {
		Stapler.CONVERT_UTILS.register(new PluginUtils(), ServerInfo.class);
		Stapler.CONVERT_UTILS.register(new PluginUtils(), CollabServerInfo.class);
		Stapler.CONVERT_UTILS.register(new PluginUtils(), NTSServerInfo.class);
	}

	/**
	 * Utility classes are not intended to be instantiated, but the plugin doesn't work if we throw an exception.
	 */
	private PluginUtils() {
	}

	/**
	 * Encode string.
	 *
	 * @param text the text
	 * @return the string
	 * @throws EncoderException the encoder exception
	 */
	public static String encode(final String text) throws EncoderException {
		return BCODEC.encode(text, Charsets.UTF_8.name());
	}

	/**
	 * Decode string.
	 *
	 * @param text the text
	 * @return the string
	 * @throws DecoderException the decoder exception
	 */
	public static String decode(final String text) throws DecoderException {
		return BCODEC.decode(text);
	}

	/**
	 * Get the configured instance for the plugin.
	 *
	 * @param project the project
	 * @return plugin options
	 */
	public static NeoLoadPluginOptions getPluginOptions(final AbstractProject<?, ?> project) {
		final Project<?, ?> proj;
		NeoBuildAction nba = null;
		if (!(project instanceof Project)) {
			return null;
		}
		proj = (Project<?, ?>) project;
		final List<Builder> builders = proj.getBuilders();
		for (final Builder b : builders) {
			if (b instanceof NeoBuildAction) {
				nba = (NeoBuildAction) b;
				break;
			}
		}

		return (NeoLoadPluginOptions) nba;
	}

	/**
	 * Gets plugin options.
	 *
	 * @param project the project
	 * @return the plugin options
	 */
	public static NeoLoadPluginOptions getPluginOptions(final Job<?, ?> project) {
		return project.getProperty(SimpleBuildOption.class);
	}

	/**
	 * Get the configured instance for the plugin.
	 *
	 * @param project the project
	 * @return neo build action
	 */
	public static NeoBuildAction getNeoBuildAction(final AbstractProject<?, ?> project) {

		if (!(project instanceof Project)) {
			return null;
		}
		for (final Builder b : ((Project<?, ?>) project).getBuilders()) {
			if (b instanceof NeoBuildAction) {
				return (NeoBuildAction) b;
			}
		}
		return null;
	}


	/**
	 * Gets neo result action.
	 *
	 * @param build the build
	 * @return the neo result action
	 */
	public static NeoResultsAction getNeoResultAction(final AbstractBuild<?, ?> build) {
		return build.getAction(NeoResultsAction.class);
	}

	/**
	 * Gets neo result action.
	 *
	 * @param build the build
	 * @return the neo result action
	 */
	public static NeoResultsAction getNeoResultAction(final Run<?, ?> build) {
		return build.getAction(NeoResultsAction.class);
	}


	/**
	 * Validate warn if empty form validation.
	 *
	 * @param fieldValue  the field value
	 * @param displayName the display name
	 * @return the form validation
	 */
	public static FormValidation validateWarnIfEmpty(final String fieldValue, final String displayName) {
		if (StringUtils.trimToNull(fieldValue) == null) {
			return FormValidation.warning("Don't forget to include the " + displayName + ".");
		}
		return FormValidation.ok();
	}

	/**
	 * Form validation error to warning form validation.
	 *
	 * @param formValidation the form validation
	 * @return the same message but an error becomes a warning. "Ok" remains "Ok"
	 */
	public static FormValidation formValidationErrorToWarning(final FormValidation formValidation) {
		if (FormValidation.Kind.ERROR.equals(formValidation.kind)) {
			return FormValidation.warning(StringEscapeUtils.unescapeHtml(formValidation.getMessage()));
		}
		return formValidation;
	}

	/**
	 * Validate url form validation.
	 *
	 * @param url the url
	 * @return the form validation
	 */
	public static FormValidation validateURL(final String url) {
		if (StringUtils.trimToNull(url) == null) {
			return FormValidation.warning("Don't forget to include the URL.");
		}
		try {
			final URI uri = new URI(url);
			if (uri.getScheme() == null || uri.getHost() == null) {
				return FormValidation.error("Invalid URL: " + url);
			}
			return FormValidation.ok();
		} catch (final Exception e) {
			return FormValidation.error("URL could not be parsed.");
		}
	}

	/**
	 * removes empty strings from a list.
	 *
	 * @param originalStrings the original strings
	 * @return list list
	 */
	public static List<String> removeAllEmpties(final String... originalStrings) {
		final List<String> cleanedStrings = new ArrayList<String>(Arrays.asList(originalStrings));
		cleanedStrings.removeAll(Arrays.asList(null, "", Collections.singleton(null)));

		final Iterator<String> it = cleanedStrings.iterator();
		while (it.hasNext()) {
			final String s2 = it.next();
			if (StringUtils.trimToEmpty(s2).length() == 0) {
				it.remove();
			}
		}

		return cleanedStrings;
	}

	/**
	 * Check if the given string points to a file on local machine.
	 * If it's not the case, just display an info message, not a warning because
	 * it might be executed on a remote host.
	 *
	 * @param file           the file
	 * @param extension      the extension
	 * @param checkExtension the check extension
	 * @param checkInPath    the check in path
	 * @return the form validation
	 */
	public static FormValidation validateFileExists(String file, final String extension, final boolean checkExtension, final boolean checkInPath) {
		// If file is null or empty, return an error
		final FormValidation emptyOrNullValidation = FormValidation.validateRequired(file);
		if (!FormValidation.Kind.OK.equals(emptyOrNullValidation.kind)) {
			return emptyOrNullValidation;
		}

		if (checkExtension && !file.toLowerCase().endsWith(extension)) {
			return FormValidation.error("Please specify a file with " + extension + " extension");
		}

		// insufficient permission to perform validation?
		if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
			return FormValidation.ok("Insufficient permission to perform legend validation.");
		}

		if (file.indexOf(File.separatorChar) >= 0) {
			// this is full legend
			File f = new File(file);
			if (f.exists()) return FileValidator.NOOP.validate(f);

			File fexe = new File(file + extension);
			if (fexe.exists()) return FileValidator.NOOP.validate(fexe);
		}

		if (Files.exists(Paths.get(file))) {
			return FormValidation.ok();
		}

		if (checkInPath) {
			String path = EnvVars.masterEnvVars.get("PATH");

			String delimiter = null;
			if (path != null) {
				for (String _dir : Util.tokenize(path.replace("\\", "\\\\"), File.pathSeparator)) {
					if (delimiter == null) {
						delimiter = ", ";
					}
					File dir = new File(_dir);

					File f = new File(dir, file);
					if (f.exists()) return FileValidator.NOOP.validate(f);

					File fexe = new File(dir, file + ".exe");
					if (fexe.exists()) return FileValidator.NOOP.validate(fexe);
				}
			}
		}

		return FormValidation.ok("There is no such file on local host. You can ignore this message if the job is executed on a remote slave.");
	}

	/**
	 * Gets html report paths.
	 *
	 * @param build     the build
	 * @param firstPath the first path
	 * @return the html report paths
	 */
	public static List<String> getHTMLReportPaths(final AbstractBuild<?, ?> build, final String firstPath) {
		List<String> paths = new ArrayList<>();

		if (firstPath != null) {
			paths.add(firstPath);
		}
		final NeoBuildAction neoBuildAction = getNeoBuildAction(build.getProject());
		final String htmlReportPath = neoBuildAction.getHTMLReportArtifactPath();
		if (htmlReportPath != null) {
			paths.add(htmlReportPath);
		}
		paths.add("neoload-report/report.html");
		return paths;
	}

	/**
	 * Gets html report paths.
	 *
	 * @param build     the build
	 * @param firstPath the first path
	 * @return the html report paths
	 */
	public static List<String> getHTMLReportPaths(final Run<?, ?> build, final String firstPath) {
		List<String> paths = new ArrayList<>();

		if (firstPath != null) {
			paths.add(firstPath);
		}

		paths.add("neoload-report/report.html");
		return paths;
	}


	/**
	 * Gets xml report paths.
	 *
	 * @param build the build
	 * @return the xml report paths
	 */
	public static List<String> getXMLReportPaths(final AbstractBuild<?, ?> build) {
		List<String> paths = new ArrayList<>();

		final NeoResultsAction neoResultAction = getNeoResultAction(build);
		if (neoResultAction != null && neoResultAction.getStoredXmlReportPath() != null) {
			paths.add(neoResultAction.getStoredXmlReportPath());
		}

		final NeoBuildAction neoBuildAction = getNeoBuildAction(build.getProject());
		final String xmlReportPath = neoBuildAction.getXMLReportArtifactPath();
		if (xmlReportPath != null) {
			paths.add(xmlReportPath);
		}
		paths.add("neoload-report/report.xml");
		return paths;
	}

	/**
	 * Gets xml report paths.
	 *
	 * @param build the build
	 * @return the xml report paths
	 */
	public static List<String> getXMLReportPaths(final Run<?, ?> build) {
		List<String> paths = new ArrayList<>();

		final NeoResultsAction neoResultAction = getNeoResultAction(build);
		if (neoResultAction != null && neoResultAction.getStoredXmlReportPath() != null) {
			paths.add(neoResultAction.getStoredXmlReportPath());
		}

		paths.add("neoload-report/report.xml");
		return paths;
	}


	/**
	 * Remove workspace or relative point string.
	 *
	 * @param report the report
	 * @return the string
	 */
	public static String removeWorkspaceOrRelativePoint(final String report) {
		if (report == null) {
			return null;
		}
		if (report.startsWith(".") && !report.startsWith("..")) {
			return report.substring(1);
		}
		return report.replaceAll("%WORKSPACE%/|\\$\\{WORKSPACE}/", "");
	}

	/**
	 * Find artifact run . artifact.
	 *
	 * @param paths the paths
	 * @param build the build
	 * @return the run . artifact
	 */
	public static Run.Artifact findArtifact(final List<String> paths, final AbstractBuild<?, ?> build) {

		if (build == null) {
			// This can happen when the plugin is reinstalled or simply after time passes. When the plugin is
			// initialized you need to get the "project" instance, iterate over every build, and reinitialize every
			// instance of this action (NeoResultsAction) so that the build instance won't be null.
			LOGGER.log(Level.SEVERE, "NeoResultsAction.findHtmlReportArtifact() build is null.");
			return null;
		}

		//To be compatible  with older
		for (String path : paths) {
			for (Run.Artifact artifact : build.getArtifacts()) {
				if (artifact.relativePath.endsWith(path)) {
					return artifact;
				}
			}
		}
		return null;
	}

	/**
	 * Find artifact run . artifact.
	 *
	 * @param paths the paths
	 * @param build the build
	 * @return the run . artifact
	 */
	public static Run.Artifact findArtifact(final List<String> paths, final Run<?, ?> build) {
		if (build == null) {
			// This can happen when the plugin is reinstalled or simply after time passes. When the plugin is
			// initialized you need to get the "project" instance, iterate over every build, and reinitialize every
			// instance of this action (NeoResultsAction) so that the build instance won't be null.
			LOGGER.log(Level.SEVERE, "NeoResultsAction.findHtmlReportArtifact() build is null.");
			return null;
		}

		//To be compatible  with older
		for (String path : paths) {
			for (Run.Artifact artifact : build.getArtifacts()) {
				if (artifact.relativePath.endsWith(path)) {
					return artifact;
				}
			}
		}
		return null;
	}

	/**
	 * Gets custom.
	 *
	 * @param path the path
	 * @param doc  the doc
	 * @return the custom
	 * @throws XPathExpressionException the x path expression exception
	 */
	public static Float getCustom(final String path, final Document doc) throws XPathExpressionException {
		if (path == null) return null;
		final Node node = XMLUtilities.findFirstByExpression(path, doc);
		if (node != null) {
			return extractNeoLoadNumber(node.getNodeValue());
		}
		return null;
	}

	/**
	 * @param valArg
	 * @return
	 */
	private static Float extractNeoLoadNumber(final String valArg) {
		String val = StringUtils.trimToEmpty(valArg);

		// remove spaces etc
		val = val.replaceAll(",", ".").replaceAll(" ", "");
		// special case for percentages
		val = val.replaceAll(Pattern.quote("%"), "");
		// special case for percentages
		val = val.replaceAll(Pattern.quote("+"), "");
		// special case for less than 0.01%
		if ("<0.01".equals(val)) {
			return 0f;
		}

		try {
			return Float.valueOf(val);
		} catch (final Exception e) {
			// we couldn't convert the result to an actual number so the value will not be included.
			// this could be +INF, -INF, " - ", NaN, etc. See com.neotys.nl.util.FormatUtils.java, getTextNumber().
		}
		return null;
	}

	/**
	 * Gets pictures folder.
	 *
	 * @param project the project
	 * @return the pictures folder
	 */
	public static File getPicturesFolder(AbstractProject<?, ?> project) {
		return new File(project.getRootDir(), "neoload-trend");
	}

	/**
	 * Gets pictures folder.
	 *
	 * @param project the project
	 * @return the pictures folder
	 */
	public static File getPicturesFolder(Job<?, ?> project) {
		return new File(project.getRootDir(), "neoload-trend");
	}

	/**
	 * Build graph.
	 *
	 * @param picturesFolder the pictures folder
	 * @param npo            the npo
	 * @param project        the project
	 */
	public static void buildGraph(final File picturesFolder, final NeoLoadPluginOptions npo, final AbstractProject<?, ?> project) {
		if (GRAPH_LOCK.tryLock(project)) {
			try {
				picturesFolder.mkdirs();

				if (picturesFolder.isDirectory()) {
					//Clean
					for (File file : picturesFolder.listFiles()) {
						file.delete();
					}
					NeoloadGraphsStatistics neoloadGraphsStatistics = new NeoloadGraphsStatistics(npo);

					for (final AbstractBuild<?, ?> build : getLimitedBuilds(npo, project)) {
						neoloadGraphsStatistics.addBuild(build);
					}
					try {
						neoloadGraphsStatistics.writePng(picturesFolder);
					} catch (IOException e) {
						LOGGER.log(Level.WARNING, "Exception occurs during the picture writing ", e);
					}
				}
			} finally {
				GRAPH_LOCK.unlock(project);
			}
		}
	}

	/**
	 * Build graph.
	 *
	 * @param picturesFolder the pictures folder
	 * @param npo            the npo
	 * @param project        the project
	 */
	public static void buildGraph(final File picturesFolder, final NeoLoadPluginOptions npo, final Job<?, ?> project) {
		if (GRAPH_LOCK.tryLock(project)) {
			try {
				picturesFolder.mkdirs();

				if (picturesFolder.isDirectory()) {
					//Clean
					for (File file : picturesFolder.listFiles()) {
						file.delete();
					}
					NeoloadGraphsStatistics neoloadGraphsStatistics = new NeoloadGraphsStatistics(npo);

					for (final Run<?, ?> build : getLimitedBuilds(npo, project)) {
						neoloadGraphsStatistics.addBuild(build);
					}
					try {
						neoloadGraphsStatistics.writePng(picturesFolder);
					} catch (IOException e) {
						LOGGER.log(Level.WARNING, "Exception occurs during the picture writing ", e);
					}
				}
			} finally {
				GRAPH_LOCK.unlock(project);
			}
		}
	}

	private static List<AbstractBuild> getLimitedBuilds(NeoLoadPluginOptions npo, final AbstractProject project) {
		final int maxTrends = npo.getMaxTrends();
		final RunList<?> builds = project.getBuilds();
		if (maxTrends > 0 && builds.size() > maxTrends) {
			return (List<AbstractBuild>) builds.subList(0, maxTrends);
		} else {
			return (List<AbstractBuild>) builds;
		}
	}

	private static List<Run> getLimitedBuilds(NeoLoadPluginOptions npo, final Job project) {
		final int maxTrends = npo.getMaxTrends();
		final RunList<?> builds = project.getBuilds();
		if (maxTrends > 0 && builds.size() > maxTrends) {
			return (List<Run>) builds.subList(0, maxTrends);
		} else {
			return (List<Run>) builds;
		}
	}


	/**
	 * Build graph.
	 *
	 * @param project the project
	 */
	public static void buildGraph(AbstractProject project) {
		try {
			final NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(project);
			final File picturesFolder = PluginUtils.getPicturesFolder(project);
			PluginUtils.buildGraph(picturesFolder, npo, project);
		} catch (Throwable th) {
			LOGGER.log(Level.WARNING, "Exception occurs during the trend building", th);
		}
	}


	/**
	 * Build graph.
	 *
	 * @param project the project
	 */
	public static void buildGraph(Job project) {
		try {
			final NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(project);
			final File picturesFolder = PluginUtils.getPicturesFolder(project);
			PluginUtils.buildGraph(picturesFolder, npo, project);
		} catch (Throwable th) {
			LOGGER.log(Level.WARNING, "Exception occurs during the trend building", th);
		}
	}

	public Object convert(@SuppressWarnings("rawtypes") final Class type, final Object value) {
		// get the main config.
		final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor = getNeoGlobalConfig();

		if (globalConfigDescriptor == null) {
			LOGGER.log(Level.FINEST, "No NeoLoad server settings found. Please add servers before configuring jobs. (getLicenseServerOptions)");
			return null;
		}

		// find the serverInfo based on the unique ID.
		@SuppressWarnings("unchecked") final Collection<ServerInfo> allServerInfo =
				CollectionUtils.union(globalConfigDescriptor.getNtsInfo(), globalConfigDescriptor.getCollabInfo());
		for (final ServerInfo si : allServerInfo) {
			if (si.getUniqueID().equals(value)) {
				return si;
			}
		}
		return null;
	}

	public static NeoGlobalConfig.DescriptorImpl getNeoGlobalConfig() {
		return (NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);
	}


	public static List<ServerInfo> getServerInfos(boolean collab) {
		final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor = getNeoGlobalConfig();

		List<ServerInfo> serverInfoList = new ArrayList<>();
		if (globalConfigDescriptor != null) {
			serverInfoList.addAll(globalConfigDescriptor.getNtsInfo());

			if (collab) {
				serverInfoList.addAll(globalConfigDescriptor.getCollabInfo());
			}
		}
		return serverInfoList;
	}

	public static ListBoxModel getServerInfosListBox(boolean collab) {
		final List<ServerInfo> serverInfos = getServerInfos(collab);
		final ListBoxModel listBoxModel = new ListBoxModel();

		if (serverInfos.isEmpty()) {
			listBoxModel.add(new ListBoxModel.Option(EMPTY_SERVER_MESSAGE, null));
		} else {
			for (final ServerInfo server : serverInfos) {
				listBoxModel.add(new ListBoxModel.Option(server.getNonEmptyLabel(collab), server.getUniqueID()));
			}
		}
		return listBoxModel;
	}

	public static String forgeArtifactoryPath(final NeoBuildAction neoBuildAction) {
		List<String> paths = new ArrayList<>();
		final String htmlReport = neoBuildAction.getHtmlReport();
		if (StringUtils.isNotEmpty(htmlReport)) {
			paths.add(htmlReport);
			final File file = new File(htmlReport);
			paths.add(file.getParent()+"/"+ FilenameUtils.removeExtension(file.getName())+"_files/**");
		}
		final String xmlReport = neoBuildAction.getXmlReport();
		if(StringUtils.isNotEmpty(xmlReport)){
			paths.add(xmlReport);
			paths.add(xmlReport.replace(".xml",".dtd"));
		}
		addIfNotEmpty(paths, neoBuildAction.getPdfReport());
		addIfNotEmpty(paths, neoBuildAction.getJunitReport());
		return StringUtils.join(paths,",");
	}

	private static void addIfNotEmpty(final List<String> paths, final String str) {
		if (StringUtils.isNotEmpty(str)) {
			paths.add(str);
		}
	}


	public static boolean isSAP(final String licenseVUSAPCount) {
		return StringUtils.isNotEmpty(licenseVUSAPCount) && !licenseVUSAPCount.equals("0");
	}
}

