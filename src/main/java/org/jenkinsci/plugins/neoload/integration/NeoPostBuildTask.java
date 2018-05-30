/*
 * Copyright (c) 2018, Neotys
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

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils;
import org.kohsuke.stapler.DataBoundConstructor;


/**
 * The type Neo post build task.
 */
public class NeoPostBuildTask extends Recorder {
	/**
	 * Instantiates a new Neo post build task.
	 */
	@DataBoundConstructor
	public NeoPostBuildTask() {
	}

	/**
	 * Gets required monitor service.
	 *
	 * @return the required monitor service
	 */
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/**
	 * Gets descriptor.
	 *
	 * @return the descriptor
	 */
	@Override
	public BuildStepDescriptor getDescriptor() {
		return super.getDescriptor();
	}

	/**
	 * This human readable name is used in the configuration screen.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return "Refresh NeoLoad Trend";
	}

	/**
	 * Perform boolean.
	 *
	 * @param build    the build
	 * @param launcher the launcher
	 * @param listener the listener
	 * @return the boolean
	 */
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		PluginUtils.buildGraph(build.getProject());
		return true;
	}


	/**
	 * The type Descriptor.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {


		/**
		 * Is applicable boolean.
		 *
		 * @param aClass the a class
		 * @return the boolean
		 */
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 *
		 * @return the display name
		 */
		public String getDisplayName() {
			return "Refresh NeoLoad trends";
		}
	}

}
