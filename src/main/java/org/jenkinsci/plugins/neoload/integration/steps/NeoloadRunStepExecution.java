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

import com.google.inject.Inject;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.apache.commons.collections.CollectionUtils;
import org.jenkinsci.plugins.neoload.integration.NeoBuildAction;
import org.jenkinsci.plugins.neoload.integration.NeoGlobalConfig;
import org.jenkinsci.plugins.neoload.integration.supporting.NTSServerInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.ServerInfo;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.util.Collection;


public class NeoloadRunStepExecution extends AbstractSynchronousNonBlockingStepExecution<Boolean> {

    @StepContextParameter
    private transient TaskListener listener;

	@StepContextParameter
	private transient Run build;

	@StepContextParameter
	private transient FilePath ws;

	@StepContextParameter
	private transient Launcher launcher;

	@Inject
	private transient NeoloadRunStep step;

	@Override
	protected Boolean run() throws Exception {
		listener.getLogger().println("Running NeoLoad with executable : " + step.getExecutable());

		NTSServerInfo licenseServerFilled = getNtsServer(step.getNeoSharedLicence());
		ServerInfo projectServerFilled = getProjectServer(step.getNeoSharedProject());

		if (step.getProjectType() == null){
			if(step.getLocalProjectFile() != null && ! "".equals(step.getLocalProjectFile())){
				step.setProjectType(NeoloadRunStep.DescriptorImpl.defaultProjectType);
			}else if (projectServerFilled != null){
				step.setProjectType(NeoloadRunStep.DescriptorImpl.sharedProjectType);
			}else{
				listener.getLogger().println("No project has be defined (shared or local). The execution can not run");
			}
		}

		NeoBuildAction neoBuildAction = new NeoBuildAction(step.getExecutable(), step.getProjectType(), step.getReportType(),
				step.getLocalProjectFile(),
				step.getNeoSharedProject() != null ? step.getNeoSharedProject().getProjectName() : "",
				step.getScenarioName(), step.getHtmlReport(),
				step.getXmlReport(), step.getPdfReport(), step.getJunitReport(),
				step.isDisplayTheGUI(), step.getTestResultName(), step.getTestDescription(), step.getLicenseType(),
				step.getNeoSharedLicence() != null ? step.getNeoSharedLicence().getLicenseVUCount() : SharedNeoLoadLicense.DescriptorImpl.defaultLicenseVUCount,
				step.getNeoSharedLicence() != null ? step.getNeoSharedLicence().getLicenseDuration() : SharedNeoLoadLicense.DescriptorImpl.defaultDuration,
				step.getCustomCommandLineOptions(),
				step.getNeoSharedProject() != null ? step.getNeoSharedProject().isPublishTestResults() : false,
				projectServerFilled, licenseServerFilled,
				step.isShowTrendAverageResponse(), step.isShowTrendErrorRate(),
				step.getGraphOptionsInfo(), step.getMaxTrends());

		if (launcher == null) {
			launcher = ws.createLauncher(listener);
		}

		return neoBuildAction.perform(build, ws, launcher, listener);

	}

	private NTSServerInfo getNtsServer(SharedNeoLoadLicense sharedLicence) {

		if(sharedLicence == null){
			return null;
		}

		String label = sharedLicence.getLicenceName();
		final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
				(NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

		if (globalConfigDescriptor == null) {
			listener.getLogger().println("No NeoLoad server settings found. Please add servers before configuring jobs. (getLicenseServerOptions)");
		} else {
			for (final NTSServerInfo server : globalConfigDescriptor.getNtsInfo()) {
				if (server.getLabel().equals(label) ){
					return server;
				}
			}
		}
		listener.getLogger().println("No NeoLoad server settings found. Please add servers before configuring jobs. (getLicenseServerOptions)");
		return null;
	}

	private ServerInfo getProjectServer(SharedNeoLoadProject sharedProject) {
		if (sharedProject == null){
			return null;
		}

		String label = sharedProject.getProjectServerName();
		final NeoGlobalConfig.DescriptorImpl globalConfigDescriptor =
				(NeoGlobalConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(NeoGlobalConfig.class);

		if (globalConfigDescriptor == null) {
			listener.getLogger().println("No NeoLoad server settings found. Please add servers before configuring jobs. (getLicenseServerOptions)");
			return null;
		}

		// find the serverInfo based on the unique ID.
		@SuppressWarnings("unchecked") final Collection<ServerInfo> allServerInfo =
				CollectionUtils.union(globalConfigDescriptor.getNtsInfo(), globalConfigDescriptor.getCollabInfo());
		for (final ServerInfo si : allServerInfo) {
			if (si.getLabel().equals(label)) {
				return si;
			}
		}

		return null;
	}
}
