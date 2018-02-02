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
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.MockObjects;
import org.jenkinsci.plugins.neoload.integration.supporting.NeoLoadReportDocTest;
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


	/** Mock project for testing. */
	private MockObjects mo = null;

	private URL urlXml;
	private URL urlHtml;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		mo = new MockObjects();
		urlXml = NeoLoadReportDocTest.class.getResource("data/myReport.xml");
		urlHtml = NeoLoadReportDocTest.class.getResource("data/myReport.html");
	}


	@Test
	public void testGetBuild() {
		final NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild(),urlXml.getFile(),urlHtml.getFile());
		assertTrue(nra.getBuild() == mo.getAbstractBuild());
	}

	/** Test that the report file is not fond. */
	@Test
	public void testGetHtmlReportFilePath_DontFindReportFile() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab,urlXml.getFile(),urlHtml.getFile());
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
		final NeoResultsAction nra = new NeoResultsAction(ab,urlXml.getFile(),urlHtml.getFile());
		assertTrue(nra.getHtmlReportFilePath() == null);

		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}



	@Test
	public void testGetDisplayName() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab,urlXml.getFile(),urlHtml.getFile());
		assertTrue(nra.getDisplayName() == null);
	}

	@Test
	public void testGetIconFileName() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab,urlXml.getFile(),urlHtml.getFile());
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetUrlName() {
		final AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		final NeoResultsAction nra = new NeoResultsAction(ab,urlXml.getFile(),urlHtml.getFile());
		assertTrue(nra.getUrlName() == null);
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

		//NeoResultsAction.addActionIfNotExists(abstractBuild);

		Mockito.verify(abstractBuild, Mockito.never()).addAction((Action) Matchers.any());
	}

	@Test
	public void testIsFromCurrentBuild2() throws IOException, InterruptedException {
		final AbstractBuild abstractBuild = mo.getAbstractBuild();
		final NeoResultsAction nra = new NeoResultsAction(abstractBuild,urlXml.getFile(),urlHtml.getFile());

		// ---------------
		final long buildStartTimeInMillis = abstractBuild.getTimestamp().getTimeInMillis();
		final long middleOfRunTime = buildStartTimeInMillis + abstractBuild.getDuration() / 2;
		final Calendar betweenStartAndEnd = Calendar.getInstance();
		betweenStartAndEnd.setTimeInMillis(middleOfRunTime);
		String actualContent = FileUtils.readFileToString(mo.getReportFileArtifact().getFile());
		actualContent += NeoResultsAction.COMMENT_APPLIED_STYLE + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART1 +
				"0" + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART2;
		/*assertTrue("The associated build number is for the current the build.",
				nra.isFromTheCurrentBuild(mo.getReportFileArtifact(), actualContent));*/

		// ---------------
		actualContent = FileUtils.readFileToString(mo.getReportFileArtifact().getFile());
		actualContent += NeoResultsAction.COMMENT_APPLIED_STYLE + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART1 +
				"12345" + NeoResultsAction.COMMENT_APPLIED_FOR_BUILD_PART2;
		/*assertFalse("The associated build number is for the current the build.",
				nra.isFromTheCurrentBuild(mo.getReportFileArtifact(), actualContent));*/
	}


}
