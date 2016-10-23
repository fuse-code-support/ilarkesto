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

import ilarkesto.base.Str;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.Time;
import ilarkesto.core.time.TimePeriod;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DayAtWork implements Comparable<DayAtWork> {

	private Date date;
	private LinkedList<WorkActivity> activities = new LinkedList<WorkActivity>();

	public DayAtWork(String line) {
		date = new Date(line.substring(0, line.indexOf(" ")));
	}

	public TimePeriod getWorkTime() {
		TimePeriod ret = new TimePeriod();
		for (WorkActivity activity : activities) {
			ret = ret.add(activity.getWorktime());
		}
		return ret;
	}

	public TimePeriod getTotaltime() {
		return getStart().getPeriodTo(getEnd());
	}

	public Time getStart() {
		if (activities.isEmpty()) throw new IllegalStateException("No activities: " + date.format());
		Time firstStart = null;
		for (WorkActivity activity : activities) {
			Time start = activity.getStart();
			if (firstStart == null || start.isBefore(firstStart)) firstStart = start;
		}
		return firstStart;
	}

	public Time getEnd() {
		Time lastEnd = null;
		for (WorkActivity activity : activities) {
			Time end = activity.getEnd();
			if (lastEnd == null || end.isAfter(lastEnd)) lastEnd = end;
		}
		return lastEnd;
	}

	public TimePeriod getPauseTime() {
		return getTotaltime().subtract(getWorkTime());
	}

	public WorkActivity getLastActivity() {
		if (activities.isEmpty()) return null;
		return activities.getLast();
	}

	public WorkActivity addActivity(String line) throws ParseException {
		WorkActivity activity = new WorkActivity(this, line);
		activities.add(activity);
		return activity;
	}

	public List<WorkActivity> getActivities() {
		return activities;
	}

	@Override
	public int compareTo(DayAtWork o) {
		return date.compareTo(o.date);
	}

	public Date getDate() {
		return date;
	}

	public boolean isAlreadyBookedInHiszilla() {
		for (WorkActivity activity : activities) {
			if (activity.isAlreadyBookedInHiszilla()) return true;
		}
		return false;
	}

	private Set<String> getActivitesTexts() {
		LinkedHashSet<String> ret = new LinkedHashSet<String>();
		for (WorkActivity activity : activities) {
			ret.add(activity.getText());
		}
		return ret;
	}

	public String getActivitiesText() {
		Set<String> texts = getActivitesTexts();
		texts.remove("Daily Scrum");
		// texts.remove("Sprint Review");
		return Str.concat(texts, " | ");
	}

}
