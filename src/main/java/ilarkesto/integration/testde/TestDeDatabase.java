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

import ilarkesto.core.auth.LoginData;
import ilarkesto.core.auth.LoginDataProvider;
import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Tm;
import ilarkesto.integration.testde.TestDe.Article;
import ilarkesto.integration.testde.TestDe.ArticleRef;
import ilarkesto.integration.testde.TestDe.ArticlesIndex;
import ilarkesto.integration.testde.TestDe.SubArticleRef;
import ilarkesto.io.AFileStorage;
import ilarkesto.io.IO;
import ilarkesto.io.SimpleFileStorage;
import ilarkesto.io.TextFileCache;
import ilarkesto.json.JsonMapper;
import ilarkesto.json.JsonMapper.TypeResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestDeDatabase {

	public static void main(String[] args) throws ParseException {
		TestDeDatabase db = new TestDeDatabase(new SimpleFileStorage(new File("runtimedata/test.de")),
				LoginDataProvider.NULL_PROVIDER);
		List<ArticleRef> newArticles = db.updateIndex(OperationObserver.DUMMY);
		log.info("New articles:", newArticles);
	}

	private static Log log = Log.get(TestDeDatabase.class);

	private AFileStorage storage;

	private TextFileCache articlePagesCache;
	private ArticlesIndex index;
	private Class indexResourcePath;

	private ArticleList viewed;
	private ArticleList favorites;
	private LoginDataProvider loginDataProvider;

	public TestDeDatabase(AFileStorage storage, LoginDataProvider loginDataProvider) {
		super();
		this.storage = storage;
		this.loginDataProvider = loginDataProvider;

		articlePagesCache = new TextFileCache(storage.getSubStorage("articlePagesCache"), new ArticlePageLoader());

		viewed = new ArticleList("viewedArticles.txt");
		favorites = new ArticleList("favoriteArticles.txt");
	}

	public ArticleList getViewed() {
		return viewed;
	}

	public ArticleList getFavorites() {
		return favorites;
	}

	public File loadPdf(SubArticleRef subArticleRef, ArticleRef articleRef, OperationObserver observer)
			throws ParseException {
		if (!subArticleRef.isPdf()) throw new IllegalArgumentException("Sub article is not PDF: " + subArticleRef);
		File pdfFile = getPdfFile(subArticleRef);
		if (pdfFile.exists()) return pdfFile;
		loginIfAvailable(observer);
		TestDe.downloadPdf(subArticleRef, articleRef, pdfFile, observer);
		return pdfFile;
	}

	private File getPdfFile(SubArticleRef ref) {
		String filename = Str.toFileCompatibleString(ref.getPageRef());
		return storage.getFile(filename);
	}

	public void preloadArticle(ArticleRef articleRef, OperationObserver observer) throws ParseException {
		Article article = loadArticle(articleRef, observer);
		for (SubArticleRef subArticle : article.getSubArticles()) {
			if (subArticle.isLocked()) continue;
			if (subArticle.isPdf()) {
				loadPdf(subArticle, articleRef, observer);
			} else {
				loadSubArticleHtml(subArticle, observer);
			}
		}
	}

	public Article loadArticle(ArticleRef ref, OperationObserver observer) throws ParseException {
		String html = articlePagesCache.loadFromCache(ref.getPageRef(), observer);
		if (html != null) {
			Article article = TestDe.parseArticle(ref, html);
			if (!article.containsLockedSubArticles()) return article;
			if (loginDataProvider.getLoginData() == null) return article;
			// if (!TestDe.http.isInternetAvailable()) return article;
			articlePagesCache.delete(ref.getPageRef());
		}
		loginIfAvailable(observer);
		html = articlePagesCache.load(ref.getPageRef(), observer);
		return TestDe.parseArticle(ref, html);
	}

	private synchronized void loginIfAvailable(OperationObserver observer) {
		LoginData loginData = loginDataProvider.getLoginData();
		if (loginData == null) return;
		if (getLastLoginAgeInMinutes() < 10) return;
		login(loginData, observer);
	}

	private long getLastLoginAgeInMinutes() {
		return (System.currentTimeMillis() - lastLogin) / Tm.MINUTE;
	}

	private long lastLogin;

	private synchronized void login(LoginData loginData, OperationObserver observer) {
		lastLogin = 0;
		TestDe.login(loginData, observer);
		lastLogin = System.currentTimeMillis();
	}

	public void clearArticle(ArticleRef articleRef) {
		articlePagesCache.delete(articleRef.getPageRef());
	}

	public void clearSubArticle(SubArticleRef subArticleRef) {
		articlePagesCache.delete(subArticleRef.getPageRef());
	}

	private String loadSubArticleHtml(String pageRef, OperationObserver observer) {
		return articlePagesCache.load(pageRef, observer);
	}

	public String loadSubArticleHtml(SubArticleRef ref, OperationObserver observer) {
		loginIfAvailable(observer);
		return loadSubArticleHtml(ref.getPageRef(), observer);
	}

	private synchronized boolean importFromResource() {
		if (indexResourcePath == null) return false;
		File indexFile = getIndexFile();
		if (indexFile.exists()) return false;
		log.info("Importing index from resource");
		IO.copyResource("index.json", indexFile, indexResourcePath);
		return true;
	}

	public TestDeDatabase setIndexResourcePath(Class indexResourcePath) {
		this.indexResourcePath = indexResourcePath;
		return this;
	}

	public synchronized ArticlesIndex getIndex(OperationObserver observer) {
		if (index == null) {
			boolean imported = importFromResource();
			File indexFile = getIndexFile();
			observer.onOperationInfoChanged(OperationObserver.LOADING_CACHE, indexFile.getAbsolutePath());
			if (!indexFile.exists()) {
				log.info("Article index file does not exist:", indexFile);
				index = new ArticlesIndex();
				return index;
			}
			try {
				index = JsonMapper.deserialize(indexFile, ArticlesIndex.class, typeResolver);
				log.info("Article index loaded:", index.getArticlesCount(), "articles");
			} catch (Exception ex) {
				log.error("Article indes loading failed:", ex);
				indexFile.delete();
				return getIndex(observer);
			}
			if (imported) {
				viewed.addAll(index.getArticles());
			} else {
				for (ArticleRef article : index.getArticles()) {
					article.repairTitle();
				}
			}
		}

		return index;
	}

	// public void updateIndexIfNecessary(OperationObserver observer) {
	// File indexFile = getIndexFile();
	// TimePeriod age = new DateAndTime(indexFile.lastModified()).getPeriodToNow();
	// long ageInHours = age.toHours();
	// log.info("Last update was", ageInHours, "hours ago");
	// if (indexFile.exists() && ageInHours < 24) return;
	// updateIndex(observer);
	// }

	public synchronized List<ArticleRef> updateIndex(OperationObserver observer) {
		getIndex(observer);
		List<ArticleRef> newArticles;
		try {
			newArticles = TestDe.update(index, observer);
		} catch (ParseException ex1) {
			throw new RuntimeException(ex1);
		}
		File indexFile = getIndexFile();
		observer.onOperationInfoChanged(OperationObserver.SAVING, indexFile.getAbsolutePath());
		try {
			JsonMapper.serialize(index, indexFile);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return newArticles;
	}

	public List<ArticleRef> getFavoriteArticles(OperationObserver observer) {
		ArticlesIndex index = getIndex(observer);
		List<ArticleRef> ret = new ArrayList<ArticleRef>();
		for (ArticleRef ref : index.getArticles()) {
			if (!favorites.contains(ref)) continue;
			ret.add(ref);
		}
		return ret;
	}

	public List<ArticleRef> getNewArticles(OperationObserver observer) {
		ArticlesIndex index = getIndex(observer);
		List<ArticleRef> ret = new ArrayList<ArticleRef>();
		for (ArticleRef ref : index.getArticles()) {
			if (viewed.contains(ref)) continue;
			ret.add(ref);
		}
		return ret;
	}

	public File getIndexFile() {
		return storage.getFile("index.json");
	}

	static final TypeResolver typeResolver = new TypeResolver() {

		@Override
		public Class resolveType(Object object, String field) {
			return null;
		}

		@Override
		public Class resolveArrayType(Object object, String field) {
			return ArticleRef.class;
		}
	};

	public class ArticleList {

		private String filename;
		private Set<String> pageIds;

		public ArticleList(String filename) {
			super();
			this.filename = filename;
			load();
		}

		public boolean contains(ArticleRef articleRef) {
			if (articleRef == null) return false;
			return pageIds.contains(articleRef.getPageId());
		}

		public synchronized void add(ArticleRef articleRef) {
			pageIds.add(articleRef.getPageId());
			save();
		}

		public synchronized void remove(ArticleRef articleRef) {
			pageIds.remove(articleRef.getPageId());
			save();
		}

		public synchronized void addAll(Collection<ArticleRef> articleRefs) {
			for (ArticleRef articleRef : articleRefs) {
				pageIds.add(articleRef.getPageId());
			}
			save();
		}

		private synchronized void save() {
			PrintWriter out;
			try {
				out = new PrintWriter(new FileWriter(getFile()));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			for (String id : pageIds) {
				out.println(id);
			}
			IO.close(out);
		}

		private synchronized void load() {
			pageIds = new HashSet<String>();
			File file = getFile();
			if (file.exists()) {
				BufferedReader in;
				try {
					in = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException ex) {
					throw new RuntimeException(ex);
				}
				try {
					String line;
					while ((line = in.readLine()) != null) {
						String articleId = line.trim();
						if (Str.isBlank(articleId)) continue;
						pageIds.add(articleId);
					}
				} catch (IOException ex) {
					log.error(ex);
				} finally {
					IO.closeQuiet(in);
				}
			}
		}

		private File getFile() {
			return storage.getFile(filename);
		}
	}

}
