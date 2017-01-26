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

import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;
import ilarkesto.net.Http;
import ilarkesto.net.httpclient.HttpRequest.Method;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
public class HttpResponse {

	private static final Log log = Log.get(HttpResponse.class);

	final HttpRequest request;
	private HttpURLConnection connection;
	private File cacheFile;
	private InputStream cacheInputStream;

	private int statusCode;
	private Map<String, HttpResponseHeader> headers;
	private String charset = IO.UTF_8;

	HttpResponse(HttpRequest request, int statusCode, HttpURLConnection connection) throws IOException {
		this.request = request;
		this.statusCode = statusCode;
		this.connection = connection;

		processHeaders();
		processCookies();
		determineCharset();

		if (request.session.cache != null && request.method == Method.GET) {
			if (isStatusCodeNotModified()) {
				cacheFile = request.session.cache.getCachedFile(request.url);
			} else {
				cacheFile = request.session.cache.cache(this);
			}
			if (cacheFile != null && !cacheFile.exists()) cacheFile = null;
		}
	}

	public HttpResponse followRedirects(int maxFollowCount) {
		if (maxFollowCount < 1) return this;
		if (statusCode != Http.RESPONSE_SC_MOVED_PERMANENTLY && statusCode != Http.RESPONSE_SC_FOUND) return this;
		String location = getHeaderValue(Http.RESPONSE_HEADER_LOCATION);
		if (location == null)
			throw new RuntimeException("HTTP error. Status 301 received, but Location-Header missing.");
		log.debug("Following redirect:", location);
		return request.session.request(location).execute().followRedirects(maxFollowCount - 1);
	}

	public String readToString() {
		try {
			return IO.readToString(getInputStream(), charset);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			close();
		}
	}

	public void saveToFile(File file) {
		try {
			IO.copyDataToFile(getInputStream(), file);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			close();
		}
	}

	private InputStream getInputStream() throws IOException {
		if (cacheFile != null) {
			cacheInputStream = new BufferedInputStream(new FileInputStream(cacheFile));
			return cacheInputStream;
		}
		return getConnectionInputStream();
	}

	InputStream getConnectionInputStream() throws IOException {
		return connection.getInputStream();
	}

	public void close() {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
		if (cacheInputStream != null) {
			IO.closeQuiet(cacheInputStream);
			cacheInputStream = null;
		}
	}

	private void determineCharset() {
		String contentType = getHeaderValue(Http.RESPONSE_HEADER_CONTENT_TYPE);
		if (contentType == null) return;
		for (String param : contentType.replace(" ", "").split(";")) {
			if (param.startsWith("charset=")) {
				charset = param.split("=", 2)[1];
				charset = Str.removePrefix(charset, "\"");
				charset = Str.removeSuffix(charset, "\"");
				break;
			}
		}
	}

	private void processHeaders() {
		headers = new LinkedHashMap<String, HttpResponseHeader>();
		for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			String name = entry.getKey();
			List<String> value = entry.getValue();
			HttpResponseHeader header = new HttpResponseHeader(name, value);
			headers.put(name, header);
			if (HttpSession.debug) log.debug("  Received header <", header);
		}
	}

	private void processCookies() {
		List<String> cookies = getHeaderValues(Http.RESPONSE_HEADER_SET_COOKIE);
		if (cookies == null || cookies.isEmpty()) return;
		for (String cookie : cookies) {
			if (Str.isBlank(cookie)) continue;
			request.getSession().addCookie(new HttpCookie(cookie));
		}
	}

	public String getHeaderValue(String name) {
		HttpResponseHeader header = getHeader(name);
		if (header == null) return null;
		return header.getFirstValue();
	}

	public List<String> getHeaderValues(String name) {
		HttpResponseHeader header = getHeader(name);
		if (header == null) return null;
		return header.getValue();
	}

	public HttpResponseHeader getHeader(String name) {
		return headers.get(name);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public boolean isStatusCodeOkOrNotModified() {
		return isStatusCodeOk() || isStatusCodeNotModified();
	}

	public boolean isStatusCodeOk() {
		return statusCode == Http.RESPONSE_SC_OK;
	}

	public boolean isStatusCodeNotModified() {
		return statusCode == Http.RESPONSE_SC_NOT_MODIFIED;
	}

	public HttpResponse checkIfStatusCodeOkish() {
		if (!isStatusCodeOkOrNotModified())
			throw new RuntimeException("HTTP " + request + " failed. StatusCode: " + statusCode);
		return this;
	}

	public HttpResponse setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	public long getContentLength() {
		String hcl = getHeaderValue(Http.REQUEST_HEADER_CONTENT_LENGTH);
		if (hcl == null) return -1;
		try {
			return Long.parseLong(hcl);
		} catch (Exception ex) {
			return -1;
		}
	}

}
