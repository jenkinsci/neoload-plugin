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

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The type Graph options curve info.
 */
public class GraphOptionsCurveInfo extends AbstractDescribableImpl<GraphOptionsCurveInfo>
		implements Serializable, Comparable<GraphOptionsCurveInfo>, Comparator<GraphOptionsCurveInfo> {

	/**
	 * Generated.
	 */
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
		return EqualsBuilder.reflectionEquals(this, obj);
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
