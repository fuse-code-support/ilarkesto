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

import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.gwt.client.Updatable;
import ilarkesto.gwt.client.desktop.BuilderPanel;
import ilarkesto.gwt.client.desktop.Widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FieldEditorDialogBox {

	private static Log log = Log.get(FieldEditorDialogBox.class);

	private AEditableField field;
	private Widget editorWidget;

	private DialogBox dialog;
	private Label errorLabel;

	private ASelfdocPanel selfdocPanel;
	private Integer customWidth;

	public FieldEditorDialogBox(AEditableField field) {
		this.field = field;
	}

	public void submit() {
		errorLabel.setText(null);
		Widgets.hide(errorLabel);
		try {
			field.submit();
		} catch (Exception ex) {
			log.info(ex);
			errorLabel.setText(Str.formatException(ex));
			Widgets.showAsBlock(errorLabel);
			return;
		}

		Updatable updatable = field.getUpdatable();
		if (updatable != null) updatable.update();
		field.update();

		if (dialog == null) return;
		dialog.hide();
		dialog = null;
	}

	public void center() {
		if (dialog == null) return;
		dialog.center();
	}

	public void cancel() {
		if (dialog == null) return;
		dialog.hide();
		dialog = null;
	}

	public void show() {
		createDialog();
		dialog.center();
		dialog.show();
		Widgets.focusAndSelect(editorWidget);
	}

	private void createDialog() {
		dialog = Widgets.dialog(field.isEditorDialogAutohide(), field.getLabel(), createContent(), createFooter(),
			customWidth);
		dialog.setModal(false);
		if (selfdocPanel != null) selfdocPanel.setDialog(dialog);
	}

	private Widget createContent() {
		BuilderPanel panel = new BuilderPanel();
		panel.setSpacing(0);

		String tooltip = field.getTooltip();
		if (tooltip != null) {
			Label tooltipLabel = new Label(tooltip);
			tooltipLabel.getElement().getStyle().setMarginBottom(Widgets.defaultSpacing, Unit.PX);
			panel.add(tooltipLabel);
		}

		errorLabel = createErrorLabel();
		panel.add(errorLabel);

		IsWidget header = field.createEditorHeaderWidget();
		if (header != null) {
			panel.add(header);
			panel.add(Widgets.verticalSpacer());
		}

		editorWidget = field.createEditorWidgetForUse();
		panel.add(editorWidget);
		if (field.isSelfdocEnabled()) {
			String selfdocKey = field.getSelfdocKey();
			panel.add(Widgets.verticalSpacer());
			selfdocPanel = Widgets.selfdocPanel(selfdocKey, null, null);
			panel.add(selfdocPanel);
		}

		return Widgets.frame(panel.asWidget());
	}

	private Widget createFooter() {
		Button applyButton = new Button(field.getApplyButtonLabel(), new ApplyClickHandler());
		applyButton.getElement().setId("applyButton");
		applyButton.setStyleName("goon-Button");
		applyButton.getElement().getStyle().setMarginTop(Widgets.defaultSpacing, Unit.PX);

		Button cancelButton = new Button("Abbrechen", new CancelClickHandler());
		cancelButton.getElement().setId("cancelButton");
		cancelButton.setStyleName("goon-Button");

		List<? extends IsWidget> additionalButtons = field.getAdditionalDialogButtons(this);

		List buttons = new ArrayList();
		buttons.add(cancelButton);
		if (additionalButtons != null) buttons.addAll(additionalButtons);
		buttons.add(applyButton);
		return Widgets.buttonRow(buttons);
	}

	private Label createErrorLabel() {
		Label label = new Label();
		Style style = label.getElement().getStyle();
		style.setDisplay(Display.NONE);
		style.setColor("#cc0000");
		style.setMarginTop(Widgets.defaultSpacing, Unit.PX);
		style.setMarginBottom(Widgets.defaultSpacing, Unit.PX);
		return label;
	}

	public boolean isField(AField f) {
		return field == f;
	}

	public AEditableField getField() {
		return field;
	}

	public void setFieldValue(String value) {
		if (field instanceof AEditableTextBoxField) {
			((AEditableTextBoxField) field).setTextBoxValue(value);
		} else {
			throw new RuntimeException(
					"Unsupported field type for FieldEditorDialogBox.setFieldValue(): " + field.getClass());
		}
	}

	public FieldEditorDialogBox setCustomWidth(Integer customWidth) {
		this.customWidth = customWidth;
		return this;
	}

	private class ApplyClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			submit();
		}
	}

	private class CancelClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			cancel();
		}
	}

}
