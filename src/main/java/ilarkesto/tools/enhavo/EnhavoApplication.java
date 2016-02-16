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
package ilarkesto.tools.enhavo;

import ilarkesto.base.Utl;
import ilarkesto.concurrent.TaskManager;
import ilarkesto.core.persistance.EntitiesBackend;
import ilarkesto.core.persistance.InMemoryEntitiesBackend;
import ilarkesto.di.app.AApplication;
import ilarkesto.di.app.ApplicationStarter;
import ilarkesto.io.AFileChangeWatchTask;
import ilarkesto.io.IO;

import java.io.File;
import java.io.IOException;

public class EnhavoApplication extends AApplication {

	public static void main(String[] args) {
		ApplicationStarter.startApplication(EnhavoApplication.class, args);
	}

	@Override
	protected void onStart() {

		EnhavoCommandLineArgs args = getCommandLineArgs();

		if (args.getPath() == null) {
			exitWithError("Missing <cms-path> parameter");
			return;
		}
		if (args.isToMany()) {
			exitWithError("Too many arguments");
			return;
		}

		if (args.isLoop()) {
			loop();
			return;
		}

		if (!args.isWatch()) getCmsContext().build();
	}

	@Override
	protected boolean isPreventProcessEnd() {
		if (getCommandLineArgs().isWatch()) return true;
		return super.isPreventProcessEnd();
	}

	@Override
	protected void scheduleTasks(TaskManager tm) {
		if (getCommandLineArgs().isWatch()) tm.start(new WatchTask(getCmsContext()));
	}

	@Override
	protected void onShutdown() {}

	private void exitWithError(String message) {
		System.out.println(message);
		System.exit(1);
	}

	private void loop() {
		while (true) {
			getCmsContext().build();
			Utl.sleep(5000);
		}
	}

	static class WatchTask extends AFileChangeWatchTask {

		private CmsContext cms;

		public WatchTask(CmsContext cms) {
			super(cms.getInputDir(), 300, 3000);
			this.cms = cms;
		}

		@Override
		protected void onChange() {
			cms.build();
		}

	}

	private File applicationDataDir;

	@Override
	public String getApplicationDataDir() {
		if (applicationDataDir == null) {
			try {
				applicationDataDir = new File(getCommandLineArgs().getPath()).getCanonicalFile().getAbsoluteFile();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			if (!applicationDataDir.exists()) {
				System.out.println("Creating new CMS: " + applicationDataDir.getPath());
				IO.createDirectory(applicationDataDir);
			}
		}
		return applicationDataDir.getPath();
	}

	@Override
	protected EntitiesBackend createEntitiesBackend() {
		return new InMemoryEntitiesBackend();
	}

	@Override
	protected boolean isSingleton() {
		return true;
	}

	private CmsContext cmsContext;

	public CmsContext getCmsContext() {
		if (cmsContext == null) cmsContext = new CmsContext(new File(getApplicationDataDir()), null);
		return cmsContext;
	}

	private EnhavoCommandLineArgs commandLineArgs;

	public EnhavoCommandLineArgs getCommandLineArgs() {
		if (commandLineArgs == null) commandLineArgs = new EnhavoCommandLineArgs(getArguments());
		return commandLineArgs;
	}

}
