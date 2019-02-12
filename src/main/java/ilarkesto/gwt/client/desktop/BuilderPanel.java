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

import ilarkesto.core.persistance.AEntity;
import ilarkesto.gwt.client.AAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class BuilderPanel implements IsWidget {

	private static final int SPACING_LINE = -1;

	private FlowPanel panel;
	private boolean horizontal;
	private int spacing = Widgets.defaultSpacing;
	private TextAlign childTextAlign;
	private String childWidth;

	private List<AAction> titleActions;

	public BuilderPanel() {
		panel = new FlowPanel();
		panel.getElement().getStyle().setWidth(100, Unit.PCT);
	}

	public BuilderPanel(String cardTitle) {
		this();
		setStyleCard();
		createTitle(cardTitle);
	}

	public void prepareTitleAction(AAction action) {
		if (action == null) return;
		if (titleActions == null) titleActions = new ArrayList<AAction>();
		titleActions.add(action);
	}

	private final SimplePanel getColorMarker(String color) {
		SimplePanel p = new SimplePanel();
		p.setHeight("3px");
		p.getElement().getStyle().setBackgroundColor(color);
		return p;
	}

	public final BuilderPanel addColorMarker(String color) {
		return add(getColorMarker(color));
	}

	public final BuilderPanel createTitle(String title, AAction... actions) {
		BuilderPanel titleRow = createTitleInternal(title);
		for (AAction action : actions) {
			if (action == null) continue;
			titleRow.addWithPadding(action);
		}
		if (titleActions != null) {
			for (AAction action : titleActions) {
				if (action == null) continue;
				titleRow.addWithPadding(action);
			}
		}
		return titleRow;
	}

	public final BuilderPanel createTitle(String title, AEntity entity) {
		return createTitle(title).addWithPadding(Widgets.gotoEntityButton(entity));
	}

	private final BuilderPanel createTitleInternal(String title) {
		BuilderPanel titleRow = createHorizontalChild();

		titleRow.addWithPadding(Widgets.textTitle(title));

		titleRow.setChildTextAlign(TextAlign.RIGHT);
		titleRow.setChildWidth("1px");

		return titleRow;
	}

	public final BuilderPanel createChild() {
		BuilderPanel child = new BuilderPanel();
		add(child);
		child.setSpacing(spacing);
		return child;
	}

	public final BuilderPanel createHorizontalChild() {
		return createChild().setHorizontal();
	}

	public final BuilderPanel add(Widget child) {
		if (child == null) return this;

		if (panel.getWidgetCount() > 0) addSpacer(spacing);

		addChild(child, childWidth);

		return this;
	}

	public BuilderPanel addSpacer(int spacing) {
		if (spacing == 0) return this;
		if (spacing == SPACING_LINE) {
			if (horizontal) {
				panel.add(horizontalWrapper(Widgets.horizontalSpacer(), Widgets.defaultSpacing + "px"));
			} else {
				panel.add(Widgets.verticalLine(0));
			}
		} else {
			if (horizontal) {
				panel.add(horizontalWrapper(Widgets.horizontalSpacer(spacing), spacing + "px"));
			} else {
				panel.add(Widgets.verticalSpacer(spacing));
			}
		}
		return this;
	}

	public BuilderPanel addSpacer() {
		return addSpacer(Widgets.defaultSpacing);
	}

	private final void addChild(IsWidget child, String width) {
		if (horizontal) {
			panel.add(horizontalWrapper(child, width));
		} else {
			panel.add(child);
		}
	}

	private SimplePanel horizontalWrapper(IsWidget child, String width) {
		SimplePanel wrapper = new SimplePanel(child.asWidget());
		wrapper.setStyleName("goon-horizontalWrapper");
		Style style = wrapper.getElement().getStyle();
		style.setProperty("display", "table-cell");
		if (childTextAlign != null) style.setTextAlign(childTextAlign);
		if (width != null) wrapper.setWidth(width);
		return wrapper;
	}

	public BuilderPanel setChildTextAlign(TextAlign childTextAlign) {
		this.childTextAlign = childTextAlign;
		return this;
	}

	public BuilderPanel setChildWidth(String childWidth) {
		this.childWidth = childWidth;
		return this;
	}

	public final BuilderPanel addWarningText(Object... children) {
		for (Object child : children) {
			addWithPadding(Widgets.textWarning(child));
		}
		return this;
	}

	public final BuilderPanel addSecondaryText(Object... children) {
		for (Object child : children) {
			add(Widgets.textSecondary(child));
		}
		return this;
	}

	public final BuilderPanel addWithPadding(Object... children) {
		return addWithPadding(Widgets.defaultSpacing, children);
	}

	public final BuilderPanel addWithPadding(int padding, Object... children) {
		if (children == null) return this;
		for (Object child : children) {
			if (child == null) continue;
			if (child instanceof Collection) {
				for (Object o : (Collection) child) {
					if (o == null) continue;
					addWithPadding(padding, new Object[] { o });
				}
				continue;
			}
			add(Widgets.frame(createWidget(child), padding));
		}
		return this;
	}

	public final BuilderPanel add(Object... children) {
		return add(true, children);
	}

	public final BuilderPanel add(boolean focusable, Object... children) {
		for (Object child : children) {
			if (child == null) continue;
			if (child instanceof Collection) {
				for (Object o : (Collection) child) {
					add(o);
				}
				continue;
			}
			add(createWidget(focusable, child));
		}
		return this;
	}

	public final BuilderPanel addWithEvenWidths(Object... children) {
		int width = 100 / count(children);
		setChildWidth(width + "%");
		return add(children);
	}

	private int count(Object... children) {
		int ret = 0;
		for (Object child : children) {
			if (child == null) continue;
			ret++;
		}
		return ret;
	}

	private Widget createWidget(Object child) {
		return createWidget(true, child);
	}

	private Widget createWidget(boolean focusable, Object child) {
		if (child instanceof AEntity) {
			AEntity entity = (AEntity) child;
			FocusPanel focusPanel = Widgets.clickable(Widgets.widget(entity));
			// focusable.getel // TODO
			focusPanel.getElement().getStyle().setPadding(Widgets.defaultSpacing, Unit.PX);
			Widgets.addGotoEntityClickHandler(focusPanel, entity);
			return focusPanel;
		}
		return Widgets.widget(child);
	}

	public final BuilderPanel clear() {
		panel.clear();
		return this;
	}

	public BuilderPanel setStyleCard() {
		setSpacingLine();
		panel.setStyleName("goon-CardPanel");
		return this;
	}

	public BuilderPanel setBackgroundColor(String color) {
		panel.getElement().getStyle().setBackgroundColor(color);
		return this;
	}

	public BuilderPanel setWidth(int width) {
		panel.getElement().getStyle().setWidth(width, Unit.PX);
		return this;
	}

	public BuilderPanel setHeight(int width) {
		panel.getElement().getStyle().setHeight(width, Unit.PX);
		return this;
	}

	public BuilderPanel setSpacingLine() {
		return setSpacing(SPACING_LINE);
	}

	public BuilderPanel setSpacing(int spacing) {
		this.spacing = spacing;
		return this;
	}

	public final BuilderPanel setHorizontal() {
		return setHorizontal(true);
	}

	public final BuilderPanel setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
		if (horizontal) {
			panel.getElement().getStyle().setProperty("display", "table");
		}
		return this;
	}

	public BuilderPanel setBackgroundDarker() {
		panel.getElement().getStyle().setBackgroundColor("#eeeeee");
		return this;
	}

	public BuilderPanel setId(String id) {
		panel.getElement().setId(id);
		return this;
	}

	public Style getPanelStyle() {
		return panel.getElement().getStyle();
	}

	@Override
	public final Widget asWidget() {
		return panel;
	}

}
