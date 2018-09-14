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
package ilarkesto.io;

import ilarkesto.core.base.MapBuilder;
import ilarkesto.json.JsonObject;
import ilarkesto.testng.ATest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.testng.annotations.Test;

public class IOTest extends ATest {

	@Test
	public void textFiles() throws FileNotFoundException {
		File file = getTestOutputFile("text.txt");
		IO.writeFile(file, "abc", IO.UTF_8);

		assertEquals(IO.readFile(file, IO.UTF_8), "abc");

		assertEquals(IO.readToString(new FileInputStream(file), IO.UTF_8), "abc");
	}

	@Test
	public void post() throws MalformedURLException, IOException {
		String url = "http://httpbin.org/post";
		Map parameters = new MapBuilder(true).put("field1", "a").put("field2", "ü").put("field3", "c").getMap();

		String result = IO.postAndGetResult(url, parameters, IO.UTF_8, null, null);

		// String result = new ApacheHttpDownloader().post(url, parameters, IO.UTF_8);

		log.info(result);
		JsonObject jForm = new JsonObject(result).getObject("form");
		assertEquals(jForm.getString("field1"), "a");
		assertEquals(jForm.getString("field2"), "ü");
		assertEquals(jForm.getString("field3"), "c");
	}

	@Test
	public void stringInputStream() {
		assertEquals(IO.readToString(new IO.StringInputStream("täst", IO.UTF_8), IO.UTF_8), "täst");
		assertEquals(IO.readToString(new IO.StringInputStream("täst", IO.ISO_LATIN_1), IO.ISO_LATIN_1), "täst");
	}
}
