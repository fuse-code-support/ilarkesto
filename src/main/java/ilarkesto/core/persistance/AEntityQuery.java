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
import ilarkesto.core.fp.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AEntityQuery<E extends Entity> implements Predicate<E> {

	public Set<E> list() {
		return ATransaction.get().findAllAsSet(this);
	}

	public E findFirst() {
		return (E) ATransaction.get().findFirst(this);
	}

	public boolean existsAtLeastOne() {
		return findFirst() != null;
	}

	@Override
	public abstract boolean test(E entity);

	public Class<E> getType() {
		return null;
	}

	public List<E> filter(Collection<E> entities) {
		ArrayList<E> ret = new ArrayList<E>();
		for (E entity : entities) {
			if (test(entity)) ret.add(entity);
		}
		return ret;
	}

	@Override
	public String toString() {
		return Utl.getSimpleName(getClass());
	}

	public boolean testType(Class typeToTest) {
		Class<E> queryType = getType();
		if (queryType == null) return true;
		return isInstanceOf(typeToTest, queryType);
	}

	static boolean isInstanceOf(Class givenType, Class requiredType) {
		if (requiredType.equals(givenType)) return true;
		Class superType = givenType.getSuperclass();
		if (superType.equals(Object.class)) return false;
		return isInstanceOf(superType, requiredType);
	}

}
