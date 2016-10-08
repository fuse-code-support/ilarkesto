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

public abstract class AEditableTextField extends AEditableTextBoxField<String> {

	private boolean valueIsHref;

	@Override
	protected String prepareValue(String text) {
		// if (valueIsHref) return Str.toUrl(text);
		return text;
	}

	@Override
	public void validateValue(String value) throws RuntimeException {
		super.validateValue(value);
		if (valueIsHref && value != null) {
			if (!value.startsWith("http://") && !value.startsWith("https://"))
				throw new RuntimeException("Invalid URL (missing http:// or https://)");
		}
	}

	@Override
	protected String getHref() {
		if (valueIsHref) return Str.toUrl(getValue());
		return super.getHref();
	}

	public AEditableTextField setValueIsHref(boolean valueIsHref) {
		this.valueIsHref = valueIsHref;
		return this;
	}

	@Override
	protected String getDisplayValue() {
		if (valueIsHref) return Str.formatUrlAsLink(super.getDisplayValue(), 42);
		return super.getDisplayValue();
	}
}
