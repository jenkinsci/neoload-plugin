/**
 * 
 */
package org.jenkinsci.plugins.neoload_integration.supporting;

import java.awt.Color;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

import junit.framework.TestCase;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mockito;

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
		StaplerResponse rsp = Mockito.mock(StaplerResponse.class);
		ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);
		
		Mockito.when(rsp.getOutputStream()).thenReturn(sos);
		
		nlg.doPng(null, rsp);
	}
	
	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadGraph#createGraph()}.
	 */
	@Test
	public void testCreateGraph() {
		JFreeChart chart = nlg.createGraph();
		assertNotNull(chart.getPlot());
	}

}
