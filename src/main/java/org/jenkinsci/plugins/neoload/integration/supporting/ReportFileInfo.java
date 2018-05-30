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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * The type Report file info.
 */
public class ReportFileInfo implements Serializable {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 4505907991423991607L;
	private String path;

	/**
	 * Instantiates a new Report file info.
	 */
	public ReportFileInfo() {
	}

	/**
	 * Instantiates a new Report file info.
	 *
	 * @param path          the path
	 * @param loginUser     the login user
	 * @param loginPassword the login password
	 */
	@DataBoundConstructor
	public ReportFileInfo(final String path, final String loginUser, final String loginPassword) {
		this.path = path;
	}

	/**
	 * Gets path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets path.
	 *
	 * @param url the url
	 */
	public void setPath(final String url) {
		this.path = url;
	}

	/**
	 * To string string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
