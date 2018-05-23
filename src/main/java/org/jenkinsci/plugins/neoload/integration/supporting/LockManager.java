package org.jenkinsci.plugins.neoload.integration.supporting;

import hudson.model.AbstractProject;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Lock manager.
 */
public class LockManager {
	private final Set<String> lockSet = new HashSet<>();

	/**
	 * Get key from project string.
	 *
	 * @param project the project
	 * @return the string
	 */
	static final String getKeyFromProject(AbstractProject project){
		return project.getUrl();
	}

	/**
	 * Instantiates a new Lock manager.
	 */
	public LockManager() {
	}


	/**
	 * Unlock.
	 *
	 * @param project the project
	 */
	public synchronized void unlock(AbstractProject project){
		lockSet.remove(getKeyFromProject(project));
	}

	/**
	 * Is locked boolean.
	 *
	 * @param project the project
	 * @return the boolean
	 */
	public synchronized boolean isLocked(AbstractProject project){
		return lockSet.contains(getKeyFromProject(project));
	}

	/**
	 * Try lock boolean.
	 *
	 * @param project the project
	 * @return the boolean
	 */
	public synchronized boolean tryLock(AbstractProject project){
		final String key = getKeyFromProject(project);
		if(lockSet.contains(key)){
			return false;
		}else{
			lockSet.add(key);
			return true;
		}
	}
}
