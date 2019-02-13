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
package ilarkesto.tools.enhavo;

import ilarkesto.core.base.Str;
import ilarkesto.integration.ftp.FtpClient;
import ilarkesto.io.IO;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FtpContentPackage extends AContentPackage {

	private Map<String, Long> timings = new HashMap<String, Long>();
	private FtpClient ftp;
	private String remotePath;

	private Set<String> createdDirs = new HashSet<String>();

	private File cacheDir;

	public FtpContentPackage(FtpClient ftp, String basePath, File cacheDir) {
		super();
		this.ftp = ftp;
		this.remotePath = basePath;
		this.cacheDir = cacheDir;
	}

	@Override
	public void put(String path, Object object) {
		if (object == null) return;
		path = getPath(path);

		long start = System.currentTimeMillis();

		if (object.equals(DIRECTORY)) {
			ftp.createDir(path);
		} else if (object instanceof File) {
			createDirForFile(path);
			ftp.uploadFileIfNotThere(path, (File) object);
		} else {
			uploadText(path, object);
		}

		long runtime = System.currentTimeMillis() - start;
		timings.put(path, runtime);
	}

	protected void uploadText(String path, Object object) {
		String text = object.toString();

		if (cacheDir == null) {
			ftp.uploadText(path, text);
			return;
		}

		File cacheFile = new File(cacheDir.getPath() + "/" + Str.toFileCompatibleString(ftp.getIdentifier()) + "/"
				+ remotePath + "/" + path);
		if (!isTextSameAsCache(text, cacheFile)) {
			IO.writeFile(cacheFile, text, IO.UTF_8);
		}

		ftp.uploadFileIfNotThere(path, cacheFile);
	}

	private boolean isTextSameAsCache(String text, File cacheFile) {
		if (!cacheFile.exists()) return false;
		String cachedText = IO.readFile(cacheFile, IO.UTF_8);
		return text.equals(cachedText);
	}

	private void createDirForFile(String path) {
		int idx = path.lastIndexOf('/');
		if (idx < 1) return;
		String dirpath = path.substring(0, idx);
		if (createdDirs.contains(dirpath)) return;
		ftp.createDir(dirpath);
		createdDirs.add(dirpath);
	}

	private String getPath(String path) {
		if (remotePath == null) return path;
		return remotePath + "/" + path;
	}

	public Map<String, Long> getTimings() {
		return timings;
	}

}
