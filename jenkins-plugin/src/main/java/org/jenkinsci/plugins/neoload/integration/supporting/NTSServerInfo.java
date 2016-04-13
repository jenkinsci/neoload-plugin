package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

public class NTSServerInfo extends ServerInfo implements Serializable, Comparable<NTSServerInfo>, Comparator<NTSServerInfo> {

	/** Generated. */
	private static final long serialVersionUID = 3912702075580266980L;

	private String collabPath;
	private String licenseID;

	public NTSServerInfo() {
	}

	@DataBoundConstructor
	public NTSServerInfo(final String uniqueID, final String url, final String loginUser, final String loginPassword, 
			final String collabPath, final String licenseID) {
		super(uniqueID, url, loginUser, loginPassword);
		this.collabPath = collabPath;
		this.licenseID = licenseID;
	}

	public String getCollabPath() {
		return collabPath;
	}
	public void setCollabPath(final String collabPath) {
		this.collabPath = collabPath;
	}
	public String getLicenseID() {
		return licenseID;
	}
	public void setLicenseID(final String licenseID) {
		this.licenseID = licenseID;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int compare(final NTSServerInfo o1, final NTSServerInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	public int compareTo(final NTSServerInfo o) {
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
}
