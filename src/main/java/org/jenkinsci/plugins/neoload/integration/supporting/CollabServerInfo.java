package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

public class CollabServerInfo extends ServerInfo implements Serializable, Comparable<CollabServerInfo>, Comparator<CollabServerInfo> {

	/** Generated. */
	private static final long serialVersionUID = -3421381227895425218L;

	private String privateKey;
	private String passphrase;

	public CollabServerInfo() {
	}

	@DataBoundConstructor
	public CollabServerInfo(final String uniqueID, final String url, final String loginUser, final String loginPassword, 
			final String privateKey, final String passphrase) {
		super(uniqueID, url, loginUser, loginPassword);
		this.privateKey = privateKey;
		this.passphrase = passphrase;
	}

	/** @return the privateKey */
	public String getPrivateKey() {
		return privateKey;
	}

	/** @param privateKey the privateKey to set */
	public void setPrivateKey(final String privateKey) {
		this.privateKey = privateKey;
	}

	/** @return the passphrase  */
	public String getPassphrase() {
		return passphrase;
	}

	/** @param passphrase the passphrase to set  */
	public void setPassphrase(final String passphrase) {
		this.passphrase = passphrase;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int compare(final CollabServerInfo o1, final CollabServerInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	public int compareTo(final CollabServerInfo o) {
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
