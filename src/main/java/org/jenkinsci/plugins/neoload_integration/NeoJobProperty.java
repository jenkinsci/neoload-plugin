package org.jenkinsci.plugins.neoload_integration;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;

import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadPluginOptions;
import org.kohsuke.stapler.DataBoundConstructor;

/** This class corresponds with the configuration of a job. */
public class NeoJobProperty extends JobProperty<AbstractProject<?,?>> implements NeoLoadPluginOptions {

    @Extension(optional = true)
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendAverageResponse;

	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendErrorRate;

	@DataBoundConstructor
	public NeoJobProperty(boolean showTrendAverageResponse, boolean showTrendErrorRate) {
		super();
		this.showTrendAverageResponse = showTrendAverageResponse;
		this.showTrendErrorRate = showTrendErrorRate;
	}
	
	public static final class DescriptorImpl extends JobPropertyDescriptor {
		@Override
		public String getDisplayName() {
			return "!" + this.getClass().getName() + "!";
		}
    }

	/** @return the showTrendAverageResponse */
	@Override
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	/** @return the showTrendErrorRate */
	@Override
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}

}
