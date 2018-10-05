package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.gwt.client.desktop.CodemirrorWidget;
import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;

public abstract class AEditableCodemirrorTextField extends AEditableField {

	private CodemirrorWidget codemirror;

	public abstract void applyValue(String value);

	protected abstract String getValue();

	protected abstract String getMode();

	@Override
	protected IsWidget createEditorWidget() {
		codemirror = new CodemirrorWidget(getMode(), getValue(), false);
		Style style = codemirror.getElement().getStyle();
		style.setWidth(getTextBoxWidth(), Unit.PX);
		style.setHeight(getTextBoxHeight(), Unit.PX);
		return codemirror;
	}

	@Override
	public void trySubmit() throws RuntimeException {
		String text = prepareText(codemirror.getValue());
		String value = prepareValue(text);
		validateValue(value);
		applyValue(value);
	}

	@Override
	public boolean isValueSet() {
		return getValue() != null;
	}

	protected String prepareValue(String text) {
		return text;
	}

	protected String prepareText(String text) {
		if (text == null) return null;
		text = text.trim();
		if (text.isEmpty()) return null;
		return text;
	}

	public void validateValue(String value) throws RuntimeException {
		if (value == null && isMandatory() && !isSubmittingEmptyMandatoryFieldAllowed())
			throw new RuntimeException("Eingabe erforderlich.");
	}

	protected int getTextBoxWidth() {
		// return (int) (Window.getClientWidth() * 0.9);
		return (int) ((Widgets.defaultDialogWidth()) * 0.95);
	}

	protected int getTextBoxHeight() {
		return (int) (Window.getClientHeight() * 0.7);
	}

	protected String getDisplayMaxWidth() {
		return Window.getClientWidth() + "px";
	}

	@Override
	public IsWidget createDisplayWidget() {
		String text = getValue();
		if (text == null) {
			text = getAlternateValueIfValueIsNull();
		}
		CodemirrorWidget codemirror = new CodemirrorWidget(getMode(), text, true);
		// codemirror.getElement().getStyle().setCursor(Cursor.POINTER);
		return codemirror;
	}

	public String getAlternateValueIfValueIsNull() {
		return null;
	}

	public int getMaxLength() {
		return -1;
	}

	private class EnterKeyUpHandler implements KeyUpHandler {

		@Override
		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && event.getNativeEvent().getCtrlKey()) {
				submitOrParentSubmit();
			}
		}

	}

}
