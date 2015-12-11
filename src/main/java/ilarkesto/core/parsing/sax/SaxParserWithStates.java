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

import java.util.Stack;

public class SaxParserWithStates extends ASaxParser {

	private Stack<ASaxParserState> stateStack = new Stack<ASaxParserState>();

	public SaxParserWithStates(ASaxParserState rootState) {
		rootState.setParser(this);
		stateStack.push(rootState);
	}

	@Override
	protected void token(String token) {
		stateStack.peek().token(token);
	}

	@Override
	protected String[] getTokens() {
		return stateStack.peek().getTokens();
	}

	@Override
	protected void text(String text) {
		stateStack.peek().text(text);
	}

	public void pushState(ASaxParserState state) {
		state.setParser(this);
		stateStack.push(state);
	}

	protected final void popState() {
		if (stateStack.size() <= 1) return; // TODO exception?
		stateStack.pop();
	}

}
