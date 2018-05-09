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
package org.jenkinsci.plugins.neoload.integration.steps;

import hudson.Extension;
import hudson.model.Item;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig;
import org.jenkinsci.plugins.neoload.integration.supporting.NTSServerInfo;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

import static org.jenkinsci.plugins.neoload.integration.NeoBuildAction.DescriptorImpl.buildNTSDisplayNameString;

public class SharedNeoLoadLicense extends AbstractStepImpl implements Serializable {

	private String licenseVUCount = DescriptorImpl.defaultLicenseVUCount;
	private String licenseDuration = DescriptorImpl.defaultDuration;
	private String licenceName;


	public String getLicenseVUCount() {
		return licenseVUCount;
	}

	@DataBoundSetter
	public void setLicenseVUCount(String licenseVUCount) {
		this.licenseVUCount = licenseVUCount;
	}

	public String getLicenseDuration() {
		return licenseDuration;
	}

	@DataBoundSetter
	public void setLicenseDuration(String licenseDuration) {
		this.licenseDuration = licenseDuration;
	}

	public String getLicenceName() {
		return licenceName;
	}

	@DataBoundConstructor
	public SharedNeoLoadLicense(final String licenceName) {
		this.licenceName = licenceName;
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {
		public static final String defaultLicenseVUCount = "50";
		public static final String defaultDuration = "2";

		public DescriptorImpl() {
			super(SharedNeoLoadLicenseExecution.class);
		}

		public String getFunctionName() {
			return "sharedNeoLoadLicense";
		}

		public String getDisplayName() {
			return "NeoLoad Shared License";
		}

		public static ListBoxModel doFillLicenceNameItems(@AncestorInPath final Item project) {
			final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
					(NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

			final ListBoxModel listBoxModel = new ListBoxModel();

			if (globalConfigDescriptor == null) {
				//LOGGER.log(Level.FINEST, "No NeoLoad server settings found. Please add servers before configuring jobs. (getLicenseServerOptions)");
			} else {
				for (final NTSServerInfo server : globalConfigDescriptor.getNtsInfo()) {
					final String displayName = buildNTSDisplayNameString(server, false);
					final String optionValue = server.getLabel();
					final ListBoxModel.Option option = new ListBoxModel.Option(displayName, optionValue);
					listBoxModel.add(option);
				}
			}

			if (listBoxModel.isEmpty()) {
				//LOGGER.finest("There is no NTS Server configured !");
				listBoxModel.add(new ListBoxModel.Option("Please configure Jenkins System Settings for NeoLoad to add an NTS server.",
						null));
			}
			return listBoxModel;
		}
	}

}
