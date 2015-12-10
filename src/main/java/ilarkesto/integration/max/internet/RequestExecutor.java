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
package ilarkesto.integration.max.internet;

import ilarkesto.net.httpclient.HttpResponse;
import ilarkesto.net.httpclient.HttpSession;

import java.util.Map;

public class RequestExecutor {

	private HttpSession httpSession = new HttpSession();

	public String postAndGetContent(String url, Map<String, String> parameters) {
		HttpResponse response = post(url, parameters);
		return getContent(response);
	}

	public ilarkesto.net.httpclient.HttpResponse post(String url, Map<String, String> parameters) {
		ilarkesto.net.httpclient.HttpResponse r = httpSession.request(url).setPostParameters(parameters).execute();
		if (!r.isResponseCodeOk()) throw new RuntimeException("HTTP POST failed: " + r.readToString());
		return r;
	}

	public String get(String url) {
		HttpResponse response = httpSession.request(url).execute();
		if (!response.isResponseCodeOk())
			throw new RuntimeException("HTTP GET failed with HTTP Code " + response.getResponseCode());
		return getContent(response);
	}

	private String getCookieValue(String name) {
		return httpSession.getCookieValue(name);
	}

	public String getSessionId() {
		return getCookieValue("JSESSIONID");
	}

	private String getContent(HttpResponse response) {
		return response.readToString();
	}

}
