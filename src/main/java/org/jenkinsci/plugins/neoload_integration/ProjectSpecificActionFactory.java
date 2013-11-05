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
