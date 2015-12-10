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

import ilarkesto.core.base.OperationObserver;
import ilarkesto.io.IO;
import ilarkesto.net.Http;
import ilarkesto.net.httpclientx.HttpException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

// http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
public class HttpRequest {

	public static enum Method {
		GET, POST
	}

	private HttpSession session = HttpSession.defaultSession;

	private Method method = Method.GET;
	private String charset = IO.UTF_8;
	private String url;

	private Map<String, String> postParameters;

	private OperationObserver operationObserver;

	public HttpRequest(String url) {
		super();
		this.url = url;
	}

	public HttpResponse execute() throws IOException {
		if (operationObserver != null) operationObserver.onOperationInfoChanged(OperationObserver.DOWNLOADING, url);

		URL javaUrl;
		try {
			javaUrl = new URL(url);
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Malformed URL: " + url, ex);
		}

		HttpURLConnection connection = (HttpURLConnection) javaUrl.openConnection();

		try {
			writeRequest(connection);
		} finally {
			if (connection != null) connection.disconnect();
		}

		return new HttpResponse(session, connection);
	}

	private void writeRequest(HttpURLConnection connection) throws IOException {

		try {
			connection.setRequestMethod(method.name());
		} catch (ProtocolException ex) {
			throw new HttpException("Setting request method to " + method.name() + " failed.", ex);
		}

		switch (method) {
			case GET:
				writeGET(connection);
				return;
			case POST:
				writePOST(connection);
				return;
		}
	}

	private void writeGET(HttpURLConnection connection) {
		connection.setRequestProperty(Http.REQUEST_HEADER_ACCEPT_CHARSET, charset);
		writeCookies(connection);
	}

	private void writePOST(HttpURLConnection connection) throws IOException {
		connection.setDoOutput(true);
		connection.setRequestProperty(Http.REQUEST_HEADER_ACCEPT_CHARSET, charset);
		writeCookies(connection);

		byte[] data = null;

		if (postParameters != null) {
			connection.addRequestProperty(Http.REQUEST_HEADER_CONTENT_TYPE,
				"application/x-www-form-urlencoded;charset=" + charset);
			data = Http.encodePostParametersToByteArray(postParameters, charset);
		}

		if (data != null) {
			connection.addRequestProperty(Http.REQUEST_HEADER_CONTENT_LENGTH, String.valueOf(data.length));
			connection.getOutputStream().write(data);
		}
	}

	private void writeCookies(HttpURLConnection connection) {
		for (HttpCookie cookie : session.getCookies()) {
			connection.addRequestProperty(Http.REQUEST_HEADER_COOKIE, cookie.getName() + "=" + cookie.getValue());
		}
	}

	public HttpRequest setPostParameter(String name, String value) {
		setMethod(Method.POST);
		if (postParameters == null) postParameters = new LinkedHashMap<String, String>();
		if (value == null) {
			postParameters.remove(value);
		} else {
			postParameters.put(name, value);
		}
		return this;
	}

	public HttpRequest setMethod(Method method) {
		this.method = method;
		return this;
	}

	@Override
	public String toString() {
		return method + " " + url;
	}

}
