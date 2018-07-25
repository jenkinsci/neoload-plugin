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

import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.ChannelClosedException;
import hudson.tasks.*;
import hudson.tasks.Messages;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Neoload run launcher.
 */
public class NeoloadRunLauncher extends CommandInterpreter {

	private static final Logger LOGGER = Logger.getLogger(NeoloadRunLauncher.class.getName());


	private final MyInterpreter interpreter;

	/**
	 * Instantiates a new Neoload run launcher.
	 *
	 * @param command  the command
	 * @param launcher the launcher
	 */
	public NeoloadRunLauncher(String command, Launcher launcher) {
		super(command);
		if (launcher.isUnix()) {
			interpreter = new ShellMine(command);
		}else{
			interpreter = new BatchFileMine(command);
		}
	}

	@Override
	public String[] buildCommandLine(FilePath script) {
		return interpreter.buildCommandLine(script);
	}

	/**
	 * Function duplicated from CommandInterpreter with a transform to work with new "Run" object
	 * for pipeline functionality
	 *
	 * @param build    the build
	 * @param ws       the ws
	 * @param launcher the launcher
	 * @param listener the listener
	 * @return the boolean
	 * @throws InterruptedException the interrupted exception
	 */
	public boolean perform(Run<?, ?> build, FilePath ws, Launcher launcher, TaskListener listener) throws InterruptedException {

		LOGGER.log(Level.FINEST, "Executing command: " + command);
		FilePath script = null;
		int r = -1;
		try {
			try {
				script = createScriptFile(ws);
			} catch (IOException e) {
				Util.displayIOException(e, listener);
				e.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_UnableToProduceScript()));
				return false;
			}

			try {
				EnvVars envVars = build.getEnvironment(listener);
				ws.mkdirs();
				r = join(launcher.launch().cmds(buildCommandLine(script)).envs(envVars).stdout(listener).pwd(ws).start());
			} catch (IOException e) {
				Util.displayIOException(e, listener);
				e.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_CommandFailed()));
			} catch (Throwable th){
				th.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_CommandFailed()));
			}
			//LOGGER.log(Level.FINEST, "returned: " + r);
			return r == 0;
		} finally {
			try {
				if (script != null)
					script.delete();
			} catch (IOException e) {
				if (r == -1 && e.getCause() instanceof ChannelClosedException) {
					// JENKINS-5073
					// r==-1 only when the execution of the command resulted in IOException,
					// and we've already reported that error. A common error there is channel
					// losing a connection, and in that case we don't want to confuse users
					// by reporting the 2nd problem. Technically the 1st exception may not be
					// a channel closed error, but that's rare enough, and JENKINS-5073 is common enough
					// that this suppressing of the error would be justified
					LOGGER.log(Level.FINE, "Script deletion failed", e);
				} else {
					Util.displayIOException(e, listener);
					e.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_UnableToDelete(script)));
				}
			} catch (Exception e) {
				e.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_UnableToDelete(script)));
			}
		}
	}

	// /!\ The code below (till eof) has been duplicated from NeoBuildAction !!!!
	// It would need refactoring but has not been done because Jenkins works
	// with the reflectiveness of code and refactoring might have cause regression
	@Override
	protected String getContents() {
		return interpreter.getContents();
	}

	@Override
	protected String getFileExtension() {
		return interpreter.getFileExtension();
	}


	interface MyInterpreter{
		String getContents();
		String getFileExtension();
		String[] buildCommandLine(FilePath script);
	}

	/**
	 * The type Batch file mine.
	 */
	public static class BatchFileMine extends BatchFile implements MyInterpreter{
		/**
		 * Instantiates a new Batch file mine.
		 */
		public BatchFileMine(final  String command) {
			super(command);
		}

		@Override
		public String getContents() {
			return super.getContents();
		}

		@Override
		public String getFileExtension() {
			return super.getFileExtension();
		}

		@Extension // Because it final in parent !
		public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
			@Override
			public String getHelpFile() {
				return "/help/project-config/batch.html";
			}

			public String getDisplayName() {
				return Messages.BatchFile_DisplayName();
			}

			@Override
			public Builder newInstance(StaplerRequest req, JSONObject data) {
				return new BatchFile(data.getString("command"));
			}

			public boolean isApplicable(Class<? extends AbstractProject> jobType) {
				return true;
			}
		}
	}

	/**
	 * The type Shell mine.
	 */
	public static class ShellMine extends Shell implements MyInterpreter {
		/**
		 * Instantiates a new Shell mine.
		 */
		public ShellMine(final String command) {
			super(command);
		}

		@Override
		public String getContents() {
			return super.getContents();
		}

		@Override
		public String getFileExtension() {
			return super.getFileExtension();
		}
		@Extension
		public static class DescriptorImpl extends Shell.DescriptorImpl{

		}
	}
}
