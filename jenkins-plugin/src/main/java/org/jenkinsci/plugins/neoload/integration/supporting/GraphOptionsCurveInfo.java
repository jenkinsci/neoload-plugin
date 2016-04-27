package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;

public class GraphOptionsCurveInfo extends AbstractDescribableImpl<GraphOptionsCurveInfo> 
implements Serializable, Comparable<GraphOptionsCurveInfo>, Comparator<GraphOptionsCurveInfo> {

	/** Generated. */
	private static final long serialVersionUID = 8172436270325483285L;
	
	private String path;

	public GraphOptionsCurveInfo() {
	}

	@DataBoundConstructor
	public GraphOptionsCurveInfo(final String path) {
		this.path = path;
	}
	
	/** @return the path */
	public String getPath() {
		return path;
	}

	/** @param path the path to set */
	@DataBoundSetter
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int compare(final GraphOptionsCurveInfo o1, final GraphOptionsCurveInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	public int compareTo(final GraphOptionsCurveInfo o) {
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
    public static class DescriptorImpl extends Descriptor<GraphOptionsCurveInfo> {
		public String getDisplayName() {
			return "This display name serves no purpose.";
		}
	}
}
