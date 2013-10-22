/*
 * Copyright (c) 2013, Neotys
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
package org.jenkinsci.plugins.neoload_integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.Run.Artifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class NeoResultsActionTest extends HudsonTestCase {
	
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
		assertNotNull(new NeoResultsAction(mo.getAbstractBuild(), true));
	}
	
	@Test
	public void testGetBuild() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild(), true);
		assertTrue(nra.getBuild() == mo.getAbstractBuild());
	}

	/** Tets that the report file is not fond.
	 * 
	 */
	@Test
	public void testGetHtmlReportFilePath_DontFindReportFile() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab, true);
		assertTrue(nra.getHtmlReportFilePath() == null);
		
		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	/** Test that the report file is found when it includes the correct tag. */
	@Test
	public void testGetHtmlReportFilePath_DoFindReportFileWithTag() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab, true);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(ab.getTimestamp()).thenReturn(cal);
		
		// set the duration to 60 minutes
		when(ab.getDuration()).thenReturn((long) (1000 * 60 * 60));
		// set the file create date to 30 seconds after the start date
		List<?> artifacts = ab.getArtifacts();
		for (Object o: artifacts) {
			Artifact a = (Artifact) o;
			a.getFile().setLastModified(ab.getTimestamp().getTimeInMillis() + 1000 * 30);
		}
		
		assertTrue(nra.getDisplayName() != null);
		assertTrue(nra.getUrlName() != null);
		assertTrue(nra.getIconFileName() != null);
	}

	/** Test that the report file is found when it includes the correct tag. */
	@Test
	public void testGetHtmlReportFilePath_OldData() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab, true);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 2);
		when(ab.getTimestamp()).thenReturn(cal);
		
		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}
	
	/** Test that the report file is found when it does not include the correct tag. 
	 * @throws IOException */
	@Test
	public void testGetHtmlReportFilePath_DoFindReportFileWithoutTag() throws IOException {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab, true);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(ab.getTimestamp()).thenReturn(cal);
		// set the duration to 60 minutes
		when(ab.getDuration()).thenReturn((long) (1000 * 60 * 60));
		
		// remove the neoload tag for all html artifacts
		List<?> artifacts = ab.getArtifacts();
		for (Object o: artifacts) {
			Artifact a = (Artifact) o;
			a.getFile().setLastModified(ab.getTimestamp().getTimeInMillis() + 1000 * 30);
			
			String absolutePath = a.getFile().getAbsolutePath();
			if ("html".equalsIgnoreCase(FilenameUtils.getExtension(absolutePath))) {
				// set the file create date to 30 seconds after the start date
				// remove the NeoLoad tag
				String contents = FileUtils.readFileToString(a.getFile());
				contents = contents.replaceAll(Pattern.quote(NeoResultsAction.TAG_HTML_GENERATED_BY_NEOLOAD), 
						"");
				a.getFile().delete();
				FileUtils.writeStringToFile(a.getFile(), contents);
				a.getFile().setLastModified(ab.getTimestamp().getTimeInMillis() + 1000 * 30);
			}
		}
		
		assertTrue(nra.getDisplayName() != null);
		assertTrue(nra.getUrlName() != null);
		assertTrue(nra.getIconFileName() != null);
	}

	
	/** Test that if the plugin is uninstalled and reinstalled that the file date alone is not sufficient to add the report link.
	 * This looks exactly like {@link #testGetHtmlReportFilePath_DoFindReportFileWithoutTag()} except for one setting. 
	 * @throws IOException */
	@Test
	public void testGetHtmlReportFilePath_EarlyExit() throws IOException {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab, false);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(ab.getTimestamp()).thenReturn(cal);
		// set the duration to 60 minutes
		when(ab.getDuration()).thenReturn((long) (1000 * 60 * 60));
		
		// remove the neoload tag for all html artifacts
		List<?> artifacts = ab.getArtifacts();
		for (Object o: artifacts) {
			Artifact a = (Artifact) o;
			a.getFile().setLastModified(ab.getTimestamp().getTimeInMillis() + 1000 * 30);
			
			String absolutePath = a.getFile().getAbsolutePath();
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
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab, true);
		assertTrue(nra.getDisplayName() == null);
	}

	@Test
	public void testGetIconFileName() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab, true);
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetUrlName() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab, true);
		assertTrue(nra.getUrlName() == null);
	}
	
	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils#addActionIfNotExists(hudson.model.AbstractBuild)}.
	 */
	@Test
	public void testAddActionIfNotExists() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		
		AbstractBuild<?, ?> abstractBuild = mo.getAbstractBuild();
		when(abstractBuild.getActions()).thenReturn(actions);
		
		ArgumentCaptor<Action> argument = ArgumentCaptor.forClass(Action.class);
		
		NeoResultsAction.addActionIfNotExists(abstractBuild, true);

		Mockito.verify(abstractBuild).addAction(argument.capture());
		assertTrue(argument.getValue() instanceof NeoResultsAction);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils#addActionIfNotExists(hudson.model.AbstractBuild)}.
	 */
	@Test
	public void testAddActionIfNotExistsDontAdd() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(NeoResultsAction.class));
		
		AbstractBuild<?, ?> abstractBuild = mo.getAbstractBuild();
		when(abstractBuild.getActions()).thenReturn(actions);
		
		NeoResultsAction.addActionIfNotExists(abstractBuild, true);

		Mockito.verify(abstractBuild, Mockito.never()).addAction((Action) Matchers.any());
	}

}
