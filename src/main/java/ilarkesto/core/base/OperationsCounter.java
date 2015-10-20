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
package ilarkesto.core.base;

import ilarkesto.core.time.TimePeriod;

public class OperationsCounter {

	// private Map<String, Counter> countersByName = new HashMap<String, OperationsCounter.Counter>();
	private LazyMap<String, Counter> countersByName = new LazyMap<String, OperationsCounter.Counter>() {

		@Override
		protected Counter create(String key) {
			return new Counter(key);
		}
	};

	public void count(String name, long runtime) {
		countersByName.get(name).count(runtime);
	}

	@Override
	public String toString() {
		MultilineBuilder mb = new MultilineBuilder();
		for (Counter counter : Utl.sort(countersByName.getAll())) {
			mb.ln(counter.toString());
		}
		return mb.toString();
	}

	private class Counter implements Comparable<Counter> {

		private String name;

		private int count;
		private long totalRuntime;

		public Counter(String name) {
			super();
			this.name = name;
		}

		public void count(long runtime) {
			count++;
			totalRuntime += runtime;
		}

		@Override
		public String toString() {
			return name + ": " + count + " / " + new TimePeriod(totalRuntime / count).toShortestString("en");
		}

		@Override
		public int compareTo(Counter o) {
			return name.compareTo(o.name);
		}

	}

}
