/*
 * Copyright (c) 2013, Neotys
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
