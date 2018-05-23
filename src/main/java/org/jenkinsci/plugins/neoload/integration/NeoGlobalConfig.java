package org.jenkinsci.plugins.neoload.integration;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.collections.CollectionUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.CollabServerInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.NTSServerInfo;
import org.jenkinsci.plugins.neoload.integration.supporting.PluginUtils;
import org.jenkinsci.plugins.neoload.integration.supporting.ServerInfo;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

/**
 * The type Neo global config.
 */
public class NeoGlobalConfig extends GlobalConfiguration implements Serializable {

	/** Generated. */
	private static final long serialVersionUID = -7914538879734307398L;

	/** Log various messages. */
	private static final Logger LOGGER = Logger.getLogger(NeoGlobalConfig.class.getName());

	/**
	 * Instantiates a new Neo global config.
	 */
	public NeoGlobalConfig() {
	}

	/**
	 * The type Descriptor.
	 */
	@Extension
	public static final class DescriptorImpl extends Descriptor<GlobalConfiguration> {

		private List<NTSServerInfo> ntsInfo = Collections.emptyList();
		private List<CollabServerInfo> collabInfo = Collections.emptyList();

		/**
		 * Instantiates a new Descriptor.
		 */
		public DescriptorImpl() {
			super(NeoGlobalConfig.class);
			load();
		}

		/**
		 * Gets display name.
		 *
		 * @return the display name
		 */
		@Override
		public String getDisplayName() {
			return "NeoLoad Plugin Global Config";
		}

		/**
		 * Configure boolean.
		 *
		 * @param req  the req
		 * @param json the json
		 * @return the boolean
		 * @throws FormException the form exception
		 */
		@Override
		public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
			// this could be a JSONObject or a JSONArray.
			final Object ntsInfoJson = json.get("ntsInfoName");
			final List<NTSServerInfo> tempNTSInfo = req.bindJSONToList(NTSServerInfo.class, ntsInfoJson);

			final Object collabInfoJson = json.get("collabInfoName");
			final List<CollabServerInfo> tempCollabInfo = req.bindJSONToList(CollabServerInfo.class, collabInfoJson);

			// known issue: action: create a new server, click "Apply" to save the settings multiple times.
			// result: the uniqueID changes every time the apply button is clicked. this is because the hidden
			// uniqueID field is always sent as blank. once the user reloads the page the issue is fixed.
			ntsInfo = tempNTSInfo == null ? Collections.<NTSServerInfo> emptyList() : tempNTSInfo;
			collabInfo = tempCollabInfo == null ? Collections.<CollabServerInfo> emptyList() : tempCollabInfo;

			final Collection<ServerInfo> allServerInfo = CollectionUtils.union(ntsInfo, collabInfo);
			// encode the passwords so that they're not plain text
			for (final ServerInfo info : allServerInfo) {
				final String plainTextPassword = info.getLoginPassword();
				try {
					final String encoded = PluginUtils.encode(plainTextPassword);
					info.setLoginPassword(encoded);

				} catch (final EncoderException e) {
					LOGGER.log(Level.SEVERE, "Issue encoding password.", e);
				}
			}

			save();
			return true;
		}

		/**
		 * Gets nts info.
		 *
		 * @return the nts info
		 */
		public List<NTSServerInfo> getNtsInfo() {
			return ntsInfo;
		}

		/**
		 * Sets nts info.
		 *
		 * @param ntsInfo the nts info
		 */
		public void setNtsInfo(final List<NTSServerInfo> ntsInfo) {
			this.ntsInfo = ntsInfo;
		}

		/**
		 * Do check url form validation.
		 *
		 * @param url the url
		 * @return the form validation
		 */
		public FormValidation doCheckUrl(@QueryParameter final String url) {
			return PluginUtils.validateURL(url);
		}

		/**
		 * Do check login user form validation.
		 *
		 * @param loginUser the login user
		 * @return the form validation
		 */
		public FormValidation doCheckLoginUser(@QueryParameter final String loginUser) {
			return PluginUtils.validateWarnIfEmpty(loginUser, "user");
		}

		/**
		 * Do check login password form validation.
		 *
		 * @param loginPassword the login password
		 * @return the form validation
		 */
		public FormValidation doCheckLoginPassword(@QueryParameter final String loginPassword) {
			return PluginUtils.validateWarnIfEmpty(loginPassword, "password");
		}

		/**
		 * Do check private key form validation.
		 *
		 * @param privateKey the private key
		 * @return the form validation
		 */
		public FormValidation doCheckPrivateKey(@QueryParameter final String privateKey) {
			return PluginUtils.validateFileExists(privateKey, "", false, false);
		}

		/**
		 * Gets collab info.
		 *
		 * @return the collab info
		 */
		public List<CollabServerInfo> getCollabInfo() {
			return collabInfo;
		}

		/**
		 * Sets collab info.
		 *
		 * @param collabInfo the collab info
		 */
		public void setCollabInfo(final List<CollabServerInfo> collabInfo) {
			this.collabInfo = collabInfo;
		}
	}
}
