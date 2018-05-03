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
	public CollabServerInfo(final String uniqueID, final String url, final String loginUser, final String loginPassword, final String label, 
			final String privateKey, final String passphrase) {
		super(uniqueID, url, loginUser, loginPassword, label);
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
