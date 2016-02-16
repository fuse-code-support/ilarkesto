/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.di.app;

import ilarkesto.base.Sys;
import ilarkesto.cli.ACommand;
import ilarkesto.core.localization.Localizer;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.di.BeanContainer;
import ilarkesto.di.BeanProvider;
import ilarkesto.di.MultiBeanProvider;
import ilarkesto.io.IO;
import ilarkesto.logging.DefaultLogRecordHandler;
import ilarkesto.logging.JavaLogging;
import ilarkesto.logging.Log4jLogging;

import java.io.File;
import java.util.Locale;
import java.util.Map.Entry;

public class ApplicationStarter {

	private static final Log LOG = Log.get(ApplicationStarter.class);

	public static <A extends AApplication> A startApplication(Class<A> applicationClass, BeanProvider beanProvider,
			String... arguments) {

		Log.setDebugEnabled(Sys.isDevelopmentMode());
		DefaultLogRecordHandler.activate();
		Localizer.setCurrent(Locale.getDefault().getLanguage(), Localizer.EN);
		LOG.info("********************************************************************************");
		LOG.info("Starting application:", applicationClass.getName());
		logEnvironmentInfo();
		LOG.info("    arguments:   ", arguments);
		for (Entry<String, Object> entry : beanProvider.getAllBeans().entrySet()) {
			LOG.info("    " + entry.getKey() + ":", entry.getValue());
		}

		try {
			A application = applicationClass.newInstance();
			if (beanProvider != null) beanProvider.autowire(application);
			application.setArguments(arguments);
			DefaultLogRecordHandler.setLogFile(new File(application.getApplicationDataDir() + "/error.log"));
			JavaLogging.redirectToLoggers();
			try {
				Log4jLogging.redirectToLoggers();
			} catch (Throwable ex) {}
			application.start();
			LOG.info("Application started:", application.getApplicationName() + " "
					+ application.getBuildProperties().getReleaseLabel());
			LOG.info("********************************************************************************\n");
			return application;
		} catch (Exception ex) {
			LOG.fatal("Starting application failed.", ex);
			throw new RuntimeException(ex);
		}
	}

	public static void logEnvironmentInfo() {
		String mode = Sys.isDevelopmentMode() ? "DEVELOPMENT" : "PRODUCTION";
		LOG.info("   ", mode, "MODE");
		LOG.info("    time:        ", new DateAndTime(Sys.getStartupTime()));
		LOG.info("    system user: ", Sys.getUsersName());
		LOG.info("    user home:   ", Sys.getUsersHomePath());
		LOG.info("    work-path:   ", IO.getWorkDir());
		LOG.info("    temp-path:   ", IO.getTempDir());
		LOG.info("    locale:      ", Locale.getDefault());
		LOG.info("    encoding:    ", Sys.getFileEncoding());
		LOG.info("    java:        ", Sys.getJavaHome() + " " + Sys.getJavaRuntimeVersion());
		LOG.info("    max memory:  ", Sys.getMaxMemoryInMegabytes() + " MB");
		// LOG.info(" java version: ", Sys.getJavaRuntimeVersion());
	}

	public static <A extends AApplication> A startApplication(Class<A> applicationClass, String... arguments) {
		return startApplication(applicationClass, new MultiBeanProvider(), arguments);
	}

	public static void executeCommand(Class<? extends ACommand> commandClass, String... arguments) {
		BeanContainer beanContainer = new BeanContainer();
		beanContainer.put("commandClass", commandClass);
		startApplication(CommandApplication.class, beanContainer, arguments);
	}

	// --- dependencies ---

}
