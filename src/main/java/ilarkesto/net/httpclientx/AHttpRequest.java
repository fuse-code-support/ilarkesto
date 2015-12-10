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

import ilarkesto.io.IO;
import ilarkesto.net.Http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AHttpRequest {

	protected HttpURLConnection connection;

	protected abstract String createUri();

	protected abstract void writeRequest();

	protected abstract void handleResponse();

	private int responseCode;

	protected String readResponseText(String charset) {
		InputStream is;
		try {
			is = connection.getInputStream();
		} catch (IOException ex) {
			throw new HttpException("Reading response failed.", ex);
		}
		return IO.readToString(is, charset);
	}

	public final AHttpRequest execute() {

		String uri = createUri();

		URL url;
		try {
			url = new URL(uri);
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Malformed URL: " + uri, ex);
		}

		try {
			connection = (HttpURLConnection) url.openConnection();
			writeRequest();

			responseCode = connection.getResponseCode();
			handleResponse(responseCode);

		} catch (Exception ex) {
			throw new HttpException("HTTP request failed: " + url, ex);
		} finally {
			if (connection != null) connection.disconnect();
		}

		return this;
	}

	protected void handleResponse(int responseCode) {
		if (responseCode == Http.RESPONSE_SC_OK) {
			handleResponse();
			return;
		}

		throw new HttpException("Unexpected HTTP response code: " + responseCode, null);
	}

	protected int getResponseCode() {
		return responseCode;
	}

}
