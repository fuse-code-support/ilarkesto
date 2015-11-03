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
import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.gwt.client.AAction;
import ilarkesto.gwt.client.ADataTransferObject;
import ilarkesto.gwt.client.ClientDataTransporter;
import ilarkesto.gwt.client.Updatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public abstract class AObjectTableWithGroups<O, G> implements IsWidget, Updatable {

	protected Log log = Log.get(getClass());

	public enum Mouseover {
		NONE, ROW, CELL
	};

	private SimplePanel asWidgetWrapper;
	private BuilderPanel wrapper;
	private FlexTable table;
	private List<Row> rows;
	private List<AColumn> columns = new ArrayList<AColumn>();
	private int sortColumnIndex = -1;
	private boolean reverseSort;
	protected boolean extended;

	protected abstract Collection<O> getObjects();

	public boolean isExtended() {
		return extended;
	}

	public final void add(AColumn column) {
		column.index = columns.size();
		columns.add(column);
	}

	protected boolean isClickable() {
		return true;
	}

	@Deprecated
	protected boolean isColumnClickable(int column) {
		return isClickable();
	}

	protected Mouseover getMouseover() {
		return isClickable() ? Mouseover.ROW : Mouseover.CELL;
	}

	protected void onUpdate() {
		if (sortColumnIndex == -1) {
			sortColumnIndex = getInitialSortColumnIndex();
			reverseSort = isInitialSortReverse();
		}

		Collection<O> objects;
		try {
			objects = getObjects();
		} catch (Exception ex) {
			throw new RuntimeException(Str.getSimpleName(getClass()) + ".getObjects() failed.", ex);
		}

		log.info("Objects loaded:", objects.size());

		int rowIndex = -1;

		if (isColumnTitlesEnabled()) {
			rowIndex++;
			for (AColumn column : columns) {
				String columnTitle;
				try {
					columnTitle = column.getTitle();
				} catch (Exception ex) {
					log.error(ex);
					columnTitle = "ERROR: " + Str.formatException(ex);
				}

				boolean customSortingEnabled = isCustomSortingEnabled();

				Widget titleWidget = Widgets.textFieldlabel(columnTitle, false);
				if (titleWidget != null && customSortingEnabled && isShowColumnSortingToggleIcon())
					titleWidget.addStyleName("columnTitleWithSortToggle");

				if (titleWidget != null && (sortColumnIndex == column.index)) {
					// Sortierte Spalte hervorheben
					Style style = titleWidget.getElement().getStyle();
					style.setColor("#444");
					style.setFontWeight(FontWeight.BOLD);
					if (reverseSort) style.setFontStyle(FontStyle.ITALIC);
				}

				table.setWidget(rowIndex, column.index, Widgets.frame(titleWidget, Widgets.defaultSpacing, 0,
					Widgets.defaultSpacing, Widgets.defaultSpacing / 2));

				if (customSortingEnabled) {
					for (int col = 0; col < columns.size(); col++) {
						table.getCellFormatter().setStyleName(rowIndex, col, "clickable");
					}
				}
			}
		}

		if (isColumnFilteringEnabled()) {
			rowIndex++;
			for (AColumn column : columns) {
				TextBox filterTextbox = column.getFilterWidget();
				SimplePanel frame = Widgets.frame(filterTextbox, Widgets.defaultSpacing);
				table.setWidget(rowIndex, column.index, frame);
			}
		}

		try {
			rows = createRows(objects, rowIndex);
		} catch (Exception ex) {
			throw new RuntimeException(Str.getSimpleName(getClass()) + ".createRows() failed.", ex);
		}
		table.setVisible(!rows.isEmpty());

		for (Row row : rows) {
			appendRow(row);
			rowIndex++;
		}

		for (int i = 0; i < getFootRowCount(); i++) {
			rowIndex++;
			for (AColumn column : columns) {
				table.setWidget(rowIndex, column.index, column.getFootCellWidget(i));
			}
		}

	}

	protected boolean isShowColumnSortingToggleIcon() {
		return false;
	}

	public void updateFootCells() {
		int rowIndex = -1;
		if (isColumnTitlesEnabled()) rowIndex++;
		if (isColumnFilteringEnabled()) rowIndex++;
		rowIndex += rows.size();
		for (int i = 0; i < getFootRowCount(); i++) {
			rowIndex++;
			for (AColumn column : columns) {
				table.setWidget(rowIndex, column.index, column.getFootCellWidget(i));
			}
		}
	}

	protected int getInitialSortColumnIndex() {
		return -1;
	}

	protected boolean isInitialSortReverse() {
		return false;
	}

	private void appendRow(Row row) {
		if (row.object == null) {
			Widget groupWidget;
			try {
				groupWidget = createGroupWidget(row.group);
			} catch (Exception ex) {
				throw new RuntimeException(Str.getSimpleName(getClass()) + "getGroupWidget() failed");
			}
			table.setWidget(row.tableRowIndex, 0, groupWidget);
			table.getFlexCellFormatter().setColSpan(row.tableRowIndex, 0, columns.size());
		} else {
			for (AColumn column : columns) {
				table.setWidget(row.tableRowIndex, column.index, column.getCellWidget(row.object));
				column.formatCell(row.tableRowIndex, row.object, table.getCellFormatter());
			}
			if (isClickable()) {
				switch (getMouseover()) {
					case ROW:
						table.getRowFormatter().setStyleName(row.tableRowIndex, "clickable");
						break;
					case CELL:
						for (int col = 0; col < columns.size(); col++) {
							if (isColumnClickable(col)) {
								table.getCellFormatter().setStyleName(row.tableRowIndex, col, "clickable");
							}
						}
						break;
					case NONE:
						break;
				}
			}
		}
	}

	protected int getFootRowCount() {
		return 0;
	}

	protected Widget createGroupWidget(G group) {
		if (group instanceof IsWidget) return ((IsWidget) group).asWidget();
		return createGroupHeader(group, "white", Colors.googleBlue);
	}

	protected Widget createGroupHeader(Object text, String textColor, String backgroundColor) {
		Label title = Widgets.text(text);
		Style style = title.getElement().getStyle();
		style.setColor(textColor);
		style.setBackgroundColor(backgroundColor);
		style.setPadding(Widgets.defaultSpacing, Unit.PX);
		return title;
	}

	@Override
	public final Updatable update() {
		log.debug("update()");

		table = null;
		wrapper = null;
		rows = null;
		columns = new ArrayList<AColumn>();

		table = new FlexTable();
		table.getElement().setId(getId() + "_table");
		table.setStyleName("goon-ObjectTable");

		wrapper = new BuilderPanel();
		if (isCardStyle()) wrapper.setStyleCard();
		wrapper.setId(getId());
		// wrapper.setSpacing(0);
		if (getColorForMarker() != null) wrapper.addColorMarker(getColorForMarker());
		init(wrapper);
		wrapper.add(table);
		initWrapperAfterTable(wrapper);

		for (AColumn column : columns) {
			column.formatColumn(table.getColumnFormatter());
		}

		onTableAdded(wrapper, table);

		reverseSort = isReverseSort();
		try {
			onUpdate();
		} catch (Exception ex) {
			throw new RuntimeException(Str.getSimpleName(getClass()) + ".onUpdate() failed", ex);
		}

		asWidgetWrapper.clear();
		asWidgetWrapper.add(wrapper);

		return this;
	}

	protected String getColorForMarker() {
		return null;
	}

	protected boolean isCardStyle() {
		return true;
	}

	protected boolean isColumnTitlesEnabled() {
		return isCustomSortingEnabled();
	}

	protected boolean isColumnFilteringEnabled() {
		return false;
	}

	protected String getId() {
		return Str.getSimpleName(getClass()).replace('$', '_');
	}

	protected void init(BuilderPanel wrapper) {}

	protected void initWrapperAfterTable(BuilderPanel wrapper) {}

	private ClickEvent clickEvent;

	protected void onTableAdded(final BuilderPanel wrapper, final FlexTable table) {
		table.setWidth("100%");
		table.setCellSpacing(10);
		table.setCellPadding(10);
		table.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				clickEvent = event;
				Cell cell = table.getCellForEvent(event);
				if (cell == null) return;
				int row = cell.getRowIndex();
				int columnIndex = cell.getCellIndex();
				if (row == getColumnsTitleRow()) {
					if (!isCustomSortingEnabled()) return;
					if (sortColumnIndex == columnIndex) {
						reverseSort = !reverseSort;
					} else {
						sortColumnIndex = columnIndex;
						reverseSort = false;
					}
					update();
					return;
				} else if (row == getColumnsFilterRow()) { return; }
				O object = getObject(row);
				if (object == null) return;

				columns.get(columnIndex).onClick(object);
				clickEvent = null;
			}

			private int getColumnsTitleRow() {
				if (!isColumnTitlesEnabled()) return -2;
				return 0;
			}

			private int getColumnsFilterRow() {
				if (!isColumnFilteringEnabled()) return -2;
				int row = 0;
				if (isColumnTitlesEnabled()) row++;
				return row;
			}
		});
	}

	protected void onClick(O object, int column) {}

	protected final ClickEvent getClickEvent() {
		return clickEvent;
	}

	public final O getObject(int row) {
		if (isColumnTitlesEnabled()) row--;
		if (isColumnFilteringEnabled()) row--;
		if (row >= rows.size()) return null;
		return rows.get(row).object;
	}

	private Object getAutoSortValue(O o) {
		if (sortColumnIndex < 0) {
			Object sortValue = getSortValue(o);
			for (int i = 0; i < columns.size(); i++) {
				if (columns.get(i).equals(sortValue)) {
					sortColumnIndex = i;
					break;
				}
			}
			return sortValue;
		}
		AObjectTableWithGroups<O, G>.AColumn column = columns.get(sortColumnIndex);
		return column.getSortValue(o);
	}

	protected Object getFallbackSortValue(O o) {
		return o.toString();
	}

	protected Object getSortValue(O o) {
		return o;
	}

	@Override
	public final Widget asWidget() {
		if (asWidgetWrapper == null) {
			asWidgetWrapper = new SimplePanel();
		}
		if (wrapper == null) {
			if (this instanceof DataForClientLoader) {
				loadDataFromServer();
			} else {
				update();
			}
		}
		return asWidgetWrapper;
	}

	protected final void loadDataFromServer() {
		asWidgetWrapper.clear();
		asWidgetWrapper.add(new BuilderPanel().setStyleCard().addWithPadding(Widgets.waitinfo()));
		DataForClientLoaderHelper.load((DataForClientLoader) this);
	}

	public void loadDataForClientOnServer(ClientDataTransporter transporter, ActivityParameters parameters) {
		Collection<? extends AEntity> objects;
		try {
			objects = (Collection<? extends AEntity>) getObjects();
		} catch (ClassCastException ex) {
			log.warn(getClass().getName() + ".loadDataForClientOnServer() failed", ex);
			return;
		}
		transporter.sendToClient(objects);
	}

	public void dataReceivedOnClient(ADataTransferObject result) {
		update();
	}

	public ActivityParameters createParametersForServer() {
		return AActivity.getCurrent().getParameters();
	}

	protected boolean isGroupingEnabled() {
		return true;
	}

	protected abstract G getGroup(O o);

	protected String getHref(O o) {
		return null;
	}

	protected String getRowColor(O o) {
		return null;
	}

	public Integer getIndentation(O o) {
		return null;
	}

	public List<Row> createRows(Collection<O> objects, int rowIndex) {
		List<Row> ret;
		if (!isGroupingEnabled()) {
			ret = new ArrayList<Row>(objects.size());
			int count = addRows(ret, objects, rowIndex);
			rowIndex += count;
			return ret;
		}

		ret = new ArrayList<Row>();

		Map<G, List<O>> objectsByGroup = new HashMap<G, List<O>>();
		for (O o : objects) {
			G group = getGroup(o);
			if (group == null) throw new IllegalStateException("getGroup() returned null for:" + o);
			List<O> list = objectsByGroup.get(group);
			if (list == null) {
				list = new ArrayList<O>();
				objectsByGroup.put(group, list);
			}
			list.add(o);
		}

		List<G> groups = new ArrayList<G>(objectsByGroup.keySet());
		Collections.sort(groups, groupComparator);

		for (G group : groups) {
			ret.add(new Row(group, null, ++rowIndex));
			int count = addRows(ret, objectsByGroup.get(group), rowIndex);
			rowIndex += count;
		}

		return ret;
	}

	protected boolean isReverseSort() {
		return reverseSort;
	}

	protected boolean isAutoSortingEnabled() {
		return true;
	}

	protected boolean isCustomSortingEnabled() {
		return getInitialSortColumnIndex() >= 0;
	}

	private int addRows(List<Row> ret, Collection<O> objects, int rowIndex) {
		List<O> list = new ArrayList<O>(objects);
		if (isAutoSortingEnabled() || isCustomSortingEnabled()) {
			try {
				sortObjects(list);
			} catch (Exception ex) {
				throw new RuntimeException(Str.getSimpleName(getClass()) + ".sortObjects() failed.", ex);
			}
		}
		int count = 0;
		for (O o : list) {
			ret.add(new Row(null, o, ++rowIndex));
			count++;
		}
		return count;
	}

	private final void sortObjects(List<O> objects) {
		Collections.sort(objects, comparator);
	}

	private void applyColumnFilters() {
		for (Row row : rows) {
			table.getRowFormatter().setVisible(row.tableRowIndex, row.matchesColumnFilters());
		}
		onColumnFiltersChanged();
	}

	public List<String> getColumnFilterTexts() {
		ArrayList<String> ret = new ArrayList<String>();
		for (AColumn column : columns) {
			ret.add(column.filterText == null ? "" : column.filterText);
		}
		return ret;
	}

	protected void onColumnFiltersChanged() {}

	private final class Row {

		private G group;
		private O object;
		private int tableRowIndex;

		public Row(G group, O object, int tableRowIndex) {
			super();
			this.group = group;
			this.object = object;
			this.tableRowIndex = tableRowIndex;
		}

		public boolean matchesColumnFilters() {
			if (group != null) return true;
			if (object == null) return false;
			for (AColumn column : columns) {
				if (!column.matchesFilter(object)) return false;
			}
			return true;
		}

	}

	private final Comparator<O> comparator = new Comparator<O>() {

		@Override
		public int compare(O a, O b) {
			Object sortA = getAutoSortValue(a);
			Object sortB = getAutoSortValue(b);
			int ret = isReverseSort() ? Utl.compare(sortB, sortA) : Utl.compare(sortA, sortB);
			if (ret != 0) return ret;

			sortA = getFallbackSortValue(a);
			sortB = getFallbackSortValue(b);
			ret = isReverseSort() ? Utl.compare(sortB, sortA) : Utl.compare(sortA, sortB);
			return ret;
		}
	};

	protected int compareGroups(G a, G b) {
		return Utl.compare(a, b);
	}

	private final Comparator<G> groupComparator = new Comparator<G>() {

		@Override
		public int compare(G a, G b) {
			return compareGroups(a, b);
		}

	};

	public class AColumn implements Updatable {

		private int index;
		private TextBox filterTextbox;
		private String filterText;

		public String getTitle() {
			return null;
		}

		public TextBox getFilterWidget() {
			if (filterTextbox == null) {
				filterTextbox = new TextBox();
				// filterTextbox.getElement().setPropertyString("placeholder", "Filter");
				filterTextbox.setStyleName("goon-TableFilterTextbox");
				filterTextbox.addKeyUpHandler(new KeyUpHandler() {

					@Override
					public void onKeyUp(KeyUpEvent event) {
						filterText = filterTextbox.getText();
						if (Str.isBlank(filterText)) {
							filterText = null;
						} else {
							filterText = filterText.toLowerCase();
						}
						applyColumnFilters();
					}

				});

			}
			return filterTextbox;
		}

		final boolean matchesFilter(O object) {
			return matchesFilter(object, filterText);
		}

		protected boolean matchesFilter(O object, String filterText) {
			if (filterText == null) return true;
			Object value = getFilterValue(object);
			if (value == null) return false;

			String s = value.toString().toLowerCase();
			return s.contains(filterText);
		}

		public Object getSortValue(O o) {
			return getCellValue(o);
		}

		public Object getCellValue(O o) {
			return o;
		}

		public Object getFilterValue(O o) {
			return getCellValue(o);
		}

		public void formatColumn(ColumnFormatter columnFormatter) {
			String width = getWidth();
			if (width != null) columnFormatter.setWidth(index, width);
			if (isTrimmed()) columnFormatter.addStyleName(index, "trimmedCell");
		}

		public void formatCell(int row, O o, CellFormatter cellFormatter) {
			String color = getColor(o);
			if (color != null) {
				cellFormatter.getElement(row, index).getStyle().setColor(color);
			}

		}

		public String getColor(O o) {
			return AObjectTableWithGroups.this.getRowColor(o);
		}

		public String getHref(O o, Widget cellWidget) {
			if (cellWidget instanceof Button) return null;
			return AObjectTableWithGroups.this.getHref(o);
		}

		protected String getWidth() {
			if (isTrimmed()) return "1px";
			return null;
		}

		protected boolean isTrimmed() {
			return false;
		}

		protected boolean isNoWrap() {
			return isTrimmed();
		}

		protected boolean isPadded() {
			return true;
		}

		public Widget getCellWidget(O o) {
			Object cellValue;
			try {
				cellValue = getCellValue(o);
			} catch (Exception ex) {
				log.error(Str.getSimpleName(getClass()) + ".getCellValue() (for column " + index + ") failed.", ex);
				cellValue = "ERROR: " + Str.formatException(ex);
			}
			Widget cellWidget = Widgets.widget(cellValue);
			if (cellWidget != null && isNoWrap()) cellWidget.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);

			Widget ret = isPadded() ? Widgets.frame(cellWidget) : cellWidget;

			if (index == 0) ret = Widgets.indent(ret, getIndentation(o));

			String href = getHref(o, cellWidget);
			if (href == null) return ret;

			return Widgets.anchor(ret, href, null);
		}

		public Widget getFootCellWidget(int index) {
			Object cellValue;
			try {
				cellValue = getFootCellValue(index);
			} catch (Exception ex) {
				log.error(Str.getSimpleName(getClass()) + ".getCellValue() (for column " + index + ") failed.", ex);
				cellValue = "ERROR: " + Str.formatException(ex);
			}
			Widget cellWidget = Widgets.widget(cellValue);
			if (cellWidget == null) return null;
			if (isNoWrap()) cellWidget.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
			cellWidget.getElement().getStyle().setColor(getFootCellColor(index));
			return isPadded() ? Widgets.frame(cellWidget) : cellWidget;
		}

		protected String getFootCellColor(int index) {
			return Colors.googleBlue;
		}

		protected Object getFootCellValue(int index) {
			return null;
		}

		@Override
		public Updatable update() {
			AObjectTableWithGroups.this.update();
			return this;
		}

		public void onClick(O object) {
			AObjectTableWithGroups.this.onClick(object, index);
		}

		public int getIndex() {
			return index;
		}

	}

	public class ToggleExtendAction extends AAction {

		@Override
		protected void onExecute() {
			extended = !extended;
			update();
		}

		@Override
		public String getLabel() {
			if (extended) return "Zusätzliche Spalten ausblenden";
			return "Zusätzliche Spalten einblenden";
		}

		@Override
		protected String getIconName() {
			return "extend";
		}

	}

}
