/*
 * Copyright (c) 2018, Neotys
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
package org.jenkinsci.plugins.neoload.integration.supporting;

import hudson.model.Job;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Lock manager.
 */
public class LockManager {
	private final Set<String> lockSet = new HashSet<>();

	/**
	 * Instantiates a new Lock manager.
	 */
	public LockManager() {
	}

	/**
	 * Gets key from project.
	 *
	 * @param project the project
	 * @return the key from project
	 */
	static final String getKeyFromProject(Job project) {
		return project.getUrl();
	}

	/**
	 * Unlock.
	 *
	 * @param project the project
	 */
	public synchronized void unlock(Job project) {
		lockSet.remove(getKeyFromProject(project));
	}

	/**
	 * Is locked boolean.
	 *
	 * @param project the project
	 * @return the boolean
	 */
	public synchronized boolean isLocked(Job project) {
		return lockSet.contains(getKeyFromProject(project));
	}

	/**
	 * Try lock boolean.
	 *
	 * @param project the project
	 * @return the boolean
	 */
	public synchronized boolean tryLock(Job project) {
		final String key = getKeyFromProject(project);
		if (lockSet.contains(key)) {
			return false;
		} else {
			lockSet.add(key);
			return true;
		}
	}
}
