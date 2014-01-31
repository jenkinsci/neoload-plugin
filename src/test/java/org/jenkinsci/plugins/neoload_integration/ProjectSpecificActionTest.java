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

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.util.RunList;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import com.neotys.nl.controller.report.transform.NeoLoadReportDoc;

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
		final ProjectSpecificAction projectSpecificAction = new ProjectSpecificAction(mo.getApWithOptions());
		assertNotNull(projectSpecificAction);
	}

	@Test
	public void testGetUrlName() {
		final ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithOptions());
		assertTrue(psa.getUrlName() != null);
	}

	@Test
	public void testShowAvgGraph() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
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
		final ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		psa.graphDataExists();
	}

	@Test
	public void testGetErrGraph() {
		final ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		final NeoLoadGraph g = psa.getErrGraph();
		assertTrue(g != null);
	}

	@Test
	public void testGetAvgGraph() {
		final ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		final NeoLoadGraph g = psa.getAvgGraph();
		assertTrue(g != null);
	}

	@SuppressWarnings("null")
	@Test
	public void testGetErrGraph2() throws IOException {
		final AbstractProject<?,? extends AbstractBuild<?,?>> ap = mo.getApWithOptions();

		final RunList<AbstractBuild<?,?>> rl = (RunList<AbstractBuild<?, ?>>) ap.getBuilds();
		// add the same build to the project multiple times
		final AbstractBuild<?, ?> abstractBuild = mo.getAbstractBuild();
		rl.add(abstractBuild);
		rl.add(abstractBuild);
		rl.add(abstractBuild);
		Mockito.when(ap.getBuilds()).thenReturn(rl);

		final List<Run<?, ?>.Artifact> artifacts = abstractBuild.getArtifacts();
		for (final Run<?, ?>.Artifact a: artifacts) {
			NeoResultsActionTest.setArtifactFileTimetoAfterBuildTime(abstractBuild, a);

			if ("xml".equalsIgnoreCase(FilenameUtils.getExtension(a.getFileName()))) {
				String contents = FileUtils.readFileToString(a.getFile());
				if (contents.contains("start=\"")) {
					final String replacementDate =
							new SimpleDateFormat(NeoResultsActionTest.STANDARD_TIME_FORMAT).format(
									abstractBuild.getTimestamp().getTimeInMillis() + TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS));
					contents = contents.replaceAll(Pattern.quote("start=\"" + MockObjects.getStartDateInXmlFile()),
							"std_start_time=\"" + replacementDate + "\" dontUse_start=\"dont use me");
					FileUtils.write(a.getFile(), contents, "UTF-8");
				}
			}
		}

		final ProjectSpecificAction psa = new ProjectSpecificAction(ap);
		psa.refreshGraphData();
		final NeoLoadGraph g = psa.getErrGraph();
		assertTrue(g != null);
		assertTrue(g.getyAxisLabel().toLowerCase().contains("error rate"));
	}

	@SuppressWarnings("null")
	@Test
	public void testGetAvgGraph2() throws IOException {
		final AbstractProject<?,? extends AbstractBuild<?,?>> ap = mo.getApWithOptions();

		final RunList<AbstractBuild<?,?>> rl = (RunList<AbstractBuild<?, ?>>) ap.getBuilds();
		// add the same build to the project multiple times
		final AbstractBuild<?, ?> abstractBuild = mo.getAbstractBuild();
		rl.add(abstractBuild);
		rl.add(abstractBuild);
		rl.add(abstractBuild);
		Mockito.when(ap.getBuilds()).thenReturn(rl);

		final List<Run<?, ?>.Artifact> artifacts = abstractBuild.getArtifacts();
		for (final Run<?, ?>.Artifact a: artifacts) {
			NeoResultsActionTest.setArtifactFileTimetoAfterBuildTime(abstractBuild, a);

			if ("xml".equalsIgnoreCase(FilenameUtils.getExtension(a.getFileName()))) {
				String contents = FileUtils.readFileToString(a.getFile());
				if (contents.contains("start=\"")) {
					final String replacementDate =
							new SimpleDateFormat(NeoResultsActionTest.STANDARD_TIME_FORMAT).format(
									abstractBuild.getTimestamp().getTimeInMillis() + TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS));
					contents = contents.replaceAll(Pattern.quote("start=\"" + MockObjects.getStartDateInXmlFile()),
							"std_start_time=\"" + replacementDate + "\" dontUse_start=\"dont use me");
					FileUtils.write(a.getFile(), contents, "UTF-8");
				}
			}
		}
		final ProjectSpecificAction psa = new ProjectSpecificAction(ap);
		psa.refreshGraphData();
		final NeoLoadGraph g = psa.getAvgGraph();
		assertTrue(g != null);
		assertTrue(g.getyAxisLabel().toLowerCase().contains("avg") || g.getyAxisLabel().toLowerCase().contains("average"));
	}

	@Test
	public void testFindXmlResultsFileValidFileHasNoCorrespondingDate() throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
		final AbstractBuild<?, ?> abstractBuild = mo.getAbstractBuild();

		NeoLoadReportDoc result = ProjectSpecificAction.findXMLResultsFile(abstractBuild);
		assertNull("should have an invalid date", result);

		final List<Run<?, ?>.Artifact> artifacts = abstractBuild.getArtifacts();
		for (final Run<?, ?>.Artifact a: artifacts) {
			NeoResultsActionTest.setArtifactFileTimetoAfterBuildTime(abstractBuild, a);

			if ("xml".equalsIgnoreCase(FilenameUtils.getExtension(a.getFileName()))) {
				String contents = FileUtils.readFileToString(a.getFile());
				if (contents.contains("start=\"")) {
					final String replacementDate =
							new SimpleDateFormat(NeoResultsActionTest.STANDARD_TIME_FORMAT).format(
									abstractBuild.getTimestamp().getTimeInMillis() + TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS));
					contents = contents.replaceAll(Pattern.quote("start=\"" + MockObjects.getStartDateInXmlFile()),
							"std_start_time=\"" + replacementDate + "\" dontUse_start=\"dont use me");
					FileUtils.write(a.getFile(), contents, "UTF-8");
				}
			}
		}

		result = ProjectSpecificAction.findXMLResultsFile(abstractBuild);
		assertNotNull("should have a valid date", result);
	}

	@Test
	public void testGetIconFileName() {
		final ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		psa.getIconFileName();
	}

	@Test
	public void testGetDisplayName() {
		final ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		psa.getDisplayName();
	}

}
