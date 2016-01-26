/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.integration.imdb;

import ilarkesto.base.Str;
import ilarkesto.core.base.Parser;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.logging.Log;
import ilarkesto.html.dom.HtmlPage;
import ilarkesto.html.dom.HtmlParser;
import ilarkesto.html.dom.HtmlTag;
import ilarkesto.integration.httpunit.HttpUnit;
import ilarkesto.net.httpclient.HttpSession;

import java.io.File;

import org.xml.sax.SAXException;

import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

public class Imdb {

	static final String URL_AKAS = "http://akas.imdb.com";
	static final String URL_COM = "http://www.imdb.com";
	static final String URL_DE = "http://www.imdb.de";

	static final String PATH_TITLE = "/title/";
	static final String PATH_TRAILER = "/video/screenplay/";

	static final String URL_AKAS_TITLE = URL_AKAS + PATH_TITLE;
	static final String URL_AKAS_TRAILER = URL_AKAS + PATH_TRAILER;

	static final String URL_COVERS = "http://ia.media-imdb.com/images/M/";

	private static Log log = Log.get(Imdb.class);

	public static String determineIdByTitle(String title, boolean guess) {
		title = getUntil(title, "1080");
		title = getUntil(title, "780");

		log.info("Determining IMDB-ID by title:", title);
		WebResponse response = HttpUnit.loadPage(getTitleSearchUrl(title));
		String url = response.getHeaderField("LOCATION");
		if (!Str.isBlank(url) && url.startsWith(URL_AKAS_TITLE)) {
			url = Str.removePrefix(url, URL_AKAS_TITLE);
			if (url.contains("/")) url = Str.cutTo(url, "/");
			return url;
		}

		WebLink[] links;
		try {
			links = response.getLinks();
		} catch (SAXException ex) {
			throw new RuntimeException(ex);
		}
		for (WebLink link : links) {
			String linkUrl = link.getURLString();
			if (Str.isBlank(linkUrl)) continue;
			linkUrl = Str.removePrefix(linkUrl, URL_AKAS);
			if (linkUrl.startsWith(PATH_TITLE)) {
				if (guess) return extractId(linkUrl);
				String linkTitle = link.getText();
				if (title.equals(linkTitle)) return extractId(linkUrl);
			}
		}

		return null;
	}

	private static String getUntil(String s, String until) {
		int idx = s.indexOf(until);
		if (idx < 3) return s;
		return s.substring(0, idx).trim();
	}

	public static ImdbRecord loadRecord(String imdbId) {
		if (imdbId == null) return null;
		String url = getPageUrl(imdbId);
		log.info("Loading IMDB record:", imdbId);
		String html = new HttpSession().downloadText(url);
		// System.out.println("-----\n\n" + akasHtml + "\n\n-----------");
		String title;
		Integer year;
		String coverId;
		String trailerId;
		try {
			title = parseTitle(html);
			year = parseYear(html);
			coverId = parseCoverId(html);
			trailerId = parseTrailerId(html);
			// String tagline = parseInfoContent(akasPage, "Tagline");
			// String plot = parseInfoContent(akasPage, "Plot");
			// String awards = parseInfoContent(akasPage, "Awards");
		} catch (Exception ex) {
			throw new RuntimeException("Parsing IMDB page failed: " + url, ex);
		}

		String titleDe;
		try {
			titleDe = parseTitleDe(html);
		} catch (ParseException ex) {
			throw new RuntimeException("Parsing IMDB page failed: " + url, ex);
		}

		// if (titleDe != null) {
		// titleDe = Str.removePrefix(titleDe, title).trim();
		// titleDe = Str.removePrefix(titleDe, "-").trim();
		// }

		return new ImdbRecord(imdbId, title, titleDe, year, coverId, trailerId);
	}

	private static String parseTrailerId(String html) {
		String id = Str.cutFromTo(html, "href=\"" + PATH_TRAILER + "", "/");
		if (id == null) return null;
		if (id.contains("?")) return Str.cutTo(id, "?");
		return id;
	}

	private static Integer parseYear(String html) throws ParseException {
		Parser parser = new Parser(html);
		parser.gotoAfter("<meta name=\"title\" content=\"");
		String title = parser.getUntil("\"");
		if (title == null) return null;

		int idx = title.lastIndexOf(" (");
		if (idx < 1) return null;
		String s = Str.cutFromTo(title.substring(idx), "(", ")");
		if (s == null) return null;
		// s = Str.removePrefix(s, "Video");
		// s = Str.removePrefix(s, "TV");
		// s = Str.removeSuffix(s, "?");
		// s = Str.removeSuffix(s, "–");
		while (s.contains(" ") && !(s.startsWith("20") || s.startsWith("19"))) {
			s = Str.cutFrom(s, " ");
		}
		s = s.trim();
		return Integer.parseInt(s.substring(0, 4));
	}

	private static String parseTitle(String html) throws ParseException {
		Parser parser = new Parser(html);
		parser.gotoAfter("<meta property='og:title' content=\"");
		String title = parser.getUntil("\"");
		if (title == null) return null;
		title = Str.cutTo(title, " (");
		int idx = title.indexOf(" (");
		if (idx > 0) title = title.substring(0, idx).trim();
		title = Str.removePrefix(title, "IMDb -").trim();
		return title;
	}

	private static String parseTitleDe(String html) throws ParseException {
		Parser parser = new Parser(html);
		parser.gotoAfter("<meta name=\"title\" content=\"");
		String title = parser.getUntil("\"");
		if (title == null) return null;
		title = Str.cutTo(title, " (");
		int idx = title.indexOf(" (");
		if (idx > 0) title = title.substring(0, idx).trim();
		title = Str.removePrefix(title, "IMDb -").trim();
		return title;
	}

	private static String parseCoverId(String html) throws ParseException {
		HtmlPage page = new HtmlParser().parse(html);

		HtmlTag container = null; // page.getTagByStyleClass("poster");
		if (container == null) container = page.getTagById("img_primary");

		if (container == null) return null;

		HtmlTag img = container.isName("img") ? container : container.getTagByName("img");
		if (img == null) return null;

		String url = img.getAttribute("src");

		// Parser parser = new Parser(html);
		// parser.gotoAfter("id=\"img_primary\"");
		// parser.gotoAfter("<img");
		// parser.gotoAfter("src=\"");
		// String url = parser.getUntil("\"");

		// HTMLElement img;
		// try {
		// img = response.getElementWithID("primary-poster");
		// } catch (SAXException ex) {
		// throw new RuntimeException(ex);
		// }
		// if (img == null) {
		// TableCell td;
		// try {
		// td = (TableCell) response.getElementWithID("img_primary");
		// } catch (SAXException ex1) {
		// throw new RuntimeException(ex1);
		// }
		// WebImage[] images = td.getImages();
		// if (images != null && images.length > 0) img = images[0];
		// }
		//
		// if (img == null) return null;
		// String url = img.getAttribute("src");

		if (url == null) return null;
		if (!url.startsWith(URL_COVERS)) return null;
		if (!url.contains("._")) return null;
		String id = Str.removePrefix(url, URL_COVERS);
		id = id.substring(0, id.indexOf("._"));
		return id;
	}

	public static String getTitleSearchUrl(String title) {
		return URL_AKAS + "/find?s=tt&q=" + Str.encodeUrlParameter(title);
	}

	public static String getPageUrl(String imdbId) {
		return URL_AKAS_TITLE + imdbId + "/";
	}

	public static String getPageUrlDe(String imdbId) {
		return URL_AKAS_TITLE + imdbId + "/";
	}

	public static void downloadCover(String coverId, File destinationFile) {
		String url = getCoverUrl(coverId);
		log.info("Downloading IMDB cover:", url);
		new HttpSession().downloadToFile(url, destinationFile);
	}

	public static String getCoverUrl(String coverId) {
		if (coverId == null) return null;
		return URL_COVERS + coverId + "._V1._SX510_SY755_.jpg";
	}

	public static String getTrailerUrl(String trailerId) {
		if (trailerId == null) return null;
		return URL_AKAS_TRAILER + trailerId + "/";
	}

	public static String extractId(String url) {
		if (Str.isBlank(url)) return null;
		String id = url;
		id = Str.removePrefix(id, URL_AKAS);
		id = Str.removePrefix(id, URL_COM);
		id = Str.removePrefix(id, URL_DE);
		id = Str.removePrefix(id, PATH_TITLE);
		id = Str.removeSuffixStartingWith(id, "/");
		return id;
	}
}
