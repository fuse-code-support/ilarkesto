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
package ilarkesto.gwt.client.desktop;

import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Desktop {

	private static final Log log = Log.get(Desktop.class);

	public static ASidebarWidget sidebar;

	public static void initialize() {
		log.info("initialize()");

		sidebar = Widgets.desktopSidebar();
		if (sidebar != null) {
			RootPanel.get("logoContainer").add(new SidebarToggleWidget());
			RootPanel.get("sidebarContainer").add(sidebar);
		}

		log.info("Initialized.");
	}

	public static void showError(Throwable ex) {
		showError(Str.formatException(ex));
	}

	public static void showError(String message) {
		DialogBox dialog = new DialogBox(true, true);
		dialog.setText("Autsch!");
		Label messageLabel = new Label(message);
		VerticalPanel vertical = new VerticalPanel();
		vertical.getElement().getStyle().setPadding(20, Unit.PX);
		vertical.add(messageLabel);
		dialog.setWidget(vertical);
		dialog.center();
		dialog.show();
	}

	public static void showWorkspace(Workspace workspace) {
		if (sidebar != null) sidebar.hide();

		RootPanel workspaceContainer = RootPanel.get("workspaceContainer");
		workspaceContainer.clear();

		RootPanel titleContainer = RootPanel.get("titleContainer");
		titleContainer.clear();

		RootPanel actionbarContainer = RootPanel.get("actionbarContainer");
		actionbarContainer.clear();

		Window.scrollTo(0, 0);

		titleContainer.add(workspace.getTitleWidget());
		workspaceContainer.add(workspace.getContentWrapper());
		actionbarContainer.add(workspace.getActionbar());
		Window.setTitle(workspace.getTitleText() + workspace.getWindowTitlePostfix());
	}

	public static class SidebarToggleWidget implements IsWidget {

		private FocusPanel focusPanel;

		public SidebarToggleWidget() {
			focusPanel = new FocusPanel();
			Style style = focusPanel.getElement().getStyle();
			style.setWidth(42, Unit.PX);
			style.setHeight(56, Unit.PX);
			focusPanel.addStyleName("clickable");
			focusPanel.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					focusPanel.setFocus(false);
					if (sidebar != null) sidebar.toggle();
				}
			});
		}

		@Override
		public Widget asWidget() {
			return focusPanel;
		}

	}
}
