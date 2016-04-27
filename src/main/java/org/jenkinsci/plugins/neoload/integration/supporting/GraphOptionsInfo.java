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
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import net.sf.json.JSONObject;

public class GraphOptionsInfo extends AbstractDescribableImpl<GraphOptionsInfo> 
implements Serializable, Comparable<GraphOptionsInfo>, Comparator<GraphOptionsInfo> {

	/** Generated. */
	private static final long serialVersionUID = 7705837928952454627L;
	
	private String name;
	private List<GraphOptionsCurveInfo> curve;
	private String statistic;

	public GraphOptionsInfo() {
	}

	@DataBoundConstructor
	public GraphOptionsInfo(final String name, final List<GraphOptionsCurveInfo> curve, final String statistic) {
		this.curve = curve;
		this.name = name;
		this.statistic = statistic;
	}
	
	/** @return the name */
	public String getName() {
		return name;
	}

	/** @param name the name to set */
	public void setName(String name) {
		this.name = name;
	}

	/** @return the curve */
	public List<GraphOptionsCurveInfo> getCurve() {
		return curve;
	}

	/** @param curve the curve to set */
	public void setCurve(final List<GraphOptionsCurveInfo> curve) {
		this.curve = curve;
	}

	/** @return the statistic */
	public String getStatistic() {
		return statistic;
	}

	/** @param statistic the statistic to set */
	public void setStatistic(String statistic) {
		this.statistic = statistic;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int compare(final GraphOptionsInfo o1, final GraphOptionsInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	public int compareTo(final GraphOptionsInfo o) {
		return compare(this, o);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this,  obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Extension
    public static class DescriptorImpl extends Descriptor<GraphOptionsInfo> {
		public String getDisplayName() {
			return "This display name serves no purpose.";
		}
		
		public ListBoxModel doFillStatisticItems(@AncestorInPath final Item project) {
			final ListBoxModel listBoxModel = new ListBoxModel();

			listBoxModel.add(new Option("Error %", "error"));
			listBoxModel.add(new Option("Average", "average"));
			listBoxModel.add(new Option("Percentile", "percentile"));
			
			return listBoxModel;
		}
	}

}
