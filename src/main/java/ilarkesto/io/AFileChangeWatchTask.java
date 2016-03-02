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

import ilarkesto.concurrent.ALoopTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public abstract class AFileChangeWatchTask extends ALoopTask {

	private File root;
	private WatchService watcher;
	private long sleepUntilChangeNotivication = 1000;

	protected abstract void onChange();

	public AFileChangeWatchTask(File root) {
		this.root = root;

		try {
			watcher = FileSystems.getDefault().newWatchService();
			NIO.registerDirectoryTreeForAllChanges(watcher, root);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		log.info("Watching", root.getAbsolutePath());
	}

	protected void onFirstChange() {
		onChange();
	}

	@Override
	protected void beforeLoop() throws InterruptedException {
		onFirstChange();
	}

	@Override
	protected void iteration() throws InterruptedException {
		WatchKey key = watcher.take();
		reset(key);

		log.info("CHANGE");

		if (sleepUntilChangeNotivication > 0) {
			sleep(sleepUntilChangeNotivication);
			key = watcher.poll();
			if (key != null) {
				reset(key);
			}
		}

		if (isAbortRequested()) return;

		onChange();
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
