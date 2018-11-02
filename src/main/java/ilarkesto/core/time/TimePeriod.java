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
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.core.time;

import ilarkesto.core.base.Utl;

import java.io.Serializable;

public class TimePeriod implements Comparable<TimePeriod>, Serializable {

	public static final TimePeriod ZERO = new TimePeriod(0);

	private long millis;

	public TimePeriod(long millis, boolean abs) {
		this.millis = abs ? Math.abs(millis) : millis;
	}

	public TimePeriod(long millis) {
		this(millis, false);
	}

	public TimePeriod(String s) {
		int idx = s.indexOf(':');
		if (idx >= 0) {
			millis = 0;
			int i = 0;
			while (idx >= 0) {
				String num = s.substring(0, idx);
				long l = Long.parseLong(num);
				switch (i) {
					case 0:
						millis += l * Tm.HOUR;
						break;
					case 1:
						millis += l * Tm.MINUTE;
						break;
					case 2:
						millis += l * Tm.SECOND;
						break;
					default:
						throw new IllegalArgumentException(s);
				}
				i++;
				s = s.substring(idx + 1);
				idx = s.indexOf(':');
			}
			long l = Long.parseLong(s);
			switch (i) {
				case 0:
					millis += l * Tm.HOUR;
					break;
				case 1:
					millis += l * Tm.MINUTE;
					break;
				case 2:
					millis += l * Tm.SECOND;
					break;
				default:
					millis += l;
			}
		} else {
			this.millis = Long.parseLong(s);
		}
	}

	public TimePeriod() {
		this(0);
	}

	protected TimePeriod newTimePeriod(long millis) {
		return new TimePeriod(millis);
	}

	// ---

	public TimePeriod subtract(TimePeriod tp) {
		if (tp == null) return this;
		return newTimePeriod(millis - tp.millis);
	}

	public TimePeriod add(TimePeriod tp) {
		if (tp == null) return this;
		return newTimePeriod(millis + tp.millis);
	}

	public TimePeriod addDays(int days) {
		return newTimePeriod(millis + Tm.DAY);
	}

	public TimePeriod multiplyBy(int factor) {
		return newTimePeriod(millis * factor);
	}

	public TimePeriod divide(int divisor) {
		return newTimePeriod(millis / divisor);
	}

	public TimePeriod getPeriodTo(Time other) {
		return newTimePeriod(other.toMillis() - toMillis());
	}

	public TimePeriod abs() {
		return millis < 0 ? new TimePeriod(-millis) : this;
	}

	public final long toMillis() {
		return millis;
	}

	public final long toSeconds() {
		return Tm.toSeconds(millis);
	}

	public final long toMinutes() {
		return Tm.toMinutes(millis);
	}

	public final long toHours() {
		return Tm.toHours(millis);
	}

	public final int toDays() {
		return Tm.toDays(millis);
	}

	public final int toWeeks() {
		return Tm.toWeeks(millis);
	}

	public final int toMonths() {
		return Tm.toMonths(millis);
	}

	public final int toYears() {
		return Tm.toYears(millis);
	}

	public final float toDecimalDays() {
		return (float) millis / (float) Tm.DAY;
	}

	public final boolean isNegative() {
		return millis < 0;
	}

	public final boolean isPositive() {
		return millis > 0;
	}

	public String toHoursAndMinutesString() {
		long hours = toHours();
		long minutes = toMinutes() - (hours * 60);
		StringBuilder sb = new StringBuilder();
		sb.append(hours);
		sb.append(':');
		if (minutes < 10) sb.append('0');
		sb.append(minutes);
		return sb.toString();
	}

	public String toHoursAndMinutesAndSecondsString() {
		long hours = toHours();
		long minutes = toMinutes() - (hours * 60);
		long seconds = toSeconds() - (hours * 60 * 60) - (minutes * 60);
		StringBuilder sb = new StringBuilder();
		sb.append(hours);
		sb.append(':');
		if (minutes < 10) sb.append('0');
		sb.append(minutes);
		sb.append(':');
		if (seconds < 10) sb.append('0');
		sb.append(seconds);
		return sb.toString();
	}

	public String toShortestString() {
		return toShortestString(Utl.getLanguage());
	}

	public final String toShortestString(String language) {
		return Tm.getLocalizer(language).shortestPeriod(millis);
	}

	public final String toHoursAndMinutes() {
		long hours = toHours();
		long remainingMillis = millis - (hours * 3600000);
		long minutes = remainingMillis / 60000;
		return hours + (minutes > 9 ? ":" : ":0") + minutes;
	}

	public boolean isGreaterThen(TimePeriod other) {
		return millis > other.millis;
	}

	public boolean isLessThen(TimePeriod other) {
		return millis < other.millis;
	}

	@Override
	public final int compareTo(TimePeriod o) {
		if (millis == o.millis) return 0;
		return millis > o.millis ? 1 : -1;
	}

	@Override
	public final int hashCode() {
		return (int) millis;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof TimePeriod)) return false;
		return millis == ((TimePeriod) obj).millis;
	}

	@Override
	public final String toString() {
		return String.valueOf(millis);
	}

	// --- static ---

	public static TimePeriod seconds(int seconds) {
		return new TimePeriod(seconds * Tm.SECOND);
	}

	public static TimePeriod minutes(int minutes) {
		return new TimePeriod(minutes * Tm.MINUTE);
	}

	public static TimePeriod hours(int hours) {
		return new TimePeriod(hours * Tm.HOUR);
	}

	public static TimePeriod days(int days) {
		return new TimePeriod(days * Tm.DAY);
	}

	public static TimePeriod weeks(int weeks) {
		return new TimePeriod(weeks * Tm.WEEK);
	}

}
