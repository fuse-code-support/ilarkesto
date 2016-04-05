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
package ilarkesto.icalendar;

import ilarkesto.core.base.Utl;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ICal {

	private String prodId;
	private String version;
	private String calscale;
	private String method;

	// X-WR-CALNAME:Rinteln-
	// App
	// X-WR-TIMEZONE:Europe/
	// Berlin
	// X-WR-CALDESC:
	// Veranstaltungen in
	//
	// Rinteln (für die Anzeige in der Rinteln-App
	// )

	private ICal() {}

	public static ICal create() {
		ICal cal = new ICal();
		cal.setProdId("-//Witoslaw Koczewski//Ilarkesto");
		cal.setVersion("2.0");
		cal.setCalscale("GREGORIAN");
		cal.setMethod("PUBLISH");
		return cal;
	}

	private static class Parser {

		ICal cal;

		public ICal parse(BufferedReader in) {
			cal = new ICal();

			Field field;
			while ((field = readField(in)) != null) {
				adopt(field);
			}

			return cal;
		}

		private Field readField(BufferedReader in) {
			return null;
		}

		private void adopt(Field field) {
			if (field.isBegin("VCALENDAR")) return;
		}

	}

	public void write(PrintWriter out) {
		new Field("BEGIN", "VCALENDAR").write(out);

		new Field("PRODID", prodId).write(out);
		new Field("VERSION", version).write(out);
		new Field("CALSCALE", calscale).write(out);
		new Field("METHOD", method).write(out);

		new Field("END", "VCALENDAR").write(out);
	}

	public static ICal parse(BufferedReader in) {
		return new Parser().parse(in);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getProdId() {
		return prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCalscale() {
		return calscale;
	}

	public void setCalscale(String calscale) {
		this.calscale = calscale;
	}

	private class Field {

		private String name;
		private String value;

		public Field(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public boolean isBegin(String section) {
			return "BEGIN".equals(name) && Utl.equals(value, section);
		}

		public boolean isEnd(String section) {
			return "END".equals(name) && Utl.equals(value, section);
		}

		public void write(PrintWriter out) {
			out.print(name);
			out.print(':');

			// TODO multiline
			if (value != null) out.print(value);

			out.println();
		}

	}

}
