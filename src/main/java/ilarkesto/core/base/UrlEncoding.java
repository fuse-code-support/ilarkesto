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
package ilarkesto.core.base;

public class UrlEncoding {

	public static final String encode(String s) {
		if (s == null) return "";
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			sb.append(encode(s.charAt(i)));
		}
		return sb.toString();
	}

	private static String encode(char c) {
		switch (c) {
			case ' ':
				return "%20";
			case '!':
				return "%21";
			case '"':
				return "%22";
			case '#':
				return "%23";
			case '$':
				return "%24";
			case '%':
				return "%25";
			case '&':
				return "%26";
			case '\'':
				return "%27";
			case '(':
				return "%28";
			case ')':
				return "%29";
			case '*':
				return "%2A";
			case '+':
				return "%2B";
			case ',':
				return "%2C";
			case '/':
				return "%2F";
			case ':':
				return "%3A";
			case ';':
				return "%3B";
			case '=':
				return "%3D";
			case '?':
				return "%3F";
			case '@':
				return "%40";
			case '[':
				return "%5B";
			case '\\':
				return "%5C";
			case ']':
				return "%5D";
			case '{':
				return "%7B";
			case '|':
				return "%7C";
			case '}':
				return "%7D";
			default:
				return String.valueOf(c);
		}
	}

}
