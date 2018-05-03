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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

public final class XMLUtilities implements Serializable {

	/** Generated. */
	private static final long serialVersionUID = -8773441162622083954L;

	/** Used for XPATH expressions. */
	private static final XPathFactory XPATHFACTORY = XPathFactory.newInstance();

	/** Used for XPATH expressions. */
	private static final XPath XPATH = XPATHFACTORY.newXPath();

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(XMLUtilities.class.getName());

	/** Utility classes are not intended to be instantiated. */
	private XMLUtilities() {
		throw new IllegalAccessError();
	}

	public static String toXMLEscaped(final Object obj) {
		final StringBuilder sb = new StringBuilder();
		sb.append(obj.getClass().getName()).append(";");
		sb.append(StringEscapeUtils.unescapeXml(new Gson().toJson(obj)));
		return sb.toString();

		// xstream works fine here but the .fromXML() does not work.
		//		XSTREAM.toXML(obj);
	}

	public static Object fromXMLEscaped(final String xml) {
		final String dataArray[] = xml.split(";", 2);
		final String className = dataArray[0];
		final String data = StringEscapeUtils.unescapeXml(dataArray[1]);
		final Gson gson = new Gson();
		final ServerInfo si;
		try {
			si = (ServerInfo) gson.fromJson(data, Class.forName(className));

		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Issue converting stored data to a class instance. Type: " + className + ", data: " + xml, e);
			return null;
		}
		return si;

		// I couldn't get xstream to work. It works in test cases but not when jenkins is running. It seems like a classloader issue.
		//		return XSTREAM.fromXML(xml);
	}

	/** Create an xml Node using the passed in text. Use getOwnerDocument() to get the document.
	 * @param xmlText
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Node createNodeFromText(final String xmlText) throws ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		final DocumentBuilder db = dbf.newDocumentBuilder();
		final InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlText));

		final Document doc = db.parse(is);

		return doc.getFirstChild();
	}

	/** Return all nodes matching the passed in expression. Useful legend expressions are listed below:<br/>
	 * <table class="reference">
	 * <tbody>
	 * <tr>
	 * <th align="left" valign="top" width="25%">Expression</th>
	 * <th align="left" valign="top">Description</th>
	 * </tr>
	 * <tr>
	 * <td valign="top"><i>nodename</i></td>
	 * <td valign="top">Selects all nodes with the name "<i>nodename</i>"</td>
	 * </tr>
	 * <tr>
	 * <td valign="top">/</td>
	 * <td valign="top">Selects from the root node</td>
	 * </tr>
	 * <tr>
	 * <td valign="top">//</td>
	 *          <td valign="top">Selects nodes in the document from the current node that match the selection no matter where they are </td>
	 * </tr>
	 * <tr>
	 * <td valign="top">.</td>
	 * <td valign="top">Selects the current node</td>
	 * </tr>
	 * <tr>
	 * <td valign="top">..</td>
	 * <td valign="top">Selects the parent of the current node</td>
	 * </tr>
	 * <tr>
	 * <td valign="top">@</td>
	 * <td valign="top">Selects attributes</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 * @param expression search for this expression
	 * @param nodes search this and all child nodes
	 * @return
	 * @throws XPathExpressionException
	 */
	public static List<Node> findByExpression(final String expression, final Node searchNode) throws XPathExpressionException {
		final XPathExpression expr = XPATH.compile(expression);
		final NodeList nl = (NodeList) expr.evaluate(searchNode, XPathConstants.NODESET);

		return toList(nl);
	}

	/** Return the first node matching the passed in expression or null if none was found.
	 * @param expression
	 * @param searchNode
	 * @return
	 * @throws XPathExpressionException
	 */
	public static Node findFirstByExpression(final String expression, final Node searchNode) throws XPathExpressionException {
		final List<Node> results = findByExpression(expression, searchNode);

		if (results != null && results.size() > 0) {
			return results.get(0);
		}

		return null;
	}

	/**
	 * @param attributes
	 * @return a map where the attribute is the key and the value is the value.
	 */
	public static Map<String, String> getMap(final NamedNodeMap attributes) {
		final Map<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < attributes.getLength(); i++) {
			if (attributes.item(i) != null && attributes.item(i).getNodeName() != null) {
				map.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
			}
		}

		return map;
	}

	/** Turns a NodeList into a list.
	 * @param nodeList
	 * @return
	 */
	public static List<Node> toList(final NodeList nodeList) {
		final List<Node> list = new ArrayList<Node>(nodeList.getLength());

		for (int nlIndex = 0; nlIndex < nodeList.getLength(); nlIndex++) {
			list.add(nodeList.item(nlIndex));
		}

		return list;
	}

	/** Read an xml file.
	 * @param srcFile
	 * @return a document created from the passed in file legend
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document readXmlFile(final String srcFile) throws ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document doc = docBuilder.parse(srcFile);

		// see http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		return doc;
	}

}
