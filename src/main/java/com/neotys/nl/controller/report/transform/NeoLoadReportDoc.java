package com.neotys.nl.controller.report.transform;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
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

	/** Constructor.
	 * @param xmlFilePath
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public NeoLoadReportDoc(String xmlFilePath) throws ParserConfigurationException, SAXException, IOException {
		if ((xmlFilePath != null) && 
				("xml".equalsIgnoreCase(FilenameUtils.getExtension(xmlFilePath)))) {

			doc = XMLUtilities.readXmlFile(xmlFilePath);
		} else {
			// to avoid npe
			doc = XMLUtilities.createNodeFromText("<empty></empty>").getOwnerDocument();
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
	public Float getGenericAvgValue(String path, String typeAttributeValue) throws XPathExpressionException {
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
				numVal = Float.valueOf(val);
			}
		}

		return numVal;
	}

	/** @return the doc */
	public Document getDoc() {
		return doc;
	}

}
