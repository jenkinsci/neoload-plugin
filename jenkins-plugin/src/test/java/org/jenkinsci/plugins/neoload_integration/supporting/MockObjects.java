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
package org.jenkinsci.plugins.neoload_integration.supporting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import hudson.FilePath;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.Run.Artifact;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.util.RunList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.codehaus.plexus.util.ReflectionUtils;

public class MockObjects {
	
	/** Mock project for testing. */
	private AbstractProject apWithOptions = null;

	/** Mock project for testing. */
	private AbstractProject apWithoutOptions = null;

	/** Mock object for testing. */
	private Publisher publisherWithNeoOptions = null;
	
	/** Mock object for testing. */
	private AbstractBuild abstractBuild = null;
	
	/** Mock object for testing. */
	private List<Artifact> artifacts = null;

	/** Constructor. 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws IllegalAccessException */
	public MockObjects() throws FileNotFoundException, IOException, IllegalAccessException {
		// abstract project without options
		List<Publisher> publishersWithoutNeoOptions = new ArrayList<Publisher>();
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		
		DescribableList describableListWithoutNeoOptions = mock(DescribableList.class);
		when(describableListWithoutNeoOptions.iterator()).thenReturn(publishersWithoutNeoOptions.iterator());
		
		apWithoutOptions = mock(AbstractProject.class, "AbstractProject no plugin options");
		when(apWithoutOptions.getPublishersList()).thenReturn(describableListWithoutNeoOptions);
		when(apWithoutOptions.getDisplayName()).thenReturn("projectNameAPWithoutOptions");
		
		// abstract project with options
		List<Publisher> publishersWithNeoOptions = new ArrayList<Publisher>();
		publishersWithNeoOptions.addAll(publishersWithoutNeoOptions);
		publisherWithNeoOptions = mock(Publisher.class, withSettings().extraInterfaces(NeoLoadPluginOptions.class));
		NeoLoadPluginOptions nlpo = (NeoLoadPluginOptions) publisherWithNeoOptions;
		when(nlpo.isShowTrendAverageResponse()).thenReturn(true);
		when(nlpo.isShowTrendErrorRate()).thenReturn(true);
		publishersWithNeoOptions.add(publisherWithNeoOptions);

		DescribableList describableListWithNeoOptions = mock(DescribableList.class);
		when(describableListWithNeoOptions.iterator()).thenReturn(publishersWithNeoOptions.iterator());

		apWithOptions = mock(AbstractProject.class, "AbstractProject with plugin options");
		when(apWithOptions.getPublishersList()).thenReturn(describableListWithNeoOptions);
		when(apWithOptions.getDisplayName()).thenReturn("projectNameAPWithOptions");
		
		// abstract build
		abstractBuild = mock(AbstractBuild.class);
		when(abstractBuild.getProject()).thenReturn(apWithOptions);
		when(abstractBuild.getResult()).thenReturn(Result.SUCCESS);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		when(abstractBuild.getTimestamp()).thenReturn(cal);

		// add artifact to build.getArtifacts
		List<Artifact> artifacts = createArtifacts();
		when(abstractBuild.getArtifacts()).thenReturn(artifacts);
		
		// prepare the build for: build.getWorkspace().toURI().getPath() + File.separatorChar + artifact.toString();
		File f = new File(artifacts.get(0).getFile().getParent());
		FilePath fp = new FilePath(f);
		ReflectionUtils.setVariableValueInObject(abstractBuild, "workspace", f.getParent());
		when(abstractBuild.getBuiltOn()).thenReturn(Hudson.getInstance());
		
		final File artifactsDir = artifacts.get(0).getFile().getParentFile();
		when(abstractBuild.getArtifactsDir()).thenReturn(artifactsDir);
		

		RunList rl = new RunList<Run>();
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
	private static List<Artifact> createArtifacts() throws FileNotFoundException, IOException, IllegalAccessException {
		// create new test files
		URL url = MockObjects.class.getResource("neoload-report.zip");
		List<File> createdFiles = ZipUtils.unzip(url.getFile(), new File(url.getFile()).getParent());
		Artifact a = null;
		List<Artifact> artifacts = new ArrayList<Artifact>(createdFiles.size());
		
		for (File f: createdFiles) {
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
	public AbstractProject getApWithOptions() {
		return apWithOptions;
	}

	/** @return the apWithoutOptions */
	public AbstractProject getApWithoutOptions() {
		return apWithoutOptions;
	}

	/** @return the publisherWithNeoOptions */
	public Publisher getPublisherWithNeoOptions() {
		return publisherWithNeoOptions;
	}

	/** @return the abstractBuild */
	public AbstractBuild getAbstractBuild() {
		return abstractBuild;
	}

}
