package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.Serializable;

public class PluginUtils implements Serializable {
	
	/** Utility classes are not intended to be instantiated. */
	private PluginUtils() {
		throw new IllegalAccessError();
	}
	
    /** Get the configured instance for the plugin.
	 * @param project
	 * @return
	 */
    public static NeoLoadPluginOptions getPluginOptions(AbstractProject<?, ?> project) {
		NeoLoadPluginOptions npo = null;

		// look through all post build steps for the correct one.
    	DescribableList<Publisher,Descriptor<Publisher>> pubs = project.getPublishersList();
		for (Publisher p : pubs) {
			if (p instanceof NeoLoadPluginOptions) {
				npo = (NeoLoadPluginOptions) p;
				break;
			}
		}

		return npo;
	}

}
