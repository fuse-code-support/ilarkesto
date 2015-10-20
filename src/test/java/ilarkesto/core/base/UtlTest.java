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
package ilarkesto.core.base;

import ilarkesto.testng.ATest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

public class UtlTest extends ATest {

	@Test
	void getOptimalScale() {
		assertEquals(Utl.getOptimalScale(new BigDecimal("1")), 0);
		assertEquals(Utl.getOptimalScale(new BigDecimal("1.0")), 0);

		assertEquals(Utl.getOptimalScale(new BigDecimal("1.1")), 1);
		assertEquals(Utl.getOptimalScale(new BigDecimal("1.10")), 1);

		assertEquals(Utl.getOptimalScale(new BigDecimal("1.123")), 3);
		assertEquals(Utl.getOptimalScale(new BigDecimal("1.001000")), 3);
	}

	@Test
	public void divide() {
		assertEquals(Utl.divide(new BigDecimal("1"), new BigDecimal("2")), new BigDecimal("0.5"));
	}

	@Test
	public void divideWithScale() {
		assertEquals(Utl.divide(new BigDecimal("1"), new BigDecimal("2"), 1), new BigDecimal("0.5"));
		assertEquals(Utl.divide(new BigDecimal("1"), new BigDecimal("4"), 1), new BigDecimal("0.3"));
		assertEquals(Utl.divide(new BigDecimal("1"), new BigDecimal("4"), 2), new BigDecimal("0.25"));
	}

	@Test
	public void sortReverse() {
		List<String> list = new ArrayList<String>();
		list.add("c");
		list.add("a");
		list.add("b");
		Utl.sortReverse(list);
		assertEquals(list.get(0), "c");
		assertEquals(list.get(1), "b");
		assertEquals(list.get(2), "a");
	}

	@Test
	public void moveElementUp() {
		assertEquals(Utl.moveElementUp(Utl.arrayList("a", "b", "c"), "b"), Utl.arrayList("b", "a", "c"));
		assertEquals(Utl.moveElementUp(Utl.arrayList("a", "b", "c"), "a"), Utl.arrayList("a", "b", "c"));
		assertEquals(Utl.moveElementUp(Utl.arrayList("a", "c"), "b"), Utl.arrayList("a", "b", "c"));
	}

	@Test
	public void moveElementDown() {
		assertEquals(Utl.moveElementDown(Utl.arrayList("a", "b", "c"), "b"), Utl.arrayList("a", "c", "b"));
		assertEquals(Utl.moveElementDown(Utl.arrayList("a", "b", "c"), "c"), Utl.arrayList("a", "b", "c"));
		assertEquals(Utl.moveElementDown(Utl.arrayList("a", "c"), "b"), Utl.arrayList("a", "c", "b"));
	}

	@Test
	public void equals() {
		Object[] a = { "hello", "equals" };
		Object[] b = { "hello", "equals" };
		Object[] c = { "hello", "world" };
		assertTrue(Utl.equals(a, b));
		assertFalse(Utl.equals(a, c));
	}

	@Test
	public void removeDuplicates() {
		List<String> list = Utl.toList("a", "b", "b", "a");
		Utl.removeDuplicates(list);
		assertEquals(list, Utl.toList("a", "b"));
	}

}
