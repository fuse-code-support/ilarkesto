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

public abstract class AFileChangeWatchTask extends ALoopTask {

	private File root;
	private long minSleep;
	private long maxSleep;
	private float sleepIncrementFactor = 1.02f;

	private int directoryDepthLimit = -1;

	private DirChangeState changeState;
	private long sleep;

	protected abstract void onChange();

	public AFileChangeWatchTask(File root, long minSleep, long maxSleep) {
		super();
		this.root = root;
		this.minSleep = minSleep;
		this.maxSleep = maxSleep;

		sleep = minSleep;
	}

	protected void onFirstChange() {
		onChange();
	}

	@Override
	protected void beforeLoop() throws InterruptedException {
		changeState = new DirChangeState(root);
		if (directoryDepthLimit >= 0) changeState.setDirectoryDepthLimit(directoryDepthLimit);
		onFirstChange();
	}

	@Override
	protected void iteration() throws InterruptedException {
		if (!changeState.isChanged()) {
			sleep = Math.min(maxSleep, (long) (sleep * sleepIncrementFactor));
			return;
		}

		sleep = minSleep;
		onChange();
		changeState.reset();
	}

	@Override
	protected long getSleepTimeBetweenIterations() {
		return sleep;
	}

	public AFileChangeWatchTask setDirectoryDepthLimit(int directoryDepthLimit) {
		this.directoryDepthLimit = directoryDepthLimit;
		return this;
	}

}
