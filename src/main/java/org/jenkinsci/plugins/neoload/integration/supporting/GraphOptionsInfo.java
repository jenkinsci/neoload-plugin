package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;

/**
 * The type Graph options info.
 */
public class GraphOptionsInfo extends AbstractDescribableImpl<GraphOptionsInfo>
implements Serializable, Comparable<GraphOptionsInfo>, Comparator<GraphOptionsInfo> {

	/** Generated. */
	private static final long serialVersionUID = 7705837928952454627L;
	
	private String name;
	private List<GraphOptionsCurveInfo> curve;
	private String statistic;

	/**
	 * Instantiates a new Graph options info.
	 */
	public GraphOptionsInfo() {
	}

	/**
	 * Instantiates a new Graph options info.
	 *
	 * @param name      the name
	 * @param curve     the curve
	 * @param statistic the statistic
	 */
	@DataBoundConstructor
	public GraphOptionsInfo(final String name, final List<GraphOptionsCurveInfo> curve, final String statistic) {
		this.curve = curve;
		this.name = name;
		this.statistic = statistic;
	}

	/**
	 * Gets name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets curve.
	 *
	 * @return the curve
	 */
	public List<GraphOptionsCurveInfo> getCurve() {
		return curve;
	}

	/**
	 * Sets curve.
	 *
	 * @param curve the curve to set
	 */
	public void setCurve(final List<GraphOptionsCurveInfo> curve) {
		this.curve = curve;
	}

	/**
	 * Gets statistic.
	 *
	 * @return the statistic
	 */
	public String getStatistic() {
		return statistic;
	}

	/**
	 * Sets statistic.
	 *
	 * @param statistic the statistic to set
	 */
	public void setStatistic(String statistic) {
		this.statistic = statistic;
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
	public int compare(final GraphOptionsInfo o1, final GraphOptionsInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	/**
	 * Compare to int.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareTo(final GraphOptionsInfo o) {
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
    public static class DescriptorImpl extends Descriptor<GraphOptionsInfo> {
		/**
		 * Gets display name.
		 *
		 * @return the display name
		 */
		public String getDisplayName() {
			return "This display name serves no purpose.";
		}

		/**
		 * Do fill statistic items list box model.
		 *
		 * @param project the project
		 * @return the list box model
		 */
		public ListBoxModel doFillStatisticItems(@AncestorInPath final Item project) {
			final ListBoxModel listBoxModel = new ListBoxModel();

			listBoxModel.add(new Option("Error %", "error"));
			listBoxModel.add(new Option("Average", "average"));
			listBoxModel.add(new Option("Percentile", "percentile"));
			
			return listBoxModel;
		}
	}

}
