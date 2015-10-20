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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class LazyMap<K, V> {

	private Map<K, V> map = new HashMap<K, V>();

	protected abstract V create(K key);

	public final synchronized V get(K key) {
		V value = map.get(key);
		if (value == null) {
			value = create(key);
			map.put(key, value);
		}
		return value;
	}

	public final synchronized void release() {
		map.clear();
	}

	public Collection<V> getAll() {
		return map.values();
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LazyMap)) return false;
		return map.equals(((LazyMap) obj).map);
	}

	// --- implementations ----

	public static <K, V> LazyMap<K, Set<V>> createHashSetInstance() {
		return new LazyMap<K, Set<V>>() {

			@Override
			protected Set<V> create(K key) {
				return new HashSet<V>();
			}
		};
	}

}
