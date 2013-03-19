/**
 * 
 */
package org.jenkinsci.plugins.neoload_integration.supporting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.NeoResultsAction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author ajohnson
 *
 */
public class PluginUtilsTest extends TestCase {
	
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
	
	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils#getPluginOptions(hudson.model.AbstractProject)}.
	 */
	@Test
	public void testGetPluginOptions() {
		NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(mo.getApWithoutOptions());
		assertTrue(npo == null);
		
		npo = PluginUtils.getPluginOptions(mo.getApWithOptions());
		assertTrue(npo == mo.getPublisherWithNeoOptions());
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils#addActionIfNotExists(hudson.model.AbstractBuild)}.
	 */
	@Test
	public void testAddActionIfNotExists() {
		List<Action> actions = new ArrayList<>();
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		
		AbstractBuild abstractBuild = mo.getAbstractBuild();
		when(abstractBuild.getActions()).thenReturn(actions);
		
		ArgumentCaptor<Action> argument = ArgumentCaptor.forClass(Action.class);
		
		PluginUtils.addActionIfNotExists(abstractBuild);

		Mockito.verify(abstractBuild).addAction(argument.capture());
		assertTrue(argument.getValue() instanceof NeoResultsAction);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils#addActionIfNotExists(hudson.model.AbstractBuild)}.
	 */
	@Test
	public void testAddActionIfNotExistsDontAdd() {
		List<Action> actions = new ArrayList<>();
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(Action.class));
		actions.add(mock(NeoResultsAction.class));
		
		AbstractBuild abstractBuild = mo.getAbstractBuild();
		when(abstractBuild.getActions()).thenReturn(actions);
		
		PluginUtils.addActionIfNotExists(abstractBuild);

		Mockito.verify(abstractBuild, Mockito.never()).addAction((Action) Matchers.any());
	}
	
}
