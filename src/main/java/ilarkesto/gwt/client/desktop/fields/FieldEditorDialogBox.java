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

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextAlign;
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
		dialog = Widgets.dialog(field.isEditorDialogAutohide(), field.getLabel(), createContent(), createFooter());
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

		BuilderPanel buttonRow = new BuilderPanel().setHorizontal().setSpacing(0);

		buttonRow.setChildTextAlign(TextAlign.RIGHT);
		buttonRow.add(Widgets.horizontalSpacer());
		buttonRow.setChildWidth("1px");

		List<? extends IsWidget> additionalButtons = field.getAdditionalDialogButtons(this);
		buttonRow.addWithPadding(3, new Object[] { cancelButton });
		buttonRow.addWithPadding(3, new Object[] { additionalButtons });
		buttonRow.addWithPadding(3, new Object[] { applyButton });

		return buttonRow.asWidget();
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
