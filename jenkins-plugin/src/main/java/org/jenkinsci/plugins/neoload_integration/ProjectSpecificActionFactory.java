package org.jenkinsci.plugins.neoload_integration;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;

import java.util.Collection;
import java.util.Collections;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

@Extension
public class ProjectSpecificActionFactory extends TransientProjectActionFactory
		implements Describable<ProjectSpecificActionFactory> {

	@Override
	public Collection<? extends Action> createFor(AbstractProject job) {
		return Collections.singleton(new ProjectSpecificAction(job));
	}

	@Override
	public Descriptor<ProjectSpecificActionFactory> getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<ProjectSpecificActionFactory> {

		public DescriptorImpl() {
		}

		@Override
		public String getDisplayName() {
			return "!" + this.getClass().getSimpleName() + "!";
		}

		@Override
        public ProjectSpecificActionFactory newInstance(StaplerRequest req, JSONObject formData) {
			return new ProjectSpecificActionFactory();
		}

		@Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			return super.configure(req, formData);
		}

		public static boolean isShowGraph() {
			return true;
		}
	}

}
