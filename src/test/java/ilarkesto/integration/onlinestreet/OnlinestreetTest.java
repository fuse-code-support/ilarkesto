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
package ilarkesto.integration.onlinestreet;

import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class OnlinestreetTest extends ATest {

	@Test
	public void getHtmlByPLZ() {
		String html = Onlinestreet.getHtmlByPlz(30159);
		assertContains(html, "Hannover");
	}

	// @Test
	// public void getCityByPLZ() {
	// assertEquals(Onlinestreet.getCityByPlz(31737), "Rinteln");
	// assertEquals(Onlinestreet.getCityByPlz(30159), "Hannover");
	// }
	//
	// @Test
	// public void getCitiesByPLZ() {
	// List<String> cities = Onlinestreet.getCitiesByPlz(31737);
	// assertContains(cities, "Rinteln");
	// assertContains(cities, "Strücken");
	// }

}
