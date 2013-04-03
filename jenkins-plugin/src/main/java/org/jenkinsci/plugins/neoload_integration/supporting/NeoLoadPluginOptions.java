package org.jenkinsci.plugins.neoload_integration.supporting;

public interface NeoLoadPluginOptions {
	
	/** @return the showTrendAverageResponse */
	public abstract boolean isShowTrendAverageResponse();

	/** @return the showTrendErrorRate */
	public abstract boolean isShowTrendErrorRate();
	
}