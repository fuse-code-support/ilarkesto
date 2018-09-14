package ilarkesto.gwt.client.desktop.fields;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public class GoonDateBox extends AGoonTextBox {

	private final DateBox dateBox;

	public GoonDateBox(Format format) {
		dateBox = new DateBox();
		dateBox.getElement().setAttribute("autocomplete", "off");
		dateBox.setFormat(format);
		dateBox.getDatePicker().setYearAndMonthDropdownVisible(true);
	}

	@Override
	public Widget asWidget() {
		return dateBox.asWidget();
	}

	@Override
	public String getText() {
		return dateBox.getTextBox().getValue();
	}

	@Override
	public void setMaxLength(int maxLength) {
		dateBox.getTextBox().setMaxLength(maxLength);
	}

	@Override
	public void setValue(String value) {
		dateBox.getTextBox().setValue(value);
	}

	@Override
	public Element getElement() {
		return dateBox.getElement();
	}

	@Override
	public void addKeyUpHandler(KeyUpHandler enterKeyUpHandler) {
		dateBox.getTextBox().addKeyUpHandler(enterKeyUpHandler);
	}

	@Override
	public void setEnabled(boolean b) {
		dateBox.setEnabled(b);
	}

	@Override
	public void setTitle(String editVetoMessage) {
		dateBox.setTitle(editVetoMessage);
	}

	public DateBox getDateBox() {
		return dateBox;
	}

	public void addValueChangeHandler(ValueChangeHandler<java.util.Date> valueChangeHandler) {
		dateBox.addValueChangeHandler(valueChangeHandler);
	}

}
