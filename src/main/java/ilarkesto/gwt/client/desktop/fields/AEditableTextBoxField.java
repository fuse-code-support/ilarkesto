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

import ilarkesto.gwt.client.desktop.Widgets;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public abstract class AEditableTextBoxField<T> extends AEditableField {

	public interface Validator<T> {

		public void validate(T value) throws RuntimeException;
	}

	private Validator<T> validator;

	private AGoonTextBox textBox;

	public abstract void applyValue(T value);

	protected abstract String getValue();

	protected abstract T prepareValue(String text);

	@Override
	public boolean isValueSet() {
		return getValue() != null;
	}

	protected String prepareText(String text) {
		if (text == null) return null;
		text = text.trim();
		if (text.isEmpty()) return null;
		return text;
	}

	public void validateValue(T value) throws RuntimeException {
		if (value == null && isMandatory()) throw new RuntimeException("Eingabe erforderlich.");
		if (value != null && validator != null) validator.validate(value);
	}

	public AEditableTextBoxField<T> setValidator(Validator<T> validator) {
		this.validator = validator;
		return this;
	}

	@Override
	public void trySubmit() throws RuntimeException {
		String text = prepareText(textBox.getText());
		T value = prepareValue(text);
		validateValue(value);
		applyValue(value);
	}

	@Override
	public AGoonTextBox createEditorWidget() {
		textBox = createTextBox();
		Style style = textBox.getElement().getStyle();
		style.setWidth(getTextBoxWidth(), Unit.PX);
		style.setPadding(Widgets.defaultSpacing, Unit.PX);
		textBox.setMaxLength(getMaxLength());

		String value = getValue();
		if (value == null) value = getAlternateValueIfValueIsNull();
		textBox.setValue(value);

		textBox.getElement().setId(getId() + "_textBox");
		if (getEditVetoMessage() == null) {
			textBox.addKeyUpHandler(new EnterKeyUpHandler());
		} else {
			textBox.setEnabled(false);
			textBox.setTitle(getEditVetoMessage());
		}
		return textBox;
	}

	public final void setTextBoxValue(String value) {
		textBox.setValue(value);
	}

	protected AGoonTextBox createTextBox() {
		List<String> suggestions = getSuggestions();
		if (suggestions != null) return new GoonSuggestBox(suggestions);
		Format format = getFormat();
		if (format != null) return new GoonDateBox(format);
		return new GoonTextBox(isMaskedInEditMode());
	}

	public List<String> getSuggestions() {
		return null;
	}

	public Format getFormat() {
		return null;
	}

	protected boolean isMasked() {
		return false;
	}

	protected boolean isMaskedInEditMode() {
		return isMasked();
	}

	private int getTextBoxWidth() {
		int width = Window.getClientWidth();
		if (width > 700) width = 700;
		return width;
	}

	protected int getMaxLength() {
		return 1024;
	}

	protected String getDisplayValue() {
		return getValue();
	}

	@Override
	public IsWidget createDisplayWidget() {
		String text = getDisplayValue();
		if (text != null && isMasked()) text = "**********";
		if (text != null) {
			String suffix = getSuffix();
			if (suffix != null) text += " " + suffix;
		}
		Label label = new Label();

		if (text == null) {
			text = getAlternateValueIfValueIsNull();
			label.getElement().getStyle().setColor("#AAA");
		} else {
			String suffix = getDisplaySuffix();
			if (suffix != null) text += suffix;
		}

		label.setText(text);
		label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
		if (isDisplayValueAlignedRight()) label.getElement().getStyle().setTextAlign(TextAlign.RIGHT);
		if (isDisplayValueNoWrap()) label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
		return label;
	}

	private boolean isDisplayValueNoWrap() {
		return isDisplayValueAlignedRight();
	}

	protected boolean isDisplayValueAlignedRight() {
		return isLabelAlignRight();
	}

	protected String getDisplaySuffix() {
		return null;
	}

	public String getAlternateValueIfValueIsNull() {
		return null;
	}

	public String getSuffix() {
		return null;
	}

	private class EnterKeyUpHandler implements KeyUpHandler {

		@Override
		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				submitOrParentSubmit();
			}
		}

	}

}
