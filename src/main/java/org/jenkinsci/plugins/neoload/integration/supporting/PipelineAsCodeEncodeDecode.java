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

package org.jenkinsci.plugins.neoload.integration.supporting;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.neoload.integration.steps.NeoloadRunStep;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PipelineAsCodeEncodeDecode {


	private static final String EXECUTABLE = "executable";
	private static final String PROJECT = "project";
	private static final String REPORT_XML = "reportXml";
	private static final String REPORT_HTML = "reportHtml";
	private static final String REPORT_JUNIT = "reportJunit";
	private static final String REPORT_PDF = "reportPdf";
	private static final String DISPLAY_GUI = "displayGui";
	private static final String TEST_NAME = "testName";
	private static final String TEST_DESCRIPTION = "testDescription";
	private static final String GRAPH_TRENDS_MAX_POINTS = "graphTrendsMaxPoints";
	private static final String SCENARIO = "scenario";
	private static final String NTS_SERVER = "server";
	private static final String DURATION = "duration";
	private static final String VU_COUNT = "vuCount";
	private static final String SHARED_LICENSE = "sharedLicense";
	private static final String COLLAB_SERVER = "server";
	private static final String COLLAB_PROJECT_NAME = "name";
	private static final String PUBLISH_TEST_RESULT = "publishTestResult";
	private static final String GRAPH_AVG_RESPONSE_TIME = "AvgResponseTime";
	private static final String GRAPH_ERROR_RATE = "ErrorRate";
	private static final String TREND_GRAPHS = "trendGraphs";
	private static final String GRAPH_CURVE = "curve";
	private static final String GRAPH_STATISTIC = "statistic";
	private static final String GRAPH_NAME = "name";
	private static final String COMMAND_LINE_OPTION = "commandLineOption";
	private static final String AUTO_ARCHIVE = "autoArchive";

	public static NeoloadRunStep decode(final Map<String, Object> map) throws Exception {
		NeoloadRunStep neoloadRunStep = new NeoloadRunStep(getString(map, SCENARIO));
		parseProject(map, neoloadRunStep);
		parseReport(map, neoloadRunStep);

		neoloadRunStep.setExecutable(getOptionalString(map, EXECUTABLE, ""));
		neoloadRunStep.setDisplayGUI(getOptionalBoolean(map, DISPLAY_GUI, false));
		neoloadRunStep.setTestDescription(getOptionalString(map, TEST_DESCRIPTION, NeoloadRunStep.DEFAULT_TEST_DESCRIPTION));
		neoloadRunStep.setTestResultName(getOptionalString(map, TEST_NAME, NeoloadRunStep.DEFAULT_TEST_NAME));
		neoloadRunStep.setCustomCommandLineOptions(getOptionalString(map, COMMAND_LINE_OPTION, ""));

		parseSharedLicense(map, neoloadRunStep);
		decodeGraph(map, neoloadRunStep);

		neoloadRunStep.setAutoArchive(getOptionalBoolean(map, AUTO_ARCHIVE, neoloadRunStep.isAutoArchive()));
		return neoloadRunStep;
	}

	public static void encodeGraph(final Map<String, Object> map, final NeoloadGraphDefinitionStep neoloadGraphDefinitionStep) {

		if (neoloadGraphDefinitionStep.getMaxTrends() > 0) {
			map.put(GRAPH_TRENDS_MAX_POINTS, neoloadGraphDefinitionStep.getMaxTrends());
		}
		PipelineAsCodeEncodeDecode.serializeTrendsGraphs(map, neoloadGraphDefinitionStep);
	}

	public static Map<String, Object> encode(final NeoloadRunStep neoloadRunStep) {
		final Map<String, Object> stringObjectMap = new LinkedHashMap<>();

		addIfNotEmpty(stringObjectMap, EXECUTABLE, neoloadRunStep.getExecutable());

		stringObjectMap.put(PROJECT, serializeProject(neoloadRunStep));

		if (NeoloadRunStep.REPORT_TYPE_CUSTOM.equals(neoloadRunStep.getReportType())) {


			if (!NeoloadRunStep.DEFAULT_XML_REPORT.equals(neoloadRunStep.getXmlReport())) {
				stringObjectMap.put(REPORT_XML, neoloadRunStep.getXmlReport());
			}

			if (!NeoloadRunStep.DEFAULT_HTML_REPORT.equals(neoloadRunStep.getHtmlReport())) {
				stringObjectMap.put(REPORT_HTML, neoloadRunStep.getHtmlReport());
			}

			if (!NeoloadRunStep.DEFAULT_JUNIT_REPORT.equals(neoloadRunStep.getJunitReport())) {
				stringObjectMap.put(REPORT_JUNIT, neoloadRunStep.getJunitReport());
			}

			addIfNotEmpty(stringObjectMap, REPORT_PDF, neoloadRunStep.getPdfReport());
		}

		if (neoloadRunStep.isDisplayGUI()) {
			stringObjectMap.put(DISPLAY_GUI, "true");
		}

		if (!NeoloadRunStep.DEFAULT_TEST_NAME.equals(neoloadRunStep.getTestResultName())) {
			stringObjectMap.put(TEST_NAME, neoloadRunStep.getTestResultName());
		}

		addIfNotEmpty(stringObjectMap, TEST_DESCRIPTION, neoloadRunStep.getTestDescription());


		addIfNotEmpty(stringObjectMap, COMMAND_LINE_OPTION, neoloadRunStep.getCustomCommandLineOptions());

		stringObjectMap.put(SCENARIO, neoloadRunStep.getScenarioName());

		serializeSharedLicense(stringObjectMap, neoloadRunStep);
		encodeGraph(stringObjectMap, neoloadRunStep);

		if (!neoloadRunStep.isAutoArchive()) {
			stringObjectMap.put(AUTO_ARCHIVE, "false");
		}

		return stringObjectMap;

	}

	public static void decodeGraph(final Map<String, Object> map, NeoloadGraphDefinitionStep neoloadGraphDefinitionStep) throws NeoloadParseException {
		PipelineAsCodeEncodeDecode.parseTrendGraphs(map, neoloadGraphDefinitionStep);
		neoloadGraphDefinitionStep.setMaxTrends(getOptionalNumber(map, GRAPH_TRENDS_MAX_POINTS, 0));

	}


	private static String getOptionalString(final Map<String, Object> map, final String key, final String defaultValue) {
		final Object o = map.get(key);
		if (o == null) {
			return defaultValue;
		} else {
			return o.toString();
		}
	}

	private static boolean getOptionalBoolean(final Map<String, Object> map, final String key, final boolean defaultValue) {
		final Object o = map.get(key);
		if (o != null) {
			if (o instanceof Boolean) {
				return (Boolean) o;
			} else {
				return Boolean.parseBoolean(o.toString());
			}
		}
		return defaultValue;
	}

	private static int getOptionalNumber(final Map<String, Object> map, final String key, final int defaultValue) {
		final Object o = map.get(key);
		if (o != null) {
			if (o instanceof Integer) {
				return (Integer) o;
			} else {
				return Integer.parseInt(o.toString());
			}
		}
		return defaultValue;

	}

	private static void parseReport(final Map<String, Object> map, final NeoloadRunStep neoloadRunStep) {
		final String reportXML = getOptionalString(map, REPORT_XML, null);
		final String reportPDF = getOptionalString(map, REPORT_PDF, null);
		final String reportHTML = getOptionalString(map, REPORT_HTML, null);
		final String reportJunit = getOptionalString(map, REPORT_JUNIT, null);

		boolean isCustom = reportHTML != null || reportJunit != null || reportPDF != null || reportXML != null;

		neoloadRunStep.setReportType(isCustom ? NeoloadRunStep.REPORT_TYPE_CUSTOM : NeoloadRunStep.REPORT_TYPE_DEFAULT);
		if (reportXML != null) {
			neoloadRunStep.setXmlReport(reportXML);
		}
		if (reportHTML != null) {
			neoloadRunStep.setHtmlReport(reportHTML);
		}
		if (reportJunit != null) {
			neoloadRunStep.setJunitReport(reportJunit);
		}
		if (reportPDF != null) {
			neoloadRunStep.setPdfReport(reportPDF);
		}
	}

	private static void parseTrendGraphs(final Map<String, Object> map, final NeoloadGraphDefinitionStep neoloadRunStep) throws NeoloadParseException {
		final Object o = map.get(TREND_GRAPHS);
		if (o instanceof List) {
			List<GraphOptionsInfo> listOfGraph = new ArrayList<>();
			for (Object obj : (List) o) {
				if (obj.equals(GRAPH_AVG_RESPONSE_TIME)) {
					neoloadRunStep.setShowTrendAverageResponse(true);
				} else if (obj.equals(GRAPH_ERROR_RATE)) {
					neoloadRunStep.setShowTrendErrorRate(true);
				} else if (obj instanceof Map) {
					listOfGraph.add(parseGraph((Map) obj));
				} else {
					throw new NeoloadParseException("Unrecognized graph trends");
				}
			}
			if (!listOfGraph.isEmpty()) {
				neoloadRunStep.setGraphOptionsInfo(listOfGraph);
			}
		}
	}

	private static GraphOptionsInfo parseGraph(final Map<String, Object> obj) throws NeoloadParseException {
		final GraphOptionsInfo graphOptionsInfo = new GraphOptionsInfo();
		graphOptionsInfo.setStatistic(getString(obj, GRAPH_STATISTIC));
		graphOptionsInfo.setName(getString(obj, GRAPH_NAME));
		final Object curveObj = obj.get(GRAPH_CURVE);
		List<GraphOptionsCurveInfo> graphOptionsCurveInfos = new ArrayList<>();
		if (curveObj instanceof List) {
			for (Object curve : (List) curveObj) {
				graphOptionsCurveInfos.add(new GraphOptionsCurveInfo(curve.toString()));
			}
		} else {
			throw new NeoloadParseException("Unrecognized graph");
		}
		graphOptionsInfo.setCurve(graphOptionsCurveInfos);
		return graphOptionsInfo;
	}

	private static void parseSharedLicense(final Map<String, Object> map, final NeoloadRunStep neoloadRunStep) throws NeoloadParseException {
		final Object o = map.get(SHARED_LICENSE);
		if (o != null) {
			if (o instanceof Map) {
				Map<String, Object> sharedLicenseMap = (Map) o;
				neoloadRunStep.setLicenseType(NeoloadRunStep.LICENSE_TYPE_SHARED);
				neoloadRunStep.setLicenseDuration(getString(sharedLicenseMap, DURATION));
				neoloadRunStep.setLicenseServer((NTSServerInfo) getServerInfo(getString(sharedLicenseMap, NTS_SERVER), false));
				neoloadRunStep.setLicenseVUCount(getString(sharedLicenseMap, VU_COUNT));
			} else {
				throw new NeoloadParseException("Error during the shared license parsing");
			}
		}
	}


	private static void parseProject(final Map<String, Object> map, final NeoloadRunStep neoloadRunStep) throws NeoloadParseException {
		final Object project = map.get(PROJECT);
		if (project instanceof String) {
			neoloadRunStep.setProjectType(NeoloadRunStep.PROJECT_TYPE_LOCAL);
			neoloadRunStep.setLocalProjectFile((String) project);
		} else {
			if (project instanceof Map) {
				Map<String, Object> projectMap = (Map<String, Object>) project;
				neoloadRunStep.setProjectType(NeoloadRunStep.PROJECT_TYPE_SHARED);
				neoloadRunStep.setSharedProjectName(getString(projectMap, COLLAB_PROJECT_NAME));
				neoloadRunStep.setSharedProjectServer(getServerInfo(getString(projectMap, COLLAB_SERVER), true));
			}
		}
	}

	private static ServerInfo getServerInfo(final String serverLabel, final boolean isCollab) throws NeoloadParseException {
		final List<ServerInfo> serverInfos = PluginUtils.getServerInfos(isCollab);
		for (ServerInfo serverInfo : serverInfos) {
			if (serverLabel.equalsIgnoreCase(serverInfo.getNonEmptyLabel(isCollab))) {
				return serverInfo;
			}
		}

		final ServerInfo serverInfo = computeNearestServerInfo(serverInfos, serverLabel, isCollab);
		if (serverInfo == null) {
			throw new NeoloadParseException("Server " + serverLabel + " not found !");
		} else {
			return serverInfo;
		}
	}

	private static int computeScore(String s1,String s2){
		int score = 0;
		if(StringUtils.isNotEmpty(s2) && s1.contains(s2)){
			score = 2;
			if(s1.length() == s2.length()){
				score ++;
			}
		}
		return score;
	}

	private static ServerInfo computeNearestServerInfo(final List<ServerInfo> serverInfos, final String serverLabel, final boolean isCollab) {
		//Labels not found. Try to find with different parameters
		ServerInfo result = null;
		int maxScore = 0;
		for (ServerInfo serverInfo : serverInfos) {
			int score = computeScore(serverLabel,serverInfo.getUrl());
			score += computeScore(serverLabel,serverInfo.getLoginUser());

			if (serverInfo instanceof NTSServerInfo) {
				NTSServerInfo ntsInfo = (NTSServerInfo) serverInfo;
				score += computeScore(serverLabel,isCollab ? ntsInfo.getCollabPath() : ntsInfo.getLicenseID());
			}
			if (maxScore < score) {
				result = serverInfo;
				maxScore = score;
			}
		}
		return result;
	}

	private static String getString(Map<String, Object> map, final String key) throws NeoloadParseException {
		final Object object = map.get(key);
		if (object == null) {
			throw new NeoloadParseException(key + " is mandatory");
		}
		return object.toString();
	}


	private static void addIfNotEmpty(final Map<String, Object> map, final String key, final String value) {
		if (value != null && !value.trim().isEmpty()) {
			map.put(key, value);
		}
	}

	private static void serializeSharedLicense(final Map<String, Object> stringObjectMap, final NeoloadRunStep neoloadRunStep) {
		if (NeoloadRunStep.LICENSE_TYPE_SHARED.equals(neoloadRunStep.getLicenseType())) {
			Map<String, Object> confNTS = new LinkedHashMap<>();
			confNTS.put(NTS_SERVER, neoloadRunStep.getLicenseServer().getNonEmptyLabel(false));

			storeInteger(confNTS, DURATION, neoloadRunStep.getLicenseDuration());
			storeInteger(confNTS, VU_COUNT, neoloadRunStep.getLicenseVUCount());
			stringObjectMap.put(SHARED_LICENSE, confNTS);
		}
	}

	private static void storeInteger(final Map<String, Object> conf, final String key, String value) {
		try {
			conf.put(key, Integer.parseInt(value));
		} catch (Exception xe) {
			conf.put(key, value);
		}
	}

	private static Object serializeProject(final NeoloadRunStep neoloadRunStep) {
		if (NeoloadRunStep.PROJECT_TYPE_LOCAL.equals(neoloadRunStep.getProjectType())) {
			return neoloadRunStep.getLocalProjectFile();
		}
		Map<String, Object> confShared = new LinkedHashMap<>();
		confShared.put(COLLAB_SERVER, neoloadRunStep.getSharedProjectServer().getNonEmptyLabel(true));
		confShared.put(COLLAB_PROJECT_NAME, neoloadRunStep.getSharedProjectName());
		confShared.put(PUBLISH_TEST_RESULT, neoloadRunStep.isPublishTestResults());
		return confShared;
	}


	private static void serializeTrendsGraphs(final Map<String, Object> stringObjectMap, final NeoloadGraphDefinitionStep neoloadRunStep) {
		List<Object> graphList = serializeCurve(neoloadRunStep.getGraphOptionsInfo());

		if (neoloadRunStep.isShowTrendAverageResponse()) {
			graphList.add(GRAPH_AVG_RESPONSE_TIME);
		}
		if (neoloadRunStep.isShowTrendErrorRate()) {
			graphList.add(GRAPH_ERROR_RATE);
		}
		if (!graphList.isEmpty()) {
			stringObjectMap.put(TREND_GRAPHS, graphList);
		}
	}

	private static List<Object> serializeCurve(List<GraphOptionsInfo> graphsOptionsCurveInfo) {

		List<Object> graphList = new ArrayList<>();

		if (graphsOptionsCurveInfo != null) {
			for (GraphOptionsInfo graphOptionsInfo : graphsOptionsCurveInfo) {
				Map<String, Object> graph = new LinkedHashMap<>();
				graph.put(GRAPH_NAME, graphOptionsInfo.getName());
				graph.put(GRAPH_CURVE, serializeCurves(graphOptionsInfo.getCurve()));
				graph.put(GRAPH_STATISTIC, graphOptionsInfo.getStatistic());
				graphList.add(graph);
			}
		}
		return graphList;

	}


	private static List<String> serializeCurves(final List<GraphOptionsCurveInfo> curves) {
		List<String> curveList = new ArrayList<>();
		for (GraphOptionsCurveInfo graphOptionsInfo : curves) {
			curveList.add(graphOptionsInfo.getPath());
		}
		return curveList;
	}
}
