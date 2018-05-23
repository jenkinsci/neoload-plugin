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
