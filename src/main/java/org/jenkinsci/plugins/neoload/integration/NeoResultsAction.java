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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils;

import com.neotys.nl.controller.report.transform.NeoLoadReportDoc;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.Run.Artifact;
import hudson.remoting.VirtualChannel;

/** This class integrates with the side panel of the specific run of a job. The side panel consists of the navigation links on the left.
 * 
 * Listens for the AbstractBuild so it can be referenced later. */
public class NeoResultsAction implements Action, Serializable {

	/** Generated. */
	private static final long serialVersionUID = -7304979204013061982L;

	/** This tag is found in certain pages generated by NeoLoad. */
	public static final String TAG_HTML_GENERATED_BY_NEOLOAD = "#HTML Report Generated by NeoLoad#";

	/** This is added to a file to mark whether the styles have been applied or not. */
	static final String COMMENT_APPLIED_STYLE = "<!-- NeoLoad Jenkins plugin applied style -->";

	/** This is added to a file to mark the date and time the file was processed. It also serves to tell us that the custom styles were applied. */
	static final String COMMENT_APPLIED_FOR_BUILD_PART1 = "<!-- PROCESSED DURING BUILD: ";

	/** This is added to a file to mark the date and time the file was processed. */
	static final String COMMENT_APPLIED_FOR_BUILD_PART2 = " -->";

	/** This is added to a file to mark whether the styles have been applied or not. */
	private static final String COMMENT_CSS_APPLIED_STYLE = "/* NeoLoad Jenkins plugin applied style */";

	/** The current build. */
	private final transient AbstractBuild<?, ?> build;

	/** True if the report file is found without any issues. This allows us to only show the link when the report file is found. */
	private Boolean foundReportFile = null;

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(NeoResultsAction.class.getName());

	/** @param target */
	NeoResultsAction(final AbstractBuild<?, ?> target) {
		super();
		build = target;
	}

	/**
	 * @param build
	 */
	public static void addActionIfNotExists(final AbstractBuild<?, ?> build) {
		boolean alreadyAdded = false;
		final List<Action> buildActions = build.getActions();
		for (int actionIndex = 0; actionIndex < buildActions.size(); actionIndex++) {
			final Action a = build.getActions().get(actionIndex);
			if (a instanceof NeoResultsAction) {
				alreadyAdded = true;

				// even though the action already exists we replace it, because sometimes old builds lose their
				// data for no apparent reason.
				buildActions.set(actionIndex, new NeoResultsAction(build));
				break;
			}
		}

		if (!alreadyAdded) {
			final NeoResultsAction nra = new NeoResultsAction(build);
			build.addAction(nra);
			LOGGER.log(Level.FINE, "Build " + build.number + ", Added action to build of job " +
					build.getProject().getDisplayName());

			// we process the file now so that the timestamp can be added before the build ends.
			nra.getHtmlReportFilePath();
		}
	}

	/** For storing artifact data. */
	static final class FileAndContent {
		/** Artifact data. */
		private final File file;

		/** URL to the artifact in Jenkins. */
		private final String href;

		/** Artifact data. */
		private String content = null;

		/** Constructor.
		 * 
		 * @param file
		 * @param href
		 * @param content
		 */
		public FileAndContent(final File file, final String href, final String content) {
			this.file = file;
			this.href = href;
			this.content = content;
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private FileAndContent findHtmlReportArtifact() {
		if (build == null) {
			// This can happen when the plugin is reinstalled or simply after time passes. When the plugin is 
			// initialized you need to get the "project" instance, iterate over every build, and reinitialize every
			// instance of this action (NeoResultsAction) so that the build instance won't be null. 
			LOGGER.log(Level.SEVERE, "NeoResultsAction.findHtmlReportArtifact() build is null.");
		}

		final Iterator<?> it = build.getArtifacts().iterator();
		FileAndContent ac = null;

		// remove files that don't match
		while (it.hasNext()) {
			final Artifact artifact = (Artifact) it.next();

			// if it's an html file
			if (artifact.getFileName().length() > 4 &&
					"html".equalsIgnoreCase(artifact.getFileName().substring(artifact.getFileName().length() - 4))) {

				// verify file contents
				try {
					final String content = FileUtils.fileRead(artifact.getFile().getAbsolutePath());
					if (content != null && isNeoLoadHTMLReport(content)) {
						// verify that the file was created during the current build
						if (isFromTheCurrentBuild(artifact, content)) {
							ac = new FileAndContent(artifact.getFile(), artifact.getHref(), content);
							break;
						}
						LOGGER.log(Level.WARNING,
								"Build " + build.number + ": Found " + artifact.relativePath + 
								", but it's linked to a different build (" + getAssociatedBuildNumberFromFile(content) + "). "
								+ "You should clean your workspace before running this job.");
					}
				} catch (final Exception e) {
					LOGGER.log(Level.FINE, "Error reading file. " + e.getMessage(), e);
				}
			}
		}

		return ac;
	}

	/**
	 * @param artifact
	 * @param fileContent
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	boolean isFromTheCurrentBuild(final Run<?, ?>.Artifact artifact, final String fileContent) throws IOException, InterruptedException {
		final String workspaceFilePath = getWorkspaceFilePath(artifact);
		final int buildNumberFromFile = getAssociatedBuildNumberFromFile(fileContent);

		if (buildNumberFromFile >= 0) {
			LOGGER.log(Level.FINE, "Build " + build.number + ", File corresponds to build " + buildNumberFromFile + ": " + workspaceFilePath);
			if (build.number == buildNumberFromFile) {
				// the build number was read correctly and it corresponds to the current build.
				return true;
			}

			// the build number was read correctly and it does not correspond to the current build.
			LOGGER.log(Level.FINEST, "Build " + build.number + ", File " + artifact.relativePath + " corresponds to build " + 
					buildNumberFromFile + ". You should clean your workspace before running this job.");
			return false;
		}

		final Calendar buildStartTime = PluginUtils.getBuildStartTime(build);
		final Calendar buildEndTime = PluginUtils.getBuildEndTime(build);
		final Calendar workspaceFileCreateTime = getWorkspaceFileCreateTime(build, workspaceFilePath);
		final SimpleDateFormat sdf = new SimpleDateFormat(NeoLoadReportDoc.STANDARD_TIME_FORMAT);

		final boolean isFromCurrentBuild = buildStartTime.before(workspaceFileCreateTime) && buildEndTime.after(workspaceFileCreateTime);

		LOGGER.log(Level.FINE, "Build " + build.number + ", No pre-existing build number found. Start / file time / end : " +
				sdf.format(buildStartTime.getTime()) + " / " + sdf.format(workspaceFileCreateTime.getTime()) + " / " +
				sdf.format(buildEndTime.getTime()) + ", File: " + workspaceFilePath +
				", isFromCurrentBuild: " + isFromCurrentBuild);

		// this should only be true when the build is run for the first time.
		return isFromCurrentBuild;
	}

	/**
	 * @param build2
	 * @param artifact
	 * @param workspaceFilePath
	 * @return
	 */
	Calendar getWorkspaceFileCreateTime(final AbstractBuild<?, ?> buildParam, final String workspaceFilePath) {
		final Calendar workspaceFileCreateTime = Calendar.getInstance();
		try {
			// we now use the file date to verify that the file is from the current build.
			final FilePath file = buildParam.getWorkspace();
			// get the file date of the workspace file from the remote machine because the file doesn't exist on the local machine.
			final long workspaceFileLastModifiedDate = file.act(new FileCallableForModifiedDate(workspaceFilePath));
			workspaceFileCreateTime.setTime(new Date(workspaceFileLastModifiedDate));

		} catch (final Exception e) {
			LOGGER.log(Level.FINE, "Build " + build.number + ", Issue reading workspace file time for (" + buildParam.number + "): " +
					workspaceFilePath, e);
			workspaceFileCreateTime.setTime(NeoLoadReportDoc.DATE_1970);
		}

		return workspaceFileCreateTime;
	}

	/** Returns the file modification date of a file on a local or a remote machine. */
	static class FileCallableForModifiedDate implements FilePath.FileCallable<Long> {
		/** Generated. */
		private static final long serialVersionUID = 5191449389416826768L;

		private final String fullFilePath;

		public FileCallableForModifiedDate(final String fullFilePath) {
			this.fullFilePath = fullFilePath;
		}

		/* (non-Javadoc)
		 * @see hudson.FilePath.FileCallable#invoke(java.io.File, hudson.remoting.VirtualChannel)
		 */
		public Long invoke(final File f, final VirtualChannel channel) throws IOException, InterruptedException {
			final File myFile = new File(fullFilePath);
			if (!myFile.exists()) {
				LOGGER.fine("Can't find artifact file in the workspace. I'm looking for " + myFile.getPath());
				// return a date in 1970
				return 0L;
			}
			return myFile.lastModified();
		}
	}

	/**
	 * @param fileContent
	 * @return the number of the build or NO_BUILD_FOUND if nothing was found.
	 */
	int getAssociatedBuildNumberFromFile(final String fileContent) {
		int buildNumberFromFile = PluginUtils.findBuildNumberUsingPattern(fileContent);
		try {
			if (buildNumberFromFile != PluginUtils.NO_BUILD_FOUND) {
				return buildNumberFromFile;
			}

			// the COMMENT_APPLIED_FOR_BUILD_PART1/2 tags will give us the build the file is associated with.
			if (fileContent.contains(COMMENT_APPLIED_FOR_BUILD_PART1)) {
				// extract the time the file was processed.
				String buildNumberString = fileContent.substring(fileContent.indexOf(COMMENT_APPLIED_FOR_BUILD_PART1) +
						COMMENT_APPLIED_FOR_BUILD_PART1.length());
				buildNumberString = buildNumberString.substring(0, buildNumberString.indexOf(COMMENT_APPLIED_FOR_BUILD_PART2)).trim();

				buildNumberFromFile = Integer.valueOf(buildNumberString);
				return buildNumberFromFile;
			}

		} catch (final Exception e) {
			LOGGER.log(Level.FINE, "Build " + build.number + ", Issue reading associated build number. ", e);
		}
		return buildNumberFromFile;
	}

	/**
	 * @param artifact
	 * @return
	 */
	private String getWorkspaceFilePath(final Run<?, ?>.Artifact artifact) {
		final FilePath file = build.getWorkspace();
		final String workspaceDirectory = file.getRemote();
		final String workspaceFilePath = workspaceDirectory + File.separatorChar + artifact.relativePath;

		return workspaceFilePath;
	}

	/**
	 * @param content
	 * @return true if the passed in content is the html file of a NeoLoad generated report
	 */
	private static boolean isNeoLoadHTMLReport(final String content) {
		if (content.contains(TAG_HTML_GENERATED_BY_NEOLOAD)) {
			return true;
		}

		return false;
	}

	/** Allows access to sidepanel.jelly from index.jelly.
	 * @return
	 */
	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public String getHtmlReportFilePath() {
		final FileAndContent ac = findHtmlReportArtifact();

		if (ac != null) {
			// append the style changes if it hasn't already been done
			if (!ac.content.contains(COMMENT_APPLIED_FOR_BUILD_PART1)) {
				applySpecialFormatting(ac);
			}

			foundReportFile = true;
			return ac.href;
		}

		foundReportFile = false;
		return null;
	}

	/** Fix the horizontal scrollbar by adding overflow-x: hidden in many places.
	 * @param ac
	 * @throws IOException
	 */
	private void applySpecialFormatting(final FileAndContent ac) {
		try {
			final String buildNumberForFileContent = COMMENT_APPLIED_FOR_BUILD_PART1 +
					build.number + COMMENT_APPLIED_FOR_BUILD_PART2;

			// adjust the content.
			ac.content = ac.content.replaceAll(Matcher.quoteReplacement("id=\"menu\""), "id=\"menu\" style='overflow-x: hidden;' ");
			ac.content = ac.content.replaceAll(Matcher.quoteReplacement("id=\"content\""), "id=\"content\" style='overflow-x: hidden;' ");
			if (!ac.content.contains(COMMENT_APPLIED_FOR_BUILD_PART1)) {
				ac.content += buildNumberForFileContent;
			}

			// write the content
			final long modDate = ac.file.lastModified();
			if (ac.file.canWrite()) {
				ac.file.delete();
				FileUtils.fileWrite(ac.file.getAbsolutePath(), ac.content);
				// keep the old modification date
				ac.file.setLastModified(modDate);
			}

			// find the menu.html
			String temp = ac.content.substring(ac.content.indexOf("src=\"") + 5);
			temp = temp.substring(0, temp.indexOf('\"'));
			final String menuLink = ac.file.getParent() + File.separatorChar + temp;
			String menuContent = FileUtils.fileRead(menuLink);
			menuContent = menuContent.replace(Matcher.quoteReplacement("body {"), "body {\noverflow-x: hidden;");

			if (!menuContent.contains(COMMENT_APPLIED_FOR_BUILD_PART1)) {
				menuContent += buildNumberForFileContent;
			}
			new File(menuLink).delete();
			FileUtils.fileWrite(menuLink, menuContent);

			// find the style.css
			temp = ac.content.substring(ac.content.indexOf("<link"), ac.content.indexOf(">", ac.content.indexOf("<link")));
			temp = temp.substring(temp.indexOf("href=") + 6, temp.length() - 1);
			final String styleLink = ac.file.getParent() + File.separatorChar + temp;
			String styleContent = FileUtils.fileRead(styleLink);
			styleContent = styleContent.replace(Matcher.quoteReplacement("body {"), "body {\noverflow-x: hidden;");
			styleContent += COMMENT_CSS_APPLIED_STYLE;
			new File(styleLink).delete();
			FileUtils.fileWrite(styleLink, styleContent);

		} catch (final IOException e) {
			// this operation is not important enough to throw an exception.
			LOGGER.log(Level.FINEST, "Couldn't add custom style to report files.");
		}
	}

	public String getDisplayName() {
		setFoundReportFile();
		if (!foundReportFile) {
			return null;
		}
		return "Performance Result";
	}

	public String getIconFileName() {
		setFoundReportFile();
		if (!foundReportFile) {
			return null;
		}
		return "/plugin/neoload-jenkins-plugin/images/logo48.png";
	}

	public String getUrlName() {
		setFoundReportFile();
		if (!foundReportFile) {
			return null;
		}
		return "neoload-report";
	}

	/** Set true if we can find the report file. */
	private void setFoundReportFile() {
		if (foundReportFile == null) {
			getHtmlReportFilePath();
		}
	}
}