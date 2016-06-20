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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.MockObjects;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;

public class NeoResultsActionTest extends HudsonTestCase {

	/** A time format that is used for all languages in the context of this plugin. */
	public static final String STANDARD_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";

	/** Mock project for testing. */
	private MockObjects mo = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		mo = new MockObjects();
	}

	@Test
	public void testNeoResultsAction() {
		assertNotNull(new NeoResultsAction(mo.getAbstractBuild()));
	}

	@Test
	public void testGetBuild() {
		final NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getBuild() == mo.getAbstractBuild());
	}

	/** Test that the report file is not fond. */
	@Test
	public void testGetHtmlReportFilePath_DontFindReportFile() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getHtmlReportFilePath() == null);

		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	/** Test that the report file is not fond.
	 * @throws IOException */
	@Test
	public void testFindHtmlReportArtifact_DontFindReportFile() throws IOException {
		String actualContent = FileUtils.readFileToString(mo.getReportFileArtifact().getFile());
		actualContent += NeoResultsAction.COMMENT_APPLIED_STYLE + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART1 +
				"99999" + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART2;
		FileUtils.write(mo.getReportFileArtifact().getFile(), actualContent);

		final AbstractBuild ab = mo.getAbstractBuild();
		final List artifacts = new ArrayList();
		artifacts.add(mo.getReportFileArtifact());
		Mockito.when(ab.getArtifacts()).thenReturn(artifacts);
		final NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getHtmlReportFilePath() == null);

		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	/** Test that the report file is found when it includes the correct tag.
	 * @throws IOException */
	@Test
	public void testGetHtmlReportFilePath_DoFindReportFileWithTag() throws IOException {
		final AbstractBuild ab = mo.getAbstractBuild();
		final NeoResultsAction nra = new NeoResultsAction(ab);

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(ab.getTimestamp()).thenReturn(cal);

		// set the duration to 60 minutes
		when(ab.getDuration()).thenReturn((long) (1000 * 60 * 60));
		// set the file create date to 30 seconds after the start date
		final List<Run.Artifact> artifacts = ab.getArtifacts();
		for (final Run.Artifact a: artifacts) {
			setArtifactFileTimetoAfterBuildTime(ab, a);
		}

		assertTrue(nra.getDisplayName() != null);
		assertTrue(nra.getUrlName() != null);
		assertTrue(nra.getIconFileName() != null);
	}

	/**
	 * @param ab
	 * @param a
	 * @throws IOException
	 */
	public static void setArtifactFileTimetoAfterBuildTime(final AbstractBuild ab, final Run.Artifact a) throws IOException {
		final long middleOfRunTime = ab.getTimestamp().getTimeInMillis() + ab.getDuration() / 2;
		final File file = a.getFile();

		// update the file modification time.
		file.setLastModified(middleOfRunTime);

		final Calendar buildEndTime = Calendar.getInstance();
		buildEndTime.setTime(ab.getTimestamp().getTime());
		buildEndTime.add(Calendar.MILLISECOND, (int) ab.getDuration());
		// we add X seconds leeway because usually the endTime matches the processed time exactly.
		buildEndTime.add(Calendar.SECOND, 15);

		final String debugMessage = "Start/File/End times: " + DateFormatUtils.format(ab.getTimestamp(), STANDARD_TIME_FORMAT) +
				" / " + DateFormatUtils.format(file.lastModified(), STANDARD_TIME_FORMAT) + " / " +
				DateFormatUtils.format(buildEndTime, STANDARD_TIME_FORMAT);
		final Calendar fileTime = Calendar.getInstance();
		fileTime.setTimeInMillis(file.lastModified());
		assertTrue(debugMessage, ab.getTimestamp().before(fileTime));
		assertTrue(debugMessage, buildEndTime.after(fileTime));
	}

	/** Test that the report file is found when it includes the correct tag. */
	@Test
	public void testGetHtmlReportFilePath_OldData() {
		final AbstractBuild ab = mo.getAbstractBuild();
		final NeoResultsAction nra = new NeoResultsAction(ab);

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 2);
		when(ab.getTimestamp()).thenReturn(cal);

		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	/** Test that if the plugin is uninstalled and reinstalled that the file date alone is not sufficient to add the report link.
	 * This looks exactly like {@see #testGetHtmlReportFilePath_DoFindReportFileWithoutTag()} except for one setting.
	 * @throws IOException */
	@Test
	public void testGetHtmlReportFilePath_EarlyExit() throws IOException {
		final AbstractBuild ab = mo.getAbstractBuild();
		final NeoResultsAction nra = new NeoResultsAction(ab);

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(ab.getTimestamp()).thenReturn(cal);
		// set the duration to 60 minutes
		when(ab.getDuration()).thenReturn((long) (1000 * 60 * 60));

		// remove the neoload tag for all html artifacts
		final List<Run.Artifact> artifacts = ab.getArtifacts();
		for (final Run.Artifact a: artifacts) {
			a.getFile().setLastModified(ab.getTimestamp().getTimeInMillis() + 1000 * 30);

			final String absolutePath = a.getFile().getAbsolutePath();
			if ("html".equalsIgnoreCase(FilenameUtils.getExtension(absolutePath))) {
				// set the file create date to 30 seconds after the start date
				// remove the NeoLoad tag
				String contents = FileUtils.readFileToString(a.getFile());
				contents = contents.replaceAll(Pattern.quote(NeoResultsAction.TAG_HTML_GENERATED_BY_NEOLOAD),
						"");
				a.getFile().delete();
				a.getFile().setLastModified(ab.getTimestamp().getTimeInMillis() + 1000 * 30);
			}
		}

		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetDisplayName() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getDisplayName() == null);
	}

	@Test
	public void testGetIconFileName() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetUrlName() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getUrlName() == null);
	}

	/**
	 * Test method for {@see org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils#addActionIfNotExists(hudson.model.AbstractBuild)}.
	 */
	@Test
	public void testAddActionIfNotExists() {
		final List<Action> actions = new ArrayList<Action>();
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));

		final AbstractBuild abstractBuild = mo.getAbstractBuild();
		when(abstractBuild.getActions()).thenReturn(actions);

		final ArgumentCaptor<Action> argument = ArgumentCaptor.forClass(Action.class);

		NeoResultsAction.addActionIfNotExists(abstractBuild);

		Mockito.verify(abstractBuild).addAction(argument.capture());
		assertTrue(argument.getValue() instanceof NeoResultsAction);
	}

	/**
	 * Test method for {@see org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils#addActionIfNotExists(hudson.model.AbstractBuild)}.
	 */
	@Test
	public void testAddActionIfNotExistsDontAdd() {
		final List<Action> actions = new ArrayList<Action>();
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(NeoResultsAction.class));

		final AbstractBuild abstractBuild = mo.getAbstractBuild();
		when(abstractBuild.getActions()).thenReturn(actions);

		NeoResultsAction.addActionIfNotExists(abstractBuild);

		Mockito.verify(abstractBuild, Mockito.never()).addAction((Action) Matchers.any());
	}

	@Test
	public void testIsFromCurrentBuild2() throws IOException, InterruptedException {
		final AbstractBuild abstractBuild = mo.getAbstractBuild();
		final NeoResultsAction nra = new NeoResultsAction(abstractBuild);

		// ---------------
		final long buildStartTimeInMillis = abstractBuild.getTimestamp().getTimeInMillis();
		final long middleOfRunTime = buildStartTimeInMillis + abstractBuild.getDuration() / 2;
		final Calendar betweenStartAndEnd = Calendar.getInstance();
		betweenStartAndEnd.setTimeInMillis(middleOfRunTime);
		String actualContent = FileUtils.readFileToString(mo.getReportFileArtifact().getFile());
		actualContent += NeoResultsAction.COMMENT_APPLIED_STYLE + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART1 +
				"0" + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART2;
		assertTrue("The associated build number is for the current the build.",
				nra.isFromTheCurrentBuild(mo.getReportFileArtifact(), actualContent));

		// ---------------
		actualContent = FileUtils.readFileToString(mo.getReportFileArtifact().getFile());
		actualContent += NeoResultsAction.COMMENT_APPLIED_STYLE + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART1 +
				"12345" + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART2;
		assertFalse("The associated build number is for the current the build.",
				nra.isFromTheCurrentBuild(mo.getReportFileArtifact(), actualContent));
	}

	@Test
	public void testGetAssociatedBuildFromFile() {
		final AbstractBuild abstractBuild = mo.getAbstractBuild();
		final NeoResultsAction nra = new NeoResultsAction(abstractBuild);
		assertEquals("No data is provided so the default value should be used.", -1, nra.getAssociatedBuildNumberFromFile("bob"));

		assertEquals("Invalid data is provided so the default value should be used.", -1,
				nra.getAssociatedBuildNumberFromFile(".... " + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART1));

		assertEquals("The correct number should have been extracted.", 27,
				nra.getAssociatedBuildNumberFromFile(".... " + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART1 + "27" +
						NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART2));
	}

	@Test
	public void testGetAssociatedBuildFromFile2() {
		final AbstractBuild abstractBuild = mo.getAbstractBuild();
		final NeoResultsAction nra = new NeoResultsAction(abstractBuild);
		assertEquals("No data is provided so the default value should be used.", -1, nra.getAssociatedBuildNumberFromFile("bob"));

		assertEquals("Invalid data is provided so the default value should be used.", -1,
				nra.getAssociatedBuildNumberFromFile(".... #Build number: "));

		assertEquals("The correct number should have been extracted.", 28,
				nra.getAssociatedBuildNumberFromFile(".... #Build number: 28#"));
	}
}
