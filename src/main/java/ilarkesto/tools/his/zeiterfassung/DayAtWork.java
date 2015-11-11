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

import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.time.Date;

import java.util.LinkedList;
import java.util.List;

public class DayAtWork implements Comparable<DayAtWork> {

	private Date date;
	private LinkedList<WorkActivity> activities = new LinkedList<WorkActivity>();

	public DayAtWork(String line) {
		date = new Date(line.substring(0, line.indexOf(" ")));
	}

	public WorkActivity getLastActivity() {
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

}
