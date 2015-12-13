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
import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

public class OutputField extends AField {

	private String label;
	private Object value;
	private String suffix;
	private String href;
	private boolean mandatory = false;
	private String color;

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

		String text = Str.format(value);
		if (suffix != null) text += " " + suffix;
		Label ret = Widgets.text(text);
		if (color != null) ret.getElement().getStyle().setColor(color);
		return ret;
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

}
