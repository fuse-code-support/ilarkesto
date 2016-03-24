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

	@Test
	public void parseWithParameters() throws ParseException {
		assertParseToString("<!doctype html><html><div id=\"a\">test</div></html>");
	}

	@Test
	public void parseWithParametersWithoutQuotas() throws ParseException {
		HtmlPage page = new HtmlParser().parse("<!doctype html><html><div width=2>test</div></html>");
		HtmlTag div = page.getTagByName("div");
		assertEquals(div.getAttribute("width"), "2");
	}

	@Test
	public void parseWithParametersWithGt() throws ParseException {
		HtmlPage page = new HtmlParser().parse("<!doctype html><html><div attr=\"a>b\">test</div></html>");
		HtmlTag div = page.getTagByName("div");
		assertEquals(div.getAttribute("attr"), "a>b");
	}

	@Test
	public void parseWithComments() throws ParseException {
		assertParseToString("<!doctype html><html><!--comment-->hello world</html>");
	}

	@Test
	public void parseWithScript() throws ParseException {
		assertParseToString("<!doctype html><html><script> script <code> here</script></html>");
	}

	@Test
	public void parseWithSpaceInTag() throws ParseException {
		HtmlParser parser = new HtmlParser();
		parser.parse("<!doctype html><html ></html>");
	}

	private static void assertParseToString(String html) throws ParseException {
		assertEquals(new HtmlParser().parse(html).toHtml(), html);
	}

}
