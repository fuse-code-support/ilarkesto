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
package ilarkesto.gwt.server;

import ilarkesto.base.Sys;
import ilarkesto.core.base.Str;
import ilarkesto.core.localization.Localizer;
import ilarkesto.ui.web.AHtmlPageBuilder;
import ilarkesto.ui.web.HtmlBuilder;
import ilarkesto.webapp.AWebApplication;
import ilarkesto.webapp.GwtSuperDevMode;

public abstract class ADesktopHtmlPageBuilder extends AHtmlPageBuilder {

	protected abstract void workspaceContainer(HtmlBuilder html);

	protected String getGwtName() {
		return null;
	}

	protected abstract String getCss();

	@Override
	protected void bodyContent(HtmlBuilder html) {
		AWebApplication webApplication = AWebApplication.get();
		html.comment(webApplication.getApplicationName() + " " + webApplication.getBuildProperties().getBuild());
		html.IFRAMEgwthistory();
		html.NOSCRIPTdefault();

		html.startDIV().setId("desktop");

		html.startDIV().setId("header");

		html.startDIV().setId("logoContainer");
		html.endDIV();

		if (Sys.isDevelopmentMode() && getGwtName() != null) {
			html.startDIV().setId("gwtSuperDevModeContainer");
			// html.A(
			// "javascript:%7B window.__gwt_bookmarklet_params %3D
			// %7B'server_url'%3A'http%3A%2F%2Flocalhost%3A9876%2F'%7D%3B var s %3D
			// document.createElement('script')%3B s.src %3D
			// 'http%3A%2F%2Flocalhost%3A9876%2Fdev_mode_on.js'%3B
			// void(document.getElementsByTagName('head')%5B0%5D.appendChild(s))%3B%7D",
			// "GWT");
			html.startA(GwtSuperDevMode.getCompileHref(getGwtName()));
			html.text("GWT");
			html.endA();
			html.endDIV();
		}

		html.startDIV().setId("titleContainer");
		html.endDIV();

		html.startDIV().setId("commandContainer");
		html.endDIV();

		html.startDIV().setId("actionbarContainer");
		html.endDIV();

		html.endDIV(); // header

		html.startDIV().setId("contentarea");

		html.startDIV().setId("sidebarContainer");
		html.endDIV();

		html.startDIV().setId("workspaceContainer").setWidth("100%");
		workspaceContainer(html);
		html.endDIV();

		html.endDIV(); // contentarea

		html.endDIV(); // desktop
	}

	@Override
	protected void headerContent(HtmlBuilder html) {
		html.LINKfavicon();
		html.TITLE(AWebApplication.get().getApplicationLabel());
		String gwtName = getGwtName();
		if (gwtName != null) {
			html.META("gwt:property", "locale=" + Localizer.get().getLanguage());
			html.SCRIPTjavascript(gwtName + "/" + gwtName + ".nocache.js", null);
		}

		String css = getCss();
		if (!Str.isBlank(css)) {
			html.startSTYLEcss();
			html.html(css);
			html.endSTYLE();
		}
	}

	@Override
	protected String getLanguage() {
		return "en";
	}

	@Override
	protected String getTitle() {
		return AWebApplication.get().getApplicationLabel();
	}

}
