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

public class TextByTextFilter {

	public static boolean matches(Object value, String filterText) {
		if (Str.isBlank(filterText)) return true;
		if (value == null) return false;

		filterText = filterText.toLowerCase();
		String s = value.toString().toLowerCase();

		for (String option : filterText.split("\\|")) {
			option = option.trim();

			if (matchesOption(s, option)) return true;
		}

		return false;
	}

	private static boolean matchesOption(String s, String filterText) {
		for (String must : filterText.split("\\ ")) {
			must = must.trim();

			if (!matchesMust(s, must)) return false;
		}
		return true;

	}

	private static boolean matchesMust(String s, String filterText) {
		if (filterText.startsWith("!")) {
			// NOT
			if (filterText.length() == 1) return true;
			return !s.contains(filterText.substring(1));
		}

		return s.contains(filterText);
	}

}
