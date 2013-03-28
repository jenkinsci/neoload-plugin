package org.jenkinsci.plugins.neoload_integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class NeoResultsActionTest extends TestCase {
	
	/** Mock project for testing. */
	private MockObjects mo = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		mo = new MockObjects();
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

	@Test
	public void testGetHtmlReportFilePath() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getHtmlReportFilePath() == null);
		
		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetHtmlReportFilePath2() {
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab);
		
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
