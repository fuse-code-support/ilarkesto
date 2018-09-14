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
package ilarkesto.core.persistance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AEntitySetBackReferenceHelper<E extends AEntity> {

	private Map<String, Set<String>> cachesById = Persistence.transactionManager
			.synchronizedMap(new HashMap<String, Set<String>>());

	protected abstract Set<E> loadById(String id);

	public Set<E> getById(String id) {
		// if (AEntityDatabase.instance.isPartial()) return loadById(id);
		Set<String> cache = cachesById.get(id);

		if (cache != null) {
			try {
				return (Set<E>) AEntity.getByIdsAsSet(cache);
			} catch (EntityDoesNotExistException ex) {
				cachesById.remove(id);
			}
		}

		Set<E> entities = loadById(id);
		if (!Persistence.transactionManager.isTransactionWithChangesOpen()) {
			cachesById.put(id, Persistence.getIdsAsSet(entities));
		}
		return entities;
	}

	public void clear(String id) {
		cachesById.remove(id);
	}

	public void clear(Collection<String> ids) {
		for (String id : ids) {
			clear(id);
		}
	}

}
