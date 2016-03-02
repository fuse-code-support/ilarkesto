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
package ilarkesto.io;

import ilarkesto.core.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirChangeState {

	private static final Log log = Log.get(DirChangeState.class);

	private File root;
	private WatchService watcher;

	public DirChangeState(File root) {
		super();
		this.root = root;

		try {
			watcher = FileSystems.getDefault().newWatchService();
			NIO.registerDirectoryTreeForAllChanges(watcher, root);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean isChanged() {
		WatchKey key = watcher.poll();

		if (key == null) return false;

		reset(key);

		return true;
	}

	private void reset(WatchKey key) {
		key.pollEvents();
		if (!key.reset()) log.warn("key.reset() returned false for watcher on", root.getAbsolutePath());
		try {
			NIO.registerDirectoryTreeForAllChanges(watcher, root);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
