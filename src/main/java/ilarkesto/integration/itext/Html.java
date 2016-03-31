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
package ilarkesto.integration.itext;

import ilarkesto.core.base.Utl;
import ilarkesto.pdf.AHtml;
import ilarkesto.pdf.APdfBuilder;
import ilarkesto.pdf.APdfElement;
import ilarkesto.pdf.FontStyle;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;

public class Html extends AHtml implements ItextElement {

	public Html(APdfElement parent, FontStyle fontStyle) {
		super(parent, fontStyle);
	}

	@Override
	public Element[] createITextElements(Document document) {

		String cssClassName = "itextpdfelement";
		code = "<div class='" + cssClassName + "'>" + code + "</div>";
		StyleSheet css = new StyleSheet();
		css.loadStyle(cssClassName, "font-family", fontStyle.getFont());
		css.loadStyle(cssClassName, "color", fontStyle.getColor().toString());
		css.loadStyle(cssClassName, "font-size", APdfBuilder.mmToPoints(fontStyle.getSize()) + "pt");
		css.loadStyle(cssClassName, "size", APdfBuilder.mmToPoints(fontStyle.getSize()) + "pt");
		css.loadStyle(cssClassName, "line-height", "100em");

		StringReader reader = new StringReader(cleanup(code));
		List<Element> elements;
		HTMLWorker worker = new HTMLWorker(document);
		try {
			elements = worker.parseToList(reader, css);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		return Utl.toArray(elements, new Element[elements.size()]);
	}

	private static String cleanup(String code) {
		if (code == null) return null;
		code = code.replace("font-size: inherit;", "");
		return code;
	}
}
