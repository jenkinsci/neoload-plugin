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
import hudson.util.RunList;

import java.util.Map;

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
		AbstractBuild<?, ?> ab = mo.getAbstractBuild();
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
		
		RunList<AbstractBuild<?, ?>> rl = ap.getBuilds();
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
		
		RunList<AbstractBuild<?, ?>> rl = ap.getBuilds();
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
	public void testGetAvgGraphPoints() {
		AbstractProject ap = mo.getApWithOptions();
		
		RunList<AbstractBuild<?, ?>> rl = ap.getBuilds();
		// add the same build to the project multiple times
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		Mockito.when(ap.getBuilds()).thenReturn(rl);

		ProjectSpecificAction psa = new ProjectSpecificAction(ap);
		psa.refreshGraphData();
		Map<String, Float> points = psa.getAvgGraphPoints();
		assertTrue(points.size() > 0);
		
		AbstractBuild<?, ?> build = rl.get(0);
		Mockito.when(build.getDisplayName()).thenReturn("bob");
		points = psa.getAvgGraphPoints();
		assertTrue(points.get("bob") != null);
	}

	@Test
	public void testGetErrGraphPoints() {
		AbstractProject ap = mo.getApWithOptions();
		
		RunList<AbstractBuild<?, ?>> rl = ap.getBuilds();
		// add the same build to the project multiple times
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		rl.add(mo.getAbstractBuild());
		Mockito.when(ap.getBuilds()).thenReturn(rl);

		ProjectSpecificAction psa = new ProjectSpecificAction(ap);
		psa.refreshGraphData();
		Map<String, Float> points = psa.getErrGraphPoints();
		assertTrue(points.size() > 0);
		
		AbstractBuild<?, ?> build = rl.get(0);
		Mockito.when(build.getDisplayName()).thenReturn("bob");
		points = psa.getErrGraphPoints();
		assertTrue(points.get("bob") != null);
	}

	@Test
	public void testGetIconFileName() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		assertTrue(null == psa.getIconFileName());
	}

	@Test
	public void testGetDisplayName() {
		ProjectSpecificAction psa = new ProjectSpecificAction(mo.getApWithoutOptions());
		assertTrue(null != psa.getDisplayName());
	}

}
