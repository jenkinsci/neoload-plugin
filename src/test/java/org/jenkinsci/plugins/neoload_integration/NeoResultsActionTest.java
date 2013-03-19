package org.jenkinsci.plugins.neoload_integration;

import static org.junit.Assert.fail;
import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.junit.Before;
import org.junit.Test;

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
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
	}

	@Test
	public void testGetBuild() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getBuild() == mo.getAbstractBuild());
	}

	@Test
	public void testGetHtmlReportFilePath() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getHtmlReportFilePath() == null);
	}

	@Test
	public void testGetDisplayName() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getDisplayName() == null);
	}

	@Test
	public void testGetIconFileName() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetUrlName() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getUrlName() == null);
	}

}
