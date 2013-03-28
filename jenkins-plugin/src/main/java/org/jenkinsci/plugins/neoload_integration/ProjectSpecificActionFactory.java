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

/** Necessary Jenkins fluff to make the associated class actually be taken into account. */
@Extension(optional = true)
public class ProjectSpecificActionFactory extends TransientProjectActionFactory
		implements Describable<ProjectSpecificActionFactory> {

	@Override
	public Collection<? extends Action> createFor(AbstractProject job) {
		return Collections.singleton(new ProjectSpecificAction(job));
	}

	public Descriptor<ProjectSpecificActionFactory> getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension(optional = true)
	public static final DescriptorImplPSA DESCRIPTOR = new DescriptorImplPSA();

    public static final class DescriptorImplPSA extends Descriptor<ProjectSpecificActionFactory> {

		public DescriptorImplPSA() {
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
