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
	private AbstractProject apWithOptions = null;

	/** Mock project for testing. */
	private AbstractProject apWithoutOptions = null;

	/** Mock object for testing. */
	private Publisher publisherWithNeoOptions = mock(Publisher.class, withSettings().extraInterfaces(NeoLoadPluginOptions.class));
	
	/** Mock object for testing. */
	private AbstractBuild abstractBuild = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		// ap without options
		List<Publisher> publishersWithoutNeoOptions = new ArrayList<>();
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		
		DescribableList describableListWithoutNeoOptions = mock(DescribableList.class);
		when(describableListWithoutNeoOptions.iterator()).thenReturn(publishersWithoutNeoOptions.iterator());
		
		apWithoutOptions = mock(AbstractProject.class, "AbstractProject no plugin options");
		when(apWithoutOptions.getPublishersList()).thenReturn(describableListWithoutNeoOptions);
		when(apWithoutOptions.getDisplayName()).thenReturn("projectNameAPWithoutOptions");
		
		// ap with options
		List<Publisher> publishersWithNeoOptions = new ArrayList<>();
		publishersWithNeoOptions.addAll(publishersWithoutNeoOptions);
		publishersWithNeoOptions.add(publisherWithNeoOptions);

		DescribableList describableListWithNeoOptions = mock(DescribableList.class);
		when(describableListWithNeoOptions.iterator()).thenReturn(publishersWithNeoOptions.iterator());

		apWithOptions = mock(AbstractProject.class, "AbstractProject with plugin options");
		when(apWithOptions.getPublishersList()).thenReturn(describableListWithNeoOptions);
		when(apWithOptions.getDisplayName()).thenReturn("projectNameAPWithOptions");
		
		// abstract build
		abstractBuild = mock(AbstractBuild.class);
		when(abstractBuild.getProject()).thenReturn(apWithOptions);

	}
	
	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils#getPluginOptions(hudson.model.AbstractProject)}.
	 */
	@Test
	public void testGetPluginOptions() {
		NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(apWithoutOptions);
		assertTrue(npo == null);
		
		npo = PluginUtils.getPluginOptions(apWithOptions);
		assertTrue(npo == publisherWithNeoOptions);
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
		
		when(abstractBuild.getActions()).thenReturn(actions);
		
		PluginUtils.addActionIfNotExists(abstractBuild);

		Mockito.verify(abstractBuild, Mockito.never()).addAction((Action) Matchers.any());
	}
	
}
