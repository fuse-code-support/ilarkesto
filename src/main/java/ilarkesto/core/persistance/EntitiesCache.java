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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EntitiesCache<E extends Entity> implements EntitiesProvider<E> {

	private Map<Class, Map<String, E>> entitiesByTypeById = Persistence.transactionManager
			.synchronizedMap(new HashMap<Class, Map<String, E>>());

	@Override
	public <C extends Collection<E>> C getAll(C resultCollection) {
		for (Map<String, E> entitiesById : entitiesByTypeById.values()) {
			resultCollection.addAll(entitiesById.values());
		}
		return resultCollection;
	}

	@Override
	public Set<E> getAllAsSet() {
		return getAll(new HashSet<E>());
	}

	@Override
	public List<E> getAllAsList() {
		return getAll(new ArrayList<E>());
	}

	public Set<String> getAllIds() {
		Set<String> ret = new HashSet<String>();
		for (Map<String, E> entitiesById : entitiesByTypeById.values()) {
			ret.addAll(entitiesById.keySet());
		}
		return ret;
	}

	public Set<E> findAllAsSet(AEntityQuery query) {
		return find(query, new HashSet<E>());
	}

	@Override
	public <C extends Collection<E>> C find(AEntityQuery<E> query, C resultCollection) {
		HashMap<Class, Map<String, E>> localCopy = new HashMap<Class, Map<String, E>>(entitiesByTypeById);
		for (Entry<Class, Map<String, E>> entry : localCopy.entrySet()) {
			if (!query.testType(entry.getKey())) continue;

			Map<String, E> entitiesById = entry.getValue();
			if (query.getClass().equals(AllByTypeQuery.class)) {
				resultCollection.addAll(entitiesById.values());
			} else {
				Collection<E> entities = new ArrayList<E>(entitiesById.values());
				for (E entity : entities) {
					if (query.test(entity)) resultCollection.add(entity);
				}
			}
		}

		return resultCollection;
	}

	@Override
	public E findFirst(AEntityQuery query) {
		HashMap<Class, Map<String, E>> localCopy = new HashMap<Class, Map<String, E>>(entitiesByTypeById);
		for (Entry<Class, Map<String, E>> entry : localCopy.entrySet()) {
			if (!query.testType(entry.getKey())) continue;

			Map<String, E> entitiesById = entry.getValue();
			Collection<E> entities = new ArrayList<E>(entitiesById.values());
			for (E entity : entities) {
				if (query.test(entity)) return entity;
			}
		}

		return null;
	}

	public void add(E entity) {
		Class type = entity.getClass();
		Map<String, E> entitiesById = entitiesByTypeById.get(type);
		if (entitiesById == null) {
			entitiesById = new HashMap<String, E>();
			entitiesByTypeById.put(type, entitiesById);
		}
		entitiesById.put(entity.getId(), entity);
	}

	public void addAll(Collection<E> entities) {
		if (entities == null) return;
		for (E entity : entities) {
			add(entity);
		}
	}

	public E remove(String entityId) {
		if (entityId == null) return null;
		for (Map<String, E> entitiesById : entitiesByTypeById.values()) {
			E removed = entitiesById.remove(entityId);
			if (removed != null) return removed;
		}
		return null;
	}

	public void removeAll(Collection<String> ids) {
		if (ids == null) return;
		for (String id : ids) {
			remove(id);
		}
	}

	@Override
	public boolean containsWithId(String id) {
		for (Map<String, E> entitiesById : entitiesByTypeById.values()) {
			if (entitiesById.containsKey(id)) return true;
		}
		return false;
	}

	@Override
	public E getById(String id) throws EntityDoesNotExistException {
		E entity = null;
		for (Map<String, E> entitiesById : entitiesByTypeById.values()) {
			entity = entitiesById.get(id);
			if (entity != null) break;
		}
		if (entity == null) throw new EntityDoesNotExistException(id);
		return entity;
	}

	@Override
	public Set<E> getByIdsAsSet(Collection<String> ids) throws EntityDoesNotExistException {
		return getByIds(ids, new HashSet<E>(ids.size()));
	}

	@Override
	public List<E> getByIdsAsList(Collection<String> ids) throws EntityDoesNotExistException {
		return getByIds(ids, new ArrayList<E>(ids.size()));
	}

	@Override
	public <C extends Collection<E>> C getByIds(Collection<String> ids, C resultContainer)
			throws EntityDoesNotExistException {
		for (String id : ids) {
			resultContainer.add(getById(id));
		}
		return resultContainer;
	}

	public int size() {
		int sum = 0;
		for (Map<String, E> entitiesById : entitiesByTypeById.values()) {
			sum += entitiesById.size();
		}
		return sum;
	}

	public boolean isEmpty() {
		for (Map<String, E> entitiesById : entitiesByTypeById.values()) {
			if (!entitiesById.isEmpty()) return false;
		}
		return true;
	}

	public final Map<Class, Integer> countEntities() {
		Map<Class, Integer> countsByType = new HashMap<Class, Integer>();

		for (Entry<Class, Map<String, E>> entry : entitiesByTypeById.entrySet()) {
			countsByType.put(entry.getKey(), entry.getValue().size());
		}

		return countsByType;
	}

}
