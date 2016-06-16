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
package ilarkesto.tools.his.zeiterfassung;

import ilarkesto.core.base.Parser;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.Time;
import ilarkesto.core.time.TimePeriod;
import ilarkesto.io.CsvWriter;

public class WorkActivity {

	private DayAtWork dayAtWork;
	private Time start;
	private Time end;
	private String hiszillaId;
	private String achievoId;
	private String text;
	private boolean bookingRequired = true;
	private boolean alreadyBookedInHiszilla;

	public WorkActivity(DayAtWork dayAtWork, String line) throws ParseException {
		this.dayAtWork = dayAtWork;
		Parser parser = new Parser(line);
		parser.gotoAfter("* ");
		start = new Time(parser.getUntilAndGotoAfter(" - "));
		end = new Time(parser.getUntilAndGotoAfter(" "));

		if (parser.getNext(1).startsWith("!")) {
			bookingRequired = false;
			parser.gotoAfter(" ");
		}

		if (parser.getNext(1).startsWith("%")) {
			WorkActivity lastActivity = dayAtWork.getLastActivity();
			hiszillaId = lastActivity.getHiszillaId();
			achievoId = lastActivity.getAchievoId();
			text = lastActivity.getText();
			return;
		}

		if (parser.getNext(1).startsWith("#")) {
			hiszillaId = parser.getUntilAndGotoAfter(" ").substring(1);
		}

		if (parser.getNext(1).startsWith("$")) {
			achievoId = parser.getUntilAndGotoAfter(" ").substring(1);
		}

		text = parser.getRemaining();
	}

	public boolean isBookingRequired() {
		return bookingRequired;
	}

	public String getText() {
		return text;
	}

	public String getHiszillaId() {
		return hiszillaId;
	}

	public String getAchievoId() {
		return achievoId;
	}

	public void setAchievoId(String achievoId) {
		this.achievoId = achievoId;
	}

	public Time getStart() {
		return start;
	}

	public Time getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return start + " - " + end + " | " + hiszillaId + " | " + achievoId + " | " + text;
	}

	public void appendTo(CsvWriter csv) {
		csv.writeField(""); // projectId
		csv.writeField(""); // package
		csv.writeField(achievoId); // phaseId
		Date date = dayAtWork.getDate();

		// csv.writeField(date.getMonth() + "/" + date.getDay() + "/" + date.getYear()); // activitydate
		csv.writeField(date.formatDayMonthYear());

		csv.writeField("788"); // userid
		csv.writeField(text); // remark
		csv.writeField(getWorktimeInMinutes()); // time
		csv.writeField("0"); // business_trip
		csv.writeField(hiszillaId); // hiszilla
		csv.writeField(""); // remaining_effort_time
		csv.writeField("");
		csv.writeField(date.getWeekday().format());
		csv.writeField(date.format());
		csv.closeRecord();
	}

	public long getWorktimeInMinutes() {
		return getWorktime().toMinutes();
	}

	public TimePeriod getWorktime() {
		return start.getPeriodTo(end);
	}

	public void setAlreadyBookedInHiszilla(boolean alreadyBookedInHiszilla) {
		this.alreadyBookedInHiszilla = alreadyBookedInHiszilla;
	}

	public boolean isAlreadyBookedInHiszilla() {
		return alreadyBookedInHiszilla;
	}
}
