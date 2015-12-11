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
package ilarkesto.integration.jura;

import ilarkesto.core.base.Str;
import ilarkesto.core.parsing.sax.ParseException;

public class JuraNormReference {

	private String book;
	private String norm;
	private String sections;

	public JuraNormReference(String book, String norm, String sections) {
		super();
		this.book = book;
		this.norm = norm;
		this.sections = sections;
	}

	public JuraNormReference(String book, String norm) {
		this(book, norm, null);
	}

	public static JuraNormReference parse(String s) throws ParseException {
		if (Str.isBlank(s)) return null;

		String orig = s;

		s = s.replace("Art.", "§");

		s = s.trim();
		if (s.contains("§")) s = s.substring(s.indexOf('§') + 1).trim();
		s = Str.removePrefix(s, "§");
		s = s.trim();

		int spaceIdx = Str.indexOf(s, new String[] { " ", "-" }, 0);
		if (spaceIdx < 0) throw new ParseException("Illegal string:", orig);
		String norm = s.substring(0, spaceIdx);
		norm = Str.removeSuffix(norm, ",").trim();
		s = s.substring(spaceIdx).trim();

		String book;
		String sections;
		spaceIdx = s.lastIndexOf(' ');
		if (spaceIdx < 0) {
			book = s;
			sections = null;
		} else {
			book = s.substring(spaceIdx).trim();
			sections = s.substring(0, spaceIdx).trim();
		}

		JuraNormReference ref = new JuraNormReference(book, norm, sections);
		return ref;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("§ ").append(norm);
		if (sections != null) sb.append(" ").append(sections);
		sb.append(" ").append(book);
		return sb.toString();
	}

	public void setSections(String sections) {
		this.sections = sections;
	}

	public String getBook() {
		return book;
	}

	public String getNorm() {
		return norm;
	}

	public String getSections() {
		return sections;
	}

}
