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
package ilarkesto.webapp;

import ilarkesto.base.Sys;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Date;
import ilarkesto.integration.itext.PdfBuilder;
import ilarkesto.io.IO;
import ilarkesto.json.JsonObject;
import ilarkesto.ui.html.Html;
import ilarkesto.ui.web.HtmlBuilder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RequestWrapper<S extends AWebSession> {

	private static Log log = Log.get(RequestWrapper.class);

	private HttpServletRequest request;
	private HttpServletResponse response;
	private S session;
	private boolean responseServed;

	public RequestWrapper(HttpServletRequest request, HttpServletResponse response) {
		super();
		this.request = request;
		this.response = response;

		setRequestEncoding(IO.UTF_8);
	}

	// --- response helpers ---

	public void serve(File file) {
		serve(file, true, true);
	}

	public void serve(File file, boolean setFilename, boolean enableCaching) {
		Servlet.serveFile(file, request, response, setFilename, enableCaching);
		responseServed = true;
	}

	public void write(PdfBuilder pdf) {
		try {
			pdf.write(response.getOutputStream());
		} catch (IOException ex) {
			throw new RuntimeException("Writing PDF failed", ex);
		}
		responseServed = true;
	}

	public void write(JsonObject json) {
		if (Sys.isDevelopmentMode()) {
			write(json.toFormatedString());
		} else {
			write(json.toString());
		}
		responseServed = true;
	}

	public void write(File file) {
		IO.copyData(file, getOutputStream());
	}

	public void writeWithContentType(File file) {
		setContentType(Servlet.getMimeType(file));
		write(file);
	}

	public void write(byte[] data) {
		try {
			getOutputStream().write(data);
		} catch (IOException ex) {
			throw new RuntimeException("Writing response data failed", ex);
		}
		responseServed = true;
	}

	public void write(String s) {
		try {
			response.getWriter().print(s);
		} catch (IOException ex) {
			throw new RuntimeException("Writing response failed: " + this, ex);
		}
		responseServed = true;
	}

	public void setFilename(String filename) {
		Servlet.setFilename(filename, response);
	}

	public void setContentTypeRss() {
		setContentType("application/rss+xml");
	}

	public void setContentTypeCss() {
		setContentType("text/css");
	}

	public void setContentTypeText() {
		setContentType("text/plain");
	}

	public void setContentTypeJson() {
		setContentType("application/json");
	}

	public void setContentTypeHtml() {
		setContentType("text/html");
	}

	public void setContentTypePdf() {
		setContentType("application/pdf");
	}

	public void setCookie(String name, String value, int maxAgeInSeconds) {
		Servlet.setCookie(response, name, value, maxAgeInSeconds);
	}

	public void removeCookie(String name) {
		Servlet.removeCookie(response, name);
	}

	public void setContentType(String type) {
		if (type == null) return;
		response.setContentType(type);
	}

	public void setCharacterEncoding(String charset) {
		if (charset == null) return;
		response.setCharacterEncoding(charset);
	}

	public void setCharacterEncodingUtf8() {
		setCharacterEncoding(IO.UTF_8);
	}

	public void sendRedirect(String url) {
		log.debug("Sending redirect:", url);
		try {
			response.sendRedirect(url);
		} catch (IOException ex) {
			throw new RuntimeException("Redirecting failed", ex);
		}
		responseServed = true;
	}

	public void sendErrorForbidden() {
		sendError(HttpServletResponse.SC_FORBIDDEN, null);
	}

	public void sendErrorNotFound() {
		sendError(HttpServletResponse.SC_NOT_FOUND, null);
	}

	public void sendErrorNoContent() {
		sendError(HttpServletResponse.SC_NO_CONTENT, null);
	}

	public void sendErrorServiceUnavailable(String message) {
		sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, message);
	}

	public void sendErrorInternal(String message) {
		sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
	}

	private void sendError(int errorCode, String message) {
		Servlet.sendError(response, errorCode, message);
		responseServed = true;
	}

	public void preventCaching() {
		Servlet.setHeadersForNoCaching(response);
	}

	// --- request helpers ---

	public void setRequestEncoding(String charset) {
		try {
			request.setCharacterEncoding(charset);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Setting request character encoding failed: " + charset, ex);
		}
	}

	public String getCookie(String name) {
		return Servlet.getCookieValue(request, name);
	}

	public JsonObject readContentToJson() {
		String s = readContentToString();
		if (Str.isBlank(s)) throw new RuntimeException("Illegal request. Missing JSON content.");
		return JsonObject.parse(s);
	}

	public String readContentToString() {
		return Servlet.readContentToString(request);
	}

	public String getUri() {
		return request.getRequestURI();
	}

	public String getUriWithoutContext() {
		return Servlet.getUriWithoutContext(request);
	}

	public String getUrl() {
		return request.getRequestURL().toString();
	}

	public String getBaseUrl() {
		return Servlet.getBaseUrl(request);
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	// --- request parameters ---

	public Date getDate(String name) {
		String value = get(name);
		return value == null ? null : new Date(value);
	}

	public String get(String name) {
		return request.getParameter(name);
	}

	public String get(String name, String defaultValue) {
		String value = get(name);
		return value == null ? defaultValue : value;
	}

	public String getMandatory(String name) {
		String value = get(name);
		if (value == null) throw new RuntimeException("Missing mandatory request parameter: " + name);
		return value;
	}

	public Map<String, String> getParametersAsMap() {
		return Servlet.getParametersAsMap(request);
	}

	// --- session attributes ---

	public String getSessionAttributeAsString(String name) {
		return (String) getSessionAttribute(name);
	}

	public Object getSessionAttribute(String name) {
		return getHttpSession().getAttribute(name);
	}

	public void setSessionAttribute(String name, Object value) {
		getHttpSession().setAttribute(name, value);
	}

	// --- ---

	public boolean isResponseServed() {
		return responseServed;
	}

	public void setResponseServed(boolean responseServed) {
		this.responseServed = responseServed;
	}

	public OutputStream getOutputStream() {
		responseServed = true;
		try {
			return response.getOutputStream();
		} catch (IOException ex) {
			throw new RuntimeException("Writing response failed", ex);
		}
	}

	public PrintWriter getWriter() {
		responseServed = true;
		try {
			return response.getWriter();
		} catch (IOException ex) {
			throw new RuntimeException("Writing response failed", ex);
		}
	}

	public HttpSession getHttpSession() {
		return request.getSession();
	}

	public S getSession() {
		if (session == null) session = (S) AWebApplication.get().getWebSession(request);
		return session;
	}

	public HttpServletRequest getHttpRequest() {
		return request;
	}

	public HttpServletResponse getHttpResponse() {
		return response;
	}

	@Override
	public String toString() {
		return getUriWithoutContext();
	}

	public void send(Html html) {
		setContentTypeHtml();
		html.build(new HtmlBuilder(getWriter(), IO.UTF_8));
	}

}
