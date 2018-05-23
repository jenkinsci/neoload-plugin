package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateFormatUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The type Server info.
 */
public class ServerInfo implements Serializable {

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(ServerInfo.class.getName());

	/** Generated. */
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
	 * Sets unique id.
	 *
	 * @param uniqueID the unique id
	 */
	public void setUniqueID(final String uniqueID) {
		this.uniqueID = uniqueID;
	}

	/**
	 * Gets unique id.
	 *
	 * @return the unique id
	 */
	public String getUniqueID() {
		if (StringUtils.trimToEmpty(uniqueID).length() == 0) {
			synchronized (ServerInfo.class) {
				return DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd kk:mm:ss.SSS (Z)");
			}
		}

		return uniqueID;
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
}
