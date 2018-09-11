package ilarkesto.gwt.client.desktop.fields;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public abstract class AEditableCodemirrorTextField extends AEditableField {

	// private TextArea textArea;
	private static int count = 0;

	private final String wrapperId = "codemirror_" + ++count;
	private SimplePanel wrapper;

	public abstract void applyValue(String value);

	protected abstract String getValue();

	protected abstract String getMode();

	@Override
	protected IsWidget createEditorWidget() {
		wrapper = new SimplePanel();
		wrapper.getElement().setId(wrapperId);
		Style style = wrapper.getElement().getStyle();
		style.setWidth(Window.getClientWidth() * 0.8, Unit.PX);
		wrapper.addAttachHandler(new AttachEvent.Handler() {

			@Override
			public void onAttachOrDetach(AttachEvent ev) {
				if (ev.isAttached()) {
					insertCodemirror(wrapperId, getMode(), getValue());
				}
			}
		});
		return wrapper;
	}

	private native Object insertCodemirror(String wrapperId, String mode, String value)
	/*-{
	    if (mode === 'json') mode = {name: 'javascript', json: true};
	    var wrapper = $wnd.document.getElementById(wrapperId);
		var codemirror = $wnd.CodeMirror(wrapper, {
			value: value,
			mode:  mode
		});
		return codemirror;
	}-*/;

	@Override
	public void trySubmit() throws RuntimeException {
		// String text = prepareText(textArea.getText());
		// String value = prepareValue(text);
		// validateValue(value);
		// applyValue(value);
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

	private int getTextBoxWidth() {
		int width = Window.getClientWidth();
		if (width > 700) width = 700;
		return width;
	}

	protected String getTextBoxMaxWidth() {
		return Window.getClientWidth() + "px";
	}

	protected int getTextBoxHeight() {
		return 100;
	}

	protected String getDisplayMaxWidth() {
		return Window.getClientWidth() + "px";
	}

	@Override
	public IsWidget createDisplayWidget() {
		String text = getValue();

		Label label = new Label();
		Style style = label.getElement().getStyle();
		if (text == null) {
			text = getAlternateValueIfValueIsNull();
			style.setColor("#AAA");
		}

		label.setText(text);
		style.setWhiteSpace(WhiteSpace.PRE_WRAP);
		style.setProperty("maxWidth", getDisplayMaxWidth());
		style.setOverflow(Overflow.AUTO);

		return label;
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
