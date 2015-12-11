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
package ilarkesto.core.parsing.sax;

import ilarkesto.core.base.Parser;

public abstract class ASaxParser {

	private Parser parser;

	protected abstract String[] getTokens();

	protected abstract void text(String text);

	protected abstract void token(String token);

	public final ASaxParser parse(String text) throws ParseException {
		if (text == null) return this;
		parser = new Parser(text);
		while (!parser.isEnd()) {
			parseNext();
		}
		return this;
	}

	private void parseNext() {
		String[] tokens = getTokens();
		String text = parser.getUntilIf(tokens);
		if (text == null) {
			text(parser.getRemaining());
			parser.gotoEnd();
			return;
		}
		if (text.length() > 0) {
			text(text);
			try {
				parser.gotoAfter(text);
			} catch (ilarkesto.core.base.Parser.ParseException ex) {
				throw new IllegalStateException("Internal parser error: expected text missing");
			}
		}
		for (String token : tokens) {
			if (!parser.isNext(token)) continue;
			token(token);
			try {
				parser.gotoAfter(token);
			} catch (ilarkesto.core.base.Parser.ParseException ex) {
				throw new IllegalStateException("Internal parser error: expected token missing");
			}
			return;
		}
		throw new IllegalStateException("Internal parser error: identified token not found");
	}

}
