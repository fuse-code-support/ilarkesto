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

import ilarkesto.base.Sys;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.base.Str;
import ilarkesto.core.localization.Localizer;
import ilarkesto.core.time.Date;
import ilarkesto.io.CsvWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Zeiterfassung {

	private static List<DayAtWork> days = new ArrayList<DayAtWork>();
	private static DayAtWork currentDay;
	private static boolean hiszillaLineReached;

	public static void main(String[] args) throws Exception {
		Localizer.setCurrent(Localizer.DE);
		parse(new File(Sys.getUsersHomePath() + "/myfiles/his/zeiterfassung/zeiterfassung.txt"));
		Collections.sort(days);
		determineAchievoIds();
		writeCsv(new File(Sys.getUsersHomePath() + "/myfiles/his/zeiterfassung/zeiterfassung.csv"));
		System.out.println("-------------------------------------------------------------------------------");
		printForOrganizanto();
		System.out.println("-------------------------------------------------------------------------------");
		printForHiszilla();
	}

	private static void printForOrganizanto() {
		for (DayAtWork day : days) {
			if (day.isAlreadyBookedInHiszilla()) continue;
			Date date = day.getDate();
			System.out.println(date.getWeekday().toShortString("en") + ", " + date.formatDayMonthYear() + " | "
					+ day.getStart().format() + " - " + day.getEnd().format() + " | "
					+ day.getWorkTime().toHoursAndMinutes() + " | " + day.getPauseTime().toMinutes() + " | "
					+ day.getActivitiesText());
		}
	}

	private static void printForHiszilla() {
		Map<String, Long> minutesByHiszillaId = new HashMap<String, Long>();

		for (DayAtWork day : days) {
			for (WorkActivity activity : day.getActivities()) {
				if (activity.isAlreadyBookedInHiszilla()) continue;
				String hiszillaId = activity.getHiszillaId();
				if (hiszillaId == null) continue;
				long time = activity.getWorktimeInMinutes();
				Long total = minutesByHiszillaId.get(hiszillaId);
				if (total == null) {
					total = time;
				} else {
					total = total.longValue() + time;
				}
				minutesByHiszillaId.put(hiszillaId, total);
			}
		}

		for (Entry<String, Long> entry : minutesByHiszillaId.entrySet()) {
			Long minutes = entry.getValue();
			String hiszillaId = entry.getKey();
			String time = String.valueOf(minutes.floatValue() / 60f);
			System.out.println(
				time + " | " + hiszillaId + " | https://hiszilla.his.de/hiszilla/show_bug.cgi?id=" + hiszillaId);
		}
	}

	private static void writeCsv(File file) throws IOException {
		FileWriter out = new FileWriter(file);
		CsvWriter csv = new CsvWriter(out);
		int currentWeek = 0;
		for (DayAtWork day : days) {
			Date date = day.getDate();
			if (!isPreviousMonth(date)) continue;

			if (currentWeek != date.getWeek()) {
				currentWeek = date.getWeek();
				csv.closeRecord();
				csv.closeRecord();
			}
			for (WorkActivity activity : day.getActivities()) {
				if (!activity.isBookingRequired()) continue;
				activity.appendTo(csv);
			}
		}
		out.close();
	}

	private static boolean isPreviousMonth(Date date) {
		if (Date.today().getFirstDateOfMonth().equals(date.getFirstDateOfMonth())) return true;
		if (Date.today().getFirstDateOfMonth().addDays(-1).getFirstDateOfMonth().equals(date.getFirstDateOfMonth()))
			return true;
		return false;
	}

	private static void determineAchievoIds() {
		for (DayAtWork day : days) {
			for (WorkActivity activity : day.getActivities()) {
				if (!activity.isBookingRequired()) continue;
				activity.setAchievoPackageId("10282");
				activity.setAchievoProjectId("5461");

				if (activity.getAchievoId() != null) continue;
				String hiszillaId = activity.getHiszillaId();
				if (hiszillaId != null) {
					activity.setAchievoId(determineAchievoIdByHiszillaId(hiszillaId, activity.getText()));
				} else {
					String id = determineAchievoIdByText(activity.getText());
					activity.setAchievoId(id);
				}
			}
		}
	}

	private static String determineAchievoIdByText(String text) {
		// if ("Sprint Review".equals(text)) return "27683";
		// if ("Integrationstest".equals(text)) return "27684";
		// if ("Product Backlog Refinement".equals(text)) return "27817";
		// for (DayAtWork day : days) {
		// for (WorkActivity activity : day.getActivities()) {
		// if (activity.getAchievoId() == null) continue;
		// if (Str.isBlank(activity.getText())) continue;
		// if (text.equals(activity.getText())) return activity.getAchievoId();
		// }
		// }
		if (!hiszillaLineReached) System.out.println("Achievo-ID fehlt. Text: " + text);
		return null;
	}

	private static String determineAchievoIdByHiszillaId(String hiszillaId, String text) {
		if (hiszillaId == null) return null;
		for (DayAtWork day : days) {
			for (WorkActivity activity : day.getActivities()) {
				if (activity.getAchievoId() == null) continue;
				if (hiszillaId.equals(activity.getHiszillaId())) return activity.getAchievoId();
			}
		}
		if (!hiszillaLineReached) System.out.println("Achievo-ID fehlt. Hiszilla: " + hiszillaId + " Text: " + text);
		return null;
	}

	private static void parse(File file) throws IOException, ParseException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = null;
		int lineCount = 0;
		while ((line = in.readLine()) != null) {
			lineCount++;
			if (Str.isBlank(line)) continue;
			line = line.trim();
			if (line.startsWith("//")) continue;
			if (line.startsWith("#")) continue;
			if (line.startsWith("---")) {
				hiszillaLineReached = true;
				continue;
			}
			try {
				if (line.startsWith("* ")) {
					currentDay.addActivity(line).setAlreadyBookedInHiszilla(hiszillaLineReached);
					continue;
				}
				currentDay = new DayAtWork(line);
				days.add(currentDay);
			} catch (Exception ex) {
				throw new RuntimeException("Parsing failed at line " + lineCount, ex);
			}
		}
		in.close();
	}

}
