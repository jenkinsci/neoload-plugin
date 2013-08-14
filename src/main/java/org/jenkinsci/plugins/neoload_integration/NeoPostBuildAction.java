package org.jenkinsci.plugins.neoload_integration;

import java.io.Serializable;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

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
	public NeoPostBuildAction(boolean showTrendAverageResponse, boolean showTrendErrorRate) {
		// this method and the annotation @DataBoundConstructor are required for jenkins 1.393 even if no params are passed in.
		this.showTrendAverageResponse = showTrendAverageResponse;
		this.showTrendErrorRate = showTrendErrorRate;
	}
	
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		NeoResultsAction.addActionIfNotExists(build);

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
				@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
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
