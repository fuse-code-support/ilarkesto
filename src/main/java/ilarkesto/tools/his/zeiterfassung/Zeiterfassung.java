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
		parse(new File(Sys.getUsersHomePath() + "/inbox/his/zeiterfassung/zeiterfassung.txt"));
		Collections.sort(days);
		determineAchievoIds();
		writeCsv(new File(Sys.getUsersHomePath() + "/inbox/his/zeiterfassung/zeiterfassung.csv"));
		printForHiszilla();
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
			System.out.println(minutes + " | " + hiszillaId + " | https://hiszilla.his.de/hiszilla/show_bug.cgi?id="
					+ hiszillaId);
		}
	}

	private static void writeCsv(File file) throws IOException {
		FileWriter out = new FileWriter(file);
		CsvWriter csv = new CsvWriter(out);
		int currentMonth = 0;
		for (DayAtWork day : days) {
			if (currentMonth != day.getDate().getMonth()) {
				currentMonth = day.getDate().getMonth();
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

	private static void determineAchievoIds() {
		for (DayAtWork day : days) {
			for (WorkActivity activity : day.getActivities()) {
				if (!activity.isBookingRequired()) continue;
				if (activity.getAchievoId() != null) continue;
				String hiszillaId = activity.getHiszillaId();
				if (hiszillaId != null) {
					activity.setAchievoId(determineAchievoIdByHiszillaId(hiszillaId, activity.getText()));
				} else {
					activity.setAchievoId(determineAchievoIdByText(activity.getText()));
				}
			}
		}
	}

	private static String determineAchievoIdByText(String text) {
		if ("Product Backlog Refinement".equals(text)) return "22804";
		for (DayAtWork day : days) {
			for (WorkActivity activity : day.getActivities()) {
				if (activity.getAchievoId() == null) continue;
				if (Str.isBlank(activity.getText())) continue;
				if (text.equals(activity.getText())) return activity.getAchievoId();
			}
		}
		System.out.println("Achievo-ID fehlt. Text: " + text);
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
		System.out.println("Achievo-ID fehlt. Hiszilla: " + hiszillaId + " Text: " + text);
		return null;
	}

	private static void parse(File file) throws IOException, ParseException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = in.readLine()) != null) {
			if (Str.isBlank(line)) continue;
			if (line.startsWith("---")) {
				hiszillaLineReached = true;
				continue;
			}
			if (line.startsWith("* ")) {
				currentDay.addActivity(line).setAlreadyBookedInHiszilla(hiszillaLineReached);
				continue;
			}
			currentDay = new DayAtWork(line);
			days.add(currentDay);
		}
		in.close();
	}

}
