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
package ilarkesto.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class Http {

	public static final String REQUEST_HEADER_ACCEPT_CHARSET = "Accept-Charset";
	public static final String REQUEST_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String REQUEST_HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String REQUEST_HEADER_COOKIE = "Cookie";
	public static final String REQUEST_HEADER_IF_NONE_MATCH = "If-None-Match";
	public static final String REQUEST_HEADER_ETAG = "ETag";

	public static final String RESPONSE_HEADER_SET_COOKIE = "Set-Cookie";
	public static final String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String RESPONSE_HEADER_LOCATION = "Location";

	public static final int RESPONSE_SC_CONTINUE = 100;
	public static final int RESPONSE_SC_OK = 200;
	public static final int RESPONSE_SC_NOT_MODIFIED = 304;
	public static final int RESPONSE_SC_MOVED_PERMANENTLY = 301; // redirect
	public static final int RESPONSE_SC_FOUND = 302; // found
	public static final int RESPONSE_SC_NOT_FOUND = 404;

	public static String encodePostParametersToString(Map<String, String> parameters, String encoding) {
		StringBuilder postData = new StringBuilder();

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (postData.length() != 0) {
				postData.append('&');
			}
			try {
				postData.append(URLEncoder.encode(entry.getKey(), encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
			postData.append('=');
			try {
				postData.append(URLEncoder.encode(String.valueOf(entry.getValue()), encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
		}

		return postData.toString();
	}

	public static byte[] encodePostParametersToByteArray(Map<String, String> parameters, String encoding) {
		try {
			return encodePostParametersToString(parameters, encoding).toString().getBytes(encoding);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

}
