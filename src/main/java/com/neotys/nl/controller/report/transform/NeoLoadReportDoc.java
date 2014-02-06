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

import hudson.model.AbstractBuild;

import java.io.IOException;
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Objects;

/** A wrapper for an xml document.
 * @author ajohnson
 * 
 */
public class NeoLoadReportDoc {

	/** A default return value when a valid date can't be found (January 1, 1970, 00:00:00 GMT). */
	public static final Date DATE_1970 = new Date(0);

	/** A time format that is used for all languages in the context of this plugin. */
	public static final String STANDARD_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";

	/** The actual xml document. */
	private final Document doc;

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(NeoLoadReportDoc.class.getName());

	/** Constructor.
	 * @param xmlFilePath
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public NeoLoadReportDoc(final String xmlFilePath) {
		Document tempDoc = null;
		try {
			if (xmlFilePath != null &&
					"xml".equalsIgnoreCase(FilenameUtils.getExtension(xmlFilePath))) {

				tempDoc = XMLUtilities.readXmlFile(xmlFilePath);
			} else {
				// to avoid npe
				tempDoc = XMLUtilities.createNodeFromText("<empty></empty>").getOwnerDocument();
			}
		} catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error reading xml file. " + e.getMessage(), e);
		}
		doc = tempDoc;
	}

	public NeoLoadReportDoc(final Document doc) {
		this.doc = doc;
	}

	/**
	 * @return true if this is a valid NeoLoad report document. false otherwise.
	 * @throws XPathExpressionException
	 */
	public boolean isValidReportDoc() throws XPathExpressionException {
		final List<Node> nodes = XMLUtilities.findByExpression("/report/summary/all-summary/statistic-item", doc);

		if (nodes == null || nodes.size() == 0) {
			return false;
		}

		return true;
	}

	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	public Float getAverageResponseTime() throws XPathExpressionException {
		// @type='httppage' finds a node where with an attribute named "type" with a value of "httppage".
		// @avg finds the value of the attribute named "avg"
		final Node errorRateNode = XMLUtilities.findFirstByExpression("/report/summary/all-summary/statistic-item[@type='httppage']/@avg", doc);

		if (errorRateNode != null) {
			return extractNeoLoadNumber(errorRateNode.getNodeValue());
		}
		return null;
	}

	/**
	 * @return
	 * @throws XPathExpressionException
	 */
	public Float getErrorRatePercentage() throws XPathExpressionException {
		// @name='error_percentile' finds a node where with an attribute named "name" with a value of "error_percentile".
		final Node errorRateNode = XMLUtilities.findFirstByExpression("/report/summary/statistics/statistic[@name='error_percentile']/@value", doc);

		if (errorRateNode != null) {
			return extractNeoLoadNumber(errorRateNode.getNodeValue());
		}
		return null;
	}

	/**
	 * @param valArg
	 * @return
	 */
	private static Float extractNeoLoadNumber(final String valArg) {
		String val = StringUtils.trimToEmpty(valArg);

		// remove spaces etc
		val = val.replaceAll(",", ".").replaceAll(" ", "");
		// special case for percentages
		val = val.replaceAll(Pattern.quote("%"), "");
		// special case for percentages
		val = val.replaceAll(Pattern.quote("+"), "");
		// special case for less than 0.01%
		if ("<0.01".equals(val)) {
			return 0f;
		}

		try {
			return Float.valueOf(val);
		} catch (final Exception e) {
			// we couldn't convert the result to an actual number so the value will not be included.
			// this could be +INF, -INF, " - ", NaN, etc. See com.neotys.nl.util.FormatUtils.java, getTextNumber().
		}
		return null;
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
	public boolean hasCorrespondingDate(final AbstractBuild<?, ?> build) throws XPathExpressionException {
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(build);
		final Calendar buildEndTime = PluginUtils.getBuildEndTime(build);
		// we add X seconds leeway because sometimes the endTime matches the processed time exactly.
		buildEndTime.add(Calendar.SECOND, 15);

		final Calendar fileCreationTimeCal = getNeoLoadCreationDate();

		final boolean hasCorrespondingDate = fileCreationTimeCal.after(buildStartTime) && fileCreationTimeCal.before(buildEndTime);
		LOGGER.fine("Build " + build.number + ", hasCorrespondingDate buildStart / fileCreate / buildend: " +
				DateFormatUtils.format(buildStartTime, STANDARD_TIME_FORMAT) + " / " +
				DateFormatUtils.format(fileCreationTimeCal, STANDARD_TIME_FORMAT) + " / " +
				DateFormatUtils.format(buildEndTime, STANDARD_TIME_FORMAT) + " , hasCorrespondingDate: " + hasCorrespondingDate);

		return hasCorrespondingDate;
	}

	Calendar getNeoLoadCreationDate() throws XPathExpressionException {
		final List<Node> nodes = XMLUtilities.findByExpression("/report/summary/test", doc);

		// we didn't find the time so we use 1970 as a default.
		if (nodes == null || nodes.size() != 1) {
			return PluginUtils.toCalendar(DATE_1970);
		}

		final Map<String, String> attributes = XMLUtilities.getMap(nodes.get(0).getAttributes());
		try {
			final String fileCreationTimeStr = Objects.firstNonNull(attributes.get("std_start_time"), attributes.get("start"));
			final Date fileCreationTime = parseFileCreationTime(fileCreationTimeStr);
			final Calendar fileCreationTimeCal = PluginUtils.toCalendar(fileCreationTime);

			return fileCreationTimeCal;

		} catch (final Exception e) {
			// this may be a null pointer exception from the {@code Objects#firstNonNull()} method.
			LOGGER.log(Level.WARNING, "Issue parsing dates in " + doc.getDocumentURI(), e);
			return PluginUtils.toCalendar(DATE_1970);
		}
	}

	/**
	 * @param fileCreationTimeStr
	 * @return
	 */
	Date parseFileCreationTime(final String fileCreationTimeStr) {
		try {
			// try the standard format.
			final SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_TIME_FORMAT);
			return sdf.parse(fileCreationTimeStr);
		} catch (final Exception e) {
			// don't care
		}

		// try English. Mar 20, 2013 3:01:26 PM
		try {
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);
			return df.parse(fileCreationTimeStr);
		} catch (final Exception e) {
			// don't care
		}

		// try French. 22 mars 2013 11:07:33
		try {
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);
			return df.parse(fileCreationTimeStr);
		} catch (final Exception e) {
			// don't care
		}

		// try local machine format
		try {
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
			return df.parse(fileCreationTimeStr);
		} catch (final Exception e) {
			// can't figure out what the date format is...
			LOGGER.log(Level.FINE, "Can't parse date (" + fileCreationTimeStr + ") in xml file " + doc.getDocumentURI());
		}

		return DATE_1970;
	}

}
