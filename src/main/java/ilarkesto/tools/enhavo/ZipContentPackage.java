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

import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipContentPackage extends AContentPackage {

	private static final Log log = Log.get(ZipContentPackage.class);

	private ZipOutputStream zipOs;

	public ZipContentPackage(OutputStream os) {
		super();
		this.zipOs = new ZipOutputStream(new BufferedOutputStream(os));
	}

	@Override
	public void put(String path, Object object) {

		if (object == null) return;

		if (object instanceof File) {
			try {
				addFile((File) object, path);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			return;
		}

		try {
			addText(object.toString(), path);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void addText(String text, String path) throws IOException {
		if (!path.contains(".")) path += ".json";
		log.debug("ZipEntry:", path);
		ZipEntry e = new ZipEntry(path);
		zipOs.putNextEntry(e);
		IO.writeText(zipOs, text, IO.UTF_8);
		zipOs.closeEntry();
	}

	private void addFile(File file, String path) throws IOException {
		if (!file.exists()) return;
		log.debug("ZipEntry:", path);
		ZipEntry e = new ZipEntry(path);
		zipOs.putNextEntry(e);
		IO.copyData(file, zipOs);
		zipOs.closeEntry();
	}

	public void close() {
		try {
			zipOs.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
