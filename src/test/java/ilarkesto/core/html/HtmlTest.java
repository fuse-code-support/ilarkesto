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
package ilarkesto.core.html;

import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class HtmlTest extends ATest {

	@Test
	public void removeTag() {
		assertEquals(Html.removeTag("a<br>b", "br"), "ab");
		assertEquals(Html.removeTag("<br>a", "br"), "a");
		assertEquals(Html.removeTag("<br>a<br>b<br>", "br"), "ab");
		assertEquals(Html.removeTag("<p>hello</p>", "p"), "hello");
		assertEquals(Html.removeTag("<p class=\"boo\">hello</p>", "p"), "hello");
	}

	@Test
	public void removeStyleAttribute() {
		assertEquals(Html.removeStyleAttribute("<p style=\"color: blue;\">a<p>", "color"), "<p style=\"\">a<p>");
	}

}
