package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.Serializable;

public class PluginUtils implements Serializable {

    /** Get the configured instance for the plugin.
     * @param project
     * @return
     */
    public static NeoLoadPluginOptions getPluginOptions(AbstractProject<?, ?> project) {
    	NeoLoadPluginOptions npba = null;
    	
    	// look through all post build steps for the project
    	DescribableList<Publisher,Descriptor<Publisher>> pubs = project.getPublishersList();
    	for (Publisher p: pubs) {
    		if (p instanceof NeoLoadPluginOptions) {
    			npba = (NeoLoadPluginOptions) p;
    			break;
    		}
    	}
    	
    	return npba;
    }

}
