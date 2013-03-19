package org.jenkinsci.plugins.neoload_integration;

import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.junit.Before;
import org.junit.Test;

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
		psa.getErrGraph();
	}

	@Test
	public void testGetAvgGraph() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		psa.getAvgGraph();
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
