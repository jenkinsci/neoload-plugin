package org.jenkinsci.plugins.neoload.integration.supporting;

import org.jenkinsci.plugins.neoload.integration.ProjectSpecificAction;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Neoload curves x path stat.
 */
public class NeoloadCurvesXPathStat {
	private static final Logger LOGGER = Logger.getLogger(ProjectSpecificAction.class.getName());


	private final String legend;
	private final List<String> xPaths;
	private final Color color;
	private final Map<Integer, Float> buildToValue = new TreeMap<>();

	/**
	 * Instantiates a new Neoload curves x path stat.
	 *
	 * @param legend the legend
	 * @param color  the color
	 * @param xPaths the x paths
	 */
	public NeoloadCurvesXPathStat(final String legend, Color color, final String... xPaths) {
		this.legend = legend;
		this.xPaths = Arrays.asList(xPaths);
		this.color = color;
	}


	/**
	 * Add build.
	 *
	 * @param buildNumber the build number
	 * @param document    the document
	 */
	public void addBuild(final int buildNumber, final Document document) {
		for (String xPath: xPaths){
			try {
				final Float data = PluginUtils.getCustom(xPath, document);
				if(data != null) {
					buildToValue.put(buildNumber, data);
					break;
				}
			} catch (XPathExpressionException e) {
				LOGGER.log(Level.WARNING,"Exception occurs while parsing results",e);
			}
		}
	}


	/**
	 * Gets legend.
	 *
	 * @return the legend
	 */
	public String getLegend() {
		return legend;
	}

	/**
	 * Gets paths.
	 *
	 * @return the paths
	 */
	public List<String> getxPaths() {
		return xPaths;
	}

	/**
	 * Gets color.
	 *
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Gets build to value.
	 *
	 * @return the build to value
	 */
	public Map<Integer, Float> getBuildToValue() {
		return buildToValue;
	}

	/**
	 * Get builds number int.
	 *
	 * @return the int
	 */
	public int getBuildsNumber(){
		return buildToValue.size();
	}
}
