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
package ilarkesto.core.logging;

import java.util.LinkedList;
import java.util.List;

public abstract class LogRecordHandler {

	private LinkedList<LogRecord> latestLogs = new LinkedList<LogRecord>();

	public void log(LogRecord record) {
		synchronized (latestLogs) {
			latestLogs.add(record);
			if (latestLogs.size() > 230) latestLogs.removeFirst();
		}
	}

	public void flush() {}

	public List<LogRecord> getLatestLogs() {
		return latestLogs;
	}

	public String getLatestLogsAsString() {
		synchronized (latestLogs) {
			StringBuilder sb = new StringBuilder();
			for (LogRecord record : latestLogs) {
				sb.append(record.toString()).append("\n");
			}
			return sb.toString();
		}
	}

}
