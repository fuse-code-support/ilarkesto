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
package ilarkesto.integration.testde;

import ilarkesto.testng.ATest;

public class TestDeDatabaseTest extends ATest {

	// @Test
	// public void update() throws ParseException {
	// TestDeDatabase db = new TestDeDatabase(new SimpleFileStorage(getTestOutputFile("test.de")),
	// LoginDataProvider.NULL_PROVIDER);
	// db.updateIndex(observer);
	// ArticlesIndex index = db.getIndex(observer);
	// assertNotNull(index);
	// List<ArticleRef> articles = index.getArticles();
	// assertTrue(articles.size() >= 2770, articles.size() + " articles.");
	// }
	//
	// @Test
	// public void mapping() throws IOException, ilarkesto.json.JsonSaxParser.ParseException {
	// ArticleRef ar = new ArticleRef(new Date(), "Supertest", "supertest-23-0");
	//
	// ArticlesIndex index = new ArticlesIndex();
	// index.addNewArticles(Arrays.asList(ar));
	//
	// String json = JsonMapper.serialize(index);
	//
	// ArticlesIndex indexCopy = JsonMapper.deserialize(json, ArticlesIndex.class,
	// TestDeDatabase.typeResolver);
	//
	// assertEquals(index.getArticlesCount(), indexCopy.getArticlesCount());
	// }
}
