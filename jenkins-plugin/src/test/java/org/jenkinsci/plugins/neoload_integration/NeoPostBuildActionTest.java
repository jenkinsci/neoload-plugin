package org.jenkinsci.plugins.neoload_integration;

import static org.junit.Assert.*;
import hudson.Launcher;
import hudson.tasks.BuildStepMonitor;
import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class NeoPostBuildActionTest extends TestCase {
	
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
	public void testNeoPostBuildAction() {
		NeoPostBuildAction npba = new NeoPostBuildAction(false, false);
	}

	@Test
	public void testNeoPostBuildActionDescriptorImpl() {
		NeoPostBuildAction.DescriptorImpl di = new NeoPostBuildAction.DescriptorImpl();
		di.getDisplayName();
		di.isApplicable(null);
	}

	@Test
	public void testPerform() {
		NeoPostBuildAction npba = new NeoPostBuildAction(false, false);
		npba.perform(mo.getAbstractBuild(), null, null);
	}

	@Test
	public void testGetRequiredMonitorService() {
		NeoPostBuildAction npba = new NeoPostBuildAction(false, false);
		assertTrue(npba.getRequiredMonitorService() == BuildStepMonitor.NONE);
	}

	@Test
	public void testIsShowTrendAverageResponse() {
		NeoPostBuildAction npba = new NeoPostBuildAction(false, false);
		assertFalse(npba.isShowTrendAverageResponse());
		npba = new NeoPostBuildAction(true, true);
		assertTrue(npba.isShowTrendAverageResponse());
	}

	@Test
	public void testIsShowTrendErrorRate() {
		NeoPostBuildAction npba = new NeoPostBuildAction(false, false);
		assertFalse(npba.isShowTrendErrorRate());
		npba = new NeoPostBuildAction(true, true);
		assertTrue(npba.isShowTrendErrorRate());
	}

}
