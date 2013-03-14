package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;

import java.io.Serializable;
import java.util.Map;

public class PluginUtils implements Serializable {

    /** Get the configured instance for the plugin.
	 * @param project
	 * @return
	 */
    public static NeoLoadPluginOptions getPluginOptions(AbstractProject<?, ?> project) {
		NeoLoadPluginOptions npo = null;

		// look through all job properties for the correct one
		Map<JobPropertyDescriptor, ?> props = project.getProperties();
		for (Object jobProperty: props.values()) {
			if (jobProperty instanceof NeoLoadPluginOptions) {
				npo = (NeoLoadPluginOptions) jobProperty;
				break;
			}
		}

		return npo;
	}

}
