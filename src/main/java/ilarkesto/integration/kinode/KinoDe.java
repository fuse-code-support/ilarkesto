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
package ilarkesto.integration.kinode;

import ilarkesto.core.base.OperationObserver;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.Time;
import ilarkesto.html.dom.HtmlPage;
import ilarkesto.html.dom.HtmlParser;
import ilarkesto.html.dom.HtmlTag;
import ilarkesto.net.httpclient.HttpSession;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KinoDe {

	private static final Log log = Log.get(KinoDe.class);

	private static final String URL_BASE = "http://www.kino.de";
	private static final String URL_BASE_PROGRAMM = URL_BASE + "/kinoprogramm/";
	private static final String URL_BASE_FILM = URL_BASE + "/film/";

	public static final String CINEMA_ID_RINTELN = "rinteln/kinocenter-rinteln-k13450";
	public static final String CINEMA_ID_BUECKEBURG = "bueckeburg/residenz-kino-center-k13531";

	private static HttpSession http = new HttpSession();

	private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");

	private static final Map<String, Movie> movies = new HashMap<String, Movie>();

	public static String getMovieUrl(String movieId) {
		return URL_BASE_FILM + movieId;
	}

	static Movie loadMovie(String movieId, OperationObserver observer) {
		Movie movie = movies.get(movieId);
		if (movie != null) return movie;

		String url = getMovieUrl(movieId);
		if (observer != null) observer.onOperationInfoChanged(OperationObserver.DOWNLOADING, url);
		String html = http.downloadText(url);
		HtmlPage page;
		try {
			page = new HtmlParser().parse(html);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}

		String posterUrl = null;
		HtmlTag tPoster = page.getTagByStyleClass("article-poster");
		if (tPoster != null) {
			HtmlTag tImg = tPoster.getTagByName("img");
			if (tImg != null) {
				posterUrl = tImg.getAttribute("src");
			}
		}

		String title = movieId;
		for (HtmlTag tMeta : page.getTagsByName("meta", true)) {
			if (tMeta.isAttribute("property", "og:title")) {
				title = tMeta.getAttribute("content");
				break;
			}
		}

		movie = new Movie(movieId, title, posterUrl);
		movies.put(movieId, movie);
		return movie;
	}

	public static void loadShows(String cinemaId, MovieShowConsumer callback, OperationObserver observer) {
		String url = URL_BASE_PROGRAMM + cinemaId;
		if (observer != null) observer.onOperationInfoChanged(OperationObserver.DOWNLOADING, url);
		String html = http.downloadText(url);
		HtmlPage page;
		try {
			page = new HtmlParser().parse(html);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
		HtmlTag outer = page.getTagByStyleClass("programScheduleOuter");
		if (outer == null) {
			log.info("Missing tag with class 'programScheduleOuter' for " + cinemaId);
			return;
		}
		processShows(outer.getTagByNameAndStyleClass("div", "programSchedule"), cinemaId, callback, observer);

		HtmlTag tScript = outer.getTagByName("script");
		String script = tScript.getContentAsText();

		processScript(script, cinemaId, callback, observer);
	}

	private static void processScript(String script, String cinemaId, MovieShowConsumer callback,
			OperationObserver observer) {
		for (int i = 2; i <= 7; i++) {
			int startIdx = script.indexOf(".day" + i + ".schedule = '");
			if (startIdx < 0) return;
			startIdx += 18;
			int endIdx = script.indexOf("';", startIdx);
			String html = script.substring(startIdx, endIdx);
			if (Str.isBlank(html)) return;

			HtmlPage page;
			try {
				page = new HtmlParser().parse(html);
			} catch (ParseException ex) {
				throw new RuntimeException(ex);
			}
			processShows(page, cinemaId, callback, observer);
		}
	}

	private static void processShows(HtmlTag tag, String cinemaId, MovieShowConsumer callback,
			OperationObserver observer) {
		// log.TEST("schedule:", tag.toFormatedString());

		String id = null;
		String title = null;

		for (HtmlTag tLi : tag.getTagsByName("li", true)) {
			List<HtmlTag> tTimes = tLi.getTagsByName("time", true);
			if (!tTimes.isEmpty()) {
				Date date = null;
				List<Time> times = new ArrayList<Time>();
				for (HtmlTag tTime : tTimes) {
					String sTime = tTime.getAttribute("datetime");
					DateAndTime dateAndTime;
					try {
						dateAndTime = new DateAndTime(dateFormat.parse(sTime));
					} catch (java.text.ParseException ex) {
						throw new RuntimeException(ex);
					}
					log.info("  -", dateAndTime);
					date = dateAndTime.getDate();
					times.add(dateAndTime.getTime());
				}
				if (id == null) continue;
				log.info("*", title, "->", id);
				Movie movie = loadMovie(id, observer);
				callback.onMovieShow(cinemaId, date, times, movie.id, movie.title, movie.description, movie.posterUrl);
				continue;
			}

			HtmlTag tA = tLi.getTagByName("a");
			if (tA == null) {
				id = Str.cutFromTo(tLi.getContentAsText(), "film/", "/");
				title = tLi.getContentAsText();
				title = title.substring(title.indexOf(">") + 1);
			} else {
				id = Str.cutFromTo(tA.getAttribute("href"), "film/", "/");
				title = tA.getContentAsText();
			}
			title = title.replace("\\'", "'");
		}

	}

	static class Movie {

		public String id;
		public String title;
		public String description;
		public String posterUrl;

		public Movie(String id, String title, String posterUrl) {
			super();
			this.id = id;
			this.title = title;
			this.posterUrl = posterUrl;
		}

	}

}
