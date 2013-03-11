package org.jenkinsci.plugins.neoload_integration;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientBuildActionFactory;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.apache.log4j.Level;

/**
 * A very simple {@link hudson.model.TransientProjectActionFactory} which
 * creates new {@link NeoResultsAction}s for the target
 * {@link hudson.model.AbstractProject}.
 * 
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
@Extension
public final class NeoTransientBuildActionFactory extends TransientBuildActionFactory {
	
	/** Log various messages. */
	private static final Logger logger = Logger.getLogger(NeoTransientBuildActionFactory.class.getName());
    
    @Override
    public Collection<? extends Action> createFor(AbstractBuild target) {
    	NeoResultsAction nra = new NeoResultsAction(target);
    	
    	try {
			if (nra.getHtmlReportFilePath() != null) {
				return Collections.singleton(new NeoResultsAction(target));
			}
		} catch (IOException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
    	
    	return Collections.EMPTY_LIST;
    }
    
}
