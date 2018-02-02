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

import org.jenkinsci.plugins.neoload.integration.NeoResultsAction;
import org.kohsuke.stapler.Stapler;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.model.Run;
import hudson.util.RunList;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.BCodec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.neoload.integration.NeoBuildAction;
import org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig;

import com.google.common.base.Charsets;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.FormValidation.FileValidator;
import jenkins.model.Jenkins;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;

public final class PluginUtils implements Serializable, Converter {

	/**
	 * Encode passwords so that they're not plain text on the disk.
	 */
	private static final BCodec BCODEC = new BCodec();

	static {
		Stapler.CONVERT_UTILS.register(new PluginUtils(), ServerInfo.class);
		Stapler.CONVERT_UTILS.register(new PluginUtils(), CollabServerInfo.class);
		Stapler.CONVERT_UTILS.register(new PluginUtils(), NTSServerInfo.class);
	}
	public static final LockManager GRAPH_LOCK = new LockManager();


	/**
	 * Log various messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(PluginUtils.class.getName());

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = -3063042074729452263L;

	/**
	 * Utility classes are not intended to be instantiated, but the plugin doesn't work if we throw an exception.
	 */
	private PluginUtils() {
	}

	public static String encode(final String text) throws EncoderException {
		return BCODEC.encode(text, Charsets.UTF_8.name());
	}

	public static String decode(final String text) throws DecoderException {
		return BCODEC.decode(text);
	}

	/**
	 * Get the configured instance for the plugin.
	 *
	 * @param project
	 * @return
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
	 * Get the configured instance for the plugin.
	 *
	 * @param project
	 * @return
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
	public static NeoResultsAction getNeoResultAction(final AbstractBuild<?, ?> build){
		return build.getAction(NeoResultsAction.class);
	}

	/**
	 * This could be DateUtils.toCalendar instead but then I would have to deal with maven dependencies again.
	 *
	 * @param date
	 * @return
	 */
	public static Calendar toCalendar(final Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}



	public ServerInfo convert(@SuppressWarnings("rawtypes") final Class type, final Object value) {
		// get the main config.
		final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
				(NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

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

	public static FormValidation validateWarnIfEmpty(final String fieldValue, final String displayName) {
		if (StringUtils.trimToNull(fieldValue) == null) {
			return FormValidation.warning("Don't forget to include the " + displayName + ".");
		}
		return FormValidation.ok();
	}

	/**
	 * @param formValidation
	 * @return the same message but an error becomes a warning. "Ok" remains "Ok"
	 */
	public static FormValidation formValidationErrorToWarning(final FormValidation formValidation) {
		if (FormValidation.Kind.ERROR.equals(formValidation.kind)) {
			return FormValidation.warning(StringEscapeUtils.unescapeHtml(formValidation.getMessage()));
		}
		return formValidation;
	}

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
	 * @param originalStrings
	 * @return
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

	public static List<String> getHTMLReportPaths(final AbstractBuild<?, ?> build,final String firstPath) {
		List<String> paths = new ArrayList<>();

		if(firstPath != null){
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

	public static List<String> getXMLReportPaths(final AbstractBuild<?, ?> build) {
		List<String> paths = new ArrayList<>();

		final NeoResultsAction neoResultAction = getNeoResultAction(build);
		if(neoResultAction != null && neoResultAction.getStoredXmlReportPath()!=null){
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

	public static String removeWorkspace(final String report) {
		if (report == null) {
			return null;
		}
		return report.replaceAll("%WORKSPACE%/|\\$\\{WORKSPACE}/", "");
	}


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

	public static File getPicturesFolder(AbstractProject<?, ?> project) {
		return new File(project.getRootDir(), "neoload-trend");
	}

	public static void buildGraph(final File picturesFolder, final NeoLoadPluginOptions npo, final AbstractProject<?, ?> project) {
		if(GRAPH_LOCK.tryLock(project)) {
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


	private static List<AbstractBuild> getLimitedBuilds(NeoLoadPluginOptions npo, final AbstractProject project) {
		final int maxTrends = npo.getMaxTrends();
		final RunList<?> builds = project.getBuilds();
		if (maxTrends > 0 && builds.size() > maxTrends) {
			return (List<AbstractBuild>) builds.subList(0, maxTrends);
		} else {
			return (List<AbstractBuild>) builds;
		}
	}

	public static void buildGraph(AbstractProject project) {
		final NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(project);
		final File picturesFolder = PluginUtils.getPicturesFolder(project);
		PluginUtils.buildGraph(picturesFolder, npo, project);
	}
}

