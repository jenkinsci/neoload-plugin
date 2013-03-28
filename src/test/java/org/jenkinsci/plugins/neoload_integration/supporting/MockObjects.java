package org.jenkinsci.plugins.neoload_integration.supporting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
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
import java.util.List;

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
	 * @throws FileNotFoundException */
	public MockObjects() throws FileNotFoundException, IOException {
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

		// add artifact to build.getArtifacts
		List<Artifact> artifacts = createArtifacts();
		when(abstractBuild.getArtifacts()).thenReturn(artifacts);

		RunList rl = new RunList<Run>();
		rl.add(abstractBuild);
		when(apWithOptions.getBuilds()).thenReturn(rl);
		when(apWithoutOptions.getBuilds()).thenReturn(rl);
	}

	/** Create artifacts out of all files in the zip file.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private List<Artifact> createArtifacts() throws FileNotFoundException, IOException {
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

	/** @param apWithOptions the apWithOptions to set */
	public void setApWithOptions(AbstractProject apWithOptions) {
		this.apWithOptions = apWithOptions;
	}

	/** @param apWithoutOptions the apWithoutOptions to set */
	public void setApWithoutOptions(AbstractProject apWithoutOptions) {
		this.apWithoutOptions = apWithoutOptions;
	}

	/** @param publisherWithNeoOptions the publisherWithNeoOptions to set */
	public void setPublisherWithNeoOptions(Publisher publisherWithNeoOptions) {
		this.publisherWithNeoOptions = publisherWithNeoOptions;
	}

	/** @param abstractBuild the abstractBuild to set */
	public void setAbstractBuild(AbstractBuild abstractBuild) {
		this.abstractBuild = abstractBuild;
	}

	/** @return the artifacts */
	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	/** @param artifacts the artifacts to set */
	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

}
