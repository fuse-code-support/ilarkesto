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

import java.io.File;
import java.util.Map;

public class DirChangeState {

	private File dir;
	private int directoryDepthLimit = Integer.MAX_VALUE;
	private Map<String, Long> modificationTimes;

	public DirChangeState(File dir) {
		super();
		this.dir = dir;

		reset();
	}

	public boolean checkAndReset() {
		boolean changed = isChanged();
		if (changed) reset();
		return changed;
	}

	public boolean isChanged() {
		return isChanged(dir, directoryDepthLimit);
	}

	private boolean isChanged(File file, int depth) {
		long modificationTime = file.lastModified();
		if (modificationTime <= 0) return false; // does not exist

		Long previousModificationTime = modificationTimes.get(file.getPath());
		if (previousModificationTime == null) return true; // did not exist before
		if (previousModificationTime != modificationTime) return true;

		if (depth > 0) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null) {
					for (File f : files) {
						if (isChanged(f, depth - 1)) return true;
					}
				}
			}
		}

		return false;
	}

	public void reset() {
		modificationTimes = IO.getModificationTimes(dir);
	}

	public DirChangeState setDirectoryDepthLimit(int limitDirectoryDepth) {
		this.directoryDepthLimit = limitDirectoryDepth;
		return this;
	}

}
