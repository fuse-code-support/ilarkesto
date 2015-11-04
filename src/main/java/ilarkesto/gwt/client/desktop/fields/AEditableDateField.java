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

import ilarkesto.core.time.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public abstract class AEditableDateField extends AEditableTextBoxField<Date> {

	public static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");

	@Override
	public Date prepareValue(String value) {
		if (value == null) return null;
		value = value.trim();
		if (value.isEmpty()) return null;

		java.util.Date javaDate;
		try {
			javaDate = format.parse(value);
		} catch (Exception ex) {
			throw new RuntimeException("Eingabe muß ein Datum sein. TT.MM.JJJJ, z.B. 01.01.2001");
		}
		Date date = new Date(javaDate);
		if (date.getYear() > 9999)
			throw new RuntimeException("Eingabe muß ein Datum sein. TT.MM.JJJJ, z.B. 01.01.2001");
		if (date.getYear() < 100) date = date.addYears(2000);
		if (date.getYear() < 1000)
			throw new RuntimeException("Eingabe muß ein Datum sein. TT.MM.JJJJ, z.B. 01.01.2001");
		return date;
	}

	@Override
	public Format getFormat() {
		return new DateBox.DefaultFormat(format);
	}

	@Override
	protected int getMaxLength() {
		return 10;
	}

	public final Date getValueAsDate() {
		return prepareValue(getValue());
	}

}
