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
package ilarkesto.core.search;

import ilarkesto.core.base.Str;

import java.util.ArrayList;
import java.util.Collection;

public class SearchText {

	private Collection<String> words;
	private boolean deep;

	public SearchText(String text) {
		words = new ArrayList<String>();
		text = text.toLowerCase().trim();
		int idx = text.indexOf(' ');
		while (idx > 0) {
			words.add(text.substring(0, idx));
			text = text.substring(idx + 1).trim();
			idx = text.indexOf(' ');
		}
		words.add(text.trim());
	}

	public SearchText(Collection<String> words) {
		super();
		this.words = words;
	}

	public Collection<String> getWords() {
		return words;
	}

	public <C extends Collection<Searchable>> C collectMatching(C resultContainer,
			Collection<? extends Searchable> searchables) {
		for (Searchable searchable : searchables) {
			if (searchable.matches(this)) resultContainer.add(searchable);
		}
		return resultContainer;
	}

	public boolean matches(Object... values) {
		for (String word : words) {
			if (!matchesAny(word, values)) return false;
		}
		return true;
	}

	private boolean matchesAny(String word, Object[] values) {
		for (Object value : values) {
			if (value == null) continue;
			String s = value.toString().toLowerCase();
			if (s.contains(word)) return true;
		}
		return false;
	}

	public boolean isDeep() {
		return deep;
	}

	public SearchText setDeep(boolean deep) {
		this.deep = deep;
		return this;
	}

	@Override
	public String toString() {
		return Str.concat(words, " ");
	}

}
