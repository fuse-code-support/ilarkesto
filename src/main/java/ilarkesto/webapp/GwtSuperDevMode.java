/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.webapp;

import ilarkesto.base.Proc;
import ilarkesto.base.Sys;
import ilarkesto.base.Utl;
import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.dev.codeserver.CodeServer;
import com.google.gwt.dev.codeserver.Options;
import com.google.gwt.dev.codeserver.WebServer;

public class GwtSuperDevMode {

	private static Log log = Log.get(GwtSuperDevMode.class);

	private int port = 9876;
	private Set<String> sources = new LinkedHashSet<String>();
	private Set<String> modules = new LinkedHashSet<String>();
	private boolean precompile = true;
	private boolean incremental = true;
	private WebServer webServer;
	private Proc proc;
	private File workDir;

	public void startCodeServerInSeparateProcessWithJarsFromIlarkesto() {
		List<String> cp = new ArrayList<String>();
		File ilarkestoDir = new File(workDir.getPath() + "/ilarkesto");
		if (!ilarkestoDir.exists())
			throw new RuntimeException("Ilarkesto directory does not exist: " + ilarkestoDir.getAbsolutePath());
		for (File file : IO.listFiles(new File(ilarkestoDir.getPath() + "/lib"))) {
			if (!file.isFile()) continue;
			String name = file.getName();
			if (!name.endsWith(".jar")) continue;
			if (name.startsWith("gwt")) cp.add("ilarkesto/lib/" + name);
			if (name.startsWith("validation-api")) cp.add("ilarkesto/lib/" + name);
		}
		startCodeServerInSeparateProcess(workDir, cp);
	}

	public void startCodeServerInSeparateProcess(File workDir, Collection<String> classpath) {
		IO.delete(getWorkDir());

		log.info("Starting GWT Super Dev Mode CodeServer on port", port, "from", workDir.getAbsolutePath());

		Proc proc = new Proc("java");
		proc.setWorkingDir(workDir);
		proc.setRedirectOutputToSysout(true);
		if (!classpath.isEmpty()) proc.addParameters("-classpath", Str.concat(classpath, Sys.getPathSeparator()));
		proc.addParameter("com.google.gwt.dev.codeserver.CodeServer");
		if (!precompile) proc.addParameter("-noprecompile");
		if (!incremental) proc.addParameter("-noincremental");
		proc.addParameters("-port", String.valueOf(port));
		proc.addParameters("-workDir", getWorkDir());

		for (String source : sources) {
			proc.addParameter("-src");
			if (!source.startsWith("/")) source = getBasePath() + Sys.getFileSeparator() + source;
			File sourceFile = new File(source);
			if (!sourceFile.exists()) throw new IllegalStateException("Path does not exist: " + source);
			proc.addParameter(source);
		}

		for (String module : modules) {
			proc.addParameter(module);
		}

		proc.start();
		Utl.sleep(1000);
		if (!proc.isRunning())
			throw new RuntimeException("Starting GWT SuperDevMode CodeServer failed: " + proc.getOutput());
	}

	public void startCodeServerInSeparateThread() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				startCodeServer();
			}
		});
		thread.setName("GWT SuperDevMode Code Server");
		thread.start();
	}

	public void startCodeServer() {
		if (webServer != null) throw new IllegalStateException("Already started");
		IO.delete(getWorkDir());

		log.info("Starting GWT Super Dev Mode CodeServer on port", port);
		Sys.setProperty("gwt.codeserver.port", String.valueOf(port));
		RuntimeTracker rt = new RuntimeTracker();
		Options options = createOptions();
		try {
			webServer = CodeServer.start(options);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		log.info("CodeServer started in", rt.getRuntimeFormated());
	}

	public void stopCodeServer() {
		log.info("Stopping CodeServer");
		if (webServer != null) {
			try {
				webServer.stop();
			} catch (Exception ex) {
				log.error("Stopping CodeServer failed.", ex);
			}
		}
		if (proc != null) {
			proc.destroyQuiet();
		}
	}

	private String getWorkDir() {
		String base = getBasePath();
		String workDir = base + "/runtimedata/gwt-code-server-output";
		File workdirFile = new File(workDir);
		workdirFile.mkdirs();
		if (!workdirFile.exists()) throw new IllegalStateException("Path does not exist: " + workDir);
		return workDir;
	}

	private String getBasePath() {
		String base = Sys.getWorkDir().getAbsolutePath().replace("\n", Sys.getPathSeparator());
		if (!new File(base).exists()) throw new IllegalStateException("Path does not exist: " + base);
		return base;
	}

	private Options createOptions() {
		List<String> args = new ArrayList<String>();

		if (!precompile) args.add("-noprecompile");
		if (!incremental) args.add("-noincremental");

		// port
		args.add("-port");
		args.add(String.valueOf(port));

		// workdir
		args.add("-workDir");
		args.add(getWorkDir());

		// sources
		for (String source : sources) {
			args.add("-src");
			if (!source.startsWith("/")) source = getBasePath() + Sys.getPathSeparator() + source;
			File sourceFile = new File(source);
			if (!sourceFile.exists()) throw new IllegalStateException("Path does not exist: " + source);
			args.add(source);
		}

		// modules
		for (String module : modules) {
			args.add(module);
		}

		log.info("Args:", args);
		Options options = new Options();
		boolean parsed = options.parseArgs(args.toArray(new String[args.size()]));
		if (!parsed) throw new RuntimeException("Parsing args failed: " + Str.format(args));
		return options;
	}

	public GwtSuperDevMode addModules(String... modules) {
		for (String module : modules) {
			this.modules.add(module);
		}
		return this;
	}

	public GwtSuperDevMode addSources(String... sources) {
		for (String source : sources) {
			this.sources.add(source);
		}
		return this;
	}

	public GwtSuperDevMode setIncremental(boolean incremental) {
		this.incremental = incremental;
		return this;
	}

	public GwtSuperDevMode setPrecompile(boolean precompile) {
		this.precompile = precompile;
		return this;
	}

	public static String getCompileHref(String moduleName) {
		return "javascript:%7B window.__gwt_bookmarklet_params %3D %7Bserver_url%3A'http%3A%2F%2Flocalhost%3A9876%2F'%2Cmodule_name%3A'"
				+ moduleName
				+ "'%7D%3B var s %3D document.createElement('script')%3B s.src %3D 'http%3A%2F%2Flocalhost%3A9876%2Fdev_mode_on.js'%3B void(document.getElementsByTagName('head')%5B0%5D.appendChild(s))%3B%7D";
	}

	public GwtSuperDevMode setWorkDir(File workDir) {
		this.workDir = workDir;
		return this;
	}

}
