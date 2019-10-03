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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import hudson.util.Secret;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * @author ajohnson
 *
 */
public class XMLUtilitiesTest extends TestCase {

	/** Used to find the xml document. */
	private URL url = null;

	/** Holds the contents of the xml document. */
	private Document d = null;


	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		url = this.getClass().getResource("data/books.xml");
		d = XMLUtilities.readXmlFile(url.getFile());
	}

	@Test
	public void testToAndFromXML_A() throws ParserConfigurationException, SAXException, IOException {
		final NTSServerInfo ntsServerInfo = new NTSServerInfo("123", "http://serveraddress:8080/", "loginUser", Secret.fromString("<>!@#$%^&*()_+"), "Label 1", "/repository_1",
				"MCwCFBU8hLebFPEcgunM5wPU0IoUEmkEAhQF8G4snQv9YLWGl4t10nXZnaW+5w==");
		final String xmlEscaped = XMLUtilities.toXMLEscaped(ntsServerInfo);

		final NTSServerInfo ntsServerInfoCopy = (NTSServerInfo) XMLUtilities.fromXMLEscaped(xmlEscaped);
		assertTrue("they're not equal? why????", ntsServerInfo.equals(ntsServerInfoCopy));
	}

	@Test
	public void testToAndFromXML_B() throws ParserConfigurationException, SAXException, IOException {
		final CollabServerInfo collabServerInfo = new CollabServerInfo("123", "svn://serveraddress:3690/neoload", "><loginUser", Secret.fromString("!@#$%^&*()_+"), "Label 2",
				null, null);
		final String xmlEscaped = XMLUtilities.toXMLEscaped(collabServerInfo);

		final CollabServerInfo collabServerInfoCopy = (CollabServerInfo) XMLUtilities.fromXMLEscaped(xmlEscaped);
		assertTrue("they're not equal? why????", collabServerInfo.equals(collabServerInfoCopy));
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload.integration.supporting.XMLUtilities#createNodeFromText(java.lang.String)}.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testCreateNodeFromText() throws ParserConfigurationException, SAXException, IOException {
		final String content = "weeeeeeeeeee";
		final Node n = XMLUtilities.createNodeFromText("<test>" + content + "</test>");

		assertTrue(content.equals(n.getTextContent()));
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload.integration.supporting.XMLUtilities#findByExpression(java.lang.String, org.w3c.dom.Node)}.
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testFindByExpression() throws XPathExpressionException {
		assertTrue(XMLUtilities.findByExpression("/bookstore/book/title", d).size() == 4);
		assertTrue("Everyday Italian".equals(
				XMLUtilities.findByExpression("/bookstore/book[1]/title", d).get(0).getTextContent()));
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload.integration.supporting.XMLUtilities#findFirstByExpression(java.lang.String, org.w3c.dom.Node)}.
	 * @throws XPathExpressionException
	 * @throws DOMException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testFindFirstByExpression() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		assertTrue("Everyday Italian".equals(XMLUtilities.findFirstByExpression("/bookstore/book/title", d).getTextContent()));
		assertTrue(XMLUtilities.findFirstByExpression("/bookstore/book/title", XMLUtilities.createNodeFromText("<empty></empty>")) == null);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload.integration.supporting.XMLUtilities#getMap(org.w3c.dom.NamedNodeMap)}.
	 * @throws XPathExpressionException
	 */
	@Test
	public void testGetMap() throws XPathExpressionException {
		final Map<String, String> map = XMLUtilities.getMap(XMLUtilities.findFirstByExpression("/bookstore/book/title", d).getAttributes());
		assertTrue(map.size() > 0);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload.integration.supporting.XMLUtilities#toList(org.w3c.dom.NodeList)}.
	 * @throws XPathExpressionException
	 */
	@Test
	public void testToList() throws XPathExpressionException {
		final List<Node> list = XMLUtilities.findByExpression("/bookstore/book/title", d);
		assertTrue(list.size() > 0);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload.integration.supporting.XMLUtilities#readXmlFile(java.lang.String)}.
	 */
	@Test
	public void testReadXmlFile() {
		assertTrue(d != null);
	}

}
