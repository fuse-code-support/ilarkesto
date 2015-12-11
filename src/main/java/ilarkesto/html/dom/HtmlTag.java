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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HtmlTag extends AHtmlData implements HtmlDataContainer {

	private HtmlTag parent;
	private String name;
	private Map<String, String> attributes;
	private boolean closed;
	private List<AHtmlData> contents;

	public HtmlTag(HtmlTag parent, String name, Map<String, String> attributes, boolean closed) {
		super();
		this.parent = parent;
		this.name = name.toLowerCase();
		this.attributes = attributes;
		this.closed = closed;
	}

	@Override
	public void add(AHtmlData data) {
		if (closed) throw new IllegalStateException();
		if (contents == null) contents = new ArrayList<AHtmlData>();
		contents.add(data);
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isShort() {
		if (isClosed()) return true;
		if (name.equals("img")) return true;
		if (name.equals("br")) return true;
		if (name.equals("hr")) return true;
		if (name.equals("meta")) return true;
		return false;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public List<AHtmlData> getContents() {
		return contents;
	}

	public HtmlTag getParent() {
		return parent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(name);
		if (closed) {
			sb.append("/>");
			return sb.toString();
		}
		sb.append(">");

		if (contents != null) {
			for (AHtmlData data : contents) {
				sb.append(data.toString());
			}
		}

		sb.append("</" + name + ">");

		return sb.toString();
	}

}
