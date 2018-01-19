package org.jenkinsci.plugins.neoload.integration.supporting;

import hudson.model.AbstractProject;

import java.util.HashSet;
import java.util.Set;

public class LockManager {
	private final Set<String> lockSet = new HashSet<>();
	static final String getKeyFromProject(AbstractProject project){
		return project.getUrl();
	}
	public LockManager() {
	}


	public synchronized void unlock(AbstractProject project){
		lockSet.remove(getKeyFromProject(project));
	}
	public synchronized boolean isLocked(AbstractProject project){
		return lockSet.contains(getKeyFromProject(project));
	}
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
