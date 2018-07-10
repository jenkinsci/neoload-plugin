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
import org.jenkinsci.plugins.neoload.integration.NeoBuildAction;
import org.jenkinsci.plugins.neoload.integration.supporting.ServerInfo;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;


/**
 * The type Neoload run step execution.
 */
public class NeoloadRunStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

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
	protected Void run() throws Exception {
		listener.getLogger().println("Running NeoLoad with executable : " + step.getExecutable());

		ServerInfo projectServerFilled = step.getSharedProjectServer();

		if (step.getProjectType() == null) {
			if (step.getLocalProjectFile() != null && !"".equals(step.getLocalProjectFile())) {
				step.setProjectType(NeoloadRunStep.PROJECT_TYPE_LOCAL);
			} else if (projectServerFilled != null) {
				step.setProjectType(NeoloadRunStep.PROJECT_TYPE_SHARED);
			} else {
				listener.getLogger().println("No project has be defined (shared or local). The execution can not run");
			}
		}

		NeoBuildAction neoBuildAction = new NeoBuildAction(step);

		if (launcher == null) {
			launcher = ws.createLauncher(listener);
		}

		neoBuildAction.perform(build, ws, launcher, listener);
		return null;
	}

}
