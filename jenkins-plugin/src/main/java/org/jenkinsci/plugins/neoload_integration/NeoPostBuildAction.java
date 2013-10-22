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
package org.jenkinsci.plugins.neoload_integration;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.Serializable;

import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadPluginOptions;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This class adds the link to the html report to a build after the build has
 * completed. Extend Recorder instead of Notifier for Hudson compatability.
 * 
 * This class also holds the settings chosen by the user for the plugin.
 */
@SuppressWarnings("unchecked")
public class NeoPostBuildAction extends Recorder implements NeoLoadPluginOptions, Serializable {

	/** Generated. */
	private static final long serialVersionUID = -7633475904852232979L;

	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendAverageResponse;

	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendErrorRate;

	@DataBoundConstructor
	public NeoPostBuildAction(final boolean showTrendAverageResponse, final boolean showTrendErrorRate) {
		// this method and the annotation @DataBoundConstructor are required for jenkins 1.393 even if no params are passed in.
		this.showTrendAverageResponse = showTrendAverageResponse;
		this.showTrendErrorRate = showTrendErrorRate;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
		// (at the end of a build) add the html results link to the build if it's not already there.
		NeoResultsAction.addActionIfNotExists(build, true);

		return true;
	}

	@Extension(optional = true)
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public DescriptorImpl() {
			super(NeoPostBuildAction.class);
		}

		@Override
		public String getDisplayName() {
			return "Incorporate NeoLoad Results";
		}

		@Override
		public boolean isApplicable(
				@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
			return true;
		}
	}

	/** @return the showTrendAverageResponse */
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	/** @return the showTrendErrorRate */
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}
}
