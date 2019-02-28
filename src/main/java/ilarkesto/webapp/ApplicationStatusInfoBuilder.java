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
import ilarkesto.core.base.Bytes;
import ilarkesto.core.logging.LogRecord;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.TimePeriod;
import ilarkesto.gwt.server.AGwtConversation;
import ilarkesto.logging.DefaultLogRecordHandler;
import ilarkesto.ui.web.HtmlBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class ApplicationStatusInfoBuilder {

	public void buildAll(HtmlBuilder html) {
		sessions(html);
		conversations(html);
		runtime(html);
		processes(html);
		threads(html);
		// TODO: entities
		// TODO: available disk space
		// TODO: threadlocals
		systemProperties(html);
		environment(html);
		errors(html);
	}

	private void errors(HtmlBuilder html) {
		sectionHeader(html, "Warnings and Errors");
		List<LogRecord> logs = DefaultLogRecordHandler.getErrors();
		logsTable(html, logs);
	}

	private void runtime(HtmlBuilder html) {
		sectionHeader(html, "Runtime");
		startTABLE(html);

		Runtime runtime = Runtime.getRuntime();
		long freeMemory = runtime.freeMemory();
		long totalMemory = runtime.totalMemory();
		long usedMemory = totalMemory - freeMemory;
		long maxMemory = runtime.maxMemory();
		long availableMemory = maxMemory - usedMemory;
		double usedMemoryPercent = usedMemory * 100d / maxMemory;
		double availableMemoryPercent = availableMemory * 100d / maxMemory;
		DateAndTime startupTime = new DateAndTime(Sys.getStartupTime());
		keyValueRow(html, "Startup time", startupTime);
		keyValueRow(html, "Run time", startupTime.getPeriodToNow().toShortestString());
		keyValueRow(html, "Used memory",
			new Bytes(usedMemory).toRoundedString() + " (" + new DecimalFormat("#0").format(usedMemoryPercent) + "%)");
		keyValueRow(html, "Available memory", new Bytes(availableMemory).toRoundedString() + " ("
				+ new DecimalFormat("#0").format(availableMemoryPercent) + "%)");
		keyValueRow(html, "Max memory", new Bytes(maxMemory).toRoundedString());

		keyValueRow(html, "Available processors", String.valueOf(runtime.availableProcessors()));
		keyValueRow(html, "Default locale", Locale.getDefault().toString());

		endTABLE(html);
		html.flush();
	}

	private void conversations(HtmlBuilder html) {
		sectionHeader(html, "Active Conversations");
		startTABLE(html);
		headersRow(html, "#", "User", "Project", "Last request");
		List<AGwtConversation> conversations = new ArrayList<AGwtConversation>(
				AWebApplication.get().getGwtConversations());
		Collections.sort(conversations);
		for (AGwtConversation conversation : conversations) {
			valuesRow(html, conversation, conversation.getLastTouched().getPeriodToNow().toShortestString() + " ago");
		}
		endTABLE(html);
	}

	private void sessions(HtmlBuilder html) {
		sectionHeader(html, "Active Sessions");
		startTABLE(html);
		headersRow(html, "User", "Last request", "Age", "Host", "Agent");
		List<AWebSession> sessions = new ArrayList<AWebSession>((AWebApplication.get().getWebSessions()));
		Collections.sort(sessions);
		for (AWebSession session : sessions) {
			valuesRow(html, session, session.getLastTouched().getPeriodToNow().toShortestString() + " ago",
				session.getSessionStartedTime().getPeriodToNow().toShortestString(), session.getInitialRemoteHost(),
				session.getUserAgent());
		}
		endTABLE(html);
	}

	private void threads(HtmlBuilder html) {
		sectionHeader(html, "Threads");
		startTABLE(html);
		headersRow(html, "Name", "Prio", "State", "Group", "Stack trace");
		for (Thread thread : Utl.getAllThreads()) {
			String groupName = thread.getThreadGroup().getName();
			StackTraceElement[] stackTrace = thread.getStackTrace();
			valuesRow(html, thread.getName(), thread.getPriority(), thread.getState(), groupName,
				Utl.formatStackTrace(stackTrace, " -> "));
		}
		endTABLE(html);
	}

	private void systemProperties(HtmlBuilder html) {
		sectionHeader(html, "Java System Properties");
		startTABLE(html);
		Properties properties = System.getProperties();
		for (Object key : properties.keySet()) {
			String property = key.toString();
			keyValueRow(html, property, properties.getProperty(property));
		}
		endTABLE(html);
		html.flush();
	}

	private void processes(HtmlBuilder html) {
		sectionHeader(html, "Spawned processes");
		startTABLE(html);
		headersRow(html, "Command", "Start time", "Run time");
		for (Proc proc : Proc.getRunningProcs()) {
			long startTime = proc.getStartTime();
			long runTime = proc.getRunTime();
			valuesRow(html, proc.toString(), new DateAndTime(startTime).getTime(), new TimePeriod(runTime));
		}
		endTABLE(html);
	}

	private void environment(HtmlBuilder html) {
		sectionHeader(html, "Environment");
		startTABLE(html);
		Map<String, String> env = System.getenv();
		for (String key : env.keySet()) {
			keyValueRow(html, key, env.get(key));
		}
		endTABLE(html);
		html.flush();
	}

	protected void logsTable(HtmlBuilder html, List<LogRecord> logs) {
		startTABLE(html);
		headersRow(html, "Level", "Logger", "Message", "Context");
		for (LogRecord log : logs) {
			String color = "#666";
			if (log.level.isErrorOrWorse()) color = "#c00";
			if (log.level.isWarn()) color = "#990";
			if (log.level.isInfo()) color = "#000";
			valuesRowColored(html, color, log.level, log.name, log.getParametersAsString(), log.context);
		}
		endTABLE(html);
	}

	protected void startTABLE(HtmlBuilder html) {
		html.startTABLE();
	}

	protected void headersRow(HtmlBuilder html, String... headers) {
		html.startTR();

		for (String header : headers) {
			html.startTH().setStyle(getLabelStyle());
			html.text(header);
			html.endTH();
		}

		html.endTR();
		html.flush();
	}

	protected void valuesRowColored(HtmlBuilder html, String color, Object... values) {
		html.startTR();

		for (Object value : values) {
			html.startTD().setStyle(getValueStyle() + " color: " + color + ";");
			html.text(value);
			html.endTD();
		}

		html.endTR();
		html.flush();
	}

	protected void valuesRow(HtmlBuilder html, Object... values) {
		html.startTR();

		for (Object value : values) {
			html.startTD().setStyle(getValueStyle());
			html.text(value);
			html.endTD();
		}

		html.endTR();
		html.flush();
	}

	protected void keyValueRow(HtmlBuilder html, String key, Object value) {
		html.startTR();

		html.startTD().setStyle(getLabelStyle());
		html.text(key);
		html.endTD();

		html.startTD().setStyle(getValueStyle());
		html.text(value);
		html.endTD();

		html.endTR();
		html.flush();
	}

	protected void endTABLE(HtmlBuilder html) {
		html.endTABLE();
		html.flush();
	}

	protected void sectionHeader(HtmlBuilder html, String title) {
		html.H2(title);
	}

	private String getLabelStyle() {
		return "color: #999; font-weight: normal; padding: 2px 20px 2px 5px; text-align: left;";
	}

	private String getValueStyle() {
		return "font-family: mono; padding: 2px 20px 2px 5px;";
	}

}
