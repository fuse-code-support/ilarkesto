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

import java.io.File;

public class Folder implements PicturesProvider {

	private Repository repository;
	private String filename;

	public Folder(Repository repository, String filename) {
		super();
		this.repository = repository;
		this.filename = filename;
	}

	@Override
	public AStream<Picture> pictures() {
		return new ArrayStream<File>(getDir().listFiles()).filter(prPictureFiles).map(fnFileToPicture);
	}

	public File getDir() {
		return new File(repository.getDir().getPath() + "/" + filename);
	}

	private Function<File, Picture> fnFileToPicture = new Function<File, Picture>() {

		@Override
		public Picture eval(File e) {
			return new Picture(Folder.this, e.getName());
		}
	};

	private Predicate<File> prPictureFiles = new Predicate<File>() {

		@Override
		public boolean test(File e) {
			if (!e.isFile()) return false;
			return true;
		}
	};
}
