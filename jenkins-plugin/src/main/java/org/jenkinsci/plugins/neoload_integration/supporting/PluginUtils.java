package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.neoload_integration.NeoResultsAction;

public class PluginUtils implements Serializable {
	
	/** Utility classes are not intended to be instantiated. */
	private PluginUtils() {
		throw new IllegalAccessError();
	}
	
	/** Log various messages. */
	private static Logger logger = Logger.getLogger(PluginUtils.class.getName());

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
    
    /**
     * @param build
     */
    public static void addActionIfNotExists(AbstractBuild<?, ?> build) {
    	boolean alreadyAdded = false;
    	for (Action a: build.getActions()) {
    		if (a instanceof NeoResultsAction) {
    			alreadyAdded = true;
    			break;
    		}
    	}
    	
    	if (!alreadyAdded) {
    		NeoResultsAction nra = new NeoResultsAction(build);
    		build.addAction(nra);
    		logger.log(Level.INFO, "Added Performance Result link to build " + build.number + " of job " + 
    				build.getProject().getDisplayName());
    	}
    }

}
