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
import ilarkesto.core.base.Uuid;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;
import ilarkesto.net.Http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
public class HttpRequest {

	private static final Log log = Log.get(HttpRequest.class);

	public static enum Method {
		GET, POST
	}

	HttpSession session = HttpSession.defaultSession;

	private Method method = Method.GET;
	private String charset = IO.UTF_8;
	private String url;

	private Map<String, String> postParameters;
	private List<AHttpPostAttachment> postAttachments;
	private List<HttpRequestHeader> headers;

	private OperationObserver operationObserver;

	public HttpRequest(String url) {
		super();
		this.url = url;
	}

	public HttpResponse execute() {
		log.debug(this);
		if (operationObserver != null) operationObserver.onOperationInfoChanged(OperationObserver.DOWNLOADING, url);

		URL javaUrl;
		try {
			javaUrl = new URL(url);
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Malformed URL: " + url, ex);
		}

		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) javaUrl.openConnection();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		try {
			writeRequest(connection);
		} catch (IOException ex) {
			if (connection != null) connection.disconnect();
			throw new RuntimeException(ex);
		}

		int responseCode;
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException ex) {
			if (connection != null) connection.disconnect();
			throw new RuntimeException("HTTP request failed:", ex);
		}

		try {
			return new HttpResponse(this, responseCode, connection);
		} catch (IOException ex) {
			if (connection != null) connection.disconnect();
			throw new RuntimeException(ex);
		}
	}

	private void writeRequest(HttpURLConnection connection) throws IOException {

		try {
			connection.setRequestMethod(method.name());
		} catch (ProtocolException ex) {
			throw new RuntimeException("Setting request method to " + method.name() + " failed.", ex);
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
		writeHeaders(connection);
		writeCookies(connection);
	}

	private void writePOST(HttpURLConnection connection) throws IOException {
		connection.setDoOutput(true);
		connection.setRequestProperty(Http.REQUEST_HEADER_ACCEPT_CHARSET, charset);
		writeHeaders(connection);
		writeCookies(connection);

		if (postAttachments == null) {
			if (postParameters != null) {
				writePostParametersAsFormUrlencoded(connection);
			}
		} else {
			writePostAttachments(connection);
		}

	}

	private static final String CRLF = "\r\n";

	private void writePostAttachments(HttpURLConnection connection) throws IOException {
		String boundary = Uuid.create();

		writeHeader(connection, Http.REQUEST_HEADER_CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);

		OutputStream out = connection.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, charset), true);

		if (postParameters != null) {
			for (Map.Entry<String, String> entry : postParameters.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				writer.append("--" + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
				writer.append(CRLF).append(value).append(CRLF).flush();
			}

		}

		for (AHttpPostAttachment attachment : postAttachments) {
			attachment.write(boundary, out, writer);
		}
		writer.append("--" + boundary + "--").append(CRLF).flush();
	}

	private void writePostParametersAsFormUrlencoded(HttpURLConnection connection) throws UnsupportedEncodingException,
			IOException {
		writeHeader(connection, Http.REQUEST_HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded;charset="
				+ charset);
		byte[] data = Http.encodePostParametersToByteArray(postParameters, charset);
		writeHeader(connection, Http.REQUEST_HEADER_CONTENT_LENGTH, String.valueOf(data.length));
		if (HttpSession.debug) log.debug("  Writing data:", new String(data, charset));
		connection.getOutputStream().write(data);
	}

	private void writeHeader(HttpURLConnection connection, String name, String value) {
		if (HttpSession.debug) log.debug("  Writing header >", name, ": ", value);
		connection.setRequestProperty(name, value);
	}

	private void writeCookies(HttpURLConnection connection) {
		Collection<HttpCookie> cookies = session.getCookies();
		if (cookies.isEmpty()) return;
		StringBuilder sb = new StringBuilder();
		for (HttpCookie cookie : cookies) {
			if (sb.length() > 0) sb.append("; ");
			sb.append(cookie.getName() + "=" + cookie.getValue());
		}
		writeHeader(connection, Http.REQUEST_HEADER_COOKIE, sb.toString());
	}

	private void writeHeaders(HttpURLConnection connection) {
		if (headers == null) return;
		for (HttpRequestHeader header : headers) {
			writeHeader(connection, header.name, header.value);
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

	public HttpRequest setPostParameters(Map<String, String> parameters) {
		if (parameters == null || parameters.isEmpty()) return this;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			setPostParameter(entry.getKey(), entry.getValue());
		}
		return this;
	}

	public HttpRequest setMethod(Method method) {
		this.method = method;
		return this;
	}

	public HttpRequest addHeader(String name, String value) {
		if (headers == null) headers = new ArrayList<HttpRequestHeader>();
		headers.add(new HttpRequestHeader(name, value));
		return this;
	}

	public HttpRequest addPostAttachment(String parameterName, File file) {
		return addPostAttachment(new FileHttpPostAttachment(parameterName, file));
	}

	public HttpRequest addPostAttachment(AHttpPostAttachment attachment) {
		if (postAttachments == null) postAttachments = new ArrayList<AHttpPostAttachment>();
		postAttachments.add(attachment);
		return this;
	}

	public HttpRequest setSession(HttpSession session) {
		this.session = session;
		return this;
	}

	public HttpSession getSession() {
		return session;
	}

	@Override
	public String toString() {
		return method + " " + url;
	}

	private class HttpRequestHeader {

		private String name;
		private String value;

		public HttpRequestHeader(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

	}

}
