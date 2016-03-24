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
package ilarkesto.net.httpclient;

import ilarkesto.core.logging.Log;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HttpSession {

	private static Log log = Log.get(HttpSession.class);

	public static HttpCache defaultCache;

	static HttpSession defaultSession = new HttpSession();
	static boolean debug = false;

	private final Map<String, HttpCookie> cookies = new HashMap<String, HttpCookie>();
	private final int redirectCount = 3;

	private String charset;
	HttpCache cache = defaultCache;

	public void addCookie(HttpCookie cookie) {
		String name = cookie.getName();
		boolean replace = cookies.containsKey(name);
		cookies.put(name, cookie);
		log.debug("Cookie", replace ? "replaced" : "added", ">", cookie);
	}

	public Collection<HttpCookie> getCookies() {
		return cookies.values();
	}

	public HttpRequest request(String url) {
		HttpRequest request = new HttpRequest(url);
		request.setSession(this);
		if (charset != null) request.setCharset(charset);
		return request;
	}

	public String getCookieValue(String name) {
		HttpCookie cookie = getCookie(name);
		if (cookie == null) return null;
		return cookie.getValue();
	}

	public HttpCookie getCookie(String name) {
		return cookies.get(name);
	}

	public HttpCache getCache() {
		return cache;
	}

	public void setCache(HttpCache cache) {
		this.cache = cache;
	}

	// --- helper ---

	public String downloadText(String url) {
		return request(url).execute().followRedirects(redirectCount).checkIfStatusCodeOkish().readToString();
	}

	public void downloadToFile(String url, File file) {
		request(url).execute().followRedirects(redirectCount).checkIfStatusCodeOkish().saveToFile(file);
	}

	public String postAndDownloadText(String url, Map<String, String> params) {
		return request(url).setPostParameters(params).execute().followRedirects(redirectCount).checkIfStatusCodeOkish()
				.readToString();
	}

	public String postAndDownloadText(String url, Map<String, String> params, File file) {
		return request(url).setPostParameters(params).addPostAttachment("file", file).execute()
				.followRedirects(redirectCount).checkIfStatusCodeOkish().readToString();
	}

	public HttpSession setCharset(String charset) {
		this.charset = charset;
		return this;
	}

}
