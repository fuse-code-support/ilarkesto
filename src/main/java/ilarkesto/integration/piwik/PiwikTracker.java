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
package ilarkesto.integration.piwik;

import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.Time;
import ilarkesto.io.IO;
import ilarkesto.net.HttpDownloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PiwikTracker {

	private String serverUrl;
	private int idsite;
	private String clientUrl;

	private int rec = 1;
	private String apiv = "1";

	private String userAgent = "Ilarkesto/1";
	private boolean dummyMode;

	private Queue<Request> requestQueue = new ConcurrentLinkedQueue<Request>();

	public PiwikTracker(String serverUrl, int idsite, String clientUrl) {
		super();
		this.serverUrl = serverUrl;
		this.idsite = idsite;
		this.clientUrl = clientUrl;
	}

	public void flush() {
		Request request;
		while ((request = requestQueue.peek()) != null) {
			post(request);
			requestQueue.remove(request);
		}
	}

	private void post(Request request) {
		if (dummyMode) return;
		String url = serverUrl + "/piwik.php";
		getHttp().post(url, request.getParameters(), IO.UTF_8);
	}

	private void queue(Request request) {
		requestQueue.add(request);
	}

	public Request request(String path) {
		return new Request(path);
	}

	public HttpDownloader getHttp() {
		return new HttpDownloader();
	}

	public PiwikTracker setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public PiwikTracker setDummyMode(boolean dummyMode) {
		this.dummyMode = dummyMode;
		return this;
	}

	public class Request {

		private Map<String, String> parameters = new HashMap<String, String>();

		public Request(String path) {
			put("idsite", idsite);
			put("rec", rec);
			put("url", clientUrl + "/" + path);
			put("apiv", apiv);
			put("ua", userAgent);

			Time time = DateAndTime.now().getTime();
			put("h", time.getHour());
			put("m", time.getMinute());
			put("s", time.getSecond());
		}

		/**
		 * @param category The event category. Must not be empty. (eg. Videos, Music, Games...)
		 * @param action The event action. Must not be empty. (eg. Play, Pause, Duration, Add Playlist,
		 *            Downloaded, Clicked...)
		 * @param name The event name. (eg. a Movie name, or Song name, or File name...)
		 * @param value The event value. Must be a float or integer value (numeric), not a string.
		 */
		public Request event(String category, String action, String name, float value) {
			return put("e_c", category).put("e_a", action).put("e_n", name).put("e_v", value);
		}

		/**
		 * An override value for the User-Agent HTTP header field. The user agent is used to detect the
		 * operating system and browser used.
		 */
		public Request ua(String value) {
			return put("ua", value);
		}

		/**
		 * The amount of time it took the server to generate this action, in milliseconds. This value is used
		 * to process the Page speed report Avg. generation time column in the Page URL and Page Title
		 * reports, as well as a site wide running average of the speed of your server. Note: when using the
		 * Javascript tracker this value is set to the ime for server to generate response + the time for
		 * client to download response.
		 */
		public Request gtMs(long value) {
			return put("gt_ms", String.valueOf(value));
		}

		/**
		 * defines the User ID for this request. User ID is any non empty unique string identifying the user
		 * (such as an email address or a username). To access this value, users must be logged-in in your
		 * system so you can fetch this user ID from your system, and pass it to Piwik. The User ID appears in
		 * the visitor log, the Visitor profile, and you can Segment reports for one or several User ID
		 * (userId segment). When specified, the User ID will be "enforced". This means that if there is no
		 * recent visit with this User ID, a new one will be created. If a visit is found in the last 30
		 * minutes with your specified User ID, then the new action will be recorded to this existing visit.
		 */
		public Request uid(String value) {
			return put("uid", value);
		}

		/**
		 * The title of the action being tracked. It is possible to use slashes / to set one or several
		 * categories for this action. For example, Help / Feedback will create the Action Feedback in the
		 * category Help.
		 */
		public Request actionName(String value) {
			return put("action_name", value);
		}

		private Request put(String actionName, long value) {
			parameters.put(actionName, String.valueOf(value));
			return this;
		}

		private Request put(String actionName, double value) {
			parameters.put(actionName, String.valueOf(value));
			return this;
		}

		private Request put(String actionName, String value) {
			if (value == null) return this;
			parameters.put(actionName, value);
			return this;
		}

		public void post() {
			PiwikTracker.this.post(this);
		}

		public void queue() {
			PiwikTracker.this.queue(this);
		}

		public Map<String, String> getParameters() {
			return parameters;
		}

	}

}
