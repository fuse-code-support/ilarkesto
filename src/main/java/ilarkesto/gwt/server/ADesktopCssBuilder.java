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

import ilarkesto.gwt.client.desktop.Colors;
import ilarkesto.ui.web.CssBuilder;

public abstract class ADesktopCssBuilder extends CssBuilder {

	public String colBg = "#eeeeee";
	public String colBgHeader = Colors.googleLightGreen;
	public String colBgDialog = "#f5f5f5";
	public String colBgButton = Colors.googleBlue;
	public String colSeparatorLine = "#eeeeee";

	public ADesktopCssBuilder() {
		init();

		reset();
		defaults();
		goonTable();
		goonButton();
		goonAnchorButton();
		goonAnchorPanel();
		dialog();
		desktop();
		cards();
		codeMirror();
	}

	private void codeMirror() {
		style(".CodeMirrorWidget .CodeMirror").heightAuto();
		style(".CodeMirror-readonly .CodeMirror .CodeMirror-vscrollbar").zIndexAuto();
		style(".CodeMirror-readonly .CodeMirror .CodeMirror-hscrollbar").zIndexAuto();
		style(".CodeMirror-readonly .CodeMirror .CodeMirror-scrollbar-filler").zIndexAuto();
		style(".CodeMirror-readonly .CodeMirror .CodeMirror-gutter-filler").zIndexAuto();
		style(".CodeMirror-readonly .CodeMirror .CodeMirror-line").zIndexAuto();
	}

	protected void init() {}

	private void goonTable() {
		String color = Colors.googleGreen;
		String colorHovering = Colors.googleLightGreen;
		style(".TableColumnSetsToggler .gwt-ToggleButton").fontSize(12).padding(0, 5).whiteSpaceNowrap().marginLeft(10)
				.border(1, color).borderRadius(5);

		style(".TableColumnSetsToggler .gwt-ToggleButton-up").color(color).background("#fff");
		style(".TableColumnSetsToggler .gwt-ToggleButton-up-hovering").color("#fff").background(colorHovering);

		style(".TableColumnSetsToggler .gwt-ToggleButton-down").color("#fff").background(color);
		style(".TableColumnSetsToggler .gwt-ToggleButton-down-hovering").color("#fff").background(colorHovering);

		style(".goon-ObjectTable .columnTitleWithSortToggle")
				.background("url(img/ic_action_sort-toggle.png) left center no-repeat").backgroundSize(18)
				.paddingLeft(16);

	}

	private void dialog() {
		style("#goon .gwt-DialogBox .dialogMiddleCenter" + ", #goon .gwt-DialogBox .dialogMiddleLeft"
				+ ", #goon .gwt-DialogBox .dialogMiddleRight" + ", #goon .gwt-DialogBox .dialogTopLeft"
				+ ", #goon .gwt-DialogBox .dialogTopCenter" + ", #goon .gwt-DialogBox .dialogTopRight"
				+ ", #goon .gwt-DialogBox .dialogBottomLeft" + ", #goon .gwt-DialogBox .dialogBottomCenter"
				+ ", #goon .gwt-DialogBox .dialogBottomRight" + ", #goon .gwt-DialogBox .Caption")
						.background(colBgDialog);
		style("#goon .gwt-DialogBox .Caption").borderTopNone().borderBottomNone().fontWeightBold().paddingTop(15);
	}

	private void defaults() {
		// font
		style("#goon, #goon table td, #goon select, #goon input, #goon textarea, #goon .richtextarea, #goon button")
				.fontFamily("OpenSans").fontSize(16).lineHeight(22);

		h1().fontSize("300%").fontWeightBold();
		h2().fontSize("200%").fontWeightBold();
		h3().fontSize("150%").fontWeightBold();
		h4().fontSize("120%").fontWeightBold();

		style("strong, b").fontWeightBold();

		input().padding(5);
		button().cursorPointer();
		hr().height(1).borderNone().background("#dddddd").margin(0).padding(0);

		style(".clickable").cursorPointer();
		style(".centered").marginLeftRightAuto();

		style("#goon .gwt-Label").whiteSpacePreWrap();
	}

	protected void goonButton() {
		style("#goon .goon-Button").background(colBgButton).colorWhite().border(1, Colors.googleLightBlue)
				.fontSize("75%").whiteSpaceNowrap();
		style("#goon .goon-Button:hover").background(Colors.googleLightBlue);
		int iconSize = 24;
		style("#goon .goon-Button > img").padding(0).margin(0).width(iconSize).height(iconSize);
		style("#goon .goon-Button-iconOnly").backgroundNone().width(iconSize).height(iconSize).padding(0).borderNone();
		style("#goon .goon-Button-disabled").backgroundNone().padding(0).borderNone();

		iconSize = 32;
		style("#actionbarContainer .goon-Button > img").width(iconSize).height(iconSize);
		style("#actionbarContainer .goon-Button-iconOnly").width(iconSize).height(iconSize);
	}

	private void goonAnchorButton() {
		style(".goon-AnchorButton").textDecorationNone();
		int iconSize = 24;
		style(".goon-AnchorButton > img").padding(0).margin(0).width(iconSize).height(iconSize);
	}

	private void goonAnchorPanel() {
		style("a.goon-AnchorPanel, a.goon-AnchorPanel:hover, a.goon-AnchorPanel:visited").textDecorationNone()
				.colorInherit();
	}

	protected void desktop() {
		style("#goon").background(colBg).padding(0).margin(0);
		style("#desktop").background(colBg).height(2000);

		// header
		int headerHeight = 54;
		int headerSpacing = 10;
		style("#header").positionFixed(0, 0).displayTable().height(headerHeight).width100().background(colBgHeader)
				.colorWhite().boxShadow(0, 1, 1, "#999").zIndex(42);
		style("#header > div").displayTableCell().verticalAlignMiddle();
		style("#logoContainer").width(42).backgroundUrl("img/header-navig.png").backgroundRepeatNoRepeat()
				.backgroundAttachmentFixed().backgroundPosition(0, 13);
		style("#titleContainer").width(99, "%").marginRight(headerSpacing).fontSize("110%").fontWeightBold()
				.textShadow(1, 1, 1, "#666");
		style("#titleContainer .breadcrumb").fontSize("75%").boxShadow(0, 0, 0, "#999");
		style("#titleContainer .breadcrumb .gwt-Label").color("#999");
		style("#titleContainer .breadcrumb a, #titleContainer .breadcrumb a:visited").color("#EEE")
				.textDecorationNone();
		style("#titleContainer .breadcrumb a:hover").color(Colors.googleLightBlue);
		style("#commandContainer").width(1, "%");
		style("#commandContainer input").background("#D3E992").border(1, Colors.googleGreen).padding(5);
		style("#actionbarContainer").width(1, "%");
		style("#gwtSuperDevModeContainer").width(100);
		style("#gwtSuperDevModeContainer a").displayBlock().floatLeft().margin(headerSpacing).padding(5)
				.background(Colors.googleOrange).colorWhite().fontWeightBold().textDecorationNone().cursorPointer();

		// contentArea
		style("#contentarea").displayTable().marginTop(headerHeight).height100().width100().verticalAlignTop();
		style("#sidebarContainer").displayTableCell().background("linear-gradient(to right, #dddddd, #cccccc)")
				.verticalAlignTop();
		style("#workspaceContainer").displayTableCell().width100().padding(12);

		// style("#goon .gwt-PopupPanel").borderNone().zIndex(100);
	}

	protected void cards() {
		style(".goon-CardPanel").backgroundWhite().border(0, colSeparatorLine).boxShadow(1, 1, 0, "#cccccc")
				.color("#444444");
		style(".goon-CardPanel .title").color("#222222").fontWeightBold().fontSize("105%");
		style(".goon-CardPanel .clickable:hover").background("#f8f8ff");

		// style(".goon-ObjectTable").border(5, "white");
		style(".goon-ObjectTable td, .goon-Object-table th").border(1, colSeparatorLine);
		style(".goon-ObjectTable .trimmedCell").whiteSpaceNowrap();
	}

}
