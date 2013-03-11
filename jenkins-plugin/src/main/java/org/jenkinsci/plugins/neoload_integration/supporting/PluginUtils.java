package org.jenkinsci.plugins.neoload_integration.supporting;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.util.LogTaskListener;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;


import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;

public class PluginUtils implements Serializable {
	
	/** The environment variable for the workspace. */
	private static final String ENV_WORKSPACE = "WORKSPACE";

	/** Log various messages. */
	private static final Logger logger = Logger.getLogger(PluginUtils.class.getName());
	
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
    	
	/**
	 * @param r
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static String getWorkspace(Run<?, ?> r) throws IOException, InterruptedException {
		EnvVars vars = r.getEnvironment(new LogTaskListener(logger, Level.INFO));
		String workspace = vars.get(ENV_WORKSPACE);
		
		return workspace + getNeotysWorkspacePath();
	}
	
	public static String getWorkspace(AbstractBuild<?, ?> build) {
		return build.getWorkspace() + getNeotysWorkspacePath();
	}
	
	/**
	 * @return
	 */
	private static String getNeotysWorkspacePath() {
		return File.separatorChar + "neotys-results" + File.separatorChar;
	}

	/** I give you a list of file names from a list of file objects.
	 * @param fileObjects a list of file objects.
	 * @param includePath if true then the full file path is included. false means I'll only return the names of the files.
	 * @return
	 */
	public static List<String> getFileNames(Collection<File> fileObjects, boolean includePath) {
		List<String> fileNames = new ArrayList<>(fileObjects.size());
		
		for (File f: fileObjects) {
			if (includePath) {
				fileNames.add(f.getAbsolutePath());
			} else {
				fileNames.add(f.getName());
			}
		}
		
		return fileNames;
	}

	/**
	 * @param reportFileLocation 
	 * @param workspace
	 * @return
	 */
	public static String findReportFileLocation(String reportFileLocation, String workspace) {
		// possibilities: full path, path relative to workspace, no path at all (blank).
		
		if (StringUtils.trimToNull(reportFileLocation) == null) {
			// blank path: fail or search the workspace to find the file
			
		} else if (new File(workspace + File.separatorChar + reportFileLocation).exists()) {
			// the path is relative to the workspace
			reportFileLocation = workspace + File.separatorChar + reportFileLocation;

		} else if (new File(reportFileLocation).exists()) {
			// the full path is given
		}
		
		return reportFileLocation;
	}

	/** Replace environment variables.
	 * @param orig
	 * @param envVars
	 * @return
	 */
	public static String doReplacements(final String orig, final EnvVars envVars) {
		String str = orig;
		
		for (String key: envVars.keySet()) {
			str = str.replaceAll("(?i)\\$\\{" + key + "\\}", Matcher.quoteReplacement(envVars.get(key)));
		}
		
		return str;
	}

	/**
	 * @param reportFiles
	 * @return
	 * @throws IOException
	 */
	public static String getXMLFilePath(String reportFiles) throws IOException {
		List<String> files = Collections.EMPTY_LIST;
		String file = null;
		File reportFileDir = new File(reportFiles);
		
		// create the directory if necessary
		if (reportFileDir.exists()) {
			// get a list of all files in the directory
			files = FileUtils.getFileNames(new File(reportFiles), null, null, true);
		} else {
			logger.log(Level.INFO, "Report file directory not found. I'm looking for " + reportFiles);
		}
		
		// look for the xml file, because using the includes paramter in getFileNames() didn't work.
		for (String f: files) {
			if (f.matches("(?i).*xml")) {
				file = f;
				break;
			}
		}
		
		if (file == null) {
			logger.log(Level.INFO, "Can't find NeoLoad xml results file. I'm looking in " + reportFiles);
		}
		
		return file;
	}

	/** Get all builds for a job.
	 * @param build
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<AbstractBuild> getAllBuilds(final AbstractBuild build) {
		List<AbstractBuild> allBuilds = new ArrayList<>();
		AbstractBuild pointer = build;
		
		// get all builds after the current one
		while (pointer != null) {
			allBuilds.add(pointer);
			
			pointer = pointer.getNextBuild();
		}
		
		// get all builds before the current one
		pointer = build.getPreviousBuild();
		while (pointer != null) {
			allBuilds.add(0, pointer);
			
			pointer = pointer.getPreviousBuild();
		}
		
		return allBuilds;
	}

}
