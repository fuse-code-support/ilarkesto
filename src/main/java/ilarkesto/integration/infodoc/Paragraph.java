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
package ilarkesto.integration.infodoc;

import ilarkesto.core.base.Str;
import ilarkesto.json.JsonObject;

public class Paragraph extends AInfoDocElement {

	private String text;

	public Paragraph(InfoDocStructure structure, String text) {
		super(structure);
		this.text = text;
	}

	@Override
	public String toHtml(AHtmlContext context, AReferenceResolver referenceResolver) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n<p style='" + context.getElementDepthStyle(getDepth()) + " color:" + context.getColor(getDepth())
				+ ";'>").append(context.getIndentationPrefix(this))
				.append(context.getIndentationPrefix(this) + Str.toHtml(text, true)).append("</p>\n");
		return sb.toString();
	}

	@Override
	public JsonObject toJson(AReferenceResolver referenceResolver) {
		JsonObject ret = new JsonObject();
		ret.put("type", "paragraph");
		ret.put("depth", getDepth());
		ret.put("text", text);
		return ret;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return getText();
	}

}
