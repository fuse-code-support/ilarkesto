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
package ilarkesto.net.httpclient;

import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class HttpSessionTest extends ATest {

	@Test
	public void testWithCache() {
		String url = "https://servisto.de/projects/ilarkesto/etag-test.html";
		String content = "etag-test 1\n";

		HttpSession session = new HttpSession();
		session.setCache(null);

		assertEquals(session.downloadText(url), content);

		session.setCache(new HttpCache(getTestOutputFile("cache")));

		assertEquals(session.downloadText(url), content);
		assertEquals(session.downloadText(url), content);
	}

	@Test
	public void https() {
		String text = new HttpSession().disableSsl().downloadText("https://kunagi-demo.servisto.de/kunagi/login.html");
		System.out.println(text);
	}
}
