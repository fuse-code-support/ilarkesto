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

import ilarkesto.io.IO;

import java.io.File;
import java.io.OutputStream;
import java.net.URLConnection;

public class FileHttpPostAttachment extends ABinaryHttpPostAttachment {

	private File file;
	private String filename;
	private String contentType;

	public FileHttpPostAttachment(String name, File file) {
		super(name);
		this.file = file;

		filename = file.getName();
		contentType = URLConnection.guessContentTypeFromName(file.getName());
	}

	@Override
	protected void writeData(OutputStream out) {
		IO.copyData(file, out);
	}

	@Override
	protected String getContentType() {
		return contentType;
	}

	@Override
	protected String getFilename() {
		return filename;
	}

}
