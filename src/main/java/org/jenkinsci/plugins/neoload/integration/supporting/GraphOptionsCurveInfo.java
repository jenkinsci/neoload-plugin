package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * The type Graph options curve info.
 */
public class GraphOptionsCurveInfo extends AbstractDescribableImpl<GraphOptionsCurveInfo>
implements Serializable, Comparable<GraphOptionsCurveInfo>, Comparator<GraphOptionsCurveInfo> {

	/** Generated. */
	private static final long serialVersionUID = 8172436270325483285L;
	
	private String path;

	/**
	 * Instantiates a new Graph options curve info.
	 */
	public GraphOptionsCurveInfo() {
	}

	/**
	 * Instantiates a new Graph options curve info.
	 *
	 * @param path the path
	 */
	@DataBoundConstructor
	public GraphOptionsCurveInfo(final String path) {
		this.path = path;
	}

	/**
	 * Gets path.
	 *
	 * @return the legend
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets path.
	 *
	 * @param path the legend to set
	 */
	@DataBoundSetter
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * To string string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Compare int.
	 *
	 * @param o1 the o 1
	 * @param o2 the o 2
	 * @return the int
	 */
	public int compare(final GraphOptionsCurveInfo o1, final GraphOptionsCurveInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	/**
	 * Compare to int.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareTo(final GraphOptionsCurveInfo o) {
		return compare(this, o);
	}

	/**
	 * Equals boolean.
	 *
	 * @param obj the obj
	 * @return the boolean
	 */
	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this,  obj);
	}

	/**
	 * Hash code int.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/**
	 * The type Descriptor.
	 */
	@Extension
    public static class DescriptorImpl extends Descriptor<GraphOptionsCurveInfo> {
		/**
		 * Gets display name.
		 *
		 * @return the display name
		 */
		public String getDisplayName() {
			return "This display name serves no purpose.";
		}
	}
}
