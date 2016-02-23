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

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.TextBox;

public abstract class AEditableFloatField extends AEditableTextBoxField<Float> {

	@Override
	public Float prepareValue(String value) {
		if (value == null) return null;
		value = value.trim();
		if (value.isEmpty()) return null;
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException ex) {
			throw new RuntimeException("Eingabe einer Zahl erforderlich.");
		}
	}

	@Override
	protected int getMaxLength() {
		return 13;
	}

	@Override
	public AGoonTextBox createEditorWidget() {
		AGoonTextBox textBox = super.createEditorWidget();
		// textBox.addKeyPressHandler(new NumericKeyPressHandler());
		// textBox.addKeyUpHandler(new NumericKeyUpHandler());
		// textBox.addMouseOutHandler(new NumericMouseOutHandler());
		return textBox;
	}

	@Override
	protected boolean isDisplayValueAlignedRight() {
		return true;
	}

	@Override
	protected boolean isLabelAlignRight() {
		return true;
	}

	private class NumericKeyPressHandler implements KeyPressHandler {

		@Override
		public void onKeyPress(KeyPressEvent event) {

			if (!Character.isDigit(event.getCharCode())) {
				((TextBox) event.getSource()).cancelKey();
			}
		}
	}

	private class NumericKeyUpHandler implements KeyUpHandler {

		@Override
		public void onKeyUp(KeyUpEvent event) {
			TextBox textBox = (TextBox) event.getSource();
			String input = textBox.getText();
			if (!input.matches("[0-9]*")) {
				textBox.setText("");
			}
		}
	}

	private class NumericMouseOutHandler implements MouseOutHandler {

		@Override
		public void onMouseOut(MouseOutEvent event) {
			TextBox textBox = (TextBox) event.getSource();
			String input = textBox.getText();
			if (!input.matches("[0-9]*")) {
				textBox.setText("");
			}

		}

	}

}
