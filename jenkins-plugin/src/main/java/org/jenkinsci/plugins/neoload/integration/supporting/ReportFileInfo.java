package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

public class ReportFileInfo implements Serializable {

	private String path;

	/** Generated. */
	private static final long serialVersionUID = 4505907991423991607L;

	public ReportFileInfo() {
	}

	@DataBoundConstructor
	public ReportFileInfo(final String path, final String loginUser, final String loginPassword) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	public void setPath(final String url) {
		this.path = url;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
