/*
 * Copyright (c) 2016, Neotys
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
package org.jenkinsci.plugins.neoload.integration.supporting;

import java.awt.Color;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mockito;

import junit.framework.TestCase;

/**
 * @author ajohnson
 *
 */
public class NeoLoadGraphTest extends TestCase {

	/** Holds graph data. */
	private DefaultCategoryDataset ds = null;

	/** A graph instance. */
	private NeoLoadGraph nlg = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		ds = new DefaultCategoryDataset();
		ds.addValue(1, "rowKey", "columnKey");
		ds.addValue(2, "rowKey", "columnKey");
		ds.addValue(3, "rowKey", "columnKey");
		ds.addValue(4, "rowKey", "columnKey");

		nlg = new NeoLoadGraph(ds, "Avg Resp Time (secs)", new Color(237, 184, 0));
	}

	/** Test another method.
	 * @throws IOException */
	@Test
	public void testDoPNG() throws IOException {
		final StaplerResponse rsp = Mockito.mock(StaplerResponse.class);
		final ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);

		Mockito.when(rsp.getOutputStream()).thenReturn(sos);

		nlg.doPng(null, rsp);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload.integration.supporting.NeoLoadGraph#createGraph()}.
	 */
	@Test
	public void testCreateGraph() {
		final JFreeChart chart = nlg.createGraph();
		assertNotNull(chart.getPlot());
	}

}
