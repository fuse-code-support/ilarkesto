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
package ilarkesto.tools.picturemanager;

import ilarkesto.core.fp.AStream;
import ilarkesto.core.fp.ArrayStream;
import ilarkesto.core.fp.Function;
import ilarkesto.core.fp.Predicate;
import ilarkesto.core.logging.Log;

import java.io.File;

public class Repository {

	private static final Log log = Log.get(Repository.class);

	private File dir;

	public Repository(File dir) {
		super();
		this.dir = dir;
	}

	public static Repository createTestDummy() {
		return new Repository(new File("/home/witek/inbox/photos/picturemanager-repo"));
	}

	public File getDir() {
		return dir;
	}

	public AStream<Folder> folders() {
		return new ArrayStream<File>(dir.listFiles()).filter(prDirs).map(fnFileToFolder);
	}

	private Function<File, Folder> fnFileToFolder = new Function<File, Folder>() {

		@Override
		public Folder eval(File e) {
			return new Folder(Repository.this, e.getName());
		}
	};

	private Predicate<File> prDirs = new Predicate<File>() {

		@Override
		public boolean test(File e) {
			return e.isDirectory();
		}
	};
}
