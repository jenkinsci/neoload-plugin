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
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig;
import org.jenkinsci.plugins.neoload.integration.supporting.CollabServerInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.NTSServerInfo;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;


import static org.jenkinsci.plugins.neoload.integration.NeoBuildAction.DescriptorImpl.buildNTSDisplayNameString;

public class SharedNeoLoadProject  extends AbstractStepImpl implements Serializable {

	private String projectServerName;
	private String projectName;
	private boolean publishTestResults;

	public String getProjectServerName() {
		return projectServerName;
	}

	public String getProjectName() {
		return projectName;
	}

	public boolean isPublishTestResults() {
		return publishTestResults;
	}

	@DataBoundConstructor
	public SharedNeoLoadProject(final String projectServerName, final String projectName, final boolean publishTestResults) {
		this.projectServerName = projectServerName;
		this.projectName = projectName;
		this.publishTestResults = publishTestResults;
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {

		public DescriptorImpl() {
			super(SharedNeoLoadProjectExecution.class);
		}

		public String getFunctionName() {
			return "SharedNeoLoadProject";
		}

		public String getDisplayName() {
			return "NeoLoad Shared Project Server";
		}

		public static ListBoxModel doFillProjectServerNameItems(@AncestorInPath final Item project) {
			final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
					(org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

			final ListBoxModel listBoxModel = new ListBoxModel();

			if (globalConfigDescriptor == null) {
				//should print an error but there is no runtime directly accessible
			} else {
				for (final NTSServerInfo server : globalConfigDescriptor.getNtsInfo()) {
					final String displayName = buildNTSDisplayNameString(server, true);
					final String optionValue = server.getLabel();
					final ListBoxModel.Option option = new ListBoxModel.Option(displayName, optionValue);
					listBoxModel.add(option);
				}
			}

			for (final CollabServerInfo server : globalConfigDescriptor.getCollabInfo()) {
				final String displayName;
				if (StringUtils.trimToEmpty(server.getLabel()).length() > 0) {
					displayName = server.getLabel();
				} else {
					displayName = server.getUrl() + ", User: " + server.getLoginUser();
				}
				final String optionValue = server.getLabel();
				final ListBoxModel.Option option = new ListBoxModel.Option(displayName, optionValue);

				listBoxModel.add(option);
			}

			if (listBoxModel.isEmpty()) {
				listBoxModel.add(new ListBoxModel.Option("Please configure Jenkins System Settings for NeoLoad to add a server.",
						null));
			}
			return listBoxModel;
		}
	}
}
