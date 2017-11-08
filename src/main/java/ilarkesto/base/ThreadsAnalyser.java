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
package ilarkesto.base;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class ThreadsAnalyser {

	private State capureState() {
		State state = new State();
		for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			state.threads.add(new ThreadInfo(entry.getKey(), entry.getValue()));
		}
		return state;
	}

	class ThreadInfo {

		private String id;

		public ThreadInfo(Thread thread, StackTraceElement[] stack) {
			id = "";
			id += " | Id " + thread.getId();
			id += " | ThreadGroup.name " + thread.getThreadGroup().getName();
		}

		@Override
		public String toString() {
			return id;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ThreadInfo)) return false;
			return id.equals(((ThreadInfo) obj).id);
		}

	}

	class State {

		private long time = System.currentTimeMillis();
		private Set<ThreadInfo> threads = new HashSet<ThreadInfo>();

	}

}
