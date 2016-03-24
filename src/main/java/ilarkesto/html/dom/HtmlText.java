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

import ilarkesto.core.base.Args;

public class HtmlText extends AHtmlData {

	private HtmlTag parent;
	private String text;

	public HtmlText(HtmlTag parent, String text) {
		Args.assertNotNull(parent, "parent");
		this.parent = parent;
		this.text = text;
	}

	@Override
	public String getText() {
		return text == null ? null : text;
	}

	public HtmlTag getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return text;
	}

}
