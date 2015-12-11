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
package ilarkesto.integration.jura;

import ilarkesto.core.parsing.sax.ParseException;
import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class JuraNormReferenceTest extends ATest {

	@Test
	public void parse() throws ParseException {
		assertNorm(JuraNormReference.parse("§ 102 BGB"), "BGB", "102", null);
		assertNorm(JuraNormReference.parse("§ 102 I, II BGB"), "BGB", "102", "I, II");

		assertNorm(JuraNormReference.parse("§§ 102, 103 BGB"), "BGB", "102", "103");
		assertNorm(JuraNormReference.parse("§§ 102-107 BGB"), "BGB", "102", "-107");

		assertNorm(JuraNormReference.parse("Art. 5 GG"), "GG", "5", null);
	}

	@Test
	public void testToString() {
		assertEquals(new JuraNormReference("BGB", "102").toString(), "§ 102 BGB");
	}

	private void assertNorm(JuraNormReference ref, String book, String code, Object sections) {
		assertNotNull(ref);
		assertEquals(ref.getBook(), book);
		assertEquals(ref.getNorm(), code);
		assertEquals(ref.getSections(), sections);
	}

}
