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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public abstract class AHttpPostAttachment {

	private String name;

	public abstract void write(String boundary, OutputStream out, PrintWriter writer) throws IOException;

	protected abstract String getFilename();

	protected abstract String getContentType();

	public AHttpPostAttachment(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + ": " + getClass().getSimpleName();
	}

}
