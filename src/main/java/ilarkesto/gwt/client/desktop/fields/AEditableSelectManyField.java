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
package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.core.base.Str;
import ilarkesto.core.base.UserInputException;
import ilarkesto.gwt.client.AAction;
import ilarkesto.gwt.client.desktop.AObjectTableWithGroups;
import ilarkesto.gwt.client.desktop.ActionButton;
import ilarkesto.gwt.client.desktop.BuilderPanel;
import ilarkesto.gwt.client.desktop.Colors;
import ilarkesto.gwt.client.desktop.Widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableSelectManyField extends AEditableField {

	private static final int ITEM_COUNT_TO_SHOW_SELECT_ALL_ACTIONS = 10;

	private boolean showAsTable = false;

	private Map<String, CheckBox> checkboxes;

	private ItemsTable table;

	public abstract void applyValue(List<String> selectedKeys);

	public abstract String getValueForKey(String key);

	public abstract Collection<String> createOptionKeys();

	public abstract Collection<String> getSelectedOptionKeys();

	public int getSelectableOptionsCount() {
		return -1;
	}

	public String getDisplayValueForKey(String key) {
		return getValueForKey(key);
	}

	@Override
	public boolean isValueSet() {
		Collection<String> keys = getSelectedOptionKeys();
		return keys != null && !keys.isEmpty();
	}

	@Override
	public void trySubmit() throws UserInputException {
		List<String> selectedKeys = table == null ? getSelectedKeysFromCheckboxes() : table.getSelectedKeys();
		int selectableOptionsCount = getSelectableOptionsCount();
		if (selectableOptionsCount > 0 && selectedKeys.size() > selectableOptionsCount)
			throw new UserInputException("Es können maximal " + selectableOptionsCount + " Werte ausgewählt wreden.");
		applyValue(selectedKeys);
	}

	private List<String> getSelectedKeysFromCheckboxes() {
		List<String> selectedKeys = new ArrayList<String>();
		for (Map.Entry<String, CheckBox> entry : checkboxes.entrySet()) {
			String key = entry.getKey();
			CheckBox checkBox = entry.getValue();
			if (checkBox.getValue().booleanValue()) selectedKeys.add(key);
		}
		return selectedKeys;
	}

	@Override
	public final IsWidget createEditorWidget() {
		Collection<String> optionKeys = createOptionKeys();
		if (isShowAsTable(optionKeys)) {
			table = createItemsTable(optionKeys);
			return table;
		}
		return createCheckboxesEditorWidget(optionKeys);
	}

	protected ItemsTable createItemsTable(Collection<String> optionKeys) {
		return new ItemsTable(optionKeys);
	}

	protected boolean isShowAsTable(Collection<String> optionKeys) {
		return showAsTable || optionKeys.size() >= 17;
	}

	public AEditableSelectManyField setShowAsTable(boolean showAsTable) {
		this.showAsTable = showAsTable;
		return this;
	}

	private IsWidget createCheckboxesEditorWidget(Collection<String> optionKeys) {
		checkboxes = new LinkedHashMap<String, CheckBox>();

		boolean horizontal = isHorizontal();
		Panel panel = horizontal ? new FlowPanel() : new VerticalPanel();

		Collection<String> selectedKeys = getSelectedOptionKeys();

		int inRow = 0;

		for (String key : optionKeys) {
			String label = getTextForOption(getValueForKey(key));
			CheckBox checkBox = new CheckBox(label);
			checkBox.getElement().setId(getId() + "_checkbox_" + key);
			checkBox.setValue(selectedKeys.contains(key));
			if (getEditVetoMessage() == null) {} else {
				checkBox.setEnabled(false);
				checkBox.setTitle(getEditVetoMessage());
			}
			updateStyle(checkBox);
			checkBox.addValueChangeHandler(new CheckboxChangeHandler(checkBox));
			if (horizontal) {
				Style style = checkBox.getElement().getStyle();
				style.setProperty("minWidth", "100px");
				style.setDisplay(Display.BLOCK);
				style.setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
				style.setWidth(Math.max(100, (Widgets.defaultInputWidth() / 3) - 3), Unit.PX);
				style.setMarginRight(Widgets.defaultSpacing, Unit.PX);
			}
			checkboxes.put(key, checkBox);
			panel.add(checkBox);
			inRow++;
			if (horizontal && inRow >= 3) {
				panel.add(new HTML("<br>"));
				inRow = 0;
			}
		}
		if (horizontal) {
			panel.add(Widgets.clear());
		}

		if (optionKeys.size() >= ITEM_COUNT_TO_SHOW_SELECT_ALL_ACTIONS) {
			panel.add(new ActionButton(new SelectAllCheckboxesAction()));
			panel.add(new ActionButton(new DeselectAllCheckboxesAction()));
		}

		return panel;
	}

	private String getTextForOption(Object value) {
		return value == null ? "" : value.toString();
	}

	private void updateStyle(CheckBox checkBox) {
		Style style = checkBox.getElement().getStyle();
		if (checkBox.getValue().booleanValue()) {
			style.setColor("#000");
		} else {
			style.setColor("#666");
		}
	}

	protected boolean isHorizontal() {
		return true;
	}

	@Override
	public IsWidget createDisplayWidget() {
		return new Label(createDisplayText());
	}

	protected String createDisplayText() {
		Collection<String> selectedKeys = getSelectedOptionKeys();
		List<String> values = new ArrayList<String>(selectedKeys.size());
		for (String key : selectedKeys) {
			String itemText = getDisplayValueForKey(key);
			values.add(itemText);
		}
		return isDisplayMultiline(values) ? Str.concat(values, "\n", "* ", null) : Str.concat(values, ", ");
	}

	protected boolean isDisplayMultiline(List<String> values) {
		return values.size() > 1 && Str.getTotalLength(values) >= 120;
	}

	public class ItemsTable extends AObjectTableWithGroups<Item, Object> {

		private ArrayList<Item> items;

		public ItemsTable(Collection<String> optionKeys) {
			Collection<String> selectedKeys = getSelectedOptionKeys();
			items = new ArrayList<Item>(optionKeys.size());
			for (String key : optionKeys) {
				Item item = new Item(key, getValueForKey(key));
				items.add(item);
				if (selectedKeys.contains(item.key)) item.selected = true;
			}
		}

		@Override
		protected void init(BuilderPanel wrapper) {
			super.init(wrapper);

			if (items.size() >= ITEM_COUNT_TO_SHOW_SELECT_ALL_ACTIONS)
				wrapper.createTitle("", new SelectAllInTableAction(), new DeselectAllInTableAction());

			add(new AColumn() {

				@Override
				public Widget getCellWidget(Item o) {
					if (!o.selected) return null;
					return Widgets.icon("checked", 32);
				}

				@Override
				protected boolean isTrimmed() {
					return true;
				}

				@Override
				public TextBox getFilterWidget() {
					return null;
				}

			});

			initColumns();

		}

		@Override
		public String getSelfdocKey() {
			return null;
		}

		protected void initColumns() {
			add(new AColumn() {

				@Override
				public Object getCellValue(Item o) {
					return getTextForOption(o.value);
				}

			});
		}

		@Override
		protected Collection<Item> getObjects() {
			return items;
		}

		@Override
		protected void onClick(Item object, int column) {
			if (getEditVetoMessage() != null) return;
			object.selected = !object.selected;
			updateCells();
		}

		@Override
		protected boolean isClickable() {
			return getEditVetoMessage() == null;
		}

		@Override
		protected boolean isColumnFilteringEnabled() {
			return items.size() > 10;
		}

		public List<String> getSelectedKeys() {
			ArrayList<String> ret = new ArrayList<String>();
			for (Item item : items) {
				if (item.selected) ret.add(item.key);
			}
			return ret;
		}

		@Override
		protected String getRowColor(Item o) {
			if (o.selected) return Colors.googlePurple;
			return Colors.greyedText;
		}

		@Override
		protected boolean isGroupingEnabled() {
			return false;
		}

		@Override
		protected Object getGroup(Item o) {
			throw new IllegalStateException("getGroup() should never be called here");
		}

		class SelectAllInTableAction extends AAction {

			@Override
			public String getLabel() {
				return "Alle auswählen";
			}

			@Override
			protected String getIconName() {
				return "select_all";
			}

			@Override
			protected void onExecute() {
				for (Item item : items) {
					item.selected = true;
				}
				ItemsTable.this.update();
			}
		}

		class DeselectAllInTableAction extends AAction {

			@Override
			public String getLabel() {
				return "Keine auswählen";
			}

			@Override
			protected String getIconName() {
				return "deselect_all";
			}

			@Override
			protected void onExecute() {
				for (Item item : items) {
					item.selected = false;
				}
				ItemsTable.this.update();
			}

		}
	}

	public static class Item {

		private String key;
		private Object value;
		private boolean selected;

		public Item(String key, Object value) {
			super();
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public boolean isSelected() {
			return selected;
		}

		public <T> T getValue(Class<T> valueType) {
			return (T) value;
		}

	}

	class SelectAllCheckboxesAction extends AAction {

		@Override
		public String getLabel() {
			return "Alle auswählen";
		}

		@Override
		protected String getIconName() {
			return "select_all";
		}

		@Override
		protected void onExecute() {
			Boolean value = true;
			for (CheckBox checkbox : checkboxes.values()) {
				checkbox.setValue(value);
			}
		}
	}

	class DeselectAllCheckboxesAction extends AAction {

		@Override
		public String getLabel() {
			return "Keine auswählen";
		}

		@Override
		protected String getIconName() {
			return "deselect_all";
		}

		@Override
		protected void onExecute() {
			Boolean value = false;
			for (CheckBox checkbox : checkboxes.values()) {
				checkbox.setValue(value);
			}
		}

	}

	class CheckboxChangeHandler implements ValueChangeHandler<Boolean> {

		private CheckBox checkBox;

		public CheckboxChangeHandler(CheckBox checkBox) {
			super();
			this.checkBox = checkBox;
		}

		@Override
		public void onValueChange(ValueChangeEvent<Boolean> event) {
			updateStyle(checkBox);
		}
	}

}
