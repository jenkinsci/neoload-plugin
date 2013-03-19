package org.jenkinsci.plugins.neoload_integration;

import hudson.model.Action;
import hudson.model.Descriptor.FormException;

import java.util.Collection;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.neoload_integration.ProjectSpecificActionFactory.DescriptorImplPSA;
import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;

public class ProjectSpecificActionFactoryTest extends TestCase {
	
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
	public void testCreateForAbstractProject() {
		ProjectSpecificActionFactory psa = new ProjectSpecificActionFactory();
		Collection<? extends Action> c = psa.createFor(mo.getApWithOptions());
		assertFalse(c.isEmpty());
		assertTrue(c.size() == 1);
	}

	@Test
	public void testGetDescriptor() throws FormException {
		ProjectSpecificActionFactory psa = new ProjectSpecificActionFactory();
		DescriptorImplPSA d = (DescriptorImplPSA) psa.getDescriptor();
		assertNotNull(d);
		
		d.getDisplayName();
		d.newInstance((StaplerRequest)null, (JSONObject)null);
		d.configure((StaplerRequest)null, (JSONObject)null);
		DescriptorImplPSA.isShowGraph();
	}

}
