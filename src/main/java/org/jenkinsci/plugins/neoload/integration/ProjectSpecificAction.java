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
package org.jenkinsci.plugins.neoload.integration;

import com.google.common.collect.Lists;
import hudson.Functions;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;
import hudson.util.IOUtils;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import org.jenkinsci.plugins.neoload.integration.supporting.GraphOptionsInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.NeoLoadPluginOptions;
import org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Along with the jelly file and the Factory class, this class adds the two trend graphs to a job page.
 */
public class ProjectSpecificAction implements ProminentProjectAction {

	/**
	 * A link to the Jenkins job.
	 */
	private final AbstractProject<?, ?> project;

	private final NeoLoadPluginOptions npo;

	private final File picturesFolder;


	/**
	 * Instantiates a new Project specific action.
	 *
	 * @param project the project
	 */
	public ProjectSpecificAction(final AbstractProject<?, ?> project) {
		this.project = project;
		npo = PluginUtils.getPluginOptions(project);
		picturesFolder = PluginUtils.getPicturesFolder(project);
	}

	/**
	 * Gets icon file name.
	 *
	 * @return the icon file name
	 */
	@Override
	public String getIconFileName() {
		return hasGraph() ? "/plugin/neoload-jenkins-plugin/images/refresh.png" : null;
	}

	/**
	 * Gets display name.
	 *
	 * @return the display name
	 */
	@Override
	public String getDisplayName() {
		return hasGraph() ? "Refresh NeoLoad trends" : null;
	}


	private boolean hasGraph() {
		if (npo == null) {
			return false;
		}
		if (picturesFolder != null &&
				picturesFolder.exists() &&
				picturesFolder.isDirectory() &&
				picturesFolder.list().length > 0
				) {
			return true;
		}
		final List<GraphOptionsInfo> graphOptionsInfo = npo.getGraphOptionsInfo();
		return graphOptionsInfo != null && (!graphOptionsInfo.isEmpty()) ||
				npo.isShowTrendAverageResponse() ||
				npo.isShowTrendErrorRate();
	}


	/**
	 * This corresponds to the url of the image files displayed on the job page.
	 *
	 * @return the url name
	 * @see hudson.model.Action#getUrlName() hudson.model.Action#getUrlName()
	 */
	public String getUrlName() {
		return "neoload";
	}


	/**
	 * Scan build report.
	 */
	public void scanBuildReport(){
		if(npo == null || !npo.isScanAllBuilds()){
			return;
		}
		for(Run run : project.getBuilds()){
			if(run.getAction(NeoResultsAction.class)==null){
				if(run instanceof AbstractBuild) {
					run.addAction(new NeoResultsAction((AbstractBuild<?, ?>) run,null,null));
				}
			}
		}
	}

	/**
	 * Gets charts name.
	 *
	 * @return list of trends inside neoload-trend directory
	 */
	public List<String> getChartsName() {
		scanBuildReport();
		if (PluginUtils.GRAPH_LOCK.tryLock(project)) {
			try {
				List<String> chartName = new ArrayList<>();

				if (picturesFolder.isDirectory()) {
					final File[] files = picturesFolder.listFiles(new FileFilter() {
						@Override
						public boolean accept(final File pathname) {
							return pathname.toString().toLowerCase().endsWith(".png");
						}
					});
					if (files != null) {
						Arrays.sort(files);
						for (File pictureFile : files) {
							chartName.add("neoload/img/" + pictureFile.getName());
						}
					}
				}
				return chartName;
			} finally {
				PluginUtils.GRAPH_LOCK.unlock(project);
			}
		} else {
			return Lists.newArrayList(Jenkins.getInstance().getRootUrl() + Jenkins.RESOURCE_PATH + "/images/spinner.gif");
		}
	}

	/**
	 * Build graphs.
	 */
	public void buildGraphs() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				PluginUtils.buildGraph(picturesFolder, npo, project);
			}
		}).start();

	}


	/**
	 * Gets graph options info.
	 *
	 * @return the graphOptionsInfo
	 */
	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		return npo.getGraphOptionsInfo();
	}

	/**
	 * Is neoload build job boolean.
	 *
	 * @return the boolean
	 */
//Detect if neoload plugin is added
	public boolean isNeoloadBuildJob() {
		return npo != null;

	}

	/**
	 * Gets project.
	 *
	 * @return the project
	 */
	public AbstractProject<?, ?> getProject() {
		return project;
	}

	/**
	 * Used from javascript to check lock state.
	 *
	 * @param req the req
	 * @param rsp the rsp
	 * @throws IOException the io exception
	 */
	public void doChecklock(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
		final ServletOutputStream outputStream = rsp.getOutputStream();
		final String resp = PluginUtils.GRAPH_LOCK.isLocked(project) ? "locked" : "free";
		outputStream.println(resp);
		outputStream.close();
	}


	/**
	 * This is the method Hudson uses when a dynamic png is referenced in a jelly file.
	 *
	 * @param imageName the image name
	 * @return img
	 */
	public Png getImg(String imageName) {
		return new Png(new File(picturesFolder, imageName));
	}

	/**
	 * The type Png.
	 */
	public static class Png {
		private final File file;

		/**
		 * Instantiates a new Png.
		 *
		 * @param file the file
		 */
		Png(File file) {
			this.file = file;
		}

		/**
		 * This is the method Hudson uses when a dynamic png is referenced in a jelly file.
		 *
		 * @param req the req
		 * @param rsp the rsp
		 * @throws IOException the io exception
		 */
		public void doIndex(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
			rsp.setContentType("image/png");
			final ServletOutputStream os = rsp.getOutputStream();
			IOUtils.copy(file, os);
			os.close();
		}
	}
}