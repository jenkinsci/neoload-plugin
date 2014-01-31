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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.neoload_integration.supporting.MockObjects;
import org.jenkinsci.plugins.neoload_integration.supporting.PluginUtils;
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

	/** These values help validate that the correct values are extracted from the report. */
	URL specificGraphValues = null;

	/** An invalid NeoLoad report doc. */
	URL urlInvalid = null;

	/** Mock project for testing. */
	private MockObjects mo = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		final URL url = NeoLoadReportDoc.class.getResource("xmlReports.zip");
		ZipUtils.unzip(url.getFile(), new File(url.getFile()).getParent());

		urlValid = NeoLoadReportDoc.class.getResource("report-valid.xml");
		urlInvalid = NeoLoadReportDoc.class.getResource("report-invalid.xml");
		specificGraphValues = NeoLoadReportDoc.class.getResource("specificGraphValues.xml");

		mo = new MockObjects();
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
	 * Test method for {@link com.neotys.nl.controller.report.transform.NeoLoadReportDoc#NeoLoadReportDoc(java.lang.String)}.
	 * @throws XPathExpressionException
	 */
	@Test
	public void testNeoLoadReportDocStringMissingFile() throws XPathExpressionException {
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc("intentionally missing file for test case.xml");
		assertFalse(nlrd.isValidReportDoc());
		assertNull("no file should have been found", nlrd.getDoc());
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
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);

		// get the build start and end dates, set the document date to before the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(beforeBuild.getTime());
		contents = contents.replaceAll(Pattern.quote(MockObjects.getStartDateInHtmlFile()), replacementDate);
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertFalse(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewer2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);

		// get the build start and end dates, set the document date to after the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(afterBuild.getTime());
		contents = contents.replaceAll(Pattern.quote(MockObjects.getStartDateInHtmlFile()), replacementDate);
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertFalse(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewer3() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);

		// get the build start and end dates, set the document date to during the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(duringBuild.getTime());
		contents = contents.replaceAll(Pattern.quote(MockObjects.getStartDateInHtmlFile()), replacementDate);
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertTrue(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewerFrenchFalse1() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);

		// get the build start and end dates, set the document date to before the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(beforeBuild.getTime());
		contents = contents.replaceAll(Pattern.quote(MockObjects.getStartDateInHtmlFile()), replacementDate);
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertFalse(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewerFrenchFalse2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);

		// get the build start and end dates, set the document date to after the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(afterBuild.getTime());
		contents = contents.replaceAll(Pattern.quote(MockObjects.getStartDateInHtmlFile()), replacementDate);
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertFalse(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewerFrenchTrue() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);

		// get the build start and end dates, set the document date to during the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));

		final String replacementDate = df.format(duringBuild.getTime());
		contents = contents.replaceAll(Pattern.quote(MockObjects.getStartDateInHtmlFile()), replacementDate);
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertTrue(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewerStandardTrue() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);

		// get the build start and end dates, set the document date to during the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(duringBuild.getTime());
		contents = contents.replaceAll(Pattern.quote("start=\"" + MockObjects.getStartDateInHtmlFile()),
				"std_start_time=\"" + replacementDate + "\" dontUse_start=\"dont use me");
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertTrue(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewerStandardFalse1() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);

		// get the build start and end dates, set the document date to during the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(beforeBuild.getTime());
		contents = contents.replaceAll(Pattern.quote("start=\"" + MockObjects.getStartDateInHtmlFile()),
				"std_start_time=\"" + replacementDate + "\" dontUse_start=\"dont use me");
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertFalse(nlrd.hasCorrespondingDate(ab));
	}

	@Test
	public void testNeoLoadReportDocIsNewerStandardFalse2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final AbstractBuild<?, ?> ab = mo.getAbstractBuild();
		final Calendar buildStartTime = PluginUtils.getBuildStartTime(ab);
		final long middleOfRunTime = buildStartTime.getTimeInMillis() + ab.getDuration() / 2;
		final Calendar duringBuild = Calendar.getInstance();
		duringBuild.setTimeInMillis(middleOfRunTime);
		final Calendar beforeBuild = Calendar.getInstance();
		beforeBuild.set(Calendar.YEAR, 1980);
		final Calendar afterBuild = Calendar.getInstance();
		afterBuild.set(Calendar.YEAR, 9980);
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH);

		// get the build start and end dates, set the document date to during the build, test.
		String contents = FileUtils.readFileToString(new File(urlValid.getFile()));
		final String replacementDate = df.format(afterBuild.getTime());
		contents = contents.replaceAll(Pattern.quote("start=\"" + MockObjects.getStartDateInHtmlFile()),
				"std_start_time=\"" + replacementDate + "\" dontUse_start=\"dont use me");
		FileUtils.write(new File(urlValid.getFile()), contents, "UTF-8");

		final Document d = XMLUtilities.readXmlFile(urlValid.getFile());
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(d);
		assertFalse(nlrd.hasCorrespondingDate(ab));
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
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(urlValid.getFile());
		assertTrue(nlrd.getDoc() != null);
		assertTrue(nlrd.isValidReportDoc());
	}

	@Test
	public void testGetNeoLoadCreationDateValid() throws XPathExpressionException {
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(urlValid.getFile());
		final Calendar calendar = nlrd.getNeoLoadCreationDate();
		assertTrue("A valid date should have been found.", calendar.after(PluginUtils.toCalendar(new Date(100000))));
	}

	@Test
	public void testGetNeoLoadCreationDateInvalid() throws XPathExpressionException {
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(urlInvalid.getFile());
		final Calendar calendar = nlrd.getNeoLoadCreationDate();
		assertTrue("No valid date should have been found.", calendar.before(PluginUtils.toCalendar(new Date(100000))));
	}

	@Test
	public void testGetNeoLoadCreationDateInvalid2() throws XPathExpressionException {
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc("non existent file");
		final Calendar calendar = nlrd.getNeoLoadCreationDate();
		assertTrue("No valid date should have been found.", calendar.before(PluginUtils.toCalendar(new Date(100000))));
	}

	@Test
	public void testParseFileCreationTimeUnrecognized() {
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc("non existent file");
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 1971);

		final Calendar returnedCalendar = PluginUtils.toCalendar(nlrd.parseFileCreationTime("abcdefg"));
		assertTrue("The default date should be 1970", calendar.after(returnedCalendar));
	}

	@Test
	public void testAverageResponseTime() throws XPathExpressionException {
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(specificGraphValues.getFile());
		assertEquals("the average response time is wrong", 3.4, nlrd.getAverageResponseTime(), 0.001);
	}

	@Test
	public void testErrorRatePercentage() throws XPathExpressionException {
		final NeoLoadReportDoc nlrd = new NeoLoadReportDoc(specificGraphValues.getFile());
		assertEquals("the error rate percentage is wrong", 10.1, nlrd.getErrorRatePercentage(), 0.001);
	}
}
