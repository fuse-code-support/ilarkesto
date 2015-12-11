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
package ilarkesto.templating;

import ilarkesto.core.parsing.sax.ASaxParserState;
import ilarkesto.core.parsing.sax.ParseException;

import java.io.File;

public class MustacheLikeTemplateParser extends ATemplateParser {

	@Override
	protected String[] getTokens() {
		return new String[] { "{{{", "{{" };
	}

	@Override
	protected void token(String token) {
		pushState(new MustacheState(token.length() == 3));
	}

	@Override
	protected void text(String text) {
		builder.text(text);
	}

	public static Template parseTemplate(String template) throws ParseException {
		return new MustacheLikeTemplateParser().parse(template).getTemplate();
	}

	public static Template parseTemplate(File templateFile) throws ParseException {
		return new MustacheLikeTemplateParser().parse(templateFile).getTemplate();
	}

	class MustacheState extends ASaxParserState {

		private boolean triple;

		public MustacheState(boolean triple) {
			super();
			this.triple = triple;
		}

		@Override
		protected String[] getTokens() {
			return new String[] { triple ? "}}}" : "}}" };
		}

		@Override
		protected void token(String token) {
			popState();
		}

		@Override
		protected void text(String text) {
			if (text.equals("/")) {
				builder.endContainer();
				return;
			}
			if (text.startsWith(">>")) {
				builder.startComponentInclude(text.substring(2));
				return;
			}
			if (text.startsWith(">")) {
				builder.include(text.substring(1));
				return;
			}
			if (text.startsWith("<<")) {
				builder.startInlineTemplate(text.substring(2));
				return;
			}
			if (text.startsWith("#")) {
				builder.startLoop(text.substring(1));
				return;
			}
			builder.variable(text).setEscape(!triple);
		}
	}

}
