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

import org.jenkinsci.plugins.neoload.integration.ProjectSpecificAction;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Neoload curves x path stat.
 */
public class NeoloadCurvesXPathStat {
	private static final Logger LOGGER = Logger.getLogger(ProjectSpecificAction.class.getName());


	private final String legend;
	private final List<String> xPaths;
	private final Color color;
	private final Map<Integer, Float> buildToValue = new TreeMap<>();

	/**
	 * Instantiates a new Neoload curves x path stat.
	 *
	 * @param legend the legend
	 * @param color  the color
	 * @param xPaths the x paths
	 */
	public NeoloadCurvesXPathStat(final String legend, Color color, final String... xPaths) {
		this.legend = legend;
		this.xPaths = Arrays.asList(xPaths);
		this.color = color;
	}


	/**
	 * Add build.
	 *
	 * @param buildNumber the build number
	 * @param document    the document
	 */
	public void addBuild(final int buildNumber, final Document document) {
		for (String xPath : xPaths) {
			try {
				final Float data = PluginUtils.getCustom(xPath, document);
				if (data != null) {
					buildToValue.put(buildNumber, data);
					break;
				}
			} catch (XPathExpressionException e) {
				LOGGER.log(Level.WARNING, "Exception occurs while parsing results", e);
			}
		}
	}


	/**
	 * Gets legend.
	 *
	 * @return the legend
	 */
	public String getLegend() {
		return legend;
	}

	/**
	 * Gets paths.
	 *
	 * @return the paths
	 */
	public List<String> getxPaths() {
		return xPaths;
	}

	/**
	 * Gets color.
	 *
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Gets build to value.
	 *
	 * @return the build to value
	 */
	public Map<Integer, Float> getBuildToValue() {
		return buildToValue;
	}

	/**
	 * Get builds number int.
	 *
	 * @return the int
	 */
	public int getBuildsNumber() {
		return buildToValue.size();
	}
}
