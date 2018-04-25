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
package ilarkesto.integration.rintelnde;

import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.time.Date;
import ilarkesto.integration.rintelnde.BissIndex.Lebenslage;
import ilarkesto.integration.rintelnde.BissIndex.Lebenslage.Anliegen;
import ilarkesto.integration.rintelnde.Branchenbuch.Category;
import ilarkesto.integration.rintelnde.Branchenbuch.Entry;
import ilarkesto.testng.ATest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

public class RintelnDeTest extends ATest {

	private static final OperationObserver observer = OperationObserver.DUMMY;

	@Test
	public void downloadPressemitteilungen() throws ParseException {
		List<Pressemitteilung> mitteilungen = RintelnDe.downloadPressemitteilungen(observer);
		assertNotEmpty(mitteilungen);

		Pressemitteilung mitteilung = mitteilungen.get(0);
		assertNotNull(mitteilung.getDate());
		String html = RintelnDe.downloadPressemitteilungContent(mitteilung.getId(), observer);
		log.info(mitteilung, "->", html);
		assertNotNull(html);

		Pressemitteilungen pressemitteilungen = new Pressemitteilungen();
		RintelnDe.downloadPressemitteilungen(pressemitteilungen, observer);
		assertEquals(pressemitteilungen.getMitteilungen().size(), mitteilungen.size());
		RintelnDe.downloadPressemitteilungen(pressemitteilungen, observer);
		assertEquals(pressemitteilungen.getMitteilungen().size(), mitteilungen.size());
	}

	@Test
	public void downloadBranchenbuch() throws ParseException {
		Branchenbuch branchenbuch = RintelnDe.downloadBranchenbuch(observer);
		assertNotNull(branchenbuch);

		List<Category> masterCategories = branchenbuch.getCategories();
		assertSize(masterCategories, 14);
		Category mcBehoerden = masterCategories.get(2);
		assertEquals(mcBehoerden.getLabel(), "Behörden & Verbände");

		List<Category> behoerdenCategories = mcBehoerden.getCategories();
		assertSize(behoerdenCategories, 3);
		Category cVerbaende = behoerdenCategories.get(2);
		assertEquals(cVerbaende.getLabel(), "Verbände, Vereine");

		int leafCategoriesCount = branchenbuch.getLeafCategoriesCount();
		log.info("leafCategoriesCount", leafCategoriesCount);
		assertTrue(leafCategoriesCount >= 141);

		branchenbuch.getJson().write(getTestOutputFile("branchenbuch.json"), true);
	}

	@Test
	public void downloadBranchenbuchEntriesVerbaende() throws ParseException {
		List<Entry> verbaende = RintelnDe.downloadBranchenbuchEntries(28, observer);
		assertSize(verbaende, 9);
		Entry volkshochschule = verbaende.get(8);
		assertEquals(volkshochschule.getLabel(), "Ver.di Senioren Ortsverein Rinteln");
	}

	@Test
	public void downloadCalendar() throws ParseException {
		for (int i = 0; i < 7; i++) {
			Date date = Date.inDays(i);
			Collection<Integer> ids = RintelnDe.downloadCalendarEventIds(date, observer);
			for (Integer id : ids) {
				Map<String, String> fields = RintelnDe.downloadCalendarEntryFields(id, observer);
				String label = fields.get(RintelnDe.CALENDAR_ENTRY_FIELD_LABEL);
				assertNotNull(label);
				log.info(date, "->", label);
				assertFalse(label.contains("ERROR"));
			}
		}
	}

	// @Test
	// public void downloadCalendarEntryFields() throws ParseException {
	// Map<String, String> event = RintelnDe.downloadCalendarEntryFields(5644, observer);
	// assertEquals(event.get(RintelnDe.CALENDAR_ENTRY_FIELD_LABEL),
	// "Gottesdienst in der Ev.-luth. St. Nikolai-Kirche Rinteln");
	// assertEquals(
	// event.get(RintelnDe.CALENDAR_ENTRY_FIELD_IMAGE_URL),
	// "http://v2.cos.commercio.de/assets/1b33a22b-a3f8-424b-a994-84e7245f0f0a/_resampled/SetRatioSize320240-16a11a30-aaf5-4f4d-badf-c881817a118f.jpg");
	// assertEquals(event.get("Termine"), "Wöchentlich am Sonntag ab 10:00 Uhr.");
	// assertEquals(event.get("Treffpunkt"), "St. Nikolai-Kirche, Kirchplatz, 31737 Rinteln");
	// assertEquals(event.get("Ort"), "Rinteln");
	// assertEquals(event.get("Organisator"), "Ev.-luth. St. Nikolai-Kirche Rinteln");
	// assertContains(event.get(RintelnDe.CALENDAR_ENTRY_FIELD_DESCRIPTION),
	// "Weitere Gottesdiensttermine auch auf der Homepage");
	// }
	//
	// @Test
	// public void downloadCalendarEventIds() throws ParseException {
	// Collection<Integer> ids = RintelnDe.downloadCalendarEventIds(new Date(2014, 8, 4), observer);
	// assertContains(ids, 8305);
	// // assertSize(ids, 3);
	// }

	@Test
	public void extractPageContentBoxes() {
		String html = RintelnDe.downloadPageContent("app-biss/anliegen/1615_", observer);
		List<String> boxes = RintelnDe.extractPageContentBoxes(html);
		assertSize(boxes, 3);
	}

	@Test
	public void downloadBissIndex() throws ParseException {
		BissIndex bisIndex = RintelnDe.downloadBissIndex(observer);
		assertNotNull(bisIndex);
		List<Lebenslage> lebenslages = bisIndex.getLebenslages();
		assertSize(lebenslages, 9);
		for (Lebenslage lebenslage : lebenslages) {
			List<Anliegen> anliegens = lebenslage.getAnliegens();
			assertTrue(anliegens.size() > 0);
		}

		bisIndex.getJson().write(getTestOutputFile("bissIndex.json"), true);
	}

	@Test
	public void downloadBissAnliegenAusbildungBerufGGewerbe() throws ParseException {
		List<Anliegen> anliegens = RintelnDe.downloadBissAnliegens(1561, observer);
		assertTrue(anliegens.size() >= 50);
		Anliegen arbeitslosigkeit = anliegens.get(0);
		assertEquals(arbeitslosigkeit.getLabel(), "Arbeitslosigkeit");
	}

	@Test
	public void downloadBissAnliegenWohnenBauen() throws ParseException {
		List<Anliegen> anliegens = RintelnDe.downloadBissAnliegens(1569, observer);
		assertTrue(anliegens.size() >= 50);
	}

	@Test
	public void downloadBissLebenslages() throws ParseException {
		List<Lebenslage> lebenslages = RintelnDe.downloadBissLebenslages(observer);
		assertSize(lebenslages, 9);
		assertEquals("Wohnen und Bauen", lebenslages.get(8).getLabel());
	}

	@Test
	public void downloadHome() {
		String content = RintelnDe.downloadPageContent(RintelnDe.PAGE_HOME, observer);
		assertContains(content, "<h2>Home</h2>");
	}

}
