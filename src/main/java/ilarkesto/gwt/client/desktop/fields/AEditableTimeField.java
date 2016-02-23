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

import ilarkesto.core.time.Time;

public abstract class AEditableTimeField extends AEditableTextBoxField<Time> {

	@Override
	public Time prepareValue(String value) {
		if (value == null) return null;
		value = value.trim();
		if (value.isEmpty()) return null;

		try {
			return new Time(value);
		} catch (Exception ex) {
			throw new RuntimeException("Eingabe muß eine Uhrzeit sein. HH:MM, z.B. 10:30");
		}
	}

	@Override
	protected int getMaxLength() {
		return 10;
	}

	public final Time getValueAsTime() {
		return prepareValue(getValue());
	}

}
