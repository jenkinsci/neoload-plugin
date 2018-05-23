package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The type Collab server info.
 */
public class CollabServerInfo extends ServerInfo implements Serializable, Comparable<CollabServerInfo>, Comparator<CollabServerInfo> {

	/** Generated. */
	private static final long serialVersionUID = -3421381227895425218L;

	private String privateKey;
	private String passphrase;

	/**
	 * Instantiates a new Collab server info.
	 */
	public CollabServerInfo() {
	}

	/**
	 * Instantiates a new Collab server info.
	 *
	 * @param uniqueID      the unique id
	 * @param url           the url
	 * @param loginUser     the login user
	 * @param loginPassword the login password
	 * @param label         the label
	 * @param privateKey    the private key
	 * @param passphrase    the passphrase
	 */
	@DataBoundConstructor
	public CollabServerInfo(final String uniqueID, final String url, final String loginUser, final String loginPassword, final String label, 
			final String privateKey, final String passphrase) {
		super(uniqueID, url, loginUser, loginPassword, label);
		this.privateKey = privateKey;
		this.passphrase = passphrase;
	}

	/**
	 * Gets private key.
	 *
	 * @return the privateKey
	 */
	public String getPrivateKey() {
		return privateKey;
	}

	/**
	 * Sets private key.
	 *
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(final String privateKey) {
		this.privateKey = privateKey;
	}

	/**
	 * Gets passphrase.
	 *
	 * @return the passphrase
	 */
	public String getPassphrase() {
		return passphrase;
	}

	/**
	 * Sets passphrase.
	 *
	 * @param passphrase the passphrase to set
	 */
	public void setPassphrase(final String passphrase) {
		this.passphrase = passphrase;
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
	public int compare(final CollabServerInfo o1, final CollabServerInfo o2) {
		return CompareToBuilder.reflectionCompare(o1, o2);
	}

	/**
	 * Compare to int.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareTo(final CollabServerInfo o) {
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
