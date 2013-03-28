package org.jenkinsci.plugins.neoload_integration;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.util.RunList;
import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ProjectSpecificActionTest extends TestCase {
	
	/** Objects for testing. */
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
	public void testProjectSpecificAction() {
		@SuppressWarnings("unused")
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithOptions()); 
	}

	@Test
	public void testGetUrlName() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithOptions());
		assertTrue(psa.getUrlName() != null);
	}

	@Test
	public void testShowAvgGraph() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getResult()).thenReturn(Result.FAILURE);
		assertFalse(psa.showAvgGraph());
		
		psa = new ProjectSpecificAction(mo.getApWithOptions());
		assertFalse(psa.showAvgGraph());
	}

	@Test
	public void testShowErrGraph() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions()); 
		assertFalse(psa.showErrGraph());
		
		psa = new ProjectSpecificAction(mo.getApWithOptions());
		assertFalse(psa.showErrGraph());
	}

	@Test
	public void testGraphDataExists() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions()); 
		psa.graphDataExists();
	}

	@Test
	public void testGetErrGraph() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		NeoLoadGraph g = psa.getErrGraph();
		assertTrue(g != null);
	}

	@Test
	public void testGetAvgGraph() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		NeoLoadGraph g = psa.getAvgGraph();
		assertTrue(g != null);
	}

	@Test
	public void testGetErrGraph2() {
		AbstractProject ap = mo.getApWithOptions();
		
		RunList rl = ap.getBuilds();
		// add the same build to the project multiple times
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		Mockito.when(ap.getBuilds()).thenReturn(rl);

		ProjectSpecificAction psa = new ProjectSpecificAction(ap);
		psa.refreshGraphData();
		NeoLoadGraph g = psa.getErrGraph();
		assertTrue(g != null);
	}

	@Test
	public void testGetAvgGraph2() {
		AbstractProject ap = mo.getApWithOptions();
		
		RunList rl = ap.getBuilds();
		// add the same build to the project multiple times
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		Mockito.when(ap.getBuilds()).thenReturn(rl);

		ProjectSpecificAction psa = new ProjectSpecificAction(ap);
		psa.refreshGraphData();
		NeoLoadGraph g = psa.getAvgGraph();
		assertTrue(g != null);
	}

	@Test
	public void testGetIconFileName() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		psa.getIconFileName();
	}

	@Test
	public void testGetDisplayName() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		psa.getDisplayName();
	}

}
