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
package ilarkesto.ui.html;

import ilarkesto.ui.web.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

public class Body extends Element {

	private List<Component> bottomComponents = new ArrayList<Component>();

	public Body() {
		super("body");
	}

	@Override
	protected void buildContent() {
		super.buildContent();
		for (Component component : bottomComponents) {
			component.build(html);
		}
	}

	public Body addToBottom(Component component) {
		bottomComponents.add(component);
		return this;
	}

	public Body addBottmJavascriptRef(String href) {
		addToBottom(new Element("script").attr("src", href));
		return this;
	}

	public Body addBottmJavascriptCode(String javascriptCode) {
		addToBottom(new Element("script").html(javascriptCode));
		return this;
	}

	public void addGoogleAnalytics(final String webPropertyId) {
		addToBottom(new Component() {

			@Override
			public void build(HtmlBuilder html) {
				html.googleAnalytics(webPropertyId);
			}
		});
	}

}