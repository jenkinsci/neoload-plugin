package org.jenkinsci.plugins.neoload_integration.supporting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.Run.Artifact;
import hudson.tasks.Publisher;
import hudson.tasks.ArtifactArchiver;
import hudson.util.DescribableList;
import hudson.util.RunList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.plugins.neoload_integration.NeoResultsAction;

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
	private Artifact artifact = null;

	/** Constructor. 
	 * @throws IOException 
	 * @throws FileNotFoundException */
	public MockObjects() throws FileNotFoundException, IOException {
		// abstract project without options
		List<Publisher> publishersWithoutNeoOptions = new ArrayList<>();
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		publishersWithoutNeoOptions.add(mock(Publisher.class));
		
		DescribableList describableListWithoutNeoOptions = mock(DescribableList.class);
		when(describableListWithoutNeoOptions.iterator()).thenReturn(publishersWithoutNeoOptions.iterator());
		
		apWithoutOptions = mock(AbstractProject.class, "AbstractProject no plugin options");
		when(apWithoutOptions.getPublishersList()).thenReturn(describableListWithoutNeoOptions);
		when(apWithoutOptions.getDisplayName()).thenReturn("projectNameAPWithoutOptions");
		
		// abstract project with options
		List<Publisher> publishersWithNeoOptions = new ArrayList<>();
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
		
		RunList rl = new RunList<>();
		rl.add(abstractBuild);
		when(apWithOptions.getBuilds()).thenReturn(rl);
		when(apWithoutOptions.getBuilds()).thenReturn(rl);
		
		// artifact
		artifact = mock(Artifact.class);
		
		// create new test files
		URL url = MockObjects.class.getResource("neoload-report.zip");
		ZipUtils.unzip(url.getFile(), new File(url.getFile()).getParent());
		
		// add artifact to build.getArtifacts
		// add artifact.getFileName, artifact.getFile, artifact.getHref
		url = this.getClass().getResource("myReport.html");
		File f = new File(url.getFile());
		when(artifact.getFile()).thenReturn(f);
		when(artifact.getFileName()).thenReturn(f.getName());
		when(artifact.getHref()).thenReturn("http://href.url");
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

	/** @return the artifact */
	public Artifact getArtifact() {
		return artifact;
	}

	/** @param artifact the artifact to set */
	public void setArtifact(Artifact artifact) {
		this.artifact = artifact;
	}


}
