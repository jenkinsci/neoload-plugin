package org.jenkinsci.plugins.neoload_integration;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientBuildActionFactory;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.jenkinsci.plugins.neoload_integration.supporting.NeoLoadPluginOptions;

/** Necessary Jenkins fluff to make the associated class actually be taken into account. */
@Extension(optional = true)
public final class NeoTransientBuildActionFactory extends TransientBuildActionFactory {

	/** Log various messages. */
	private static final Logger logger = Logger.getLogger(NeoTransientBuildActionFactory.class.getName());

	@Override
	public Collection<? extends Action> createFor(AbstractBuild target) {
		NeoResultsAction nra = new NeoResultsAction(target);

		try {
			if (nra.getHtmlReportFilePath() != null) {
				return Collections.singleton(nra);
			}
		} catch (IOException e) {
			logger.severe(NeoLoadPluginOptions.LOG_PREFIX + e.getMessage());
			e.printStackTrace();
		}

		return Collections.EMPTY_LIST;
	}

}
