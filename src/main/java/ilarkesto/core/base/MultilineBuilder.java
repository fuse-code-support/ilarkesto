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

public class MultilineBuilder {

	private StringBuilder sb = new StringBuilder();
	private String linePrefix;

	public MultilineBuilder lnIfAllNotNull(Object... words) {
		for (Object o : words) {
			if (o == null) return this;
		}
		return ln(words);
	}

	public MultilineBuilder ln(Object... words) {
		boolean added = false;
		boolean first = true;
		for (Object o : words) {
			if (o == null) continue;
			String word = o instanceof Throwable ? Str.formatException((Throwable) o) : Str.format(o);
			if (Str.isBlank(word)) continue;
			if (first) {
				first = false;
				if (linePrefix != null) sb.append(linePrefix);
			} else {
				sb.append(" ");
			}
			sb.append(word);
			added = true;
		}
		if (added) {
			sb.append("\n");
		}
		return this;
	}

	public boolean isEmpty() {
		return Str.isBlank(sb.toString());
	}

	public MultilineBuilder setLinePrefix(String linePrefix) {
		this.linePrefix = linePrefix;
		return this;
	}

	public String getLinePrefix() {
		return linePrefix;
	}

	public String toStringOrNull() {
		if (isEmpty()) return null;
		return toString();
	}

	@Override
	public String toString() {
		return sb.toString().trim();
	}
}
