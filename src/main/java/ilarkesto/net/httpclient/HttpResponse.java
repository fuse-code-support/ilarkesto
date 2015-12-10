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

import ilarkesto.io.IO;
import ilarkesto.net.Http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
public class HttpResponse {

	private HttpSession session;
	private HttpURLConnection connection;

	private int responseCode;
	private Map<String, HttpHeader> headers;
	private String charset = IO.UTF_8;

	HttpResponse(HttpSession session, HttpURLConnection connection) throws IOException {
		this.session = session;
		this.connection = connection;

		responseCode = connection.getResponseCode();

		headers = new LinkedHashMap<String, HttpHeader>();
		for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
			String name = header.getKey();
			List<String> value = header.getValue();
			headers.put(name, new HttpHeader(name, value));
		}

		processCookies();
		determineCharset();
	}

	public String readToString() {
		try {
			return IO.readToString(connection.getInputStream(), charset);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			close();
		}
	}

	public void close() {
		if (connection == null) return;
		connection.disconnect();
		connection = null;
	}

	private void determineCharset() {
		String contentType = getHeaderValue(Http.RESPONSE_HEADER_CONTENT_TYPE);
		if (contentType == null) return;
		for (String param : contentType.replace(" ", "").split(";")) {
			if (param.startsWith("charset=")) {
				charset = param.split("=", 2)[1];
				break;
			}
		}
	}

	private void processCookies() {
		List<String> cookies = getHeaderValues(Http.RESPONSE_HEADER_SET_COOKIE);
		if (cookies == null || cookies.isEmpty()) return;
		for (String cookie : cookies) {
			session.addCookie(new HttpCookie(cookie));
		}
	}

	public String getHeaderValue(String name) {
		HttpHeader header = getHeader(name);
		if (header == null) return null;
		return header.getFirstValue();
	}

	public List<String> getHeaderValues(String name) {
		HttpHeader header = getHeader(name);
		if (header == null) return null;
		return header.getValue();
	}

	public HttpHeader getHeader(String name) {
		return headers.get(name);
	}

	public int getResponseCode() {
		return responseCode;
	}

	public boolean isResponseCodeOk() {
		return responseCode == Http.RESPONSE_CODE_OK;
	}

}
