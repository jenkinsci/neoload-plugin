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
		
		// print certain exceptions
		NeoResultsAction.throwExceptions = true;
	}

	@Test
	public void testNeoResultsAction() {
		@SuppressWarnings("unused")
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
	}
	
	@Test
	public void testGetBuild() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getBuild() == mo.getAbstractBuild());
	}

	/** Tets that the report file is not fond.
	 * 
	 */
	@Test
	public void testGetHtmlReportFilePath_DontFindReportFile() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getHtmlReportFilePath() == null);
		
		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	/** Test that the report file is found when it includes the correct tag. */
	@Test
	public void testGetHtmlReportFilePath_DoFindReportFileWithTag() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(ab.getTimestamp()).thenReturn(cal);
		
		assertTrue(nra.getDisplayName() != null);
		assertTrue(nra.getUrlName() != null);
		assertTrue(nra.getIconFileName() != null);
	}

	/** Test that the report file is found when it includes the correct tag. */
	@Test
	public void testGetHtmlReportFilePath_OldData() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab);
		
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
		NeoResultsAction nra = new NeoResultsAction(ab);
		
		// remove the neoload tag for all html artifacts
		List artifacts = ab.getArtifacts();
		for (Object o: artifacts) {
			Artifact a = (Artifact) o;
			String absolutePath = a.getFile().getAbsolutePath();
			if ("html".equalsIgnoreCase(FilenameUtils.getExtension(absolutePath))) {
				// remove the NeoLoad tag
				String contents = FileUtils.readFileToString(a.getFile());
				contents = contents.replaceAll(Pattern.quote(NeoResultsAction.TAG_HTML_GENERATED_BY_NEOLOAD), 
						"");
				a.getFile().delete();
				FileUtils.writeStringToFile(a.getFile(), contents);
			}
		}
		
		assertTrue(nra.getDisplayName() != null);
		assertTrue(nra.getUrlName() != null);
		assertTrue(nra.getIconFileName() != null);
	}

	@Test
	public void testGetDisplayName() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getDisplayName() == null);
	}

	@Test
	public void testGetIconFileName() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetUrlName() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
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
		
		NeoResultsAction.addActionIfNotExists(abstractBuild);

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
		
		NeoResultsAction.addActionIfNotExists(abstractBuild);

		Mockito.verify(abstractBuild, Mockito.never()).addAction((Action) Matchers.any());
	}

}
