/**
 * 
 */
package org.jenkinsci.plugins.neoload_integration.supporting;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author ajohnson
 *
 */
public class XMLUtilitiesTest extends TestCase {

	/** Used to find the xml document. */
	private URL url = null;
	
	/** Holds the contents of the xml document. */
	private Document d = null;

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		url = this.getClass().getResource("books.xml");
		d = XMLUtilities.readXmlFile(url.getFile());
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#createNodeFromText(java.lang.String)}.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	@Test
	public void testCreateNodeFromText() throws ParserConfigurationException, SAXException, IOException {
		String content = "weeeeeeeeeee";
		Node n = XMLUtilities.createNodeFromText("<test>" + content + "</test>");
		
		assertTrue(content.equals(n.getTextContent()));
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#findByExpression(java.lang.String, org.w3c.dom.Node)}.
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
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#findFirstByExpression(java.lang.String, org.w3c.dom.Node)}.
	 * @throws XPathExpressionException 
	 * @throws DOMException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	@Test
	public void testFindFirstByExpression() throws XPathExpressionException {
		assertTrue("Everyday Italian".equals(XMLUtilities.findFirstByExpression("/bookstore/book/title", d).getTextContent()));
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#findFirstValueByExpression(java.lang.String, org.w3c.dom.Node)}.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws DOMException 
	 */
	@Test
	public void testFindFirstValueByExpression() throws XPathExpressionException {
		assertTrue("en".equals(XMLUtilities.findFirstValueByExpression("/bookstore/book/title/@lang", d)));
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#getMap(org.w3c.dom.NamedNodeMap)}.
	 * @throws XPathExpressionException 
	 */
	@Test
	public void testGetMap() throws XPathExpressionException {
		Map<String, String> map = XMLUtilities.getMap(XMLUtilities.findFirstByExpression("/bookstore/book/title", d).getAttributes());
		assertTrue(map.size() > 0);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#toList(org.w3c.dom.NodeList)}.
	 * @throws XPathExpressionException 
	 */
	@Test
	public void testToList() throws XPathExpressionException {
		List<Node> list = XMLUtilities.findByExpression("/bookstore/book/title", d);
		assertTrue(list.size() > 0);
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#nodeToString(org.w3c.dom.Node)}.
	 * @throws XPathExpressionException 
	 * @throws TransformerException 
	 */
	@Test
	public void testNodeToString() throws XPathExpressionException, TransformerException {
		Node n = XMLUtilities.findFirstByExpression("/bookstore/book[1]/title", d);
		String text = XMLUtilities.nodeToString(n);
		assertTrue(text.trim().endsWith("<title lang=\"en\">Everyday Italian</title>"));
	}

	/**
	 * Test method for {@link org.jenkinsci.plugins.neoload_integration.supporting.XMLUtilities#readXmlFile(java.lang.String)}.
	 */
	@Test
	public void testReadXmlFile() {
		assertTrue(d != null);
	}

}
