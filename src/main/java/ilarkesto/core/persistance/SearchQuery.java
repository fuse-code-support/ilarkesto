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

import ilarkesto.core.search.SearchText;

import java.util.ArrayList;
import java.util.Collection;

public class SearchQuery extends AEntityQuery<AEntity> {

	private SearchText searchText;

	private Collection<Class> types;
	private Collection<Class> excludedTypes;

	public SearchQuery(String text) {
		super();
		this.searchText = new SearchText(text);
	}

	public SearchQuery setDeep(boolean deep) {
		searchText.setDeep(deep);
		return this;
	}

	@Override
	public boolean test(AEntity entity) {
		return acceptType(entity) && entity.matches(searchText);
	}

	private boolean acceptType(AEntity entity) {
		if (types == null && excludedTypes == null) return true;
		Class type = entity.getClass();
		if (excludedTypes != null && excludedTypes.contains(type)) return false;
		if (types == null || types.isEmpty()) return true;
		return types.contains(type);
	}

	public SearchQuery addTypes(Class... types) {
		for (Class type : types) {
			if (this.types == null) this.types = new ArrayList<Class>(types.length);
			this.types.add(type);
		}
		return this;
	}

	public SearchQuery addExcludedTypes(Class... types) {
		for (Class type : types) {
			if (this.excludedTypes == null) this.excludedTypes = new ArrayList<Class>(types.length);
			this.excludedTypes.add(type);
		}
		return this;
	}

}
