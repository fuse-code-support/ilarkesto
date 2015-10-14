package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;

public abstract class AEditableSuggestBoxField<T> extends AEditableField {

	private SuggestBox suggestBox;

	public abstract void applyValue(T value);

	protected abstract String getValue();

	protected abstract T prepareValue(String text);

	protected abstract SuggestOracle getSuggestOracle();

	protected String prepareText(String text) {
		if (text == null) return null;
		text = text.trim();
		if (text.isEmpty()) return null;
		return text;
	}

	public void validateValue(T value) throws RuntimeException {
		if (value == null && isMandatory()) throw new RuntimeException("Eingabe erforderlich.");
	}

	@Override
	public void trySubmit() throws RuntimeException {
		String text = prepareText(suggestBox.getText());
		T value = prepareValue(text);
		validateValue(value);
		applyValue(value);
	}

	@Override
	public IsWidget createEditorWidget() {
		suggestBox = new SuggestBox(getSuggestOracle());
		Style style = suggestBox.getElement().getStyle();
		style.setWidth(getTextBoxWidth(), Unit.PX);
		style.setPadding(Widgets.defaultSpacing, Unit.PX);
		// TODO
		// suggestBox.setMaxLength(getMaxLength());
		suggestBox.setValue(getValue());
		suggestBox.getElement().setId(getId() + "_textBox");
		if (getEditVetoMessage() == null) {
			suggestBox.addKeyUpHandler(new EnterKeyUpHandler());
		} else {
			suggestBox.setEnabled(false);
			suggestBox.setTitle(getEditVetoMessage());
		}
		return suggestBox;
	}

	private int getTextBoxWidth() {
		int width = Window.getClientWidth();
		if (width > 700) width = 700;
		return width;
	}

	protected int getMaxLength() {
		return 1024;
	}

	@Override
	public IsWidget createDisplayWidget() {
		String text = getValue();
		if (text != null) {
			String suffix = getSuffix();
			if (suffix != null) text += " " + suffix;
		}

		Label label = new Label(text);
		label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
		return label;
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
