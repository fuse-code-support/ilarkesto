package ilarkesto.gwt.client.desktop.fields;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class GoonTextBox extends AGoonTextBox {

	private TextBox textBox;

	public GoonTextBox(boolean password) {
		textBox = password ? new PasswordTextBox() : new TextBox();
		textBox.getElement().setAttribute("autocomplete", "off");
	}

	@Override
	public Widget asWidget() {
		return textBox;
	}

	@Override
	public String getText() {
		return textBox.getText();
	}

	@Override
	public void setMaxLength(int maxLength) {
		textBox.setMaxLength(maxLength);
	}

	@Override
	public void setValue(String value) {
		textBox.setValue(value);
	}

	@Override
	public Element getElement() {
		return textBox.getElement();
	}

	@Override
	public void addKeyUpHandler(KeyUpHandler enterKeyUpHandler) {
		textBox.addKeyUpHandler(enterKeyUpHandler);
	}

	@Override
	public void setEnabled(boolean b) {
		textBox.setEnabled(b);
	}

	@Override
	public void setTitle(String editVetoMessage) {
		textBox.setTitle(editVetoMessage);
	}

}
