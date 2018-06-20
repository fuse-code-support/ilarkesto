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
package ilarkesto.core.time;

import java.io.Serializable;

public class MonthAndYear implements Comparable<MonthAndYear>, Serializable {

	protected int year;
	protected int month;

	private transient int hashCode;

	public MonthAndYear(int year, int month) {
		super();
		this.year = year;
		this.month = month;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	@Override
	public final int hashCode() {
		if (hashCode == 0) {
			hashCode = 23;
			hashCode = hashCode * 37 + year;
			hashCode = hashCode * 37 + month;
		}
		return hashCode;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) return false;
		MonthAndYear other = (MonthAndYear) obj;
		return other.month == month && other.year == year;
	}

	@Override
	public final int compareTo(MonthAndYear other) {
		if (other == null) return 1;
		if (year > other.year) return 1;
		if (year < other.year) return -1;
		if (month > other.month) return 1;
		if (month < other.month) return -1;
		return 0;
	}

	public DateRange getDateRange() {
		return new DateRange(new Date(year, month, 1), new Date(year, month, Tm.getDaysInMonth(year, month)));
	}

	public boolean isCurrent() {
		return equals(Date.today().getMonthAndYear());
	}

	public MonthAndYear addMonths(int months) {
		int years = months / 12;
		months = months - (years * 12);
		int newMonth = month + months;
		if (newMonth > 12) {
			years++;
			newMonth -= 12;
		} else if (newMonth <= 0) {
			years--;
			newMonth += 12;
		}
		int newYear = year + years;
		return new MonthAndYear(newYear, newMonth);
	}

}
