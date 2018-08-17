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

import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.core.search.SearchText;
import ilarkesto.core.search.Searchable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AEntity extends ABaseEntity implements Entity, Searchable, TransferableEntity {

	private static final transient Log log = Log.get(AEntity.class);

	@Override
	public AEntity setId(String id) {
		super.setId(id);
		return this;
	}

	protected final ValuesCache getCache() {
		return Persistence.getValuesCache(getId());
	}

	public String getDeleteVeto() {
		return "Objekt ist nicht löschbar";
	}

	@Deprecated
	public final boolean isDeletable() {
		if (!isPersisted()) return false;
		return getDeleteVeto() == null;
	}

	@Override
	public boolean matches(SearchText search) {
		return search.matches(toString(), getId());
	}

	public static boolean exists(String id) {
		return Transaction.get().containsWithId(id);
	}

	public static AEntity getById(String id) {
		if (id == null) return null;
		return Transaction.get().getById(id);
	}

	public static List<AEntity> getByIds(Collection<String> ids) {
		return Transaction.get().getByIdsAsList(ids);
	}

	public static List<AEntity> getByIds(String[] ids) {
		return Transaction.get().getByIdsAsList(Utl.toList(ids));
	}

	public static Set<AEntity> getByIdsAsSet(Collection<String> ids) {
		return Transaction.get().getByIdsAsSet(ids);
	}

}
