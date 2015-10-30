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
package ilarkesto.core.base;

public class Filepath {

	private String path = "";

	public Filepath(String... pathElements) {
		super();
		append(pathElements);
	}

	public Filepath append(String... pathElements) {
		if (pathElements.length == 0) return this;
		StringBuilder sb = new StringBuilder();
		sb.append(path);
		for (String element : pathElements) {
			if (element == null) continue;
			element = element.replace('\\', '/');
			if (Str.isBlank(element)) continue;
			if (sb.length() > 0) sb.append("/");
			sb.append(element);
		}
		path = sb.toString();
		return this;
	}

	public String getLastElementName() {
		String ret = path;
		while (ret.endsWith("/"))
			ret = ret.substring(0, ret.length() - 1);
		if (ret.isEmpty()) return null;
		int idx = ret.lastIndexOf('/');
		if (idx < 0) return ret;
		ret = ret.substring(idx + 1);
		if (ret.isEmpty()) return null;
		return ret;
	}

	public String getParentAsString() {
		String ret = path;
		while (ret.endsWith("/"))
			ret = ret.substring(0, ret.length() - 1);
		int idx = ret.lastIndexOf('/');
		if (idx < 1) return null;
		return ret.substring(0, idx);
	}

	public Filepath getParent() {
		String parent = getParentAsString();
		return parent == null ? null : new Filepath(parent);
	}

	@Override
	public String toString() {
		return path;
	}

}
