package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateFormatUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class ServerInfo implements Serializable {

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(ServerInfo.class.getName());

	/** Generated. */
	private static final long serialVersionUID = 460155244760235756L;

	private String uniqueID = "";
	private String url;
	private String loginUser;
	private String loginPassword;

	public ServerInfo() {
	}

	@DataBoundConstructor
	public ServerInfo(final String uniqueID, final String url, final String loginUser, final String loginPassword) {
		this.uniqueID = uniqueID;
		this.url = url;
		this.loginUser = loginUser;
		this.loginPassword = loginPassword;

		// we assign a new unique ID only if necessary. we don't want to overwrite an existing unique ID.
		if (StringUtils.trimToNull(this.uniqueID) == null) {
			this.uniqueID = getUniqueID();
		}
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(final String url) {
		this.url = url;
	}
	public String getLoginUser() {
		return loginUser;
	}
	public void setLoginUser(final String loginUser) {
		this.loginUser = loginUser;
	}
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
	public void setLoginPassword(final String loginPassword) {
		this.loginPassword = loginPassword;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public void setUniqueID(final String uniqueID) {
		this.uniqueID = uniqueID;
	}
	public String getUniqueID() {
		if (StringUtils.trimToEmpty(uniqueID).length() == 0) {
			synchronized (ServerInfo.class) {
				return DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd kk:mm:ss.SSS (Z)");
			}
		}

		return uniqueID;
	}

	public String getUniqueIDDoNotGenerate() {
		return uniqueID;
	}
}
