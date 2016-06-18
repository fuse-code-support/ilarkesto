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
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.ui.html;

public class Head extends Element {

	public Head() {
		super("head");
	}

	public Head title(String text) {
		add(new Element("title").add(text));
		return this;
	}

	public Head addCssRef(String href) {
		add(new Element("link").attr("rel", "stylesheet").attr("href", href));
		return this;
	}

	public Head addCssCode(String cssCode) {
		add(new Element("style").html(cssCode));
		return this;
	}

	public Head addFavicon(String href) {
		add(new Element("link").attr("rel", "shortcut icon").attr("type", "image/x-icon").attr("href", href));
		return this;
	}

}