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
package ilarkesto.html.dom;

import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class HtmlParserTest extends ATest {

	@Test
	public void parseSimplest() throws ParseException {
		assertParseToString("<!doctype html><html></html>");
		assertParseToString("<!doctype html><html>content</html>");
	}

	@Test
	public void parseNested() throws ParseException {
		assertParseToString("<!doctype html><html><title>hello world</title></html>");
		assertParseToString("<!doctype html><html><p>hello</p> <p>world</p></html>");
	}

	private void assertParseToString(String html) throws ParseException {
		assertEquals(new HtmlParser().parse(html).toString(), html);
	}

}
