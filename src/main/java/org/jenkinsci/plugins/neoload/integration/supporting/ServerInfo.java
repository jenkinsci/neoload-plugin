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
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Server info.
 */
public class ServerInfo extends AbstractStepImpl implements Serializable {

	/**
	 * Log various messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(ServerInfo.class.getName());

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 460155244760235756L;

	private String uniqueID = "";
	private String url;
	private String loginUser;
	private String loginPassword;
	private String label;

	/**
	 * Instantiates a new Server info.
	 */
	public ServerInfo() {
	}

	/**
	 * Instantiates a new Server info.
	 *
	 * @param uniqueID      the unique id
	 * @param url           the url
	 * @param loginUser     the login user
	 * @param loginPassword the login password
	 * @param label         the label
	 */
	@DataBoundConstructor
	public ServerInfo(final String uniqueID, final String url, final String loginUser, final String loginPassword, final String label) {
		this.uniqueID = uniqueID;
		this.url = url;
		this.loginUser = loginUser;
		this.loginPassword = loginPassword;
		this.label = label;

		// we assign a new unique ID only if necessary. we don't want to overwrite an existing unique ID.
		if (StringUtils.trimToNull(this.uniqueID) == null) {
			this.uniqueID = getUniqueID();
		}
	}

	/**
	 * Gets url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets url.
	 *
	 * @param url the url
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * Gets login user.
	 *
	 * @return the login user
	 */
	public String getLoginUser() {
		return loginUser;
	}

	/**
	 * Sets login user.
	 *
	 * @param loginUser the login user
	 */
	public void setLoginUser(final String loginUser) {
		this.loginUser = loginUser;
	}

	/**
	 * Gets login password.
	 *
	 * @return the login password
	 */
	public String getLoginPassword() {
		// try to decode the password.
		try {
			final String decoded = PluginUtils.decode(loginPassword);
			return decoded;

		} catch (final DecoderException e) {
			// this happens during normal usage when saving the password on the config page.
			LOGGER.log(Level.FINEST, "Issue decoding password for server. URL: " + url + ", user: " + loginUser +
					", message: " + e.getMessage());
		}

		return loginPassword;
	}

	/**
	 * Sets login password.
	 *
	 * @param loginPassword the login password
	 */
	public void setLoginPassword(final String loginPassword) {
		this.loginPassword = loginPassword;
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
	 * Gets unique id.
	 *
	 * @return the unique id
	 */
	public String getUniqueID() {
		if (StringUtils.trimToEmpty(uniqueID).length() == 0) {
			synchronized (ServerInfo.class) {
				return UUID.randomUUID().toString();
			}
		}
		return uniqueID;
	}


	public String getNonEmptyLabel(boolean forCollab){
		if(getLabel().trim().isEmpty()) {
			return getUrl() + ", User: " + getLoginUser();
		}else{
			return getLabel();
		}
	}
	/**
	 * Sets unique id.
	 *
	 * @param uniqueID the unique id
	 */
	public void setUniqueID(final String uniqueID) {
		this.uniqueID = uniqueID;
	}

	/**
	 * Gets unique id do not generate.
	 *
	 * @return the unique id do not generate
	 */
	public String getUniqueIDDoNotGenerate() {
		return uniqueID;
	}

	/**
	 * Gets label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * Sets label.
	 *
	 * @param label the label
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * The type Descriptor.
	 */
	@Extension
	public static class DescriptorImpl {

		/**
		 * Gets display name.
		 *
		 * @return the display name
		 */
		public String getDisplayName() {
			return "ServerInfo";
		}
	}
}
