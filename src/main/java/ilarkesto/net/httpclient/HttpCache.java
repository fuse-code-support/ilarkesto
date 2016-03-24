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
import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;
import ilarkesto.json.JsonObject;
import ilarkesto.net.Http;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

public class HttpCache {

	private static final Log log = Log.get(HttpCache.class);

	private int maxFileSize = 1000000;
	private int maxCacheSize = 23000000;
	private File cacheDir;
	private long cacheSize = -1;

	public HttpCache(File cacheDir) {
		super();
		this.cacheDir = cacheDir;
	}

	public File cache(HttpResponse resp) throws IOException {
		String url = resp.request.url;
		if (url.length() > 127) return null;

		String etag = resp.getHeaderValue(Http.REQUEST_HEADER_ETAG);
		if (etag == null) return null;

		long contenLength = resp.getContentLength();
		if (contenLength > maxFileSize) return null;

		cleanup();

		return cache(url, etag, resp.getConnectionInputStream());
	}

	private File cache(String url, String etag, InputStream is) {
		log.info("Caching", url);
		initCacheSize();

		File metaFile = new File(path(url) + ".json");
		JsonObject jMeta = new JsonObject();
		jMeta.put("ETag", etag);

		File cachedFile = new File(path(url));
		if (cachedFile.exists()) cacheSize -= cachedFile.length();
		IO.copyDataToFile(is, cachedFile);
		cacheSize += cachedFile.length();

		IO.writeFile(metaFile, jMeta.toFormatedString(), IO.UTF_8);

		return cachedFile;
	}

	public String getCachedFileEtag(String url) {
		File metaFile = new File(path(url) + ".json");
		if (!metaFile.exists()) return null;
		return new JsonObject(IO.readFile(metaFile, IO.UTF_8)).getString("ETag");
	}

	public File getCachedFile(String url) {
		File file = new File(path(url));
		if (file.exists()) {
			log.debug("Cached file:", file.getAbsolutePath());
		}
		return file;
	}

	private String path(String url) {
		return cacheDir.getPath() + "/" + urlToFilename(url);
	}

	private String urlToFilename(String url) {
		return Str.toFileCompatibleString(url, "-") + ".dat";
	}

	private void cleanup() {
		initCacheSize();
		if (cacheSize < maxCacheSize) return;

		for (File file : Utl.sort(IO.listFiles(cacheDir, cacheFilesFilter), chacheFilesComparator)) {
			cacheSize -= file.length();
			file.delete();
			new File(file.getPath() + ".json").delete();
			if (cacheSize < maxCacheSize) return;
		}
	}

	private void initCacheSize() {
		if (cacheSize > -1) return;
		cacheSize = IO.getSize(cacheDir);
	}

	private static final FileFilter cacheFilesFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) return false;
			if (!file.getName().endsWith(".dat")) return false;
			return true;
		}
	};

	private static final Comparator<File> chacheFilesComparator = new Comparator<File>() {

		@Override
		public int compare(File a, File b) {
			return Utl.compare(a.lastModified(), b.lastModified());
		}

	};

}
