package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RichTextArea.FontSize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableRichTextField extends AEditableField {

	private RichTextArea textArea;
	private RichTextToolbar toolbar;

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
		if (value == null && isMandatory()) throw new RuntimeException("Eingabe erforderlich.");
	}

	@Override
	public void trySubmit() throws RuntimeException {
		String html = prepareText(toolbar.getHtml());
		String value = prepareValue(html);
		validateValue(value);
		applyValue(value);
	}

	@Override
	public Widget createEditorWidget() {
		textArea = new RichTextArea();
		textArea.getElement().setId(getId() + "_textArea");
		textArea.getElement().getStyle().setProperty("minWidth", "1000px");
		textArea.getElement().getStyle().setProperty("minHeight", "400px");
		textArea.getElement().getStyle().setBackgroundColor("#fff");
		textArea.getElement().getStyle().setBorderWidth(1, Unit.PX);
		textArea.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
		textArea.getElement().getStyle().setBorderColor("#aaa");
		textArea.addInitializeHandler(new InitializeHandler() {

			@Override
			public void onInitialize(InitializeEvent event) {
				textArea.getFormatter().setFontName("Helvetica");
				textArea.getFormatter().setFontSize(FontSize.X_SMALL);
			}
		});
		Style style = textArea.getElement().getStyle();
		style.setWidth(getTextBoxWidth(), Unit.PX);
		style.setHeight(100, Unit.PX);
		style.setPadding(Widgets.defaultSpacing, Unit.PX);

		String value = getValue();
		if (value == null) value = getAlternateValueIfValueIsNull();
		textArea.setHTML(value);

		if (getEditVetoMessage() == null) {
			textArea.addKeyUpHandler(new EnterKeyUpHandler());
		} else {
			textArea.setEnabled(false);
			textArea.setTitle(getEditVetoMessage());
		}

		toolbar = new RichTextToolbar(textArea);

		VerticalPanel vp = new VerticalPanel();
		textArea.setWidth("100%");

		vp.add(toolbar);
		vp.add(textArea);

		return vp;
	}

	private int getTextBoxWidth() {
		int width = Window.getClientWidth();
		if (width > 700) width = 700;
		return width;
	}

	@Override
	public IsWidget createDisplayWidget() {
		String html = getValue();

		HTML htmlWidget = new HTML();
		if (html == null) {
			html = getAlternateValueIfValueIsNull();
			htmlWidget.getElement().getStyle().setColor("#AAA");
		}

		htmlWidget.setHTML(html);
		// html.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
		return htmlWidget;
	}

	public String getAlternateValueIfValueIsNull() {
		return null;
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
