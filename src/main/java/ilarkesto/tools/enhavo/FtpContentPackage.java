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

import ilarkesto.integration.ftp.FtpClient;

import java.io.File;

public class FtpContentPackage implements ContentPackage {

	private FtpClient ftp;
	private String remotePath;

	public FtpContentPackage(FtpClient ftp, String basePath) {
		super();
		this.ftp = ftp;
		this.remotePath = basePath;
	}

	@Override
	public void put(String path, Object object) {
		if (object == null) return;

		if (object.equals(DIRECTORY)) {
			ftp.createDir(path);
			return;
		}

		if (object instanceof File) {
			ftp.uploadFile(getPath(path), (File) object);
			return;
		}

		ftp.uploadText(getPath(path), object.toString());
	}

	private String getPath(String path) {
		if (remotePath == null) return path;
		return remotePath + "/" + path;
	}

}
