package org.jenkinsci.plugins.neoload.integration.supporting;

import org.jenkinsci.plugins.neoload.integration.ProjectSpecificAction;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeoloadCurvesXPathStat {
	private static final Logger LOGGER = Logger.getLogger(ProjectSpecificAction.class.getName());


	private final String legend;
	private final List<String> xPaths;
	private final Color color;
	private final Map<Integer, Float> buildToValue = new TreeMap<>();

	public NeoloadCurvesXPathStat(final String legend, Color color, final String... xPaths) {
		this.legend = legend;
		this.xPaths = Arrays.asList(xPaths);
		this.color = color;
	}


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


	public String getLegend() {
		return legend;
	}

	public List<String> getxPaths() {
		return xPaths;
	}

	public Color getColor() {
		return color;
	}

	public Map<Integer, Float> getBuildToValue() {
		return buildToValue;
	}

	public int getBuildsNumber(){
		return buildToValue.size();
	}
}
