package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.model.Action;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.neoload_integration.NeoResultsAction;

public class PluginUtils implements Serializable {
	
	/** Log various messages. */
	private static Logger logger = Logger.getLogger(PluginUtils.class.getName());

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
