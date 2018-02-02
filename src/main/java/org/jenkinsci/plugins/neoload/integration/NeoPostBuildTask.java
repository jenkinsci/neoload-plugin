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

public class NeoPostBuildTask extends Recorder {
	@DataBoundConstructor
	public NeoPostBuildTask() {
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;

	}

	@Override
	public BuildStepDescriptor getDescriptor() {
		return super.getDescriptor();
	}

	/**
	 * This human readable name is used in the configuration screen.
	 */
	public String getDisplayName() {
		return "Refresh NeoLoad Trend";
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		PluginUtils.buildGraph(build.getProject());
		return true;
	}


	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {


		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Refresh NeoLoad trends";
		}
	}

}
