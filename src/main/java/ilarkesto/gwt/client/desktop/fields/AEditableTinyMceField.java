package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.gwt.client.desktop.TinyMceWidget;
import ilarkesto.gwt.client.desktop.TinyMceWidget.TinyMceConfigurator;
import ilarkesto.gwt.client.desktop.Widgets;

import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableTinyMceField extends AEditableField implements TinyMceConfigurator {

	private TinyMceWidget tinymce;

	public abstract void applyValue(String value);

	protected abstract String getValue();

	@Override
	public Widget createEditorWidget() {
		tinymce = new TinyMceWidget(getValue(), this);
		tinymce.setWidth(Widgets.defaultInputWidth() + "px");
		tinymce.setHeight(getTextBoxHeight() + "px");
		return tinymce;
	}

	@Override
	public void trySubmit() throws RuntimeException {
		String html = tinymce.getValue();
		String value = prepareValue(html);
		validateValue(value);
		applyValue(value);
	}

	@Override
	public void configureTinyMce(JSONObject conf) {
		List<String> imageFiles = getToolbarInlineImages();
		if (imageFiles != null) {
			String prefix = getToolbarInlineImageUrlPrefix();
			JSONArray list = new JSONArray();
			int i = 0;
			for (String imageFile : imageFiles) {
				JSONObject image = new JSONObject();
				image.put("title", new JSONString(imageFile));
				image.put("value", new JSONString(prefix + imageFile));
				list.set(i, image);
				i++;
			}
			conf.put("image_list", list);
		}
	}

	@Override
	public boolean isEditorDialogAutohide() {
		return false;
	}

	@Override
	public boolean isValueSet() {
		return getValue() != null;
	}

	protected String prepareValue(String text) {
		return text;
	}

	protected final String prepareText(String text) {
		if (text == null) return null;
		text = text.trim();
		if (text.isEmpty()) return null;

		return text;
	}

	public void validateValue(String value) throws RuntimeException {
		if (value == null && isMandatory() && !isSubmittingEmptyMandatoryFieldAllowed())
			throw new RuntimeException("Eingabe erforderlich.");
	}

	public String getToolbarInlineImageUrlPrefix() {
		return "";
	}

	protected int getTextBoxHeight() {
		return 450;
	}

	protected int getDisplayHeight() {
		return getTextBoxHeight();
	}

	@Override
	public IsWidget createDisplayWidget() {
		TinyMceWidget tinyMce = new TinyMceWidget(getValue(), new TinyMceConfigurator() {

			@Override
			public void configureTinyMce(JSONObject conf) {
				conf.put("readonly", new JSONNumber(1));
				conf.put("inline", JSONBoolean.getInstance(false));
				conf.put("menubar", JSONBoolean.getInstance(false));
				conf.put("toolbar", JSONBoolean.getInstance(false));
				conf.put("statusbar", JSONBoolean.getInstance(false));
				conf.put("plugins", new JSONString(""));
				conf.put("height", new JSONString(getDisplayHeight() + "px"));
			}

		});

		IsWidget editor = tinyMce;
		if (getEditVetoMessage() == null) {
			editor = Widgets.flowPanel(editor, Widgets.verticalSpacer(),
				Widgets.textSecondary("Zum Bearbeiten hier klicken"));
		}

		return Widgets.frame(editor, Widgets.defaultSpacing);
	}

	public List<String> getToolbarInlineImages() {
		return Collections.emptyList();
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
