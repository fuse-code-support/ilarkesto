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
import ilarkesto.core.localization.Localizer;
import ilarkesto.core.money.Money;
import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

public class OutputField extends AField {

	private String label;
	private Object value;
	private String suffix;
	private String href;
	private boolean mandatory = false;
	private String color;
	private Boolean alignRight;

	public OutputField(String label, Object value) {
		super();
		this.label = label;
		this.value = value;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public OutputField setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
		return this;
	}

	public OutputField setValue(Object value) {
		this.value = value;
		return this;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public IsWidget createDisplayWidget() {
		if (value == null) return Widgets.widget(value);

		String text = valueAsString();
		if (suffix != null) text += " " + suffix;

		Label ret = Widgets.text(text);
		Style style = ret.getElement().getStyle();
		if (color != null) style.setColor(color);
		if (isAlignRight()) style.setTextAlign(com.google.gwt.dom.client.Style.TextAlign.RIGHT);
		return ret;
	}

	protected boolean isAlignRight() {
		if (alignRight != null) return alignRight.booleanValue();

		if (value instanceof Money) return true;
		if (value instanceof Number) return true;
		return false;
	}

	@Override
	protected boolean isLabelAlignRight() {
		return isAlignRight();
	}

	protected String valueAsString() {
		if (value instanceof Number) return Localizer.get().format((Number) value, true, 2, true);
		return Str.format(value);
	}

	@Override
	protected String getHref() {
		if (href != null) return href;
		if (value == null) return null;
		if (value.toString().startsWith("http://")) return value.toString();
		return null;
	}

	public OutputField setSuffix(String suffix) {
		this.suffix = suffix;
		return this;
	}

	public OutputField setHref(String href) {
		this.href = href;
		return this;
	}

	public OutputField setColor(String color) {
		this.color = color;
		return this;
	}

	public OutputField setAlignRight(Boolean alignRight) {
		this.alignRight = alignRight;
		return this;
	}

}
