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

import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class TimePeriodTest extends ATest {

	@Test
	public void subtract() {
		assertEquals(TimePeriod.hours(8).subtract(TimePeriod.hours(1)), new TimePeriod("7:00"));
		assertEquals(TimePeriod.hours(8).subtract(TimePeriod.minutes(30)), new TimePeriod("7:30"));
	}

	@Test
	public void getPeriodTo() {
		Time begin = new Time("9:00");
		Time end = new Time("17:30");

		assertEquals(begin.getPeriodTo(end), new TimePeriod(8 * Tm.HOUR + 30 * Tm.MINUTE));
	}

	@Test
	public void constructors() {
		assertEquals(TimePeriod.seconds(1).toMillis(), 1000);
		assertEquals(TimePeriod.minutes(1).toMillis(), 60000);
		assertEquals(TimePeriod.hours(1).toMillis(), 3600000);

		assertEquals(TimePeriod.days(1).toMillis(), 86400000);
		assertEquals(TimePeriod.days(2).toMillis(), 86400000l * 2);
		assertEquals(TimePeriod.days(5).toMillis(), 86400000l * 5);
		assertEquals(TimePeriod.days(15).toMillis(), 86400000l * 15);
		assertEquals(TimePeriod.days(24).toMillis(), 86400000l * 24);
		assertEquals(TimePeriod.days(25).toMillis(), 86400000l * 25);
		assertEquals(TimePeriod.days(30).toMillis(), 86400000l * 30);

		assertEquals(TimePeriod.weeks(1).toMillis(), 604800000);
	}

	@Test
	public void toDecimalDays() {
		assertEquals(TimePeriod.days(1).toDecimalDays(), 1.0f, 0.0001f);
		assertEquals(TimePeriod.hours(12).toDecimalDays(), 0.5f, 0.0001f);
	}

	@Test
	public void toDays() {
		assertEquals(new TimePeriod(Tm.DAY).toDays(), 1);
		assertEquals(new TimePeriod(Tm.DAY * 2).toDays(), 2);
		assertEquals(new TimePeriod(Tm.DAY * 13).toDays(), 13);
		assertEquals(new TimePeriod(Tm.DAY * 512).toDays(), 512);
	}

	@Test
	public void toShortestString() {
		assertEquals(new TimePeriod(Tm.SECOND * 20).toShortestString("en"), "20 seconds");
		assertEquals(new TimePeriod(Tm.MINUTE).toShortestString("en"), "1 minute");
		assertEquals(new TimePeriod(Tm.HOUR * 3).toShortestString("en"), "3 hours");
		assertEquals(new TimePeriod(Tm.DAY * 7).toShortestString("en"), "7 days");
		assertEquals(new TimePeriod(Tm.WEEK * 2).toShortestString("en"), "2 weeks");
		assertEquals(new TimePeriod(Tm.MONTH * 11).toShortestString("en"), "11 months");
	}

	@Test
	public void constructorMillis() {
		assertEquals(new TimePeriod("1").toMillis(), 1);
	}

	@Test
	public void constructorTime() {
		assertEquals(new TimePeriod("0:0:0:1").toMillis(), 1);
	}

}
