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
package ilarkesto.core.persistance;

import ilarkesto.core.base.Str;

public class AStringValueField extends AValueField<String> {

	private String value;

	@Override
	public final void setValue(String value) {
		value = prepareValue(value);
		if (isValue(value)) return;
		this.value = value;
		onValueChanged(value);
	}

	protected void onValueChanged(String value) {}

	protected String prepareValue(String value) {
		return value;
	}

	@Override
	public final String getValue() {
		return getValue();
	}

	@Override
	public final boolean isValueSet() {
		return value != null;
	}

	public final boolean isBlank() {
		return Str.isBlank(value);
	}

	public final int getLength() {
		return value == null ? 0 : value.length();
	}

	@Override
	public final boolean isValue(String testValue) {
		if (this.value == null && testValue == null) return true;
		return this.value != null && this.value.equals(testValue);
	}

	@Override
	public String toString() {
		return value;
	}

}
