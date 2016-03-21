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

import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class FulltextFeedConverterTest extends ATest {

	@Test
	public void engadget() {
		String text = FulltextFeedConverter
				.downloadText("http://de.engadget.com/2016/03/21/autoblog-wie-man-in-wurde-einen-baumstamm-fahrt/");
		assertContains(text, "Manchmal muss auch sowas sein");
		assertContains(text, "Zweck versteigert.");
		assertContainsNot(text, "QUELLE");
	}

	@Test
	public void heise() {
		FeedItem item = new FeedItem("Automatische Geschwindigkeitskontrolle: Bilderkennung bremst Fahrzeuge aus",
				"In seinem neuen Van bietet Ford auf Wunsch einen intelligenten Limiter, der auf Geschwindigkeitsbegrenzungen reagiert.<img width='1' height='1' src='http://heise.de.feedsportal.com/c/35207/f/653901/s/46016565/sc/21/mf.gif' border='0'/><br clear='all'/><br/><br/><a href=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/rc/1/rc.htm\" rel=\"nofollow\"><img src=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/rc/1/rc.img\" border=\"0\"/></a><br/><a href=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/rc/2/rc.htm\" rel=\"nofollow\"><img src=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/rc/2/rc.img\" border=\"0\"/></a><br/><a href=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/rc/3/rc.htm\" rel=\"nofollow\"><img src=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/rc/3/rc.img\" border=\"0\"/></a><br/><br/><a href=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/a2.htm\"><img src=\"http://da.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/a2.img\" border=\"0\"/></a><img width=\"1\" height=\"1\" src=\"http://pi.feedsportal.com/r/224852254658/u/89/f/653901/c/35207/s/46016565/sc/21/a2t.img\" border=\"0\"/>");
		item.setLink(
			"http://www.heise.de/newsticker/meldung/Automatische-Geschwindigkeitskontrolle-Bilderkennung-bremst-Fahrzeuge-aus-2596126.html?wt_mc=rss.ho.beitrag.rdf");
		assertContainsNot(item.getDescription(), "Der Preis für das System wurde noch nicht genannt.");
		FulltextFeedConverter.replaceDescription(item);
		assertContains(item.getDescription(), "Der Preis für das System wurde noch nicht genannt.");
	}

}
