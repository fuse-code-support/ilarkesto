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
package ilarkesto.integration.organizanto;

import ilarkesto.base.Sys;
import ilarkesto.concurrent.ATask;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.net.httpclient.HttpSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizantoTracker extends ATask {

	private final Log log = Log.get(getClass());

	private String propertyKey;

	private HttpSession http = new HttpSession();
	private List<Event> events = Collections.synchronizedList(new ArrayList<Event>());

	public OrganizantoTracker(String propertyKey) {
		this.propertyKey = propertyKey;
	}

	public void trackValue(String name, String value) {
		track(name, value, null, null, false);
	}

	public void track(String name, String value, String message, String info, boolean alert) {
		if (Sys.isDevelopmentMode()) return;

		Event event = new Event(DateAndTime.now(), name, value, message, info, alert);

		events.add(event);
	}

	@Override
	protected void perform() throws InterruptedException {
		flush();
	}

	public void flush() {
		try {
			for (Event event : new ArrayList<Event>(events)) {
				postEvent(event);
			}
		} catch (Exception ex) {
			log.info("Transmitting events failed:", ex);
		}
	}

	private synchronized void postEvent(Event event) {
		String url = Organizanto.URL_SERVICES + "track";
		Map<String, String> params = new HashMap<String, String>();
		params.put("propertyKey", propertyKey);
		params.put("time", event.time.toString());
		params.put("name", event.name);
		params.put("value", event.value);
		params.put("message", event.message);
		params.put("info", event.info);
		params.put("alert", String.valueOf(event.alert));
		http.postAndDownloadText(url, params);
		events.remove(event);
		log.info("Event transmitted:", event);
	}

	public static class Event {

		private DateAndTime time;
		private String name;
		private String value;
		private String message;
		private String info;
		private boolean alert;

		public Event(DateAndTime time, String name, String value, String message, String info, boolean alert) {
			super();
			this.time = time;
			this.name = name;
			this.value = value;
			this.message = message;
			this.info = info;
			this.alert = alert;
		}

		@Override
		public String toString() {
			return name + " " + value + " " + message;
		}
	}

}
