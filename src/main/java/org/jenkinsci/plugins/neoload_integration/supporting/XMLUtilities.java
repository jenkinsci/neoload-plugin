package org.jenkinsci.plugins.neoload_integration.supporting;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XMLUtilities implements Serializable {

	/** Generated. */
	private static final long serialVersionUID = -8773441162622083954L;

	/** Used for XPATH expressions. */
	private static final XPathFactory XPATHFACTORY = XPathFactory.newInstance();

	/** Used for XPATH expressions. */
	private static final XPath XPATH = XPATHFACTORY.newXPath();

	/** Utility classes are not intended to be instantiated. */
	private XMLUtilities() {
		throw new IllegalAccessError();
	}
	
	/** Create an xml Node using the passed in text. Use getOwnerDocument() to get the document. 
	 * @param xmlText
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Node createNodeFromText(final String xmlText) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlText));

		Document doc = db.parse(is);

		return doc.getFirstChild();
	}

	/** Return all nodes matching the passed in expression. Useful path expressions are listed below:<br/>
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
	public static List<Node> findByExpression(final String expression, Node searchNode) throws XPathExpressionException {
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
	public static Node findFirstByExpression(final String expression, Node searchNode) throws XPathExpressionException {
		List<Node> results = findByExpression(expression, searchNode);

		if ((results != null) && (results.size() > 0)) {
			return results.get(0);
		}

		return null;
	}

	/**
	 * @param expression
	 * @param searchNode
	 * @return
	 * @throws XPathExpressionException
	 */
	public static String findFirstValueByExpression(String expression, Node searchNode) throws XPathExpressionException {
		Node n = findFirstByExpression(expression, searchNode);

		if (n != null) {
			return n.getNodeValue();
		}

		return null;
	}

	/**
	 * @param attributes
	 * @return a map where the attribute is the key and the value is the value.
	 */
	public static Map<String, String> getMap(NamedNodeMap attributes) {
		Map<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < attributes.getLength(); i++) {
			if ((attributes.item(i) != null) && (attributes.item(i).getNodeName() != null)) {
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

	/**
	 * @param n
	 * @return
	 * @throws TransformerException
	 */
	public static String nodeToString(Node n) throws TransformerException {
		StringWriter sw = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		transformer.transform(new DOMSource(n), new StreamResult(sw));
		return sw.toString();
	}

	/** Read an xml file.
	 * @param srcFile
	 * @return a document created from the passed in file path
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
