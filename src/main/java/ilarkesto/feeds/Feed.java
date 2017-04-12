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

import ilarkesto.base.Str;
import ilarkesto.base.Tm;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.integration.jdom.JDom;
import ilarkesto.io.IO;
import ilarkesto.io.StringOutputStream;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class Feed {

	private String title;
	private String link;
	private String description;
	private String language;
	private String pubDate; // RFC 822
	private String lastBuildDate;
	private List<FeedItem> items = new ArrayList<FeedItem>();

	public static Feed load(String url) {
		Document document = JDom.createDocumentFromUrl(url);
		Element eRoot = document.getRootElement();
		Element eChannel = JDom.getChild(eRoot, "channel");
		if (eChannel == null) eChannel = eRoot;

		String title = JDom.getChildText(eChannel, "title");
		String link = JDom.getChildText(eChannel, "link");
		if (Str.isBlank(link)) link = JDom.getChildAttributeValue(eChannel, "link", "href");
		String description = JDom.getChildText(eChannel, "description");
		if (description == null) description = JDom.getChildText(eChannel, "subtitle");
		Feed feed = new Feed(title, link, description);
		feed.setLastBuildDate(JDom.getChildText(eChannel, "lastBuildDate"));
		feed.setPubDate(JDom.getChildText(eChannel, "lastBuildDate"));
		feed.setLanguage(JDom.getChildText(eChannel, "language"));

		List<Element> eItems = JDom.getChildren(eChannel, "item");
		if (eItems.isEmpty()) eItems = JDom.getChildren(eChannel, "entry");
		for (Element eItem : eItems) {
			String itemTitle = JDom.getChildText(eItem, "title");
			String itemDescription = JDom.getChildText(eItem, "description");
			if (itemDescription == null) itemDescription = JDom.getChildText(eItem, "summary");
			FeedItem item = new FeedItem(itemTitle, itemDescription);
			feed.addItem(item);
			String itemLink = JDom.getChildText(eItem, "link");
			if (Str.isBlank(itemLink)) itemLink = JDom.getChildAttributeValue(eItem, "link", "href");
			item.setLink(itemLink);
			String itemPubDate = JDom.getChildText(eItem, "pubDate");
			if (itemPubDate == null) itemPubDate = JDom.getChildText(eItem, "published");
			item.setPubDate(itemPubDate);
			item.setGuid(JDom.getChildText(eItem, "guid"));
		}

		return feed;
	}

	public Feed(String title, String link, String description) {
		super();
		this.title = title;
		this.link = link;
		this.description = description;
	}

	public Feed sortItems() {
		Collections.sort(items);
		return this;
	}

	public Document createRssJDom() {
		Document document = new Document();
		Element eRoot = new Element("rss");
		eRoot.setAttribute("version", "2.0");
		document.setRootElement(eRoot);

		Element eChannel = JDom.addElement(eRoot, "channel");
		JDom.addTextElement(eChannel, "title", title);
		JDom.addTextElement(eChannel, "link", link);
		JDom.addTextElement(eChannel, "description", description);
		if (language != null) JDom.addTextElement(eChannel, "language", language);
		if (pubDate != null) JDom.addTextElement(eChannel, "pubDate", pubDate);
		if (lastBuildDate != null) JDom.addTextElement(eChannel, "lastBuildDate", lastBuildDate);

		for (FeedItem item : items) {
			item.appendTo(eChannel);
		}

		return document;
	}

	public void writeRss(OutputStream out, String encoding) {
		JDom.write(createRssJDom(), out, encoding);
	}

	public String createRssText() {
		StringOutputStream out = new StringOutputStream();
		writeRss(out);
		return out.toString();
	}

	public void writeRss(OutputStream out) {
		JDom.write(createRssJDom(), out, IO.UTF_8);
	}

	public void addItem(FeedItem item) {
		items.add(item);
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

	public String getLanguage() {
		return language;
	}

	public Feed setLanguage(String language) {
		this.language = language;
		return this;
	}

	public List<FeedItem> getItems() {
		return items;
	}

	public Feed setPubDate(String pubDate) {
		this.pubDate = pubDate;
		return this;
	}

	public String getPubDate() {
		return pubDate;
	}

	public Feed setPubDate(DateAndTime pubDate) {
		return setPubDate(pubDate == null ? null : Tm.FORMAT_RFC822.format(pubDate));
	}

	public String getLastBuildDate() {
		return lastBuildDate;
	}

	public Feed setLastBuildDate(String lastBuildDate) {
		this.lastBuildDate = lastBuildDate;
		return this;
	}

	@Override
	public String toString() {
		return title;
	}

}
