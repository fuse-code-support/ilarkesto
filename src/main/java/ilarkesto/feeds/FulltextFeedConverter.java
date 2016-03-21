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
package ilarkesto.feeds;

import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;
import ilarkesto.net.HttpDownloader;

import java.util.HashSet;
import java.util.Set;

public class FulltextFeedConverter {

	private static final Log log = Log.get(FulltextFeedConverter.class);

	public static void main(String[] args) {
		// FulltextFeedConverter converter = new
		// FulltextFeedConverter("http://www.heise.de/newsticker/heise.rdf");
		// converter.update();
		// System.out.println(converter.feed);
		// for (FeedItem item : converter.feed.getItems()) {
		// System.out.println(" * " + item);
		// System.out.println(" " + item.getDescription());
		// }
		// System.out.println(converter.feed.createRssText());

		log.info("\n\n >>>",
			downloadText("http://www.inside-handy.de/tipps/36598-amazon-blitzpreise-samsung-galaxy-s6-zum-sparpreis"));

	}

	private String url;
	private Feed feed;
	private Set<FeedItem> updatedItems = new HashSet<FeedItem>();

	public static HttpDownloader downloader = HttpDownloader.create();

	public FulltextFeedConverter(String url) {
		super();
		this.url = url;
	}

	public synchronized void update() {
		feed = Feed.load(url);
		log.info("Feed loaded:", feed);
		for (FeedItem item : feed.getItems()) {
			if (updatedItems.contains(item)) continue;
			try {
				replaceDescription(item);
			} catch (Exception ex) {
				log.warn("Replacing feed item description failed:", item.getLink(), ex);
				continue;
			}
			updatedItems.add(item);
		}
	}

	static void replaceDescription(FeedItem item) {
		String link = item.getLink();
		if (Str.isBlank(link)) return;

		replaceDescription(item, link);
	}

	private static void replaceDescription(FeedItem item, String link) {
		String text = downloadText(link);
		if (link.startsWith("http://www.zeit.de") && !link.contains("/komplettansicht")
				&& text.contains("Auf einer Seite lesen")) {
			replaceDescription(item, link + "/komplettansicht");
			return;
		}
		if (Str.isBlank(text)) return;
		item.setDescription(text);
	}

	static String downloadText(String url) {
		log.info("Downloading", url);
		String text = downloader.downloadText(url, IO.UTF_8);
		if (Str.isBlank(text)) return null;
		text = extract(text, url);
		text = optimize(text);
		return text;
	}

	private static String optimize(String text) {
		for (int i = 1; i < 6; i++) {
			int iDest = i + 3;
			text = text.replace("<h" + i, "<h" + iDest).replace("</h" + i, "</h" + iDest);
			text = text.replace("<H" + i, "<H" + iDest).replace("</H" + i, "</H" + iDest);
		}

		text = text.replace("type=\"text/javascript\"", "type=\"text/unsupported\"");

		return text;
	}

	private static String extract(String text, String url) {
		if (text == null) return null;

		int idx = -1;

		if ((idx = text.indexOf("<div class=\"meldung_wrapper\">")) > 0) {
			log.debug("heise.de");
			text = text.substring(idx + 29);
			text = Str.removeSuffixStartingWith(text, "</div>");
			return text;
		}

		if ((idx = text.indexOf("<div class=\"article-body")) > 0) {
			log.debug("zeit.de");
			String nextPageUrl = extractZeitDeNextPageUrl(text);
			text = text.substring(idx);
			text = Str.removeSuffixStartingWith(text, "<a href=\"http://www.zeit.de\"");
			text = Str.removeSuffixStartingWith(text, "<div class=\"articlefooter af\">");
			text = Str.removeSuffixStartingWith(text, "<div class=\"article-pagination");
			if (nextPageUrl != null) {
				String next = downloader.downloadText(nextPageUrl, IO.UTF_8);
				if (!Str.isBlank(next)) return text;
				text += extract(next, nextPageUrl);
			}
			return text;
		}

		if ((idx = text.indexOf("<div class=\"entry\"")) > 0) {
			log.debug("<div class=\"entry\">"); // abgeordnetenwatch
			text = text.substring(idx);
			text = Str.removeSuffixStartingWith(text, "</div>");
			return text;
		}

		if ((idx = text.indexOf("<div class=\"o-article_block")) > 0) {
			log.debug("class=article-content"); // engadget.com 2016-03-21
			text = text.substring(idx);
			text = Str.removeSuffixStartingWith(text, "<footer");
			return text;
		}
		if ((idx = text.indexOf("<div class=\"article-content\"")) > 0) {
			log.debug("class=article-content"); // engadget.com
			text = text.substring(idx);
			text = Str.removeSuffixStartingWith(text, "<div class=\"post-meta\"");
			return text;
		}

		if ((idx = text.indexOf("<article")) > 0) {
			log.debug("<article>"); // golem
			text = text.substring(idx);
			text = Str.removeSuffixStartingWith(text, "</article>");
			return text;
		}

		if ((idx = text.indexOf("<div vocab=\"http://schema.org/\" typeof=\"NewsArticle\">")) > 0) {
			log.debug("div typeof=\"NewsArticle\""); // inside-handy
			text = text.substring(idx);
			text = Str.removeSuffixStartingWith(text, "<div class=\"share-container social_bar\"");
			return text;
		}

		if ((idx = text.indexOf("<div class=\"copy post-body\" itemprop=\"articleBody\">")) > 0) {
			log.debug("<div class=\"copy post-body\" itemprop=\"articleBody\">"); // engadget.com
			text = text.substring(idx);
			text = Str.removeSuffixStartingWith(text, "<p class=\"read-more\"");
			return text;
		}

		log.warn("Identification failed:", url, "\n-----\n" + text);

		if ((idx = text.indexOf("<body>")) > 0) {
			text = text.substring(idx + 6);
			text = Str.removeSuffixStartingWith(text, "</body>");
			return text;
		}

		return text;
	}

	private static String extractZeitDeNextPageUrl(String text) {
		int idx = text.indexOf("<a id=\"hp.article.bottom.paginierung.");
		if (idx < 0) return null;
		idx = text.indexOf("href=\"", idx);
		if (idx < 0) return null;
		idx += 6;
		return text.substring(idx, text.indexOf("\"", idx + 1));
	}

	public Feed getFeed() {
		return feed;
	}
}
