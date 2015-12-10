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

import java.util.List;

public class HttpResponseHeader {

	private String name;
	private List<String> value;

	public HttpResponseHeader(String name, List<String> value) {
		super();
		this.name = name;
		this.value = value;
	}

	public List<String> getValue() {
		return value;
	}

	public String getFirstValue() {
		if (value == null || value.isEmpty()) return null;
		return value.get(0);
	}

	@Override
	public String toString() {
		String sValue = value.size() == 1 ? value.get(0) : Str.format(value);
		return name + ": " + sValue;
	}

}
