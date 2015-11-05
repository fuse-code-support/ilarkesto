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
import ilarkesto.gwt.client.desktop.ActionButton;
import ilarkesto.gwt.client.desktop.ActivityParameters;
import ilarkesto.gwt.client.desktop.Widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableMultiFieldField extends AEditableField {

	private List<? extends AEditableField> subFields;

	protected abstract List<? extends AEditableField> getSubFields();

	public ActivityParameters createParametersForServer() {
		return new ActivityParameters();
	}

	@Override
	public boolean isValueSet() {
		return true;
	}

	@Override
	public final IsWidget createEditorWidget() {
		VerticalPanel panel = new VerticalPanel();
		initializeEditorPanel(panel);
		if (subFields == null) subFields = getSubFields();
		for (AEditableField subField : subFields) {
			subField.setParent(this);
			panel.add(createLabelWidget(subField));
			panel.add(new HTML("<div style='clear: both;'></div>"));
			panel.add(Widgets.verticalSpacer(2));
			if (subField.isSelfdocEnabled()) {
				String selfdocKey = subField.getSelfdocKey();
				panel.add(Widgets.horizontalPanel(0, subField.createEditorWidgetForUse(),
					new ActionButton(Widgets.selfdocAction(selfdocKey, subField.getLabel(), subField.getTooltip()),
							false)));
			} else {
				panel.add(subField.createEditorWidgetForUse());
			}
			panel.add(Widgets.verticalSpacer());
		}
		return panel;
	}

	protected void initializeEditorPanel(VerticalPanel panel) {}

	public Widget createLabelWidget(AEditableField field) {
		String labelText = field.getLabel();
		if (labelText == null) labelText = "";

		String html = "<strong>" + Str.toHtml(labelText) + "</strong>";

		String tooltip = field.getTooltip();
		if (!Str.isBlank(tooltip)) html += " - " + Str.toHtml(tooltip);

		HTML widget = new HTML(html);
		Style style = widget.getElement().getStyle();
		style.setFontSize(65, Unit.PCT);
		style.setColor("#999999");
		return widget;
	}

	@Override
	public void trySubmit() throws RuntimeException {
		List<String> errors = new ArrayList<String>();
		for (AEditableField subField : subFields) {
			if (subField.getEditVetoMessage() != null) continue;
			try {
				subField.trySubmit();
			} catch (Exception ex) {
				errors.add(subField.getLabel() + ": " + Str.formatException(ex));
			}
		}
		if (errors.isEmpty()) return;
		throw new RuntimeException(Str.concat(errors, "\n"));
	}

	@Override
	public boolean isSelfdocEnabled() {
		return false;
	}

}
