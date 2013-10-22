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
package com.neotys.nl.controller.report.transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** A wrapper for an xml document.
 * @author ajohnson
 * 
 */
public class NeoLoadReportDoc {

	/** The actual xml document. */
	private Document doc = null;

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(NeoLoadReportDoc.class.getName());

	/** Constructor.
	 * @param xmlFilePath
	 */
	public NeoLoadReportDoc(final String xmlFilePath) {
		try {
			if ((xmlFilePath != null) &&
					("xml".equalsIgnoreCase(FilenameUtils.getExtension(xmlFilePath)))) {

				doc = XMLUtilities.readXmlFile(xmlFilePath);
			} else {
				// to avoid npe
				doc = XMLUtilities.createNodeFromText("<empty></empty>").getOwnerDocument();
			}
		} catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error reading xml file " + xmlFilePath + ". " + e.getMessage(), e);
		}
	}

	public NeoLoadReportDoc(final Document doc) {
		this.doc = doc;
	}

	/**
	 * @return true if this is a valid NeoLoad report document. false otherwise.
	 * @throws XPathExpressionException
	 */
	public boolean isValidReportDoc() throws XPathExpressionException {
		if (doc == null) {
			return false;
		}

		final List<Node> nodes = XMLUtilities.findByExpression("/report/summary/all-summary/statistic-item", doc);

		if ((nodes == null) || (nodes.size() == 0)) {
			return false;
		}

		return true;
	}

	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	public Float getAverageResponseTime() throws XPathExpressionException {
		return getGenericAvgValue("/report/summary/all-summary/statistic-item", "virtualuser");
	}

	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	public Float getErrorRatePercentage() throws XPathExpressionException {
		return getGenericAvgValue("/report/summary/all-summary/statistic-item", "httppage");
	}

	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	private Float getGenericAvgValue(final String path, final String typeAttributeValue) throws XPathExpressionException {
		final List<Node> nodes = XMLUtilities.findByExpression(path, doc);
		Float numVal = null;
		String val = null;

		// look at the nodes we found
		for (final Node searchNode : nodes) {
			// look for the avg response time node
			val = XMLUtilities.findFirstValueByExpression("@type", searchNode);
			if (typeAttributeValue.equalsIgnoreCase(val)) {
				// this is the correct node. get the avg response time value.
				val = XMLUtilities.findFirstValueByExpression("@avg", searchNode);
				// remove spaces etc
				val = val.replaceAll(",", ".").replaceAll(" ", "");
				// special case for percentages
				val = val.replaceAll(Pattern.quote("%"), "");
				// special case for percentages
				val = val.replaceAll(Pattern.quote("+"), "");
				// special case for less than 0.01%
				if ("<0.01".equals(val)) {
					numVal = 0f;
				} else {
					try {
						numVal = Float.valueOf(val);
					} catch (final Exception e) {
						// we couldn't convert the result to an actual number so the value will not be included.
						// this could be +INF, -INF, " - ", NaN, etc. See com.neotys.nl.util.FormatUtils.java, getTextNumber().
					}
				}
			}
		}

		return numVal;
	}

	/** @return the doc */
	public Document getDoc() {
		return doc;
	}

	/**
	 * @param cal
	 * @return
	 * @throws XPathExpressionException
	 */
	public boolean isNewerThan(final Calendar calBuildTime) throws XPathExpressionException {
		final List<Node> nodes = XMLUtilities.findByExpression("/report/summary/test", doc);

		// false if we didn't find the time
		if ((nodes == null) || (nodes.size() != 1)) {
			return false;
		}

		final Map<String, String> attributes = XMLUtilities.getMap(nodes.get(0).getAttributes());

		try {
			// look for a time formatted in a standard way
			if (attributes.containsKey("std_start_time")) {
				final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
				final Date d = sdf.parse(attributes.get("std_start_time"));
				return PluginUtils.toCalendar(d).after(calBuildTime);
			}
		} catch (final Exception e) {
			// don't care
		}

		final String startTime = attributes.get("start");

		// try English. Mar 20, 2013 3:01:26 PM
		try {
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);
			final Date d = df.parse(startTime);
			return PluginUtils.toCalendar(d).after(calBuildTime);
		} catch (final Exception e) {
			// don't care
		}

		// try French. 22 mars 2013 11:07:33
		try {
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);
			final Date d = df.parse(startTime);
			return PluginUtils.toCalendar(d).after(calBuildTime);
		} catch (final Exception e) {
			// don't care
		}

		// try local machine format
		try {
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
			final Date d = df.parse(startTime);
			return PluginUtils.toCalendar(d).after(calBuildTime);
		} catch (final Exception e) {
			// can't figure out what the date format is...
			LOGGER.log(Level.FINE, "Can't parse date in xml file " + doc.getDocumentURI());
		}

		return false;
	}

}
