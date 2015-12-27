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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class MultiColumnPanel implements IsWidget {

	private HorizontalPanel columnsPanel;
	private int currentIndex;
	private int skipCount;
	private boolean responsive = true;
	private Map<Integer, String> columnsWidthsByIndex = new HashMap<Integer, String>();

	public MultiColumnPanel() {
		super();
		columnsPanel = new HorizontalPanel();
		columnsPanel.getElement().getStyle().setWidth(100, Unit.PCT);
	}

	public MultiColumnPanel(String widthForFirstColumn) {
		this();
		setWidthForCurrentColumn(widthForFirstColumn);
	}

	public MultiColumnPanel(int widthForFirstColumn) {
		this(widthForFirstColumn + "px");
	}

	public void add(Object... widgets) {
		addToColumn(currentIndex, widgets);
	}

	public void nextColumn() {
		nextColumn(null);
	}

	public void nextColumn(int maxWidth) {
		nextColumn(maxWidth + "px");
	}

	public void nextColumnForAufgabesKommentars() {
		nextColumn(300);
	}

	public void nextColumn(String maxWidth) {
		if (responsive) {
			if (Window.getClientWidth() < 1024) {
				skipCount++;
				return;
			}
			if (Window.getClientWidth() < 1600) {
				if (skipCount == 0) {
					skipCount++;
					return;
				} else {
					skipCount = 0;
				}
			}
		}
		currentIndex++;
		setWidthForCurrentColumn(maxWidth);
	}

	public String setWidthForCurrentColumn(String width) {
		return columnsWidthsByIndex.put(currentIndex, width);
	}

	private void addToColumn(int index, Object... widgets) {
		Panel column = getColumn(index);
		for (Object widget : widgets) {
			if (widget == null) continue;
			column.add(Widgets.widget(widget));
			column.add(Widgets.verticalSpacer());
		}
	}

	private Panel getColumn(int index) {
		while (index >= columnsPanel.getWidgetCount()) {
			String width = columnsWidthsByIndex.get(index);
			Panel columnPanel = createColumnPanel(index > 0, width);
			columnsPanel.add(columnPanel);
			if (width != null) columnsPanel.setCellWidth(columnPanel, width);
		}
		return (Panel) columnsPanel.getWidget(index);
	}

	private Panel createColumnPanel(boolean spacing, String width) {
		FlowPanel ret = new FlowPanel();
		if (spacing) {
			ret.getElement().getStyle().setMarginLeft(Widgets.defaultSpacing, Unit.PX);
		}
		if (width != null) {
			ret.getElement().getStyle().setProperty("minWidth", "300px");
		}
		return ret;
	}

	@Override
	public Widget asWidget() {
		return columnsPanel;
	}

	public MultiColumnPanel setResponsive(boolean responsive) {
		this.responsive = responsive;
		return this;
	}

}
