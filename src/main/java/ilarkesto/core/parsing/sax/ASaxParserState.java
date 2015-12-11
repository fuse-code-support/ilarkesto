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

public abstract class ASaxParserState {

	private SaxParserWithStates parser;

	protected abstract void text(String text);

	protected abstract String[] getTokens();

	protected abstract void token(String token);

	void setParser(SaxParserWithStates parser) {
		this.parser = parser;
	}

	protected final void pushState(ASaxParserState state) {
		parser.pushState(state);
	}

	protected final void popState() {
		parser.popState();
	}

	public ASaxParserState parse(String text) throws ParseException {
		new SaxParserWithStates(this).parse(text);
		return this;
	}

}
