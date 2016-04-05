/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.fp;

import ilarkesto.core.base.Tuple;
import ilarkesto.core.fp.Function;
import ilarkesto.core.fp.Predicate;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class FP extends ilarkesto.core.fp.FP {

	public static <I, K, V> Map<K, V> map(Collection<I> elements, Function<I, Tuple<K, V>> mapFunction) {
		Map<K, V> ret = new HashMap<K, V>();
		for (I e : elements) {
			Tuple<K, V> keyValue = mapFunction.eval(e);
			ret.put(keyValue.getA(), keyValue.getB());
		}
		return ret;
	}

	// --- functions / predicates ---

	public static final Function<File, String> FILE_PATH = new Function<File, String>() {

		@Override
		public String eval(File e) {
			return e.getPath();
		}

	};

	public static List<String> filePaths(Collection<File> files) {
		return foreach(files, FILE_PATH);
	}

	public static final Predicate<File> FILE_EXISTS = new Predicate<File>() {

		@Override
		public boolean test(File e) {
			return e.exists();
		}

	};

	public static List<File> existingFilesList(Collection<File> files) {
		return filterList(FILE_EXISTS, files);
	}

	public static Set<File> existingFilesSet(Collection<File> files) {
		return filterSet(FILE_EXISTS, files);
	}

	public static List<File> filterFilesList(Collection<File> files, final FileFilter filter) {
		return FP.filterList(new Predicate<File>() {

			@Override
			public boolean test(File file) {
				return filter.accept(file);
			}

		}, files);
	}

}
