package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The type Report file info.
 */
public class ReportFileInfo implements Serializable {

	private String path;

	/** Generated. */
	private static final long serialVersionUID = 4505907991423991607L;

	/**
	 * Instantiates a new Report file info.
	 */
	public ReportFileInfo() {
	}

	/**
	 * Instantiates a new Report file info.
	 *
	 * @param path          the path
	 * @param loginUser     the login user
	 * @param loginPassword the login password
	 */
	@DataBoundConstructor
	public ReportFileInfo(final String path, final String loginUser, final String loginPassword) {
		this.path = path;
	}

	/**
	 * Gets path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets path.
	 *
	 * @param url the url
	 */
	public void setPath(final String url) {
		this.path = url;
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

}
