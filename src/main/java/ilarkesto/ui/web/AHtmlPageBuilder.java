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
package ilarkesto.ui.web;

public abstract class AHtmlPageBuilder {

	protected abstract void bodyContent(HtmlBuilder html);

	protected abstract void headerContent(HtmlBuilder html);

	protected abstract String getTitle();

	protected abstract String getLanguage();

	public HtmlBuilder build(HtmlBuilder html) {
		String lang = getLanguage();
		html.startHTML(lang);
		html.startHEAD(getTitle());
		headerContent(html);
		html.endHEAD();
		html.startBODY(getBodyId());
		bodyContent(html);
		html.endBODY();
		html.endHTML();
		return html;
	}

	protected String getBodyId() {
		return null;
	}

	@Override
	public String toString() {
		return build(new HtmlBuilder()).toString();
	}

}
