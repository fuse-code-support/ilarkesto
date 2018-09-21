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
import ilarkesto.core.base.UserInputException;
import ilarkesto.core.base.Utl;
import ilarkesto.core.localization.Localizer;
import ilarkesto.core.money.Money;
import ilarkesto.core.money.MultipleCurrenciesException;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.core.time.DateRange;
import ilarkesto.gwt.client.AAction;
import ilarkesto.gwt.client.desktop.fields.AField;
import ilarkesto.gwt.client.desktop.fields.ASelfdocPanel;
import ilarkesto.gwt.client.editor.ABooleanEditorModel;
import ilarkesto.gwt.client.editor.ADateAndTimeEditorModel;
import ilarkesto.gwt.client.editor.ADateEditorModel;
import ilarkesto.gwt.client.editor.AFieldModel;
import ilarkesto.gwt.client.editor.AIntegerEditorModel;
import ilarkesto.gwt.client.editor.ATextEditorModel;
import ilarkesto.gwt.client.editor.DateAndTimeEditorWidget;
import ilarkesto.gwt.client.editor.DateEditorWidget;
import ilarkesto.gwt.client.editor.IntegerEditorWidget;
import ilarkesto.gwt.client.editor.RichtextEditorWidget;
import ilarkesto.gwt.client.editor.TextEditorWidget;
import ilarkesto.gwt.client.editor.YesNoEditorWidget;

import java.util.Iterator;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Clear;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class Widgets {

	public static int defaultSpacing = 8;

	public static int defaultInputWidth() {
		return Math.min(900, Window.getClientWidth() - (defaultSpacing * 6));
	}

	public static HTML link(String href) {
		return link(href, Str.formatUrlAsLink(href));
	}

	public static HTML link(String href, String text) {
		if (Str.isBlank(href)) return null;
		return new HTML("<a href=\"" + href + "\" target=\"_blank\">" + SafeHtmlUtils.htmlEscape(text) + "</a>");
	}

	public static Image icon(String iconName) {
		return icon(iconName, null);
	}

	public static Image icon(String iconName, Integer size) {
		return icon(iconName, size, null);
	}

	public static Image icon(String iconName, Integer size, String tooltip) {
		if (iconName == null) return null;
		Image image = new Image("img/ic_" + iconName + ".png");
		image.setAltText(iconName);
		if (size != null) image.setSize(size + "px", size + "px");
		if (!Str.isBlank(tooltip)) image.setTitle(tooltip);
		return image;
	}

	public static Image image(String url, Integer width) {
		if (url == null) return null;
		Image image = new Image(url);
		if (width != null) image.setWidth(width + "px");
		return image;
	}

	public static Image loaderAnimation() {
		Image image = new Image("img/loader.gif");
		image.setWidth("24px");
		return image;
	}

	public static FlowPanel flowPanel(Object... objects) {
		FlowPanel panel = new FlowPanel();
		for (Object object : objects) {
			if (object == null) continue;
			Widget widget = widget(object);
			if (widget == null) continue;
			panel.add(widget);
		}
		if (panel.getWidgetCount() == 0) return null;
		return panel;
	}

	public static Widget notification(Object object) {
		if (object == null) return null;

		Label label = new Label(Str.format(object));
		Style style = label.getElement().getStyle();
		style.setColor(Colors.googlePurple);
		style.setFontWeight(FontWeight.BOLD);

		return new BuilderPanel().setStyleCard().addColorMarker(Colors.googleRed).addWithPadding(label).asWidget();
	}

	public static void showDialog(String title, UserInputException ex) {
		showDialog(title, ex.getMessage());
	}

	public static DialogBox showDialog(String title, Object message) {
		IsWidget widget = null;
		if (message instanceof IsWidget) widget = (IsWidget) message;
		if (widget == null) widget = text(message);
		DialogBox dialog = dialog(true, title, widget);
		dialog.show();
		return dialog;
	}

	public static <W extends Widget> W floatLeft(W widget) {
		if (widget == null) return null;
		widget.getElement().getStyle().setFloat(Float.LEFT);
		return widget;
	}

	public static FlowPanel horizontalFlowPanel(int spacing, Object... widgets) {
		return horizontalFlowPanel(spacing, Utl.toList(widgets));
	}

	public static FlowPanel horizontalFlowPanel(int spacing, Iterable widgets) {
		FlowPanel panel = new FlowPanel();
		boolean first = true;
		for (Object object : widgets) {
			Widget widget = widget(object);
			if (widget == null) continue;

			if (first) {
				first = false;
			} else {
				if (spacing > 0) panel.add(horizontalSpacer(spacing));
			}

			widget.getElement().getStyle().setFloat(Float.LEFT);
			panel.add(widget);
		}
		panel.add(clear());
		return panel;
	}

	public static HorizontalPanel horizontalPanel(int spacing, Object... widgets) {
		return horizontalPanel(spacing, Utl.toList(widgets));
	}

	public static HorizontalPanel horizontalPanel(int spacing, Iterable widgets) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setBorderWidth(0);
		boolean first = true;
		for (Object object : widgets) {
			Widget widget = widget(object);
			if (widget == null) continue;

			if (first) {
				first = false;
			} else {
				if (spacing > 0) panel.add(horizontalSpacer(spacing));
			}
			panel.add(widget);
		}
		return panel;
	}

	public static Label textNoWrap(Object text) {
		if (text == null) return null;
		Label label = text(text);
		label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
		return label;
	}

	public static PopupPanel popup(boolean autoHide, String title, IsWidget widget) {
		BuilderPanel bp = new BuilderPanel();
		if (title != null) {
			Label titleLabel = textTitleInverted(title);
			titleLabel.getElement().getStyle().setPadding(Widgets.defaultSpacing, Unit.PX);
			bp.add(titleLabel);
		}
		bp.add(widget);

		PopupPanel dialog = new PopupPanel(autoHide, true);
		dialog.getElement().getStyle().setProperty("maxWidth", "90%");
		dialog.setWidget(bp);
		dialog.setGlassEnabled(true);
		Style style = dialog.getElement().getStyle();
		style.setPadding(0, Unit.PX);
		dialog.center();

		return dialog;
	}

	public static Widget buttonRow(Object... buttons) {
		BuilderPanel buttonRow = new BuilderPanel().setHorizontal().setSpacing(0);

		buttonRow.setChildTextAlign(TextAlign.RIGHT);
		buttonRow.add(Widgets.horizontalSpacer());
		buttonRow.setChildWidth("1px");

		buttonRow.addWithPadding(3, buttons);

		return buttonRow.asWidget();
	}

	public static final int workspaceMarginTop = 56 + defaultSpacing;

	public static DialogBox dialog(boolean autoHide, String title, IsWidget contentWidget, IsWidget footerWidget,
			Integer customWidth) {

		int width = customWidth == null ? defaultDialogWidth() : customWidth;
		int height = Window.getClientHeight() - workspaceMarginTop - 150;
		Widget scroller = scroller(contentWidget, width + "px", height + "px");

		Label titleWidget = text(title);
		titleWidget.setWidth(width + "px");
		Style titleStyle = titleWidget.getElement().getStyle();
		titleStyle.setFontWeight(FontWeight.BOLD);
		titleStyle.setMarginLeft(defaultSpacing, Unit.PX);
		titleStyle.setMarginRight(defaultSpacing, Unit.PX);
		titleStyle.setMarginBottom(defaultSpacing, Unit.PX);

		FlowPanel content = new FlowPanel();
		content.add(titleWidget);
		content.add(scroller);
		content.add(verticalLine(5));
		if (footerWidget != null) content.add(footerWidget);

		DialogBox dialog = new DialogBox(autoHide, true);
		dialog.setWidget(content);
		// dialog.setText(title);
		dialog.setModal(false);
		dialog.setGlassEnabled(true);
		Style style = dialog.getElement().getStyle();
		style.setProperty("maxWidth", "95%");
		style.setPadding(0, Unit.PX);
		dialog.center();

		return dialog;
	}

	public static int defaultDialogWidth() {
		return Math.min(defaultInputWidth() + 50, (int) (Window.getClientWidth() * 0.9));
	}

	public static DialogBox dialog(boolean autoHide, String title, IsWidget contentWidget) {
		return dialog(autoHide, title, contentWidget, null, null);
	}

	public static DialogBox dialog(boolean autoHide, String title, IsWidget contentWidget, IsWidget footerWidget) {
		return dialog(autoHide, title, contentWidget, footerWidget, null);
	}

	public static Widget scrollerY(IsWidget content) {
		SimplePanel panel = new SimplePanel(content.asWidget());
		Style style = panel.getElement().getStyle();
		style.setProperty("width", "auto");
		style.setOverflowX(Overflow.HIDDEN);
		style.setOverflowY(Overflow.SCROLL);
		return panel;
	}

	public static Widget scroller(IsWidget content, String width, String height) {
		SimplePanel panel = new SimplePanel(content.asWidget());
		Style style = panel.getElement().getStyle();
		style.setProperty("width", width);
		style.setProperty("maxHeight", height);
		style.setOverflowX(Overflow.AUTO);
		style.setOverflowY(Overflow.SCROLL);
		return panel;

	}

	public static SimplePanel frame(Object widget) {
		return frame(widget, defaultSpacing);
	}

	public static SimplePanel frame(Object widget, int padding) {
		SimplePanel panel = new SimplePanel(widget(widget));
		panel.setStyleName("goon-frame");
		panel.getElement().getStyle().setPadding(padding, Unit.PX);
		return panel;
	}

	public static SimplePanel frame(Object widget, int paddingLeftRight, int paddingTopBottom) {
		return frame(widget, paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
	}

	public static SimplePanel frame(Object widget, int paddingLeft, int paddingTop, int paddingRight,
			int paddingBottom) {
		SimplePanel panel = new SimplePanel(widget(widget));
		panel.setStyleName("goon-frame");
		panel.getElement().getStyle().setPaddingLeft(paddingLeft, Unit.PX);
		panel.getElement().getStyle().setPaddingTop(paddingTop, Unit.PX);
		panel.getElement().getStyle().setPaddingRight(paddingRight, Unit.PX);
		panel.getElement().getStyle().setPaddingBottom(paddingBottom, Unit.PX);
		return panel;
	}

	public static Label textTitleInverted(Object object) {
		if (object == null) return null;
		Label label = new Label(Str.format(object));
		Style style = label.getElement().getStyle();
		style.setFontWeight(FontWeight.BOLD);
		style.setFontSize(105, Unit.PCT);
		style.setBackgroundColor(Colors.googleBlue);
		style.setColor("white");
		return label;
	}

	public static Label textTitle(Object object) {
		if (object == null) return null;
		Label label = new Label(Str.format(object));
		Style style = label.getElement().getStyle();
		style.setFontWeight(FontWeight.BOLD);
		style.setFontSize(105, Unit.PCT);
		style.setColor("#666");
		return label;
	}

	public static Label textWarning(Object object) {
		if (object == null) return null;
		Label label = text(object);
		label.getElement().getStyle().setColor(Colors.googleOrange);
		return label;
	}

	public static Label textWarningExtreme(Object object) {
		if (object == null) return null;
		Label label = text(object);
		label.getElement().getStyle().setColor(Colors.googleRed);
		return label;
	}

	public static Label textAssurence(Object object) {
		if (object == null) return null;
		Label label = text(object);
		label.getElement().getStyle().setColor(Colors.googleGreen);
		return label;
	}

	public static Label textNotification(Object object) {
		if (object == null) return null;
		Label label = text(object);
		label.getElement().getStyle().setColor(Colors.googlePurple);
		return label;
	}

	public static Label textError(Object object) {
		if (object == null) return null;
		if (object instanceof MultipleCurrenciesException) {
			MultipleCurrenciesException ex = (MultipleCurrenciesException) object;
			object = "Währungen: " + ex.getCurrencyA() + ", " + ex.getCurrencyB();
		}
		Label label = new Label(Str.format(object));
		label.getElement().getStyle().setColor(Colors.googleLightRed);
		return label;
	}

	public static Widget textFieldlabel(Object text, boolean appendMandatoryMarker) {
		if (text == null) return null;

		String html = "<span style='color: #999; font-size: 65%;'>" + Str.toHtml(Str.format(text), true);
		if (appendMandatoryMarker) html += "<span style='color: " + Colors.googleOrange + "'> *</span>";
		html += "</span>";

		return new HTML(html);
	}

	public static Label textSecondary(Object object) {
		if (object == null) return null;
		Label label = new Label(Str.format(object));
		label.getElement().getStyle().setColor("#999999");
		return label;
	}

	public static Label textLabel(Object object) {
		if (object == null) return null;
		Label label = new Label(Str.format(object));
		label.getElement().getStyle().setColor(Colors.googleBlue);
		return label;
	}

	public static Label text(Number object, boolean thousandsSeparator) {
		return new Label(Localizer.get().format(object, thousandsSeparator));
	}

	public static HTML html(String html) {
		if (html == null) return null;
		return new HTML(html);
	}

	public static Label text(Object object) {
		if (object == null) return null;
		if (object instanceof DateRange) return new Label(((DateRange) object).formatShortest());
		Label label;
		if (object instanceof Number) {
			Number number = (Number) object;
			label = new Label(Localizer.get().format(number, true, 2, true));
		} else {
			label = new Label(Str.format(object));
		}
		if (object instanceof Money || object instanceof Number) {
			label.getElement().getStyle().setTextAlign(TextAlign.RIGHT);
			label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
		}
		return label;
	}

	public static Label textColored(Object object, String color) {
		Label label = text(object);
		if (label != null && color != null) label.getElement().getStyle().setColor(color);
		return label;
	}

	public static SimplePanel wrapperFloatLeft(Widget child) {
		if (child == null) return null;
		SimplePanel wrapper = new SimplePanel(child);
		wrapper.getElement().getStyle().setFloat(Float.LEFT);
		return wrapper;
	}

	public static Widget gotoHrefButton(String href, boolean targetBlank) {
		return gotoHrefButton(href, targetBlank, null);
	}

	public static Widget gotoHrefButton(String href, boolean targetBlank, String iconName) {
		if (href == null) return null;
		Anchor anchor = new Anchor("", href);
		anchor.addStyleName("goon-AnchorButton");
		if (targetBlank) anchor.setTarget("_blank");

		if (iconName == null) iconName = getHrefIcon(href);
		Image image = new Image("img/ic_action_" + iconName + ".png");
		image.addStyleName("goon-AnchorButton-img");
		anchor.getElement().appendChild(image.getElement());

		return anchor;
	}

	private static String getHrefIcon(String href) {
		if (href == null) return "goto-entity";
		if (href.startsWith("http://")) return "href";
		if (href.startsWith("https://")) return "href";
		if (href.startsWith("mailto:")) return "email";
		return "goto-entity";
	}

	public static Focusable focusAndSelect(IsWidget widget) {
		Focusable focusable = focus(widget);
		if (focusable == null) return focusable;

		if (focusable instanceof ValueBoxBase && !(focusable instanceof TextArea)) {
			((ValueBoxBase) focusable).selectAll();
		} else if (focusable instanceof DateBox) {
			((DateBox) focusable).getTextBox().selectAll();
		}

		return focusable;
	}

	/**
	 * Focus the first focusable widget in the given widget or its child widgets.
	 *
	 * @param widget
	 * @return the widget which was focused
	 */
	public static Focusable focus(IsWidget widget) {
		if (widget == null) return null;

		if (widget instanceof DateBox) {
			DateBox dateBox = (DateBox) widget;
			Focusable focusable = dateBox.getTextBox();
			focusable.setFocus(true);
			return focusable;
		} else if (widget instanceof Focusable) {
			Focusable focusable = (Focusable) widget;
			focusable.setFocus(true);
			return focusable;
		}

		if (widget instanceof HasOneWidget) return focus(((HasOneWidget) widget).getWidget());

		if (widget instanceof HasWidgets) {
			Iterator<Widget> iterator = ((HasWidgets) widget).iterator();
			while (iterator.hasNext()) {
				Focusable focusable = focus(iterator.next());
				if (focusable != null) return focusable;
			}
		}

		return null;
	}

	public static Widget verticalLine(int margin) {
		SimplePanel spacer = new SimplePanel();
		Style style = spacer.getElement().getStyle();
		style.setWidth(100, Unit.PCT);
		style.setHeight(1, Unit.PX);
		style.setBackgroundColor("#eeeeee");
		style.setMarginTop(margin, Unit.PX);
		style.setMarginBottom(margin, Unit.PX);
		return spacer;
	}

	public static Widget horizontalLine(int margin) {
		SimplePanel spacer = new SimplePanel();
		Style style = spacer.getElement().getStyle();
		style.setFloat(Float.LEFT);
		style.setWidth(1, Unit.PX);
		style.setHeight(100, Unit.PCT);
		style.setBackgroundColor("#999999");
		style.setMarginLeft(margin, Unit.PX);
		style.setMarginRight(margin, Unit.PX);
		return spacer;
	}

	public static SimplePanel horizontalSpacer() {
		return horizontalSpacer(defaultSpacing);
	}

	public static SimplePanel horizontalSpacer(int width) {
		SimplePanel spacer = new SimplePanel();
		Style style = spacer.getElement().getStyle();
		style.setWidth(width, Unit.PX);
		spacer.getElement().getStyle().setHeight(1, Unit.PX);
		spacer.getElement().getStyle().setFloat(Float.LEFT);
		return spacer;
	}

	public static Widget verticalSpacer() {
		return verticalSpacer(defaultSpacing);
	}

	public static Widget verticalSpacer(int height) {
		SimplePanel spacer = new SimplePanel();
		Style style = spacer.getElement().getStyle();
		style.setWidth(1, Unit.PX);
		spacer.getElement().getStyle().setHeight(height, Unit.PX);
		return spacer;
	}

	public static void hide(Widget widget) {
		widget.getElement().getStyle().setDisplay(Display.NONE);
	}

	public static void showAsBlock(Widget widget) {
		widget.getElement().getStyle().setDisplay(Display.BLOCK);
	}

	public static Widget clear() {
		SimplePanel panel = new SimplePanel();
		panel.getElement().getStyle().setClear(Clear.BOTH);
		return panel;
	}

	public static FocusPanel clickable(IsWidget content) {
		FocusPanel panel = focusPanel(content);
		panel.addStyleName("clickable");
		return panel;
	}

	public static FocusPanel focusPanel(IsWidget content) {
		return new FocusPanel(content.asWidget());
	}

	public static Widget anchor(Widget widget, String href, String target) {
		if (widget == null) return null;
		if (href == null) return widget;
		return new AnchorPanel(widget, href, target);
	}

	static class AnchorPanel extends SimplePanel {

		public AnchorPanel(IsWidget content, String href, String target) {
			super(DOM.createAnchor());
			getElement().setAttribute("href", href);
			if (target != null) getElement().setAttribute("target", target);
			setStyleName("goon-AnchorPanel");
			add(content);
		}

	}

	public static Widget waitinfo() {
		return loaderAnimation();
	}

	public static Widget waitinfo(String text) {
		text = text == null ? "..." : text + "...";
		return textSecondary(text);
	}

	public static Widget indent(Widget widget, Integer indentation) {
		if (indentation == null) return widget;
		int i = indentation.intValue();
		if (i == 0) return widget;
		SimplePanel frame = frame(widget);
		frame.getElement().getStyle().setPaddingLeft(i * defaultSpacing * 3, Unit.PX);
		return frame;
	}

	public static Widget widget(Object object) {
		return extensionPoint.widget(object);
	}

	@Deprecated
	public static Widget gotoEntityButton(AEntity entity) {
		return extensionPoint.gotoEntityButton(entity);
	}

	public static void addGotoEntityClickHandler(FocusPanel focusPanel, AEntity entity) {
		extensionPoint.addGotoEntityClickHandler(focusPanel, entity);
	}

	public static AAction selfdocAction(String selfdocKey, String title, String tooltip) {
		if (selfdocKey == null) return null;
		return extensionPoint.selfdocAction(selfdocKey, title, tooltip);
	}

	public static ASelfdocPanel selfdocPanel(String selfdocKey, String title, String tooltip) {
		return extensionPoint.selfdocPanel(selfdocKey, title, tooltip);
	}

	public static Workspace workspace() {
		return extensionPoint.workspace();
	}

	public static ASidebarWidget desktopSidebar() {
		return extensionPoint.desktopSidebar();
	}

	protected static ExtensionPoint extensionPoint = new ExtensionPoint();

	protected static class ExtensionPoint {

		public Widget widget(Object object) {
			if (object == null) return null;
			if (object instanceof Widget) return (Widget) object;
			if (object instanceof IsWidget) return ((IsWidget) object).asWidget();
			if (object instanceof AField) return ((AField) object).getWidget();
			if (object instanceof AAction) return new ActionButton((AAction) object).asWidget();
			if (object instanceof AFieldModel) return editor((AFieldModel) object).asWidget();
			return text(object);
		}

		private IsWidget editor(AFieldModel model) {
			if (model == null) return null;
			IsWidget editor;
			if (model instanceof ATextEditorModel) {
				if (((ATextEditorModel) model).isRichtext()) {
					editor = new RichtextEditorWidget((ATextEditorModel) model);
				} else {
					editor = new TextEditorWidget((ATextEditorModel) model);
					if (((ATextEditorModel) model).isSwitchToEditModeIfNull()) {
						((TextEditorWidget) editor).switchToEditModeIfNull();
					}
				}
			} else if (model instanceof ADateAndTimeEditorModel) {
				editor = new DateAndTimeEditorWidget((ADateAndTimeEditorModel) model);
			} else if (model instanceof ADateEditorModel) {
				editor = new DateEditorWidget((ADateEditorModel) model);
			} else if (model instanceof AIntegerEditorModel) {
				editor = new IntegerEditorWidget((AIntegerEditorModel) model);
			} else if (model instanceof ABooleanEditorModel) {
				editor = new YesNoEditorWidget((ABooleanEditorModel) model);
			} else {
				throw new RuntimeException("Unsupported field model: " + model.getClass().getSimpleName());
			}
			return editor;
		}

		public ASidebarWidget desktopSidebar() {
			return new ASidebarWidget();
		}

		public Workspace workspace() {
			return new Workspace(new BreadcrumbHelper());
		}

		public ASelfdocPanel selfdocPanel(String selfdocKey, String title, String tooltip) {
			return null;
		}

		public void addGotoEntityClickHandler(FocusPanel focusPanel, AEntity entity) {
			throw new RuntimeException("Not implemented in ilarkesto. Widgets.extensionPoint not activated.");
		}

		public Widget gotoEntityButton(AEntity entity) {
			return null;
		}

		public AAction selfdocAction(String selfdocKey, String title, String tooltip) {
			return null;
		}
	}

}
