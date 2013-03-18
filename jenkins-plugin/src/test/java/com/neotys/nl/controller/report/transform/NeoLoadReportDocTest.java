/**
 * 
 */
package com.neotys.nl.controller.report.transform;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author ajohnson
 *
 */
public class NeoLoadReportDocTest extends TestCase {

	/** A valid NeoLoad report doc. */
	URL urlValid = this.getClass().getResource("report-valid.xml");
	
	/** An invalid NeoLoad report doc. */
	URL urlInvalid = this.getClass().getResource("report-invalid.xml");
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link com.neotys.nl.controller.report.transform.NeoLoadReportDoc#NeoLoadReportDoc(java.lang.String)}.
	 * @throws XPathExpressionException 
	 */
	@Test
	public void testNeoLoadReportDocString() throws XPathExpressionException {
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(urlValid.getFile());
		assertTrue(nlrd.isValidReportDoc());
		
		nlrd = new NeoLoadReportDoc(urlInvalid.getFile());
		assertFalse(nlrd.isValidReportDoc());
		
		nlrd = new NeoLoadReportDoc((String)null);
		assertFalse(nlrd.isValidReportDoc());
	}

	/**
	 * Test method for {@link com.neotys.nl.controller.report.transform.NeoLoadReportDoc#NeoLoadReportDoc(org.w3c.dom.Document)}.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	@Test
	public void testNeoLoadReportDocDocument() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertTrue(nlrd.isValidReportDoc());
		
		d = XMLUtilities.readXmlFile(urlInvalid.getFile());
		nlrd = new NeoLoadReportDoc(d);
		assertFalse(nlrd.isValidReportDoc());
	}

	/**
	 * Test method for {@link com.neotys.nl.controller.report.transform.NeoLoadReportDoc#getAverageResponseTime()}.
	 * @throws XPathExpressionException 
	 */
	@Test
	public void testGetAverageResponseTime() throws XPathExpressionException {
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(urlValid.getFile());
		nlrd.getAverageResponseTime();
		
		nlrd = new NeoLoadReportDoc(urlInvalid.getFile());
		nlrd.getAverageResponseTime();
	}

	/**
	 * Test method for {@link com.neotys.nl.controller.report.transform.NeoLoadReportDoc#getErrorRatePercentage()}.
	 * @throws XPathExpressionException 
	 */
	@Test
	public void testGetErrorRatePercentage() throws XPathExpressionException {
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(urlValid.getFile());
		nlrd.getErrorRatePercentage();
		
		nlrd = new NeoLoadReportDoc(urlInvalid.getFile());
		nlrd.getErrorRatePercentage();
	}

	/**
	 * Test method for {@link com.neotys.nl.controller.report.transform.NeoLoadReportDoc#getDoc()}.
	 * @throws XPathExpressionException 
	 */
	@Test
	public void testGetDoc() throws XPathExpressionException {
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(urlValid.getFile());
		assertTrue(nlrd.getDoc() != null);
		assertTrue(nlrd.isValidReportDoc());
	}

}
