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

import ilarkesto.core.base.EnumMapper;
import ilarkesto.core.base.Utl;
import ilarkesto.gwt.client.desktop.AObjectTable;
import ilarkesto.gwt.client.desktop.BuilderPanel;
import ilarkesto.gwt.client.desktop.Colors;
import ilarkesto.gwt.client.desktop.Widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableSelectOneField extends AEditableField {

	private static final String NULL_KEY = "__NULL__";

	private ListBox listBox;
	private Map<String, RadioButton> radioButtons;
	private ItemsTable table;

	private boolean showAsTable;

	public abstract void applyValue(String value);

	public abstract String getSelectedOptionKey();

	public abstract EnumMapper<String, String> getOptions();

	@Override
	public boolean isValueSet() {
		return getSelectedOptionKey() != null;
	}

	@Override
	public void trySubmit() throws RuntimeException {
		String value = getSelectedValue();
		if (value == null && isMandatory() && !isSubmittingEmptyMandatoryFieldAllowed())
			throw new RuntimeException("Auswahl erforderlich");
		applyValue(value);
	}

	private String getSelectedValue() {
		if (table != null) {
			String selectedKey = table.getSelectedKey();
			if (NULL_KEY.equals(selectedKey)) return null;
			return selectedKey;
		} else if (radioButtons != null) {
			for (String value : radioButtons.keySet()) {
				RadioButton radioButton = radioButtons.get(value);
				if (!radioButton.getValue()) continue;
				if (NULL_KEY.equals(value)) return null;
				return value;
			}
		} else {
			int selectedIndex = listBox.getSelectedIndex();
			String value = selectedIndex < 0 ? null : listBox.getValue(selectedIndex);
			if (NULL_KEY.equals(value)) value = null;
			return value;
		}
		return null;
	}

	@Override
	public IsWidget createEditorWidget() {
		if (isShowAsTable()) {
			table = createItemsTable(getOptions());
			return table;
		} else if (isShowAsRadioButtons()) {
			return createRadioButtonPanel();
		} else {
			return createListBox();
		}
	}

	protected ItemsTable createItemsTable(EnumMapper<String, String> options) {
		return new ItemsTable(options);
	}

	public AEditableSelectOneField setShowAsTable(boolean showAsTable) {
		this.showAsTable = showAsTable;
		return this;
	}

	protected boolean isShowAsTable() {
		return showAsTable;
	}

	private Collection<String> getOptionKeys() {
		return getOptions().getKeys();
	}

	protected boolean isShowAsRadioButtons() {
		int optionsCount = getOptionKeys().size();
		if (optionsCount > 20) return false;
		if (isParentMultiField() && optionsCount > 2) return false;
		return true;
	}

	private boolean isParentMultiField() {
		return getParent() instanceof AEditableMultiFieldField;
	}

	private boolean isRadioButtonsHorizontal(Collection<String> optionKeys) {
		return isMandatory() ? optionKeys.size() <= 2 : optionKeys.size() <= 1;
	}

	private Panel createRadioButtonPanel() {
		radioButtons = new HashMap<String, RadioButton>();

		final EnumMapper<String, String> options = getOptions();
		boolean horizontal = isRadioButtonsHorizontal(options.getKeys());
		Panel panel = horizontal ? new FlowPanel() : new VerticalPanel();

		if (!isMandatory()) {
			panel.add(createRadioButton(horizontal, NULL_KEY, getNullValueLabel()));
		}

		for (String key : options.getKeys()) {
			String label = options.getValueForKey(key);
			panel.add(createRadioButton(horizontal, key, label));
		}
		panel.add(Widgets.clear());

		return panel;
	}

	protected String getNullValueLabel() {
		return "<Keine Auswahl>";
	}

	private boolean isSelectedOptionKey(String key) {
		String selectedKey = getSelectedOptionKey();
		if (NULL_KEY.equals(key) && selectedKey == null) return true;
		if (key.equals(selectedKey)) return true;
		return false;
	}

	private RadioButton createRadioButton(boolean horizontal, String key, String label) {
		RadioButton radioButton = new RadioButton(getId(), label);
		radioButton.getElement().setId(getId() + "_radiobutton_");

		radioButton.setValue(isSelectedOptionKey(key));

		if (getEditVetoMessage() != null) {
			radioButton.setEnabled(false);
			radioButton.setTitle(getEditVetoMessage());
		}

		Style style = radioButton.getElement().getStyle();
		style.setProperty("minWidth", "100px");
		style.setProperty("minHeight", "32px");
		style.setDisplay(Display.BLOCK);
		style.setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
		style.setWidth(horizontal ? getTextBoxWidth() / 2 : getTextBoxWidth(), Unit.PX);
		style.setMarginRight(Widgets.defaultSpacing, Unit.PX);
		if (NULL_KEY.equals(key)) {
			style.setColor(Colors.greyedText);
		}

		if (!isParentMultiField()) {
			radioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (fieldEditorDialogBox == null) return;
					fieldEditorDialogBox.submit();
				}
			});
		}
		radioButtons.put(key, radioButton);
		return radioButton;
	}

	private ListBox createListBox() {
		listBox = new ListBox();
		listBox.getElement().setId(getId() + "_listBox");
		Style style = listBox.getElement().getStyle();
		style.setWidth(getTextBoxWidth(), Unit.PX);
		style.setPadding(Widgets.defaultSpacing, Unit.PX);

		int i = 0;
		String selectedKey = getSelectedOptionKey();
		int selectedIndex = -1;
		if (!isMandatory() || isSubmittingEmptyMandatoryFieldAllowed()) {
			listBox.addItem(getNullValueLabel(), NULL_KEY);
			selectedIndex = 0;
			i++;
		}

		EnumMapper<String, String> options = getOptions();
		Collection<String> keys = options.getKeys();
		for (String key : keys) {
			String label = options.getValueForKey(key);
			listBox.addItem(label, key);
			if (Utl.equals(selectedKey, key)) selectedIndex = i;
			i++;
		}
		if (selectedIndex >= 0) listBox.setSelectedIndex(selectedIndex);
		if (getEditVetoMessage() == null) {} else {
			listBox.setEnabled(false);
			listBox.setTitle(getEditVetoMessage());
		}

		// listBox.addClickHandler(new ClickHandler() {
		//
		// @Override
		// public void onClick(ClickEvent event) {
		// if (fieldEditorDialogBox == null) return;
		// fieldEditorDialogBox.submit();
		// }
		// });

		if (keys.size() < 3) {
			listBox.addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					if (fieldEditorDialogBox == null) return;
					fieldEditorDialogBox.submit();
					// NativeEvent nat = event.getNativeEvent();
					// Log.TEST("onChange:", nat.getType(), nat.getRelatedEventTarget(),
					// nat.getCurrentEventTarget()
					// .getClass(), nat.getKeyCode(), nat.getButton(), nat.getCharCode());
				}
			});
		}

		listBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				onSelectionChanged();
			}

		});

		listBox.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() != 13) return;
				if (fieldEditorDialogBox == null) return;
				fieldEditorDialogBox.submit();
			}
		});

		return listBox;
	}

	protected void onSelectionChanged() {}

	private int getTextBoxWidth() {
		int width = Window.getClientWidth();
		if (width > 700) width = 700;
		return width;
	}

	@Override
	public IsWidget createDisplayWidget() {
		String value = null;
		String key = getSelectedOptionKey();
		if (key != null) {
			value = getOptions().getValueForKey(key);
			if (value == null) value = "[" + key + "]";
		}
		return new Label(prepareValueForDisplay(key, value));
	}

	protected String prepareValueForDisplay(String key, String value) {
		if (key == null) return getAlternateValueIfValueIsNull();
		return value;
	}

	protected String getAlternateValueIfValueIsNull() {
		return null;
	}

	public class ItemsTable extends AObjectTable<Item> {

		private ArrayList<Item> items;

		public ItemsTable(EnumMapper<String, String> options) {
			String selectedKey = getSelectedOptionKey();
			items = new ArrayList<Item>();
			for (String key : options.getKeys()) {
				Item item = new Item(key, options.getValueForKey(key));
				items.add(item);
				if (Utl.equals(key, selectedKey)) item.selected = true;
			}
		}

		@Override
		protected void init(BuilderPanel wrapper) {
			super.init(wrapper);

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

		protected void initColumns() {
			add(new AColumn() {

				@Override
				public Object getCellValue(Item o) {
					return o.value;
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
			for (Item item : items) {
				item.selected = item == object;
			}

			if (!isParentMultiField()) {
				if (fieldEditorDialogBox == null) return;
				fieldEditorDialogBox.submit();
				return;
			}

			update();
		}

		@Override
		protected boolean isClickable() {
			return getEditVetoMessage() == null;
		}

		@Override
		protected boolean isColumnFilteringEnabled() {
			return items.size() > 42;
		}

		public String getSelectedKey() {
			for (Item item : items) {
				if (item.selected) return item.key;
			}
			return null;
		}

		@Override
		protected String getRowColor(Item o) {
			if (o.selected) return Colors.googlePurple;
			return Colors.greyedText;
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

}
