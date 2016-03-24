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

import ilarkesto.core.base.Str;
import ilarkesto.integration.BeanshellExecutor;
import ilarkesto.io.FileNameComparator;
import ilarkesto.io.IO;
import ilarkesto.json.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilesContentProvider extends AContentProvider {

	private File dir;
	private BeanshellExecutor beanshellExecutor;

	public FilesContentProvider(File dir, ContentProvider fallback) {
		super(fallback);
		this.dir = dir;
	}

	@Override
	protected Object onGet(String key) {
		File file = new File(dir.getPath() + "/" + Str.securePath(key));
		if (!file.exists()) return null;

		return createFromFile(file);
	}

	private Object createFromFile(File file) {
		if (file.isDirectory()) return createFromDir(file);

		String name = file.getName();

		if (name.endsWith(".json")) {
			JsonObject json = JsonObject.loadFile(file, false);
			appendFilename(json, file);
			return json;
		}

		if (name.endsWith(".bs")) return executeBeanshellScript(file);

		return IO.readFile(file);
	}

	private Object executeBeanshellScript(File file) {
		String script = IO.readFile(file);
		if (Str.isBlank(script)) return null;

		if (beanshellExecutor == null) beanshellExecutor = new BeanshellExecutor();

		return beanshellExecutor.executeScript(script);
	}

	private Object createFromDir(File dir) {
		if (dir.getName().endsWith(".list")) return createListFromDir(dir);
		if (dir.getName().endsWith(".struct")) return createStructFromDir(dir);
		return createJsonFromDir(dir);
	}

	private Object createJsonFromDir(File dir) {
		JsonObject ret = new JsonObject();
		appendFilename(ret, dir);
		for (File file : listFilesInOrder(dir)) {
			Object object = createFromFile(file);
			String property = file.getName();
			Str.removeSuffixStartingWithLast(property, ".");
			ret.put(property, object);
		}
		return ret;
	}

	private void appendFilename(JsonObject json, File file) {
		String name = file.getName();
		json.put("$filename", name);
		name = Str.removeSuffixStartingWith(name, ".");
		json.put("$filename.cleaned", name);
	}

	private List createListFromDir(File dir) {
		ArrayList ret = new ArrayList();
		for (File file : listFilesInOrder(dir)) {
			ret.add(createFromFile(file));
		}
		return ret;
	}

	private JsonObject createStructFromDir(File dir) {
		File structFile = new File(dir + "/struct.json");
		JsonObject ret = structFile.exists() ? JsonObject.loadFile(structFile) : new JsonObject();
		appendFilename(ret, dir);
		for (File file : listFilesInOrder(dir)) {
			String name = file.getName();
			if (name.equals("struct.json")) continue;
			name = Str.removeSuffix(name, ".txt");
			name = Str.removeSuffix(name, ".html");
			name = Str.removeSuffix(name, ".json");
			name = Str.removeSuffix(name, ".list");
			name = Str.removeSuffix(name, ".struct");
			ret.put(name, createFromFile(file));
		}
		return ret;
	}

	private List<File> listFilesInOrder(File dir) {
		ArrayList<File> ret = new ArrayList<File>();
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) return ret;
		for (File file : files) {
			if (file.getName().equals(".DS_Store")) continue;
			ret.add(file);
		}
		Collections.sort(ret, FileNameComparator.INSTANCE);
		return ret;
	}

	public FilesContentProvider setBeanshellExecutor(BeanshellExecutor beanshellExecutor) {
		this.beanshellExecutor = beanshellExecutor;
		return this;
	}

}
