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
package ilarkesto.gwt.client.bootstrap;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class NavbarWidget implements IsWidget {

	private FlowPanel navbar;
	private SimplePanel navbarBrand;

	public NavbarWidget() {
		navbar = new FlowPanel();
		// TODO change tag to <nav>
		navbar.addStyleName("navbar");
		navbar.addStyleName("navbar-full");
		navbar.addStyleName("navbar-fixed-top");
		navbar.addStyleName("navbar-dark");
		navbar.addStyleName("bg-inverse");

		navbarBrand = new SimplePanel();
		navbarBrand.addStyleName("navbar-brand");
		navbarBrand.add(new Label("Brand here"));
		navbar.add(navbarBrand);
	}

	@Override
	public Widget asWidget() {
		return navbar;
	}

}
