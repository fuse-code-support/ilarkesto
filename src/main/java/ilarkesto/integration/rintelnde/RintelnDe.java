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
import ilarkesto.core.base.Pair;
import ilarkesto.core.base.Parser;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.base.Str;
import ilarkesto.core.html.Html;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Date;
import ilarkesto.integration.rintelnde.BissIndex.Lebenslage;
import ilarkesto.integration.rintelnde.BissIndex.Lebenslage.Anliegen;
import ilarkesto.integration.rintelnde.Branchenbuch.Category;
import ilarkesto.integration.rintelnde.Branchenbuch.Entry;
import ilarkesto.io.IO;
import ilarkesto.net.httpclient.HttpSession;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RintelnDe {

	private static final Log log = Log.get(RintelnDe.class);

	private static final String charset = IO.UTF_8;

	public static final String URL_BASE = "https://www.rinteln.de/";
	public static final String URL_WEBCAM_IMAGE = "http://adx-cache.de/webcam/webcam.jpg";

	public static final String PAGE_HOME = "home";
	// public static final String BISS = "buergerinformations-und-servicesystem-biss-was-erledige-ich-wo-2";
	public static final String PAGE_BISS = "app-biss";
	public static final String PAGE_BRANCHENBUCH = "branchenbuch";
	public static final String PAGE_CALENDAR = "veranstaltungskalender";
	public static final String PAGE_PRESSEMITTEILUNGEN = "pressemitteilungen-der-stadt-rinteln";

	public static HttpSession httpSession = new HttpSession();

	public static List<String> extractPageContentBoxes(String html) {
		if (html == null) return null;
		Parser parser = new Parser(html);
		List<String> ret = new LinkedList<String>();
		while (true) {
			if (!parser.gotoAfterIf("<div class=\"contentbox\">")) break;
			String boxContent = parser.getUntilIf("<div class=\"contentbox\">");
			if (boxContent == null) {
				boxContent = parser.getRemaining();
				ret.add(boxContent);
				break;
			} else {
				ret.add(boxContent);
			}
		}
		return ret;
	}

	public static String trimPageContent(String html) {
		if (html == null) return null;
		Parser parser = new Parser(html);
		if (parser.gotoAfterIf("<div data-role=\"content\">")) {
			try {
				return parseJqmPageContent(parser);
			} catch (ParseException ex) {
				log.warn("Parsing JQM page content failed", ex);
				return html;
			}
		}
		return html;
	}

	private static String parseJqmPageContent(Parser parser) throws ParseException {
		parser.skipWhitespace();

		if (parser.isNext("<h3")) parser.gotoAfter("</h3>");
		parser.skipWhitespace();

		if (parser.isNext("<p>Der folgende Eintrag wird bereitgestellt durch")) parser.gotoAfter("</p>");
		parser.skipWhitespace();

		if (parser.isNext("<img class=\"lazy\"")) parser.gotoAfter("/>");
		parser.skipWhitespace();

		if (parser.isNext("<div class=\"icons\"")) parser.gotoAfter("</div>");
		parser.skipWhitespace();

		String applemapsPrefix = "<a href=\"http://maps.apple.com/maps";
		if (parser.contains(applemapsPrefix)) return parser.getUntil(applemapsPrefix);

		return parser.getRemaining();
	}

	public static String downloadPageContent(String path, OperationObserver observer) {
		return downloadPageContent(path, 0, observer);
	}

	private static String downloadPageContent(String path, int offset, OperationObserver observer) {
		String url = getDataPageUrl(path, offset);
		log.info("Downloading", url);
		observer.onOperationInfoChanged(OperationObserver.DOWNLOADING, url);

		return httpSession.downloadText(url);
	}

	private static String getDataPageUrl(String path, int offset) {
		String url = "?appdata=1";
		if (offset > 0) url += "&start=" + offset;
		return getPageUrl(path) + url;
	}

	public static String getPageUrl(String path) {
		return URL_BASE + path;
	}

	// --- Pressemitteilungen ---

	static String downloadPressemitteilungContent(int pageId, OperationObserver observer) {
		String html = downloadPageContent("/pressemitteilungen-der-stadt-rinteln/details/" + pageId + "_", observer);
		html = trimPageContent(html);

		Parser parser = new Parser(html);
		if (parser.gotoAfterIf("<span class=\"subtitle\">")) {
			parser.gotoAfterIf("</span>");
			html = parser.getRemaining();
		}

		html = html.trim();

		html = Str.removeSuffix(html, "</div>");

		return html;
	}

	public static void downloadPressemitteilungen(Pressemitteilungen pressemitteilungen, OperationObserver observer)
			throws ParseException {
		List<Pressemitteilung> mitteilungen = downloadPressemitteilungen(observer);
		for (Pressemitteilung m : mitteilungen) {
			if (pressemitteilungen.containsMitteilung(m.getId())) continue;
			log.info("New Pressemitteilung:", m);
			m.setTextAsHtml(downloadPressemitteilungContent(m.getId(), observer));
			pressemitteilungen.addMitteilung(m);
		}
	}

	static List<Pressemitteilung> downloadPressemitteilungen(OperationObserver observer) throws ParseException {
		String s = downloadPageContent(PAGE_PRESSEMITTEILUNGEN, observer);
		List<Pressemitteilung> ret = new ArrayList<Pressemitteilung>();
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
		for (Pair<Integer, String> pair : parseApplist(s, PAGE_PRESSEMITTEILUNGEN + "/details/")) {
			String label = pair.b;
			Integer pageId = pair.a;
			int idx = s.indexOf(PAGE_PRESSEMITTEILUNGEN + "/details/" + pageId);
			idx = s.indexOf("<span>vom ", idx);
			String dateS = s.substring(idx + 10, idx + 20);
			Date date;
			try {
				date = new Date(dateFormat.parse(dateS));
			} catch (java.text.ParseException ex) {
				throw new ParseException("Invalid date: " + dateS);
			}
			ret.add(new Pressemitteilung(label, pageId, date));
		}
		return ret;
	}

	public static String getPressemitteilungPagePath(int pageId) {
		return PAGE_PRESSEMITTEILUNGEN + "/details/" + pageId + "_";
	}

	// --- Branchenbuch ---

	public static Branchenbuch downloadBranchenbuch(OperationObserver observer) throws ParseException {
		Branchenbuch branchenbuch = new Branchenbuch();

		// Oberkategorien
		List<Category> masterCategories = downloadBranchenbuchCategories(PAGE_BRANCHENBUCH, observer);
		branchenbuch.setCategories(masterCategories);

		// Kategorien
		for (Category masterCategory : masterCategories) {
			List<Category> categories = downloadBranchenbuchCategories(
				getBranchenbuchCategoryPagePath(masterCategory.getId()), observer);
			masterCategory.setCategories(categories);
		}

		// Einträge
		// for (Category category : leafCategories) {
		// List<Entry> entries = downloadBranchenbuchEntries(category.getId(), observer);
		// category.setEntries(entries);
		// }

		return branchenbuch;
	}

	public static String getBranchenbuchCategoryPagePath(int categoryId) {
		return PAGE_BRANCHENBUCH + "/category/" + categoryId + "/";
	}

	public static List<Entry> downloadBranchenbuchEntries(int categoryId, OperationObserver observer)
			throws ParseException {
		return downloadBranchenbuchEntries(categoryId, 0, observer);
	}

	private static List<Entry> downloadBranchenbuchEntries(int categoryId, int offset, OperationObserver observer)
			throws ParseException {
		String html = downloadPageContent(getBranchenbuchCategoryPagePath(categoryId), offset, observer);

		Parser parser = new Parser(html);
		List<Entry> ret = new LinkedList<Branchenbuch.Entry>();
		while (parser.gotoAfterIf("href=\"/branchenbuch/detail/")) {
			String idAsString = parser.getUntil("/");
			int id = Integer.parseInt(idAsString);
			parser.gotoAfter("<strong>");
			String label = parser.getUntil("</strong>");
			label = Html.convertHtmlToText(label);
			Entry entry = new Entry(label, id);
			ret.add(entry);
		}

		if (parser.gotoAfterIf("<a class=\"next\"")) {
			parser.gotoAfter("start=");
			String sNextOffset = parser.getUntil("\"");
			int nextOffset = Integer.parseInt(sNextOffset);
			if (nextOffset > offset) {
				List<Entry> more = downloadBranchenbuchEntries(categoryId, nextOffset, observer);
				ret.addAll(more);
			} else {
				log.warn("offset=" + offset + ", nextOffset=" + nextOffset);
			}
		}

		return ret;
	}

	private static List<Category> downloadBranchenbuchCategories(String path, OperationObserver observer)
			throws ParseException {
		String s = downloadPageContent(path, observer);
		List<Category> ret = new LinkedList<Category>();
		for (Pair<Integer, String> pair : parseApplist(s, "/branchenbuch/category/")) {
			ret.add(new Category(pair.b, pair.a));
		}
		return ret;
	}

	// --- Calendar ---

	public static final String CALENDAR_ENTRY_FIELD_LABEL = "label";
	public static final String CALENDAR_ENTRY_FIELD_IMAGE_URL = "imageUrl";
	public static final String CALENDAR_ENTRY_FIELD_DESCRIPTION = "description";

	public static Map<String, String> downloadCalendarEntryFields(Integer entryId, OperationObserver observer)
			throws ParseException {
		String path = getCalendarEntryPagePath(entryId);
		String html = downloadPageContent(path, observer);

		Map<String, String> map = new HashMap<String, String>();
		Parser parser = new Parser(html);
		parser.gotoAfter("<div data-role=\"content\">");
		String label = null;
		while (true) {
			parser.skipWhitespace();
			if (!parser.isNext("<h3")) break;
			parser.gotoAfter(">");
			String labelPart = parser.getUntilAndGotoAfter("</h3>");
			labelPart = Html.convertHtmlToText(labelPart);
			if (label == null) {
				label = labelPart;
			} else {
				label += " " + labelPart;
			}
		}
		map.put("label", label);
		if (parser.isNext("<img")) {
			parser.gotoAfter("data-original=\"");
			String image = parser.getUntil("\"");
			map.put("imageUrl", image);
		}
		if (parser.gotoAfterIf("<table class=\"details\">")) {
			while (true) {
				parser.skipWhitespace();
				if (!parser.isNext("<tr")) break;
				parser.gotoAfter("<td");
				parser.gotoAfter(">");
				String key = parser.getUntilAndGotoAfter("</td>");
				key = Html.convertHtmlToText(key);
				key = Str.removeSuffix(key, ":");
				parser.gotoAfter("<td");
				parser.gotoAfter(">");
				String value = parser.getUntil("</td>");
				value = Html.convertHtmlToText(value);
				if (key.equals("Internet")) {
					int from = value.indexOf("href=\"");
					if (from < 0) return null;
					from += 6;
					int to = value.indexOf("\"", from);
					String href = value.substring(from, to);
					if (href.startsWith("http://http://")) href = href.substring(7);
					value = href;
				}
				map.put(key, value.trim());
				parser.gotoAfter("</tr>");

			}
			parser.gotoAfter("</table>");
		}
		parser.skipWhitespace();
		String description = parser.getUntil("<a href=\"http://maps.apple.com", "</div>");
		description = description.trim();
		map.put(CALENDAR_ENTRY_FIELD_DESCRIPTION, description);
		return map;
	}

	public static String getCalendarEntryPagePath(Integer entryId) {
		return PAGE_CALENDAR + "/veranstaltung/" + entryId;
	}

	public static String getCalendarPagePath(Date date) {
		return PAGE_CALENDAR + "/veranstaltungen/0/114/" + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
	}

	public static Set<Integer> downloadCalendarEventIds(Date day, OperationObserver observer) throws ParseException {
		String path = getCalendarPagePath(day);
		String html = downloadPageContent(path, observer);
		Parser parser = new Parser(html);
		Set<Integer> ret = new HashSet<Integer>();
		while (parser.gotoAfterIf("href=\"/veranstaltungskalender/veranstaltung/")) {
			String idAsString = parser.getUntil("/");
			int id = Integer.parseInt(idAsString);
			ret.add(id);
		}
		return ret;
	}

	// --- BISS ---

	static List<Lebenslage> downloadBissLebenslages(OperationObserver observer) throws ParseException {
		String s = downloadPageContent(PAGE_BISS, observer);
		List<Lebenslage> ret = new LinkedList<Lebenslage>();
		for (Pair<Integer, String> pair : parseApplist(s, "/app-biss/lebenslage/")) {
			ret.add(new Lebenslage(pair.b, pair.a));
		}
		return ret;
	}

	static List<Anliegen> downloadBissAnliegens(int i, OperationObserver observer) throws ParseException {
		String s = downloadPageContent(PAGE_BISS + "/lebenslage/" + i, observer);
		List<Anliegen> ret = new LinkedList<Anliegen>();
		for (Pair<Integer, String> pair : parseApplist(s, "/app-biss/anliegen/")) {
			ret.add(new Anliegen(pair.b, pair.a));
		}
		return ret;
	}

	private static List<Pair<Integer, String>> parseApplist(String data, String path) throws ParseException {
		Parser p = new Parser(data);
		p.gotoAfter("<ul class=\"applist\">");
		List<Pair<Integer, String>> ret = new LinkedList<Pair<Integer, String>>();
		while (true) {
			p.skipWhitespace();
			if (!p.isNext("<li")) break;
			p.gotoAfter(path);
			String sId = p.getUntil("_", "/");
			Integer id = Integer.parseInt(sId);
			p.gotoAfter("<span");
			p.gotoAfter(">");
			String htmlLabel = p.getUntil("</span>");
			String label = Html.convertHtmlToText(htmlLabel);
			p.gotoAfter("</li>");
			Pair<Integer, String> pair = new Pair<Integer, String>(id, label);
			ret.add(pair);
		}
		return ret;
	}

	public static BissIndex downloadBissIndex(OperationObserver observer) throws ParseException {
		List<Lebenslage> lebenslages = downloadBissLebenslages(observer);
		for (Lebenslage lebenslage : lebenslages) {
			List<Anliegen> anliegens = downloadBissAnliegens(lebenslage.getId(), observer);
			lebenslage.setAnliegens(anliegens);
		}
		BissIndex index = new BissIndex();
		index.setLebenslages(lebenslages);
		return index;
	}

}
