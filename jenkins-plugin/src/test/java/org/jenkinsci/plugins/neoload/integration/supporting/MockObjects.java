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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.util.ReflectionUtils;
import org.jenkinsci.plugins.neoload.integration.NeoBuildAction;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Run.Artifact;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.util.RunList;

public class MockObjects {

	/** Mock project for testing. */
	private final Project<?,? extends AbstractBuild<?,?>> apWithOptions;

	/** Mock project for testing. */
	private final Project<?,? extends AbstractBuild<?,?>> apWithoutOptions;

	/** Mock object for testing. */
	private final Publisher publisherWithNeoOptions;

	/** Mock object for testing. */
	private final AbstractBuild abstractBuild;
	
	private final NeoBuildAction neoBuildAction;

	private final Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact reportFileArtifact;
	private final Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact reportFileArtifactXml;

	/** The date the test started in the report file. */
	private static final String START_DATE_IN_HTML_FILE = "Mar 18, 2013 11:15:39 AM";

	/** The date the test started in the report file. */
	private static final String START_DATE_IN_XML_FILE = "Mar 20, 2013 3:00:56 PM";


	/** Constructor.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws IllegalAccessException */
	public MockObjects() throws FileNotFoundException, IOException, IllegalAccessException {
		// abstract project without options
		final List<Publisher> publishersWithoutNeoOptions = new ArrayList<Publisher>();
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));

		final DescribableList<Publisher, Descriptor<Publisher>> describableListWithoutNeoOptions = mock(DescribableList.class);
		when(describableListWithoutNeoOptions.iterator()).thenReturn(publishersWithoutNeoOptions.iterator());

		apWithoutOptions = mock(Project.class, "Project no plugin options");
		when(apWithoutOptions.getPublishersList()).thenReturn(describableListWithoutNeoOptions);
		when(apWithoutOptions.getDisplayName()).thenReturn("projectNameAPWithoutOptions");

		// abstract project with options
		final List<Publisher> publishersWithNeoOptions = new ArrayList<Publisher>();
		publishersWithNeoOptions.addAll(publishersWithoutNeoOptions);
		publisherWithNeoOptions = mock(Publisher.class, withSettings().extraInterfaces(NeoLoadPluginOptions.class));
		final NeoLoadPluginOptions nlpo = (NeoLoadPluginOptions) publisherWithNeoOptions;
		when(nlpo.isShowTrendAverageResponse()).thenReturn(true);
		when(nlpo.isShowTrendErrorRate()).thenReturn(true);
		publishersWithNeoOptions.add(publisherWithNeoOptions);

		final DescribableList<Publisher, Descriptor<Publisher>> describableListWithNeoOptions = mock(DescribableList.class);
		when(describableListWithNeoOptions.iterator()).thenReturn(publishersWithNeoOptions.iterator());
		final List<Builder> builders = new ArrayList<Builder>();
		final NTSServerInfo ntssi = new NTSServerInfo("uniqueID", "http://url.com:8080", "loginUser", "loginPassword", "Label", "collabPath", "licenseID");
		neoBuildAction = 
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
		builders.add(neoBuildAction);

		apWithOptions = mock(Project.class, "AbstractProject with plugin options");
		when(apWithOptions.getPublishersList()).thenReturn(describableListWithNeoOptions);
		when(apWithOptions.getBuilders()).thenReturn(builders);
		when(apWithOptions.getDisplayName()).thenReturn("projectNameAPWithOptions");

		// abstract build
		abstractBuild = mock(AbstractBuild.class);
		when(abstractBuild.getProject()).thenReturn(apWithOptions);
		when(abstractBuild.getResult()).thenReturn(Result.SUCCESS);

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(abstractBuild.getTimestamp()).thenReturn(cal);
		// the test lasted 10 minutes
		final long buildDuration = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS) * 60;
		when(abstractBuild.getDuration()).thenReturn(buildDuration);

		// add artifact to build.getArtifacts
		final List<Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact> artifacts = createArtifacts();
		when(abstractBuild.getArtifacts()).thenReturn(artifacts);

		Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact localReportFileArtifact = null;
		Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact localXmlReportFileArtifact = null;
		for (final Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact a: artifacts) {
			if (a.getFile().getName().contains("eport.html")) {
				localReportFileArtifact = a;
			} else if (a.getFile().getName().contains("eport.xml")) {
				localXmlReportFileArtifact = a;
			}
		}
		reportFileArtifact = localReportFileArtifact;
		reportFileArtifactXml = localXmlReportFileArtifact;

		// prepare the build for: build.getWorkspace().toURI().getPath() + File.separatorChar + artifact.toString();
		final File f = new File(artifacts.get(0).getFile().getParent());
		ReflectionUtils.setVariableValueInObject(abstractBuild, "workspace", f.getParent());
		when(abstractBuild.getBuiltOn()).thenReturn(Hudson.getInstance());

		final File artifactsDir = artifacts.get(0).getFile().getParentFile();
		when(abstractBuild.getArtifactsDir()).thenReturn(artifactsDir);

		final RunList rl = new RunList();
		rl.add(abstractBuild);
		when(apWithOptions.getBuilds()).thenReturn(rl);
		when(apWithoutOptions.getBuilds()).thenReturn(rl);
	}

	/** Create artifacts out of all files in the zip file.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private static List<Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact> createArtifacts() throws FileNotFoundException, IOException, IllegalAccessException {
		// create new test files
		final URL url = MockObjects.class.getResource("neoload-report.zip");
		final List<File> createdFiles = ZipUtilities.unzip(url.getFile(), new File(url.getFile()).getParent());
		Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact a = null;
		final List<Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact> artifacts = new ArrayList<Run<? extends Job<?,?>, ? extends Run<?,?>>.Artifact>(createdFiles.size());

		for (final File f: createdFiles) {
			a = mock(Artifact.class);

			// add artifact.getFileName, artifact.getFile, artifact.getHref
			when(a.getFile()).thenReturn(f);
			when(a.getFileName()).thenReturn(f.getName());
			when(a.getHref()).thenReturn("http://href.url/" + f.getName());
			when(a.toString()).thenReturn(f.getName());
			ReflectionUtils.setVariableValueInObject(a, "relativePath", f.getName());

			artifacts.add(a);
		}

		return artifacts;
	}

	/** @return the apWithOptions */
	public AbstractProject<?, ?> getApWithOptions() {
		return apWithOptions;
	}

	/** @return the apWithoutOptions */
	public AbstractProject<?, ?> getApWithoutOptions() {
		return apWithoutOptions;
	}

	/** @return the publisherWithNeoOptions */
	public Publisher getPublisherWithNeoOptions() {
		return publisherWithNeoOptions;
	}

	/** @return the abstractBuild */
	public AbstractBuild<?, ?> getAbstractBuild() {
		return abstractBuild;
	}

	/** @return the reportFileArtifact */
	public Run<?, ?>.Artifact getReportFileArtifact() {
		return reportFileArtifact;
	}

	/** @return the reportFileArtifactXml */
	public Run<?, ?>.Artifact getReportFileArtifactXml() {
		return reportFileArtifactXml;
	}

	/** @return the startDateInFile */
	public static String getStartDateInHtmlFile() {
		return START_DATE_IN_HTML_FILE;
	}

	/** @return the startDateInXmlFile */
	public static String getStartDateInXmlFile() {
		return START_DATE_IN_XML_FILE;
	}
	
	public NeoBuildAction getNeoBuildAction() {
		return neoBuildAction;
	}

}
