package ilarkesto.integration.law.gesetzeiminternetde;

import ilarkesto.base.Str;
import ilarkesto.core.time.Date;
import ilarkesto.io.IO;
import ilarkesto.io.SimpleFileStorage;
import ilarkesto.law.Book;
import ilarkesto.law.BookCache;
import ilarkesto.law.BookCacheCollection;
import ilarkesto.law.BookIndex;
import ilarkesto.law.BookIndexCache;
import ilarkesto.law.BookRef;
import ilarkesto.law.Norm;
import ilarkesto.law.Searcher;
import ilarkesto.law.Searcher.SearchResultConsumer;
import ilarkesto.testng.ATest;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

public class GiiTest extends ATest {

	@Test
	public void testIndex() {
		BookIndexCache indexCache = getBookCaches().bookIndexCache.get();
		indexCache.update(true);

		BookIndex index = indexCache.getPayload();
		List<BookRef> books = index.getBooks();
		assertNotEmpty(books);

		IO.writeFile(getTestOutputFile("GiiBookIndex.json"), index.getJson().toFormatedString(), IO.UTF_8);

		// BookRef binSchStrOAbweichV = index.getBookByCode("64BinSchStrOAbweichV");
		// assertNotNull(binSchStrOAbweichV);
		// assertEquals(binSchStrOAbweichV.getTitle(),
		// "Vierundsechzigste Verordnung zur vorübergehenden Abweichung von der
		// Binnenschifffahrtsstraßen-Ordnung");

		Set<String> codes = new HashSet<String>();
		for (BookRef book : books) {
			String code = book.getCode();
			if (Str.containsNonLetterOrDigit(code)) fail("Illegal book code: " + code);
			if (codes.contains(code)) fail("Duplicate book code: " + code);
			codes.add(code);
		}
	}

	@Test
	public void searchBGB() {
		SearchResultConsumerImpl consumer = new SearchResultConsumerImpl();
		BookCacheCollection bookCaches = getBookCaches();
		bookCaches.getBookCache("BGB").update(true);
		Searcher searcher = new Searcher("BGB", consumer, bookCaches, false);
		searcher.run();

		assertContains(consumer.books, getBookIndex().getBookByCode("BGB"));
		assertTrue(consumer.norms.isEmpty());
	}

	@Test
	public void searchUrhg2() {
		SearchResultConsumerImpl consumer = new SearchResultConsumerImpl();
		BookCacheCollection bookCaches = getBookCaches();
		bookCaches.getBookCache("UrhG").update(true);
		Searcher searcher = new Searcher("UrhG 10", consumer, bookCaches, false);
		searcher.run();

		BookRef urhgRef = getBookIndex().getBookByCode("UrhG");
		BookCache cache = bookCaches.getBookCache(urhgRef);
		Book urhg = cache.getPayload();
		if (urhg == null) {
			cache.update(false);
			urhg = cache.getPayload();
		}
		Norm urhg10 = urhg.getNormByCodeNumber("10");

		assertContains(consumer.books, urhgRef);
		assertContains(consumer.norms, urhg10);
	}

	@Test
	public void testRfbv() {
		BookRef ref = getBookIndex().getBookByCode("RfBVerordnungRfBV");
		Book book = getGii().loadBook(ref);
		assertEquals(book.getRef().getCode(), "RfBVerordnungRfBV");
		assertEquals(book.getRef().getTitle(),
			"Verordnung über den kollektiven Teil der Rückstellung für Beitragsrückerstattung");
	}

	@Test
	public void testSgb10Kap12() {
		BookRef ref = getBookIndex().getBookByCode("SGB10Kap12");
		Book book = getGii().loadBook(ref);
		assertEquals(book.getRef().getCode(), "SGB10Kap12");

		List<Norm> norms = book.getAllNorms();
		assertSize(norms, 4);
	}

	// @Test
	// public void testEgbgb() {
	// BookRef ref = getBookIndex().getBookByCode("EGBGB");
	// Book book = getGii().loadBook(ref);
	// assertNotNull(book);
	//
	// Norm n1 = book.getNormByCodeNumber("1");
	// assertEquals(n1.getRef().getCode(), "Art 1");
	// }

	@Test
	public void testStgb() {
		BookRef ref = getBookIndex().getBookByCode("StGB");
		Book book = getGii().loadBook(ref);
		assertNotNull(book);

		List<Norm> norms = book.getAllNorms();
		assertSize(norms, 538);
	}

	@Test
	public void testAEG() {
		BookRef ref = getBookIndex().getBookByCode("AEG");
		Book book = getGii().loadBook(ref);
		assertNotNull(book);

		List<Norm> norms = book.getAllNorms();
		assertSize(norms, 70);
	}

	@Test
	public void testGG() {
		BookRef ref = getBookIndex().getBookByCode("GG");
		Book book = getGii().loadBook(ref);
		assertNotNull(book);
		assertSize(book.getAllNorms(), 199);

		Norm n76 = book.getNormByCodeNumber("76");
		assertNotNull(n76);
		assertEquals(n76.getRef().getCode(), "Art 76");
	}

	@Test
	public void testWoGG() {
		BookRef ref = getBookIndex().getBookByCode("WoGG");
		Book book = getGii().loadBook(ref);
		assertNotNull(book);

		List<Norm> norms = book.getAllNorms();
		Norm norm1 = norms.get(0);
		assertEquals(norm1.getRef().getCode(), "§ 1");
	}

	@Test
	public void testStvg() {
		BookRef ref = getBookIndex().getBookByCode("StVG");
		Book book = getGii().loadBook(ref);
		assertNotNull(book);
		assertEquals(book.getRef().getCode(), "StVG");
		assertEquals(book.getRef().getTitle(), "Straßenverkehrsgesetz");

		List<Norm> norms = book.getAllNorms();
		assertSize(norms, 102);

		Norm n1 = norms.get(0);
		assertStartsWith(n1.getTextAsString(), "(1) Kraftfahrzeuge ");
		assertContains(n1.getTextAsString(), "(2) Als Kraftfahrzeuge im Sinne ");
	}

	@Test
	public void testStvo() {
		BookRef ref = getBookIndex().getBookByCode("StVO");
		Book book = getGii().loadBook(ref);
		assertNotNull(book);
		assertEquals(book.getIssueDate(), new Date(2013, 3, 6));
		assertContains(book.getStatusComment(), "in Kraft");
		assertContains(book.getFooterAsHtml(), "Textnachweis");
		List<Norm> norms = book.getAllNorms();
		assertSize(norms, 61);
	}

	@Test
	public void testBgb() {
		BookRef ref = getBookIndex().getBookByCode("BGB");
		Book book = getGii().loadBook(ref);
		log.debug(book.getJson().toFormatedString());
		assertEquals(book.getRef().getCode(), "BGB");
		assertEquals(book.getRef().getTitle(), "Bürgerliches Gesetzbuch");
	}

	@Test
	public void testAabg() {
		BookRef ref = getBookIndex().getBookByCode("AABG");
		Book book = getGii().loadBook(ref);
		assertEquals(book.getRef().getCode(), "AABG");
		assertEquals(book.getRef().getTitle(),
			"Gesetz zur Begrenzung der Arzneimittelausgaben der gesetzlichen Krankenversicherung");

		assertSize(book.getAllNorms(), 4);
	}

	@Test(enabled = false)
	public void giiReferences() {
		GiiLawProvider gii = getGii();
		for (BookRef bookRef : getBookIndex().getBooks()) {
			String code = bookRef.getCode();
			String reference = bookRef.getJson().getString("giiReference");

			System.out.println(code);
			assertEquals(gii.getGiiReference(code), reference);

			// if (!gii.getGiiReference(code).equals(reference)) {
			// System.out.println(" GII_REFERENCES.put(\"" + code + "\", \"" + reference + "\");");
			// }
		}
	}

	// --- ---

	private BookIndex getBookIndex() {
		BookIndexCache indexCache = getBookCaches().bookIndexCache.get();
		indexCache.update(false);

		BookIndex index = indexCache.getPayload();
		assertNotEmpty(index.getBooks());
		return index;
	}

	private BookCacheCollection getBookCaches() {
		return new BookCacheCollection(getGii());
	}

	private GiiLawProvider getGii() {
		return new GiiLawProvider(new SimpleFileStorage(getTestOutputFile("books")));
	}

	class SearchResultConsumerImpl implements SearchResultConsumer {

		private List<BookRef> books = new LinkedList<BookRef>();
		private List<Norm> norms = new LinkedList<Norm>();

		@Override
		public void onBookFound(BookRef book) {
			books.add(book);
			log.info("Book found:", book);
		}

		@Override
		public void onNormFound(Norm norm) {
			norms.add(norm);
			log.info("Norm found:", norm);
		}

		@Override
		public void onSearchFinished() {}

		@Override
		public void onSearchBroadenedToCachedBooks() {}

		@Override
		public void onSearchRestricted(String code) {}

	}

}
