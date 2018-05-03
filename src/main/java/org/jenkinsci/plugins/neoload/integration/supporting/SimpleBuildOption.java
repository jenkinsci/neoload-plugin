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
package org.jenkinsci.plugins.neoload.integration.supporting;

import java.util.List;

/**
 * Simple implementation of the NeoLoadPluginOption to pass the needed infos from the original Run to
 * the NeoResultAction
 */
public class SimpleBuildOption implements NeoLoadPluginOptions{

	private final boolean showTrendAverageResponse;

	private final boolean showTrendErrorRate;

	private final List<GraphOptionsInfo> graphOptionsInfos;

	private final int maxTrends;

	private final String xmlReportArtifactPath;

	private final String htmlReportArtifactPath;

	public SimpleBuildOption(boolean showTrendAverageResponse, boolean showTrendErrorRate, List<GraphOptionsInfo> graphOptionsInfos, int maxTrends, String xMLReportArtifactPath, String hTMLReportArtifactPath){
		this.showTrendAverageResponse = showTrendAverageResponse;
		this.showTrendErrorRate = showTrendErrorRate;

		this.graphOptionsInfos = graphOptionsInfos;
		this.maxTrends = maxTrends;
		this.xmlReportArtifactPath = xMLReportArtifactPath;
		this.htmlReportArtifactPath = hTMLReportArtifactPath;
	}

	@Override
	public boolean isShowTrendAverageResponse() {
		return showTrendAverageResponse;
	}

	@Override
	public boolean isShowTrendErrorRate() {
		return showTrendErrorRate;
	}

	@Override
	public List<GraphOptionsInfo> getGraphOptionsInfo() {
		return graphOptionsInfos;
	}

	@Override
	public int getMaxTrends() {
		return maxTrends;
	}

	@Override
	public String getXMLReportArtifactPath() {
		return null;
	}

	@Override
	public String getHTMLReportArtifactPath() {
		return null;
	}
}
