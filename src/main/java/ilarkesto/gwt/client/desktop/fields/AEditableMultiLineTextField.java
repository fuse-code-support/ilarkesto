package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

public abstract class AEditableMultiLineTextField extends AEditableField {

	private TextArea textArea;

	public abstract void applyValue(String value);

	protected abstract String getValue();

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

	@Override
	public void trySubmit() throws RuntimeException {
		String text = prepareText(textArea.getText());
		String value = prepareValue(text);
		validateValue(value);
		applyValue(value);
	}

	@Override
	public TextArea createEditorWidget() {
		textArea = new TextArea();
		textArea.getElement().setId(getId() + "_textArea");
		Style style = textArea.getElement().getStyle();
		style.setWidth(Widgets.defaultInputWidth(), Unit.PX);
		style.setHeight(getTextBoxHeight(), Unit.PX);
		style.setPadding(Widgets.defaultSpacing, Unit.PX);

		String value = getValue();
		if (value == null) value = getAlternateValueIfValueIsNull();
		textArea.setValue(value);
		if (value != null && getParent() == null) {
			// if an AlternativeValue is set and the field isnt part of a MutliFieldField
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

				@Override
				public void execute() {
					// selectAll only works if widget is attached to the DOM
					textArea.selectAll();
				}
			});
		}

		if (getEditVetoMessage() == null) {
			textArea.addKeyUpHandler(new EnterKeyUpHandler());
		} else {
			textArea.setEnabled(false);
			textArea.setTitle(getEditVetoMessage());
		}

		if (getMaxLength() > -1) {
			textArea.getElement().setAttribute("maxlength", String.valueOf(getMaxLength()));
		}

		return textArea;
	}

	protected int getTextBoxHeight() {
		return 100;
	}

	protected String getDisplayMaxWidth() {
		return Widgets.defaultInputWidth() + "px";
	}

	@Override
	public Label createDisplayWidget() {
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
