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

public class HttpCookie {

	private String name;
	private String value;

	public HttpCookie(String s) {
		int idx = s.indexOf(';');
		if (idx > 0) s = s.substring(0, idx);
		idx = s.indexOf('=');
		name = s.substring(0, idx).trim();
		value = s.substring(idx + 1).trim();
		Log.TEST(">>>>>>> cookie >>>", name, ">>>", value);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}

}
