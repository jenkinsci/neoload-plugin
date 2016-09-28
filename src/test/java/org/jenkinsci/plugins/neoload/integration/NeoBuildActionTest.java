package org.jenkinsci.plugins.neoload.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.neoload.integration.supporting.CollabServerInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.MockObjects;
import org.jenkinsci.plugins.neoload.integration.supporting.NTSServerInfo;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.mockito.Mockito;

import hudson.Launcher;
import jenkins.model.Jenkins;

public class NeoBuildActionTest extends HudsonTestCase {

	/** Mock project for testing. */
	private MockObjects mo = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		mo = new MockObjects();
	}

	@Test
	public void testPrepareCommandLineBasic() {
		final NTSServerInfo ntssi = new NTSServerInfo("uniqueID", "http://url.com:8080", "loginUser", "loginPassword", "Label 1", "collabPath", "licenseID");
		final NeoBuildAction neoBuildAction = 
				new NeoBuildAction("c:/NeoLoad/executable", 
				"shared-project-type", // project type - local or shared. 
				"reportTypeDefault", // report type
				"c:/local_Project_File.prj", 
				"Shared_Project_Name", "Scenario_Name",
				"c:/htmlReport.html", "c:/xmlReport.xml", "c:/pdfReport.pdf", "c:/junitReport.xml", 
				false, // display the GUI
				"test result name", "test description", 
				"shared-license-type", // license type - local or shared. 
				"50", // VU count for license
				"1", // license hours
				"", // custom command line options
				true, // publish test results
				ntssi, ntssi, // shared project server, license server.
				true, // show trend average response
				true, // show trend error rate
				null); // graph info

		Launcher launcher = Mockito.mock(Launcher.class);
		final String cl = neoBuildAction.prepareCommandLine(launcher).toString();
		
		// "executable" -checkoutProject "SharedProjectName" -NTS "http://url.com:8080" -NTSLogin "loginUser:PASSWORD" 
		// -NTSCollabPath "collabPath" -publishTestResult -launch "ScenarioName" -testResultName "test result name" 
		// -description "test description" -leaseLicense "licenseID:50:1" 
		// -report "c:/htmlReport.html,c:/xmlReport.xml,c:/pdfReport.pdf" -SLAJUnitResults "c:/junitReport.xml" -noGUI
		assertTrue("the command line should contain the neoload executable", cl.contains(neoBuildAction.getExecutable()));

		assertTrue("shared project name should be there", cl.contains(neoBuildAction.getSharedProjectName()));
		assertTrue("local project file should not be there", !cl.contains(neoBuildAction.getLocalProjectFile()));

		assertTrue("we said yes to publish", cl.contains("publishTestResult"));
		
		assertTrue("when leasing a license the license ID should be there", cl.contains(ntssi.getLicenseID()));
	}

	@Test
	public void testPrepareCommandLineNTSAndThirdPartySVNServer() {
		final CollabServerInfo csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "COLLAB_loginUser", 
				"COLLAB_loginPassword", "Label", "COLLAB_privateKey", "COLLAB_passphrase");
		final NTSServerInfo ntssi = new NTSServerInfo("NTS_uniqueID", "http://NTS.com:8080", "NTS_loginUser", 
				"NTS_loginPassword", "Label", "NTS_collabPath", "NTS_licenseID");
		final NeoBuildAction neoBuildAction = 
				new NeoBuildAction("c:/NeoLoad/executable", 
				"shared-project-type", // project type - local or shared. 
				"reportTypeDefault", // report type
				"c:/local_Project_File.prj", 
				"Shared_Project_Name", "Scenario_Name",
				"c:/htmlReport.html", "c:/xmlReport.xml", "c:/pdfReport.pdf", "c:/junitReport.xml", 
				false, // display the GUI
				"test result name", "test description", 
				"shared-license-type", // license type - local or shared. 
				"50", // VU count for license
				"1", // license hours
				"", // custom command line options
				true, // publish test results
						csi, ntssi, // shared project server, license server.
				true, // show trend average response
				true, // show trend error rate
				null); // graph info

		Launcher launcher = Mockito.mock(Launcher.class);
		final String cl = neoBuildAction.prepareCommandLine(launcher).toString();

		assertTrue("The third party server should be used for checking out the project, not NTS.", 
				cl.contains(csi.getUrl()));

		assertTrue("The third party server should be used for checking out the project, not NTS.", 
				!cl.contains(ntssi.getCollabPath()));
	}

	@Test
	public void testPrepareCommandLineExistingLicense() {
		final CollabServerInfo csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "COLLAB_loginUser", 
				"COLLAB_loginPassword", "Label", "COLLAB_privateKey", "COLLAB_passphrase");
		final NTSServerInfo ntssi = new NTSServerInfo("NTS_uniqueID", "http://NTS.com:8080", "NTS_loginUser", 
				"NTS_loginPassword", "Label", "NTS_collabPath", "NTS_licenseID");
		final NeoBuildAction neoBuildAction = 
				new NeoBuildAction("c:/NeoLoad/executable", 
				"shared-project-type", // project type - local or shared. 
				"reportTypeDefault", // report type
				"c:/local_Project_File.prj", 
				"Shared_Project_Name", "Scenario_Name",
				"c:/htmlReport.html", "c:/xmlReport.xml", "c:/pdfReport.pdf", "c:/junitReport.xml", 
				false, // display the GUI
				"test result name", "test description", 
				"local", // license type - local or shared.
				"50", // VU count for license
				"1", // license hours
				"", // custom command line options
				true, // publish test results
				ntssi, null, // shared project server, license server.
				true, // show trend average response
				true, // show trend error rate
				null); // graph info

		Launcher launcher = Mockito.mock(Launcher.class);
		final String cl = neoBuildAction.prepareCommandLine(launcher).toString();

		assertTrue("we used a LOCAL license type so this shouldn't be there.", !cl.contains(ntssi.getLicenseID()));
	}

	@Test
	public void testPrepareCommandLineLocalProject() {
		final CollabServerInfo csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "COLLAB_loginUser", 
				"COLLAB_loginPassword", "Label", "COLLAB_privateKey", "COLLAB_passphrase");
		final NTSServerInfo ntssi = new NTSServerInfo("NTS_uniqueID", "http://NTS.com:8080", "NTS_loginUser", 
				"NTS_loginPassword", "Label", "NTS_collabPath", "NTS_licenseID");
		final NeoBuildAction neoBuildAction = 
				new NeoBuildAction("c:/NeoLoad/executable", 
				"local", // project type - local or shared.
				"reportTypeDefault", // report type
				"c:/local_Project_File.prj", 
				"Shared_Project_Name", "Scenario_Name",
				"c:/htmlReport.html", "c:/xmlReport.xml", "c:/pdfReport.pdf", "c:/junitReport.xml", 
				false, // display the GUI
				"test result name", "test description", 
				"shared-license-type", // license type - local or shared. 
				"50", // VU count for license
				"1", // license hours
				"", // custom command line options
				true, // publish test results
				ntssi, ntssi, // shared project server, license server.
				true, // show trend average response
				true, // show trend error rate
				null); // graph info

		Launcher launcher = Mockito.mock(Launcher.class);
		final String cl = neoBuildAction.prepareCommandLine(launcher).toString();

		assertTrue("there must be a reference to the local project file", cl.contains(neoBuildAction.getLocalProjectFile()));
		assertTrue("the shared project name must not be there for a local project", 
				!cl.contains(neoBuildAction.getSharedProjectName()));
	}

	@Test
	public void testSetupCollabLogin() {
		CollabServerInfo csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "COLLAB_loginUser", 
				"COLLAB_loginPassword", "Label", "COLLAB_privateKey", "COLLAB_passphrase");
		final NTSServerInfo ntssi = new NTSServerInfo("NTS_uniqueID", "http://NTS.com:8080", "NTS_loginUser", 
				"NTS_loginPassword", "Label", "NTS_collabPath", "NTS_licenseID");
		final NeoBuildAction neoBuildAction = 
				new NeoBuildAction("c:/NeoLoad/executable", 
				"shared-project-type", // project type - local or shared. 
				"reportTypeDefault", // report type
				"c:/local_Project_File.prj", 
				"Shared_Project_Name", "Scenario_Name",
				"c:/htmlReport.html", "c:/xmlReport.xml", "c:/pdfReport.pdf", "c:/junitReport.xml", 
				false, // display the GUI
				"test result name", "test description", 
				"shared-license-type", // license type - local or shared. 
				"50", // VU count for license
				"1", // license hours
				"", // custom command line options
				true, // publish test results
				ntssi, ntssi, // shared project server, license server.
				true, // show trend average response
				true, // show trend error rate
				null); // graph info

		final Map<String, String> hashedPasswords = new HashMap<String, String>();
		hashedPasswords.put(csi.getLoginPassword(), csi.getLoginPassword());

		
		// -CollabLogin "<login>:<hashed password>:<private key>:<hashed passphrase>"
		csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "COLLAB_loginUser", 
				"COLLAB_loginPassword", "Label", "COLLAB_privateKey", "COLLAB_passphrase");
		String result = neoBuildAction.setupCollabLogin(hashedPasswords, csi).toString();
		assertTrue("all fields should be present",
				result.contains(csi.getLoginUser() + ":" + csi.getLoginPassword() + ":" + csi.getPrivateKey() + ":" + 
						csi.getPassphrase()));
				
		// -CollabLogin "<login>:<hashed password>:<private key>"
		csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "COLLAB_loginUser", 
				"COLLAB_loginPassword", "Label", "COLLAB_privateKey", null);
		result = neoBuildAction.setupCollabLogin(hashedPasswords, csi).toString();
		assertTrue("too many fields",
				!result.contains(csi.getLoginUser() + ":" + csi.getLoginPassword() + ":" + csi.getPrivateKey() + ":" + 
						csi.getPassphrase()));
		assertTrue(result.contains(csi.getLoginUser() + ":" + csi.getLoginPassword() + ":" + csi.getPrivateKey()));

		// -CollabLogin "<login>:<hashed password>"
		csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "COLLAB_loginUser", 
				"COLLAB_loginPassword", "Label", null, "");
		result = neoBuildAction.setupCollabLogin(hashedPasswords, csi).toString();
		assertTrue("too many fields",
				!result.contains(csi.getLoginUser() + ":" + csi.getLoginPassword() + ":" + csi.getPrivateKey() + ":" + 
						csi.getPassphrase()));
		assertTrue("too many fields", 
				!result.contains(csi.getLoginUser() + ":" + csi.getLoginPassword() + ":" + csi.getPrivateKey()));
		assertTrue(result.contains(csi.getLoginUser() + ":" + csi.getLoginPassword()));

		// -CollabLogin "<private key>:<hashed passphrase>"
		csi = new CollabServerInfo("COLLAB_uniqueID", "COLLAB_url", "", 
				"", "Label", "COLLAB_privateKey", "COLLAB_passphrase");
		result = neoBuildAction.setupCollabLogin(hashedPasswords, csi).toString();
		assertTrue("too many fields",
				!result.contains(csi.getLoginUser() + ":" + csi.getLoginPassword() + ":" + csi.getPrivateKey() + ":" + 
						csi.getPassphrase()));
		assertTrue("too many fields",
				!result.contains(csi.getLoginPassword() + ":" + csi.getPrivateKey() + ":" + csi.getPassphrase()));
		assertTrue(result.contains(csi.getPrivateKey() + ":" + csi.getPassphrase()));
	}
}
