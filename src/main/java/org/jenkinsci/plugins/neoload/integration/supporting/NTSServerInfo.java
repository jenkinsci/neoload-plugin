package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The type Nts server info.
 */
public class NTSServerInfo extends ServerInfo implements Serializable, Comparable<NTSServerInfo>, Comparator<NTSServerInfo> {

	/** Generated. */
	private static final long serialVersionUID = 3912702075580266980L;

	private String collabPath;
	private String licenseID;

	/**
	 * Instantiates a new Nts server info.
	 */
	public NTSServerInfo() {
	}

	/**
	 * Instantiates a new Nts server info.
	 *
	 * @param uniqueID      the unique id
	 * @param url           the url
	 * @param loginUser     the login user
	 * @param loginPassword the login password
	 * @param label         the label
	 * @param collabPath    the collab path
	 * @param licenseID     the license id
	 */
	@DataBoundConstructor
	public NTSServerInfo(final String uniqueID, final String url, final String loginUser, final String loginPassword, final String label, 
			final String collabPath, final String licenseID) {
		super(uniqueID, url, loginUser, loginPassword, label);
		this.collabPath = collabPath;
		this.licenseID = licenseID;
	}

	/**
	 * Gets collab path.
	 *
	 * @return the collab path
	 */
	public String getCollabPath() {
		return collabPath;
	}

	/**
	 * Sets collab path.
	 *
	 * @param collabPath the collab path
	 */
	public void setCollabPath(final String collabPath) {
		this.collabPath = collabPath;
	}

	/**
	 * Gets license id.
	 *
	 * @return the license id
	 */
	public String getLicenseID() {
		return licenseID;
	}

	/**
	 * Sets license id.
	 *
	 * @param licenseID the license id
	 */
	public void setLicenseID(final String licenseID) {
		this.licenseID = licenseID;
	}

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
	public int compare(final NTSServerInfo o1, final NTSServerInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	/**
	 * Compare to int.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareTo(final NTSServerInfo o) {
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
}
