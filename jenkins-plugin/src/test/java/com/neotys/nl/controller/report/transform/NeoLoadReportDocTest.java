/**
 * 
 */
package com.neotys.nl.controller.report.transform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities;
import org.jenkinsci.plugins.neoload_integration.supporting.ZipUtils;
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
	
	/** The date the test started in the report file. */
	private static final String START_DATE_IN_FILE = "Mar 18, 2013 11:15:39 AM";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		URL url = NeoLoadReportDoc.class.getResource("xmlReports.zip");
		ZipUtils.unzip(url.getFile(), new File(url.getFile()).getParent());
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

	@Test
	public void testNeoLoadReportDocIsNewer1() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);

		Calendar cal = Calendar.getInstance();
		
		assertFalse(nlrd.isNewerThan(cal));
		
		cal.set(Calendar.YEAR, 1980);
		assertTrue(nlrd.isNewerThan(cal));
	}

	@Test
	public void testNeoLoadReportDocIsNewerFrench() throws Exception {
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		
		// french
		contents = contents.replaceAll(Pattern.quote(START_DATE_IN_FILE), "22 mars 2013 11:07:33");
		FileUtils.write(new File(urlValid.getFile()), contents);
		
		Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		
		Calendar cal = Calendar.getInstance();
		
		assertFalse(nlrd.isNewerThan(cal));
		
		cal.set(Calendar.YEAR, 1980);
		assertTrue(nlrd.isNewerThan(cal));
	}
	
	@Test
	public void testNeoLoadReportDocIsNewerStandard() throws Exception {
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		
		// french
		contents = contents.replaceAll(Pattern.quote("start=\"" + START_DATE_IN_FILE), 
				"std_start_time=\"2013-03-20 15:00:56\" dontUse_start=\"dont use me");
		FileUtils.write(new File(urlValid.getFile()), contents);
		
		Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		
		Calendar cal = Calendar.getInstance();
		
		assertFalse(nlrd.isNewerThan(cal));
		
		cal.set(Calendar.YEAR, 1980);
		assertTrue(nlrd.isNewerThan(cal));
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
