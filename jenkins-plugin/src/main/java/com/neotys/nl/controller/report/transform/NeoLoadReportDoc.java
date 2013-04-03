package com.neotys.nl.controller.report.transform;

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
import org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public NeoLoadReportDoc(String xmlFilePath) {
		try {
			if ((xmlFilePath != null) && 
					("xml".equalsIgnoreCase(FilenameUtils.getExtension(xmlFilePath)))) {

				doc = XMLUtilities.readXmlFile(xmlFilePath);
			} else {
				// to avoid npe
				doc = XMLUtilities.createNodeFromText("<empty></empty>").getOwnerDocument();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error reading xml file " + xmlFilePath + ". " + e.getMessage(), e);
		}
	}

	public NeoLoadReportDoc(Document doc) {
		this.doc = doc;
	}

	/**
	 * @return true if this is a valid NeoLoad report document. false otherwise.
	 * @throws XPathExpressionException
	 */
	public boolean isValidReportDoc() throws XPathExpressionException {
		List<Node> nodes = XMLUtilities.findByExpression("/report/summary/all-summary/statistic-item", doc);

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
	private Float getGenericAvgValue(String path, String typeAttributeValue) throws XPathExpressionException {
		List<Node> nodes = XMLUtilities.findByExpression(path, doc);
		Float numVal = null;
		String val = null;

		// look at the nodes we found
		for (Node searchNode : nodes) {
			// look for the avg response time node
			val = XMLUtilities.findFirstValueByExpression("@type", searchNode);
			if (typeAttributeValue.equalsIgnoreCase(val)) {
				// this is the correct node. get the avg response time value.
				val = XMLUtilities.findFirstValueByExpression("@avg", searchNode);
				val = val.replaceAll(",", ".").replaceAll(" ", ""); // remove spaces etc
				val = val.replaceAll(Pattern.quote("%"), ""); // special case for percentages
				val = val.replaceAll(Pattern.quote("+"), ""); // special case for percentages
				if ("<0.01".equals(val)) { // special case for less than 0.01%
					numVal = 0f;
				} else {
					try {
						numVal = Float.valueOf(val);
					} catch (Exception e) {
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
	public boolean isNewerThan(Calendar calBuildTime) throws XPathExpressionException {
		List<Node> nodes = XMLUtilities.findByExpression("/report/summary/test", doc);
		
		// false if we didn't find the time
		if ((nodes == null) || (nodes.size() != 1)) {
			return false;
		}
		
		Map<String, String> attributes = XMLUtilities.getMap(nodes.get(0).getAttributes());
		
		try {
			// look for a time formatted in a standard way
			if (attributes.containsKey("std_start_time")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
				Date d = sdf.parse(attributes.get("std_start_time"));
				return PluginUtils.toCalendar(d).after(calBuildTime);
			}
		} catch (Exception e) {
			// don't care
		}
		
		String startTime = attributes.get("start");
		
		// try English. Mar 20, 2013 3:01:26 PM
		try {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);
			Date d = df.parse(startTime);
			return PluginUtils.toCalendar(d).after(calBuildTime);
		} catch (Exception e) {
			// don't care
		}
		
		// try French. 22 mars 2013 11:07:33
		try {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);
			Date d = df.parse(startTime);
			return PluginUtils.toCalendar(d).after(calBuildTime);
		} catch (Exception e) {
			// don't care
		}
		
		// try local machine format
		try {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
			Date d = df.parse(startTime);
			return PluginUtils.toCalendar(d).after(calBuildTime);
		} catch (Exception e) {
			// can't figure out what the date format is...
			LOGGER.log(Level.FINE, "Can't parse date in xml file " + doc.getDocumentURI());
		}
		
		return false;
	}

}
