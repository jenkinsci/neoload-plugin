package org.jenkinsci.plugins.neoload_integration;

import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.model.Run.Artifact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.jenkinsci.plugins.neoload_integration.supporting.ZipUtils;
import org.junit.Before;
import org.junit.Test;
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
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
	}
	
	@Test
	public void testGetBuild() {
		NeoResultsAction nra = new NeoResultsAction(mo.getAbstractBuild());
		assertTrue(nra.getBuild() == mo.getAbstractBuild());
	}

	@Test
	public void testGetHtmlReportFilePath() throws FileNotFoundException, IOException {
		AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getHtmlReportFilePath() == null);
		
		assertTrue(nra.getDisplayName() == null);
		assertTrue(nra.getUrlName() == null);
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetHtmlReportFilePath2() throws FileNotFoundException, IOException {
		AbstractBuild ab = mo.getAbstractBuild();
		NeoResultsAction nra = new NeoResultsAction(ab);
		
		assertTrue(nra.getDisplayName() != null);
		assertTrue(nra.getUrlName() != null);
		assertTrue(nra.getIconFileName() != null);
	}

	@Test
	public void testGetDisplayName() {
		AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getDisplayName() == null);
	}

	@Test
	public void testGetIconFileName() {
		AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getIconFileName() == null);
	}

	@Test
	public void testGetUrlName() {
		AbstractBuild ab = mo.getAbstractBuild();
		Mockito.when(ab.getArtifacts()).thenReturn(Collections.EMPTY_LIST);
		NeoResultsAction nra = new NeoResultsAction(ab);
		assertTrue(nra.getUrlName() == null);
	}

}
