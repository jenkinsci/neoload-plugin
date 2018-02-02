/*
 * Copyright (c) 2016, Neotys
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

/**
 * A wrapper for an xml document.
 *
 * @author ajohnson
 */
public class NeoLoadReportDoc {

	private static final String VIRTUAL_USER_XML_PATH = "/report/virtual-users";
	private static final String MONITOR_XML_PATH = "/report/monitors/monitored-host[@name='";

	private NeoLoadReportDoc(){

	}
	/**
	 * String for the average type.
	 */
	public static final String AVG = "/@avg";

	/**
	 * String for the value type.
	 */
	public static final String VAL = "/@value";

	/**
	 * String for the error% type.
	 */
	public static final String ERROR_RATE = "/@error_rate";

	/**
	 * String for the percentile3 type.
	 */
	public static final String PERCENTILE2 = "/@percentile2";

	/**
	 * String for the percentile3 type.
	 */
	public static final String PERCENTILE3 = "/@percentile3";





	/**
	 * @param litePath the legend of the request to get the information.<br />
	 *                 <i>Exemple : "UserPath/Actions/(Transaction or Page)/..."</i>
	 * @param type     the type of value search.
	 * @return the xpath.
	 */
	public static String getXPathForCustomGraph(final String litePath, final String type) {
		String path = VIRTUAL_USER_XML_PATH;
		final String[] tabSplited;
		if (litePath.startsWith("/")) {
			tabSplited = litePath.substring(1).split(">");
		} else {
			tabSplited = litePath.split(">");
		}
		for (final String str : tabSplited) {
			path += "/statistic-item[@name='" + str + "']";
		}
		return path + getTypeByStatistic(type);
	}

	/**
	 * @param litePath the legend of the request to get the information.<br />
	 *                 <i>Exemple : "UserPath/Actions/(Transaction or Page)/..."</i>
	 * @param type     the type of value search.
	 * @return the xpath.
	 */
	public static String getXPathForCustomMonitorOrLGGraph(final String litePath, final String type) {
		String path = MONITOR_XML_PATH;
		if (litePath.startsWith("/")) {
			final String splited = litePath.substring(1);
			final int index = splited.indexOf('/');
			if (index < 0) {
				return null;
			}
			path += splited.substring(0, index);
		} else {
			final int index = litePath.indexOf('/');
			if (index < 0) {
				return null;
			}
			path += litePath.substring(0, index);
		}

		path += "']/monitor/counters/statistic-item[@name='";
		path += litePath;
		path += "']";
		return path +  getTypeByStatistic(type);
	}



	private static String getTypeByStatistic(final String statistic) {
		final String value;
		switch (statistic) {
			case "percentile":
				value = PERCENTILE2;
				break;
			case "average":
				value = AVG;
				break;
			case "error":
				value = ERROR_RATE;
				break;
			default:
				value = VAL;
				break;
		}
		return value;
	}

}
