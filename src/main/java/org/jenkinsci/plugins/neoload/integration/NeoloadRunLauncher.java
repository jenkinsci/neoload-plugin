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
package org.jenkinsci.plugins.neoload.integration;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.ChannelClosedException;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.neoload.integration.NeoBuildAction.isOsWindows;

public class NeoloadRunLauncher extends CommandInterpreter {

	private static final Logger LOGGER = Logger.getLogger(NeoloadRunLauncher.class.getName());

	private final boolean isUnix;

	public NeoloadRunLauncher(String command, Launcher launcher) {
		super(command);
		isUnix = launcher.isUnix();
	}

	@Override
	public String[] buildCommandLine(FilePath script) {
		if (isUnix){
			return shellCommandLine(script);
		}else{
			return batchCommandLine(script);
		}
	}

	/**
	 * Function duplicated from CommandInterpreter with a transform to work with new "Run" object
	 * for pipeline functionality
	 */
	public boolean perform(Run<?,?> build, FilePath ws, Launcher launcher, TaskListener listener) throws InterruptedException {

		LOGGER.log(Level.FINEST, "Executing command: " + command);
		FilePath script=null;
		int r = -1;
		try {
			try {
				script = createScriptFile(ws);
			} catch (IOException e) {
				Util.displayIOException(e,listener);
				e.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_UnableToProduceScript()));
				return false;
			}

			try {
				EnvVars envVars = build.getEnvironment(listener);
				File f = new File(ws.getRemote());
				if (! f.isDirectory() && ! f.exists()){
					f.mkdirs();
				}
				r = join(launcher.launch().cmds(buildCommandLine(script)).envs(envVars).stdout(listener).pwd(ws).start());
			} catch (IOException e) {
				Util.displayIOException(e, listener);
				e.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_CommandFailed()));
			}
			return r==0;
		} finally {
			try {
				if(script!=null)
					script.delete();
			} catch (IOException e) {
				if (r==-1 && e.getCause() instanceof ChannelClosedException) {
					// JENKINS-5073
					// r==-1 only when the execution of the command resulted in IOException,
					// and we've already reported that error. A common error there is channel
					// losing a connection, and in that case we don't want to confuse users
					// by reporting the 2nd problem. Technically the 1st exception may not be
					// a channel closed error, but that's rare enough, and JENKINS-5073 is common enough
					// that this suppressing of the error would be justified
					LOGGER.log(Level.FINE, "Script deletion failed", e);
				} else {
					Util.displayIOException(e,listener);
					e.printStackTrace( listener.fatalError(hudson.tasks.Messages.CommandInterpreter_UnableToDelete(script)) );
				}
			} catch (Exception e) {
				e.printStackTrace( listener.fatalError(hudson.tasks.Messages.CommandInterpreter_UnableToDelete(script)) );
			}
			return true;
		}
	}

	//BatchFile
	public String[] batchCommandLine(FilePath script) {
		return new String[] {"cmd","/c","call",script.getRemote()};
	}

	//Shell
	/**
	 * Older versions of bash have a bug where non-ASCII on the first line
	 * makes the shell think the file is a binary file and not a script. Adding
	 * a leading line feed works around this problem.
	 */
	public String[] shellCommandLine(FilePath script) {
		return new Shell(command).buildCommandLine(script);
	}

	private static String addLineFeedForNonASCII(String s) {
		if(!s.startsWith("#!")) {
			if (s.indexOf('\n')!=0) {
				return "\n" + s;
			}
		}
		return s;
	}

	// /!\ The code below (till eof) has been duplicated from NeoBuildAction !!!!
	// It would need refactoring but has not been done because Jenkins works
	// with the reflectiveness of code and refactoring might have cause regression
	@Override
	protected String getContents() {
		if (SystemUtils.IS_OS_WINDOWS) {
			return new BatchFileMine().getContents();
		}
		return new ShellMine().getContents();
	}

	@Override
	protected String getFileExtension() {
		if (SystemUtils.IS_OS_WINDOWS) {
			return new BatchFileMine().getFileExtension();
		}
		return new ShellMine().getFileExtension();
	}

	public class BatchFileMine extends BatchFile {
		public BatchFileMine() {
			super(NeoloadRunLauncher.this.command);
		}

		@Override
		public String getContents() {
			return super.getContents();
		}

		@Override
		public String getFileExtension() {
			return super.getFileExtension();
		}
	}

	public class ShellMine extends Shell {
		public ShellMine() {
			super(NeoloadRunLauncher.this.command);
		}

		@Override
		public String getContents() {
			return super.getContents();
		}

		@Override
		public String getFileExtension() {
			return super.getFileExtension();
		}
	}
}
