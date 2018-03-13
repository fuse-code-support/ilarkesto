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

import java.util.ArrayList;
import java.util.List;

public class InfoDocStructure {

	private List<AInfoDocElement> elements = new ArrayList<AInfoDocElement>();
	private Header lastHeader;

	public boolean isPrefixingRequired() {
		for (AInfoDocElement element : elements) {
			if (element.getDepth() > 0) return true;
		}
		return false;
	}

	public int getIndexInDepth(AInfoDocElement element) {
		int i = elements.indexOf(element);
		if (i <= 0) return 0;

		int depth = element.getDepth();

		int index = 0;
		while (i > 0) {
			i--;
			AInfoDocElement e = elements.get(i);
			if (!e.isPrefixed()) continue;
			int eDepth = e.getDepth();
			if (eDepth > depth) continue;
			if (eDepth < depth) break;
			index++;
		}
		return index;
	}

	private void add(AInfoDocElement element) {
		if (element == null) return;
		elements.add(element);
		element.setHeader(lastHeader);
		if (element instanceof Header) {
			Header header = (Header) element;
			lastHeader = header;
		}
	}

	public Object toJsonObject(AReferenceResolver referenceResolver) {
		List<JsonObject> ret = new ArrayList<JsonObject>();
		for (AInfoDocElement element : elements) {
			ret.add(element.toJson(referenceResolver));
		}
		return ret;
	}

	public String toHtml(AHtmlContext context, AReferenceResolver resolver) {
		StringBuilder sb = new StringBuilder();
		for (AInfoDocElement element : elements) {
			sb.append(element.toHtml(context, resolver));
		}
		return sb.toString();
	}

	public static InfoDocStructure parse(String text) {
		if (text == null) return null;

		InfoDocStructure ret = new InfoDocStructure();

		text = text.replace("\r", "");
		text = text.trim();

		while (!text.isEmpty()) {
			int idx = text.indexOf("\n\n");
			if (idx > 0) {
				String element = text.substring(0, idx);
				text = text.substring(idx + 2).trim();
				ret.add(parseElement(ret, element));
			} else {
				ret.add(parseElement(ret, text));
				text = "";
			}
		}

		ret.add(parseElement(ret, text));

		return ret;
	}

	private static AInfoDocElement parseElement(InfoDocStructure strucutre, String text) {
		if (Str.isBlank(text)) return null;
		if (text.startsWith("# ")) return new Comment(strucutre, text.substring(2).trim());
		if (text.startsWith("@")) {
			text = text.substring(1).trim();
			String alternativeTitle = null;
			int idx = text.indexOf('/');
			if (idx > 0) {
				alternativeTitle = text.substring(idx + 1);
				alternativeTitle = Str.removeSuffixStartingWith(alternativeTitle, "/").trim();
				text = text.substring(0, idx).trim();
			}
			return new Reference(strucutre, text, alternativeTitle);
		}

		int headerDepth = getHeaderDepth(text);
		if (headerDepth >= 0) return new Header(strucutre, text.substring(text.indexOf(' ')).trim(), headerDepth);

		return new Paragraph(strucutre, text);
	}

	private static int getHeaderDepth(String s) {
		s = s.trim();
		s = Str.cutTo(s, " ");
		if (s == null) return -1;

		if (s.startsWith("(P)")) return -1;

		if (s.equals("!")) return 0;
		if (s.equals("!!")) return 1;
		if (s.equals("!!!")) return 2;
		if (s.equals("!!!!")) return 3;
		if (s.equals("!!!!!")) return 4;
		if (s.equals("!!!!!!")) return 5;

		if (s.length() < 2) return -1;

		if (s.endsWith(".")) {
			s = s.substring(0, s.length() - 1);
			if (Str.isAllDigits(s)) return 2;
			if (Str.isAllUppercase(s)) return 1;
			return -1;
		}

		if (s.endsWith(")")) {
			if (s.startsWith("(") && Str.isAllDigits(s.substring(1, s.length() - 1))) return 5;

			if (Character.isUpperCase(s.charAt(0))) return 0;

			if (s.indexOf(')') >= 2) return 4;
			return 3;
		}

		return -1;
	}

	public List<AInfoDocElement> getElements() {
		return elements;
	}

}
