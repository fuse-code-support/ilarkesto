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

import ilarkesto.base.CommandLineArgs;

public class EnhavoCommandLineArgs {

	private String path;
	private boolean loop;
	private boolean watch;
	private boolean toMany;

	public EnhavoCommandLineArgs(String[] args) {
		CommandLineArgs cla = new CommandLineArgs(args);

		path = cla.popParameter();
		loop = cla.popFlag("loop");
		watch = cla.popFlag("watch");
		toMany = cla.containsAny();
	}

	public boolean isWatch() {
		return watch;
	}

	public boolean isLoop() {
		return loop;
	}

	public String getPath() {
		return path;
	}

	public boolean isToMany() {
		return toMany;
	}
}
