package org.jenkinsci.plugins.neoload_integration;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/** Without this class the two trend graphs are not displayed. */
@Extension(optional = true)
public class ProjectSpecificActionFactory extends TransientProjectActionFactory
implements Describable<ProjectSpecificActionFactory>, Serializable {

	/** Generated. */
	private static final long serialVersionUID = -1955069445418117473L;

	@Override
	public Collection<? extends Action> createFor(final AbstractProject job) {
		return Collections.singleton(new ProjectSpecificAction(job));
	}

	public Descriptor<ProjectSpecificActionFactory> getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension(optional = true)
	public static final DescriptorImplPSA DESCRIPTOR = new DescriptorImplPSA();

	public static final class DescriptorImplPSA extends Descriptor<ProjectSpecificActionFactory> implements Serializable {

		/** Generated. */
		private static final long serialVersionUID = 7549069766029770042L;

		public DescriptorImplPSA() {
		}

		@Override
		public String getDisplayName() {
			return "!" + this.getClass().getSimpleName() + "!";
		}

		@Override
		public ProjectSpecificActionFactory newInstance(final StaplerRequest req, final JSONObject formData) {
			return new ProjectSpecificActionFactory();
		}

		@Override
		public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
			return super.configure(req, formData);
		}

		public static boolean isShowGraph() {
			return true;
		}
	}

}
