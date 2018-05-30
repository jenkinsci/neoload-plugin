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
 *
 */

package org.jenkinsci.plugins.neoload.integration.steps;

import hudson.Extension;
import org.jenkinsci.plugins.neoload.integration.supporting.GraphOptionsInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.NeoloadGraphDefinitionStep;
import org.jenkinsci.plugins.neoload.integration.supporting.PipelineAsCodeEncodeDecode;
import org.jenkinsci.plugins.structs.describable.UninstantiatedDescribable;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoloadRefreshTrendStep extends AbstractStepImpl implements NeoloadGraphDefinitionStep {

	private int maxTrends=0;
	private boolean showTrendAverageResponse=false;
	private boolean showTrendErrorRate=false;
	private List<GraphOptionsInfo> graphs;
	private boolean hasArguments = false;

	@DataBoundConstructor
	public NeoloadRefreshTrendStep(){

	}


	/**
	 * Gets max trends.
	 *
	 * @return the max trends
	 */
	@Override
	public int getMaxTrends() {
		return maxTrends;
	}

	/**
	 * Sets max trends.
	 *
	 * @param maxTrends the max trends
	 */
	@Override
	@DataBoundSetter
	public void setMaxTrends(final int maxTrends) {
		this.maxTrends = maxTrends;
	}

	/**
	 * Is show trend average response boolean.
	 *
	 * @return the boolean
	 */
	@Override
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	/**
	 * Sets show trend average response.
	 *
	 * @param showTrendAverageResponse the show trend average response
	 */
	@Override
	@DataBoundSetter
	public void setShowTrendAverageResponse(final boolean showTrendAverageResponse) {
		this.showTrendAverageResponse = showTrendAverageResponse;
	}

	/**
	 * Is show trend error rate boolean.
	 *
	 * @return the boolean
	 */
	@Override
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}

	/**
	 * Sets show trend error rate.
	 *
	 * @param showTrendErrorRate the show trend error rate
	 */
	@Override
	@DataBoundSetter
	public void setShowTrendErrorRate(final boolean showTrendErrorRate) {
		this.showTrendErrorRate = showTrendErrorRate;
	}

	/**
	 * Gets graph options info.
	 *
	 * @return the graph options info
	 */
	@Override
	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		return graphs;
	}

	/**
	 * Sets graph options info.
	 *
	 * @param graphOptionsInfo the graph options info
	 */
	@Override
	@DataBoundSetter
	public void setGraphOptionsInfo(final List<GraphOptionsInfo> graphOptionsInfo) {
		this.graphs = graphOptionsInfo;
	}

	public boolean hasArguments() {
		return hasArguments;
	}

	public void setHasArguments(final boolean hasArguments) {
		this.hasArguments = hasArguments;
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {

		public DescriptorImpl() {
			super(NeoloadRefreshTrendStepExecution.class);
		}

		public DescriptorImpl(final Class<? extends StepExecution> executionType) {
			super(executionType);
		}

		@Override
		public String getFunctionName() {
			return "neoloadRefreshTrends";
		}

		@Override
		public String getDisplayName() {
			return "Refresh NeoLoad Trends";
		}


		@Override
		public Map<String, Object> defineArguments(final Step step) {
			Map<String,Object> encoded = new HashMap<>();
			PipelineAsCodeEncodeDecode.encodeGraph(encoded,(NeoloadGraphDefinitionStep) step);

			return encoded;
		}

		public UninstantiatedDescribable uninstantiate(final Step step) throws UnsupportedOperationException {
			return new UninstantiatedDescribable(defineArguments(step));
		}

		@Override
		public Step newInstance(final Map<String, Object> arguments) throws Exception {
			final NeoloadRefreshTrendStep neoloadRefreshTrendStep = new NeoloadRefreshTrendStep();
			neoloadRefreshTrendStep.setHasArguments(!arguments.isEmpty());
			PipelineAsCodeEncodeDecode.decodeGraph(arguments,neoloadRefreshTrendStep);
			return neoloadRefreshTrendStep;
		}
	}
}
