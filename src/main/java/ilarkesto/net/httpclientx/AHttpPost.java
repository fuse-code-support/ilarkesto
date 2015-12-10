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
package ilarkesto.net.httpclientx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.Map;

public abstract class AHttpPost extends AHttpRequest {

	private String requestContentEncoding;
	private StringBuilder postData;

	protected abstract void writeRequestParameters();

	@Override
	protected void writeRequest() {
		requestContentEncoding = getRequestContentEncoding();

		postData = new StringBuilder();
		writeRequestParameters();

		byte[] postDataBytes;
		try {
			postDataBytes = postData.toString().getBytes(requestContentEncoding);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}

		try {
			connection.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			throw new HttpException("Setting request method to POST failed", ex);
		}
		connection.setRequestProperty("Content-Type", getRequestContentType());
		connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		connection.setDoOutput(true);

		try {
			connection.getOutputStream().write(postDataBytes);
		} catch (IOException ex) {
			throw new HttpException("Writing POST data failed.", ex);
		}
	}

	protected void writeRequestParameters(Map<String, String> parameters) {
		if (parameters == null || parameters.isEmpty()) return;
		for (Map.Entry<String, String> param : parameters.entrySet()) {
			String name = param.getKey();
			String value = param.getValue();
			writeRequestParameter(name, value);
		}
	}

	protected void writeRequestParameter(String name, String value) {
		if (postData.length() != 0) {
			postData.append('&');
		}
		try {
			postData.append(URLEncoder.encode(name, requestContentEncoding));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		postData.append('=');
		try {
			postData.append(URLEncoder.encode(String.valueOf(value), requestContentEncoding));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected String getRequestContentType() {
		return "application/x-www-form-urlencoded";
	}

	protected String getRequestContentEncoding() {
		return "UTF-8";
	}

}
