package org.jenkinsci.plugins.neoload_integration.supporting;

public interface NeoLoadPluginOptions {
	
	/** Prefix output messages with this. */
	public static final String LOG_PREFIX = "NeoLoad Integration: ";

	/** @return the showTrendAverageResponse */
	public abstract boolean isShowTrendAverageResponse();

	/** @return the showTrendErrorRate */
	public abstract boolean isShowTrendErrorRate();
}