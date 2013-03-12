package org.jenkinsci.plugins.neoload_integration;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadPluginOptions;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unchecked")
public class NeoPostBuildAction extends Notifier implements NeoLoadPluginOptions {
	
	/** Prefix log messages with this. */
	public static final String LOG_PREFIX = "NeoLoad Integration: ";

	/** User option presented in the GUI. Report file location(s). */
	private final String reportFileLocation;

	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendAverageResponse;
	
	/** User option presented in the GUI. Show the average response time. */
	private final boolean showTrendErrorRate;
	
	@DataBoundConstructor
	public NeoPostBuildAction(String reportFileLocation, boolean showTrendAverageResponse, boolean showTrendErrorRate, 
			String slaMapping) {
		super();
		this.reportFileLocation = reportFileLocation;
		this.showTrendAverageResponse = showTrendAverageResponse;
		this.showTrendErrorRate = showTrendErrorRate;
	}
	
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		return super.prebuild(build, listener);
	}
	
	@Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		return true;
	}


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(NeoPostBuildAction.class);
        }

        @Override
		public String getDisplayName() {
            return "Incorporate NeoLoad Results";
        }

        @Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

	/** @return the reportFileLocation */
	public String getReportFileLocation() {
		return reportFileLocation;
	}

	/* (non-Javadoc)
	 * @see org.jenkinsci.plugins.neoload_integration.NeoLoadPluginOptions#isShowTrendAverageResponse()
	 */
	@Override
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	/* (non-Javadoc)
	 * @see org.jenkinsci.plugins.neoload_integration.NeoLoadPluginOptions#isShowTrendErrorRate()
	 */
	@Override
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}

}
