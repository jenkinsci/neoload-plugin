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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
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
	URL urlValid = null;
	
	/** An invalid NeoLoad report doc. */
	URL urlInvalid = null;
	
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
		
		urlValid = NeoLoadReportDoc.class.getResource("report-valid.xml");
		urlInvalid = NeoLoadReportDoc.class.getResource("report-invalid.xml");
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
