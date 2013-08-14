package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public final class PluginUtils implements Serializable {
	
	/** Generated. */
	private static final long serialVersionUID = -3063042074729452263L;

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
    
    /** This could be DateUtils.toCalendar instead but then I would have to deal with maven dependencies again.
     * @param date
     * @return
     */
    public static Calendar toCalendar(Date date) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(date);
    	
    	return cal;
    }

}
