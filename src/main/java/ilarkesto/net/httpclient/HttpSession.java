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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpSession {

	private static Log log = Log.get(HttpSession.class);

	public static HttpCache defaultCache;
	private static SSLSocketFactory ignorantSslSocketFactory;

	static HttpSession defaultSession = new HttpSession();
	public static boolean debug = false;

	private final Map<String, HttpCookie> cookies = new HashMap<String, HttpCookie>();
	private final int redirectCount = 3;

	private String charset;
	HttpCache cache = defaultCache;
	private boolean sslCheckDisabled;

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
		if (sslCheckDisabled) request.setSslCheckDisabled(sslCheckDisabled);
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

	public HttpSession setSslCheckDisabled(boolean sslCheckDisabled) {
		this.sslCheckDisabled = sslCheckDisabled;
		return this;
	}

	public HttpSession disableSsl() {
		return setSslCheckDisabled(true);
	}

	public boolean isSslCheckDisabled() {
		return sslCheckDisabled;
	}

	public static SSLSocketFactory getIgnorantSslSocketFactory() {
		if (ignorantSslSocketFactory == null) ignorantSslSocketFactory = createIgnorantSslSocketFactory();
		return ignorantSslSocketFactory;
	}

	public static SSLSocketFactory createIgnorantSslSocketFactory() {
		TrustManager[] byPassTrustManagers = new TrustManager[] { new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) {}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) {}
		} };
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
		try {
			sslContext.init(null, byPassTrustManagers, new SecureRandom());
		} catch (KeyManagementException ex) {
			throw new RuntimeException(ex);
		}
		return sslContext.getSocketFactory();
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
