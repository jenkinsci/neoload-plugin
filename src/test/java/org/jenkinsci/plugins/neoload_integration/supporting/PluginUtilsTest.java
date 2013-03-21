/**
 * 
 */
package org.jenkinsci.plugins.neoload_integration.supporting;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.neotys.tools.unittest.UnitTests;

/**
 * @author ajohnson
 *
 */
public class PluginUtilsTest extends TestCase {
	
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
	public void testPluginUtils() throws ReflectiveOperationException {
		UnitTests.assertIsNotInstantiable(PluginUtils.class);
	}
	
	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils#getPluginOptions(hudson.model.AbstractProject)}.
	 */
	@Test
	public void testGetPluginOptions() {
		NeoLoadPluginOptions npo = PluginUtils.getPluginOptions(mo.getApWithoutOptions());
		assertTrue(npo == null);
		
		npo = PluginUtils.getPluginOptions(mo.getApWithOptions());
		assertTrue(npo == mo.getPublisherWithNeoOptions());
	}
	
}
