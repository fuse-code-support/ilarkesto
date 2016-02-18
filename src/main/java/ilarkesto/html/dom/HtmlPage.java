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
package ilarkesto.html.dom;

import ilarkesto.core.base.Str;

import java.util.ArrayList;

public class HtmlPage extends HtmlTag {

	public HtmlPage() {
		super(null, "doctype", null, false);
		contents = new ArrayList<AHtmlData>(1);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (AHtmlData data : contents) {
			sb.append(data.toString());
		}
		return sb.toString();
	}

	public HtmlTag getBody() {
		return getTagByName("body");
	}

	public HtmlTag getHead() {
		return getTagByName("head");
	}

	public String getHeadTitle() {
		HtmlTag head = getHead();
		if (head == null) return null;
		return Str.trimAndNull(head.getText());
	}

	public HtmlTag getBodyOrRoot() {
		HtmlTag body = getBody();
		if (body != null) return body;
		for (AHtmlData data : contents) {
			if (data instanceof HtmlTag) return (HtmlTag) data;
		}
		return null;
	}
}
