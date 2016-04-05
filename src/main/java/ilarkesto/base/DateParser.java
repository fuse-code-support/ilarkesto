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

import ilarkesto.core.time.Date;

import java.text.ParseException;

public class DateParser {

	public static Date parseDate(String s) throws ParseException {
		if (s == null) return null;
		s = s.trim();
		String[] sa = Str.tokenizeToArray(s, ".,- ");
		if (sa.length == 0) throw new ParseException("Not a Date: " + s, -1);
		if (sa.length > 3) throw new ParseException("Not a Date: " + s, -1);
		int[] ia = new int[sa.length];
		for (int i = 0; i < ia.length; i++) {
			try {
				ia[i] = Integer.parseInt(sa[i]);
			} catch (NumberFormatException e) {
				throw new ParseException("Not a Date: " + s, -1);
			}
		}

		if (ia.length == 3) return new Date(Tm.year(ia[2]), ia[1], ia[0]);

		Date today = Date.today();
		int todayDay = today.getDay();
		int todayMonth = today.getMonth();
		int todayYear = today.getYear();

		if (ia.length == 2) {
			if (ia[1] > 12) return new Date(Tm.year(ia[1]), ia[0], todayDay);
			return new Date(todayYear, ia[1], ia[0]);
		}

		if (ia[0] > 31) return new Date(Tm.year(ia[0]), todayMonth, todayDay);
		return new Date(todayYear, todayMonth, ia[0]);
	}

}
