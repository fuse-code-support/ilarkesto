package ilarkesto.net;

import ilarkesto.base.Reflect;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HttpDownloader {

	public static Class<? extends HttpDownloader> defaultType = ApacheHttpDownloader.class;

	private static final Log log = Log.get(HttpDownloader.class);

	private static final int DEFAULT_REDIRECTS = 3;

	private String username;
	private String password;
	private String baseUrl;

	private boolean sslServerCheckingDisabled = true;

	private Map<String, String> cookieStore = new HashMap<String, String>();

	public HttpDownloader() {}

	public static HttpDownloader create() {
		return Reflect.newInstance(defaultType);
	}

	public void setSslServerCheckingDisabled(boolean sslVerificationDisabled) {
		this.sslServerCheckingDisabled = sslVerificationDisabled;
	}

	public boolean isSslServerCheckingDisabled() {
		return sslServerCheckingDisabled;
	}

	public boolean isInternetAvailable() {
		return true;
	}

	public String post(String url, Map<String, String> parameters, String charset) {
		return post(url, parameters, null, charset);
	}

	public String post(String url, Map<String, String> parameters, Map<String, String> requestHeaders, String charset) {
		if (requestHeaders != null && !requestHeaders.isEmpty())
			throw new IllegalArgumentException("request headers not supported with " + getClass().getName());
		HttpURLConnection connection;
		try {
			connection = IO.post(new URL(url), parameters, charset, null, null);
		} catch (Exception ex) {
			throw new RuntimeException("HTTP POST failed.", ex);
		}
		int responseCode;
		String responseMessage;
		try {
			responseCode = connection.getResponseCode();
			responseMessage = connection.getResponseMessage();
		} catch (IOException ex) {
			throw new RuntimeException("HTTP POST failed.", ex);
		}
		if (responseCode != HttpURLConnection.HTTP_OK)
			throw new RuntimeException("HTTP response code not OK: " + responseCode + " " + responseMessage);

		Map<String, List<String>> headers = connection.getHeaderFields();
		// for (Entry<String, List<String>> headerEntry : headers.entrySet()) {
		// String name = headerEntry.getKey();
		// List<String> values = headerEntry.getValue();
		// log.debug("HTTP response header:", name, values);
		// }
		List<String> cookieStrings = headers.get("Set-Cookie");
		if (cookieStrings != null) {
			for (String s : cookieStrings) {
				s = s.substring(0, s.indexOf(';'));
				int idx = s.indexOf('=');
				String name = s.substring(0, idx);
				String value = s.substring(idx + 1, s.length() - 1);
				cookieStore.put(name, value);
				log.info("Cookie stored:", name, "=", value);
			}
		}

		String contentEncoding = connection.getContentEncoding();
		if (contentEncoding == null) contentEncoding = IO.UTF_8;
		try {
			return IO.readToString(connection.getInputStream(), contentEncoding);
		} catch (IOException ex) {
			throw new RuntimeException("HTTP POST failed.", ex);
		}
	}

	public final void downloadUrlToFile(String url, File file) {
		downloadUrlToFile(url, file, DEFAULT_REDIRECTS);
	}

	public void downloadUrlToFile(String url, File file, int followRedirects) {
		IO.downloadUrlToFile(url, file.getPath());
	}

	public void downloadZipAndExtract(String url, File dir) {
		url = getFullUrl(url);
		log.info(url);
		InputStream is = null;
		ZipInputStream zis = null;
		try {
			is = IO.openUrlInputStream(url, username, password);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			while (true) {
				try {
					ze = zis.getNextEntry();
				} catch (IOException ex) {
					throw new RuntimeException("Extracting zip file failed: " + url, ex);
				}
				if (ze == null) break;
				File file = new File(dir.getPath() + "/" + ze.getName());
				IO.createDirectory(file.getParentFile());
				BufferedOutputStream out;
				try {
					out = new BufferedOutputStream(new FileOutputStream(file));
				} catch (FileNotFoundException ex) {
					throw new RuntimeException("Writing file failed:" + file, ex);
				}
				IO.copyData(zis, out);
				IO.close(out);
			}
		} finally {
			IO.closeQuiet(zis);
			IO.closeQuiet(is);
		}
	}

	public final String downloadText(String url, String charset) {
		return downloadText(url, charset, DEFAULT_REDIRECTS);
	}

	public String downloadText(String url, String charset, int followRedirects) throws HttpRedirectException {
		url = getFullUrl(url);
		log.info(url);
		return IO.downloadUrlToString(url, charset, username, password);
	}

	public final String getBaseUrl(String url) {
		if (url == null) return null;
		int offset = 0;
		if (url.contains("//")) offset = url.indexOf("//") + 2;
		int idx = url.indexOf("/", offset);
		if (idx < 0) return url;
		return url.substring(0, idx);
	}

	public final String getFullUrl(String url) {
		return getFullUrl(url, baseUrl);
	}

	public final String getFullUrl(String url, String baseUrl) {
		if (baseUrl == null) return url;
		if (Str.isLink(url)) return url;
		return baseUrl + url;
	}

	public HttpDownloader setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public HttpDownloader setAuthentication(String username, String password) {
		this.username = username;
		this.password = password;
		return this;
	}

	// ---

	public static class HttpRedirectException extends RuntimeException {

		public HttpRedirectException(String location) {
			super(location);
		}

		public String getLocation() {
			return getMessage();
		}

	}

	public void upload(String url, File file, Map<String, String> map, Object object, String charset) {
		throw new RuntimeException("Not implemented: upload()");
	}

}
